package etudiant;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IEtudiant extends Remote {
    public void modifierLaListeDesUtilisateursDuServeur(ArrayList<String> liste) throws RemoteException;
    public void recevoirUnMessageDuServeur(String nom_utilisateur, String message) throws RemoteException;
    public void recevoirLaPremierePositionDuPartieAjouteeSurLeTableauBlanc(double position_x, double position_y, double largeurDuLigne, String couleur) throws RemoteException;
    public void recevoirPartieAjouteeSurLeTableauBlanc(double position_x, double position_y, double largeurDuLigne, String couleur) throws RemoteException;
    public void supprimerTousLesDessinsDuTableauBlancEnvoyerParServeur() throws RemoteException;
    public void recevoirFichierDuServeur(String nom_utilisateur_source,String role_utilisateur_source,ArrayList<Integer> inc, String nom_fichier) throws RemoteException;
}
