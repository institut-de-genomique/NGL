package controllers.plates.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import controllers.TPLCommonController;
import fr.cea.ig.ngl.NGLApplication;
import lims.cns.dao.LimsManipDAO;
import lims.models.Well;
import models.laboratory.common.instance.property.PropertyFileValue;
import play.api.modules.spring.Spring;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import validation.ContextValidation;
import validation.utils.ValidationHelper;

public class Plates extends TPLCommonController {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Plates.class);
	
	private final NGLApplication          app;
	private final Form<PropertyFileValue> fileForm;	
	
//	@Inject
//	public Plates(NGLContext ctx) {
//		this.ctx = ctx;	
//		fileForm = ctx.form(PropertyFileValue.class);
//	}
	
	@Inject
	public Plates(NGLApplication app) {
		this.app = app;	
		fileForm = app.form(PropertyFileValue.class);
	}

	// @BodyParser.Of(value = BodyParser.Json.class, maxLength = 5000 * 1024)
	@BodyParser.Of(value = fr.cea.ig.play.IGBodyParsers.Json5MB.class)
	public Result importFile(Integer emnco){
		Form<PropertyFileValue> filledForm = getFilledForm(fileForm,PropertyFileValue.class);
		PropertyFileValue pfv = filledForm.get();
		if (pfv != null) {
//			ContextValidation contextValidation = new ContextValidation(getCurrentUser(), filledForm.errors());
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(getCurrentUser(), filledForm);
			if (!contextValidation.hasErrors()) {
				try {
					List<Well> wells = importFile(emnco, pfv, contextValidation);
					if (!contextValidation.hasErrors()) {	
						return ok(Json.toJson(wells));
					}
				} catch(Throwable e) {
					e.printStackTrace();
					contextValidation.addError("Error :", e.getMessage()+"");
				}
			}
			// return badRequest(filledForm.errors-AsJson());
			return badRequest(app.errorsAsJson(contextValidation.getErrors()));
		} else {
			return badRequest("missing file");
		}		
	}


	private List<Well> importFile(Integer emnco, 
			                      PropertyFileValue pfv,
			                      ContextValidation contextValidation)  throws Exception {
		logger.debug("Load plate files");
//		InputStream is = new ByteArrayInputStream(pfv.value);
//		InputStream is = new ByteArrayInputStream(pfv.getValue());
		InputStream is = new ByteArrayInputStream(pfv.byteValue());
		Workbook wb = WorkbookFactory.create(is);
		Sheet sheet = wb.getSheetAt(0);//case sensitive??
		if (sheet == null ) {
			contextValidation.addError("Erreurs fichier", "Pas d'onglet 0");
			return null;
		}
		List<Well> wells = new ArrayList<>(0);
		Set<String> wellPosition = new TreeSet<>();
		Set<String> nomManips = new TreeSet<>();
		for (int i = 1; i <= 97; i++) {
			if (sheet.getRow(i) != null && sheet.getRow(i).getCell(0) != null && sheet.getRow(i).getCell(0).getCellType()!=Cell.CELL_TYPE_BLANK) {
				String nomManip = getStringValue(sheet.getRow(i).getCell(0));
			//if(nomManip != null){
			
				String line = getStringValue(sheet.getRow(i).getCell(1));
				String column = getStringValue(sheet.getRow(i).getCell(2));
				if(null == column) column = getNumericValue(sheet.getRow(i).getCell(2)).intValue()+"";
				
				if (ValidationHelper.validateNotEmpty(contextValidation, nomManip, "nom manip : ligne = "+i)
						&& ValidationHelper.validateNotEmpty(contextValidation, line, "Ligne : ligne = "+i)
						&& ValidationHelper.validateNotEmpty(contextValidation, column, "Colonne : ligne = "+i)
						&& isPlatePosition(contextValidation, line+column, 96, i, wellPosition)
						&& isNotAlreadyPresent(contextValidation,nomManip, nomManips,i)){
					
					LimsManipDAO  limsManipDAO = Spring.getBeanOfType(LimsManipDAO.class);
					Well well = limsManipDAO.getWell(nomManip);
					if (ValidationHelper.validateNotEmpty(contextValidation, well, "manip non trouvé : ligne = "+i)
							&& isSameEmnco(contextValidation, well, emnco, i)
							&& isNotInsideAPlate(contextValidation, well, i)) {
						well.x = Integer.valueOf(column);
						well.y = line;
						wells.add(well);
					}					
				}
			} else {
				logger.debug("Line "+i+" empty, manip name empty !! so ignored");
			}
			//verifier que c'est une position definie et valide 	
		}
		return wells;
	}

	private boolean isNotAlreadyPresent(ContextValidation contextValidation, String nomManip, Set<String> nomManips, int lineNum) {
		if (nomManips.contains(nomManip)) {
			contextValidation.addError("Erreurs fichier", "Nom manip en double : "+nomManip+". Ligne"+lineNum);
			return false;
		} else {
			nomManips.add(nomManip);
			return true;
		}
	}

	private boolean isNotInsideAPlate(ContextValidation contextValidation, Well well, int i) {
		if (well.x != null && well.y != null){
			contextValidation.addError("Erreurs fichier", "Etape manip déjà sur une plaque : ligne = "+i);
			return false;
		} else {
			return true;
		}
	}

	private boolean isSameEmnco(ContextValidation contextValidation, Well well, Integer emnco, int i) {
		if (!well.typeCode.equals(emnco)) {
			contextValidation.addError("Erreurs fichier", "Etape manip et étape choisie différente : ligne = "+i);
			return false;
		} else {
			return true;
		}
	}
	
	protected static String getStringValue(Cell cell) {
		//test cell existance  first !!
		//a string can also be a formula
		if (cell != null && ( Cell.CELL_TYPE_STRING == cell.getCellType() || Cell.CELL_TYPE_FORMULA == cell.getCellType())){
			return cell.getStringCellValue();
		} else {
			return null;
		}
	}
	
	protected static Double getNumericValue(Cell cell) {
		//test cell existance ffirst !!
		if (cell != null &&  (Cell.CELL_TYPE_NUMERIC == cell.getCellType() || Cell.CELL_TYPE_FORMULA == cell.getCellType())) {
			return cell.getNumericCellValue();
		} else if (cell != null && Cell.CELL_TYPE_STRING == cell.getCellType()) {
			return Double.valueOf(cell.getStringCellValue());
		} else {
			return null;
		}
	}
	
	public static boolean isPlatePosition(ContextValidation contextValidation, String position, int plFormat, int lineNum, Set<String> wellPosition){
		if ((position.length() < 2) || (position.length() > 3 )) {
			contextValidation.addError("Erreurs fichier", "Position puit inconnu : "+position+". Ligne"+lineNum);
			return false;
		}
		String row = position.substring(0,1);
		String column = position.substring(1);
		//Logger.info("DEBUG...row:"+ row + " -column:"+ column );
		
		int col = Integer.parseInt(column); // et si la string ne correspond pas a un nombre ???
		if (wellPosition.contains(position)) {
			contextValidation.addError("Erreurs fichier", "Position puit en double : "+position+". Ligne"+lineNum);			
		} else {
			wellPosition.add(position);
		}
		if (plFormat == 96) {
			if (row.matches("[A-H]") && (col>=1 && col<=12)){
				return true;
			} else { 
				contextValidation.addError("Erreurs fichier", "Position puit en dehors de ce qui est possible : "+position+". Ligne"+lineNum);
				return false; 
			}
		} else if (plFormat == 384) {
	    	if (row.matches("[A-P]") &&  (col>=1 && col <=24)){
				return true;
			} else { 
				contextValidation.addError("Erreurs fichier", "Position puit en dehors de ce qui est possible : "+position+". Ligne"+lineNum);
				return false;
			}
		} else {
			// unsupported plate format
			return false;
		}
	}
	
}
