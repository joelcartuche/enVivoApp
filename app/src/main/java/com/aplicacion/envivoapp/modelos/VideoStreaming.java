package com.aplicacion.envivoapp.modelos;


import java.util.Date;

public class VideoStreaming {
    private String idVideoStreaming;
    private String urlVideoStreaming;
    private Date FechaTransmision;
    private String idVendedor;
    private Boolean eliminado;
    private Boolean iniciado;

    public VideoStreaming() {
        this.eliminado=false;
        this.iniciado = false;
    }

    public Boolean getIniciado() {
        return iniciado;
    }

    public void setIniciado(Boolean iniciado) {
        this.iniciado = iniciado;
    }

    public Boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
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

    @Override
    public String toString() {
        return FechaTransmision.getDate()+" / "
                +FechaTransmision.getMonth()+" / "
                +FechaTransmision.getYear();
    }
}
