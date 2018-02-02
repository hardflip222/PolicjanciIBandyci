package com.example.piotr.pinzynierska;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.Timer;
import java.util.TimerTask;

public class CreateGameActivity extends AppCompatActivity {

    Button createGameButton;
    Spinner spinner;
    Spinner czasSpinner;
    CheckBox botyCheckBox;
    Integer[] numbers = {1,2,3,4,5,6,7,8,9,10};
    Integer[] czasGry = {15,30,45};
    int number=1;
    int czas = 15;
    ConnectionDetector cd;
    Timer timerInternet;
    View mLayout;


    CzyInternetDziala czyInternetDziala;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);
        mLayout = findViewById(R.id.createGameActivity);

        createGameButton = (Button) findViewById(R.id.createGameButton);
        spinner = (Spinner) findViewById(R.id.playersNumer);
        spinner.setAdapter(new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item,numbers));
        czasSpinner = (Spinner) findViewById(R.id.czasSpinner);
        czasSpinner.setAdapter(new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item,czasGry));

        botyCheckBox = (CheckBox) findViewById(R.id.botyChechBox);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                number = numbers[position];
               // Toast.makeText(getApplicationContext(),numbers[position],Toast.LENGTH_SHORT).show();
                Log.e("numer:",String.valueOf(number));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        czasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                czas = czasGry[position];
                // Toast.makeText(getApplicationContext(),numbers[position],Toast.LENGTH_SHORT).show();
                Log.e("czas:",String.valueOf(czas));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ni = (EditText) findViewById(R.id.nickEditText);
                EditText naz = (EditText) findViewById(R.id.nameEditText);
                EditText has = (EditText) findViewById(R.id.passEditText);
                String nazwa=naz.getText().toString();
                String haslo=has.getText().toString();
                String nick =ni.getText().toString();

                if(nazwa.equals("") || haslo.equals("") || nick.equals(""))
                {
                    Snackbar.make(mLayout, "Nie wszystkie dane zostały wypełnione",
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
                else {

                       if(botyCheckBox.isChecked() && number == 1)
                       {
                           Toast.makeText(getApplicationContext(),"Wybrano gre z jednym gracze. Wybierz więcej by grać z botami!!!",Toast.LENGTH_SHORT).show();
                       }
                       else {

                           new CreateGame().execute("http://polizlo.5v.pl/test/NowaGra.php");
                       }
                }
            }
        });


        cd = new ConnectionDetector(this);
        timerInternet = new Timer();
        czyInternetDziala = new CzyInternetDziala(this);
        timerInternet.schedule(czyInternetDziala,0,1*3000);


    }






    private class CreateGame extends AsyncTask<String, Void, String> {

        // okienko dialogowe, które każe użytkownikowi czekać
        private ProgressDialog dialog = new ProgressDialog(CreateGameActivity.this);

        private String nazwa;
        private String haslo;
        private String nick;



        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {


            EditText ni = (EditText) findViewById(R.id.nickEditText);
            EditText naz = (EditText) findViewById(R.id.nameEditText);
            EditText has = (EditText) findViewById(R.id.passEditText);
            this.nazwa=naz.getText().toString();
            this.haslo=has.getText().toString();
            this.nick=ni.getText().toString();

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
                data.put("nazwa",this.nazwa);
                data.put("haslo",this.haslo);
                data.put("nick",this.nick);
                data.put("max",number);
                data.put("czas",czas);
                if(botyCheckBox.isChecked())
                data.put("boty","T");
                else
                data.put("boty","N");



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
                else
                    Log.d("Informacja:", "Udalo sie");

                // pobranie odpowiedzi serwera
                InputStream in = new BufferedInputStream(
                        connection.getInputStream());

                // konwersja InputStream na String
                // wynik będzie przekazany do metody onPostExecute()
                return streamToString(in);

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(GameActivity.class.getSimpleName(), e.toString());
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


                Log.e("TakaSamaNazwa",json.toString());
                if(Integer.parseInt(json.optString("status")) == 1)
                {

                    Toast.makeText(getApplicationContext(),json.optString("msg"),Toast.LENGTH_SHORT).show();
                    new DolaczDoGry().execute("http://polizlo.5v.pl/test/DolaczDoGry.php");

                }
                else if(Integer.parseInt(json.optString("status")) == 2)
                {
                    Toast.makeText(getApplicationContext(),json.optString("msg"),Toast.LENGTH_SHORT).show();
                }



            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(GameActivity.class.getSimpleName(), e.toString());
            }




        }
    }








    private class DolaczDoGry extends AsyncTask<String, Void, String> {

        private String n;
        private String nk;

        // okienko dialogowe, które każe użytkownikowi czekać
        private ProgressDialog dialog = new ProgressDialog(CreateGameActivity.this);


        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {

            EditText ni = (EditText) findViewById(R.id.nickEditText);
            EditText naz = (EditText) findViewById(R.id.nameEditText);
            this.nk= ni.getText().toString();
            this.n = naz.getText().toString();

            Log.e("Naz",n);
            Log.e("Nick",nk);

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
                    Intent intent = new Intent(CreateGameActivity.this,GamePanelActivity.class);
                    intent.putExtra("status","Z");
                    intent.putExtra("nazwa",this.n);
                    intent.putExtra("nick",this.nk);
                    intent.putExtra("max",number);
                    intent.putExtra("czas",czas);
                    intent.putExtra("boty",botyCheckBox.isChecked());
                    startActivity(intent);
                }

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }
        }
    }





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
            Log.d(GameActivity.class.getSimpleName(), e.toString()+"inputstrinf to string");
        }

        return stringBuilder.toString();
    }





    class CzyInternetDziala extends TimerTask
    {

        private  CreateGameActivity parent;

        public CzyInternetDziala(CreateGameActivity parent) {
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
