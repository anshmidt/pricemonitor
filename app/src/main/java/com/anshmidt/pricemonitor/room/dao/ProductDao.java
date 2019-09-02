package com.anshmidt.pricemonitor.room.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.anshmidt.pricemonitor.room.entity.Product;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Product product);

    @Delete
    void delete(Product product);

    @Query("SELECT * FROM products")
    List<Product> getAllProducts();

    @Query("SELECT id, name FROM products WHERE name = :productName")
    Product getProductByName(String productName);

    @Query("SELECT id FROM products WHERE name = :productName")
    int getProductIdByName(String productName);

    @Query("SELECT id, name FROM products WHERE id = :productId")
    Product getProductById(int productId);

    @Query("DELETE FROM products")
    void deleteAllProducts();

    @Query("DELETE FROM products WHERE name = :productName")
    void deleteProductByName(String productName);


}
