package dao;

import dao.exceptions.NonexistentEntityException;
import dto.Kardex;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Productos;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


public class KardexJpaController implements Serializable {

    public KardexJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_SistemaVentas_war_1.0-SNAPSHOTPU");
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Kardex kardex) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Productos idProducto = kardex.getIdProducto();
            if (idProducto != null) {
                idProducto = em.getReference(idProducto.getClass(), idProducto.getIdProducto());
                kardex.setIdProducto(idProducto);
            }
            em.persist(kardex);
            if (idProducto != null) {
                idProducto.getKardexCollection().add(kardex);
                idProducto = em.merge(idProducto);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Kardex kardex) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Kardex persistentKardex = em.find(Kardex.class, kardex.getIdKardex());
            Productos idProductoOld = persistentKardex.getIdProducto();
            Productos idProductoNew = kardex.getIdProducto();
            if (idProductoNew != null) {
                idProductoNew = em.getReference(idProductoNew.getClass(), idProductoNew.getIdProducto());
                kardex.setIdProducto(idProductoNew);
            }
            kardex = em.merge(kardex);
            if (idProductoOld != null && !idProductoOld.equals(idProductoNew)) {
                idProductoOld.getKardexCollection().remove(kardex);
                idProductoOld = em.merge(idProductoOld);
            }
            if (idProductoNew != null && !idProductoNew.equals(idProductoOld)) {
                idProductoNew.getKardexCollection().add(kardex);
                idProductoNew = em.merge(idProductoNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = kardex.getIdKardex();
                if (findKardex(id) == null) {
                    throw new NonexistentEntityException("The kardex with id " + id + " no longer exists.");
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
            Kardex kardex;
            try {
                kardex = em.getReference(Kardex.class, id);
                kardex.getIdKardex();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The kardex with id " + id + " no longer exists.", enfe);
            }
            Productos idProducto = kardex.getIdProducto();
            if (idProducto != null) {
                idProducto.getKardexCollection().remove(kardex);
                idProducto = em.merge(idProducto);
            }
            em.remove(kardex);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Kardex> findKardexEntities() {
        return findKardexEntities(true, -1, -1);
    }

    public List<Kardex> findKardexEntities(int maxResults, int firstResult) {
        return findKardexEntities(false, maxResults, firstResult);
    }

    private List<Kardex> findKardexEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Kardex.class));
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

    public Kardex findKardex(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Kardex.class, id);
        } finally {
            em.close();
        }
    }

    public int getKardexCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Kardex> rt = cq.from(Kardex.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
