package com.example.ecohelper;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RegistroEx extends AppCompatActivity {

    private TextView txtMessage;
    private EditText edtCorreo;
    private Button btnBuscarCorreo;
    private Button btnBack;
    private String detectedObject;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registroex);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        txtMessage = findViewById(R.id.txtMessage);
        edtCorreo = findViewById(R.id.edtCorreo);
        btnBuscarCorreo = findViewById(R.id.btnBuscarCorreo);
        btnBack = findViewById(R.id.btnBack);

        detectedObject = getIntent().getStringExtra("DETECTED_OBJECT");

        if (detectedObject == null || detectedObject.equals("No reconocido")) {
            txtMessage.setText("Por favor reescanear");
        } else {
            txtMessage.setText("Se ha registrado " + detectedObject + " correctamente");
        }

        btnBuscarCorreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarCorreo();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarYAgregarPuntos();
            }
        });

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressed();
            }
        });
    }

    private void buscarCorreo() {
        final String correo = edtCorreo.getText().toString().trim();

        if (TextUtils.isEmpty(correo)) {
            Toast.makeText(this, "Ingrese un correo para buscar.", Toast.LENGTH_SHORT).show();
            return;
        }

        Query query = usersRef.orderByChild("correo").equalTo(correo);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(RegistroEx.this, "Correo encontrado correctamente.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegistroEx.this, "No se encontró ningún usuario con ese correo.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RegistroEx.this, "Error al buscar usuario: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buscarYAgregarPuntos() {
        final String correo = edtCorreo.getText().toString().trim();

        if (TextUtils.isEmpty(correo)) {
            Toast.makeText(this, "Ingrese un correo para buscar.", Toast.LENGTH_SHORT).show();
            return;
        }

        Query query = usersRef.orderByChild("correo").equalTo(correo);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        agregarPuntos(snapshot.getKey());
                    }
                } else {
                    Toast.makeText(RegistroEx.this, "No se encontró ningún usuario con ese correo.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RegistroEx.this, "Error al buscar usuario: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarPuntos(String userId) {
        int puntosAgregados = 0;

        switch (detectedObject) {
            case "Botellas de plástico":
                puntosAgregados = 10;
                break;
            case "Botellas de vidrio":
                puntosAgregados = 15;
                break;
            case "Latas":
                puntosAgregados = 20;
                break;
            case "Bolsa de papeles":
                puntosAgregados = 5;
                break;
            case "Cartón":
                puntosAgregados = 15;
                break;
            default:
                break;
        }

        int finalPuntosAgregados = puntosAgregados;
        usersRef.child(userId).child("puntos").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int puntosActuales = dataSnapshot.getValue(Integer.class);
                    int nuevosPuntos = puntosActuales + finalPuntosAgregados;

                    usersRef.child(userId).child("puntos").setValue(nuevosPuntos)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegistroEx.this, "Se han agregado " + finalPuntosAgregados + " puntos correctamente.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegistroEx.this, MenuPrincipal.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(RegistroEx.this, "Error al agregar puntos: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(RegistroEx.this, "No se encontró ningún usuario con ese correo.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RegistroEx.this, "Error al buscar usuario: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
