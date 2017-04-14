package com.turovetsnikita.belrwclient.fragments;

/**
 * Created by Nikita on 20.3.17.
 */

public class Car {

    short num;
    String descr,type;
    float price;
    short tplaces;
    byte uplaces,usplaces,lplaces,lsplaces;
    String places;

    Car (short num,String descr,String type,float price, short tplaces,
         byte lplaces, byte uplaces, byte lsplaces, byte usplaces, String places) {
        this.num = num;
        this.descr = descr;
        this.type = type;
        this.price = price;

        this.tplaces = tplaces;

        this.lplaces = lplaces;
        this.uplaces = uplaces;
        this.lsplaces = lsplaces;
        this.usplaces = usplaces;

        this.places = places;
    }
}
