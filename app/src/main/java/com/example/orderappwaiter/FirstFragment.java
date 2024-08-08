package com.example.orderappwaiter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.orderappwaiter.databinding.FragmentFirstBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private MainActivity activity;
    public ArrayList<Integer> availableList;
    public ArrayList<Integer> orderList;
    public ArrayList<String> itemList;
    public TextView[] orderListItems;
    public TextView[] availableListItems;
    public TextView[] itemListItems;
    public boolean second = false;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = (MainActivity)getActivity();
        binding.buttonFirst.setOnClickListener(view117 -> NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment));
        // Creates lists of items. Uses ArrayLists to code start easier.
        orderList = new ArrayList<>();
        for (int ordercount = 0; ordercount < 8; ordercount++) {
            orderList.add(0);
        }
        availableList = activity.getAvailable();
        System.out.println("Length of available is" + availableList.size());
        itemList = activity.getItems();
        try {
            updateUi();
        } catch( Exception e) {
            Log.d("UpdateUIError", String.valueOf(e));
        }
        // Creates lists of the UI elements
        orderListItems = new TextView[]{binding.orderedOne, binding.orderedTwo, binding.orderedThree, binding.orderedFour, binding.orderedFive, binding.orderedSix, binding.orderedSeven, binding.orderedEight};
        availableListItems = new TextView[]{binding.availableOne, binding.availableTwo, binding.availableThree, binding.availableFour, binding.availableFive, binding.availableSix, binding.availableSeven, binding.availableEight};
        itemListItems = new TextView[]{binding.itemOne, binding.itemTwo, binding.itemThree, binding.itemFour, binding.itemFive, binding.itemSix, binding.itemSeven, binding.itemEight};
        // Creates the button variables
        Button increaseOne = binding.increaseOne;
        Button increaseTwo = binding.increaseTwo;
        Button increaseThree = binding.increaseThree;
        Button increaseFour = binding.increaseFour;
        Button increaseFive = binding.increaseFive;
        Button increaseSix = binding.increaseSix;
        Button increaseSeven = binding.increaseSeven;
        Button increaseEight = binding.increaseEight;
        // Sets the listeners for buttons
        // fix for null
        increaseOne.setOnClickListener(view19 -> {
            if (availableList.get(0) > 0) {
                orderList.set(0, orderList.get(0) + 1);
                orderListItems[0].setText(String.valueOf(orderList.get(0)));
                availableList.set(0, availableList.get(0)-1);
                activity.availableList.set(0, availableList.get(0));
                availableListItems[0].setText(String.valueOf(availableList.get(0)));
            }
        });
        increaseTwo.setOnClickListener(view110 -> {
            if (availableList.get(1) > 0) {
                orderList.set(1, orderList.get(1) + 1);
                binding.orderedTwo.setText(String.valueOf(orderList.get(1)));
                availableList.set(1, availableList.get(1)-1);
                activity.availableList.set(1, availableList.get(1));
                availableListItems[1].setText(String.valueOf(availableList.get(1)));
            }
        });
        increaseThree.setOnClickListener(view111 -> {
            int x = 2;
            if (availableList.get(x) > 0) {
                orderList.set(x, orderList.get(x) + 1);
                orderListItems[x].setText(String.valueOf(orderList.get(x)));
                availableList.set(x, availableList.get(x)-1);
                activity.availableList.set(x, availableList.get(x));
                availableListItems[x].setText(String.valueOf(availableList.get(x)));

            }
        });
        increaseFour.setOnClickListener(view112 -> {
            int x = 3;
            if (availableList.get(x) > 0) {
                orderList.set(x, orderList.get(x) + 1);
                orderListItems[x].setText(String.valueOf(orderList.get(x)));
                availableList.set(x, availableList.get(x)-1);
                activity.availableList.set(x, availableList.get(x));
                availableListItems[x].setText(String.valueOf(availableList.get(x)));
            }
        });
        increaseFive.setOnClickListener(view113 -> {
            int x = 4;
            if (availableList.get(x) > 0) {
                orderList.set(x, orderList.get(x) + 1);
                orderListItems[x].setText(String.valueOf(orderList.get(x)));
                availableList.set(x, availableList.get(x)-1);
                activity.availableList.set(x, availableList.get(x));
                availableListItems[x].setText(String.valueOf(availableList.get(x)));

            }
        });
        increaseSix.setOnClickListener(view114 -> {
            int x = 5;
            if (availableList.get(x) > 0) {
                orderList.set(x, orderList.get(x) + 1);
                orderListItems[x].setText(String.valueOf(orderList.get(x)));
                availableList.set(x, availableList.get(x)-1);
                activity.availableList.set(x, availableList.get(x));
                availableListItems[x].setText(String.valueOf(availableList.get(x)));
            }
        });
        increaseSeven.setOnClickListener(view115 -> {
            int x = 6;
            if (availableList.get(x) > 0) {
                orderList.set(x, orderList.get(x) + 1);
                orderListItems[x].setText(String.valueOf(orderList.get(x)));
                availableList.set(x, availableList.get(x)-1);
                activity.availableList.set(x, availableList.get(x));
                availableListItems[x].setText(String.valueOf(availableList.get(x)));
            }
        });
        increaseEight.setOnClickListener(view116 -> {
            int x = 7;
            if (availableList.get(x) > 0) {
                orderList.set(x, orderList.get(x) + 1);
                orderListItems[x].setText(String.valueOf(orderList.get(x)));
                availableList.set(x, availableList.get(x)-1);
                activity.availableList.set(x, availableList.get(x));
                availableListItems[x].setText(String.valueOf(availableList.get(x)));
            }
        });

        // Creates the Decrease variables
        Button decreaseOne = binding.decreaseOne;
        Button decreaseTwo = binding.decreaseTwo;
        Button decreaseThree = binding.decreaseThree;
        Button decreaseFour = binding.decreaseFour;
        Button decreaseFive = binding.decreaseFive;
        Button decreaseSix = binding.decreaseSix;
        Button decreaseSeven = binding.decreaseSeven;
        Button decreaseEight = binding.decreaseEight;
        decreaseOne.setOnClickListener(view14 -> {
            int x = 0;
            if (orderList.get(x)> 0) {
                orderList.set(x, orderList.get(x)-1);
                orderListItems[x].setText(String.valueOf(orderList.get(x)));
                availableList.set(x, availableList.get(x)+1);
                activity.availableList.set(0, availableList.get(0));
                availableListItems[x].setText(String.valueOf(availableList.get(x)));
            }
        });
        decreaseTwo.setOnClickListener(view13 -> {
            int x = 1;
            if (orderList.get(x)> 0) {
                orderList.set(x, orderList.get(x)-1);
                orderListItems[x].setText(String.valueOf(orderList.get(x)));
                availableList.set(x, availableList.get(x)+1);
                activity.availableList.set(1, availableList.get(1));
                availableListItems[x].setText(String.valueOf(availableList.get(x)));
            }
        });
        decreaseThree.setOnClickListener(view12 -> {
            int x = 2;
            if (orderList.get(x)> 0) {
                orderList.set(x, orderList.get(x)-1);
                orderListItems[x].setText(String.valueOf(orderList.get(x)));
                availableList.set(x, availableList.get(x)+1);
                activity.availableList.set(x, availableList.get(x));
                availableListItems[x].setText(String.valueOf(availableList.get(x)));
            }
        });
        decreaseFour.setOnClickListener(view1 -> {
            int x = 3;
            if (orderList.get(x)> 0) {
                orderList.set(x, orderList.get(x)-1);
                orderListItems[x].setText(String.valueOf(orderList.get(x)));
                availableList.set(x, availableList.get(x)+1);
                activity.availableList.set(x, availableList.get(x));
                availableListItems[x].setText(String.valueOf(availableList.get(x)));
            }
        });
        decreaseFive.setOnClickListener(view15 -> {
            int x = 4;
            if (orderList.get(x)> 0) {
                orderList.set(x, orderList.get(x)-1);
                orderListItems[x].setText(String.valueOf(orderList.get(x)));
                availableList.set(x, availableList.get(x)+1);
                activity.availableList.set(x, availableList.get(x));
                availableListItems[x].setText(String.valueOf(availableList.get(x)));
            }
        });
        decreaseSix.setOnClickListener(view16 -> {
            int x = 5;
            if (orderList.get(x)> 0) {
                orderList.set(x, orderList.get(x)-1);
                orderListItems[x].setText(String.valueOf(orderList.get(x)));
                availableList.set(x, availableList.get(x)+1);
                activity.availableList.set(x, availableList.get(x));
                availableListItems[x].setText(String.valueOf(availableList.get(x)));
            }
        });
        decreaseSeven.setOnClickListener(view17 -> {
            int x = 6;
            if (orderList.get(x)> 0) {
                orderList.set(x, orderList.get(x)-1);
                orderListItems[x].setText(String.valueOf(orderList.get(x)));
                availableList.set(x, availableList.get(x)+1);
                activity.availableList.set(x, availableList.get(x));
                availableListItems[x].setText(String.valueOf(availableList.get(x)));
            }
        });
        decreaseEight.setOnClickListener(view18 -> {
            int x = 7;
            if (orderList.get(x)> 0) {
                orderList.set(x, orderList.get(x)-1);
                orderListItems[x].setText(String.valueOf(orderList.get(x)));
                availableList.set(x, availableList.get(x)+1);
                activity.availableList.set(x, availableList.get(x));
                availableListItems[x].setText(String.valueOf(availableList.get(x)));
                Snackbar.make(view, String.valueOf(orderList), Snackbar.LENGTH_LONG)
                        .setAction("", null).show();
            }
        });
        binding.sendOrder.setOnClickListener(view19 -> {
            Sender sender = new Sender();
            sender.serverID = activity.serverID;
            sender.name = String.valueOf(binding.customerName.getText());
            sender.orders = orderList;
            boolean merger = binding.mergeMode.isChecked();
            if (merger) {
                sender.type = 'M';
            }
            Thread sendThread = new Thread(sender);
            sendThread.start();
            try {
                sendThread.join(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (sender.success) {
                Snackbar.make(view, "Order success!", Snackbar.LENGTH_LONG)
                        .setAction("Connected", null).show();
                clearUi();
            }
        });
        if (second) {
            activity.setFragment(this);
        } else {
            second = true;
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.setFragment(null);
        binding = null;
    }
    public void updateUi() {
        itemList = activity.getItems();
        availableList = activity.getAvailable();
        Log.d("UpdateUI", "Called");
        Log.d("UpdateUI", "UPDATEUICALL");
        binding.itemOne.setText(itemList.get(0));
        binding.itemTwo.setText(itemList.get(1));
        binding.itemThree.setText(itemList.get(2));
        binding.itemFour.setText(itemList.get(3));
        binding.itemFive.setText(itemList.get(4));
        binding.itemSix.setText(itemList.get(5));
        binding.itemSeven.setText(itemList.get(6));
        binding.itemEight.setText(itemList.get(7));
        binding.availableOne.setText(String.valueOf(availableList.get(0)));
        binding.availableTwo.setText(String.valueOf(availableList.get(1)));
        binding.availableThree.setText(String.valueOf(availableList.get(2)));
        binding.availableFour.setText(String.valueOf(availableList.get(3)));
        binding.availableFive.setText(String.valueOf(availableList.get(4)));
        binding.availableSix.setText(String.valueOf(availableList.get(5)));
        binding.availableSeven.setText(String.valueOf(availableList.get(6)));
        binding.availableEight.setText(String.valueOf(availableList.get(7)));
    }
    public void clearUi() {
        for (int i = 0; i < 8; i++) {
            orderList.set(i, 0);
            orderListItems[i].setText("0");
            binding.customerName.setText("");
        }
    }
}
