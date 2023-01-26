package etudiant;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import serveur.IServeur;

import java.io.IOException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class EtudiantChatController extends UnicastRemoteObject implements IEtudiant, Initializable {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button deconnecterButton;
    @FXML
    private ListView<String> listeAfficheUtilisateurs;
    @FXML
    public VBox vBoxMessages;
    @FXML
    private TextField messageInput;

    public static String nom_utilisateur = "";
    public IServeur iServeur;
    public EtudiantChatController() throws RemoteException {
        super();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String url_rmi = "rmi://127.0.0.1/irisi";
        try {
            /**
             * Nom d'utilisateur comme nom pour que le serveur puisse
             * utiliser l'interface de cet utilisateur
             */
            Naming.rebind(nom_utilisateur,this);

            iServeur = (IServeur) Naming.lookup(url_rmi);
            iServeur.enregistrerUtilisateurDansLaSessionEtEnvoiLaListe(nom_utilisateur);
        } catch (Exception e) {}

        welcomeLabel.setText(nom_utilisateur);

        messageInput.addEventFilter(KeyEvent.KEY_PRESSED, event->{
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    sendMessageButtonOnAction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void deconnecterButtonOnAction() throws IOException {
        iServeur.decconnecterUtilisateur(nom_utilisateur);

        Stage stage = (Stage) deconnecterButton.getScene().getWindow();
        stage.close();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 560, 350);
        Stage newStage = new Stage();
        newStage.initStyle(StageStyle.UNDECORATED);
        newStage.setScene(scene);
        newStage.show();
    }

    public void sendMessageButtonOnAction() throws RemoteException {
        if(messageInput.getText().isBlank() == false){
            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER_RIGHT);
            hbox.setPadding(new Insets(5,5,5,10));

            Text text = new Text(messageInput.getText());
            text.setFill(Color.color(0.934,0.945,0.996));

            TextFlow textFlow = new TextFlow(text);
            textFlow.setStyle("-fx-color:rgb(239,242,255);"+" -fx-background-color:rgb(15,125,242);"+" -fx-background-radius: 7px 2px 2px 7px");
            textFlow.setPadding(new Insets(5,10,5,10));
            textFlow.setMaxWidth(200);

            hbox.getChildren().add(textFlow);
            vBoxMessages.getChildren().add(hbox);

            iServeur.envoiMessage(nom_utilisateur,messageInput.getText());

            messageInput.clear();
        }
    }

    @Override
    public void modifierLaListeDesUtilisateursDuServeur(ArrayList<String> listeUtilisateurs) throws RemoteException {
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

    @Override
    public void recevoirUnMessageDuServeur(String nom_utilisateur, String message) throws RemoteException {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(5,5,5,10));

        Text textNomUtilisateur = new Text(nom_utilisateur+"\n");
        Text textMessage = new Text(message);

        textNomUtilisateur.setFill(Color.web("002B5B"));

        TextFlow textFlow = new TextFlow(textNomUtilisateur,textMessage);
        textFlow.setStyle("-fx-background-color:rgb(233,233,235);"+" -fx-background-radius: 2px 7px 7px 2px");
        textFlow.setPadding(new Insets(5,10,5,10));
        textFlow.setMaxWidth(200);

        hbox.getChildren().add(textFlow);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vBoxMessages.getChildren().add(hbox);
            }
        });
    }
}