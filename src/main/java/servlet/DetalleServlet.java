package servlet;

import dto.Detalleventas;
import dto.Productos;
import dto.Ventas;
import dao.DetalleventasJpaController;
import dao.ProductosJpaController;
import dao.VentasJpaController;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.json.*;

@WebServlet(name = "DetalleServlet", urlPatterns = {"/detalle"})
public class DetalleServlet extends HttpServlet {

    private EntityManagerFactory emf;
    private DetalleventasJpaController detCtrl;
    private VentasJpaController ventaCtrl;
    private ProductosJpaController prodCtrl;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");
        detCtrl = new DetalleventasJpaController(emf);
        ventaCtrl = new VentasJpaController(emf);
        prodCtrl = new ProductosJpaController(emf);
    }

    // GET /detalle
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            List<Detalleventas> lista = detCtrl.findDetalleventasEntities();
            JSONArray arr = new JSONArray();
            for (Detalleventas d : lista) {
                JSONObject o = new JSONObject();
                o.put("idVenta", d.getIdVenta().getIdVenta());
                o.put("idProducto", d.getIdProducto().getIdProducto());
                o.put("producto", d.getIdProducto().getNombre());
                o.put("cantidad", d.getCantProd());
                o.put("precio", d.getDetallePrecio());
                o.put("subtotal", d.getSbttPrecio());
                arr.put(o);
            }
            resp.setContentType("application/json");
            resp.getWriter().print(arr.toString());
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al listar detalles");
        }
    }

    // POST /detalle
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            // Leer JSON
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString();
            System.out.println("DETALLE.POST BODY: " + body);
            JSONObject json = new JSONObject(sb.toString());

            int idVenta = json.getInt("idVenta");
            JSONArray dets = json.getJSONArray("detalles");

            Ventas venta = ventaCtrl.findVentas(idVenta);
            if (venta == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Venta no encontrada");
                return;
            }

            // Procesar cada detalle
            for (int i = 0; i < dets.length(); i++) {
                JSONObject di = dets.getJSONObject(i);
                int idProd = di.getInt("idProducto");
                int cant = di.getInt("cantProd");
                double precio = di.getDouble("detallePrecio");
                double subtotal = di.getDouble("sbttTotal");

                Productos prod = prodCtrl.findProductos(idProd);
                if (prod == null || cant > prod.getStock()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Stock insuficiente o producto inválido (ID " + idProd + ")");
                    return;
                }

                // Crear Detalleventas
                Detalleventas detalle = new Detalleventas();
                detalle.setIdVenta(venta);
                detalle.setIdProducto(prod);
                detalle.setCantProd(cant);
                detalle.setDetallePrecio(precio);
                detalle.setSbttPrecio(subtotal);
                detCtrl.create(detalle);

                // Ajustar stock
                prod.setStock(prod.getStock() - cant);
                prodCtrl.edit(prod);
            }

            // Respuesta OK
            resp.setContentType("application/json");
            resp.getWriter().print(new JSONObject().put("estado", "ok").toString());

        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al registrar detalle: " + e.getMessage());
        }
    }
}
