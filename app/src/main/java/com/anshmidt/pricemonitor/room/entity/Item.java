package com.anshmidt.pricemonitor.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.List;

@Entity(tableName = Item.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(
                        entity = Product.class,
                        parentColumns = Product.KEY_ID,
                        childColumns = Item.KEY_PRODUCT_ID
                ),
                @ForeignKey(
                        entity = Store.class,
                        parentColumns = Store.KEY_ID,
                        childColumns = Item.KEY_STORE_ID
                ),
        },
        indices = {
                @Index(Item.KEY_PRODUCT_ID),
                @Index(Item.KEY_STORE_ID)
        }
)
public class Item {

    public static final String TABLE_NAME = "items";
    public static final String KEY_ID = "id";
    public static final String KEY_URL = "url";
    public static final String KEY_STORE_ID = "store_id";
    public static final String KEY_PRODUCT_ID = "product_id";

    @ColumnInfo(name = KEY_ID)
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = KEY_URL)
    public String url;

    @ColumnInfo(name = KEY_PRODUCT_ID)
    public int productId; // foreign key ref. product.id

    @ColumnInfo(name = KEY_STORE_ID)
    public int storeId; // foreign key ref. stores.id



    //for inserting only
    @Ignore
    public Item(String url, int productId, int storeId) {
        this.url = url;
        this.productId = productId;
        this.storeId = storeId;
    }


    public Item(int id, String url, int productId, int storeId) {
        this.id = id;
        this.url = url;
        this.productId = productId;
        this.storeId = storeId;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", productId=" + productId +
                ", storeId=" + storeId +
                '}';
    }
}
