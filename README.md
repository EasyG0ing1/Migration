# Migration

Migration is a utility that will take your static IP address mappings in OPNsense and migrate them over to the Kea DHCP server that comes with OPNsense version 24.

### Here is a video tutorial if that works best for you

[<img src="img/VideoThumb.png" width="100%">](https://youtu.be/pd3yLJx3z90)

### This is a simple tool to use:

1) [Download the program](https://github.com/EasyG0ing1/Migration/releases/latest) for your operating system (they are native binaries, no need for a Java runtime environment).
    * Create a clean folder to put the program in
2) From your OPNsense interface, go to Kea DHCP / Kea DHCPv4 / Subnets
3) Define all of your subnets and their IP Pools.
    * The tool uses those newly created subnets to automatically assign your current reservations to the correct subnet.
4) Apply those changes
5) Go to System / Configuration / Backups
6) Click on Download Configuration
    * Save the file as `config.xml` in the same folder you downloaded to tool into
    * Make a copy of it just in case
7) Go to a shell and go to that folder
8) run `migrate check` and the program will examine your config file to make sure it has everything necessary to do the migration
9) If everything is ok, just run `migrate`
10) There will be a file created called `new_config.xml`
11) Go back into OPNsense under backups and restore this new file.
12) Done!

If you have any problems you can create an issue.

### Contributing
Create an Issue or a Pull Request if you want to contribute to the project.

### Updates

* 2.0.0
  * Streamlined use of XML library, eliminating unnecessary calls.
  * Program now outputs a file that can be directly imported into OPNsense

* 1.0.1
  * Added more detailed error reporting

* 1.0.0
  * Initial Release
