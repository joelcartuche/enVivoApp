package com.aplicacion.envivoapp.activityParaClientes.fragmentos;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.aplicacion.envivoapp.R;


public class FragmentoAyudaCliente extends Fragment {
    private WebView webView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_fragmento_ayuda_cliente, container, false);
        webView = root.findViewById(R.id.webAyudaCliente);
        String url = "https://sites.google.com/unl.edu.ec/centro-de-ayuda/inicio";
        webView.loadUrl(url);
        return root ;
    }
}