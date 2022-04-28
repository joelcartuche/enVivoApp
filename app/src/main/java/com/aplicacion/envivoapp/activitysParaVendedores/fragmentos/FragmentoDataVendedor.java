package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.BreakIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FragmentoDataVendedor extends Fragment {

    private FirebaseAuth firebaseAuth;
    private EditText nombre,cedula,celular,telefono,diasEsperaCancelacion;
    private CheckBox tieneTienda;
    private Button guardar;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();
    private Vendedor vendedorGlobal;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_data_vendedor, container, false);
        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos

        nombre = root.findViewById(R.id.txtNombre);
        cedula = root.findViewById(R.id.txtCedula);
        celular = root.findViewById(R.id.txtCelular);
        telefono = root.findViewById(R.id.txtTelefono);
        diasEsperaCancelacion = root.findViewById(R.id.txtNumeroDiasCancelacion);
        guardar = root.findViewById(R.id.btnGuardar);

        vendedorGlobal = ((MyFirebaseApp) getContext().getApplicationContext()).getVendedor();
        if (vendedorGlobal ==null){
            guardar();
        }else {
            /*Log.d("DATA","nombre:"+vendedorGlobal.getNombre()+
                    "\n cedula :"+vendedorGlobal.getCedula()+
                    "\n celular: "+vendedorGlobal.getCelular()+
                    "\n telefono: "+vendedorGlobal.getTelefono());*/
            try {
                nombre.setText(encriptacionDatos.desencriptar(vendedorGlobal.getNombre()));
                cedula.setText(encriptacionDatos.desencriptar(vendedorGlobal.getCedula()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                celular.setText(encriptacionDatos.desencriptar(vendedorGlobal.getCelular()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                telefono.setText(encriptacionDatos.desencriptar(vendedorGlobal.getTelefono()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            diasEsperaCancelacion.setText(vendedorGlobal.getDiasEperaCancelacion()+"");
            actualizar(vendedorGlobal);
        }

        return root;
    }

    private void guardar(){
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()){
                    Vendedor vendedor = new Vendedor();
                    vendedor.setIdVendedor(UUID.randomUUID().toString());
                    try {
                        vendedor.setNombre(encriptacionDatos.encriptar(nombre.getText().toString()));
                        vendedor.setCedula(encriptacionDatos.encriptar(cedula.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        vendedor.setCelular(encriptacionDatos.encriptar(celular.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        vendedor.setTelefono(encriptacionDatos.encriptar(telefono.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    vendedor.setDiasEperaCancelacion(Integer.parseInt(diasEsperaCancelacion.getText().toString()));
                    vendedor.setUidUsuario(firebaseAuth.getCurrentUser().getUid());
                    databaseReference.child("Vendedor").child(vendedor.getIdVendedor()).setValue(vendedor).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {//evaluamos si los datos se guardaron satisfactoriamente

                            Toast.makeText(getContext(), "Los datos se han guardado correctamente", Toast.LENGTH_LONG).show();
                            Fragment fragment =  new FragmentoHomeVendedor();
                            getActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
                                    .replace(R.id.home_content_vendedor, fragment)
                                    .commit();
                        }
                    }).addOnFailureListener(new OnFailureListener() { //evaluamos si a ocurrido algun error al guardar los datos
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "No se a podido guardar los datos", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void actualizar(Vendedor vendedor){
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()){
                    try {
                        vendedor.setNombre(encriptacionDatos.encriptar(nombre.getText().toString()));
                        vendedor.setCedula(encriptacionDatos.encriptar(cedula.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        vendedor.setCelular(encriptacionDatos.encriptar(celular.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        vendedor.setTelefono(encriptacionDatos.encriptar(telefono.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    vendedor.setDiasEperaCancelacion(Integer.parseInt(diasEsperaCancelacion.getText().toString()));
                    vendedor.setUidUsuario(firebaseAuth.getCurrentUser().getUid());

                    Map<String, Object> map = new HashMap<>();
                    map.put("nombre",vendedor.getNombre());
                    map.put("cedula",vendedor.getCedula());
                    if (vendedor.getCelular()!=null) {
                        map.put("celular", vendedor.getCelular());
                    }
                    if(vendedor.getTelefono()!=null) {
                        map.put("telefono", vendedor.getTelefono());
                    }
                    map.put("diasEperaCancelacion",vendedor.getDiasEperaCancelacion());
                    databaseReference.child("Vendedor").child(vendedor.getIdVendedor()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(),"Datos actualizados con éxito",Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(),"Error al actualizar los datos intentelo de nuevo",Toast.LENGTH_LONG).show();
                        }
                    });


                }
            }
        });
    }

    private Boolean validarCampos(){
        if (nombre.getText().toString().isEmpty()){
            nombre.setError("Ingrese el nombre");
        }else if (cedula.getText().toString().isEmpty()){
            cedula.setError("Ingrese el numero de cedula");
        }else if (celular.getText().toString().isEmpty()
                &&telefono.getText().toString().isEmpty()){
            celular.setError("Ingrese el un telefono o celular");
        }else if (!validarCedula(cedula.getText().toString())){
            cedula.setError("Cédula no válida");
        }else if (!validarTelefono()){
        }else if (!validarCelular()){
        }else{
            return true;
        }
        return  false;
    }
    public Boolean validarTelefono(){
        if (!telefono.getText().toString().isEmpty()) {
            if (telefono.getText().toString().length() != 8) {
                if (telefono.getText().toString().length() != 9) {
                    telefono.setError("Teléfono no válido");
                    return false;
                }
            } else if (telefono.getText().toString().length() != 9) {
                if (telefono.getText().toString().length() != 8) {
                    telefono.setError("Teléfono no válido");
                    return false;
                }
            }
        }
        return true;
    }

    public Boolean validarCelular(){
        if (!celular.getText().toString().isEmpty()){
            if (celular.getText().toString().length() !=10){
                celular.setError("Celular no válido");
                return false;
            }
        }
        return true;
    }
    public static boolean validarCedula(String x) {
        int suma = 0;
        if (x.length() == 9) {
            System.out.println("Ingrese su cedula de 10 digitos");
            return false;
        } else {
            int a[] = new int[x.length() / 2];
            int b[] = new int[(x.length() / 2)];
            int c = 0;
            int d = 1;
            for (int i = 0; i < x.length() / 2; i++) {
                a[i] = Integer.parseInt(String.valueOf(x.charAt(c)));
                c = c + 2;
                if (i < (x.length() / 2) - 1) {
                    b[i] = Integer.parseInt(String.valueOf(x.charAt(d)));
                    d = d + 2;
                }
            }

            for (int i = 0; i < a.length; i++) {
                a[i] = a[i] * 2;
                if (a[i] > 9) {
                    a[i] = a[i] - 9;
                }
                suma = suma + a[i] + b[i];
            }
            int aux = suma / 10;
            int dec = (aux + 1) * 10;
            if ((dec - suma) == Integer.parseInt(String.valueOf(x.charAt(x.length() - 1))))
                return true;
            else if (suma % 10 == 0 && x.charAt(x.length() - 1) == '0') {
                return true;
            } else {
                return false;
            }

        }
    }
}
