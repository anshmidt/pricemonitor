package com.anshmidt.pricemonitor.room;


import android.content.Context;

import com.anshmidt.pricemonitor.room.dao.ItemDao;
import com.anshmidt.pricemonitor.room.dao.PriceDao;
import com.anshmidt.pricemonitor.room.dao.ProductDao;
import com.anshmidt.pricemonitor.room.dao.StoreDao;
import com.anshmidt.pricemonitor.room.entity.Item;
import com.anshmidt.pricemonitor.room.entity.Price;
import com.anshmidt.pricemonitor.room.entity.Product;
import com.anshmidt.pricemonitor.room.entity.Store;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Item.class, Price.class, Product.class, Store.class}, version = 2)
public abstract class PricesDatabase extends RoomDatabase {
    private static volatile PricesDatabase INSTANCE;

    public static final String DATABASE_NAME = "prices_database";

    public abstract ItemDao itemDao();
    public abstract PriceDao priceDao();
    public abstract ProductDao productDao();
    public abstract StoreDao storeDao();


    static PricesDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PricesDatabase.class) {
                if (INSTANCE == null) {
                    // Create database
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PricesDatabase.class, PricesDatabase.DATABASE_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
