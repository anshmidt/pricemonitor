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

import com.anshmidt.pricemonitor.data.DataManager;
import com.anshmidt.pricemonitor.data.ItemData;
import com.anshmidt.pricemonitor.data.ProductData;
import com.anshmidt.pricemonitor.dialogs.ProductSettingsBottomSheetFragment;
import com.anshmidt.pricemonitor.room.entity.Price;
import com.jjoe64.graphview.GraphView;

import java.util.List;

public class ProductsListAdapter extends RecyclerView.Adapter<ProductsListAdapter.ViewHolder> implements PriceInStoreListAdapter.PricesListListener {

    private Context context;
    public List<ProductData> productDataList;
    private DataManager dataManager;
    private GraphPlotter graphPlotter;
    private StoreColorAssigner storeColorAssigner;

    public ProductsListAdapter(Context context, List<ProductData> productDataList, DataManager dataManager, GraphPlotter graphPlotter, StoreColorAssigner storeColorAssigner) {
        this.context = context;
        this.productDataList = productDataList;
        this.dataManager = dataManager;
        this.graphPlotter = graphPlotter;
        this.storeColorAssigner = storeColorAssigner;
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
        ProductData productData = productDataList.get(position);
        final String productName = productData.product.name;
        viewHolder.productTitleTextView.setText(productName);
        viewHolder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayMainBottomSheet(productName);
            }
        });

        viewHolder.priceInStoreListRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        PriceInStoreListAdapter priceInStoreListAdapter = new PriceInStoreListAdapter(productData, context, dataManager, storeColorAssigner);
        priceInStoreListAdapter.setPricesListListener(this, viewHolder, position);
        viewHolder.priceInStoreListRecyclerView.setAdapter(priceInStoreListAdapter);

        graphPlotter.clearGraph(viewHolder.graph);

        if (dataManager.isEmpty(productData)) {
            graphPlotter.setXAxisRange(viewHolder.graph, null, null, false);
        } else {
            graphPlotter.setXAxisRange(
                    viewHolder.graph,
                    dataManager.getMinDate(productData).get(),
                    dataManager.getMaxDate(productData).get(),
                    true);
        }

        for (ItemData itemData : productData.itemDataList) {
            int graphColor = storeColorAssigner.getColorByStoreId(itemData.store.id);
            graphPlotter.addSeriesToGraph(itemData.prices, viewHolder.graph, graphColor, false);
        }

    }

    @Override
    public int getItemCount() {
        return productDataList.size();
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
        // display graph series corresponding to the clicked store on top of the other series
        ProductData productData = productDataList.get(position);
        for (ItemData itemData : productData.itemDataList) {
            String storeName = itemData.store.name;
            if (storeName.equals(clickedStoreName)) {
                int storeColor = storeColorAssigner.getColorByStoreId(itemData.store.id);
                List<Price> clickedItemPrices = itemData.prices;
                graphPlotter.addSeriesToGraph(clickedItemPrices, parentViewHolder.graph, storeColor, false);
            }
        }

    }


}
