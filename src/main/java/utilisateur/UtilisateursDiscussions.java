package utilisateur;

import javafx.scene.layout.VBox;

public class UtilisateursDiscussions {
    public String nom_utilisateur;
    public VBox vBox;

    public UtilisateursDiscussions(String nom_utilisateur, VBox vBox){
        this.nom_utilisateur = nom_utilisateur;
        this.vBox = vBox;
    }

    public String toString(){
        return this.nom_utilisateur;
    }
}
