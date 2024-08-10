package com.example.orderappwaiter;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
@Deprecated
public class Sender extends Thread {
    public String serverID;
    public String name;
    public ArrayList<Integer> orders;
    public char type = 'O';
    public boolean success = false;

    public void run() {
        if (serverID != null) {
            // Makes Socket global
            Socket socket;
            try {
                // gets the IP address of the current device
                String ip = getIPAddress();
                // Outputs the Current Device's IP address
                Log.d("IPADDRESS", ip);
                assert ip != null;
                // Creates an array of the parts of the IP, split around the "."s.
                String[] bits = ip.split("\\.");
                System.out.println(bits);
                // Creates the full IP by adding the first three items in "bits" together with dots between to the server ID
                String to_connect = bits[0] + "." + bits[1] + "." + bits[2] + "." + serverID;
                System.out.println(to_connect);
                // Creates a socket (global to function, see above) on port 65433, using the IP to_connect
                socket = new Socket(to_connect, 65433);
                System.out.println("Socket created.");
                System.out.println(socket);
                try {
                    //Creates the DataOutputStream to the server
                    OutputStream to_server = socket.getOutputStream();
                    DataOutputStream out = new DataOutputStream(to_server);
                    // Creates the dataInputStream from the server
                    InputStream from_server = socket.getInputStream();
                    DataInputStream in = new DataInputStream(from_server);
                    // O for Order, M for MergeOrder. Default is O (See top)
                    out.writeChar(type);
                    writeString(name, out);
                    for (int i = 0; i < 8; i++) {
                        writeInt(orders.get(i), out);
                    }
                    System.out.println("Written");
                    // UNTESTED
                    for (int i = 0; i < 6; i++) {
                        String result = readString(in);
                        if (result != null) {
                            System.out.println(result);
                            if (Objects.equals(result, "S")) {
                                success = true;
                                writeString("Y",out);
                                System.out.println("SUCCESS");
                            } else {
                                Log.d(result, result);
                            }
                        }
                    }
                    socket.close();
                } catch (Exception e) {
                    Log.d("newConnection 2:", "Something went wrong while getting data.");
                    Log.d("ErrorLog:", String.valueOf(e));
                    // If there is an exception, return the empty list, to be handled by code.
                }
            } catch (Exception e) {
                Log.d("newConnection 1:", "Something went wrong while creating ip/sockets");
                Log.d("ErrorLog", String.valueOf(e));
                // If there is an exception, return the empty list, to be handled by code.
            }
        }
    }
    public static Integer readInt(DataInputStream in) {
        while (true) {
            try {
                return in.readInt();
            } catch (EOFException e) {
                Log.d("No message so", "looping");
            } catch (IOException e) {
                Log.d("Server", "Stream Closed");
            }
        }
    }
    public static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            Log.d("ExceptionServer", "Waiting.");
        }
    }
    public static String readString(DataInputStream in) {
        int length = 0;
        ArrayList<Character> message = new ArrayList<>();
        int checks = 0;
        while (checks < 33) {
            try {
                length = in.readInt();
                break;
            } catch (EOFException e) {
                Log.d("No message so", "looping 115");
                wait(5);
                checks++;
            } catch (IOException e) {
                Log.d("Server", "Stream Closed");
                return null;
            }
        }
        if (checks == 33) {return null;}
        for (int i = 0; i < length; i++) {
            while (true) {
                try {
                    // Count system needed to prevent softlock
                    message.add(in.readChar());
                    break;
                } catch (EOFException e) {
                    Log.d("No message so", "looping");
                    wait(50);
                } catch (IOException e) {
                    Log.d("Server", "Stream Closed");
                    return null;
                }
            }
        }
        return decode(length, message);
    }
    public static String decode(Integer length, ArrayList<Character> chars) {
        String to_return = "";
        for (int i = 0; i < length; i++) {
            to_return += chars.get(i);
        }
        return to_return;
    }
    public static void writeString(String message, DataOutputStream out) {
        int length = message.length();
        try {
            out.writeInt(length);
        } catch (IOException e) {
            Log.d("Sender", "Error, noOutputStream.");

        }
        try {
            out.writeChars(message);
        } catch (IOException e) {
            Log.d("Sender", "Error, noOutputStream.");
        }
    }
    public static void writeInt(int number, DataOutputStream out) {
        try {
            out.writeInt(number);
        } catch (IOException e) {
            Log.d("Sender", "Error, noOutputStream");
        }
    }
    public static String getIPAddress() {
        boolean useIPv4 = false;
        try {
            List<NetworkInterface> interfaces =
                    Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface interface_ : interfaces) {

                for (InetAddress inetAddress :
                        Collections.list(interface_.getInetAddresses())) {

                    /* a loopback address would be something like 127.0.0.1 (the device
                       itself). we want to return the first non-loopback address. */
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipAddr = inetAddress.getHostAddress();
                        boolean isIPv4 = ipAddr.indexOf('.') < 3;

                        if (isIPv4 && !useIPv4) {
                            continue;
                        }
                        if (useIPv4 && !isIPv4) {
                            int delim = ipAddr.indexOf('%'); // drop ip6 zone suffix
                            ipAddr = delim < 0 ? ipAddr.toUpperCase() :
                                    ipAddr.substring(0, delim).toUpperCase();
                        }
                        return ipAddr;
                    }
                }

            }
        } catch (Exception ignored) { } // if we can't connect, just return empty string
        return "";
    }
    public boolean getSuccess() {return success;}
}
