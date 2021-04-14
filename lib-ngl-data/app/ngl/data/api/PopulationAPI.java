package ngl.data.api;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.google.inject.Inject;

import fr.cea.ig.ngl.tmp.NGLDatabase;
import fr.cea.ig.ngl.utils.UserExecutionTime;
import models.utils.ModelDAOs;
import ngl.data.NGLSchemaInfo;
import nglapps.DataService;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Result;
import services.description.common.LevelService;
import services.description.common.MeasureService;
import services.description.common.ObjectTypeService;
import services.description.common.StateService;
import services.description.container.ContainerService;

public class PopulationAPI {
	
	private static final play.Logger.ALogger logger = play.Logger.of(PopulationAPI.class);
	
	private final DataService dataService;
	private final ModelDAOs   mdao; 
	private final NGLDatabase db;
	
	@Inject
	public PopulationAPI(ModelDAOs mdao, DataService dataService, NGLDatabase db) {
		this.mdao        = mdao;
		this.dataService = dataService;
		this.db          = db;
	}

	private void cleanDB(DataSource ds, NGLSchemaInfo nsi) {
	    for (String tn : nsi.getReverseDependentNames()) {
	        try (Connection connection = ds.getConnection();
	                Statement statement = connection.createStatement()) {
	            String sql = "delete from " + tn;
	            // logger.debug("{}",sql);
	            statement.executeUpdate(sql);
	            logger.debug("deleted data from table '{}'", tn);
	        } catch (Exception e) {
	            logger.warn("error while deleting data from {} : {}", tn, e.getMessage());
	        }
	    }

	}
	
	/**
	 * Remove all data from db and reload data
	 * @return Result object
	 */
	public Result cleanAndFullPopulation() {
        try {
            DataSource ds = db.getDataSource();
            NGLSchemaInfo nsi = new NGLSchemaInfo();
            cleanDB(ds, nsi);
            Map<String, List<ValidationError>> errors = loadData(ds, nsi);
            if (errors.size() > 0) {
                return badRequest(Json.toJson(errors));
            } else {
            	logger.info("NGLAll description is loaded!");
                return ok();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
	}
	
	/**
	 * load data into db without deleting previous data
	 * @return Result object
	 */
	public Result fullPopulation() {
		try {
			DataSource ds = db.getDataSource();
			NGLSchemaInfo nsi = new NGLSchemaInfo();
			Map<String, List<ValidationError>> errors = loadData(ds, nsi);
			if (errors.size() > 0)
				return badRequest(Json.toJson(errors));
			logger.info("NGLAll description is loaded!");
			return ok();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// return internalServerError(e.getMessage());
			throw new RuntimeException(e);
		}				
	}

    private Map<String, List<ValidationError>> loadData(DataSource ds, NGLSchemaInfo nsi) throws Exception, SQLException {
        Map<String,List<ValidationError>> errors = new HashMap<>();
        long et = 0;
        et += UserExecutionTime.run("institutes",   () -> { dataService                .saveInstitutes    (errors); });
        et += UserExecutionTime.run("object types", () -> { new ObjectTypeService(mdao).saveData          (errors); });
        et += UserExecutionTime.run("states",       () -> { new StateService     (mdao).saveData          (errors); });			
        //ResolutionService.main(errors);
        et += UserExecutionTime.run("levels",       () -> { new LevelService     (mdao).saveData          (errors); });
        et += UserExecutionTime.run("measures",     () -> { new MeasureService   (mdao).saveData          (errors); });
        et += UserExecutionTime.run("containers",   () -> { new ContainerService (mdao).saveData          (errors); });
        et += UserExecutionTime.run("instruments",  () -> { dataService                .saveInstrumentData(errors); });
        et += UserExecutionTime.run("samples",      () -> { dataService                .saveSampleData    (errors); });
        et += UserExecutionTime.run("imports",      () -> { dataService                .saveImportData    (errors); });
        et += UserExecutionTime.run("experiments",  () -> { dataService                .saveExperimentData(errors); });
        et += UserExecutionTime.run("processes",    () -> { dataService                .saveProcessData   (errors); });
        et += UserExecutionTime.run("projects",     () -> { dataService                .saveProjectData   (errors); });
        et += UserExecutionTime.run("runs",         () -> { dataService                .saveRunData       (errors); });
        et += UserExecutionTime.run("treatments",   () -> { dataService                .saveTreatmentData (errors); });
        logger.debug("done save in {}ms", et);
        // Read count * from all the tables.
        int count = 0;
        for (String tn : nsi.getDependentNames()) {
        	try (Connection connection = ds.getConnection();
        			Statement statement = connection.createStatement()) {
        		String sql = "select count(*) from " + tn;
        		ResultSet rs = statement.executeQuery(sql);
        		while (rs.next()) {
        			int tCount = rs.getInt(1);
        			logger.debug("count {} = {}", tn, tCount);
        			count += tCount;
        		}
        	}
        }
        logger.debug("total count {}", count);
        return errors;
    }

	
	
}
