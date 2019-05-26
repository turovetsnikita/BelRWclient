package com.turovetsnikita.belrwclient;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.turovetsnikita.belrwclient.adapters.ItemClickSupport;
import com.turovetsnikita.belrwclient.data.AppBase;
import com.turovetsnikita.belrwclient.data.AppBase.TrainCache;
import com.turovetsnikita.belrwclient.data.AppDbHelper;

public class TrainActivity extends AppCompatActivity {

    Context context = TrainActivity.this;

    RecyclerView mrv1;
    private List<Train> train;
    LinearLayoutManager llm;
    TrainRVAdapter adapter;
    Toolbar toolbar;
    LinearLayout mprogressbar,mnointernet;
    Locale russian = new Locale("ru");
    private CoordinatorLayout coordLayout;

    SimpleDateFormat tf1 = new SimpleDateFormat("HH", russian),
            tf2 = new SimpleDateFormat("mm", russian),
            df1 = new SimpleDateFormat("EE, dd MMM", russian),
            df2 = new SimpleDateFormat("yyyy-MM-dd", russian),
            df3 = new SimpleDateFormat("dd.MM.yyyy", russian);

    String[] cl = {"Общий","Сидячий","Плацкартный","Купе","Мягкий","СВ"};
    String date;
    int LENGTH_VERY_LONG = 5000;

    private AppDbHelper mDbHelper;
    private int firstnotdeparted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        coordLayout = (CoordinatorLayout) findViewById(R.id.coordLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setSupportActionBar(toolbar);

        mrv1 = (RecyclerView)findViewById(R.id.rv1);
        mrv1.setHasFixedSize(true);
        llm = new LinearLayoutManager(context);
        mrv1.setLayoutManager(llm);
        mprogressbar = (LinearLayout) findViewById(R.id.progressBar);
        mnointernet = (LinearLayout) findViewById(R.id.nointernet);

        ItemClickSupport.addTo(mrv1).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() { //нажатие по карточке
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if (isOnline()) {
                    Intent intent = new Intent(context, DetailsActivity.class);
                    intent.putExtra("url", "http://rasp.rw.by/ru/route/?from="+
                            getIntent().getStringExtra("depart")+"&from_exp=0&from_esr=0&to="+
                            getIntent().getStringExtra("arrival")+"&to_exp=0&to_esr=0&date="+df2.format(getIntent().getLongExtra("chosen", 0)));
                    intent.putExtra("depart_num", train.get(position).depart_num);
                    intent.putExtra("arrival_num", train.get(position).arrival_num);
                    intent.putExtra("train_num", train.get(position).num);
                    intent.putExtra("date", df3.format(getIntent().getLongExtra("chosen", 0)));
                    intent.putExtra("time", train.get(position).t_depart);
                    intent.putExtra("tr_route", train.get(position).tr_route);
                    intent.putExtra("routehref", train.get(position).routehref);
                    intent.putExtra("carhref", train.get(position).seatshref);
                    intent.putExtra("seats", train.get(position).train_seats);
                    startActivity(intent); //передаем управление и данные в активити с выбором вагона
                }
                else {
                    mnointernet.setVisibility(View.VISIBLE);
                }

            }
        });
        ItemClickSupport.addTo(mrv1).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                return false;
            }
        });

        String d = getIntent().getStringExtra("from_value"),
                a = getIntent().getStringExtra("to_value");
        int length_limit = 12;
        if (d.length() > length_limit) d = d.substring(0, length_limit) + "..";
        //if (a.length() > length_limit) a = a.substring(0, length_limit);
        if (getIntent().getLongExtra("chosen", 0)!=1)
        toolbar.setTitle("Поезда " + df1.format(getIntent().getLongExtra("chosen", 0)));
        else toolbar.setTitle("Поезда " + "на все дни");
        toolbar.setSubtitle(d + " — " + a);
        setSupportActionBar(toolbar);

        mDbHelper = new AppDbHelper(this);

        initializeData();
        initializeAdapter();
    }

    @Override
    protected void onResume () {
        super.onResume();
        mrv1.setVisibility(View.INVISIBLE);
        mprogressbar.setVisibility(View.VISIBLE);
        initializeData();
        invalidateOptionsMenu();
        train = readTrainCache();
        if (isequalsActivityPreferences() && (train.size()>0)) { //совпадает с недавним ненулевым запросом
            invalidateOptionsMenu();
            mrv1.setVisibility(View.VISIBLE);
            initializeAdapter();
            //adapter.notifyItemRangeInserted(0, train.size()); //анимация
            llm.scrollToPosition(firstnotdeparted);
            Snackbar.make(coordLayout, "Обновлено " + textRefreshTime() + " назад", LENGTH_VERY_LONG)
                    .setAction("Обновить", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mrv1.setVisibility(View.INVISIBLE);
                            mprogressbar.setVisibility(View.VISIBLE);
                            if (isOnline()) { //при наличии сети
                                initializeData();
                                invalidateOptionsMenu();
                                initializeAdapter();
                                new getData().execute();
                            }
                            else { //нет сети
                                mnointernet.setVisibility(View.GONE);
                                mprogressbar.setVisibility(View.GONE);
                            }
                        }
                    }).show();
            mprogressbar.setVisibility(View.GONE);
            mnointernet.setVisibility(View.GONE);
        }
        else { //новый запрос
            if (isOnline()) { //при наличии сети
                initializeData();
                invalidateOptionsMenu();
                new getData().execute();
                mnointernet.setVisibility(View.GONE);
            }
            else { //нет сети
                mnointernet.setVisibility(View.VISIBLE);
                mrv1.setVisibility(View.VISIBLE);
                mprogressbar.setVisibility(View.GONE);
            }
        }
    }

    private class getData extends AsyncTask<String, Integer, Document> {

        @Override
        protected Document doInBackground(String... arg) {
            Document doc;
            if (getIntent().getLongExtra("chosen", 0)!=1) date=df2.format(getIntent().getLongExtra("chosen", 0));
            else date="everyday";
            try {
                doc = Jsoup.connect("http://rasp.rw.by/ru/route/?"+
                        "from="+getIntent().getStringExtra("from_value").replace(" ","%20")+
                        "&from_exp="+getIntent().getStringExtra("from_exp")+
                        "&from_esr="+getIntent().getStringExtra("from_esr")+
                        "&to="+getIntent().getStringExtra("to_value").replace(" ","%20")+
                        "&to_exp="+getIntent().getStringExtra("to_exp")+
                        "&to_esr="+getIntent().getStringExtra("to_esr")+
                        "&date="+date)
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
                parseTrain(result.select("tr")); //непосредственно парсинг

                if ((train.size() != 0)) { //порядок

                    mrv1.setVisibility(View.VISIBLE);
                    mprogressbar.setVisibility(View.GONE);
                    initializeAdapter(); //адаптер
                    llm.scrollToPosition(firstnotdeparted);
                    adapter.notifyItemRangeInserted(0, train.size()); //анимация
                    invalidateOptionsMenu();
                }
                else { //если нет поездов/ошибка сайта/html не разобран
                    String message;
                    if (!result.select("div.b-note").text().equals("")) {
                        message = result.select("div.b-note").text();
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
                        .setMessage(R.string.text_error)
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
        }
    }

    //непосредственно парсинг
    private void parseTrain(Elements elements) {
        Element tr_elmt; //элемент с данными о поезде
        Elements seats_elmt; //элемент с данными о местах на поезд
        long time = System.currentTimeMillis();
        initializeData();
        firstnotdeparted = 0;
        for (int i = 0; i < elements.size(); i++) {
            String alldays = "",href = "",seatshref = "",depart_num = "", arrival_num = "";
            short[] arr1 = {0, 0, 0, 0, 0, 0};
            float[] arr2 = {0, 0, 0, 0, 0, 0};

            tr_elmt = elements.get(i);
            if (!tr_elmt.hasClass("b-train"))
                continue; //пропустить шапку и кнопку "Показать отправившиеся"
            String descr = tr_elmt.select("div.train_description").text(),
                    reg_check = tr_elmt.select("i.b-spec.spec_reserved").attr("title");
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

            String train_start_time = tr_elmt.select("b.train_start-time").text();
            Boolean reg = false;
            String travel_time = "", buf = tr_elmt.select("span.train_time-total").text();
            if (buf.length()>6) {
                if (buf.substring(0,buf.indexOf(" ")).length()<2) travel_time += "+ 0" + buf.substring(0,buf.indexOf(" "));
                else travel_time += "+ " + buf.substring(0,buf.indexOf(" "));
                buf = buf.substring(buf.indexOf(" ")+1,buf.length());
                buf = buf.substring(buf.indexOf(" ")+1,buf.length());
                if (buf.substring(0,buf.indexOf(" ")).length()<2) travel_time += ":0" + buf.substring(0,buf.indexOf(" "));
                else travel_time += ":" + buf.substring(0,buf.indexOf(" "));
            }
            else {
                if (buf.substring(0,buf.indexOf(" ")).length()<2) travel_time += "+ 00:0" + buf.substring(0,buf.indexOf(" "));
                else travel_time += "+ 00:" + buf.substring(0,buf.indexOf(" "));
            }

            if (getIntent().getLongExtra("chosen", 0)!=1) { //смотрим места

                if (reg_check.contains("Возможна")) reg = true;
                String note, pr;

                seats_elmt = tr_elmt.select("ul.train_details-group");
                if (seats_elmt.size() > 0)
                    for (int k = 0; k < seats_elmt.size(); k++) {
                        Element onetype = seats_elmt.get(k);
                        note = onetype.select("li.train_note").text();
                        for (int j = 0; j < 6; j++) {
                            if (note.equals(cl[j]) || ((j == 0) && (note.isEmpty()))) {
                                try {
                                    String pl = onetype.select("li.train_place").text();
                                    pl += " ";
                                    short[] places = {0, 0, 0, 0, 0, 0, 0, 0};
                                    int t = 0;
                                    do {
                                        places[t] = Short.parseShort(pl.substring(0, pl.indexOf(" ")));
                                        pl = pl.substring(pl.indexOf(" ") + 1, pl.length());
                                        t++;
                                    } while (pl.length() > 0);
                                    short res = 0;
                                    for (int y = 0; y < t; y++)
                                        res += places[y];
                                    arr1[j] = res;
                                } catch (NumberFormatException e) {
                                    arr1[j] = 1000; // электричка-дизель = места всегда есть
                                }

                                pr = onetype.select("li.train_price").text();
                                pr = pr.replace(",", ".");
                                //if (pr.contains("руб.")) pr = pr.substring(0, pr.length() - 5);
                                pr = pr.replace(" руб.", "");
                                pr = pr.replace(" ", "\n");
                                pr += "\n";
                                float[] prices = {0, 0, 0, 0, 0, 0, 0, 0};
                                int t = 0;
                                try {
                                    do {
                                        prices[t] = Float.parseFloat(pr.substring(0, pr.indexOf("\n")).replace("\u00a0",""));
                                        pr = pr.substring(pr.indexOf("\n") + 1, pr.length());
                                        t++;
                                    } while (pr.length() > 0);

                                    float res = prices[0];
                                    for (int l = 0; l < t; l++)
                                        if (prices[l] < res) res = prices[l];
                                    if (t > 1) arr2[j] = -res;
                                    else arr2[j] = res;

                                } catch (NumberFormatException e) {
                                    arr2[j] = -1;
                                }
                                break;
                            }
                        }
                    }

                //подготовка части ссылки на свободные места
                href = tr_elmt.select("div.train_name a").attr("href");

                depart_num = href.substring(href.indexOf("from_exp=")+9,href.indexOf("from_exp=")+9+7);
                arrival_num = href.substring(href.indexOf("to_exp=")+7,href.indexOf("to_exp=")+7+7);

                if (((lines==1) || (lines==4)) && (arr1[0]==1000)) seatshref = ""; //на электрички-дизели ссылки нет
                else
                    seatshref = "http://rasp.rw.by/ru/ajax/route/car_places/?from="+depart_num+
                            "&to="+arrival_num+
                            "&date="+df2.format(getIntent().getLongExtra("chosen", 0))+
                            "&train_number="+tr_elmt.select("small.train_id").text()+
                            "&car_type=";

                //отправившиеся поезда
                long chosen = getIntent().getLongExtra("chosen",0);
                if (chosen<time) {
                    int hr = Integer.parseInt(train_start_time.substring(0, 2)),
                            mn = Integer.parseInt(train_start_time.substring(3, 5));
                    if ((hr < Integer.parseInt(tf1.format(time))) ||
                            ((hr == Integer.parseInt(tf1.format(time))) && (mn < Integer.parseInt(tf2.format(time))))) {
                        train_start_time = "-" + train_start_time;
                    }
                }
            }
            else { //смотрим расписание на все дни
                href = tr_elmt.select("div.train_name a").attr("href");
                alldays = tr_elmt.select("td.train_item.train_days.regional_only").text();
            }

            //создаем объект
            train.add(train.size(), new Train(tr_elmt.select("small.train_id").text(),
                    lines,
                    tr_elmt.select("a.train_text").text(),
                    train_start_time,
                    travel_time,
                    tr_elmt.select("b.train_end-time").text().substring(0,5),
                    arr1,
                    arr2,
                    reg,
                    alldays,
                    href,
                    seatshref,
                    depart_num,
                    arrival_num));
            if (train.get(train.size()-1).t_depart.contains("-")) firstnotdeparted = train.size();
        }
        clearTrainCache();
        saveTrainCache(train);
        saveActivityPreferences();
    }

    private void saveTrainCache(List<Train> cached) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < cached.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(TrainCache.num, cached.get(i).num);
                values.put(TrainCache.lines, String.valueOf(cached.get(i).lines));
                values.put(TrainCache.tr_route, cached.get(i).tr_route);
                values.put(TrainCache.t_depart, cached.get(i).t_depart);
                values.put(TrainCache.t_travel, cached.get(i).t_travel);
                values.put(TrainCache.t_arrival, cached.get(i).t_arrival);
                values.put(TrainCache.train_seats, packArray(cached.get(i).train_seats));
                values.put(TrainCache.train_price, packArray(cached.get(i).train_price));
                values.put(TrainCache.reg, String.valueOf(cached.get(i).reg));
                values.put(TrainCache.alldays, cached.get(i).alldays);
                values.put(TrainCache.routehref, cached.get(i).routehref);
                values.put(TrainCache.seatshref, cached.get(i).seatshref);
                values.put(TrainCache.depart_num, cached.get(i).depart_num);
                values.put(TrainCache.arrival_num, cached.get(i).arrival_num);
                long newRowId = db.insert(TrainCache.TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    private void clearTrainCache () {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TrainCache.TABLE_NAME);
            db.execSQL("CREATE TABLE " + AppBase.TrainCache.TABLE_NAME + " ("
                    + AppBase.TrainCache._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + AppBase.TrainCache.num + " TEXT NOT NULL, "
                    + AppBase.TrainCache.lines + " TEXT NOT NULL, "
                    + AppBase.TrainCache.tr_route + " TEXT NOT NULL, "
                    + AppBase.TrainCache.t_depart + " TEXT NOT NULL, "
                    + AppBase.TrainCache.t_travel + " TEXT NOT NULL, "
                    + AppBase.TrainCache.t_arrival + " TEXT NOT NULL, "
                    + AppBase.TrainCache.train_seats + " TEXT NOT NULL, "
                    + AppBase.TrainCache.train_price + " TEXT NOT NULL, "
                    + AppBase.TrainCache.reg + " TEXT NOT NULL, "
                    + AppBase.TrainCache.alldays + " TEXT NOT NULL, "
                    + AppBase.TrainCache.routehref + " TEXT NOT NULL, "
                    + AppBase.TrainCache.seatshref + " TEXT NOT NULL, "
                    + AppBase.TrainCache.depart_num + " TEXT NOT NULL, "
                    + AppBase.TrainCache.arrival_num + " TEXT NOT NULL);");
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    private List<Train> readTrainCache() {
        List<Train> train2 = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        db.beginTransaction();
        firstnotdeparted = 0;
        String[] projection = {
                TrainCache._ID,
                TrainCache.num,
                TrainCache.lines,
                TrainCache.tr_route,
                TrainCache.t_depart,
                TrainCache.t_travel,
                TrainCache.t_arrival,
                TrainCache.train_seats,
                TrainCache.train_price,
                TrainCache.reg,
                TrainCache.alldays,
                TrainCache.routehref,
                TrainCache.seatshref,
                TrainCache.depart_num,
                TrainCache.arrival_num};

        Cursor cursor = db.query(
                TrainCache.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        try {
            int IDColumnIndex = cursor.getColumnIndex(TrainCache._ID);
            int numColumnIndex = cursor.getColumnIndex(TrainCache.num);
            int linesColumnIndex = cursor.getColumnIndex(TrainCache.lines);
            int tr_routeColumnIndex = cursor.getColumnIndex(TrainCache.tr_route);
            int t_departColumnIndex = cursor.getColumnIndex(TrainCache.t_depart);
            int t_travelColumnIndex = cursor.getColumnIndex(TrainCache.t_travel);
            int t_arrivalColumnIndex = cursor.getColumnIndex(TrainCache.t_arrival);
            int train_seatsColumnIndex = cursor.getColumnIndex(TrainCache.train_seats);
            int train_priceColumnIndex = cursor.getColumnIndex(TrainCache.train_price);
            int regColumnIndex = cursor.getColumnIndex(TrainCache.reg);
            int alldaysColumnIndex = cursor.getColumnIndex(TrainCache.alldays);
            int routehrefColumnIndex = cursor.getColumnIndex(TrainCache.routehref);
            int seatshrefColumnIndex = cursor.getColumnIndex(TrainCache.seatshref);
            int depart_numColumnIndex = cursor.getColumnIndex(TrainCache.depart_num);
            int arrival_numColumnIndex = cursor.getColumnIndex(TrainCache.arrival_num);

            while (cursor.moveToNext()) {
                String currentID = cursor.getString(IDColumnIndex); //в отладочных целях
                String currentNum = cursor.getString(numColumnIndex);
                String currentLines = cursor.getString(linesColumnIndex);
                String currentTr_route = cursor.getString(tr_routeColumnIndex);
                String currentT_depart = cursor.getString(t_departColumnIndex);
                String currentT_travel = cursor.getString(t_travelColumnIndex);
                String currentT_arrival = cursor.getString(t_arrivalColumnIndex);
                String currentTrain_seats = cursor.getString(train_seatsColumnIndex);
                String currentTrain_price = cursor.getString(train_priceColumnIndex);
                String currentReg = cursor.getString(regColumnIndex);
                String currentAlldays = cursor.getString(alldaysColumnIndex);
                String currentRoutehref = cursor.getString(routehrefColumnIndex);
                String currentSeatshref = cursor.getString(seatshrefColumnIndex);
                String currentDepart_num = cursor.getString(depart_numColumnIndex);
                String currentArrival_num = cursor.getString(arrival_numColumnIndex);

                train2.add(new Train(currentNum,Byte.valueOf(currentLines),currentTr_route,
                        currentT_depart,currentT_travel,currentT_arrival,
                        unpackArray(currentTrain_seats),unpackArray(currentTrain_price),
                        Boolean.valueOf(currentReg),currentAlldays,
                        currentRoutehref,currentSeatshref,currentDepart_num,currentArrival_num));
                if (currentT_depart.contains("-")) firstnotdeparted = train2.size();
            }
            db.setTransactionSuccessful();
        } finally {
            cursor.close();
            db.endTransaction();
        }
        if (train2.size()!=0) return train2;
        else return null;
    }

    private String packArray (short[] arr) {
        String res = "";
        for (int i=0; i<6; i++) {
            res+=String.valueOf(arr[i])+"|";
        }
        return res;
    }

    private String packArray (float[] arr) {
        String res = "";
        for (int i=0; i<6; i++) {
            res+=String.valueOf(arr[i])+"|";
        }
        return res;
    }

    private float[] unpackArray (String src) {
        float res[] = {0,0,0,0,0,0};
        for (int i=0; i<6; i++) {
            res[i] = Float.valueOf(src.substring(0,src.indexOf("|")));
            src = src.substring(src.indexOf("|")+1,src.length());
        }
        return res;
    }

    public String textRefreshTime() {
        String res;
        long interval = System.currentTimeMillis() - Long.valueOf(getActivityPreferences("refreshed"));
        if (interval<60*1000) {
            res="менее 1 минуты";
        }
        else {
            if (interval < 60 * 60 * 1000) {
                res = String.valueOf((int) interval / (60 * 1000));
                if ((res.length()==2) && (res.startsWith("1"))) res+=" минут";
                else switch (res.substring(res.length()-1,res.length())) {
                    case "1": { res+=" минуту"; break;}
                    case "2": { res+=" минуты"; break;}
                    case "3": { res+=" минуты"; break;}
                    case "4": { res+=" минуты"; break;}
                    default: { res+=" минут"; break;}
                }
            }
            else if (interval<24*60*60*1000) {
                res=String.valueOf((int)interval/(60*60*1000));
                if ((res.length()==2) && (res.startsWith("1"))) res+=" часов";
                else switch (res.substring(res.length()-1,res.length())) {
                    case "1": { res+=" час"; break;}
                    case "2": { res+=" часа"; break;}
                    case "3": { res+=" часа"; break;}
                    case "4": { res+=" часа"; break;}
                    default: { res+=" часов"; break;}
                }
            }
            else {
                res=String.valueOf((int)interval/(24*60*60*1000));
                if ((res.length()==2) && (res.startsWith("1"))) res+=" дней";
                else switch (res.substring(res.length()-1,res.length())) {
                    case "1": { res+=" день"; break;}
                    case "2": { res+=" дня"; break;}
                    case "3": { res+=" дня"; break;}
                    case "4": { res+=" дня"; break;}
                    default: { res+=" дней"; break;}
                }
            }
        }
        return res;
    }

    protected void saveActivityPreferences() {
        SharedPreferences activityPreferences = getPreferences(Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = activityPreferences.edit();
        editor.putString("from_value", getIntent().getStringExtra("from_value"));
        editor.putString("from_exp", getIntent().getStringExtra("from_exp"));
        editor.putString("from_esr", getIntent().getStringExtra("from_esr"));
        editor.putString("to_value", getIntent().getStringExtra("to_value"));
        editor.putString("to_exp", getIntent().getStringExtra("to_exp"));
        editor.putString("to_esr", getIntent().getStringExtra("to_esr"));
        editor.putString("chosen", df2.format(getIntent().getLongExtra("chosen",0)));
        editor.putLong("refreshed", System.currentTimeMillis());
        editor.apply();
    }

    protected String getActivityPreferences(String param) {
        SharedPreferences activityPreferences = getPreferences(Activity.MODE_PRIVATE);
        if (param.equals("from_value")) return activityPreferences.getString(param, "");
        if (param.equals("from_exp")) return activityPreferences.getString(param, "");
        if (param.equals("from_esr")) return activityPreferences.getString(param, "");
        if (param.equals("to_value")) return activityPreferences.getString(param, "");
        if (param.equals("to_exp")) return activityPreferences.getString(param, "");
        if (param.equals("to_esr")) return activityPreferences.getString(param, "");
        if (param.equals("chosen")) return activityPreferences.getString(param, "");
        if (param.equals("refreshed")) return String.valueOf(activityPreferences.getLong(param, 0));
        else return "";
    }

    private Boolean isequalsActivityPreferences() {
        SharedPreferences activityPreferences = getPreferences(Activity.MODE_PRIVATE);
        if (activityPreferences.getString("from_value", "").equals(getIntent().getStringExtra("from_value"))) {
            if (activityPreferences.getString("from_exp", "").equals(getIntent().getStringExtra("from_exp"))) {
                if (activityPreferences.getString("from_esr", "").equals(getIntent().getStringExtra("from_esr"))) {
                    if (activityPreferences.getString("to_value", "").equals(getIntent().getStringExtra("to_value"))) {
                        if (activityPreferences.getString("to_exp", "").equals(getIntent().getStringExtra("to_exp"))) {
                            if (activityPreferences.getString("to_esr", "").equals(getIntent().getStringExtra("to_esr"))) {
                                if (activityPreferences.getString("chosen", "").equals(df2.format(getIntent().getLongExtra("chosen", 0)))) return true;
                                else return false;
                            }
                            else return false;
                        }
                        else return false;
                    }
                    else return false;
                }
                else return false;
            }
            else return false;
        }
        else return false;
    }

    // определение наличия подключения к Интернет
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void initializeData(){
        train = new ArrayList<>();
    }

    private void initializeAdapter(){
        adapter = new TrainRVAdapter(train);
        mrv1.setAdapter(adapter);
    }

    //изменение состояний меню извне, с помощью invalidateOptionsMenu()
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (train.size()!=0) menu.getItem(0).setVisible(true);
        else menu.getItem(0).setVisible(false);
        return true;
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
        if (id == R.id.refresh) {
            mrv1.setVisibility(View.INVISIBLE);
            mprogressbar.setVisibility(View.VISIBLE);
            if (isOnline()) {
                initializeData();
                invalidateOptionsMenu();
                initializeAdapter();
                new getData().execute();
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
        getMenuInflater().inflate(R.menu.train, menu);
        return true;
    }

}
