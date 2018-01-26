package com.example.piotr.pinzynierska;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Piotr on 2017-11-29.
 */

public class GamesArrayAdapter extends BaseAdapter {


    Context mContext;
    ArrayList<Game> mGames;
    LayoutInflater mInflater;

    private static class ViewHolder
    {
        ImageView gameIcon;
        TextView nameTextView;
        TextView zalozycielTextView;
        TextView maxPlayersTextView;
        TextView ileOsobTextView;
        TextView stanTextView;
    }

    public  GamesArrayAdapter(Context c, ArrayList<Game> games)
    {
        mContext = c;
        mGames = games;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return mGames.size();
    }

    @Override
    public Object getItem(int position) {
        return mGames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {



        ViewHolder viewHolder;

        if(convertView == null)
        {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.games_list_item,parent,false);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            viewHolder.zalozycielTextView = (TextView) convertView.findViewById(R.id.zalozycielTextView);
            viewHolder.maxPlayersTextView = (TextView) convertView.findViewById(R.id.maxPlayersTextView);
            viewHolder.ileOsobTextView = (TextView) convertView.findViewById(R.id.ileGraczyTextView);
            viewHolder.stanTextView= (TextView) convertView.findViewById(R.id.stanTextView);

            convertView.setTag(viewHolder);

        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Game game = mGames.get(position);
        viewHolder.nameTextView.setText(game.name);
        viewHolder.zalozycielTextView.setText(game.zalozyciel);
        viewHolder.maxPlayersTextView.setText(String.valueOf(game.max_osob));
        viewHolder.ileOsobTextView.setText(String.valueOf(game.ile_osob));
        viewHolder.stanTextView.setText(game.stan);

        return convertView;


    }




}
