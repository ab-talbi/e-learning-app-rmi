package admin;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    ArrayList<String> listeUtilisateurs;

    public static String nom_utilisateur = "";

    public IServeurPourAdmin iServeurPourAdmin;

    public EspaceAdminController() throws RemoteException {
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
            listeUtilisateurs = iServeurPourAdmin.afficherLaListeDesEtudiants();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        modifierLaListeDesUtilisateurs(listeUtilisateurs);
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
     * @param listeUtilisateurs
     * @throws RemoteException
     */
    public void modifierLaListeDesUtilisateurs(ArrayList<String> listeUtilisateurs){
        if(listeUtilisateurs != null){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    listeAfficheUtilisateurs.getItems().setAll(listeUtilisateurs);
                    listeAfficheUtilisateurs.getItems();
                    listeAfficheUtilisateurs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {

                        }
                    });
                }
            });
        }else{
            System.out.println("la liste est null");
        }
    }
}