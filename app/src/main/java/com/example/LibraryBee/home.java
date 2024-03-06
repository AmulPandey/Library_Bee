package com.example.LibraryBee;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
public class home extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Correctly find the ImageView
        ImageView homeProfile = view.findViewById(R.id.homepro);
        Button btn3 = view.findViewById(R.id.btn3);

        // Set click listener and start activity
        homeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HomeProfile.class); // Use getActivity() to get activity context
                startActivity(intent); // Start the new activity
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), paymentActivity.class); // Use getActivity() to get activity context
                startActivity(intent); // Start the new activity
            }
        });

        return view;
    }
}

