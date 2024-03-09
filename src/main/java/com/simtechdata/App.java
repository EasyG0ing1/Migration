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

    private static final String exeFolder = System.getProperty("user.dir");
    private static final Path outFile = Paths.get(exeFolder, "config_out.xml");
    private static final Path inFile = Paths.get(exeFolder, "config.xml");


    public static void main(String[] args) throws Exception {

        int test1 = -1 << 10;
        int test2 = 1 << 10;
        String binary1 = Integer.toBinaryString(test1).replaceAll("(.{8})","$1 ");
        String binary2 = Integer.toBinaryString(test2).replaceAll("(.{8})","$1 ");
        System.out.println(test1 + "\t" + binary1);
        System.out.println(test2 + "\t\t" + binary2);
        System.exit(0);

        if(args.length > 0) {
            if(args[0].toLowerCase().contains("check")) {
                check();
                System.exit(0);
            }
        }

        if(!check()) {
            System.exit(0);
        }


        /**
         * First thing to do is scour the config.xml file for the existing static IP assignments.
         * Put them into the staticmaps List
         */


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

        /**
         * Next, we get the list of subnets that were created in Kea so that we can check
         * each static IP address against each subnet then assign the subnets UUID to the
         * new record.
         * Subnets go into the List, subnet4List (subnet4 being unique to Kea)
         */

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

        /**
         * Next, we iterate through the existing static assignments and do binary comparison against every
         * subnet until one hits then we assign that subnets UUID to the new mapping. So the new mapping gets
         * the ip address, the mac address, hostname and description. That is all done in the reservation object
         * which goes into the reservationList.
         * We do generate a random UUID for the new mapping because Kea likes to use them for record identifiers
         * and the import process will be expecting to see a UUID for the record.
         */

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


        /**
         * Now we have everything we need. All that is left to do is use the POJOs and construct the Opnsense
         * instance properly so that we can export it to our output file.
         */

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

    /**
     * Generates one UUID for each Kea static mapping
     * @return String
     */
    private static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }


    /**
     * This method loops through the subnets checking the IP address for a match.
     * @param ipAddy - Pass it the IP address
     * @param subnets - Pass it the list of subnets
     * @return String
     */
    private static String getSubnet(String ipAddy, List<Subnet4> subnets) throws UnknownHostException {
        // Iterate subnets and find out if the IP addy is in one of the subnets
        for (Subnet4 subnet : subnets) {
            if (ipInSubnet(ipAddy, subnet.getSubnet())) {
                return subnet.getUuid();
            }
        }
        throw new RuntimeException("IP Address " + ipAddy + " Does not have a matching subnet in Kea configuration. See help for more information");
    }

    /**
     * This does the "Heavy lifting" of checking the IP address against a given subnet address/mask.
     * @param ipAddy - IP Address
     * @param network - Network in the format xxx.xxx.xxx.xxx/xx
     * @return String
     */
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

    /**
     * This function was broken out of the ipInSubnet method for simplification
     * @param address - byte array of the ip address
     * @param mask - subnet mask in number of bits
     * @return byte array
     */
    private static byte[] applyMask(byte[] address, int mask) {
        int addr = ByteBuffer.wrap(address).getInt();
        return ByteBuffer.allocate(4).putInt(addr & mask).array();
    }

    /**
     * This method checks the config.xml file to make sure it has everything needed to perform the migration.
     * @return true if it passed, otherwise - false;
     */
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
