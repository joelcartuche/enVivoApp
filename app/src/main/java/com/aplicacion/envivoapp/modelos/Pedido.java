package com.aplicacion.envivoapp.modelos;

import java.util.Date;

public class Pedido {
    private String idPedido;
    private Integer cantidadProducto;
    private String descripcionProducto;
    private String codigoProducto;
    private Double  precioProducto;
    private String nombreProducto;
    private Date fechaPedido;
    private Boolean aceptado;
    private Boolean cancelado;
    private Boolean pagado;
    private Boolean eliminado;
    private String idVendedor;
    private String idCliente;
    private String idStreaming;
    private String imagen;
    private Producto producto;
    private String idCliente_idVendedor;


    private  Date fechaFinalPedido;

    public Pedido() {
        this.aceptado=false;
        this.cancelado=false;
        this.pagado = false;
        this.eliminado=false;
        this.imagen = null;
    }

    public String getIdCliente_idVendedor() {
        return idCliente_idVendedor;
    }

    public void setIdCliente_idVendedor(String idCliente_idVendedor) {
        this.idCliente_idVendedor = idCliente_idVendedor;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Date getFechaFinalPedido() {
        return fechaFinalPedido;
    }

    public void setFechaFinalPedido(Date fechaFinalPedido) {
        this.fechaFinalPedido = fechaFinalPedido;
    }

    public Boolean getPagado() {
        return pagado;
    }

    public void setPagado(Boolean pagado) {
        this.pagado = pagado;
    }

    public Boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }

    public String getIdStreaming() {
        return idStreaming;
    }

    public void setIdStreaming(String idStreaming) {
        this.idStreaming = idStreaming;
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public Date getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(Date fechaPedido) {
        this.fechaPedido = fechaPedido;
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

    public double getPrecioProducto() {
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

    public String getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(String idPedido) {
        this.idPedido = idPedido;
    }

    public Boolean getAceptado() {
        return aceptado;
    }

    public void setAceptado(Boolean aceptado) {
        this.aceptado = aceptado;
    }

    public Boolean getCancelado() {
        return cancelado;
    }

    public void setCancelado(Boolean cancelado) {
        this.cancelado = cancelado;
    }
}
