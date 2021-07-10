package com.aplicacion.envivoapp.modelos;

public class Vendedor {
    private String idVendedor;
    private String nombre;
    private String cedula;
    private String celular;
    private String telefono;
    private Integer diasEperaCancelacion;
    private boolean tieneTienda;
    private String uidUsuario;

    public Vendedor() {
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Integer getDiasEperaCancelacion() {
        return diasEperaCancelacion;
    }

    public void setDiasEperaCancelacion(Integer diasEperaCancelacion) {
        this.diasEperaCancelacion = diasEperaCancelacion;
    }

    public boolean isTieneTienda() {
        return tieneTienda;
    }

    public void setTieneTienda(boolean tieneTienda) {
        this.tieneTienda = tieneTienda;
    }

    public String getUidUsuario() {
        return uidUsuario;
    }

    public void setUidUsuario(String uidUsuario) {
        this.uidUsuario = uidUsuario;
    }
}
