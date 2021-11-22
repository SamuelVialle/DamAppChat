Application DAM CHAT
Dans la partie suivante nous allons nous créer pas à pas une application de chat, 
composée de blocs réutilisables dans vos futurs créations :
-	La gestion d’un système d’authentification par mail et mot de passe : 
-	Une page de login
-	Une page de création de compte
-	Une page pour réinitialiser son mot de passe 
-	Une page pour la gestion du profil avec la possibilité
    	De modifier ou supprimer l’image de l’avatar (via un menu)
    	De modifier le mot de passe 
    	De modifier le nom de l’utilisateur

-	L’application sera composée de fragments  

## La charte graphique
    - Modification de colors.xml en s'aidant de material.io
    - Changer le nom des var pour que ça concorde avec material.io
    - Choix d'un icône sur le site flaticon.com
    - Télécharger le SVG pour ic_launcher_foreground
    - Télécharger le png (en 100x100) pour utiulisation futur

#Les pages de login
Ajout d'un package pour contenir toutes les pages du login
--> Login
    - Création d'une empty activity nommée LoginActivity
    - Construction du Layout
    - Ajout pour le circleView
        - Round image pour afficher l'image dans un rond (il est possible d'utiliser Glide pour le faire aussi)
            implementation 'com.mikhaellopez:circularimageview:3.0.2'
    - Implémentation de Firebase UI dans le fichier gradle
    
--> Sign Up UI
    - Firebase Signup validations
    - Implémentation de Firebase Signup
    - Signup update only name
    - Signup update avatar

-->Le profil utilisateur
    - Profile UI
    - Implémentation des features
    - Change password
    - Reset Password

#Les classes communes
-- > La custom progressbar 
    - Ajout du layout custom_progressBar (Changement du constarint en cardView)
    - Ajout de l'include dans tous les layouts de login
    - Déclaration et initialisation dans tous les endroits nécessitant cette progressBar 

--> Gestion l’accès à internet
    - Ajout empty Activity NoInternetActivity
    - Ajout de la classe Util dans laquelle on ajoute la méthode pour vérifier si la connexion existe
    - Ajout des permissions dans le manifest
    - Modification des différents endroits où la connexion est requise
--> Le splashscreen animé
    - Ajout Empty activity --> SplashActivity
    - Ajout directory Anim dans res
        - Ajout new animation dans anim
        

#Home page
--> La page d’accueil : Ajout du TableLayout dans Main Activity
    - Pour obtenir un TabLayout personnalisé il faut d'abord ajouter le composant conatiner dans activity_main
    - Puis créer les tabs perso dans res/layout/tab_... 
Les fragments
Gestion du bouton back

La base de données

La fonction recherche d’amis
