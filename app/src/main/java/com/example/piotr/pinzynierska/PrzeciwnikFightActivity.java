package com.example.piotr.pinzynierska;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;

public class PrzeciwnikFightActivity extends AppCompatActivity {


    String ja;
    String przeciwnik;
    int miss;
    int crit;
    boolean boty;

    Handler customHandler = new Handler();

    ImageView imageView;
    TextView zlodziejTextView, czasTextView;
    ProgressBar hpProgressBar;
    int hpStatus = 200;
    int mojCzas=0;




    Long startTime =0L, timeInMiliseconsd = 0l, timeSwabBuff=0L, updateTime =0L ;

    Runnable updateTimerThread = new Runnable()
    {

        @Override
        public void run()
        {
            timeInMiliseconsd = SystemClock.uptimeMillis()-startTime;
            updateTime = timeSwabBuff + timeInMiliseconsd;
            int sec = (int)(updateTime/1000);

            czasTextView.setText(String.valueOf(sec));
            customHandler.postDelayed(this,0);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_przeciwnik_fight);



        Intent intent = getIntent();
        ja = intent.getStringExtra("ja");
        miss = intent.getIntExtra("miss",20);
        crit = intent.getIntExtra("crit",10);
        boty = intent.getBooleanExtra("boty",false);

        imageView = (ImageView) findViewById(R.id.przeciwnikView);
        zlodziejTextView = (TextView) findViewById(R.id.niTextView);
        czasTextView = (TextView) findViewById(R.id.timeTextView);
        zlodziejTextView.setText(przeciwnik);

        hpProgressBar = (ProgressBar) findViewById(R.id.hpProgressBar);
        hpProgressBar.setProgress(hpStatus);

        imageView.setImageResource(R.drawable.przeciwnik);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Random mG = new Random();
                int missValue = mG.nextInt(99)+1;
                Random mC = new Random();
                int critValue = mC.nextInt(99)+1;

                if(hpStatus == 200)
                {
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread,0);
                    hpStatus--;
                }
                else if(hpStatus <=0)
                {
                    timeSwabBuff+=timeInMiliseconsd;
                    customHandler.removeCallbacks(updateTimerThread);
                    mojCzas = Integer.parseInt(czasTextView.getText().toString());
                    Intent res = new Intent();
                    res.putExtra("boty",boty);
                    res.putExtra("czas", mojCzas);
                    setResult(Activity.RESULT_CANCELED,res);
                    finish();


                }
                else {

                    if(missValue<=miss)
                    {
                        Toast.makeText(getApplicationContext(),"Miss",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        if(critValue<=crit)
                        {
                            Toast.makeText(getApplicationContext(),"Crit",Toast.LENGTH_SHORT).show();
                            hpStatus-=3;
                            hpProgressBar.setProgress(hpStatus);

                        }
                        else {
                            hpStatus--;
                            hpProgressBar.setProgress(hpStatus);
                        }
                    }

                }
            }
        });



    }
}
