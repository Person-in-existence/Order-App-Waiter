package com.example.orderappwaiter;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        // Scan for devices
        // Use a new thread so we don't block UI thread
        new Thread() {
            public void run() {
                // Make the progress bar visible
                binding.progressBar.setProgress(0);
                showProgressBar();
                ArrayList<Device> devices = Network.scanDevices(binding.progressBar::setProgress);
                activity.runOnUiThread(() -> {
                    // Use a try to stop crash if user clicks off before search is finished.
                    try {
                        showDevices(devices);
                    } catch (Exception ignored) {}
                });
            }
        }.start();
    }
    public void showDevices(ArrayList<Device> devices) {
        hideProgressBar();

        // Clear the table
        binding.deviceTable.removeAllViews();

        Log.v("SecondFragment", "Show devices called with: " + devices);
        TableLayout table = binding.deviceTable;

        for (Device device: devices) {
            TableRow deviceRow = new TableRow(activity);

            // Name
            TextView nameView = new TextView(activity);
            nameView.setText(device.name);
            deviceRow.addView(nameView);

            table.addView(deviceRow);
        }


    }
    public void showProgressBar() {
        binding.progressBar.setVisibility(VISIBLE);
    }

    public void hideProgressBar() {
        binding.progressBar.setVisibility(INVISIBLE);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}