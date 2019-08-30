package com.anshmidt.pricemonitor.dagger;

import android.content.Context;

import com.anshmidt.pricemonitor.data.DataManager;
import com.anshmidt.pricemonitor.DatabaseHelper;
import com.anshmidt.pricemonitor.GraphPlotter;
import com.anshmidt.pricemonitor.NotificationHelper;
import com.anshmidt.pricemonitor.R;
import com.anshmidt.pricemonitor.StoreColorAssigner;
import com.anshmidt.pricemonitor.scrapers.StoreScraperFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private final Context context;

    public AppModule(Context context) {
        this.context = context;
    }

    @Provides
    Context provideContext() {
        return context;
    }

    @Provides
    @Singleton
    DatabaseHelper provideDatebaseHelper(Context context, DataManager dataManager) {
        return new DatabaseHelper(context, dataManager);
    }

    @Provides
    DataManager provideDataManager() {
        return new DataManager();
    }

    @Provides
    StoreScraperFactory provideStoreScraperFactory(Context context) {
        return new StoreScraperFactory(context);
    }

    @Provides
    GraphPlotter provideGraphPlotter(Context context, DataManager dataManager) {
        return new GraphPlotter(context, dataManager);
    }

    @Provides
    NotificationHelper provideNotificationHelper(Context context) {
        return new NotificationHelper(context);
    }

//    @Provides
//    ProductsListAdapter provideItemsListMultipleStoresAdapter(Context context, ArrayList<Product> products, DataManager dataManager, GraphPlotter graphPlotter) {
//        return new ProductsListAdapter(context, products, dataManager, graphPlotter);
//    }

    @Provides
    int[] getStoresColors(Context context) {
        return context.getResources().getIntArray(R.array.storesColors);
    }

    @Provides
    StoreColorAssigner provideStoreColorAssigner(Context context) {
        return new StoreColorAssigner(context);
    }

}
