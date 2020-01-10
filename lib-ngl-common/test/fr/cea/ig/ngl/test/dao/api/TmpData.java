//package fr.cea.ig.ngl.test.dao.api;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//
//import javax.inject.Singleton;
//import javax.naming.NamingException;
//import javax.naming.directory.SearchResult;
//
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import fr.cea.ig.ngl.dao.api.APIException;
//import fr.cea.ig.ngl.dao.api.APIValidationException;
//import fr.cea.ig.ngl.dao.projects.MembersProjectsAPI;
//import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
//import fr.cea.ig.ngl.test.AbstractAPITests;
//import fr.cea.ig.ngl.test.dao.api.factory.TestMembersProjectFactory;
//import fr.cea.ig.ngl.test.dao.api.factory.TestProjectFactory;
//import models.laboratory.project.instance.Members;
//import models.laboratory.project.instance.Project;
//import models.laboratory.project.instance.UserMembers;
//import play.Logger.ALogger;
//import services.projects.members.ActiveDirectoryServices;
//import utils.AbstractTests;
//
//@Singleton  
//public class TmpData extends AbstractTests implements AbstractAPITests {
//
//	private static final play.Logger.ALogger logger = play.Logger.of(TmpData.class);
//
//	private static MembersProjectsAPI api;
//	private static ActiveDirectoryServices services;
//
//	private static final String USER = "ngsrg";
//
//	final static String testUser1="test1";
//	final static String testUser2="test2";
//
//
//	@BeforeClass
//	public static void setUpClass() {
//		assertTrue(app.isDev());
//		api = app.injector().instanceOf(MembersProjectsAPI.class);
//		services = app.injector().instanceOf(ActiveDirectoryServices.class);
//	}
//
//	@AfterClass
//	public static void cleanClass()
//	{
//	}
//
//	@Override
//	public void setUpData() {
//	}
//
//	@Override
//	public void deleteData(){
//	}
//
//	@Override
//	public ALogger logger() {
//		return null;
//	}
//
//	@Test
//	public void testCreate() throws APIValidationException, NamingException
//	{
//
//		services.deleteGroup("g_BFB");
//		services.removeGroupFromUser(testUser1, "g_BFB");
//		services.removeGroupFromUser(testUser2, "g_BFB");
//		services.deleteGroup("g_BFB_admin");
//
//		Members members = new Members();
//		members.codeProjet="BFB";
//		members.admins.add(new UserMembers("ejacoby"));
//		members = api.create(members, USER);
//		members.users=Arrays.asList(new UserMembers(testUser1));
//		members = api.update(members, USER);
//	}
//
//	
//}
