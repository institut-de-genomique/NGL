package services.ncbi;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

// import play.Logger;

public class NCBITaxon {
	
	private static final play.Logger.ALogger logger = play.Logger.of(NCBITaxon.class);
	
	private Document doc;
	public  String   code;
	public  boolean  error; 
	public  boolean  exists; 
	
	public NCBITaxon() {
	}
	
	public NCBITaxon(String code, Document doc) {
		this.code   = code;
		this.doc    = doc;
		this.error  = getIsError();
		this.exists = getIsTaxon();
	}
	
	private String getValue(String expression) throws XPathExpressionException {
		XPath xPath =  XPathFactory.newInstance().newXPath();
		//String expression = "/TaxaSet/Taxon/ScientificName";
		//read a string value
		if (doc != null) {
			String value = xPath.compile(expression).evaluate(doc);
			if (StringUtils.isNotBlank(value)) {
				return value;
			}
		}
		return null;				
	}
		
//	private Boolean getIsTaxon() {
//		try {
//			String value = getValue("/TaxaSet/Taxon");
//			if (StringUtils.isNotBlank(value)) {
//				return Boolean.TRUE;
//			}
//		} catch (XPathExpressionException e) {
//			logger.error("Error Xpath /TaxaSet/Taxon "+e.getMessage());
//		}
//		return Boolean.FALSE;
//	}
	
	private boolean getIsTaxon() {
		try {
			String value = getValue("/TaxaSet/Taxon");
			return StringUtils.isNotBlank(value);
		} catch (XPathExpressionException e) {
			logger.error("Error Xpath /TaxaSet/Taxon "+e.getMessage());
		}
		return false;
	}

//	private Boolean getIsError() {
//		try {
//			String value = getValue("/eFetchResult/ERROR");
//			if (StringUtils.isNotBlank(value)) {
//				return Boolean.TRUE;
//			}
//		} catch (XPathExpressionException e) {
//			logger.error("Error Xpath /eFetchResult/ERROR "+e.getMessage());
//		}
//		return Boolean.FALSE;
//	}

	private boolean getIsError() {
		try {
			String value = getValue("/eFetchResult/ERROR");
			return StringUtils.isNotBlank(value);
		} catch (XPathExpressionException e) {
			logger.error("Error Xpath /eFetchResult/ERROR " + e.getMessage());
		}
		return false;
	}

	public String getScientificName() {
		return getResult("/TaxaSet/Taxon/ScientificName");
	}

	public String getLineage() {
		return getResult("/TaxaSet/Taxon/Lineage");
	}

	private String getResult(String xpath)  {
		try {
			if (!error && exists) {
				return getValue(xpath);
			} else if (!error && !exists) {
				return "Taxon code " + code + " does not exist";
			} else if (error) {
				return "Taxon code " + code + " is in error";
			} else {
				return null;
			}
		} catch (XPathExpressionException e) {
			logger.error("Error Xpath " + xpath + " " + e.getMessage());
		}
		return null;
	}
	
}
