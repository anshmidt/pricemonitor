package com.anshmidt.pricemonitor.room.dao;


import com.anshmidt.pricemonitor.room.entity.Product;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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
