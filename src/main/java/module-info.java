module etudiant {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;
    requires java.sql;

    exports etudiant;
    opens etudiant to javafx.fxml;
    exports serveur;
    opens serveur to java.rmi;
}