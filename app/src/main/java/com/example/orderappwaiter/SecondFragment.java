package com.example.orderappwaiter;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.orderappwaiter.databinding.FragmentSecondBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import networking.Device;
import networking.Network;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private MainActivity activity;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(v -> NavHostFragment.findNavController(SecondFragment.this)
                .navigate(R.id.action_SecondFragment_to_FirstFragment));

        activity = (MainActivity) requireActivity();
        activity.setSecondFragment(this);

        // Set devices on start so that we have previous devices
        setDevices(activity.devices);
        if (activity.isScanning) {
            // Show the progress bar
            showProgressBar();
        }

        binding.scan.setOnClickListener(v->{
            if (!activity.isScanning) {
                activity.scanDevices();
            }
        });
    }

    public void setDevices(ArrayList<Device> devices) {
        // Clear table
        binding.deviceTable.removeAllViews();
        for (Device device: devices) {
            addDevice(device);
        }
    }
    public void clearDevices() {
        activity.runOnUiThread(()->binding.deviceTable.removeAllViews());
    }

    public void addDevice(Device device) {
        try {
            activity.runOnUiThread(() -> {
                try {
                    TableRow deviceRow = new TableRow(activity);
                    // Name
                    TextView nameView = new TextView(activity);
                    nameView.setPadding(5,5,16,5);
                    nameView.setText(device.name);
                    deviceRow.addView(nameView);

                    // Device Type
                    TextView typeView = new TextView(activity);
                    typeView.setText(device.typeName());
                    deviceRow.addView(typeView);

                    // Button
                    Button button = new Button(activity);
                    button.setText("Connect!");
                    button.setOnClickListener(view -> {
                        Network.joinServer(device.getJoinCode());
                    });
                    deviceRow.addView(button);

                    binding.deviceTable.addView(deviceRow);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {}
    }
    public void showProgressBar() {
        binding.progressBar.setVisibility(VISIBLE);
    }

    public void hideProgressBar() {
        // Try to protect from null issues
        try {
            binding.progressBar.setVisibility(INVISIBLE);
        } catch (Exception ignored) {}
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        activity.setSecondFragment(null);
    }

}