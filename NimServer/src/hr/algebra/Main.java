/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra;

import hr.algebra.model.MessengerService;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zakesekresa
 */
public class Main {

    public static void main(String[] args) {
        try {
            ServerThread serverThread;
            serverThread = new ServerThread();
            serverThread.start();

            MessengerService server = serverThread;
            MessengerService stub = (MessengerService) UnicastRemoteObject
                    .exportObject((MessengerService) server, 0);

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("MessengerService", stub);
        } catch (RemoteException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
