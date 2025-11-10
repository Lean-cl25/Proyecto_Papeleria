package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavbarController {

    @FXML
    void MenuPrincipal(ActionEvent event) {
        cambiarPantalla(event, "/view/main-view.fxml");
    }

    @FXML
    void abrirProductos(ActionEvent event) {
        cambiarPantalla(event, "/view/Productos/Pantalla-Productos.fxml");
    }

    @FXML
    void abrirCategorias(ActionEvent event) {
        cambiarPantalla(event, "/view/Categorias/Pantalla-Categorias.fxml");
    }

    @FXML
    void abrirProveedores(ActionEvent event) {
        cambiarPantalla(event, "/view/Proveedores/Pantalla-Proveedores.fxml");
    }



    private void cambiarPantalla(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 🔹 Guarda el tamaño actual antes de cambiar la escena
            double ancho = stage.getWidth();
            double alto = stage.getHeight();

            Scene nuevaScene = new Scene(root, ancho, alto);
            stage.setScene(nuevaScene);
            stage.setMaximized(true); // asegura que siga maximizado
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
