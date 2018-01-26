package com.example.piotr.pinzynierska;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = GameActivity.class.getSimpleName();
    private ArrayList<Player> players;
    private Player ja;
    private String nazwa;
    private String status;
    private boolean firstTime = true;
    private boolean isSetArea = false;
    private boolean isSetNapad = false;
    private Circle area;
    private Marker napadRabunkowy;
    private Polyline line;
    public final static int FIGHT = 0;
    boolean odwiedzony = false;
    boolean lupy = false;
    private ArrayList<Marker> dziuple;
    private ArrayList<Circle> pola;
    private char dziupleOdwiedzone[];
    private ArrayList<Marker> przeciwnicy;
    private char przeciwnicyOdwiedzeni[];

    private TextView czas;
    private CountDownTimer countDownTimer;
    private int czasGry;
    private long timeLeftInMiliseconds = 1000*60;
    private boolean timeRunning;
    private boolean boty = false;
    private  boolean botyUstawione = false;
    private  int max;
    private boolean wyslanieBotow = false;
    private TextView punktyPaneltextView;
    private TextView critPanelTextView;

    AlertDialog.Builder mBuilder;
    View mView;
    TextView txtView;
    Button btn;
    AlertDialog dialogZlodziej;

    AlertDialog.Builder pBuilder;
    View pView;
    TextView ptxtView;
    AlertDialog dialogPolicjant;

    AlertDialog.Builder kBuilder;
    View kView;
    TextView ktxtView;
    AlertDialog dialogKoniec;
    Button kbtn;

    AlertDialog.Builder poBuilder;
    View poView;
    TextView poPrtxtView;
    TextView poGrtxtView;
    TextView poWygranatxtView;
    Button pobtn;
    AlertDialog dialogPrzeciwnik;


    Timer timerStartGame;

    StartGryPoNapadzie startGryPoNapadzie;


    Timer timer;
    CzyZlodziejZostalZlapany czyZlodziejZostalZlapany;
    Criteria locationCriteria;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        czas = (TextView) findViewById(R.id.textView10);
        punktyPaneltextView = (TextView) findViewById(R.id.punktyPanelView);
        critPanelTextView = (TextView) findViewById(R.id.critPanelView);



        Intent intnet = getIntent();
        players = new ArrayList<>();
        dziuple = new ArrayList<>();
        przeciwnicy = new ArrayList<>();
        pola = new ArrayList<>();
        ja = new Player(intnet.getStringExtra("nick"),intnet.getStringExtra("nacja"));
        this.nazwa = intnet.getStringExtra("nazwa");
        this.status=intnet.getStringExtra("status");
        this.boty = intnet.getBooleanExtra("boty",false);
        this.max = intnet.getIntExtra("max",1);
        this.czasGry = intnet.getIntExtra("czas",15);
        this.timeLeftInMiliseconds=1000*60*czasGry;
        punktyPaneltextView.setText("Punkty: "+ja.getPoints());
        critPanelTextView.setText("Szansa na krytyczne uderzenie: "+ja.getCrit());
        setLocationMen();

        if(ja.getNacja().equals("Z")) {
            timer = new Timer();
            czyZlodziejZostalZlapany = new CzyZlodziejZostalZlapany();
            timer.schedule(czyZlodziejZostalZlapany, 0, 1 * 3000);
        }

        timerStartGame = new Timer();
        startGryPoNapadzie = new StartGryPoNapadzie(this);
        timerStartGame.schedule(startGryPoNapadzie,0,1*1000);

        updateTime();



             mBuilder = new AlertDialog.Builder(GameActivity.this);
             mView = getLayoutInflater().inflate(R.layout.dialog_zlodziej, null);
            txtView = (TextView) mView.findViewById(R.id.napisZlodzieje);
             btn = (Button) mView.findViewById(R.id.btnZlodzieje);
            txtView.setText("Udaj się do miejsca napadu rabunkowego!!!");
            mBuilder.setView(mView);
            dialogZlodziej = mBuilder.create();
        dialogZlodziej.setCancelable(false);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogZlodziej.cancel();
                }
            });



        pBuilder = new AlertDialog.Builder(GameActivity.this);
        pView = getLayoutInflater().inflate(R.layout.dialog_policjant, null);
        ptxtView = (TextView) pView.findViewById(R.id.napisPol);
        ptxtView.setText("Oczekiwanie, aż złodzieje zrobią napad rabunkowy");
        pBuilder.setView(pView);
        dialogPolicjant = pBuilder.create();
        dialogPolicjant.setCancelable(false);


        kBuilder = new AlertDialog.Builder(GameActivity.this);
        kView = getLayoutInflater().inflate(R.layout.dialog_koniec, null);
        ktxtView = (TextView) kView.findViewById(R.id.koniecTextView);
        kbtn = (Button) kView.findViewById(R.id.koniecButton);
        ktxtView.setText("Koniec Gry!!!");
        kBuilder.setView(kView);
        dialogKoniec = kBuilder.create();
        dialogKoniec.setCancelable(false);

        kbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new UstawPunkty().execute("http://polizlo.5v.pl/test/DodajPunkty.php");
                for(int p =0;p<=players.size();p++)
                {
                    new UstawPunktyBotow(p).execute("http://polizlo.5v.pl/test/DodajPunkty.php");
                }
                Intent intent = new Intent(getApplicationContext(),ResultActivity.class);
                intent.putExtra("nazwa",nazwa);
                intent.putExtra("status",status);
                startActivity(intent);
                dialogKoniec.cancel();
            }
        });



    }


    public void startStop()
    {
        if(timeRunning)
        {
            stopTimer();
        }
        else
        {
            startTimer();
        }
    }

    public void startTimer()
    {
           countDownTimer = new CountDownTimer(timeLeftInMiliseconds,1000) {
               @Override
               public void onTick(long millisUntilFinished) {

                   timeLeftInMiliseconds = millisUntilFinished;
                   updateTime();
               }

               @Override
               public void onFinish() {

               }
           }.start();

        timeRunning = true;
    }

    public void stopTimer()
    {
        countDownTimer.cancel();
        timeRunning=false;
    }

    public void updateTime()
    {
        int minutes = (int) timeLeftInMiliseconds/60000;
        int seconds = (int) timeLeftInMiliseconds % 60000 /1000;

        String timeLeftText;

        if(minutes<10) timeLeftText="0"+minutes;
        else
        timeLeftText = ""+minutes;

        timeLeftText+=":";
        if(seconds<10) timeLeftText+="0";
        timeLeftText+=seconds;

        czas.setText(timeLeftText);

        if(minutes == 0 && seconds ==1)
        {
            dialogKoniec.show();
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;




        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        mMap.setMinZoomPreference(15.0f);
        mMap.setMaxZoomPreference(20.0f);

        //klikniecie na złodzieja
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                boolean w= false;
                int ooo=0;
                if(ja.getNacja().equals("P"))
                {
                    if(marker.getTag().equals("Z"))
                    {
                        for(int i=0;i<players.size();i++)
                        {
                            if(players.get(i).getNick().equals(marker.getTitle()))
                            {
                                ooo=i;
                                if(players.get(i).getAtak())
                                {
                                    w = true;
                                }
                            }
                        }

                        if(w)
                        {
                            Toast.makeText(getApplicationContext(),"Zlodziej juz z Toba walczyl",Toast.LENGTH_SHORT).show();
                        }
                        else {

                                 if(boty)
                                 {
                                     Intent intent = new Intent(getApplicationContext(),PrzeciwnikFightActivity.class);
                                      intent.putExtra("ja",ja.getNick());
                                      intent.putExtra("przeciwnik",marker.getTitle());
                                     intent.putExtra("miss",ja.getMiss());
                                     intent.putExtra("crit",ja.getCrit());
                                     intent.putExtra("boty",true);
                                     players.get(ooo).setAtak();
                                     startActivityForResult(intent, FIGHT);
                                 }
                                 else {
                                     czyZlodziejWalczy c = new czyZlodziejWalczy();
                                     c.setZ(marker.getTitle());
                                     c.execute("http://polizlo.5v.pl/test/CzyZlodziejWalczy.php");
                                 }
                        }
                        // Intent intent = new Intent(getApplicationContext(),FightActivity.class);
                        // intent.putExtra("nazwaGry",nazwa);
                        // intent.putExtra("ja",ja.getNick());
                        // intent.putExtra("przeciwnik",marker.getTitle());
                        // startActivity(intent);
                    }
                }
                else
                {
                    if(marker.getTag().equals("tak"))
                    {
                        Toast.makeText(getApplicationContext(),"OK",Toast.LENGTH_SHORT).show();

                        if(odwiedzony)
                        {

                            if(lupy==false)
                            {
                                int ilezaniesionych = 0;
                                for(int a=0;a<dziupleOdwiedzone.length;a++)
                                {
                                    if(dziupleOdwiedzone[a]=='T')
                                        ilezaniesionych++;
                                }


                                if(ilezaniesionych==dziupleOdwiedzone.length) {

                                    Toast.makeText(getApplicationContext(), "Wszystkie lupy zaniesione", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    lupy = true;
                                    Toast.makeText(getApplicationContext(), "Zanies łupy do dziupli", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"Nie zaniosles lupów do dziupli",Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            new CzyNapadnieto().execute("http://polizlo.5v.pl/test/CzyNapadnieto.php");
                        }
                    }
                    if(marker.getTag().equals("nie"))
                    {
                        Toast.makeText(getApplication(),"Za daleko",Toast.LENGTH_SHORT).show();
                    }


                    for(int i =0;i<dziuple.size();i++) {

                        if (marker.getTag().equals("lupyOK"+i)) {

                            if (odwiedzony) {
                                if (lupy == true) {
                                    if(dziupleOdwiedzone[i]=='N') {
                                        Toast.makeText(getApplication(), "Zaniosles lupy", Toast.LENGTH_SHORT).show();
                                        lupy = false;
                                        dziupleOdwiedzone[i] = 'T';
                                        ja.addPoints(1);
                                    }
                                    else
                                    {
                                        Toast.makeText(getApplication(), "Zaniosles już łupy do tej dziupli", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else {
                                    Toast.makeText(getApplication(), "Brak lupów", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(getApplication(), "Napad nie został zrobiony", Toast.LENGTH_SHORT).show();
                                // new CzyNapadnieto().execute("http://polizlo.5v.pl/test/CzyNapadnieto.php");
                            }

                        }
                        if (marker.getTag().equals("lupyNO"+i)) {
                            Toast.makeText(getApplicationContext(), "Za daleko", Toast.LENGTH_SHORT).show();
                        }
                    }


                }


                for(int g =0;g<przeciwnicy.size();g++) {
                    if (marker.getTag().equals("przeciwnik" + g) && getDistanceInMeters(ja.getLatlng(),przeciwnicy.get(g).getPosition())<ja.getZasieg().getRadius() ) {
                        if (przeciwnicyOdwiedzeni[g] == 'T')
                            Toast.makeText(getApplicationContext(), "Przeciwnik już pokonany!!!", Toast.LENGTH_SHORT).show();
                        else {
                            przeciwnicyOdwiedzeni[g] = 'T';
                            przeciwnicy.get(g).setVisible(false);
                            Intent intent = new Intent(getApplicationContext(), PrzeciwnikFightActivity.class);

                            intent.putExtra("ja", ja.getNick());
                            intent.putExtra("miss", ja.getMiss());
                            intent.putExtra("crit", ja.getCrit());
                            startActivityForResult(intent, FIGHT);
                        }
                    }
                    if(marker.getTag().equals("przeciwnik" + g) && getDistanceInMeters(ja.getLatlng(),przeciwnicy.get(g).getPosition())>ja.getZasieg().getRadius())
                    {
                        Toast.makeText(getApplicationContext(),"za daleko",Toast.LENGTH_SHORT).show();
                    }
                }


                return false;
            }
        });




    }


    //Lokalizacja
    private void setLocationMen()
    {

        locationCriteria = new Criteria();
        locationCriteria.setAccuracy(Criteria.ACCURACY_FINE);


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }



        locationManager.requestLocationUpdates(locationManager.getBestProvider(locationCriteria,true), 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(ja.getMarker()!=null)
                    ja.getMarker().remove();
                if(ja.getZasieg()!=null)
                    ja.getZasieg().remove();
                if(line!=null)
                    line.remove();

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                LatLng pozycja = new LatLng(latitude,longitude);
                ja.setLatLng(pozycja);

                /*

                if(isSetArea==false && isSetNapad ==false) {
                    Log.e("LOKALIZACJA", pozycja.toString());
                    //ustawiamy obszar gry
                    if (status.equals("Z")) {
                        setObszarGry(pozycja);
                        setNapadRabunkowy(pozycja);
                    } else {

                        new PobierzObszar().execute("http://polizlo.5v.pl/test/PobierzObszar.php");
                        new PobierzNapad().execute("http://polizlo.5v.pl/test/PobierzNapadRabunkowy.php");
                    }
                }

                  */
                if(!isSetArea) {
                    Log.e("LOKALIZACJA", pozycja.toString());
                    //ustawiamy obszar gry
                    if (status.equals("Z")) {
                        setObszarGry(pozycja);
                        setNapadRabunkowy(pozycja);
                        setDziuple(pozycja);
                        setPola(pozycja);
                        setPrzeciwnicy(pozycja);




                        mMap.moveCamera(CameraUpdateFactory.newLatLng(ja.getLatlng()));

                    } else {

                        new PobierzObszar().execute("http://polizlo.5v.pl/test/PobierzObszar.php");
                        new PobierzNapad().execute("http://polizlo.5v.pl/test/PobierzNapadRabunkowy.php");
                        new PobierzDziuple().execute("http://polizlo.5v.pl/test/PobierzDziuple.php");
                        new PobierzPola().execute("http://polizlo.5v.pl/test/PobierzPola.php");
                        new PobierzPrzeciwnikow().execute("http://polizlo.5v.pl/test/PobierzPrzeciwnikow.php");
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(ja.getLatlng()));

                    }
                    isSetArea = true;
                    //isSetNapad=true;

                    if(ja.getNacja().equals("Z"))
                        dialogZlodziej.show();
                    if(ja.getNacja().equals("P"))
                        dialogPolicjant.show();

                }



                //Ustawienie siebie na mapie
                if(ja.getNacja().equals("P")) {
                    ja.setMarker(mMap.addMarker(new MarkerOptions().position(ja.getLatlng()).title(ja.getNick()).icon(BitmapDescriptorFactory.fromResource(R.drawable.policjant)).flat(false)));
                    ja.getMarker().setTag("JA");
                }
                else {
                    ja.setMarker(mMap.addMarker(new MarkerOptions().position(ja.getLatlng()).title(ja.getNick()).icon(BitmapDescriptorFactory.fromResource(R.drawable.zlodziej)).flat(false)));
                    ja.getMarker().setTag("JA");
                    //  line =  mMap.addPolyline(new PolylineOptions().add(ja.getLatlng()).add(napadRabunkowy.getPosition()).color(Color.RED) );
                }



                ja.setZasieg(mMap.addCircle(new CircleOptions().center(ja.getLatlng()).radius(100).strokeWidth(10).strokeColor(Color.WHITE).fillColor(Color.argb(128, 176, 224, 230)).clickable(true)));

                new AktualizujLokalizacje().execute("http://polizlo.5v.pl/test/DodajLokalizacje.php");



                new PobierzGraczy().execute("http://polizlo.5v.pl/test/PobierzGraczy.php");





                //wyswietlanie na mapie graczy, sprawdzanie czy złodiej jest w zasęgu widocznosci policjanta
                for(int i =0;i<players.size();i++)
                {
                    Player p = players.get(i);
                    if(p.getMarker()!=null)
                        p.getMarker().remove();

                    if(p.getNacja().equals("P")) {
                        p.setMarker(mMap.addMarker(new MarkerOptions().position(p.getLatlng()).title(p.getNick()).icon(BitmapDescriptorFactory.fromResource(R.drawable.policjant)).flat(false)));
                        p.getMarker().setTag("P");
                    }
                    else {


                        if(ja.getNacja().equals("P")) {



                            double distance = getDistanceInMeters(ja.getLatlng(),p.getLatlng());

                            if(distance<ja.getZasieg().getRadius())
                            {
                                p.setMarker(mMap.addMarker(new MarkerOptions().position(p.getLatlng()).title(p.getNick()).icon(BitmapDescriptorFactory.fromResource(R.drawable.zlodziej)).flat(false)));
                                p.getMarker().setVisible(true);
                                p.getMarker().setTag("Z");



                            }
                            else {
                                p.setMarker(mMap.addMarker(new MarkerOptions().position(p.getLatlng()).title(p.getNick()).icon(BitmapDescriptorFactory.fromResource(R.drawable.zlodziej)).flat(false)));
                                p.getMarker().setVisible(false);
                                p.getMarker().setTag("Z");
                            }
                        }
                        else
                            p.setMarker(mMap.addMarker(new MarkerOptions().position(p.getLatlng()).title(p.getNick()).icon(BitmapDescriptorFactory.fromResource(R.drawable.zlodziej)).flat(false)));
                        p.getMarker().setTag("Z");
                    }
                }




            //sprawdzenie czy Policjant jest w zaaiegu pola
                if(ja.getNacja().equals("P"))
                {


                    for(int i = 0; i < pola.size(); i++)
                    {

                        double distance = getDistanceInMeters(ja.getLatlng(),pola.get(i).getCenter());

                        if(distance<pola.get(i).getRadius())
                        {
                            Log.e("dist",String.valueOf(distance));
                           for(int a = 0;a<players.size();a++)
                           {


                               Player p = players.get(a);

                               double distance2 = getDistanceInMeters(ja.getLatlng(),p.getLatlng());

                               if(players.get(a).getNacja().equals("Z") && distance2>ja.getZasieg().getRadius())
                               {
                                   if(p.getMarker()!=null)
                                       p.getMarker().remove();

                                   p.setMarker(mMap.addMarker(new MarkerOptions().position(p.getLatlng()).title(p.getNick()).icon(BitmapDescriptorFactory.fromResource(R.drawable.zlodziej)).flat(false)));
                                   p.getMarker().setVisible(true);
                                   p.getMarker().setTag("ZP");
                               }

                           }

                        }


                    }

                }



                if(napadRabunkowy!=null) {

                    if (ja.getNacja().equals("Z")) {


                        double distance = getDistanceInMeters(ja.getLatlng(), napadRabunkowy.getPosition());

                        if (distance < ja.getZasieg().getRadius()) {
                            napadRabunkowy.setTag("tak");

                        } else {
                            napadRabunkowy.setTag("nie");
                        }
                    }
                }


                //sprawdzenie czy jestem w zasięgu dziupli

                for(int i = 0;i<dziuple.size();i++) {
                    if (dziuple.get(i) != null) {

                        if (ja.getNacja().equals("Z")) {


                            double distance = getDistanceInMeters(ja.getLatlng(), dziuple.get(i).getPosition());

                            if (distance < ja.getZasieg().getRadius()) {
                                dziuple.get(i).setTag("lupyOK"+i);

                            } else {
                                dziuple.get(i).setTag("lupyNO"+i);
                            }
                        }
                    }
                }



                punktyPaneltextView.setText("Punkty: "+ja.getPoints());
                critPanelTextView.setText("Szansa na krytyczne uderzenie: "+ja.getCrit());


                if(botyUstawione)
                {
                    ruchBotow();
                }




            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider)
            {
                Toast.makeText(getApplicationContext(),"GPS wyłączony",Toast.LENGTH_SHORT).show();
            }
        });


    }





    private void ruchBotow()
    {

        for (int e = 0; e < max-1; e++) {

            if(players.get(e).getLatlng().latitude==0)
            {

            }
            else
                {
                new UaktualnijLokalizacjeBotow(e).execute("http://polizlo.5v.pl/test/DodajLokalizacje.php");
            }
        }


    }


    private void setNapadRabunkowy(LatLng latlng)
    {

        double stopien = 0.0000092;
            Random generator = new Random();
            Random plusMinus = new Random();

            double znak = plusMinus.nextInt(2);
            double los = (generator.nextInt(112)+100)*stopien;

        double lat;
        double lng;
        LatLng srodek = area.getCenter();
        if(znak==0)
            lat = srodek.latitude +los*(-1.0);
        else
            lat = srodek.latitude +los;
        znak = plusMinus.nextInt(2);
        if(znak==0)
            lng = srodek.longitude+los*(-1.0);
        else
            lng=srodek.longitude+los;

        //double radius = area.getRadius();
        LatLng pozycja = new LatLng(lat,lng);


        napadRabunkowy =mMap.addMarker(new MarkerOptions().position(pozycja).title("napad").icon(BitmapDescriptorFactory.fromResource(R.drawable.napad)).flat(false));
        napadRabunkowy.setTag("nie");
        if(ja.getNacja().equals("P"))
            napadRabunkowy.setVisible(false);
        Log.e("NAPADDDDD",napadRabunkowy.getPosition().toString());
        isSetNapad=true;
        new UstawNapad().execute("http://polizlo.5v.pl/test/UstawNapadRabunkowy.php");
    }

    private void setObszarGry(LatLng latlng)
    {


        area = mMap.addCircle(new CircleOptions()
                .center(latlng)
                .radius(300)
                .strokeWidth(10)
                .strokeColor(Color.RED)
                .clickable(true));
        isSetArea=true;
        new UstawObszar().execute("http://polizlo.5v.pl/test/UstawObszar.php");

    }


    private void setDziuple(LatLng latlng)
    {
        double stopien = 0.0000092;
        for(int i =0;i<3;i++) {
            Random generator = new Random();
            Random plusMinus = new Random();

            double znak = plusMinus.nextInt(2);
            double los = (generator.nextInt(212)+1)*stopien;
            double lat;
            double lng;
            LatLng srodek = latlng;
            if (znak == 0)
                lat = srodek.latitude + los * (-1.0);
            else
                lat = srodek.latitude + los;
            znak = plusMinus.nextInt(2);
            if (znak == 0)
                lng = srodek.longitude + los * (-1.0);
            else
                lng = srodek.longitude + los;

            //double radius = area.getRadius();
            LatLng pozycja = new LatLng(lat, lng);

            dziuple.add(mMap.addMarker(new MarkerOptions().position(pozycja).title("dziupla").icon(BitmapDescriptorFactory.fromResource(R.drawable.dziupla)).flat(false)));

        }

        for(int i =0;i<dziuple.size();i++) {
            new UstawDziuple(i).execute("http://polizlo.5v.pl/test/UstawDziuple.php");
        }

        if(ja.getNacja().equals("P"))
        {
            for(int i =0;i<dziuple.size();i++)
            {
                dziuple.get(i).setVisible(false);
            }
        }

        dziupleOdwiedzone = new char[dziuple.size()];
        for(int i =0;i<dziuple.size();i++)
        {
            dziupleOdwiedzone[i] = 'N';
        }



    }




    private void setPola(LatLng latlng)
    {
        double stopien = 0.0000092;
        for(int i =0;i<3;i++) {
            Random generator = new Random();
            Random plusMinus = new Random();

            double znak = plusMinus.nextInt(2);
            double los = (generator.nextInt(212)+1)*stopien;
            double lat;
            double lng;
            LatLng srodek = latlng;
            if (znak == 0)
                lat = srodek.latitude + los * (-1.0);
            else
                lat = srodek.latitude + los;
            znak = plusMinus.nextInt(2);
            if (znak == 0)
                lng = srodek.longitude + los * (-1.0);
            else
                lng = srodek.longitude + los;

            //double radius = area.getRadius();
            LatLng pozycja = new LatLng(lat, lng);




            pola.add(mMap.addCircle(new CircleOptions()
                    .center(pozycja)
                    .radius(30)
                    .strokeWidth(10)
                    .strokeColor(Color.GREEN)
                    .fillColor(Color.argb(128, 102, 255, 255))
                    .clickable(true)));

        }

        for(int i =0;i<pola.size();i++) {
            new UstawPola(i).execute("http://polizlo.5v.pl/test/UstawPola.php");
        }

        if(ja.getNacja().equals("Z"))
        {
            for(int i =0;i<pola.size();i++)
            {
                pola.get(i).setVisible(false);
            }
        }





    }




    private void setPrzeciwnicy(LatLng latlng)
    {double stopien = 0.0000092;
        for(int i =0;i<3;i++) {
            Random generator = new Random();
            Random plusMinus = new Random();

            double znak = plusMinus.nextInt(2);
            double los = (generator.nextInt(212)+1)*stopien;
            double lat;
            double lng;
            LatLng srodek = latlng;
            if (znak == 0)
                lat = srodek.latitude + los * (-1.0);
            else
                lat = srodek.latitude + los;
            znak = plusMinus.nextInt(2);
            if (znak == 0)
                lng = srodek.longitude + los * (-1.0);
            else
                lng = srodek.longitude + los;

            //double radius = area.getRadius();
            LatLng pozycja = new LatLng(lat, lng);
            przeciwnicy.add(mMap.addMarker(new MarkerOptions().position(pozycja).title("przeciwnik").icon(BitmapDescriptorFactory.fromResource(R.drawable.pytajnik2)).flat(false)));

        }

        for(int i =0;i<przeciwnicy.size();i++) {
            przeciwnicy.get(i).setTag("przeciwnik"+i);
            new UstawPrzeciwnikow(i).execute("http://polizlo.5v.pl/test/UstawPrzeciwnikow.php");

        }


        przeciwnicyOdwiedzeni = new char[przeciwnicy.size()];
        for(int i =0;i<przeciwnicy.size();i++)
        {
            przeciwnicyOdwiedzeni[i] = 'N';
        }

    }





    // wysyła na serwer aktualna lokalizacje
    private class AktualizujLokalizacje extends AsyncTask<String, Void, String> {


        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {


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
                data.put("nick",ja.getNick());
                data.put("Lat",String.valueOf(ja.getLatlng().latitude));
                data.put("Lng",String.valueOf(ja.getLatlng().longitude));




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

        }
    }





    // Ustawia obszar gry
    private class UstawObszar extends AsyncTask<String, Void, String> {


        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {


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
                data.put("Lat",String.valueOf(ja.getLatlng().latitude));
                data.put("Lng",String.valueOf(ja.getLatlng().longitude));




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

        }
    }





    // Pobiera obszar gry
    private class PobierzObszar extends AsyncTask<String, Void, String> {


        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {
            Log.e("Pobiram obszar","Pobieram obszar");

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
        protected void onPostExecute(String result)
        {
            Log.e("Pobrany obszar: ",result);

            try {
                JSONObject jsonObject = new JSONObject(result);

                String szerokosc = jsonObject.optString("szerokosc");
                String dlugosc = jsonObject.optString("dlugosc");

                LatLng obszar = new LatLng(Double.parseDouble(szerokosc),Double.parseDouble(dlugosc));

                area = mMap.addCircle(new CircleOptions()
                        .center(obszar)
                        .radius(1000)
                        .strokeWidth(10)
                        .strokeColor(Color.RED)
                        .clickable(true));


                Log.e("Pobralem obszar","Pobralem oszar");
                // isSetArea=true;
            }
            catch (Exception e) {
                // obsłuż wyjątek
                Log.d(GameActivity.class.getSimpleName(), e.toString());
            }
        }
    }



    // Ustawia miejsce napadu
    private class UstawNapad extends AsyncTask<String, Void, String> {


        String lat;
        String lng;
        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {

            lat = String.valueOf(napadRabunkowy.getPosition().latitude);
            lng = String.valueOf(napadRabunkowy.getPosition().longitude);

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
                // Log.e("NAPAD",napadRabunkowy.getPosition().toString());
                data.put("Lat",lat);
                data.put("Lng",lng);

                Log.e("NAPAAD JESTEM",lat);
                Log.e("Napaad Jestem",lng);


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

        }
    }



    // Pobiera miejsce napadu
    private class PobierzNapad extends AsyncTask<String, Void, String> {


        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {

            Log.e("PobieramNapad","PobieramNapad");
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
        protected void onPostExecute(String result)
        {
            Log.e("Pobrany napad: ",result);

            try {
                JSONObject jsonObject = new JSONObject(result);

                String szerokosc = jsonObject.optString("szerokosc");
                String dlugosc = jsonObject.optString("dlugosc");

                LatLng napad = new LatLng(Double.parseDouble(szerokosc),Double.parseDouble(dlugosc));

                napadRabunkowy =mMap.addMarker(new MarkerOptions().position(napad).title("napad").icon(BitmapDescriptorFactory.fromResource(R.drawable.napad)).flat(false));
                // isSetNapad=true;
                Log.e("Pobralem napad","Pobralem napad");
                if(ja.getNacja().equals("P"))
                    napadRabunkowy.setVisible(false);

            }
            catch (Exception e) {
                // obsłuż wyjątek
                Log.d(GameActivity.class.getSimpleName(), e.toString());
            }
        }
    }







    // Ustawia obszar gry
    private class UstawDziuple extends AsyncTask<String, Void, String> {



        int ile;
        String lt;
        String lg;
        UstawDziuple(int a)
        {
            this.ile = a;
            this.lt =String.valueOf(dziuple.get(ile).getPosition().latitude);
            this.lg = String.valueOf(dziuple.get(ile).getPosition().longitude);
        }

        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {


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

                Log.e("DZIUPLE","DZIUPLE");

                JSONObject data = new JSONObject();
                data.put("nazwa",nazwa);
                data.put("Lat",this.lt);
                data.put("Lng",this.lg);






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
                Log.d(GameActivity.class.getSimpleName(), e.toString()+" nieeeeee");
                return null;
            }


        }

        // metoda wykonuje się po zakończeniu metody głównej,
        // której wynik będzie przekazany;
        // w tej metodzie mamy dostęp do UI
        @Override
        protected void onPostExecute(String result) {



        }
    }






    // Ustawia obszar gry
    private class UstawPrzeciwnikow extends AsyncTask<String, Void, String> {



        int ile;
        String lt;
        String lg;
        UstawPrzeciwnikow(int a)
        {
            this.ile = a;
            this.lt =String.valueOf(przeciwnicy.get(ile).getPosition().latitude);
            this.lg = String.valueOf(przeciwnicy.get(ile).getPosition().longitude);
        }

        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {


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

                Log.e("Przeciwnicy","przeciwnicy");

                JSONObject data = new JSONObject();
                data.put("nazwa",nazwa);
                data.put("Lat",this.lt);
                data.put("Lng",this.lg);






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
                Log.d(GameActivity.class.getSimpleName(), e.toString()+" nieeeeee");
                return null;
            }


        }

        // metoda wykonuje się po zakończeniu metody głównej,
        // której wynik będzie przekazany;
        // w tej metodzie mamy dostęp do UI
        @Override
        protected void onPostExecute(String result) {



        }
    }







    // Pobiera dziuple
    private class PobierzDziuple extends AsyncTask<String, Void, String> {


        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {

            Log.e("PobieramDziuple","PobieramDziuple");
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
        protected void onPostExecute(String result)
        {
            Log.e("Pobrany napad: ",result);

            try {
                JSONObject jsonObject = new JSONObject(result);

                JSONArray jsonArrayDlugosci = new JSONArray(jsonObject.optString("dlugosci"));
                JSONArray jsonArraySzerokosci = new JSONArray(jsonObject.optString("szerokosci"));

                for(int i =0;i<jsonArrayDlugosci.length();i++)
                {
                    JSONObject jsonObjD = new JSONObject(jsonArrayDlugosci.getString(i));
                    JSONObject jsonObjS = new JSONObject(jsonArraySzerokosci.getString(i));
                    String dlugosc = jsonObjD.optString("dlugosc");
                    String szerokosc = jsonObjS.optString("szerokosc");


                    LatLng pozycja = new LatLng(Double.parseDouble(szerokosc),Double.parseDouble(dlugosc));
                    dziuple.add(mMap.addMarker(new MarkerOptions().position(pozycja).title("dziupla").icon(BitmapDescriptorFactory.fromResource(R.drawable.dziupla)).flat(false)));

                }

                if(ja.getNacja().equals("P"))
                {
                    for(int i =0;i<dziuple.size();i++)
                    {
                        dziuple.get(i).setVisible(false);
                    }
                }

                dziupleOdwiedzone = new char[dziuple.size()];
                for(int i =0;i<dziuple.size();i++)
                {
                    dziupleOdwiedzone[i] = 'N';
                }


            }
            catch (Exception e) {
                // obsłuż wyjątek
                Log.d(GameActivity.class.getSimpleName(), e.toString());
            }
        }
    }












    // Pobiera przeciwniko
    private class PobierzPrzeciwnikow extends AsyncTask<String, Void, String> {


        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {

            Log.e("PobieramPrzeciwnikow","PobieramPrzeciwnikow");
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
        protected void onPostExecute(String result)
        {
            Log.e("Pobrani przeciwnicy: ",result);

            try {
                JSONObject jsonObject = new JSONObject(result);

                JSONArray jsonArrayDlugosci = new JSONArray(jsonObject.optString("dlugosci"));
                JSONArray jsonArraySzerokosci = new JSONArray(jsonObject.optString("szerokosci"));

                for(int i =0;i<jsonArrayDlugosci.length();i++)
                {
                    JSONObject jsonObjD = new JSONObject(jsonArrayDlugosci.getString(i));
                    JSONObject jsonObjS = new JSONObject(jsonArraySzerokosci.getString(i));
                    String dlugosc = jsonObjD.optString("dlugosc");
                    String szerokosc = jsonObjS.optString("szerokosc");


                    LatLng pozycja = new LatLng(Double.parseDouble(szerokosc),Double.parseDouble(dlugosc));
                    przeciwnicy.add(mMap.addMarker(new MarkerOptions().position(pozycja).title("przeciwnik").icon(BitmapDescriptorFactory.fromResource(R.drawable.pytajnik2)).flat(false)));

                }


                //ogólnie mają być niewidoczni

                przeciwnicyOdwiedzeni = new char[przeciwnicy.size()];
                for(int i =0;i<przeciwnicy.size();i++)
                {
                    przeciwnicy.get(i).setTag("przeciwnik"+i);
                    przeciwnicyOdwiedzeni[i] = 'N';
                }


            }
            catch (Exception e) {
                // obsłuż wyjątek
                Log.d(GameActivity.class.getSimpleName(), e.toString());
            }
        }
    }







    // Ustawia Pola
    private class UstawPola extends AsyncTask<String, Void, String> {



        int ile;
        String lt;
        String lg;
        UstawPola(int a)
        {

            this.ile = a;
            this.lt =String.valueOf(pola.get(ile).getCenter().latitude);
            this.lg = String.valueOf(pola.get(ile).getCenter().longitude);
        }

        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {


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

                Log.e("POLA","POLA");

                JSONObject data = new JSONObject();
                data.put("nazwa",nazwa);
                data.put("Lat",this.lt);
                data.put("Lng",this.lg);






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
                Log.d(GameActivity.class.getSimpleName(), e.toString()+" nieeeeee");
                return null;
            }


        }

        // metoda wykonuje się po zakończeniu metody głównej,
        // której wynik będzie przekazany;
        // w tej metodzie mamy dostęp do UI
        @Override
        protected void onPostExecute(String result) {



        }
    }







    // Pobiera Pola
    private class PobierzPola extends AsyncTask<String, Void, String> {


        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {

            Log.e("PobieramPola","PobieramPola");
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
        protected void onPostExecute(String result)
        {
            Log.e("Pobrane POLE: ",result);

            try {
                JSONObject jsonObject = new JSONObject(result);

                JSONArray jsonArrayDlugosci = new JSONArray(jsonObject.optString("dlugosci"));
                JSONArray jsonArraySzerokosci = new JSONArray(jsonObject.optString("szerokosci"));

                for(int i =0;i<jsonArrayDlugosci.length();i++)
                {
                    JSONObject jsonObjD = new JSONObject(jsonArrayDlugosci.getString(i));
                    JSONObject jsonObjS = new JSONObject(jsonArraySzerokosci.getString(i));
                    String dlugosc = jsonObjD.optString("dlugosc");
                    String szerokosc = jsonObjS.optString("szerokosc");


                    LatLng pozycja = new LatLng(Double.parseDouble(szerokosc),Double.parseDouble(dlugosc));
                    pola.add(mMap.addCircle(new CircleOptions()
                            .center(pozycja)
                            .radius(50)
                            .strokeWidth(10)
                            .strokeColor(Color.GREEN)
                            .fillColor(Color.argb(128, 102, 255, 255))
                            .clickable(true)));


                }

                if(ja.getNacja().equals("Z"))
                {
                    for(int i =0;i<pola.size();i++)
                    {
                        pola.get(i).setVisible(false);
                    }
                }




            }
            catch (Exception e) {
                // obsłuż wyjątek
                Log.d(GameActivity.class.getSimpleName(), e.toString());
            }
        }
    }












    //Pobranie graczy z serwera
    private class PobierzGraczy extends AsyncTask<String, Void, String>
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


        @Override
        protected void onPostExecute(String result) {

            try {
                // reprezentacja obiektu JSON w Javie

                if(firstTime ==true) {
                    Log.e("elko", result);
                    JSONObject jsonObject = new JSONObject(result);
                    Log.d("json: ", jsonObject.toString());


                    JSONArray jsonArrayNicki = new JSONArray(jsonObject.optString("nicki"));
                    JSONArray jsonArrayNacje = new JSONArray(jsonObject.optString("nacje"));
                    JSONArray jsonArraySzerokosci = new JSONArray(jsonObject.optString("szerokosci"));
                    JSONArray jsonArrayDlugosci = new JSONArray(jsonObject.optString("dlugosci"));

                    for (int i = 0; i < jsonArrayNicki.length(); i++) {
                        JSONObject jsonObjNi = new JSONObject(jsonArrayNicki.getString(i));
                        JSONObject jsonObjNa = new JSONObject(jsonArrayNacje.getString(i));
                        JSONObject jsonObjD = new JSONObject(jsonArrayDlugosci.getString(i));
                        JSONObject jsonObjS = new JSONObject(jsonArraySzerokosci.getString(i));
                        String ni = jsonObjNi.optString("nick");
                        String na = jsonObjNa.optString("nacja");

                        double lat = Double.parseDouble(jsonObjS.optString("szerokosc"));
                        double lng = Double.parseDouble(jsonObjD.optString("dlugosc"));

                        if (!(ni.equals(ja.getNick()))) {
                            Log.e("Gracz:",ni);
                            players.add(new Player(ni, na, lat, lng));

                        }
                    }
                    firstTime = false;

                    if(boty) {

                        for (int e = 0; e < max-1; e++) {
                            Log.e("BOTYYYYYYYY", "BOTYYYYYYYYY");
                            new WyslijLokalizacjeBotow(e).execute("http://polizlo.5v.pl/test/DodajLokalizacje.php");


                        }
                        new CzyNapadnieto().execute("http://polizlo.5v.pl/test/CzyNapadnieto.php");

                      botyUstawione=true;
                    }

                }
                else
                {

                    Log.e("elko", result);
                    JSONObject jsonObject = new JSONObject(result);
                    Log.d("json: ", jsonObject.toString());


                    JSONArray jsonArrayNicki = new JSONArray(jsonObject.optString("nicki"));
                    JSONArray jsonArrayNacje = new JSONArray(jsonObject.optString("nacje"));
                    JSONArray jsonArraySzerokosci = new JSONArray(jsonObject.optString("szerokosci"));
                    JSONArray jsonArrayDlugosci = new JSONArray(jsonObject.optString("dlugosci"));

                    for (int i = 0; i < jsonArrayNicki.length(); i++) {
                        JSONObject jsonObjNi = new JSONObject(jsonArrayNicki.getString(i));
                        JSONObject jsonObjNa = new JSONObject(jsonArrayNacje.getString(i));
                        JSONObject jsonObjD = new JSONObject(jsonArrayDlugosci.getString(i));
                        JSONObject jsonObjS = new JSONObject(jsonArraySzerokosci.getString(i));
                        String ni = jsonObjNi.optString("nick");
                        String na = jsonObjNa.optString("nacja");

                        double lat = Double.parseDouble(jsonObjS.optString("szerokosc"));
                        double lng = Double.parseDouble(jsonObjD.optString("dlugosc"));

                        for(int j=0;j<players.size();j++)
                        {
                            if(players.get(j).getNick().equals(ni))
                            {
                                LatLng latLng = new LatLng(lat,lng);
                                players.get(j).setLatLng(latLng);
                                break;
                            }
                        }


                    }

                }

            } catch(Exception e){
                // obsłuż wyjątek
                Log.e(StartActivity.class.getSimpleName(), e.toString()+"O nie!!!");
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



    //zamiana odleglosci na metry
    private double getDistanceInMeters(LatLng point1, LatLng point2)
    {
        double radius = 6371000.0;
        double diffLat = (point1.latitude - point2.latitude) *Math.PI / 180;
        double diffLong = (point1.longitude - point2.longitude) * Math.PI / 180;

        double a = Math.sin(diffLat/2.0) * Math.sin(diffLat/2.0) + Math.cos(point2.latitude*Math.PI / 180)*Math.cos(point1.latitude* Math.PI / 180)*Math.sin(diffLong/2.0)*Math.sin(diffLong/2.0);
        double b = 2* Math.asin(Math.sqrt(a));

        double distance = (radius*b);
        return distance;

    }


    //Sprawdzenie czy zlodziej jest w trakcie walki
    private class czyZlodziejWalczy extends AsyncTask<String, Void, String>
    {

        private String zlodziej;

        public void setZ(String zlodzej)
        {
            this.zlodziej=zlodzej;
        }

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
                data.put("nick", zlodziej);


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
                if(json.opt("status").toString().equals("1"))
                {
                    UstawWalke u = new UstawWalke();
                    u.setZ(zlodziej);
                    u.execute("http://polizlo.5v.pl/test/UstawWalke.php");
                }

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }
        }


    }



    //Ustawienie walki ze złodziejem
    private class UstawWalke extends AsyncTask<String, Void, String>
    {

        private String zlodziej;

        public void setZ(String zlodzej)
        {
            this.zlodziej=zlodzej;
        }
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
                data.put("nickZ", zlodziej);
                data.put("nickP",ja.getNick());


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
                Intent intent = new Intent(getApplicationContext(),FightActivity.class);
                intent.putExtra("nazwaGry",nazwa);
                intent.putExtra("ja",ja.getNick());
                intent.putExtra("przeciwnik",zlodziej);
                intent.putExtra("nacja",ja.getNacja());
                intent.putExtra("miss",ja.getMiss());
                intent.putExtra("crit",ja.getCrit());
                startActivityForResult(intent,FIGHT);

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }
        }


    }







    class CzyZlodziejZostalZlapany extends TimerTask
    {


        public void run()
        {
            String result="";

            try {
                // zakładamy, że jest tylko jeden URL
                URL url = new URL("http://polizlo.5v.pl/test/CzyZlodziejZlapany.php");
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
                data.put("nick",ja.getNick());



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

                if(json.optInt("status") == 0)
                {
                    Intent intent = new Intent(getApplicationContext(),FightActivity.class);
                    intent.putExtra("nazwaGry",nazwa);
                    intent.putExtra("ja",ja.getNick());
                    intent.putExtra("przeciwnik",json.optString("przeciwnik"));
                    intent.putExtra("nacja",ja.getNacja());
                    intent.putExtra("miss",ja.getMiss());
                    intent.putExtra("crit",ja.getCrit());
                    timer.cancel();
                    startActivity(intent);
                }

                //  timer.cancel();
            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }



        }


    }



    //Ustawienie walki ze złodziejem
    private class CzyNapadnieto extends AsyncTask<String, Void, String>
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


                odwiedzony=true;

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }
        }


    }











    class StartGryPoNapadzie extends TimerTask

    {

        private  GameActivity parent;

        public StartGryPoNapadzie(GameActivity parent) {
            this.parent = parent;
        }

        public void run() {

            String result="";

            try {
                // zakładamy, że jest tylko jeden URL
                URL url = new URL("http://polizlo.5v.pl/test/StartGryPoNapadzie.php");
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


                // pobranie danych do InputStream
                InputStream in = new BufferedInputStream(
                        connection.getInputStream());

                // konwersja InputStream na String
                // wynik będzie przekazany do metody onPostExecute()
                result = streamToString(in);

            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }


            try {
                Log.e("STARTGRTPONAPADZIE",result);

                JSONObject json = new JSONObject(result);

                if(Integer.parseInt(json.optString("status")) == 1)
                {


                    parent.runOnUiThread(new Runnable() {
                        public void run() {
                            startTimer();
                            if(ja.getNacja().equals("P"))
                            dialogPolicjant.cancel();
                        }
                    });

                    timerStartGame.cancel();



                }



            } catch (Exception e) {
                // obsłuż wyjątek
                Log.d(StartActivity.class.getSimpleName(), e.toString());
            }

        }
    }




    // wysyła na serwer aktualna lokalizacje
    private class UstawPunkty extends AsyncTask<String, Void, String> {


        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {


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
                data.put("nick",ja.getNick());
                data.put("punkty",String.valueOf(ja.getPoints()));





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

        }
    }








    // wysyła na serwer aktualna lokalizacje
    private class UstawPunktyBotow extends AsyncTask<String, Void, String> {


        int ktoryBot;

        public UstawPunktyBotow(int a)
        {
            this.ktoryBot = a;
        }

        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {


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
                data.put("nick", players.get(ktoryBot).getNick());
                data.put("punkty",String.valueOf(players.get(ktoryBot).getPoints()));





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

        }
    }









    // wysyła na serwer aktualna lokalizacje
    private class WyslijLokalizacjeBotow extends AsyncTask<String, Void, String> {

        int ktoryBot;

        public WyslijLokalizacjeBotow(int a)
        {
            this.ktoryBot = a;
        }


        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {


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
                data.put("nazwa", nazwa);
                data.put("nick", players.get(ktoryBot).getNick());



                double stopien = 0.0000092;
                Random generator = new Random();
                Random plusMinus = new Random();

                double znak = plusMinus.nextInt(2);
                double los = (generator.nextInt(212) + 1) * stopien;
                double lat;
                double lng;
                LatLng srodek = ja.getLatlng();
                if (znak == 0)
                    lat = srodek.latitude + los * (-1.0);
                else
                    lat = srodek.latitude + los;
                znak = plusMinus.nextInt(2);
                if (znak == 0)
                    lng = srodek.longitude + los * (-1.0);
                else
                    lng = srodek.longitude + los;

                //double radius = area.getRadius();
                LatLng poz = new LatLng(lat, lng);


                data.put("Lat", String.valueOf(poz.latitude));
                data.put("Lng", String.valueOf(poz.longitude));

                Random gen = new Random();
                int losik = gen.nextInt(4);
                players.get(ktoryBot).addPoints(losik);



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
                Log.d(GameActivity.class.getSimpleName(), e.toString());
                return null;
            }


        }

        // metoda wykonuje się po zakończeniu metody głównej,
        // której wynik będzie przekazany;
        // w tej metodzie mamy dostęp do UI
        @Override
        protected void onPostExecute(String result) {

        }
    }







    // wysyła na serwer aktualna lokalizacje
    private class UaktualnijLokalizacjeBotow extends AsyncTask<String, Void, String> {

        int ktoryBot;

        public UaktualnijLokalizacjeBotow(int a)
        {
            this.ktoryBot = a;
        }


        // metoda wykonywana jest zaraz przed główną operacją (doInBackground())
        // mamy w niej dostęp do elementów UI
        @Override
        protected void onPreExecute() {


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
                data.put("nazwa", nazwa);
                data.put("nick", players.get(ktoryBot).getNick());



                double stopien = 0.0000092;
                Random generator = new Random();
                Random plusMinus = new Random();

                double znak = plusMinus.nextInt(2);
                double los = (generator.nextInt(3) + 2);

                double lat;
                double lng;
                LatLng srodek = players.get(ktoryBot).getLatlng();
                if (znak == 0)
                    lat = srodek.latitude + los*stopien * (-1.0);
                else
                    lat = srodek.latitude + los*stopien;
                znak = plusMinus.nextInt(2);
                if (znak == 0)
                    lng = srodek.longitude + los*stopien * (-1.0);
                else
                    lng = srodek.longitude + los*stopien;

                //double radius = area.getRadius();
                LatLng poz = new LatLng(lat, lng);


                data.put("Lat", String.valueOf(poz.latitude));
                data.put("Lng", String.valueOf(poz.longitude));





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
                Log.d(GameActivity.class.getSimpleName(), e.toString());
                return null;
            }


        }

        // metoda wykonuje się po zakończeniu metody głównej,
        // której wynik będzie przekazany;
        // w tej metodzie mamy dostęp do UI
        @Override
        protected void onPostExecute(String result) {

        }
    }















    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==FIGHT)
        {
            if(resultCode== Activity.RESULT_OK)
            {
                if(data.getBooleanExtra("wygrana",false))
                {
                    Toast.makeText(getApplicationContext(),"WYGRALES",Toast.LENGTH_SHORT).show();
                    if(ja.getNacja().equals("P"))
                        ja.addPoints(1);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"PRZEGRALES",Toast.LENGTH_SHORT).show();
                }

                if(ja.getNacja().equals("P"))
                {
                    for(int i=0;i<players.size();i++)
                    {
                        if( players.get(i).getNick().equals(data.getStringExtra("przeciwnik")))
                        {
                            players.get(i).setAtak();
                            break;
                        }
                    }
                }

            }
            else
            {
                int czas;
                boolean bbbb;
                czas= data.getIntExtra("czas",0);
                bbbb = data.getBooleanExtra("boty",false);

                Random gen = new Random();





               int czzas_P = gen.nextInt(6)+22;



                poBuilder = new AlertDialog.Builder(GameActivity.this);
                poView = getLayoutInflater().inflate(R.layout.dialog_przeciwnik, null);
                poPrtxtView = (TextView) poView.findViewById(R.id.czasPrzeciwnikaTextView);
                poGrtxtView = (TextView) poView.findViewById(R.id.czasGraczatextView);
                poWygranatxtView= (TextView) poView.findViewById(R.id.wygranaTextView);
                pobtn = (Button) poView.findViewById(R.id.przeciwnikWalkaButton);
                poPrtxtView.setText("Czas przeciwnika: "+czzas_P);
                poGrtxtView.setText("Twój czas: "+czas);
                if(czas>czzas_P)
                {

                    poWygranatxtView.setText("Przegrana!!!");
                }
                else
                {
                    if(bbbb)
                    {
                       poWygranatxtView.setText("Wygrana!!!!");
                       ja.addPoints(1);
                    }
                    else {
                        poWygranatxtView.setText("Wygrana!!!! (+5% do krytycznego uderzenia!!!");
                        ja.setCrit(5);
                    }
                }
                poBuilder.setView(poView);
                dialogPrzeciwnik = poBuilder.create();
                dialogPrzeciwnik.setCancelable(false);


                pobtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogPrzeciwnik.cancel();
                    }
                });



               dialogPrzeciwnik.show();



            }
        }
    }
}
