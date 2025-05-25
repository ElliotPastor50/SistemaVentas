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

    public void create(Detalleventas detalleventas) {
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
                idProducto.getDetalleventasList().add(detalleventas);
                em.merge(idProducto);
            }

            if (idVenta != null) {
                idVenta.getDetalleventasList().add(detalleventas);
                em.merge(idVenta);
            }

            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Detalleventas detalleventas) throws NonexistentEntityException, Exception {
    EntityManager em = null;
    try {
        em = getEntityManager();
        em.getTransaction().begin();

        Detalleventas persistentDetalleventas = em.find(Detalleventas.class, detalleventas.getIdDetalleVenta());

        Productos idProductoOld = persistentDetalleventas.getIdProducto();
        Productos idProductoNew = detalleventas.getIdProducto();

        Ventas idVentaOld = persistentDetalleventas.getIdVenta();
        Ventas idVentaNew = detalleventas.getIdVenta();

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
            idProductoOld.getDetalleventasList().remove(persistentDetalleventas);
            em.merge(idProductoOld);
        }

        if (idProductoNew != null && !idProductoNew.equals(idProductoOld)) {
            idProductoNew.getDetalleventasList().add(detalleventas);
            em.merge(idProductoNew);
        }

        if (idVentaOld != null && !idVentaOld.equals(idVentaNew)) {
            idVentaOld.getDetalleventasList().remove(persistentDetalleventas);
            em.merge(idVentaOld);
        }

        if (idVentaNew != null && !idVentaNew.equals(idVentaOld)) {
            idVentaNew.getDetalleventasList().add(detalleventas);
            em.merge(idVentaNew);
        }

        em.getTransaction().commit();
    } catch (Exception ex) {
        String msg = ex.getLocalizedMessage();
        if (msg == null || msg.length() == 0) {
            Integer id = detalleventas.getIdDetalleVenta();
            if (findDetalleventas(id) == null) {
                throw new NonexistentEntityException("El detalleventas con ID " + id + " ya no existe.");
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
            throw new NonexistentEntityException("El detalleventas con ID " + id + " ya no existe.", enfe);
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
