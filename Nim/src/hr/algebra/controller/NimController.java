/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.controller;

import hr.algebra.ClientThread;
import hr.algebra.model.ChatMessage;
import hr.algebra.model.GameState;
import hr.algebra.model.Player;
import hr.algebra.utilities.FileUtils;
import hr.algebra.utilities.MessageUtils;
import hr.algebra.utilities.ReflectionUtils;
import hr.algebra.utilities.SerializationUtils;
import hr.algebra.xml.DOM;
import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * FXML Controller class
 *
 * @author Sime
 */
public class NimController implements Initializable {

    private static final Image burnedMatch = new Image("hr/algebra/images/match_burned.png");
    private static final Image litMatch = new Image("hr/algebra/images/match_lit.png");
    private static final Image unlitMatch = new Image("hr/algebra/images/match_unlit.png");

    private static final String UNLIT = "unlit";
    private static final String LIT = "lit";
    private static final String BURNED = "burned";

    private static final int NUMBER_OF_MATCHES = 16;

    private int matchesBurned;

    private final List<ImageView> selectedMatches = new ArrayList<>();

    private final List<GameState> moves = new ArrayList<>();

    private static Player playerName;

    @FXML
    private Button btnRemoveMatches;
    @FXML
    private GridPane gpMatchArea;
    @FXML
    private Label lblPlayer;

    private ClientThread clientThread;

    // private boolean gameStarted;
    public static void setPlayerName(Player playerName) {
        NimController.playerName = playerName;
    }
    @FXML
    private TextArea taChatMessages;
    @FXML
    private TextField tbMessage;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initClientThread();
        updateLabel(playerName.toString());

    }

    private void initClientThread() {
        clientThread = new ClientThread(this);
        clientThread.setDaemon(true);
        clientThread.start();

    }

    private void disableGame() {
        gpMatchArea.setDisable(true);
        gpMatchArea.setOpacity(0.5);
    }

    private void enableGame() {
        gpMatchArea.setDisable(false);
        gpMatchArea.setOpacity(1);
    }

    @FXML
    private void matchClicked(MouseEvent event) {

        ImageView clickedMatch = (ImageView) event.getSource();

        if (clickedMatch.getAccessibleText().equals(UNLIT)) {
            clickedMatch.setImage(litMatch);
            clickedMatch.setAccessibleText(LIT);
            selectedMatches.add(clickedMatch);

        } else {
            clickedMatch.setImage(unlitMatch);
            clickedMatch.setAccessibleText(UNLIT);
            selectedMatches.remove(clickedMatch);

        }
        btnRemoveMatches.setDisable(!validateSelectedMatches());

    }

    @FXML
    private void removeMatches() {

        for (ImageView selectedMatch : selectedMatches) {
            selectedMatch.setDisable(true);
            selectedMatch.setImage(burnedMatch);
            selectedMatch.setAccessibleText(BURNED);
        }

        selectedMatches.clear();
        btnRemoveMatches.setDisable(true);

        disableGame();
        ClientThread.sendRequest(getCurrentState());
    }

    private void gameEnded(String name) {

        restartGame();

        if (MessageUtils.showEndGameDialog(name)) {

            try {
                DOM.Write(moves);
            } catch (ParserConfigurationException | TransformerException ex) {
                Logger.getLogger(NimController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private boolean validateSelectedMatches() {

        return selectedMatches.stream().map(i -> GridPane.getRowIndex(i)).distinct().count() == 1
                && selectedMatches.size() > 0;

    }

    @FXML
    private void exitGame() {
        System.exit(0);
    }

    private void updateLabel(String player) {
        lblPlayer.setText(player);
    }

    @FXML
    private void generateDocumentation() {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append(System.lineSeparator());
        htmlBuilder.append("<html>");
        htmlBuilder.append(System.lineSeparator());
        htmlBuilder.append("<head>");
        htmlBuilder.append(System.lineSeparator());
        htmlBuilder.append("<title>Dokumentacija</title>");
        htmlBuilder.append(System.lineSeparator());
        htmlBuilder.append("</head>");
        htmlBuilder.append(System.lineSeparator());
        htmlBuilder.append("<body>");
        htmlBuilder.append(System.lineSeparator());
        ReflectionUtils.findAllPackagesAndClasses("src/", htmlBuilder);
        htmlBuilder.append("<p></p>");
        htmlBuilder.append(System.lineSeparator());
        htmlBuilder.append("</body>");
        htmlBuilder.append(System.lineSeparator());
        htmlBuilder.append("</html>");
        htmlBuilder.append(System.lineSeparator());

        try (FileWriter htmlWriter = new FileWriter("dokumentacija.html")) {
            htmlWriter.write(htmlBuilder.toString());

            MessageUtils.showInfoMessage("Uspješno spremanje dokumentacije",
                    "Informacija!",
                    "Datoteka \"dokumentacija.html\""
                    + " je uspješno generirana!", AlertType.INFORMATION);

        } catch (IOException ex) {
            Logger.getLogger(NimController.class.getName()).log(
                    Level.SEVERE, null, ex);
            MessageUtils.showInfoMessage("Neuspješno spremanje dokumentacije",
                    "Informacija!",
                    "Dogodila se greška!", AlertType.ERROR);
        }
    }

    @FXML
    private void saveGame() {
        GameState gameState = getCurrentState();

        try {
            File file = FileUtils.saveFileDialog(btnRemoveMatches.getScene().getWindow(), "ser");
            if (file != null) {
                SerializationUtils.write(gameState, file.getAbsolutePath());
            }
        } catch (IOException ex) {
            Logger.getLogger(NimController.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    private GameState getCurrentState() {
        List<Point> points = new ArrayList<>();
        for (Node node : gpMatchArea.getChildren()) {

            Integer columnIndex = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);

            if (((ImageView) node).getAccessibleText().equals(BURNED)) {
                Point point = new Point(rowIndex, columnIndex);
                points.add(point);
            }

        }
        return new GameState(points, playerName);

    }

    @FXML
    private void loadGame() {
        File file = FileUtils.uploadFileDialog(btnRemoveMatches.getScene().getWindow(), "ser");
        if (file != null) {
            try {
                GameState gs = (GameState) SerializationUtils.read(file.getAbsolutePath());
                loadGameState(gs);
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(NimController.class.getName()).log(Level.SEVERE, null, ex);
                MessageUtils.showInfoMessage("Neuspješno učitavanje igre.",
                        "Greška!!",
                        "Dogodila se greška!", AlertType.ERROR);
            }

        }
    }

    public void loadGameState(GameState gs) {
        moves.add(gs);
        if (!gs.getPlayerOnTurn().equals(playerName)) {
            enableGame();

            for (Point coordinate : gs.getBurnedMatches()) {

                if (getMatchByRowColumnIndex(coordinate.x, coordinate.y).isPresent()) {
                    ImageView match = getMatchByRowColumnIndex(coordinate.x, coordinate.y).get();
                    match.setImage(burnedMatch);
                    match.setAccessibleText(BURNED);
                    match.setDisable(true);
                }
            }
        } else {
            disableGame();
        }

        btnRemoveMatches.setDisable(true);

        matchesBurned = gs.getBurnedMatches().size();
        if (matchesBurned == NUMBER_OF_MATCHES) {
            gameEnded(gs.getPlayerOnTurn().toString());
        }
        selectedMatches.clear();

    }

    @FXML
    private void restartGame() {
        updateLabel(playerName.toString());
        matchesBurned = 0;
        for (Node component : gpMatchArea.getChildren()) {
            if (component instanceof ImageView) {
                ((ImageView) component).setImage(unlitMatch);
                component.setAccessibleText(UNLIT);
                component.setDisable(false);
            }
        }
        selectedMatches.clear();
        btnRemoveMatches.setDisable(true);
        enableGame();
    }

    private Optional<ImageView> getMatchByRowColumnIndex(final int row, final int column) {
        Optional<ImageView> optMatch = Optional.empty();
        Iterable<Node> childrens = gpMatchArea.getChildren();
        for (Node node : childrens) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                return Optional.of((ImageView) node);
            }
        }
        return optMatch;
    }

    @FXML
    private void sendMessage() {
        if (tbMessage.getText().trim().isEmpty()) {
            return;
        }
        try {
            clientThread.sendMessage(tbMessage.getText().trim(), playerName.toString());
            tbMessage.clear();
        } catch (RemoteException ex) {
            Logger.getLogger(NimController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void updateChat(ChatMessage chatMessage) {
        taChatMessages.appendText(chatMessage.toString() + System.lineSeparator());

    }

    @FXML
    private void replayGame(ActionEvent event) {
        File file = FileUtils.uploadFileDialog(btnRemoveMatches.getScene().getWindow(), "xml");

        if (file != null) {
            try {

                replay(DOM.Read(file));

            } catch (SAXException | IOException | ParserConfigurationException ex) {
                Logger.getLogger(NimController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private void replay(List<GameState> gamestates) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (GameState gs : gamestates) {
                    try {
                        Platform.runLater(() -> {
                            lblPlayer.setText(gs.getPlayerOnTurn().getName());

                        });

                        Thread.sleep(1000);

                        for (Point coordinate : gs.getBurnedMatches()) {
                            if (getMatchByRowColumnIndex(coordinate.x, coordinate.y).isPresent()) {

                                Platform.runLater(() -> {
                                    ImageView match = getMatchByRowColumnIndex(coordinate.x, coordinate.y).get();
                                    match.setImage(burnedMatch);
                                    match.setAccessibleText(BURNED);
                                    match.setDisable(true);
                                });

                            }
                        }
                        Thread.sleep(1000);

                    } catch (InterruptedException ex) {
                        Logger.getLogger(NimController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        }
        ).start();

    }
}
