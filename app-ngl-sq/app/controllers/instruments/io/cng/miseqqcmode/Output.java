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
		String content = null; 
		
		//récupérer la valeur de la key "fdrType"dans contextValidation
		Object ftype =contextValidation.getObject("fdrType");
		
		if (!"DUAL-INDEX".equals(tagModel.tagType)) {
			if ("LRM".equals(ftype) ){	
				logger.info("Génération feuille de route Miseq mode / exp="+ experiment.typeCode + " / type="+ ftype ); 
				content = OutputHelper.format(sampleSheet_1_LRM.render(experiment,containers).body());
			} else if ("MSR".equals(ftype)) {
				// old format
				logger.info("Génération feuille de route Miseq mode / exp="+ experiment.typeCode + " / type="+ ftype );
				content = OutputHelper.format(sampleSheet_1.render(experiment,containers).body());
			}else {
				throw new RuntimeException("Miseq sampleSheet type not managed : "+experiment.typeCode + " /" +ftype);
			}
		} else {
			if ("LRM".equals(ftype) ){	
				logger.info("Génération feuille de route Miseq mode / exp="+ experiment.typeCode + " / type="+ ftype );
				content = OutputHelper.format(sampleSheet_2_LRM.render(experiment,containers).body());
			} else if ("MSR".equals(ftype)) {
				// old format
				logger.info("Génération feuille de route Miseq mode / exp="+ experiment.typeCode + " / type="+ ftype );
				content = OutputHelper.format(sampleSheet_2.render(experiment,containers).body());
			}else {
				throw new RuntimeException("Miseq sampleSheet type not managed : "+experiment.typeCode + " /" +ftype);
			}
		}
		
		/*SUPSQCNG-921 ajouter le type de fichier dans le nom
		String filename = OutputHelper.getInstrumentPath(experiment.instrument.code)+experiment.instrumentProperties.get("miseqReagentCassette").value+".csv";
		*/
		
		/* NOTE 28/01/2020 il semble y  avoir un probleme du cote de LRM et MSR   !
		   LRM lit les FDR MSR et MSR lit les FDR LRM !!!!!!
		   pour l'instant ne pas nommer les FDR avec _MSR mais  _1  ( 1 parce que c'etait la FDR originale )
		                  ne pas nommer les FDR avec _LRM mais  _2  ( 2 parceque ce sont les nouvelles FDR )   
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
}
