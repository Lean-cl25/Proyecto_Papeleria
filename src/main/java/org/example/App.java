package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.database.Conexion;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Carga la vista principal
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Papelería - Sistema de Ventas");
        stage.setScene(scene);
        stage.show();

        // Inicia la conexión a la base de datos desde aquí
        try {
            Conexion.conectar();
        } catch (Exception e) {
            System.err.println("❌ No se pudo conectar a la base de datos: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de conexión");
            alert.setHeaderText("No se pudo conectar a la base de datos");
            alert.setContentText("Verifique la configuración de MySQL.\n\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    @Override
    public void stop() throws Exception {
        // Cierra la conexión al salir
        Conexion.cerrarConexion();
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}
