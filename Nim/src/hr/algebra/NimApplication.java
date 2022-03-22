/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra;

import hr.algebra.controller.NimController;
import hr.algebra.model.Player;
import java.io.IOException;
import java.util.Optional;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Sime
 */
public class NimApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        NimController.setPlayerName(new Player(enterNameDialog()));
        Parent root = FXMLLoader.load(getClass().getResource("view/Nim.fxml"));
        Scene scene = new Scene(root);
        primaryStage.getIcons().add(new Image("hr/algebra/images/icon.png"));
        primaryStage.setTitle("Nim");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private String enterNameDialog() {
        TextInputDialog dialog = new TextInputDialog("Joža");
        dialog.setTitle("Nim");
        dialog.setHeaderText("Pozdrav, prije igranja moraš unijeti ime!");
        dialog.setContentText("Ime:");
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent() || result.get().trim().isEmpty()) {
            System.exit(0);
        }
        return result.get().trim();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
