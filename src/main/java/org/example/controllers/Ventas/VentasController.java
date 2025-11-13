package org.example.controllers.Ventas;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.database.Conexion;
import org.example.models.Productos;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VentasController {

    @FXML private TextField txtCodigoBarras;
    @FXML private TableView<Productos> tableVenta;
    @FXML private TableColumn<Productos, String> colCodigo;
    @FXML private TableColumn<Productos, String> colNombre;
    @FXML private TableColumn<Productos, BigDecimal> colPrecio;
    @FXML private TableColumn<Productos, Integer> colCantidad;
    @FXML private TableColumn<Productos, BigDecimal> colTotal;
    @FXML private Label lblTotal;

    @FXML
    public void initialize() {
        configurarTabla();
    }

    @FXML
    void MenuPrincipal(ActionEvent event) {
        cambiarPantalla(event, "/view/main-view.fxml");
    }

    private void configurarTabla() {
        // 🔹 Vincula columnas con las propiedades del modelo
        colCodigo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCodigoBarras()));
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));
        colPrecio.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrecioVenta()));

        // Si tu tabla tiene cantidad y total, puedes inicializarlas en 1 y precioVenta respectivamente
        colCantidad.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(1)); // valor fijo por ahora
        colTotal.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrecioVenta()));
    }

    // 🔹 Evento: cuando el usuario presiona Enter tras escanear el código
    @FXML
    void onCodigoEnter(ActionEvent event) {
        String codigo = txtCodigoBarras.getText().trim();
        if (codigo.isEmpty()) return;

        Productos producto = buscarProductoPorCodigo(codigo);
        if (producto != null) {
            agregarProductoTabla(producto);
            txtCodigoBarras.clear();
        } else {
            mostrarAlerta("Producto no encontrado", "No existe un producto con código: " + codigo);
        }
    }

    // 🔹 Evento: abrir ventana para buscar manualmente el producto
    @FXML
    void onBuscarProducto(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Ventas/Buscar-Producto.fxml"));
            Parent root = loader.load();

            BuscarProductoController controller = loader.getController();
            controller.setVentasController(this);

            Stage modal = new Stage();
            modal.setScene(new Scene(root));
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle("Buscar producto");
            modal.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Agrega un producto a la tabla
    public void agregarProductoTabla(Productos producto) {
        tableVenta.getItems().add(producto);
        calcularTotal();
    }

    // 🔹 Recalcula el total de la venta
    private void calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Productos p : tableVenta.getItems()) {
            total = total.add(p.getPrecioVenta());
        }
        lblTotal.setText("$" + total);
    }

    // 🔹 Busca un producto en la BD por código de barras
    private Productos buscarProductoPorCodigo(String codigoBarras) {
        String sql = "SELECT * FROM productos WHERE codigo_barras = ? AND status = 1";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigoBarras);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Productos p = new Productos();
                p.setIdProducto(rs.getInt("id_categoria"));
                p.setCodigoBarras(rs.getString("codigo_barras"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                p.setStockActual(rs.getInt("stock_actual"));
                return p;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 🔹 Muestra una alerta
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    //Cambio de Pantallas parte Principal
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
