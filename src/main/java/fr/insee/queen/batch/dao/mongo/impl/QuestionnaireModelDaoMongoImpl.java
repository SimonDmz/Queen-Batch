package fr.insee.queen.batch.dao.mongo.impl;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonMongo;
import fr.insee.queen.batch.dao.CampaignDao;
import fr.insee.queen.batch.dao.QuestionnaireModelDao;
import fr.insee.queen.batch.object.Campaign;
import fr.insee.queen.batch.object.QuestionnaireModel;

/**
 * Service for the QuestionnaireModel entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonMongo.class)
public class QuestionnaireModelDaoMongoImpl implements QuestionnaireModelDao{
	
	@Autowired
	MongoTemplate mongoTemplate;
	
	@Autowired
	CampaignDao campaignDao;

	/**
	 * Implements the creation of a QuestionnaireModel in database
	 * @param questionnaireModel
	 * @throws SQLException
	 */
	@Override
	public void create(QuestionnaireModel questionnaireModel, String campaignId) throws SQLException {
		mongoTemplate.save(questionnaireModel, "questionnaire_model");
	}
	
	/**
	 * Implementation to check if a QuestionnaireModel already exist in database 
	 * @param id
	 * @return boolean
	 * @throws SQLException
	 */
	@Override
	public boolean exist(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(id));
		return mongoTemplate.findOne(query, QuestionnaireModel.class, "questionnaire_model") != null;
	}


	/**
	 * Retrieve the QuestionnaireModel by the id passed in parameter
	 * @param id
	 * @return QuestionnaireModel object
	 */
	@Override
	public QuestionnaireModel findById(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(id));
		return mongoTemplate.findOne(query, QuestionnaireModel.class, "questionnaire_model");
	}
	
	/**
	 * Retrieve the QuestionnaireModel by the id passed in parameter
	 * @param id
	 * @return QuestionnaireModel object
	 */
	@Override
	public List<QuestionnaireModel> findByCampaignId(String campaignId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(campaignId));
		return mongoTemplate.findOne(query, Campaign.class, "campaign").getQuestionnaireModels();
	}

	/**
	 * Implements the update of a questionnaireModel in database
	 * @param questionnaireModel
	 * @throws SQLException 
	 */
	@Override
	public void updateCampaignId(QuestionnaireModel questionnaireModel) throws SQLException {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(questionnaireModel.getId()));
		Update update = new Update();
		Campaign campaign = campaignDao.findById(questionnaireModel.getCampaignId());
		if(campaign != null) {
			update.set("campaign", campaign);
		}
		mongoTemplate.findAndModify(query, update, QuestionnaireModel.class, "questionnaire_model");
	}

	/**
	 * Implementation to check if a QuestionnaireModel already exist in database 
	 * @param id
	 * @return boolean
	 * @throws SQLException
	 */
	@Override
	public boolean existForCampaign(String id, String campaignId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("campign.id").is(campaignId));
		return mongoTemplate.findOne(query, QuestionnaireModel.class, "questionnaire_model") != null;
	}

	/**
	 * Delete association between campaign and questionnaire model
	 * @param questionnaireModel
	 * @throws SQLException 
	 */
	@Override
	public void deleteCampaignIdForQuestionnaireModel(String campaignId) throws SQLException {
		List<QuestionnaireModel> qmList = findByCampaignId(campaignId);
		qmList.stream().forEach(qm -> {
			qm.setCampaignId(null);
			mongoTemplate.save(qm, "questionnaire_model");
		});
		
	}
}
