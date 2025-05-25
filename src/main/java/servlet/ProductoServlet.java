package servlet;

import dao.ProductosJpaController;
import dao.exceptions.IllegalOrphanException;
import dao.exceptions.NonexistentEntityException;
import dto.Productos;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(urlPatterns = {"/producto"})
public class ProductoServlet extends HttpServlet {

    private ProductosJpaController productosDAO;

    @Override
    public void init() throws ServletException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");
        productosDAO = new ProductosJpaController(emf);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try {
            List<Productos> productos = productosDAO.findProductosEntities();
            JSONArray jsonArray = new JSONArray();
            for (Productos p : productos) {
                JSONObject obj = new JSONObject();
                obj.put("idProducto", p.getIdProducto());
                obj.put("nombre", p.getNombre());
                obj.put("precio", p.getPrecio());
                obj.put("stock", p.getStock());
                jsonArray.put(obj);
            }
            response.getWriter().write(jsonArray.toString());

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        JSONObject respuesta = new JSONObject();

        try (BufferedReader reader = request.getReader()) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject json = new JSONObject(jsonBuilder.toString());

            String nombre = json.getString("nombre");
            Double precio = json.getDouble("precio");
            int stock = json.getInt("stock");

            Productos nuevo = new Productos();
            nuevo.setNombre(nombre);
            nuevo.setPrecio(precio);
            nuevo.setStock(stock);

            productosDAO.create(nuevo);

            respuesta.put("success", true);
            respuesta.put("message", "Producto agregado correctamente");
        } catch (Exception e) {
            respuesta.put("success", false);
            respuesta.put("message", "Error: " + e.getMessage());
        }

        response.getWriter()
                .write(respuesta.toString());
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try (BufferedReader reader = request.getReader()) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject json = new JSONObject(jsonBuilder.toString());

            Integer id = json.getInt("idProducto");
            JSONObject obj = new JSONObject();
            try {
                productosDAO.destroy(id);

                obj.put("success", true);
                obj.put("message", "Eliminacion correcta");

            } catch (NonexistentEntityException e) {

                obj.put("success", false);
                obj.put("message", e.getMessage());

            } catch (IllegalOrphanException ex) {
                Logger.getLogger(ProductoServlet.class.getName()).log(Level.SEVERE, null, ex);
                obj.put("success", false);
                obj.put("message", ex.getMessage());
            }

            response.getWriter().write(obj.toString());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        JSONObject respuesta = new JSONObject();

        try (BufferedReader reader = request.getReader()) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject json = new JSONObject(jsonBuilder.toString());

            int id = json.getInt("idProducto");
            String nombre = json.getString("nombre");
            Double precio = json.getDouble("precio");
            int password = json.getInt("stock");
            Productos existente = productosDAO.findProductos(id);

            if (existente != null) {
                existente.setNombre(nombre);
                existente.setPrecio(precio);
                existente.setStock(password);

                productosDAO.edit(existente);

                respuesta.put("success", true);
                respuesta.put("message", "Producto actualizado");
            } else {
                respuesta.put("success", false);
                respuesta.put("message", "Producto no encontrado");
            }

            response.getWriter().write(respuesta.toString());

        } catch (Exception ex) {
            Logger.getLogger(UsuarioServlet.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
}
