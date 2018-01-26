package com.example.piotr.pinzynierska;

/**
 * Created by Piotr on 2017-11-29.
 */

public class Game
{
    public final String name;
    public final String zalozyciel;
    public final int max_osob;
    public final String stan;
    public final int ile_osob;

    public Game(String name, String zalozyciel,int max_osob, String stan, int ile_osob)
    {
        this.name = name;
        this.zalozyciel = zalozyciel;
        this.max_osob = max_osob;
        this.stan=stan;
        this.ile_osob=ile_osob;
    }
}
