package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FragmentoHomeVendedor extends Fragment implements  View.OnClickListener{


    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private Button local,pedidos,generarReportes,videoStreamings;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_home_vendedor, container, false);
        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        local = root.findViewById(R.id.btnLocalHomeVendedor);
        pedidos = root.findViewById(R.id.btnPedidosHomeVendedor);
        generarReportes = root.findViewById(R.id.btnReportesHomeVendedor);
        videoStreamings = root.findViewById(R.id.btnStreamingsHomeVendedor);

        local.setOnClickListener(FragmentoHomeVendedor.this::onClick);
        pedidos.setOnClickListener(FragmentoHomeVendedor.this::onClick);
        generarReportes.setOnClickListener(FragmentoHomeVendedor.this::onClick);
        videoStreamings.setOnClickListener(FragmentoHomeVendedor.this::onClick);

        return root;
    }

    @Override
    public void onClick(View view) {
        Button b = (Button)view;
        Fragment fragment = null;
        switch (b.getId()){
            case R.id.btnLocalHomeVendedor:
                fragment = new FragmentoDataLocal();
                break;
            case R.id.btnPedidosHomeVendedor:
                fragment = new FragmentoPedidoVendedor();
                break;
            case R.id.btnReportesHomeVendedor:
                fragment = new FragmentoReporte();
                break;
            case R.id.btnStreamingsHomeVendedor:
                fragment = new FragmentoGestionVideos();
                break;
            default:
                throw new IllegalArgumentException("menu option not implemented!!");
        }
        if (fragment!=null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.home_content_vendedor, fragment)
                    .commit();
        }
    }
}
