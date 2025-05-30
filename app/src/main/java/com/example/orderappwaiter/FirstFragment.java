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

import java.util.ArrayList;

import networking.Order;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private MainActivity activity;
    public ArrayList<Integer> availableList;
    public ArrayList<Integer> orderList;
    public ArrayList<String> itemList;
    public TextView[] orderListItems;
    public TextView[] availableListItems;
    public TextView[] itemListItems;

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
        orderList = activity.orderList;
        for (int ordercount = 0; ordercount < 8; ordercount++) {
            orderList.add(0);
        }
        availableList = activity.getAvailable();
        System.out.println("Length of available is " + availableList.size());
        itemList = activity.getItems();

        // Creates lists of the UI elements
        orderListItems = new TextView[]{binding.orderedOne, binding.orderedTwo, binding.orderedThree, binding.orderedFour, binding.orderedFive, binding.orderedSix, binding.orderedSeven, binding.orderedEight};
        availableListItems = new TextView[]{binding.availableOne, binding.availableTwo, binding.availableThree, binding.availableFour, binding.availableFive, binding.availableSix, binding.availableSeven, binding.availableEight};
        itemListItems = new TextView[]{binding.itemOne, binding.itemTwo, binding.itemThree, binding.itemFour, binding.itemFive, binding.itemSix, binding.itemSeven, binding.itemEight};
        Button[] increases = new Button[] {binding.increaseOne, binding.increaseTwo, binding.increaseThree, binding.increaseFour, binding.increaseFive, binding.increaseSix, binding.increaseSeven, binding.increaseEight};
        Button[] decreases = new Button[] {binding.decreaseOne, binding.decreaseTwo, binding.decreaseThree, binding.decreaseFour, binding.decreaseFive, binding.decreaseSix, binding.decreaseSeven, binding.decreaseEight};
        // Sets the listeners for buttons
        // TODO: fix for null?

        // Increases
        for (int index = 0; index < increases.length; index ++ ) {
            int finalIndex = index;

            increases[index].setOnClickListener(view111 -> {
                if (!activity.locked & availableList.size() > finalIndex) {
                    if (availableList.get(finalIndex) > 0) {
                        orderList.set(finalIndex, orderList.get(finalIndex) + 1);
                        orderListItems[finalIndex].setText(String.valueOf(orderList.get(finalIndex)));
                        availableList.set(finalIndex, availableList.get(finalIndex) - 1);
                        activity.quantities.set(finalIndex, availableList.get(finalIndex));
                        availableListItems[finalIndex].setText(String.valueOf(availableList.get(finalIndex)));
                    }
                }
            });
        }

        // Decreases
        for (int index = 0; index < decreases.length; index++) {
            int finalIndex = index;
            decreases[index].setOnClickListener(view14 -> {
                if (!activity.locked & orderList.get(finalIndex)> 0) {
                    orderList.set(finalIndex, orderList.get(finalIndex)-1);
                    orderListItems[finalIndex].setText(String.valueOf(orderList.get(finalIndex)));
                    availableList.set(finalIndex, availableList.get(finalIndex)+1);
                    activity.quantities.set(0, availableList.get(0));
                    availableListItems[finalIndex].setText(String.valueOf(availableList.get(finalIndex)));
                }
            });
        }


        binding.sendOrder.setOnClickListener(view19 -> {
            if (!activity.locked) {
                sendOrder();
            }
        });
        updateUi();
        activity.setFirstFragment(this);

    }
    private void sendOrder() {
        // Lock controls so we don't mess anything up
        activity.locked = true;
        ArrayList<Order.OrderItem> items = new ArrayList<>();
        for (int index = 0; index < orderList.size(); index++) {
            if (orderList.get(index) != 0) {
                items.add(new Order.OrderItem((short) index, orderList.get(index)));
            }
        }
        String customerName = String.valueOf(binding.customerName.getText());

        // 0 for order ID because it doesn't matter
        Order order = new Order(items, customerName, 0);
        activity.sendOrder(order);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.setFirstFragment(null);
        binding = null;
    }
    public void updateUi() {
        itemList = activity.getItems();
        availableList = activity.getAvailable();

        Log.d("UpdateUI", "Called");


        assert itemListItems.length <= itemList.size();
        for (int index = 0; index < itemListItems.length; index++) {
            itemListItems[index].setText(itemList.get(index));
        }

        assert availableListItems.length <= availableList.size();
        for (int index = 0; index < availableListItems.length; index++) {
            availableListItems[index].setText(String.valueOf(availableList.get(index)));
        }

        assert orderListItems.length <= orderList.size();
        for (int index = 0; index < orderListItems.length; index++) {
            orderListItems[index].setText(String.valueOf(orderList.get(index)));
        }

    }
    public void clearUi() {
        binding.customerName.setText("");
        for (int i = 0; i < 8; i++) {
            orderList.set(i, 0);
            orderListItems[i].setText("0");
        }
        updateUi();
    }
}
