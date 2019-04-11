//package ad.services;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//
//import javax.naming.NamingException;
//import javax.naming.directory.SearchResult;
//
//import org.junit.After;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import fr.cea.ig.ngl.dao.projects.MembersProjectsUtils;
//import services.projects.members.ActiveDirectoryServices;
//import utils.AbstractTests;
//
//public class ActiveDirectoryServicesTest extends AbstractTests{
//
//	final String testUser1="test1";
//	final String testUser2="test2";
//
//	static String groupTest1="g_test1";
//	static String groupTest2="g_test2";
//
//	static String ouTest = "OU=Orga,OU=UserGroups,OU=UserAccounts";
//	static String ouTest2 = "OU=UserGroups,OU=UserAccounts";
//	
//	static String ouUserTest = "OU=Regular,OU=UserAccounts";
//	static ActiveDirectoryServices adServices;
//
//	static ActiveDirectoryServices ADServices() {
//		return app.injector().instanceOf(ActiveDirectoryServices.class);
//	}
//
//	@BeforeClass
//	public static void initData()
//	{
//		adServices = ADServices();
//	}
//
//	@After
//	public void removeData() throws NamingException
//	{
//		adServices.deleteGroup(groupTest1);
//		adServices.deleteGroup(groupTest2);
//		adServices.removeGroupFromUser(testUser1, groupTest2);
//		adServices.removeGroupFromUser(testUser2, groupTest2);
//	}
//
//	@Test
//	public void testSearchAD() throws NamingException
//	{
//		//Get ldap account by account name
//		SearchResult searchResult = adServices.findUserAccountByAccountName(testUser1);
//		assertEquals(searchResult.getAttributes().get("sAMAccountName").get(),testUser1);
//	}
//
//	@Test
//	public void testCreateGroup() throws NamingException
//	{
//		//Rechercher un compte ldap par un account name
//		adServices.createGroup(groupTest1,false);
//
//		//Check group created
//		SearchResult resultGroup = adServices.findGroupAccountByAccountName(groupTest1);
//		assertNotNull(resultGroup);
//		//Check attributes
//		assertEquals(resultGroup.getAttributes().get("msSFU30NisDomain").get(),"pc");
//
//	}
//	
//	@Test
//	public void testCreateUnixGroup() throws NamingException
//	{
//		String newGID = adServices.getGIDAvalaible(100, 200);
//		assertNotNull(newGID);
//		adServices.createGroup(groupTest1,true);
//		//Check group created
//		SearchResult resultGroup = adServices.findGroupAccountByAccountName(groupTest1);
//		assertNotNull(resultGroup);
//		//Check attributes
//		assertEquals(resultGroup.getAttributes().get("gidNumber").get(),newGID);
//	}
//
//	@Test
//	public void testAddUserToGroup() throws NamingException
//	{
//		//Create group
//		adServices.createGroup(groupTest2,false);
//
//		adServices.addUserToGroup(testUser1, groupTest2);
//
//		//Check members group
//		SearchResult resultGroup = adServices.findGroupAccountByAccountName(groupTest2);
//		SearchResult resultUser = adServices.findUserAccountByAccountName(testUser1);
//		System.out.println("Member "+resultGroup.getAttributes().get("member").get());
//		assertTrue(((String)resultGroup.getAttributes().get("member").get()).contains((String)resultUser.getAttributes().get("DistinguishedName").get()));
//		assertTrue(((String)resultUser.getAttributes().get("MemberOf").get()).contains(groupTest2));
//	}
//
//	@Test
//	public void testListMember() throws NamingException
//	{
//		adServices.createGroup(groupTest2,false);
//
//		adServices.addUserToGroup(testUser1, groupTest2);
//		adServices.addUserToGroup(testUser2, groupTest2);
//
//		List<String> members = adServices.getMembers(groupTest2);
//		assertEquals(members.size(),2);
//	}
//
//	@Test
//	public void testListAllMembers() throws NamingException
//	{
//		List<String> members = adServices.getAllMembers();
//		assertNotNull(members);
//		assertTrue(members.size()>0);
//	}
//
//	@Test
//	public void testListMembersNotInGroup() throws NamingException
//	{
//		adServices.createGroup(groupTest2,false);
//
//		adServices.addUserToGroup(testUser1, groupTest2);
//
//		List<String> members = adServices.getMembersNotInGroup(groupTest2);
//		assertEquals(members.stream().filter(value->value.equals(testUser1)).count(),0);
//	}
//
//	@Test
//	public void testAddUserGroupToGroup() throws NamingException
//	{
//		adServices.createGroup(groupTest1,false);
//		adServices.addUserToGroup(testUser1, groupTest1);
//		adServices.createGroup(groupTest2,false);
//		adServices.addUserToGroup(testUser2, groupTest2);
//
//		adServices.addUsersGroupToGroup(groupTest1, groupTest2);
//
//		SearchResult resultGroup = adServices.findGroupAccountByAccountName(groupTest2);
//		assertTrue(((String)resultGroup.getAttributes().get("member").get()).contains(groupTest1));
//	}
//
//	@Test
//	public void testGetGroupFromOU() throws NamingException
//	{
//		List<String> listName = adServices.getGroups(ouTest);
//		assertNotNull(listName);
//		assertTrue(listName.size()>0);
//	}
//	
//	@Test
//	public void testGetOUFromOU() throws NamingException
//	{
//		List<String> listName = adServices.getOrganizationUnits(ouTest2);
//		for(String name : listName){
//			System.out.println("Name "+name);
//		}
//		assertNotNull(listName);
//		assertTrue(listName.size()>0);
//	}
//	
//	@Test
//	public void testGetUsersFroOU() throws NamingException
//	{
//		List<String> listLogins = adServices.getMembersByOrganizationUnit(ouUserTest);
//		assertNotNull(listLogins);
//		assertTrue(listLogins.size()>0);
//	}
//
//	@Test
//	public void testGetMapUsersFromOU() throws NamingException
//	{
//		List<String> attributeNames =  Arrays.asList(MembersProjectsUtils.samAccountName,MembersProjectsUtils.displayName);
//		List<Map<String,String>> listUsers = adServices.getMembersByOrganizationUnit(ouUserTest, attributeNames);
//		assertNotNull(listUsers);
//		assertTrue(listUsers.size()>0);
//	}
//	
//}
