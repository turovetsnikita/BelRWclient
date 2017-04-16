package com.turovetsnikita.belrwclient.fragments;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.turovetsnikita.belrwclient.R;

import java.util.List;

/**
 * Created by Nikita on 15.3.17.
 */

public class RouteRVAdapter extends RecyclerView.Adapter<RouteRVAdapter.RouteViewHolder> {

    public static class RouteViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView station,stop_time,stop_date,dep_time,dep_date,stop_int;
        RouteViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);

            station = (TextView)itemView.findViewById(R.id.station);
            stop_time = (TextView)itemView.findViewById(R.id.stop_time);
            stop_date = (TextView)itemView.findViewById(R.id.stop_date);
            dep_time = (TextView)itemView.findViewById(R.id.dep_time);
            dep_date = (TextView)itemView.findViewById(R.id.dep_date);
            stop_int = (TextView)itemView.findViewById(R.id.stop_int);
        }
    }

    List<Route> route;

    RouteRVAdapter(List<Route> route){
        this.route = route;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_route, viewGroup, false);
        RouteViewHolder pvh = new RouteViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(RouteViewHolder routeViewHolder, int i) {
        int med_grey = Color.parseColor("#BDBDBD"),
                black = Color.parseColor("#212121"),
                accent = Color.parseColor("#ffa000");

        routeViewHolder.station.setText(route.get(i).station);

        routeViewHolder.stop_date.setText("");
        routeViewHolder.dep_date.setText("");

        if (!route.get(i).stop_int.equals(""))
            routeViewHolder.stop_int.setText(" ("+route.get(i).stop_int+")");
        else routeViewHolder.stop_int.setText("");


        if (route.get(i).visit) {
            routeViewHolder.station.setTextColor(black);
            if ((route.get(i).stop_time.equals("")) && (route.get(i).dep_time.equals("")))
                routeViewHolder.stop_time.setTextColor(med_grey);
            else routeViewHolder.stop_time.setTextColor(black);
            routeViewHolder.stop_date.setTextColor(accent); //
            routeViewHolder.dep_time.setTextColor(black);
            routeViewHolder.dep_date.setTextColor(accent); //
            routeViewHolder.stop_int.setTextColor(black);
        }
        else {
            routeViewHolder.station.setTextColor(med_grey);
            routeViewHolder.stop_time.setTextColor(med_grey);
            routeViewHolder.stop_date.setTextColor(med_grey);
            routeViewHolder.dep_time.setTextColor(med_grey);
            routeViewHolder.dep_date.setTextColor(med_grey);
            routeViewHolder.stop_int.setTextColor(med_grey);
        }

        if ((route.get(i).stop_time.equals("")) && (route.get(i).dep_time.equals(""))) {
            routeViewHolder.stop_time.setText(R.string.text_no_data);
            routeViewHolder.dep_time.setText("");
        }
        else {
            if (!route.get(i).stop_time.equals("")) {
                if (!route.get(i).stop_date.equals("")) {
                    routeViewHolder.stop_time.setText(route.get(i).stop_time + ", ");
                    routeViewHolder.stop_date.setText(route.get(i).stop_date);
                } else
                    routeViewHolder.stop_time.setText(route.get(i).stop_time);
            } else routeViewHolder.stop_time.setText("(Начальная станция)");

            if (!route.get(i).dep_time.equals("")) {
                if (!route.get(i).dep_date.equals("")) {
                    routeViewHolder.dep_time.setText(" — " + route.get(i).dep_time + ", ");
                    routeViewHolder.dep_date.setText(route.get(i).dep_date);
                } else routeViewHolder.dep_time.setText(" — " + route.get(i).dep_time);
            } else routeViewHolder.dep_time.setText(" — (Конечная станция)");
        }

    }

    @Override
    public int getItemCount() {
        return route.size();
    }

}

