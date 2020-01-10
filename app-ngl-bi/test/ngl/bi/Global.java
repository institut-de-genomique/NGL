package ngl.bi;

import fr.cea.ig.ngl.test.TestAppAuthFactory;
import fr.cea.ig.ngl.tmp.INGLDataDB;
import ngl.data.NGLDataDBMulti;
import ngl.data.PopulateDB;
import nglapps.IApplicationData;
import nglapps.cns.CNSApplicationData;
import rules.services.IDrools6Actor;
import rules.services.LazyRules6Executor;

public class Global {
	
	public static final TestAppAuthFactory af = 
			new TestAppAuthFactory("ngl-bi.test.conf")
			.override(INGLDataDB.class, NGLDataDBMulti.class)
			.overrideEagerly(PopulateDB.class)
			.configure("institute", "CNS")
			.override(IApplicationData.class, CNSApplicationData.class)
			.override(IDrools6Actor.class, LazyRules6Executor.class);
	
}
