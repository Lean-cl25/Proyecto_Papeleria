package org.example.controllers.Productos;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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


public class ProductoController {

    @FXML private FlowPane contenedorTarjetas;
    @FXML private Button btnAgregar;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private ComboBox<String> cbOrdenar;
    @FXML private CheckBox chkAgotados;


    private ObservableList<Productos> listaProductos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        cargarProductosDesdeDB();
        cargarCategoriasEnFiltro();
        mostrarTarjetas();

        txtBuscar.textProperty().addListener((o, oldV, newV) -> aplicarFiltros());
        cbCategoria.valueProperty().addListener((o, oldV, newV) -> aplicarFiltros());
        cbOrdenar.valueProperty().addListener((o, oldV, newV) -> aplicarFiltros());
        chkAgotados.selectedProperty().addListener((o, oldV, newV) -> aplicarFiltros());

        btnAgregar.setOnAction(e -> AddProductos());
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
            mostrarTarjetas();


        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error al cargar productos");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }

    }

    private void cargarCategoriasEnFiltro() {
        cbCategoria.getItems().clear();
        cbCategoria.getItems().add("Todas");

        String query = "SELECT nombre FROM Categorias WHERE status = 1";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                cbCategoria.getItems().add(rs.getString("nombre"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        cbCategoria.getSelectionModel().selectFirst();
    }

    private void mostrarTarjetas() {
        mostrarTarjetas(listaProductos);
    }

    private void mostrarTarjetas(ObservableList<Productos> lista) {
        contenedorTarjetas.getChildren().clear();

        for (Productos p : lista) {

            VBox card = new VBox();
            card.getStyleClass().add("product-card");
            card.setSpacing(10);
            card.setAlignment(Pos.CENTER);

            // Imagen
            ImageView imgView = new ImageView();
            imgView.setFitWidth(140);
            imgView.setFitHeight(140);
            imgView.getStyleClass().add("card-image");

            if (p.getImagen() != null && !p.getImagen().isEmpty()) {
                try {
                    imgView.setImage(new Image("file:" + p.getImagen()));
                } catch (Exception e) {
                    imgView.setImage(null);
                }
            }

            Label nombre = new Label(p.getNombre());
            nombre.getStyleClass().add("card-title");

            Label categoria = new Label("Categoría: " +
                    (p.getCategoria() != null ? p.getCategoria().getNombre() : "N/A"));
            categoria.getStyleClass().add("card-sub");

            Label stock = new Label("Stock: " + p.getStockActual());
            stock.getStyleClass().add("card-sub");

            HBox botones = new HBox();
            botones.setAlignment(Pos.CENTER);
            botones.getStyleClass().add("card-buttons");

            Button btnEditar = new Button("Editar");
            btnEditar.getStyleClass().add("btn-edit");
            btnEditar.setOnAction(e -> abrirVentanaEditar(p));

            Button btnEliminar = new Button("Eliminar");
            btnEliminar.getStyleClass().add("btn-delete");
            btnEliminar.setOnAction(e -> desactivarProducto(p));

            botones.getChildren().addAll(btnEditar, btnEliminar);
            card.getChildren().addAll(imgView, nombre, categoria, stock, botones);
            contenedorTarjetas.getChildren().add(card);
        }
    }


    private void aplicarFiltros() {

        ObservableList<Productos> filtrados = FXCollections.observableArrayList(listaProductos);

        // 🔍 Filtrar búsqueda
        String texto = txtBuscar.getText().toLowerCase();
        if (!texto.isEmpty()) {
            filtrados.removeIf(p ->
                    !p.getNombre().toLowerCase().contains(texto) &&
                            !p.getCodigoBarras().toLowerCase().contains(texto)
            );
        }

        // 📂 Filtrar categoría
        String cat = cbCategoria.getValue();
        if (cat != null && !cat.equals("Todas")) {
            filtrados.removeIf(p ->
                    p.getCategoria() == null ||
                            !p.getCategoria().getNombre().equals(cat)
            );
        }

        // 🔴 Mostrar agotados
        if (chkAgotados.isSelected()) {
            filtrados.removeIf(p -> p.getStockActual() > 0);
        }

        // 📉 Ordenar stock
        if (cbOrdenar.getValue() != null) {
            switch (cbOrdenar.getValue()) {
                case "Stock ascendente" -> filtrados.sort((a, b) -> a.getStockActual() - b.getStockActual());
                case "Stock descendente" -> filtrados.sort((a, b) -> b.getStockActual() - a.getStockActual());
            }
        }

        // Mostrar resultados
        mostrarTarjetas(filtrados);
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

    private void desactivarProducto(Productos producto) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Deseas eliminar este producto?");
        confirm.setContentText("Producto: " + producto.getNombre());

        if (confirm.showAndWait().get() == ButtonType.OK) {
            String query = "UPDATE Productos SET status = 0 WHERE id_producto = " + producto.getIdProducto();

            try (Connection conn = Conexion.getConnection();
                 Statement stmt = conn.createStatement()) {

                int filas = stmt.executeUpdate(query);
                if (filas > 0) {
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setHeaderText("Producto eliminado correctamente");
                    info.showAndWait();
                    cargarProductosDesdeDB(); // recarga la tabla
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setHeaderText("No se pudo eliminado el producto");
                    error.showAndWait();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setHeaderText("Error al inactivar producto");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        }
    }




}
