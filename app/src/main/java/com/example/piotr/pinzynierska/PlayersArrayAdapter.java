package com.example.piotr.pinzynierska;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Piotr on 2017-09-24.
 */

public class PlayersArrayAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<Player> mPlayers;
    LayoutInflater mInflater;


    private static class ViewHolder
    {
        TextView nickTextView;
        TextView nacjaTextView;
        ImageView imageView;

    }

    public  PlayersArrayAdapter(Context c, ArrayList<Player> players)
    {
        mContext = c;
        mPlayers = players;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return mPlayers.size();
    }

    @Override
    public Object getItem(int position) {
        return mPlayers.get(position);
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
            convertView = mInflater.inflate(R.layout.players_list_item,parent,false);
            viewHolder.nickTextView = (TextView) convertView.findViewById(R.id.nickTextView);
            viewHolder.nacjaTextView = (TextView) convertView.findViewById(R.id.nacjaTextView);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.playerImageView);

            convertView.setTag(viewHolder);

        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Player player = mPlayers.get(position);
        viewHolder.nickTextView.setText(player.getNick());
        viewHolder.nacjaTextView.setText(player.getNacja());
        if(player.getNacja().equals("P"))
        {
            viewHolder.imageView.setImageResource(R.drawable.policjant);
        }
        if(player.getNacja().equals("Z"))
        {
            viewHolder.imageView.setImageResource(R.drawable.zlodziej);
        }


        return convertView;


    }



}
