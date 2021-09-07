package fr.insee.queen.batch.dao;

import java.sql.SQLException;

import fr.insee.queen.batch.object.Nomenclature;

/**
 * Interface for the Nomenclature entity
 * @author scorcaud
 *
 */
public interface NomenclatureDao {
	
	/**
	 * Create a Nomenclature in database
	 * @param nomenclature
	 * @throws SQLException
	 */
    void create(Nomenclature nomenclature) throws SQLException;
    
    /**
     * Get a Nomenclature by id in database
     * @param id
     * @return Nomenclature object
     * @throws Exception
     */
    Nomenclature findById(String id);
    
    /**
     * Update a Nomenclature in database
     * @param nomenclature
     * @throws SQLException 
     * @throws Exception
     */
	void update(Nomenclature nomenclature) throws SQLException;

	/**
	 * Check if nomenclature exist in db
	 * @param id
	 * @return
	 */
	boolean exist(String id);

	/**
	 * Delete a nomenclature
	 * @param nomenclature
	 */
	void delete(Nomenclature nomenclature);

	/**
	 * Check if the nomenclature is used
	 * @param id
	 * @return
	 */
	boolean isUsed(String id);
}
