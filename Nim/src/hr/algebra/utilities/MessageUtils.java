/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.utilities;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 *
 * @author dnbele
 */
public class MessageUtils {

    public static void showInfoMessage(String title, String headerText, String contentText, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public static boolean showEndGameDialog(String winner) {

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game ended!");
        alert.setHeaderText("Congrats");
        alert.setContentText(winner + " won.");

        ButtonType buttonOK = new ButtonType("OK");
        ButtonType buttonSave = new ButtonType("Save replay");

        alert.getButtonTypes().setAll(buttonOK, buttonSave);

        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == buttonSave;

    }
}
