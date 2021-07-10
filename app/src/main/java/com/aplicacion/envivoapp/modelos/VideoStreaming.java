package com.aplicacion.envivoapp.modelos;


import java.util.Date;

public class VideoStreaming {
    private String idVideoStreaming;
    private String urlVideoStreaming;
    private Date FechaTransmision;
    private String idVendedor;

    public VideoStreaming() {
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getIdVideoStreaming() {
        return idVideoStreaming;
    }

    public void setIdVideoStreaming(String idVideoStreaming) {
        this.idVideoStreaming = idVideoStreaming;
    }

    public String getUrlVideoStreaming() {
        return urlVideoStreaming;
    }

    public void setUrlVideoStreaming(String urlVideoStreaming) {
        this.urlVideoStreaming = urlVideoStreaming;
    }

    public Date getFechaTransmision() {
        return FechaTransmision;
    }

    public void setFechaTransmision(Date fechaTransmision) {
        FechaTransmision = fechaTransmision;
    }
}
