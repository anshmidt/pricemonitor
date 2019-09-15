package com.anshmidt.pricemonitor.room.entity;



import com.anshmidt.pricemonitor.room.DateConverter;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import static androidx.room.ForeignKey.CASCADE;


@Entity(tableName = Price.TABLE_NAME,
        foreignKeys = @ForeignKey(
                entity = Item.class,
                parentColumns = Item.KEY_ID,
                childColumns = Price.KEY_ITEM_ID,
                onDelete=CASCADE
        ),
        indices = @Index(Price.KEY_ITEM_ID)
)
@TypeConverters(DateConverter.class)
public class Price {

    public static final String TABLE_NAME = "prices";
    public static final String KEY_ID = "id";
    public static final String KEY_DATE = "date";

    public static final String KEY_ITEM_ID = "item_id";
    public static final String KEY_PRICE = "price";

    @ColumnInfo(name = KEY_ID)
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = KEY_DATE)
    public Date date;

    @ColumnInfo(name = KEY_ITEM_ID)
    public int itemId; // foreign key ref. items.id

    @ColumnInfo(name = KEY_PRICE)
    public int price;

    // for inserting only
    @Ignore
    public Price(Date date, int itemId, int price) {
        this.date = date;
        this.itemId = itemId;
        this.price = price;
    }


    public Price(int id, Date date, int itemId, int price) {
        this.id = id;
        this.date = date;
        this.itemId = itemId;
        this.price = price;
    }

    @Override
    public String toString() {
        return "Price{" +
                "id=" + id +
                ", date=" + date +
                ", itemId=" + itemId +
                ", price=" + price +
                '}';
    }
}
