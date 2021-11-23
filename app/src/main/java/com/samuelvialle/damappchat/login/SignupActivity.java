package com.samuelvialle.damappchat.login;

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
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.samuelvialle.damappchat.Common.NodesNames;
import com.samuelvialle.damappchat.Common.Util;
import com.samuelvialle.damappchat.NoInternetActivity;
import com.samuelvialle.damappchat.R;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    /**
     * 1 Variables globales
     **/
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private String name, email, password, confirmPassword;

    /**
     * 5.1 Ajout de la variable FirebaseUser
     */
    private FirebaseUser firebaseUser;
    /**
     * 6.1 Ajout de la variable de liaison avec les collections du Cloud FireStore
     **/
    private CollectionReference collectionReference; // Le reste est dans la méthode initFireStore
    /**
     * 7.1 Ajout de la variable de liaison avec FirebaseStorage
     **/
    private StorageReference fileStorage;
    /**
     * 7.5 Variables des Uri du fichier image de l'avatar utilisateur
     */
    private Uri localFileUri; // L'Uri du fichier sur le terminal
    private Uri serverFileUri; // L'urL du fichier stocké dans le storage (on parle bien ici d'un U R L (ELLE))
    /**
     * 7.6 Variable pour la localisation de l'ImageView
     */
    private ImageView ivAvatar;
    private String userID;
    /**
     * 11 Ajout de la progressBar
     **/
    private View progressBar;

    /**
     * 2 Méthode initUI pour faire le lien entre le design et le code
     **/
    public void initUI() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        /** 7.6 Bis lien avec le design **/
        ivAvatar = findViewById(R.id.ivAvatar);
        /** 11.1 Init ProgressBar **/
        progressBar = findViewById(R.id.progressBar);
    }

    private void initFirebase(){
        /** 6.2 Insertion dans Firestore **/
        collectionReference = FirebaseFirestore
                .getInstance() // Obtient une instance de connexion à la db
                .collection(NodesNames.USERS); // La référence equi vient de la classe NodesNames
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);
        /** 3 Appel de la méthode initUI **/
        initUI();
        initFirebase();

        /** 7.2 Initialisation du bucket pour le stockage des avatars utilisateurs **/
        fileStorage = FirebaseStorage.getInstance().getReference();

        /** 12.2 Association du clic dans le keyboard **/
        etConfirmPassword.setOnEditorActionListener(editorActionListener);
    }

    /**
     * 4 Méthode pour la gestion du click sur le bouton directement via la méthode onClick du xml
     * et ajout des vérifications suivantes :
     * - Nom non vide
     * - Mail correct
     * - Password non vide et comprenant au minimum 6 caractères
     * - Vérification du password avec confirmpassword pour un match identique
     **/
    public void btnSignupClick(View v) {
        name = etName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        confirmPassword = etConfirmPassword.getText().toString().trim();

        // Les vérifications
        // Si les cases sont vides
        if (name.equals("")) {
            etName.setError(getString(R.string.enter_name));
        } else if (email.equals("")) {
            etEmail.setError(getString(R.string.enter_email));
        } else if (password.equals("")) {
            etPassword.setError(getString(R.string.enter_password));
        } else if (confirmPassword.equals("")) {
            etConfirmPassword.setError(getString(R.string.confirm_password));
        }
        // Les patterns pour vérifier si il s'agit bien d'un email
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etName.setError(getString(R.string.enter_correct_email));
        }
        // Vérification password identique
        else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.password_mismatch));
        }
        // Si toutes les vérifications sont validées, il est possible de s'enregistrer
        else {
            // 12 Ajout de la vérification de la connection internet
            if (Util.connectionAvailable(this)) // Si la connexion fonctionne
            { // Alors on exécute la méthode
                // 11.9 ProgressBar
                progressBar.setVisibility(View.VISIBLE);
                final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        // Ajout la méthode addOnCompleteListener pour vérifier la bonne transmition des
                        // informations à Firebase Authenticator
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<AuthResult> task) {
                                // 11.10 ProgressBar
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    /** 5.2 Association de l'utilisateur courant à FirebaseUser dans le cadre du
                                     * changement de nom **/
                                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                    userID = firebaseUser.getUid();
                                    Log.i(TAG, "User creation " + userID);
//                               // Affichage d'un toast de réussite
//                               Toast.makeText(SignupActivity.this, R.string.user_created_successfully, Toast.LENGTH_SHORT).show();
//                               // Lancement de l'activité suivante
//                               startActivity(new Intent(SignupActivity.this, LoginActivity.class));
//                                /** 6.3 On appelle la méthode updateOnlyUser() pour valider l'enreistrement dans Authenticator
//                                 * et dans RealTime db les 2 lignes commentées ci-dessus ne s'affiche que si l'enregistrement
//                                 * c'est bien passé dans les 2 endroits
//                                 */
//                                updateOnlyUser();
                                    /** 8 On lance la bonne méthode d'enregistrement dans la base en fonction de l'ajout d'un avatar ou non **/
                                    if (localFileUri != null) {
                                        updateNameAndPhoto();
                                    } else {
                                        updateNameOnly();
                                    }
                                } else {
                                    // Affichage d'un Toast d'erreur avec l'erreur de la task (%1$s)
                                    Toast.makeText(SignupActivity.this,
                                            getString(R.string.signup_failed, task.getException()),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                // A noter qu'il est possible d'ajouter la méthode suivante, identique au else ci-dessus
//            .addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                  Toast.makeText(SignupActivity.this,
//                                       getString(R.string.signup_failed, task.getException()),
//                                       Toast.LENGTH_SHORT).show();
//                }
//            });
                // 9.1 Sinon
            } else {
                startActivity(new Intent(SignupActivity.this, NoInternetActivity.class));
            }
        }
    }

    /**
     * 5.1 Ajout de la méthode pour changer le nom de l'utilisateur
     **/
    private void updateNameOnly() {
        // 11.5 ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        // Utilisation de la méthode UserProfileChangeRequest pour charger le nom de l'utilisateur
        // qui s'est enregistré
        // Gestion de remplissage d'Authenticator
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .build();

        // Gestion du remplissage de la base de données
        /** 5.3 Update du nom du profile utilisateur à partir de l'edittext  **/
        firebaseUser.updateProfile(request)
                // Ajout d'un listener qui affiche un Toast si tout c'est bien déroulé
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                        // Tout c'est bien passé
                        // 11.6 ProgressBar
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Création du HashMap pour la gestion des données
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put(NodesNames.NAME, etName.getText().toString().trim());
                            hashMap.put(NodesNames.EMAIL, etEmail.getText().toString().trim());
                            hashMap.put(NodesNames.ONLINE, "true"); // User set ONLINE, true, car il est dans on profile
                            hashMap.put(NodesNames.AVATAR, ""); // Vide pour le moment
                            Log.i(TAG, "Name only " + userID);
                            // 11.7 ProgressBar
                            progressBar.setVisibility(View.VISIBLE);
                            // Envoie des données vers Realtime db
                            collectionReference.document(userID).set(hashMap)
                                    // On vérifie le bon déroulement avec .addOnCompleteListener()
                                    // Si tout se passe bien l'utilisateur est dirigé vers la page de login
                                    // A noter qu'il faut rappeler le contexte (l'endroit où s'exécute la méthode
                                    // pour que l'action soit validée
                                    .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                            //11.8 ProgressBar
                                            progressBar.setVisibility(View.GONE);
                                            // Affichage d'un toast de réussite
                                            Toast.makeText(SignupActivity.this, R.string.user_created_successfully,
                                                    Toast.LENGTH_SHORT).show();
                                            // Lancement de l'activité suivante
                                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                        }
                                    });

                        } else {
                            // Il y a un problème
                            Toast.makeText(SignupActivity.this,
                                    getString(R.string.nameUpdateFailed, task.getException()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * 7.3 Ajout de la méthode pour la gestion de l'avatar de l'utilisateur
     * Ne pas oublier de la lier à l'imageView dans le XML
     **/
    public void pickImage(View v) {
        /**
         *  9 Ajout de la vérification de la permission de parcourir les dossiers du terminal
         * Avant toute chose il faut ajouter la permission dans le manifest
         **/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // Ajout de l'intent qui va ouvrir la galerie du terminal pour choisir un photo : Intent.ACTION_PICK
            // Il faut ensuite ajouter l'espace de stockage dans lequel recherché, ici les images stockées sur tout le terminal
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // On demande de démarré l'activité avec un résultat, en effet il faut récupérer l'URL de l'espace de stockage (Storage)
            // de l'image pour le recopier dans notre base de données Users
            startActivityForResult(intent, 101);
            // A noter que le request code peut-être n'importe quoi, il n'y en a un qu'un seul dans cette activité
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    102);
        }
    }

    /**
     * 10 Ajout de la méthode pour vérifier si l'on à la permission ou non
     **/
    @Override
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
     * 7.4 Action à effectuée en résultat de la méthode pickImage()
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Vérifications
        // Le requestCode renvoyé est-il le bon ?
        if (requestCode == 101) {
            // Il y'a bien eu sélection d'image (sinon resultcode = RESULT_CANCELED)
            if (resultCode == RESULT_OK) {
                // Ajout des variables globales des uri cf 7.5
                localFileUri = data.getData();
                // Affectation de l'image sélectionnée à l'avatar (pour la variable globale cf 7.5)
                ivAvatar.setImageURI(localFileUri);
            }
        }
    }

    /**
     * 7.7 Ajout de la méthode pour uploader l'image sur le storage et récupérer son URL pour remplir la db Users
     */
    private void updateNameAndPhoto() {
        //  Renommage du fichier avec l'userid + le type de fichier (ici jpg)
        String strFileName = firebaseUser.getUid() + ".jpg";
        // On place la photo dans un dossier dans le storage
        final StorageReference fileRef = fileStorage.child("avatars_user/" + strFileName);
        // 11.2 Ajout progressBar
        progressBar.setVisibility(View.VISIBLE);
        // On fait l'upload
        fileRef.putFile(localFileUri)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        // 11.3 ProgressBar
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // On récupére l'url de l'image uploadée
                            fileRef.getDownloadUrl().addOnSuccessListener(SignupActivity.this,
                                    new OnSuccessListener<Uri>() {
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

                                                                HashMap<String, String> hashMap = new HashMap<>();

                                                                hashMap.put(NodesNames.NAME, etName.getText().toString().trim());
                                                                hashMap.put(NodesNames.EMAIL, etEmail.getText().toString().trim());
                                                                hashMap.put(NodesNames.ONLINE, "true");
                                                                hashMap.put(NodesNames.AVATAR, serverFileUri.getPath());

                                                                Log.i(TAG, "Name and picture : " + userID);

                                                                collectionReference.document(userID).set(hashMap)
                                                                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                Toast.makeText(SignupActivity.this, R.string.user_created_successfully, Toast.LENGTH_SHORT).show();
                                                                                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                                                            }
                                                                        });
                                                            } else {
                                                                Toast.makeText(SignupActivity.this,
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
     * 12 Ajout des boutons next et send à la place du retour chariot du keyboard
     **/
    private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // Utilisation de actionId qui correspond à l'action ajouter dans le xml
            switch (actionId) {
                case EditorInfo.IME_ACTION_DONE:
                    btnSignupClick(v);
            }
            return false; // On laisse le return à false pour empêcher le comportement normal du clavier
        }
    };

}