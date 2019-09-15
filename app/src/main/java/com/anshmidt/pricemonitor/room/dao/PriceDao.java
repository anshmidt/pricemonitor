package com.anshmidt.pricemonitor.room.dao;


import com.anshmidt.pricemonitor.room.entity.Price;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
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
    Price getLatestPriceForItem(int itemId);

    @Query("SELECT * FROM prices WHERE item_id = :itemId ORDER BY item_id LIMIT 1 OFFSET 1")
    Price getPreviousPriceForItem(int itemId);

    @Query("DELETE FROM prices WHERE item_id = :itemId")
    void deleteAllPricesForItem(int itemId);

    @Query("SELECT * FROM prices")
    List<Price> getAllPrices();

    @Query("DELETE FROM prices")
    void deleteAllPrices();



}
