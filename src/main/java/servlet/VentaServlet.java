package servlet;

import dao.ClientesJpaController;
import dao.VentasJpaController;
import dto.Clientes;
import dto.Ventas;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@WebServlet(name = "VentaServlet", urlPatterns = {"/venta"})
public class VentaServlet extends HttpServlet {

    private EntityManagerFactory emf;
    private VentasJpaController ventaCtrl;
    private ClientesJpaController clienteCtrl;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");
        ventaCtrl    = new VentasJpaController(emf);
        clienteCtrl = new ClientesJpaController(emf);
    }

    // GET /venta
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        try {
            List<Ventas> ventas = ventaCtrl.findVentasEntities();
            JSONArray arr = new JSONArray();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Ventas v : ventas) {
                JSONObject o = new JSONObject();
                o.put("idVenta", v.getIdVenta());
                o.put("fecha", sdf.format(v.getFechaVenta()));
                Clientes c = v.getIdCliente();
                o.put("cliente", c.getNombre() + " " + c.getApellido());
                arr.put(o);
            }
            resp.setContentType("application/json");
            resp.getWriter().print(arr.toString());
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al listar ventas");
        }
    }

    // POST /venta
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        try {
            // Leer todo el JSON de la petición
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject json = new JSONObject(sb.toString());

            // Intentar leer idCliente, si no existe usar nombre
            Clientes cliente = null;
            if (json.has("idCliente")) {
                int idCli = json.getInt("idCliente");
                cliente = clienteCtrl.findClientes(idCli);
            } else if (json.has("cliente")) {
                String nom = json.getString("cliente").trim();
                cliente = clienteCtrl.findByNombre(nom);
            }
            if (cliente == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cliente no válido");
                return;
            }

            // Leer fecha
            String fechaStr = json.getString("fecha");
            Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(fechaStr);

            // Crear venta
            Ventas venta = new Ventas();
            venta.setIdCliente(cliente);
            venta.setFechaVenta(fecha);
            ventaCtrl.create(venta);

            // Responder { idVenta }
            JSONObject res = new JSONObject();
            res.put("idVenta", venta.getIdVenta());
            resp.setContentType("application/json");
            resp.getWriter().print(res.toString());

        } catch (JSONException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "JSON inválido: " + e.getMessage());
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno: " + e.getMessage());
        }
    }
}
