package fr.insee.queen.batch.dao.mongo.impl;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonMongo;
import fr.insee.queen.batch.dao.NomenclatureDao;
import fr.insee.queen.batch.object.Nomenclature;
import fr.insee.queen.batch.object.QuestionnaireModel;

/**
 * Service for the Nomenclature entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonMongo.class)
public class NomenclatureDaoMongoImpl implements NomenclatureDao {
	
	@Autowired
	MongoTemplate mongoTemplate;

	/**
	 * Implements the creation of a Nomenclature in database
	 * @param nomenclature
	 * @throws SQLException
	 */
	@Override
	public void create(Nomenclature nomenclature) throws SQLException {
	    mongoTemplate.save(nomenclature, "nomenclature");
	}
	
	/**
	 * Implements the delete of a Nomenclature in database
	 * @param nomenclature
	 */
	@Override
	public void delete(Nomenclature nomenclature) {
		mongoTemplate.remove(nomenclature, "nomenclature");
	}
	
	/**
	 * Implements the update of a Nomenclature in database
	 * @param nomenclature
	 * @throws SQLException 
	 */
	@Override
	public void update(Nomenclature nomenclature) throws SQLException {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(nomenclature.getId()));
		Update update = new Update();
		update.set("label", nomenclature.getLabel());
		update.set("value", nomenclature.getValue().toJSONString());
		mongoTemplate.findAndModify(query, update, Nomenclature.class, "nomenclature");
	}
	
	/**
	 * Retrieve the nomenclature by the id passed in parameter
	 * @param id
	 * @return Nomenclature object
	 */
	@Override
	public Nomenclature findById(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(id));
		return mongoTemplate.findOne(query, Nomenclature.class, "nomenclature");
	}
	
	/**
	 * Check if nomenclature exist
	 */
	@Override
	public boolean exist(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(id));
		return mongoTemplate.findOne(query, Nomenclature.class, "nomenclature") != null;
	}
	
	/**
	 * Check is nomenclature is used
	 */
	@Override
	public boolean isUsed(String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("nomenclatures.id").is(id));
		QuestionnaireModel qm = mongoTemplate.findOne(query, QuestionnaireModel.class, "questionnaire_model");
		if(qm != null) {
			return true;
		} else {
			return false;
		}
	}

}
