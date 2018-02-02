package com.example.piotr.pinzynierska;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class JoinActivity extends AppCompatActivity {

    String name;
    Button btn;
    int czas;

    View mLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        mLayout = findViewById(R.id.joinActivity);

        Intent intent = getIntent();
        this.name=intent.getStringExtra("name");
        this.czas = intent.getIntExtra("czas",15);

        TextView text = (TextView) findViewById(R.id.gameNameTextView);
        text.setText(this.name);

        btn = (Button) findViewById(R.id.joinButtonn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText has = (EditText) findViewById(R.id.passssEditText);
                String haslo=has.getText().toString();

                TextView nazw = (TextView) findViewById(R.id.nickEditText);
                String naz = nazw.getText().toString();

                if(naz.equals("") || haslo.equals(""))
                {
                    Snackbar.make(mLayout, "Nie wszystkie dane zostały wypełnione",
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
                else {

                    new SprawdzHaslo()
                            // .execute("http://192.168.1.12/test/SprawdzHaslo.php");
                            .execute("http://polizlo.5v.pl/test/SprawdzHaslo.php");
                }
            }
        });
    }









    private class SprawdzHaslo extends AsyncTask<String, Void, String> {


        // okienko dialogowe, które każe użytkownikowi czekać
        private ProgressDialog dialog = new ProgressDialog(JoinActivity.this);
        private String haslo;
        private String naz;


        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {

            // wyświetlamy okienko dialogowe każące czekać
            EditText has = (EditText) findViewById(R.id.passssEditText);
            this.haslo=has.getText().toString();

            TextView nazw = (TextView) findViewById(R.id.nickEditText);
            this.naz = nazw.getText().toString();
            dialog.setMessage("Czekaj...");
            dialog.setCancelable(false);
            dialog.show();
        }

        // główna operacja, która wykona się w osobnym wątku
        // nie ma w niej dostępu do elementów UI
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

                data.put("nazwa",name);
                data.put("haslo",this.haslo);



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

        // metoda wykonuje się po zakończeniu metody głównej,
        // której wynik będzie przekazany;
        // w tej metodzie mamy dostęp do UI
        @Override
        protected void onPostExecute(String result) {
            // chowamy okno dialogowe
            dialog.dismiss();

            try {
                // reprezentacja obiektu JSON w Javie



                JSONObject json = new JSONObject(result);

                Log.e("Informacja: ",json.toString());


                Toast.makeText(getApplicationContext(),"status: "+json.optString("status")+", msg: "+json.optString("msg"),Toast.LENGTH_SHORT).show();
                // pobranie pól obiektu JSON i wyświetlenie ich na ekranie
                // ((TextView) findViewById(R.id.response_id)).setText("status: "+ json.optString("status"));
                // ((TextView) findViewById(R.id.response_name)).setText("msg: "
                //       + json.optString("msg"));

                if(Integer.parseInt(json.optString("status")) == 1)
                {
                    new DolaczDoGry()
                            //   .execute("http://192.168.1.12/test/DolaczDoGry.php");
                            .execute("http://polizlo.5v.pl/test/DolaczDoGry.php");
                }

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }
        }
    }









    private class DolaczDoGry extends AsyncTask<String, Void, String> {

        private String n;
        private String nk;

        // okienko dialogowe, które każe użytkownikowi czekać
        private ProgressDialog dialog = new ProgressDialog(JoinActivity.this);


        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {

            EditText ni = (EditText) findViewById(R.id.nickEditText);
            this.nk= ni.getText().toString();
            this.n = name;

            // wyświetlamy okienko dialogowe każące czekać
            dialog.setMessage("Czekaj...");
            dialog.setCancelable(false);
            dialog.show();
        }

        // główna operacja, która wykona się w osobnym wątku
        // nie ma w niej dostępu do elementów UI
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
                data.put("nazwa",n);
                data.put("nick",nk);


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

        // metoda wykonuje się po zakończeniu metody głównej,
        // której wynik będzie przekazany;
        // w tej metodzie mamy dostęp do UI
        @Override
        protected void onPostExecute(String result) {

            Log.d("B","bbbb");
            // chowamy okno dialogowe
            dialog.dismiss();

            try {
                // reprezentacja obiektu JSON w Javie

                // listView.setVisibility(View.INVISIBLE);

                Log.d("INFO:",result);

                JSONObject json = new JSONObject(result);

                Log.e("Informacja: ",json.toString());

                Toast.makeText(getApplicationContext(),"status: "+json.optString("status")+", msg: "+json.optString("msg"),Toast.LENGTH_SHORT).show();
                // pobranie pól obiektu JSON i wyświetlenie ich na ekranie
                // ((TextView) findViewById(R.id.response_id)).setText("status: "+ json.optString("status"));
                // ((TextView) findViewById(R.id.response_name)).setText("msg: "
                //       + json.optString("msg"));

                if(Integer.parseInt(json.optString("status")) == 1)
                {
                    Intent intent = new Intent(JoinActivity.this,GamePanelActivity.class);
                    intent.putExtra("status","g");
                    intent.putExtra("nazwa",this.n);
                    intent.putExtra("nick",this.nk);
                    intent.putExtra("czas",czas);
                    startActivity(intent);
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

