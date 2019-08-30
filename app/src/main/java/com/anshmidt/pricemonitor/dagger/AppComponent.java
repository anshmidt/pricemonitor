package com.anshmidt.pricemonitor.dagger;

import com.anshmidt.pricemonitor.ServerRequestsWorker;
import com.anshmidt.pricemonitor.activities.MainActivity;
import com.anshmidt.pricemonitor.dialogs.AddProductDialogFragment;
import com.anshmidt.pricemonitor.dialogs.ProductSettingsBottomSheetFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, RoomModule.class})
public interface AppComponent {
    void inject(MainActivity mainActivity);
    void inject(ServerRequestsWorker serverRequestsWorker);
    void inject(AddProductDialogFragment addProductDialogFragment);
    void inject(ProductSettingsBottomSheetFragment productSettingsBottomSheetFragment);
}
