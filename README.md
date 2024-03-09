# Migration

Migration is a utility that will take your static IP address mappings in OPNsense and migrate them over to the Kea DHCP server that comes with OPNsense version 24.

### Here is a video tutorial if that works best for you

[<img src="img/VideoThumb.png" width="100%">](https://youtu.be/pYkvz7qmnzM)

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
10) There will be a file created called `config_out.xml` open it in a text editor
11) Copy the entire section between `<reservations>` and `</reservations>` including the XML tags.
11) Open `config.xml` in your text editor
12) Do a search for `reservations` you should find a line that looks like this:
    `<reservations/>`
13) Highlight the entire line and paste what you copied from step 11, replacing that line with the copied text.
14) Save the file
15) Go back into OPNsense and in backups / Restore, uncheck the box for rebooting - that won't be necessary.
16) Click `Choose File` and get config.xml that you just saved
17) Restore configuration
18) DONE!

If you have any problems you can create an issue.

### Contributing
Create an Issue or a Pull Request if you want to contribute to the project.
