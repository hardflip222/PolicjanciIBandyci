package com.example.piotr.pinzynierska;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class FightActivity extends AppCompatActivity {

    String ja;
    String przeciwnik;
    String nacja;
    String nazwaGry;
    int miss;
    int crit;

    Handler customHandler = new Handler();

    ImageView imageView;
    TextView zlodziejTextView, czasTextView;
    ProgressBar hpProgressBar;
    int hpStatus = 200;
    int czasWalkiPrzeciwnika = 0;
    int mojCzas=0;

    Timer timer;
    PobierzCzas pobierzCzas;

    Timer timer2;
    UsunWalke usunWalke;

    boolean wygrana = false;
    boolean koniec = false;


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
        setContentView(R.layout.activity_fight);

        Intent intent = getIntent();
        ja = intent.getStringExtra("ja");
        przeciwnik = intent.getStringExtra("przeciwnik");
        nacja = intent.getStringExtra("nacja");
        nazwaGry = intent.getStringExtra("nazwaGry");
        miss = intent.getIntExtra("miss",20);
        crit = intent.getIntExtra("crit",10);

        imageView = (ImageView) findViewById(R.id.przeciwnikView);
        zlodziejTextView = (TextView) findViewById(R.id.niTextView);
        czasTextView = (TextView) findViewById(R.id.timeTextView);
        zlodziejTextView.setText(przeciwnik);

        hpProgressBar = (ProgressBar) findViewById(R.id.hpProgressBar);
        hpProgressBar.setProgress(hpStatus);

        if(nacja.equals("P"))
        {
            imageView.setImageResource(R.drawable.zlodziej2);
        }
        else
        {
            imageView.setImageResource(R.drawable.policjant2);
        }

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
                else if(hpStatus ==0 && koniec == false)
                {
                    timeSwabBuff+=timeInMiliseconsd;
                    customHandler.removeCallbacks(updateTimerThread);
                    koniec = true;
                    new UstawCzas().execute("http://polizlo.5v.pl/test/UstawCzas.php");


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





    private class UstawCzas extends AsyncTask<String, Void, String>
    {


        private String czas;

        @Override
        protected void onPreExecute() {

            this.czas = czasTextView.getText().toString();
        }

        @Override
        protected String doInBackground(String... urls) {

            try {
                // zakładamy, że jest tylko jeden URL
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);

                // zezwolenie na wysyłanie danych
                connection.setDoOutput(true);
                // ustawienie typu wysyłanych danych
                connection.setRequestProperty("Content-Type",
                        "application/json");
                // ustawienie metody
                connection.setRequestMethod("POST");

                // stworzenie obiektu do wysłania
                JSONObject data = new JSONObject();
                data.put("nazwa", nazwaGry);
                data.put("nick",ja);
                data.put("czas",this.czas);
                data.put("nacja",nacja);


                // wysłanie obiektu
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream(),
                                "UTF-8"));
                writer.write(data.toString());
                writer.close();

                //////////////////////////////////////////
                // na tym etapie obiekt został wysłany
                // i dostaliśmy odpowiedź serwera
                //////////////////////////////////////////

                // sprawdzenie kodu odpowiedzi, 200 = OK
                if (connection.getResponseCode() != 200) {
                    throw new Exception("Bad Request");

                }


                // pobranie odpowiedzi serwera
                InputStream in = new BufferedInputStream(
                        connection.getInputStream());

                // konwersja InputStream na String
                // wynik będzie przekazany do metody onPostExecute()
                return streamToString(in);

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
                return null;
            }


        }

        @Override
        protected void onPostExecute(String result) {

            try {

                Log.e("res",result);

                JSONObject json = new JSONObject(result);

                Toast.makeText(getApplicationContext(), "status: " + json.optString("status") + ", msg: " + json.optString("msg"), Toast.LENGTH_SHORT).show();
                // pobranie pól obiektu JSON i wyświetlenie ich na ekranie
                // ((TextView) findViewById(R.id.response_id)).setText("status: "+ json.optString("status"));
                // ((TextView) findViewById(R.id.response_name)).setText("msg: "
                //       + json.optString("msg"));
                mojCzas=Integer.parseInt(czas);
                timer = new Timer();
                pobierzCzas  = new PobierzCzas();
                timer.schedule(pobierzCzas,0, 1*3000);

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }
        }


    }






    class PobierzCzas extends TimerTask
    {


        public void run()
        {
            String result="";

            try {
                // zakładamy, że jest tylko jeden URL
                URL url = new URL("http://polizlo.5v.pl/test/PobierzCzasWalki.php");
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);

                // zezwolenie na wysyłanie danych
                connection.setDoOutput(true);
                // ustawienie typu wysyłanych danych
                connection.setRequestProperty("Content-Type",
                        "application/json");
                // ustawienie metody
                connection.setRequestMethod("POST");

                // stworzenie obiektu do wysłania
                JSONObject data = new JSONObject();
                data.put("nazwa",nazwaGry);
                data.put("nick",ja);
                data.put("nacja",nacja);



                // wysłanie obiektu
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream(),
                                "UTF-8"));
                writer.write(data.toString());
                writer.close();

                //////////////////////////////////////////
                // na tym etapie obiekt został wysłany
                // i dostaliśmy odpowiedź serwera
                //////////////////////////////////////////

                // sprawdzenie kodu odpowiedzi, 200 = OK
                if (connection.getResponseCode() != 200) {
                    throw new Exception("Bad Request");
                }


                // pobranie odpowiedzi serwera
                InputStream in = new BufferedInputStream(
                        connection.getInputStream());

                // konwersja InputStream na String
                // wynik będzie przekazany do metody onPostExecute()
                result= streamToString(in);

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }


            try {
                // reprezentacja obiektu JSON w Javie


                Log.e("Zly",result);
                JSONObject json = new JSONObject(result);
                int c = Integer.parseInt(json.optString("czas"));

                if(c!=0) {
                    czasWalkiPrzeciwnika = c;
                    if(czasWalkiPrzeciwnika>mojCzas)
                    {
                        wygrana=true;
                    }
                    timer.cancel();

                    Log.e("Czas",String.valueOf(czasWalkiPrzeciwnika));

                    new UstawKoniecWalki().execute("http://polizlo.5v.pl/test/UstawKoniecWalki.php");
                    Log.e("UUUUU","Ustawiam koniec walki");
                }
                else
                {
                    Log.e("Czas","walka nieskonczona");
                }

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }



        }


    }


    private class UstawKoniecWalki extends AsyncTask<String, Void, String>
    {



        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... urls) {

            try {
                // zakładamy, że jest tylko jeden URL
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);

                // zezwolenie na wysyłanie danych
                connection.setDoOutput(true);
                // ustawienie typu wysyłanych danych
                connection.setRequestProperty("Content-Type",
                        "application/json");
                // ustawienie metody
                connection.setRequestMethod("POST");

                // stworzenie obiektu do wysłania

                Log.e("UUUUU","Przed Wyslaniem");
                JSONObject data = new JSONObject();
                data.put("nazwa", nazwaGry);
                data.put("nick",ja);
                data.put("nacja",nacja);


                // wysłanie obiektu
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream(),
                                "UTF-8"));
                writer.write(data.toString());
                writer.close();

                //////////////////////////////////////////
                // na tym etapie obiekt został wysłany
                // i dostaliśmy odpowiedź serwera
                //////////////////////////////////////////

                // sprawdzenie kodu odpowiedzi, 200 = OK
                if (connection.getResponseCode() != 200) {
                    throw new Exception("Bad Request");

                }

                Log.e("UUUUU","Po wyslaniu");

                // pobranie odpowiedzi serwera
                InputStream in = new BufferedInputStream(
                        connection.getInputStream());

                // konwersja InputStream na String
                // wynik będzie przekazany do metody onPostExecute()
                return streamToString(in);

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
                return null;
            }


        }

        @Override
        protected void onPostExecute(String result) {

            try {


                JSONObject json = new JSONObject(result);

                Toast.makeText(getApplicationContext(), "status: " + json.optString("status") + ", msg: " + json.optString("msg"), Toast.LENGTH_SHORT).show();
                // pobranie pól obiektu JSON i wyświetlenie ich na ekranie
                // ((TextView) findViewById(R.id.response_id)).setText("status: "+ json.optString("status"));
                // ((TextView) findViewById(R.id.response_name)).setText("msg: "
                //       + json.optString("msg"));
                if(nacja.equals("Z"))
                {
                    Intent res = new Intent();
                    res.putExtra("wygrana",wygrana);
                    res.putExtra("przeciwnik",przeciwnik);
                    setResult(Activity.RESULT_OK,res);
                    finish();
                }
                else
                {
                    timer2 = new Timer();
                    usunWalke  = new UsunWalke();
                    timer2.schedule(usunWalke,0, 1*3000);
                }

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }
        }


    }




    class UsunWalke extends TimerTask
    {


        public void run()
        {
            String result="";

            try {
                // zakładamy, że jest tylko jeden URL
                URL url = new URL("http://polizlo.5v.pl/test/UsunWalke.php");
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);

                // zezwolenie na wysyłanie danych
                connection.setDoOutput(true);
                // ustawienie typu wysyłanych danych
                connection.setRequestProperty("Content-Type",
                        "application/json");
                // ustawienie metody
                connection.setRequestMethod("POST");

                // stworzenie obiektu do wysłania
                JSONObject data = new JSONObject();
                data.put("nazwa",nazwaGry);
                data.put("nick",ja);



                // wysłanie obiektu
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream(),
                                "UTF-8"));
                writer.write(data.toString());
                writer.close();

                //////////////////////////////////////////
                // na tym etapie obiekt został wysłany
                // i dostaliśmy odpowiedź serwera
                //////////////////////////////////////////

                // sprawdzenie kodu odpowiedzi, 200 = OK
                if (connection.getResponseCode() != 200) {
                    throw new Exception("Bad Request");
                }


                // pobranie odpowiedzi serwera
                InputStream in = new BufferedInputStream(
                        connection.getInputStream());

                // konwersja InputStream na String
                // wynik będzie przekazany do metody onPostExecute()
                result= streamToString(in);

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }


            try {
                // reprezentacja obiektu JSON w Javie



                JSONObject json = new JSONObject(result);

                if(json.optInt("status") == 1)
                {
                    Log.e("Usunieta","Usunieto");
                    timer2.cancel();
                    Intent res = new Intent();
                    res.putExtra("wygrana",wygrana);
                    res.putExtra("przeciwnik",przeciwnik);
                    setResult(Activity.RESULT_OK,res);
                    finish();
                }
                else
                {
                    Log.e("Nie usunieto","nie Usunieto");
                }

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }



        }


    }









    // konwersja z InputStream do String
    public static String streamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        try {

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }

            reader.close();

        } catch (IOException e) {
            // obsłuż wyjątek
            Log.d(StartActivity.class.getSimpleName(), e.toString()+"inputstrinf to string");
        }

        return stringBuilder.toString();
    }




}
