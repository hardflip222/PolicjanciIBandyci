package com.example.piotr.pinzynierska;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    Button createButton;
    Button joinButton;
    ConnectionDetector cd;
    Timer timerInternet;
    CzyInternetDziala czyInternetDziala;
    private static final int PERMISSION_REQUEST_GPS = 0;

    private  View mLayout;

    AlertDialog.Builder mBuilder;
    View mView;
    TextView txtView;
    Button btn;
    AlertDialog dialogPermission;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mLayout = findViewById(R.id.mainLayout);

        mBuilder = new AlertDialog.Builder(StartActivity.this);
        mView = getLayoutInflater().inflate(R.layout.dialog_zlodziej, null);
        txtView = (TextView) mView.findViewById(R.id.napisZlodzieje);
        btn = (Button) mView.findViewById(R.id.btnZlodzieje);
        txtView.setText("Trzeba ustawić zezwolenie na GPS by móc grać dalej");
        mBuilder.setView(mView);
        dialogPermission = mBuilder.create();
        dialogPermission.setCancelable(false);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialogPermission.cancel();
                ActivityCompat.requestPermissions(StartActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_GPS);

            }
        });

        showGPSPreview();

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
                       // Toast.makeText(parent.getBaseContext(), "Włącz internet i GPS!!!", Toast.LENGTH_LONG).show();
                        Snackbar.make(mLayout, "Włącz internet i GPS.",
                                Snackbar.LENGTH_SHORT)
                                .show();
                    }
                });
            }

        }


    }






    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_REQUEST_GPS) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                Snackbar.make(mLayout, "Zezwolono na GPS",
                        Snackbar.LENGTH_SHORT)
                        .show();


            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, "Nie zezwolono na GPS.",
                        Snackbar.LENGTH_SHORT)
                        .show();
                finish();
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }


    private void showGPSPreview() {
        // BEGIN_INCLUDE(startCamera)
        // Check if the Camera permission has been granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
            Snackbar.make(mLayout,
                    "Zezwolenie na GPS jest ustawione.",
                    Snackbar.LENGTH_SHORT).show();

        } else {
            // Permission is missing and must be requested.

           requestGPSPermission();
        }
        // END_INCLUDE(startCamera)
    }





    private void requestGPSPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.

           dialogPermission.show();

            /*
            Snackbar.make(mLayout, "Zezwolenie na GPS jest wymagane do działania aplikacji.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(StartActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_GPS);
                }
            }).show();
           */
        } else {
            Snackbar.make(mLayout,
                    "Permission is not available. Requesting camera permission.",
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_GPS);
        }
    }



}
