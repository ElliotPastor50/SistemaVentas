package dao;

import dao.exceptions.IllegalOrphanException;
import dao.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Detalleventas;
import dto.Clientes;
import dto.Ventas;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


public class VentasJpaController implements Serializable {

    public VentasJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Ventas ventas) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Detalleventas detalleventas = ventas.getDetalleventas();
            if (detalleventas != null) {
                detalleventas = em.getReference(detalleventas.getClass(), detalleventas.getIdDetalleVenta());
                ventas.setDetalleventas(detalleventas);
            }
            Clientes idCliente = ventas.getIdCliente();
            if (idCliente != null) {
                idCliente = em.getReference(idCliente.getClass(), idCliente.getIdCliente());
                ventas.setIdCliente(idCliente);
            }
            em.persist(ventas);
            if (detalleventas != null) {
                Ventas oldIdVentaOfDetalleventas = detalleventas.getIdVenta();
                if (oldIdVentaOfDetalleventas != null) {
                    oldIdVentaOfDetalleventas.setDetalleventas(null);
                    oldIdVentaOfDetalleventas = em.merge(oldIdVentaOfDetalleventas);
                }
                detalleventas.setIdVenta(ventas);
                detalleventas = em.merge(detalleventas);
            }
            if (idCliente != null) {
                idCliente.getVentasCollection().add(ventas);
                idCliente = em.merge(idCliente);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Ventas ventas) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Ventas persistentVentas = em.find(Ventas.class, ventas.getIdVenta());
            Detalleventas detalleventasOld = persistentVentas.getDetalleventas();
            Detalleventas detalleventasNew = ventas.getDetalleventas();
            Clientes idClienteOld = persistentVentas.getIdCliente();
            Clientes idClienteNew = ventas.getIdCliente();
            List<String> illegalOrphanMessages = null;
            if (detalleventasOld != null && !detalleventasOld.equals(detalleventasNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Detalleventas " + detalleventasOld + " since its idVenta field is not nullable.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (detalleventasNew != null) {
                detalleventasNew = em.getReference(detalleventasNew.getClass(), detalleventasNew.getIdDetalleVenta());
                ventas.setDetalleventas(detalleventasNew);
            }
            if (idClienteNew != null) {
                idClienteNew = em.getReference(idClienteNew.getClass(), idClienteNew.getIdCliente());
                ventas.setIdCliente(idClienteNew);
            }
            ventas = em.merge(ventas);
            if (detalleventasNew != null && !detalleventasNew.equals(detalleventasOld)) {
                Ventas oldIdVentaOfDetalleventas = detalleventasNew.getIdVenta();
                if (oldIdVentaOfDetalleventas != null) {
                    oldIdVentaOfDetalleventas.setDetalleventas(null);
                    oldIdVentaOfDetalleventas = em.merge(oldIdVentaOfDetalleventas);
                }
                detalleventasNew.setIdVenta(ventas);
                detalleventasNew = em.merge(detalleventasNew);
            }
            if (idClienteOld != null && !idClienteOld.equals(idClienteNew)) {
                idClienteOld.getVentasCollection().remove(ventas);
                idClienteOld = em.merge(idClienteOld);
            }
            if (idClienteNew != null && !idClienteNew.equals(idClienteOld)) {
                idClienteNew.getVentasCollection().add(ventas);
                idClienteNew = em.merge(idClienteNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = ventas.getIdVenta();
                if (findVentas(id) == null) {
                    throw new NonexistentEntityException("The ventas with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Ventas ventas;
            try {
                ventas = em.getReference(Ventas.class, id);
                ventas.getIdVenta();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The ventas with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Detalleventas detalleventasOrphanCheck = ventas.getDetalleventas();
            if (detalleventasOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Ventas (" + ventas + ") cannot be destroyed since the Detalleventas " + detalleventasOrphanCheck + " in its detalleventas field has a non-nullable idVenta field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Clientes idCliente = ventas.getIdCliente();
            if (idCliente != null) {
                idCliente.getVentasCollection().remove(ventas);
                idCliente = em.merge(idCliente);
            }
            em.remove(ventas);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Ventas> findVentasEntities() {
        return findVentasEntities(true, -1, -1);
    }

    public List<Ventas> findVentasEntities(int maxResults, int firstResult) {
        return findVentasEntities(false, maxResults, firstResult);
    }

    private List<Ventas> findVentasEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Ventas.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Ventas findVentas(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Ventas.class, id);
        } finally {
            em.close();
        }
    }

    public int getVentasCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Ventas> rt = cq.from(Ventas.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
