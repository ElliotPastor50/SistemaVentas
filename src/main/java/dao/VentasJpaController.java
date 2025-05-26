package dao;

import dao.exceptions.IllegalOrphanException;
import dao.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dto.Clientes;
import dto.Detalleventas;
import dto.Ventas;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;


public class VentasJpaController implements Serializable {

    public VentasJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Ventas ventas) {
        if (ventas.getDetalleventas() == null) {
            ventas.setDetalleventas(new ArrayList<Detalleventas>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Clientes idCliente = ventas.getIdCliente();
            if (idCliente != null) {
                idCliente = em.getReference(idCliente.getClass(), idCliente.getIdCliente());
                ventas.setIdCliente(idCliente);
            }
            List<Detalleventas> attachedDetalleventas = new ArrayList<Detalleventas>();
            for (Detalleventas detalleventasDetalleventasToAttach : ventas.getDetalleventas()) {
                detalleventasDetalleventasToAttach = em.getReference(detalleventasDetalleventasToAttach.getClass(), detalleventasDetalleventasToAttach.getIdDetalleVenta());
                attachedDetalleventas.add(detalleventasDetalleventasToAttach);
            }
            ventas.setDetalleventas(attachedDetalleventas);
            em.persist(ventas);
            if (idCliente != null) {
                idCliente.getVentasCollection().add(ventas);
                idCliente = em.merge(idCliente);
            }
            for (Detalleventas detalleventasDetalleventas : ventas.getDetalleventas()) {
                Ventas oldIdVentaOfDetalleventasDetalleventas = detalleventasDetalleventas.getIdVenta();
                detalleventasDetalleventas.setIdVenta(ventas);
                detalleventasDetalleventas = em.merge(detalleventasDetalleventas);
                if (oldIdVentaOfDetalleventasDetalleventas != null) {
                    oldIdVentaOfDetalleventasDetalleventas.getDetalleventas().remove(detalleventasDetalleventas);
                    oldIdVentaOfDetalleventasDetalleventas = em.merge(oldIdVentaOfDetalleventasDetalleventas);
                }
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
            Clientes idClienteOld = persistentVentas.getIdCliente();
            Clientes idClienteNew = ventas.getIdCliente();
            List<Detalleventas> detalleventasOld = persistentVentas.getDetalleventas();
            List<Detalleventas> detalleventasNew = ventas.getDetalleventas();
            List<String> illegalOrphanMessages = null;
            for (Detalleventas detalleventasOldDetalleventas : detalleventasOld) {
                if (!detalleventasNew.contains(detalleventasOldDetalleventas)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Detalleventas " + detalleventasOldDetalleventas + " since its idVenta field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (idClienteNew != null) {
                idClienteNew = em.getReference(idClienteNew.getClass(), idClienteNew.getIdCliente());
                ventas.setIdCliente(idClienteNew);
            }
            List<Detalleventas> attachedDetalleventasNew = new ArrayList<Detalleventas>();
            for (Detalleventas detalleventasNewDetalleventasToAttach : detalleventasNew) {
                detalleventasNewDetalleventasToAttach = em.getReference(detalleventasNewDetalleventasToAttach.getClass(), detalleventasNewDetalleventasToAttach.getIdDetalleVenta());
                attachedDetalleventasNew.add(detalleventasNewDetalleventasToAttach);
            }
            detalleventasNew = attachedDetalleventasNew;
            ventas.setDetalleventas(detalleventasNew);
            ventas = em.merge(ventas);
            if (idClienteOld != null && !idClienteOld.equals(idClienteNew)) {
                idClienteOld.getVentasCollection().remove(ventas);
                idClienteOld = em.merge(idClienteOld);
            }
            if (idClienteNew != null && !idClienteNew.equals(idClienteOld)) {
                idClienteNew.getVentasCollection().add(ventas);
                idClienteNew = em.merge(idClienteNew);
            }
            for (Detalleventas detalleventasNewDetalleventas : detalleventasNew) {
                if (!detalleventasOld.contains(detalleventasNewDetalleventas)) {
                    Ventas oldIdVentaOfDetalleventasNewDetalleventas = detalleventasNewDetalleventas.getIdVenta();
                    detalleventasNewDetalleventas.setIdVenta(ventas);
                    detalleventasNewDetalleventas = em.merge(detalleventasNewDetalleventas);
                    if (oldIdVentaOfDetalleventasNewDetalleventas != null && !oldIdVentaOfDetalleventasNewDetalleventas.equals(ventas)) {
                        oldIdVentaOfDetalleventasNewDetalleventas.getDetalleventas().remove(detalleventasNewDetalleventas);
                        oldIdVentaOfDetalleventasNewDetalleventas = em.merge(oldIdVentaOfDetalleventasNewDetalleventas);
                    }
                }
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
            List<Detalleventas> detalleventasOrphanCheck = ventas.getDetalleventas();
            for (Detalleventas detalleventasOrphanCheckDetalleventas : detalleventasOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Ventas (" + ventas + ") cannot be destroyed since the Detalleventas " + detalleventasOrphanCheckDetalleventas + " in its detalleventas field has a non-nullable idVenta field.");
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
