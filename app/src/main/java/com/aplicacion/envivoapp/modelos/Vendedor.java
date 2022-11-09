package com.aplicacion.envivoapp.modelos;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Vendedor {
    private String idVendedor;
    private String nombre;
    private String cedula;
    private String celular;
    private String telefono;
    private Integer diasEperaCancelacion;
    private String uidUsuario;
    private int numCalificacionesBuenas;
    private int numCalificacionesMalas;



    public Vendedor() {
        this.numCalificacionesBuenas =0;
        this.numCalificacionesMalas =0;
    }


    public int getNumCalificacionesBuenas() {
        return numCalificacionesBuenas;
    }

    public void setNumCalificacionesBuenas(int numCalificacionesBuenas) {
        this.numCalificacionesBuenas = numCalificacionesBuenas;
    }

    public int getNumCalificacionesMalas() {
        return numCalificacionesMalas;
    }

    public void setNumCalificacionesMalas(int numCalificacionesMalas) {
        this.numCalificacionesMalas = numCalificacionesMalas;
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

    public String getUidUsuario() {
        return uidUsuario;
    }

    public void setUidUsuario(String uidUsuario) {
        this.uidUsuario = uidUsuario;
    }

    @NonNull
    @Override
    public String toString() {
        return nombre;
    }
}
