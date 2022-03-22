/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra;

import hr.algebra.controller.NimController;
import hr.algebra.model.ChatMessage;
import hr.algebra.model.GameState;
import hr.algebra.model.MessengerService;
import hr.algebra.utilities.ByteUtils;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 *
 * @author dnlbe
 */
public class ClientThread extends Thread {

    private static final String PROPERTIES_FILE = "..\\socket.properties";
    private static final String CLIENT_PORT = "CLIENT_PORT";
    private static final String GROUP = "GROUP";
    private static final String PORT = "PORT";
    private static final String HOST = "HOST";
    private static final Properties PROPERTIES = new Properties();

    private Registry registry;
    private MessengerService server;

    static {
        try {
            PROPERTIES.load(new FileInputStream(PROPERTIES_FILE));
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private final NimController controller;

    public ClientThread(NimController controller) {
        this.controller = controller;
        initMessaging();
    }

    private void initMessaging() {
        try {
            registry = LocateRegistry.getRegistry();
            server = (MessengerService) registry
                    .lookup("MessengerService");
        } catch (RemoteException | NotBoundException remoteException) {
            Logger.getLogger(NimController.class.getName()).log(Level.SEVERE, null, remoteException);
        }
    }

    public static void sendRequest(GameState gs) {
        try (Socket clientSocket = new Socket(PROPERTIES.getProperty(HOST), Integer.valueOf(PROPERTIES.getProperty(PORT)))) {
            System.err.println("Client is connecting to: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
            sendSerializableRequest(clientSocket, gs);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void sendSerializableRequest(Socket clientSocket, GameState gs) throws IOException, ClassNotFoundException {
        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
        oos.writeObject(gs);
    }

    @Override
    public void run() {
        try (MulticastSocket client = new MulticastSocket(Integer.valueOf(PROPERTIES.getProperty(CLIENT_PORT)))) {
            InetAddress groupAddress = InetAddress.getByName(PROPERTIES.getProperty(GROUP));
            System.err.println(controller.hashCode() + " joining group");
            client.joinGroup(groupAddress);

            while (true) {
                System.err.println(controller.hashCode() + " listening...");
                byte[] packetByteLength = new byte[4];
                DatagramPacket packet = new DatagramPacket(packetByteLength, packetByteLength.length);
                client.receive(packet);
                int length = ByteUtils.byteArrayToInt(packetByteLength);

                // we can read payload of that length
                byte[] packetBytes = new byte[length];
                packet = new DatagramPacket(packetBytes, packetBytes.length);
                client.receive(packet);
                try (ByteArrayInputStream bais = new ByteArrayInputStream(packetBytes);
                        ObjectInputStream ois = new ObjectInputStream(bais)) {
                    Object readObject = ois.readObject();
                    if (readObject instanceof GameState) {
                        Platform.runLater(() -> {
                            controller.loadGameState((GameState) readObject);
                        });
                    } else if (readObject instanceof ChatMessage) {
                        Platform.runLater(() -> {
                            controller.updateChat((ChatMessage) readObject);
                        });

                    }

                }
            }

        } catch (SocketException | UnknownHostException e) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException | ClassNotFoundException e) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void sendMessage(String content, String author) throws RemoteException {
        if(server==null) return;
        server.sendMessage(new ChatMessage(content, author));
    }
}
