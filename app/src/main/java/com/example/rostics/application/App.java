package com.example.rostics.application;

import com.yandex.mapkit.MapKitFactory;

public class App extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Сначала ставим ключ
        MapKitFactory.setApiKey("6917afe6-7ea1-4d46-8953-f63f222168bd");
        // Потом инициализируем
        MapKitFactory.initialize(this);
    }
}

