package scripts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;
import org.mongojack.DBUpdate.Builder;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import io.jsonwebtoken.lang.Collections;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.dao.PropertyDefinitionDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertyImgValue;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import play.Logger;
import play.Logger.ALogger;
import validation.ContextValidation;

/**
 * Pour le moment le script ne gère que les images de niveau Run ou ReaSet, pour les niveaus Analyse et Lane une mise à jour du script sera nécessaire.
 * 
 * Le script nécessite dans le body un fichier xlsx avec une entrée par ligne, et en colonnes:
 * 
 * CODE DE L OBJET | CODE DU TRAITEMENT | CODE DE LA PROPRIETE | CHEMIN DE L IMAGE | LARGEUR DE L IMAGE | HAUTEUR DE L IMAGE
 * 
 * Le paramètre header permet d'indiquer si la première ligne du xlsx est une ligne de header ou une ligne à traiter
 * Le paramètre jira contient le numéro du ticket JIRA à renseigner en commentaire technique
 * 
 * @author aprotat
 *
 */
public class UpdateTreatmentImages extends ScriptWithArgsAndExcelBody<UpdateTreatmentImages.Args> {
	
	public static class Args {
		public String headers;
	}
	
	static final int CODE_INDEX 		= 0;
	static final int TREATMENT_INDEX 	= 1;
	static final int PROPERTY_INDEX 	= 2;
	static final int IMAGE_PATH_INDEX 	= 3;
	static final int IMAGE_WIDTH_INDEX 	= 4;
	static final int IMAGE_HEIGHT_INDEX = 5;
	
	static final Map<String, List<Level>> MAP_LEVELS = new HashMap<>();
	
	static final ALogger logger = Logger.of(UpdateTreatmentImages.class);

	@Override
	public void execute(UpdateTreatmentImages.Args args, XSSFWorkbook workbook) throws Exception {
		logger.info("Start script " + this.getClass().getCanonicalName());
		Iterator<Row> rows = getRows(args, workbook);
		ContextValidation contextValidation = ContextValidation.createUpdateContext("ngl-support");
		Map<String, TreatmentHolder<?>> treatmentHolders = new HashMap<>();
		rows.forEachRemaining(this.processRow(contextValidation, treatmentHolders));
		validateAll(treatmentHolders.values(), contextValidation);
		if(contextValidation.hasErrors()) {
			contextValidation.displayErrors(logger);
		} else {
			saveWorkflow(treatmentHolders);
		}
		logger.info("End script " + this.getClass().getCanonicalName());
	}

	private Iterator<Row> getRows(UpdateTreatmentImages.Args args, XSSFWorkbook workbook) {
		Iterator<Row> rows = workbook.getSheetAt(0).rowIterator();
		if(Boolean.parseBoolean(args.headers)) { rows.next(); }
		return rows;
	}
	
	private Consumer<Row> processRow(ContextValidation contextValidation, Map<String, TreatmentHolder<?>> treatmentHolders) {
		return (Row row) -> {
			RowProcessor processor = loadRowProcessor(row, contextValidation);
			processor.loadDBObject(treatmentHolders);
			processor.loadImage();
			processor.updateTreatmentImage();
			processor.register(treatmentHolders);
		};
	}
	
	private RowProcessor loadRowProcessor(Row row, ContextValidation contextValidation) {
		String code 				= getStringFrom(row, CODE_INDEX);
		String treatmentCode 		= getStringFrom(row, TREATMENT_INDEX);
		String treatmentProperty 	= getStringFrom(row, PROPERTY_INDEX);
		String imgPath 				= getStringFrom(row, IMAGE_PATH_INDEX);
		Integer imgWidth 			= getIntegerFrom(row, IMAGE_WIDTH_INDEX);
		Integer imgHeight 			= getIntegerFrom(row, IMAGE_HEIGHT_INDEX);
		logger.info("Row " + row.getRowNum() + " : read from xlsx");
		return new RowProcessor(contextValidation, row.getRowNum(), code, treatmentCode, treatmentProperty, imgPath, imgWidth, imgHeight);
	}
	
	private Cell getCell(Row row, int index) {
		Cell cell = row.getCell(index);
		if(cell == null) {
			throw new RuntimeException("Row "+ row.getRowNum() + " column " + index + " is not defined");
		}
		return cell;
	}

	private String getStringFrom(Row row, int index) {
		String value =  getCell(row, index).getStringCellValue();
		if(value == null) {
			throw new RuntimeException("Row "+ row.getRowNum() + " column " + index + " is null");
		}
		return value;
	}
	
	private Integer getIntegerFrom(Row row, int index) {
		Cell cell = getCell(row, index);
		Integer value = cell.getCellType() == Cell.CELL_TYPE_NUMERIC ? (int) cell.getNumericCellValue() : Integer.parseInt(getStringFrom(row, index));
		if(value == null || value == 0) {
			throw new RuntimeException("Row "+ row.getRowNum() + " column " + index + " is null");
		}
		return value;
	}
	
	private void validateAll(Collection<TreatmentHolder<?>> holders, ContextValidation contextValidation) {
		logger.info("validate updated treatments");
		holders.forEach((TreatmentHolder<?> holder) -> {
			if(holder == null) {  throw new RuntimeException("Error : null object in treatmentHolders"); }
			String key = holder.object.code + ".treatments";
			contextValidation.addKeyToRootKeyName(key);
			holder.validateTreatments(contextValidation);
			contextValidation.removeKeyFromRootKeyName(key);
		});
	}
	
	private void saveWorkflow(Map<String, TreatmentHolder<?>> treatmentHolders) {
		treatmentHolders.values().forEach(TreatmentHolder::save);
	}
	
	public static List<Level> getLevelsFrom(String propertyCode) {
		if(MAP_LEVELS.containsKey(propertyCode)) { return MAP_LEVELS.get(propertyCode); }
		PropertyDefinitionDAO dao = PropertyDefinition.find.get();
		PropertyDefinition propertyDefinition = dao.findByCode(propertyCode);
		if(propertyDefinition == null) { throw new RuntimeException("Invalid property code : '" + propertyCode + "'"); }
		if(propertyDefinition.levels == null) { throw new RuntimeException("No levels for property code : '" + propertyCode + "'"); }
		MAP_LEVELS.put(propertyCode, propertyDefinition.levels);
		return propertyDefinition.levels;
	}
	
	public static class RowProcessor {
		
		private final ContextValidation contextValidation;
		private final int rowNumber;
		private final TreatmentProcessor treatmentProcessor;
		private final ImageLoader imageLoader;
		
		private RowProcessor(ContextValidation contextValidation, int rowNumber, String code, String treatmentCode, String treatmentProperty, String imagePath, int width, int height) {
			this.contextValidation = contextValidation;
			this.rowNumber = rowNumber;
			this.treatmentProcessor = new TreatmentProcessor(code, treatmentCode, treatmentProperty);
			this.imageLoader = new ImageLoader(imagePath, width, height);
		}

		public void loadDBObject(Map<String, TreatmentHolder<?>> treatmentHolders) {
			logger.info("Row " + this.rowNumber + " : load " + this.rowTreatment());
			if(this.treatmentProcessor.isLoaded()) {
				throw new RuntimeException(this.rowTreatment() + " is already loaded!");
			}
			this.treatmentProcessor.load(contextValidation, rowNumber, treatmentHolders);
		}
		
		public void loadImage() {
			logger.info("Row " + this.rowNumber + " : load " + this.rowImage());
			if(this.imageLoader.isLoaded()) {
				throw new RuntimeException(this.rowImage() + " is already loaded!");
			}
			this.imageLoader.load(contextValidation, rowNumber);
		}
		
		public void updateTreatmentImage() {
			if(!this.treatmentProcessor.isLoaded() || !this.imageLoader.isLoaded()) { return; }
			logger.info("Row " + this.rowNumber + " : update " + this.rowTreatment() + " with " + this.rowImage());
			PropertyValue imagePropertyValue = this.imageLoader.getImagePropertyValue();
			this.treatmentProcessor.update(contextValidation, rowNumber, imagePropertyValue);
		}
		
		public void register(Map<String, TreatmentHolder<?>> treatmentHolders) {
			if(!this.treatmentProcessor.isLoaded()) { return; }
			logger.info("Row " + this.rowNumber + " : register " + this.rowTreatment());
			treatmentHolders.put(treatmentProcessor.code, treatmentProcessor.getTreatmentHolder());
		}
		
		private String rowTreatment() {
			return this.treatmentProcessor.toString();
		}
		
		private String rowImage() {
			return this.imageLoader.toString();
		}
		
	}
	
	private static class TreatmentProcessor {
		
		static final ALogger logger = Logger.of(TreatmentProcessor.class);
		
		private final String code;
		private final String treatmentCode;
		private final String treatmentProperty;
		private TreatmentHolder<?> treatmentHolder;
		
		private  TreatmentProcessor(String code, String treatmentCode, String treatmentProperty) {
			this.code = code;
			this.treatmentCode = treatmentCode;
			this.treatmentProperty = treatmentProperty;
		}
		
		public String toString() {
			return "Object (" + String.valueOf(this.code) + ") Treatment (" + String.valueOf(this.treatmentCode)  + ") Property (" + String.valueOf(this.treatmentProperty) + ")";
		}
		
		public boolean isLoaded() {
			return this.treatmentHolder != null;
		}
		
		public TreatmentHolder<?> getTreatmentHolder() {
			return this.treatmentHolder;
		}
		
		public void load(ContextValidation contextValidation, int rowNumber, Map<String, TreatmentHolder<?>> treatmentHolders) {
			try {
				if(treatmentHolders.containsKey(code)) { 
					reuseDBObject(treatmentHolders);
				} else {
					loadByObjectType(contextValidation, rowNumber);
				}
			} catch (Exception e) {
				contextValidation.addError("line." + rowNumber, e.getMessage());
			}
		}
		
		private void loadByObjectType(ContextValidation contextValidation, int rowNumber) {
			String loadType = getLoadType(contextValidation, rowNumber);
			if(loadType == null) { throw new RuntimeException("Null level for " + this.toString()); }
			switch(loadType) {
			case "Run": loadTreatmentFrom(contextValidation, rowNumber, InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class); break;
			case "ReadSet": loadTreatmentFrom(contextValidation, rowNumber, InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class); break;
			default: throw new RuntimeException("Invalide level code '" + loadType + "' for " + this.toString());
			}
		}
		
		private String getLoadType(ContextValidation contextValidation, int rowNumber) {
			List<Level> levels = getLevelsFrom(treatmentProperty);
			if(Collections.isEmpty(levels)) { throw new RuntimeException("No levels for property '" + treatmentProperty + "'"); }
			Level firstLevel = levels.get(0);
			return firstLevel.code;
		}
		
		private Query getQuery() {
			return DBQuery.and(DBQuery.is("code", this.code), DBQuery.exists("treatments."+treatmentCode));
		}
		
		private <T extends DBObject> void loadTreatmentFrom(ContextValidation contextValidation, int rowNumber,  String collection, Class<T> clazz) {
			logger.info("Using class " + clazz.getName() + " to read object " + code);
			T t = MongoDBDAO.findOne(collection, (Class<T>) clazz, getQuery());
			if(t == null) { throw new RuntimeException("No Object '" + code + "' for type '" + clazz.getName() + "' and treatment '" + this.treatmentCode + "'"); }
			this.treatmentHolder = this.getTreatmentHolderFor(clazz, t);
		}
		
		private void reuseDBObject(Map<String, TreatmentHolder<?>> treatmentHolders) {
			TreatmentHolder<?> holder = treatmentHolders.get(code);
			this.treatmentHolder = holder;
		}
		
		@SuppressWarnings("unchecked")
		private <T extends DBObject> TreatmentHolder<T> getTreatmentHolderFor(Class<T> clazz, T t) {
			if(Run.class.equals(clazz)) {
				logger.info("Use object " + code + " as Run");
				return (TreatmentHolder<T>) new RunTreatmentHolder((Run) t);
			} else if (ReadSet.class.equals(clazz)) {
				logger.info("Use object " + code + " as ReadSet");
				return (TreatmentHolder<T>) new ReadSetTreatmentHolder((ReadSet) t);
			} else {
				throw new RuntimeException("Class " + clazz.getName() + " not a Run or ReadSet for " + this.toString());
			}
		}
		
		public void update(ContextValidation contextValidation, int rowNumber, PropertyValue imagePropertyValue) {
			try {
				this.treatmentHolder.updateTreatment(treatmentCode, treatmentProperty, imagePropertyValue);
			} catch (Exception e) {
				contextValidation.addError("line." + rowNumber, e.getMessage());
			}
		}
		
	}
	
	private static class ImageLoader {
		
		static final ALogger logger = Logger.of(ImageLoader.class);
		
		private final String imagePath;
		private final int width;
		private final int height;
		private PropertyValue propertyValue;
		
		public ImageLoader(String imagePath, int width, int height) {
			this.imagePath = imagePath;
			this.width = width;
			this.height = height;
		}
		
		public String toString() {
			return "Image (" + String.valueOf(this.imagePath) + ")";
		}
		
		public boolean isLoaded() {
			return this.propertyValue != null;
		}
		
		public void load(ContextValidation contextValidation, int rowNumber) {
			File imageFile = new File(this.imagePath);
			try {
				this.propertyValue = new PropertyImgValue(imageFile, width, height);
			} catch (IOException e) {
				logger.error(e.getMessage());
				contextValidation.addErrorKey("line." + rowNumber, "Image Path", imagePath);
			}
		}
		
		public PropertyValue getImagePropertyValue() {
			return this.propertyValue;
		}
		
	}
	
	private static class RunTreatmentHolder extends TreatmentHolder<Run> {

		public RunTreatmentHolder(Run object) {
			super(object);
		}

		@Override
		public Map<String, Treatment> getTreatments() {
			return this.object.treatments;
		}

		@Override
		protected void validateTreatment(ContextValidation contextValidation, Treatment treatment) {
			treatment.validate(contextValidation, this.object);
		}

		@Override
		protected TraceInformation getObjectTraceInfo() {
			return this.object.traceInformation;
		}

		@Override
		protected void save(Builder build) {
			MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.is("code", this.object.code), build);
		}
		
	}
	
	private static class ReadSetTreatmentHolder extends TreatmentHolder<ReadSet> {

		public ReadSetTreatmentHolder(ReadSet object) {
			super(object);
		}

		@Override
		public Map<String, Treatment> getTreatments() {
			return this.object.treatments;
		}

		@Override
		protected void validateTreatment(ContextValidation contextValidation, Treatment treatment) {
			treatment.validate(contextValidation, this.object);
		}

		@Override
		protected TraceInformation getObjectTraceInfo() {
			return this.object.traceInformation;
		}

		@Override
		protected void save(DBUpdate.Builder build) {
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", this.object.code), build);
		}
		
	}
	
	private static abstract class TreatmentHolder<T extends DBObject> {
		
		protected final T object;
		private final List<String> updatedTreatmentCodes = new ArrayList<>();
		
		public TreatmentHolder(T object) {
			this.object = object;
		}

		public abstract Map<String, Treatment> getTreatments();
		
		protected abstract void validateTreatment(ContextValidation contextValidation, Treatment treatment);
		
		protected abstract TraceInformation getObjectTraceInfo();
		
		protected abstract void save(DBUpdate.Builder build);
		
		public void updateTreatment(String treatmentCode, String treatmentProperty, PropertyValue propertyValue) {
			Treatment treatment = this.getTreatment(treatmentCode);
			Map<String, PropertyValue> properties = getTreatmentProperties(treatment, treatmentProperty);
			if(properties != null && properties.containsKey(treatmentProperty)) {
				properties.put(treatmentProperty, propertyValue);
				this.getObjectTraceInfo().modificationStamp("ngl-support");
				this.updatedTreatmentCodes.add(treatmentCode);
			} else {
				throw new RuntimeException("No property '" + treatmentProperty + "' to update in Treatment " + treatmentCode);
			}
		}
		
		private Map<String, PropertyValue> getTreatmentProperties(Treatment treatment, String treatmentProperty) {
			Map<String, Map<String, PropertyValue>> results = treatment.results();
			List<Level> levels = getLevelsFrom(treatmentProperty);
			if(levels.size() != 2) {
				throw new RuntimeException("Treatment Property '" + treatmentProperty + "' doesn't have 2 levels");
			}
			String secondaryLevel = levels.get(1).name.toLowerCase();
			return results.get(secondaryLevel);
		}
		
		private Treatment getTreatment(String code) {
			Map<String, Treatment> treatments = this.getTreatments();
			if(treatments.containsKey(code)) {
				return treatments.get(code);
			} else {
				throw new RuntimeException("No treatment '" + code + "' in object " + object.code);
			}
		}
		
		public void validateTreatments(ContextValidation contextValidation) {
			Map<String, Treatment> treatments = getTreatments();
			updatedTreatmentCodes.forEach((String code) -> {
				if(treatments.containsKey(code)) {
					Treatment treatment = treatments.get(code);
					this.validateTreatment(contextValidation, treatment);
				} else {
					throw new RuntimeException("No treatment '" + code + "' in object " + object.code);
				}
			});
		}
		
		public void save() {
			DBUpdate.Builder build = DBUpdate.set("traceInformation", this.getObjectTraceInfo());
			for(String treatmentCode : this.updatedTreatmentCodes) {
				build = build.set("treatments." + treatmentCode, getTreatment(treatmentCode));
			}
			this.save(build);
		}
		
	}

}
