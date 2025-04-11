package networking;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.orderappwaiter.MainActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import networking.packets.Header;
import networking.packets.Type0;
import networking.packets.Type1;


public class Network {
    public static final String subnet = "192.168.1.";
    public static final short NETWORK_VERSION_NUMBER = 2;
    public static final int PORT = 65433;
    public static final boolean useIPv4 = false;

    private static Connection connection;
    protected static MainActivity activity;
    private static final AtomicLong orderID = new AtomicLong(0);
    private static boolean serverJoinReturned = false;

    public static ArrayList<Device> scanDevices(@Nullable NetworkScanner.ProgressBarUpdate progressBar) {
        return NetworkScanner.scan(progressBar);
    }

    /**
     * Joins the server with the given join code (last IP part).
     * @param joinCode The code of the server to join. This is the final part of the IP.
     * @return Whether the connection was successful.
     */
    public static void joinServer(String joinCode) {
        Log.v("networking.Network", "Trying to join server: " + joinCode);
        serverJoinReturned = false;
        new Thread() {
            @Override
            public void run() {
                try {
                    // TODO: EVALUATE
                    // FIX FOR MULTIPLE SERVER CONNECTIONS SAME DEVICE
                    if (connection != null) {
                        connection.close(true);
                    }
                    String ip = subnet + joinCode;
                    Socket socket = new Socket(ip, PORT);
                    connection = new Connection(socket, true, Network::notifyConnectionLost);
                    // Request session data from connection
                    Type0 packet = new Type0(new Header(NETWORK_VERSION_NUMBER, (short) 0, connection.getIdempotency()));
                    packet.setSendListener(success -> {
                        serverJoinReturned = true;
                        activity.joinedServer(success);
                        Log.d("networking.Network", "Returned, success: " + success);
                    });
                    connection.sendPacket(packet);
                    Thread.sleep(1000); // Wait 1 second
                    // If false, trigger activity.joinedServer(false)
                    if (!serverJoinReturned) {
                        Log.w("networking.Network", "Server join failed to return");
                        // Shut the connection - it hasn't worked.
                        connection.close(false);
                        activity.joinedServer(false);
                    }
                } catch (Exception e) {e.printStackTrace();}
            }
        }.start();

    }
    private static void notifyConnectionLost(Connection ignored) {
        Log.d("networking.Network", "Connection lost!");
        activity.onConnectionLost();
    }
    public static long getNewOrderID() {
        return orderID.incrementAndGet();
    }

    public static void removeOrderByID(long orderID) {
        Log.e("networking.Network", "Received call to removeOrderByID, not expected on a waiter device. assert false");
        assert false;
    }
    public static void setSessionData(SessionData data) {
        activity.setSessionData(data);
    }
    static void addOrderAndUpdate(Order order, Connection connection) {
        Log.e("networking.Network", "Received call to addOrderAndUpdate, not expected on a waiter device. assert false");
        assert false;
    }
    public static void setOrderData(OrderData data) {
        Log.e("networking.Network", "Received call to setOrderData, not expected on a waiter device. assert false");
        assert false;
    }
    public static void addOrder(Order order) {
        Log.e("networking.Network","Received call to addOrder, not expected on a waiter device. assert false");
        assert false;
    }
    public static boolean addOrderChecksum(Order order, int receivedChecksum) {
        // Add the order
        addOrder(order);

        // Calculate checksum
        int actualChecksum = activity.makeChecksum();

        return receivedChecksum == actualChecksum;
    }
    public static boolean removeOrderChecksum(long orderID, int receivedChecksum) {
        // Remove the order
        removeOrderByID(orderID);

        // Check checksum
        int actualChecksum = activity.makeChecksum();

        return receivedChecksum == actualChecksum;
    }
    public static boolean addRemoveItemsByAmount(Order amounts, int checksum) {
        activity.addRemoveItemsByAmounts(amounts);

        // Check checksum
        return activity.makeChecksum() == checksum;
    }
    public static void sendOrder(Order order) {
        if (connection == null) {
            Log.w("networking.Network", "Connection not open");
            activity.showSnackbar("Not connected to a device");
            return;
        }
        if (!connection.isOpen()) {
            Log.w("networking.Network", "Connection not open");
            activity.showSnackbar("Not connected to a device");
            return;
        }
        Log.v("networking.Network", "sending order");
        Type1 packet = new Type1(new Header(NETWORK_VERSION_NUMBER, (short) 1, connection.getIdempotency()), order);
        packet.setSendListener(activity::onOrderSent);
        connection.sendPacket(packet);
    }
    public static OrderData getOrderData() {
        Log.w("networking.Network", "Received call to getOrderData, not expected on a waiter device. assert false | return null");
        assert false;
        return null;
    }


    protected static SessionData getSessionData() {
        return activity.getSessionData();
    }

    protected static short getDeviceType() {
        return MainActivity.DEVICE_TYPE;
    }
    protected static String getDeviceName() {
        return "Names haven't been implemented yet.";
    }



    public static void writeString(String string, DataOutputStream out) throws IOException {
        int stringLength = string.length();
        out.writeInt(stringLength);
        for (int index = 0; index < stringLength; index++) {
            out.writeChar(string.charAt(index));
        }
    }

    public static String readString(DataInputStream in) throws IOException {
        int stringLength = in.readInt();
        StringBuilder stringBuilder = new StringBuilder(stringLength);
        for (int index = 0; index < stringLength; index++) {
            stringBuilder.append(in.readChar());
        }
        return stringBuilder.toString();
    }

    public static String getIPAddress() {
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
                        if (ipAddr != null) {
                            boolean isIPv4 = ipAddr.indexOf('.') < 3;

                            if (isIPv4 && !useIPv4) {
                                continue;
                            }
                            if (useIPv4 && !isIPv4) {
                                int delim = ipAddr.indexOf('%'); // drop ip6 zone suffix
                                ipAddr = delim < 0 ? ipAddr.toUpperCase() : ipAddr.substring(0, delim).toUpperCase();
                            }
                            return ipAddr;
                        }
                    }
                }

            }
        } catch (Exception ignored) { } // if we can't connect, just return empty string
        return "";
    }

    public static void setActivity(MainActivity activity) {
        Network.activity = activity;
    }

}
