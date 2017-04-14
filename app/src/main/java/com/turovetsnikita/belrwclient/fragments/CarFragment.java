package com.turovetsnikita.belrwclient.fragments;

/**
 * Created by Nikita on 11.3.17.
 */

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.turovetsnikita.belrwclient.adapters.ItemClickSupport;
import com.turovetsnikita.belrwclient.R;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CarFragment extends Fragment{

    RecyclerView mrv2;
    private List<Car> car;
    LinearLayoutManager llm;
    LinearLayout progressbar;
    CarRVAdapter adapter2;

    String carhref;
    String carhrefpost = "";

    short[] ts;

    public CarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.car_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mrv2 = (RecyclerView) getView().findViewById(R.id.rv2);
        mrv2.setHasFixedSize(true);
        llm = new LinearLayoutManager(getContext());
        mrv2.setLayoutManager(llm);

        progressbar = (LinearLayout) getView().findViewById(R.id.progressBar);

        initializeData();
        initializeAdapter();

        ItemClickSupport.addTo(mrv2).setOnItemClickListener(new ItemClickSupport.OnItemClickListener(){
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                //TODO: expandable recyclerview
                //TODO: Chrome -> F12 -> Network -> PaymentRedirect -> Headers -> Form Data (та самая повторная отправка формы)
            }
        });
        ItemClickSupport.addTo(mrv2).setOnItemLongClickListener (new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                return false;
            }
        });

        mrv2.setVisibility(View.INVISIBLE);
        progressbar.setVisibility(View.VISIBLE);

        carhref = getActivity().getIntent().getStringExtra("carhref");

        if (!carhref.equals("")) {
            ts = getActivity().getIntent().getShortArrayExtra("seats");

            for (int i = 0; i < 6; i++) {
                if (ts[i] == 0) {
                    carhrefpost = String.valueOf(i + 1);
                    new getCars().execute();
                    break;
                }
            }
        }
        if (carhref.equals("")) { //если смотрим вагоны электричек-дизелей или все 6 типов сразу
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.text_error)
                    .setMessage(R.string.text_error_cars)
                    .setCancelable(false)
                    .setPositiveButton(R.string.text_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
            mrv2.setVisibility(View.VISIBLE);
            progressbar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private class getCars extends AsyncTask<String, Integer, Document> {
        @Override
        protected Document doInBackground(String... arg) {
            Document doc;
            try {
                doc = Jsoup.connect(carhref + carhrefpost)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .post();
            }
            catch (IOException e) {
                return null;
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document result) {
            if (result!=null) {
                grabCarriage(result.select("body").toString());
                if (car!=null) {
                    mrv2.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.INVISIBLE);
                    initializeAdapter();
                    adapter2.notifyItemRangeInserted(0, car.size());
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.text_error)
                            .setMessage(R.string.text_error_processing_data)
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
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.text_error)
                        .setMessage(R.string.text_no_internet)
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
            mrv2.setVisibility(View.VISIBLE);
            progressbar.setVisibility(View.INVISIBLE);
        }
    }

    //непосредственно граббинг вагонов
    public void grabCarriage(String cars) {
        cars = StringEscapeUtils.unescapeJava(cars); // "расшифровка" unicode

        List<Car> res= new ArrayList<>();

        String descr,type;
        float price;
        short tplaces;
        short num;
        byte lplaces,uplaces,lsplaces,usplaces;
        String places;

        do {
            do {
                cars = cutToParam(cars, "price"); //убираем лишнее
                type = getParam(cars, "typeAbbr");
                descr = getParam(cars, "type");
                price = Float.parseFloat(getParam(cars, "price_byn").replace(" ",""));
                do {
                    cars = cutToParam(cars, "number");  //убираем лишнее
                    num = Short.parseShort(getParam(cars, "number"));
                    tplaces=-1; lplaces=-1; uplaces=-1; lsplaces=-1; usplaces=-1;

                    if (!getParam(cars, "totalPlaces").equals("")) tplaces = Byte.parseByte(getParam(cars, "totalPlaces"));
                    if (!getParam(cars, "lowerPlaces").equals("")) {
                        lplaces = Byte.parseByte(getParam(cars, "lowerPlaces"));
                        uplaces = Byte.parseByte(getParam(cars, "upperPlaces"));
                    if (!getParam(cars, "lowerSidePlaces").equals("")) {
                        lsplaces = Byte.parseByte(getParam(cars, "lowerSidePlaces"));
                        usplaces = Byte.parseByte(getParam(cars, "upperSidePlaces"));
                    }
                    }
                    places = getArr(cars);
                    cars = cutToParam(cars, "hideLegend");  //убираем лишнее
                    res.add(new Car(num, descr, type, price, tplaces, lplaces, uplaces, lsplaces, usplaces, places));
                } while ((getParamPos(cars,"number")<getParamPos(cars,"price")) ||
                        ((getParamPos(cars,"number")==-1) ^ (getParamPos(cars,"price")==-1)));
            } while (getParamPos(cars,"price")!=-1);
            cars="";
        } while (cars.length()>0);

        for (int i = 0; i < res.size(); i++)
            car.add(0,res.get(i));
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

    public String getArr (String source) {
        String param = "emptyPlaces", buf, res = "", divider = ", ";
        source = cutToParam(source,param);
        param = "\"" + param + "\"";
        source = source.substring(source.indexOf(param)+param.length()+1, source.indexOf("\"]")+2);
        do {
            param = "\"";
            if (source.contains("\",\"")) {
                buf = source.substring(source.indexOf(param)+param.length(), source.indexOf("\",\""));
                param = "\",";
                source = source.substring(source.indexOf(param)+param.length(), source.length());
            }
            else {
                buf = source.substring(source.indexOf(param)+param.length(), source.indexOf("\"]"));
                source = "";
            }
            res+=buf+divider;
        } while (source.indexOf("]")>0);
        res = res.substring(0,res.length()-divider.length());
        return res;
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
        buf = source/*.substring(source.indexOf(param), source.length())*/;
        if (buf.contains(param)) {
            buf = buf.substring(buf.indexOf(param), buf.length());
        }
        else buf = "";
        return buf;
    }

    private void initializeData(){
        car = new ArrayList<>();
    }

    private void initializeAdapter(){
        adapter2 = new CarRVAdapter(car);
        mrv2.setAdapter(adapter2);
    }
}