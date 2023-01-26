package etudiant;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IEtudiant extends Remote {
    public void modifierLaListeDesUtilisateursDuServeur(ArrayList<String> liste) throws RemoteException;
    public void recevoirUnMessageDuServeur(String nom_utilisateur, String message) throws RemoteException;
}
