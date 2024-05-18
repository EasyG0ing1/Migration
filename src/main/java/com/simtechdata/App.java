package com.simtechdata;

import com.simtechdata.enums.Mode;
import com.simtechdata.migrate.Message;
import com.simtechdata.migrate.Migrate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;


public class App {

    public static void main(String[] args) {
        processArgs(args);
        new Migrate().start();
        System.exit(0);
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
                    case "v", "version", "--version", "-v", "-version" -> {
                        showVersion();
                        System.exit(0);
                    }
                    case "?", "--help", "-help", "help" -> help();
                    case "howto", "how", "--how", "--howto" -> howTo();
                    case "debug" -> Mode.setMode(Mode.DEBUG);
                    case "graal", "graalvm" -> {
                        System.out.println("Running GraalVM");
                        Mode.setMode(Mode.GRAAL);
                    }
                    case "test" -> {
                        if (args.length > 1) {
                            String filename = args[1];
                            new Migrate(filename).testNew();
                        }
                        else {
                            new Migrate().testNew();
                        }
                        System.exit(0);
                    }
                    case "file" -> {
                        if (args.length > 1) {
                            String filename = args[1];
                            new Migrate(filename).start();
                            System.exit(0);
                        }
                        else {
                            System.out.println("Missing filename");
                            System.exit(1);
                        }
                    }
                }
            }
        }
        if (argumentPassed && !Mode.isDebug() && !Mode.isGraal()) {
            help();
        }
    }

    /**
     * Pulls the version number from the property file and shows it to the user
     */
    public static void showVersion() {
        Properties prop = new Properties();
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input == null) {
                System.out.println("Could not determine current version");
            }
            else {
                prop.load(input);
                System.out.println(prop.getProperty("version"));
            }
        }
        catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

}
