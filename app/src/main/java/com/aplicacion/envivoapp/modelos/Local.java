package com.aplicacion.envivoapp.modelos;

import com.google.android.gms.maps.model.LatLng;

public class Local {
    String idLocal;
    String Nombre;
    String CallePrincipal;
    String CalleSecundaria;
    String Referencia;
    Double Latitud;
    Double Longitud;
    String Telefono;
    String Celular;
    String idVendedor;

    public Local() {
    }

    public String getCallePrincipal() {
        return CallePrincipal;
    }

    public void setCallePrincipal(String callePrincipal) {
        CallePrincipal = callePrincipal;
    }

    public String getCalleSecundaria() {
        return CalleSecundaria;
    }

    public void setCalleSecundaria(String calleSecundaria) {
        CalleSecundaria = calleSecundaria;
    }

    public String getReferencia() {
        return Referencia;
    }

    public void setReferencia(String referencia) {
        Referencia = referencia;
    }

    public Double getLatitud() {
        return Latitud;
    }

    public Double getLongitud() {
        return Longitud;
    }

    public void setLatitud(double latitud){
        Latitud= latitud;
    }
    public void  setLongitud(double longitud){
        Longitud = longitud;
    }


    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(String idLocal) {
        this.idLocal = idLocal;
    }

    public String getTelefono() {
        return Telefono;
    }

    public void setTelefono(String telefono) {
        Telefono = telefono;
    }

    public String getCelular() {
        return Celular;
    }

    public void setCelular(String celular) {
        Celular = celular;
    }
}
