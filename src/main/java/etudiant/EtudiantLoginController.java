package etudiant;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import serveur.IServeur;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static etudiant.EtudiantChatController.nom_utilisateur;
import static etudiant.EtudiantChatController.role;

public class EtudiantLoginController implements Initializable {

    @FXML
    private Button annulerButton;
    @FXML
    private ImageView logoImageView;
    @FXML
    private TextField usernameInput;
    @FXML
    private PasswordField passwordInput;
    @FXML
    private Label errorMessageLogin;

    public EtudiantLoginController() {}

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File logoFile = new File("Images/logo.png");
        Image logoImage = new Image(logoFile.toURI().toString());
        logoImageView.setImage(logoImage);

        //Evenement pour fair login en utilisant ENTER sur l'input de mot de passe
        passwordInput.addEventFilter(KeyEvent.KEY_PRESSED, event->{
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    loginButtonOnAction();
                } catch (IOException | NotBoundException | SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void annulerButtonOnAction() {
        Stage stage = (Stage) annulerButton.getScene().getWindow();
        stage.close();
        Platform.exit();
        System.exit(0);
    }

    public void loginButtonOnAction() throws IOException, NotBoundException, SQLException {
        if(usernameInput.getText().isBlank() == true || passwordInput.getText().isBlank() == true){
            errorMessageLogin.setText("Remplir les champs d'abord");
        }else{
            String url = "rmi://127.0.0.1/irisi";
            IServeur iServeur = (IServeur) Naming.lookup(url);
            //System.out.println(iServeur.echo("Hello world"));
            /**
             * seConnecter est une methode qui renvoi un message
             * sois success/le message de succes
             * ou error/le message d'erreur
             */
            String[] reponse = iServeur.seConnecter(usernameInput.getText(),passwordInput.getText());

            String erreurOuSuccess = reponse[0];
            String message = reponse[1]; //Pour success ==> le role, pour erreur ==> message d'erreeur
            if(erreurOuSuccess.equals("erreur")){
                errorMessageLogin.setText(message);
            }else{
                nom_utilisateur = usernameInput.getText();
                role = reponse[1];
                Stage stage = (Stage) annulerButton.getScene().getWindow();
                stage.close();

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("chat.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 733, 520);
                Stage newStage = new Stage();
                newStage.setTitle("Chat");
                newStage.setScene(scene);
                newStage.show();
            }
        }
    }
}