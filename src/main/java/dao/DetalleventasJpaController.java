/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dao.exceptions.NonexistentEntityException;
import dto.Detalleventas;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Productos;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Naomi Alejandra Vega
 */
public class DetalleventasJpaController implements Serializable {

    public DetalleventasJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

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
            em.persist(detalleventas);
            if (idProducto != null) {
                idProducto.getDetalleventasCollection().add(detalleventas);
                idProducto = em.merge(idProducto);
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
            if (idProductoNew != null) {
                idProductoNew = em.getReference(idProductoNew.getClass(), idProductoNew.getIdProducto());
                detalleventas.setIdProducto(idProductoNew);
            }
            detalleventas = em.merge(detalleventas);
            if (idProductoOld != null && !idProductoOld.equals(idProductoNew)) {
                idProductoOld.getDetalleventasCollection().remove(detalleventas);
                idProductoOld = em.merge(idProductoOld);
            }
            if (idProductoNew != null && !idProductoNew.equals(idProductoOld)) {
                idProductoNew.getDetalleventasCollection().add(detalleventas);
                idProductoNew = em.merge(idProductoNew);
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
                idProducto.getDetalleventasCollection().remove(detalleventas);
                idProducto = em.merge(idProducto);
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
