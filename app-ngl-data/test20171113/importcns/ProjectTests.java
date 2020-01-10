package importcns;

import java.sql.SQLException;

import models.Constants;
import models.utils.dao.DAOException;

import org.junit.Assert;
import org.junit.Test;

import services.instance.project.ProjectImportCNS;
import utils.AbstractTests;
import validation.ContextValidation;

public class ProjectTests extends AbstractTests {
	
	@Test
	public void importProject() throws SQLException, DAOException{
		ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		ProjectImportCNS.createProject(contextValidation);
		Assert.assertEquals(contextValidation.errors.size(),0);
	}

}
