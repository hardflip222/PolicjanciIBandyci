package com.example.piotr.pinzynierska;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Piotr on 2017-12-06.
 */

public class PointsArrayAdapter extends BaseAdapter{


    Context mContext;
    ArrayList<PiontsPlayer> mPlayers;
    LayoutInflater mInflater;


    private static class ViewHolder
    {
        TextView nickTextView;
        TextView nacjaTextView;
        TextView punktyTextView;
        ImageView imageView;

    }

    public  PointsArrayAdapter(Context c, ArrayList<PiontsPlayer> players)
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



        PointsArrayAdapter.ViewHolder viewHolder;

        if(convertView == null)
        {
            viewHolder = new PointsArrayAdapter.ViewHolder();
            convertView = mInflater.inflate(R.layout.punkty_list_item,parent,false);
            viewHolder.nickTextView = (TextView) convertView.findViewById(R.id.pnacjaPunktyTextView);
            viewHolder.nacjaTextView = (TextView) convertView.findViewById(R.id.pnickPunktyTextView);
            viewHolder.punktyTextView = (TextView) convertView.findViewById(R.id.ppunktyPunktyTextView);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.pPunktyImageView);

            convertView.setTag(viewHolder);

        }
        else
        {
            viewHolder = (PointsArrayAdapter.ViewHolder) convertView.getTag();
        }

        PiontsPlayer player = mPlayers.get(position);
        viewHolder.nickTextView.setText(player.getNacja());
        viewHolder.nacjaTextView.setText(player.getNick());
        viewHolder.punktyTextView.setText(String.valueOf(player.getPoints()));
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
