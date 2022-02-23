package com.samuelvialle.damappchat.findfriends;

import static com.samuelvialle.damappchat.common.Constants.CURRENT_USER;
import static com.samuelvialle.damappchat.common.Constants.FIRESTORE_INSTANCE;
import static com.samuelvialle.damappchat.common.Constants.FRIEND_REQUESTS;
import static com.samuelvialle.damappchat.common.Constants.USERS;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.samuelvialle.damappchat.R;

import java.util.HashMap;

public class FindFriendAdapter extends FirestoreRecyclerAdapter<FindFriendModel, FindFriendAdapter.FindFriendViewHolder> {
    /**
     * Variables globales
     **/
    private String userId; // Pour récupérer l'id de l'utilisateur courant
    private static final String TAG = "FindFriendAdapter";

    // Gestion des bases de données
    CollectionReference usersCollectionReference = FIRESTORE_INSTANCE.collection(USERS);
    CollectionReference friendRequestCollectionReference = FIRESTORE_INSTANCE.collection(FRIEND_REQUESTS);

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FindFriendAdapter(@NonNull FirestoreRecyclerOptions<FindFriendModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position, @NonNull FindFriendModel model) {
        /** 6 Comme la view est associée à l'adapter, il est maintenant possible d'accéder aux objets via le holder **/
        String fullName = model.getName();
        String userAvatar = model.getAvatar();

        // Le nom depuis les informations de Authenticator
        holder.tvFullName.setText(fullName);

        // L'image de l'avatar depuis le storage
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_avatar_256x256)
                .error(R.drawable.default_avatar_256x256);

        Context context = holder.ivProfile.getContext();
        Glide.with(context)
                .load(userAvatar)
                .apply(options)
                .fitCenter()
                .override(150, 150)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivProfile);

        // Gestion des boutons pour envoyer les requetes
        if (model.isRequestSent()) {
            holder.btnSendRequest.setVisibility(View.GONE);
            holder.btnCancelRequest.setVisibility(View.VISIBLE);
        } else {
            holder.btnSendRequest.setVisibility(View.VISIBLE);
            holder.btnCancelRequest.setVisibility(View.GONE);
        }

        if (FindFriendModel.isRequestSent()) {
            holder.btnSendRequest.setVisibility(View.GONE);
            holder.btnCancelRequest.setVisibility(View.VISIBLE);
        } else {
            holder.btnSendRequest.setVisibility(View.VISIBLE);
            holder.btnCancelRequest.setVisibility(View.GONE);
        }


    }

    @NonNull
    @Override
    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /** 5 Dans cette méthode on va inflate le layout item_find_friend, de plus on encapsuler cet inflate dans une view pour
         //         * pouvoir l'utiliser par la suite **/
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_find_friends, parent, false);

        // 5.1 On va utiliser le viewHolder de la classe pour afficher la vue créée ci-dessus
        return new FindFriendViewHolder(view);
    }

    @Override
    public void onError(@NonNull FirebaseFirestoreException e) {
        Log.e(TAG, "Error Firebase : " + e.getMessage());
    }

    public class FindFriendViewHolder extends RecyclerView.ViewHolder {
        // Variables lien design code
        private final ImageView ivProfile;
        private final TextView tvFullName;
        private final Button btnSendRequest, btnCancelRequest;
        private final ProgressBar pbRequest;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialiastion des vues
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            btnSendRequest = itemView.findViewById(R.id.btnSendRequest);
            btnCancelRequest = itemView.findViewById(R.id.btnCancelRequest);
            pbRequest = itemView.findViewById(R.id.pbRequest);

            // On récupère l'id de l'utilisateur courant
            userId = CURRENT_USER.getUid();
            // Création de la référence vers le document
            DocumentReference friendRequest = friendRequestCollectionReference.document(userId);

            //Changement des boutons en fonction de l'état dans la base
            friendRequest.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                Log.i(TAG, "onComplete: " + document.getData());
                                //TODO LA SUITE POUR AFFICHER LES BOUTONS DANS LE BON SENS 
                            }
                        }
                    });


            btnSendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(position);
                        // Récupération de l'Id de l'user auquel on envoi la demande
                        String sendFirendRequestToUserId = documentSnapshot.getId();
                        // Création de l'envoi vers FindFriendRequest db avec un HashMap
                        HashMap<String, Object> hashMap = new HashMap<>();
                        // Création de la paire clé / valeur
                        hashMap.put(sendFirendRequestToUserId, "sent");
                        // Vérification de l'existance du document en question
                        friendRequest.get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                // Is le document existe on fait un update
                                                friendRequest.update(hashMap);
                                            } else {
                                                // Sinon on le crée
                                                friendRequest.set(hashMap);
                                            }
                                        }
                                    }
                                });
                    }
                    // Changemnt des boutons
                    btnSendRequest.setVisibility(View.GONE);
                    btnCancelRequest.setVisibility(View.VISIBLE);
                }
            });

            btnCancelRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(position);
                        String sendFirendRequestToUserId = documentSnapshot.getId();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put(sendFirendRequestToUserId, "canceled");
                        DocumentReference friendRequest = friendRequestCollectionReference.document(userId);
                        friendRequest.update(hashMap);
                    }
                    // Changemnt des boutons
                    btnSendRequest.setVisibility(View.VISIBLE);
                    btnCancelRequest.setVisibility(View.GONE);
                }
            });
        }
    }
}

