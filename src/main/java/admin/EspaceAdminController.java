package admin;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import serveur.IServeurPourAdmin;

import java.io.IOException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class EspaceAdminController implements Initializable {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button deconnecterButton;
    @FXML
    private ListView<String> listeAfficheUtilisateurs;
    @FXML
    private TextField nom_field;
    @FXML
    private TextField prenom_field;
    @FXML
    private TextField nom_utilisateur_field;
    @FXML
    private PasswordField mot_de_passe_field;
    @FXML
    private Label erreur_message_ajouter_utilisateur;
    @FXML
    private ChoiceBox choisir_role;
    @FXML
    private TextField nom_classe_field;
    @FXML
    private ChoiceBox choisir_prof;
    @FXML
    private Label erreur_message_classe;

    ArrayList<String> listeEtudiants;
    ArrayList<String> listeEtudiantsSelectionne;
    ArrayList<String> listeProfesseurs;

    public static String nom_utilisateur = "";

    public IServeurPourAdmin iServeurPourAdmin;

    public EspaceAdminController() {
        super();
    }

    /**
     * l'initialisation de la fenetre
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String url_rmi = "rmi://127.0.0.1/irisi";
        try {
            iServeurPourAdmin = (IServeurPourAdmin) Naming.lookup(url_rmi);
        } catch (Exception e) {}

        welcomeLabel.setText(nom_utilisateur);
        try {
            listeEtudiants = iServeurPourAdmin.afficherUneListe("Etudiant");
            listeProfesseurs = iServeurPourAdmin.afficherUneListe("Professeur");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        modifierLaListeDesUtilisateurs(listeEtudiants);

        //initialiser choisir_role
        choisir_role.getItems().add("Etudiant");
        choisir_role.getItems().add("Professeur");
        choisir_role.getItems().add("Admin");

        choisir_role.valueProperty().set("Etudiant");

        //initialisation du messages d'erreurs
        erreur_message_ajouter_utilisateur.setText("");
        erreur_message_classe.setText("");

        //initialisation choisir_prof
        for (String professeur : listeProfesseurs) {
            choisir_prof.getItems().add(professeur);
        }

    }

    /**
     * Pour la déconnection
     * @throws IOException
     */
    public void deconnecterButtonOnAction() throws IOException {
        Stage stage = (Stage) deconnecterButton.getScene().getWindow();
        stage.close();
        //Retour au login
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login_admin.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 560, 350);
        Stage newStage = new Stage();
        newStage.initStyle(StageStyle.UNDECORATED);
        newStage.setScene(scene);
        newStage.show();
    }

    /**
     * Pour modifier la liste des utilisateurs connectés
     * qui a eté envoyé par le serveur aprés qu'un utilisateur à fait login
     * @param listeEtudiants
     * @throws RemoteException
     */
    public void modifierLaListeDesUtilisateurs(ArrayList<String> listeEtudiants){
        if(listeEtudiants != null){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    listeAfficheUtilisateurs.getItems().setAll(listeEtudiants);
                    listeAfficheUtilisateurs.getItems();
                    listeAfficheUtilisateurs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

                    listeAfficheUtilisateurs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {

                        }
                    });

                    listeAfficheUtilisateurs.setOnMouseClicked(new EventHandler<Event>() {

                        @Override
                        public void handle(Event event) {
                            ObservableList<String> selectedItems =  listeAfficheUtilisateurs.getSelectionModel().getSelectedItems();

                            if(listeEtudiantsSelectionne != null){
                                listeEtudiantsSelectionne.clear();
                            }
                            for(String s : selectedItems){
                                listeEtudiantsSelectionne.add(s);
                            }
                        }

                    });

                }
            });
        }else{
            System.out.println("la liste est null");
        }
    }

    /**
     * Pour envoyer les information de l'utilisateur à ajouter au serveur
     * @throws RemoteException
     */
    public void ajouterUnUtilisateurALaBaseDeDonnesOnAction() throws RemoteException, SQLException {
        String role_utilisateur = (String) choisir_role.getValue();
        if(nom_field.getText().isBlank() == true || nom_utilisateur_field.getText().isBlank() == true || prenom_field.getText().isBlank() == true || mot_de_passe_field.getText().isBlank() == true || role_utilisateur == null){
            erreur_message_ajouter_utilisateur.setTextFill(Color.web("#FF3030"));
            erreur_message_ajouter_utilisateur.setText("Tous les champs sont obligatoires");
        }else{
            String[] reponse = iServeurPourAdmin.ajouterUnUtilisateurALaBaseDeDonnes(nom_field.getText(),prenom_field.getText(), nom_utilisateur_field.getText(),mot_de_passe_field.getText(), role_utilisateur);

            if(reponse[0].equals("erreur")){
                erreur_message_ajouter_utilisateur.setTextFill(Color.web("#FF3030"));
            }else{
                erreur_message_ajouter_utilisateur.setTextFill(Color.web("#79FC3C"));

                //Pour modifier la liste des etudiants si un etudiant qui a ete ajouté
                if(role_utilisateur.equals("Etudiant")){
                    try {
                        listeEtudiants = iServeurPourAdmin.afficherUneListe("Etudiant");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    modifierLaListeDesUtilisateurs(listeEtudiants);
                }

                //Pour modifier la liste des professeurs si un prof qui a ete ajouté
                if(role_utilisateur.equals("Professeur")){
                    if(listeProfesseurs != null){
                        listeProfesseurs.clear();
                    }
                    try {
                        listeProfesseurs = iServeurPourAdmin.afficherUneListe("Professeur");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    choisir_prof.getSelectionModel().clearSelection();
                    choisir_prof.getItems().clear();
                    for (String professeur : listeProfesseurs) {
                        choisir_prof.getItems().add(professeur);
                    }
                }

                //initialiser les champs
                initialiserChampsDeAjouterUnUtilisateur();

            }
            erreur_message_ajouter_utilisateur.setText(reponse[1]);
        }
    }

    /**
     * Ici je veux traiter l'ajout du classe (apres)...
     */

    /**
     * initialiser tous les champs d'ajout d'un utilisateur sans le message
     */
    public void initialiserChampsDeAjouterUnUtilisateur(){
        nom_field.clear();
        nom_utilisateur_field.clear();
        prenom_field.clear();
        mot_de_passe_field.clear();
        choisir_role.valueProperty().set("Etudiant");
    }

    /**
     * initialiser tous les champs d'ajout d'un utilisateur avec le message
     */
    public void initialiserChampsDeAjouterUnUtilisateurOnAction(){
        initialiserChampsDeAjouterUnUtilisateur();
        erreur_message_ajouter_utilisateur.setText("");
    }

    /**
     * initialiser tous les champs d'ajout d'une classe sans le message
     */
    public void initialiserChampsDeAjouterUneClasse(){
        nom_classe_field.clear();
        choisir_prof.valueProperty().set(null);
    }

    /**
     * initialiser tous les champs d'ajout d'une classe avec le message
     */
    public void initialiserChampsDeAjouterUneClasseOnAction(){
        initialiserChampsDeAjouterUneClasse();
        erreur_message_classe.setText("");
    }
}