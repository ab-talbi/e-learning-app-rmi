package serveur;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

public interface IServeurPourAdmin extends Remote {
    public ArrayList<String> afficherUneListe(String role_ou_classe) throws RemoteException, SQLException;
    public String[] ajouterUnUtilisateurALaBaseDeDonnes(String nom, String prenom, String nom_utilisateur, String mot_de_passe,String role_utilisateur, String nom_classe) throws RemoteException, SQLException;
    public String[] ajouterUneClasseALaBaseDeDonnes(String nom_classe, String nom_prof_associe) throws RemoteException, SQLException;
}
