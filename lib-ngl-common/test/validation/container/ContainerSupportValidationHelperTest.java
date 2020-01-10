package validation.container;

import static org.fest.assertions.Assertions.assertThat;
import models.laboratory.container.description.ContainerCategory;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.container.instance.Container;
import models.laboratory.project.instance.Project;
import models.laboratory.storage.instance.Storage;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import models.utils.instance.ContainerSupportHelper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import utils.AbstractTests;
import utils.Constants;
import validation.ContextValidation;
import validation.container.instance.ContainerSupportValidationHelper;
import fr.cea.ig.MongoDBDAO;

public class ContainerSupportValidationHelperTest extends AbstractTests {
	static Container container;
	static Project project;
	static Storage stock;
	
	@BeforeClass
	public static void initData() throws DAOException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		stock=saveDBOject(Storage.class, InstanceConstants.STORAGE_COLL_NAME, "stock");
		
		container=new Container();
		container.categoryCode=ContainerCategory.find.findByCode("tube").code;
		container.support=ContainerSupportHelper.getContainerSupport(ContainerSupportCategory.find.findByCode("tube").code, 1, "test", "1", "1");

	}

	@AfterClass
	public static void deleteData() {
		MongoDBDAO.getCollection(InstanceConstants.CONTAINER_COLL_NAME,Container.class).drop();
		MongoDBDAO.getCollection(InstanceConstants.STORAGE_COLL_NAME,Storage.class).drop();

	}
	
	
	/**
	 *  SupportCode / Line / Column unique position in creation
	 */
	
	@Test
	public void validateUniqueSupportCodePositionCode() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		contextValidation.setCreationMode();
		//Container is not yet serialized
		ContainerSupportValidationHelper.validateUniqueContainerSupportCodePosition(container.support, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	@Test
	public void validateUniqueSupportCodePositionCodeExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		contextValidation.setCreationMode();
		container=MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME,container);
		//Container is in the database
		ContainerSupportValidationHelper.validateUniqueContainerSupportCodePosition(container.support, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(1);
		//remove container for the others tests : (same context before and after)
		MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME,container);
	}
	
	@Test
	public void validateUniqueSupportCodePositionCodeNotCreationMode() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		contextValidation.setUpdateMode();
		ContainerSupportValidationHelper.validateUniqueContainerSupportCodePosition(container.support, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
		contextValidation.setDeleteMode();
		ContainerSupportValidationHelper.validateUniqueContainerSupportCodePosition(container.support, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	
	
	/**
	 *  Container support category 
	 * @throws DAOException 
	 */

	@Test
	public void validateContainerSupportCategoryCode() throws DAOException {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerSupportValidationHelper.validateContainerSupportCategoryCode(ContainerSupportCategory.find.findAll().get(0).code, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	@Test
	public void validateContainerSupportCategoryCodeRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerSupportValidationHelper.validateContainerSupportCategoryCode(null, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(1);
	}

	@Test
	public void validateContainerSupportCategoryCodeNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerSupportValidationHelper.validateContainerSupportCategoryCode(null, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(1);
	}	
	
	/**
	 *  Stock 
	 */

	@Test
	public void validateStockCode() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerSupportValidationHelper.validateStorageCode(stock.code, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	@Test
	public void validateStockCodeNotRequired() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerSupportValidationHelper.validateStorageCode(null, contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(0);
	}

	@Test
	public void validateStockCodeNotExist() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		ContainerSupportValidationHelper.validateStorageCode("notexist", contextValidation);
		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
	}		


}
