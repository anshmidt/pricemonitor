package com.anshmidt.pricemonitor.dagger;

import com.anshmidt.pricemonitor.ServerRequestsWorker;
import com.anshmidt.pricemonitor.activities.MainActivity;
import com.anshmidt.pricemonitor.dialogs.AddItemDialogFragment;
import com.anshmidt.pricemonitor.dialogs.ProductSettingsBottomSheetFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(MainActivity mainActivity);
    void inject(ServerRequestsWorker serverRequestsWorker);
    void inject(AddItemDialogFragment addItemDialogFragment);
    void inject(ProductSettingsBottomSheetFragment productSettingsBottomSheetFragment);
}
