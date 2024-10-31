package com.example.ecohelper;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Anadir extends AppCompatActivity {

    private EditText edtCorreo;
    private EditText edtPuntos;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        edtCorreo = findViewById(R.id.edtCorreo);
        edtPuntos = findViewById(R.id.edtPuntos);
        Button btnBuscar = findViewById(R.id.btnBuscar);
        Button btnAPuntos = findViewById(R.id.btnAPuntos);
        Button btnReporte = findViewById(R.id.btnReporte);
        Button btnReg = findViewById(R.id.btnReg);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarUsuario();
            }
        });

        btnAPuntos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarPuntos();
            }
        });

        btnReporte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Anadir.this, Reporte.class);
                startActivity(intent);
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Anadir.this, MenuPrincipal.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void buscarUsuario() {
        final String correo = edtCorreo.getText().toString().trim();

        if (TextUtils.isEmpty(correo)) {
            Toast.makeText(this, "Ingrese un correo para buscar.", Toast.LENGTH_SHORT).show();
            return;
        }

        Query query = usersRef.orderByChild("correo").equalTo(correo);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        int puntos = snapshot.child("puntos").getValue(Integer.class);
                        edtPuntos.setHint("Puntos: " + puntos);
                        Toast.makeText(Anadir.this, "Usuario encontrado. Puntos: " + puntos, Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(Anadir.this, "No se encontró ningún usuario con ese correo.", Toast.LENGTH_SHORT).show();
                    edtPuntos.setHint("Puntos");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Anadir.this, "Error al buscar usuario: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarPuntos() {
        final String correo = edtCorreo.getText().toString().trim();
        String puntosString = edtPuntos.getText().toString().trim();

        if (TextUtils.isEmpty(correo) || TextUtils.isEmpty(puntosString)) {
            Toast.makeText(this, "Ingrese un correo y puntos válidos.", Toast.LENGTH_SHORT).show();
            return;
        }

        final int puntos = Integer.parseInt(puntosString);

        usersRef.orderByChild("correo").equalTo(correo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String userId = snapshot.getKey();
                        int puntosActuales = snapshot.child("puntos").getValue(Integer.class);
                        int nuevosPuntos = puntosActuales + puntos;

                        usersRef.child(userId).child("puntos").setValue(nuevosPuntos)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(Anadir.this, "Se han agregado " + puntos + " puntos correctamente.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(Anadir.this, "Error al agregar puntos: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                } else {
                    Toast.makeText(Anadir.this, "No se encontró ningún usuario con ese correo.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Anadir.this, "Error al buscar usuario: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
