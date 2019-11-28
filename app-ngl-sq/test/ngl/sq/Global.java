package ngl.sq;

import fr.cea.ig.ngl.test.TestAppAuthFactory;
import fr.cea.ig.ngl.tmp.INGLDataDB;
import ngl.data.NGLDataDBMulti;
import ngl.data.PopulateDB;
import nglapps.IApplicationData;
import nglapps.cns.CNSApplicationData;

public class Global {

	public static final TestAppAuthFactory af = 
			new TestAppAuthFactory("ngl-sq.test.conf")
			.override(INGLDataDB.class, NGLDataDBMulti.class)
			.overrideEagerly(PopulateDB.class)
			.configure("institute", "CNS")
			.override(IApplicationData.class, CNSApplicationData.class);
	
}

