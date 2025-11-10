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

    /*@FXML
    void abrirProductos(ActionEvent event) {
        cambiarPantalla(event, "/view/Productos/Producto.fxml");
    }

    @FXML
    void abrirCategorias(ActionEvent event) {
        cambiarPantalla(event, "/view/Categorias/Categoria.fxml");
    }

    @FXML
    void abrirProveedores(ActionEvent event) {
        cambiarPantalla(event, "/view/Proveedores/Proveedor.fxml");
    }

    @FXML
    void abrirConfiguracion(ActionEvent event) {
        cambiarPantalla(event, "/view/Configuracion.fxml");
    }*/

    private void cambiarPantalla(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

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
