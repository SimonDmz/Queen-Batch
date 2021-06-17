package fr.insee.queen.batch.dao;

import java.sql.SQLException;
import java.util.List;

import fr.insee.queen.batch.object.Campaign;

/**
 * Interface for the Campaign entity
 * @author scorcaud
 *
 */
public interface CampaignDao {
	
	/**
	 * Create an Campaign in database
	 * @param campaign
	 * @throws SQLException
	 */
    void create(Campaign campaign);

    /**
     * Get an Campaign by id in database
     * @param id
     * @return Nomenclature object
     * @throws Exception
     */
	Campaign findById(String id);

	/**
     * Check if an Campaign already exist in database
     * @param id
     * @return boolean
     * @throws Exception
     */
	boolean exist(String id);

	/**
     * Get all Campaign id in database
	 * @return {@link List} of {@link String}
	 */
	List<Campaign> findAll();

	/**
	 * Delete a campaign by his id
	 * @param id
	 */
	void delete(String id);

}
