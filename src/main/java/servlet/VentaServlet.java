package servlet;

import dao.VentasJpaController;
import dao.ClientesJpaController;
import dao.DetalleventasJpaController;
import dao.ProductosJpaController;
import dao.exceptions.NonexistentEntityException;
import dto.Clientes;
import dto.Detalleventas;
import dto.Productos;
import dto.Ventas;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

@WebServlet(name = "VentasServlet", urlPatterns = {"/venta"})
public class VentaServlet extends HttpServlet {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");
    VentasJpaController ventaDAO;
    ClientesJpaController clienteDAO;
    ProductosJpaController productoDAO;
    DetalleventasJpaController detalleDAO;

    @Override
    public void init() {
        this.ventaDAO = new VentasJpaController(emf);
        this.clienteDAO = new ClientesJpaController(emf);
        this.productoDAO = new ProductosJpaController(emf);
        this.detalleDAO = new DetalleventasJpaController(emf);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        List<Ventas> ventas = ventaDAO.findVentasEntities();
        JSONArray array = new JSONArray();

        for (Ventas v : ventas) {
            JSONObject obj = new JSONObject();
            obj.put("idVenta", v.getIdVenta());
            obj.put("nombreCliente", v.getIdCliente().getNombre() + " " + v.getIdCliente().getApellido());
            obj.put("fecha", v.getFechaVenta() != null ? v.getFechaVenta().toString() : JSONObject.NULL);
            array.put(obj);
        }

        response.getWriter().write(array.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        /*        Timestamp fechaVenta;*/
        JSONObject data = new JSONObject(new JSONTokener(request.getReader()));
        Ventas venta = new Ventas();

        Clientes cliente = clienteDAO.findClientes(data.getInt("idCliente"));
        if (cliente == null) {
            response.sendError(400, "Cliente no encontrado");
            return;
        }
        venta.setIdCliente(cliente);

        try {
            if (data.has("fecha") && !data.isNull("fecha") && !data.getString("fecha").isBlank()) {
                String fechaStr = data.getString("fecha");
                venta.setFechaVenta(Timestamp.valueOf(fechaStr + " 00:00:00"));
            } else {
                venta.setFechaVenta(new Timestamp(System.currentTimeMillis()));
            }
        } catch (IllegalArgumentException ex) {
            response.sendError(400, "Formato de fecha inválido. Usa yyyy-MM-dd");
            return;
        }

        try {
            // Crear la venta primero sin detalles
            ventaDAO.create(venta);

            if (data.has("detalles")) {
                JSONArray detalles = data.getJSONArray("detalles");

                for (int i = 0; i < detalles.length(); i++) {
                    JSONObject detalleObj = detalles.getJSONObject(i);
                    int idProducto = detalleObj.getInt("idProducto");
                    int cantidadVendida = detalleObj.getInt("cantProd");

                    Productos productoManaged = productoDAO.findProductos(idProducto);
                    if (productoManaged == null) {
                        response.sendError(400, "Producto no encontrado: " + idProducto);
                        return;
                    }

                    //stock y venta
                    if (cantidadVendida > productoManaged.getStock()) {
                        response.sendError(HttpServletResponse.SC_CONFLICT, "Stock insuficiente para el producto: " + productoManaged.getNombre());
                        return;
                    }

                    int nuevoStock = productoManaged.getStock() - cantidadVendida;
                    productoManaged.setStock(nuevoStock);
                    productoDAO.edit(productoManaged);

                    // Crear detalle de venta
                    Detalleventas detalleVenta = new Detalleventas();
                    detalleVenta.setIdVenta(venta);
                    detalleVenta.setIdProducto(productoManaged);
                    detalleVenta.setCantProd(cantidadVendida);
                    detalleVenta.setDetallePrecio(productoManaged.getPrecio() * cantidadVendida);

                    detalleDAO.create(detalleVenta);
                }
            }

            JSONObject res = new JSONObject();
            res.put("idVenta", venta.getIdVenta());
            response.getWriter().print(res.toString());

        } catch (IOException | JSONException e) {
            response.sendError(500, "Error al guardar venta: " + e.getMessage());
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(VentaServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            response.sendError(500, "Error al guardar venta: " + ex.getMessage());
        }
    }
}
