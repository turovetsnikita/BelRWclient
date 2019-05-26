package com.turovetsnikita.belrwclient;

/**
 * Created by Nikita on 25.2.17.
 */

class Recent {
    String from_value,from_exp,from_esr,to_value,to_exp,to_esr;

    Recent(String from_value, String from_exp, String from_esr, String to_value, String to_exp, String to_esr) {
        this.from_value = from_value;
        this.from_exp = from_exp;
        this.from_esr = from_esr;
        this.to_value = to_value;
        this.to_exp = to_exp;
        this.to_esr = to_esr;
    }

}