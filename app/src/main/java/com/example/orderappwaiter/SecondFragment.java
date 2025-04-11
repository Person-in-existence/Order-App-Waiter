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

        SecondFragment thisReference = this;

        // Scan for devices
        // Use a new thread so we don't block UI thread
        new Thread() {
            public void run() {
                showProgressBar();
                Network.scanDevices(thisReference::addDevice, thisReference::hideProgressBar);
            }
        }.start();
    }

    public void addDevice(Device device) {
        activity.runOnUiThread(()->{
            Log.d("SecondFragment", "addDevice called: " + device);
            TableRow deviceRow = new TableRow(activity);
            // Name
            TextView nameView = new TextView(activity);
            nameView.setText(device.name);
            deviceRow.addView(nameView);

            // Device Type
            TextView typeView = new TextView(activity);
            typeView.setText(device.typeName());
            deviceRow.addView(typeView);

            // Button
            Button button = new Button(activity);
            button.setText("Connect!");
            button.setOnClickListener(view->{
                Network.joinServer(device.getJoinCode());
            });
            deviceRow.addView(button);

            binding.deviceTable.addView(deviceRow);
        });
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