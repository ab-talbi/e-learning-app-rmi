package serveur;

import utilisateur.IUtilisateur;

public class Session {
    public String nom_utilisateur;
    public IUtilisateur iEtudiant;

    public Session(String nom_utilisateur, IUtilisateur iEtudiant){
        this.nom_utilisateur = nom_utilisateur;
        this.iEtudiant = iEtudiant;
    }

    public String toString(){
        return this.nom_utilisateur;
    }
}
