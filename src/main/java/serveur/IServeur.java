package serveur;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

public interface IServeur extends Remote {
    public String echo(String msg) throws RemoteException;
    public String[] seConnecter(String nom_utilisateur, String mot_de_passe) throws RemoteException, SQLException, MalformedURLException, NotBoundException;
    public void enregistrerUtilisateurDansLaSessionEtEnvoiLaListe(String nom_utilisateur) throws RemoteException, MalformedURLException, NotBoundException;
    public void decconnecterUtilisateur(String nom_utilisateur) throws RemoteException;
    public void envoiMessage(String nom_utilisateur, String message) throws RemoteException;
}
