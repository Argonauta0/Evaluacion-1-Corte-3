package ni.edu.uam.laboratorioorm.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import ni.edu.uam.laboratorioorm.database.JPAUtil;
import ni.edu.uam.laboratorioorm.model.Equipo;
import ni.edu.uam.laboratorioorm.model.TipoEquipo;

import java.util.List;

public class EquipoDAO {

    private TipoEquipo obtenerTipoEquipoManaged(EntityManager em, Equipo equipo) {
        if (equipo.getTipoEquipo() == null || equipo.getTipoEquipo().getIdTipo() == null) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de equipo válido.");
        }

        TipoEquipo tipo = em.find(TipoEquipo.class, equipo.getTipoEquipo().getIdTipo());
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de equipo seleccionado no existe en la base de datos.");
        }
        return tipo;
    }

    public boolean existeCodigo(String codigo, Long idExcluir) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT COUNT(e) FROM Equipo e WHERE LOWER(e.codigo) = LOWER(:codigo)";
            if (idExcluir != null) {
                jpql += " AND e.idEquipo <> :idExcluir";
            }

            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("codigo", codigo.trim());
            if (idExcluir != null) {
                query.setParameter("idExcluir", idExcluir);
            }

            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public void guardar(Equipo equipo) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TipoEquipo tipoManaged = obtenerTipoEquipoManaged(em, equipo);
            equipo.setTipoEquipo(tipoManaged);

            em.persist(equipo);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void actualizar(Equipo equipo) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TipoEquipo tipoManaged = obtenerTipoEquipoManaged(em, equipo);
            equipo.setTipoEquipo(tipoManaged);

            em.merge(equipo);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public boolean eliminar(Long idEquipo) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Equipo equipo = em.find(Equipo.class, idEquipo);
            if (equipo == null) {
                tx.rollback();
                return false;
            }

            em.remove(equipo);
            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public List<Equipo> listar() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Equipo e JOIN FETCH e.tipoEquipo ORDER BY e.idEquipo",
                    Equipo.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Consulta JPQL parametrizada que permite buscar por fragmento de nombre,
     * filtrar por TipoEquipo, o aplicar ambos criterios combinados.
     */
    public List<Equipo> buscarPorNombreYTipo(String fragmentoNombre, TipoEquipo tipoFiltro) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            boolean tieneNombre = fragmentoNombre != null && !fragmentoNombre.trim().isEmpty();
            boolean tieneTipo = tipoFiltro != null && tipoFiltro.getIdTipo() != null;

            StringBuilder jpql = new StringBuilder("SELECT e FROM Equipo e JOIN FETCH e.tipoEquipo WHERE 1=1");

            if (tieneNombre) {
                jpql.append(" AND LOWER(e.nombre) LIKE LOWER(:nombre)");
            }
            if (tieneTipo) {
                jpql.append(" AND e.tipoEquipo.idTipo = :idTipo");
            }
            jpql.append(" ORDER BY e.idEquipo");

            TypedQuery<Equipo> query = em.createQuery(jpql.toString(), Equipo.class);

            if (tieneNombre) {
                query.setParameter("nombre", "%" + fragmentoNombre.trim() + "%");
            }
            if (tieneTipo) {
                query.setParameter("idTipo", tipoFiltro.getIdTipo());
            }

            return query.getResultList();
        } finally {
            em.close();
        }
    }
}