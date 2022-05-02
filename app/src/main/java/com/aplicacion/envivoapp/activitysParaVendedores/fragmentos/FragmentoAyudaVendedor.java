package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.aplicacion.envivoapp.R;


public class FragmentoAyudaVendedor extends Fragment {
    private WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragmento_ayuda_vendedor, container, false);
        webView = root.findViewById(R.id.webAyudaVendedor);
        String url = "https://sites.google.com/unl.edu.ec/centrodeayudavendedor/inicio";
        webView.loadUrl(url);
        return root;
    }
}