package com.aplicacion.envivoapp.utilidades;

import android.app.Application;

import com.google.android.gms.maps.model.LatLng;

public class MyApplication extends Application {

    private LatLng latLng;

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
