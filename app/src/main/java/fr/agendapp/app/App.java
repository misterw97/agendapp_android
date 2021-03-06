package fr.agendapp.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import fr.agendapp.app.factories.SyncFactory;
import fr.agendapp.app.objects.User;
import fr.agendapp.app.pages.LoginPage;
import fr.agendapp.app.pages.MainPage;

/**
 * Classe d'entrée (MAIN) dans l'applicartion = point de départ
 */
public class App extends AppCompatActivity {

    public static final String TAG = "Agendapp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Méthode déclenchée à la création de l'application (=ouverture)
        super.onCreate(savedInstanceState);
        // Affiche le SplashScreen
        setContentView(R.layout.splashscreen);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Page où rediriger l'utilisateur
        Intent page;
        // Stockage local (mémoire interne)
        SharedPreferences preferences = getSharedPreferences(TAG, MODE_PRIVATE);
        // On vérifie que les données nécessaires au démarrage sont bien présentes
        if (isLogged(preferences)) {
            // Initialise le service de communication avec le serveur d'APIs
            SyncFactory.init(this, preferences.getString("token", "x"));
            // Initialise l'objet Utilisateur courant et lance une synchronisation silencieuse
            User.init(this, true);
            // redirige vers la page des devoirs
            page = new Intent(App.this, MainPage.class);
        } else {
            // redirige vers la page d'identification
            page = new Intent(App.this, LoginPage.class);
        }
        // Selon le cas, on redirige l'utilisateur vers la page appropriée
        startActivity(page);
    }

    /**
     * Les données nécessaires au lancement sont : le token d'identification aux APIs
     *
     * @param preferences LocalStorage
     * @return True si les données nécessaires au démarrage sont présentes, False sinon
     */
    private boolean isLogged(SharedPreferences preferences) {
        return !(
                preferences.getString("token", "x").equals("x")
        );
    }

}
