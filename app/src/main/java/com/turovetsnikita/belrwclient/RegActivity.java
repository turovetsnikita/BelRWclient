package com.turovetsnikita.belrwclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class RegActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
    }

    /*
    try {
                doc = pass_data.parse();
                String useragent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";

                Connection.Response response = Jsoup //передаем введенные данные
                        .connect("https://poezd.rw.by" + )
                        .userAgent(useragent)
                        .method(Connection.Method.POST)
                        .data("lastname", "")
                        .data("firstname", "")
                        .data("patronymic", "")
                        .data("phone", "")
                        .data("country", "")
                        .data("address", "")
                        .data("email", "")
                        .data("sex", "male")
                        .data("age", "")
                        .data("captcha", "")
                        .data("login", "")
                        .data("password", "")
                        .data("appPassword", "")
                        .data("_finish", "Зарегистрировать")
                        //.data("ip", "86.57.167.105")
                        //.data("currentTime", "1503485660676")
                        .followRedirects(true)
                        .execute();

        doc = response.parse();
    }

     */
}
