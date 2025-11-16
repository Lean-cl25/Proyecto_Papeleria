package org.example.controllers.Ventas;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
    @FXML private TableColumn<Productos, Void> colAcciones; // nueva columna para botones
    @FXML private Label lblTotal;
    @FXML private TextField txtEfectivo;



    private ObservableList<Productos> listaVenta = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTabla();
        tableVenta.setItems(listaVenta);
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodigoBarras()));
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colPrecio.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPrecioVenta()));
        colCantidad.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCantidad()));
        colTotal.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTotal()));

        // 🔹 Agregar botones + y - dentro de la tabla
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnMas = new Button("+");
            private final Button btnMenos = new Button("-");
            private final HBox hbox = new HBox(5, btnMenos, btnMas);

            {
                btnMas.setOnAction(event -> {
                    Productos producto = getTableView().getItems().get(getIndex());
                    producto.setCantidad(producto.getCantidad() + 1);
                    producto.setTotal(producto.getPrecioVenta().multiply(BigDecimal.valueOf(producto.getCantidad())));
                    tableVenta.refresh();
                    calcularTotal();
                });

                btnMenos.setOnAction(event -> {
                    Productos producto = getTableView().getItems().get(getIndex());
                    producto.setCantidad(producto.getCantidad() - 1);
                    if (producto.getCantidad() <= 0) {
                        listaVenta.remove(producto); // eliminar si llega a 0
                    } else {
                        producto.setTotal(producto.getPrecioVenta().multiply(BigDecimal.valueOf(producto.getCantidad())));
                    }
                    tableVenta.refresh();
                    calcularTotal();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
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

    // 🔹 Agregar producto (sin duplicar, incrementa cantidad)
    public void agregarProductoTabla(Productos producto) {
        for (Productos p : listaVenta) {
            if (p.getCodigoBarras().equals(producto.getCodigoBarras())) {
                p.setCantidad(p.getCantidad() + 1);
                p.setTotal(p.getPrecioVenta().multiply(BigDecimal.valueOf(p.getCantidad())));
                tableVenta.refresh();
                calcularTotal();
                return;
            }
        }

        // Si no existe, agregar nuevo
        producto.setCantidad(1);
        producto.setTotal(producto.getPrecioVenta());
        listaVenta.add(producto);
        calcularTotal();
    }

    // 🔹 Recalcula el total general
    private void calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Productos p : listaVenta) {
            total = total.add(p.getTotal());
        }
        lblTotal.setText("$" + total);
    }

    // 🔹 Buscar producto en BD
    private Productos buscarProductoPorCodigo(String codigoBarras) {
        String sql = "SELECT * FROM productos WHERE codigo_barras = ? AND status = 1";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigoBarras);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Productos p = new Productos();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setCodigoBarras(rs.getString("codigo_barras"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                p.setStockActual(rs.getInt("stock_actual"));
                p.setCantidad(1);
                p.setTotal(p.getPrecioVenta());
                return p;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 🔹 Buscar manualmente
    @FXML
    void onBuscarProducto(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Ventas/Buscar-Producto.fxml"));
            Parent root = loader.load();

            BuscarProductoController controller = loader.getController();
            controller.setVentasController(this);
            Scene scene = new Scene(root, 810, 600); // tamaño de la ventana modal

            Stage modal = new Stage();
            modal.setScene(scene);
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle("Buscar producto");
            modal.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onFinalizarVenta(ActionEvent event) {
        if (listaVenta.isEmpty()) {
            mostrarAlerta("Venta vacía", "No hay productos en la venta.");
            return;
        }

        BigDecimal totalVenta = new BigDecimal(lblTotal.getText().replace("$", ""));
        BigDecimal montoEfectivo = BigDecimal.ZERO;

        boolean pagoEnEfectivo = false;

        try {
            if (!txtEfectivo.getText().trim().isEmpty()) {
                montoEfectivo = new BigDecimal(txtEfectivo.getText().trim());
                pagoEnEfectivo = montoEfectivo.compareTo(BigDecimal.ZERO) > 0;
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Monto en efectivo inválido.");
            return;
        }

        // 🔹 Si paga en efectivo, calcular vuelto
        if (pagoEnEfectivo) {
            if (montoEfectivo.compareTo(totalVenta) < 0) {
                mostrarAlerta("Efectivo insuficiente", "El monto recibido no cubre la venta.");
                return;
            }

            BigDecimal vuelto = montoEfectivo.subtract(totalVenta);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Vuelto");
            alert.setHeaderText("Vuelto para el cliente");
            alert.setContentText("Debe devolver: €" + vuelto);
            alert.showAndWait();
        }

        // 🔹 Actualizar stock en base de datos
        actualizarStockBD();

        // 🔹 Limpiar la venta
        listaVenta.clear();
        tableVenta.refresh();
        txtEfectivo.clear();
        lblTotal.setText("0.00");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Venta completada");
        alert.setHeaderText(null);
        alert.setContentText("La venta se ha completado correctamente.");
        alert.showAndWait();
    }


    private void actualizarStockBD() {
        String sql = "UPDATE productos SET stock_actual = stock_actual - ? WHERE id_producto = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Productos p : listaVenta) {
                stmt.setInt(1, p.getCantidad());
                stmt.setInt(2, p.getIdProducto());
                stmt.addBatch();
            }

            stmt.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error BD", "Ocurrió un error al actualizar el stock.");
        }
    }


    // 🔹 Mostrar alerta
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    void MenuPrincipal(ActionEvent event) {
        cambiarPantalla(event, "/view/main-view.fxml");
    }

    private void cambiarPantalla(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            double ancho = stage.getWidth();
            double alto = stage.getHeight();

            Scene nuevaScene = new Scene(root, ancho, alto);
            stage.setScene(nuevaScene);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
