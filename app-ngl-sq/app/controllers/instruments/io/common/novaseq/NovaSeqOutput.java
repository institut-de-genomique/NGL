package controllers.instruments.io.common.novaseq;

// 02/02/2018 NGL-1770

import java.util.List;

import controllers.instruments.io.common.novaseq.tpl.txt.sampleSheet_1;
import controllers.instruments.io.common.novaseq.tpl.txt.sampleSheet_2;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;
import controllers.instruments.io.utils.TagModel;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;

//class NovaSeqSampleSheet extends TextOutput {
//	
//	public void header(Experiment experiment, List<Container> containers) {
//		// Header
//		println("[Header]");
//		println("IEMFileVersion,5");
//		println("Experiment Name,", experiment.code);
//		println("Date,", new SimpleDateFormat("MM/dd/yyyy").format(experiment.traceInformation.creationDate));
//		println("Workflow,GenerateFASTQ");
//		println("Application,NovaSeq FASTQ Only");
//		println("Instrument Type,NovaSeq");
//		println("Assay,");
//		println("Index Adapters,");
//		println("Description,", containers.get(0).support.code);
//		println("Chemistry,Default");
//		println();
//	}
//	
//	public void reads(Experiment experiment) {
//		// Reads
//		println("[Reads]");
//		println(experiment.instrumentProperties.get("nbCyclesRead1").value.toString());
//		println(experiment.instrumentProperties.get("nbCyclesRead2").value.toString());
//		println();		
//	}
//	
//	public void settings() {
//		// Settings
//		println("[Settings]");
//		println("Adapter,AGATCGGAAGAGCACACGTCTGAACTCCAGTCA");
//		println("AdapterRead2,AGATCGGAAGAGCGTCGTGTAGGGAAAGAGTGT");
//		println();
//	}
//	
//	public String sampleId(Container c, Content co) {
//		return c.support.line 
//				+ "_" + co.sampleCode
//				+ "_" + OutputHelper.getContentProperty(co,"libProcessTypeCode")
//				+ "_" + OutputHelper.getContentProperty(co,"tag");
//	}
//	
//	public String description(Content co) {
//		return OutputHelper.getContentProperty(co,"tag") + "_" + co.percentage;
//	}
//	
//	public NovaSeqSampleSheet sheet1(Experiment experiment, List<Container> containers) {
//		header(experiment, containers);
//		reads(experiment);
//		settings();
//		// Data
//		println("[Data]");
//		println("Sample_ID,Sample_Name,Sample_Plate,Sample_Well,I7_Index_ID,index,Sample_Project,Description");
//		containers.sort(((a,b) -> a.code.compareTo(b.code)));
//		for (Container c : containers) {
//			for (Content co : c.contents) {
////				String sample_id     = c.support.line 
////						+ "_" + co.sampleCode
////						+ "_" + OutputHelper.getContentProperty(co,"libProcessTypeCode")
////						+ "_" + OutputHelper.getContentProperty(co,"tag");
//				String sample_id = sampleId(c,co);
//				
//				String i7_index_name = OutputHelper.getContentProperty(co,"tag");
//				String i7_index_seq  = OutputHelper.getSequence(OutputHelper.getIndex("index-illumina-sequencing",OutputHelper.getContentProperty(co,"tag")));
////				String description   = OutputHelper.getContentProperty(co,"tag") + "_" + co.percentage;
//				String description = description(co);
//				// Only one of the two is needed
//				//println(sample_id, ",,,,", i7_index_name, ",", i7_index_seq, ",", co.projectCode, ",", description);
//				printfln("%s,,,,%s,%s,%s,%s", sample_id, i7_index_name, i7_index_seq, co.projectCode, description);
//			}
//		} 
//		return this;
//	}
//	
//	public NovaSeqSampleSheet sheet2(Experiment experiment, List<Container> containers) {
//		header(experiment, containers);
//		reads(experiment);
//		settings();
//		// data
//		println("[Data]");
//		println("Sample_ID,Sample_Name,Sample_Plate,Sample_Well,I7_Index_ID,index,I5_Index_ID,index2,Sample_Project,Description");
//		containers.sort((a,b) -> a.code.compareTo(b.code));
//		for(Container c : containers) { 
//			for(Content co : c.contents) {
////				String sample_id = c.support.line 
////						+ "_" + co.sampleCode 
////						+ "_" + OutputHelper.getContentProperty(co,"libProcessTypeCode")
////						+ "_" + OutputHelper.getContentProperty(co,"tag");
//				String sample_id = sampleId(c,co); 
////			@*** 13/02/2018ATTENTION tous les noms d'index dual ne suivent pas la nomenclature Illumina <index1>-<index2>=> ne pas splitter, ne pas mettre les noms dans la FDR
////			@{i7_index_name=OutputHelper.getContentProperty(co,"tag").split("-")(0)}
////			@{i5_index_name=OutputHelper.getContentProperty(co,"tag").split("-")(1)}
////			***@
//				String[] indexSeq = OutputHelper.getSequence(OutputHelper.getIndex("index-illumina-sequencing",OutputHelper.getContentProperty(co,"tag"))).split("-");
////				String i7_index_seq = OutputHelper.getSequence(OutputHelper.getIndex("index-illumina-sequencing",OutputHelper.getContentProperty(co,"tag"))).split("-")[0];
////				String i5_index_seq = OutputHelper.getSequence(OutputHelper.getIndex("index-illumina-sequencing",OutputHelper.getContentProperty(co,"tag"))).split("-")[1];
//				String i7_index_seq = indexSeq[0];
//				String i5_index_seq = indexSeq[1];
////				String description  = OutputHelper.getContentProperty(co,"tag") + "_" + co.percentage;
//				String description = description(co);
//				printfln("%s,,,,,%s,,%s,%s,%s", sample_id, i7_index_seq, i5_index_seq, co.projectCode, description);
//			}
//		}
//		return this;		
//	}
//	
//}
//

public abstract class NovaSeqOutput extends AbstractOutput {

	private static final play.Logger.ALogger logger = play.Logger.of(NovaSeqOutput.class);
	
	@Override
	public File generateFile(Experiment experiment, ContextValidation contextValidation) {
		List<Container> containers = OutputHelper.getInputContainersFromExperiment(experiment);
		TagModel tagModel = OutputHelper.getTagModel(containers);
		
		String content = null;
		
		if (!"DUAL-INDEX".equals(tagModel.tagType)) {
			content = OutputHelper.format(sampleSheet_1.render(experiment,containers).body());	
		} else {
			content = OutputHelper.format(sampleSheet_2.render(experiment,containers).body());	
		}
		
		String filename = OutputHelper.getInstrumentPath(experiment.instrument.code)+containers.get(0).support.code+".csv";
		
//		System.out.println("instrument code= "+ experiment.instrument.code );
//		System.out.println("instrument path= "+  OutputHelper.getInstrumentPath(experiment.instrument.code));
		logger.debug("instrument code = {}", experiment.instrument.code );
		logger.debug("instrument path = {}", OutputHelper.getInstrumentPath(experiment.instrument.code));
		
		File file = new File(filename, content);
		OutputHelper.writeFile(file);
		return file;
	}

}
