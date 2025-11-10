package org.example.controllers.Productos;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.database.Conexion;
import org.example.models.Categorias;
import org.example.models.Proveedores;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.scene.image.ImageView;



public class AddProductosController {

    @FXML private TextField txtCodigoBarras;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtPrecioCompra;
    @FXML private TextField txtPrecioVenta;
    @FXML private TextField txtStock;
    @FXML private TextField txtImagen;
    @FXML private Button btnSeleccionarImagen;
    @FXML private ComboBox<Categorias> cbCategoria;
    @FXML private ComboBox<Proveedores> cbProveedor;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private ImageView imgPreview;
    @FXML private Button btnEliminarImagen;

    private boolean productoAgregado = false;

    @FXML
    public void initialize() {
        txtCodigoBarras.requestFocus();

        cargarCategorias();
        cargarProveedores();

        btnCancelar.setOnAction(e -> cerrarVentana());
        btnGuardar.setOnAction(e -> guardarProducto());


        cbCategoria.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Categorias categoria, boolean empty) {
                super.updateItem(categoria, empty);
                setText(empty || categoria == null ? null : categoria.getNombre());
            }
        });

        cbCategoria.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Categorias categoria, boolean empty) {
                super.updateItem(categoria, empty);
                setText(empty || categoria == null ? null : categoria.getNombre());
            }
        });

        cbProveedor.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Proveedores proveedor, boolean empty) {
                super.updateItem(proveedor, empty);
                setText(empty || proveedor == null ? null : proveedor.getNombre());
            }
        });

        cbProveedor.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Proveedores proveedor, boolean empty) {
                super.updateItem(proveedor, empty);
                setText(empty || proveedor == null ? null : proveedor.getNombre());
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

    private void guardarProducto() {
        // Primero, limpiamos estilos de error anteriores
        limpiarEstilosError();
        boolean hayError = false;

        //Validar nombre
        if (txtNombre.getText().trim().isEmpty()) {
            marcarCampoError(txtNombre);
            hayError = true;
        }

        //Validar precios
        if (txtPrecioCompra.getText().trim().isEmpty()) {
            marcarCampoError(txtPrecioCompra);
            hayError = true;
        }

        if (txtPrecioVenta.getText().trim().isEmpty()) {
            marcarCampoError(txtPrecioVenta);
            hayError = true;
        }

        //Validar stock
        if (txtStock.getText().trim().isEmpty()) {
            marcarCampoError(txtStock);
            hayError = true;
        }

        //Si hay algún error, mostramos alerta y detenemos ejecución
        if (hayError) {
            mostrarAlerta("Por favor, complete todos los campos obligatorios antes de guardar.", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = Conexion.getConnection()) {

            String codigo = txtCodigoBarras.getText().trim();
            if (codigo.isEmpty()) {
                codigo = generarCodigoEAN13();
                txtCodigoBarras.setText(codigo);
            }

            String sql = "INSERT INTO Productos (codigo_barras, nombre, descripcion, precio_compra, precio_venta, stock_actual, id_categoria, id_proveedor, imagen, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, txtCodigoBarras.getText());
            ps.setString(2, txtNombre.getText());

            // Descripción opcional
            String descripcion = txtDescripcion.getText().trim();
            if (descripcion.isEmpty()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, descripcion);
            }

            ps.setBigDecimal(4, new BigDecimal(txtPrecioCompra.getText()));
            ps.setBigDecimal(5, new BigDecimal(txtPrecioVenta.getText()));
            ps.setInt(6, Integer.parseInt(txtStock.getText()));

            // Categoría y proveedor pueden ser nulos
            if (cbCategoria.getValue() == null) {
                ps.setNull(7, java.sql.Types.INTEGER);
            } else {
                ps.setInt(7, cbCategoria.getValue().getIdCategoria());
            }

            if (cbProveedor.getValue() == null) {
                ps.setNull(8, java.sql.Types.INTEGER);
            } else {
                ps.setInt(8, cbProveedor.getValue().getIdProveedor());
            }

            String imagen = txtImagen.getText().trim();
            if (imagen.isEmpty()) {
                ps.setNull(9, java.sql.Types.VARCHAR);
            } else {
                ps.setString(9, imagen);
            }

            int filas = ps.executeUpdate();

            if (filas > 0) {
                productoAgregado = true;
                mostrarAlerta("Producto agregado correctamente", Alert.AlertType.INFORMATION);
                Stage stage = (Stage) btnGuardar.getScene().getWindow();
                stage.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al guardar el producto: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (NumberFormatException e) {
            mostrarAlerta("Por favor, asegúrese de que los campos numéricos sean válidos.", Alert.AlertType.WARNING);
        }
    }



    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Gestión de Productos");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public boolean isProductoAgregado() {
        return productoAgregado;
    }

    public void setProductoAgregado(boolean productoAgregado) {
        this.productoAgregado = productoAgregado;
    }

    //Genera un código de barras EAN-13 válido (13 dígitos).
    private String generarCodigoEAN13() {
        //Generamos los primeros 12 dígitos de forma aleatoria
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            int digito = (int) (Math.random() * 10);
            codigo.append(digito);
        }

        //Calculamos el dígito de control (checksum)
        int checksum = calcularChecksumEAN13(codigo.toString());
        codigo.append(checksum);

        return codigo.toString();
    }

    //Calcula el dígito de control para un código EAN-13 (los primeros 12 dígitos).
    private int calcularChecksumEAN13(String codigo12) {
        int suma = 0;
        for (int i = 0; i < codigo12.length(); i++) {
            int digito = Character.getNumericValue(codigo12.charAt(i));
            // posiciones pares se multiplican por 3
            if ((i + 1) % 2 == 0) {
                suma += digito * 3;
            } else {
                suma += digito;
            }
        }
        int resto = suma % 10;
        return (resto == 0) ? 0 : 10 - resto;
    }

    private void marcarCampoError(TextField campo) {
        campo.getStyleClass().add("text-field-error");
    }

    private void limpiarEstilosError() {
        txtNombre.getStyleClass().remove("text-field-error");
        txtPrecioCompra.getStyleClass().remove("text-field-error");
        txtPrecioVenta.getStyleClass().remove("text-field-error");
        txtStock.getStyleClass().remove("text-field-error");
    }

}
