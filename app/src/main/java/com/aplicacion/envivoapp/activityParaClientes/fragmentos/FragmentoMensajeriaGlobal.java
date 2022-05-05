package com.aplicacion.envivoapp.activityParaClientes.fragmentos;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterMensajeriaGlobal;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Mensaje_Cliente_Vendedor;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.utilidades.EncriptacionDatos;
import com.aplicacion.envivoapp.utilidades.MyFirebaseApp;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FragmentoMensajeriaGlobal extends Fragment {


    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos
    private StorageReference storageReference;

    private List<Mensaje> listMensaje = new ArrayList<>();
    private RecyclerView gridViewMensaje;
    private AdapterMensajeriaGlobal gridAdapterMensaje;
    private  Boolean esPrimerMensajeVendedor;
    private TextView tituloMesaje;
    private EditText mensajeEnv;
    private Button enviar, imagen;
    private Vendedor vendedorGlobal;
    private Cliente clienteGlobal;
    private final int PICKER = 1;
    private  String pathArchivo = "";
    private EncriptacionDatos encrip = new EncriptacionDatos();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_mensajeria_global, container, false);

        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage
        storageReference = storage.getReference();

        tituloMesaje = root.findViewById(R.id.txtNombreVendedorMensajeriaGlobal);
        mensajeEnv = root.findViewById(R.id.txtMensajeMensajeriaGlobal);
        enviar = root.findViewById(R.id.btnEnviarMensajeriaGlobal);
        gridViewMensaje = root.findViewById(R.id.gridMensajeriaGlobal);
        imagen = root.findViewById(R.id.btnImagenGlobalMensajeria);

        vendedorGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor(); //recogemos los datos del vendedor
        clienteGlobal = ((MyFirebaseApp) getActivity().getApplicationContext()).getCliente();
        existeMensajeriaClienteVendedor();

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorImagenes();
            }
        });


        enviar.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (!mensajeEnv.getText().toString().equals("")){
                    //buscamos el cliente
                    if (clienteGlobal != null) {
                        if (!clienteGlobal.getBloqueado()){
                            Mensaje mensaje = new Mensaje(); //instanciamos el mensaje


                            LocalDateTime tiempoActual = LocalDateTime.now(); //obtenemos la hora y fecha actual
                            // creamos la fecha
                            Date fecha = new Date();
                            fecha.setDate(tiempoActual.getDayOfMonth());
                            fecha.setMonth(tiempoActual.getMonth().getValue());
                            fecha.setYear(tiempoActual.getYear());
                            fecha.setHours(tiempoActual.getHour());
                            fecha.setMinutes(tiempoActual.getMinute());
                            fecha.setSeconds(tiempoActual.getSecond());

                            //creamo el mensaje
                            mensaje.setFecha(fecha);
                            String idMensaje = databaseReference.push().getKey();
                            mensaje.setIdMensaje(idMensaje);
                            try {
                                mensaje.setTexto(encrip.encriptar(mensajeEnv.getText().toString()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            mensaje.setCliente(clienteGlobal);
                            mensaje.setVendedor(vendedorGlobal);
                            mensaje.setIdStreaming(null);
                            mensaje.setEsGlobal(true);
                            mensaje.setEsVededor(false);
                            mensaje.setIdCliente_idVendedor(clienteGlobal.getIdCliente()+"_"+vendedorGlobal.getIdVendedor());

                            mensajeEnv.setText("");

                            databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(),"Error al enviar el mensaje",Toast.LENGTH_LONG).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (esPrimerMensajeVendedor){
                                        try {
                                            crearMensajeriaClienteVendedor();
                                            esPrimerMensajeVendedor = false;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    gridViewMensaje.setVerticalScrollbarPosition(listMensaje.size());
                                }
                            });


                        }else{
                            Dialog dialog = new Utilidades().cuadroError(getContext(),"Usted ha sido bloqueado contactese con el vendedor");
                            dialog.show();
                        }
                    }

                }
            }
        });
        leerMensaje();

        return root;
    }

private void existeMensajeriaClienteVendedor(){
    String idCliVende= clienteGlobal.getIdCliente()+"_"+vendedorGlobal.getIdVendedor();

    Dialog dialogoCargando = new Utilidades().dialogCargar(getContext());
    dialogoCargando.show();

    databaseReference.child("Mensaje_Cliente_Vendedor").
            child(idCliVende).
            get().
            addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    esPrimerMensajeVendedor = true;
                    dialogoCargando.dismiss();
                }
            }).addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
        @Override
        public void onSuccess(DataSnapshot snapshot) {
            if ( snapshot.exists()){
                esPrimerMensajeVendedor = false;
                dialogoCargando.dismiss();
            }else{
                esPrimerMensajeVendedor = true;
                dialogoCargando.dismiss();
            }

        }
    });
}

    private void crearMensajeriaClienteVendedor() throws Exception {
        //encriptamos los datos del vendedor
        Vendedor vendeorAux = vendedorGlobal;
        vendeorAux.setIdVendedor(vendeorAux.getIdVendedor());
        vendeorAux.setCelular(encrip.encriptar(vendeorAux.getCelular()));
        vendeorAux.setTelefono(encrip.encriptar(vendeorAux.getTelefono()));
        vendeorAux.setNombre(encrip.encriptar(vendeorAux.getNombre()));
        vendeorAux.setCedula(encrip.encriptar(vendeorAux.getCedula()));
        Mensaje_Cliente_Vendedor mensaje_cliente_vendedor = new Mensaje_Cliente_Vendedor();
        mensaje_cliente_vendedor.setCliente(clienteGlobal);
        mensaje_cliente_vendedor.setVendedor(vendeorAux);
        mensaje_cliente_vendedor.setIdCliente_idVendedor(clienteGlobal.getIdCliente()+"_"+vendedorGlobal.getIdVendedor());
        databaseReference.
                child("Mensaje_Cliente_Vendedor").
                child(mensaje_cliente_vendedor.getIdCliente_idVendedor()).
                setValue(mensaje_cliente_vendedor);
    }

    private void borrarGrid(){
        listMensaje.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        gridAdapterMensaje = new AdapterMensajeriaGlobal(getContext(), listMensaje, databaseReference, storage);
        gridViewMensaje.setAdapter(gridAdapterMensaje); //configuramos el view
        gridViewMensaje.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void leerMensaje() {
        borrarGrid();
        if (clienteGlobal != null) {
            if (!clienteGlobal.getBloqueado()) {
                Query queryMensaje = databaseReference.child("Mensaje").
                        orderByChild("idCliente_idVendedor").
                        equalTo(clienteGlobal.getIdCliente()+"_"+vendedorGlobal.getIdVendedor());
                queryMensaje.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        borrarGrid();
                        Mensaje mensaje = null;
                        for (DataSnapshot ds1 : snapshot.getChildren()) {
                            Mensaje mensajeAux = ds1.getValue(Mensaje.class);

                            try {
                                mensajeAux.setTexto(encrip.desencriptar(mensajeAux.getTexto()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                mensajeAux.setImagen(encrip.desencriptar(mensajeAux.getImagen()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (mensajeAux.getVendedor().getIdVendedor()!=null) {
                                if (mensajeAux.getEsEliminado() != null) {
                                    if (!mensajeAux.getEsEliminado()) {


                                        mensaje = mensajeAux;
                                    }
                                } else {
                                    mensaje = mensajeAux;
                                }
                                if (mensaje != null && mensaje.getEsGlobal()) {
                                    listMensaje.add(mensaje);
                                }
                            }
                        }

                        if (listMensaje.size() == 0) {
                            borrarGrid();
                        } else {
                            gridAdapterMensaje = new AdapterMensajeriaGlobal(getContext(), listMensaje, databaseReference, storage);
                            gridViewMensaje.setAdapter(gridAdapterMensaje); //configuramos el view}
                            gridViewMensaje.setLayoutManager(new LinearLayoutManager(getContext()));
                            gridViewMensaje.getLayoutManager().scrollToPosition(listMensaje.size() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        borrarGrid();
                    }
                });
            }else{
                Dialog dialog = new Utilidades().cuadroError(getContext(),"Usted ha sido bloqueado contactese con el vendedor");
                dialog.show();
            }
        }

    }
    private  void selectorImagenes(){//inicia el cuadro de dialogo para seleccionar una imagen
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent,"Seleccione un archivo para subir"),1);
        }catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(getContext(),"Por favor instale un administrados de archivos",Toast.LENGTH_SHORT).show();
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PICKER:
                if (resultCode == RESULT_OK){
                    Uri pathArchivo = data.getData(); //Obtenemos el uri de la imagen seleccionada
                    //cargamos el bitmap
                    if (clienteGlobal!=null){

                        //ecnriptamos los datos del vendedor
                        Vendedor vendedorAux = new Vendedor();
                        try {
                            vendedorAux.setTelefono( encrip.encriptar(vendedorGlobal.getTelefono()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            vendedorAux.setCelular(encrip.encriptar(vendedorGlobal.getCelular()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            vendedorAux.setCedula(encrip.encriptar(vendedorGlobal.getCedula()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        vendedorAux.setDiasEperaCancelacion(vendedorGlobal.getDiasEperaCancelacion());
                        vendedorAux.setIdVendedor(vendedorGlobal.getIdVendedor());
                        try {
                            vendedorAux.setNombre( encrip.encriptar(vendedorGlobal.getNombre()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        vendedorAux.setUidUsuario(vendedorGlobal.getUidUsuario());

                        //fin de encriptacion
                        // creamos el mensaje
                        Mensaje mensaje = new Mensaje(); //instanciamos el mensaje
                        Dialog cargando = new Utilidades().dialogCargar(getContext());
                        cargando.show();

                        LocalDateTime tiempoActual = LocalDateTime.now(); //obtenemos la hora y fecha actual
                        // creamos la fecha
                        Date fecha = new Date();
                        fecha.setDate(tiempoActual.getDayOfMonth());
                        fecha.setMonth(tiempoActual.getMonth().getValue());
                        fecha.setYear(tiempoActual.getYear());
                        fecha.setHours(tiempoActual.getHour());
                        fecha.setMinutes(tiempoActual.getMinute());
                        fecha.setSeconds(tiempoActual.getSecond());

                        //creamo el mensaje
                        mensaje.setFecha(fecha);
                        String idMensaje = databaseReference.push().getKey();
                        mensaje.setIdMensaje(idMensaje);
                        mensaje.setTexto("");
                        mensaje.setCliente(clienteGlobal);
                        mensaje.setVendedor(vendedorAux);
                        mensaje.setIdStreaming(null);
                        mensaje.setEsGlobal(true);
                        mensaje.setEsVededor(false);
                        mensaje.setIdCliente_idVendedor(clienteGlobal.getIdCliente()+"_"+vendedorGlobal.getIdVendedor());

                        if (!pathArchivo.equals("")){
                            try {
                                StorageReference storageRef = storage.getReference().child(idMensaje);//creamos la referencia para subir datos
                                mensaje.setImagen(encrip.encriptar(storageRef.getPath()));
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                Bitmap bitImagen = getBitmapFromUri(pathArchivo);
                                bitImagen.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] dataList = baos.toByteArray();
                                UploadTask uploadTask = storageRef.putBytes(dataList);
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Log.w("ImagenError","Error al cargar la imagen",exception);
                                        cargando.dismiss();
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        if (esPrimerMensajeVendedor){
                                            try {
                                                crearMensajeriaClienteVendedor();
                                                esPrimerMensajeVendedor = false;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        }
                                        databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje);
                                        cargando.dismiss();
                                    }
                                });

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                cargando.dismiss();
                            } catch (IOException e) {
                                e.printStackTrace();
                                cargando.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                                cargando.dismiss();
                            }
                        }else{
                            cargando.dismiss();
                        }
                    }
                }
                break;
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException { //obtenemos un bitmap de la ruta real del archivo
        ParcelFileDescriptor parcelFileDescriptor =
                getContext().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
}
