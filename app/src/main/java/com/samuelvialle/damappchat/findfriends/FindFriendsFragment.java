package com.samuelvialle.damappchat.findfriends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.samuelvialle.damappchat.Common.NodesNames;
import com.samuelvialle.damappchat.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 1 Suppression des données sauf du constructeur
 **/
public class FindFriendsFragment extends Fragment {

    /**
     * 2 Ajouts des variables globales
     **/
    // Le recyclerView
    private RecyclerView rvFindFriends;
    // L'adapter pour faire le lien entre les données de FB et le design
    private FindFriendAdapter findFriendAdapter;
    // La liste des données
    private List<FindFriendModel> findFriendModelList;
    // Le text view dans le cas où il n'y ai pas d'entrées dans la db
    private TextView tvEmptyFriendList;

    // Les variables pour la connextion à la db
    // La référence vers la base
    private DatabaseReference databaseReference;
    // La référence vers l'utilisateur courant
    private FirebaseUser currentUser;

    // La progressBar
    private View progressBar;

    // Définition du String avatar pour la gestion de l'affichage de l'image dans le RecyclerView
    String avatar;

    public FindFriendsFragment() {
        // Required empty public constructor
    }


    /**
     * 2 On ne change rien dans la vue tout convient pour l'affichage
     **/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_friends, container, false);
    }

    /**
     * 3 On va utiliser le onViewCreated pour effectuer les différentes actions personnalisables de notre app
     **/
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialisation des vues (lien design // code )
        rvFindFriends = view.findViewById(R.id.rvFindFriends);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyFriendList = view.findViewById(R.id.tvEmptyFriendList);

        // Initialisation du recyclerView en utilisant le layoutManager pour ajouter des lignes automatiquements dans le recycler
        rvFindFriends.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Initialisation de la liste
        findFriendModelList = new ArrayList<>();
        // Initialisation de l'adapter à partir la liste
        findFriendAdapter = new FindFriendAdapter(getActivity(), findFriendModelList);

        // Association de la liste au Recycler grace à l'adapter
        rvFindFriends.setAdapter(findFriendAdapter);

        // Initialisation de la db Firebase avec le node USERS pour tous les récupérer
        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodesNames.USERS);

        // Gestion de l'utilisateur courant
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialisation du textView en cas de liste vide
        tvEmptyFriendList.setVisibility(View.VISIBLE);

        // Initialisation de la progressBar pour la rendre visible le temps de la recherche dans la base
        progressBar.setVisibility(View.VISIBLE);

        // De plus nous allons ordonner les résultats pour les afficher dans l'ordre Alphabéthique
        Query query = databaseReference.orderByChild(NodesNames.NAME);
        // Ajout d'une query pour écouter le résultat et les afficher grace au snapshot
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                // On commence par effacer la liste
                findFriendModelList.clear();
                // Puis on force l'affichage par ordre alphabétique
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // On recherche dans le tableau crée par le snapshot tout les objets ds
                    String userId = ds.getKey();
                    // On retire l'utilisateur courant pour qu'il s'envoie pas de request
                    if (userId.equals(currentUser.getUid()))
                        return;

                    // On vérifie que le nom ne soit pas égale à null
                    if (ds.child(NodesNames.NAME).getValue() != null) {
                        // Alors on affiche la variable avec le nom complet de l'utilisateur
                        String fullName = ds.child(NodesNames.NAME).getValue().toString();
                        if (ds.child(NodesNames.AVATAR).getValue() != null) {
                            // Penser à définir le String avatar en global
                            avatar = ds.child(NodesNames.AVATAR).getValue().toString();
                        } else {
                            // On transforme le path du drawable en string
                            avatar = "drawable://" + R.drawable.ic_user;
                        }
                        // On utilise le constructeur de findFriendModel pour ajouter les données
                        // Ici il y a 4 attributs : fullName, avatar, userId et si l'on a envoyé une request
                        // Pour le moment cette option sera laissé à False
                        findFriendModelList.add(new FindFriendModel(fullName, avatar, userId, false));

                        // On notifie à l'adpter que les données on changées
                        findFriendAdapter.notifyDataSetChanged();

                        // A partir d'ici on est sur d'avoir les données de la db donc on peut cacher le tvEmpty et la progressBar
                        tvEmptyFriendList.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                /** 4 Gestion des erreurs **/
                progressBar.setVisibility(View.GONE);
                // Affichage d'un Toast d'erreur
                Toast.makeText(getContext(),
                        getContext().getString(R.string.failed_to_fetch_friend, error.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
}