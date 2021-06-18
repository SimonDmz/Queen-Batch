package fr.insee.queen.batch.service;

import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import fr.insee.queen.batch.config.ApplicationContext;
import fr.insee.queen.batch.dao.CampaignDao;
import fr.insee.queen.batch.dao.DataDao;
import fr.insee.queen.batch.dao.NomenclatureDao;
import fr.insee.queen.batch.dao.ParadataEventDao;
import fr.insee.queen.batch.dao.PersonalizationDao;
import fr.insee.queen.batch.dao.QuestionnaireModelDao;
import fr.insee.queen.batch.dao.StateDataDao;
import fr.insee.queen.batch.dao.SurveyUnitDao;
import fr.insee.queen.batch.object.Campaign;
import fr.insee.queen.batch.object.Data;
import fr.insee.queen.batch.object.Nomenclature;
import fr.insee.queen.batch.object.Personalization;
import fr.insee.queen.batch.object.QuestionnaireModel;
import fr.insee.queen.batch.object.StateData;
import fr.insee.queen.batch.object.SurveyUnit;

@Service
public class DatasetService {
	
	@Autowired
	AnnotationConfigApplicationContext context;
	/**
	 * This method initialize the data for testing
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void createDataSet() throws Exception {
		// Datasource initialization
		context = new AnnotationConfigApplicationContext(ApplicationContext.class);
		NomenclatureDao nomenclatureDao = context.getBean(NomenclatureDao.class);
		QuestionnaireModelDao questionnaireModelDao = context.getBean(QuestionnaireModelDao.class);
		CampaignDao campaignDao = context.getBean(CampaignDao.class);
		SurveyUnitDao surveyUnitDao = context.getBean(SurveyUnitDao.class);
		DataDao dataDao = context.getBean(DataDao.class);
		StateDataDao stateDataDao = context.getBean(StateDataDao.class);
		PersonalizationDao personalizationDao = context.getBean(PersonalizationDao.class);
		ParadataEventDao paradataEventDao = context.getBean(ParadataEventDao.class);
		Nomenclature nomenclatureCities2019 = new Nomenclature("cities2019", "french cities 2019", new JSONArray());
		Nomenclature nomenclatureRegions2019 = new Nomenclature("regions2019", "french regions 2019", new JSONArray());
		nomenclatureDao.create(nomenclatureCities2019);
		nomenclatureDao.create(nomenclatureRegions2019);
		QuestionnaireModel questionnaireSimpsons = new QuestionnaireModel("simpsons", "Questionnaire about the Simpsons tv show", new JSONObject(), "SIMPSONS2020X00");
		questionnaireModelDao.create(questionnaireSimpsons, null);
		Campaign campaignSimpsons = new Campaign("SIMPSONS2020X00", "Survey on the Simpsons tv show 2020");
		campaignSimpsons.setQuestionnaireModels(List.of(questionnaireSimpsons));
		campaignDao.create(campaignSimpsons);
		Personalization personalization = new Personalization();
		personalization.setId(UUID.randomUUID());
		personalization.setValue(new JSONArray());
		Data data11 = new Data(UUID.randomUUID(), new JSONObject(), null);
		Data data12 = new Data(UUID.randomUUID(), new JSONObject(), null);
		Data data13 = new Data(UUID.randomUUID(), new JSONObject(), null);
		Data data14 = new Data(UUID.randomUUID(), new JSONObject(), null);
		Data data15 = new Data(UUID.randomUUID(), new JSONObject(), null);
		StateData stateData11 = new StateData(UUID.randomUUID(), "VALIDATED", 20210513124955L, "1", null);
		StateData stateData12 = new StateData(UUID.randomUUID(), "VALIDATED", 20210513124955L, "1", null);
		StateData stateData13 = new StateData(UUID.randomUUID(), "COMPLETED", 20210513124955L, "1", null);
		StateData stateData14 = new StateData(UUID.randomUUID(), "INIT", 20210513124955L, "1", null);
		StateData stateData15 = new StateData(UUID.randomUUID(), "VALIDATED", 20210513124955L, "1", null);
		SurveyUnit su11 = new SurveyUnit("11", campaignSimpsons, questionnaireSimpsons, null, data11, stateData11, personalization);
		SurveyUnit su12 = new SurveyUnit("12", campaignSimpsons, questionnaireSimpsons, null, data12, stateData12, null);
		SurveyUnit su13 = new SurveyUnit("13", campaignSimpsons, questionnaireSimpsons, null, data13, stateData13, null);
		SurveyUnit su14 = new SurveyUnit("14", campaignSimpsons, questionnaireSimpsons, null, data14, stateData14, null);
		SurveyUnit su15 = new SurveyUnit("15", campaignSimpsons, questionnaireSimpsons, null, data15, stateData15, null);
		JSONObject paradataJson1 = new JSONObject();
		paradataJson1.put("idSu", "15");
		paradataJson1.put("events", " [\r\n" + 
				"        {\r\n" + 
				"            \"test\": \"test\"\r\n" + 
				"        }\r\n" + 
				"    ]");
		paradataEventDao.createParadata(paradataJson1);
		surveyUnitDao.createSurveyUnit(su11);
		surveyUnitDao.createSurveyUnit(su12);
		surveyUnitDao.createSurveyUnit(su13);
		surveyUnitDao.createSurveyUnit(su14);
		surveyUnitDao.createSurveyUnit(su15);
		dataDao.createData(su11);
		dataDao.createData(su12);
		dataDao.createData(su13);
		dataDao.createData(su14);
		dataDao.createData(su15);
		stateData11.setSurveyUnit(su11);
		stateData12.setSurveyUnit(su12);
		stateData13.setSurveyUnit(su13);
		stateData14.setSurveyUnit(su14);
		stateData15.setSurveyUnit(su15);
		stateDataDao.createStateData(stateData11);
		stateDataDao.createStateData(stateData12);
		stateDataDao.createStateData(stateData13);
		stateDataDao.createStateData(stateData14);
		stateDataDao.createStateData(stateData15);
		personalizationDao.createPersonalization(su11);
		System.out.println("DB created");
	}
}
