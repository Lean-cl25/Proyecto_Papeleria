package org.example.controllers.Productos;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.database.Conexion;
import org.example.models.Categorias;
import org.example.models.Productos;
import org.example.models.Proveedores;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditProductosController {

    @FXML private TextField txtCodigoBarras;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtPrecioCompra;
    @FXML private TextField txtPrecioVenta;
    @FXML private TextField txtStock;
    @FXML private TextField txtImagen;
    @FXML private Button btnSeleccionarImagen;
    @FXML private Button btnEliminarImagen;
    @FXML private ComboBox<Categorias> cbCategoria;
    @FXML private ComboBox<Proveedores> cbProveedor;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private ImageView imgPreview;

    private Productos producto; // producto a editar
    private boolean productoEditado = false;

    @FXML
    public void initialize() {
        // Bloqueamos código de barras
        txtCodigoBarras.setEditable(false);

        // Botones
        btnCancelar.setOnAction(e -> cerrarVentana());
        btnGuardar.setOnAction(e -> guardarCambios());

        cbCategoria.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Categorias categoria, boolean empty) {
                super.updateItem(categoria, empty);
                setText(empty || categoria == null ? null : categoria.getNombre());
            }
        });

        cbCategoria.setConverter(new javafx.util.StringConverter<Categorias>() {
            @Override
            public String toString(Categorias categoria) {
                return categoria == null ? "" : categoria.getNombre();
            }
            @Override
            public Categorias fromString(String string) {
                return null; // no lo necesitamos aquí
            }
        });

        cbProveedor.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Proveedores proveedor, boolean empty) {
                super.updateItem(proveedor, empty);
                setText(empty || proveedor == null ? null : proveedor.getNombre());
            }
        });

        cbProveedor.setConverter(new javafx.util.StringConverter<Proveedores>() {
            @Override
            public String toString(Proveedores proveedor) {
                return proveedor == null ? "" : proveedor.getNombre();
            }
            @Override
            public Proveedores fromString(String string) {
                return null; // no se usa
            }
        });

        btnSeleccionarImagen.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar imagen del producto");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );

            File archivo = fileChooser.showOpenDialog(btnSeleccionarImagen.getScene().getWindow());
            if (archivo != null) {
                txtImagen.setText(archivo.getAbsolutePath());
                imgPreview.setImage(new javafx.scene.image.Image(archivo.toURI().toString()));
            }
        });

        btnEliminarImagen.setOnAction(e -> {
            txtImagen.clear();
            imgPreview.setImage(null);
        });

        cargarCategorias();
        cargarProveedores();
    }

    public void setProducto(Productos producto) {
        this.producto = producto;
        llenarCampos();
    }

    private void llenarCampos() {
        txtCodigoBarras.setText(producto.getCodigoBarras());
        txtNombre.setText(producto.getNombre());
        txtDescripcion.setText(producto.getDescripcion());
        txtPrecioCompra.setText(producto.getPrecioCompra().toString());
        txtPrecioVenta.setText(producto.getPrecioVenta().toString());
        txtStock.setText(String.valueOf(producto.getStockActual()));
        txtImagen.setText(producto.getImagen());

        if (producto.getImagen() != null) {
            File f = new File(producto.getImagen());
            if(f.exists()) {
                imgPreview.setImage(new Image(f.toURI().toString()));
            }
        }

        if (producto.getCategoria() != null) cbCategoria.getSelectionModel().select(producto.getCategoria());
        if (producto.getProveedor() != null) cbProveedor.getSelectionModel().select(producto.getProveedor());
    }

    private void guardarCambios() {
        try (Connection conn = Conexion.getConnection()) {
            String sql = "UPDATE Productos SET nombre=?, descripcion=?, precio_compra=?, precio_venta=?, stock_actual=?, id_categoria=?, id_proveedor=?, imagen=? WHERE id_producto=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtNombre.getText());
            ps.setString(2, txtDescripcion.getText().isEmpty() ? null : txtDescripcion.getText());
            ps.setBigDecimal(3, new BigDecimal(txtPrecioCompra.getText()));
            ps.setBigDecimal(4, new BigDecimal(txtPrecioVenta.getText()));
            ps.setInt(5, Integer.parseInt(txtStock.getText()));
            ps.setObject(6, cbCategoria.getValue() != null ? cbCategoria.getValue().getIdCategoria() : null);
            ps.setObject(7, cbProveedor.getValue() != null ? cbProveedor.getValue().getIdProveedor() : null);
            ps.setString(8, txtImagen.getText());
            ps.setInt(9, producto.getIdProducto());

            ps.executeUpdate();
            productoEditado = true;
            cerrarVentana();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al actualizar producto: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    public boolean isProductoEditado() {
        return productoEditado;
    }

    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Editar Producto");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cargarCategorias() {
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM Categorias WHERE status = 1");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cbCategoria.getItems().add(new Categorias(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("status")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarProveedores() {
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM Proveedores WHERE status = 1");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cbProveedor.getItems().add(new Proveedores(
                        rs.getInt("id_proveedor"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("direccion"),
                        rs.getInt("status")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
