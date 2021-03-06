package com.yooiistudios.newskit.iab;

import android.content.Intent;

/**
 * Created by Dongheyon Jeong in News-Kit from Yooii Studios Co., LTD. on 15. 5. 1.
 *
 * IabManager
 *  In-App-Billing 이 공통적으로 제공하는 인터페이스를 추상화
 */
public abstract class IabManager {
    public abstract void setup();
    public abstract void dispose();
    public abstract void purchase(String sku);
    public abstract boolean handleActivityResult(int requestCode, int resultCode, Intent data);
    public abstract boolean isHelperDisposed();

    protected static class IabDetailNotFoundException extends Exception {
        public IabDetailNotFoundException() {
            super("Some product detail not found.");
        }
    }
}
