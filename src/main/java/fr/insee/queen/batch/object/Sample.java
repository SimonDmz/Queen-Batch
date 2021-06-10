package fr.insee.queen.batch.object;

import java.util.List;

/**
* Object XmlSample : represent the sample XML file
* 
* @author Claudel Benjamin
* 
*/
public class Sample {
	/**
	* The fileName of sample
	*/
	private String fileName;
	/**
	* The campaign of sample
	*/
	private Campaign campaign;
	
	private List<SurveyUnit> surveyUnits;
	

	public Sample(String fileName, Campaign campaign, List<SurveyUnit> surveyUnits) {
		super();
		this.fileName = fileName;
		this.campaign = campaign;
		this.surveyUnits = surveyUnits;
	}
	
	public Sample() {
		super();
	}

	/**
	 * @return fileName of sample
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * @return campaign of sample
	 */
	public Campaign getCampaign() {
		return campaign;
	}
	/**
	 * @param campaign xmlCampaign to set
	 */
	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	/**
	 * @return the surveyUnits
	 */
	public List<SurveyUnit> getSurveyUnits() {
		return surveyUnits;
	}

	/**
	 * @param surveyUnits the surveyUnits to set
	 */
	public void setSurveyUnits(List<SurveyUnit> surveyUnits) {
		this.surveyUnits = surveyUnits;
	}
}
