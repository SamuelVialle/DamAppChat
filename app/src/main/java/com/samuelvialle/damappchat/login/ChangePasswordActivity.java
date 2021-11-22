package com.samuelvialle.damappchat.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.samuelvialle.damappchat.Common.Util;
import com.samuelvialle.damappchat.NoInternetActivity;
import com.samuelvialle.damappchat.R;

import org.jetbrains.annotations.NotNull;

public class ChangePasswordActivity extends AppCompatActivity {

    /**
     * Variables globales
     **/
    private TextInputEditText etPassword, etConfirmPassword;
    //PB
    private View progressBar;

    private void initUI() {
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBar); // PB2

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_change_password);

        // Appel des méthodes
        initUI();

        /** 10.2 Association du clic dans le keyboard **/
        etConfirmPassword.setOnEditorActionListener(editorActionListener);
    }

    public void btnSaveNewPassword(View v) {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (password.equals("")) {
            etPassword.setError(getString(R.string.enter_password));
        } else if (confirmPassword.equals("")) {
            etConfirmPassword.setError(getString(R.string.confirm_password));
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.password_mismatch));
        } else {
            // 9 Ajout de la vérification de la connection internet
            if (Util.connectionAvailable(this)) // Si la connexion fonctionne
            { // Alors on exécute la méthode
                //Upadate password
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                //PB3
                progressBar.setVisibility(View.VISIBLE);
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                if (firebaseUser != null) {
                    firebaseUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE); //PB4
                            if (task.isSuccessful()) {
                                Toast.makeText(ChangePasswordActivity.this, R.string.password_changed_successfully, Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, getString(R.string.something_went_wrong, task.getException()),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    // 9.1 Sinon
                } else {
                    startActivity(new Intent(ChangePasswordActivity.this, NoInternetActivity.class));
                }
            }
        }
    }

    /** 10.2 Ajout des boutons next et send à la place du retour chariot du keyboard **/
    private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // Utilisation de actionId qui correspond à l'action ajouter dans le xml
            switch (actionId){
                case EditorInfo.IME_ACTION_DONE:
                    btnSaveNewPassword(v);
            }
            return false; // On laisse le return à false pour empêcher le comportement normal du clavier
        }
    };

}