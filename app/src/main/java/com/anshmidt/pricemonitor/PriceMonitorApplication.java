package com.anshmidt.pricemonitor;

import android.app.Application;

import com.anshmidt.pricemonitor.dagger.AppComponent;
import com.anshmidt.pricemonitor.dagger.AppModule;
import com.anshmidt.pricemonitor.dagger.DaggerAppComponent;

public class PriceMonitorApplication extends Application {

    private static AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public static AppComponent getComponent() {
        return component;
    }
}
