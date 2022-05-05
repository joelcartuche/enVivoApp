package com.aplicacion.envivoapp.activitysParaVendedores.fragmentos;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterMensajeriaGlobalVendedores;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.modelos.Mensaje_Cliente_Vendedor;
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

public class FragmentoMensajeriaGlobalVendedor extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos
    private StorageReference storageReference;
    private EncriptacionDatos encriptacionDatos = new EncriptacionDatos();

    private List<Mensaje> listMensaje = new ArrayList<>();
    private RecyclerView gridViewMensaje;
    private AdapterMensajeriaGlobalVendedores gridAdapterMensaje;
    private Vendedor vendedorGlobal;


    private Button enviar,imagen;
    private EditText textoMensaje;
    private Cliente cliente;
    private final int PICKER = 1;
    private  String pathArchivo = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_mensajeria_global_vendedor, container, false);
        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance(); //para la insersion de archivos
        storageReference= storage.getReference();

        gridViewMensaje = root.findViewById(R.id.gridMensajeriaGlobalVendedor);
        enviar= root.findViewById(R.id.btnEnviarMensajeriaGlobalVendedor);
        textoMensaje = root.findViewById(R.id.txtTextiMensajeriaGlobalVendedor);
        imagen= root.findViewById(R.id.btnImagenGlobalVendedor);

        cliente = ((MyFirebaseApp) getActivity().getApplicationContext()).getCliente(); //recogemos los datos del vendedor
        vendedorGlobal =((MyFirebaseApp) getActivity().getApplicationContext()).getVendedor();

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorImagenes();
            }
        });

        //funcion del boton enviar mensaje
        enviar.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (!textoMensaje.getText().toString().equals("")){
                    // creamos el mensaje
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
                        mensaje.setTexto(encriptacionDatos.encriptar(textoMensaje.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mensaje.setCliente(cliente);
                    mensaje.setVendedor(vendedorGlobal);
                    mensaje.setIdStreaming(null);
                    mensaje.setEsGlobal(true);
                    mensaje.setEsVededor(true);
                    mensaje.setIdCliente_idVendedor(cliente.getIdCliente()+"_"+vendedorGlobal.getIdVendedor());


                    databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            textoMensaje.setText("");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(),"Error al enviar el mensaje intentelo de nuevo",Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        });
        //cargamos los mensajes
        leerMensaje();
        return root;
    }

    private void crearMensajeriaClienteVendedor(){
        Mensaje_Cliente_Vendedor mensaje_cliente_vendedor = new Mensaje_Cliente_Vendedor();
        mensaje_cliente_vendedor.setCliente(cliente);
        mensaje_cliente_vendedor.setVendedor(vendedorGlobal);
        mensaje_cliente_vendedor.setIdCliente_idVendedor(cliente.getIdCliente()+"_"+vendedorGlobal.getIdVendedor());
        databaseReference.
                child("Mensaje_Cliente_Vendedor").
                child(mensaje_cliente_vendedor.getIdCliente_idVendedor()).
                setValue(mensaje_cliente_vendedor);
    }

    private void borrarGrid(){
        listMensaje.clear();//borramos en caso de quedar algo en la cache
        //Inicialisamos el adaptador
        gridAdapterMensaje = new AdapterMensajeriaGlobalVendedores(getContext(),null,
                listMensaje,
                databaseReference,
                storage,
                firebaseAuth);
        gridViewMensaje.setAdapter(gridAdapterMensaje); //configuramos el view
        gridViewMensaje.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    private void leerMensaje() {
        if (vendedorGlobal != null) {
            Query queryMensaje = databaseReference.child("Mensaje").
                    orderByChild("idCliente_idVendedor").
                    equalTo(cliente.getIdCliente()+"_"+vendedorGlobal.getIdVendedor());

            queryMensaje.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    borrarGrid();
                    for (DataSnapshot ds1 : snapshot.getChildren()) {
                        Log.d("DATA", ds1.toString());
                        Mensaje mensajeAux = ds1.getValue(Mensaje.class);
                        if (mensajeAux != null
                                && !mensajeAux.getEsClienteBloqueado()
                                && !mensajeAux.getEsEliminado()
                                && mensajeAux.getEsGlobal()) {
                            Log.d("Entree","-----------");
                            try {
                                mensajeAux.setImagen(encriptacionDatos.desencriptar(mensajeAux.getImagen()));
                                Log.d("Entree","-----------");
                                Log.d("Entree",mensajeAux.getIdMensaje());
                                Log.d("Entree",mensajeAux.getImagen());
                                Log.d("Entree","-----------");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                mensajeAux.setTexto(encriptacionDatos.desencriptar(mensajeAux.getTexto()));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            listMensaje.add(mensajeAux);

                        }else{
                            Log.e("Error","Mensajeria global null 266");
                        }
                    }

                    gridAdapterMensaje = new AdapterMensajeriaGlobalVendedores(getContext(),
                            vendedorGlobal,
                            listMensaje,
                            databaseReference,
                            storage,
                            firebaseAuth);
                    gridViewMensaje.setAdapter(gridAdapterMensaje); //configuramos el view
                    gridViewMensaje.setLayoutManager(new LinearLayoutManager(getContext()));
                    gridViewMensaje.getLayoutManager().scrollToPosition(listMensaje.size()-1);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    borrarGrid();
                }
            });
        }else{
            borrarGrid();
        }

    }

    private  void selectorImagenes(){//inicia el cuadro de dialogo para seleccionar una imagen
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent,"Seleccione un archivo para subir"),1);
        }catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(getContext(),"Por favorm instale un administrados de archivos",Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PICKER:
                if (resultCode == RESULT_OK){
                    Uri pathArchivo = data.getData();
                    if (vendedorGlobal!=null){
                        // creamos el mensaje
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
                            mensaje.setTexto(encriptacionDatos.encriptar(""));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mensaje.setCliente(cliente);
                        mensaje.setVendedor(vendedorGlobal);
                        mensaje.setIdStreaming(null);
                        mensaje.setEsGlobal(true);
                        mensaje.setEsVededor(true);
                        mensaje.setIdCliente_idVendedor(cliente.getIdCliente()+"_"+vendedorGlobal.getIdVendedor());

                        if (!pathArchivo.equals("")){
                            //cargamos la imagen
                            try {
                                StorageReference storageRef = storage.getReference().child(idMensaje);//creamos la referencia para subir datos
                                mensaje.setImagen(storageRef.getPath());
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                Bitmap bitImagen = getBitmapFromUri(pathArchivo);
                                bitImagen.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] dataList = baos.toByteArray();
                                UploadTask uploadTask = storageRef.putBytes(dataList);
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Toast.makeText(getContext(),"Error al subir la imagen intentelo de nuevo",Toast.LENGTH_LONG).show();
                                        Log.w("ImagenError","Error al cargar la imagen",exception);
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        try {
                                            mensaje.setImagen(encriptacionDatos.encriptar(mensaje.getImagen()));
                                            databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje);

                                        } catch (Exception e) {
                                            Toast.makeText(getContext(),"Error al subir la imagen intentelo de nuevo",Toast.LENGTH_LONG).show();
                                            e.printStackTrace();
                                        }

                                    }
                                });

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                    }else{
                        Log.e("Error","Mensajeria Global Vendedor null 398");
                    }
                }
                break;
        }

    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getActivity().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
}
