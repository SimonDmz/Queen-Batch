package fr.insee.queen.batch.utils;

import fr.insee.lunatic.conversion.JSONCleaner;
import fr.insee.lunatic.conversion.XMLLunaticFlatToJSONLunaticFlatTranslator;
import fr.insee.lunatic.conversion.XMLLunaticToXMLLunaticFlatTranslator;
import fr.insee.lunatic.conversion.data.XMLLunaticDataToJSON;
import fr.insee.lunatic.conversion.data.XMLLunaticToXSDData;
import fr.insee.lunatic.utils.Modele;
import fr.insee.lunatic.utils.SchemaValidator;
import fr.insee.queen.batch.Constants;
import fr.insee.queen.batch.dao.QuestionnaireModelDao;
import fr.insee.queen.batch.exception.BatchException;
import fr.insee.queen.batch.exception.ValidateException;
import fr.insee.queen.batch.object.Comment;
import fr.insee.queen.batch.object.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Campaign on XML Content - getXmlNodeFile - validateXMLSchema -
 * xmlToCampaign - xmlToQuestionnaireModel - xmlToNomenclatures
 *
 * @author Claudel Benjamin
 */
@Service
public class XmlUtils {
    private static final Logger logger = LogManager.getLogger(XmlUtils.class);

    @Autowired
    QuestionnaireModelDao questionnaireModelDao;

    /**
     * get an XML node in an XML File
     *
     * @param filename filename reference
     * @param nodeName nodeName to search
     * @return the XML node find
     * @throws IOException
     */
    public static NodeList getXmlNodeFile(String filename, String nodeName) throws IOException {
        FileInputStream fis = null;
        try {
            // an instance of factory that gives a document builder
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
            DocumentBuilder db = dbf.newDocumentBuilder();
            fis = new FileInputStream(new File(filename));
            Document doc = db.parse(fis);
            doc.getDocumentElement().normalize();
            return doc.getElementsByTagName(nodeName);
        } catch (Exception e) {
            if (fis != null) fis.close();
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Validate an XML file by XSD validator
     *
     * @param xsdPath xsd path
     * @param xmlPath xml path
     * @return true if XML is valid
     * @throws IOException
     * @throws XMLStreamException
     * @throws BatchException
     */
    public static boolean validateXMLSchema(URL model, String xmlPath) throws ValidateException, IOException, XMLStreamException {
        FileInputStream fis = null;
        XMLStreamReader xmlEncoding = null;
        FileReader fr = null;
        ValidateException ve = null;
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(model);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Validator validator = schema.newValidator();
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            fis = new FileInputStream(new File(xmlPath));
            fr = new FileReader(xmlPath);
            xmlEncoding = XMLInputFactory.newInstance().createXMLStreamReader(fr);
            if (xmlEncoding.getCharacterEncodingScheme().equals("UTF8") || xmlEncoding.getCharacterEncodingScheme().equals(StandardCharsets.UTF_8.toString())) {
                validator.validate(new StreamSource(fis));
            }
            if (model.equals(Constants.MODEL_SAMPLE)) {
                validateXMLSchemaQuestionnaire(xmlPath);
                validateXMLSchemaData(xmlPath);
            }
        } catch (Exception e) {
            ve = new ValidateException("Error during validation : " + e.getMessage());
        } finally {
            if (fis != null) fis.close();
            if (fr != null) fr.close();
            if (xmlEncoding != null) xmlEncoding.close();
        }
        if (ve != null) throw ve;
        logger.log(Level.INFO, "{} validate with {}", xmlPath, model);
        return true;
    }

    /**
     * Validate an XML file by XSD validator
     *
     * @param xsdPath xsd path
     * @param xmlPath xml path
     * @return true if XML is valid
     * @throws IOException
     * @throws BatchException
     */
    public static void validateXMLSchemaData(String xmlPath) throws ValidateException, IOException {
        try {
            // Get Questionnaire.xml to create Data.xsd
            Document questionnaireXml = getQuestionnaireXml(xmlPath);
            if (!questionnaireXml.hasChildNodes()) {
                return;
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(questionnaireXml), new StreamResult(Files.createTempFile("tempQuestionnaire", ".xml").toFile()));
            XMLLunaticToXSDData xmlLunaticToXSDData = new XMLLunaticToXSDData();
            // Creating Data.xsd
            questionnaireXml.setDocumentURI(Constants.TEMP_FOLDER + "/tempQuestionnaire.xml");
            File fileQuestionnaireXml = new File(questionnaireXml.getDocumentURI());
            File xsdFile = xmlLunaticToXSDData.transform(fileQuestionnaireXml);

            // Validating Data schema with Data.xsd
            validateData(xmlPath, transformer, xsdFile);

            try {
                Files.delete(Paths.get(fileQuestionnaireXml.getAbsolutePath()));
            } catch (IOException e) {
                logger.warn("Unable to delete tempQuestionnaire.xml temporary file :"+e.getMessage());

            }
        } catch (Exception e) {
            throw new ValidateException("Error during validation of data : " + e.getMessage());
        }
        logger.log(Level.INFO, "{} validate with generated Data.xsd", xmlPath);
    }

    /**
     * Validate an XML file by XSD validator
     *
     * @param xsdPath xsd path
     * @param xmlPath xml path
     * @return true if XML is valid
     * @throws IOException
     * @throws BatchException
     */
    public static void validateXMLSchemaQuestionnaire(String xmlPath) throws ValidateException, IOException {
        try {
            Document questionnaireXml = getQuestionnaireXml(xmlPath);
            if (!questionnaireXml.hasChildNodes()) {
                logger.log(Level.INFO, "No questionnaire tag to validate");
                return;
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(questionnaireXml), new StreamResult(Files.createTempFile("tempQuestionnaire", ".xml").toFile()));
            questionnaireXml.setDocumentURI(Constants.TEMP_FOLDER + "/tempQuestionnaire.xml");
            SchemaValidator sv = new SchemaValidator(Modele.HIERARCHICAL);
            File fileQuestionnaireXml = new File(questionnaireXml.getDocumentURI());
            sv.validateFile(fileQuestionnaireXml);
			try {
				Files.delete(Paths.get(fileQuestionnaireXml.getAbsolutePath()));
			} catch (IOException e) {
                logger.warn("Unable to delete tempQuestionnaire.xml temporary file :"+e.getMessage());

			}
            logger.log(Level.INFO, "{} validate with generated LunaticModel.xsd", xmlPath);

        } catch (Exception e) {
            throw new ValidateException("Error during validation of questionnaire : " + e.getMessage());
        }

    }

    /**
     * Retreive questionnaire to xml format from sample
     *
     * @param xmlPath
     * @return
     * @throws ValidateException
     */
    public static Document getQuestionnaireXml(String xmlPath) throws ValidateException {
        try {
            Document questionaireXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            NodeList lstNodeQuestionnaire = getXmlNodeFile(xmlPath, "Questionnaire");
            if (lstNodeQuestionnaire != null && lstNodeQuestionnaire.getLength() != 0) {
                for (int i = 0; i < lstNodeQuestionnaire.getLength(); i++) {
                    Node node = lstNodeQuestionnaire.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Node copyNode = questionaireXml.importNode(node, true);
                        questionaireXml.appendChild(copyNode);
                    }
                }
            }
            return questionaireXml;
        } catch (Exception e) {
            throw new ValidateException("Error during validation of data : " + e.getMessage());
        }
    }

    /**
     * Validate Data for Each survey-unit
     *
     * @param xmlPath
     * @param transformer
     * @param xsdFile
     * @throws ValidateException
     * @throws IOException
     */
    public static void validateData(String xmlPath, Transformer transformer, File xsdFile) throws ValidateException, IOException {
        FileInputStream fis = null;
        try {
            Document dataXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            NodeList lstNodeData = getXmlNodeFile(xmlPath, "Data");
            if (lstNodeData != null && lstNodeData.getLength() != 0) {
                for (int i = 0; i < lstNodeData.getLength(); i++) {
                    Node node = lstNodeData.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Node copyNode = dataXml.importNode(node, true);
                        dataXml.appendChild(copyNode);
                        transformer.transform(new DOMSource(dataXml), new StreamResult(Files.createTempFile("tempData", ".xml").toFile()));

                        //Validation data
                        SchemaFactory factoryData = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                        Schema schemaData = factoryData.newSchema(xsdFile);
                        factoryData.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                        factoryData.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                        Validator validatorData = schemaData.newValidator();
                        validatorData.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                        validatorData.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                        fis = new FileInputStream(new File(Constants.TEMP_FOLDER + "/tempData.xml"));
                        validatorData.validate(new StreamSource(fis));
                        fis.close();
                        dataXml.removeChild(copyNode);
                    }
                }
            }
            File fileDataXml = new File(Constants.TEMP_FOLDER + "/tempData.xml");
            if (fileDataXml.exists()) {
				try {
					Files.delete(Paths.get(fileDataXml.getAbsolutePath()));
				} catch (IOException e) {
					logger.warn("Unable to delete tempData.xml temporary file :"+e.getMessage());
				}
            }
        } catch (Exception e) {
            if (fis != null) fis.close();
            throw new ValidateException("Error during validation of data : " + e.getMessage());
        }
    }

    /**
     * get campaign in xml file
     *
     * @param isloading
     * @param fileName  the file name
     * @return opertion
     * @throws IOException
     * @throws ValidateException
     * @throws BatchException
     * @throws DOMException
     */
    public static List<Nomenclature> xmlToNomenclature(boolean isloading, String fileName, String pathToJson) throws IOException, ValidateException, BatchException {
        List<Nomenclature> lstNomenclature = new ArrayList<>();
        NodeList lstNodeNomenclature = getXmlNodeFile(fileName, "Nomenclature");
        if (lstNodeNomenclature != null) {
            for (int itr = 0; itr < lstNodeNomenclature.getLength(); itr++) {
                Node nodeNomenclature = lstNodeNomenclature.item(itr);
                if (nodeNomenclature.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) nodeNomenclature;
                    Nomenclature nomenclature = new Nomenclature();
                    nomenclature.setId(e.getElementsByTagName("Id").item(0).getTextContent());
                    nomenclature.setLabel(e.getElementsByTagName("Label").item(0).getTextContent());
                    if (isloading) {
                        if (!PathUtils.isFileExist(pathToJson + e.getElementsByTagName("Id").item(0).getTextContent() + Constants.JSON)) {
                            throw new BatchException(String.format("File %s.json doesn't exist", e.getElementsByTagName("Id").item(0).getTextContent()));
                        }
                        nomenclature.setValue(JSONUtils.jsonFileToJsonObject(pathToJson + nomenclature.getId() + Constants.JSON));
                    }
                    lstNomenclature.add(nomenclature);
                }
            }
        }
        return lstNomenclature;
    }

    /**
     * get campaign in xml file
     *
     * @param fileName the file name
     * @return opertion
     * @throws IOException
     */
    public static Campaign xmlToCampaign(String fileName) throws IOException {
        Campaign campaign = null;
        NodeList lstNodeCampaign = getXmlNodeFile(fileName, "Campaign");
        if (lstNodeCampaign != null && lstNodeCampaign.getLength() == 1) {
            for (int itr = 0; itr < lstNodeCampaign.getLength(); itr++) {
                Node nodeCampaign = lstNodeCampaign.item(itr);
                if (nodeCampaign.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) nodeCampaign;
                    campaign = new Campaign();
                    campaign.setId(e.getElementsByTagName("Id").item(0).getTextContent());
                    if (e.getElementsByTagName("Label").item(0) != null) {
                        campaign.setLabel(e.getElementsByTagName("Label").item(0).getTextContent());
                    }
                }
            }
        } else {
            logger.log(Level.ERROR, "Log error => more than one campaign in file");
        }
        return campaign;
    }

    /**
     * get questionnaire model in xml file
     *
     * @param fileName the file name
     * @return questionnaire model
     * @throws IOException
     */
    public static QuestionnaireModel xmlToQuestionnaireModel(String fileName) throws IOException {
        QuestionnaireModel questionnaireModel = null;
        NodeList lstNodeQuestionnaireModel = getXmlNodeFile(fileName, "QuestionnaireModelId");
        if (lstNodeQuestionnaireModel != null && lstNodeQuestionnaireModel.getLength() == 1) {
            for (int itr = 0; itr < lstNodeQuestionnaireModel.getLength(); itr++) {
                Node nodeQuestionnaireModel = lstNodeQuestionnaireModel.item(itr);
                if (nodeQuestionnaireModel.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) nodeQuestionnaireModel;
                    questionnaireModel = new QuestionnaireModel();
                    questionnaireModel.setId(e.getElementsByTagName("Id").item(0).getTextContent());
                    questionnaireModel.setLabel(e.getElementsByTagName("Label").item(0).getTextContent());
                }
            }
        } else {
            logger.log(Level.ERROR, "Log error => morethan one questionnaire model in file");
        }
        return questionnaireModel;
    }

    /**
     * get nomenclature in xml file
     *
     * @param fileName the file name
     * @return nomenclature
     * @throws IOException
     */
    public static List<String> xmlToNomenclatures(String fileName) throws IOException {
        List<String> nomenclatures = new ArrayList<>();
        NodeList lstNodeNomenclature = getXmlNodeFile(fileName, "NomenclatureId");
        if (lstNodeNomenclature != null) {
            for (int itr = 0; itr < lstNodeNomenclature.getLength(); itr++) {
                Node nodeQuestionnaireModel = lstNodeNomenclature.item(itr);
                if (nodeQuestionnaireModel.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) nodeQuestionnaireModel;
                    nomenclatures.add(e.getTextContent());
                }
            }
        }
        return nomenclatures;
    }

    /**
     * get nomenclature in xml file
     *
     * @param fileName the file name
     * @return nomenclature
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Personalization xmlToPersonalization(Node personalization) {
        JSONArray jsonArray = new JSONArray();
        if (personalization != null && (personalization.getChildNodes().getLength() > 1 ||
                (personalization.getChildNodes().getLength() == 1 && personalization.getChildNodes().item(0).getNodeType() == Node.ELEMENT_NODE))) {
            for (int i = 0; i < personalization.getChildNodes().getLength(); i++) {
                Node node = personalization.getChildNodes().item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("Variable")) {
                    JSONObject jsonTemp = new JSONObject();
                    Element e = (Element) node;
                    jsonTemp.put("name", e.getElementsByTagName("Name").item(0).getTextContent());
                    jsonTemp.put("value", e.getElementsByTagName("Value").item(0).getTextContent());
                    jsonArray.add(jsonTemp);
                }
            }
            return new Personalization(UUID.randomUUID(), jsonArray, null);
        }
        return null;
    }

    /**
     * This method takes an XML Node and parse it to create
     * a JSONObject associated using the Lunatic library
     *
     * @param fileName
     * @param nodeName
     * @return JSONObject
     * @throws Exception
     */
    public static JSONObject lunaticXmlToJSON(String fileName, String nodeName) throws Exception {
        NodeList questionnaireXml = getXmlNodeFile(fileName, nodeName);
        Node nodeQuestionnaireXml = null;
        String questionnaireString = null;
        JSONCleaner jsonCleaner = new JSONCleaner();
        if (questionnaireXml != null) {
            for (int itr = 0; itr < questionnaireXml.getLength(); itr++) {
                nodeQuestionnaireXml = questionnaireXml.item(itr);
                questionnaireString = nodeToString(nodeQuestionnaireXml);
            }
        }
        if (StringUtils.isBlank(questionnaireString)) {
            return new JSONObject();
        }
        XMLLunaticToXMLLunaticFlatTranslator translator = new XMLLunaticToXMLLunaticFlatTranslator();
        XMLLunaticFlatToJSONLunaticFlatTranslator translator2 = new XMLLunaticFlatToJSONLunaticFlatTranslator();
        String stringQuestionnaire = jsonCleaner.clean(translator2.translate(translator.generate(questionnaireString)));
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(stringQuestionnaire);

    }

    /**
     * Transform xml to data from SurveyUnit Element
     *
     * @param surveyUnit
     * @return
     * @throws Exception
     */
    public static Data xmlToData(Element surveyUnit) throws Exception {
        if (surveyUnit.getElementsByTagName("Data").item(0) != null) {
            return new Data(UUID.randomUUID(), dataXmlToJSON(surveyUnit.getElementsByTagName("Data").item(0)), null);
        }
        throw new BatchException("Surevy unit with id : " + surveyUnit.getElementsByTagName("Id").item(0).getTextContent() + " does not have data");
    }

    /**
     * This method takes an XML Node and parse it to create
     * a JSONObject associated using the Data xsd
     *
     * @param fileName
     * @param nodeName
     * @return JSONObject
     * @throws Exception
     */
    public static JSONObject dataXmlToJSON(Node data) throws Exception {
        if (data != null && (data.getChildNodes().getLength() > 1 ||
                (data.getChildNodes().getLength() == 1 && data.getChildNodes().item(0).getNodeType() == Node.ELEMENT_NODE))) {
            Document dataXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            XMLLunaticDataToJSON xmlLunaticDataToJSON = new XMLLunaticDataToJSON();
            File fileDataXml = Files.createTempFile(Constants.TEMP_FOLDER, "tempFileData", ".xml").toFile();
            Node copyNode = dataXml.importNode(data, true);
            dataXml.appendChild(copyNode);
            transformer.transform(new DOMSource(dataXml), new StreamResult(fileDataXml));
            File dataJson = xmlLunaticDataToJSON.transform(fileDataXml);
            String stringdata = dataJson.toString();

            try {
                Files.delete(Paths.get(fileDataXml.getAbsolutePath()));
            } catch (IOException e) {
                logger.warn("Unable to delete tempFileData.xml temporary file :"+e.getMessage());
            }
            JSONParser parser = new JSONParser();
            FileReader dataFileReader = new FileReader(stringdata);
            JSONObject dataJsonObject = (JSONObject) parser.parse(dataFileReader);
            dataFileReader.close();
            try {
                Files.delete(Paths.get(dataJson.getAbsolutePath()));
            } catch (IOException e) {
                logger.warn("Unable to delete xml2json temporary file :"+e.getMessage());
            }
            return dataJsonObject;
        }
        return new JSONObject();
    }

    /**
     * This method convert a Node Object in String
     *
     * @param node
     * @return String of the node Object passed in parameter
     * @throws TransformerException
     */
    private static String nodeToString(Node node) throws TransformerException {
        StringWriter sw = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));
        return sw.toString();
    }

    /**
     * This method takes a document and an id in entry, it removes the surveyUnit node
     * identified by its id in the document
     *
     * @param doc
     * @param xmlId
     * @return StreamResult
     * @throws XPathExpressionException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public static StreamResult removeSurveyUnitNode(Document doc, String xmlId) throws XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile("//SurveyUnit[Id = \"" + xmlId + "\"]");
        Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
        Node prevElement = node.getPreviousSibling();
        if (prevElement != null && prevElement.getNodeType() == Node.TEXT_NODE && prevElement.getNodeValue().trim().length() == 0) {
            node.getParentNode().removeChild(prevElement);
        }
        Node parent = node.getParentNode();
        parent.removeChild(node);
        DOMSource domSource = new DOMSource(doc);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter sw = new StringWriter();
        StreamResult sr = new StreamResult(sw);
        transformer.transform(domSource, sr);
        return sr;
    }

    /**
     * This method update an error file for the sample steps. It writes all the objects
     * in the sample.xml that created errors
     *
     * @param sr
     * @param fileName
     */
    public static void updateSampleFileErrorList(StreamResult sr, String fileName) {
        // writing to file
        File fileNew = new File(fileName);
        try (FileOutputStream fop = new FileOutputStream(fileNew)) {
            // if file doesnt exists, then create it
            if (!fileNew.exists() && !fileNew.createNewFile()) {
                logger.log(Level.ERROR, "Failed to create file %s", fileName);
            }
            // get the content in bytes
            String xmlString = sr.getWriter().toString();
            byte[] contentInBytes = xmlString.getBytes();
            fop.write(contentInBytes);
            fop.flush();
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage());
        }
    }

    /***
     * This method crates a Sample object from a file.
     * @param fileName
     * @return Sample object
     * @throws Exception
     */
    public Sample createSample(String fileName) throws Exception {
        Sample sample = new Sample();
        sample.setFileName(fileName);
        sample.setCampaign(xmlToCampaign(sample.getFileName()));
        sample.setSurveyUnits(xmlToSurveyUnits(sample.getFileName(), sample.getCampaign()));

        return sample;
    }

    /**
     * get survey unit in xml file
     *
     * @param fileName the file name
     * @return survey unit
     * @throws Exception
     */
    public List<SurveyUnit> xmlToSurveyUnits(String fileName, Campaign campaign) throws Exception {
        logger.info("Using {} as root temporary directory", System.getProperty("java.io.tmpdir"));
        List<SurveyUnit> surveyUnits = new ArrayList<>();
        List<String> surveyUnitsIds = new ArrayList<>();
        Set<QuestionnaireModel> questionnaireModels = new HashSet<>();
        NodeList lstNodeSurveyUnit = getXmlNodeFile(fileName, "SurveyUnit");
        if (lstNodeSurveyUnit != null) {
            for (int itru = 0; itru < lstNodeSurveyUnit.getLength(); itru++) {
                Node nodeSurveyUnit = lstNodeSurveyUnit.item(itru);
                if (nodeSurveyUnit.getNodeType() == Node.ELEMENT_NODE) {
                    Element surveyUnit = (Element) nodeSurveyUnit;
                    if (surveyUnit.getElementsByTagName("Id").item(0).getTextContent() != null &&
                            !surveyUnitsIds.contains(surveyUnit.getElementsByTagName("Id").item(0).getTextContent())) {
                        QuestionnaireModel questionnaireModel = new QuestionnaireModel();
                        try {
                            questionnaireModel = questionnaireModelDao.findById(surveyUnit.getElementsByTagName("QuestionnaireModelId").item(0).getTextContent());
                        } catch (Exception e) {
                            throw new ValidateException(String.format("Error on find questionnaire by id %s : %s", surveyUnit.getElementsByTagName("QuestionnaireModelId").item(0).getTextContent(), e.getMessage()));
                        }
                        questionnaireModel.setCampaignId(campaign.getId());
                        questionnaireModels.add(questionnaireModel);
                        surveyUnits.add(
                                new SurveyUnit(surveyUnit.getElementsByTagName("Id").item(0).getTextContent(),
                                        campaign,
                                        questionnaireModel,
                                        new Comment(UUID.randomUUID()),
                                        xmlToData(surveyUnit),
                                        new StateData(),
                                        xmlToPersonalization(surveyUnit.getElementsByTagName("Personalization").item(0))));
                        surveyUnitsIds.add(surveyUnit.getElementsByTagName("Id").item(0).getTextContent());
                    } else {
                        throw new BatchException("Surevy unit with id : " + surveyUnit.getElementsByTagName("Id").item(0).getTextContent() + " is duplicated");
                    }
                }
            }
            if (!questionnaireModels.isEmpty()) {
                campaign.setQuestionnaireModels(List.copyOf(questionnaireModels));
            }
        }
        return surveyUnits;
    }

}
