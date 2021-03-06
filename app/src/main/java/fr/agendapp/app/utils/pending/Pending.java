package fr.agendapp.app.utils.pending;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import fr.agendapp.app.App;
import fr.agendapp.app.factories.NotificationFactory;
import fr.agendapp.app.factories.SyncFactory;
import fr.agendapp.app.listeners.SyncListener;

/**
 * Gestion de la synchronisation usuelle avec le serveur Agendapp
 * --> Envoi des actions en attente
 * --> Déclenchement d'une vérification de la présence d'une nouvelle version de devoirs
 *
 * @author Valentin Viennot
 * @author Dylan Habans
 */
public abstract class Pending {


    private static final int VERSION_LIFETIME = 5;
    private static int[] lifetime = {0, 0};

    /**
     * Récupérer l'ancienne pending list du stockage local
     * (à appeler à l'ouverture)
     */
    public static void init(Context context) {
        PendDO.initList(context);
        PendFLAG.initList(context);
        PendDEL.initList(context);
        PendDELc.initList(context);
        PendCOMM.initList(context);
        PendALERT.initList(context);
        PendMERGE.initList(context);
        PendADD.initList(context);
    }

    /**
     * Enregistre la pending list dans le stockage local
     * (à appeler à la fermeture ou après chaque send)
     */
    private static void save(Context context) {
        if (context != null) {
            PendDO.saveList(context);
            PendFLAG.saveList(context);
            PendDEL.saveList(context);
            PendDELc.saveList(context);
            PendCOMM.saveList(context);
            PendALERT.saveList(context);
            PendMERGE.saveList(context);
            PendADD.saveList(context);
        }
    }

    /**
     * Nettoie les listes (inclus une sauvegarde)
     * A appeler en cas de réussite d'envoi des actions en attente
     *
     * @param context Android
     */
    public static void clear(Context context) {
        if (context != null) {
            PendDO.clearList(context);
            PendFLAG.clearList(context);
            PendDEL.clearList(context);
            PendDELc.clearList(context);
            PendCOMM.clearList(context);
            PendALERT.clearList(context);
            PendMERGE.clearList(context);
            PendADD.clearList(context);
        }
    }

    /**
     * Envoie les listes d'actions en attente au serveur pour traitement si elles ne sont pas vides
     * Sinon lance régulièrement une synchronisation des devoirs
     *
     * @param syncListener        Callback post synchronisation
     * @param context             Android Context
     * @param notificationFactory Ajout de notifications en cas d'erreur
     */
    public static void send(SyncListener syncListener, Context context, @Nullable NotificationFactory notificationFactory) {
        if (context != null && syncListener != null) {
            // Récupération des actions en attente au format JSON
            String json = toJson();
            // S'il y a des actions en attente
            if (json != null) {
                Log.i(App.TAG, "send JSON : " + json);
                // On les sauvegarde dans l'attente de l'envoi
                save(context);
                // On envoi les actions en attente (suivi d'une récupération des devoirs au SyncListener)
                SyncFactory.getInstance(context).synchronize(syncListener, context, json, notificationFactory);
            } else {
                // Lorsqu'il n'y a pas d'actions en attente
                // On se contente de demander la nouvelle version des devoirs
                // Mais on ne souhaite pas le faire aussi souvent que l'envoi des listes d'attente (pour économiser le réseau)
                int i = syncListener.isArchives() ? 1 : 0;
                // On décrémente le "temps de vie" d'une version
                lifetime[i]--;
                // Si le temps de vie est expiré
                if (lifetime[i] < 0) {
                    // On le remet à "zéro"
                    lifetime[i] = VERSION_LIFETIME;
                    // Lance une synchronisation
                    SyncFactory.getInstance(context).getVersion(syncListener, context, notificationFactory);
                } else {
                    syncListener.onSyncNotAvailable();
                }
            }
        }
    }

    /**
     * @return Liste des listes d'actions en attente au format JSON, null si aucune action en attente
     */
    private static String toJson() {
        // Premiere liste à être ajoutée
        boolean first = true;
        // Nombre d'actions en attente
        int size = 0;
        // Début du tableau de listes d'actions
        String json = "{";
        // Liste de devoirs marqués comme fait/non fait
        if (PendDO.size() > 0) {
            // On n'ajoute la liste que si elle contient des actions
            size += PendDO.size();
            // Cette liste est forcément la première si elle est ajoutée
            // Il ne faut pas la précéder d'une virgule
            json += "\"" + PendDO.getName() + "\":" + PendDO.getList();
            // Si on a ajouté cette liste, la prochaine ne sera donc plus la première ajoutée
            first = false;
        }
        // Liste de marqueurs
        if (PendFLAG.size() > 0) {
            size += PendFLAG.size();
            // Si cette liste n'est pas la première ajoutée, il faut la précéder d'une virgule
            if (!first) json += ",";
            json += "\"" + PendFLAG.getName() + "\":" + PendFLAG.getList();
            // Quoi qu'il arrive, si on ajoute cette liste la suivante ne sera pas la première
            first = false;
        }
        // Liste de devoirs supprimés
        if (PendDEL.size() > 0) {
            size += PendDEL.size();
            if (!first) json += ",";
            json += "\"" + PendDEL.getName() + "\":" + PendDEL.getList();
            first = false;
        }
        // Liste de commentaires supprimés
        if (PendDELc.size() > 0) {
            size += PendDELc.size();
            if (!first) json += ",";
            json += "\"" + PendDELc.getName() + "\":" + PendDELc.getList();
            first = false;
        }
        // Liste de commentaires ajoutés
        if (PendCOMM.size() > 0) {
            size += PendCOMM.size();
            if (!first) json += ",";
            json += "\"" + PendCOMM.getName() + "\":" + PendCOMM.getList();
            first = false;
        }
        // Liste de devoirs signalés
        if (PendALERT.size() > 0) {
            size += PendALERT.size();
            if (!first) json += ",";
            json += "\"" + PendALERT.getName() + "\":" + PendALERT.getList();
            first = false;
        }
        // Liste de devoirs ajoutés
        if (PendADD.size() > 0) {
            size += PendADD.size();
            if (!first) json += ",";
            json += "\"" + PendADD.getName() + "\":" + PendADD.getList();
            first = false;
        }
        // Liste de devoirs à fusionner
        if (PendMERGE.size() > 0) {
            size += PendMERGE.size();
            if (!first) json += ",";
            json += "\"" + PendMERGE.getName() + "\":" + PendMERGE.getList();
            // Cette liste est forcément la dernière si elle est ajoutée
        }
        json += "}";
        return size > 0 ? json : null;
    }

}