package com.aplicacion.envivoapp.utilidades;

import android.app.Application;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseApp extends Application {
    private LatLng latLng = null;

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true); //abilitamos la persistencia de datos
    }
}
