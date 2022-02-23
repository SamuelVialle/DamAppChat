package com.samuelvialle.damappchat.findfriends;

import static com.samuelvialle.damappchat.common.Constants.CURRENT_USER;
import static com.samuelvialle.damappchat.common.Constants.FIRESTORE_INSTANCE;
import static com.samuelvialle.damappchat.common.Constants.FRIEND_REQUESTS;
import static com.samuelvialle.damappchat.common.Constants.NAME;
import static com.samuelvialle.damappchat.common.Constants.USERS;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.samuelvialle.damappchat.R;
import com.samuelvialle.damappchat.login.SignupActivity;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 1 Suppression des données sauf du constructeur
 **/
public class FindFriendsFragment extends Fragment {


    /**
     * 2 Ajouts des variables globales
     **/
    // La vue pour les éléments
    private View view;       // Permet son utilisation dans la méthode init ;)
    // Le recyclerView
    private RecyclerView rvFindFriends;
    // L'adapter pour faire le lien entre les données de FB et le design
    private FindFriendAdapter findFriendAdapter;
    // La liste des données
    private List<FindFriendModel> findFriendModelList;
    // Le text view dans le cas où il n'y ai pas d'entrées dans la db
    private TextView tvEmptyFriendList;

    // Les variables pour la connextion à la db
    // La référence vers les collections
    private CollectionReference usersCollectionReference; // La base Users pour récupérer les informations
    private CollectionReference friendRequestCollectionReference; // La base friendRequest

    // La référence de l'utilisateur courant
    private FirebaseUser currentUser;

    // La progressBar
    private View progressBar;

    // Définition du String avatar pour la gestion de l'affichage de l'image dans le RecyclerView
    String avatar;

    public FindFriendsFragment() {
        // Required empty public constructor !!
    }

    private void initUI() {
        // Initialisation des vues (lien design // code )
        rvFindFriends = view.findViewById(R.id.rvFindFriends);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyFriendList = view.findViewById(R.id.tvEmptyFriendList);

        // Initialisation du recyclerView en utilisant le layoutManager pour ajouter des lignes automatiquements dans le recycler
        rvFindFriends.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void initFB() {
        // Gestion de l'utilisateur courant
        currentUser = CURRENT_USER;

        // Initialisation des bases
        usersCollectionReference = FIRESTORE_INSTANCE.collection(USERS);
        friendRequestCollectionReference = FIRESTORE_INSTANCE.collection(FRIEND_REQUESTS);
    }

    /**
     * 2 On ne change rien dans la vue tout convient pour l'affichage
     **/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_find_friends, container, false);
        return view;
    }

    /**
     * 3 On va utiliser le onViewCreated pour effectuer les différentes actions personnalisables de notre app
     **/
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUI();
        initFB();

        // Initialisation du textView en cas de liste vide
        tvEmptyFriendList.setVisibility(View.VISIBLE);

        // Initialisation de la progressBar pour la rendre visible le temps de la recherche dans la base
        progressBar.setVisibility(View.VISIBLE);

        // Appel de la méthode pour afficher les données des users dans le recyclerView
        getUsers();
    }

    Query queryUsersCollection;

    private void getUsers() {
        // Query de tout les utilisateurs pour les afficher dans le recyclerView
        queryUsersCollection = usersCollectionReference
                .orderBy(NAME, Query.Direction.ASCENDING);


        // Gestion de l'affichage des données dans le RecyclerView
        FirestoreRecyclerOptions<FindFriendModel> findFriendModelFirestoreRecyclerOptions =
                new FirestoreRecyclerOptions.Builder<FindFriendModel>()
                        .setQuery(queryUsersCollection, FindFriendModel.class)
                        .build();
        findFriendAdapter = new FindFriendAdapter(findFriendModelFirestoreRecyclerOptions);
        rvFindFriends.setAdapter(findFriendAdapter);
        findFriendAdapter.startListening();




        // Gestion de l'affichage du message si la base n'est pas vide
        tvEmptyFriendList.setVisibility(View.GONE);
        // Gestion de l'affichage de la progressBar
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (CURRENT_USER == null) {
            startActivity(new Intent(getActivity(), SignupActivity.class));
        } else {
            findFriendAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        findFriendAdapter.stopListening();
    }
}