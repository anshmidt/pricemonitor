package com.anshmidt.pricemonitor.dialogs;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anshmidt.pricemonitor.PriceMonitorApplication;
import com.anshmidt.pricemonitor.R;
import com.anshmidt.pricemonitor.room.PricesRepository;

import javax.inject.Inject;

public class ProductSettingsBottomSheetFragment extends BottomSheetDialogFragment {

    public interface ProductSettingsBottomSheetListener {
        void onProductDeleted(String productName);
    }

    public final String FRAGMENT_TAG = "productSettingsBottomSheetFragment";
    public final String KEY_PRODUCT_NAME = "product_name";
    String productName;

    TextView deleteProductTextView;
    TextView addStoreTextView;
    @Inject PricesRepository pricesRepository;
//    @Inject DatabaseHelper databaseHelper;

    @Override
    public void onAttach(Context context) {
        PriceMonitorApplication.getComponent().inject(this);
        super.onAttach(context);
    }

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

                dismiss();
                ProductSettingsBottomSheetListener productSettingsBottomSheetListener = (ProductSettingsBottomSheetListener) getActivity();
                productSettingsBottomSheetListener.onProductDeleted(productName);
            }
        });

        addStoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddProductDialogFragment addProductDialogFragment = new AddProductDialogFragment();

                Bundle productBundle = new Bundle();
                productBundle.putString(addProductDialogFragment.KEY_PRODUCT_NAME, productName);
                addProductDialogFragment.setArguments(productBundle);

                FragmentManager manager = getActivity().getFragmentManager();
                addProductDialogFragment.show(manager, addProductDialogFragment.FRAGMENT_TAG);
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
