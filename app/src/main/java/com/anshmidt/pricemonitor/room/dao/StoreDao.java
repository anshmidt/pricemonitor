package com.anshmidt.pricemonitor.room.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.anshmidt.pricemonitor.room.entity.Store;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface StoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Store store);

    @Update
    void update(Store store);

    @Delete
    void delete(Store store);

    @Query("SELECT id FROM stores WHERE url = :storeUrl")
    Integer getStoreIdByUrl(String storeUrl);

    @Query("SELECT id FROM stores WHERE name = :storeName")
    Integer getStoreIdByName(String storeName);

    @Query("SELECT stores.name FROM items LEFT OUTER JOIN stores ON items.store_id = stores.id WHERE items.id = :itemId")
    String getStoreNameByItemId(int itemId);

    @Query("SELECT stores.url FROM items LEFT OUTER JOIN stores ON items.store_id = stores.id WHERE items.id = :itemId")
    String getStoreUrlByItemId(int itemId);

    @Query("SELECT stores.id, stores.name, stores.url FROM items LEFT OUTER JOIN stores ON items.store_id = stores.id WHERE items.id = :itemId")
    Store getStoreByItemId(int itemId);

    @Query("SELECT id, name, url FROM stores WHERE id = :id")
    Store getStoreById(int id);

    @Query("SELECT id, name, url FROM stores WHERE url = :url")
    Store getStoreByUrl(String url);

    @Query("SELECT url from stores")
    List<String> getAllStoreUrls();

    @Query("SELECT * FROM stores")
    List<Store> getAllStores();

    @Query("DELETE FROM stores")
    void deleteAllStores();


}

