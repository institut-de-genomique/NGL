package scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.experiments.ExperimentsAPI;
import fr.cea.ig.ngl.dao.protocols.ProtocolsAPI;
import models.laboratory.experiment.instance.Experiment;
/**
 * INPUT PROTOCOLE CODE
 * Script qui prend en compte les tags secondaires
 * Les deux onglets sont obligatoires même si pas de code expérience
 * ATTENTION le user défini doit avoir un rôle admin pour pouvoir lancer la propagation
 * ATTENTION le user défini dans la base de descrption doit etre renseigné dans la table user_application avec application ngl-sq
 * @author ejacoby
 *
 */
public class UpdateProtocolCodeOfExp extends ScriptWithExcelBody {

//    public static class Args {}
    
    private final ProtocolsAPI protoAPI;
    private final ExperimentsAPI expAPI;
    
    @Inject
    public UpdateProtocolCodeOfExp(ProtocolsAPI protoAPI, ExperimentsAPI expAPI) {
        this.protoAPI = protoAPI;
        this.expAPI   = expAPI;
    }

    
    
    /**
     * Format of excel file supported:                                          <br>
            1st sheet "racine": experiments which require cascading properties <br>
            2nd sheet "experiments": experiments without cascading properties <br>
                                                                             <br>
            For each sheet:                                                  <br>
            1st line: header of columns (the name used are free)             <br>
            
            | Experiment | Proto name | New proto name | New proto Version | <br>
                                                                             <br>            

            1st column: code of experiment                                   <br>
            2nd column: name of current protocol                             <br>
            3rd column: name of new protocol wanted                          <br>
            4th column: version of new protocol wanted (optional)            <br>
                                                                             <br>
            Make sure to remove any empty line in the file.
     */
    @Override
    public void execute(XSSFWorkbook workbook) throws Exception {
        Map<String, String> protocols = new HashMap<>();
        this.protoAPI.all().forEach(p -> protocols.put(p.name, p.code));
        
//        File xlsx = (File) body.asMultipartFormData().getFile("xlsx").getFile();
//        FileInputStream fis = new FileInputStream(xlsx);
//
//        // Finds the workbook instance for XLSX file
//        XSSFWorkbook workbook = new XSSFWorkbook (fis);
        
        List<String> failedCodes = new ArrayList<>();
        getLogger().info("Update experiments with cascading properties");
        failedCodes = updateExperimentWithCascading(workbook.getSheet("racine"), protocols);
        
        getLogger().info("Update experiments without cascading properties");
        failedCodes.addAll(updateExperimentWithoutCascading(workbook.getSheet("experiments"), protocols));
        
        if(failedCodes.size() != 0) {
            getLogger().warn("Some experiments have not been updated");
            getLogger().warn(failedCodes.toString());
        } else {
            getLogger().info("All experiments have been updated");
        }
    }

    private List<String> updateExperimentWithoutCascading(XSSFSheet sheet, Map<String, String> protocols) {
        return updateExperiment(sheet, protocols, false);
    }
    
    private List<String> updateExperimentWithCascading(XSSFSheet sheet, Map<String, String> protocols) {
        return updateExperiment(sheet, protocols, true);
    }

    private List<String> updateExperiment(XSSFSheet sheet, Map<String, String> protocols, boolean cascade) {
        List<String> failedCodes = new ArrayList<>();
        final String user = "ngl-admin";
        
        sheet.rowIterator().forEachRemaining(row -> {
            if(row.getRowNum() == 0) return; // skip header
            
            String expCode      = row.getCell(0).getStringCellValue();
            String oldProtoName = row.getCell(1).getStringCellValue();
            String newProtoName = row.getCell(2).getStringCellValue();
            
            String oldProtoCode = null;
            String newProtoCode = null;
            if(StringUtils.isNoneBlank(oldProtoName) && StringUtils.isNoneBlank(newProtoName)) {
                if(protocols.containsKey(oldProtoName) && protocols.containsKey(newProtoName)) {
                    oldProtoCode = protocols.get(oldProtoName);
                    newProtoCode = protocols.get(newProtoName);
                } else {
                    getLogger().error("(" + expCode + ") no protocol [" + oldProtoName + " or " + newProtoName + "] found into protocol list");
                    return;
                }
            } else {
                getLogger().error("(" + expCode + ") no value in file for old protocol name or new protocol name");
                return;
            }
            
            try {
                // Update the experiment with new protocol after checking the current protocol into experiment
                Experiment exp = this.expAPI.get(expCode);
                if(exp.protocolCode.equals(oldProtoCode)){
                        //&& (exp.protocolCode.equals("prt_wait") || exp.protocolCode.equals("prt_wait_2"))) { //little 'hack' because 2 protos have the same name and are identicals (manually reviewed)
                    exp.protocolCode = newProtoCode;
                    try {
                        if(! cascade) {
                            expAPI.update(exp, user);
                        } else {
                            expAPI.updateWithCascadingContentProperties(exp, user);
                        }
                    } catch (APIException e) {
                        getLogger().error("(" + expCode + ") update failed: " + e.getMessage());
                        getLogger().debug(e.getMessage(), e);
                    }
                    getLogger().info("experiment " + expCode + " updated (cascade=" + cascade + ")");
                } else {
                    getLogger().warn("(" + expCode + ") protocol code does not correspond [" + exp.protocolCode + "] -vs- [" + oldProtoCode + "]");
                    failedCodes.add(expCode);
                }
            } catch (Exception e) {
                getLogger().error("Experiment does not exist " + expCode);
                getLogger().debug(e.getMessage(), e);
                failedCodes.add(expCode);
            }
        });
        return failedCodes;
    }
}
