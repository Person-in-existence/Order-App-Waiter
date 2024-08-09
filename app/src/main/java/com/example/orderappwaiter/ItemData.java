package com.example.orderappwaiter;

import java.util.ArrayList;

public class ItemData {
    public final ArrayList<String> itemNames;
    public final ArrayList<Integer> itemQuantities;

    public ItemData(ArrayList<String> itemNames, ArrayList<Integer> itemQuantities) {
        this.itemNames = itemNames;
        this.itemQuantities = itemQuantities;
    }
}
