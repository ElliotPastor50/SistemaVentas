package dao;

import dao.exceptions.IllegalOrphanException;
import dao.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Detalleventas;
import dto.Kardex;
import dto.Productos;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class ProductosJpaController implements Serializable {

    public ProductosJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");

    public ProductosJpaController() {
        this.emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Productos productos) {
        if (productos.getKardexCollection() == null) {
            productos.setKardexCollection(new ArrayList<>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Detalleventas> detalleventasList = productos.getDetalleventasList();
            if (detalleventasList != null) {
                List<Detalleventas> attachedDetalleventasList = new ArrayList<>();
                for (Detalleventas dv : detalleventasList) {
                    dv = em.getReference(dv.getClass(), dv.getIdDetalleVenta());
                    attachedDetalleventasList.add(dv);
                }
                productos.setDetalleventasList(attachedDetalleventasList);
            }

            Collection<Kardex> attachedKardexCollection = new ArrayList<>();
            for (Kardex kardexCollectionKardexToAttach : productos.getKardexCollection()) {
                kardexCollectionKardexToAttach = em.getReference(kardexCollectionKardexToAttach.getClass(), kardexCollectionKardexToAttach.getIdKardex());
                attachedKardexCollection.add(kardexCollectionKardexToAttach);
            }
            productos.setKardexCollection(attachedKardexCollection);
            em.persist(productos);
            if (detalleventasList != null) {
                for (Detalleventas dv : detalleventasList) {
                    Productos oldProducto = dv.getIdProducto();
                    dv.setIdProducto(productos);
                    dv = em.merge(dv);
                    if (oldProducto != null) {
                        oldProducto.getDetalleventasList().remove(dv);
                        em.merge(oldProducto);
                    }
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
            List<Detalleventas> detalleventasOld = persistentProductos.getDetalleventasList();
            List<Detalleventas> detalleventasNew = productos.getDetalleventasList();

            Collection<Kardex> kardexCollectionOld = persistentProductos.getKardexCollection();
            Collection<Kardex> kardexCollectionNew = productos.getKardexCollection();

            /*List<String> illegalOrphanMessages = null;

            for (Detalleventas oldDetalle : detalleventasOld) {
            if (!detalleventasNew.contains(oldDetalle)) {
            if (illegalOrphanMessages == null) {
            illegalOrphanMessages = new ArrayList<>();
            }
            illegalOrphanMessages.add("You must retain Detalleventas " + oldDetalle + " since its idProducto field is not nullable.");
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
            }*/
            List<Detalleventas> attachedDetalleventasNew = new ArrayList<>();
            for (Detalleventas newDetalle : detalleventasNew) {
                newDetalle = em.getReference(newDetalle.getClass(), newDetalle.getIdDetalleVenta());
                attachedDetalleventasNew.add(newDetalle);
            }
            productos.setDetalleventasList(attachedDetalleventasNew);

            Collection<Kardex> attachedKardexCollectionNew = new ArrayList<>();
            for (Kardex kardexCollectionNewKardexToAttach : kardexCollectionNew) {
                kardexCollectionNewKardexToAttach = em.getReference(kardexCollectionNewKardexToAttach.getClass(), kardexCollectionNewKardexToAttach.getIdKardex());
                attachedKardexCollectionNew.add(kardexCollectionNewKardexToAttach);
            }
            kardexCollectionNew = attachedKardexCollectionNew;
            productos.setKardexCollection(kardexCollectionNew);
            productos = em.merge(productos);

            for (Detalleventas newDetalle : detalleventasNew) {
                if (!detalleventasOld.contains(newDetalle)) {
                    Productos oldProducto = newDetalle.getIdProducto();
                    newDetalle.setIdProducto(productos);
                    newDetalle = em.merge(newDetalle);
                    if (oldProducto != null && !oldProducto.equals(productos)) {
                        oldProducto.getDetalleventasList().remove(newDetalle);
                        em.merge(oldProducto);
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
                throw new NonexistentEntityException("El producto con el id: " + id + " se eliminó correctamente", enfe);
            }
            List<String> illegalOrphanMessages = null;

            List<Detalleventas> detalleventasOrphanCheck = productos.getDetalleventasList();
            if (detalleventasOrphanCheck != null && !detalleventasOrphanCheck.isEmpty()) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<>();
                }
                illegalOrphanMessages.add("El producto (" + productos + ") no puede ser eliminado ya que está relacionado a un detalle");
            }

            Collection<Kardex> kardexCollectionOrphanCheck = productos.getKardexCollection();
            if (kardexCollectionOrphanCheck != null && !kardexCollectionOrphanCheck.isEmpty()) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<>();
                }
                illegalOrphanMessages.add("El producto (" + productos + ") no puede ser eliminado ya que está relacionado a un kardex");
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
