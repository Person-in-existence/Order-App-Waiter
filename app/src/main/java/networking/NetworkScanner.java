package networking;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import networking.packets.Header;
import networking.packets.Type7;
import networking.packets.Type8;

class NetworkScanner {
    public static final int scanWaitTime = 8750; // Time in ms
    public static ArrayList<Device> scan(@Nullable ProgressBarUpdate progressBar)  {
        Device[] devices = new Device[256];

        String thisPart = Network.getIPAddress().split("\\.")[3];
        int finalPart = Integer.parseInt(thisPart);
        Type7 packet = new Type7(new Header(Network.NETWORK_VERSION_NUMBER, (short) 7, -1));
        for (int index = 0; index < 256; index++) {
            int finalIndex = index;
            new Thread() {
                public void run() {

                    Log.d("NetworkScanner",String.valueOf(finalIndex));
                    // Dont detect this device
                    if(finalIndex ==finalPart)  {
                        return;
                    }

                    String ip = Network.subnet + finalIndex;
                    try {
                        Socket socket = new Socket(ip, Network.PORT);

                        // Send a type 7 packet
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        DataInputStream in = new DataInputStream(socket.getInputStream());
                        socket.setSoTimeout(1000);


                        packet.send(out);
                        // End packet


                        Header header = new Header(in);

                        if (header.type != 8) {
                            Log.e("networking.NetworkScanner", "Packet type other than 8 received!");
                            out.close();
                            socket.close();
                            return;
                        }
                        Type8 packet = new Type8(header, in);

                        // Add the device
                        devices[finalIndex] = packet.makeDevice(socket.getInetAddress());
                        Log.v("networking.NetworkScanner", "Device success! IP " + socket.getInetAddress());

                        // Close in
                        in.close();
                        // Close out
                        out.close();

                        socket.close();
                    } catch(IOException e){
                        Log.d("NetworkScanner", Network.subnet + finalIndex + " exception: " + e.getMessage());
                        Log.d("NetworkScanner", Arrays.toString(e.getStackTrace()));
                    }
                }

            }.start();
        }
        // Check whether the progressbar needs to be updated
        if (progressBar != null) {
            long currentMillis = System.currentTimeMillis();
            long target = currentMillis + scanWaitTime;
            while (currentMillis <= target) {
                long difference = target-currentMillis;
                int progress = (int) (100-((double) difference)/scanWaitTime*100);
                System.out.println(progress);
                progressBar.update(progress);
                currentMillis = System.currentTimeMillis();
                try {
                    Thread.sleep(scanWaitTime/100);
                } catch (InterruptedException ignored) {}

            }
        } else {
            // If not, just sleep for 1000
            try {
                Thread.sleep(scanWaitTime);
            } catch (InterruptedException ignored) {}
        }

        System.out.println(Arrays.toString(devices));
        // Put into a smaller array
        ArrayList<Device> arrayListDevices = new ArrayList<>();
        for (Device device : devices) {
            if (device != null) {
                arrayListDevices.add(device);
            }
        }
        System.out.println(arrayListDevices.size());
        return arrayListDevices;
    }
    public interface ProgressBarUpdate {
        void update(int progress);
    }

}
