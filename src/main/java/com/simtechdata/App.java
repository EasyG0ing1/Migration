package com.simtechdata;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.simtechdata.pojo.*;
import com.simtechdata.pojo.opnsense.Opnsense;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class App {

    private static String exeFolder = System.getProperty("user.dir");
    private static Path outFile = Paths.get(exeFolder, "config_out.xml");
    private static Path inFile = Paths.get(exeFolder, "config.xml");


    public static void main(String[] args) throws Exception {

        if(args.length > 0) {
            if(args[0].toLowerCase().contains("check")) {
                check();
                System.exit(0);
            }
        }

        if(!check()) {
            System.exit(0);
        }

        String xml = Files.readString(inFile);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        List<Staticmap> staticmaps = new ArrayList<>();
        NodeList nList = doc.getElementsByTagName("staticmap");
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                Staticmap staticmap = new Staticmap();
                staticmap.setMac(element.getElementsByTagName("mac").item(0).getTextContent());
                staticmap.setIpaddr(element.getElementsByTagName("ipaddr").item(0).getTextContent());

                NodeList descrNodeList = element.getElementsByTagName("descr");
                NodeList hostnameNodeList = element.getElementsByTagName("hostname");

                if (descrNodeList.getLength() > 0) {
                    staticmap.setDescr(descrNodeList.item(0).getTextContent());
                }

                if (hostnameNodeList.getLength() > 0) {
                    staticmap.setHostname(hostnameNodeList.item(0).getTextContent());
                }

                staticmaps.add(staticmap);
            }
        }

        NodeList dhcp4NodeList = doc.getElementsByTagName("dhcp4");
        Node dhcp4Node = dhcp4NodeList.item(0);
        Element dhcp4Element = (Element) dhcp4Node;
        NodeList subnetsList = dhcp4Element.getElementsByTagName("subnets");
        Node subnetsNode = subnetsList.item(0);
        Element subnetsElement = (Element) subnetsNode;
        NodeList subnet4NodeList = subnetsElement.getElementsByTagName("subnet4");
        List<Subnet4> subnet4List = new ArrayList<>();
        for (int i = 0; i < subnet4NodeList.getLength(); i++) {
            Node subnet4Node = subnet4NodeList.item(i);
            if (subnet4Node.getNodeType() == Node.ELEMENT_NODE) {
                Element subnet4Element = (Element) subnet4Node;
                Subnet4 subnet4 = new Subnet4();
                subnet4.setSubnet(subnet4Element.getElementsByTagName("subnet").item(0).getTextContent());
                subnet4.setUuid(subnet4Element.getAttribute("uuid"));
                subnet4List.add(subnet4);
            }
        }

        List<Reservation> reservationList = new ArrayList<>();
        for (Staticmap staticMap : staticmaps) {
            Reservation reservation = new Reservation();
            String ipAddy = staticMap.getIpaddr();
            reservation.setUuid(getUUID());
            reservation.setSubnet(ipAddy);
            reservation.setHw_Address(staticMap.getMac());
            reservation.setIp_Address(staticMap.getIpaddr());
            reservation.setHostname(staticMap.getHostname());
            reservation.setDescription(staticMap.getDescr());
            reservation.setSubnet(getSubnet(ipAddy, subnet4List));
            reservationList.add(reservation);
        }


        Opnsense opnsense = new Opnsense();
        OPNsense opNsense = new OPNsense();
        Kea kea = new Kea();
        Dhcp4 dhcp4 = new Dhcp4();
        Subnets subnets = new Subnets();
        Reservations reservations = new Reservations();
        reservations.setReservation(reservationList);
        subnets.setSubnet4(subnet4List);
        dhcp4.setSubnets(subnets);
        dhcp4.setReservations(reservations);
        kea.setDhcp4(dhcp4);
        opNsense.setKea(kea);
        opnsense.setOPNsense(opNsense);
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String newXML = opnsense.toString();
        System.out.println(outFile);
        outFile.toFile().createNewFile();
        Files.writeString(outFile, newXML, Charset.defaultCharset());
    }

    private static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    private static byte[] applyMask(byte[] address, int mask) {
        int addr = ByteBuffer.wrap(address).getInt();
        return ByteBuffer.allocate(4).putInt(addr & mask).array();
    }

    private static boolean ipInSubnet(String ipAddy, String network) throws UnknownHostException {
        String[] parts = network.split("/");
        String networkAddress = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);

        byte[] networkBytes = InetAddress.getByName(networkAddress).getAddress();
        byte[] ipBytes = ipAddy.equals(networkAddress) ? networkBytes : InetAddress.getByName(ipAddy).getAddress();

        int mask = -1 << (32 - prefixLength);

        ByteBuffer networkMasked = ByteBuffer.wrap(applyMask(networkBytes, mask));
        ByteBuffer ipMasked = ByteBuffer.wrap(applyMask(ipBytes, mask));

        return networkMasked.getInt() == ipMasked.getInt();
    }

    private static String getSubnet(String ipAddy, List<Subnet4> subnets) throws UnknownHostException {
        for (Subnet4 subnet : subnets) {
            if (ipInSubnet(ipAddy, subnet.getSubnet())) {
                return subnet.getUuid();
            }
        }
        throw new RuntimeException("IP Address " + ipAddy + " Does not have a matching subnet in Kea configuration. See help for more information");
    }

    private static boolean check() throws IOException, ParserConfigurationException, SAXException {

        if(!inFile.toFile().exists()) {
            System.out.println("I do not see the config.xml file. It needs to be in this folder: " + inFile.toAbsolutePath());
            return false;
        }

        String xml = Files.readString(inFile);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        NodeList oldStaticsNodeList = doc.getElementsByTagName("staticmap");
        NodeList subnetsNodeList = doc.getElementsByTagName("subnet4");
        boolean haveStaticsToMap = oldStaticsNodeList != null && oldStaticsNodeList.getLength() > 0;
        boolean haveNewSubnets = subnetsNodeList != null && subnetsNodeList.getLength() > 0;
        if(haveStaticsToMap && haveNewSubnets) {
            System.out.println("Your config file has the necessary information to do the migration.");
        }
        if(!haveStaticsToMap) {
            System.out.println("I do not see any DHCP static IP addresses to port over to Kea DHCP server.");
        }
        if(!haveNewSubnets) {
            System.out.println("I do not see the new subnets created in the Kea DHCP server config.");
        }
        return haveNewSubnets && haveStaticsToMap;
    }
}
