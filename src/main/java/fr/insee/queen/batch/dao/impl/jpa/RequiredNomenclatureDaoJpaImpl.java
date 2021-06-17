package fr.insee.queen.batch.dao.impl.jpa;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonJpa;
import fr.insee.queen.batch.dao.RequiredNomenclatureDao;
import fr.insee.queen.batch.object.Nomenclature;

/**
 * Service for the  RequiredNomenclature entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonJpa.class)
public class RequiredNomenclatureDaoJpaImpl implements RequiredNomenclatureDao{
	
	@Autowired
	JdbcTemplate jdbcTemplate;

	/**
	 * Implements the creation of a RequiredNomenclature in database
	 * @param id
	 * @param code
	 * @throws SQLException
	 */
	@Override
	public void create(String id, String code) {
		StringBuilder qString = new StringBuilder("INSERT INTO required_nomenclature (id_required_nomenclature, code) VALUES (?, ?)");
	    jdbcTemplate.update(qString.toString(), id, code);
	}
	
	/**
	 * Implementation to check if an RequiredNomenclature already exist in database 
	 * @param id
	 * @param code
	 * @return boolean
	 * @throws SQLException
	 */
	@Override
	public boolean exist(String id, String code) {
		StringBuilder qString = new StringBuilder("SELECT COUNT(id_required_nomenclature) FROM required_nomenclature ")
					.append("WHERE id_required_nomenclature=? and code=?");
		Long nbRes = jdbcTemplate.queryForObject(qString.toString(), new Object[]{id,code}, Long.class);
		return nbRes>0;
	}
	
	/**
	 * Get the nomenclature list for a Questionnaire id
	 */
	@Override
	public List<String> findByQuestionnaireId(String questionnaireId) {
		StringBuilder qString = new StringBuilder("SELECT code FROM required_nomenclature WHERE id_required_nomenclature=?");
		return jdbcTemplate.queryForList(qString.toString(), new Object[]{questionnaireId}, String.class);
	}

	/**
	 * Delete the nomenclature by his id
	 */
	@Override
	public void deleteByNomenclatureId(Nomenclature nomenclature) {
		StringBuilder qString = new StringBuilder("DELETE FROM required_nomenclature WHERE code = ?");
		jdbcTemplate.update(qString.toString(), nomenclature.getId());
	}

}
