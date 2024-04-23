package com.simtechdata;

public class Message {


    public static final String HELP = """
                
                OPNsense DHCP Static Address Migration
                --------------------------------------
                
                **** IT IS MANDATORY that you are working with version 24 or later of
                     OPNsense. This tool cannot work using the backup config of an
                     older version of OPNsense.
                
                This is a tool to help you migrate from ISC DHCP server to the newer
                Kea DHCP Server in OPNsense version 24. It will take the static IP
                assignments that you currently have and convert them over to the
                Kea format which you can then import into OPNsense.
                
                For quick documentation, go to https://github.com/EasyG0ing1/Migration
                
                Possible arguments are:
                
                how, --howto    -   Get the detailed steps for doing the migration.
                v, --version    -   get the version info.
                ?, --help       -   This help.

                """;

    public static final String HOW_TO = """
                
                Instructions
                ------------
                
                **** IT IS MANDATORY that you are working with version 24 or later of
                     OPNsense. This tool cannot work using the backup config of an
                     older version of OPNsense.
                
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
                7  - Go back into OPNsense under System / Configuration / Backups
                8  - UNCHECK the box that says Reboot after restore
                     - Make sure everything else stays at default settings
                9  - Click Choose File and select new_config.xml
                10 - Click Restore configuration
                
                You're done!
                
                To create an issue, go to https://github.com/EasyG0ing1/Migration and click on Issues
                
                """;

    public static final String NO_STATIC_MAP = """

                                           There was a problem while processing the existing static mappings.
                                           It appears that there might not be any existing static DHCP mappings available
                                           and therefore, there is nothing to process.

                                           Go back into OPNsense and make sure you have existing DHCP static maps in the old DHCP service.
            
                                           If you find that you do in fact have old DHCP static maps to be migrated, then check each one
                                           of them and make sure they have a proper IP address and MAC address assigned to each map.

            """;

    public static final String SUBNET_PROBLEMS_HAVING_GUID = """

            The <subnet4> nodes in the config.xml file contain the definitions of the subnets that
            you created in step one of this process (see migrate --howto). However, even though
            I can find a <subnet4> node which seems to have a valid UUID, I cannot find the xml
            tag <subnet> which would contain the definition of the subnet. An example of what
            that definition will look like, would be in the format of either of these:
            
                192.168.1.0/24
                10.10.10.0/24
            
            Where the address on the left side will be a valid IPv4 subnet address
            that is in alignment with the number of bits in the subnet mask and that
            number of bits is the number on the right of the slash.
            
            We cannot proceed with the migration until those subnets are properly defined.
            
            Please run `migrate --howto` for specific instructions on how to do that.

            """;

    public static final String SUBNET_EXPECTED_BUT_NOT_FOUND = """
            
            I was expecting to find a Kea DHCP subnet definition, but instead I found none.
            Since the program merely iterates through the number of definitions it was given
            based on the number of <subnet4> nodes returned, it is impossible to speculate
            as to why this problem happened. A probable reason could be that your config.xml
            file is corrupted and should be re-created.
            
            Run `migrate --howto` for instructions.
            
            """;

    public static final String NO_KEA_SUBNETS_FOUND = """
            
            Problem:
                No Kea subnets found. Did you create them before downloading config.xml?
            
                Run `migrate --howto` for instructions
            
            """;

    public static final String STATIC_MAP_NO_IP = """
            
            Problem:
                Though your config.xml file has a <staticmap> node, no IP address could be found in a specific map.
            
            Thoughts:
                Please go into OPNsense and check all of your static mappings in the old DHCP server to make sure
                that they at least contain an IP address and a MAC address or simply scroll through your config.xml
                file and do a search for <staticmap> where each static mapping exists as a complete <staticmap> node
                so you would expect to see many sections of xml text that are defined by the <staticmap></staticmap>
                tags. An example of one would look like this:
            
                <staticmap>
                	<mac>cc:cc:10:10:10:11</mac>
                	<ipaddr>10.10.10.11</ipaddr>
                	<hostname>Misc1</hostname>
                	<winsserver/>
                	<dnsserver/>
                	<ntpserver/>
                </staticmap>
            
                What is mandatory in these nodes is that the MAC address and the IP address exist and are accurate.
            
            """;


    public static final String GENERIC_CONFIG_XML = """
            
            Problem:
                There was a problem with config.xml.
            
            Thoughts:
                You might try obtaining a clean cop from you OPNsense firewall.
            
            """;


    public static final String NO_CONFIG_FILE = """
            
            Problem:
                Could not find config.xml, which needs be in the same folder that this program is in
                and you need to be in that folder when you run migrate.
            
            Thoughts:
                Make sure you are in the correct folder and that both the config.xml file and the migrate
                program are in that folder, then try again.
            
                run `migrate --howto` for instructions.
            
            """;

    public static final String CONFIG_NO_STATIC_MAPS = """
            
            Problem:
                Could not find any static mappings in your config.xml file.
            
            Thoughts:
                Go back into OPNsense and make sure you have actual static mappings defined in the old DHCP
                service then re-create the config.xml file and try again.
            
                run `migrate --howto` for instructions.
            
            """;

    public static final String TEMPLATE = """
            
            Problem:
            
            
            Thoughts:
            

                run `migrate --howto` for instructions.
            
            """;

    public static String STATIC_MAP_IP_NOT_FOUND(String macAddress, String CIDString, String hostname, String description) {
        String mac = macAddress.isEmpty() ? "NONE" : macAddress;
        String cid = CIDString.isEmpty() ? "NONE" : CIDString;
        String host = hostname.isEmpty() ? "NONE" : hostname;
        String desc = description.isEmpty() ? "NONE" : description;
        return String.format("""
                                      Problem:
                                            One of your static mappings does not have an IP Address assigned to it.
                                     
                                       Discussion:
                                            Every static map MUST HAVE an IP address AND a MAC address. Here are the remaining
                                            details from the static map which might help you locate it in your ISC DHCP
                                            configuration. Edit the mapping so that it has an IP address and a MAC address then
                                            start the migration process over again from step 1.
                                   
                                            Mac:         %s
                                            CID:         %s
                                            Hostname:    %s
                                            Description: %s
                                     
                                     """, mac, cid, host, desc);
    }

    public static String MAC_CID_NODE_NOT_FOUND(String ipAddress, String hostname, String description) {
        String ipAddy = ipAddress.isEmpty() ? "NONE" : ipAddress;
        String host = hostname.isEmpty() ? "NONE" : hostname;
        String desc = description.isEmpty() ? "NONE" : description;
        return String.format("""
                                      Problem:
                                            Could not find a Mac address OR a CID value in static mapping.
                                     
                                       Thoughts:
                                            The remaining values for this static mapping are:
                                     
                                            IP Address:  %s
                                            Hostname:    %s
                                            Description: %s
                                     
                                            Every static mapping MUST have a Mac address or there is no way to create an assignment
                                            for it in Kea DHCP. Please either assign a Mac address to the static mapping or remove the
                                            static mapping completely from your ISC config, then start the migration process over again
                                            from step 1.
                                     
                                            While looking at your ISC mappings, save time and please make sure that ALL mappings have
                                            a MAC address.
                                     
                                     """, ipAddy, host, desc);
    }

    public static String HAS_CID_ONLY() {
        return """
                                      Problem:
                                            You have static mappings that only have a CID assigned to them without a MAC address.
                                            As of April 2024, Kea DHCP server does support either MAC address or CID as a valid
                                            identifier. However, OPNsense has not yet included the CID feature of Kea DHCP and it is
                                            assumed that it will be included in a future update.
                                     
                                            You have two options if you wish to include these mappings:
                                     
                                            1) Proceed with the migration and import new_config.xml into OPNsense, then add these
                                               mappings manually under Services / Kea DHCP / Kea DHCPv4 / Reservations.
                                     
                                            2) Go back into your ISC static mappings and clear the CID value then assign a proper
                                               MAC address. Once all mappings have been fixed, start the migration process over
                                               again from step 1.
                                     
                                            Here are the static mappings that have NOT been ported to Kea:
                                     """;
    }

    public static String EXCLUDED_STATIC_MAPPING(String ipAddress, String hostname, String description) {
        return String.format("""
                                     
                                                IP Address:  %s
                                                Hostname:    %s
                                                Description: %s
                                     
                                     """, ipAddress, hostname, description);
    }

    public static final String NULL_STATIC_MAP = """
            
            Problem:
                The attempt to instantiate the Java object Staticmap returned a null value.
            
            Thoughts:
                This should never happen, but in case it has, create an issue on the Github page
                for this project.

                run `migrate --howto` for instructions.
            
            """;

    public static String IP_HAS_NO_SUBNET(String ipAddress) {
        return String.format("""
                                      Problem:
                                            IP Address: %s
                                            Has no matching Kea DHCP subnet.
                                     
                                       Thoughts:
                                            Please make sure that the Kea subnets are correctly defined.
                                     
                                            Run `migrate --howto` for instructions.
                                     
                                     """, ipAddress);
    }


    public static String INVALID_IP_ADDRESS(String ipAddress) {
        return String.format("""
                                      Problem:
                                            IP Address: %s
                                            is NOT A valid IP address, or no address existed in the specific static map.
                                     
                                       Thoughts:
                                            Look through the config.xml file and search for <ipaddr> and see if any of them
                                            are missing values or have an IP address that is incorrect.
                                     
                                     """, ipAddress);
    }



    public static String INVALID_MAC_ADDRESS(String macAddress, String cidString, String hostname, String description) {
        String mac  = macAddress.isEmpty() ? "NONE" : macAddress;
        String cid  = cidString.isEmpty() ? "NONE"  : cidString;
        String host = hostname.isEmpty() ? "NONE"   : hostname;
        String desc = description.isEmpty() ? "NONE": description;
        return String.format("""
                                      Problem:
                                            MAC Address: %s
                                            is NOT A valid MAC address, or no address existed in the specific static map.
                                     
                                       Thoughts:
                                            Go into your ISC DHCP static mappings and look for the record that has this
                                            MAC address. Then correct the problem. Here are the details from the mapping,
                                            which should help you locate the record:

                                   
                                            Mac:         %s
                                            CID:         %s
                                            Hostname:    %s
                                            Description: %s
                                     
                                            A valid MAC Address looks similar to this:
                                     
                                                aa:3f:c2:fd:65:2a
                                     
                                            Where there are six hexadecimal numbers with a colon between them.
                                            The letters can be A through F and the numbers can be 0 through 9.
                                            The letters are NOT case sensitive (upper and lower case are OK).
                                     
                                     """, mac, mac, cid, host, desc);
    }


    public static final String CONFIG_NO_RESERVATIONS = """
            
            Problem:
                Your config.xml file does not appear to contain the required node called <reservations>
            
            Thoughts:
                If you have created your config.xml file with any version of OPNsense that is NOT
                version 24, then you need to upgrade to version 24 first, then follow the steps
                to complete your DHCP static map migration.
            
                Run `migrate --howto` for instructions.
            
            """;

    public static final String SUCCESS = """
            
            The output file, new_config.xml has been successfully created.
            
            Please go into OPNsense, then:
                - Click on System / Configuration / Backups
                - UNCHECK box next to Reboot
                - Click Choose File
                - Find `new_config.xml` that was just created
                - Click Restore configuration
            
                Done!
            
            """;

    public static final String CREATE_ISSUE = """
            
            Create an issue for this problem by going to https://github.com/EasyG0ing1/Migration and clicking on Issues.
            
            Make sure you copy and paste the stack trace above into the issue that you create.
            
            """;

}
