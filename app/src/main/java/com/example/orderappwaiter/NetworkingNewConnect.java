package com.example.orderappwaiter;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
@Deprecated
public class NetworkingNewConnect extends Thread {
    public String currentIP = "";
    public ArrayList[] full;
    public ArrayList[] list;
    public String serverID;
    public MainActivity parent;

    public void run() {
        assert serverID != null;
        // Makes Socket global
        Socket socket;
        // Creates the two ArrayLists which will be returned
        ArrayList<Integer> available = new ArrayList<Integer>();
        ArrayList<String> items = new ArrayList<String>();
        try {
            // gets the IP address of the current device
            String ip = getIPAddress();
            //String ip = "192.168.0.122";
            // Outputs the Current Device's IP address
            Log.d("IPADDRESS", ip);
            assert ip != null;
            // Creates an array of the parts of the IP, split around the "."s.
            String[] bits = ip.split("\\.");
            // Creates the full IP by adding the first three items in "bits" together with dots between to the server ID
            String to_connect = bits[0] + "." + bits[1] + "." + bits[2] + "." + serverID;
            // Sets the variable "currentIP" to the IP made above
            currentIP = to_connect;
            // Outputs that IP
            Log.d("Ip", currentIP);
            // Creates a socket (global to function, see above) on port 65433, using the IP to_connect
            socket = new Socket(to_connect, 65433);
            System.out.println(socket);
            try {
                //Creates the DataOutputStream to the server
                OutputStream to_server = socket.getOutputStream();
                DataOutputStream out = new DataOutputStream(to_server);
                // Creates the dataInputStream from the server
                InputStream from_server = socket.getInputStream();
                DataInputStream in = new DataInputStream(from_server);
                // N for newConnection
                out.writeChar('N');
                // Loops through the 8 integers sent for available
                wait(10);
                for (int index = 0; index < 8; index++) {
                    // Tries each item up to ten times if not enough data
                    available.add(readInt(in));
                }

                for (int indexitem = 0; indexitem < 8; indexitem++) {
                    items.add(readString(in));
                    Log.d("NETWORKINGNEWCONNECT", String.valueOf(items));
                }
                full = new ArrayList[]{available, items};
                Log.d("ReturningFULL", Arrays.toString(full));

                list = full;
                } catch (Exception e) {
                Log.d("newConnection 2:", "Something went wrong while getting data.");
                Log.d("ErrorLog:", String.valueOf(e));
                // If there is an exception, return the empty list, to be handled by code.
                list = full;
                parent.setAvailable(available);
                parent.setItemList(items);
            }
        } catch (Exception e) {
            Log.d("newConnection 1:", "Something went wrong while creating ip/sockets");
            Log.d("ErrorLog", String.valueOf(e));
            // If there is an exception, return the empty list, to be handled by code.
            list = full;
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

    public ArrayList[] get() {Log.d("Returning" , Arrays.toString(full)); return full;}
    public String ip() {return currentIP;}
    public static String readString(DataInputStream in) {
        int length=0;
        ArrayList<Character> message = new ArrayList<>();
        int checks = 0;
        while (checks < 33) {
            try {
                length = in.readInt();
                break;
            } catch (EOFException e) {
                Log.d("No message so", "looping");
                wait(5);
                checks++;
            } catch (IOException e) {
                Log.d("Server", "Stream Closed");
                return null;
            }
        }
        if (checks == 33) {
            return null;
        }
        for (int i = 0; i < length; i++) {
            while (true) {
                try {
                    message.add(in.readChar());
                    break;
                } catch (EOFException e) {
                    Log.d("No message so", "looping");
                    wait(5);
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
    public static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            Log.d("ExceptionServer", "Waiting.");
        }
    }
    public static Integer readInt(DataInputStream in) {
        int count = 0;
        while (true) {
            try {
                return in.readInt();
            } catch (EOFException e) {
                Log.d("No message so", "looping");
                count++;
                if (count == 10000) {
                    return null;
                }
            } catch (IOException e) {
                Log.d("Server", "Stream Closed");
                return null;
            }
        }
    }
}



