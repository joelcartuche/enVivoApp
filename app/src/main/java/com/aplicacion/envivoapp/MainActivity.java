package com.aplicacion.envivoapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.aplicacion.envivoapp.activityParaClientes.DataCliente;
import com.aplicacion.envivoapp.activityParaClientes.ListarVendedores;
import com.aplicacion.envivoapp.activitysParaVendedores.DataVendedor;
import com.aplicacion.envivoapp.activitysParaVendedores.GestionVideos;
import com.aplicacion.envivoapp.fragmentos.UsuarioTieneCuenta;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Usuario;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private CallbackManager callBackManager;  //crea un administrador de devoluciones de llamada que gestione las respuestas de inicio de sesión.
    private LoginButton loginButton; //boton para redireccionar el inicio de sesión con facebook
    private  FirebaseAuth firebaseAuth; //alamacena el usuario de firebase
    private  FirebaseAuth.AuthStateListener authStateListener;
    private Button iniciarSesion;
    private RadioButton esVendedor,esCliente;
    private  AccessTokenTracker accessTokenTracker;

    private FragmentTransaction fragmentTransaction; //dinamismo para el fragmento
    private Fragment fragmentUsuarioTieneCuenta; //intanciamos los fragmentos


    private EditText correo,clave;


    Button guardar;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //variables para el logeo de facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        callBackManager = CallbackManager.Factory.create();//creamos el administrador de llamadas
        firebaseAuth = FirebaseAuth.getInstance(); //instanciamos el usuario de firebase

        loginButton = findViewById(R.id.login_button);//almacenamos el boton de facebook

        esVendedor = findViewById(R.id.radioButtonEsVendedor);
        esCliente = findViewById(R.id.radioButtonEsCliente);
        fragmentUsuarioTieneCuenta = new UsuarioTieneCuenta();
        correo = (EditText) findViewById(R.id.txtCorreoUsuario);//probar logeo
        clave = (EditText) findViewById(R.id.txtClaveUsuario);
        guardar = (Button) findViewById(R.id.guardarUsuario);
        iniciarSesion = findViewById(R.id.btnIniciarSesion);
        usuario = new Usuario();
        loginButton.setVisibility(View.INVISIBLE);



        esVendedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setVisibility(View.VISIBLE);
            }
        });
        esCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setVisibility(View.VISIBLE);
            }
        });


        //inicio logeo
        loginButton.setReadPermissions("email","public_profile");//leemos los permisos del email
        // If using in a fragment
        //loginButton.setFragment(MainActivity.this);

        // Callback registration
        loginButton.registerCallback(callBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //
                Log.d("FacebookAuthentication","OnSuccess"+loginResult);
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

                Log.d("FacebookAuthentication","OnCancel");
            }

            @Override
            public void onError(FacebookException error) {

                Log.d("FacebookAuthentication","OnError"+error);
            }


        });

        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser();
                if (user!= null){
                    updateUI(user);
                }else{
                    updateUI(null);
                }
            }
        };

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null){
                    firebaseAuth.signOut();
                }
            }
        };


        //fin de logeo

        iniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setVisibility(View.INVISIBLE);
                esCliente.setChecked(false);
                esVendedor.setChecked(false);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frameUserTieneCuenta,fragmentUsuarioTieneCuenta);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });

        inicialiceFirebase();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callBackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookToken(AccessToken accessToken) {
        Log.d("FacebookAuthentication","handleFacebookToken"+accessToken);
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken()); //almacenamos las credenciales del incio de secion

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() { //inciciamos la cesion en firebase con las credenciales
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d("FacebookAuthentication","sign in whith credential: successful");

                    FirebaseUser user= firebaseAuth.getCurrentUser(); //almacenamos los datos del usuario

                    updateUI(user); //

                }else{
                    Log.d("FacebookAuthentication","sign in whith credential: error",task.getException());
                    Toast.makeText(MainActivity.this,"Autenticacion fallida",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    public void buscarUsuario(FirebaseUser firebaseUser){
        if (firebaseAuth.getCurrentUser() != null){
            String usuarioLogeado = firebaseAuth.getCurrentUser().getUid();//obtenemos el uid del usuario logeado
            Toast.makeText(MainActivity.this,"Usuario existente ",Toast.LENGTH_LONG).show();
            databaseReference.child("Usuario").child(usuarioLogeado).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        usuario =snapshot.getValue(Usuario.class);
                        if (usuario.getEsVendedor()){
                            startActivity(new Intent(MainActivity.this, GestionVideos.class));
                            finish();//para que el usuario no pueda regresar a activitys anteriores
                        }else{//buscamos si ya tiene un usuario ingresado
                            databaseReference.child("Cliente").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        for (DataSnapshot ds:snapshot.getChildren()){
                                            Cliente cli = ds.getValue(Cliente.class);
                                            if(cli.getUidUsuario().equals(usuario.getUidUser())){//en caso de ya tener los datos ingresados
                                                startActivity(new Intent(MainActivity.this, ListarVendedores.class)); //en caso de ya aver ingresado sus datos inciamos listar vendedores
                                                finish();//para que el usuario no pueda regresar a activitys anteriores
                                            }else{
                                                startActivity(new Intent(MainActivity.this, DataCliente.class));//en caso de no haber ingresado los datos iniciamos data cliente
                                                finish();//para que el usuario no pueda regresar a activitys anteriores
                                            }
                                        }
                                    }else{//en caso de que no exista nada en la base de datos
                                        if (usuario.getEsVendedor()){
                                            startActivity(new Intent(MainActivity.this, ListarVendedores.class)); //en caso de ya aver ingresado sus datos inciamos listar vendedores
                                            finish();//para que el usuario no pueda regresar a activitys anteriores
                                        }else{
                                            startActivity(new Intent(MainActivity.this, DataCliente.class));//en caso de no haber ingresado los datos iniciamos data cliente
                                            finish();//para que el usuario no pueda regresar a activitys anteriores
                                        }
                                    }


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    }else{
                        //if (snapshot.getValue() == null && usuario.getUidUser() == null){//en caso de que no existan elemntos usuario en la base de datos
                            usuario.setEmail(firebaseUser.getEmail());//seteamos el email
                            usuario.setEsVendedor(esVendedor.isChecked());// seteamos si es o no vendedor
                            usuario.setUidUser(firebaseUser.getUid());//seteamos el usuario

                            databaseReference.child("Usuario").child(usuario.getUidUser()).setValue(usuario);//almacenamos los datos en firebase
                            Toast.makeText(MainActivity.this,"guardado con exito",Toast.LENGTH_LONG).show();

                            if (usuario.getEsVendedor()){
                                startActivity(new Intent(MainActivity.this, DataVendedor.class));

                            }else{
                                startActivity(new Intent(MainActivity.this, DataCliente.class));
                                //finish(); //para que el usuario no pueda regresar a activitys anteriores
                            }
                        //}
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void updateUI(FirebaseUser firebaseUser){
        if (firebaseUser!=null ) { //een caso de no exitir conexion con firebase
            databaseReference.child("Usuario").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists() ){
                        buscarUsuario(firebaseUser);

                    }else{
                        if (snapshot.getValue() == null && usuario.getUidUser() == null){//en caso de que no existan elemntos usuario en la base de datos
                            usuario.setEmail(firebaseUser.getEmail());//seteamos el email
                            usuario.setEsVendedor(esVendedor.isChecked());// seteamos si es o no vendedor
                            usuario.setUidUser(firebaseUser.getUid());//seteamos el usuario

                            databaseReference.child("Usuario").child(usuario.getUidUser()).setValue(usuario);//almacenamos los datos en firebase
                            Toast.makeText(MainActivity.this,"guardado con exito",Toast.LENGTH_LONG).show();

                            if (usuario.getEsVendedor()){
                                startActivity(new Intent(MainActivity.this, DataVendedor.class));

                            }else{
                                startActivity(new Intent(MainActivity.this, DataCliente.class));
                                //finish(); //para que el usuario no pueda regresar a activitys anteriores
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            /*//añadimos la imagen al text view
            String foto= user.getPhotoUrl().toString();
            foto = foto+"?type=large";
            Piccasso.get().Load(foto).into(ImagenViewAsignado)        }
            */
        }else{
            //Retornamos una imagen predefinida
            //imageView.setImageResourse(R.drawable.Logo);
        }
    }

    private void inicialiceFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();//inicializamos firebaseDatabase
        databaseReference = firebaseDatabase.getReference();//inicializamos el reference a la base de datos
    }



    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener); //iniciamos la aplicacion enviando el usuario antes logeado
        buscarUsuario(firebaseAuth.getCurrentUser());

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}