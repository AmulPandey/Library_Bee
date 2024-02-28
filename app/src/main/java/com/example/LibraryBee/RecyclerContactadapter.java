package com.example.LibraryBee;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class RecyclerContactadapter extends RecyclerView.Adapter<RecyclerContactadapter.ViewHolder> {

    private Context context;
    private ArrayList<contactModel> arrcontact;

    private int lastposition =-1;
    public RecyclerContactadapter(Context context, ArrayList<contactModel> arrcontact) {
        this.context = context;
        this.arrcontact = arrcontact;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        contactModel contact = arrcontact.get(position);

        holder.imgcontact.setImageResource(contact.getImg());
        holder.txtname.setText(contact.getName());
        holder.txtnumber.setText(contact.getNumber());

        setAnimation(holder.itemView,position);

        holder.LLrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.add_layout);


                EditText editname = dialog.findViewById(R.id.editname);
                EditText editnumber = dialog.findViewById(R.id.editnumber);
                Button btn = dialog.findViewById(R.id.btn);
                TextView txt = dialog.findViewById(R.id.txttitle);

                btn.setText("Update");
                txt.setText("Update Contact");
                editname.setText((arrcontact.get(position)).name);
                editnumber.setText((arrcontact.get(position)).number);

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name="",number="";
                        if(!editname.getText().toString().equals("")) {
                            name = editname.getText().toString();
                        }
                        else
                            Toast.makeText(context, "Please Enter Name", Toast.LENGTH_SHORT).show();

                        if(!editnumber.getText().toString().equals("")){
                            number= editnumber.getText().toString();
                        }
                        else
                            Toast.makeText(context, "Please Enter Number", Toast.LENGTH_SHORT).show();

                        arrcontact.set(position,new contactModel(R.drawable.contactpng,name,number));
                        notifyItemChanged(position);
                        dialog.dismiss();

                    }
                });

                dialog.show();
            }
        });

        holder.LLrow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle("Delete Contact")
                        .setMessage("Are You Sure")
                        .setIcon(R.drawable.delete_24)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                arrcontact.remove(position);
                                notifyItemRemoved(position);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                builder.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrcontact.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtname, txtnumber;
        ImageView imgcontact;
        LinearLayout LLrow;

        public ViewHolder(View itemView) {
            super(itemView);
            txtname = itemView.findViewById(R.id.txtname);
            txtnumber = itemView.findViewById(R.id.txtnumber);
            imgcontact = itemView.findViewById(R.id.imgcontact);
            LLrow=itemView.findViewById(R.id.LLrow);
        }

    }

    private void setAnimation(View viewtoanimate,int position){

        if(position> lastposition)
        {
            Animation slideIn = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewtoanimate.startAnimation(slideIn);
            lastposition=position;
        }

    }
}

