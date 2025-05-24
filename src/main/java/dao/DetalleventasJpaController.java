package dao;

import dao.exceptions.IllegalOrphanException;
import dao.exceptions.NonexistentEntityException;
import dto.Detalleventas;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Productos;
import dto.Ventas;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DetalleventasJpaController implements Serializable {

    public DetalleventasJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Detalleventas detalleventas) throws IllegalOrphanException {
        List<String> illegalOrphanMessages = null;
        Productos idProductoOrphanCheck = detalleventas.getIdProducto();
        if (idProductoOrphanCheck != null) {
            Detalleventas oldDetalleventasOfIdProducto = idProductoOrphanCheck.getDetalleventas();
            if (oldDetalleventasOfIdProducto != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("The Productos " + idProductoOrphanCheck + " already has an item of type Detalleventas whose idProducto column cannot be null. Please make another selection for the idProducto field.");
            }
        }
        Ventas idVentaOrphanCheck = detalleventas.getIdVenta();
        if (idVentaOrphanCheck != null) {
            Detalleventas oldDetalleventasOfIdVenta = idVentaOrphanCheck.getDetalleventas();
            if (oldDetalleventasOfIdVenta != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("The Ventas " + idVentaOrphanCheck + " already has an item of type Detalleventas whose idVenta column cannot be null. Please make another selection for the idVenta field.");
            }
        }
        if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Productos idProducto = detalleventas.getIdProducto();
            if (idProducto != null) {
                idProducto = em.getReference(idProducto.getClass(), idProducto.getIdProducto());
                detalleventas.setIdProducto(idProducto);
            }
            Ventas idVenta = detalleventas.getIdVenta();
            if (idVenta != null) {
                idVenta = em.getReference(idVenta.getClass(), idVenta.getIdVenta());
                detalleventas.setIdVenta(idVenta);
            }
            em.persist(detalleventas);
            if (idProducto != null) {
                idProducto.setDetalleventas(detalleventas);
                idProducto = em.merge(idProducto);
            }
            if (idVenta != null) {
                idVenta.setDetalleventas(detalleventas);
                idVenta = em.merge(idVenta);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Detalleventas detalleventas) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Detalleventas persistentDetalleventas = em.find(Detalleventas.class, detalleventas.getIdDetalleVenta());
            Productos idProductoOld = persistentDetalleventas.getIdProducto();
            Productos idProductoNew = detalleventas.getIdProducto();
            Ventas idVentaOld = persistentDetalleventas.getIdVenta();
            Ventas idVentaNew = detalleventas.getIdVenta();
            List<String> illegalOrphanMessages = null;
            if (idProductoNew != null && !idProductoNew.equals(idProductoOld)) {
                Detalleventas oldDetalleventasOfIdProducto = idProductoNew.getDetalleventas();
                if (oldDetalleventasOfIdProducto != null) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("The Productos " + idProductoNew + " already has an item of type Detalleventas whose idProducto column cannot be null. Please make another selection for the idProducto field.");
                }
            }
            if (idVentaNew != null && !idVentaNew.equals(idVentaOld)) {
                Detalleventas oldDetalleventasOfIdVenta = idVentaNew.getDetalleventas();
                if (oldDetalleventasOfIdVenta != null) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("The Ventas " + idVentaNew + " already has an item of type Detalleventas whose idVenta column cannot be null. Please make another selection for the idVenta field.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (idProductoNew != null) {
                idProductoNew = em.getReference(idProductoNew.getClass(), idProductoNew.getIdProducto());
                detalleventas.setIdProducto(idProductoNew);
            }
            if (idVentaNew != null) {
                idVentaNew = em.getReference(idVentaNew.getClass(), idVentaNew.getIdVenta());
                detalleventas.setIdVenta(idVentaNew);
            }
            detalleventas = em.merge(detalleventas);
            if (idProductoOld != null && !idProductoOld.equals(idProductoNew)) {
                idProductoOld.setDetalleventas(null);
                idProductoOld = em.merge(idProductoOld);
            }
            if (idProductoNew != null && !idProductoNew.equals(idProductoOld)) {
                idProductoNew.setDetalleventas(detalleventas);
                idProductoNew = em.merge(idProductoNew);
            }
            if (idVentaOld != null && !idVentaOld.equals(idVentaNew)) {
                idVentaOld.setDetalleventas(null);
                idVentaOld = em.merge(idVentaOld);
            }
            if (idVentaNew != null && !idVentaNew.equals(idVentaOld)) {
                idVentaNew.setDetalleventas(detalleventas);
                idVentaNew = em.merge(idVentaNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = detalleventas.getIdDetalleVenta();
                if (findDetalleventas(id) == null) {
                    throw new NonexistentEntityException("The detalleventas with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Detalleventas detalleventas;
            try {
                detalleventas = em.getReference(Detalleventas.class, id);
                detalleventas.getIdDetalleVenta();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The detalleventas with id " + id + " no longer exists.", enfe);
            }
            Productos idProducto = detalleventas.getIdProducto();
            if (idProducto != null) {
                idProducto.setDetalleventas(null);
                idProducto = em.merge(idProducto);
            }
            Ventas idVenta = detalleventas.getIdVenta();
            if (idVenta != null) {
                idVenta.setDetalleventas(null);
                idVenta = em.merge(idVenta);
            }
            em.remove(detalleventas);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Detalleventas> findDetalleventasEntities() {
        return findDetalleventasEntities(true, -1, -1);
    }

    public List<Detalleventas> findDetalleventasEntities(int maxResults, int firstResult) {
        return findDetalleventasEntities(false, maxResults, firstResult);
    }

    private List<Detalleventas> findDetalleventasEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Detalleventas.class));
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

    public Detalleventas findDetalleventas(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Detalleventas.class, id);
        } finally {
            em.close();
        }
    }

    public int getDetalleventasCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Detalleventas> rt = cq.from(Detalleventas.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
