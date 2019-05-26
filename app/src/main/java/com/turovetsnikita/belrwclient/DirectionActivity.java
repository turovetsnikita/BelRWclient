package com.turovetsnikita.belrwclient;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.turovetsnikita.belrwclient.adapters.ItemClickSupport;
import com.turovetsnikita.belrwclient.data.AppBase;
import com.turovetsnikita.belrwclient.data.AppBase.RecentDirections;
import com.turovetsnikita.belrwclient.data.AppDbHelper;


//TODO: min API operability checked - 19 (4.4)
//TODO: не проработаны ладшафтные ориентации
//TODO: избавитьсся от AlertDialog
//TODO: уведомления об отправлении/скором прибытии

public class DirectionActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    AutoCompleteTextView mTextView, mTextView2;
    TextInputLayout mTextViewLayout, mTextView2Layout;
    ProgressBar pb1,pb2;
    EditText mDate;
    AppCompatButton search_button,swap_button;
    AppCompatImageButton show_cached;
    RecyclerView mrv1;
    ArrayList<Recent> recent = new ArrayList<>(15);
    LinearLayoutManager llm;
    RecentRVAdapter adapter;
    Locale russian = new Locale("ru");
    Calendar date = Calendar.getInstance();
    SimpleDateFormat df1 = new SimpleDateFormat("EE, dd MMM", russian);

    String searching,from_exp,from_esr,to_exp,to_esr;
    Date mx; //выбранная дата
    long st,en;
    private CoordinatorLayout coordLayout;
    Context context = DirectionActivity.this;
    private static long back_pressed;
    int LENGTH_VERY_LONG = 5000;
    SharedPreferences sp;

    boolean isfirstTextView;
    private AppDbHelper mDbHelper;
    private List<Stations> mStations;
    private List<String> mList;
    private ArrayAdapter<String> mAutoCompleteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_view_activity);
        coordLayout = (CoordinatorLayout) findViewById(R.id.coordLayout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_direction_activity);
        setSupportActionBar(toolbar); //setTitle строго до этой команды

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                HideKeybClearFocus();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        mTextView = (AutoCompleteTextView) findViewById(R.id.AutoCompleteTextView);
        mTextView2 = (AutoCompleteTextView) findViewById(R.id.AutoCompleteTextView2);
        mTextViewLayout = (TextInputLayout) findViewById(R.id.textInputLayout);
        mTextView2Layout = (TextInputLayout) findViewById(R.id.textInputLayout2);
        pb1 = (ProgressBar) findViewById(R.id.progress_bar_1);
        pb2 = (ProgressBar) findViewById(R.id.progress_bar_2);
        mDate = (EditText) findViewById(R.id.editText);
        search_button = (AppCompatButton) findViewById(R.id.search);
        swap_button = (AppCompatButton) findViewById(R.id.swap);
        show_cached = (AppCompatImageButton) findViewById(R.id.show_cached);
        mDate.setText("сегодня");
        mrv1 = (RecyclerView)findViewById(R.id.rv1);
        mrv1.setHasFixedSize(true);
        llm = new LinearLayoutManager(context);
        mrv1.setLayoutManager(llm);

        ItemClickSupport.addTo(mrv1).setOnItemClickListener(new ItemClickSupport.OnItemClickListener(){
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                HideKeybClearFocus();
                mTextView.setError(null);
                mTextView2.setError(null);
/*
                if ((!mTextView.getText().toString().equals(recent.get(position).depart)) && (!mTextView2.getText().toString().equals(recent.get(position).arrival)))
                    startAnimation(true,true);
                else {
                    if (!mTextView.getText().toString().equals(recent.get(position).depart))
                        startAnimation(true, false);
                    if (!mTextView2.getText().toString().equals(recent.get(position).arrival))
                        startAnimation(false, true);
                }
*/

                mTextView.removeTextChangedListener(null);
                mTextView2.removeTextChangedListener(null);

                mTextView.setText(recent.get(position).from_value);
                from_exp= recent.get(position).from_exp;
                from_esr= recent.get(position).from_esr;
                mTextView2.setText(recent.get(position).to_value);
                to_exp= recent.get(position).to_exp;
                to_esr= recent.get(position).to_esr;


                mTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        pb1.setVisibility(View.GONE);
                        if (s.length()>2) {
                            searching=String.valueOf(s);
                            isfirstTextView=true;
                            pb1.setVisibility(View.VISIBLE);
                            new getStations().execute();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                mTextView2.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        pb2.setVisibility(View.GONE);
                        if (s.length()>2) {
                            searching=String.valueOf(s);
                            isfirstTextView=false;
                            pb2.setVisibility(View.VISIBLE);
                            new getStations().execute();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
            }
        });
        ItemClickSupport.addTo(mrv1).setOnItemLongClickListener (new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                HideKeybClearFocus();
                recent.remove(position);
                deleteAllHistory();
                insertHistory(recent);
                initializeAdapter();
                adapter.notifyItemRemoved(position);
                return true;
            }
        });
        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(v);
            }
        });
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(v);
            }
        });
        swap_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swap(v);
            }
        });
        show_cached.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StationsActivity.class);
                startActivity(intent);
            }
        });

        mDbHelper = new AppDbHelper(this);

        initializeData();
        fromBasetoArray();
        initializeAdapter();

        mTextView.setSelectAllOnFocus(true);
        mTextView2.setSelectAllOnFocus(true);

        prepareList();
        mAutoCompleteAdapter = new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line, mList);

        mTextView.setAdapter(mAutoCompleteAdapter);
        mTextView2.setAdapter(mAutoCompleteAdapter);

        mTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pb1.setVisibility(View.GONE);
                if (s.length()>2) {
                    searching=String.valueOf(s);
                    isfirstTextView=true;
                    pb1.setVisibility(View.VISIBLE);
                    new getStations().execute();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mTextView2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pb2.setVisibility(View.GONE);
                if (s.length()>2) {
                    searching=String.valueOf(s);
                    isfirstTextView=false;
                    pb2.setVisibility(View.VISIBLE);
                    new getStations().execute();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mTextView.setOnItemClickListener (new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView , View view , int position ,long arg3)
            {
                mTextView.removeTextChangedListener(null);
                mTextView2.removeTextChangedListener(null);

                mTextView.setText(mStations.get(position).value);
                from_exp= mStations.get(position).exp;
                from_esr= mStations.get(position).esr;
                HideKeybClearFocus();

                mTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        pb1.setVisibility(View.GONE);
                        if (s.length()>2) {
                            searching=String.valueOf(s);
                            isfirstTextView=true;
                            pb1.setVisibility(View.VISIBLE);
                            new getStations().execute();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                mTextView2.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        pb2.setVisibility(View.GONE);
                        if (s.length()>2) {
                            searching=String.valueOf(s);
                            isfirstTextView=false;
                            pb2.setVisibility(View.VISIBLE);
                            new getStations().execute();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
            }
        });

        mTextView2.setOnItemClickListener (new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView , View view , int position ,long arg3)
            {
                mTextView2.setText(mStations.get(position).value);
                to_exp= mStations.get(position).exp;
                to_esr= mStations.get(position).esr;
                HideKeybClearFocus();
            }
        });

        mTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mTextView.removeTextChangedListener(null);
                    mTextView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            pb1.setVisibility(View.GONE);
                            if (s.length()>2) {
                                searching=String.valueOf(s);
                                isfirstTextView=true;
                                pb1.setVisibility(View.VISIBLE);
                                new getStations().execute();
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });
                }
            }
        });
        mTextView2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mTextView2.removeTextChangedListener(null);
                    mTextView2.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            pb2.setVisibility(View.GONE);
                            if (s.length()>2) {
                                searching=String.valueOf(s);
                                isfirstTextView=false;
                                pb2.setVisibility(View.VISIBLE);
                                new getStations().execute();
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });
                }
            }
        });

        sp = PreferenceManager.getDefaultSharedPreferences(context);
        new checkNotification().execute();
    }

    //нажатие кнопки Поиск
    public void search(View v) {
        HideKeybClearFocus();
        mTextView.setError(null);
        mTextView2.setError(null);

        if (mTextView.getText().toString().isEmpty()) {
            mTextView.setError("");
            Snackbar.make(coordLayout, R.string.text_enter_depart_station, LENGTH_VERY_LONG).show();
        }
        else
        if (mTextView2.getText().toString().isEmpty()) {
            mTextView2.setError("");
            Snackbar.make(coordLayout, R.string.text_enter_arrival_station, LENGTH_VERY_LONG).show();

        }
        else {
            if (mDate.getText().toString().equals("сегодня"))
                mx = new Date(System.currentTimeMillis());
            if (mDate.getText().toString().equals("завтра"))
                mx = new Date(System.currentTimeMillis()+86400000L); //+1day
            if (mDate.getText().toString().equals("на все дни"))
                mx = new Date(1);

            //TODO: отдельный диалог для ввода
            int pos = inHistoryNum();
            if (pos==-1)
            {
                recent.add(0, new Recent(mTextView.getText().toString(),
                        from_exp,
                        from_esr,
                        mTextView2.getText().toString(),
                        to_exp,
                        to_esr));
                deleteAllHistory();
                insertHistory(recent);
                initializeAdapter();
                adapter.notifyItemInserted(0);
                mrv1.smoothScrollToPosition(0);
            }
            else if (pos!=0) {
                recent.remove(pos);
                recent.add(0, new Recent(mTextView.getText().toString(),
                        from_exp,
                        from_esr,
                        mTextView2.getText().toString(),
                        to_exp,
                        to_esr));
                deleteAllHistory();
                insertHistory(recent);
                initializeAdapter();
                adapter.notifyItemMoved(pos,0);
            }

            Intent intent = new Intent(context, TrainActivity.class);
            intent.putExtra("from_value", mTextView.getText().toString());
            intent.putExtra("from_exp", from_exp);
            intent.putExtra("from_esr", from_esr);
            intent.putExtra("to_value", mTextView2.getText().toString());
            intent.putExtra("to_exp", to_exp);
            intent.putExtra("to_esr", to_esr);
            intent.putExtra("chosen", mx.getTime());
            startActivity(intent); //передаем управление активити с результатами поиска
        }
    }

    public void swap(View v) {
        HideKeybClearFocus();
        if ((!mTextView.getText().toString().equals("")) || (!mTextView2.getText().toString().equals(""))) {
            String value_buf = mTextView.getText().toString();
            mTextView.setText(mTextView2.getText().toString());
            mTextView2.setText(value_buf);

            String exp_buf = from_exp;
            from_exp = to_exp;
            to_exp = exp_buf;

            String esr_buf = from_esr;
            from_esr = to_esr;
            to_esr = esr_buf;

            startAnimation(true, true);
        }

        final Animation animationRotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        swap_button.startAnimation(animationRotate);
    }

    private void prepareList() {
        // подготовим список для автозаполнения
        mList = new ArrayList<>();
    }

    //подсказки в autocompletetextiew
    private class getStations extends AsyncTask<String,Integer,Document> {
        @Override
        protected Document doInBackground(String... arg) {
            Document doc;

            try {
                doc = Jsoup.connect("http://rasp.rw.by/ru/ajax/autocomplete/search/?term="+searching)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .post();
            }
            catch (Exception e) {
                return null;
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document result) {
            if (result!=null) {
                prepareList();
                grabStations(result.select("body").toString());

                if (mStations!=null) {
                    for (int i=0;i<mStations.size();i++)
                        mList.add(mStations.get(i).label);

                    mAutoCompleteAdapter = new ArrayAdapter<>(context,android.R.layout.simple_dropdown_item_1line, mList);
                    if (isfirstTextView) {
                        mTextView.setAdapter(mAutoCompleteAdapter);
                        pb1.setVisibility(View.GONE);
                    }
                    else {
                        mTextView2.setAdapter(mAutoCompleteAdapter);
                        pb2.setVisibility(View.GONE);
                    }
                }
                else {
                    Toast.makeText(context,"error_processing_data", Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(context,"error_timeout", Toast.LENGTH_LONG).show();
            }

            if (isfirstTextView) {
                pb1.setVisibility(View.GONE);
            }
            else {
                pb2.setVisibility(View.GONE);
            }

        }
    }

    //проверка уведомлений о перерывах в работе системы покупки билетов
    private class checkNotification extends AsyncTask<String,Integer,Document> {
        @Override
        protected Document doInBackground(String... arg) {
            Document doc;
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
                    doc = Jsoup.connect("http://poezd.rw.by/wps/portal/home/rp")
                            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                            .post();
            }
            catch (Exception e) {
                return null;
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document result) {
            if (result!=null) {
                Element elem = result.select("div.bzd-left-column-block").first();
                if (elem.select("h3").text().contains("Уважаемые")) {
                    final String msg = elem.text();
                    if (!msg.equals(getActivityPreferences("notif_text"))) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("ВАЖНО")
                                .setMessage(msg)
                                .setCancelable(false)
                                .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        })
                                .setNegativeButton("Не напоминать",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                saveActivityPreferences(msg);
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
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


    //непосредственно граббинг станций
    public void grabStations(String stations) {
        stations = StringEscapeUtils.unescapeJava(stations); // "расшифровка" unicode

        mStations = new ArrayList<>();
        List<Stations> res= new ArrayList<>();

        String label,value,exp,esr;

        do {
            do {
                stations = cutToParam(stations, "label"); //убираем лишнее
                label = getParam(stations, "label");
                value = getParam(stations, "value");
                exp = getParam(stations, "exp");
                esr = getParam(stations, "ecp");
                stations = cutToParam(stations, "otd");  //убираем лишнее
                res.add(new Stations(label,value,exp,esr));
            } while (getParamPos(stations,"label")!=-1);
            stations="";
        } while (stations.length()>0);

        for (int i = 0; i < res.size(); i++)
            mStations.add(res.get(i));
    }

    public String getParam (String source, String param) { //string, byte, boolean
        source = cutToParam(source,param);
        param = "\"" + param + "\"";
        if (source.contains(param)) {
            source = source.substring(source.indexOf(param)+param.length()+1, source.indexOf(",\""));
            source = source.replace("\"","");
            source = source.replace(",",".");
        }
        else source = "";
        return source;
    }

    public Integer getParamPos (String source, String param) {
        param = "\"" + param + "\"";
        Integer res;
        if (source.contains(param)) res = source.indexOf(param);
        else res = -1;
        return res;
    }

    public String cutToParam(String source, String param) {
        param = "\"" + param + "\"";
        String buf;
        buf = source;
        if (buf.contains(param)) {
            buf = buf.substring(buf.indexOf(param), buf.length());
        }
        else buf = "";
        return buf;
    }

    private void insertHistory(List<Recent> ins) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < ins.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(RecentDirections.from_value, ins.get(i).from_value);
                values.put(RecentDirections.from_exp, ins.get(i).from_exp);
                values.put(RecentDirections.from_esr, ins.get(i).from_esr);
                values.put(RecentDirections.to_value, ins.get(i).to_value);
                values.put(RecentDirections.to_exp, ins.get(i).to_exp);
                values.put(RecentDirections.to_esr, ins.get(i).to_esr);
                long newRowId = db.insert(RecentDirections.TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    private void deleteAllHistory () {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DROP TABLE IF EXISTS " + RecentDirections.TABLE_NAME);
            db.execSQL("CREATE TABLE " + AppBase.RecentDirections.TABLE_NAME + " ("
                    + AppBase.RecentDirections._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + AppBase.RecentDirections.from_value + " TEXT NOT NULL, "
                    + AppBase.RecentDirections.from_exp + " TEXT NOT NULL, "
                    + AppBase.RecentDirections.from_esr + " TEXT NOT NULL, "
                    + AppBase.RecentDirections.to_value + " TEXT NOT NULL, "
                    + AppBase.RecentDirections.to_exp + " TEXT NOT NULL, "
                    + AppBase.RecentDirections.to_esr + " TEXT NOT NULL);");
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    private void fromBasetoArray() {
        // Создадим и откроем для чтения базу данных
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        db.beginTransaction();
        // Зададим условие для выборки - список столбцов
        String[] projection = {
                RecentDirections._ID,
                RecentDirections.from_value,
                RecentDirections.from_exp,
                RecentDirections.from_esr,
                RecentDirections.to_value,
                RecentDirections.to_exp,
                RecentDirections.to_esr};

        // Делаем запрос
        Cursor cursor = db.query(
                RecentDirections.TABLE_NAME,   // таблица
                projection,            // столбцы
                null,                  // столбцы для условия WHERE
                null,                  // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // порядок сортировки

        try {
            // Узнаем индекс каждого столбца
            int IDColumnIndex = cursor.getColumnIndex(RecentDirections._ID);
            int from_valueColumnIndex = cursor.getColumnIndex(RecentDirections.from_value);
            int from_expColumnIndex = cursor.getColumnIndex(RecentDirections.from_exp);
            int from_esrColumnIndex = cursor.getColumnIndex(RecentDirections.from_esr);
            int to_valueColumnIndex = cursor.getColumnIndex(RecentDirections.to_value);
            int to_expColumnIndex = cursor.getColumnIndex(RecentDirections.to_exp);
            int to_esrColumnIndex = cursor.getColumnIndex(RecentDirections.to_esr);

            int i=0;
            // Проходим через все ряды
            while (cursor.moveToNext()) {
                // Используем индекс для получения строки или числа
                String currentID = cursor.getString(IDColumnIndex); //в отладочных целях
                String currentFrom_value = cursor.getString(from_valueColumnIndex);
                String currentFrom_exp = cursor.getString(from_expColumnIndex);
                String currentFrom_esr = cursor.getString(from_esrColumnIndex);
                String currentTo_value = cursor.getString(to_valueColumnIndex);
                String currentTo_exp = cursor.getString(to_expColumnIndex);
                String currentTo_esr = cursor.getString(to_esrColumnIndex);
                // Выводим значения каждого столбца
                recent.add(i, new Recent(currentFrom_value,currentFrom_exp,currentFrom_esr,
                        currentTo_value,currentTo_exp,currentTo_esr));
                i++;
            }
            db.setTransactionSuccessful();
        } finally {
            // Всегда закрываем курсор после чтения
            cursor.close();
            db.endTransaction();
        }
    }

    private void initializeData(){
        recent = new ArrayList<>(15);
    }

    private void initializeAdapter(){
        adapter = new RecentRVAdapter(recent);
        mrv1.setAdapter(adapter);
    }

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(final DatePicker view, int year, int month, int day) {
            date.set(Calendar.YEAR, year);
            date.set(Calendar.MONTH, month);
            date.set(Calendar.DAY_OF_MONTH, day);
            long chosen = date.getTimeInMillis();
            st = System.currentTimeMillis();
            if (chosen<st) {
                mDate.setText("сегодня");
            }
			else {
				if (chosen<st+86402000L) { //1day +2sec
                mDate.setText("завтра");
				}
				else {
					if (chosen>en) {
						mx=new Date(en);
						mDate.setText(df1.format(mx));
						Snackbar.make(coordLayout, R.string.text_set_max_date,Snackbar.LENGTH_LONG).show();
					}
					else {
						mx=new Date(date.getTimeInMillis());
						mDate.setText(df1.format(mx));
					}
				}
			}
        }
    };

    // отображаем диалоговое окно для выбора даты
    public void setDate(View v) {
        HideKeybClearFocus();
        final DatePickerDialog datePickerDialog=new DatePickerDialog(context,dateSetListener,date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
        if (mDate.getText().toString().equals("сегодня")) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            datePickerDialog.updateDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH ));

            datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.datePicker_negativeButton_tomorrow), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                        dialog.cancel();
                        mDate.setText("завтра");
                    }
                }
            });
        }
        else if (mDate.getText().toString().equals("завтра")) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis() + 86400000L); //+1day
            datePickerDialog.updateDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH ));

            datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.datePicker_negativeButton_today), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                        dialog.cancel();
                        mDate.setText("сегодня");
                    }
                }
            });
        }
        else {
            datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.datePicker_negativeButton_today), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                        dialog.cancel();
                        mDate.setText("сегодня");
                    }
                }
            });
        }
        /*datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.datePicker_positiveButton), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {

                }
            }
        });*/
        datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.datePicker_neutralButton), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEUTRAL) {
                    dialog.cancel();
                    mDate.setText("на все дни");
                }
            }
        });
        datePickerDialog.setTitle(null);
        st = System.currentTimeMillis()-1000;
        datePickerDialog.getDatePicker().setMinDate(st);
        en = st+5184000000L; //+60day
        datePickerDialog.getDatePicker().setMaxDate(en);
        datePickerDialog.show();
    }

    // очистка фокуса (перевод на layout), скрытие клавиатуры
    private void HideKeybClearFocus( ){
        mTextView.clearFocus();
        mTextView2.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    // определение наличия подключения к Интернет
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    protected void saveActivityPreferences(String text) {
        SharedPreferences activityPreferences = getPreferences(Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = activityPreferences.edit();
        editor.putString("notif_text", text);
        editor.apply();
    }

    protected String getActivityPreferences(String param) {
        SharedPreferences activityPreferences = getPreferences(Activity.MODE_PRIVATE);
        if (param.equals("notif_text")) return activityPreferences.getString(param, "");
        else return "";
    }

    // определение наличия/позиции запрашиваемого маршрута в истории
    private int inHistoryNum(){
        for (int i = 0; i< recent.size(); i++)
            if ((mTextView.getText().toString().equals(recent.get(i).from_value))
                    && (from_exp.equals(recent.get(i).from_exp))
                    && (from_esr.equals(recent.get(i).from_esr))
                    && (mTextView2.getText().toString().equals(recent.get(i).to_value))
                    && (to_exp.equals(recent.get(i).to_exp))
                    && (to_esr.equals(recent.get(i).to_esr)))
                return i;
        return -1;
    }

    // прятать боковую панель при нажатии "Назад" + выход по двойному нажатию
    @Override
    public void onBackPressed() {
        HideKeybClearFocus();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (back_pressed + 1000 > System.currentTimeMillis())
                super.onBackPressed();
            else
                Snackbar.make(coordLayout,"Нажмите еще раз для выхода",Snackbar.LENGTH_LONG).show();
                back_pressed = System.currentTimeMillis();
        }
    }

    // анимация изменений в полях ввода
    private void startAnimation(boolean arrival, boolean depart) {
        int duration = 200;
        if (arrival && depart) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(mTextView, "textColor",
                    Color.WHITE, ContextCompat.getColor(context, R.color.colorBlack)).setDuration(duration);
            objectAnimator.setEvaluator(new ArgbEvaluator());
            objectAnimator.start();

            ObjectAnimator objectAnimator2 = ObjectAnimator.ofInt(mTextView2, "textColor",
                    Color.WHITE, Color.WHITE, ContextCompat.getColor(context, R.color.colorBlack)).setDuration(duration*2);
            objectAnimator2.setEvaluator(new ArgbEvaluator());
            objectAnimator2.start();
        }
        else {
            ObjectAnimator objectAnimator;
            if (arrival)
                objectAnimator = ObjectAnimator.ofInt(mTextView, "textColor",
                        Color.WHITE, ContextCompat.getColor(context, R.color.colorBlack)).setDuration(duration);

            else
                objectAnimator = ObjectAnimator.ofInt(mTextView2, "textColor",
                        Color.WHITE, ContextCompat.getColor(context, R.color.colorBlack)).setDuration(duration);

            objectAnimator.setEvaluator(new ArgbEvaluator());
            objectAnimator.start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.direction, menu);
        return true;
    }

	// обработчики кнопок тулбара
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up search_button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

	//обработчики кнопок боковой панели
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.online_scoreboard) { //Онлайн-табло
            Intent intent = new Intent(context, ScoreboardActivity.class);
            startActivity(intent);
            Toast.makeText(context,"Сервис \"Онлайн-табло\" функционирует в режиме опытной эксплуатации", Toast.LENGTH_LONG).show();
        } else if (id == R.id.account) { //Личный кабинет (список пассажиров + предстоящие/совершенные поездки)
            Snackbar.make(coordLayout,"Отображение предстоящих/совершенных поездок, регистрационные данные",Snackbar.LENGTH_LONG).show();
        } else if (id == R.id.data_usage) { // Настройки -> Использование данных
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    "com.android.settings",
                    "com.android.settings.Settings$DataUsageSummaryActivity"));
            startActivity(intent);
            Toast.makeText(context,"Найдите в списке пункт \"БЖД Билеты\"", Toast.LENGTH_LONG).show();
        } else if (id == R.id.app_settings) { //Настройки программы
            Intent intent = new Intent(context, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.app_help) { //Справка
            Snackbar.make(coordLayout,"Отображение справки)",Snackbar.LENGTH_LONG).show();
        } else if (id == R.id.about) { //О программе
            Snackbar.make(coordLayout,"О программе",Snackbar.LENGTH_LONG).show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}
