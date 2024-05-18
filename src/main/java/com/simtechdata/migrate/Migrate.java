package com.simtechdata.migrate;

import com.simtechdata.data.StaticmapLocal;
import com.simtechdata.enums.Mode;
import com.simtechdata.pojos.Reservation;
import com.simtechdata.pojos.Subnet4;
import org.apache.commons.io.FileUtils;
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
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
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
import java.util.*;

public class Migrate {

    public Migrate() {
        CONFIG_PATH = Paths.get(EXE_FOLDER, "config.xml");
        NEW_CONFIG_PATH = Paths.get(EXE_FOLDER, "new_config.xml");
    }

    public Migrate(String filename) {
        String newConfigFilename = STR."new_\{filename}";
        CONFIG_PATH = Paths.get(EXE_FOLDER, filename);
        NEW_CONFIG_PATH = Paths.get(EXE_FOLDER, newConfigFilename);
        System.out.println(STR."New Config Filename: \{newConfigFilename}");
    }

    private final String EXE_FOLDER = System.getProperty("user.dir");
    private final Path CONFIG_PATH;
    private final Path NEW_CONFIG_PATH;
    private final String NL = System.getProperty("line.separator");
    private final List<Subnet4> subnet4List = new ArrayList<>();
    private final List<Reservation> reservationList = new ArrayList<>();
    private final Map<String, Integer> netFailureMapCount = new HashMap<>();
    private Document doc = null;
    private final LinkedList<StaticmapLocal> staticMappings = new LinkedList<>();
    private NodeList subnet4NodeList = null;
    private String configXML = "";
    private static int finalCount = 0;


    public void start() {
        createDocument();
        getStaticMappings();
        getKeaSubnetList();
        createSubnet4List();
        createKeaReservations();
        if (finalCount > 0) {
            askUserToDeleteOldMappings();
        }
        saveNewConfig();
    }

    public void testNew() {
        File newConfigFile = NEW_CONFIG_PATH.toFile();
        if (newConfigFile.exists()) {
            try {
                String configXML = FileUtils.readFileToString(newConfigFile, Charset.defaultCharset());
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(new InputSource(new StringReader(configXML)));
                if (doc == null) {
                    System.out.println(Message.GENERIC_CONFIG_XML);
                    System.exit(1);
                }
                Node reservations = doc.getElementsByTagName("reservations").item(0);
                int count = 0;
                int duplicates = 0;
                Set<String> ipAddresses = new HashSet<>();
                if (reservations.getNodeType() == Node.ELEMENT_NODE) {
                    NodeList reservationList = reservations.getChildNodes();
                    int qty = reservationList.getLength();
                    for (int i = 0; i < qty; i++) {
                        Node node = reservationList.item(i);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            String name = node.getNodeName();
                            if (name.equalsIgnoreCase("reservation")) {
                                count++;
                                Reservation reservation = convertNodeToReservation(node);
                                if (reservation != null) {
                                    String ipAddy = reservation.getIp_address();
                                    if (ipAddresses.contains(ipAddy)) {
                                        duplicates++;
                                    }
                                    ipAddresses.add(ipAddy);
                                }
                                else {
                                    System.out.println("reservation is null");
                                    System.exit(1);
                                }
                            }
                        }
                    }
                    if (reservationList != null && count > 0) {
                        System.out.println(Message.TEST_NODES(count, duplicates, NEW_CONFIG_PATH.getFileName().toString()));
                    }
                    else {
                        System.out.println("Could not find any reservations in new_config.xml");
                        System.exit(1);
                    }
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
        }
        else {
            System.out.println(STR."Could not find file: \{NEW_CONFIG_PATH}");
            System.exit(1);
        }
    }

    private Reservation convertNodeToReservation(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            String uuid = element.getAttribute("uuid");
            String subnet = getElementTextContent(element, "subnet");
            String ipAddress = getElementTextContent(element, "ip_address");
            String hwAddress = getElementTextContent(element, "hw_address");
            String hostname = getElementTextContent(element, "hostname");
            String description = getElementTextContent(element, "description");

            return new Reservation(uuid, subnet, ipAddress, hwAddress, hostname, description);
        }
        return null;
    }

    private String getElementTextContent(Element parentElement, String tagName) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    private void createDocument() {
        try {
            if (CONFIG_PATH.toFile().exists()) {
                configXML = Files.readString(CONFIG_PATH);
            }
            else {
                System.out.println(Message.NO_CONFIG_FILE);
                System.exit(1);
            }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new InputSource(new StringReader(configXML)));
            if (doc == null) {
                System.out.println(Message.GENERIC_CONFIG_XML);
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
    }

    private void getStaticMappings() {
        Node dhcpdNode = doc.getElementsByTagName("dhcpd").item(0);
        if (dhcpdNode.getNodeType() == Node.ELEMENT_NODE) {
            NodeList dhcpdNodeList = dhcpdNode.getChildNodes();
            for (int x = 0; x < dhcpdNodeList.getLength(); x++) {
                Node netNode = dhcpdNodeList.item(x);
                String netName = netNode.getNodeName();
                NodeList netNodeList = netNode.getChildNodes();
                for (int y = 0; y < netNodeList.getLength(); y++) {
                    Node staticMapNode = netNodeList.item(y);
                    String name = staticMapNode.getNodeName();
                    if (name.equalsIgnoreCase("staticmap")) {
                        NodeList valueNodeList = staticMapNode.getChildNodes();
                        if (valueNodeList != null && valueNodeList.getLength() > 0) {
                            String mac = "";
                            String ipAddress = "";
                            String cid = "";
                            String hostname = "";
                            String description = "";
                            for (int z = 0; z < valueNodeList.getLength(); z++) {
                                Node valueNode = valueNodeList.item(z);
                                String valueName = valueNode.getNodeName();
                                String value = valueNode.getTextContent();
                                if (value != null) {
                                    switch (valueName) {
                                        case "mac" -> mac = value;
                                        case "ipaddr" -> ipAddress = value;
                                        case "cid" -> cid = value;
                                        case "hostname" -> hostname = value;
                                        case "descr" -> description = value;
                                    }
                                }
                            }
                            boolean addStatic = !mac.isEmpty() && !ipAddress.isEmpty();
                            StaticmapLocal staticmapLocal = new StaticmapLocal(mac, ipAddress, cid, hostname, description, netName);
                            if (addStatic) {
                                staticMappings.addLast(staticmapLocal);
                                finalCount++;
                            }
                            else {
                                addFailure(netName);
                            }
                        }
                        else {
                            addFailure(netName);
                        }
                    }
                }
            }
        }
        if (finalCount == 0) {
            System.out.println(Message.NO_STATIC_MAP);
            System.exit(1);
        }
    }

    private void getKeaSubnetList() {
        /**
         * This method gets the list of subnets that were created in Kea so that we can check
         * each static IP address against each subnet then assign the subnets UUID to the
         * new Kea static mapping. Subnets go into subnet4List (subnet4 being unique to Kea)
         */

        subnet4NodeList = doc.getElementsByTagName("subnet4");
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

        if (subnet4NodeList == null || subnet4NodeList.getLength() == 0 || subnet4List.isEmpty()) {
            System.out.println(Message.NO_KEA_SUBNETS_FOUND);
            System.exit(1);
        }
    }

    private void createSubnet4List() {

        /**
         * This method gets the list of subnets that were created in Kea and puts them into
         * subnet4List (subnet4 being an XML tag that is unique to Kea) so that we can check
         * each static IP address against each subnet then assign the subnets UUID to the
         * new record.
         */

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

    private void createKeaReservations() {
        Set<String> uuidSet = new HashSet<>();
        Set<String> ipAddresses = new HashSet<>();
        int count = 0;
        for (StaticmapLocal staticMap : staticMappings) {
            String mac = staticMap.getMac();
            String cid = staticMap.getCid();
            String ipAddy = staticMap.getIpAddress();
            String description = staticMap.getDescription();
            String hostname = staticMap.getHostname();
            String uuid = "";
            if (hostname.isEmpty()) {
                hostname = cid;
            }
            while (uuidSet.contains(uuid) || uuid.isEmpty()) {
                uuid = getUUID();
            }
            uuidSet.add(uuid);
            String subnet = getSubnet(ipAddy);
            if (subnet.isEmpty()) {
                System.out.println(Message.IP_HAS_NO_SUBNET(ipAddy));
                System.exit(1);
            }
            Reservation rsv = new Reservation(uuid, subnet, ipAddy, mac, hostname, description);
            reservationList.add(rsv);
            if (Mode.isDebug()) {
                String dbs = STR."Reservation Added:\{NL}IPAddy:      \{ipAddy}\{NL}Mac:         \{mac}\{NL}Hostname:    \{hostname}\{NL}Description: \{description}\{NL}";
                System.out.println(dbs);
            }
            count++;
        }
        if (Mode.isDebug()) {
            System.out.println(STR."Total added to reservationList:      \{count}");
        }
        Node reservationNode = doc.getElementsByTagName("reservations").item(0);
        int reservationCount = reservationNode.getChildNodes().getLength();
        if (reservationCount > 0) {
            System.out.println(Message.reservationsExist(reservationCount, CONFIG_PATH.getFileName().toString()));
            System.exit(1);
        }
        if (reservationNode.getNodeType() == Node.ELEMENT_NODE) {
            Element reservationElement = (Element) reservationNode;
            for (Reservation reservation : reservationList) {
                reservationElement.appendChild(reservation.getReservation(doc));
            }
        }
    }

    private void askUserToDeleteOldMappings() {
        System.out.println(STR."\{finalCount} Kea DHCP static mappings have been successfully migrated.\nWould you like to remove ALL static mappings from your ISC DHCP configuration?");
        String response = Mode.isForced() ? "y" : "";
        while (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("n")) {
            System.out.print("(Y/N):");
            response = new Scanner(System.in).nextLine();
        }
        if (response.equalsIgnoreCase("y")) {
            Node dhcpdNode = doc.getElementsByTagName("dhcpd").item(0);
            if (dhcpdNode == null) {
                System.out.println("Could not remove old mappings because the dhcpd node came back null!");
                System.out.print("Do you wish to continue? Your static mappings will still migrate safely if you chose Y\n(Y/N)? ");
                if (new Scanner(System.in).nextLine().equalsIgnoreCase("n")) {
                    System.exit(1);
                }
            }
            else {
                NodeList networks = dhcpdNode.getChildNodes();
                for (int i = 0; i < networks.getLength(); i++) {
                    Node networkNode = networks.item(i);
                    String nodeName = networkNode.getNodeName();
                    if (networkNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) networkNode;
                        NodeList staticMapNodes = element.getElementsByTagName("staticmap");
                        boolean removed = false;
                        while (staticMapNodes.getLength() > 0) {
                            String staticName = staticMapNodes.item(0).getNodeName();
                            if (Mode.isDebug())
                                System.out.println(STR."\{nodeName}:\{staticName} Removed");
                            Node staticMapNode = staticMapNodes.item(0);
                            element.removeChild(staticMapNode);
                            removed = true;
                        }
                        if (removed) { //add the <staticmap/> tag indicating there are no static mappings for this network
                            Element noStaticMap = doc.createElement("staticmap");
                            element.appendChild(noStaticMap);
                        }
                    }
                }
            }
        }
    }

    private void saveNewConfig() {
        try {
            if (!netFailureMapCount.isEmpty()) {
                System.out.println(Message.STATIC_MAP_FAILURES(netFailureMapCount));
            }
            if (finalCount > 0) {
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                StringWriter writer = new StringWriter();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(writer);
                transformer.transform(source, result);
                String pattern = "\\r?\\n\\s+\\r?\\n";
                String prettyXML = writer.toString().replaceAll(pattern, NL);
                File outFile = NEW_CONFIG_PATH.toFile();
                if (outFile.exists())
                    FileUtils.forceDelete(outFile);
                FileUtils.writeStringToFile(outFile, prettyXML, Charset.defaultCharset());
                System.out.println(Message.success(outFile.getName()));
            }
            else {
                System.out.println(STR."finalCount: \{finalCount}");
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

    }

    private String findParentOfDHCPDMapping(Node node) {
        Node current = node;
        while (current != null && !"dhcpd".equals(current.getParentNode().getNodeName())) {
            current = current.getParentNode();
        }
        return current != null ? current.getNodeName() : null;
    }

    private void addFailure(String net) {
        if (netFailureMapCount.containsKey(net)) {
            Integer value = netFailureMapCount.get(net) + 1;
            netFailureMapCount.replace(net, value);
        }
        else {
            netFailureMapCount.put(net, 1);
        }
    }

    /**
     * Generates one UUID for each Kea static mapping
     *
     * @return String
     */
    private String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /**
     * This method loops through the subnets checking the IP address for a match.
     *
     * @param ipAddy - Pass it the IP address
     * @return String
     */
    private String getSubnet(String ipAddy) {
        try {
            // Iterate subnets and find out if the IP addy is in one of the subnets
            for (Subnet4 subnet : subnet4List) {
                if (ipInSubnet(ipAddy, subnet.getSubnet())) {
                    return subnet.getUuid();
                }
            }
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
        return "";
    }

    /**
     * This does the "Heavy lifting" of checking the IP address against a given subnet address/mask.
     *
     * @param ipAddy  - IP Address
     * @param network - Network in the format xxx.xxx.xxx.xxx/xx
     * @return String
     */
    private boolean ipInSubnet(String ipAddy, String network) {
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
    private byte[] applyMask(byte[] address, int mask) {
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
     * Used by the Message class
     *
     * @return number of successful migrations
     */
    public static int getFinalCount() {
        return finalCount;
    }
}
