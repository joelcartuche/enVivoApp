package com.aplicacion.envivoapp.modelos;

import java.util.Date;

public class Mensaje {
    private String idMensaje;
    private String texto;
    private Date fecha;
    private String idcliente;
    private String idvendedor;
    private String idStreaming;
    private Boolean pedidoAceptado;
    private Boolean pedidoCancelado;
    private Boolean esVededor;
    private Boolean canceloPedido;
    private Boolean cambioPedido;
    private Boolean esGlobal;
    private Boolean esClienteBloqueado;
    private String imagen;

    public Mensaje() {
        this.pedidoAceptado = false;
        this.pedidoCancelado = false;
        this.esVededor = false;
        this.canceloPedido = false;
        this.cambioPedido = false;
        this.esGlobal = false;
        this.esClienteBloqueado = false;
        this.imagen=null;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Boolean getEsClienteBloqueado() {
        return esClienteBloqueado;
    }

    public void setEsClienteBloqueado(Boolean esClienteBloqueado) {
        this.esClienteBloqueado = esClienteBloqueado;
    }

    public Boolean getEsGlobal() {
        return esGlobal;
    }

    public void setEsGlobal(Boolean esGlobal) {
        this.esGlobal = esGlobal;
    }

    public Boolean getCanceloPedido() {
        return canceloPedido;
    }

    public void setCanceloPedido(Boolean canceloPedido) {
        this.canceloPedido = canceloPedido;
    }

    public Boolean getCambioPedido() {
        return cambioPedido;
    }

    public void setCambioPedido(Boolean cambioPedido) {
        this.cambioPedido = cambioPedido;
    }

    public Boolean getEsVededor() {
        return esVededor;
    }

    public void setEsVededor(Boolean esVededor) {
        this.esVededor = esVededor;
    }

    public Boolean getPedidoCancelado() {
        return pedidoCancelado;
    }

    public void setPedidoCancelado(Boolean pedidoCancelado) {
        this.pedidoCancelado = pedidoCancelado;
    }

    public Boolean getPedidoAceptado() {
        return pedidoAceptado;
    }

    public void setPedidoAceptado(Boolean pedidoAceptado) {
        this.pedidoAceptado = pedidoAceptado;
    }

    public String getIdStreaming() {
        return idStreaming;
    }

    public void setIdStreaming(String idStreaming) {
        this.idStreaming = idStreaming;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(String idMensaje) {
        this.idMensaje = idMensaje;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getIdcliente() {
        return idcliente;
    }

    public void setIdcliente(String idcliente) {
        this.idcliente = idcliente;
    }

    public String getIdvendedor() {
        return idvendedor;
    }

    public void setIdvendedor(String idvendedor) {
        this.idvendedor = idvendedor;
    }
}
