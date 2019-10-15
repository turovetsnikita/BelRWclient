package com.turovetsnikita.belrwclient.fragments;

/**
 * Created by Nikita on 11.3.17.
 */

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.turovetsnikita.belrwclient.PassengersActivity;
import com.turovetsnikita.belrwclient.adapters.ItemClickSupport;
import com.turovetsnikita.belrwclient.R;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CarFragment extends Fragment{

    RecyclerView mrv2;
    private List<Car> car;
    LinearLayoutManager llm;
    LinearLayout progressbar,warning_cars,error_processing_data,error_timeout;
    CarRVAdapter adapter2;

    String carhref;
    short[] ts;
    DecimalFormat df = new DecimalFormat("00");

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
        warning_cars = (LinearLayout) getView().findViewById(R.id.warning_no_cars);
        error_processing_data = (LinearLayout) getView().findViewById(R.id.error_processing_data);
        error_timeout = (LinearLayout) getView().findViewById(R.id.error_timeout);

        initializeData();
        initializeAdapter();

        ItemClickSupport.addTo(mrv2).setOnItemClickListener(new ItemClickSupport.OnItemClickListener(){
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), PassengersActivity.class);
                intent.putExtra("home", getActivity().getIntent().getStringExtra("url"));
                intent.putExtra("depart_station", getActivity().getIntent().getStringExtra("depart_num"));
                intent.putExtra("arrival_station", getActivity().getIntent().getStringExtra("arrival_num"));
                intent.putExtra("num", getActivity().getIntent().getStringExtra("train_num"));
                intent.putExtra("depart_date", getActivity().getIntent().getStringExtra("date"));
                intent.putExtra("depart_time", getActivity().getIntent().getStringExtra("time"));
                intent.putExtra("car_num", df.format(car.get(position).num));
                intent.putExtra("hash", car.get(position).hash);
                intent.putExtra("class", car.get(position).typeabbr);
                startActivity(intent);
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
        warning_cars.setVisibility(View.GONE);
        error_processing_data.setVisibility(View.GONE);
        error_timeout.setVisibility(View.GONE);

        carhref = getActivity().getIntent().getStringExtra("carhref");


        if (!carhref.equals("")) {
            ts = getActivity().getIntent().getShortArrayExtra("seats");
            new getCars().execute();
        }

        if (carhref.equals("")) { //если смотрим вагоны электричек-дизелей или все 6 типов сразу
            warning_cars.setVisibility(View.VISIBLE);
            mrv2.setVisibility(View.VISIBLE);
            progressbar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private class getCars extends AsyncTask<String, Integer, Document[]> {
        @Override
        protected Document[] doInBackground(String... arg) {
            int j=0,classes=0;
            for (int i = 0; i < 6; i++)
                if (ts[i] != 0) classes++;
            Document[] doc = new Document[classes];
            for (int i = 0; i < 6; i++) {
                if (ts[i] != 0) {
                    try {
                        doc[j] = Jsoup.connect(carhref + String.valueOf(i+1))
                                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                                .post();
                    } catch (IOException e) {
                        return null;
                    }
                    j++;
                }
            }

            return doc;
        }

        @Override
        protected void onPostExecute(Document[] result) {
            if (result!=null) {
                initializeData();
                for (int i = 0; i < result.length; i++)
                    grabCarriage(result[i].select("body").toString());
                if (car!=null) {
                    mrv2.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.INVISIBLE);
                    initializeAdapter();
                    adapter2.notifyItemRangeInserted(0, car.size());
                }
                else {
                    error_processing_data.setVisibility(View.VISIBLE);
                }
            }
            else {
                error_timeout.setVisibility(View.VISIBLE);
            }
            mrv2.setVisibility(View.VISIBLE);
            progressbar.setVisibility(View.INVISIBLE);
        }
    }

    //непосредственно граббинг вагонов
    public void grabCarriage(String cars) {
        cars = StringEscapeUtils.unescapeJava(cars); // "расшифровка" unicode
        List<Car> res= new ArrayList<>();
        String type,typeabbr,typeabbrpost,sign,places,hash;
        float price;
        short tplaces, num;
        byte lplaces,uplaces,lsplaces,usplaces;
        do {
            do {
                cars = cutToParam(cars, "price"); //убираем лишнее
                type = getParam(cars, "type");
                typeabbr = getParam(cars, "typeAbbr");
                typeabbrpost = getParam(cars, "typeAbbrPostfix");
                sign = getParam(cars, "sign");
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
                    hash = getParam(cars, "hash");
                    cars = cutToParam(cars, "noSmoking");  //убираем лишнее
                    res.add(new Car(num, type,typeabbr,typeabbrpost, sign, price, tplaces, lplaces, uplaces, lsplaces, usplaces, places, hash));
                } while ((getParamPos(cars,"number")<getParamPos(cars,"price")) ||
                        ((getParamPos(cars,"number")==-1) ^ (getParamPos(cars,"price")==-1)));
            } while (getParamPos(cars,"price")!=-1);
            cars="";
        } while (cars.length()>0);
        for (int i = 0; i < res.size(); i++)
            car.add(res.get(i));
    }

    public String getParam (String source, String param) { //string, byte, boolean
        source = cutToParam(source,param);
        param = "\"" + param + "\"";
        if (source.contains(param)) {
            if ((source.contains(",\"")) && (source.contains("}")))
            {
                if ((source.indexOf(",\""))<(source.indexOf("}"))) {
                    source = source.substring(source.indexOf(param) + param.length() + 1, source.indexOf(",\""));
                    source = source.replace("\"", "");
                    source = source.replace(",", ".");
                }
                else
                {
                    source = source.substring(source.indexOf(param) + param.length()+1, source.indexOf("}"));
                    source = source.replace("}", "");
                    source = source.replace(",", ".");
                }
            }
            else
            {
                if ((source.indexOf(",\""))>(source.indexOf("}"))) {
                    source = source.substring(source.indexOf(param) + param.length() + 1, source.indexOf(",\""));
                    source = source.replace("\"", "");
                    source = source.replace(",", ".");
                }
                else
                {
                    source = source.substring(source.indexOf(param) + param.length()+1, source.indexOf("}"));
                    source = source.replace("}", "");
                    source = source.replace(",", ".");
                }
            }


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
        buf = source;
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