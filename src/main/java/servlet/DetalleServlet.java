package servlet;

import java.io.IOException;
import javax.persistence.EntityManagerFactory;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.DetalleventasJpaController;
import dao.VentasJpaController;
import dao.ProductosJpaController;
import dto.Detalleventas;
import dto.Productos;
import dto.Ventas;
import java.util.List;
import javax.persistence.Persistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

@WebServlet(name = "DetalleServlet", urlPatterns = {"/detalle"})
public class DetalleServlet extends HttpServlet {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");
    DetalleventasJpaController detalleDAO;
    VentasJpaController ventasDAO;
    ProductosJpaController productosDAO;

    @Override
    public void init() {
        detalleDAO = new DetalleventasJpaController(emf);
        ventasDAO = new VentasJpaController(emf);
        productosDAO = new ProductosJpaController(emf);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JSONArray data = new JSONArray(new JSONTokener(request.getReader()));

        try {
            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);

                Detalleventas detalle = new Detalleventas();

                Ventas venta = ventasDAO.findVentas(obj.getInt("idVenta"));
                Productos producto = productosDAO.findProductos(obj.getInt("idProducto"));

                if (venta == null || producto == null) {
                    response.sendError(400, "Venta o Producto no encontrado");
                    return;
                }

                detalle.setIdVenta(venta);
                detalle.setIdProducto(producto);
                detalle.setCantProd(obj.getInt("cantProd"));
                detalle.setDetallePrecio((float) obj.getDouble("detallePrecio"));
                detalle.setSbttPrecio((float) obj.getDouble("sbttPrecio"));

                detalleDAO.create(detalle);
            }

            response.setStatus(201); // Created
        } catch ( IOException | JSONException e) {
            e.printStackTrace();
            response.sendError(500, "Error al guardar detalles: " + e.getMessage());
        }
    }
    
    @Override
protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    List<Detalleventas> detalles = detalleDAO.findDetalleventasEntities();
    JSONArray array = new JSONArray();

    for (Detalleventas d : detalles) {
        JSONObject obj = new JSONObject();
        obj.put("idDetalleVenta", d.getIdDetalleVenta());
        obj.put("idVenta", d.getIdVenta().getIdVenta());
        obj.put("idProducto", d.getIdProducto().getIdProducto());
        obj.put("cantProd", d.getCantProd());
        obj.put("detallePrecio", d.getDetallePrecio());
        obj.put("sbttPrecio", d.getSbttPrecio());
        array.put(obj);
    }

    response.getWriter().write(array.toString());
}

}
