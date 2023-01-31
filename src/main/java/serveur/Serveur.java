package serveur;

import utilisateur.IUtilisateur;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;

public class Serveur extends UnicastRemoteObject implements IServeur, IServeurPourAdmin {

    private static ArrayList<Session> session = new ArrayList<Session>();
    private static boolean interdit_de_dessiner_dans_le_tableau_blanc = false;

    static final String DB_URL = "jdbc:mysql://localhost:3310/rmi-e-learning-db";
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
        String mot_de_passe_utilisateur_hashed = motDePasseHashing(mot_de_passe);
        String mot_de_passe_bd = "";
        String role = "";
        String nom_classe = "";
        String[] message_de_retour = new String[3];

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
            String strSelect = "Select * from registration where nom_utilisateur= ?";

            PreparedStatement st = conn.prepareStatement(strSelect);
            st.setString(1,nom_utilisateur);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                mot_de_passe_bd = rs.getString(4);
                role = rs.getString(5);
                nom_classe = rs.getString(6);
            }

            if(mot_de_passe_utilisateur_hashed.equals(mot_de_passe_bd)){
                message_de_retour[0] = "success";
                message_de_retour[1] = role;
                message_de_retour[2] = nom_classe;
            }else{
                message_de_retour[0] = "erreur";
                message_de_retour[1] = "Nom d'utilisateur ou mot de passe est incorrecte";
                message_de_retour[2] = "";
            }
        }else{
            message_de_retour[0] = "erreur";
            message_de_retour[1] = "Vous etes déjà connecté!";
            message_de_retour[1] = "";
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
        IUtilisateur iUtilisateur = (IUtilisateur) Naming.lookup("rmi://127.0.0.1/" + nom_utilisateur);
        session.add(new Session(nom_utilisateur,iUtilisateur));

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
    public void deconnecterUtilisateur(String nom_utilisateur, String role) throws RemoteException {
        //retirer l'utilisateur de la session
        for (int i = 0 ; i<session.size();i++){
            if(session.get(i).nom_utilisateur.equals(nom_utilisateur)){
                session.remove(i);
                break;
            }
        }
        //Si le prof qui à déconnecte il faut que les etudiant ont le droit de dessiner
        if(role.equals("Professeur")){
            for(int i = 0 ; i < session.size() ; i++){
                session.get(i).iEtudiant.reponseDuServeurPourAuthorisationDesEtudiantsADissinerDuServeur(false);
            }
        }
        //modifier la liste des utilisateurs
        for(int i = 0 ; i < session.size() ; i++){
            session.get(i).iEtudiant.modifierLaListeDesUtilisateursDuServeur(getListeUtilisateurs(session.get(i).nom_utilisateur));
        }
    }

    /**
     * Méthode pour envoyer un message d'un utillisateur au autres utilisateurs
     * @param nom_utilisateur
     * @param message
     * @throws RemoteException
     */
    @Override
    public void envoiMessage(String nom_utilisateur, String message) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if(!session.get(i).nom_utilisateur.equals(nom_utilisateur)){
                session.get(i).iEtudiant.recevoirUnMessageDuServeur(nom_utilisateur,message,"C'est un message de groupe");
            }
        }
    }

    /**
     * Méthode pour envoyer un message d'un utillisateur à un autre
     * @param nom_utilisateur
     * @param message
     * @param nom_utilisateur_destination
     * @throws RemoteException
     */
    @Override
    public void envoiMessage(String nom_utilisateur, String message, String nom_utilisateur_destination) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if(session.get(i).nom_utilisateur.equals(nom_utilisateur_destination)){
                session.get(i).iEtudiant.recevoirUnMessageDuServeur(nom_utilisateur,message,nom_utilisateur_destination);
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
     * @param nouvelle_valeur
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
    public void envoiFichier(String zone_discussion_ou_partage, String nom_utilisateur_source,String role_utilisateur_source,ArrayList<Integer> inc, String nom_fichier, String nom_utilisateur_destination) throws RemoteException {
        if(zone_discussion_ou_partage.equals("Zone_de_partage")){
            for(int i = 0 ; i < session.size() ; i++){
                session.get(i).iEtudiant.recevoirFichierDuServeurPourZoneDePartage(nom_utilisateur_source,role_utilisateur_source,inc,nom_fichier);
            }
        }else if(nom_utilisateur_destination.equals("")){
            for(int i = 0 ; i < session.size() ; i++){
                session.get(i).iEtudiant.recevoirFichierDuServeurPourZoneDeDiscussion(nom_utilisateur_source,role_utilisateur_source,inc,nom_fichier,nom_utilisateur_destination);
            }
        }else{
            for(int i = 0 ; i < session.size() ; i++){
                if(nom_utilisateur_destination.equals(session.get(i).nom_utilisateur) || nom_utilisateur_source.equals(session.get(i).nom_utilisateur)){
                    session.get(i).iEtudiant.recevoirFichierDuServeurPourZoneDeDiscussion(nom_utilisateur_source,role_utilisateur_source,inc,nom_fichier,nom_utilisateur_destination);
                }
            }
        }

    }

    /**
     *IServeur methodes pour Admin
     */

    /**
     * récuperer les utilisateurs de la base de donnees a partir du role passé au parametre
     * @param role_ou_classe
     * @return
     * @throws RemoteException
     * @throws SQLException
     */
    @Override
    public ArrayList<String> afficherUneListe(String role_ou_classe) throws RemoteException, SQLException {
        ArrayList<String> laListeAEnvoyer = new ArrayList<>();
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

        if(role_ou_classe.equals("Classe")){
            String strSelect = "Select * from classes";

            PreparedStatement st = conn.prepareStatement(strSelect);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                laListeAEnvoyer.add(rs.getString("nom_classe"));
            }
        }else{
            String strSelect = "Select * from registration where role= ?";

            PreparedStatement st = conn.prepareStatement(strSelect);
            st.setString(1,role_ou_classe);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                laListeAEnvoyer.add(rs.getString("nom_utilisateur"));
            }
        }

        return laListeAEnvoyer;
    }

    /**
     * Ajouter un utilisateur à la base de donnes
     * @param nom
     * @param prenom
     * @param nom_utilisateur
     * @param mot_de_passe
     * @param role_utilisateur
     * @return
     * @throws RemoteException
     * @throws SQLException
     */
    @Override
    public String[] ajouterUnUtilisateurALaBaseDeDonnes(String nom, String prenom, String nom_utilisateur, String mot_de_passe, String role_utilisateur, String nom_classe) throws RemoteException, SQLException {
        String[] message_a_afficher = new String[2];

        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();

        // Voir est ce que le nom d'utilisateur est disponible
        String selectQuery = "Select * from registration where nom_utilisateur= ?";
        PreparedStatement st = conn.prepareStatement(selectQuery);
        st.setString(1,nom_utilisateur);

        ResultSet rs = st.executeQuery();
        boolean exists = false;
        while (rs.next()) {
            exists = true;
        }

        //Pour envoyer un message à l'utilisateur, soit erreur ou success
        if(exists){
            message_a_afficher[0] = "erreur";
            message_a_afficher[1] = "Le nom d'utilisateur n'est pas disponible";
        }else{
            if(role_utilisateur.equals("Professeur") || role_utilisateur.equals("Admin")){
                nom_classe = "";
                String insertQuery = "INSERT INTO registration VALUES ('"+nom_utilisateur+"','"+nom+"','"+prenom+"','"+motDePasseHashing(mot_de_passe)+"','"+role_utilisateur+"','"+nom_classe+"')";
                stmt.executeUpdate(insertQuery);

                message_a_afficher[0] = "success";
                message_a_afficher[1] = "L'utilisateur est crée avec succes";
            }else{
                if(nom_classe.equals("")){
                    message_a_afficher[0] = "erreur";
                    message_a_afficher[1] = "Pour un étudiant il faut choisir une classe";
                }else{
                    String insertQuery = "INSERT INTO registration VALUES ('"+nom_utilisateur+"','"+nom+"','"+prenom+"','"+motDePasseHashing(mot_de_passe)+"','"+role_utilisateur+"','"+nom_classe+"')";
                    stmt.executeUpdate(insertQuery);

                    message_a_afficher[0] = "success";
                    message_a_afficher[1] = "L'utilisateur est crée avec succes";
                }
            }
        }

        return message_a_afficher;
    }

    @Override
    public String[] ajouterUneClasseALaBaseDeDonnes(String nom_classe, String nom_prof_associe) throws RemoteException, SQLException {
        String[] message_a_afficher = new String[2];

        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();

        // Voir est ce que le nom de la classe est disponible
        String selectQuery = "Select * from classes where nom_classe= ?";
        PreparedStatement st = conn.prepareStatement(selectQuery);
        st.setString(1,nom_classe);

        ResultSet rs = st.executeQuery();
        boolean exists_nom = false;
        while (rs.next()) {
            exists_nom = true;
        }
        // Voir est ce que le prof est disponible (le prof peut etre dans une seule classe)
        String selectQuery1 = "Select * from classes where nom_prof_associe= ?";
        PreparedStatement st1 = conn.prepareStatement(selectQuery1);
        st1.setString(1,nom_prof_associe);

        ResultSet rs1 = st1.executeQuery();
        boolean exists_prof = false;
        while (rs1.next()) {
            exists_prof = true;
        }

        //Pour envoyer un message à l'admin, soit erreur ou success
        if(exists_nom){
            message_a_afficher[0] = "erreur";
            message_a_afficher[1] = "Le nom de cette classe n'est pas disponible";
        }else if(exists_prof){
            message_a_afficher[0] = "erreur";
            message_a_afficher[1] = "Le prof n'est pas disponible pour cette classe";
        }else{
            String insertQuery = "INSERT INTO classes VALUES ('"+nom_classe+"','"+nom_prof_associe+"')";
            stmt.executeUpdate(insertQuery);

            //modifier la table registration pour ajouter la classe à ce prof
            String insertQuery1 = "UPDATE registration set classe=? where nom_utilisateur=?";
            PreparedStatement st2 = conn.prepareStatement(insertQuery1);
            st2.setString(1, nom_classe);
            st2.setString(2, nom_prof_associe);
            int updateStatus=st2.executeUpdate();

            message_a_afficher[0] = "success";
            message_a_afficher[1] = "La classe a etait crée avec succes";
        }

        return message_a_afficher;
    }

    /**
     * Pour raison de securité pour le mot de passe avant de l'enregistrer dans la base de donnés
     * @param mot_de_passe
     * @return
     * @throws NoSuchAlgorithmException
     */
    public String motDePasseHashing(String mot_de_passe){
        MessageDigest messageDigest = null;
        String mot_de_passe_final = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA");
            messageDigest.update(mot_de_passe.getBytes());
            byte[] resultat = messageDigest.digest();
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : resultat) {
                stringBuilder.append(String.format("%02x",b));
            }

            mot_de_passe_final = stringBuilder.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return mot_de_passe_final;
    }
}
