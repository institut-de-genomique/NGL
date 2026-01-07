package scripts.protocols;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class UpdateProtocolPropertyValue extends ScriptWithExcelBody {
    
    private final ExperimentsAPI expAPI;
    
    @Inject
    public UpdateProtocolPropertyValue(ExperimentsAPI expAPI) {
        this.expAPI = expAPI;
    }
    
    @Override
    public void execute(XSSFWorkbook workbook) throws Exception { 
        getLogger().error("Start UpdateProtocolPropertyValue.");

        List<String> failedCodes = new ArrayList<>();

        getLogger().error("Update protocol with cascading properties.");      
        updateProtocol(workbook.getSheetAt(0), true);

        try {
            getLogger().error("Update experiments with cascading properties.");
            failedCodes = updateProtocol(workbook.getSheetAt(1), false);
        } catch (IllegalArgumentException e) {
            // Il y a un seul onglet.
        }

        if (failedCodes.size() != 0) {
            getLogger().error("Some experiments have not been updated.");
            getLogger().error(failedCodes.toString());
        } else {
            getLogger().error("All experiments have been updated.");
        }

        getLogger().error("End UpdateProtocolPropertyValue.");
    }

    private List<String> updateProtocol(XSSFSheet sheet, boolean cascade) {
        List<String> failedCodes = new ArrayList<>();
        final String user = "ngl-support";
        
        sheet.rowIterator().forEachRemaining(row -> {
            if(row.getRowNum() == 0) return; 
            
            String protoCode = row.getCell(0).getStringCellValue();

            String propertyName = row.getCell(1).getStringCellValue();
            String newPropertyValue = row.getCell(2).getStringCellValue();
            
            Protocol protocol = MongoDBDAO.findByCode(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class, protoCode);
            
            if (protocol != null) {
                 for (Map.Entry<String, PropertyValue> entry : protocol.properties.entrySet()) {
                    if (propertyName.equals(entry.getKey())) {
                        ((PropertySingleValue) protocol.properties.get(entry.getKey())).value = newPropertyValue;

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
                    }
                }          
            } else {
                getLogger().error("Le protocol '" + protoCode + "' n'existe pas.");
            }
        });

        return failedCodes;
    }
}
