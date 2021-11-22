package com.samuelvialle.damappchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.samuelvialle.damappchat.findfriends.FindFriendsFragment;
import com.samuelvialle.damappchat.profile.ProfileActivity;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {
    /** Cette activité est l'activité principale, elle hébergera les fragments de notre application ainsi
     * que les différents menu et autre
     */

    /**
     * 4 Ajout du TabLayout pour la gestion des fragments
     **/
    private TabLayout tabLayout;
    private ViewPager viewPager;


    /**
     * 1 Ajout d'un nouveau menu dans le répertoire menu : menu_main
     * 1.1 dans le layout activity_main ajouter de l'icône d'accès au profil dans l'action bar
     **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 4.1 Initialisation des composants graphiques
        tabLayout = findViewById(R.id.tabMain);
        viewPager = findViewById(R.id.vpMain);

        // Appel du viewPager
        setViewPager();
    }

    /**
     * 2 Ajout de la méthode onCreateOptionsMenu pour afficher le menu
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // On souffle le xml de menu_main dans le menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 3 Ajout de la méthode onOptionsItemSelected pour activer le clic sur les items du menu
     **/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.mnuProfile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }

    /** 4.2 Adapter pour les fragments possibilité de l'extraire vers une autre classe **/
    class Adapter extends FragmentPagerAdapter {
        // 4.3 Ajout des différentes méthodes avac ALT + ENTER 2x

        public Adapter(@NonNull @NotNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @NotNull
        @Override
        public Fragment getItem(int position) {
            // 4.4 Ajout d'un switch pour afficher les différents fragments en fonction de la position
            switch (position) {
                case 0:
                    ChatFragment chatFragment = new ChatFragment();
                    return chatFragment;
                case 1:
                    RequestsFragment requestsFragment = new RequestsFragment();
                    return requestsFragment;
                case 2:
                     FindFriendsFragment findFriendsFragment = new FindFriendsFragment();
                     return findFriendsFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // 4.5 Changement du return 0 par
            return tabLayout.getTabCount(); // renvoie le nombre de tab du tablayout
        }
    }

    /** 5 Ajout de la méthode pour afficher les tab à partir du code Java **/
    private void setViewPager(){
        // Ajout des 3 tab créées, comparable au finViewById
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.tab_chat));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.tab_requests));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.tab_findfriends));

        // Affectation de la gravity à Fill pour que les tab remplissent tout l'espace
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Initialisation de l'adapter en faisant appel au support pour l'affichage de chacun des fragments
        Adapter adapter = new Adapter(getSupportFragmentManager(),
                // Permet de mettre dans le cycle de vie le fragment courant à onResume lors du swipe
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        // On attribue l'adapter à notre viewpager
        viewPager.setAdapter(adapter);

        // Ajout de la méthode pour la gestion des actions dans le tab avec un listener avec ces 3 méthodes
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Sélectionne la tab en fonction de l'icone cliqué
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Ajout de la méthode onPageChange Listener
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // On peut alors appeler la méthode setViewPager dans le onCreate
    }

    /** 6 Gestion du onBackPress */
    // Création d'une variable boolean pour le double back press
    private boolean doubleBackPressed = false;

    // Gestion du onBackPressed
    @Override
    public void onBackPressed() {
        // 6.5 On retire le super.OnBackPressed car sa fonction par défaut est de fermer l'activité ce que nous voulons éviter
        // super.onBackPressed();
        // 6.1 On vérifie si l'utilisateur n'est pas sur la tabChat
        if(tabLayout.getSelectedTabPosition()>0){
            // 6.2 Renvoie à la tabChat
            tabLayout.selectTab(tabLayout.getTabAt(tabLayout.getSelectedTabPosition() -1 ));
        } else { // 6.3 Si l'utilisateur est déja sur la tab Chat (0)
            if(doubleBackPressed){
                finishAffinity(); // Ferme toutes les actvités ouvertes
            } else {
                doubleBackPressed = true;
                // 6.4 Ajout d'un toast pour expliquer comment quitter l'app
                Toast.makeText(this, R.string.press_back_to_exit, Toast.LENGTH_SHORT).show();
                // Ajout d'un delais car si on laisse la méthode tel quel alors doubleBackPressed reste à true
                // Il est possible d'utiliser
                //Thread.sleep(2000);
                // Ce qui n'est pas bon dans le main activity car il s'agit du thread principal (ou UI thread)
                // Il est préférable d'ajouter un nouveau thread en utilisant la classe android .os.Handler pour ajouter le délais
                android.os.Handler handler = new android.os.Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackPressed = false;
                    }
                }, 2000); // Cela va permettre de déclencher le contenu de la méthode après 2 secondes
            }
        }
    }
}