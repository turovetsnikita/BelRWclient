package com.turovetsnikita.belrwclient;

/**
 * Created by Nikita on 4.3.17.
 */


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class TrainRVAdapter extends RecyclerView.Adapter<TrainRVAdapter.TrainViewHolder> {

    public static class TrainViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView num,tr_route,t_depart,t_travel,t_arrival,
                train_seats_o,train_price_o,train_seats_s,train_price_s,train_seats_sc,train_price_sc,
                train_seats_c,train_price_c,train_seats_sw,train_price_sw,train_seats_su,train_price_su,reg,
                alldays_text;
        LinearLayout seats_prices,alldays;
        RelativeLayout train_o,train_s,train_sc,train_c,train_sw,train_su;

        TrainViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);

            num = (TextView)itemView.findViewById(R.id.train_num);

            tr_route  = (TextView)itemView.findViewById(R.id.train_route);
            t_depart = (TextView)itemView.findViewById(R.id.train_depart_time);
            t_travel = (TextView)itemView.findViewById(R.id.train_travel_time);
            t_arrival = (TextView)itemView.findViewById(R.id.train_arrival_time);

            seats_prices= (LinearLayout) itemView.findViewById(R.id.seats_prices);

            train_seats_o= (TextView)itemView.findViewById(R.id.train_seats_o);
            train_seats_s= (TextView)itemView.findViewById(R.id.train_seats_s);
            train_seats_sc= (TextView)itemView.findViewById(R.id.train_seats_sc);
            train_seats_c= (TextView)itemView.findViewById(R.id.train_seats_c);
            train_seats_sw= (TextView)itemView.findViewById(R.id.train_seats_sw);
            train_seats_su= (TextView)itemView.findViewById(R.id.train_seats_su);

            train_price_o= (TextView)itemView.findViewById(R.id.train_price_o);
            train_price_s= (TextView)itemView.findViewById(R.id.train_price_s);
            train_price_sc= (TextView)itemView.findViewById(R.id.train_price_sc);
            train_price_c= (TextView)itemView.findViewById(R.id.train_price_c);
            train_price_sw= (TextView)itemView.findViewById(R.id.train_price_sw);
            train_price_su= (TextView)itemView.findViewById(R.id.train_price_su);

            train_o= (RelativeLayout)itemView.findViewById(R.id.train_o);
            train_s= (RelativeLayout)itemView.findViewById(R.id.train_s);
            train_sc= (RelativeLayout)itemView.findViewById(R.id.train_sc);
            train_c= (RelativeLayout)itemView.findViewById(R.id.train_c);
            train_sw= (RelativeLayout)itemView.findViewById(R.id.train_sw);
            train_su= (RelativeLayout)itemView.findViewById(R.id.train_su);

            reg  = (TextView)itemView.findViewById(R.id.train_reg);

            alldays= (LinearLayout) itemView.findViewById(R.id.alldays);
            alldays_text= (TextView) itemView.findViewById(R.id.alldays_text);
        }
    }

    List<Train> train;

    TrainRVAdapter(List<Train> train){
        this.train = train;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public TrainViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_train, viewGroup, false);
        TrainViewHolder pvh = new TrainViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(TrainViewHolder trainViewHolder, int i) {
        String date_format = "%.2f",
                green = "#43a047",orange = "#fb8c00", red = "#e53935",
                med_grey = "#BDBDBD", black = "#212121";
        trainViewHolder.num.setText(train.get(i).num);
        switch (train.get(i).lines) {
            case 1: {
                trainViewHolder.num.setBackgroundResource(R.drawable.rounded_blue);
                break;
            }
            case 2: {
                trainViewHolder.num.setBackgroundResource(R.drawable.rounded_green);
                break;
            }
            case 3: {
                trainViewHolder.num.setBackgroundResource(R.drawable.rounded_yellow);
                break;
            }
            case 4: {
                trainViewHolder.num.setBackgroundResource(R.drawable.rounded_red);
                break;
            }
            default: {
                trainViewHolder.num.setBackgroundResource(R.drawable.rounded_grey);
                break;
            }
        }

        trainViewHolder.tr_route.setText(train.get(i).tr_route);
        trainViewHolder.t_arrival.setText(train.get(i).t_arrival);

        trainViewHolder.t_travel.setVisibility(View.VISIBLE);
        trainViewHolder.t_travel.setText(train.get(i).t_travel);

        trainViewHolder.seats_prices.setVisibility(View.GONE);
        trainViewHolder.alldays.setVisibility(View.GONE);

        trainViewHolder.reg.setVisibility(View.INVISIBLE);

        if (train.get(i).t_depart.contains("-")) { // если отправился
            trainViewHolder.t_depart.setTextColor(Color.parseColor(med_grey));
            trainViewHolder.t_travel.setTextColor(Color.parseColor(med_grey));
            trainViewHolder.t_arrival.setTextColor(Color.parseColor(med_grey));
            trainViewHolder.tr_route.setTextColor(Color.parseColor(med_grey));
            trainViewHolder.num.setTextColor(Color.parseColor(med_grey));
            trainViewHolder.num.setBackgroundResource(R.drawable.rounded_hidden);
            trainViewHolder.t_depart.setText(train.get(i).t_depart.substring(1,train.get(i).t_depart.length()));
        }
        else {
            trainViewHolder.t_depart.setTextColor(Color.parseColor(black));
            trainViewHolder.t_travel.setTextColor(Color.parseColor(black));
            trainViewHolder.t_arrival.setTextColor(Color.parseColor(black));
            trainViewHolder.tr_route.setTextColor(Color.parseColor(black));
            trainViewHolder.num.setTextColor(Color.parseColor(black));
            trainViewHolder.t_depart.setText(train.get(i).t_depart);

            int j = 0;

            if (train.get(i).alldays.equals("")) { // если смотрим на конкретный день
                trainViewHolder.cv.setFocusable(true);
                trainViewHolder.reg.setVisibility(View.INVISIBLE);

                TextView seats = trainViewHolder.train_seats_o, price = trainViewHolder.train_price_o;
                RelativeLayout seats_layout = trainViewHolder.train_o;
                String cur = "BYN";
                do {
                    switch (j) {
                        case 0: {
                            seats = trainViewHolder.train_seats_o;
                            price = trainViewHolder.train_price_o;
                            seats_layout = trainViewHolder.train_o;
                            break;
                        }
                        case 1: {
                            seats = trainViewHolder.train_seats_s;
                            price = trainViewHolder.train_price_s;
                            seats_layout = trainViewHolder.train_s;
                            break;
                        }
                        case 2: {
                            seats = trainViewHolder.train_seats_sc;
                            price = trainViewHolder.train_price_sc;
                            seats_layout = trainViewHolder.train_sc;
                            break;
                        }
                        case 3: {
                            seats = trainViewHolder.train_seats_c;
                            price = trainViewHolder.train_price_c;
                            seats_layout = trainViewHolder.train_c;
                            break;
                        }
                        case 4: {
                            seats = trainViewHolder.train_seats_su;
                            price = trainViewHolder.train_price_su;
                            seats_layout = trainViewHolder.train_su;
                            break;
                        }
                        case 5: {
                            seats = trainViewHolder.train_seats_sw;
                            price = trainViewHolder.train_price_sw;
                            seats_layout = trainViewHolder.train_sw;
                            break;
                        }
                    }
                    seats_layout.setVisibility(View.GONE);
                    if (train.get(i).train_seats[j] != 0) {

                        trainViewHolder.seats_prices.setVisibility(View.VISIBLE);
                        seats_layout.setVisibility(View.VISIBLE);
                        if (train.get(i).train_price[j] == -1) price.setText(R.string.text_no_data);
                        else {
                            if (train.get(i).train_price[j] < 0)
                                price.setText("от " + String.format(Locale.FRANCE, date_format, Math.abs(train.get(i).train_price[j])) + " " + cur);
                            else
                                price.setText("" + String.format(Locale.FRANCE, date_format, train.get(i).train_price[j]) + " " + cur);
                        }

                        seats.setTextColor(Color.parseColor(green));
                        if (train.get(i).train_seats[j] < 50)
                            seats.setTextColor(Color.parseColor(orange));
                        if (train.get(i).train_seats[j] < 15)
                            seats.setTextColor(Color.parseColor(red));
                        if (train.get(i).train_seats[j] == 1000)
                            seats.setText(R.string.text_a_lot_of_seats);
                        else seats.setText(String.valueOf(train.get(i).train_seats[j]));
                    }
                    j++;
                } while (j < 6);
                if (train.get(i).reg) trainViewHolder.reg.setVisibility(View.VISIBLE);
            }
            else { // если смотрим на все дни
                trainViewHolder.cv.setFocusable(true);
                //trainViewHolder.cv.setClickable(false);

                trainViewHolder.alldays.setVisibility(View.VISIBLE);

                trainViewHolder.reg.setVisibility(View.GONE);

                if (train.get(i).alldays.contains("кроме")) trainViewHolder.alldays_text.setTextColor(Color.parseColor(red));
                else trainViewHolder.alldays_text.setTextColor(Color.parseColor(black));
                trainViewHolder.alldays_text.setText(train.get(i).alldays);
            }
        }

    }

    @Override
    public int getItemCount() {
        return train.size();
    }

}
