package com.turovetsnikita.belrwclient;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Nikita on 8.4.17.
 */

public class ScoreboardRVAdapter extends RecyclerView.Adapter<ScoreboardRVAdapter.ScoreboardViewHolder> {

    public static class ScoreboardViewHolder extends RecyclerView.ViewHolder {

        TextView descr,t_depart,t_arrival,tr_route,num,numbering,way,platform,tardiness;
        LinearLayout numbering_layout,way_layout,tardiness_layout;

        ScoreboardViewHolder(final View itemView) {
            super(itemView);

            descr = (TextView)itemView.findViewById(R.id.descr);
            t_depart = (TextView)itemView.findViewById(R.id.train_depart_time);
            t_arrival = (TextView)itemView.findViewById(R.id.train_arrival_time);
            tr_route  = (TextView)itemView.findViewById(R.id.train_route);
            num = (TextView)itemView.findViewById(R.id.train_num);

            numbering= (TextView)itemView.findViewById(R.id.numbering);

            way= (TextView)itemView.findViewById(R.id.way);
            platform= (TextView)itemView.findViewById(R.id.platform);

            tardiness= (TextView)itemView.findViewById(R.id.tardiness);

            numbering_layout= (LinearLayout)itemView.findViewById(R.id.numbering_layout);
            way_layout= (LinearLayout)itemView.findViewById(R.id.way_layout);
            tardiness_layout= (LinearLayout)itemView.findViewById(R.id.tardiness_layout);
        }
    }

    List<Scoreboard> scoreboard;

    ScoreboardRVAdapter(List<Scoreboard> scoreboard){
        this.scoreboard = scoreboard;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ScoreboardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_scoreboard, viewGroup, false);
        ScoreboardViewHolder pvh = new ScoreboardViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(ScoreboardViewHolder scoreboardViewHolder, int i) {
        scoreboardViewHolder.tr_route.setText(scoreboard.get(i).tr_route);
        scoreboardViewHolder.num.setText(scoreboard.get(i).num);

        switch (scoreboard.get(i).lines) {
            case 1: {
                scoreboardViewHolder.num.setBackgroundResource(R.drawable.rounded_blue);
                break;
            }
            case 2: {
                scoreboardViewHolder.num.setBackgroundResource(R.drawable.rounded_green);
                break;
            }
            case 3: {
                scoreboardViewHolder.num.setBackgroundResource(R.drawable.rounded_yellow);
                break;
            }
            case 4: {
                scoreboardViewHolder.num.setBackgroundResource(R.drawable.rounded_red);
                break;
            }
            default: {
                scoreboardViewHolder.num.setBackgroundResource(R.drawable.rounded_grey);
                break;
            }
        }

        if (!scoreboard.get(i).descr.equals("")) {
            scoreboardViewHolder.descr.setText(scoreboard.get(i).descr);
            scoreboardViewHolder.descr.setVisibility(View.VISIBLE);
        }
        else scoreboardViewHolder.descr.setVisibility(View.GONE);

        if (!scoreboard.get(i).t_arrival.equals("")) scoreboardViewHolder.t_arrival.setText(scoreboard.get(i).t_arrival);
        else scoreboardViewHolder.t_arrival.setText("");

        if (!scoreboard.get(i).t_depart.equals("")) scoreboardViewHolder.t_depart.setText(scoreboard.get(i).t_depart);
        else scoreboardViewHolder.t_depart.setText("");

        if (!scoreboard.get(i).numbering.equals("")) {
            scoreboardViewHolder.numbering.setText(scoreboard.get(i).numbering);
            scoreboardViewHolder.numbering_layout.setVisibility(View.VISIBLE);
        }
        else scoreboardViewHolder.numbering_layout.setVisibility(View.GONE);

        if (!scoreboard.get(i).way.equals("")) {
            scoreboardViewHolder.way_layout.setVisibility(View.VISIBLE);
            scoreboardViewHolder.way.setText(scoreboard.get(i).way);
            scoreboardViewHolder.platform.setText(scoreboard.get(i).platform);
        }
        else scoreboardViewHolder.way_layout.setVisibility(View.GONE);

        if (!scoreboard.get(i).tardiness.equals("")) {
            scoreboardViewHolder.tardiness_layout.setVisibility(View.VISIBLE);
            scoreboardViewHolder.tardiness.setText(scoreboard.get(i).tardiness);
        }
        else scoreboardViewHolder.tardiness_layout.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return scoreboard.size();
    }

}
