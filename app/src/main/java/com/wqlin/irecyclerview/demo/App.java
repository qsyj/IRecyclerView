package com.wqlin.irecyclerview.demo;

import android.app.Application;

import com.wqlin.irecyclerview.demo.network.OkHttp;

/**
 * Created by aspsine on 16/4/6.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        OkHttp.init(getApplicationContext());
    }

}
