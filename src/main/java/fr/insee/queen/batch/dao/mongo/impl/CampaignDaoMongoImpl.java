package fr.insee.queen.batch.dao.mongo.impl;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonMongo;
import fr.insee.queen.batch.dao.CampaignDao;
import fr.insee.queen.batch.object.Campaign;

/**
 * Service for the Campaign entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonMongo.class)
public class CampaignDaoMongoImpl implements CampaignDao {
	
	@Autowired
	MongoTemplate mongoTemplate;

	/**
	 * Implements the creation of a Campaign in database
	 * @param campaign
	 * @throws SQLException
	 */
	@Override
	public void create(Campaign campaign) {
		if(campaign.getLabel().equals(null) || campaign.getLabel().isBlank()) {
			campaign.setLabel("");
		}
	    mongoTemplate.save(campaign, "campaign");
	}
	
	/**
	 * Implementation to check if an Campaign already exist in database 
	 * @param id
	 * @return boolean
	 * @throws SQLException
	 */
	@Override
	public boolean exist(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(id));
		return mongoTemplate.findOne(query, Campaign.class, "campaign") != null;
	}
	
	/**
	 * Retrieve the Campaign by the id passed in parameter
	 * @param id
	 * @return Campaign object
	 */
	@Override
	public Campaign findById(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(id));
		return mongoTemplate.findOne(query, Campaign.class, "campaign");
	}

	/**
	 * Retrieve all the campaigns
	 */
	@Override
	public List<Campaign> findAll() {
		return mongoTemplate.findAll(Campaign.class, "campaign");
	}

	/**
	 * Delete a campaign by his id
	 */
	@Override
	public void delete(String id) {
		Campaign campaign = new Campaign();
		campaign = findById(id);
		mongoTemplate.remove(campaign, "campaign");
	}
}
