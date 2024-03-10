package com.example.LibraryBee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    BottomNavigationView btnview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        String userId = auth.getCurrentUser().getUid();

        // Schedule the subscription job


        btnview=findViewById(R.id.btnview);

        btnview.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
              int id = item.getItemId();

              if (id==R.id.nav_home){
                  loadfrag(new home(),false);
              }
              else  if (id==R.id.nav_map){
                  loadfrag(new maps(),false);
                }
//              else  if (id==R.id.nav_utilities){
//                  loadfrag(new utilities(),false);
//                }
//              else  if (id==R.id.nav_members){
//                  loadfrag(new contact(),false);
//                }
              else{//profile
                  loadfrag(new notifications(),true);
                }
                return true;
            }
        });

        btnview.setSelectedItemId(R.id.nav_home);
    }

    public void loadfrag(Fragment fragment,boolean flag){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if(flag)
            ft.add(R.id.container , fragment);
        else
            ft.replace(R.id.container , fragment);
        ft.commit();
    }
}