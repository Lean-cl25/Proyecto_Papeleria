package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    void onVentasPressed(ActionEvent event) {
        cambiarPantalla(event, "/view/Ventas/pantalla-ventas.fxml");
    }

    @FXML
    void onStockPressed(ActionEvent event) {
        cambiarPantalla(event, "/view/Productos/Pantalla-Productos.fxml");
    }

    private void cambiarPantalla(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
