package ngl.refactoring.state;

import models.laboratory.common.description.ObjectType;

/**
 * Pseudo enumeration definition of objects types (kind of boxed ObjectType.CODE
 * values).
 * 
 * @author vrd
 *
 */
public class ObjectTypes {
	
	public static final ObjectType 		
		Project, 
		Process, 
		Sample, 
		Container, 
		Instrument, 
		Reagent,
		Experiment, 
		Import, 
		Run, 
		Treatment, 
		ReadSet, 
		Analysis, 
		SRASubmission, 
		SRAConfiguration, 
		SRAStudy, 
		SRASample, 
		SRAExperiment,
		ReagentReception;
	
	public static final ObjectType[] values;
	
	static {
		values = new ObjectType[] {
			Project          = new ObjectType(ObjectType.CODE.Project         ), 
			Process          = new ObjectType(ObjectType.CODE.Process         ), 
			Sample           = new ObjectType(ObjectType.CODE.Sample          ), 
			Container        = new ObjectType(ObjectType.CODE.Container       ), 
			Instrument       = new ObjectType(ObjectType.CODE.Instrument      ), 
			Reagent          = new ObjectType(ObjectType.CODE.Reagent         ),
			Experiment       = new ObjectType(ObjectType.CODE.Experiment      ), 
			Import           = new ObjectType(ObjectType.CODE.Import          ), 
			Run              = new ObjectType(ObjectType.CODE.Run             ), 
			Treatment        = new ObjectType(ObjectType.CODE.Treatment       ), 
			ReadSet          = new ObjectType(ObjectType.CODE.ReadSet         ), 
			Analysis         = new ObjectType(ObjectType.CODE.Analysis        ), 
			SRASubmission    = new ObjectType(ObjectType.CODE.SRASubmission   ), 
			SRAConfiguration = new ObjectType(ObjectType.CODE.SRAConfiguration), 
			SRAStudy         = new ObjectType(ObjectType.CODE.SRAStudy        ), 
			SRASample        = new ObjectType(ObjectType.CODE.SRASample       ), 
			SRAExperiment    = new ObjectType(ObjectType.CODE.SRAExperiment   ),
			ReagentReception = new ObjectType(ObjectType.CODE.ReagentReception)
		};
	}
	
}
