package utilisateur;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import serveur.IServeur;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class UtilisateurChatController extends UnicastRemoteObject implements IUtilisateur, Initializable {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button deconnecterButton;
    @FXML
    private ListView<String> listeAfficheUtilisateurs;
    @FXML
    public VBox vBoxMessages;
    @FXML
    private VBox vBoxFichiers;
    @FXML
    private TextField messageInput;
    @FXML
    private Canvas canvas;
    @FXML
    private HBox buttonBox;


    public static String nom_utilisateur = "";
    public static String role = "";

    public boolean interdit_de_dessiner_dans_le_tableau_blanc = false;

    public IServeur iServeur;

    public GraphicsContext graphicsContext;
    Color couleurLocaleGraphique = Color.BLACK;
    double largeurDuLigneGraphique = 1;

    FileChooser fileChooser = new FileChooser();

    public UtilisateurChatController() throws RemoteException {
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
            /**
             * Nom d'utilisateur comme nom pour que le serveur puisse
             * utiliser l'interface de cet utilisateur
             */
            Naming.rebind(nom_utilisateur,this);

            iServeur = (IServeur) Naming.lookup(url_rmi);
            iServeur.enregistrerUtilisateurDansLaSessionEtEnvoiLaListe(nom_utilisateur);
            //pour initialiser l'interdit de tableu blanc du Serveur
            interdit_de_dessiner_dans_le_tableau_blanc = iServeur.voirAuthorisationDeDessinerSurTanleauBlanc();
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
            if(role.equals("Professeur") || !interdit_de_dessiner_dans_le_tableau_blanc){
                graphicsContext.clearRect(1, 1, graphicsContext.getCanvas().getWidth() - 2,
                        graphicsContext.getCanvas().getHeight() - 2);

                try {
                    iServeur.supprimerTousLesDessinsDuTableauBlanc(nom_utilisateur);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
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

        final Button interdirLesEtudiantsADissinerButton = new Button("Interdir");
        interdirLesEtudiantsADissinerButton.setOnAction(actionEvent -> {
            try {
                if(role.equals("Professeur")){
                    if(interdit_de_dessiner_dans_le_tableau_blanc){
                        interdirLesEtudiantsADissinerButton.setText("Interdir");
                    }else {
                        interdirLesEtudiantsADissinerButton.setText("Authoriser");
                    }
                    iServeur.changerAuthorisationDesEtudiantsADissinerDuServeur(!interdit_de_dessiner_dans_le_tableau_blanc);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        if(role.equals("Professeur")){
            interdirLesEtudiantsADissinerButton.setVisible(true);
        }else{
            interdirLesEtudiantsADissinerButton.setVisible(false);
        }

        interdirLesEtudiantsADissinerButton.setTranslateX(10);

        buttonBox.getChildren().addAll(colorChooser, sizeChooser, resetButton, interdirLesEtudiantsADissinerButton);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent event) {
                        if(role.equals("Professeur") || !interdit_de_dessiner_dans_le_tableau_blanc){
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
                    }
                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent event) {
                        if(role.equals("Professeur") || !interdit_de_dessiner_dans_le_tableau_blanc){
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
            textFlow.setMaxWidth(150);

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
        textFlow.setMaxWidth(150);

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

    /**
     * Pour interdir ou authoriser les etudiants de dessiner dans le tableau blanc
     * @param interdit_de_dessiner_dans_le_tableau_blanc
     * @throws RemoteException
     */
    @Override
    public void reponseDuServeurPourAuthorisationDesEtudiantsADissinerDuServeur(boolean interdit_de_dessiner_dans_le_tableau_blanc) throws RemoteException {
        this.interdit_de_dessiner_dans_le_tableau_blanc = interdit_de_dessiner_dans_le_tableau_blanc;
    }

    /**
     * Lorsque on click sur le button choisir un fichier, on selectionne le fichier qu'on veut envoyer
     * et utiliser iServeur.envoiFichierATousLesUtilisateurs pour envoyer le nom de fichier avec le tableau des entiers pour le transfere au fichier
     */
    public void choisirUnFichierButtonOnAction(){
        Stage stage = new Stage();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                ArrayList<Integer> inc;
                try (FileInputStream in = new FileInputStream(selectedFile)) {
                    inc = new ArrayList<>();
                    int c=0;
                    while((c=in.read()) != -1) {
                        inc.add(c);
                    }
                }

                iServeur.envoiFichierATousLesUtilisateurs(nom_utilisateur,role,inc, selectedFile.getName());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Recevoir le fichier et afficher son nom qui est clickable pour l'enregistrer ou l'afficher
     * @param nom_utilisateur_source
     * @param role_utilisateur_source
     * @param inc
     * @param nom_fichier
     * @throws RemoteException
     */
    @Override
    public void recevoirFichierDuServeur(String nom_utilisateur_source,String role_utilisateur_source, ArrayList<Integer> inc, String nom_fichier) throws RemoteException {
        String nom_a_afficher = "";
        String couleur_nom;

        if(nom_utilisateur_source.equals(nom_utilisateur)){
            nom_a_afficher = "Moi";
            couleur_nom = "51005B";
        }else if(role_utilisateur_source.equals("Professeur")){
            nom_a_afficher = "Professeur";
            couleur_nom = "5B0000";
        }else{
            nom_a_afficher = nom_utilisateur_source;
            couleur_nom = "015B00";
        }

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(5,5,5,5));

        Text textNomAAfficher = new Text(nom_a_afficher + "\n");
        textNomAAfficher.setFill(Color.web(couleur_nom));

        Text textFichier = new Text(nom_fichier);
        textFichier.setFill(Color.web("002B5B"));

        TextFlow textFlow = new TextFlow(textNomAAfficher,textFichier);
        textFlow.setPadding(new Insets(1,1,5,1));
        textFlow.setMaxWidth(279);

        hbox.getChildren().add(textFlow);

        final ContextMenu contextMenu = new ContextMenu();
        MenuItem telecharger = new MenuItem("Télecharger");
        contextMenu.getItems().addAll(telecharger);
        telecharger.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileOutputStream fileOutputStream;
                String repertoireChoisi = "";

                //Pour distinguer entre les systemes d'exploitation dans le chemin
                String separator;
                if(System.getProperty("os.name").startsWith("Linux") || System.getProperty("os.name").startsWith("MacOS")){
                    separator = "/";
                }
                else{
                    separator = "\\";
                }

                try{
                    //ici pout choisir le repertoire de destination
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    Stage stage = new Stage();
                    File selectedFile = directoryChooser.showDialog(stage);
                    if (selectedFile != null) {
                        repertoireChoisi = selectedFile.getAbsolutePath();
                    }else{
                        repertoireChoisi = System.getProperty("user.home");
                    }

                    fileOutputStream = new FileOutputStream( repertoireChoisi + separator + nom_fichier);
                    String[] extension = nom_fichier.split("\\.");
                    for (int i = 0; i<inc.size(); i++) {
                        int c = inc.get(i);
                        if(extension[extension.length - 1].equals("txt")||
                                extension[extension.length - 1].equals("java")||
                                extension[extension.length - 1].equals("php")||
                                extension[extension.length - 1].equals("c")||
                                extension[extension.length - 1].equals("cpp")||
                                extension[extension.length - 1].equals("xml")
                        ) {
                            fileOutputStream.write((char)c);
                        }
                        else{
                            fileOutputStream.write((byte)c);
                        }
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }catch (Exception e){}
            }
        });

        hbox.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isSecondaryButtonDown()) {
                    contextMenu.show(hbox, event.getScreenX(), event.getScreenY());
                }
            }
        });

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vBoxFichiers.getChildren().add(hbox);
            }
        });
    }
}