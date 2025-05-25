package servlet;

import dao.VentasJpaController;
import dao.ClientesJpaController;
import dto.Clientes;
import dto.Ventas;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
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

    @Override
    public void init() {
        this.ventaDAO = new VentasJpaController(emf);
        this.clienteDAO = new ClientesJpaController(emf);
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

        Timestamp fechaVenta;

        JSONObject data = new JSONObject(new JSONTokener(request.getReader()));
        Ventas venta = new Ventas();

        // cliente
        Clientes cliente = clienteDAO.findClientes(data.getInt("idCliente"));
        if (cliente == null) {
            response.sendError(400, "Cliente no encontrado");
            return;
        }
        venta.setIdCliente(cliente);

        // fecha
        try {
            if (data.has("fecha") && !data.isNull("fecha") && !data.getString("fecha").isBlank()) {
                String fechaStr = data.getString("fecha");
                fechaVenta = Timestamp.valueOf(fechaStr + " 00:00:00");
            } else {
                fechaVenta = new Timestamp(System.currentTimeMillis());
            }
        } catch (IllegalArgumentException ex) {
            response.sendError(400, "Formato de fecha inválido. Usa yyyy-MM-dd");
            return;
        }

        venta.setFechaVenta(fechaVenta);

        try {
            ventaDAO.create(venta);
            JSONObject res = new JSONObject();
            res.put("idVenta", venta.getIdVenta());
            response.getWriter().print(res.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            response.sendError(500, "Error al guardar venta: " + e.getMessage());
        }
    }
}