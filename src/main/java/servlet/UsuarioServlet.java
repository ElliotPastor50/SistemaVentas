package servlet;

import dto.Usuarios;
import dao.UsuariosJpaController;
import dao.exceptions.NonexistentEntityException;
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

@WebServlet(urlPatterns = {"/usuario"})
public class UsuarioServlet extends HttpServlet {

    private UsuariosJpaController usuariosController;

    @Override
    public void init() throws ServletException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");
        usuariosController = new UsuariosJpaController(emf);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            List<Usuarios> usuarios = usuariosController.findUsuariosEntities();
            JSONArray jsonArray = new JSONArray();
            for (Usuarios u : usuarios) {
                JSONObject obj = new JSONObject();
                obj.put("idUsuario", u.getIdUsuario());
                obj.put("nombre", u.getNombre());
                obj.put("email", u.getEmail());
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
            String action = json.getString("action");

            if ("validar".equals(action)) {
                String email = json.getString("email");
                String password = json.getString("password");

                Usuarios usuario = usuariosController.validarUsuario(email, password);

                if (usuario != null) {
                    respuesta.put("success", true);
                    respuesta.put("idUsuario", usuario.getIdUsuario());
                    respuesta.put("nombre", usuario.getNombre());
                    respuesta.put("email", usuario.getEmail());
                } else {
                    respuesta.put("success", false);
                    respuesta.put("message", "Credenciales incorrectas");
                }

            } else if ("crear".equals(action)) {
                String nombre = json.getString("nombre");
                String email = json.getString("email");
                String password = json.getString("password");

                Usuarios nuevo = new Usuarios();
                nuevo.setNombre(nombre);
                nuevo.setEmail(email);
                nuevo.setPassword(password);

                usuariosController.create(nuevo);

                respuesta.put("success", true);
                respuesta.put("message", "Usuario creado correctamente");

            } else {
                respuesta.put("success", false);
                respuesta.put("message", "Acción no válida");
            }

        } catch (Exception e) {
            respuesta.put("success", false);
            respuesta.put("message", "Error: " + e.getMessage());
        }

        response.getWriter().write(respuesta.toString());
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

            Integer id = json.getInt("idUsuario");
            JSONObject obj = new JSONObject();
            try {
                usuariosController.destroy(id);

                obj.put("success", true);
                obj.put("message", "Eliminacion correcta");
                
            } catch (NonexistentEntityException e) {

                obj.put("success", false);
                obj.put("message", e.getMessage());
                
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

            int id = json.getInt("IdUsuario");
            String nombre = json.getString("nombre");
            String email = json.getString("email");
            String password = json.getString("password");
            Usuarios existente = usuariosController.findUsuarios(id);

            if (existente != null) {
                existente.setNombre(nombre);
                existente.setEmail(email);
                existente.setPassword(password);
                usuariosController.edit(existente);

                respuesta.put("success", true);
                respuesta.put("message", "Usuario actualizado");
            } else {
                respuesta.put("success", false);
                respuesta.put("message", "Usuario no encontrado");
            }
            
            response.getWriter().write(respuesta.toString());
        } catch (Exception ex) {
            Logger.getLogger(UsuarioServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
