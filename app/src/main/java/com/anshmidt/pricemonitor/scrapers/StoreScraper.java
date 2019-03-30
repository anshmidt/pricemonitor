package com.anshmidt.pricemonitor.scrapers;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class StoreScraper {

    public interface StoreScraperListener {
        void onResponseFromServer(int price, String itemUrl);
        void onErrorResponseFromServer(VolleyError error);
    }


    Context context;
    StoreScraperListener storeScraperListener;
    public final String storeUrl;
    public final String storeTitle;
    private final String LOG_TAG = this.getClass().getSimpleName();
    public static final int PRICE_NOT_FOUND = 0;

    public StoreScraper(Context context, String storeUrl, String storeTitle) {
        this.context = context;
        this.storeUrl = storeUrl;
        this.storeTitle = storeTitle;
    }

    public void setStoreScraperListener(StoreScraperListener storeScraperListener) {
        this.storeScraperListener = storeScraperListener;
    }

    public abstract int extractPriceFromFullResponse(String fullResponse);


    public void sendAsynchronousRequest(final String itemUrl) {
        RequestQueue queue = Volley.newRequestQueue(context);
        Log.d(LOG_TAG, "sending request to url: " + itemUrl);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, itemUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        int price = extractPriceFromFullResponse(response);
                        storeScraperListener.onResponseFromServer(price, itemUrl);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        storeScraperListener.onErrorResponseFromServer(error);
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public int sendSynchronousRequest(final String itemUrl) {
        String response = null;
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, itemUrl, future, future);
        requestQueue.add(stringRequest);

        try {
            final int MAX_RESPONSE_TIMEOUT = 30;
            response = future.get(MAX_RESPONSE_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.d(LOG_TAG, e.toString());
        } catch (ExecutionException e) {
            Log.d(LOG_TAG, e.toString());
        } catch (TimeoutException e) {
            Log.d(LOG_TAG, e.toString());
        }

        int price = extractPriceFromFullResponse(response);
        return price;
    }

    protected int extractPriceByCssSelector(String fullResponse, String cssSelector) {
        Document doc = Jsoup.parse(fullResponse);

        Elements priceSpans = doc.select(cssSelector);
        //<span class="one-buy-price">52 599 руб</span>

        for (Element priceSpan : priceSpans) {
            String resultString = priceSpan.text();
            String resultStringWithoutOddChars = resultString.replaceAll("[^0-9]", "");
            return Integer.parseInt(resultStringWithoutOddChars);
        }
        return PRICE_NOT_FOUND;
    }




}
