package fr.insee.queen.batch.dao.impl.jpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonJpa;
import fr.insee.queen.batch.dao.NomenclatureDao;
import fr.insee.queen.batch.object.Nomenclature;

/**
 * Service for the Nomenclature entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonJpa.class)
public class NomenclatureDaoJpaImpl implements NomenclatureDao {
	
	@Autowired
	@Qualifier("jdbcTemplate")
	JdbcTemplate jdbcTemplate;

	/**
	 * Implements the creation of a Nomenclature in database
	 * @param nomenclature
	 * @throws SQLException
	 */
	@Override
	public void create(Nomenclature nomenclature) throws SQLException {
		StringBuilder qString = new StringBuilder("INSERT INTO nomenclature (id, label, value) VALUES (?, ?, ?)");
		PGobject model = new PGobject();
		model.setType("jsonb");
		model.setValue(nomenclature.getValue().toJSONString());
	    jdbcTemplate.update(qString.toString(), nomenclature.getId(), nomenclature.getLabel(), model);
	}
	
	/**
	 * Implements the delete of a Nomenclature in database
	 * @param nomenclature
	 */
	@Override
	public void delete(Nomenclature nomenclature) {
		StringBuilder qString = new StringBuilder("DELETE FROM nomenclature WHERE id = ?");
		jdbcTemplate.update(qString.toString(), nomenclature.getId());
	}
	
	/**
	 * Implements the update of a Nomenclature in database
	 * @param nomenclature
	 * @throws SQLException 
	 */
	@Override
	public void update(Nomenclature nomenclature) throws SQLException {
		StringBuilder qString = new StringBuilder("UPDATE nomenclature SET label = ?, value = ? WHERE id = ?");
		PGobject model = new PGobject();
		model.setType("jsonb");
		model.setValue(nomenclature.getValue().toJSONString());
	    jdbcTemplate.update(qString.toString(),  nomenclature.getLabel(), model, nomenclature.getId());
	}
	
	/**
	 * Retrieve the nomenclature by the id passed in parameter
	 * @param id
	 * @return Nomenclature object
	 */
	@Override
	public Nomenclature findById(String id) {
		List<Nomenclature> lstRes = null;
		StringBuilder qString = new StringBuilder("SELECT * FROM nomenclature WHERE id=?");
		lstRes = jdbcTemplate.query(qString.toString(), new Object[]{id}, new NomenclatureMapper());
		if(lstRes.isEmpty()) {
			return null;
		}else {
			return lstRes.get(0);
		}
	}
	
	/**
	 * Check if the nomenclature exist in db
	 */
	@Override
	public boolean exist(String id) {
		StringBuilder qString = new StringBuilder("SELECT COUNT(id) FROM nomenclature WHERE id=?");
		Long nbRes = jdbcTemplate.queryForObject(qString.toString(), new Object[]{id}, Long.class);
		return nbRes>0;	
	}
	
	/**
	 * Check if the nomenclature is already used
	 */
	@Override
	public boolean isUsed(String id) {
		StringBuilder qString = new StringBuilder("SELECT COUNT(id) FROM nomenclature n ")
				.append("JOIN required_nomenclature rn ON n.id=rn.code WHERE n.id=?");
		Long nbRes = jdbcTemplate.queryForObject(qString.toString(), new Object[]{id}, Long.class);
		return nbRes>0;	
	}
	
	/**
	 * Implements the mapping between the result of the query and the Nomenclature entity
	 * @return NomenclatureMapper
	 */
    private static final class NomenclatureMapper implements RowMapper<Nomenclature> {
        public Nomenclature mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Nomenclature(rs.getString("id"));
        }
    }

}
