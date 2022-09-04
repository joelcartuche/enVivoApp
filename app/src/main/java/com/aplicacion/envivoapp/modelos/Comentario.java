package com.aplicacion.envivoapp.modelos;

public class Comentario {

    private String idComentario ;
    private Cliente cliente;
    private Vendedor vendedor;
    private Boolean esDenuncia;
    private String mensaje;

    public Comentario() {

    }

    public String getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(String idComentario) {
        this.idComentario = idComentario;
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

    public Boolean getEsDenuncia() {
        return esDenuncia;
    }

    public void setEsDenuncia(Boolean esDenuncia) {
        this.esDenuncia = esDenuncia;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
