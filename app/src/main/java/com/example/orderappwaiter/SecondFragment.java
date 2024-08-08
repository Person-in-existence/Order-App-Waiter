package com.example.orderappwaiter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.orderappwaiter.databinding.FragmentSecondBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

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

        binding.buttonSecond.setOnClickListener(view1 -> NavHostFragment.findNavController(SecondFragment.this)
                .navigate(R.id.action_SecondFragment_to_FirstFragment));
        binding.connect.setOnClickListener(view2-> {
            binding.progressBar.setAlpha(1);
            activity = (MainActivity)getActivity();
            assert activity != null;
            ArrayList[] output = activity.newConnection(String.valueOf(binding.serverId.getText()));
            if (output == null) {
                binding.progressBar.setAlpha(0);
                Snackbar.make(view2, "An error occurred. Check that the other device is online on the same network as you and try again. See the log for full details.", Snackbar.LENGTH_LONG)
                        .setAction("Error", null).show();
            }
            else if (output.length == 2) {
                binding.progressBar.setAlpha(0);
                Snackbar.make(view2, "Connection success!", Snackbar.LENGTH_LONG)
                        .setAction("Connected", null).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}