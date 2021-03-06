package com.anshmidt.pricemonitor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.anshmidt.pricemonitor.data.DataManager;
import com.anshmidt.pricemonitor.data.ItemData;
import com.anshmidt.pricemonitor.data.ProductData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PriceInStoreListAdapter extends RecyclerView.Adapter<PriceInStoreListAdapter.ViewHolder> {

//    ArrayList<CurrentPriceInStore> priceInStoreList;
    ProductData productData;
    LayoutInflater layoutInflater;
    DataManager dataManager;
    Context context;
    StoreColorAssigner storeColorAssigner;
    PricesListListener pricesListListener;
    ProductsListAdapter.ViewHolder parentViewHolder;
    int parentPosition;

    public interface PricesListListener {
        void onStoreIconClicked(String storeName, ProductsListAdapter.ViewHolder parentViewHolder, int parentPosition);
    }

    public PriceInStoreListAdapter(ProductData productData, Context context, DataManager dataManager, StoreColorAssigner storeColorAssigner) {
        this.productData = productData;
        this.dataManager = dataManager;
        this.context = context;
        this.storeColorAssigner = storeColorAssigner;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setPricesListListener(PricesListListener pricesListListener, ProductsListAdapter.ViewHolder viewHolder, int parentPosition) {
        this.pricesListListener = pricesListListener;
        this.parentViewHolder = viewHolder;
        this.parentPosition = parentPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.price_in_store_entry, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        ItemData itemData = productData.itemDataList.get(i);
        Optional<Integer> itemCurrentPrice = dataManager.getLatestPriceValue(itemData);
        if (!itemCurrentPrice.isPresent()) {
            return;
        }
        String storeName = itemData.store.name;
        Optional<Date> latestDate = dataManager.getLatestDate(itemData);
        if (!latestDate.isPresent()) {
            return;
        }

        int storeColor = storeColorAssigner.getColorByStoreId(itemData.store.id);
        String itemUrl = itemData.item.url;

        viewHolder.priceTextView.setText(String.format(Locale.getDefault(), "%,d", itemCurrentPrice.get()));

        viewHolder.storeNameTextView.setText(storeName);
        displayDate(viewHolder.dateTextView, latestDate.get());
        displayStoreIcon(viewHolder.storeIcon, storeName, storeColor);

        viewHolder.gotoUrlImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProductWebpageInBrowser(itemUrl, context);
            }
        });


    }


    private void displayDate(TextView textView, Date date) {
        String datePattern = "HH:mm:ss dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
        String displayableDate = simpleDateFormat.format(date);
        textView.setText(displayableDate);
    }

    private void displayStoreIcon(ImageView storeIcon, String storeName, int storeColor) {
        String storeTitleFirstLetter = storeName.substring(0, 1).toUpperCase();
        TextDrawable textDrawable = TextDrawable.builder()
                .buildRound(storeTitleFirstLetter, storeColor);
        storeIcon.setImageDrawable(textDrawable);
    }

    private void openProductWebpageInBrowser(String url, Context context) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return productData.itemDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView storeIcon;
        TextView priceTextView;
        TextView storeNameTextView;
        TextView dateTextView;
        ImageButton gotoUrlImageButton;
        View itemView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            storeIcon = itemView.findViewById(R.id.item_icon_priceinstore_imageview);
            priceTextView = itemView.findViewById(R.id.current_price_priceinstore_textview);
            storeNameTextView = itemView.findViewById(R.id.store_title_priceinstore_textview);
            dateTextView = itemView.findViewById(R.id.last_request_time_priceinstore_textview);
            gotoUrlImageButton = itemView.findViewById(R.id.goto_url_priceinstore_imagebutton);

            this.itemView = itemView;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (pricesListListener != null) {
                String storeName = storeNameTextView.getText().toString();
                pricesListListener.onStoreIconClicked(storeName, parentViewHolder, parentPosition);
            }
        }
    }
}
