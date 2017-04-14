package com.turovetsnikita.belrwclient;

/**
 * Created by Nikita on 23.2.17.
 */

public class Train {
    String num;
    byte lines;
    String tr_route, t_depart, t_travel, t_arrival;
    short[] train_seats;
    float[] train_price;

    Boolean reg;
    String alldays, routehref, seatshref;

    Train(String num, byte lines, String tr_route, String t_depart, String t_travel,
          String t_arrival, short[] seats, float[] price, Boolean reg, String alldays,
          String routehref, String seatshref) {
        this.num = num;
        this.lines = lines;

        this.tr_route = tr_route;

        this.t_depart = t_depart;
        this.t_travel = t_travel;
        this.t_arrival = t_arrival;

        this.train_seats = seats;
        this.train_price = price;

        this.reg = reg;

        this.alldays = alldays;

        this.routehref = routehref;
        this.seatshref = seatshref;
    }

    Train(String num, byte lines, String tr_route, String t_depart, String t_travel,
          String t_arrival, float[] seats, float[] price, Boolean reg, String alldays,
          String routehref, String seatshref) {
        this.num = num;
        this.lines = lines;

        this.tr_route = tr_route;

        this.t_depart = t_depart;
        this.t_travel = t_travel;
        this.t_arrival = t_arrival;

        short newseats[] = {0,0,0,0,0,0};
        for (int i=0; i<6; i++) newseats[i] = (short) seats[i];
        this.train_seats = newseats;

        this.train_price = price;

        this.reg = reg;

        this.alldays = alldays;

        this.routehref = routehref;
        this.seatshref = seatshref;
    }
}
