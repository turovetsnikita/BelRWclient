package com.turovetsnikita.belrwclient;

/**
 * Created by Nikita on 25.2.17.
 */

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RecentRVAdapter extends RecyclerView.Adapter<RecentRVAdapter.HistoryViewHolder> {

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView depart;
        TextView arrival;

        HistoryViewHolder(final View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            depart = (TextView)itemView.findViewById(R.id.history_depart);
            arrival = (TextView)itemView.findViewById(R.id.history_arrival);

        }
    }

    List<Recent> stations;

    RecentRVAdapter(List<Recent> stations){
        this.stations = stations;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_recent, viewGroup, false);
        HistoryViewHolder pvh = new HistoryViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder historyViewHolder, int i) {
        historyViewHolder.depart.setText(stations.get(i).from_value);
        historyViewHolder.arrival.setText(stations.get(i).to_value);
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

}
