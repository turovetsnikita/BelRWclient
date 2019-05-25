<<<<<<< Updated upstream
package com.turovetsnikita.belrwclient;

/**
 * Created by Nikita on 11.3.17.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.turovetsnikita.belrwclient.adapters.ViewPagerAdapter;
import com.turovetsnikita.belrwclient.fragments.RouteFragment;
import com.turovetsnikita.belrwclient.fragments.InfoFragment;
import com.turovetsnikita.belrwclient.fragments.CarFragment;

public class DetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Поезд " + getIntent().getStringExtra("train_num"));
        toolbar.setSubtitle(getIntent().getStringExtra("tr_route"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String default_tab = sp.getString("train_details_default_tab", "");
        switch (default_tab) {
            case "0": {
                viewPager.setCurrentItem(0);
                break;
            }
            case "1": {
                viewPager.setCurrentItem(1);
                break;
            }
            case "2": {
                viewPager.setCurrentItem(2);
                break;
            }
            default: {
                viewPager.setCurrentItem(1);
                break;
            }
        }

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new RouteFragment(), getString(R.string.text_route_tab));
        adapter.addFragment(new CarFragment(), getString(R.string.text_cars_tab));
        adapter.addFragment(new InfoFragment(), getString(R.string.text_info_tab));
        viewPager.setAdapter(adapter);
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
=======
package com.turovetsnikita.belrwclient;

/**
 * Created by Nikita on 11.3.17.
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.turovetsnikita.belrwclient.adapters.ViewPagerAdapter;
import com.turovetsnikita.belrwclient.fragments.RouteFragment;
import com.turovetsnikita.belrwclient.fragments.InfoFragment;
import com.turovetsnikita.belrwclient.fragments.CarFragment;

public class DetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Поезд " + getIntent().getStringExtra("train_num"));
        toolbar.setSubtitle(getIntent().getStringExtra("tr_route"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String default_tab = sp.getString("train_details_default_tab", "");
        switch (default_tab) {
            case "0": {
                viewPager.setCurrentItem(0);
                break;
            }
            case "1": {
                viewPager.setCurrentItem(1);
                break;
            }
            case "2": {
                viewPager.setCurrentItem(2);
                break;
            }
            default: {
                viewPager.setCurrentItem(1);
                break;
            }
        }

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new RouteFragment(), getString(R.string.text_route_tab));
        adapter.addFragment(new CarFragment(), getString(R.string.text_cars_tab));
        adapter.addFragment(new InfoFragment(), getString(R.string.text_info_tab));
        viewPager.setAdapter(adapter);
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
>>>>>>> Stashed changes
