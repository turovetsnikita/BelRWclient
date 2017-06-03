package com.turovetsnikita.belrwclient;

import org.jsoup.Connection;

/**
 * Created by Nikita on 3.6.17.
 */

public class SavedPassengers {
    String surname,name,patronymic,doc_type,doc_num;
    Connection connection;

    SavedPassengers(String surname, String name, String patronymic, String doc_type, String doc_num, Connection connection) {
        this.surname = surname;
        this.name = name;
        this.patronymic = patronymic;
        this.doc_type = doc_type;
        this.doc_num = doc_num;
        this.connection = connection;
    }
}
