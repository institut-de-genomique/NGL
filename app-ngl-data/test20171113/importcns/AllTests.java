package importcns;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import models.utils.dao.DAOException;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;



@RunWith(Suite.class)
//All suites test
@SuiteClasses({ProjectTests.class,ContainerTests.class})
public class AllTests {

	
	public static List<String> runCodes=new ArrayList<String>();
	public static List<String> prepaCodes=new ArrayList<String>();
	public static List<String> runDelete=new ArrayList<String>();
	public static List<String> sampleCodes=new ArrayList<String>();

	public static List<String> runExtCodes=new ArrayList<String>();
	public static List<String> prepaExtCodes=new ArrayList<String>();
	
	public static Test suite() {
        TestSuite suite = new TestSuite("Test for NGL-DATA CNS");
        //$JUnit-BEGIN$

        //$JUnit-END$
        return suite;
    }
	
	
	public static void initDataRun() throws DAOException, InstantiationException,
	IllegalAccessException, ClassNotFoundException, SQLException {		
		//Miseq
		runCodes.add("140127_MIMOSA_A7PE4");
		runCodes.add("140116_FLUOR_C39MEACXX");
		runCodes.add("140124_MIMOSA_A72F0");
		runCodes.add("111018_PHOSPHORE_C05T4ACXX");
		//Run abandonne sans readSets
		runCodes.add("130910_MERCURE_D2G9NACXX");	
		//Pas de solution pour l'instant car prepaflowcell n'est pas en adequation avec le run
		//runCodes.add("080724_HELIUM_201WFAAXX");

		//Run Tara pour tester udpdate Tara
		runCodes.add("131205_MERCURE_C3959ACXX");
		//Run ble
		runCodes.add("140429_FLUOR_H89E9ADXX");
		runDelete.addAll(runCodes);
		// Miseq
		prepaCodes.add("A7PE4");
		prepaCodes.add("A72F0"); 
		prepaCodes.add("C37T3ACXX");
		prepaCodes.add("C39MEACXX");
		prepaCodes.add("C05T4ACXX");
		prepaCodes.add("D2G9NACXX");
		// prepaflowcell tag=null
		prepaCodes.add("C3K2AACXX");
		prepaCodes.add("C3959ACXX");
		//ble
		prepaCodes.add("H89E9ADXX");

		sampleCodes.add("BFY_AAA");

	}
	
	
	public static  void initDataRunExt() throws DAOException, InstantiationException,
	IllegalAccessException, ClassNotFoundException, SQLException {		
		runExtCodes.add("140703_EXTMISEQ_M00619");
		prepaExtCodes.add("M00619");
	}
	
	
	
}
