package ngl.refactoring.state;

import models.laboratory.common.description.StateCategory;

/**
 * Pseudo enumeration of state categories.
 * 
 * @author vrd
 *
 */
public class StateCategories {
	
	public static final StateCategory F;
	public static final StateCategory IP;
	public static final StateCategory IW; 
	public static final StateCategory N;
	
	public static final StateCategory[] values;
	
	static {
		values = new StateCategory[] {
			F  = new StateCategory(StateCategory.CODE.F),
			IP = new StateCategory(StateCategory.CODE.IP),
			IW = new StateCategory(StateCategory.CODE.IW), 
			N  = new StateCategory(StateCategory.CODE.N)			
		};
	}
	
}