package com.aplicacion.envivoapp.modelos;

public class Calificaciones {
    private String idCalificaciones;
    private String idCliente;
    private Vendedor vendedor;
    private Boolean esCalificacionBuena;
    private Boolean esNuevo;
    private String idCliente_esNuevo;

    public Calificaciones() {
    }

    public String getIdCliente_esNuevo() {
        return idCliente_esNuevo;
    }

    public void setIdCliente_esNuevo(String idCliente_esNuevo) {
        this.idCliente_esNuevo = idCliente_esNuevo;
    }

    public Boolean getEsNuevo() {
        return esNuevo;
    }

    public void setEsNuevo(Boolean esNuevo) {
        this.esNuevo = esNuevo;
    }

    public String getIdCalificaciones() {
        return idCalificaciones;
    }

    public void setIdCalificaciones(String idCalificaciones) {
        this.idCalificaciones = idCalificaciones;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }

    public Boolean getEsCalificacionBuena() {
        return esCalificacionBuena;
    }

    public void setEsCalificacionBuena(Boolean esCalificacionBuena) {
        this.esCalificacionBuena = esCalificacionBuena;
    }
}
