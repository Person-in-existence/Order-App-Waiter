package com.example.orderappwaiter;

import android.util.Log;

import com.google.android.material.snackbar.Snackbar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class Connection {
    public static final short TIMEOUT = 1000; // one second (ms)
    public static final short NETWORK_VERSION_NUMBER = 1;
    public MainActivity activity;
    public String serverIP;
    public int idempotencyToken = 0;
    public Socket socket;
    private Order previousOrder;
    private boolean previousSuccess;
    public Connection(String serverID, MainActivity activity) throws IOException {
        this.activity = activity;
        // Make the server IP with the first 3 parts of this device's IP address and the server ID
        String ip = getIPAddress();
        String[] bits = ip.split("\\.");
        serverIP = bits[0] + "." + bits[1] + "." + bits[2] + "." + serverID;
        socket = new Socket(serverIP, 65433);
        socket.setSoTimeout(TIMEOUT);

    }
    public ItemData getData() {
        // Enclose with a for loop, to retry once in case it fails.
        short numTries = 2;
        for (int time = 0; time < numTries; time++) {
            try {
                // Send the outgoing packet to the server
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream out = new DataOutputStream(outputStream);
                // Version Number: 1 - version 2
                out.writeShort(1);
                // Packet type: 0 - new connection/client requesting information
                out.writeShort(0);
                // Idempotency Token
                out.writeInt(idempotencyToken);
                // Empty packet body

                // Get data from server
                InputStream inputStream = socket.getInputStream();
                DataInputStream in = new DataInputStream(inputStream);

                // Before all reads, wait on in having data to read, to make sure we don't reach the end of the stream
                // Check version number == 0. If not, show an error to the client
                System.out.println("Connection62");
                short versionNumber = readShort(in);
                System.out.println("Connection64");
                if (versionNumber != NETWORK_VERSION_NUMBER) {
                    if (time < numTries - 1) {
                        safeWait(TIMEOUT);
                        // Clear the input stream
                        in.skip(in.available());
                        continue;
                    } else {
                        Snackbar.make(activity.findViewById(android.R.id.content), R.string.incompatible_client_server_version, Snackbar.LENGTH_LONG)
                                .setAction("Error", null).show();
                        break;
                    }
                }
                System.err.println("Connection 75");
                // Check packet type is 2
                short packetType = readShort(in);
                if (packetType != 2) {
                    if (time < numTries - 1) { // Retry
                        continue;
                    } else {
                        Snackbar.make(activity.findViewById(android.R.id.content), R.string.network_error, Snackbar.LENGTH_LONG)
                                .setAction("Error", null).show();
                        break;
                    }
                }

                // Read (and disregard) idempotency token
                readInt(in);

                // Main packet body
                // Number of items: short
                short numItems = readShort(in);
                ArrayList<String> itemNames = new ArrayList<>();
                ArrayList<Integer> itemQuantities = new ArrayList<>();
                for (int itemNumber = 0; itemNumber < numItems; itemNumber++) {
                    // Read the item name
                    itemNames.add(readString(in));
                    // Read the item amount
                    itemQuantities.add(readInt(in));
                }
                // End the connection
                // Increment the idempotencyToken
                idempotencyToken++;
                // Create and return an ItemData
                return new ItemData(itemNames, itemQuantities);
            } catch (IOException ie) {
                try {
                    Log.e("OrderAppWaiter Networking", "Networking failed with IOException, attempting reconnect: " + Arrays.toString(ie.getStackTrace()) + ie.getClass());
                    reconnect();
                } catch (Exception e) {
                    Log.e("OrderAppWaiter Networking", "Reconnecting failed");
                }
            } catch (Exception e) {
                Log.e("OrderAppWaiter Networking", "An error occurred in the getServer() method. " + Arrays.toString(e.getStackTrace()));
            }
        }
        Snackbar.make(activity.findViewById(android.R.id.content), R.string.connection_error, Snackbar.LENGTH_LONG)
                .setAction("Error", null).show();
        System.err.println("Connection 113");
        return null;
    }
    public boolean sendOrder(Order order) {
        // If the last order failed and this order is different, force update the idempotency token
        if (!previousSuccess & (!Objects.equals(previousOrder, order))) {
            idempotencyToken++;
        }
        int numTries = 4;
        for (int i = 0; i < 4; i++) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream out = new DataOutputStream(outputStream);

                // Packet Header
                // Version Number
                writeShort(out, NETWORK_VERSION_NUMBER);
                // Packet type 1 (Order being sent (client->server))
                writeShort(out, (short) 1);
                // Idempotency token
                writeInt(out, idempotencyToken);

                // Packet Body
                ArrayList<Short> usedPositions = new ArrayList<>();
                ArrayList<Integer> usedQuantities = new ArrayList<>();
                // Get all the non-0 items in the order to send
                for (int index = 0; index < order.getLength(); index++) {
                    if (order.orders.get(index) != 0) {
                        usedPositions.add((short) index);
                        usedQuantities.add(order.orders.get(index));
                    }
                }
                // Customer Name
                writeString(out, order.name);
                // Number of Items
                writeShort(out, (short) usedPositions.size());
                for (int index = 0; index < usedPositions.size(); index++) {
                    // Item ID
                    writeShort(out, usedPositions.get(index));
                    // Item Quantity
                    writeInt(out, usedQuantities.get(index));
                }

                // Check confirmation (type 3)
                InputStream inputStream = socket.getInputStream();
                DataInputStream in = new DataInputStream(inputStream);

                waitAvailable(in);
                // Version Number
                short versionNumber = readShort(in);


                if (versionNumber != NETWORK_VERSION_NUMBER) {
                    safeWait(TIMEOUT);
                    // Clear the input stream, as it is probably corrupted or out of phase
                    in.skip(in.available());
                    continue;
                }
                // Packet Type
                short packetType = readShort(in);
                // Check packet type is 3 - the server confirming it received the packet. If not, retry
                if (packetType != 3) {
                    safeWait(TIMEOUT);
                    // Clear the input stream, as it is probably corrupted or out of phase
                    in.skip(in.available());
                    continue;
                }
                // Idempotency token - must match the current idempotency token
                int serverIdempotencyToken = readInt(in);
                if (serverIdempotencyToken != idempotencyToken) {
                    safeWait(TIMEOUT);
                    // Clear the input stream, as it is probably corrupted or out of phase
                    in.skip(in.available());
                    continue;
                }
                // Read the boolean for whether it was successful. If it was, exit the function, otherwise, try again
                boolean success = readBoolean(in);
                if (success) {
                    idempotencyToken++;
                    previousSuccess = true;
                    return true;
                } // If this is expanded, an else clause, with a continue statement, must be added. It is omitted as this is the end of the loop.


            } catch (IOException e) {
                Log.e("OrderAppWaiter Networking", "sendOrder() Attempt failed.");
                safeWait(TIMEOUT);
            }
        }
        // If it was unsuccessful, return false and do not update the idempotency token. Log the previous order in this class. This means that if the server
        previousSuccess = false;
        previousOrder = order;
        return false;
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
                        assert ipAddr != null;
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
    public void waitAvailable(DataInputStream in) {
        try {
            if (in.available() != 0) {
                return;
            }
            short counter = 0;
            while ((in.available() == 0) & (counter < TIMEOUT)) {
                safeWait(1);
                counter++;

            }
        } catch (IOException e) {
            Log.e("OrderAppWaiter Networking", "Wait I/O Exception");
        }
    }
    // FAIL
    public short readShort(DataInputStream in) throws IOException {
        System.out.println("Connection260");
        waitAvailable(in);
        System.out.println("Connection262");
        short tries = 0;
        System.out.println("Connection264");
        while (tries < 10) {
            try {
                return in.readShort(); // Blocks.
            } catch (Exception e) {
                safeWait(10);
                Log.e("OrderAppWaiter Networking", "reading short failed " + Arrays.toString(e.getStackTrace()));
                tries++;
            }
        }
        throw new IOException();
    }
    public int readInt(DataInputStream in) throws IOException {
        waitAvailable(in);
        short tries = 0;
        while (tries < 10) {
            try {
                return in.readInt();
            } catch (IOException e) {
                Log.e("OrderAppWaiter Networking", "reading int failed " + Arrays.toString(e.getStackTrace()));
                tries++;
            }
        }
        throw new IOException();
    }
    public char readChar(DataInputStream in) throws IOException {
        waitAvailable(in);
        short tries = 0;
        while (tries < 10) {
            try {
                return in.readChar();
            } catch (IOException e) {
                Log.e("OrderAppWaiter Networking", "reading char failed " + Arrays.toString(e.getStackTrace()));
                tries++;
            }
        }
        throw new IOException();
    }
    public String readString(DataInputStream in) throws IOException {
        try {
            int stringLength = readInt(in);
            StringBuilder stringBuilder = new StringBuilder(stringLength);
            for (int i = 0; i < stringLength; i++) {
                stringBuilder.append(readChar(in));
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            Log.e("OrderAppWaiter Networking", "Failed to read string " + Arrays.toString(e.getStackTrace()));
            throw new IOException();
        }
    }
    public boolean readBoolean(DataInputStream in) throws IOException {
        waitAvailable(in);
        short tries = 0;
        while (tries < 10) {
            try {
                return in.readBoolean();
            } catch (IOException e) {
                Log.e("OrderAppWaiter Networking", "reading boolean failed " + Arrays.toString(e.getStackTrace()));
                tries++;
            }
        }
        throw new IOException();
    }
    public void writeShort(DataOutputStream out, short outShort) throws IOException {
        out.writeShort(outShort);
    }
    public void writeInt(DataOutputStream out, int outInt) throws IOException {
        out.writeInt(outInt);
    }
    public void writeChars(DataOutputStream out, String chars) throws IOException {
        out.writeChars(chars);
    }
    public void writeString(DataOutputStream out, String string) throws IOException {
        int length = string.length();
        out.writeInt(length);
        writeChars(out, string);
    }
    public void dispose() {
        try {
            // Send a packet type 5
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream out = new DataOutputStream(outputStream);
            // Version number
            out.writeShort(NETWORK_VERSION_NUMBER);
            // Packet type 5, for disconnect
            out.writeShort(5);
            // Idempotency token
            out.writeInt(idempotencyToken);

            // Disconnect
            socket.close();
            Log.d("OrderAppWaiter Networking", "Disconnected from server.");
        } catch (IOException e) {
            try {
                socket.close();
                Log.e("OrderAppWaiter Networking", "Error sending disconnect packet, connection closed.");
            } catch (IOException ex) {
                Log.e("OrderAppWaiter Networking", "Error closing socket");
            }
        }
    }
    public void safeWait(int ms) {
        try {
            System.out.println("Connection368");
            Thread.sleep(ms);
        } catch (Exception e) {
            Log.e("OrderAppWaiter Networking", "Wait fail " + Arrays.toString(e.getStackTrace()));
        }
    }
    public void reconnect() throws IOException {
        socket = new Socket(serverIP, 65433);
        socket.setSoTimeout(TIMEOUT);    }
}
