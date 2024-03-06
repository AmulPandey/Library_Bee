package com.example.LibraryBee;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class contact extends Fragment {

    RecyclerContactadapter adapter;
    ArrayList<contactModel> arrcontact = new ArrayList<>();
    FloatingActionButton add;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_members, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recontact);
        add = view.findViewById(R.id.add);
        // Initialize the adapter before using it
        adapter = new RecyclerContactadapter(requireContext(), arrcontact);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(requireContext());
                dialog.setContentView(R.layout.add_layout);

                EditText editname = dialog.findViewById(R.id.editname);
                EditText editnumber = dialog.findViewById(R.id.editnumber);
                Button btn = dialog.findViewById(R.id.btn);

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = "", number = "";
                        if (!editname.getText().toString().equals("")) {
                            name = editname.getText().toString();
                        } else
                            Toast.makeText(requireContext(), "Please Enter Name", Toast.LENGTH_SHORT).show();

                        if (!editnumber.getText().toString().equals("")) {
                            number = editnumber.getText().toString();
                        } else
                            Toast.makeText(requireContext(), "Please Enter Number", Toast.LENGTH_SHORT).show();

                        arrcontact.add(new contactModel(R.drawable.contactpng, name, number));
                        adapter.notifyItemInserted(arrcontact.size() - 1);
                        recyclerView.scrollToPosition(arrcontact.size() - 1);

                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        // Add other initial contacts...
        arrcontact.add(new contactModel(R.drawable.contactpng, "abcd", "9912359750"));
        arrcontact.add(new contactModel(R.drawable.contactpng, "abcd", "9912359750"));
        arrcontact.add(new contactModel(R.drawable.contactpng, "abcd", "9912359750"));
        arrcontact.add(new contactModel(R.drawable.contactpng, "abcd", "9912359750"));
        arrcontact.add(new contactModel(R.drawable.contactpng, "abcd", "9912359750"));
        arrcontact.add(new contactModel(R.drawable.contactpng, "abcd", "9912359750"));
        arrcontact.add(new contactModel(R.drawable.contactpng, "abcd", "9912359750"));
        arrcontact.add(new contactModel(R.drawable.contactpng, "abcd", "9912359750"));
        arrcontact.add(new contactModel(R.drawable.contactpng, "abcd", "9912359750"));
        arrcontact.add(new contactModel(R.drawable.contactpng, "abcd", "9912359750"));
        arrcontact.add(new contactModel(R.drawable.contactpng, "abcd", "9912359750"));
        arrcontact.add(new contactModel(R.drawable.contactpng, "abcd", "9912359750"));

        // Add other initial contacts...

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }
}

