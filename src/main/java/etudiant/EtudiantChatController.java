package etudiant;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
    @FXML
    private Canvas canvas;
    @FXML
    private HBox buttonBox;

    public static String nom_utilisateur = "";
    public IServeur iServeur;

    public GraphicsContext graphicsContext;
    Color couleurLocaleGraphique = Color.BLACK;
    double largeurDuLigneGraphique = 1;

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

        initialisationTableauBlanc();
    }

    /**
     * initialiser l'espace du tableau blanc avec Canvas pour le dessin
     */
    public void initialisationTableauBlanc(){

        graphicsContext = canvas.getGraphicsContext2D();
        double canvasWidth = graphicsContext.getCanvas().getWidth();
        double canvasHeight = graphicsContext.getCanvas().getHeight();

        //Par défaut
        graphicsContext.fill();
        graphicsContext.strokeRect(0, 0, canvasWidth, canvasHeight);
        graphicsContext.setFill(Color.RED);
        graphicsContext.setStroke(couleurLocaleGraphique);
        graphicsContext.setLineWidth(largeurDuLigneGraphique);

        final Button resetButton = new Button("Supprimer Tous");
        resetButton.setOnAction(actionEvent -> {
            graphicsContext.clearRect(1, 1, graphicsContext.getCanvas().getWidth() - 2,
                    graphicsContext.getCanvas().getHeight() - 2);

            try {
                iServeur.supprimerTousLesDessinsDuTableauBlanc(nom_utilisateur);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        resetButton.setTranslateX(10);

        ChoiceBox<String> colorChooser = new ChoiceBox<>(
                FXCollections.observableArrayList("Noir", "Bleu", "Rouge", "Vert", "Marron", "Orange"));
        colorChooser.getSelectionModel().selectFirst();

        colorChooser.getSelectionModel().selectedIndexProperty().addListener((ChangeListener) (ov, old, newval) -> {
            Number idx = (Number) newval;
            switch (idx.intValue()) {
                case 0:
                    couleurLocaleGraphique = Color.BLACK;
                    break;
                case 1:
                    couleurLocaleGraphique = Color.BLUE;
                    break;
                case 2:
                    couleurLocaleGraphique = Color.RED;
                    break;
                case 3:
                    couleurLocaleGraphique = Color.GREEN;
                    break;
                case 4:
                    couleurLocaleGraphique = Color.BROWN;
                    break;
                case 5:
                    couleurLocaleGraphique = Color.ORANGE;
                    break;
                default:
                    couleurLocaleGraphique = Color.BLACK;
                    break;
            }
            graphicsContext.setStroke(couleurLocaleGraphique);
        });
        colorChooser.setTranslateX(5);

        ChoiceBox<String> sizeChooser = new ChoiceBox<>(FXCollections.observableArrayList("1", "2", "3", "4", "5"));

        sizeChooser.getSelectionModel().selectFirst();

        sizeChooser.getSelectionModel().selectedIndexProperty().addListener((ChangeListener) (ov, old, newval) -> {
            Number idx = (Number) newval;

            switch (idx.intValue()) {
                case 0:
                    graphicsContext.setLineWidth(1);
                    break;
                case 1:
                    graphicsContext.setLineWidth(2);
                    break;
                case 2:
                    graphicsContext.setLineWidth(3);
                    break;
                case 3:
                    graphicsContext.setLineWidth(4);
                    break;
                case 4:
                    graphicsContext.setLineWidth(5);
                    break;
                default:
                    graphicsContext.setLineWidth(1);
                    break;
            }
            largeurDuLigneGraphique = graphicsContext.getLineWidth();
        });
        sizeChooser.setTranslateX(5);

        buttonBox.getChildren().addAll(colorChooser, sizeChooser, resetButton);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent event) {
                        graphicsContext.setStroke(couleurLocaleGraphique);
                        graphicsContext.setLineWidth(largeurDuLigneGraphique);

                        graphicsContext.beginPath();
                        graphicsContext.moveTo(event.getX(), event.getY());
                        graphicsContext.stroke();

                        /**
                         * Ici j'envoi la premiere position pour eviter un bug
                         * le bug etait qaund j'envoi la position, la derniere est liéé avec lanciennes
                         * danc un ligne que je veux pas est dissiné par défaut
                         * c'est pourquoi il me faut a chaque qulique à indiquer au autres utilisateur
                         * que voilà la premiere position
                         */
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    iServeur.envoiLaPremierePositionDuPartieAjouteeSurLeTableauBlancAuAutresUtilisateurs(nom_utilisateur,event.getX(),event.getY(),graphicsContext.getLineWidth(), String.valueOf(couleurLocaleGraphique));
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent event) {
                        graphicsContext.setStroke(couleurLocaleGraphique);
                        graphicsContext.setLineWidth(largeurDuLigneGraphique);

                        graphicsContext.lineTo(event.getX(), event.getY());
                        graphicsContext.stroke();
                        /**
                         * Ici j'envoi les positions de dessin au autres utilisateurs avec le couleur et la largeur de ligne
                         */
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    iServeur.envoiPartieAjouteeSurLeTableauBlancAuAutresUtilisateurs(nom_utilisateur,event.getX(),event.getY(),graphicsContext.getLineWidth(), String.valueOf(couleurLocaleGraphique));
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent event) {
                    }
                });
    }

    /**
     * Pour la déconnection
     * elle appelle la méthode decconnecterUtilisateur(nom_utilisateur) du serveur,
     * pour retirer cet utilisateur de la session
     * @throws IOException
     */
    public void deconnecterButtonOnAction() throws IOException {
        iServeur.deconnecterUtilisateur(nom_utilisateur);

        Stage stage = (Stage) deconnecterButton.getScene().getWindow();
        stage.close();
        //Retour au login
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 560, 350);
        Stage newStage = new Stage();
        newStage.initStyle(StageStyle.UNDECORATED);
        newStage.setScene(scene);
        newStage.show();
    }

    /**
     * Pour envoyer un message au autre(s) utilisateur(s)
     * @throws RemoteException
     */
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
            //Envoi Message à tous
            iServeur.envoiMessage(nom_utilisateur,messageInput.getText());

            messageInput.clear();
        }
    }

    /**
     * Pour modifier la liste des utilisateurs connectés
     * qui a eté envoyé par le serveur aprés qu'un utilisateur à fait login
     * @param listeUtilisateurs
     * @throws RemoteException
     */
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

    /**
     * Pour afficher un message des autres utilisateurs
     * @param nom_utilisateur
     * @param message
     * @throws RemoteException
     */
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

    /**
     * Pour recevoir la premiere position (x,y) du curseur d'un utilisateur qui est entreint de dessiner
     * @param position_x
     * @param position_y
     * @param largeurDuLigne
     * @param couleur
     * @throws RemoteException
     */
    @Override
    public void recevoirLaPremierePositionDuPartieAjouteeSurLeTableauBlanc(double position_x, double position_y, double largeurDuLigne, String couleur) throws RemoteException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Color couleurConvirti = Color.valueOf(couleur);

                graphicsContext.setLineWidth(largeurDuLigne);
                graphicsContext.setStroke(couleurConvirti);

                System.out.println("j'ai recu la 1ere position : ("+position_x+","+position_y+")");

                graphicsContext.beginPath();
                graphicsContext.moveTo(position_x,position_y);
                graphicsContext.stroke();
            }
        });
    }

    /**
     * Pour recevoir les positions (x,y) du curseur d'un utilisateur qui est entreint de dessiner ansi que la largeur de ligne et son couleur
     * @param position_x
     * @param position_y
     * @param largeurDuLigne
     * @param couleur
     * @throws RemoteException
     */
    @Override
    public void recevoirPartieAjouteeSurLeTableauBlanc(double position_x, double position_y, double largeurDuLigne, String couleur) throws RemoteException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Color couleurConvirti = Color.valueOf(couleur);

                graphicsContext.setLineWidth(largeurDuLigne);
                graphicsContext.setStroke(couleurConvirti);

                System.out.println("j'ai recu la position : ("+position_x+","+position_y+")");

                graphicsContext.lineTo(position_x, position_y);
                graphicsContext.stroke();
            }
        });
    }

    /**
     * Cette méthode est là pour vider le tableau blanc pour tous le monde
     * @throws RemoteException
     */
    @Override
    public void supprimerTousLesDessinsDuTableauBlancEnvoyerParServeur() throws RemoteException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                graphicsContext.clearRect(1, 1, graphicsContext.getCanvas().getWidth() - 2,
                        graphicsContext.getCanvas().getHeight() - 2);
            }
        });
    }
}