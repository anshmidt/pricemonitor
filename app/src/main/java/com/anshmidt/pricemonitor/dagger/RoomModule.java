package com.anshmidt.pricemonitor.dagger;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;

import com.anshmidt.pricemonitor.room.PricesDatabase;
import com.anshmidt.pricemonitor.room.dao.ItemDao;
import com.anshmidt.pricemonitor.room.dao.PriceDao;
import com.anshmidt.pricemonitor.room.dao.ProductDao;
import com.anshmidt.pricemonitor.room.dao.StoreDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RoomModule {

//    private PricesDatabase pricesDatabase;
//
//    public RoomModule(Application application) {
//        pricesDatabase = Room.databaseBuilder(application, PricesDatabase.class, PricesDatabase.DATABASE_NAME).build();
//    }
//
//    @Singleton
//    @Provides
//    PricesDatabase providesRoomDatabase() {
//        return pricesDatabase;
//    }

    @Singleton
    @Provides
    PricesDatabase providesPricesDatabase(Context context) {
        return Room.databaseBuilder(context, PricesDatabase.class, PricesDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }

    @Singleton
    @Provides
    ItemDao providesItemDao(PricesDatabase pricesDatabase) {
        return pricesDatabase.itemDao();
    }

    @Singleton
    @Provides
    PriceDao providesPriceDao(PricesDatabase pricesDatabase) {
        return pricesDatabase.priceDao();
    }

    @Singleton
    @Provides
    ProductDao providesProductDao(PricesDatabase pricesDatabase) {
        return pricesDatabase.productDao();
    }

    @Singleton
    @Provides
    StoreDao providesStoreDao(PricesDatabase pricesDatabase) {
        return pricesDatabase.storeDao();
    }
}
