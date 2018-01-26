package com.example.piotr.pinzynierska;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends AppCompatActivity {
    Button createButton;
    Button joinButton;
    ConnectionDetector cd;
    Timer timerInternet;
    CzyInternetDziala czyInternetDziala;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        createButton = (Button) findViewById(R.id.createButton);
        joinButton = (Button) findViewById(R.id.joinButton);


        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(),CreateGameActivity.class);
                startActivity(intent);


            }
        });


        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(),JoinToGameActivity.class);
                startActivity(intent);
            }
        });

        cd = new ConnectionDetector(this);
        timerInternet = new Timer();
        czyInternetDziala = new CzyInternetDziala(this);
        timerInternet.schedule(czyInternetDziala,0,1*3000);

    }




    class CzyInternetDziala extends TimerTask
    {

        private  StartActivity parent;

        public CzyInternetDziala(StartActivity parent) {
            this.parent = parent;
        }

        public void run()
        {
            if(cd.isConnected())
            {

            }
            else
            {
                parent.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(parent.getBaseContext(), "Włącz internet!!!", Toast.LENGTH_LONG).show();
                    }
                });
            }

        }


    }



}
