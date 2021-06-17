package fr.insee.queen.batch.dao.mongo.impl;

import java.sql.SQLException;
import java.util.List;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ConditonMongo;
import fr.insee.queen.batch.dao.CommentDao;
import fr.insee.queen.batch.object.Comment;
import fr.insee.queen.batch.object.SurveyUnit;

/**
 * Service for the Comment entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
@Conditional(value= ConditonMongo.class)
public class CommentDaoMongoImpl implements CommentDao {

	@Autowired
	MongoTemplate mongoTemplate;

	/**
	 * Delete all the comments for a list of SU
	 */
	@Override
	public void deleteCommentBySurveyUnitIds(List<String> lstSu) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").in(lstSu));
		List<SurveyUnit> suList = mongoTemplate.find(query, SurveyUnit.class, "survey_unit");
		suList.stream().forEach(su -> {
			if(su != null && su.getComment() != null) {
				mongoTemplate.remove(su.getComment(), "comment");
			}
		});
	}

	/**
	 * Delete all the comment for a campaign
	 */
	@Override
	public void deleteCommentByCampaignId(String campaignId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("campaign.id").is(campaignId));
		List<SurveyUnit> suList = mongoTemplate.find(query, SurveyUnit.class, "survey_unit");
		suList.stream().forEach(su -> {
			if(su != null && su.getComment() != null) {
				mongoTemplate.remove(su.getComment(), "comment");
			}
		});
	}

	/**
	 * Create a comment for a SurveyUnit
	 */
	@Override
	public void createComment(SurveyUnit surveyUnit) throws SQLException {
		Comment comment = new Comment();
		comment.setId(surveyUnit.getComment().getId());
		comment.setValue(new JSONObject());
		comment.setSurveyUnit(surveyUnit);
		mongoTemplate.save(comment, "comment");
	}

	/**
	 * Update the comment by a SU
	 */
	@Override
	public void updateComment(SurveyUnit surveyUnit) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(surveyUnit.getComment().getId()));
		Comment commentTemp = mongoTemplate.findOne(query, Comment.class, "comment");
		commentTemp.setSurveyUnit(surveyUnit);
		mongoTemplate.save(commentTemp, "comment");
	}
}
