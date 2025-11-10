package org.example.controllers.Productos;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.database.Conexion;
import org.example.models.Categorias;
import org.example.models.Productos;
import org.example.models.Proveedores;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TableColumn;
import javafx.beans.property.ReadOnlyObjectWrapper;


public class ProductoController {

    @FXML private TableView<Productos> tableProductos;
    @FXML private TableColumn<Productos, String> colCodigo;
    @FXML private TableColumn<Productos, String> colNombre;
    @FXML private TableColumn<Productos, String> colCategoria;
    @FXML private TableColumn<Productos, String> colProveedor;
    @FXML private TableColumn<Productos, String> colPrecioVenta;
    @FXML private TableColumn<Productos, Integer> colStock;
    @FXML private TableColumn<Productos, String> colStatus;
    @FXML private TableColumn<Productos, ImageView> colImagen;

    @FXML private Button btnAgregar;
    @FXML private TableColumn<Productos, Void> colEditar;

    private ObservableList<Productos> listaProductos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar columnas
        colCodigo.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getCodigoBarras()));
        colNombre.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNombre()));

        // Mostrar nombre de la categoría
        colCategoria.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(
                        cell.getValue().getCategoria() != null ? cell.getValue().getCategoria().getNombre() : ""
                )
        );

        // Mostrar nombre del proveedor
        colProveedor.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(
                        cell.getValue().getProveedor() != null ? cell.getValue().getProveedor().getNombre() : ""
                )
        );

        colPrecioVenta.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getPrecioVenta().toString())
        );
        colStock.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue().getStockActual())
        );
        colStatus.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getStatus() == 1 ? "Activo" : "Inactivo")
        );

        colImagen.setCellValueFactory(cell -> {
            String ruta = cell.getValue().getImagen();
            ImageView imageView = null;
            if (ruta != null && !ruta.isEmpty()) {
                try {
                    Image img = new Image("file:" + ruta, 50, 50, true, true); // ancho 50, alto 50
                    imageView = new ImageView(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return new ReadOnlyObjectWrapper<>(imageView);
        });

        btnAgregar.setOnAction(e -> AddProductos());

        colEditar.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️");

            {
                btnEditar.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
                btnEditar.setOnAction(event -> {
                    Productos producto = getTableView().getItems().get(getIndex());
                    abrirVentanaEditar(producto);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnEditar);
                }
            }
        });


        // Cargar productos desde la base de datos
        cargarProductosDesdeDB();

        tableProductos.setItems(listaProductos);
    }

    private void cargarProductosDesdeDB() {
        String query = "SELECT p.id_producto, p.codigo_barras, p.nombre, p.descripcion, " +
                "p.precio_compra, p.precio_venta, p.stock_actual, p.imagen, p.status, " +
                "c.id_categoria, c.nombre AS nombre_categoria, c.descripcion AS desc_categoria, " +
                "pr.id_proveedor, pr.nombre AS nombre_proveedor, pr.telefono, pr.email, pr.direccion " +
                "FROM Productos p " +
                "LEFT JOIN Categorias c ON p.id_categoria = c.id_categoria " +
                "LEFT JOIN Proveedores pr ON p.id_proveedor = pr.id_proveedor " +
                "WHERE p.status = 1";

        try {
            Connection conn = Conexion.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            listaProductos.clear();

            while (rs.next()) {
                Categorias categoria = null;
                if (rs.getInt("id_categoria") > 0) {
                    categoria = new Categorias(
                            rs.getInt("id_categoria"),
                            rs.getString("nombre_categoria"),
                            rs.getString("desc_categoria"),
                            rs.getInt("status")
                    );
                }

                Proveedores proveedor = null;
                if (rs.getInt("id_proveedor") > 0) {
                    proveedor = new Proveedores(
                            rs.getInt("id_proveedor"),
                            rs.getString("nombre_proveedor"),
                            rs.getString("telefono"),
                            rs.getString("email"),
                            rs.getString("direccion"),
                            rs.getInt("status")
                    );
                }

                Productos producto = new Productos(
                        rs.getInt("id_producto"),
                        rs.getString("codigo_barras"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getBigDecimal("precio_compra"),
                        rs.getBigDecimal("precio_venta"),
                        rs.getInt("stock_actual"),
                        rs.getString("imagen"),
                        rs.getInt("status"),
                        categoria,
                        proveedor
                );

                listaProductos.add(producto);
            }

            rs.close();
            stmt.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error al cargar productos");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private void AddProductos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Productos/Add-Producto.fxml"));
            Scene scene = new Scene(loader.load());

            Stage modal = new Stage();
            modal.setTitle("Agregar nuevo producto");
            modal.setScene(scene);
            modal.initModality(Modality.APPLICATION_MODAL);

            AddProductosController controller = loader.getController();
            modal.showAndWait();

            // Si el producto se agregó, refrescamos la tabla
            if (controller.isProductoAgregado()) {
                cargarProductosDesdeDB();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void abrirVentanaEditar(Productos producto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Productos/Edit-Producto.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Editar Producto");
            stage.initModality(Modality.APPLICATION_MODAL);

            EditProductosController controller = loader.getController();
            controller.setProducto(producto);

            stage.showAndWait();

            if(controller.isProductoEditado()) {
                cargarProductosDesdeDB();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("No se pudo abrir el formulario de edición");
            alert.showAndWait();
        }
    }



}
