package com.example.piotr.pinzynierska;



import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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


public class GamePanelActivity extends AppCompatActivity {


    String status;
    String nazwa;
    String nick;
    String nacja;
    int max;
    int czas;
    ArrayList<Player> players;
    ListView playersListView;
    Context c ;
    Button losujButton;
    Button startGameButton;
    Button botyButton;
    TextView stanGryTextView;
    TextView gameName;
    Timer timer;
    Timer timer2;
    AktualizujListeGraczy aktualizujListeGraczy;
    boolean losowanie = false;
    boolean boty = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_panel);
        final Intent intnet = getIntent();
        status = intnet.getStringExtra("status");
        nazwa = intnet.getStringExtra("nazwa");
        nick = intnet.getStringExtra("nick");
        max=intnet.getIntExtra("max",1);
        czas = intnet.getIntExtra("czas",czas);
        playersListView = (ListView) findViewById(R.id.playersListView);
        stanGryTextView = (TextView) findViewById(R.id.stanGryTextView);
        gameName = (TextView) findViewById(R.id.gameNameTextView);
        gameName.setText("Nazwa Gry: "+nazwa);
        players = new ArrayList<>();
        c = this;

        losujButton = (Button) findViewById(R.id.losujNacjeButton);
        startGameButton = (Button) findViewById(R.id.starGameButton);
        botyButton = (Button) findViewById(R.id.botyButton);


        playersListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(nick.equals(players.get(position).getNick()))
                {
                   new ZmienNacje().execute("http://polizlo.5v.pl/test/UstawNacje.php");
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Nie możesz zmienić nacji innego gracza",Toast.LENGTH_SHORT).show();
                }
            }
        });





        losujButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Losuj()
                        //      .execute("http://192.168.1.12/test/LosujNacje.php");
                        .execute("http://polizlo.5v.pl/test/LosujNacje.php");
            }
        });

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (losowanie) {

                    if(max > players.size())
                    {
                        Toast.makeText(getApplicationContext(),"Nie wszyscy gracze są zalogowani",Toast.LENGTH_SHORT).show();
                    }
                    else {

                        new RozpocznijGre()
                                //      .execute("http://192.168.1.12/test/UaktualnijStanGry.php");
                                .execute("http://polizlo.5v.pl/test/UaktualnijStanGry.php");
                        timer.cancel();
                        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                        intent.putExtra("nazwa", nazwa);
                        intent.putExtra("nick", nick);
                        intent.putExtra("nacja", nacja);
                        intent.putExtra("status", status);
                        intent.putExtra("max",max);
                        intent.putExtra("czas",czas);
                        if(boty)
                        {
                            intent.putExtra("boty", boty);
                        }
                        startActivity(intent);
                    }

                }
                else
                {
                    Toast.makeText(c,"Nie wylosowano nacji",Toast.LENGTH_SHORT).show();
                }
            }
        });

        botyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(int i =0;i<max-1;i++)
                {

                    new DolaczDoGry(nazwa,"BOT"+(i+1)).execute("http://polizlo.5v.pl/test/DolaczDoGry.php");
                    boty = true;
                }
            }
        });


        if(status.equals("g"))
        {
            losujButton.setVisibility(View.GONE);
            startGameButton.setVisibility(View.GONE);
            botyButton.setVisibility(View.GONE);
            timer2 = new Timer();
            SprawdzCzyGraRozpoczeta spr = new SprawdzCzyGraRozpoczeta();
            timer2.schedule(spr,0,1*3000);

        }
        else
        {
           // stanGryTextView.setVisibility(View.GONE);
        }


        timer = new Timer();
        aktualizujListeGraczy  = new AktualizujListeGraczy();
        timer.schedule(aktualizujListeGraczy,0, 1*3000);

    }



    class AktualizujListeGraczy extends TimerTask
    {


        public void run()
        {
            String result="";

            try {
                // zakładamy, że jest tylko jeden URL
                URL url = new URL("http://polizlo.5v.pl/test/PokazGraczy.php");
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

                Log.e("elko",result);
                JSONObject jsonObject = new JSONObject(result);
                Log.d("json: ",jsonObject.toString());


                JSONArray jsonArrayNick = new JSONArray(jsonObject.optString("nicki"));
                JSONArray jsonArrayNacje = new JSONArray(jsonObject.optString("nacje"));

                for(int i =0;i<jsonArrayNick.length();i++)
                {
                    JSONObject jsonObjNi = new JSONObject(jsonArrayNick.getString(i));
                    JSONObject jsonObjNa = new JSONObject(jsonArrayNacje.getString(i));
                    String ni = jsonObjNi.optString("nick");
                    String na = jsonObjNa.optString("nacja");;
                    if(ni.equals(nick))
                        nacja= na;
                    players.add(new Player(ni,na));

                }

                playersListView.post(new Runnable() {
                    @Override
                    public void run() {
                        PlayersArrayAdapter playersArrayAdapter = new PlayersArrayAdapter(c,players);
                        playersListView.setAdapter(playersArrayAdapter);
                    }
                });



            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }



        }


    }



    class SprawdzCzyGraRozpoczeta extends TimerTask
    {
        @Override
        public void run() {

            String result = "";

            try {
                // zakładamy, że jest tylko jeden URL
                URL url = new URL("http://polizlo.5v.pl/test/PobierzStanGry.php");
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

                result =  streamToString(in);

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());

            }

            try {
                // reprezentacja obiektu JSON w Javie

                JSONObject json = new JSONObject(result);


                //Toast.makeText(getApplicationContext(),"status: "+json.optString("status")+", msg: "+json.optString("msg"),Toast.LENGTH_SHORT).show();
                // pobranie pól obiektu JSON i wyświetlenie ich na ekranie
                // ((TextView) findViewById(R.id.response_id)).setText("status: "+ json.optString("status"));
                // ((TextView) findViewById(R.id.response_name)).setText("msg: "
                //       + json.optString("msg"));
                if(Integer.parseInt(json.optString("status")) == 1)
                {
                    Intent intent = new Intent(c,GameActivity.class);
                    intent.putExtra("nazwa",nazwa);
                    intent.putExtra("nick",nick);
                    intent.putExtra("nacja",nacja);
                    intent.putExtra("status",status);
                    intent.putExtra("max",max);
                    intent.putExtra("czas",czas);
                    timer.cancel();
                    timer2.cancel();
                    startActivity(intent);

                }

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }






        }
    }





    private class Losuj extends AsyncTask<String, Void, String> {

        // okienko dialogowe, które każe użytkownikowi czekać
        private ProgressDialog dialog = new ProgressDialog(GamePanelActivity.this);



        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {

            // wyświetlamy okienko dialogowe każące czekać
            dialog.setMessage("Czekaj...");
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

                Log.d("pokaz: ",result);
                JSONObject json = new JSONObject(result);

                Log.e("Informacjaaaaa: ",json.toString());

                Toast.makeText(getApplicationContext(),"status: "+json.optString("status")+", msg: "+json.optString("msg"),Toast.LENGTH_SHORT).show();
                losowanie = true;
                // pobranie pól obiektu JSON i wyświetlenie ich na ekranie
                // ((TextView) findViewById(R.id.response_id)).setText("status: "+ json.optString("status"));
                // ((TextView) findViewById(R.id.response_name)).setText("msg: "
                //       + json.optString("msg"));
                // if(Integer.parseInt(json.optString("status")) == 1)
                // {
                //    Log.d("msg","siemaneczko");
                //    new PokazGraczy()
                //    .execute("http://192.168.1.12/test/PokazGraczy.php");
                //            .execute("http://polizlo.5v.pl/test/PokazGraczy.php");

                // }

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }
        }
    }




    // wysyła na serwer informacje o rozpoczętej grze
    private class RozpocznijGre extends AsyncTask<String, Void, String> {

        // okienko dialogowe, które każe użytkownikowi czekać
        private ProgressDialog dialog = new ProgressDialog(GamePanelActivity.this);

        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {


            // wyświetlamy okienko dialogowe każące czekać
            dialog.setMessage("Czekaj...");
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
                data.put("nazwa",nazwa);
                data.put("stan","T");



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


                Toast.makeText(getApplicationContext(),"status: "+json.optString("status")+", msg: "+json.optString("msg"),Toast.LENGTH_SHORT).show();
                // pobranie pól obiektu JSON i wyświetlenie ich na ekranie
                // ((TextView) findViewById(R.id.response_id)).setText("status: "+ json.optString("status"));
                // ((TextView) findViewById(R.id.response_name)).setText("msg: "
                //       + json.optString("msg"));

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }
        }
    }







    // wysyła na serwer informacje o rozpoczętej grze
    private class ZmienNacje extends AsyncTask<String, Void, String> {

        // okienko dialogowe, które każe użytkownikowi czekać
        private ProgressDialog dialog = new ProgressDialog(GamePanelActivity.this);

        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {


            // wyświetlamy okienko dialogowe każące czekać
            dialog.setMessage("Czekaj...");
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
                data.put("nazwa",nazwa);
                data.put("nick",nick);



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


                Toast.makeText(getApplicationContext(),"status: "+json.optString("status")+", msg: "+json.optString("msg"),Toast.LENGTH_SHORT).show();
                // pobranie pól obiektu JSON i wyświetlenie ich na ekranie
                // ((TextView) findViewById(R.id.response_id)).setText("status: "+ json.optString("status"));
                // ((TextView) findViewById(R.id.response_name)).setText("msg: "
                //       + json.optString("msg"));

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }
        }
    }







    private class DolaczDoGry extends AsyncTask<String, Void, String> {


        String n;
        String nk;

        public DolaczDoGry(String n, String nk)
        {
            this.n = n;
            this.nk = nk;
        }


        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {


            Log.e("Naz",n);
            Log.e("Nick",nk);

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


            try {
                // reprezentacja obiektu JSON w Javie

                // listView.setVisibility(View.INVISIBLE);

                JSONObject json = new JSONObject(result);



                Toast.makeText(getApplicationContext(),"status: "+json.optString("status")+", msg: "+json.optString("msg"),Toast.LENGTH_SHORT).show();
                // pobranie pól obiektu JSON i wyświetlenie ich na ekranie
                // ((TextView) findViewById(R.id.response_id)).setText("status: "+ json.optString("status"));
                // ((TextView) findViewById(R.id.response_name)).setText("msg: "
                //       + json.optString("msg"));



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
