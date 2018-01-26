package com.example.piotr.pinzynierska;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ResultActivity extends AppCompatActivity {

    ArrayList<PiontsPlayer> players;
    ListView pointsListView;
    String nazwa;
    String status;
    Context c;
    Timer timer;
    AktualizujListePunktow aktualizujListePunktow;
    TextView wTextView;
    TextView pTextView;
    Button koniecGry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        wTextView = (TextView) findViewById(R.id.wygraniTextView);
        pTextView = (TextView) findViewById(R.id.przegraniTextView);
        koniecGry = (Button) findViewById(R.id.koniecGryButton);
        final Intent intent = getIntent();
        nazwa = intent.getStringExtra("nazwa");
        status = intent.getStringExtra("status");
        pointsListView = (ListView) findViewById(R.id.punktyListView);
        players = new ArrayList<>();
        c = this;

        timer = new Timer();
        aktualizujListePunktow  = new AktualizujListePunktow();
        timer.schedule(aktualizujListePunktow,0, 1*3000);

        koniecGry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status.equals("Z"))
                {
                    new UsunRozgrywke().execute("http://polizlo.5v.pl/test/UsunRozgrywke.php");
                }

                    Intent intent1 = new Intent(c,StartActivity.class);
                    startActivity(intent1);

            }
        });
    }







    private class UsunRozgrywke extends AsyncTask<String, Void, String>
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
                JSONObject data = new JSONObject();
                data.put("nazwa", nazwa);


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


                JSONObject json = new JSONObject(result);

                Toast.makeText(getApplicationContext(), "status: " + json.optString("status") + ", msg: " + json.optString("msg"), Toast.LENGTH_SHORT).show();
                // pobranie pól obiektu JSON i wyświetlenie ich na ekranie
                // ((TextView) findViewById(R.id.response_id)).setText("status: "+ json.optString("status"));
                // ((TextView) findViewById(R.id.response_name)).setText("msg: "




            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }
        }


    }





    class AktualizujListePunktow extends TimerTask
    {


        public void run()
        {
            String result="";

            try {
                // zakładamy, że jest tylko jeden URL
                URL url = new URL("http://polizlo.5v.pl/test/PobierzPunkty.php");
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
                data.put("nazwa",nazwa);



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


            players.clear();

            try {
                // reprezentacja obiektu JSON w Javie

                ArrayList<String> gracze = new ArrayList<>();
                int pZ=0;
                int pP=0;

                Log.e("elko",result);
                JSONObject jsonObject = new JSONObject(result);
                Log.d("json: ",jsonObject.toString());


                JSONArray jsonArrayNick = new JSONArray(jsonObject.optString("nicki"));
                JSONArray jsonArrayNacje = new JSONArray(jsonObject.optString("nacje"));
                JSONArray jsonArrayPunkty = new JSONArray(jsonObject.optString("punkty"));

                for(int i =0;i<jsonArrayNick.length();i++)
                {
                    JSONObject jsonObjNi = new JSONObject(jsonArrayNick.getString(i));
                    JSONObject jsonObjNa = new JSONObject(jsonArrayNacje.getString(i));
                    JSONObject jsonObjPu = new JSONObject(jsonArrayPunkty.getString(i));
                    String ni = jsonObjNi.optString("nick");
                    String na = jsonObjNa.optString("nacja");
                    int pu = Integer.parseInt(jsonObjPu.optString("punkt"));

                    players.add(new PiontsPlayer(ni,na,pu));
                    if(na.equals("P"))
                    {
                        pZ+=pu;
                    }
                    else
                    {
                        pP+=pu;
                    }

                }

                if(pZ>pP)
                {
                    wTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            wTextView.setText("Wygrani");
                            wTextView.setText(wTextView.getText()+" :Złodzieje!!");
                        }
                    });

                    pTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            pTextView.setText("Przegrani");
                            pTextView.setText(pTextView.getText()+" :Policjanci!!");
                        }
                    });
                }

                if(pP>pZ)
                {
                    wTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            wTextView.setText("Wygrani");
                            wTextView.setText(wTextView.getText()+" :Policjanci!!");
                        }
                    });

                    pTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            pTextView.setText("Przegrani");
                            pTextView.setText(pTextView.getText()+" :Złodzieje!!");
                        }
                    });
                }

                if(pP == pZ)
                {
                    wTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            wTextView.setText("Wygrani");
                            wTextView.setText(wTextView.getText()+" :Remis!!");
                        }
                    });

                    pTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            pTextView.setText("Przegrani");
                            pTextView.setText(pTextView.getText()+" :Remis!!");
                        }
                    });
                }


                pointsListView.post(new Runnable() {
                    @Override
                    public void run() {
                        PointsArrayAdapter pointsArrayAdapter = new PointsArrayAdapter(c,players);
                        pointsListView.setAdapter(pointsArrayAdapter);
                    }
                });



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
