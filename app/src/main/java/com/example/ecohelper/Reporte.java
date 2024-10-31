package com.example.ecohelper;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Reporte extends AppCompatActivity {

    private LinearLayout layoutReporte;
    private Button btnRepIn;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        layoutReporte = findViewById(R.id.layoutReporte);
        btnRepIn = findViewById(R.id.btnRepIn);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        btnRepIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mostrarReporte();
    }

    private void mostrarReporte() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String nombre = snapshot.child("nombre").getValue(String.class);
                    String apellidos = snapshot.child("apellidos").getValue(String.class);
                    String correo = snapshot.child("correo").getValue(String.class);
                    int puntos = snapshot.child("puntos").getValue(Integer.class);

                    TextView textView = new TextView(Reporte.this);
                    textView.setText(String.format("Nombre: %s %s\nCorreo: %s\nPuntos: %d\n",
                            nombre, apellidos, correo, puntos));
                    textView.setTextSize(18);
                    textView.setPadding(0, 0, 0, 16);

                    layoutReporte.addView(textView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
