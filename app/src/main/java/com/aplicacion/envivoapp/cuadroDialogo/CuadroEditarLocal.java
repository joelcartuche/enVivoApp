package com.aplicacion.envivoapp.cuadroDialogo;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aplicacion.envivoapp.R;
import com.aplicacion.envivoapp.modelos.Local;
import com.aplicacion.envivoapp.modelos.Vendedor;
import com.aplicacion.envivoapp.modelos.VideoStreaming;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CuadroEditarLocal {
    public interface resultadoDialogo{
        void resultado();
    }
    private CuadroEditarLocal.resultadoDialogo interfaceResultadoDialogo;

    public CuadroEditarLocal(Context context,
                             @Nullable  Local local,
                             @Nullable Vendedor vendedor,
                                Boolean esNuevo,
                                DatabaseReference reference,
                             CuadroEditarLocal.resultadoDialogo result){
        interfaceResultadoDialogo = result;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//el dialogo se presenta sin el titulo
        dialog.setCancelable(false); //impedimos el cancelamiento del dialogo
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));//le damos un color de fondo transparente
        dialog.setContentView(R.layout.cuadro_editar_local); //le asisganos el layout

        EditText nombreLocal = dialog.findViewById(R.id.txtNombreLocalCuadroLocal);
        EditText direccionLocal = dialog.findViewById(R.id.txtDireccionLocalCuadroLocal);
        EditText telefonoLocal = dialog.findViewById(R.id.txtTelefonoCuadroLocal);
        EditText celularLocal = dialog.findViewById(R.id.txtCelularCuadroLocal);
        Button guardar = dialog.findViewById(R.id.btnGuardarCuadroEditarLocal);
        Button actualizar = dialog.findViewById(R.id.btnActualizarEdicionCuadroLocal);
        Button eliminar = dialog.findViewById(R.id.btnEliminarLocalCuadroLocal);
        Button cancelar = dialog.findViewById(R.id.btnCancelarCuadroLocal);

        if (esNuevo){
            guardar.setVisibility(View.VISIBLE);
            actualizar.setVisibility(View.GONE);
            eliminar.setVisibility(View.GONE);
        }else{
            guardar.setVisibility(View.GONE);
            actualizar.setVisibility(View.VISIBLE);
            eliminar.setVisibility(View.VISIBLE);
            nombreLocal.setText(local.getNombre());
            direccionLocal.setText(local.getDireccion());
            telefonoLocal.setText(local.getCelular());
            celularLocal.setText(local.getCelular());
        }
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nombreLocal.getText().toString().equals("")){
                    Toast.makeText(context,"Ingrese el nombre del local",Toast.LENGTH_LONG).show();
                }else if (direccionLocal.getText().toString().equals("")){
                    Toast.makeText(context,"Ingrese la direccion del local",Toast.LENGTH_LONG).show();
                }else if (telefonoLocal.getText().toString().equals("")
                        && celularLocal.getText().toString().equals("")){
                    Toast.makeText(context,"Asegurese de haber ingresado el local",Toast.LENGTH_LONG).show();
                }else{
                    Local local = new Local();
                    String idLocal = reference.push().getKey();

                    local.setIdLocal(idLocal);
                    local.setNombre(nombreLocal.getText().toString());
                    local.setDireccion(direccionLocal.getText().toString());
                    local.setCelular(telefonoLocal.getText().toString());
                    local.setTelefono(telefonoLocal.getText().toString());
                    local.setIdVendedor(vendedor.getIdVendedor());
                    reference.child("Local").child(idLocal).setValue(local).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context,"Los datos han sido guardados con éxito",Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,"Los datos no se han podido guardar correctamente",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nombreLocal.getText().toString().equals("")){
                    Toast.makeText(context,"Ingrese el nombre del local",Toast.LENGTH_LONG).show();
                }else if (direccionLocal.getText().toString().equals("")){
                    Toast.makeText(context,"Ingrese la direccion del local",Toast.LENGTH_LONG).show();
                }else if (telefonoLocal.getText().toString().equals("")
                        && celularLocal.getText().toString().equals("")){
                    Toast.makeText(context,"Asegurese de haber ingresado el local",Toast.LENGTH_LONG).show();
                }else{
                    reference.child("Local").child(local.getIdLocal()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Map<String, Object> map = new HashMap<>();
                            map.put("nombre",nombreLocal.getText().toString());
                            map.put("direccion",direccionLocal.getText().toString());
                            map.put("telefono",telefonoLocal.getText().toString());
                            map.put("celular",telefonoLocal.getText().toString());
                            reference.child("Local").child(local.getIdLocal()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context,"Los datos han sido actualizados con éxito",Toast.LENGTH_LONG).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context,"A ocurrido un error al actualizar los datos",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                    });
                }
            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child("Local").child(local.getIdLocal()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"El local a sido eliminado con éxito",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"El local no pudo ser eliminado",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
