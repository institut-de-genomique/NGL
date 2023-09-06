package controllers.instruments.io.cng.miseqqcmode;

import java.io.ByteArrayInputStream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector.SelectorParseException;
import org.mockito.internal.util.collections.Sets;

import au.com.bytecode.opencsv.CSVReader;
import controllers.instruments.io.utils.AbstractMultiInput;
import controllers.instruments.io.utils.InputHelper;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import validation.ContextValidation;

/**
 *  NGL-3194: Nouveau fonctionnement le logiciel LRM produit plusieurs fichiers
 *  importer les fichiers CSV (1 par puit)
 *  utiliser 2 valeurs globales du fichier "Summary.htm"
 *  
 *  NGL-3685: nouveau fichier CSV (interop_summary.txt) à charger et avec 4 nouvelles valeurs à lire
 *  NGL-3969: gérer 2 types de fichiers "non standard" ... TODO
 */
public class MultiInput extends AbstractMultiInput {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Output.class);
	
	/**
	 * Summary.htm key
	 */
	private static final String HTML_KEY = "Summary.htm";
	
	/**
	 * copy_completed key
	 */
	private static final String COMPLETED_KEY = "copy_completed.txt";
	
	/**
	 * interop_summary.txt key
	 */
	private static final String INTEROP_KEY = "interop_summary.txt";
	
	/**
	 * Sum of all summary.csv's clustersLRM values
	 */
	private Double sumClustersLRM = 0D;
	
	/**
	 * List of runnables to set clustersPercentageLRM on post processing
	 */
	private List<Runnable> clustersPercentageRunnables = new ArrayList<>();
	
	/**
	 * Summary.htm values
	 */
	private HtmlValues htmlValues = null;
	
	/**
	 * interop CSV values
	 */
	private InteropValues interopValues = null;
	
	
	/**
	 * partial file name: position pattern
	 */
	private Pattern pattern = Pattern.compile("^([A-Z0-9]+)-([A-Z]\\d\\d?)_.+\\.summary\\.csv");
	
	/**
	 * @return the htmlValues
	 */
	public HtmlValues getHtmlValues() {
		return htmlValues;
	}

	/**
	 * @param htmlValues the htmlValues to set
	 */
	public void setHtmlValues(HtmlValues htmlValues) {
		this.htmlValues = htmlValues;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getGlobalFileKeys() {
		return Sets.newSet(HTML_KEY, COMPLETED_KEY, INTEROP_KEY);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, PropertyFileValue> getGlobalFilesMap(Experiment experiment, List<PropertyFileValue> pfvs, ContextValidation contextValidation) {
		Map<String, PropertyFileValue> map = new HashMap<>();
		for(PropertyFileValue pfv : pfvs) {
			if(HTML_KEY.equals(pfv.fullname)) {
				if(map.containsKey(HTML_KEY)) {
					contextValidation.addError("Erreurs fichier", "experiments.msg.import.multi.mutliple.occurences", HTML_KEY);
				} else {
					logger.info(pfv.fullname);
					map.put(HTML_KEY, pfv);
				}
			}
			
			if(COMPLETED_KEY.equals(pfv.fullname)) {
				if(map.containsKey(COMPLETED_KEY)) {
					contextValidation.addError("Erreurs fichier", "experiments.msg.import.multi.mutliple.occurences", COMPLETED_KEY);
				} else {
					logger.info(pfv.fullname);
					map.put(COMPLETED_KEY, pfv);
				}
			} 
			
			if(INTEROP_KEY.equals(pfv.fullname)) {
				if(map.containsKey(INTEROP_KEY)) {
					contextValidation.addError("Erreurs fichier", "experiments.msg.import.multi.mutliple.occurences", INTEROP_KEY);
				} else {
					logger.info(pfv.fullname);
					map.put(INTEROP_KEY, pfv);
				}
			}
		}
		return map;
	}
	
	private boolean patternMatch(Matcher matcher, PropertyFileValue pfv, ContextValidation contextValidation) {
		if(matcher.find()) {
			return true;
		} else {
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.multi.parsing", String.valueOf(pfv.fullname));
			return false;
		}
	}
	
	private boolean isValidPlate(Experiment experiment, String plate, PropertyFileValue pfv, ContextValidation contextValidation) {
		if(experiment.inputContainerSupportCodes.contains(plate)) {
			return true;
		} else {
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.multi.wrong.plate", String.valueOf(plate), String.valueOf(pfv.fullname), String.valueOf(experiment.code));
			return false;
		}
	}
	
	private boolean isValidPlatePosition(String position, PropertyFileValue pfv, ContextValidation contextValidation) {
		if(InputHelper.isPlatePosition(contextValidation, position, 96, 0)) {
			return true;
		} else {
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.multi.parsing.position", String.valueOf(position), String.valueOf(pfv.fullname));
			return false;
		}
	}
	
	private void addPositionFileToMap(String position, PropertyFileValue pfv, Map<String, PropertyFileValue> map, ContextValidation contextValidation) {
		if(map.containsKey(position)) {
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.multi.double.position", String.valueOf(position), String.valueOf(pfv.fullname), String.valueOf(map.get(position).fullname));
		} else {
			map.put(position, pfv);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, PropertyFileValue> getPositionsMap(Experiment experiment, List<PropertyFileValue> pfvs, ContextValidation contextValidation){
		Map<String, PropertyFileValue> map = new HashMap<>();
		for(PropertyFileValue pfv : pfvs) {
			Matcher matcher = pattern.matcher(pfv.fullname);
			if(this.patternMatch(matcher, pfv, contextValidation)) {
				String plate = matcher.group(1);
				String position = matcher.group(2);
				if(this.isValidPlate(experiment, plate, pfv, contextValidation) && this.isValidPlatePosition(position, pfv, contextValidation)) {
					this.addPositionFileToMap(position, pfv, map, contextValidation);
				}
			}
		}		
		return map;
	}
	
	private Elements getElementList(Element parent, String elementList) {
		try {
			return parent.select(elementList);
		} catch(SelectorParseException spexception) {
			throw new IllegalStateException("Cannot get HTML element '" + elementList + "' in file " + HTML_KEY, spexception);
		}
	}
	
	private Element getElementInList(Elements elementList, int elementIndex) {
		try {
			return elementList.get(elementIndex);
		} catch(IndexOutOfBoundsException ioobexception) {
			throw new IllegalStateException("No index " + elementIndex + " for HTML element '" + elementList.first().normalName() + "' in file " + HTML_KEY, ioobexception);
		}
	}
	
	private Element getElementInList(Element parent, String elementList, int elementIndex) {
		return this.getElementInList(this.getElementList(parent, elementList), elementIndex);
	}
	
	private int getIntFromColumn(Elements cols, int index, String property) {
		String value = this.getElementInList(cols, index).text();
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfexception) {
			throw new IllegalStateException("Cannot get Integer value from '" + value +"' for property '" + property + "' in file " + HTML_KEY, nfexception);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Experiment importGlobalFile(Experiment experiment, PropertyFileValue pfv, String globalFileKey, ContextValidation contextValidation) throws Exception {
		if(HTML_KEY.equals(globalFileKey)) {
			
			if (htmlValues != null) {
				throw new IllegalStateException("HtmlValues should be null on " + HTML_KEY + " parsing!");
			}
			
			try (InputStream is = new ByteArrayInputStream(pfv.byteValue());) {
				// parse document
				Document doc = Jsoup.parse(is, null, StringUtils.EMPTY);
				// get table
				Element table = this.getElementInList(doc, "table", 1);
		        	//get first row
		        	Element row = this.getElementInList(table, "tr", 1);
		        	// get columns
		            Elements cols = this.getElementList(row, "td");
	            
	            int clusters = this.getIntFromColumn(cols, 0, "clusters");
	            int clustersPF = this.getIntFromColumn(cols, 1, "clustersPF");
	            htmlValues = new HtmlValues(clusters, clustersPF);
	            
	        	return experiment;
	        }
		} else if(COMPLETED_KEY.equals(globalFileKey)) {
			return experiment;
		}
		// NGL-3685 ajouter ce else
		else if(INTEROP_KEY.equals(globalFileKey)) {
		
			if (interopValues != null) {
				throw new IllegalStateException("Interop Values should be null on " + INTEROP_KEY + " parsing!");
			}
			
			try (InputStream is = new ByteArrayInputStream(pfv.byteValue());
				CSVReader reader = new CSVReader(new InputStreamReader(is))){
				
					List<String[]> lines = reader.readAll(); 
					// hardcodé !!!
					Double runR1Q30PercentageLRMValue = getDoubleFromLine(lines, 3, 6);  // ligne 4 
					Double runR2Q30PercentageLRMValue = getDoubleFromLine(lines, 4, 6);  // ligne 5
					Double runR3Q30PercentageLRMValue = getDoubleFromLine(lines, 5, 6);  // ligne 6
					Double runR4Q30PercentageLRMValue = getDoubleFromLine(lines, 6, 6);  // ligne 7
					
					interopValues = new InteropValues(runR1Q30PercentageLRMValue, runR2Q30PercentageLRMValue,runR3Q30PercentageLRMValue,runR4Q30PercentageLRMValue);
					
			} catch(IllegalStateException e) {
				throw new IllegalStateException("File " + pfv.fullname + " : " + e.getMessage(), e);
			}
				
			return experiment;
		
		}
		
		throw new IllegalStateException("Unknown Global file: " + String.valueOf(globalFileKey));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void missingGlobalFile(String globalKey, ContextValidation contextValidation) {
		if(COMPLETED_KEY.equals(globalKey)) {
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.multi.missing.completed");
		} else {
			super.missingGlobalFile(globalKey, contextValidation);
		}
	}
	
	private Double getPercentageFromLine(List<String[]> lines, int index) {
		String percentage = getStringFromLine(lines, index).replace("%", StringUtils.EMPTY);
		if(percentage.equals("NaN")) return 0D;
		try {
			return Double.parseDouble(percentage);
		} catch (NumberFormatException e) {
			throw new IllegalStateException("Cannot get Double value from '" + String.valueOf(percentage) + "' at line: " + (index + 1), e);
		}
	}
	
	private Integer getIntFromLine(List<String[]> lines, int index) {
		String intValue = getStringFromLine(lines, index);
		try {
			return Integer.parseInt(intValue);
		} catch (NumberFormatException e) {
			throw new IllegalStateException("Cannot get Integer value from '" + String.valueOf(intValue) + "' at line: " + (index + 1), e);
		}
	}
	
	private Double getDoubleFromLine(List<String[]> lines, int index) {
		String doubleValue = getStringFromLine(lines, index);
		try {
			return Double.parseDouble(doubleValue);
		} catch (NumberFormatException e) {
			throw new IllegalStateException("Cannot get Double value from '" + String.valueOf(doubleValue) + "' at line: " + (index + 1), e);
		}
	}
	
	// 28/01/2022 FDS => pour NGL-3685; ajout param pos
	private Double getDoubleFromLine(List<String[]> lines, int index, int pos) {
		String doubleValue = getStringFromLine(lines, index, pos);
		try {
			return Double.parseDouble(doubleValue);
		} catch (NumberFormatException e) {
			throw new IllegalStateException("Cannot get Double value from '" + String.valueOf(doubleValue) + "' in position: "+ (pos + 1) +" at line: " + (index + 1), e);
		}
	}
	
	
	// NOTE FDS: cette méthode récupère UNIQUEMENT le 2ème champ de la ligne index
	private String getStringFromLine(List<String[]> lines, int index) {
		try {
			String line = getLine(lines, index)[1];
			if(line == null) {
				throw new IllegalStateException("Get empty value at line: " + (index + 1));
			} return line;
		} catch(IndexOutOfBoundsException e) {
			throw new IllegalStateException("Cannot get value from column 2 at line: " + (index + 1), e);
		}
	}
	
	// 28/01/2022 FDS récupérer le champ pos dans la ligne index => pour NGL-3685
	private String getStringFromLine(List<String[]> lines, int index, int pos) {
		try {
			String line = getLine(lines, index)[pos];
			if(line == null) {
				throw new IllegalStateException("Get empty value at line: " + (index + 1));
			} return line;
		} catch(IndexOutOfBoundsException e) {
			throw new IllegalStateException("Cannot get value from column: "+ (pos + 1)+ " at line: " + (index + 1), e);
		}
	}
	
	private String[] getLine(List<String[]> lines, int index) {
		try {
			return lines.get(index);
		} catch(IndexOutOfBoundsException e) {
			throw new IllegalStateException("Cannot get line at index: " + (index + 1), e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Experiment importPartialFile(Experiment experiment, PropertyFileValue pfv, InputContainerUsed icu,
			ContextValidation contextValidation) throws Exception {
		
			if(this.htmlValues == null) {
				throw new IllegalStateException("HtmlValues should not be null on csv parsing!");
			}
			
			if(this.interopValues == null) {
				throw new IllegalStateException("InteropValues should not be null on csv parsing!");
			}
			
			try (InputStream is = new ByteArrayInputStream(pfv.byteValue());
				    CSVReader reader = new CSVReader(new InputStreamReader(is))) {
				
					//logger.debug("reading file "+ pfv.fullname);
					
					// valeur globales au run du fichier HTML => recopiées dans chaque icu
				
					PropertySingleValue runPFclustersLRM = getOrCreatePSV(icu, "RunPFclustersLRM");
					runPFclustersLRM.value = htmlValues.getClusters();
					
					PropertySingleValue runPFclustersPercentageLRM = getOrCreatePSV(icu, "RunPFclustersPercentageLRM");
					runPFclustersPercentageLRM.value = htmlValues.getClustersPFpercent();
					
					// NGL 3685 4 valeurs globales au run du interop file => recopiées dans chaque icu 
						
					PropertySingleValue runR1Q30PercentageLRM = getOrCreatePSV(icu, "RunR1Q30PercentageLRM");
					runR1Q30PercentageLRM.value = interopValues.getRunR1Q30PercentageLRM();
					
					PropertySingleValue runR2Q30PercentageLRM = getOrCreatePSV(icu, "RunR2Q30PercentageLRM");
					runR2Q30PercentageLRM.value = interopValues.getRunR2Q30PercentageLRM();
					
					PropertySingleValue runR3Q30PercentageLRM = getOrCreatePSV(icu, "RunR3Q30PercentageLRM");
					runR3Q30PercentageLRM.value = interopValues.getRunR3Q30PercentageLRM();
					
					PropertySingleValue runR4Q30PercentageLRM = getOrCreatePSV(icu, "RunR4Q30PercentageLRM");
					runR4Q30PercentageLRM.value = interopValues.getRunR4Q30PercentageLRM();
					
					List<String[]> lines = reader.readAll(); 
					
					PropertySingleValue clustersLRM = getOrCreatePSV(icu, "clustersLRM");
					Integer clustersLRMValue = getIntFromLine(lines, 9);
					clustersLRM.value = clustersLRMValue;
					
					// NGL-3669 / reouvert : déplacer diversityLRM dans la partie commune
					PropertySingleValue diversityLRM = getOrCreatePSV(icu, "diversityLRM");
					Double diversityLRMValue= getDoubleFromLine(lines, 8);
					diversityLRM.value = diversityLRMValue;
					
					//logger.debug("Total PF reads="+clustersLRMValue+ "; Diversity="+diversityLRMValue);
					
					// NGL-3669 ajouter diversityLRMValue
					if(isIncompletePartialFile(clustersLRMValue, diversityLRMValue )) {
						importIncompletePartialFile(lines, icu, contextValidation);
					} else {
						importCommonPartialFile(lines, clustersLRMValue, icu, contextValidation);
					}
					
				} catch(IllegalStateException e) {
					throw new IllegalStateException("File " + pfv.fullname + " : " + e.getMessage(), e);
				}
		
		return experiment;
	}

	// NGL-3669 / reouvert : 2 cas sont considérés comme donnant des fichiers incomplets
	private boolean isIncompletePartialFile(Integer clustersLRMValue, Double diversityLRMValue) {
	    return ( clustersLRMValue == 0 || diversityLRMValue == 0D );
	}
	
	private void importIncompletePartialFile(List<String[]> lines, InputContainerUsed icu, 
            ContextValidation contextValidation) throws Exception {
	    PropertySingleValue clustersPercentageLRM = getOrCreatePSV(icu, "clustersPercentageLRM");
	    clustersPercentageLRM.value = 0D;
	    
	    PropertySingleValue r1Q30PercentageLRM = getOrCreatePSV(icu, "R1Q30PercentageLRM");
        r1Q30PercentageLRM.value = getPercentageFromLine(lines, 19);
        
        PropertySingleValue r2Q30PercentageLRM = getOrCreatePSV(icu, "R2Q30PercentageLRM");
        r2Q30PercentageLRM.value = getPercentageFromLine(lines, 20);
        
        PropertySingleValue r1MismatchPercentageLRM = getOrCreatePSV(icu, "R1MismatchPercentageLRM");
        r1MismatchPercentageLRM.value = getPercentageFromLine(lines, 22);
        
        PropertySingleValue r2MismatchPercentageLRM = getOrCreatePSV(icu, "R2MismatchPercentageLRM");
        r2MismatchPercentageLRM.value = getPercentageFromLine(lines, 23);
        
        PropertySingleValue r1AlignedPercentageLRM = getOrCreatePSV(icu, "R1AlignedPercentageLRM");
        r1AlignedPercentageLRM.value = getPercentageFromLine(lines, 12);
        
        PropertySingleValue r2AlignedPercentageLRM = getOrCreatePSV(icu, "R2AlignedPercentageLRM");
        r2AlignedPercentageLRM.value = getPercentageFromLine(lines, 13);
        
        PropertySingleValue medianInsertSizeLRM = getOrCreatePSV(icu, "medianInsertSizeLRM");
        medianInsertSizeLRM.value = 0;
        
        PropertySingleValue SDInsertSizeLRM = getOrCreatePSV(icu, "SDInsertSizeLRM");
        SDInsertSizeLRM.value = 0;
        
        PropertySingleValue minInsertSizeLRM = getOrCreatePSV(icu, "minInsertSizeLRM");
        minInsertSizeLRM.value = 0;
        
        PropertySingleValue maxInsertSizeLRM = getOrCreatePSV(icu, "maxInsertSizeLRM");
        maxInsertSizeLRM.value = 0;
        
        /* NGL-3669 / reouvert : remonté en partie commune
        PropertySingleValue diversityLRM = getOrCreatePSV(icu, "diversityLRM");
        diversityLRM.value = getDoubleFromLine(lines, 8);
        */
        
        PropertySingleValue duplicatesPercentageLRM = getOrCreatePSV(icu, "duplicatesPercentageLRM");
        duplicatesPercentageLRM.value = getPercentageFromLine(lines, 14);
        
        PropertySingleValue genomeLRM = getOrCreatePSV(icu, "genomeLRM");
        genomeLRM.value = getStringFromLine(lines, 6);
	}
	
	private void importCommonPartialFile(List<String[]> lines, Integer clustersLRMValue, InputContainerUsed icu, 
	        ContextValidation contextValidation) throws Exception {
        // sum all clustersLRM values
        this.sumClustersLRM += clustersLRMValue;
        
        PropertySingleValue clustersPercentageLRM = getOrCreatePSV(icu, "clustersPercentageLRM");
        // register a runnable to call later (when sumClustersLRM will be complete)
        clustersPercentageRunnables.add(() -> {
            
            clustersPercentageLRM.value = BigDecimal
                    .valueOf((clustersLRMValue*100D)/sumClustersLRM)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue();   
            
        });                         
        
        PropertySingleValue r1Q30PercentageLRM = getOrCreatePSV(icu, "R1Q30PercentageLRM");
        r1Q30PercentageLRM.value = getPercentageFromLine(lines, 19);
        
        PropertySingleValue r2Q30PercentageLRM = getOrCreatePSV(icu, "R2Q30PercentageLRM");
        r2Q30PercentageLRM.value = getPercentageFromLine(lines, 20);
        
        PropertySingleValue r1MismatchPercentageLRM = getOrCreatePSV(icu, "R1MismatchPercentageLRM");
        r1MismatchPercentageLRM.value = getPercentageFromLine(lines, 25);
        
        PropertySingleValue r2MismatchPercentageLRM = getOrCreatePSV(icu, "R2MismatchPercentageLRM");
        r2MismatchPercentageLRM.value = getPercentageFromLine(lines, 26);
        
        PropertySingleValue r1AlignedPercentageLRM = getOrCreatePSV(icu, "R1AlignedPercentageLRM");
        r1AlignedPercentageLRM.value = getPercentageFromLine(lines, 12);
        
        PropertySingleValue r2AlignedPercentageLRM = getOrCreatePSV(icu, "R2AlignedPercentageLRM");
        r2AlignedPercentageLRM.value = getPercentageFromLine(lines, 13);
        
        PropertySingleValue medianInsertSizeLRM = getOrCreatePSV(icu, "medianInsertSizeLRM");
        medianInsertSizeLRM.value = getIntFromLine(lines, 28);
        
        PropertySingleValue SDInsertSizeLRM = getOrCreatePSV(icu, "SDInsertSizeLRM");
        SDInsertSizeLRM.value = getIntFromLine(lines, 31);
        
        PropertySingleValue minInsertSizeLRM = getOrCreatePSV(icu, "minInsertSizeLRM");
        minInsertSizeLRM.value = getIntFromLine(lines, 29);
        
        PropertySingleValue maxInsertSizeLRM = getOrCreatePSV(icu, "maxInsertSizeLRM");
        maxInsertSizeLRM.value = getIntFromLine(lines, 30);
        
        /* NGL-3669 / reouvert : remonté en partie commune
        PropertySingleValue diversityLRM = getOrCreatePSV(icu, "diversityLRM");
        diversityLRM.value = getDoubleFromLine(lines, 8);
        */
        
        PropertySingleValue duplicatesPercentageLRM = getOrCreatePSV(icu, "duplicatesPercentageLRM");
        duplicatesPercentageLRM.value = getPercentageFromLine(lines, 14);
        
        PropertySingleValue genomeLRM = getOrCreatePSV(icu, "genomeLRM");
        genomeLRM.value = getStringFromLine(lines, 6);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Experiment postProcessing(Experiment experiment, ContextValidation contextValidation) throws Exception {
		// call each runnable to set clustersPercentageLRM value
		clustersPercentageRunnables.forEach(Runnable::run);
		return experiment;
	}
	
	public static final class HtmlValues {
		
		private final int clusters;
		
		private final int clustersPF;
		
		private final double clustersPFpercent;
		
		public HtmlValues(int clusters, int clustersPF) {
			this.clusters = clusters;
			this.clustersPF = clustersPF;
			this.clustersPFpercent = BigDecimal
					.valueOf(((double) clustersPF/(double) clusters)*100D)
					.setScale(1, RoundingMode.HALF_UP).doubleValue();
		}

		public int getClusters() {
			return clusters;
		}

		public int getClustersPF() {
			return clustersPF;
		}

		public double getClustersPFpercent() {
			return clustersPFpercent;
		}
		
	}
	
	/* NGL-3685 interop file
	 * 
	 */
	
	public static final class InteropValues {
		
		private final double RunR1Q30PercentageLRM;
		
		private final double RunR2Q30PercentageLRM;
		
		private final double RunR3Q30PercentageLRM;
		
		private final double RunR4Q30PercentageLRM;
		
		public InteropValues(double RunR1Q30PercentageLRM, double RunR2Q30PercentageLRM, double RunR3Q30PercentageLRM, double RunR4Q30PercentageLRM) {
			this.RunR1Q30PercentageLRM = RunR1Q30PercentageLRM;
			this.RunR2Q30PercentageLRM = RunR2Q30PercentageLRM;
			this.RunR3Q30PercentageLRM = RunR3Q30PercentageLRM;
			this.RunR4Q30PercentageLRM = RunR4Q30PercentageLRM;
		}

		public double getRunR1Q30PercentageLRM() {
			return RunR1Q30PercentageLRM;
		}

		public double getRunR2Q30PercentageLRM() {
			return RunR2Q30PercentageLRM;
		}
		
		public double getRunR3Q30PercentageLRM() {
			return RunR3Q30PercentageLRM;
		}
		public double getRunR4Q30PercentageLRM() {
			return RunR4Q30PercentageLRM;
		}
		
	}
	
}
