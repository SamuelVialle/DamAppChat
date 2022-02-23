package com.samuelvialle.damappchat.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.samuelvialle.damappchat.common.Util;
import com.samuelvialle.damappchat.NoInternetActivity;
import com.samuelvialle.damappchat.R;

import org.jetbrains.annotations.NotNull;

public class ResetPaswordActivity extends AppCompatActivity {

    /**
     * Variables globales
     **/
    private TextInputEditText etEmail;
    private TextView tvMessage;
    private LinearLayout llResetPassword, llMessageResetPassword;
    private Button btnRetry;
    private View progressBar; // PB1

    /**
     * Méthode initUI pour lier le code et le design ou in,itialisation des widgets
     **/
    private void initUi() {
        etEmail = findViewById(R.id.etEmail);
        tvMessage = findViewById(R.id.tvMessage);
        llResetPassword = findViewById(R.id.llResetPassword);
        llMessageResetPassword = findViewById(R.id.llMessageResetPassword);
        btnRetry = findViewById(R.id.btnRetry);
        progressBar = findViewById(R.id.progressBar); // PB2
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_reset_pasword);

        // Appel des méthodes
        initUi();

        /** Association du clic dans le keyboard **/
        etEmail.setOnEditorActionListener(editorActionListener);
    }

    /**
     * Méthode ResetPassword
     **/
    public void btnResetPasswordClick(View v) {
        // Extratction de l'email de l'editText
        String email = etEmail.getText().toString().trim();
        // Vérifications
        if (email.equals("")) {
            etEmail.setError(getString(R.string.enter_email));
        } else {
            // 9 Ajout de la vérification de la connection internet
            if (Util.connectionAvailable(this)) // Si la connexion fonctionne
            { // Alors on exécute la méthode
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                //PB3
                progressBar.setVisibility(View.VISIBLE);
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        // PB4
                        progressBar.setVisibility(View.GONE);
                        // On change la visibilité des Linear Layout
                        llResetPassword.setVisibility(View.GONE);
                        llMessageResetPassword.setVisibility(View.VISIBLE);

                        if (task.isSuccessful()) {
                            tvMessage.setText(getString(R.string.reset_password_instructions, email));
                            // Dans ce cas il faut afficher le bouton retry mais comme l'utilisateur vient juste d'envoyer
                            // un mail il faut le faire attendre avant de recommencer, nous allons implémenter un Timer
                            new CountDownTimer(60000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    // Affichage du texte sur le bouton retry
                                    btnRetry.setText(getString(R.string.resend_timer, String.valueOf(millisUntilFinished / 1000)));
                                    // Blocage du clic sur le bouton
                                    btnRetry.setOnClickListener(null);
                                }

                                @Override
                                public void onFinish() {
                                    btnRetry.setText(R.string.retry);

                                    btnRetry.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            llResetPassword.setVisibility(View.VISIBLE);
                                            llMessageResetPassword.setVisibility(View.GONE);

                                        }
                                    });
                                }
                            }.start();

                        } else {
                            tvMessage.setText(getString(R.string.failed_to_send_email, task.getException()));
                            btnRetry.setText(R.string.retry);

                            btnRetry.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    llResetPassword.setVisibility(View.VISIBLE);
                                    llMessageResetPassword.setVisibility(View.GONE);
                                }
                            });
                        }

                    }
                });
                // 9.1 Sinon
            } else {
                startActivity(new Intent(ResetPaswordActivity.this, NoInternetActivity.class));
            }
        }
    }

    /**
     * Méthode Close
     **/
    public void btnCloseClick(View v) {
        finish();
    }

    /**  Ajout des boutons next et send à la place du retour chariot du keyboard **/
    private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // Utilisation de actionId qui correspond à l'action ajouter dans le xml
            switch (actionId){
                case EditorInfo.IME_ACTION_DONE:
                    btnResetPasswordClick(v);
            }
            return false; // On laisse le return à false pour empêcher le comportement normal du clavier
        }
    };
}