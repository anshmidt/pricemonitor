package com.anshmidt.pricemonitor.room.dao;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.anshmidt.pricemonitor.room.entity.Item;
import com.anshmidt.pricemonitor.room.entity.Product;

import java.util.List;

@Dao
public interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Item item);

    @Delete
    void delete(Item item);

    @Query("SELECT products.id, products.name FROM items LEFT OUTER JOIN products ON items.product_id = products.id WHERE items.id = :itemId")
    Product getProductByItemId(int itemId);

    @Query("SELECT url FROM items WHERE id = :itemId")
    String getItemUrl(int itemId);

    @Query("SELECT id FROM items WHERE url = :itemUrl")
    Integer getItemId(String itemUrl);

    @Query("SELECT * FROM items WHERE url = :itemUrl")
    Item getItemByUrl(String itemUrl);

    @Query("SELECT * FROM items WHERE id = :itemId")
    Item getItemById(int itemId);

    @Query("SELECT id FROM items")
    List<Integer> getAllItemsIdList();

    @Query("SELECT items.id FROM items LEFT OUTER JOIN products ON items.product_id = products.id WHERE products.name = :productName")
    List<Integer> getItemsIdsWithSpecificProductName(String productName);

    @Query("SELECT * FROM items")
    List<Item> getAllItems();

    @Query("SELECT * FROM items WHERE product_id = :productId")
    List<Item> getItemsByProductId(int productId);

    @Query("DELETE FROM items")
    int deleteAllItems();

    @Query("DELETE FROM items WHERE product_id = :productId")
    void deleteAllItemsWithProductId(int productId);


}