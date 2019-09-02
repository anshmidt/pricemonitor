package com.anshmidt.pricemonitor.room.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.anshmidt.pricemonitor.room.entity.Price;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface PriceDao {

    @Insert
    long insert(Price price);

    @Delete
    void delete(Price price);

    @Query("SELECT * FROM prices WHERE item_id = :itemId")
    List<Price> getAllPricesForItem(int itemId);

    @Query("SELECT MAX(date), * FROM prices WHERE item_id = :itemId")
    Price getRecentPriceForItem(int itemId);

    @Query("DELETE FROM prices WHERE item_id = :itemId")
    void deleteAllPricesForItem(int itemId);

    @Query("SELECT * FROM prices")
    List<Price> getAllPrices();

    @Query("DELETE FROM prices")
    void deleteAllPrices();



}
