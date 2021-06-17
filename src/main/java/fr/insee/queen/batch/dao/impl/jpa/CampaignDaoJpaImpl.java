package fr.insee.queen.batch.dao.impl.jpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonJpa;
import fr.insee.queen.batch.dao.CampaignDao;
import fr.insee.queen.batch.object.Campaign;

/**
 * Service for the Campaign entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonJpa.class)
public class CampaignDaoJpaImpl implements CampaignDao {
	
	@Autowired
	JdbcTemplate jdbcTemplate;

	/**
	 * Implements the creation of a Campaign in database
	 * @param campaign
	 * @throws SQLException
	 */
	@Override
	public void create(Campaign campaign) {
		StringBuilder qString = new StringBuilder("INSERT INTO campaign (id, label) VALUES (?, ?)");
	    jdbcTemplate.update(qString.toString(), campaign.getId(), campaign.getLabel());
	}
	
	/**
	 * Implementation to check if an Campaign already exist in database 
	 * @param id
	 * @return boolean
	 * @throws SQLException
	 */
	@Override
	public boolean exist(String id) {
		StringBuilder qString = new StringBuilder("SELECT COUNT(id) FROM campaign WHERE id=?");
		Long nbRes = jdbcTemplate.queryForObject(qString.toString(), new Object[]{id}, Long.class);
		return nbRes>0;	
	}
	
	/**
	 * Retrieve the Campaign by the id passed in parameter
	 * @param id
	 * @return Campaign object
	 */
	@Override
	public Campaign findById(String id) {
		Campaign campaign = null;
		StringBuilder qString = new StringBuilder("SELECT id FROM campaign WHERE id=?");
		campaign = jdbcTemplate.queryForObject(qString.toString(), new Object[]{id}, new CampaigneMapper());
		return campaign;
	}
	
	/**
	 * Implements the mapping between the result of the query and the Campaign entity
	 * @return CampaignMapper
	 */
    private static final class CampaigneMapper implements RowMapper<Campaign> {
        public Campaign mapRow(ResultSet rs, int rowNum) throws SQLException         {
        	Campaign campaign = new Campaign();
        	campaign.setId(rs.getString("id"));
            return campaign;
        }
    }

    /**
     * Get all the campaign id db
     */
	@Override
	public List<Campaign> findAll() {
		StringBuilder qString = new StringBuilder("SELECT * FROM campaign");
		return jdbcTemplate.query(qString.toString(), 
				(rs, rowNum) -> new Campaign(rs.getString("id"), rs.getString("label")));
	}

	/**
	 * Delete a campaign by his id
	 */
	@Override
	public void delete(String id) {
		StringBuilder qString = new StringBuilder("DELETE FROM campaign WHERE id = ?");
		jdbcTemplate.update(qString.toString(), id);
	}
}
