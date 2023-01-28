package serveur;

import etudiant.IEtudiant;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;

public class Serveur extends UnicastRemoteObject implements IServeur {

    private static ArrayList<Session> session = new ArrayList<Session>();
    private static boolean interdit_de_dessiner_dans_le_tableau_blanc = false;

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
        String role = "";
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
             * Récuperer le mot de passe de cette utilisaateur et son role de la bd
             */
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            String strSelect = "Select * from registration where username= ?";

            PreparedStatement st = conn.prepareStatement(strSelect);
            st.setString(1,nom_utilisateur);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                mot_de_passe_bd = rs.getString(4);
                role = rs.getString(5);
            }

            if(mot_de_passe.equals(mot_de_passe_bd)){
                message_de_retour[0] = "success";
                message_de_retour[1] = role;
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
        //ajouter l'utilisateur à la session (son nom d'utilisateur et son interface)
        IEtudiant iEtudiant = ( IEtudiant ) Naming.lookup("rmi://127.0.0.1/" + nom_utilisateur);
        session.add(new Session(nom_utilisateur,iEtudiant));

        //modifier la liste des utilisateurs
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
    public void deconnecterUtilisateur(String nom_utilisateur) throws RemoteException {
        //retirer l'utilisateur de la session
        for (int i = 0 ; i<session.size();i++){
            if(session.get(i).nom_utilisateur.equals(nom_utilisateur)){
                session.remove(i);
                break;
            }
        }
        //modifier la liste des utilisateurs
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

    /**
     * C'est pour informer les autres utilisateurs de la position du premiere mouvement du curseur
     * Les autres utilisateurs recoit la positions et initialiser le couleur et largeure de la ligne
     * @param nom_utilisateur
     * @param position_x
     * @param position_y
     * @param largeurDuLigne
     * @param couleur
     * @throws RemoteException
     */
    @Override
    public void envoiLaPremierePositionDuPartieAjouteeSurLeTableauBlancAuAutresUtilisateurs(String nom_utilisateur, double position_x, double position_y, double largeurDuLigne, String couleur) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if(!session.get(i).nom_utilisateur.equals(nom_utilisateur)){
                session.get(i).iEtudiant.recevoirLaPremierePositionDuPartieAjouteeSurLeTableauBlanc(position_x,position_y,largeurDuLigne,couleur);
            }
        }
    }

    /**
     * Envoi de la position du curseur au autres utilisateur avec le couleur et la largeur de ligne
     * @param nom_utilisateur
     * @param position_x
     * @param position_y
     * @param largeurDuLigne
     * @param couleur
     * @throws RemoteException
     */
    @Override
    public void envoiPartieAjouteeSurLeTableauBlancAuAutresUtilisateurs(String nom_utilisateur, double position_x, double position_y, double largeurDuLigne, String couleur) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if(!session.get(i).nom_utilisateur.equals(nom_utilisateur)){
                session.get(i).iEtudiant.recevoirPartieAjouteeSurLeTableauBlanc(position_x,position_y,largeurDuLigne,couleur);
            }
        }
    }

    /**
     * Pour forcer la suppression du tableau blanc pour les autres utilisateurs
     * @param nom_utilisateur
     * @throws RemoteException
     */
    @Override
    public void supprimerTousLesDessinsDuTableauBlanc(String nom_utilisateur) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if(!session.get(i).nom_utilisateur.equals(nom_utilisateur)){
                session.get(i).iEtudiant.supprimerTousLesDessinsDuTableauBlancEnvoyerParServeur();
            }
        }
    }

    /**
     * Envoi de la derniere situation du tableau blanc aux nouvelles utilisateurs,
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean voirAuthorisationDeDessinerSurTanleauBlanc() throws RemoteException {
        return interdit_de_dessiner_dans_le_tableau_blanc;
    }

    /**
     * Pour interdir ou authoriser les etudiants à dissiner par le prof
     * @throws RemoteException
     */
    @Override
    public void changerAuthorisationDesEtudiantsADissinerDuServeur(boolean nouvelle_valeur) throws RemoteException {
        interdit_de_dessiner_dans_le_tableau_blanc = nouvelle_valeur;
        for(int i = 0 ; i < session.size() ; i++){
            session.get(i).iEtudiant.reponseDuServeurPourAuthorisationDesEtudiantsADissinerDuServeur(interdit_de_dessiner_dans_le_tableau_blanc);
        }
    }

    /**
     * transferer le fichieres aux autres utilisateuers
     * @param nom_utilisateur_source
     * @param role_utilisateur_source
     * @param inc
     * @param nom_fichier
     * @throws RemoteException
     */
    @Override
    public void envoiFichierATousLesUtilisateurs(String nom_utilisateur_source,String role_utilisateur_source,ArrayList<Integer> inc, String nom_fichier) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            session.get(i).iEtudiant.recevoirFichierDuServeur(nom_utilisateur_source,role_utilisateur_source,inc,nom_fichier);
        }
    }

}
