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
    @FXML HBox buttonBox;

    public static String nom_utilisateur = "";
    public IServeur iServeur;
    public GraphicsContext graphicsContext;

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
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setLineWidth(1);

        final Button resetButton = new Button("Supprimer Tous");
        resetButton.setOnAction(actionEvent -> {
            graphicsContext.clearRect(1, 1, graphicsContext.getCanvas().getWidth() - 2,
                    graphicsContext.getCanvas().getHeight() - 2);
        });
        resetButton.setTranslateX(10);

        // Set up the pen color chooser
        ChoiceBox<String> colorChooser = new ChoiceBox<>(
                FXCollections.observableArrayList("Black", "Blue", "Red", "Green", "Brown", "Orange"));
        // Select the first option by default
        colorChooser.getSelectionModel().selectFirst();

        colorChooser.getSelectionModel().selectedIndexProperty().addListener((ChangeListener) (ov, old, newval) -> {
            Number idx = (Number) newval;
            Color newColor;
            switch (idx.intValue()) {
                case 0:
                    newColor = Color.BLACK;
                    break;
                case 1:
                    newColor = Color.BLUE;
                    break;
                case 2:
                    newColor = Color.RED;
                    break;
                case 3:
                    newColor = Color.GREEN;
                    break;
                case 4:
                    newColor = Color.BROWN;
                    break;
                case 5:
                    newColor = Color.ORANGE;
                    break;
                default:
                    newColor = Color.BLACK;
                    break;
            }
            graphicsContext.setStroke(newColor);

        });
        colorChooser.setTranslateX(5);

        ChoiceBox<String> sizeChooser = new ChoiceBox<>(FXCollections.observableArrayList("1", "2", "3", "4", "5"));
        // Select the first option by default
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
        });
        sizeChooser.setTranslateX(5);

        buttonBox.getChildren().addAll(colorChooser, sizeChooser, resetButton);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent event) {
                        graphicsContext.beginPath();
                        graphicsContext.moveTo(event.getX(), event.getY());
                        graphicsContext.stroke();
                    }
                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent event) {
                        graphicsContext.lineTo(event.getX(), event.getY());
                        graphicsContext.stroke();

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    iServeur.envoiPartieAjouteeSurLeTableauBlancAuAutresUtilisateurs(nom_utilisateur,event.getX(),event.getY());
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

    @Override
    public void recevoirPartieAjouteeSurLeTableauBlanc(double x, double y) throws RemoteException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("j'ai recu la position : ("+x+","+y+")");
                graphicsContext.lineTo(x, y);
                graphicsContext.stroke();
            }
        });
    }
}