package com.example.orderappwaiter;

import java.util.ArrayList;

public class Order {
    public ArrayList<Integer> orders;
    public String name;
    public boolean setOrders(ArrayList<Integer> ordersIn) {
        if (ordersIn.size() == 8) {
            orders = ordersIn;
            return true;
        } else {
            return false;
        }
    }
    public boolean setName(String nameIn) {
        if (nameIn != null)  {
            if (nameIn != "") {
                name = nameIn;
                return true;
            }
        }
        return false;
    }
    public ArrayList<Integer> getAmounts() {
        return orders;
    }
    public int getLength() {
        return orders.size();
    }
}
