package com.aplicacion.envivoapp.utilidades;

import android.app.Application;
import android.net.Uri;

import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseApp extends Application {


    private Boolean global;
    private String idStreaming;
    private String idPedido;
    private String url;
    private int codigo;
    private int idNotificacion;
    private Vendedor vendedor;
    private Cliente cliente;
    private Uri linkAcceso;
    private Boolean esPrimerMensajeClienteVendedor;

    public Boolean getEsPrimerMensajeClienteVendedor() {
        return esPrimerMensajeClienteVendedor;
    }

    public void setEsPrimerMensajeClienteVendedor(Boolean esPrimerMensajeClienteVendedor) {
        this.esPrimerMensajeClienteVendedor = esPrimerMensajeClienteVendedor;
    }


    public Uri getLinkAcceso() {
        return linkAcceso;
    }

    public void setLinkAcceso(Uri linkAcceso) {
        this.linkAcceso = linkAcceso;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }

    public int getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(int idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(String idPedido) {
        this.idPedido = idPedido;
    }

    public String getIdStreaming() {
        return idStreaming;
    }

    public void setIdStreaming(String idStreaming) {
        this.idStreaming = idStreaming;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getGlobal() {
        return global;
    }

    public void setGlobal(Boolean global) {
        this.global = global;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true); //abilitamos la persistencia de datos
    }
}
