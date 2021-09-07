package fr.insee.queen.batch.service;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fr.insee.queen.batch.Model;
import fr.insee.queen.batch.utils.PathUtils;

/**
 * Service for folder utils
 * @author samco
 */
@Service
public class FolderService {
	
	@Autowired
	String getFolderIn;
	
	@Autowired
	String getFolderOut;
	
	@Autowired
	String getCampaignName;
	
	@Autowired
	String getFilename;
	
	public String getFolderIn() {
		return getFolderIn;
	}
	
	public String getFolderOut() {
		return getFolderOut;	
	}
	
	public String getCampaignName() {
		return getCampaignName;
	}
	
	public void setCampaignName(String xmlPath) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new File( xmlPath ));
		this.getCampaignName = document.getDocumentElement().getElementsByTagName("Id").item(0).getTextContent().toString();
	}
	
	public String getFilename() {
		return getFilename;
	}
	
	public void setFilename(Model model, String xmlPath) throws ParserConfigurationException, SAXException, IOException {
		setCampaignName(xmlPath);
		this.getFilename = model.getLabel() + "." + getCampaignName() + "." + PathUtils.getTimestampForPath().toString() + ".xml";
	}

}
