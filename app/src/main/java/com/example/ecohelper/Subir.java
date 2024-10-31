package com.example.ecohelper;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class Subir extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 123;
    private Uri imageUri;
    private Spinner spinnerEtiquetas;
    private Button btnSubirImagen;
    private ImageView imgPreview;
    private Button btnRegresar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private void limpiarCampos() {
        imgPreview.setImageDrawable(null);
        imageUri = null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subir);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        spinnerEtiquetas = findViewById(R.id.spinnerEtiquetas);
        btnSubirImagen = findViewById(R.id.btnSubirImagen);
        imgPreview = findViewById(R.id.imgPreview);
        btnRegresar = findViewById(R.id.btnRegresar);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.etiquetas_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEtiquetas.setAdapter(adapter);

        btnSubirImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subirImagen();
            }
        });

        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Subir.this, MenuPrincipal.class);
                startActivity(intent);
                finish();
            }
        });

        imgPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarImagen(v);
            }
        });
    }

    public void buscarImagen(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private void subirImagen() {
        if (imageUri != null) {
            final String etiqueta = spinnerEtiquetas.getSelectedItem().toString();
            StorageReference fileReference = mStorage.child("uploads").child(etiqueta).child(System.currentTimeMillis() + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            String userId = mAuth.getCurrentUser().getUid();

                            Map<String, Object> imageMap = new HashMap<>();
                            imageMap.put("imageUrl", downloadUrl);
                            imageMap.put("etiqueta", etiqueta);

                            mDatabase.child("imagenes").child(userId).push().setValue(imageMap)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(Subir.this, "Imagen subida correctamente", Toast.LENGTH_SHORT).show();
                                        limpiarCampos();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Subir.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Subir.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Selecciona una imagen para subir", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imgPreview.setImageURI(imageUri);
        }
    }
}
