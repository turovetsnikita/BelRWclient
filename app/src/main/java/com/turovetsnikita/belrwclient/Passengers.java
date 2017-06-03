package com.turovetsnikita.belrwclient;

/**
 * Created by Nikita on 3.6.17.
 */

public class Passengers {
    int num;
    float cost;
    String tariff,places, fio_doc,count,place_type;

    Passengers(int num, String tariff,Float cost,String places,String fio_doc,String count,String place_type) {
        this.num = num;
        this.tariff = tariff;
        this.cost = cost; //2
        this.places = places; //2
        this.fio_doc = fio_doc;
        this.count = count; //1
        this.place_type = place_type; //2
    }
}
