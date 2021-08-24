package com.aplicacion.envivoapp.activityParaClientes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.adaptadores.AdapterGridMensajeriaCliente;
import com.aplicacion.envivoapp.modelos.Mensaje;
import com.aplicacion.envivoapp.utilidades.Utilidades;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.nmd.screenshot.Screenshot;


public class MensajeriaCliente extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener,
        YouTubePlayer.PlaybackEventListener,KeyEvent.Callback {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage ; //para la insersion de archivos

    private Button enviarMensaje,quieroComprar;
    private EditText textoMensaje;
    private String idVendedor,idCliente,idStreaming,urlStreaming;

    private List<Mensaje> listMensaje = new ArrayList<>();
    private GridView gridViewMensaje;
    private AdapterGridMensajeriaCliente gridAdapterMensaje;
    private Bitmap bitmapCapturaPantalla;
    private YouTubePlayerView reproductorYoutube;
    private Screenshot screenshot;
    private final int PICKER = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajeria_cliente);


        firebaseAuth = FirebaseAuth.getInstance(); //intanciamos el usuario logeado
        firebaseDatabase = FirebaseDatabase.getInstance(); //intanciamos la base de datos firebase
        databaseReference = firebaseDatabase.getReference();//almacenamos la referrencia de la base de datos
        storage = FirebaseStorage.getInstance();//inicializamos la variable storage

        //incializaoms las variables
        enviarMensaje = findViewById(R.id.btnEnviarMensajeCliente);
        textoMensaje = findViewById(R.id.txtMensajeCliente);
        gridViewMensaje = findViewById(R.id.gridMensajeCliente);
        quieroComprar = findViewById(R.id.btnQuieroComprarMensajeCliente);
        reproductorYoutube = findViewById(R.id.videoYoutubeMensajeriaCliente);
        reproductorYoutube.initialize(new Utilidades().getClaveYoutube(), MensajeriaCliente.this);


        //fin de incializacion de variables


        //Enviamos el mensaje informativo
        DialogInterface.OnClickListener confirmar = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        new Utilidades().cuadroDialogo(MensajeriaCliente.this,confirmar,"Informacion","Antés de presionar el boton quiero comprar debe realizar una captura de pantalla del producto que se esta mostrando en el streaming para ello puede usar la combinacion de teclas o deslizando el menu de la parte superior");
        Bundle vendedor = MensajeriaCliente.this.getIntent().getExtras();
        idVendedor = vendedor.getString("vendedor"); //recogemos los datos del vendedor
        idCliente = vendedor.getString("cliente");
        urlStreaming = vendedor.getString("url"); //recogemos los datos del vendedor
        idStreaming = vendedor.getString("streaming");


        listarMensajes();//listamos los mensajes del cliente

        //Damos funcionalidad al menu
        Button btnListarVendedore = findViewById(R.id.btn_listar_vendedores_MensajeriaCliente);
        Button btnPerfil = findViewById(R.id.btn_perfil_listar_MensajeriaCliente);
        Button btnPedido = findViewById(R.id.btn_carrito_listar_MensajeriaCliente);
        Button btnSalir = findViewById(R.id.btn_salir_MensajeriaCliente);
        Button btnMensje = findViewById(R.id.btnMensajeriaGlobalMensajeriaCliente);

        Button btnHome = findViewById(R.id.btn_Home_Mensajeria_Cliente);

        new Utilidades().cargarToolbar(btnHome,
                btnListarVendedore,
                btnPerfil,
                btnPedido,
                btnSalir,
                btnMensje,
                MensajeriaCliente.this,firebaseAuth,databaseReference);

        //en caso de que el cliente desee comprar
        quieroComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectorImagenes();


                /*
                textoMensaje.setText("Quiero comprar: ");
                //bitmapCapturaPantalla = ScreenshotUtil.getInstance().takeScreenshotForScreen(MensajeriaCliente.this);
                View rootView = getWindow().getDecorView();
                bitmapCapturaPantalla = ScreenshotUtil.getInstance().takeScreenshotForView(rootView);
                Toast.makeText(MensajeriaCliente.this,"Ingrese el nombre o código y la cantidad del producto que desea",Toast.LENGTH_LONG).show();
                */

                //textoMensaje.setText("Quiero comprar: ");
                //bitmapCapturaPantalla = ThumbnailUtils.createVideoThumbnail("imgPath", MediaStore.Images.Thumbnails.MINI_KIND);;
                //Toast.makeText(MensajeriaCliente.this,"Ingrese el nombre o código y la cantidad del producto que desea",Toast.LENGTH_LONG).show();
                /*
                screenshot.notificationTitle("My screenshot title");
                screenshot.setCallback(new Screenshot.OnResultListener() {
                    @Override
                    public void result(boolean success, String filePath, Bitmap bitmap) {
                        textoMensaje.setText("Quiero comprar: ");
                        bitmapCapturaPantalla = bitmap;
                        Toast.makeText(MensajeriaCliente.this,"Ingrese el nombre o código y la cantidad del producto que desea",Toast.LENGTH_LONG).show();
                    }
                });

                final ViewGroup view= (ViewGroup) (getWindow().getDecorView().getRootView());
                screenshot.takeScreenshotFromView(findViewById(R.id.videoYoutubeMensajeriaCliente).getRootView());
                 */
            }
        });

        enviarMensaje.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
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
                mensaje.setTexto(textoMensaje.getText().toString());
                mensaje.setIdcliente(idCliente);
                mensaje.setIdvendedor(idVendedor);
                mensaje.setIdStreaming(idStreaming);
                mensaje.setPedidoAceptado(false);
                mensaje.setPedidoCancelado(false);
                mensaje.setEsVededor(false);
                mensaje.setPedidoCancelado(false);
                mensaje.setPedidoAceptado(false);
                textoMensaje.setText("");
                if (mensaje.getTexto().indexOf("Quiero comprar:") == 0) {
                    if (bitmapCapturaPantalla != null) {

                        StorageReference storageRef = storage.getReference().child(idMensaje);//creamos la referencia para subir datos
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmapCapturaPantalla.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] data = baos.toByteArray();

                        bitmapCapturaPantalla = null;

                        UploadTask uploadTask = storageRef.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.w("ImagenError","Error al cargar la imagen",exception);
                            }
                        });
                        mensaje.setImagen(storageRef.getPath());
                        databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje);

                    }else{
                        Toast.makeText(MensajeriaCliente.this,"No ha seleccionado ninguna captura de pantalla",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    databaseReference.child("Mensaje").child(idMensaje).setValue(mensaje);

                }
                
            }
        });
    }

    private Bitmap tomarCapturaPantalla() {
        Bitmap bitmap = null;
        try {
            // create bitmap screen capture View v1 = getWindow().getDecorView().getRootView().findViewById(R.id.videoYoutubeMensajeriaCliente);
            final ViewGroup view= (ViewGroup) ((ViewGroup) this
                    .findViewById(android.R.id.content)).getChildAt(0);
            bitmap = Bitmap.createBitmap(view.getWidth() , view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);
            view.draw(c);


            /*
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            v1.invalidate();
            bitmap =  Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);
            v1.destroyDrawingCache();
            */

            return bitmap;

        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }

        return null;
    }

    public  void borrarGrid(){
        listMensaje.clear();//borramos los datos ya que no hay nada en la base
        gridAdapterMensaje = new AdapterGridMensajeriaCliente(MensajeriaCliente.this,listMensaje,databaseReference,storage);
        gridViewMensaje.setAdapter(gridAdapterMensaje);
    }

    public void listarMensajes(){
        databaseReference.child("Mensaje").orderByChild("fecha").addValueEventListener(new ValueEventListener() { //buscamos todos los datos en la tabla Video Streaming
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listMensaje.clear();//borramos en caso de quedar algo en la cache
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Mensaje mensaje = ds.getValue(Mensaje.class);
                        if(mensaje.getIdcliente() != null && mensaje.getIdStreaming() !=null) {
                            if (mensaje.getIdcliente().equals(idCliente)
                                    && mensaje.getIdStreaming().equals(idStreaming)) {//aceptamos los mensades que sean del cliente y de el streaming actual
                                listMensaje.add(mensaje);
                            }
                        }
                    }
                    gridAdapterMensaje = new AdapterGridMensajeriaCliente(MensajeriaCliente.this, listMensaje, databaseReference, storage);
                    gridViewMensaje.setAdapter(gridAdapterMensaje);
                }else{
                   borrarGrid();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }


    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean fueRestaurado) {
        if(!fueRestaurado){
            youTubePlayer.cueVideo("QN7BKarpltI");//https://www.youtube.com/watch?v=QN7BKarpltI
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()){
            youTubeInitializationResult.getErrorDialog(this,1).show();
        }else{//en caso de que el error no sea de youtube o este no lo conosca.
            String error ="Error al inisializar Youtube"+youTubeInitializationResult.toString();
            Toast.makeText(getApplication(),error,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == 1){
            getYoutubePlayerProvider().initialize(new Utilidades().getClaveYoutube(),this);
        }

        switch (requestCode) {
            case PICKER:
                if (resultCode == RESULT_OK) {
                    Uri pathArchivo = data.getData(); //Obtenemos el uri de la imagen seleccionada
                    if(!pathArchivo.equals("")) {
                        try {
                            Bitmap bitImagen = getBitmapFromUri(pathArchivo);
                            bitmapCapturaPantalla = bitImagen;
                            textoMensaje.setText("Quiero comprar: ");
                            Toast.makeText(MensajeriaCliente.this,"Ingrese el nombre o código y la cantidad del producto que desea",Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        }

    }

    protected YouTubePlayer.Provider getYoutubePlayerProvider(){
        return reproductorYoutube;
    }

    @Override
    public void onPlaying() {

    }

    @Override
    public void onPaused() {

    }

    @Override
    public void onStopped() {

    }

    @Override
    public void onBuffering(boolean b) {

    }

    @Override
    public void onSeekTo(int i) {

    }

    private  void selectorImagenes(){//inicia el cuadro de dialogo para seleccionar una imagen
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent,"Seleccione la captura de pantalla del producto"),1);
        }catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(MensajeriaCliente.this,"Por favor instale un administrados de archivos",Toast.LENGTH_SHORT).show();
        }
    }
    private Bitmap getBitmapFromUri(Uri uri) throws IOException { //obtenemos un bitmap de la ruta real del archivo
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }



}


