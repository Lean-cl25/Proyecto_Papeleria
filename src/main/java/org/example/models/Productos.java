package org.example.models;

import java.math.BigDecimal;

public class Productos {
    private int idProducto;
    private String codigoBarras;
    private String nombre;
    private String descripcion;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private int stockActual;
    private String imagen; // ruta o URL de la imagen
    private int status; // 1 = activo, 0 = inactivo

    // Relaciones
    private Categorias categoria;
    private Proveedores proveedor;

    // Campos extra para el control de venta
    private int cantidad = 1;
    private BigDecimal total;

    // 🔹 Constructor vacío
    public Productos() {}

    // Constructor con parámetros actualizado
    public Productos(int idProducto, String codigoBarras, String nombre, String descripcion,
                     BigDecimal precioCompra, BigDecimal precioVenta, int stockActual,
                     String imagen, int status, Categorias categoria, Proveedores proveedor) {
        this.idProducto = idProducto;
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stockActual = stockActual;
        this.imagen = imagen;
        this.status = status;
        this.categoria = categoria;
        this.proveedor = proveedor;
    }

    // 🔹 Getters y Setters
    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    // Getter y Setter
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Categorias getCategoria() {
        return categoria;
    }

    public void setCategoria(Categorias categoria) {
        this.categoria = categoria;
    }

    public Proveedores getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedores proveedor) {
        this.proveedor = proveedor;
    }

    // Getters y setters
    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    // 🔹 toString()
    @Override
    public String toString() {
        return "Productos{" +
                "idProducto=" + idProducto +
                ", codigoBarras='" + codigoBarras + '\'' +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precioCompra=" + precioCompra +
                ", precioVenta=" + precioVenta +
                ", stockActual=" + stockActual +
                ", imagen='" + imagen + '\'' +
                ", status=" + status +
                ", categoria=" + (categoria != null ? categoria.getNombre() : "null") +
                ", proveedor=" + (proveedor != null ? proveedor.getNombre() : "null") +
                '}';
    }
}
