package com.turovetsnikita.belrwclient;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardActivity extends AppCompatActivity {

    Context context = ScoreboardActivity.this;

    RecyclerView mrv1;
    private List<Scoreboard> scoreboard;
    LinearLayoutManager llm;
    ScoreboardRVAdapter adapter;
    Toolbar toolbar;
    LinearLayout mprogressbar;
    private CoordinatorLayout coordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);
        coordLayout = (CoordinatorLayout) findViewById(R.id.coordLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mrv1 = (RecyclerView)findViewById(R.id.rv1);
        mrv1.setHasFixedSize(true);
        llm = new LinearLayoutManager(context);
        mrv1.setLayoutManager(llm);
        mprogressbar = (LinearLayout) findViewById(R.id.progressBar);

        initializeData();
        initializeAdapter();

        toolbar.setTitle("Онлайн-табло");
        toolbar.setSubtitle("Минск-Пассажирский");
        setSupportActionBar(toolbar);

        invalidateOptionsMenu();
        mrv1.setVisibility(View.INVISIBLE);
        mprogressbar.setVisibility(View.VISIBLE);

        if (isOnline()) {
            new getScoreboard().execute();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.text_warning)
                    .setMessage(R.string.text_no_internet)
                    .setCancelable(false)
                    .setPositiveButton(R.string.text_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    finish();
                                }
                            })
                    .setNeutralButton(R.string.text_settings,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    finish();
                                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();

            mrv1.setVisibility(View.VISIBLE);
            mprogressbar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume () {
        super.onResume();
    }

    private class getScoreboard extends AsyncTask<String, Integer, Document> {

        @Override
        protected Document doInBackground(String... arg) {
            Document doc;
            try {
                doc = Jsoup.connect("http://rasp.rw.by/ru/tablo/?st_exp=" + "2100000") //пока только Минск
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .timeout(20*1000)
                        .post();
            }
            catch (Exception e) {
                return null;
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document result) {

            if (result!=null) { //html страницы есть
                parseScoreboard(result.select("tr")); //непосредственно парсинг

                if ((scoreboard.size() != 0)) { //порядок
                    initializeAdapter(); //адаптер
                    adapter.notifyItemRangeInserted(0, scoreboard.size()); //анимация
                    invalidateOptionsMenu();
                }
                else { //если нет поездов/ошибка сайта/html не разобран
                    String message;
                    if (result.select("div.b-note")!=null) {
                        message = (result.select("div.b-note")).select("div.error_content").text();
                        //if (message.equals(""))
                    }
                    else message = "По вашему запросу ничего не найдено";

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.text_warning)
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton(R.string.text_ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            onBackPressed();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
            else { //html не получен (==null)
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.text_error)
                        .setMessage(R.string.text_error_timeout)
                        .setCancelable(false)
                        .setPositiveButton(R.string.text_ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            mrv1.setVisibility(View.VISIBLE);
            mprogressbar.setVisibility(View.GONE);
        }
    }

    //непосредственно парсинг
    private void parseScoreboard(Elements elements) {
        Element tr_elmt; //элемент с данными о поезде
        scoreboard.clear();
        for (int i = 0; i < elements.size(); i++) {
            tr_elmt = elements.get(i);
            if (!tr_elmt.hasClass("b-train"))
                continue; //пропустить шапку
            String descr = tr_elmt.select("div.train_description").text();
            byte lines = 0;

            if (descr.contains("Городские")) lines = 4;
            else {
                if (descr.contains("Международные")) lines = 3;
                else {
                    if (descr.contains("Межрегиональные")) lines = 2;
                    else{
                        if (descr.contains("Региональные")) lines = 1;
                    }
                }
            }

            //создаем объект
            scoreboard.add(new Scoreboard(tr_elmt.select("small.train_id").text(),
                    lines,
                    descr,
                    tr_elmt.select("span.train_text").text(),
                    tr_elmt.select("b.train_end-time").text(),
                    tr_elmt.select("b.train_start-time").text(),
                    tr_elmt.select("td.train_item.train_halts.train_number").text().replace("2",""),
                    tr_elmt.select("td.train_item.train_halts.train_way").text(),
                    tr_elmt.select("td.train_item.train_halts.train_platform").text(),
                    tr_elmt.select("td.train_item.train_halts.warning").text()));
        }
    }

    // определение наличия подключения к Интернет
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void initializeData(){
        scoreboard = new ArrayList<>();
    }

    private void initializeAdapter(){
        adapter = new ScoreboardRVAdapter(scoreboard);
        mrv1.setAdapter(adapter);
    }

    //изменение состояний меню извне, с помощью invalidateOptionsMenu()
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (scoreboard.size()!=0) menu.getItem(0).setVisible(true);
        else menu.getItem(0).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.refresh) {
            invalidateOptionsMenu();
            mrv1.setVisibility(View.INVISIBLE);
            mprogressbar.setVisibility(View.VISIBLE);
            if (isOnline()) {
                initializeData();
                initializeAdapter();
                new getScoreboard().execute();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.text_warning)
                        .setMessage(R.string.text_no_internet)
                        .setCancelable(false)
                        .setPositiveButton(R.string.text_ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setNeutralButton(R.string.text_settings,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();

                mrv1.setVisibility(View.VISIBLE);
                mprogressbar.setVisibility(View.GONE);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scoreboard, menu);
        return true;
    }
}
