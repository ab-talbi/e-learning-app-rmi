package serveur;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

public interface IServeurPourAdmin extends Remote {
    public ArrayList<String> afficherLaListeDesEtudiants() throws RemoteException, SQLException;
}
