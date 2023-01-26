package serveur;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Main {
    public static void main(String[] args) throws RemoteException, MalformedURLException, AlreadyBoundException {

        Serveur serveur = new Serveur();

        LocateRegistry.createRegistry(1099);
        Naming.bind("irisi",serveur);

        System.out.println("Serveur est pret...");
    }
}
