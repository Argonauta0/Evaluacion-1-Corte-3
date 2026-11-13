package ni.edu.uam.laboratorioorm.dao;

import jakarta.persistence.EntityManager;
import ni.edu.uam.laboratorioorm.database.JPAUtil;
import ni.edu.uam.laboratorioorm.model.TipoEquipo;

import java.util.List;

public class TipoEquipoDAO {

    public List<TipoEquipo> listar() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT t FROM TipoEquipo t ORDER BY t.nombre", TipoEquipo.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}