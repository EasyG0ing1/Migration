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

public class App {

    private static final String exeFolder = System.getProperty("user.dir");
    private static final Path outFile = Paths.get(exeFolder, "new_config.xml");
    private static final Path inFile = Paths.get(exeFolder, "config.xml");
    private static boolean isCheck = false;
    private static final String LF = System.getProperty("line.separator");
    private static boolean writeFinal = true;


    public static void main(String[] args) {
        processArgs(args);
        if (!check()) {
            System.exit(0);
        }

        /**
         * First thing to do is scour the config.xml file for the existing static IP assignments.
         * Put them into the staticmaps List
         */
        Document doc;
        List<Staticmap> staticMappings = null;
        List<Subnet4> subnet4List = new ArrayList<>();
        String configXML;
        try {
            if(!inFile.toFile().exists()) {
                System.out.println("The file " + inFile.toFile().getAbsolutePath() +LF + "Does not exist and is mandatory before this utility will work." + LF + "Run `migrate ?` for help.");
                System.exit(0);
            }
            configXML = Files.readString(inFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new InputSource(new StringReader(configXML)));
            if(doc == null) {
                System.out.println("There was a problem with config.xml. You might try obtaining a clean cop from you OPNsense firewall.");
                System.exit(0);
            }
            NodeList staticMapNodeList = doc.getElementsByTagName("staticmap");
            if (staticMapNodeList != null) {
                staticMappings = getStaticMappings(staticMapNodeList);
                if(staticMappings.isEmpty()) {
                    System.out.println("There was a problem with your config.xml. Could not find any static IP address to migrate.");
                    System.exit(0);
                }
            }
            else {
                System.out.println("There was a problem with config.xml. You might try obtaining a clean cop from you OPNsense firewall.");
                System.exit(0);
            }

            /**
             * Next, we get the list of subnets that were created in Kea so that we can check
             * each static IP address against each subnet then assign the subnets UUID to the
             * new record.
             * Subnets go into the List, subnet4List (subnet4 being unique to Kea)
             */
            NodeList subnet4NodeList = doc.getElementsByTagName("subnet4");

            if (subnet4NodeList == null || subnet4NodeList.getLength() < 1) {
                System.out.println("There was a problem with your config.xml file, no Kea subnets found." + LF + "Did you create them before downloading config.xml?");
                System.exit(0);
            }
            for (int i = 0; i < subnet4NodeList.getLength(); i++) {
                Node subnet4Node = subnet4NodeList.item(i);
                Element subnet4Element = (Element) subnet4Node;
                String uuid = subnet4Element.getAttribute("uuid");
                String subnet = subnet4Element.getElementsByTagName("subnet").item(0).getTextContent();
                subnet4List.add(new Subnet4(subnet, uuid));
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        catch (SAXException e) {
            throw new RuntimeException(e);
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
        catch (NullPointerException ne) {
            ne.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        /**
         * With everything set up, we simply use reservationList to create an instance of
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
            if(writeFinal) {
                Files.writeString(outFile, prettyXML, Charset.defaultCharset());
                System.out.println("File written:" + LF + LF + "\t" + outFile.toFile().getName() + LF + LF + "Restore that file into OPNsense");
            }
            else {
                System.out.println("Out file was NOT written due to one or more problems");
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (NullPointerException ne) {
            ne.printStackTrace();
            throw ne;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Extracts the old static IP address mappings from config.xml
     *
     * @param nodeList - NodeList
     * @return List
     */
    private static List<Staticmap> getStaticMappings(NodeList nodeList) {
        List<Staticmap> staticmaps = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            Element element = (Element) node;
            String macAddress = element.getElementsByTagName("mac").item(0).getTextContent();
            String ipAddress = element.getElementsByTagName("ipaddr").item(0).getTextContent();

            Staticmap staticmap = new Staticmap(ipAddress, macAddress);

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
        return staticmaps;
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
                        isCheck = true;
                        check();
                        System.exit(0);
                    }
                    case "v", "version", "--version", "-v", "-version" -> {
                        System.out.println("2.0.1");
                        System.exit(0);
                    }
                    case "?", "--help", "-help", "help" -> help();
                }
            }
        }
        if (argumentPassed) {
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
            System.out.println("IP Address " + ipAddy + " Does not have a matching subnet in Kea configuration. See help for more information.");
            writeFinal = false;
            return "";
        }
        catch (NullPointerException ne) {
            ne.printStackTrace();
            throw ne;
        }
        catch (Exception e) {
            e.printStackTrace();
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
            throw new RuntimeException(e);
        }
        catch (NullPointerException ne) {
            ne.printStackTrace();
            throw ne;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
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
        catch (NullPointerException ne) {
            ne.printStackTrace();
            throw ne;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * This method checks the config.xml file to make sure it has everything needed to perform the migration.
     *
     * @return true if it passed, otherwise - false;
     */
    private static boolean check() {
        try {
            if (!inFile.toFile().exists()) {
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
            if (isCheck) {
                if (haveStaticsToMap && haveNewSubnets) {
                    System.out.println("Your config file has the necessary information to do the migration.");
                }
                if (!haveStaticsToMap) {
                    System.out.println("I do not see any DHCP static IP addresses to port over to Kea DHCP server.");
                }
                if (!haveNewSubnets) {
                    System.out.println("I do not see the new subnets created in the Kea DHCP server config.");
                }
            }
            return haveNewSubnets && haveStaticsToMap;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        catch (SAXException e) {
            throw new RuntimeException(e);
        }
        catch (NullPointerException ne) {
            ne.printStackTrace();
            throw ne;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Prints the help message to console and exits
     */
    private static void help() {
        String help = """
                                
                Migration
                ---------
                                
                This is a tool to help you migrate from ISC DHCP server to the newer
                Kea DHCP Server in OPNsense version 24. It will take the static IP
                assignments that you currently have and convert them over to the
                Kea format which you can then import into OPNsense.
                                
                For quick documentation, go to https://github.com/EasyG0ing1/Migration
                                
                Possible arguments are:
                                
                check           -   Verifies that your config.xml file is good to go
                v, --version    -   get the version info.
                ?, --help       -   This help

                """;
        System.out.println(help);
        System.exit(0);
    }
}
