package com.turovetsnikita.belrwclient.fragments;

/**
 * Created by Nikita on 11.3.17.
 */

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.turovetsnikita.belrwclient.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RouteFragment extends Fragment {
//TODO: добавить "Отслеживание" + уведомления? + пункт в боковой панели? + кеш маршрута (автоматом при покупке билета) + Animator
//TODO: учесть прицепные вагоныы

    RecyclerView mrv1;
    private List<Route> route;
    LinearLayoutManager llm;
    LinearLayout progressbar,error_processing_data,error_timeout;
    RouteRVAdapter adapter1;
    String routehref;
    int visitposition;

    public RouteFragment() {
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
        //return super.onCreateView(inflater, container, savedInstanceState);

        View rootView =
                inflater.inflate(R.layout.route_fragment, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) { //TODO: выполняется СНОВА при пролистывании на 3 вкладку (см. ЖЦ Fragment)
        super.onActivityCreated(savedInstanceState);

        mrv1 = (RecyclerView) getView().findViewById(R.id.rv1);
        mrv1.setHasFixedSize(true);
        llm = new LinearLayoutManager(getContext());
        mrv1.setLayoutManager(llm);

        progressbar = (LinearLayout) getView().findViewById(R.id.progressBar);
        error_processing_data = (LinearLayout) getView().findViewById(R.id.error_processing_data);
        error_timeout = (LinearLayout) getView().findViewById(R.id.error_timeout);

        initializeData();
        initializeAdapter();

        progressbar.setVisibility(View.VISIBLE);
        mrv1.setVisibility(View.GONE);
        error_processing_data.setVisibility(View.GONE);
        error_timeout.setVisibility(View.GONE);

        routehref = getActivity().getIntent().getStringExtra("routehref");

        new getRoute().execute();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private class getRoute extends AsyncTask<String, Integer, Document> {
        @Override
        protected Document doInBackground(String... arg) {
            Document doc;
            try {
                doc = Jsoup.connect("http://rasp.rw.by"+routehref)
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
                parseRoute(result.select("div.tabs_item.shown"));
                if (route!=null) {
                    initializeAdapter();
                    adapter1.notifyItemRangeInserted(0, route.size());
                    llm.scrollToPosition(visitposition);
                }
                else {
                    error_processing_data.setVisibility(View.VISIBLE);
                }
            }
            else { //html не получен (==null)
                error_timeout.setVisibility(View.VISIBLE);
            }
            mrv1.setVisibility(View.VISIBLE);
            progressbar.setVisibility(View.GONE);
        }
    }

    // непосредственно парсинг маршрута
    public void parseRoute(Elements html) {
        String st,en,buf = routehref;

        buf = buf.replace(buf.substring(0,buf.indexOf("from=")+5),""); //удаляем ненужное
        st = buf.substring(0,buf.indexOf("&"));
        buf = buf.replace(buf.substring(0,buf.indexOf("to=")+3),""); //удаляем ненужное
        int ind = buf.indexOf("&");
        if (ind<0) ind = buf.length();
        en = buf.substring(0,ind);

        Boolean visited=false;
        Elements html_div = html.select("div.b-schedule.schedule_parts"), html_span = html.select("span.page-title_heading");

        for (int i=0;i<html_div.size();i++) {
            String trailer_buf = "Основной состав";

            if (i>0) {
                trailer_buf = html_span.get(i-1).text();
            }

            Elements stations = html_div.get(i).select("tr.b-train");
            for (int j = 0; j < stations.size(); j++) {

                String stop_time, stop_date = "", dep_time, dep_date = "", trailer = "";

                if (stations.get(j).select("a.train_name").text().contains(st)) {
                    if (!visited) visitposition = j;
                    visited = true;
                }

                if (j==0) trailer=trailer_buf;

                stop_time = stations.get(j).select("b.train_end-time").text();
                if (stop_time.contains(", ")) {
                    stop_date = stop_time.substring(stop_time.indexOf(", ") + 2, stop_time.length());
                    stop_time = stop_time.substring(0, stop_time.indexOf(", "));
                }

                dep_time = stations.get(j).select("b.train_start-time").text();
                if (dep_time.contains(", ")) {
                    dep_date = dep_time.substring(dep_time.indexOf(", ") + 2, dep_time.length());
                    dep_time = dep_time.substring(0, dep_time.indexOf(", "));
                }

                route.add(new Route(stations.get(j).select("a.train_name").text(),
                        stop_time,
                        stop_date,
                        dep_time,
                        dep_date,
                        stations.get(j).select("b.train_stop-time").text(),
                        visited,
                        trailer));

                if (stations.get(j).select("a.train_name").text().contains(en)) visited = false;
            }
        }
    }

    private void initializeData(){
        route = new ArrayList<>();
    }

    private void initializeAdapter(){
        adapter1 = new RouteRVAdapter(route);
        mrv1.setAdapter(adapter1);
    }

}