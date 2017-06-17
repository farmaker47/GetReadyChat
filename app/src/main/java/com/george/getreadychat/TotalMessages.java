package com.george.getreadychat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.george.getreadychat.data.UserDetails;

public class TotalMessages extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_messages);

        Toast.makeText(this,"First User= "+UserDetails.username+"---Second User= " + UserDetails.secondUser,Toast.LENGTH_LONG).show();
    }
}
