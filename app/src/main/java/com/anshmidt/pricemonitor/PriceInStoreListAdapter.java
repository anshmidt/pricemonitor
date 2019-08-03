package com.anshmidt.pricemonitor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.anshmidt.pricemonitor.data.CurrentPriceInStore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PriceInStoreListAdapter extends RecyclerView.Adapter<PriceInStoreListAdapter.ViewHolder> {

    ArrayList<CurrentPriceInStore> priceInStoreList;
    LayoutInflater layoutInflater;
    Context context;
    PricesListListener pricesListListener;
    ItemsListMultipleStoresAdapter.ViewHolder parentViewHolder;
    int parentPosition;

    public interface PricesListListener {
        void onStoreIconClicked(String storeName, ItemsListMultipleStoresAdapter.ViewHolder parentViewHolder, int parentPosition);
    }

    public PriceInStoreListAdapter(ArrayList<CurrentPriceInStore> currentPriceInStoreList, Context context) {
        this.priceInStoreList = currentPriceInStoreList;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    public void setPricesListListener(PricesListListener pricesListListener, ItemsListMultipleStoresAdapter.ViewHolder viewHolder, int parentPosition) {
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
        final CurrentPriceInStore currentPriceInStore = priceInStoreList.get(i);


        viewHolder.priceTextView.setText(String.format(Locale.getDefault(), "%,d", currentPriceInStore.price));

        viewHolder.storeNameTextView.setText(currentPriceInStore.storeName);
        displayDate(viewHolder.dateTextView, currentPriceInStore.date);
        displayStoreIcon(viewHolder.storeIcon, currentPriceInStore.storeName, currentPriceInStore.storeColor);

        final String productInStoreUrl = currentPriceInStore.productInStoreUrl;

        viewHolder.gotoUrlImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProductWebpageInBrowser(productInStoreUrl, context);
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
        return priceInStoreList.size();
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
