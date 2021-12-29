package com.aplicacion.envivoapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.aplicacion.envivoapp.activityParaClientes.DataCliente;
import com.aplicacion.envivoapp.activityParaClientes.HomeCliente;
import com.aplicacion.envivoapp.activityParaClientes.ListarVendedores;
import com.aplicacion.envivoapp.activitysParaVendedores.DataVendedor;
import com.aplicacion.envivoapp.activitysParaVendedores.GestionVideos;
import com.aplicacion.envivoapp.activitysParaVendedores.HomeVendedor;
import com.aplicacion.envivoapp.modelos.Cliente;
import com.aplicacion.envivoapp.modelos.Usuario;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.database.ValueEventListener;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    private CallbackManager callBackManager;  //crea un administrador de devoluciones de llamada que gestione las respuestas de inicio de sesión.
    private LoginButton loginButton,loginButtonIniciarSesion; //boton para redireccionar el inicio de sesión con facebook
    private  FirebaseAuth firebaseAuth; //alamacena el usuario de firebase
    private  FirebaseAuth.AuthStateListener authStateListener;


    private  AccessTokenTracker accessTokenTracker;

    private FragmentTransaction fragmentTransaction; //dinamismo para el fragmento
    private Fragment fragmentUsuarioTieneCuenta; //intanciamos los fragmentos


    private CardView cardIniciarSesion, cardCrearCuenta,cardImgCrearCuenta,cardImgIniciarSesion;
    private ImageView imgCrearCuenta,imgIniciarSesion;
    private Button btnCrearCuenta,btnAtras,btnIniciarSesionGoolge,btnCrearCuentaGoogle;
    private RadioButton esVendedor,esCliente;
    private Boolean esNuevo = false;
    private GoogleSignInClient googleSignInClient;
    private int RC_SIGN_IN = 1;


    private FirebaseDatabase firebaseDatabase; //almacena a firebase database de firebase
    private DatabaseReference databaseReference; //almacena eel database reference de firebase
    private Usuario usuario;

    @RequiresApi(api = Build.VERSION_CODES.N)
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
        loginButtonIniciarSesion = findViewById(R.id.login_buttonIniciarSesion);

        esVendedor = findViewById(R.id.radioButtonEsVendedor);//almacena si el usuario es vendedor
        esCliente = findViewById(R.id.radioButtonEsCliente);//almacena si el usuario es cliente
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        btnAtras = findViewById(R.id.btnAtras);
        cardCrearCuenta = findViewById(R.id.cardCrearCuenta);
        cardIniciarSesion = findViewById(R.id.cardIniciarSesion);
        cardImgCrearCuenta = findViewById(R.id.cardImgCrearCuenta);
        cardImgIniciarSesion = findViewById(R.id.cardImgIniciarSesion);
        imgIniciarSesion = findViewById(R.id.imageViewIniciarSesion);
        imgCrearCuenta = findViewById(R.id.imageViewCrearCuenta);
        btnIniciarSesionGoolge = findViewById(R.id.bntIniciarSesionGoogle);
        btnCrearCuentaGoogle = findViewById(R.id.btnCrearCuentaGoogle);

        esNuevo = false;

        //para el inicio se sesion con google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(MainActivity.this,gso);

        usuario = new Usuario();//incializamos el usuarioo

        loginButton.setVisibility(View.GONE);//desabilitamos el boton de logeo
        btnCrearCuentaGoogle.setVisibility(View.GONE);

        //incio de funcionalidad a los checkBox
        esVendedor.setOnClickListener(new View.OnClickListener() {//en caso de que el usuario seleccione cualquiera de los checkBox
            @Override
            public void onClick(View v) {
                loginButton.setVisibility(View.VISIBLE);
                btnCrearCuentaGoogle.setVisibility(View.VISIBLE);
            } //habilitamos el boton de logeo
        });
        esCliente.setOnClickListener(new View.OnClickListener() {//en caso de que el usuario seleccione cualquiera de los checkBox
            @Override
            public void onClick(View v) {
                loginButton.setVisibility(View.VISIBLE);
                btnCrearCuentaGoogle.setVisibility(View.VISIBLE);
            }//habilitamos el boton de logeo
        });
        //fin de funcionalidad a los checkBox

        //escondemos el card view de crear cuenta
        cardCrearCuenta.setVisibility(View.GONE);
        cardImgCrearCuenta.setVisibility(View.GONE);
        imgCrearCuenta.setVisibility(View.GONE);

        //en caso de que el usuario le de click a crear cuenta
        btnCrearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardIniciarSesion.setVisibility(View.GONE);//escondemos el card de iniciar sesion
                cardImgIniciarSesion.setVisibility(View.GONE);
                imgIniciarSesion.setVisibility(View.GONE);

                cardCrearCuenta.setVisibility(View.VISIBLE);//mostramos el card de crear cuenta
                cardImgCrearCuenta.setVisibility(View.VISIBLE);
                imgCrearCuenta.setVisibility(View.VISIBLE);
                esNuevo = true;
            }
        });

        btnCrearCuentaGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        //en caso de que el usuario desee regresar
        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardCrearCuenta.setVisibility(View.GONE);//escondemos el card de iniciar sesion
                cardImgCrearCuenta.setVisibility(View.GONE);
                imgCrearCuenta.setVisibility(View.GONE);

                cardIniciarSesion.setVisibility(View.VISIBLE);//mostramos el card de crear cuenta
                cardImgIniciarSesion.setVisibility(View.VISIBLE);
                imgIniciarSesion.setVisibility(View.VISIBLE);
                esNuevo=false;
            }
        });


        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser();//almacena el usuario actual logeado
                if (user!= null){//en caso de no haber usuario iniciamos el updateUI
                    updateUI(user);
                }else{
                    updateUI(null);//sino no enviamos nada a update Ui y continuamos con el usuario actual
                }
            }
        };

        btnIniciarSesionGoolge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        loginButtonIniciarSesion.setReadPermissions("email","public_profile");
        // Callback registration
        loginButtonIniciarSesion.registerCallback(callBackManager, new FacebookCallback<LoginResult>() { //hacemos un collback para el registro del usuario
            @Override
            public void onSuccess(LoginResult loginResult) {
                //

                Log.d("FacebookAuthentication","OnSuccess"+loginResult); //en caso de ser exitoso imprimimos un mensaje en consola
                handleFacebookToken(loginResult.getAccessToken()); //llamamos a la funcion handleFacebookToken para el token de facebook
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

        //inicio logeo
        loginButton.setReadPermissions("email","public_profile");//leemos los permisos del email


        // Callback registration
        loginButton.registerCallback(callBackManager, new FacebookCallback<LoginResult>() { //hacemos un collback para el registro del usuario
            @Override
            public void onSuccess(LoginResult loginResult) {
                //
                Log.d("FacebookAuthentication","OnSuccess"+loginResult); //en caso de ser exitoso imprimimos un mensaje en consola
                handleFacebookToken(loginResult.getAccessToken()); //llamamos a la funcion handleFacebookToken para el token de facebook
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

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null){//en caso de no tener el token salimos de firebase
                    firebaseAuth.signOut();
                }
            }
        };

        //fin de logeo

        inicialiceFirebase();
        //String version = Build.VERSION.RELEASE;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callBackManager.onActivityResult(requestCode,resultCode,data); //codigo para facebook

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("Goolge inicio sesión", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("Goolge inicio sesión", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Goolge inicio sesión", "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Goolge inicio sesión", "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }

    private void handleFacebookToken(AccessToken accessToken) {
        Log.d("FacebookAuthentication","handleFacebookToken"+accessToken);
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken()); //almacenamos las credenciales del incio de sesion

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
            databaseReference.child("Usuario").addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        Usuario usuario = null;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Usuario usuarioAux = ds.getValue(Usuario.class);
                            if (usuarioAux.getUidUser().equals(usuarioLogeado)) {
                                usuario = usuarioAux;
                            }
                        }
                        if (usuario != null) {
                            if (usuario.getEsVendedor()) {
                                startActivity(new Intent(MainActivity.this, HomeVendedor.class));
                                finish();//para que el usuario no pueda regresar a activitys anteriores
                            } else {//buscamos si ya tiene un usuario ingresado
                                startActivity(new Intent(MainActivity.this, HomeCliente.class)); //en caso de ya aver ingresado sus datos inciamos listar vendedores
                                finish();//para que el usuario no pueda regresar a activitys anteriores

                            }
                        }else if(usuario == null && esNuevo){
                            //if (snapshot.getValue() == null && usuario.getUidUser() == null){//en caso de que no existan elemntos usuario en la base de datos}
                            usuario = new Usuario();
                            usuario.setEmail(firebaseUser.getEmail());//seteamos el email
                            usuario.setEsVendedor(esVendedor.isChecked());// seteamos si es o no vendedor
                            usuario.setUidUser(firebaseUser.getUid());//seteamos el usuario
                            usuario.setImagen(firebaseUser.getPhotoUrl().toString());

                            Usuario finalUsuario = usuario;
                            databaseReference.child("Usuario").child(usuario.getUidUser()).setValue(usuario).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (finalUsuario.getEsVendedor()){
                                        startActivity(new Intent(MainActivity.this, DataVendedor.class));

                                    }else{
                                        startActivity(new Intent(MainActivity.this, DataCliente.class));
                                        //finish(); //para que el usuario no pueda regresar a activitys anteriores
                                    }
                                    Toast.makeText(MainActivity.this,"guardado con exito",Toast.LENGTH_LONG).show();
                                }
                            });//almacenamos los datos en firebase

                            //}
                        }else{
                            Toast.makeText(MainActivity.this,"Usted no tiene una cuenta registrada porfavor cree una", Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut();
                            LoginManager.getInstance().logOut();
                        }

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
            databaseReference.child("Usuario").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() ){
                        buscarUsuario(firebaseUser);
                    }
                }
            });
            /*//añadimos la imagen al text view
            String foto= user.getPhotoUrl().toString();
            foto = foto+"?type=large";
            Piccasso.get().Load(foto).into(ImagenViewAsignado)        }
            */
        }
    }

    private void inicialiceFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();//inicializamos firebaseDatabase
        databaseReference = firebaseDatabase.getReference();//inicializamos el reference a la base de datos
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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