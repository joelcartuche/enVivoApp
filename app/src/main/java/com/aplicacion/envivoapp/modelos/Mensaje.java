package com.aplicacion.envivoapp.modelos;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mensaje {
    private String idMensaje;
    private String texto;
    private Date fecha;
    private Cliente cliente;
    private Vendedor vendedor;
    private String idStreaming;
    private Boolean pedidoAceptado;
    private Boolean pedidoCancelado;
    private Boolean esVededor;
    private Boolean canceloPedido;
    private Boolean cambioPedido;
    private Boolean esGlobal;
    private Boolean esClienteBloqueado;
    private String imagen;
    private String idcliente;
    private String idvendedor;
    private Boolean esEliminado;
    private String idCliente_idVendedor;
    private String idVendedor_idStreaming;
    private Mensaje_Cliente_Vendedor ms_cliente_vendedor;

    public Mensaje() {
        this.pedidoAceptado = false;
        this.pedidoCancelado = false;
        this.esVededor = false;
        this.canceloPedido = false;
        this.cambioPedido = false;
        this.esGlobal = false;
        this.esClienteBloqueado = false;
        this.imagen=null;
        this.esEliminado = false;
    }


    public String getIdvendedor() {
        return idvendedor;
    }

    public void setIdvendedor(String idvendedor) {
        this.idvendedor = idvendedor;
    }

    public String getIdVendedor_idStreaming() {
        return idVendedor_idStreaming;
    }

    public String getIdcliente() {
        return idcliente;
    }

    public void setIdcliente(String idcliente) {
        this.idcliente = idcliente;
    }

    public void setIdVendedor_idStreaming(String idVendedor_idStreaming) {
        this.idVendedor_idStreaming = idVendedor_idStreaming;
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

    public String getIdCliente_idVendedor() {
        return idCliente_idVendedor;
    }

    public void setIdCliente_idVendedor(String idCliente_idVendedor) {
        this.idCliente_idVendedor = idCliente_idVendedor;
    }

    public Boolean getEsEliminado() {
        return esEliminado;
    }

    public void setEsEliminado(Boolean esEliminado) {
        this.esEliminado = esEliminado;
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

}
