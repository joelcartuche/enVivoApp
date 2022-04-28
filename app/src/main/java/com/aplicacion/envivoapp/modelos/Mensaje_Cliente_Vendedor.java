package com.aplicacion.envivoapp.modelos;

public class Mensaje_Cliente_Vendedor {
    Cliente cliente;
    Vendedor vendedor;
    Boolean elVendedorBloqueoCliente;
    String idCliente_idVendedor;


    public Mensaje_Cliente_Vendedor() {
        elVendedorBloqueoCliente = false;
    }

    public Boolean getElVendedorBloqueoCliente() {
        return elVendedorBloqueoCliente;
    }

    public void setElVendedorBloqueoCliente(Boolean elVendedorBloqueoCliente) {
        this.elVendedorBloqueoCliente = elVendedorBloqueoCliente;
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

    @Override
    public String toString() {
        return "Mensaje_Cliente_Vendedor{" +
                ", cliente=" + cliente +
                ", vendedor=" + vendedor +
                ", idCliente_idVendedor='" + idCliente_idVendedor + '\'' +
                '}';
    }
}
