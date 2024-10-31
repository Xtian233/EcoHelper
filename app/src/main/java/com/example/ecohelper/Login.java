package com.example.ecohelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private EditText edtUsuario, edtContraseña;
    private ImageView imvNoVisible;
    private CheckBox chkRecordarme;
    private TextView txvOlvidasteContraseña;
    private Button btnIniciarSesion;
    private boolean mostrarContraseña = false;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mAuth = FirebaseAuth.getInstance();

        edtUsuario = findViewById(R.id.edtUsuario);
        edtContraseña = findViewById(R.id.edtPassword);
        imvNoVisible = findViewById(R.id.imvNoVisible);
        chkRecordarme = findViewById(R.id.chkRecordarme);
        txvOlvidasteContraseña = findViewById(R.id.txvOlvidastePassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion2);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean recordarme = prefs.getBoolean("Recordarme", false);
        if (recordarme) {
            String lastEmail = prefs.getString("LastEmail", "");
            String lastPassword = prefs.getString("LastPassword", "");

            edtUsuario.setText(lastEmail);
            edtContraseña.setText(lastPassword);
            chkRecordarme.setChecked(true);
        }

        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarSesion();
            }
        });

        imvNoVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarOcultarContraseña();
            }
        });

        txvOlvidasteContraseña.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recuperarContraseña();
            }
        });
    }

    private void iniciarSesion() {
        final String usuario = edtUsuario.getText().toString().trim();
        final String contraseña = edtContraseña.getText().toString().trim();

        if (TextUtils.isEmpty(usuario) || TextUtils.isEmpty(contraseña)) {
            Toast.makeText(Login.this, "Por favor, ingrese usuario y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmailValid(usuario)) {
            Toast.makeText(Login.this, "Por favor, ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(usuario, contraseña)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Toast.makeText(Login.this, "Iniciando sesión como " + usuario, Toast.LENGTH_SHORT).show();
                                if (chkRecordarme.isChecked()) {
                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Login.this);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("LastEmail", usuario);
                                    editor.putString("LastPassword", contraseña);
                                    editor.putBoolean("Recordarme", true);
                                    editor.apply();
                                } else {
                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Login.this);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.remove("LastEmail");
                                    editor.remove("LastPassword");
                                    editor.remove("Recordarme");
                                    editor.apply();
                                }
                                Intent intent = new Intent(Login.this, MenuPrincipal.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(Login.this, "Error en la autenticación.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Login.this, "Error en la autenticación: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void mostrarOcultarContraseña() {
        if (mostrarContraseña) {
            edtContraseña.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imvNoVisible.setImageResource(R.drawable.novisible);
        } else {
            edtContraseña.setTransformationMethod(null);
            imvNoVisible.setImageResource(R.drawable.visible);
        }
        mostrarContraseña = !mostrarContraseña;
    }

    private void recuperarContraseña() {
        Toast.makeText(Login.this, "Comunícate con el área de gestión ambiental", Toast.LENGTH_SHORT).show();
    }
}
