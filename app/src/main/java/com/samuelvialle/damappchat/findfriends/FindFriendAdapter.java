package com.samuelvialle.damappchat.findfriends;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.samuelvialle.damappchat.Common.Constants;
import com.samuelvialle.damappchat.Common.NodesNames;
import com.samuelvialle.damappchat.R;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

import java.util.List;

/**
 * 1 Étendre la classe à RecyclerView.Adapter avec comme attribut le ViewHolder qui sera créé dans cette classe
 * il est cependant possible de l'externaliser
 * Les erreurs se corrigent avec [ALT] + [ENTRÉE]
 **/
public class FindFriendAdapter extends RecyclerView.Adapter<FindFriendAdapter.FindFriendViewHolder> {

    /**
     * 3 Initialisation de variables globales
     **/
    // 3.1 Le context
    private Context context;
    // 3.2 Une liste basée sur le modèle FindFriendModel
    private List<FindFriendModel> findFriendModelList;

    /**
     * 8 Envoyer la friend request
     **/
    // Déclaration des variables de Firebase
    private DatabaseReference friendRequestDatabase;
    private FirebaseUser currentUser;

    // 8.4 Création de la var usrID
    private String userId;


    /**
     * 4 Génération d'un constructeur récupérer les informations du contexte et de la liste présente dans le fragment
     **/
    public FindFriendAdapter(Context context, List<FindFriendModel> findFriendModelList) {
        this.context = context;
        this.findFriendModelList = findFriendModelList;
    }

    @NonNull
    @NotNull
    @Override
    public FindFriendAdapter.FindFriendViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        /** 5 Dans cette méthode on va inflate le layout item_find_friend, de plus on encapsuler cet inflate dans une view pour
         * pouvoir l'utiliser par la suite **/
        View view = LayoutInflater.from(context).inflate(R.layout.item_find_friends, parent, false);

        // 5.1 On va utiliser le viewHolder de la classe pour afficher la vue créée ci-dessus
        return new FindFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull FindFriendAdapter.FindFriendViewHolder holder, int position) {
        /** 6 Comme la view est associée à l'adapter, il est maintenant possible d'accéder aux objets via le holder
         * position permet de numéroter l'emplacement de chaque item dans le recycler **/
        // On fait appel au model pour accéder au contenu de la liste créée à partir de l'adapter et placer les élements
        // en fonction de leur position
        final FindFriendModel findFriendModel = findFriendModelList.get(position);

        // On peut alors affecter les valeurs de la liste pour chaque éléments en utilisant l'objet holder

        // Le nom depuis les informations de Authenticator
        holder.tvFullName.setText(findFriendModel.getUserName());

        // L'image de l'avatar depuis le storage
        // La référence provient du fichier constants dans le package Common
        StorageReference fileRef = FirebaseStorage.getInstance()
                .getReference()
                .child(Constants.IMAGES_FOLDER + "/" + findFriendModel.getAvatar());
        // On récupère alors l'url de l'Avatar
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // On utilise Glide pour afficher l'image récupérer
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.default_avatar_256x256)
                        .error(R.drawable.default_avatar_256x256)
                        .into(holder.ivProfile);
            }
        });
        // 8.1 Initialisation des éléments de firebase
        // On ajoute d'abord la référence dans les nodes
        friendRequestDatabase = FirebaseDatabase.getInstance().getReference().child(NodesNames.FRIEND_REQUESTS);
        // 8.2 Inititalisation du currentUser
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // 8.3 Association du listener au bouton pour envoyer la request
        holder.btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Une fois que l'utilisateur clique on fait disparaître le btn
                holder.btnSendRequest.setVisibility(View.GONE);
                // Pour laisser place à la progressBar
                holder.pbRequest.setVisibility(View.VISIBLE);

                // On doit récupérer l'id du User cliqué (créer une var userId) grace à la position
                userId = findFriendModel.getUserId();
                // On fait alors référence à la db et plus particulièrement au child du currentUser dans lequel on va insérer l'id du user
                // qui vient d'être demandé en ami et l'état de la request (ajouter le node dans nodesNames)
                friendRequestDatabase.child(currentUser.getUid()).child(userId).child(NodesNames.REQUEST_TYPE)
                        // Puis on va ajouter la valeur en fonction de la demande (valeur enregistrer dans les constantes)
                        .setValue(Constants.REQUEST_STATUS_SENT)
                        // Ajout d'un addOnCompleteListener pour vérifier la bonne exécution
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // On ajoute alors le fait qu'il y ai bonne réception dans la partie de l'user à qui l'on fait la demande
                                    friendRequestDatabase.child(userId).child(currentUser.getUid()).child(NodesNames.REQUEST_TYPE)
                                            .setValue(Constants.REQUEST_STATUS_RECEIVED)
                                            // Idem on ajoute addOnCompleteListener
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // On affiche un Toast
                                                        Toast.makeText(context, R.string.request_sent_succesfully, Toast.LENGTH_SHORT).show();
                                                        // Une fois la request envoyée, on change la visibility
                                                        holder.btnSendRequest.setVisibility(View.GONE);
                                                        holder.pbRequest.setVisibility(View.GONE);
                                                        holder.btnCancelRequest.setVisibility(View.VISIBLE);
                                                    } else { // Sinon on affiche un Toast avec l'erreur
                                                        Toast.makeText(context, context.getString(R.string.failed_tosent_friend_request,
                                                                task.getException())
                                                                , Toast.LENGTH_LONG).show();
                                                        // On change alors la visibility
                                                        holder.btnSendRequest.setVisibility(View.VISIBLE);
                                                        holder.pbRequest.setVisibility(View.GONE);
                                                        holder.btnCancelRequest.setVisibility(View.GONE);

                                                    }
                                                }
                                            });
                                } // On recopie le else ci dessus pour valider le premier if
                                else { // Sinon on affiche un Toast avec l'erreur
                                    Toast.makeText(context, context.getString(R.string.failed_tosent_friend_request,
                                            task.getException())
                                            , Toast.LENGTH_LONG).show();
                                    // On change alors la visibility
                                    holder.btnSendRequest.setVisibility(View.VISIBLE);
                                    holder.pbRequest.setVisibility(View.GONE);
                                    holder.btnCancelRequest.setVisibility(View.GONE);

                                }
                            } // On peut alors tester l'app et on obsrve l'affichage dans FB et aussi le fait que si l'on ferme l'app
                            // puis on la rouvre les notifications d'envois ne sont pas synchronisées il faut donc modifier
                            // le constructeur que nous avons laissé à false dans FindFriendFragment
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        /** 7 Cette méthode sert à compter le nombre d'item au total **/
        // Il remplacer la ligne suivante par le nombre de ligne de la liste (avec size)
        // return 0;
        return findFriendModelList.size();
    }

    /**
     * 2 Cette classe sert à initialiser les widgets dans le code
     */
    public class FindFriendViewHolder extends RecyclerView.ViewHolder {
        // Variables lien design code
        private ImageView ivProfile;
        private TextView tvFullName;
        private Button btnSendRequest, btnCancelRequest;
        private ProgressBar pbRequest;

        public FindFriendViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            // Initialiastion des vues
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            btnSendRequest = itemView.findViewById(R.id.btnSendRequest);
            btnCancelRequest = itemView.findViewById(R.id.btnCancelRequest);
            pbRequest = itemView.findViewById(R.id.pbRequest);
        }
    }
}
