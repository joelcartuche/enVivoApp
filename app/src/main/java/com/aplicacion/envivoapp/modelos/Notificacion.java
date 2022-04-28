package com.aplicacion.envivoapp.modelos;

public class Notificacion {
    private String idNotificacion;
    private String idCliente;
    private String idVendedor;
    private String idPedido;
    private int codigoNotificacion;
    private Boolean esNuevo;

    public String getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(String idPedido) {
        this.idPedido = idPedido;
    }

    public Notificacion() {
        esNuevo = false;
    }

    public Boolean getEsNuevo() {
        return esNuevo;
    }

    public void setEsNuevo(Boolean esNuevo) {
        this.esNuevo = esNuevo;
    }

    public String getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(String idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public int getCodigoNotificacion() {
        return codigoNotificacion;
    }

    public void setCodigoNotificacion(int codigoNotificacion) {
        this.codigoNotificacion = codigoNotificacion;
    }
}
