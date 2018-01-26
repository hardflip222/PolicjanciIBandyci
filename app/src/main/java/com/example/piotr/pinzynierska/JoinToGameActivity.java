package com.example.piotr.pinzynierska;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class JoinToGameActivity extends AppCompatActivity {
    ListView gamesListView;
    ArrayList<Game> games;
    Context c ;
    Timer timer;
    AktualizujTimerTask aktualizujTimerTask;
    ArrayList<Integer> czasy;



    class AktualizujTimerTask extends TimerTask

    {
        public void run() {

            String result="";

            try {
                // zakładamy, że jest tylko jeden URL
                URL url = new URL("http://polizlo.5v.pl/test/PobierzDostepneGry.php");
                URLConnection connection = url.openConnection();

                // pobranie danych do InputStream
                InputStream in = new BufferedInputStream(
                        connection.getInputStream());

                // konwersja InputStream na String
                // wynik będzie przekazany do metody onPostExecute()
                result = streamToString(in);
                Log.e("BLAD",result);

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }



            games.clear();


            try {
                // reprezentacja obiektu JSON w Javie

                //ArrayList<String> gierki = new ArrayList<>();


                // JSONArray jsonArray = new JSONArray(result);

                JSONObject jsonObject = new JSONObject(result);
                Log.e("json: ",jsonObject.toString());

                JSONArray jsonArrayName = new JSONArray(jsonObject.optString("games"));
                JSONArray jsonArrayZalozyciele = new JSONArray(jsonObject.optString("zalozyciele"));
                JSONArray jsonArrayMaxy = new JSONArray(jsonObject.optString("maxy"));
                JSONArray jsonArrayStany = new JSONArray(jsonObject.optString("stany"));
                JSONArray jsonArrayIle = new JSONArray(jsonObject.optString("ile"));
                JSONArray jsonArrayCzasy = new JSONArray(jsonObject.optString("czasy"));

                for(int i =0;i<jsonArrayName.length();i++)
                {
                    JSONObject jsonObjN = new JSONObject(jsonArrayName.getString(i));
                    JSONObject jsonObjZa = new JSONObject(jsonArrayZalozyciele.getString(i));
                    JSONObject jsonObjmax = new JSONObject(jsonArrayMaxy.getString(i));
                    JSONObject jsonObjstan = new JSONObject(jsonArrayStany.getString(i));
                    JSONObject jsonObjile = new JSONObject(jsonArrayIle.getString(i));
                    JSONObject jsonObjczas = new JSONObject(jsonArrayCzasy.getString(i));

                    Log.d("name",jsonArrayName.getString(i));
                    Log.d("zal",jsonArrayZalozyciele.getString(i));
                    Log.d("max",jsonArrayMaxy.getString(i));
                   Log.d("ile",jsonArrayIle.getString(i));

                    games.add(new Game(jsonObjN.optString("nazwa"),jsonObjZa.optString("zalozyciel"),Integer.parseInt(jsonObjmax.optString("mx")),jsonObjstan.optString("stan"),Integer.parseInt(jsonObjile.optString("COUNT(*)"))));
                   czasy.add(Integer.parseInt(jsonObjczas.optString("czas")));
                }




                gamesListView.post(new Runnable() {
                    @Override
                    public void run() {
                        GamesArrayAdapter gamesArrayAdapter = new GamesArrayAdapter(c,games);
                        gamesListView.setAdapter(gamesArrayAdapter);
                    }
                });



            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }

        }
    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_to_game);
        c = this;
        games = new ArrayList<>();
        czasy = new ArrayList<>();
        gamesListView = (ListView) findViewById(R.id.gamesListView);



        gamesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(games.get(position).stan.equals("T"))
                {
                    Toast.makeText(getApplicationContext(),"Gra już rozpoczęta, nie możesz dołączyć",Toast.LENGTH_SHORT).show();
                }
                else {

                    if(games.get(position).ile_osob==games.get(position).max_osob)
                    {
                        Toast.makeText(getApplicationContext(),"Maxymalna ilość osób w grze",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
                        intent.putExtra("name", games.get(position).name);
                        intent.putExtra("czas",czasy.get(position));
                        timer.cancel();
                        startActivity(intent);
                    }
                }
            }
        });


        timer = new Timer();
        aktualizujTimerTask  = new AktualizujTimerTask();
        timer.schedule(aktualizujTimerTask,0, 1*3000);


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
