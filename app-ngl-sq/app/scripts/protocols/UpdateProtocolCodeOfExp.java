package scripts.protocols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.experiments.ExperimentsAPI;
import fr.cea.ig.ngl.dao.protocols.ProtocolsAPI;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.protocol.instance.Protocol;
import models.utils.InstanceConstants;
import validation.ContextValidation;

public class UpdateProtocolCodeOfExp extends ScriptWithExcelBody {
    
    private final ProtocolsAPI protoAPI;
    private final ExperimentsAPI expAPI;
    
    @Inject
    public UpdateProtocolCodeOfExp(ProtocolsAPI protoAPI, ExperimentsAPI expAPI) {
        this.protoAPI = protoAPI;
        this.expAPI   = expAPI;
    }
    
    @Override
    public void execute(XSSFWorkbook workbook) throws Exception {
        getLogger().error("Start UpdateProtocolCodeOfExp.");

        Map<String, String> protocols = new HashMap<>();
        this.protoAPI.all().forEach(p -> protocols.put(p.name, p.code));
        
        List<String> failedCodes = new ArrayList<>();

        getLogger().error("Update experiments with cascading properties.");
        failedCodes = updateExperiment(workbook.getSheetAt(0), protocols, true);
        
        try {
            getLogger().error("Update experiments without cascading properties.");
            failedCodes = updateExperiment(workbook.getSheetAt(1), protocols, false);
        } catch (IllegalArgumentException e) {
            // Il y a un seul onglet.
        }

        if (failedCodes.size() != 0) {
            getLogger().error("Some experiments have not been updated.");
            getLogger().error(failedCodes.toString());
        } else {
            getLogger().error("All experiments have been updated.");
        }

        getLogger().error("End UpdateProtocolCodeOfExp.");
    }

    private List<String> updateExperiment(XSSFSheet sheet, Map<String, String> protocols, boolean cascade) {
        List<String> failedCodes = new ArrayList<>();
        final String user = "ngl-support";
        
        sheet.rowIterator().forEachRemaining(row -> {
            if(row.getRowNum() == 0) return; 
            
            String expCode = row.getCell(0).getStringCellValue();
            String oldProtoName = row.getCell(1).getStringCellValue();
            String newProtoName = row.getCell(2).getStringCellValue();
            
            String newProtoCode = null;
            String oldProtoCode = null;

            if (StringUtils.isNoneBlank(newProtoName)) {
                if (protocols.containsKey(newProtoName)) {
                   newProtoCode = protocols.get(newProtoName);
                } else {
                    getLogger().error("Experiment '" + expCode + "' : " + newProtoName + "' does not exist.");
                    return;
                }
            } else {
                getLogger().error("Experiment '" + expCode + "' : no value in file for 'newProtocolName'.");
                return;
            }

            if (StringUtils.isNoneBlank(oldProtoName)) {
                if (protocols.containsKey(oldProtoName)) {
                   oldProtoCode = protocols.get(oldProtoName);
                } else {
                    getLogger().error("Experiment '" + expCode + "' : " + oldProtoName + "' does not exist.");
                    return;
                }
            } else {
                getLogger().error("Experiment '" + expCode + "' : no value in file for 'oldProtocolName'.");
                return;
            }

            Protocol protocolDb = MongoDBDAO.findByCode(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class, oldProtoCode);
           
			if (protocolDb != null) {
				protocolDb.code = newProtoCode;

				ContextValidation ctxValidate = ContextValidation.createUpdateContext(user);
				protocolDb.validate(ctxValidate);

				if (!ctxValidate.hasErrors()) {
					MongoDBDAO.update(InstanceConstants.PROTOCOL_COLL_NAME, protocolDb);
				} else {
					getLogger().error("(" + protocolDb + ") update failed: " + ctxValidate.getErrors());
				}
			}
            
            try {
                // Update the experiment with new protocol after checking the current protocol into experiment
                Experiment exp = this.expAPI.get(expCode);

                exp.protocolCode = newProtoCode;

                try {
                    if (!cascade) {
                        expAPI.update(exp, user);
                    } else {
                        expAPI.updateWithCascadingContentProperties(exp, user);
                    }
                } catch (APIException e) {
                    getLogger().error("Experiment '" + expCode + "' update failed.");
                    getLogger().error(e.getMessage(), e);

                    failedCodes.add(expCode);
                }
            } catch (Exception e) {
                getLogger().error("Experiment '" + expCode + "' does not exist.");
                getLogger().error(e.getMessage(), e);

                failedCodes.add(expCode);
            }
        });

        return failedCodes;
    }
}
