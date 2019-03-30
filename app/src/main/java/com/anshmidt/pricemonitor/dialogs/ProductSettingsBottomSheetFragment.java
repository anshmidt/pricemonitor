package com.anshmidt.pricemonitor.dialogs;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anshmidt.pricemonitor.DatabaseHelper;
import com.anshmidt.pricemonitor.R;
import com.anshmidt.pricemonitor.data.Product;

public class ProductSettingsBottomSheetFragment extends BottomSheetDialogFragment {

    public interface ProductSettingsBottomSheetListener {
        void onProductDeleted(String productName);
    }

    public final String FRAGMENT_TAG = "productSettingsBottomSheetFragment";
    public final String KEY_PRODUCT_NAME = "product_name";
    String productName;

    TextView deleteProductTextView;
    TextView addStoreTextView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.product_settings_bottom_sheet, container,
                false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            productName = bundle.getString(KEY_PRODUCT_NAME);
        } else {
            productName = "";
        }

        deleteProductTextView = view.findViewById(R.id.delete_product_productsettingsbottomsheet_textview);
        addStoreTextView = view.findViewById(R.id.addstore_productsettingsbottomsheet_textview);

        deleteProductTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext());
                databaseHelper.deleteAllItemsWithName(productName);

                dismiss();
                ProductSettingsBottomSheetListener productSettingsBottomSheetListener = (ProductSettingsBottomSheetListener) getActivity();
                productSettingsBottomSheetListener.onProductDeleted(productName);
            }
        });

        addStoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddItemDialogFragment addItemDialogFragment = new AddItemDialogFragment();

                Bundle productBundle = new Bundle();
                productBundle.putString(addItemDialogFragment.KEY_PRODUCT_NAME, productName);
                addItemDialogFragment.setArguments(productBundle);

                FragmentManager manager = getActivity().getFragmentManager();
                addItemDialogFragment.show(manager, addItemDialogFragment.FRAGMENT_TAG);
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

        super.onDismiss(dialog);

    }


}
