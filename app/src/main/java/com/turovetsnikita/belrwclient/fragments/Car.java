package com.turovetsnikita.belrwclient.fragments;

/**
 * Created by Nikita on 20.3.17.
 */

public class Car {

    short num;
    String type,typeabbr,typeabbrpost,sign;
    float price;
    short tplaces;
    byte uplaces,usplaces,lplaces,lsplaces;
    String places,hash;

    Car (short num,String type,String typeabbr,String typeabbrpost,String sign,float price, short tplaces,
         byte lplaces, byte uplaces, byte lsplaces, byte usplaces, String places, String hash) {
        this.num = num;
        this.type = type;
        this.typeabbr = typeabbr;
        this.typeabbrpost = typeabbrpost;
        this.sign = sign;
        this.price = price;

        this.tplaces = tplaces;

        this.lplaces = lplaces;
        this.uplaces = uplaces;
        this.lsplaces = lsplaces;
        this.usplaces = usplaces;

        this.places = places;
        this.hash = hash;
    }
}
