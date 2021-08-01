package com.aplicacion.envivoapp.modelos;

import android.net.Uri;

public class Usuario {
    private String email;
    private String uidUser;
    private String imagen;
    private Boolean esVendedor;

    public Usuario() {
    }


    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getUidUser() {
        return uidUser;
    }

    public void setUidUser(String uidUser) {
        this.uidUser = uidUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEsVendedor() {
        return esVendedor;
    }

    public void setEsVendedor(Boolean esVendedor) {
        this.esVendedor = esVendedor;
    }

    @Override
    public String toString() {
        return "usuario{" +
                "token='" + uidUser + '\'' +
                '}';
    }
}
