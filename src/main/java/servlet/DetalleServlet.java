package servlet;

import dto.Detalleventas;
import dto.Productos;
import dto.Ventas;
import java.io.BufferedReader;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(name = "DetalleServlet", urlPatterns = {"/detalle"})
public class DetalleServlet extends HttpServlet {

    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");
    }

    // GET /detalle (sin cambios)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            JSONArray arr = new JSONArray();
            em.createQuery("SELECT d FROM Detalleventas d", Detalleventas.class)
                    .getResultList()
                    .forEach(d -> {
                        JSONObject o = new JSONObject();
                        o.put("idVenta", d.getIdVenta().getIdVenta());
                        o.put("idProducto", d.getIdProducto().getIdProducto());
                        o.put("producto", d.getIdProducto().getNombre());
                        o.put("cantidad", d.getCantProd());
                        o.put("precio", d.getDetallePrecio());
                        o.put("subtotal", d.getSbttPrecio());
                        arr.put(o);
                    });
            resp.setContentType("application/json");
            resp.getWriter().print(arr.toString());
            em.close();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al listar detalles" + e.getMessage());
        } finally {
            if (em != null) {
                em.close();
            }
        }

    }

    // POST /detalle (versión simplificada con EM directo)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Leemos JSON
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        
        JSONObject json = new JSONObject(sb.toString());
        int idVenta = json.getInt("idVenta");
        JSONArray dets = json.getJSONArray("detalles");

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Buscamos la venta y validamos
            Ventas venta = em.find(Ventas.class, idVenta);
            if (venta == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Venta no encontrada");
                return;
            }

            // Para cada detalle
            for (int i = 0; i < dets.length(); i++) {
                JSONObject di = dets.getJSONObject(i);
                int idProd = di.getInt("idProducto");
                int cant = di.getInt("cantProd");
                double precio = di.getDouble("detallePrecio");
                double subtotal = di.getDouble("sbttTotal");

                Productos prod = em.find(Productos.class, idProd);
                if (prod == null || cant > prod.getStock()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Stock insuficiente o producto inválido (ID " + idProd + ")");
                    return;
                }

                // Persistimos el detalle
                Detalleventas det = new Detalleventas();
                det.setIdVenta(venta);
                det.setIdProducto(prod);
                det.setCantProd(cant);
                det.setDetallePrecio(precio);
                det.setSbttPrecio(subtotal);
                em.persist(det);

                // Ajustamos stock
                prod.setStock(prod.getStock() - cant);
                em.merge(prod);
            }

            em.getTransaction().commit();

            // Respuesta OK
            resp.setContentType("application/json");
            resp.getWriter().print(new JSONObject().put("estado", "ok").toString());
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error al registrar detalle: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}
