package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos.GestionProducto;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Producto;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class FragmentoIngresarProductoMasivo extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos

    Button btnSubirCsvIngresarProductoMasivo;
    TextView tvConfirmacionCsvIngresarProductoMasivo;
    Button btnSeleccionarCsvIngresarProductoMasivo;
    Uri archivoSeleccionado;
    private Vendedor vendedorGlobal;
    private Map<String,Object> datosSubir = new HashMap<>();
    Dialog dialogCargando ;
    Dialog dialogSucces;


    private EncriptacionDatos encrypt= new EncriptacionDatos();
    private int VALOR_RETORNO = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragmento_ingresar_producto_masivo, container, false);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage

        vendedorGlobal =  ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor();
        btnSubirCsvIngresarProductoMasivo = root.findViewById(R.id.btnSubirCsvIngresarProductoMasivo);
        tvConfirmacionCsvIngresarProductoMasivo = root.findViewById(R.id.tvConfirmacionCsvIngresarProductoMasivo);
        btnSeleccionarCsvIngresarProductoMasivo = root.findViewById(R.id.btnSeleccionarCsvIngresarProductoMasivo);

        Utilidades util = new Utilidades();
        dialogCargando = util.dialogCargar(getContext()); //cargamos el cuadro de dialogo
        dialogSucces = util.dialogSuccess(getContext()); //cargamos el cuadro de dialogo


        //ocultamos el text view y el boton
        tvConfirmacionCsvIngresarProductoMasivo.setVisibility(View.GONE);
        btnSubirCsvIngresarProductoMasivo.setVisibility(View.GONE);


        btnSeleccionarCsvIngresarProductoMasivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarArchivo();
            }
        });
        btnSubirCsvIngresarProductoMasivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (archivoSeleccionado!=null){
                    subirProductos();
                }else{
                    Toast.makeText(getContext(),"No selecciono ningun archivo",Toast.LENGTH_LONG).show();
                }
            }
        });


        return root;
    }


    private void subirProductos() {

        databaseReference.updateChildren(datosSubir).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mostrarSucces();
                tvConfirmacionCsvIngresarProductoMasivo.setVisibility(View.GONE);
                btnSubirCsvIngresarProductoMasivo.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),"Error al subir archivos",Toast.LENGTH_LONG).show();
                tvConfirmacionCsvIngresarProductoMasivo.setVisibility(View.GONE);
                btnSubirCsvIngresarProductoMasivo.setVisibility(View.GONE);
            }
        });
    }

    private  void mostrarSucces(){
        dialogSucces.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                dialogSucces.dismiss();
            }
        }, 2000);
    }



    private void seleccionarArchivo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(Intent.createChooser(intent, "Choose File"), VALOR_RETORNO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getContext(),"No selecciono ningun archivo",Toast.LENGTH_LONG).show();
            tvConfirmacionCsvIngresarProductoMasivo.setVisibility(View.GONE);
            btnSubirCsvIngresarProductoMasivo.setVisibility(View.GONE);
        }
        if ((resultCode == RESULT_OK) && (requestCode == VALOR_RETORNO )) {
            //Procesar el resultado
            Uri uri = data.getData(); //obtener el uri content

            if (uri!=null){
                archivoSeleccionado = uri;
                if(cargarLista()){//cargamos y leemos los datos en caso de no exitir errores
                    //una ves recogido el archivo mostramos el text view y el boton
                    tvConfirmacionCsvIngresarProductoMasivo.setVisibility(View.VISIBLE);
                    btnSubirCsvIngresarProductoMasivo.setVisibility(View.VISIBLE);
                    tvConfirmacionCsvIngresarProductoMasivo.setText("Usted a seleccionado el archivo '"+uri.getPath()+"'");
                }else{
                    Toast.makeText(getContext(),"Error al leer el archivo intentelo de nuevo",Toast.LENGTH_LONG).show();
                    tvConfirmacionCsvIngresarProductoMasivo.setVisibility(View.GONE);
                    btnSubirCsvIngresarProductoMasivo.setVisibility(View.GONE);
                    datosSubir = new HashMap<>(); //reinicamos el mapeo
                }

            }else{
                Toast.makeText(getContext(),"Error al leer el archivo intentelo de nuevo",Toast.LENGTH_LONG).show();
                tvConfirmacionCsvIngresarProductoMasivo.setVisibility(View.GONE);
                btnSubirCsvIngresarProductoMasivo.setVisibility(View.GONE);
            }
        }
    }

    private Boolean cargarLista() {

//inicio
        InputStream inputStream;
        File file = null;
        try {
        if (archivoSeleccionado.getScheme().equals("file")) {
            file = new File(archivoSeleccionado.toString());

            inputStream = new FileInputStream(file);
        } else {
            inputStream = getActivity().getContentResolver().openInputStream(archivoSeleccionado);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String fila = null;
        int cont = 0; //para controlar que no se lea la cabecera
        dialogCargando.show();//mostamos el cuadro de dialogo cargando
        while((fila = br.readLine()) != null) {
            if (cont >0){ //en caso de que se  lea la cabecera del archivo

                String [] datoProducto = fila.split(";");//separamos los datos del archivo csv seleccionado
                Producto productoIngresado= new Producto();
                productoIngresado.setIdProducto(vendedorGlobal.getIdVendedor()+"_"+datoProducto[0]);
                productoIngresado.setCodigoProducto(encrypt.encriptar(datoProducto[0]));
                productoIngresado.setNombreProducto(encrypt.encriptar(datoProducto[1]));
                productoIngresado.setCantidadProducto(Integer.parseInt(datoProducto[2]));
                productoIngresado.setPrecioProducto(Double.parseDouble(datoProducto[3]));
                productoIngresado.setDescripcionProducto(encrypt.encriptar(datoProducto[4]));
                productoIngresado.setIdVendedor(vendedorGlobal.getIdVendedor());
                productoIngresado.setIdVendedor_codigoProducto(vendedorGlobal.getIdVendedor()+"_"+datoProducto[0]);

                //cargamos los datos en el mapa para subirlos
                String codigoInicial="Producto/"+productoIngresado.getIdProducto();
                datosSubir.put(codigoInicial
                                +"/idProducto",
                        productoIngresado.getIdProducto());
                datosSubir.put(codigoInicial
                                +"/codigoProducto",
                        productoIngresado.getCodigoProducto());

                datosSubir.put(codigoInicial
                                +"/nombreProducto",
                        productoIngresado.getNombreProducto());

                datosSubir.put(codigoInicial
                                +"/cantidadProducto",
                        productoIngresado.getCantidadProducto());

                datosSubir.put(codigoInicial
                                +"/precioProducto",
                        productoIngresado.getPrecioProducto());

                datosSubir.put(codigoInicial
                                +"/descripcionProducto",
                        productoIngresado.getDescripcionProducto());

                datosSubir.put(codigoInicial
                                +"/idVendedor",
                        productoIngresado.getIdVendedor());

                datosSubir.put(codigoInicial
                                +"/idVendedor_codigoProducto",
                        productoIngresado.getIdVendedor_codigoProducto());

                datosSubir.put(codigoInicial
                                +"/esEliminado",
                        productoIngresado.getEsEliminado());
            }
            cont++;
        }

        dialogCargando.dismiss();
        br.close();
        return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //fin



        return false;
    }
}