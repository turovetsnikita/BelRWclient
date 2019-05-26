package com.turovetsnikita.belrwclient.fragments;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.turovetsnikita.belrwclient.R;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Nikita on 15.3.17.
 */

public class CarRVAdapter extends RecyclerView.Adapter<CarRVAdapter.CarViewHolder> {

    DecimalFormat df = new DecimalFormat("00");

    public static class CarViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView num,descr,type,tplaces,lplaces,uplaces,lsplaces,usplaces,numofplaces,price,
            lplacestext,uplacestext,lsplacestext,usplacestext;
        LinearLayout tplacespanel,ulplacespanel,uslsplacespanel;
        RelativeLayout numofplacespanel;

        CarViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);

            num = (TextView) itemView.findViewById(R.id.num);

            type = (TextView) itemView.findViewById(R.id.type);
            descr = (TextView) itemView.findViewById(R.id.descr);

            tplacespanel = (LinearLayout) itemView.findViewById(R.id.tplacespanel);
            tplaces = (TextView) itemView.findViewById(R.id.tplaces);

            ulplacespanel = (LinearLayout) itemView.findViewById(R.id.ulplacespanel);
            lplaces = (TextView) itemView.findViewById(R.id.lplaces);
            uplaces = (TextView) itemView.findViewById(R.id.uplaces);

            uslsplacespanel = (LinearLayout) itemView.findViewById(R.id.uslsplacespanel);
            lsplaces = (TextView) itemView.findViewById(R.id.lsplaces);
            usplaces = (TextView) itemView.findViewById(R.id.usplaces);

            lplacestext = (TextView) itemView.findViewById(R.id.lplacestext);
            uplacestext = (TextView) itemView.findViewById(R.id.uplacestext);
            lsplacestext = (TextView) itemView.findViewById(R.id.lsplacestext);
            usplacestext = (TextView) itemView.findViewById(R.id.usplacestext);

            numofplaces = (TextView) itemView.findViewById(R.id.numofplaces);
            numofplacespanel = (RelativeLayout) itemView.findViewById(R.id.numofplacespanel);

            price = (TextView) itemView.findViewById(R.id.price);
        }
    }

    List<Car> car;

    CarRVAdapter(List<Car> car){
        this.car = car;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public CarViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v;
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_car, viewGroup, false);
        CarViewHolder pvh = new CarViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(CarViewHolder carViewHolder, int i) {
        String med_grey = "#BDBDBD";

        carViewHolder.num.setText(df.format(car.get(i).num));
        carViewHolder.type.setText(car.get(i).typeabbr);
        carViewHolder.descr.setText(car.get(i).type+" "+car.get(i).typeabbrpost+" "+car.get(i).sign);

        carViewHolder.price.setText(""+String.valueOf(car.get(i).price) + " BYN");

        carViewHolder.tplaces.setText(String.valueOf(car.get(i).tplaces));

        if ((car.get(i).lplaces!=-1) && (car.get(i).uplaces!=-1)) {
            carViewHolder.ulplacespanel.setVisibility(View.VISIBLE);
            carViewHolder.lplaces.setText(String.valueOf(car.get(i).lplaces));
            carViewHolder.uplaces.setText(String.valueOf(car.get(i).uplaces));

            if (car.get(i).lplaces == 0) {
                carViewHolder.lplacestext.setTextColor(Color.parseColor(med_grey));
                carViewHolder.lplaces.setTextColor(Color.parseColor(med_grey));
            }

            if (car.get(i).uplaces == 0) {
                carViewHolder.uplacestext.setTextColor(Color.parseColor(med_grey));
                carViewHolder.uplaces.setTextColor(Color.parseColor(med_grey));
            }
        }
        else {
            carViewHolder.ulplacespanel.setVisibility(View.GONE);
        }

        if ((car.get(i).lsplaces!=-1) && (car.get(i).usplaces!=-1)) {
            carViewHolder.uslsplacespanel.setVisibility(View.VISIBLE);
            carViewHolder.lsplaces.setText(String.valueOf(car.get(i).lsplaces));
            carViewHolder.usplaces.setText(String.valueOf(car.get(i).usplaces));

            if (car.get(i).lsplaces == 0) {
                carViewHolder.lsplacestext.setTextColor(Color.parseColor(med_grey));
                carViewHolder.lsplaces.setTextColor(Color.parseColor(med_grey));
            }

            if (car.get(i).usplaces == 0) {
                carViewHolder.usplacestext.setTextColor(Color.parseColor(med_grey));
                carViewHolder.usplaces.setTextColor(Color.parseColor(med_grey));
            }
        }
        else {
            carViewHolder.uslsplacespanel.setVisibility(View.GONE);
        }

        if (!car.get(i).places.equals("")) {
            carViewHolder.numofplacespanel.setVisibility(View.VISIBLE);
            carViewHolder.numofplaces.setText(car.get(i).places);
        }
        else {
            carViewHolder.numofplacespanel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return car.size();
    }

}

