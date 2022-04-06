package jaci.openrio.toast.core.network;

import jaci.openrio.delegate.DelegateClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * The SessionJoiner acts as an interface between Toast and.. well... Toast. Calling toast with the --join arguments
 * will attempt to join the Toast console on the local host, useful for when you SSH into the RoboRIO.
 *
 * @author Jaci
 */
public class ToastSessionJoiner {

    static DelegateClient LOGGER_DELEGATE;
    static DelegateClient COMMAND_DELEGATE;

    static DataOutputStream commands_out;
    static BufferedReader logger_in;

    /**
     * Start the joiner. This simply connects to the designated Toast instance on localhost and
     * creates a logger and command bus bridge.
     */
    public static void init() {
        LOGGER_DELEGATE = new DelegateClient("localhost", 5805, "TOAST_logger");
        COMMAND_DELEGATE = new DelegateClient("localhost", 5805, "TOAST_command");

        try {
            LOGGER_DELEGATE.connect();
            COMMAND_DELEGATE.connect();

            commands_out = new DataOutputStream(COMMAND_DELEGATE.getSocket().getOutputStream());
            logger_in = new BufferedReader(new InputStreamReader(LOGGER_DELEGATE.getSocket().getInputStream()));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String line;
                    try {
                        while ((line = logger_in.readLine()) != null) {
                            System.out.println(line);
                        }
                    } catch (IOException e) {
                        System.err.println("Unexpected error while reading from Logger Delegate");
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }
            }).start();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if (line.trim().equalsIgnoreCase("--exit"))
                    System.exit(0);
                commands_out.writeBytes(line + "\n");
            }
        } catch (Exception e) {
            System.err.println("Something went wrong while establishing a connection to the Toast instance...");
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
