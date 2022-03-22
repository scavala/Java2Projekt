/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra;

/**
 *
 * @author zakesekresa
 */
import hr.algebra.model.ChatMessage;
import hr.algebra.model.GameState;
import hr.algebra.model.MessengerService;
import hr.algebra.utilities.ByteUtils;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread extends Thread implements MessengerService {

    private static final String PROPERTIES_FILE = "..\\socket.properties";
    private static final String CLIENT_PORT = "CLIENT_PORT";
    private static final String GROUP = "GROUP";
    private static final String PORT = "PORT";
    private static final Properties PROPERTIES = new Properties();

    private int totalMessages;

    static {
        try {
            PROPERTIES.load(new FileInputStream(PROPERTIES_FILE));
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static final LinkedBlockingDeque<GameState> gameStates = new LinkedBlockingDeque<>();
    private static final LinkedBlockingDeque<ChatMessage> messages = new LinkedBlockingDeque<>();

    private static void acceptRequests() {
        try (ServerSocket serverSocket = new ServerSocket(Integer.valueOf(PROPERTIES.getProperty(PORT)))) {
            System.err.println("Server listening on port: " + serverSocket.getLocalPort());
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.err.println("Client connected from port: " + clientSocket.getPort());
                new Thread(() -> processSerializableClient(clientSocket)).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void processSerializableClient(Socket clientSocket) {
        try (ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {
            GameState gamestate = (GameState) ois.readObject();
            gameStates.add(gamestate);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try (DatagramSocket server = new DatagramSocket()) {
            System.err.println("Server multicasting on port:" + server.getLocalPort());
            new Thread(() -> acceptRequests()).start();
            while (true) {
                if (!gameStates.isEmpty()) {

                    sendPacket(server, gameStates);
                }
                if (!messages.isEmpty()) {

                    sendPacket(server, messages);
                }
            }
        } catch (SocketException e) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void sendPacket(final DatagramSocket server, LinkedBlockingDeque<?> items) throws IOException, UnknownHostException, NumberFormatException {
        // serialize suspect in byte[]
        byte[] gamestateBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(items.getFirst());
            items.clear();
            oos.flush();
            gamestateBytes = baos.toByteArray();
        }

        // before sending byte[], write byte[].length into payload
        byte[] gamestateBytesLength = ByteUtils.intToByteArray(gamestateBytes.length);
        InetAddress groupAddress = InetAddress.getByName(PROPERTIES.getProperty(GROUP));
        DatagramPacket packet = new DatagramPacket(
                gamestateBytesLength,
                gamestateBytesLength.length,
                groupAddress, Integer.valueOf(PROPERTIES.getProperty(CLIENT_PORT))
        );
        server.send(packet);

        packet = new DatagramPacket(
                gamestateBytes,
                gamestateBytes.length,
                groupAddress, Integer.valueOf(PROPERTIES.getProperty(CLIENT_PORT)));
        server.send(packet);
    }

    @Override
    public synchronized void sendMessage(ChatMessage message) throws RemoteException {
        totalMessages++;
        messages.add(message);
    }
}
