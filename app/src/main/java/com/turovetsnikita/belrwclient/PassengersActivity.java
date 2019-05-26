package com.turovetsnikita.belrwclient;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PassengersActivity extends AppCompatActivity {

    Spinner doctype;
    EditText surname,name,patronymic,docnum;
    String sur,nm,ptnm,dc;
    Context context = PassengersActivity.this;
    Button order_button;
    Connection.Response pass_data, rules_page;
    String jsessionid = "",sessionid = "";
    private List<SavedPassengers> savedPassengersList;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passengers);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        surname = (EditText) findViewById(R.id.Surname);
        name = (EditText) findViewById(R.id.Name);
        patronymic = (EditText) findViewById(R.id.Patronymic);
        docnum = (EditText) findViewById(R.id.DocNum);

        doctype = (Spinner) findViewById(R.id.DocType);
        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.DocTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        doctype.setAdapter(adapter);
        doctype.setSelection(0);

        order_button = (AppCompatButton) findViewById(R.id.order_button);
        order_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sur = surname.getText().toString();
                nm = name.getText().toString();
                ptnm = patronymic.getText().toString();
                dc = docnum.getText().toString();
                new gotobuying().execute();
            }
        });

        sp = PreferenceManager.getDefaultSharedPreferences(context);
        new gotoorder().execute(); //получаем актуальные данные о местах, подготовка к заказу (15 мин)

    }

    //
    private class gotoorder extends AsyncTask<String,Integer,Document> {
        @Override
        protected Document doInBackground(String... arg) {
            Document doc;
            String href_post;
            Elements findparam;
            Random r;
            do {
                try {
                    r = new Random(System.currentTimeMillis());
                    Thread.sleep(r.nextInt(701));
                }
                catch (InterruptedException ex) {
                    System.out.println("got interrupted!");
                }
            }
            while (!isOnline());

            try {
                String mainrwby = getIntent().getStringExtra("home");
                String useragent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
                String form_pref = "viewns_7_48QFVAUK6HA180IQAQVJU80004";

                Connection.Response response = Jsoup //передача формы с данными о поезде на poezd.rw.by
                        .connect("https://poezd.rw.by/wps/PA_eTicketInquire/PaymentRedirect")
                        .userAgent(useragent)
                        .method(Connection.Method.POST)
                        .data("ClientNumber", "1")
                        .data("DepartureStation", getIntent().getStringExtra("depart_station"))
                        .data("ArrivalStation", getIntent().getStringExtra("arrival_station"))
                        .data("TrainNumber", getIntent().getStringExtra("num"))
                        .data("DepartureDate", getIntent().getStringExtra("depart_date"))
                        .data("DepartureTime", getIntent().getStringExtra("depart_time"))
                        .data("CarriageNumber", getIntent().getStringExtra("car_num"))
                        .data("CancelUrl", mainrwby)
                        .data("SuccessUrl", "http://rasp.rw.by/ru")
                        .data("CheckValue", getIntent().getStringExtra("hash")) // содержится в ответе ajax
                        .data("ServiceClass", getIntent().getStringExtra("class"))
                        .followRedirects(true)
                        .execute();

                for (Map.Entry<String, String> cookie : response.cookies().entrySet())
                    if (cookie.getKey().equals("JSESSIONID")) jsessionid = cookie.getValue();

                doc = response.parse();
                if (doc.select("td.status").text().contains("Вход")) { //если не залогинены (что вряд ли)

                    findparam = doc.select("form");
                    href_post = findparam.attr("action"); // длинная кракозябра в url после авторизации (без нее нет перехода)

                    response = Jsoup //авторизация
                            .connect("https://poezd.rw.by" + href_post)
                            .userAgent(useragent)
                            .method(Connection.Method.POST)
                            .data("login", sp.getString("login",""))
                            .data("password", sp.getString("password",""))
                            .data("_rememberUser", "on")
                            .data("_login", "Войти в систему")
                            .cookies(response.cookies())
                            .followRedirects(true)
                            .execute();
                }

                rules_page = response;

                doc = response.parse();

                findparam = doc.select("form");
                href_post = findparam.attr("action"); // еще более длинная кракозябра в url после галочки

                response = Jsoup //принятие правил пользования сайтом (да-да, и так каждый раз)
                        .connect("https://poezd.rw.by" + href_post)
                        .userAgent(useragent)
                        .method(Connection.Method.POST)
                        .data(form_pref+"_:form1:conf", "on")
                        .data(form_pref+"_:form1:nextBtn", "Продолжить")
                        .data("com.sun.faces.VIEW", doc.getElementsByAttributeValue("id","com.sun.faces.VIEW").attr("value"))
                        .data(form_pref+"_:form1", form_pref+"_:form1")
                        .cookies(response.cookies())
                        .cookie("JSESSIONID", jsessionid) //храним его с момента авторизации
                        .followRedirects(true)
                        .execute();

                pass_data = response;
                doc = response.parse();
            }
            catch (Exception e) {
                return null;
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document result) {
            if (result!=null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("test")
                        .setMessage(result.select("input").toString()+"/n"+
                                result.select("p").toString()+"/n"+
                                result.select("td.status").text())
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            else { // ошибка соединения/html пуст
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Ошибка")
                        .setMessage("Ошибка/таймаут соединения")
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            String useragent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
            String form_pref = "viewns_7_48QFVAUK6HA180IQAQVJU80004";

            Connection.Response response = null;
            Elements findparam = result.getElementsByAttributeValue("id","viewns_7_48QFVAUK6HA180IQAQVJU80004_:pass:tableEx1:0:pp");
            Document doc = null;

            /*try {
                response = Jsoup //получение данных о сохраненных пассажирах
                        .connect("https://poezd.rw.by/wps/PA_eTicketInquire/faces/jsps/SelectPassenger.jsp?"+
                                "userId=" + getParam(findparam.toString(),"userId") +
                                "&name="+ form_pref + "_:pass:tableEx1:0:pp&type=0&language=ru"+
                                "&sessionId=" + getParam(findparam.toString(),"sessionId") +
                                "&globalPrice=false")
                        .userAgent(useragent)
                        .method(Connection.Method.POST)
                        .execute();
            }
            catch (Exception e) {

            }

            findparam = doc.getElementsByAttributeValue("id","view:form1");
            findparam = findparam.select("tbody");
            findparam = findparam.select("tr");

            savedPassengersList = new ArrayList<>();
            for (int i=0;i<findparam.size();i++) {
                Elements sel = findparam.get(i).select("span");
                Connection connection = Jsoup
                        .connect("https://poezd.rw.by/wps/PA_eTicketInquire/faces/jsps/SelectPassenger.jsp")
                        .method(Connection.Method.POST)
                        //.data("view:form1:passengers:1:btn:x", "11")
                        //.data("view:form1:passengers:1:btn:y", "10")
                        .data("view:form1:type1", "0")
                        .data("view:form1:userId1", doc.getElementsByAttributeValue("id","view:form1:userId1").attr("value"))
                        .data("view:form1:name1", doc.getElementsByAttributeValue("id","view:form1:name1").attr("value"))
                        .data("view:form1:selid1", String.valueOf(i))
                        .data("view:form1:globalPrice1", doc.getElementsByAttributeValue("id","view:form1:globalPrice1").attr("value"))
                        .data("view:form1:sessionId", doc.getElementsByAttributeValue("id","view:form1:sessionId").attr("value"))
                        .data("view:form1:passengers", "on")
                        .data("com.sun.faces.VIEW", doc.getElementsByAttributeValue("id","com.sun.faces.VIEW").attr("value"))
                        .data("view:form1", "view:form1");
                savedPassengersList.add(i,new SavedPassengers(sel.get(0).text(),sel.get(1).text(),sel.get(2).text(),
                        sel.get(3).text(),sel.get(4).text(), connection));
            }

            final CharSequence[] pass_items = new CharSequence[savedPassengersList.size()];
            for (int i=0;i<savedPassengersList.size();i++) {
                pass_items[i]=savedPassengersList.get(i).surname+" "+savedPassengersList.get(i).name+" "+
                        savedPassengersList.get(i).patronymic+" "+savedPassengersList.get(i).doc_type+" "+
                        savedPassengersList.get(i).doc_num;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Сохраненные пассажиры")
                    .setItems(pass_items,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Do anything you want here
                            dialog.cancel();
                        }
                    })
                    .setCancelable(false)
                    .setNegativeButton("Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
            */
            /*
            button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", editText.getText().toString());
                clipboard.setPrimaryClip(clip);
            }
            }); */

        }
    }

    //
    private class gotobuying extends AsyncTask<String,Integer,Document> {
        @Override
        protected Document doInBackground(String... arg) {
            Document doc;
            String href_post;
            Elements findparam;
            Random r;
            do {
                try {
                    r = new Random(System.currentTimeMillis());
                    Thread.sleep(r.nextInt(701));
                }
                catch (InterruptedException ex) {
                    System.out.println("got interrupted!");
                }
            }
            while (!isOnline());

            try {
                doc = pass_data.parse();
                String useragent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";

                String pre = "viewns_7_48QFVAUK6HA180IQAQVJU80004";

                sessionid = doc.getElementsByAttributeValue("id",pre+"_:pass:sessionId").attr("value");

                Connection.Response response = Jsoup //передаем введенные данные
                        .connect("https://poezd.rw.by" + doc.getElementsByAttributeValue("id",pre+"_:viewFragmentT:menu").attr("action"))
                        .userAgent(useragent)
                        .method(Connection.Method.POST)
                        .data(pre+"_:pass:tableEx1:0:thidden", "0")
                        .data(pre+"_:pass:tableEx1:0:lastname", sur)
                        .data(pre+"_:pass:tableEx1:0:name", nm)
                        .data(pre+"_:pass:tableEx1:0:patronymic", ptnm)
                        .data(pre+"_:pass:tableEx1:0:selectType", "ПБ")
                        .data(pre+"_:pass:tableEx1:0:docNum", dc)
                        .data(pre+"_:pass:tableEx1:0:selectPlCount", "1")
                        .data(pre+"_:pass:id1:typeSelector", "true")/*
                        .data(pre+"_:pass:id1:cbox1", "on")
                        .data(pre+"_:pass:id1:fromPlaces", "12")
                        .data(pre+"_:pass:id1:toPlaces", "12")*/
                        .data(pre+"_:pass:id1:freePlaces", doc.getElementsByAttributeValue("id",pre+"_:pass:id1:freePlaces").attr("value"))
                        .data(pre+"_:pass:id1:firstSeat", doc.getElementsByAttributeValue("id",pre+"_:pass:id1:firstSeat").attr("value"))
                        .data(pre+"_:pass:id1:lastSeat", doc.getElementsByAttributeValue("id",pre+"_:pass:id1:lastSeat").attr("value"))
                        .data(pre+"_:pass:conf", "on")
                        .data(pre+"_:pass:sc", doc.getElementsByAttributeValue("id",pre+"_:pass:sc").attr("value"))
                        .data(pre+"_:pass:sci", "")
                        .data(pre+"_:pass:placesTag", "")
                        .data(pre+"_:pass:trainName", doc.getElementsByAttributeValue("id",pre+"_:pass:trainName").attr("value"))
                        .data(pre+"_:pass:passCount", "1")
                        .data(pre+"_:pass:sessionId", sessionid)
                        .data(pre+"_:pass:language1", "ru")
                        .data(pre+"_:pass:df", doc.getElementsByAttributeValue("id",pre+"_:pass:df").attr("value"))
                        .data(pre+"_:pass:nextBtn", doc.getElementsByAttributeValue("id",pre+"_:pass:nextBtn").attr("value"))
                        .data("com.sun.faces.VIEW", doc.getElementsByAttributeValue("id","com.sun.faces.VIEW").attr("value"))
                        .data(pre+"_:pass", pre+"_:pass")
                        .cookies(rules_page.cookies())
                        .cookie("JSESSIONID", jsessionid)
                        .followRedirects(true)
                        .execute();

                doc = response.parse();

                href_post = doc.getElementsByAttributeValue("id",pre+"_:viewFragmentT:menu").attr("action");

                response = Jsoup //проверяем и подтверждаем заказ
                        .connect("https://poezd.rw.by" + href_post)
                        .userAgent(useragent)
                        .method(Connection.Method.POST)
                        .data(pre+"_:confirm:_id131", doc.getElementsByAttributeValue("id",pre+"_:confirm:_id131").attr("value"))
                        .data(pre+"_:confirm:sessionId", sessionid)
                        .data("com.sun.faces.VIEW", doc.getElementsByAttributeValue("id","com.sun.faces.VIEW").attr("value"))
                        .data(pre+"_:confirm", pre+"_:confirm")
                        .cookies(rules_page.cookies())
                        .cookie("JSESSIONID", jsessionid)
                        .followRedirects(true)
                        .execute();

                doc = response.parse();

                href_post = doc.getElementsByAttributeValue("id",pre+"_:viewFragmentT:menu").attr("action");

                response = Jsoup //выбираем способ оплаты для получения кода в ЕРИП
                        .connect("https://poezd.rw.by" + href_post)
                        .userAgent(useragent)
                        .method(Connection.Method.POST)
                        .data("paymentSystem", pre+"_:confirm:form1:1:paymentSystem")
                        .data(pre+"_:confirm:registrationNeeded", "on")
                        .data(pre+"_:confirm:_id182", doc.getElementsByAttributeValue("id",pre+"_:confirm:_id182").attr("value"))
                        .data(pre+"_:confirm:regFlagInfo", doc.getElementsByAttributeValue("id",pre+"_:confirm:regFlagInfo").attr("value"))
                        .data(pre+"_:confirm:confirmMess", doc.getElementsByAttributeValue("id",pre+"_:confirm:confirmMess").attr("value"))
                        .data(pre+"_:confirm:ptime", doc.getElementsByAttributeValue("id",pre+"_:confirm:ptime").attr("value"))
                        .data(pre+"_:confirm:sessionId", sessionid)
                        .data(pre+"_:confirm:showConfirmMess2", doc.getElementsByAttributeValue("id",pre+"_:confirm:showConfirmMess2").attr("value"))
                        .data(pre+"_:confirm:allowBuyMore24H", doc.getElementsByAttributeValue("id",pre+"_:confirm:allowBuyMore24H").attr("value"))
                        .data(pre+"_:confirm:confirmMess2", doc.getElementsByAttributeValue("id",pre+"_:confirm:confirmMess2").attr("value"))
                        .data("com.sun.faces.VIEW", doc.getElementsByAttributeValue("id","com.sun.faces.VIEW").attr("value"))
                        .data(pre+"_:confirm", pre + "_:confirm")
                        .cookies(rules_page.cookies())
                        .cookie("JSESSIONID", jsessionid)
                        .followRedirects(true)
                        .execute();

                doc = response.parse();
            }
            catch (Exception e) {
                return null;
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document result) {
            if (result!=null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                Elements findparam = result.select("div.block_blue");
                builder.setTitle("test")
                        .setMessage(findparam.toString()+
                                result.select("td.status").text()+
                                result.select("input").toString()+"/n"+
                                result.select("p").toString()+"/n")
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            else { // ошибка соединения/html пуст
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Ошибка")
                        .setMessage("Ошибка/таймаут соединения")
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

        }
    }

    // определение наличия подключения к Интернет
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public String getParam (String source, String param) { //string, byte, boolean
        source = cutToParam(source,param);
        param = "\'" + param + "\'";
        if (source.contains(param)) {
            source = source.substring(source.indexOf(param)+param.length()+1, source.indexOf(",\'"));
            source = source.replace("\'","");
            source = source.replace(",",".");
        }
        else source = "";
        return source;
    }

    public String cutToParam(String source, String param) {
        param = "\'" + param + "\'";
        String buf;
        buf = source;
        if (buf.contains(param)) {
            buf = buf.substring(buf.indexOf(param), buf.length());
        }
        else buf = "";
        return buf;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up search_button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
