package com.example.piotr.pinzynierska;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Piotr on 2018-01-22.
 */

public class Przeciwnik
{

    private LatLng latlng;
    private int czas_walki;
    private boolean odwiedzony;

    public Przeciwnik(LatLng latLng, int czas_walki)
    {
        this.latlng = latLng;
        this.czas_walki = czas_walki;
        this.odwiedzony = false;

    }


}
