package com.samuelvialle.damappchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.samuelvialle.damappchat.common.Util;

public class NoInternetActivity extends AppCompatActivity {
    /** On vérifie si il ya une connexion internet valide
     * en commeçant par l'ajout d'une classe Util dans la package common **/

    /** Var globales **/
    private TextView tvNoINternet;
    private ProgressBar pbNoInternet;

    // Ajout d'un objet NetworkCallBack pour connaitre l'état de la connexion
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

        // Initialisation
        tvNoINternet = findViewById(R.id.tvNoInternet);
        pbNoInternet = findViewById(R.id.pbNoInternet);

        // NetworkCallback ne fonctionne que sur les API 21 et + on va donc vérifier cela
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            networkCallback = new ConnectivityManager.NetworkCallback(){
                // Il faut override 2 méthodes :
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    // Si la connection existe on ferme cette activité et on retourne à la précédente
                    finish();
                }
                // la seconde
                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    // Si la connexion est perdue on affiche alors le TextView
                    tvNoINternet.setText(R.string.no_internet);
                }
            };
            // Il faut enregister ce callback dans le connectivityManager
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            // L'enregistrement prend 2 paramètres et alors on peut vérifier la connection
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build(),
            networkCallback); // S'il y a un changement cela sera pris en compte dans le callback
            // et donc passera dans les 2 méthodes ci-dessus
        }
    }

    // Il ne reste plus qu'à affecter 2 méthodes pour l'utilisation des boutons
    public void btnRetryClick(View v){
        // Affichage de la pb
        pbNoInternet.setVisibility(View.VISIBLE);
        // On teste alors avec la classe Util la connexion dans ce contexte
        if(Util.connectionAvailable(this)){
            finish(); // Si c'est bon on ferme cette activité
        } else {
            // Pour faire comprendre à l'utilisateur que l'on est toujours en recherche d'internet
            // on va ajouter un handler avec un delay de 2 secondes et faire disparaître la pb
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    pbNoInternet.setVisibility(View.GONE);
                }
            }, 1000);
        }
    }

    public void btnCloseClick(View view) {
        finishAffinity(); // On ferme alors toute l'application
    }

    // Ne pas oublier d'jouter les méthodes aux boutons

    // Ensuite il faut appeler cette activité à chaque fois que nous devons nous connecter à internet et
    // donc à Firebase
}