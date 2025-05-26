/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dao.exceptions.IllegalOrphanException;
import dao.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Detalleventas;
import java.util.ArrayList;
import java.util.Collection;
import dto.Kardex;
import dto.Productos;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Naomi Alejandra Vega
 */
public class ProductosJpaController implements Serializable {

    public ProductosJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Productos productos) {
        if (productos.getDetalleventasCollection() == null) {
            productos.setDetalleventasCollection(new ArrayList<Detalleventas>());
        }
        if (productos.getKardexCollection() == null) {
            productos.setKardexCollection(new ArrayList<Kardex>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Detalleventas> attachedDetalleventasCollection = new ArrayList<Detalleventas>();
            for (Detalleventas detalleventasCollectionDetalleventasToAttach : productos.getDetalleventasCollection()) {
                detalleventasCollectionDetalleventasToAttach = em.getReference(detalleventasCollectionDetalleventasToAttach.getClass(), detalleventasCollectionDetalleventasToAttach.getIdDetalleVenta());
                attachedDetalleventasCollection.add(detalleventasCollectionDetalleventasToAttach);
            }
            productos.setDetalleventasCollection(attachedDetalleventasCollection);
            Collection<Kardex> attachedKardexCollection = new ArrayList<Kardex>();
            for (Kardex kardexCollectionKardexToAttach : productos.getKardexCollection()) {
                kardexCollectionKardexToAttach = em.getReference(kardexCollectionKardexToAttach.getClass(), kardexCollectionKardexToAttach.getIdKardex());
                attachedKardexCollection.add(kardexCollectionKardexToAttach);
            }
            productos.setKardexCollection(attachedKardexCollection);
            em.persist(productos);
            for (Detalleventas detalleventasCollectionDetalleventas : productos.getDetalleventasCollection()) {
                Productos oldIdProductoOfDetalleventasCollectionDetalleventas = detalleventasCollectionDetalleventas.getIdProducto();
                detalleventasCollectionDetalleventas.setIdProducto(productos);
                detalleventasCollectionDetalleventas = em.merge(detalleventasCollectionDetalleventas);
                if (oldIdProductoOfDetalleventasCollectionDetalleventas != null) {
                    oldIdProductoOfDetalleventasCollectionDetalleventas.getDetalleventasCollection().remove(detalleventasCollectionDetalleventas);
                    oldIdProductoOfDetalleventasCollectionDetalleventas = em.merge(oldIdProductoOfDetalleventasCollectionDetalleventas);
                }
            }
            for (Kardex kardexCollectionKardex : productos.getKardexCollection()) {
                Productos oldIdProductoOfKardexCollectionKardex = kardexCollectionKardex.getIdProducto();
                kardexCollectionKardex.setIdProducto(productos);
                kardexCollectionKardex = em.merge(kardexCollectionKardex);
                if (oldIdProductoOfKardexCollectionKardex != null) {
                    oldIdProductoOfKardexCollectionKardex.getKardexCollection().remove(kardexCollectionKardex);
                    oldIdProductoOfKardexCollectionKardex = em.merge(oldIdProductoOfKardexCollectionKardex);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Productos productos) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Productos persistentProductos = em.find(Productos.class, productos.getIdProducto());
            Collection<Detalleventas> detalleventasCollectionOld = persistentProductos.getDetalleventasCollection();
            Collection<Detalleventas> detalleventasCollectionNew = productos.getDetalleventasCollection();
            Collection<Kardex> kardexCollectionOld = persistentProductos.getKardexCollection();
            Collection<Kardex> kardexCollectionNew = productos.getKardexCollection();
            List<String> illegalOrphanMessages = null;
            for (Detalleventas detalleventasCollectionOldDetalleventas : detalleventasCollectionOld) {
                if (!detalleventasCollectionNew.contains(detalleventasCollectionOldDetalleventas)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Detalleventas " + detalleventasCollectionOldDetalleventas + " since its idProducto field is not nullable.");
                }
            }
            for (Kardex kardexCollectionOldKardex : kardexCollectionOld) {
                if (!kardexCollectionNew.contains(kardexCollectionOldKardex)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Kardex " + kardexCollectionOldKardex + " since its idProducto field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Detalleventas> attachedDetalleventasCollectionNew = new ArrayList<Detalleventas>();
            for (Detalleventas detalleventasCollectionNewDetalleventasToAttach : detalleventasCollectionNew) {
                detalleventasCollectionNewDetalleventasToAttach = em.getReference(detalleventasCollectionNewDetalleventasToAttach.getClass(), detalleventasCollectionNewDetalleventasToAttach.getIdDetalleVenta());
                attachedDetalleventasCollectionNew.add(detalleventasCollectionNewDetalleventasToAttach);
            }
            detalleventasCollectionNew = attachedDetalleventasCollectionNew;
            productos.setDetalleventasCollection(detalleventasCollectionNew);
            Collection<Kardex> attachedKardexCollectionNew = new ArrayList<Kardex>();
            for (Kardex kardexCollectionNewKardexToAttach : kardexCollectionNew) {
                kardexCollectionNewKardexToAttach = em.getReference(kardexCollectionNewKardexToAttach.getClass(), kardexCollectionNewKardexToAttach.getIdKardex());
                attachedKardexCollectionNew.add(kardexCollectionNewKardexToAttach);
            }
            kardexCollectionNew = attachedKardexCollectionNew;
            productos.setKardexCollection(kardexCollectionNew);
            productos = em.merge(productos);
            for (Detalleventas detalleventasCollectionNewDetalleventas : detalleventasCollectionNew) {
                if (!detalleventasCollectionOld.contains(detalleventasCollectionNewDetalleventas)) {
                    Productos oldIdProductoOfDetalleventasCollectionNewDetalleventas = detalleventasCollectionNewDetalleventas.getIdProducto();
                    detalleventasCollectionNewDetalleventas.setIdProducto(productos);
                    detalleventasCollectionNewDetalleventas = em.merge(detalleventasCollectionNewDetalleventas);
                    if (oldIdProductoOfDetalleventasCollectionNewDetalleventas != null && !oldIdProductoOfDetalleventasCollectionNewDetalleventas.equals(productos)) {
                        oldIdProductoOfDetalleventasCollectionNewDetalleventas.getDetalleventasCollection().remove(detalleventasCollectionNewDetalleventas);
                        oldIdProductoOfDetalleventasCollectionNewDetalleventas = em.merge(oldIdProductoOfDetalleventasCollectionNewDetalleventas);
                    }
                }
            }
            for (Kardex kardexCollectionNewKardex : kardexCollectionNew) {
                if (!kardexCollectionOld.contains(kardexCollectionNewKardex)) {
                    Productos oldIdProductoOfKardexCollectionNewKardex = kardexCollectionNewKardex.getIdProducto();
                    kardexCollectionNewKardex.setIdProducto(productos);
                    kardexCollectionNewKardex = em.merge(kardexCollectionNewKardex);
                    if (oldIdProductoOfKardexCollectionNewKardex != null && !oldIdProductoOfKardexCollectionNewKardex.equals(productos)) {
                        oldIdProductoOfKardexCollectionNewKardex.getKardexCollection().remove(kardexCollectionNewKardex);
                        oldIdProductoOfKardexCollectionNewKardex = em.merge(oldIdProductoOfKardexCollectionNewKardex);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = productos.getIdProducto();
                if (findProductos(id) == null) {
                    throw new NonexistentEntityException("The productos with id " + id + " no longer exists.");
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
            Productos productos;
            try {
                productos = em.getReference(Productos.class, id);
                productos.getIdProducto();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The productos with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Detalleventas> detalleventasCollectionOrphanCheck = productos.getDetalleventasCollection();
            for (Detalleventas detalleventasCollectionOrphanCheckDetalleventas : detalleventasCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Productos (" + productos + ") cannot be destroyed since the Detalleventas " + detalleventasCollectionOrphanCheckDetalleventas + " in its detalleventasCollection field has a non-nullable idProducto field.");
            }
            Collection<Kardex> kardexCollectionOrphanCheck = productos.getKardexCollection();
            for (Kardex kardexCollectionOrphanCheckKardex : kardexCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Productos (" + productos + ") cannot be destroyed since the Kardex " + kardexCollectionOrphanCheckKardex + " in its kardexCollection field has a non-nullable idProducto field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(productos);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Productos> findProductosEntities() {
        return findProductosEntities(true, -1, -1);
    }

    public List<Productos> findProductosEntities(int maxResults, int firstResult) {
        return findProductosEntities(false, maxResults, firstResult);
    }

    private List<Productos> findProductosEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Productos.class));
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

    public Productos findProductos(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Productos.class, id);
        } finally {
            em.close();
        }
    }

    public int getProductosCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Productos> rt = cq.from(Productos.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
