package com.anshmidt.pricemonitor.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.anshmidt.pricemonitor.DatabaseHelper;
import com.anshmidt.pricemonitor.PriceMonitorApplication;
import com.anshmidt.pricemonitor.R;
import com.anshmidt.pricemonitor.scrapers.StoreScraper;
import com.anshmidt.pricemonitor.scrapers.StoreScraperFactory;

import java.util.ArrayList;

import javax.inject.Inject;

//TODO: add verification if product already present
public class AddProductDialogFragment extends DialogFragment implements StoreScraper.StoreScraperListener {


    public interface AddProductDialogListener {
        void onProductAdded(String productName, String itemUrl, String storeUrl, int price);
    }

    public final String FRAGMENT_TAG = "AddProductDialog";
    public static final String KEY_PRODUCT_NAME = "product_name";
    EditText productNameEditText;
    EditText urlEditText;
    TextInputLayout urlInputLayout;
    TextInputLayout productNameInputLayout;

    @Inject DatabaseHelper databaseHelper;

    boolean urlValidated = false;
    boolean productNameValidated = false;
    String enteredUrl;
    String knownStoreUrl;
    String productName;
    String itemUrl;
    int price = StoreScraper.PRICE_NOT_FOUND;
    AlertDialog dialog;
    ProgressBar requestProgressBar;
    TextView requestStatusTextView;
    ImageButton clearUrlButton;
    private final String STATUS_URL_VALIDATED_LOCALLY = "UrlValidatedLocally";

    @Override
    public void onAttach(Context context) {
        PriceMonitorApplication.getComponent().inject(this);
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        View subView = inflater.inflate(R.layout.dialog_add_product, null);

        productNameEditText = subView.findViewById(R.id.productname_addproductdialog_edittext);
        urlEditText = subView.findViewById(R.id.url_addproductdialog_edittext);
        urlInputLayout = subView.findViewById(R.id.url_addproductdialog_layout);
        productNameInputLayout = subView.findViewById(R.id.productname_addproductdialog_layout);
        clearUrlButton = subView.findViewById(R.id.clearurl_addproductdialog_button);

        requestProgressBar = subView.findViewById(R.id.requestprogress_addproductdialog_progressbar);
        requestStatusTextView = subView.findViewById(R.id.request_status_addproductdialog_textview);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String productName = bundle.getString(KEY_PRODUCT_NAME);
            productNameEditText.setText(productName);
            productNameEditText.setKeyListener(null);
            urlEditText.requestFocus();
            productNameValidated = true;
            this.productName = productName;
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(subView);

        builder.setPositiveButton(R.string.add_button_addproductdialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AddProductDialogListener listener = (AddProductDialogListener) getActivity();
                listener.onProductAdded(productName, itemUrl, knownStoreUrl, price);
            }
        });

        builder.setNegativeButton(R.string.cancel_button_addproductdialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        clearUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urlEditText.getText().clear();
            }
        });

        dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);


        productNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String enteredProductName = s.toString();
                if (isProductNameValid(enteredProductName)) {
                    productNameInputLayout.setError(null);
                    productNameValidated = true;
                    productName = enteredProductName;
                } else {
                    productNameInputLayout.setError(getString(R.string.error_invalid_productname_addproductdialog));
                    productNameValidated = false;
                }
                setPositiveButtonState(productNameValidated, urlValidated, price);
            }
        });

        urlEditText.addTextChangedListener(new TextWatcher() {
            final int DELAY = 2000; //ms after user stops typing
            long lastTextChangedTimestamp;
            Handler handler = new Handler();

            Runnable serverRequestTask = new Runnable() {
                @Override
                public void run() {
                    if (System.currentTimeMillis() - lastTextChangedTimestamp >= DELAY) {
                        validateUrlOnServer(enteredUrl);
                    }
                }
            };

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                urlValidated = false;
                price = StoreScraper.PRICE_NOT_FOUND;
                enteredUrl = s.toString();
                lastTextChangedTimestamp = 0;
                handler.removeCallbacks(serverRequestTask);
                setPositiveButtonState(productNameValidated, urlValidated, price);

                String urlValidatedLocallyStatus = validateUrlLocally(enteredUrl);
                if (!urlValidatedLocallyStatus.equals(STATUS_URL_VALIDATED_LOCALLY)) {
                    displayInvalidUrl(urlValidatedLocallyStatus);
                } else {
                    String knownStore = getKnownStoreForUrl(enteredUrl);
                    displayStoreFoundConnectionInProgress(knownStore);
                    handler.postDelayed(serverRequestTask, DELAY);
                    lastTextChangedTimestamp = System.currentTimeMillis();
                }



            }
        });
        return dialog;
    }

    @Override
    public void onResponseFromServer(int price, String itemUrl) {
        if (price != StoreScraper.PRICE_NOT_FOUND) {
            displaySuccessfulResponse(price, knownStoreUrl);
            urlValidated = true;
            this.price = price;
            this.itemUrl = itemUrl;
        } else {
            displaySuccessfulResponseButPriceNotFound(knownStoreUrl);
        }
        setPositiveButtonState(productNameValidated, urlValidated, price);

    }

    @Override
    public void onErrorResponseFromServer(VolleyError error) {
        String connectionError = error.toString();
        if (connectionError.replace(" ", "").isEmpty()) {
            connectionError = getString(R.string.error_unknown_addproductdialog);
        }
        displayErrorResponse(connectionError, knownStoreUrl);
    }


    public void setPositiveButtonState(boolean productNameValid, boolean urlValid, int price) {
        if (productNameValid && urlValid && (price != StoreScraper.PRICE_NOT_FOUND)) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        } else {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }



    private String validateUrlLocally(final String url) {
        if (url.isEmpty()) {
            return getString(R.string.error_empty_url_addproductdialog);
        }

        if (!doesStringHaveUrlFormat(url)) {
            return getString(R.string.error_noturl_addproductdialog);
        }

        final String knownStoreForUrl = getKnownStoreForUrl(url);
        if (knownStoreForUrl == null) {
            return getString(R.string.error_unknown_store_addproductdialog);
        }

        return STATUS_URL_VALIDATED_LOCALLY;
    }

    private void validateUrlOnServer(final String url) {
        final StoreScraper.StoreScraperListener storeScraperListener = this;
        final String knownStoreForUrl = getKnownStoreForUrl(url);

        StoreScraperFactory storeScraperFactory = new StoreScraperFactory(getContext());
        StoreScraper storeScraper = storeScraperFactory.getStoreScraper(knownStoreForUrl);

        storeScraper.setStoreScraperListener(storeScraperListener);

        storeScraper.sendAsynchronousRequest(url);


    }

    private void displayStoreFoundConnectionInProgress(String knownStoreUrl) {
        urlInputLayout.setErrorEnabled(false);
        urlInputLayout.setHelperText(getString(R.string.message_store_found_addproductdialog, knownStoreUrl));
        requestProgressBar.setVisibility(View.VISIBLE);
        requestStatusTextView.setVisibility(View.VISIBLE);
        requestStatusTextView.setText(getString(R.string.message_retrieving_price_from_server_addproductdialog));
        requestStatusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorText));
    }

    private void displaySuccessfulResponse(int price, String knownStoreUrl) {
        urlInputLayout.setErrorEnabled(false);
        urlInputLayout.setHelperText(getString(R.string.message_store_found_addproductdialog, knownStoreUrl));
        requestProgressBar.setVisibility(View.GONE);
        requestStatusTextView.setVisibility(View.VISIBLE);
        requestStatusTextView.setText(getString(R.string.message_price_retrieved_addproductdialog, price));
        requestStatusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorText));
    }
    
    private void displaySuccessfulResponseButPriceNotFound(String knownStoreUrl) {
        urlInputLayout.setErrorEnabled(false);
        urlInputLayout.setHelperText(getString(R.string.message_store_found_addproductdialog, knownStoreUrl));
        requestProgressBar.setVisibility(View.GONE);
        requestStatusTextView.setVisibility(View.VISIBLE);
        requestStatusTextView.setText(getString(R.string.error_cannot_extract_price_addproductdialog));
        requestStatusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorError));
    }

    private void displayErrorResponse(String errorText, String knownStoreUrl) {
        urlInputLayout.setErrorEnabled(false);
        urlInputLayout.setHelperText(getString(R.string.message_store_found_addproductdialog, knownStoreUrl));
        requestProgressBar.setVisibility(View.GONE);
        requestStatusTextView.setVisibility(View.VISIBLE);
        requestStatusTextView.setText(getString(R.string.error_cannot_connect_addproductdialog, errorText));
        requestStatusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorError));
    }

    private void displayInvalidUrl(String errorText) {
        urlInputLayout.setHelperText(null);
        urlInputLayout.setErrorEnabled(true);
        urlInputLayout.setError(errorText);
        requestProgressBar.setVisibility(View.GONE);
        requestStatusTextView.setVisibility(View.GONE);
    }


    private String getKnownStoreForUrl(String url) {
        
        ArrayList<String> knownStoreUrls = databaseHelper.getAllStoreUrls();
        for (String knownStoreUrl : knownStoreUrls) {
            if (url.contains(knownStoreUrl)) {
                this.knownStoreUrl = knownStoreUrl;
                return knownStoreUrl;
            }
        }
        return null;
    }

    private boolean doesStringHaveUrlFormat(String input) {
        if (!input.contains(".")) {
            return false;
        }
        if (input.length() < 4) {
            return false;
        }
        return true;
    }

    private boolean isProductNameValid(String productName) {
        if (productName.length() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
