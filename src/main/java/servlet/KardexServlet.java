package servlet;

import dto.Kardex;
import dto.Productos;
import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import dao.KardexJpaController;
import dao.ProductosJpaController;
import dao.exceptions.NonexistentEntityException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/kardex")
public class KardexServlet extends HttpServlet {

    private EntityManagerFactory emf;
    private KardexJpaController kardexController;
    private ProductosJpaController productoController;

    @Override
    public void init() {
        emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");
        kardexController = new KardexJpaController(emf);
        productoController = new ProductosJpaController(emf);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        JSONArray array = new JSONArray();
        List<Kardex> lista = kardexController.findKardexEntities();

        for (Kardex k : lista) {
            JSONObject obj = new JSONObject();
            obj.put("idKardex", k.getIdKardex());
            obj.put("cantProd", k.getCantProd());
            obj.put("moviKard", k.getMoviKard() == 1 ? "Entrada" : "Salida");
            obj.put("stock", k.getIdProducto().getStock()); // stock en el momento del registro
            obj.put("nombreProducto", k.getIdProducto().getNombre());
            array.put(obj);
        }

        out.print(array.toString());
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Leer el cuerpo del request como texto
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // Convertir a objeto JSON
            JSONObject data = new JSONObject(sb.toString());

            int moviKard = data.getInt("moviKard");
            int cantProd = data.getInt("cantProd");
            int idProducto = data.getInt("idProducto");

            Productos producto = productoController.findProductos(idProducto);
            if (producto == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Producto no encontrado");
                return;
            }

            // Validar stock
            if (moviKard == 0 && cantProd > producto.getStock()) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Stock insuficiente");
                return;
            }

            // Actualizar stock
            int nuevoStock = moviKard == 1
                    ? producto.getStock() + cantProd
                    : producto.getStock() - cantProd;

            producto.setStock(nuevoStock);
            productoController.edit(producto);

            Kardex k = new Kardex();
            k.setMoviKard(moviKard);
            k.setCantProd(cantProd);
            k.setIdProducto(producto);
            kardexController.create(k);

            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new JSONObject().put("status", "ok").toString());
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(KardexServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            System.out.println("Error en pasar el IdProducto Wey");
            Logger.getLogger(KardexServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            JSONObject data = new JSONObject(request.getReader().readLine());
            int idKardex = data.getInt("idKardex");
            int moviKard = data.getInt("moviKard");
            int cantProd = data.getInt("cantProd");
            int idProducto = data.getInt("idProducto");

            Kardex k = kardexController.findKardex(idKardex);
            Productos producto = productoController.findProductos(idProducto);

            if (k == null || producto == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Reversión stock anterior
            int stockActual = producto.getStock();
            int ajuste = (k.getMoviKard() == 1 ? -k.getCantProd() : k.getCantProd());
            stockActual += ajuste;

            // Aplicar nuevo ajuste
            int nuevoAjuste = (moviKard == 1 ? cantProd : -cantProd);
            int nuevoStock = stockActual + nuevoAjuste;

            if (nuevoStock < 0) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Stock insuficiente para actualizar");
                return;
            }

            producto.setStock(nuevoStock);
            productoController.edit(producto);

            k.setMoviKard(moviKard);
            k.setCantProd(cantProd);
            k.setIdProducto(producto);
            kardexController.edit(k);

            response.getWriter().write(new JSONObject().put("status", "updated").toString());
        } catch (Exception ex) {
            Logger.getLogger(KardexServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("idKardex"));
        Kardex k = kardexController.findKardex(id);

        if (k != null) {
            try {
                Productos producto = k.getIdProducto();
                int ajuste = (k.getMoviKard() == 1 ? -k.getCantProd() : k.getCantProd());
                producto.setStock(producto.getStock() + ajuste);
                productoController.edit(producto);
                kardexController.destroy(id);
            } catch (NonexistentEntityException ex) {
                Logger.getLogger(KardexServlet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(KardexServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        response.getWriter().write(new JSONObject().put("status", "deleted").toString());
    }
}
