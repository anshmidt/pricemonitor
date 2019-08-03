package com.anshmidt.pricemonitor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.anshmidt.pricemonitor.data.CurrentPriceInStore;
import com.anshmidt.pricemonitor.data.Product;
import com.anshmidt.pricemonitor.data.ProductInStore;
import com.anshmidt.pricemonitor.dialogs.ProductSettingsBottomSheetFragment;
import com.jjoe64.graphview.GraphView;

import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

public class ItemsListMultipleStoresAdapter extends RecyclerView.Adapter<ItemsListMultipleStoresAdapter.ViewHolder> implements PriceInStoreListAdapter.PricesListListener {

    private Context context;
    public ArrayList<Product> products;
    private DataManager dataManager;
    private GraphPlotter graphPlotter;

    public ItemsListMultipleStoresAdapter(Context context, ArrayList<Product> products, DataManager dataManager, GraphPlotter graphPlotter) {
        this.context = context;
        this.products = products;
        this.dataManager = dataManager;
        this.graphPlotter = graphPlotter;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_card_multiple_stores, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Product product = products.get(position);
        final String productName = product.name;
        viewHolder.productTitleTextView.setText(productName);
        viewHolder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayMainBottomSheet(productName);
            }
        });

        viewHolder.priceInStoreListRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        ArrayList<CurrentPriceInStore> currentPriceInStoreList = dataManager.retrieveLastPricesOfProduct(product);
        PriceInStoreListAdapter priceInStoreListAdapter = new PriceInStoreListAdapter(currentPriceInStoreList, context);
        priceInStoreListAdapter.setPricesListListener(this, viewHolder, position);
        viewHolder.priceInStoreListRecyclerView.setAdapter(priceInStoreListAdapter);


        ArrayList<ProductInStore> items = product.productInStoreList;
        graphPlotter.clearGraph(viewHolder.graph);
        graphPlotter.setXAxisRange(viewHolder.graph, dataManager.getMinDate(items), dataManager.getMaxDate(items), true);
        for (ProductInStore item : items) {
            TreeMap<Date, Integer> dataToDisplay = item.prices;
            int graphColor = item.store.color;
            graphPlotter.addSeriesToGraph(dataToDisplay, viewHolder.graph, graphColor, false);
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private void displayMainBottomSheet(String productName) {
        ProductSettingsBottomSheetFragment productSettingsBottomSheetFragment = new ProductSettingsBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(productSettingsBottomSheetFragment.KEY_PRODUCT_NAME, productName);
        productSettingsBottomSheetFragment.setArguments(bundle);
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        productSettingsBottomSheetFragment.show(fragmentManager, productSettingsBottomSheetFragment.FRAGMENT_TAG);
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {
        TextView productTitleTextView;
        GraphView graph;
        ImageButton moreButton;
        RecyclerView priceInStoreListRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productTitleTextView = itemView.findViewById(R.id.item_title_textview);
            graph = itemView.findViewById(R.id.graph);
            moreButton = itemView.findViewById(R.id.item_more_imagebutton);
            priceInStoreListRecyclerView = itemView.findViewById(R.id.priceinstore_recyclerview);
        }


    }

    @Override
    public void onStoreIconClicked(String clickedStoreName, ViewHolder parentViewHolder, int position) {
        TreeMap<Date, Integer> dataToDisplay = null;
        int storeColor = -1;

        Product product = products.get(position);
        ArrayList<ProductInStore> items = product.productInStoreList;
        for (ProductInStore item : items) {
            if (item.store.title.equals(clickedStoreName)) {
                dataToDisplay = item.prices;
                storeColor = item.store.color;
            }
        }

        graphPlotter.addSeriesToGraph(dataToDisplay, parentViewHolder.graph, storeColor, false);
    }

}
