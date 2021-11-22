package com.samuelvialle.damappchat.splashscreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.samuelvialle.damappchat.R;
import com.samuelvialle.damappchat.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    /** 1 Variables globales **/
    private ImageView ivSplash;
    private TextView tvSplash;
    private Animation animation; // Objet d'animation

    /** 2 Méthode initUi() **/
    public void initUI(){
        ivSplash = findViewById(R.id.ivSplash);
        tvSplash = findViewById(R.id.tvSplash);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /** 8 On retire l'action bar pour plus d'imertion **/
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        /** 3 Initialisation des composants graphiques **/
        initUI();

        /** 4 Initialisation du composant de l'animation créer dans res/anim **/
        animation = AnimationUtils.loadAnimation(this, R.anim.splash_animation);

        /** 5 Ajout du listener de l'animation **/
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Lorsque l'animation fini on redirige l'utilisateur vers Login de cette façon on ne
                // retourne pas sur cette activité lorsque back est pressé
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /** 6 Démarrage de l'animationdans la méthode onStart **/
    @Override
    protected void onStart() {
        super.onStart();
        ivSplash.startAnimation(animation);
        tvSplash.startAnimation(animation);
    }

    /** 7 Changer l'ordre de démarrage dans le manifest **/
}