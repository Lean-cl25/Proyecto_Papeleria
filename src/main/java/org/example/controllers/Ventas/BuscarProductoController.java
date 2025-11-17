package org.example.controllers.Ventas;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.example.database.Conexion;
import org.example.models.Productos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.geometry.Insets;

public class BuscarProductoController {

    @FXML private TextField txtBuscar;

    @FXML private FlowPane contenedorTarjetas;


    private VentasController ventasController;
    private final ObservableList<Productos> listaProductos = FXCollections.observableArrayList();

    public void setVentasController(VentasController ventasController) {
        this.ventasController = ventasController;
        cargarProductos(""); // carga inicial
    }

    @FXML
    void onBuscar() {
        cargarProductos(txtBuscar.getText().trim());
    }

    private void cargarProductos(String filtro) {
        contenedorTarjetas.getChildren().clear();

        String sql = "SELECT * FROM productos WHERE status = 1 AND (nombre LIKE ? OR codigo_barras LIKE ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + filtro + "%");
            stmt.setString(2, "%" + filtro + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                Productos p = new Productos();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setCodigoBarras(rs.getString("codigo_barras"));
                p.setNombre(rs.getString("nombre"));
                p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                p.setImagen(rs.getString("imagen"));
                p.setStockActual(rs.getInt("stock_actual"));


                contenedorTarjetas.getChildren().add(crearTarjeta(p));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox crearTarjeta(Productos p) {
        VBox card = new VBox(10);
        card.setPrefWidth(180);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER); //
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: #ddd;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10,0.2,0,2);"
        );

        // Imagen
        ImageView imgView = new ImageView();
        try {
            Image img = p.getImagen() != null && !p.getImagen().isEmpty() ?
                    new Image("file:" + p.getImagen(), 120, 120, true, true) :
                    new Image("https://via.placeholder.com/120");

            imgView.setImage(img);
        } catch (Exception e) {
            imgView.setImage(new Image("https://via.placeholder.com/120"));
        }

        imgView.setFitWidth(120);
        imgView.setFitHeight(120);
        imgView.setPreserveRatio(true);

        Label lblNombre = new Label(p.getNombre());
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label lblCodigo = new Label("Código: " + p.getCodigoBarras());
        lblCodigo.setStyle("-fx-text-fill: #555;");

        Label lblPrecio = new Label("Precio: $" + p.getPrecioVenta());
        lblPrecio.setStyle("-fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        Button btn = new Button("Seleccionar");
        btn.setStyle(
                "-fx-background-color: #2196F3;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 5 15;"
        );

        btn.setOnAction(e -> {
            ventasController.agregarProductoDesdeModal(p);
            txtBuscar.getScene().getWindow().hide();
        });

        card.getChildren().addAll(imgView, lblNombre, lblCodigo, lblPrecio, btn);
        return card;
    }



}
