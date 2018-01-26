package com.example.piotr.pinzynierska;


import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Piotr on 2017-09-24.
 */

public class Player {

    private String nick;
    private String nacja;
    private LatLng latlng;
    private Marker marker;
    private Circle mojZasieg;
    private boolean atakowany ;
    private int punkty;
    private int miss;
    private int crit;

    public Player(String nick, String nacja)
    {
        this.nick = nick;
        this.nacja = nacja;
        latlng = new LatLng(0,0);
        marker = null;
        mojZasieg = null;
        this.atakowany=false;
        this.punkty = 0;
        this.miss = 20;
        if(nacja.equals("P"))
        this.crit = 20;
        else this.crit=10;
    }

    public Player(String nick, String nacja, double lat,double lng)
    {
        this.nick = nick;
        this.nacja = nacja;
        latlng = new LatLng(lat,lng);
        marker = null;
        this.atakowany=false;
    }

    public void addPoints(int punkt)
    {
        this.punkty+=punkt;
    }

    public int getPoints()
    {
        return this.punkty;
    }

    public String getNick()
    {
        return this.nick;
    }

    public  String getNacja()
    {
        return this.nacja;
    }

    public void setLatLng(LatLng latLng)
    {
        this.latlng = latLng;
    }

    public LatLng getLatlng()
    {
        return this.latlng;
    }

    public void setMarker(Marker marker)
    {
        this.marker = marker;
    }

    public Marker getMarker()
    {
        return this.marker;
    }

    public void setZasieg(Circle circle)
    {
        this.mojZasieg = circle;
    }

    public Circle getZasieg()
    {
        return mojZasieg;
    }

    public void setAtak()
    {
        this.atakowany=true;
    }

    public boolean getAtak()
    {
        return this.atakowany;
    }

    public int getMiss()
    {
        return this.miss;
    }

    public int getCrit()
    {
        return this.crit;
    }

    public void setCrit(int a)
    {
        this.crit+=a;
    }




}
