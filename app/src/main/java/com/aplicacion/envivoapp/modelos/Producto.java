package com.aplicacion.envivoapp.modelos;

import java.util.Date;

public class Producto {
    private String idProducto;
    private Integer cantidadProducto;//
    private String descripcionProducto;//
    private String codigoProducto;//
    private Double  precioProducto;//
    private String nombreProducto;//
    private String imagen;
    private Boolean esEliminado;
    private String idVendedor;//
    private  String idVendedor_codigoProducto;//


    public String getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(String idProducto) {
        this.idProducto = idProducto;
    }

    public String getIdVendedor_codigoProducto() {
        return idVendedor_codigoProducto;
    }

    public void setIdVendedor_codigoProducto(String idVendedor_codigoProducto) {
        this.idVendedor_codigoProducto = idVendedor_codigoProducto;
    }

    public Boolean getEsEliminado() {
        return esEliminado;
    }

    public void setEsEliminado(Boolean esEliminado) {
        this.esEliminado = esEliminado;
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public Producto() {
        this.esEliminado = false;
    }


    public Integer getCantidadProducto() {
        return cantidadProducto;
    }

    public void setCantidadProducto(Integer cantidadProducto) {
        this.cantidadProducto = cantidadProducto;
    }

    public String getDescripcionProducto() {
        return descripcionProducto;
    }

    public void setDescripcionProducto(String descripcionProducto) {
        this.descripcionProducto = descripcionProducto;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public Double getPrecioProducto() {
        return precioProducto;
    }

    public void setPrecioProducto(Double precioProducto) {
        this.precioProducto = precioProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
