package com.anshmidt.pricemonitor;

import android.util.Log;

import org.junit.Test;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RxTest {


    @Test
    public void testRx() {
        Observable<String> observable = Observable.fromArray(new String[]{"one", "two", "three"});
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onNext(String s) {
                System.out.println("onNext: " + s + " in thread " + Thread.currentThread());
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("onError: " + e);
            }

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };

        observable
                .subscribeOn(Schedulers.io())
                .subscribe(observer);
    }
}
