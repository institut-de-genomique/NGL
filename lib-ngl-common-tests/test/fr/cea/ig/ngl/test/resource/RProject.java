package fr.cea.ig.ngl.test.resource;

import static fr.cea.ig.ngl.test.resource.RConstant.USER;

import fr.cea.ig.ngl.test.dao.api.factory.TestProjectFactory;
import fr.cea.ig.test.Actions;
import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.CC2;
import fr.cea.ig.util.function.F1;
import models.laboratory.project.instance.Project;

/**
 * Project actions.
 * 
 * @author vrd
 *
 */
public class RProject {

	/**
	 * Created and persisted project (instance that is saved to the data base
	 * and instance read from the database). 
	 */
	public static final CC2<Project,Project> createProjectRaw = customProjectRaw(p -> p);
	
	/**
	 * New persisted project.
	 */
	public static final CC1<Project> createProject = customProject(p -> p);
	
	/**
	 * Customize a default project.
	 * @param customize project customization
	 * @return          raw project CC (created instance, persisted instance)
	 */
	public static final CC2<Project,Project> customProjectRaw(F1<Project,Project> customize) {
		return Actions.using2(USER, () -> customize.apply(TestProjectFactory.project(USER)));
	}
	
	/**
	 * Customize a default project.
	 * @param customize project customization
	 * @return          raw project CC (created instance, persisted instance)
	 */
	public static final CC1<Project> customProject(F1<Project,Project> customize) {
		return customProjectRaw(customize).cc1((sp,rp) -> rp);
	}
	
}
