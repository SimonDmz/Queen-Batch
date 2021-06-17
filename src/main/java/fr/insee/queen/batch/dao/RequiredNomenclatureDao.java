package fr.insee.queen.batch.dao;

import java.sql.SQLException;
import java.util.List;

import fr.insee.queen.batch.object.Nomenclature;

/**
 * Interface for the RequiredNomenclature entity
 * @author scorcaud
 *
 */
public interface RequiredNomenclatureDao {
	
	/**
	 * Create a RequiredNomenclature in database
	 * @param id
	 * @param code
	 * @throws SQLException
	 */
    void create(String id, String code);
    
    /**
     * Check if a RequiredNomenclature already exist in database
     * @param id
     * @param code
     * @return boolean
     * @throws Exception
     */
    boolean exist(String id, String code);
    
    /**
     * Find nomenclature by a questionnaire id
     * @param questionnaireId
     * @return
     */
    List<String> findByQuestionnaireId(String questionnaireId);

    /**
     * Delete a nomenclature
     * @param nomenclature
     */
	void deleteByNomenclatureId(Nomenclature nomenclature);
}
