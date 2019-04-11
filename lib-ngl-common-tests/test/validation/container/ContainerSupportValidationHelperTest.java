package validation.container;

import static org.fest.assertions.Assertions.assertThat;
import static utils.TestHelper.saveDBOject;

import org.junit.Test;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.CC1Managed;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.container.description.ContainerCategory;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.container.instance.Container;
//import models.laboratory.project.instance.Project;
import models.laboratory.storage.instance.Storage;
import models.utils.InstanceConstants;
import models.utils.instance.ContainerSupportHelper;
import ngl.common.Global;
import utils.Constants;
import validation.ContextValidation;
import validation.container.instance.ContainerSupportValidationHelper;

public class ContainerSupportValidationHelperTest {

	private static class TestContext implements CC1Managed {
		
		Container container;
//		Project project;
		Storage stock;

		@Override
		public void setUp() throws Exception {
			stock = saveDBOject(Storage.class, InstanceConstants.STORAGE_COLL_NAME, "stock");
			container = new Container();
			container.categoryCode = ContainerCategory.find.get().findByCode("tube").code;
			container.support      = ContainerSupportHelper.getContainerSupport(ContainerSupportCategory.find.get().findByCode("tube").code, 1, "test", "1", "1");
		}

		@Override
		public void tearDown() {
			MongoDBDAO.getCollection(InstanceConstants.CONTAINER_COLL_NAME,Container.class).drop();
			MongoDBDAO.getCollection(InstanceConstants.STORAGE_COLL_NAME,Storage.class).drop();
		}
		
	}
	
	private static final CC1<TestContext> af =
			Global.afSq.cc1()
			.and(CCActions.managed(TestContext::new))
			.cc1((app,ctx) -> ctx);

	/*
	 * SupportCode / Line / Column unique position in creation
	 */
	@Test
	public void validateUniqueSupportCodePositionCode() throws Exception {
		af.accept(ctx -> {
//			ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//			contextValidation.setCreationMode();
			ContextValidation contextValidation = ContextValidation.createCreationContext(Constants.TEST_USER);
			// Container is not yet serialized
			ContainerSupportValidationHelper.validateUniqueContainerSupportCodePosition(contextValidation, ctx.container.support);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}

	@Test
	public void validateUniqueSupportCodePositionCodeExist() throws Exception {
		af.accept(ctx -> {
//			ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//			contextValidation.setCreationMode();
			ContextValidation contextValidation = ContextValidation.createCreationContext(Constants.TEST_USER);
			ctx.container=MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME, ctx.container);
			//Container is in the database
			ContainerSupportValidationHelper.validateUniqueContainerSupportCodePosition(contextValidation, ctx.container.support);
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
			//remove container for the others tests : (same context before and after)
			MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, ctx.container);
		});
	}
	
	@Test
	public void validateUniqueSupportCodePositionCodeNotCreationMode() throws Exception {
		af.accept(ctx -> {
//			ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
//			contextValidation.setUpdateMode();
			ContextValidation contextValidation = ContextValidation.createUpdateContext(Constants.TEST_USER);
			ContainerSupportValidationHelper.validateUniqueContainerSupportCodePosition(contextValidation, ctx.container.support);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
//			contextValidation.setDeleteMode();
			contextValidation = ContextValidation.createDeleteContext(Constants.TEST_USER);
			ContainerSupportValidationHelper.validateUniqueContainerSupportCodePosition(contextValidation, ctx.container.support);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}

	/*
	 * Container support category 
	 * @throws DAOException 
	 */
	@Test
	public void validateContainerSupportCategoryCode() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			ContainerSupportValidationHelper.validateContainerSupportCategoryCodeRequired(contextValidation, ContainerSupportCategory.find.get().findAll().get(0).code);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}

	@Test
	public void validateContainerSupportCategoryCodeRequired() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			ContainerSupportValidationHelper.validateContainerSupportCategoryCodeRequired(contextValidation, null);
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
		});
	}

	@Test
	public void validateContainerSupportCategoryCodeNotExist() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			ContainerSupportValidationHelper.validateContainerSupportCategoryCodeRequired(contextValidation, null);
			assertThat(contextValidation.getErrors().size()).isEqualTo(1);
		});
	}	
	
	/*
	 *  Stock 
	 */
	@Test
	public void validateStockCode() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			ContainerSupportValidationHelper.validateStorageCodeOptional(contextValidation, ctx.stock.code);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}

	@Test
	public void validateStockCodeNotRequired() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			ContainerSupportValidationHelper.validateStorageCodeOptional(contextValidation, null);
			assertThat(contextValidation.getErrors().size()).isEqualTo(0);
		});
	}

	@Test
	public void validateStockCodeNotExist() throws Exception {
		af.accept(ctx -> {
			ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.TEST_USER);
			ContainerSupportValidationHelper.validateStorageCodeOptional(contextValidation, "notexist");
			assertThat(contextValidation.getErrors().size()).isNotEqualTo(0);
		});
	}		

}


//package validation.container;
//
//import static org.fest.assertions.Assertions.assertThat;
//import models.laboratory.container.description.ContainerCategory;
//import models.laboratory.container.description.ContainerSupportCategory;
//import models.laboratory.container.instance.Container;
//import models.laboratory.project.instance.Project;
//import models.laboratory.storage.instance.Storage;
//import models.utils.InstanceConstants;
//import models.utils.dao.DAOException;
//import models.utils.instance.ContainerSupportHelper;
//
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import utils.AbstractTests;
//import utils.Constants;
//import validation.ContextValidation;
//import validation.container.instance.ContainerSupportValidationHelper;
//import fr.cea.ig.MongoDBDAO;
//
//public class ContainerSupportValidationHelperTest extends AbstractTests {
//	
//	static Container container;
//	static Project project;
//	static Storage stock;
//	
//	@BeforeClass
//	public static void initData() throws DAOException, InstantiationException, IllegalAccessException, ClassNotFoundException{
//		stock=saveDBOject(Storage.class, InstanceConstants.STORAGE_COLL_NAME, "stock");
//		
//		container=new Container();
//		container.categoryCode=ContainerCategory.find.get().findByCode("tube").code;
//		container.support=ContainerSupportHelper.getContainerSupport(ContainerSupportCategory.find.get().findByCode("tube").code, 1, "test", "1", "1");
//
//	}
//
//	@AfterClass
//	public static void deleteData() {
//		MongoDBDAO.getCollection(InstanceConstants.CONTAINER_COLL_NAME,Container.class).drop();
//		MongoDBDAO.getCollection(InstanceConstants.STORAGE_COLL_NAME,Storage.class).drop();
//
//	}
//	
//	
//	/**
//	 *  SupportCode / Line / Column unique position in creation
//	 */
//	
//	@Test
//	public void validateUniqueSupportCodePositionCode() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.setCreationMode();
//		//Container is not yet serialized
//		ContainerSupportValidationHelper.validateUniqueContainerSupportCodePosition(container.support, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	@Test
//	public void validateUniqueSupportCodePositionCodeExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.setCreationMode();
//		container=MongoDBDAO.save(InstanceConstants.CONTAINER_COLL_NAME,container);
//		//Container is in the database
//		ContainerSupportValidationHelper.validateUniqueContainerSupportCodePosition(container.support, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//		//remove container for the others tests : (same context before and after)
//		MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME,container);
//	}
//	
//	@Test
//	public void validateUniqueSupportCodePositionCodeNotCreationMode() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		contextValidation.setUpdateMode();
//		ContainerSupportValidationHelper.validateUniqueContainerSupportCodePosition(container.support, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//		contextValidation.setDeleteMode();
//		ContainerSupportValidationHelper.validateUniqueContainerSupportCodePosition(container.support, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	
//	
//	/**
//	 *  Container support category 
//	 * @throws DAOException 
//	 */
//
//	@Test
//	public void validateContainerSupportCategoryCode() throws DAOException {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerSupportValidationHelper.validateContainerSupportCategoryCode(ContainerSupportCategory.find.get().findAll().get(0).code, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	@Test
//	public void validateContainerSupportCategoryCodeRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerSupportValidationHelper.validateContainerSupportCategoryCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//	}
//
//	@Test
//	public void validateContainerSupportCategoryCodeNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerSupportValidationHelper.validateContainerSupportCategoryCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(1);
//	}	
//	
//	/**
//	 *  Stock 
//	 */
//
//	@Test
//	public void validateStockCode() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerSupportValidationHelper.validateStorageCode(stock.code, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	@Test
//	public void validateStockCodeNotRequired() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerSupportValidationHelper.validateStorageCode(null, contextValidation);
//		assertThat(contextValidation.errors.size()).isEqualTo(0);
//	}
//
//	@Test
//	public void validateStockCodeNotExist() {
//		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
//		ContainerSupportValidationHelper.validateStorageCode("notexist", contextValidation);
//		assertThat(contextValidation.errors.size()).isNotEqualTo(0);
//	}		
//
//
//}
