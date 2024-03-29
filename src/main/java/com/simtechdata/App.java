package com.simtechdata;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.simtechdata.pojo.Reservation;
import com.simtechdata.pojo.Reservations;
import com.simtechdata.pojo.Staticmap;
import com.simtechdata.pojo.Subnet4;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

    private static final String exeFolder = System.getProperty("user.dir");
    private static final Path outFile = Paths.get(exeFolder, "new_config.xml");
    private static final Path inFile = Paths.get(exeFolder, "config.xml");
    private static boolean isCheck = false;
    private static final String LF = System.getProperty("line.separator");


    public static void main(String[] args) {
        processArgs(args);

        /**
         * First thing to do is scour the config.xml file for the existing static IP assignments.
         * Put them into the staticmaps List
         */
        Document doc;
        List<Staticmap> staticMappings = null;
        List<Subnet4> subnet4List = new ArrayList<>();
        String configXML = "";
        try {
            if (!inFile.toFile().exists()) {
                System.out.println(Message.NO_CONFIG_FILE);
                System.exit(1);
            }
            configXML = Files.readString(inFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new InputSource(new StringReader(configXML)));
            if (doc == null) {
                System.out.println(Message.GENERIC_CONFIG_XML);
                System.exit(1);
            }
            NodeList staticMapNodeList = doc.getElementsByTagName("staticmap");
            if (staticMapNodeList != null && staticMapNodeList.getLength() > 0) {
                staticMappings = getStaticMappings(staticMapNodeList);
                if (staticMappings.isEmpty()) {
                    System.out.println(Message.STATIC_MAP_NO_IP);
                    System.exit(1);
                }
            }
            else {
                System.out.println(Message.CONFIG_NO_STATIC_MAPS);
                System.exit(1);
            }

            /**
             * Next, we get the list of subnets that were created in Kea so that we can check
             * each static IP address against each subnet then assign the subnets UUID to the
             * new record.
             * Subnets go into the List, subnet4List (subnet4 being unique to Kea)
             */
            NodeList subnet4NodeList = doc.getElementsByTagName("subnet4");

            if (subnet4NodeList != null && subnet4NodeList.getLength() > 0) {
                for (int i = 0; i < subnet4NodeList.getLength(); i++) {
                    Node subnet4Node = subnet4NodeList.item(i);
                    Element subnet4Element = (Element) subnet4Node;
                    if (subnet4Element != null) {
                        String uuid = subnet4Element.getAttribute("uuid");
                        NodeList sub4List = subnet4Element.getElementsByTagName("subnet");
                        if (sub4List != null && sub4List.getLength() > 0) {
                            Node node = sub4List.item(0);
                            if (node != null) {
                                String subnet = node.getTextContent();
                                subnet4List.add(new Subnet4(subnet, uuid));
                            }
                            else {
                                System.out.println(Message.SUBNET_PROBLEMS_HAVING_GUID);
                                System.exit(1);
                            }
                        }
                        else {
                            System.out.println(Message.SUBNET_PROBLEMS_HAVING_GUID);
                            System.exit(1);
                        }
                    }
                    else {
                        System.out.println(Message.SUBNET_EXPECTED_BUT_NOT_FOUND);
                        System.exit(1);
                    }
                }
            }
            else {
                System.out.println(Message.NO_KEA_SUBNETS_FOUND);
                System.exit(1);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
        }
        catch (ParserConfigurationException e) {
            e.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
        }
        catch (SAXException e) {
            e.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
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
        try {
            for (Staticmap m : staticMappings) {
                Reservation rsv = new Reservation();
                String ipAddy = m.getIpaddr();
                rsv.setUuid(getUUID());
                rsv.setHw_Address(m.getMac());
                rsv.setIp_Address(m.getIpaddr());
                rsv.setHostname(m.getHostname());
                rsv.setDescription(m.getDescr());
                rsv.setSubnet(getSubnet(ipAddy, subnet4List));
                reservationList.add(rsv);
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
        }


        /**
         * With everything in place, we simply use reservationList to create an instance of
         * the Reservations class, which is used as the template to generate the XML data
         * that replaces the reservations node in the config.xml file.
         */

        try {
            Reservations reservations = new Reservations(reservationList);
            XmlMapper mapper = new XmlMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            StringWriter writer = new StringWriter();
            mapper.writeValue(writer, reservations);
            String rInstanceXml = writer.toString();
            if (configXML.contains("<reservations/>") || configXML.contains("<reservations>")) {
                String newConfigXML = configXML.contains("<reservations/>") ?
                        configXML.replaceFirst("<reservations/>", rInstanceXml) :
                        configXML.replaceFirst("<reservations>[\\s\\S]*?</reservations>", rInstanceXml);
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                StreamSource source = new StreamSource(new StringReader(newConfigXML));
                writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                transformer.transform(source, result);
                String pattern = "\\r?\\n\\s+\\r?\\n";
                String prettyXML = writer.toString().replaceAll(pattern, LF);
                outFile.toFile().createNewFile();
                Files.writeString(outFile, prettyXML, Charset.defaultCharset());
                System.out.println(Message.SUCCESS);
                System.exit(0);
            }
            else {
                System.out.println(Message.CONFIG_NO_RESERVATIONS);
                System.exit(1);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
        }
        catch (NullPointerException ne) {
            ne.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
        }
    }

    /**
     * Extracts the old static IP address mappings from config.xml
     *
     * @param nodeList - NodeList
     * @return List
     */
    private static List<Staticmap> getStaticMappings(NodeList nodeList) {
        if (nodeList == null) {
            System.out.println(Message.NO_STATIC_MAP);
            System.exit(1);
        }
        List<Staticmap> staticmaps = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            try {
                Node node = nodeList.item(i);
                if (node != null) {
                    Element element = (Element) node;
                    NodeList macNodeList = element.getElementsByTagName("mac");
                    Node macNode = macNodeList.item(0);

                    if (macNodeList == null || macNode == null) {
                        System.out.println(Message.GENERIC_NODE_NOT_FOUND("<mac>", "mac"));
                        System.exit(1);
                    }

                    NodeList ipNodeList = element.getElementsByTagName("ipaddr");
                    Node ipNode = ipNodeList.item(0);

                    if (ipNodeList == null || ipNode == null) {
                        System.out.println(Message.GENERIC_NODE_NOT_FOUND("<ipaddr>", "ip"));
                        System.exit(1);
                    }

                    String macAddress = macNode.getTextContent();
                    String ipAddress = ipNode.getTextContent();

                    if (!validMac(macAddress)) {
                        System.out.println(Message.INVALID_MAC_ADDRESS(macAddress));
                        System.exit(1);
                    }

                    if (!validIPAddress(ipAddress)) {
                        System.out.println(Message.INVALID_IP_ADDRESS(ipAddress));
                        System.exit(1);
                    }

                    Staticmap staticmap = new Staticmap(ipAddress, macAddress);

                    if (staticmap == null) {
                        System.out.println(Message.NULL_STATIC_MAP);
                        System.exit(1);
                    }

                    NodeList hostnameNodeList = element.getElementsByTagName("hostname");
                    if (hostnameNodeList.getLength() > 0) {
                        staticmap.setHostname(hostnameNodeList.item(0).getTextContent());
                    }

                    NodeList descriptionNodeList = element.getElementsByTagName("descr");
                    if (descriptionNodeList.getLength() > 0) {
                        staticmap.setDescription(descriptionNodeList.item(0).getTextContent());
                    }

                    staticmaps.add(staticmap);
                }
                else {
                    System.out.println(Message.NO_STATIC_MAP);
                    System.exit(1);
                }
            }
            catch (NullPointerException e) {
                e.printStackTrace();
                System.out.println(Message.CREATE_ISSUE);
                System.exit(1);
            }
        }
        return staticmaps;
    }

    private static boolean validMac(String macAddress) {
        Matcher m = Pattern.compile("[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}").matcher(macAddress);
        return m.matches();
    }

    private static boolean validIPAddress(String ipAddress) {
        boolean ipValid = false;
        Matcher m = Pattern.compile("([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})").matcher(ipAddress);
        if (m.find()) {
            String oStr1 = m.group(1);
            String oStr2 = m.group(2);
            String oStr3 = m.group(3);
            String oStr4 = m.group(4);

            int octet1 = Integer.parseInt(oStr1);
            int octet2 = Integer.parseInt(oStr2);
            int octet3 = Integer.parseInt(oStr3);
            int octet4 = Integer.parseInt(oStr4);

            boolean o1Valid = octet1 >= 0 && octet1 <= 255;
            boolean o2Valid = octet2 >= 0 && octet2 <= 255;
            boolean o3Valid = octet3 >= 0 && octet3 <= 255;
            boolean o4Valid = octet4 >= 0 && octet4 <= 255;

            ipValid = o1Valid && o2Valid && o3Valid && o4Valid;
        }
        return ipValid;
    }


    /**
     * Handles the command line arguments when the program is first run
     *
     * @param args - String array
     */
    private static void processArgs(String[] args) {
        boolean argumentPassed = args.length > 0;
        if (args.length > 0) {
            for (String a : args) {
                String arg = a.toLowerCase();
                switch (arg) {
                    case "check" -> {
                        System.out.println("Check is no longer necessary...");
                        isCheck = true;
                    }
                    case "v", "version", "--version", "-v", "-version" -> {
                        System.out.println("2.1.1");
                        System.exit(0);
                    }
                    case "?", "--help", "-help", "help" -> help();
                    case "howto", "how", "--how", "--howto" -> howTo();
                }
            }
        }
        if (argumentPassed && !isCheck) {
            help();
        }
    }


    /**
     * Generates one UUID for each Kea static mapping
     *
     * @return String
     */
    private static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /**
     * This method loops through the subnets checking the IP address for a match.
     *
     * @param ipAddy  - Pass it the IP address
     * @param subnets - Pass it the list of subnets
     * @return String
     */
    private static String getSubnet(String ipAddy, List<Subnet4> subnets) {
        try {
            // Iterate subnets and find out if the IP addy is in one of the subnets
            for (Subnet4 subnet : subnets) {
                if (ipInSubnet(ipAddy, subnet.getSubnet())) {
                    return subnet.getUuid();
                }
            }
            System.out.println(Message.IP_HAS_NO_SUBNET(ipAddy));
            System.exit(1);
            return "";
        }
        catch (NullPointerException ne) {
            ne.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
            throw ne;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
            throw e;
        }
    }

    /**
     * This does the "Heavy lifting" of checking the IP address against a given subnet address/mask.
     *
     * @param ipAddy  - IP Address
     * @param network - Network in the format xxx.xxx.xxx.xxx/xx
     * @return String
     */
    private static boolean ipInSubnet(String ipAddy, String network) {
        try {
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
        catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
            throw new RuntimeException(e);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
            throw new RuntimeException(e);
        }
    }

    /**
     * This function was broken out of the ipInSubnet method for simplification
     *
     * @param address - byte array of the ip address
     * @param mask    - subnet mask in number of bits
     * @return byte array
     */
    private static byte[] applyMask(byte[] address, int mask) {
        try {
            int addr = ByteBuffer.wrap(address).getInt();
            return ByteBuffer.allocate(4).putInt(addr & mask).array();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(Message.CREATE_ISSUE);
            System.exit(1);
            throw new RuntimeException(e);
        }
    }

    /**
     * Prints the help message to console and exits
     */
    private static void help() {
        System.out.println(Message.HELP);
        System.exit(0);
    }

    /**
     * Prints the How to message to console and exits
     */
    private static void howTo() {
        System.out.println(Message.HOW_TO);
        System.exit(0);
    }


}
