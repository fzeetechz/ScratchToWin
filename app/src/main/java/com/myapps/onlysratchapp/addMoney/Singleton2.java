package com.myapps.onlysratchapp.addMoney;

import androidx.annotation.NonNull;

import dev.shreyaspatil.easyupipayment.listener.PaymentStatusListener;


public class Singleton2 {
    private static Singleton2 instance = null;

    //private PaymentStatusListener listener;
    private PaymentStatusListener listener;

    public static Singleton2 getInstance() {
        if(instance == null) {
            instance = new Singleton2();
        }
        return instance;
    }

    @NonNull
    public PaymentStatusListener getListener() {
        return instance.listener;
    }

    void setListener(@NonNull PaymentStatusListener listener) {
        instance.listener = listener;
    }

    public void detachListener() {
        instance.listener = null;
    }

    public boolean isListenerRegistered() {
        return (instance.listener != null);
    }
}
