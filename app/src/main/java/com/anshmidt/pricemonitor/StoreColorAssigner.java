package com.anshmidt.pricemonitor;

import android.content.Context;

import javax.inject.Inject;

public class StoreColorAssigner {

    private Context context;

    @Inject
    public StoreColorAssigner(Context context) {
        this.context = context;
    }

    public int getColorByStoreId(int storeId) {
        int[] storesColors = context.getResources().getIntArray(R.array.storesColors);
        int colorsCount = storesColors.length;
        int colorId = storeId % colorsCount;
        return storesColors[colorId];
    }
}
