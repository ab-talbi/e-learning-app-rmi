package serveur;

import etudiant.IEtudiant;
import javafx.scene.canvas.GraphicsContext;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;

public class Serveur extends UnicastRemoteObject implements IServeur {

    private static ArrayList<Session> session = new ArrayList<Session>();

    static final String DB_URL = "jdbc:mysql://localhost:3310/TestUDP";
    static final String USER = "root";
    static final String PASS = "";

    protected Serveur() throws RemoteException {
        super();
    }

    @Override
    public String echo(String msg) throws RemoteException {
        return "Le serveur Repond au : "+msg;
    }

    /**
     * Cette méthode est utilisée par l"étudiant pour s'authantifier
     * renvois des messages sois d'erreur ou de succés
     * @param nom_utilisateur
     * @param mot_de_passe
     * @return
     * @throws RemoteException
     * @throws SQLException
     * @throws MalformedURLException
     * @throws NotBoundException
     */
    @Override
    public String[] seConnecter(String nom_utilisateur, String mot_de_passe) throws RemoteException, SQLException{
        boolean utilisateur_deja_connecte = false;
        String mot_de_passe_bd = "";
        String[] message_de_retour = new String[2];

        /**
         * Pour voir est ce que l'utilisateur est connecté ou non
         */
        for(int i = 0 ; i < session.size(); i++){
            if(session.get(i).nom_utilisateur.equals(nom_utilisateur)){
                utilisateur_deja_connecte = true;
                break;
            }
        }

        if(!utilisateur_deja_connecte){
            /**
             * Récuperer le mot de passe de cette utilisaateur de la bd
             */
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            String strSelect = "Select * from registration where username= ?";

            PreparedStatement st = conn.prepareStatement(strSelect);
            st.setString(1,nom_utilisateur);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                mot_de_passe_bd = rs.getString(4);
            }

            if(mot_de_passe.equals(mot_de_passe_bd)){
                message_de_retour[0] = "success";
                message_de_retour[1] = "Vous etes connecté";
            }else{
                message_de_retour[0] = "erreur";
                message_de_retour[1] = "Nom d'utilisateur ou mot de passe est incorrecte";
            }
        }else{
            message_de_retour[0] = "erreur";
            message_de_retour[1] = "Vous etes déjà connecté!";
        }

        return message_de_retour;
    }

    /**
     * Cette méthode est là pour renvoyer une liste des utilisateurs sans l'utilisateur passé au parametre
     * @param nom_utilisateur
     * @return
     */
    public ArrayList<String> getListeUtilisateurs(String nom_utilisateur){
        ArrayList<String> listeUtilisateurs = new ArrayList<>();
        for (int i = 0 ; i < session.size() ; i++){
            if(!session.get(i).nom_utilisateur.equals(nom_utilisateur)){
                listeUtilisateurs.add(session.get(i).nom_utilisateur);
            }
        }
        return listeUtilisateurs;
    }

    /**
     * Pour enregistrer l'utilisateur dans la session avec son interface
     * et appeler la méthode modifierLaListeDesUtilisateursDuServeur de tous les utilisateurs connectés
     * pour les envoyer la nouvelle liste d'utilisateurs
     * @param nom_utilisateur
     * @throws RemoteException
     * @throws MalformedURLException
     * @throws NotBoundException
     */
    @Override
    public void enregistrerUtilisateurDansLaSessionEtEnvoiLaListe(String nom_utilisateur) throws RemoteException, MalformedURLException, NotBoundException {
        IEtudiant iEtudiant = ( IEtudiant ) Naming.lookup("rmi://127.0.0.1/" + nom_utilisateur);
        session.add(new Session(nom_utilisateur,iEtudiant));

        //Test
        System.out.println("=====Les utilisateurs connectés=====");
        for (int i = 0 ; i<session.size();i++){
            System.out.println(session.get(i).toString());
        }
        System.out.println("====================================");
        //End Test

        for(int i = 0 ; i < session.size() ; i++){
            session.get(i).iEtudiant.modifierLaListeDesUtilisateursDuServeur(getListeUtilisateurs(session.get(i).nom_utilisateur));
        }
    }

    /**
     * Cette méthode est pour retirer l'utilisateur de la session
     * et renvoi la liste modifié au autres utilisateurs
     * @param nom_utilisateur
     * @throws RemoteException
     */
    @Override
    public void decconnecterUtilisateur(String nom_utilisateur) throws RemoteException {
        for (int i = 0 ; i<session.size();i++){
            if(session.get(i).nom_utilisateur.equals(nom_utilisateur)){
                session.remove(i);
                break;
            }
        }

        //Test
        System.out.println("=====Les utilisateurs connectés=====");
        for (int i = 0 ; i<session.size();i++){
            System.out.println(session.get(i).toString());
        }
        System.out.println("====================================");
        //End Test

        for(int i = 0 ; i < session.size() ; i++){
            session.get(i).iEtudiant.modifierLaListeDesUtilisateursDuServeur(getListeUtilisateurs(session.get(i).nom_utilisateur));
        }
    }

    /**
     * Méthode pour envoyer un message d'un utillisateurs au autres utilisateurs
     * @param nom_utilisateur
     * @param message
     * @throws RemoteException
     */
    @Override
    public void envoiMessage(String nom_utilisateur, String message) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if(!session.get(i).nom_utilisateur.equals(nom_utilisateur)){
                session.get(i).iEtudiant.recevoirUnMessageDuServeur(nom_utilisateur,message);
            }
        }
    }

    @Override
    public void envoiPartieAjouteeSurLeTableauBlancAuAutresUtilisateurs(String nom_utilisateur, double x, double y) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if(!session.get(i).nom_utilisateur.equals(nom_utilisateur)){
                session.get(i).iEtudiant.recevoirPartieAjouteeSurLeTableauBlanc(x,y);
            }
        }
    }

}
