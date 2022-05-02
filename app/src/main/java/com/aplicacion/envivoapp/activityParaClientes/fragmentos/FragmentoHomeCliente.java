package com.aplicacion.envivoapp.activityParaClientes.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FragmentoHomeCliente extends Fragment implements View.OnClickListener{

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private Button streaming,pedido,chat;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_home_cliente, container, false);


        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        streaming = root.findViewById(R.id.btnStreamingsHomeComprador);
        pedido = root.findViewById(R.id.btnPedidosHomeComprador);
        chat =  root.findViewById(R.id.btnMensajeriaHomeComprador);

        streaming.setOnClickListener(this::onClick);
        pedido.setOnClickListener(this::onClick);
        chat.setOnClickListener(this::onClick);

        return root;
    }

    @Override
    public void onClick(View v) {
        Button b = (Button) v;
        Fragment fragment = null;
        switch (b.getId()){
            case R.id.btnStreamingsHomeComprador:
                ((MyFirebaseApp) getActivity().getApplicationContext()).setGlobal(false);
                fragment = new FragmentoListarVendedores();
                break;
            case R.id.btnMensajeriaHomeComprador:
                ((MyFirebaseApp) getActivity().getApplicationContext()).setGlobal(true);
                fragment = new FragmentoListarVendedores();
                break;
            case R.id.btnPedidosHomeComprador:
                fragment = new FragmentoPedidoCliente();
                break;

            default:
                throw new IllegalArgumentException("menu option not implemented!!");

        }
        if (fragment!=null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                    .replace(R.id.home_content, fragment)
                    .commit();
        }
    }
}
