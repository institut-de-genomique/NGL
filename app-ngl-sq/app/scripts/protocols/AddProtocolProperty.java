package scripts.protocols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import fr.cea.ig.ngl.dao.experiments.ExperimentsAPI;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.protocol.instance.Protocol;
import models.utils.InstanceConstants;
import validation.ContextValidation;

public class AddProtocolProperty extends ScriptWithExcelBody {
    
    private final ExperimentsAPI expAPI;
    
    @Inject
    public AddProtocolProperty(ExperimentsAPI expAPI) {
        this.expAPI = expAPI;
    }
    
    @Override
    public void execute(XSSFWorkbook workbook) throws Exception {   
        getLogger().error("Start AddProtocolProperty.");

        List<String> failedCodes = new ArrayList<>();

        getLogger().error("Update protocol with cascading properties.");
        updateProtocol(workbook.getSheetAt(0), true);

        try {
            getLogger().error("Update protocol without cascading properties.");
            updateProtocol(workbook.getSheetAt(1), false);
        } catch (IllegalArgumentException e) {
            // Il y a un seul onglet.
        }

        if (failedCodes.size() != 0) {
            getLogger().error("Some experiments have not been updated.");
            getLogger().error(failedCodes.toString());
        } else {
            getLogger().error("All experiments have been updated.");
        }

        getLogger().error("End AddProtocolProperty.");
    }

    private List<String> updateProtocol(XSSFSheet sheet, boolean cascade) {
        List<String> failedCodes = new ArrayList<>();
        final String user = "ngl-support";
        
        sheet.rowIterator().forEachRemaining(row -> {
            if(row.getRowNum() == 0) return; 
            
            String protoCode = row.getCell(0).getStringCellValue();

            String propName = row.getCell(1).getStringCellValue();
            String propValue = row.getCell(2).getStringCellValue();
            
            List<Protocol> protocols = MongoDBDAO.find(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class, DBQuery.is("code", protoCode)).toList();
            if(protocols.isEmpty()){
                  getLogger().error("Le protocol '" + protoCode + "' n'existe pas.");
            }
            else{
                Protocol protocol =protocols.get(0);

                if (protocol != null) {
                    if(protocol.properties == null){
                        protocol.properties = new HashMap<String, PropertyValue>();
                    }
                    if (!protocol.properties.containsKey(propName)) {
                        // Mise à jour de la valeur de la propriété du protocole.
                        protocol.properties.put(propName, new PropertySingleValue(propValue));

                        ContextValidation ctxValidate = ContextValidation.createUpdateContext(user);
                        protocol.validate(ctxValidate);

                        if (!ctxValidate.hasErrors()) {
                            MongoDBDAO.update(InstanceConstants.PROTOCOL_COLL_NAME, protocol);

                            // On met à jour toutes les expériences qui ont cette propriété via une cascade.

                            List<Experiment> expList = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("protocolCode", protoCode)).toList();

                            for (int i = 0; i < expList.size(); i++) {
                                Experiment exp = expList.get(i);

                                try {
                                    if (!cascade) {
                                        expAPI.update(exp, user);
                                    } else {
                                        expAPI.updateWithCascadingContentProperties(exp, user);
                                    }
                                } catch (Exception e) {
                                    getLogger().error("Experiment '" + exp.code + "' update failed.");
                                    getLogger().error(e.getMessage(), e);

                                    failedCodes.add(exp.code);
                                }  
                            }    
                        } else {
                            getLogger().error("Le protocole '" + protoCode + "' n'est pas valide. Erreur(s) : " + ctxValidate.getErrors());
                        }  
                    } else {
                        getLogger().error("Le protocole '" + protoCode + "' a déjà la propriété '" + propValue + "'.");
                    }   
                } 
                }
            
            
        });

        return failedCodes;
    }
}
