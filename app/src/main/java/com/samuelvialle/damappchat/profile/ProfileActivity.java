package com.samuelvialle.damappchat.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.samuelvialle.damappchat.Common.NodesNames;
import com.samuelvialle.damappchat.R;
import com.samuelvialle.damappchat.login.ChangePasswordActivity;
import com.samuelvialle.damappchat.login.LoginActivity;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "###### Profile Activity";
    /**
     * 1.1 Variables globales des widgets
     **/
    private TextInputEditText etName, etEmail;
    private ImageView ivAvatar;
    private View progressBar; // PB

    /**
     * 1.2 Variables globales de Firebase
     **/
    private FirebaseUser firebaseUser;
    private DocumentReference userReference;
    private StorageReference fileStorage;
    private FirebaseAuth firebaseAuth;

    /**
     * 1.3 Variables globales pour les URI
     **/
    private Uri localFileUri, serverFileUri;
    /** 1.3.1 Var du UserId **/
    private String userID;

    /** 13 Définitions des variables pour tester avant l'envoi de l'update **/
    String nameToUpdate, emailToUpdate;

    /**
     * 2 Méthode pour l'initialisation des composants initUI()
     **/
    public void initUI() {
        etEmail = findViewById(R.id.etEmail);
        etName = findViewById(R.id.etName);
        ivAvatar = findViewById(R.id.ivAvatar);
        progressBar = findViewById(R.id.progressBar); //PB2

    }

    /**
     * 3 Méthode pour l'initialisation des composants Firebase
     **/
    public void initFirebase() {
        // Instance pour le storage des images
        fileStorage = FirebaseStorage.getInstance().getReference();
        // Pour vérifier que l'utilisateur est bien connecté
        firebaseAuth = FirebaseAuth.getInstance(); // Utilisé pour se déconnecter
        firebaseUser = firebaseAuth.getCurrentUser();
        // Si l'utilisateur n'est pas vide alors on rempli les champs
        // Gestion du userID
        userID = firebaseUser.getUid();
        /** 6.2 Insertion dans la base de données **/
        userReference = FirebaseFirestore
                .getInstance() // Obtient une instance de connexion à la db
                .collection(NodesNames.USERS) // Cherche la référence souhaitée à partir de la racine de la db
                .document(userID);// L'id de l'utilisateur courant pour être modifié
        Log.i(TAG, "initFirebase: " + userID);
    }

    private void initProfileUser() {
        if (firebaseUser != null) {
            etName.setText(firebaseUser.getDisplayName());
            etEmail.setText(firebaseUser.getEmail());
            serverFileUri = firebaseUser.getPhotoUrl();

            if (serverFileUri != null) {
                // Pour afficher l'image de l'avatar nous allons utiliser la librairie Glide
                // Commencer par ajouter dans le gradle module les dépendances de glide

                // Glide dans sa dernière version a besoin d'un appendice pour gérer les erreurs de connexion
                RequestOptions options = new RequestOptions()
                        .centerCrop() // On place l'image au centre et on découpe en fonction de la taille du conteneur de l'image
                        .circleCrop() // Découpe l'image en cercle comme circularImageView
                        .error(R.drawable.ic_user) // Qu'afficher en cas d'erreur ? L'image par défaut
                        .placeholder(R.drawable.ic_user); // Qu'afficher par défaut ? L'iamge par défaut

                Glide.with(getApplicationContext()) // Dans quel contexte Glide est utilisé ? this = cette activité
                        .load(serverFileUri) // Que doit-on loader ? L'image enregistrée sur le serveur
                        .apply(options) // application des options en cas d'erreurs
                        .fitCenter() // Resize l'image et la place au centre
                        .circleCrop() // Découpe l'image en cercle comme circularImageView
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Gestio ndu cache
                        .into(ivAvatar); // A quel endroit ? Dans le widget ivAvatar
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_profile);

        /** 4 Appel des méthodes d'initialisation **/
        initUI();
        initFirebase();
        initProfileUser();

        /** 12.2 Association du clic dans le keyboard **/
        etName.setOnEditorActionListener(editorActionListener);
        etEmail.setOnEditorActionListener(editorActionListener);
    }

    /**
     * 5 Copie des méthodes updateOnlyUser et updateNameAndPhoto
     * sans oublier de changer le context par celui de l'activité dans laquelle nous sommes
     **/
    private void updateNameOnly() {
        // PB3
        progressBar.setVisibility(View.VISIBLE);
        nameToUpdate = etName.getText().toString().trim();
        emailToUpdate = etEmail.getText().toString().trim();
        // Utilisation de la méthode UserProfileChangeRequest pour charger le nom de l'utilisateur
        // qui s'est enregistré
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .build();
        /** 5.3 Update du nom du profile utilisateur à partir de l'editText  **/
        firebaseUser.updateProfile(request)
                // Ajout d'un listener qui affiche un Toast si tout c'est bien déroulé
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                        //PB4
                        progressBar.setVisibility(View.GONE);
                        // Tout c'est bien passé
                        if (task.isSuccessful()) {
                            // Insertion dans la bd à l'aide d'un hashmap
                            HashMap<String, Object> hashMap = new HashMap<>();
                            if (firebaseUser.getDisplayName() != nameToUpdate) {
                                hashMap.put(NodesNames.NAME, nameToUpdate);
                            }
                            if (firebaseUser.getEmail() != emailToUpdate) {
                                // TODO setEmail dans Authenticator, car dans cette configuration l'email ne change que dans la db
                                hashMap.put(NodesNames.EMAIL, emailToUpdate);
                            }

                            // Envoie des données vers la db
                            userReference.set(hashMap, SetOptions.merge())
                                    // On vérifie le bon déroulement avec .addOnCompleteListener()
                                    // Si tout se passe bien l'utilisateur est dirigé vers la page de login
                                    // A noter qu'il faut rappeler le contexte (l'endroit où s'exécute la méthode
                                    // pour que l'action soit validée
                                    .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                            finish();
                                        }
                                    });
                        } else {
                            // Il y a un problème
                            Toast.makeText(ProfileActivity.this,
                                    getString(R.string.nameUpdateFailed, task.getException()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateNameAndPhoto() {
        //PB5
        progressBar.setVisibility(View.VISIBLE);
        //  Renommage du fichier avec l'userid + le type de fichier (ici jpg)
        String strFileName = firebaseUser.getUid() + ".jpg";
        // On place la photo dans un dossier dans le storage
        final StorageReference fileRef = fileStorage.child("avatars_user/" + strFileName);
        // On fait l'upload
        fileRef.putFile(localFileUri)
                .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        //PB6
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // On récupére l'url de l'image uploadée
                            fileRef.getDownloadUrl().addOnSuccessListener(ProfileActivity.this, new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    serverFileUri = uri;
                                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(etName.getText().toString().trim())
                                            .setPhotoUri(serverFileUri)
                                            .build();

                                    firebaseUser.updateProfile(request)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        String userID = firebaseUser.getUid();

                                                        HashMap<String, Object> hashMap = new HashMap<>();
                                                        // Testing var to update
                                                        if (firebaseUser.getDisplayName() != nameToUpdate) {
                                                            hashMap.put(NodesNames.NAME, nameToUpdate);
                                                        } else if (firebaseUser.getEmail() != emailToUpdate) {
                                                            // TODO setEmail ...
                                                            hashMap.put(NodesNames.EMAIL, etEmail.getText().toString().trim());
                                                        }

                                                        hashMap.put(NodesNames.AVATAR, serverFileUri.getPath());

                                                        userReference.set(hashMap, SetOptions.merge())
                                                                .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                                                        finish();
                                                                    }
                                                                });

                                                    } else {
                                                        Toast.makeText(ProfileActivity.this,
                                                                getString(R.string.nameUpdateFailed, task.getException()),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                }

                            });
                        }

                    }
                });
    }

    /**
     * 6 Copie des méthodes pickImage onActityResult et onRequestPermissionResult
     * Noter que l'on rend privée et que l'on retire les attributs de la méthode pickImage,
     * en effet nous allons l'appeler dans la méthode changeAvatar()  que nous allons créer ci-dessous
     **/
    private void pickImage() {
        /**
         *  9 Ajout de la vérification de la permission de parcourir les dossiers du terminal
         * Avant toute chose il faut ajouter la permission dans le manifest
         **/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                localFileUri = data.getData();

                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .circleCrop()
                        .error(R.drawable.ic_user)
                        .placeholder(R.drawable.ic_user);

                Glide.with(getApplicationContext())
                        .load(localFileUri)
                        .apply(options)
                        .fitCenter()
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(ivAvatar);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 101);
            } else {
                Toast.makeText(this, R.string.access_permission_is_required, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 7 Ajout de la méthode changeAvatar qui permet à l'utilisateur de changer sa photo de profile
     * Ne pas oublier de lier le widget de l'image à cette méthode
     **/
    public void changeAvatar(View v) {
        // Avant toute chose il faut ajouter le dossier menu dans le répertoire res
        // Clic droit ...
        //                                    Le contexte et la vue auqelle il est attaché
        PopupMenu popupMenu = new PopupMenu(this, v);
        // On vérifie qu'il existe un avatar sur le compte
        if (serverFileUri == null) {

            popupMenu.getMenuInflater().inflate(R.menu.menu_add_avatar, popupMenu.getMenu());
            // Ajout d'un listener pour savoir sur quel item l'utilisateur à cliquer
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    // On, récupère l'id de l'item cliquer
                    int id = item.getItemId();

                    // En fonction du résultat, lancement de l'action appropriée
                    if (id == R.id.mnuAddAvatarFromGallery) {
                        pickImage();
                    } else if (id == R.id.mnuAddAvatarFromCamera) {
                        /// A MODIFIER !!!!!
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivity(intent);
                    }
                    return false;
                }
            });
            popupMenu.show();

        } else { // Sinon l'utilisateur peut remplacer l'image ou la supprimer via un popupmenu

            // Utilisation du menu_avatar pour remplir (souffler) le pop-up, l'affichage se fait dans son parent
            popupMenu.getMenuInflater().inflate(R.menu.menu_avatar, popupMenu.getMenu());
            // Ajout d'un listener pour savoir sur quel item l'utilisateur à cliquer
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    // On, récupère l'id de l'item cliquer
                    int id = item.getItemId();

                    // En fonction du résultat, lancement de l'action appropriée
                    if (id == R.id.mnuModifyAvatar) {
                        pickImage();
                    } else if (id == R.id.mnuDeleteAvatar) {
                        Toast.makeText(ProfileActivity.this, R.string.image_has_been_deleted, Toast.LENGTH_SHORT).show();
                        deleteAvatar();
                    }
                    return false;
                }
            });
            popupMenu.show();
        }
    }

    /**
     * 8 Ajout de la méthode deleteAvatar pour supprimer l'image
     **/
    private void deleteAvatar() {
        // Google Authenticator
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .setPhotoUri(null)
                .build();
        // Base de données
        firebaseUser.updateProfile(request)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            String userID = firebaseUser.getUid();

                            HashMap<String, String> hashMap = new HashMap<>();
                            // On rempli la base Users avec du vide
                            hashMap.put(NodesNames.AVATAR, "");

                            userReference.set(hashMap, SetOptions.merge())
                                    .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                            // Changement du Toast
                                            Toast.makeText(ProfileActivity.this, R.string.avatar_deleted_succesfully, Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(ProfileActivity.this,
                                    getString(R.string.nameUpdateFailed, task.getException()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * 9 Ajout de la méthode de gestion du clic sur Save
     **/
    public void btnSaveClick(View v) {
        // Avant d'envoyer les données vers les bases on vérifie que les chamsp du formulaire ne soit pas vide
        if (etName.getText().toString().trim().equals("")) {
            etName.setError(getString(R.string.enter_name));
        } else {
            if (localFileUri != null) {
                updateNameAndPhoto();
            } else {
                updateNameOnly();
            }
        }
    }

    /**
     * 10 Ajout de la méthode pour bouton signout
     */
    public void btnSignOut(View v) {
        firebaseAuth.signOut();
        // On renvoie l'utilisateur vers LoginActivity
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        // On ferme l'activité courante
        finish();
    }

    public void btnChangePasswordClick(View v) {
        startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
    }

    /**
     * 12 Ajout des boutons next et send à la place du retour chariot du keyboard
     **/
    private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // Utilisation de actionId qui correspond à l'action ajouter dans le xml
            switch (actionId) {
                case EditorInfo.IME_ACTION_DONE:
                    btnSaveClick(v);
            }
            return false; // On laisse le return à false pour empêcher le comportement normal du clavier
        }
    };
}