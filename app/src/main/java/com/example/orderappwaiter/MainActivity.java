package com.example.orderappwaiter;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.orderappwaiter.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public String currentIP = "";
    public ArrayList<Integer> availableList = new ArrayList<>();
    public ArrayList<String> itemList;
    public String serverID;
    public FirstFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        /*
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        itemList = new ArrayList<>();
        availableList = new ArrayList<>();
        for (int i =0; i < 8; i++) {
            availableList.add(0);
            itemList.add("");
        }
        Log.d("ItemListLength", String.valueOf(itemList.size()));
        Log.d("Lists", String.valueOf(availableList.get(0)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public ArrayList[] newConnection(String newServerID) {
        NetworkingNewConnect networkingNewConnect = new NetworkingNewConnect();
        serverID = newServerID;
        networkingNewConnect.parent = this;
        networkingNewConnect.serverID = newServerID;
        Thread thread = new Thread(networkingNewConnect);
        thread.start();
        try {
            thread.join(3000);
            // Fixed?
            if (networkingNewConnect.get() == null) {
                Log.d("NULLRETURN", "D:");
                //return null;
            }
            currentIP = networkingNewConnect.ip();
            Log.d("IP", currentIP);
            ArrayList[] result = networkingNewConnect.get();
            try {
                availableList = result[0];
                itemList = result[1];
            } catch (Exception e) {
                Log.d("NULLRETURN", "106-107");
                e.printStackTrace();
            }
            return result;
        } catch (Exception e){
            Log.d("NULLRETURN", String.valueOf(e));
            return null;
        }
    }
    public ArrayList<Integer> getAvailable() {return availableList;}
    public ArrayList<String> getItems() {return itemList;}
    public void setFragment(FirstFragment newFragment) {
        fragment = newFragment;
        if (newFragment != null) {
            fragment.updateUi();
        }
    }
    public void setAvailable(ArrayList<Integer> availables) {availableList = availables;if (fragment != null) {fragment.updateUi();}}
    public void setItemList(ArrayList<String> items) {itemList = items; if (fragment != null) {fragment.updateUi();}
    }
    /*
    public static ArrayList[] newConnection(String serverID) {
        // Backup function - throws network on main thread Exception.
        // Makes Socket global
        Socket socket;
        // Creates the two ArrayLists which will be returned
        ArrayList<Integer> available = new ArrayList<Integer>();
        ArrayList<String> items = new ArrayList<String>();
        // Creates the Array "Full", which contains the two ArrayLists "Available" and "Items"
        ArrayList[] full = {available, items};
        // Creates the Array "Empty", to be returned in case of an error.
        ArrayList[] empty = {};
        try {
            // gets the IP address of the current device
            String ip = Inet4Address.getLocalHost().getHostAddress();
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
            // Creates a socket (global to function, see above) on port 65432, using the IP to_connect
            socket = new Socket(to_connect, 65432);

        } catch (Exception e) {
            Log.d("newConnection 1:", "Something went wrong while creating ip/sockets");
            Log.d("ErrorLog", String.valueOf(e));
            // If there is an exception, return the empty list, to be handled by code.
            return empty;
        }
        try {
            //Creates the DataOutputStream to the server
            OutputStream to_server = socket.getOutputStream();
            DataOutputStream out = new DataOutputStream(to_server);
            // Creates the dataInputStream from the server
            InputStream from_server = socket.getInputStream();
            DataInputStream in = new DataInputStream(from_server);
            // Loops through the 8 integers sent for available
            for (int index = 0; index < 8; index++) {
                int count = 0;
                // Tries each item up to ten times if not enough data
                while (count < 10) {
                    int current = in.readInt();
                    if (current != -1) {
                        available.add(current);
                        break;
                    }
                    count++;
                }

                for (int indexitem = 0; indexitem < 8; indexitem++) {
                    // Gets the length of the next string
                    int length = in.readInt();
                    // The final string
                    String currentString = "";
                    // Loops through the sent message with length as the number of loops
                    for (int indexchar = 0; indexchar < length; indexchar++) {
                        // gets the current character and adds it to the string.
                        char currentchar = in.readChar();
                        currentString = currentString + currentchar;
                    }
                    // adds the current string to the "items" list
                    items.add(currentString);
                }
            }
            // Returns both lists in an array, with item zero being "available", and item one being "items"
            return full;
        } catch (Exception e) {
            Log.d("newConnection 2:", "Something went wrong while getting data.");
            Log.d("ErrorLog:", String.valueOf(e));
            // If there is an exception, return the empty list, to be handled by code.
            return empty;
        }
    }
    public static void update_available(ArrayList<Integer> available) {

    }
    public static void setCurrentIP(String value) {
        currentIP = value;
    }
    public static String getCurrentIP() {
        return currentIP;
    }

 */
}

