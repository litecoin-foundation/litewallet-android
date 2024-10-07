package com.breadwallet.presenter.entities;

import java.io.Serializable;

public class CurrencyEntity implements Serializable {

    //Change this after modifying the class
    private static final long serialVersionUID = 7526472295622776147L;

    public static final String TAG = CurrencyEntity.class.getName();
    public String code;
    public String name;
    public float rate;

    public CurrencyEntity(String code, String name, float rate) {
        this.code = code;
        this.name = name;
        this.rate = rate;
    }

    public CurrencyEntity() {
    }
}
