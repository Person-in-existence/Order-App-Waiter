package com.example.orderappwaiter;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.orderappwaiter.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import networking.Network;
import networking.SessionData;
import networking.Order;

public class MainActivity extends AppCompatActivity {
    public static final short DEVICE_TYPE = 0; // 0 for waiter
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public String currentIP = "";
    public ArrayList<Integer> quantities = new ArrayList<>();
    public ArrayList<String> names = new ArrayList<>();
    public ArrayList<Integer> orderList = new ArrayList<>();
    public String serverID;
    public FirstFragment fragment;
    public volatile boolean locked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Up here so it is done before other things are made
        names = new ArrayList<>();
        quantities = new ArrayList<>();
        for (int i =0; i < 8; i++) {
            quantities.add(0);
            names.add("");
            orderList.add(0);
        }

        Network.setActivity(this);

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

        Log.d("ItemListLength", String.valueOf(names.size()));
        Log.d("Lists", String.valueOf(quantities.get(0)));
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

    public void newConnection(String newServerID, SecondFragment secondFragment) {
        Network.joinServer(newServerID);
    }
    public void joinedServer(boolean success) {
        if (success) {
            Snackbar.make(findViewById(android.R.id.content), "Connection Success!", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Connection Failed. Try checking the other device is online, and on the same network, as this device.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    public int makeChecksum() {
        int total = 0;
        for (int index = 0; index < quantities.size(); index++) {
            int quantity = quantities.get(index) + orderList.get(index);
            total += (int) (Math.pow(7, index) * quantity);
        }
        return total;
    }

    public ArrayList<Integer> getAvailable() {return quantities;}
    public ArrayList<String> getItems() {return names;}
    public void setFragment(FirstFragment newFragment) {
        fragment = newFragment;
        if (newFragment != null) {
            fragment.updateUi();
        }
    }
    public void setAvailable(ArrayList<Integer> quantities) {
        this.quantities = quantities;
        if (fragment != null) {
            fragment.updateUi();
        }
    }
    public void setNameList(ArrayList<String> names) {
        this.names = names;
        if (fragment != null) {
            fragment.updateUi();
        }
    }
    public void setSessionData(SessionData data) {
        // TODO: CURRENT ORDER THINGS - OTHERWISE WE HAVE TOO MUCH BECAUSE ORDER EXISTS TOO AND CHECKSUM WILL BE INFINITE LOOP
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> quantities = new ArrayList<>();

        boolean currentOrderAffected = false;
        for (int index = 0; index < data.items.length; index++) {
            SessionData.SessionItem item = data.items[index];
            names.add(item.name);
            // Adjust for orders as well - quantity = item.quantity - ordered. If negative, set to whatever remains and notify user
            // Protect against error if size mismatch
            if (index < orderList.size()) {
                int quantity = item.quantity - orderList.get(index);
                if (quantity >= 0) {
                    quantities.add(quantity);
                } else {
                    // Quantity is 0 - set order to whatever is left and warn user
                    quantities.add(0);
                    orderList.set(index, item.quantity);
                    currentOrderAffected = true;
                }
            } else {
                Log.e("MainActivity", "Size mismatch in setSessionData: orderList.size() " + orderList.size() + " data.items.length " + data.items.length);
                quantities.add(item.quantity);
            }
        }

        // Show a warning to the user if the order has changed
        if (currentOrderAffected) {
            showSnackbar("Item stock changed: Order Affected");
        }

        this.names = names;
        this.quantities = quantities;
        updateFragment();
    }
    public SessionData getSessionData() {
        SessionData.SessionItem[] items = new SessionData.SessionItem[names.size()];
        for (int index = 0; index < names.size(); index++) {
            SessionData.SessionItem item = new SessionData.SessionItem(names.get(index), quantities.get(index));
            items[index] = item;
        }
        return new SessionData(items);
    }
    public void onConnectionLost() {
        showSnackbar("Connection Lost!");

    }
    public void onOrderSent(boolean success) {
        Log.d("MainActivity", "onOrderSent: " + success);
        if (success) {
            runOnUiThread(()->{
                showSnackbar("Order sent successfully!");
                // Clear order data from screen
                if (fragment != null) {
                    fragment.clearUi();
                } else {
                    Log.w("MainActivity", "Tried to clear fragment but it was null");
                }
            });
        } else {
            showSnackbar("Sending order failed");
        }
        // Unlock controls
        locked = false;

    }
    public void addRemoveItemsByAmounts(Order order) {
        for (Order.OrderItem item: order.items) {
            int index = item.itemID;

            // Equivalent to quantities[index] -= item.quantity
            quantities.set(index, (quantities.get(index) - item.quantity));
        }
        // Update first fragment UI so we see the changes
        updateFragment();
    }

    public void sendOrder(Order order) {
        locked = true;
        Network.sendOrder(order);

    }
    public void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content).getRootView(), message, Snackbar.LENGTH_LONG).show();
    }
    private void updateFragment() {
        runOnUiThread(()->{
            if (fragment != null) {
                fragment.updateUi();
            }
        });
    }

}

