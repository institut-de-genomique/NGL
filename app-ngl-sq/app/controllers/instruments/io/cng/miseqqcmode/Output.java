package controllers.instruments.io.cng.miseqqcmode;

/* 30/11/2020 NGL-3185: gestion du format de feuille de route Miseq pour LRM */

import java.util.List;

//import controllers.instruments.io.cng.miseqqcmode.tpl.txt.sampleSheet_1;
//import controllers.instruments.io.cng.miseqqcmode.tpl.txt.sampleSheet_2;
import controllers.instruments.io.cng.miseqqcmode.tpl.txt.*;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;
import controllers.instruments.io.utils.TagModel;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;

public class Output extends AbstractOutput {

	private static final play.Logger.ALogger logger = play.Logger.of(Output.class);
	
	@Override
	public File generateFile(Experiment experiment, ContextValidation contextValidation)  throws Exception {
		List<Container> containers = OutputHelper.getInputContainersFromExperiment(experiment);
		TagModel tagModel = OutputHelper.getTagModel(containers);
		
		//récupérer la valeur de la key "fdrType"dans contextValidation
		Object ftype =contextValidation.getObject("fdrType"); // !! en fait c'est un String...
				
		String content = getContent(experiment, containers, tagModel.tagType, (String) ftype);
		
		/*SUPSQCNG-921 ajouter le type de fichier dans le nom 
 		   NOTE 28/01/2020 il semble y avoir un probleme du cote de LRM et MSR   !
		   LRM lit les FDR MSR et MSR lit les FDR LRM !!!!!!
		   pour l'instant ne pas nommer les FDR avec _MSR mais  _1  ( 1 parce que c'etait la FDR originale )
		                  ne pas nommer les FDR avec _LRM mais  _2  ( 2 parceque ce sont les nouvelles FDR )   
		    !!! _1 et _2 ne veulent pas dire single ou dual comme dans le nom des templates !!!
			String filename = OutputHelper.getInstrumentPath(experiment.instrument.code)+experiment.instrumentProperties.get("miseqReagentCassette").value+"_"+ftype+".csv"; 
		*/
		String filename=null;
		if ("MSR".equals(ftype) ) {
			filename = OutputHelper.getInstrumentPath(experiment.instrument.code)+experiment.instrumentProperties.get("miseqReagentCassette").value+"_1.csv";
		} else {
			filename = OutputHelper.getInstrumentPath(experiment.instrument.code)+experiment.instrumentProperties.get("miseqReagentCassette").value+"_2.csv";
		}
		
		File file = new File(filename, content);
		
		OutputHelper.writeFile(file);
		return file;
	}
	
		
	// NGL-3703: nouveau type possible pour FDR: "LRM-CRefIX"=> creation de cette méthode
	private String getContent(Experiment experiment, List<Container> containers, String tagType, String ftype)  throws Exception {
		
		logger.info("Génération feuille de route Miseq mode : exp="+ experiment.typeCode + " / type="+ ftype + " / index="+ tagType);
		String content =null;
		
		if ("SINGLE-INDEX".equals(tagType)) {
			switch (ftype) {
				case "MSR":
					content = OutputHelper.format(sampleSheet_1.render(experiment,containers).body());
					break;
				case "LRM":
					content = OutputHelper.format(sampleSheet_1_LRM.render(experiment,containers).body());
					break;
				// NGL-3703: nouveau type possible        !!!! "-" interdit dans le nom du fichier scala.txt
				case "LRM-CRefIX":
					content = OutputHelper.format(sampleSheet_1_LRM_CRefIX.render(experiment,containers).body());
					break;
				default:
					throw new RuntimeException("Miseq sampleSheet type not managed : "+experiment.typeCode + " / " +ftype+ " / "+ tagType);
			}
		} else if ("DUAL-INDEX".equals(tagType)){
			switch (ftype) {
				case "MSR":
					content = OutputHelper.format(sampleSheet_2.render(experiment,containers).body());
					break;
				case "LRM":
					content = OutputHelper.format(sampleSheet_2_LRM.render(experiment,containers).body());
					break;
				// NGL-3703: nouveau type possible        !!!! "-" interdit dans le nom du fichier scala.txt
				case "LRM-CRefIX":
					content = OutputHelper.format(sampleSheet_2_LRM_CRefIX.render(experiment,containers).body());
					break;
				default:
					throw new RuntimeException("Miseq sampleSheet type not managed : "+experiment.typeCode + " / " +ftype+ " / "+ tagType);
			}
		} else {
			throw new RuntimeException("Miseq sampleSheet type not managed : "+experiment.typeCode + " / " +ftype+ " / "+ tagType);
		}
		
		return content;
	}
}
