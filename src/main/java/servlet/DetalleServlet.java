package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
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
import org.json.JSONObject;
import org.json.JSONTokener;
@WebServlet(name = "DetalleServlet", urlPatterns = {"/detalle"})
public class DetalleServlet extends HttpServlet {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");
    DetalleventasJpaController detalleDAO;
    VentasJpaController ventaDAO;
    ProductosJpaController productoDAO;

    @Override
    public void init() {
        detalleDAO = new DetalleventasJpaController(emf);
        ventaDAO = new VentasJpaController(emf);
        productoDAO = new ProductosJpaController(emf);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        JSONArray detalles = new JSONArray(new JSONTokener(request.getReader()));
        System.out.println("Tratano de Postear el detalle, se llego aqui uwur");
        try {
            for (int i = 0; i < detalles.length(); i++) {
                JSONObject item = detalles.getJSONObject(i);
                Detalleventas dv = new Detalleventas();

                Ventas venta = ventaDAO.findVentas(item.getInt("idVenta"));
                Productos prod = productoDAO.findProductos(item.getInt("idProducto"));

                dv.setIdVenta(venta);
                dv.setIdProducto(prod);
                dv.setCantProd(item.getInt("cantProd"));
                dv.setDetallePrecio(item.getDouble("detallePrecio"));
                dv.setSbttPrecio(item.getDouble("sbttTotal"));

                detalleDAO.create(dv);
            }
            response.setStatus(201);
        } catch (Exception e) {
            response.sendError(500, e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Detalleventas> detalles = detalleDAO.findDetalleventasEntities();
        JSONArray jsonArray = new JSONArray();

        for (Detalleventas d : detalles) {
            JSONObject json = new JSONObject();
            json.put("idDetalle", d.getIdDetalleVenta());
            json.put("idVenta", d.getIdVenta().getIdVenta()); // Asegúrate que getIdVenta() devuelve un objeto Venta
            json.put("idProducto", d.getIdProducto().getIdProducto()); // También es un objeto
            json.put("detallePrecio", d.getDetallePrecio());
            json.put("cantProd", d.getCantProd());
            json.put("sbttTotal", d.getSbttPrecio());

            jsonArray.put(json);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonArray.toString());
    }

}
