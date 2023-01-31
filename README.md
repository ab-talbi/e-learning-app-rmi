# Projet de E-Learning avec java RMI et javaFx


</p>

## Le projet se décompose en trois parties 
- [Partie Serveur](#partie_serveur)
- [Partie Administrateur](#partie_admin)
- [Partie Utilisateur](#partie_utilisateur)

## Partie Serveur <a name = "partie_serveur"></a>
Dans cette partie de serveur, il y a deux interfaces Remote, une pour les utilisateurs, et une autre pour les méthodes de l'administrateur, et une class Serveur qui implemente les deux interfaces.<br>
La class Serveur a comme attribut une liste de session, cette derniere est utilisée pour enregistrer les utilisateurs connectés avec ses interfaces.


### Pour la base de donnees : 
nom : rmi-e-learning-db

     DB_URL = "jdbc:mysql://localhost:3310/rmi-e-learning-db";
     
Ici le port c'est 3310, si vous voulez le changer vous pouvez trouver cette ligne dans la classe Serveur du package serveur, ligne 20.<br><br>
Il y a deux tables, une table pour les classes, et une pour les utilisateurs<br>
Voilà des utilisateurs : <br>
<table>
  <tr>
    <td>nom d'utilisateur</td>
    <td>mot de passe</td>
  </tr>
  <tr>
    <td>admin</td>
    <td>admin</td>
  </tr>
  <tr>
    <td>prof1</td>
    <td>prof1</td>
  </tr>
  <tr>
    <td>ahmed</td>
    <td>ahmed</td>
  </tr>
  <tr>
    <td>ayoub</td>
    <td>ayoub</td>
  </tr>
</table>

## Partie Administrateur <a name = "partie_admin"></a>
Dans cette partie, le package admin, a sa propre interface graphique, apres login, l'admin a le droit de créer une classe et lui associe un professeur, il peut aussi créer un utilisateur (Etudiant, Admin, ou un Professeur).<br>
Pour l'étudiant il faut lui associe une classe par défaut durant sa création.<br>
![image](https://user-images.githubusercontent.com/101748749/215832403-0b15ecb2-bbd9-4c65-af0a-14cafa2bf126.png)
<br>
![image](https://user-images.githubusercontent.com/101748749/215832668-f7d7d84b-b23d-4e90-9b91-8f735274bc7e.png)
<br><br>
## Partie Utilisateur <a name = "partie_utilisateur"></a>
Cette partie utilisateur est pour les professeurs et les étudiants, et ils sont séparés par le role, le professeur a des droits que l'étudiant n'a pas.<br>
Exemple : le professeur peut interdir les étudiants de dessiner dans le tableau blanc, et il peut aussi partager des fichiers dans la zone de partage.<br>
Les étudiants/professeurs peuvent envoyer des messages et des fichiers dans les discussions, soit du groupe ou privée.<br><br>
Dans ce package on trouve une interface qui contients des méthodes Remote, ils sont utilisée par Le Serveur. UtilisateurChatController implemente cette interface.<br><br>
Lancer un utilisateur et se connecter :<br><br>
![image](https://user-images.githubusercontent.com/101748749/215836916-364ff8c1-82c3-4e5f-a688-735201a65f8b.png)
<br><br>
Maintenant nous somme dans la classe1<br>
Il ya 4 zone : <br>
- Zone pour afficher les utilisateurs connectés, et lutilisateur peut sélectionner un autre utilisateur de la liste pour l'envoyer un message privé.
- Zone pour les discussions, soit du groupe ou privé, on peut envoyer soit un message ou un fichier dans la discussion on a choisi.
- Zone pour partage des fichiers, seulement le prof a le droit de diposer un fichier dans cette zone, et le autres utilisateurs (étudiants) peuvent les télechargers
- Zone pour le tableau blanc, ici tous le monde peut dessiner, mais toujours le prof a le droit d'interdir les étudiant à dessiner ou à supprimer les dessins.

<br><br>
Espace du prof1 dans la classe "classe1"
![image](https://user-images.githubusercontent.com/101748749/215841054-7111fd11-0106-4565-9d0d-6e3b0d88a558.png)
<br><br>
Espace du ahmed dans la meme classe "classe1"
![image](https://user-images.githubusercontent.com/101748749/215841632-3b24edcd-e3e7-4bad-b7a5-5493df5b08f2.png)

Ici, les utilisateurs connectés, sont seulement prof1, et létudiant ahmed.<br>
Pour la discussion (c'est une discussion de groupe, puisque l'utilidateur ahmed ou prof n'est pas sélectionné).<br>
Pour le tableau blanc, le prof a dessiner 'bon' en noir et ahmed a dissiné 'jour' en bleu.<br>
Pour la zone de partagé, le prof a partagé deux fichiers, EmploisDuTempsIRISI 2022-2023 S4.pdf et IMG_1025.png, et l'étudiant peut clicker droit sur le fichier et puie clicker sur télecharger, et choisir le chemin pour enregistrer le fichier.<br>
<br><br>
Maintenant pour les discussions privés, on sélectionne l'utilisateur avec "CTRL + click", et la meme chose pour le déselectionner.<br>
<br><br>
Ahmed a envoyer au prof1 un message 'voila mon devoir monsieur' et puie il a envoyé un fichier 'SendAssetForm.vue'
![image](https://user-images.githubusercontent.com/101748749/215843574-3fb219fe-c464-4856-8ce5-61c83eaa22d8.png)
<br><br>
Maintenant prof1 a recoit le message avec le ficher, et peut télecharger le fichier.
![image](https://user-images.githubusercontent.com/101748749/215844484-b14f6947-32ea-4ec2-ba1f-a26caf1891a7.png)

#### Réalisé par : TALBI AYOUB









