package com.anshmidt.pricemonitor.room.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = Product.TABLE_NAME)
public class Product {

    public static final String TABLE_NAME = "products";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";

    @ColumnInfo(name = KEY_ID)
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = KEY_NAME)
    public String name;

    // for inserting only
    @Ignore
    public Product(String name) {
        this.name = name;
    }

    public Product(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
