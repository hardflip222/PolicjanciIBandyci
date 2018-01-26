package com.example.piotr.pinzynierska;



/**
 * Created by Piotr on 2017-12-06.
 */

public class PiontsPlayer {

    private String nick;
    private String nacja;
    private int punkty;

    public PiontsPlayer(String nick, String nacja,int punkty)
    {
        this.nick = nick;
        this.nacja = nacja;
        this.punkty = punkty;
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


}
