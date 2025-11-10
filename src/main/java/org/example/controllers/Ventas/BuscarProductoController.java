package org.example.controllers.Ventas;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.example.database.Conexion;
import org.example.models.Productos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BuscarProductoController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Productos> tableProductos;
    @FXML private TableColumn<Productos, String> colCodigo;
    @FXML private TableColumn<Productos, String> colNombre;
    @FXML private TableColumn<Productos, BigDecimal> colPrecio;
    @FXML private TableColumn<Productos, String> colImagen;
    @FXML private TableColumn<Productos, Void> colSeleccionar;

    private VentasController ventasController;
    private final ObservableList<Productos> listaProductos = FXCollections.observableArrayList();

    public void setVentasController(VentasController ventasController) {
        this.ventasController = ventasController;
        configurarTabla();
        cargarProductos(""); // carga inicial
    }

    @FXML
    void onBuscar() {
        cargarProductos(txtBuscar.getText().trim());
    }

    private void configurarTabla() {
        // ✅ Vincula las columnas con las propiedades del modelo
        colCodigo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodigoBarras()));
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colPrecio.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPrecioVenta()));

        // 💡 Esta línea es la que te falta
        colImagen.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getImagen()));

        // ✅ Configura cómo se muestra la imagen
        colImagen.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String ruta, boolean empty) {
                super.updateItem(ruta, empty);

                if (empty || ruta == null || ruta.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image img = ruta.startsWith("http") ?
                                new Image(ruta, 60, 60, true, true) :
                                new Image("file:" + ruta, 60, 60, true, true);

                        imageView.setImage(img);
                        imageView.setFitWidth(60);
                        imageView.setFitHeight(60);
                        imageView.setPreserveRatio(true);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                        setGraphic(null);
                    }
                }
            }
        });

        // ✅ Configura el botón Seleccionar
        Callback<TableColumn<Productos, Void>, TableCell<Productos, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btn = new Button("Seleccionar");

            {
                btn.setOnAction(e -> {
                    Productos p = getTableView().getItems().get(getIndex());
                    ventasController.agregarProductoTabla(p);
                    txtBuscar.getScene().getWindow().hide();
                });
                btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        };

        colSeleccionar.setCellFactory(cellFactory);
    }

    private void cargarProductos(String filtro) {
        listaProductos.clear();
        String sql = "SELECT * FROM productos WHERE status = 1 AND (nombre LIKE ? OR codigo_barras LIKE ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String like = "%" + filtro + "%";
            stmt.setString(1, like);
            stmt.setString(2, like);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Productos p = new Productos();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setCodigoBarras(rs.getString("codigo_barras"));
                p.setNombre(rs.getString("nombre"));
                p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                p.setImagen(rs.getString("imagen")); // NUEVO
                listaProductos.add(p);
            }

            tableProductos.setItems(listaProductos);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
