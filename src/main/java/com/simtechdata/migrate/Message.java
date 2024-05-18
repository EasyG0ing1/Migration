package com.simtechdata.migrate;

import java.util.Map;
import java.util.Set;

public class Message {

    private static final String NL = System.getProperty("line.separator");

    public static final String HELP = """
                        \s
             OPNsense DHCP Static Address Migration
             --------------------------------------
                        \s
             **** IT IS MANDATORY that you are working with version 24 or later of
                  OPNsense. This tool cannot work using the backup config of an
                  older version of OPNsense.
                        \s
             This is a tool to help you migrate from ISC DHCP server to the newer
             Kea DHCP Server in OPNsense version 24. It will take the static IP
             assignments that you currently have and convert them over to the
             Kea format which you can then import into OPNsense.
                        \s
             For quick documentation, go to https://github.com/EasyG0ing1/Migration
                        \s
             Possible arguments are:
                        \s
             how, --howto    -   Get the detailed steps for doing the migration.
             v, --version    -   get the version info.
             ?, --help       -   This help.

            \s""";

    public static final String HOW_TO = """
                        \s
             Instructions
             ------------
                        \s
             **** IT IS MANDATORY that you are working with version 24 or later of
                  OPNsense. This tool cannot work using the backup config of an
                  older version of OPNsense.
                        \s
             EXPORT config.xml
                 1  - In OPNsense, go into Services / Kea DHCP / Kea DHCPv4
                 2  - In the right pane, click on the Subnets tab
                 3  - Create all of your networks subnets and IP Pools
                      - The pools can be done later, what matters is that the subnet is properly stated
                      - Click Apply to save the subnets you just created
                 4  - Go into System / Configuration / Backups
                 5  - Click on Download Configuration and name the file `config.xml'
                      - Save the file into the same folder that this program is in
                 6  - Run this program
                      - There will be a new file in this folder called 'new_config.xml'
                        \s
             IMPORT new_config.xml
                 1  - Go into OPNsense under System / Configuration / Backups
                 2  - UNCHECK the box that says Reboot after restore
                      - Make sure everything else stays at default settings
                 3  - Click Choose File and select new_config.xml
                 4  - Click Restore configuration
                        \s
             To create an issue, go to https://github.com/EasyG0ing1/Migration and click on Issues
                        \s
            \s""";

    public static final String SUBNET_PROBLEMS_HAVING_GUID = """

             The <subnet4> nodes in the config.xml file contain the definitions of the subnets that
             you created in step one of this process (see migrate how). However, even though
             I can find a <subnet4> node which seems to have a valid UUID, I cannot find the xml
             tag <subnet> which would contain the definition of the subnet. An example of what
             that definition will look like, would be in the format of either of these:
                        \s
                 192.168.1.0/24
                 10.10.10.0/24
                        \s
             Where the address on the left side will be a valid IPv4 subnet address
             that is in alignment with the number of bits in the subnet mask and that
             number of bits is the number on the right of the slash.
                        \s
             We cannot proceed with the migration until those subnets are properly defined.
                        \s
             For instructions: migrate how

            \s""";

    public static final String SUBNET_EXPECTED_BUT_NOT_FOUND = """
                        \s
             I was expecting to find a Kea DHCP subnet definition, but instead I found none.
             Since the program merely iterates through the number of definitions it was given
             based on the number of <subnet4> nodes returned, it is impossible to speculate
             as to why this problem happened. A probable reason could be that your config.xml
             file is corrupted and should be re-created.
                        \s
             For instructions: migrate how
                        \s
            \s""";

    public static final String NO_KEA_SUBNETS_FOUND = """
                        \s
             Problem:
                 No Kea subnets found. Did you create them before generating config.xml?
                        \s
                 For instructions: migrate how
                        \s
            \s""";

    public static final String NO_STATIC_MAP = """
                        \s
             Problem:
                 No static mappings found in your config.xml file.
                        \s
             Options:
                 Please go into OPNsense and check all of your static mappings in the ISC DHCP server to make sure
                 that your mappings contain an IP address and a MAC address or simply scroll through your config.xml
                 file and do a search for <staticmap> where each static mapping exists as a complete <staticmap> node
                 so you would expect to see many sections of xml text that are defined by the <staticmap></staticmap>
                 tags. An example of one would look like this:
                        \s
                 <staticmap>
                 	<mac>cc:cc:10:10:10:11</mac>
                 	<ipaddr>10.10.10.11</ipaddr>
                 	<hostname>SomeHostname</hostname>
                 	<winsserver/>
                 	<dnsserver/>
                 	<ntpserver/>
                 	<descr>Description of static map</descr>
                 </staticmap>
                        \s
                 What is mandatory in these nodes is that the MAC address and the IP address exist and are accurate.
                        \s
            \s""";


    public static final String GENERIC_CONFIG_XML = """
                        \s
             Problem:
                 There was a problem with config.xml. The file exists but it could not be read into a Java XML Document.
                        \s
             Options:
                 You might try obtaining a clean copy from you OPNsense firewall.
                        \s
                     For instructions: migrate how
                        \s
            \s""";


    public static final String NO_CONFIG_FILE = """
                        \s
             Problem:
                 Could not find config.xml, which needs be in the same folder that this program is in
                 and you need to be in that folder when you run migrate.
                        \s
             Options:
                 Make sure you are in the correct folder and that both the config.xml file and the migrate
                 program are in that folder, then try again.
                        \s
                 For instructions: migrate how
                        \s
            \s""";


    public static String STATIC_MAP_FAILURES(Map<String, Integer> failureMap) {
        StringBuilder sb = new StringBuilder(NL);
        for (String netName : failureMap.keySet()) {
            Integer count = failureMap.get(netName);
            sb.append("\tNetwork: ").append(netName).append("\tFailures: ").append(count).append(NL);
        }
        sb.append(NL);

        return STR."""
                
                Alert:
                    There were failures encountered when extracting static maps from your
                    ISC DHCP4 networks. This could mean that either a specific network had no static
                    mappings at all, OR it could mean that some of your static mappings were missing
                    an IP address or a MAC address.

                    Here are the networks along with the number of failures each one had:
                    \{sb.toString()}
                    If a network only has one failure, then that network most likely has no static mappings.

                    Migrate will skip the incorrect mappings and has in fact migrated \{Migrate.getFinalCount()} static maps
                    over to Kea.

                """;
    }

    public static String IP_HAS_NO_SUBNET(String ipAddress) {
        return String.format("""
                                                                          \s
                                       Problem:
                                             IP Address: %s
                                             Has no matching Kea DHCP subnet.
                                                                          \s
                                        Options:
                                             Please make sure that the Kea subnets are correctly defined.
                                                                          \s
                                             For instructions: migrate how
                                                                          \s
                                     \s""", ipAddress);
    }


    public static final String CREATE_ISSUE = """
                        \s
             Create an issue for this problem by going to https://github.com/EasyG0ing1/Migration and clicking on Issues.
                        \s
             Make sure you copy and paste the stack trace above into the issue that you create.
                        \s
            \s""";


    public static String TEST_NODES(int count, int duplicates, String filename) {
        return STR."""

                The number of reservations found in \{filename} is: \{count}
                The number of duplicate IP Addresses is: \{duplicates}
                The net total of reservations is: \{count - duplicates}

                """;
    }

    public static String DUPLICATE_IP_ADDRESS(Set<String> duplicates) {
        StringBuilder sb = new StringBuilder(NL);
        for (String ipAddress : duplicates) {
            sb.append("\t").append(ipAddress).append(NL);
        }
        sb.append(NL);
        return STR."""

                Duplicate IP addresses were found in your static mappings.
                Here is the list:
                \{sb.toString()}
                Please remove the duplicate IP addresses from your OPNsense configuration
                then re-export config.xml and try again.

                """;
    }

    public static String success(String filename) {
        return STR."""

            The output file, \{filename} has been successfully created.

            There were \{Migrate.getFinalCount()} static mappings migrated to Kea.

            For instructions on how to import the config file back into OPNsense

                    For instructions: migrate how

            """;
    }

    public static String reservationsExist(int count, String filename) {
        return STR."""

                Your \{filename} file already has \{count} Kea static IP reservations.

                Migrating again will only add duplicates to your OPNsense configuration.

                """;
    }

}
