package servlet;

import dto.Clientes;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import dao.ClientesJpaController;

@WebServlet("/cliente")
public class ClienteServlet extends HttpServlet {

    private ClientesJpaController clienteController;

    @Override
    public void init() throws ServletException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");
        clienteController = new ClientesJpaController(emf);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        List<Clientes> lista = clienteController.findClientesEntities();
        JSONArray json = new JSONArray();
        for (Clientes c : lista) {
            JSONObject obj = new JSONObject();
            obj.put("idCliente", c.getIdCliente());
            obj.put("nombre", c.getNombre());
            obj.put("apellido", c.getApellido());
            obj.put("dni", c.getDni());
            json.put(obj);
        }
        response.getWriter().print(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        JSONObject json = new JSONObject(request.getReader().lines().reduce("", (a, b) -> a + b));
        Clientes c = new Clientes();
        c.setNombre(json.getString("nombre"));
        c.setApellido(json.getString("apellido"));
        c.setDni(json.optString("dni", ""));
        try {
            clienteController.create(c);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al crear cliente.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        JSONObject json = new JSONObject(request.getReader().lines().reduce("", (a, b) -> a + b));
        try {
            Clientes c = clienteController.findClientes(json.getInt("idCliente"));
            c.setNombre(json.getString("nombre"));
            c.setApellido(json.getString("apellido"));
            c.setDni(json.optString("dni", ""));
            clienteController.edit(c);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al actualizar cliente.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        int id = Integer.parseInt(request.getParameter("idCliente"));
        try {
            clienteController.destroy(id);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al eliminar cliente.");
        }
    }
}
