package serveur;

import etudiant.IEtudiant;

public class Session {
    public String nom_utilisateur;
    public IEtudiant iEtudiant;

    public Session(String nom_utilisateur, IEtudiant iEtudiant){
        this.nom_utilisateur = nom_utilisateur;
        this.iEtudiant = iEtudiant;
    }

    public String toString(){
        return this.nom_utilisateur;
    }
}
