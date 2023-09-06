package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import fr.cea.ig.ngl.dao.api.ResolutionConfigurationAPI;
import fr.cea.ig.ngl.dao.codelabels.CodeLabelAPI;
import fr.cea.ig.ngl.dao.protocols.ProtocolsAPI;
import models.laboratory.common.description.CodeLabel;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.QualityControlResult;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.protocol.instance.Protocol;
import models.laboratory.resolutions.instance.Resolution;
import models.laboratory.resolutions.instance.ResolutionConfiguration;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

/**
 * Script permettant de générer des bilans projet type MetaB NGL-3283 destiné au collaborateur et au matériel et méthode
 * Prend en entrée une liste de code ReadSet
 * Les bilans MetaG contiennent entre autre les infos de préparation ADN, de readSet, de l'échantillon bio, échantillon parent, échantillon grand parent...
 * Le bilan MetaB contiendra en plus les infos de préparation de la banque amplicon
 * 
 * Ce script prend un fichier excel 
 *   Onglet 1 metab contient la correspondance entre le code de la propriété, le label de la colonne et le numéro de la colonne
 *   Onglet 2 la liste des codes readsets sans header (Si code readset correspond a des temoins negatif alors il faut nommer explicitement l'onglet ReadSetTN
 *   Voir template fichier dans fiche wiki : Bilan MetaB et MetaG
 * @author ejacoby
 *
 */
public class BilanProjectMetaB extends ScriptWithExcelBody{

	private final CodeLabelAPI codeLabelAPI;
	private final ResolutionConfigurationAPI resolutionConfigurationAPI;
	private final ProtocolsAPI protocolsAPI;


	@Inject
	public BilanProjectMetaB(CodeLabelAPI codeLabelAPI, ResolutionConfigurationAPI resolutionConfigurationAPI, ProtocolsAPI protocolsAPI) {
		this.codeLabelAPI   = codeLabelAPI;
		this.resolutionConfigurationAPI = resolutionConfigurationAPI;
		this.protocolsAPI = protocolsAPI;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {

		//Init Code
		Map<String,String> codeLabel = new HashMap<String, String>();

		for(CodeLabel label : codeLabelAPI.all()) {
			codeLabel.put(label.code, label.label);
		}

		for(ResolutionConfiguration resolConfs : resolutionConfigurationAPI.all()) {
			for(Resolution resolution : resolConfs.resolutions) {
				codeLabel.put(resolution.code, resolution.name);
			}
		}

		for(Protocol protocol : protocolsAPI.all()) {
			codeLabel.put(protocol.code, protocol.name);
		}

		codeLabel.put("UNSET", "---");
		codeLabel.put("TRUE", "Oui");
		codeLabel.put("FALSE", "Non");
		codeLabel.put("A", "Disponible");

		//Lecture fichier excel pour construire map code/label et map code/column selon le type
		//Get first Sheet for type and map
		XSSFSheet sheetHeader = workbook.getSheetAt(0);
		String type = sheetHeader.getSheetName();
		Logger.debug("Sheet Header name "+sheetHeader.getSheetName());
		Map<String,String> codeHeader = new HashMap<String, String>();
		Map<String,Integer> codeColumn = new HashMap<String, Integer>();

		sheetHeader.rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			codeHeader.put(row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue());
			codeColumn.put(row.getCell(0).getStringCellValue(), (int)row.getCell(2).getNumericCellValue());
		});

		//Creation du fichier de sortie
		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		Sheet sheet = wb.createSheet("Bilan "+type);

		//Ecriture des headers selon le type
		Row row = sheet.createRow(0);
		for(String key :codeHeader.keySet()) {
			row.createCell(codeColumn.get(key).intValue()).setCellValue(
					createHelper.createRichTextString(codeHeader.get(key)));
		}

		ContextValidation cv = ContextValidation.createUpdateContext("ngl-support");
		cv.putObject("Error", new ArrayList<String>());
		//Iteration sur les codes ReadSet
		XSSFSheet sheetReadSet = workbook.getSheetAt(1);
		Logger.debug("Sheet ReadSet name "+sheetReadSet.getSheetName());
		
		sheetReadSet.rowIterator().forEachRemaining(rowRS -> {
			boolean tneg=false;
			if(sheetReadSet.getSheetName().equals("ReadSetTN"))
				tneg=true;
			Logger.debug("T neg value "+tneg);
			String codeReadSet = rowRS.getCell(0).getStringCellValue();
			Logger.debug("ReadSet "+codeReadSet);
			//Find ReadSet in database
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
			//Create row
			if(readSet!=null) {
				Row rowOutput = sheet.createRow(rowRS.getRowNum()+1);
				//INFO ReadSet
				createCell(readSet.code, "readSetCode", codeColumn, rowOutput, createHelper);
				createCell(readSet.runCode, "runCode", codeColumn, rowOutput, createHelper);
				createCell(codeLabel.get(readSet.runTypeCode), "runTypeCode", codeColumn, rowOutput, createHelper);
				createCellDate(readSet.runSequencingStartDate, "runSequencingStartDate", codeColumn, rowOutput, createHelper);
				createCell(readSet.laneNumber.toString(), "laneNumber", codeColumn, rowOutput, createHelper);
				createCell(codeLabel.get(readSet.productionValuation.valid.toString()), "readSetValidQC", codeColumn, rowOutput, createHelper);
				createCell(convertSetCodeLabel(readSet.productionValuation.resolutionCodes,codeLabel), "readSetCompteRenduQC", codeColumn, rowOutput, createHelper);
				createCell(readSet.productionValuation.comment, "readSetEvalDetails", codeColumn, rowOutput, createHelper);
				createCell(codeLabel.get(readSet.bioinformaticValuation.valid.toString()), "readSetValidBioinfo", codeColumn, rowOutput, createHelper);
				createCell(convertSetCodeLabel(readSet.bioinformaticValuation.resolutionCodes,codeLabel), "readSetCompteRenduBioinfo", codeColumn, rowOutput, createHelper);

				createCell(codeLabel.get(readSet.state.code), "readsetState", codeColumn, rowOutput, createHelper);

				Logger.debug("run depot percent "+readSet.sampleOnContainer.percentage);
				createCellNumeric(readSet.sampleOnContainer.percentage, "runDepotPercent", codeColumn, rowOutput, createHelper);
				createCellNumeric((double)readSet.sampleOnContainer.containerConcentration.getValue()*1000, "runLaneConcentration", codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("novaseqFlowcellMode", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("devProdContext", readSet, codeColumn, rowOutput, createHelper);
				getTreatmentRGPropertiesNumeric("nbUsefulCycleRead1",readSet, codeColumn, rowOutput, createHelper);
				getTreatmentRGPropertiesNumeric("nbUsefulCycleRead2",readSet, codeColumn, rowOutput, createHelper);
				getTreatmentRGPropertiesNumeric("nbCluster",readSet, codeColumn, rowOutput, createHelper);

				//INFO ech. biologique
				createCell(readSet.sampleOnContainer.referenceCollab, "referenceCollab", codeColumn, rowOutput, createHelper);
				createCell(readSet.sampleOnContainer.taxonCode, "taxonCode", codeColumn, rowOutput, createHelper);
				createCell(readSet.sampleOnContainer.ncbiScientificName, "ncbiScientificName", codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("sizeFraction", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("depthOrLayer", readSet, codeColumn, rowOutput, createHelper);

				//INFO ech parent ADN
				getReadSetSampleOnContainerProperties("fromSampleCode", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerPropertiesLabel("fromSampleTypeCode", readSet, codeColumn, rowOutput, createHelper, codeLabel);
				getReadSetSampleOnContainerProperties("fromProjectCode", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("dnaTreatment", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("extractionBlankSampleCode", readSet, codeColumn, rowOutput, createHelper);

				//INFO ech courant amplicon
				//INFO ech. courant
				createCell(readSet.sampleOnContainer.sampleCode, "sampleCode", codeColumn, rowOutput, createHelper);
				createCell(codeLabel.get(readSet.sampleOnContainer.sampleTypeCode), "sampleTypeCode", codeColumn, rowOutput, createHelper);
				createCell(readSet.sampleOnContainer.projectCode, "projectCode", codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerPropertiesLabel("libProcessTypeCode", readSet, codeColumn, rowOutput, createHelper,codeLabel);
				getReadSetSampleOnContainerProperties("targetedRegion", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("amplificationPrimers", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("tagPcrBlank1SampleCode", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("tagPcrBlank2SampleCode", readSet, codeColumn, rowOutput, createHelper);

				//INFO banque amplicon
				getReadSetSampleOnContainerPropertiesNumeric("libraryInputQuantity", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerPropertiesLabel("libraryProtocol", readSet, codeColumn, rowOutput, createHelper, codeLabel);

				//Taille banque finale
				createCellNumeric(readSet.sampleOnContainer.properties.get("insertSize").getValue(), "sizeFinalBq", codeColumn, rowOutput, createHelper);
				
				String secondaryTag = null;
				String tag = null;
				if(readSet.sampleOnContainer.properties.containsKey("tag"))
					tag=readSet.sampleOnContainer.properties.get("tag").getValue().toString();
				if(readSet.sampleOnContainer.properties.containsKey("secondaryTag"))
					secondaryTag=readSet.sampleOnContainer.properties.get("secondaryTag").getValue().toString();
				if(tag!=null) {
					createCell(tag, "tag", codeColumn, rowOutput, createHelper);
					createCell(tag, "tagIteration2", codeColumn, rowOutput, createHelper);
				}
				if(secondaryTag!=null) {
					createCell(secondaryTag, "secondaryTagIteration", codeColumn, rowOutput, createHelper);
					createCell(secondaryTag, "secondaryTag", codeColumn, rowOutput, createHelper);
				}



				//INFO GRAND PARENT ECH. BIOLOGIQUE

				//Get grand parent sample ech biologique
				//Code sample courant = sample amplicon
				String sampleCodeAmplicon = readSet.sampleOnContainer.sampleCode;
				//Get path of sampleCodeAmplicon
				Sample sampleAmplicon = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCodeAmplicon);
				String sampleCodeGrandParent = sampleAmplicon.life.path.substring(1).split(",")[0];
				Sample sampleGrandParent = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCodeGrandParent);
				//Check sample grand parent not DNA
				if(sampleGrandParent.typeCode.equals("biological-sample")) {
					createCell(sampleCodeGrandParent, "sampleGrandParent", codeColumn, rowOutput, createHelper);
					createCell(codeLabel.get(sampleGrandParent.typeCode), "sampleTypeGrandParent", codeColumn, rowOutput, createHelper);
					createCell(convertSetString(sampleGrandParent.projectCodes), "projectGrandParent", codeColumn, rowOutput, createHelper);
					//Get container grand parent from none none none 
					Container containerGrandParent = MongoDBDAO.findOne(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
						DBQuery.and(DBQuery.notExists("fromPurificationTypeCode"),
								DBQuery.notExists("fromTransfertTypeCode"),
								DBQuery.or(DBQuery.size("fromTransformationTypeCodes", 0),
										DBQuery.notExists("fromTransformationTypeCodes")),
								DBQuery.in("sampleCodes", sampleCodeGrandParent)
								));
					if(containerGrandParent!=null) {
						createCell(containerGrandParent.code, "codeContainerGrandParent", codeColumn, rowOutput, createHelper);
						createCell(convertComment(containerGrandParent.comments),"commentContainerGrandParent", codeColumn, rowOutput, createHelper);
						createCell(codeLabel.get(containerGrandParent.state.code),"stateContainerGrandParent", codeColumn, rowOutput, createHelper);
						createCell(convertSetCodeLabel(containerGrandParent.state.resolutionCodes,codeLabel),"resolutionContainerGrandParent", codeColumn, rowOutput, createHelper);
						createCellDate(containerGrandParent.traceInformation.creationDate,"creationDateContainerGrandParent", codeColumn, rowOutput, createHelper);

						createCell(containerGrandParent.traceInformation.createUser,"creationUserContainerGrandParent", codeColumn, rowOutput, createHelper);
						createCell(containerGrandParent.support.storageCode, "storageContainerGrandParent", codeColumn, rowOutput, createHelper);
					}else {
						Logger.error("No grand parent for readset "+readSet.code);
						((List<String>)cv.getObject("Error")).add(readSet.code+",sampleCodeGrandParent,"+sampleCodeGrandParent+",No grand parent for readSet");
					}
				}
				//INFO PARENT ECH. ADN

				//Get sample Parent sample DNA
				String sampleCodeDNA = readSet.sampleOnContainer.properties.get("fromSampleCode").getValue().toString();
				//Get container parent DNA
				//FROM extractionDNA,NONE,NONE
				Logger.debug("Get Container DNA for sample DNA "+sampleCodeDNA);
				Logger.debug("Value tneg "+tneg);
				Container containerDNA = MongoDBDAO.findOne(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
						DBQuery.and(DBQuery.notExists("fromPurificationTypeCode"),
								DBQuery.notExists("fromTransfertTypeCode"),
								DBQuery.in("fromTransformationTypeCodes","dna-rna-extraction"),
								DBQuery.in("sampleCodes", sampleCodeDNA)
								));
				if(containerDNA==null) {
					//OR FROM NONE, NONE, NONE
					//cas des temoins neg si plusieurs containers alors prendre celui qui a une concentration si plusieurs containers mettre message erreur
					if(!tneg) {
					containerDNA = MongoDBDAO.findOne(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
							DBQuery.and(DBQuery.notExists("fromPurificationTypeCode"),
									DBQuery.notExists("fromTransfertTypeCode"),
									DBQuery.or(DBQuery.size("fromTransformationTypeCodes", 0),
											DBQuery.notExists("fromTransformationTypeCodes"),
											DBQuery.regex("fromTransformationTypeCodes", Pattern.compile("^ext-to-.+$"))),
									DBQuery.in("sampleCodes", sampleCodeDNA)
									));
					}else {
						List<Container> containersDNA = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
							DBQuery.and(DBQuery.notExists("fromPurificationTypeCode"),
									DBQuery.notExists("fromTransfertTypeCode"),
									DBQuery.or(DBQuery.size("fromTransformationTypeCodes", 0),
											DBQuery.notExists("fromTransformationTypeCodes"),
											DBQuery.regex("fromTransformationTypeCodes", Pattern.compile("^ext-to-.+$"))),
									DBQuery.in("sampleCodes", sampleCodeDNA)
									)).toList();
						if(containersDNA.size()==1)
							containerDNA=containersDNA.get(0);
						else if(containersDNA.size()>1) {
							List<Container> containersDNAConc = new ArrayList<Container>();
							for(Container containerDNAT : containersDNA) {
								if(containerDNAT.concentration!=null && containerDNAT.concentration.getValue()!=null) {
									containersDNAConc.add(containerDNAT);
								}
							}
							if(containersDNAConc.size()==1)
								containerDNA=containersDNAConc.get(0);
						}
					}
				}
				if(containerDNA!=null) {
					//createCell(containerDNA.code, "codeContainerParent", codeColumn, rowOutput, createHelper);
					createCell(containerDNA.code, "codeContainerDNA", codeColumn, rowOutput, createHelper);
					//createCell(convertSetCodeLabel(containerDNA.fromTransformationTypeCodes, codeLabel), "fromTransformationTypeParent", codeColumn, rowOutput, createHelper);
					createCell(convertSetCodeLabel(containerDNA.fromTransformationTypeCodes, codeLabel), "fromTransformationType", codeColumn, rowOutput, createHelper);
					//createCell(codeLabel.get(containerDNA.state.code), "stateContainerParent", codeColumn, rowOutput, createHelper);
					//createCell(convertSetCodeLabel(containerDNA.state.resolutionCodes,codeLabel), "resolutionContainerParent", codeColumn, rowOutput, createHelper);
					createCellDate(containerDNA.traceInformation.creationDate, "creationDateContainerParent", codeColumn, rowOutput, createHelper);
					createCell(containerDNA.traceInformation.createUser, "creationUserContainerParent", codeColumn, rowOutput, createHelper);
					createCell(convertComment(containerDNA.comments), "commentContainerParent", codeColumn, rowOutput, createHelper);
					//createCell(convertComment(containerDNA.comments), "commentContainerDNA", codeColumn, rowOutput, createHelper);
					createCell(containerDNA.support.storageCode, "storageContainerParent", codeColumn, rowOutput, createHelper);
				}else {
					Logger.error("No container DNA for readSet "+readSet.code);
					((List<String>)cv.getObject("Error")).add(readSet.code+",sampleCodeDNA,"+sampleCodeDNA+",No container DNA for readSet");
				}

				//Get experiment extraction ADN/ARN
				List<Experiment> expExtractions = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
						DBQuery.and(DBQuery.is("typeCode", "dna-rna-extraction"),
								DBQuery.in("outputContainerCodes",containerDNA.code))).toList();
				if(expExtractions.size()==1) {
					Experiment expExtraction = expExtractions.iterator().next();
					createCell(codeLabel.get(expExtraction.protocolCode), "extractionProtocolExperimentADN", codeColumn, rowOutput, createHelper);
				}else {
					Logger.error("No experiment or multiple experiment extraction ADN for DNA container "+containerDNA.code);
					((List<String>)cv.getObject("Error")).add(readSet.code+",containerDNA,"+containerDNA.code+",No experiment or multiple experiment extraction ADN for DNA container,"+expExtractions);
				}

				//Get instrumentUsedTypeCode QC du container ADN
				Logger.debug("Get qualityControl result for container DNA "+containerDNA.code);
				if(containerDNA.qualityControlResults!=null && containerDNA.qualityControlResults.size()>0) {
					//String listQC = containerDNA.qualityControlResults.stream().map(qc->codeLabel.get(qc.instrumentUsedTypeCode)).filter(i->i!=null).collect(Collectors.joining(","));
					//createCell(listQC, "instrumentTypeQCDNA", codeColumn, rowOutput, createHelper);
					
					List<String> dnaQCs = new ArrayList<String>();
					for(QualityControlResult qc : containerDNA.qualityControlResults) {
						Experiment expQC = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, qc.code);
						if(expQC!=null) {
							dnaQCs.add(codeLabel.get(expQC.instrument.typeCode));
						}
					}
					createCell(convertListString(dnaQCs), "instrumentTypeQCDNA", codeColumn, rowOutput, createHelper);
					
					String listCommentQC = containerDNA.qualityControlResults.stream().map(qc->qc.valuation.comment).filter(c->c!=null).collect(Collectors.joining(","));
					createCell(listCommentQC, "lisCommentEvalQCDNA", codeColumn, rowOutput, createHelper);
					containerDNA.qualityControlResults.sort((qc1, qc2) -> qc1.index.compareTo(qc2.index));
					//On prend le premier dosage fluo
					for(int i=0; i<containerDNA.qualityControlResults.size(); i++) {
						if(containerDNA.qualityControlResults.get(i).properties.containsKey("calculationMethod")) {
							createCell(containerDNA.qualityControlResults.get(i).properties.get("calculationMethod").getValue().toString(), "finalConcCalculMethodDNA", codeColumn, rowOutput, createHelper);
							//Create colonne rendementFirstFluo
							QualityControlResult qcr = containerDNA.qualityControlResults.get(i);
							String concentration1 ="";
							String volume1 = "";
							String quantity1 = "";
							
							if(qcr.properties.containsKey("concentration1")) {
								if(qcr.properties.get("concentration1").getValue() instanceof Integer)
									concentration1 = qcr.properties.get("concentration1").getValue().toString()+" "+((PropertySingleValue)qcr.properties.get("concentration1")).unit;
								else 
									concentration1 = getRoundValue((double)qcr.properties.get("concentration1").getValue())+" "+((PropertySingleValue)qcr.properties.get("concentration1")).unit;
								
							}
							if(qcr.properties.containsKey("volume1")) {
								if(qcr.properties.get("volume1").getValue() instanceof Integer)
									volume1 = qcr.properties.get("volume1").getValue().toString()+" "+((PropertySingleValue)qcr.properties.get("volume1")).unit;
								else
									volume1 = getRoundValue((double)qcr.properties.get("volume1").getValue())+" "+((PropertySingleValue)qcr.properties.get("volume1")).unit;
							}
							if(qcr.properties.containsKey("quantity1")) {
								if(qcr.properties.get("quantity1").getValue() instanceof Integer)
									quantity1 = qcr.properties.get("quantity1").getValue().toString()+" "+((PropertySingleValue)qcr.properties.get("quantity1")).unit;
								else	
									quantity1 = getRoundValue((double)qcr.properties.get("quantity1").getValue())+" "+((PropertySingleValue)qcr.properties.get("quantity1")).unit;
							}
							String pattern = "dd/MM/yyyy";
							SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
							String dateResult = simpleDateFormat.format(qcr.date);
							createCell(concentration1+","+volume1+","+quantity1+","+dateResult, "rendementFirstFluo", codeColumn, rowOutput, createHelper);
							//Create colonne validFirstFluo
							createCell(codeLabel.get(containerDNA.qualityControlResults.get(i).valuation.valid.toString()), "validFirstFluo", codeColumn, rowOutput, createHelper);
							break;
						}
					}
					

				}
				if(containerDNA.concentration!=null) {
					createCellNumeric(containerDNA.concentration.getValue(), "concentrationContainerDNA", codeColumn, rowOutput, createHelper);
					createCell(containerDNA.concentration.unit, "unitConcentrationContainerDNA", codeColumn, rowOutput, createHelper);
				}
				if(containerDNA.volume!=null) {
					createCellNumeric(containerDNA.volume.getValue(), "volumeContainerDNA", codeColumn, rowOutput, createHelper);
					createCell(containerDNA.volume.unit, "unitVolumeContainerDNA", codeColumn, rowOutput, createHelper);
				}
				if(containerDNA.quantity!=null) {
					createCellNumeric(containerDNA.quantity.getValue(), "quantityContainerDNA", codeColumn, rowOutput, createHelper);
					createCell(containerDNA.quantity.unit, "unitQuantityContainerDNA", codeColumn, rowOutput, createHelper);
				}
				createCell(codeLabel.get(containerDNA.state.code), "stateContainerDNA", codeColumn, rowOutput, createHelper);
				if(containerDNA.state.resolutionCodes!=null)
					createCell(convertSetCodeLabel(containerDNA.state.resolutionCodes, codeLabel), "resolutionContainerDNA", codeColumn, rowOutput, createHelper);
				createCell(codeLabel.get(containerDNA.valuation.valid.toString()), "validContainerDNA", codeColumn, rowOutput, createHelper);

				//Get path from flowcell
				String codeContainerFC = readSet.sampleOnContainer.containerCode;
				Container containerFC = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, codeContainerFC);
				List<String> pathContainer = new ArrayList<String>();
				for(String path : containerFC.treeOfLife.paths) {
					if(path.contains(","+containerDNA.code+",")) {
						pathContainer.add(path.substring(1));
					}
				}

				//Prendre le premier container from tag pcr et vérifier code sampleCodeAmplicon
				if(pathContainer.size()>1) {
					List<String> finalPathContainer = new ArrayList<String>();
					for(String path : pathContainer) {
						String[] tabPath = path.split(",");
						for(int i=0; i<tabPath.length;i++) {
							//Get container
							Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, tabPath[i]);
							if(container.fromTransformationTypeCodes!=null && container.fromTransformationTypeCodes.contains("tag-pcr")){
								if(container.sampleCodes.contains(sampleCodeAmplicon)) {
									finalPathContainer.add(path);
								}
								break;
							}
						}
					}
					pathContainer = new ArrayList<String>();
					pathContainer.addAll(finalPathContainer);
				}

				//si path toujours >1 on recherche container from dna-illumina-indexed-library
				//verifie si container a un content avec codesampleAmplicon, tag et tag secondaire
				if(pathContainer.size()>1) {
					List<String> finalPathContainer = new ArrayList<String>();
					for(String path : pathContainer) {
						String[] tabPath = path.split(",");
						for(int i=0; i<tabPath.length;i++) {
							//Get container
							Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, tabPath[i]);
							if(container.fromTransformationTypeCodes!=null && container.fromTransformationTypeCodes.contains("dna-illumina-indexed-library")){
								if(container.sampleCodes.contains(sampleCodeAmplicon)) {
									for(Content content : container.contents) {
										if(content.sampleCode.equals(sampleCodeAmplicon)) {
											if(tag!=null && secondaryTag!=null && content.properties.get("tag").getValue().toString().equals(tag) && content.properties.get("secondaryTag").getValue().toString().equals(secondaryTag)) {
												finalPathContainer.add(path);
											}else if(tag!=null && secondaryTag==null && content.properties.get("tag").getValue().toString().equals(tag)) {
												finalPathContainer.add(path);
											}
										}
									}
								}
								break;
							}
						}
					}
					pathContainer = new ArrayList<String>();
					pathContainer.addAll(finalPathContainer);
				}
				
				//INFO COURANT ECH. AMPLICON
				if(pathContainer.size()==1) {
					List<String> listContainer = Arrays.asList(pathContainer.iterator().next().split(","));
					//Get container amplicon
					List<Container> containerAmplicons = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
							DBQuery.and(DBQuery.notExists("fromPurificationTypeCode"),
									DBQuery.notExists("fromTransfertTypeCode"),
									DBQuery.or(DBQuery.in("fromTransformationTypeCodes","tag-pcr")),
									DBQuery.in("sampleCodes", sampleCodeAmplicon),
									DBQuery.in("code", listContainer)
									)).toList();
					if(containerAmplicons.size()==1) {
						Container containerAmplicon = containerAmplicons.iterator().next();

						if(containerAmplicon.qualityControlResults!=null && containerAmplicon.qualityControlResults.size()>0) {
							//String listQCAmplicon = containerAmplicon.qualityControlResults.stream().map(qc->codeLabel.get(qc.instrumentUsedTypeCode)).filter(i->i!=null).collect(Collectors.joining(","));
							//createCell(listQCAmplicon, "instrumentTypeQCAmplicon", codeColumn, rowOutput, createHelper);
							
							//Aller rechercher instrument dans les expérience de QC
							List<String> ampliconQCs = new ArrayList<String>();
							for(QualityControlResult qc : containerAmplicon.qualityControlResults) {
								Experiment expQC = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, qc.code);
								if(expQC!=null) {
									ampliconQCs.add(codeLabel.get(expQC.instrument.typeCode));
								}
							}
							createCell(convertListString(ampliconQCs), "instrumentTypeQCAmplicon", codeColumn, rowOutput, createHelper);
							
							String listCommentQCAmplicon = containerAmplicon.qualityControlResults.stream().map(qc->qc.valuation.comment).filter(c->c!=null).collect(Collectors.joining(","));
							createCell(listCommentQCAmplicon, "listCommentEvalQCAmplicon", codeColumn, rowOutput, createHelper);
							containerAmplicon.qualityControlResults.sort((qc1, qc2) -> qc1.index.compareTo(qc2.index));
							for(int i=containerAmplicon.qualityControlResults.size()-1; i>=0; i--) {
								if(containerAmplicon.qualityControlResults.get(i).properties.containsKey("calculationMethod")) {
									createCell(containerAmplicon.qualityControlResults.get(i).properties.get("calculationMethod").getValue().toString(), "finalConcCalculMethodAmplicon", codeColumn, rowOutput, createHelper);
									break;
								}
							}
						}
						if(containerAmplicon.concentration!=null) {
							createCellNumeric(containerAmplicon.concentration.getValue(), "concentrationContainerAmplicon", codeColumn, rowOutput, createHelper);
							createCell(containerAmplicon.concentration.unit, "unitConcentrationContainerAmplicon", codeColumn, rowOutput, createHelper);
						}
						if(containerAmplicon.volume!=null) {
							createCellNumeric(containerAmplicon.volume.getValue(), "volumeContainerAmplicon", codeColumn, rowOutput, createHelper);
							createCell(containerAmplicon.volume.unit, "unitVolumeContainerAmplicon", codeColumn, rowOutput, createHelper);
						}
						if(containerAmplicon.quantity!=null) {
							createCellNumeric(containerAmplicon.quantity.getValue(), "quantityContainerAmplicon", codeColumn, rowOutput, createHelper);
							createCell(containerAmplicon.quantity.unit, "unitQuantityContainerAmplicon", codeColumn, rowOutput, createHelper);
						}
						createCell(codeLabel.get(containerAmplicon.state.code), "stateContainerAmplicon", codeColumn, rowOutput, createHelper);
						if(containerAmplicon.state.resolutionCodes!=null)
							createCell(convertSetCodeLabel(containerAmplicon.state.resolutionCodes, codeLabel), "resolutionContainerAmplicon", codeColumn, rowOutput, createHelper);
						createCell(codeLabel.get(containerAmplicon.valuation.valid.toString()), "validContainerAmplicon", codeColumn, rowOutput, createHelper);

						//FROM TAG-PCR, NONE, NONE prendre seconde TAG PCR
						List<Experiment> expTagPCR = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
								DBQuery.and(DBQuery.is("typeCode", "tag-pcr"),
										DBQuery.in("outputContainerCodes", containerAmplicon.code))).toList();
						if(expTagPCR.size()==1) {
							Experiment tagPCR = expTagPCR.iterator().next();
							createCell(codeLabel.get(tagPCR.protocolCode), "tagPCRProtocolAmplicon", codeColumn, rowOutput, createHelper);
							getProperties(tagPCR.experimentProperties, "dnaPolymerase", "dnaPolymeraseAmplicon", codeColumn, rowOutput, createHelper);
							getProperties(tagPCR.experimentProperties, "nbCycles", "nbCyclesAmplicon", codeColumn, rowOutput, createHelper);

							//Get inputContainerUsed from container amplicon
							for(AtomicTransfertMethod atm : tagPCR.atomicTransfertMethods) {
								//OneToOne get OutputContainerUsed with code container amplicon
								InputContainerUsed icu = atm.inputContainerUseds.get(0);
								OutputContainerUsed ocu = atm.outputContainerUseds.get(0);
								if(ocu.code.equals(containerAmplicon.code)) {
									if(icu.experimentProperties.containsKey("nbPCR")) {
										createCellNumeric(icu.experimentProperties.get("nbPCR").getValue(), "nbPCRAmplicon", codeColumn, rowOutput, createHelper);
									}
									if(icu.experimentProperties.containsKey("inputQuantity")) {
										createCellNumeric(icu.experimentProperties.get("inputQuantity").getValue(), "inputTagPCRAmplicon", codeColumn, rowOutput, createHelper);
									}
									break;
								}
							}
						}else {
							Logger.error("Multiple tag pcr "+sampleCodeAmplicon);
							((List<String>)cv.getObject("Error")).add(readSet.code+",sampleCodeAmplicon,"+sampleCodeAmplicon+",Multiple tag pcr ,"+listContainer);
						}
					}else {
						Logger.error("Muliple container amplicon for sample "+sampleCodeAmplicon);
						((List<String>)cv.getObject("Error")).add(readSet.code+",sampleCodeAmplicon,"+sampleCodeAmplicon+",Muliple container amplicon for sample ,"+listContainer);
					}
					//Get container pool
					List<Container> containerPools = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
							DBQuery.and(DBQuery.notExists("fromPurificationTypeCode"),
									DBQuery.in("fromTransfertTypeCode","pool"),
									DBQuery.in("fromTransformationTypeCodes","tag-pcr"),
									DBQuery.in("sampleCodes", sampleCodeAmplicon),
									DBQuery.in("code", listContainer)
									)).toList();
					if(containerPools.size()==1) {
						Container containerPool = containerPools.iterator().next();
						for(Content content : containerPool.contents) {
							if(content.sampleCode.equals(sampleCodeAmplicon)) {
								if(secondaryTag!=null && content.properties.containsKey("secondaryTag") && 
										content.properties.get("secondaryTag").getValue().toString().equals(secondaryTag)) {
									createCellNumeric(content.percentage, "poolPercentageAmplicon", codeColumn, rowOutput, createHelper);
									break;
								}else {
									createCellNumeric(content.percentage, "poolPercentageAmplicon", codeColumn, rowOutput, createHelper);
									break;
								}

							}
						}
					}else if(containerPools.size()>1) {
						Logger.error("Multiple pool for "+sampleCodeAmplicon+" list container "+listContainer);
						((List<String>)cv.getObject("Error")).add(readSet.code+",sampleCodeAmplicon,"+sampleCodeAmplicon+",Multiple pool,"+listContainer);
					}
					List<String> bqInstruments = new ArrayList<String>();
					//Search Fragmentation experiment
					List<Experiment> expFragmentations = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
							DBQuery.and(DBQuery.is("typeCode", "fragmentation"),
									DBQuery.in("outputContainerCodes", listContainer))).toList();
					if(expFragmentations.size()==1) {
						Experiment expFrag = expFragmentations.iterator().next();
						bqInstruments.add(codeLabel.get(expFrag.instrument.typeCode));
					}else {
						Logger.error("Multiple experiment Fragmentation for code container "+sampleCodeAmplicon);
						((List<String>)cv.getObject("Error")).add(readSet.code+",sampleCodeAmplicon,"+sampleCodeAmplicon+",Multiple experiment Fragmentation,"+listContainer);
					}
					//Search banque illumina index experiment
					List<Experiment> expIlluminaBqs = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
							DBQuery.and(DBQuery.is("typeCode", "dna-illumina-indexed-library"),
									DBQuery.in("outputContainerCodes", listContainer))).toList();
					if(expIlluminaBqs.size()==1) {
						Experiment expBqDNA = expIlluminaBqs.iterator().next();
						createCell(codeLabel.get(expBqDNA.protocolCode), "bankDNAProtocolLong", codeColumn, rowOutput, createHelper);
						bqInstruments.add(codeLabel.get(expBqDNA.instrument.typeCode));
					}else {
						Logger.error("Multiple experiment Bq DNA for code container "+sampleCodeAmplicon);
						((List<String>)cv.getObject("Error")).add(readSet.code+",sampleCodeAmplicon,"+sampleCodeAmplicon+",Multiple experiment Bq DNA,"+listContainer);
					}
					//Search Amplification/PCR experiment
					List<Experiment> expAmpliPCRs = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
							DBQuery.and(DBQuery.is("typeCode", "pcr-amplification-and-purification"),
									DBQuery.in("outputContainerCodes", listContainer))).toList();
					if(expAmpliPCRs.size()==1) {
						Experiment expAmpliPCR = expAmpliPCRs.iterator().next();
						createCell(codeLabel.get(expAmpliPCR.protocolCode), "ampliPCRProtocolAmplicon", codeColumn, rowOutput, createHelper);
						createCell(expAmpliPCR.experimentProperties.get("dnaPolymerase").getValue().toString(), "ampliPCRDNAPolymeraseAmplicon", codeColumn, rowOutput, createHelper);
						createCellNumeric(expAmpliPCR.experimentProperties.get("nbCycles").getValue(), "ampliPCRNbCycleAmplicon", codeColumn, rowOutput, createHelper);
						bqInstruments.add(codeLabel.get(expAmpliPCR.instrument.typeCode));
						//Get inputContainerUsed from containerADN
						for(AtomicTransfertMethod atm : expAmpliPCR.atomicTransfertMethods) {
							//GET ATM OneToOne
							InputContainerUsed icu = atm.inputContainerUseds.get(0);
							OutputContainerUsed ocu = atm.outputContainerUseds.get(0);
							for(Content content : icu.contents) {
								if(content.sampleCode.equals(sampleCodeAmplicon)) {
									//Check tag and tag secondaire
									boolean checkTag=false;
									if(tag!=null && secondaryTag!=null && content.properties.get("tag").getValue().toString().equals(tag) && content.properties.get("secondaryTag").getValue().toString().equals(secondaryTag)) {
										checkTag=true;
									}else if(secondaryTag!=null && tag == null && content.properties.get("secondaryTag").getValue().toString().equals(secondaryTag)) {
										checkTag=true;
									}else if(tag!=null && secondaryTag==null && content.properties.get("tag").getValue().toString().equals(tag)) {
										checkTag=true;
									}else {
										checkTag=true;
									}
									if(checkTag) {
										if(icu.experimentProperties.containsKey("inputVolume"))
											createCellNumeric(icu.experimentProperties.get("inputVolume").getValue(), "ampliPCRInputVolumeAmplicon", codeColumn, rowOutput, createHelper);
										if(icu.experimentProperties.containsKey("inputQuantity"))
											createCellNumeric(icu.experimentProperties.get("inputQuantity").getValue(), "ampliPCRInputQuantityAmplicon", codeColumn, rowOutput, createHelper);
										if(icu.experimentProperties.containsKey("nbPCR"))
											createCellNumeric(icu.experimentProperties.get("nbPCR").getValue(), "ampliPCRNbPCRAmplicon", codeColumn, rowOutput, createHelper);
										if(ocu.experimentProperties.containsKey("PCRvolume"))
											createCellNumeric(ocu.experimentProperties.get("PCRvolume").getValue(), "ampliPCRVolumePCRAmplicon", codeColumn, rowOutput, createHelper);
										break;
									}
								}
							}
						}

					}else {
						Logger.error("Multiple experiment ampli PCR for code container "+sampleCodeAmplicon);
						((List<String>)cv.getObject("Error")).add(readSet.code+",sampleCodeAmplicon,"+sampleCodeAmplicon+",Multiple experiment ampli PCR,"+listContainer);
					}
					//Search Ampure POST/PCR
					List<Experiment> expPostAmpures = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
							DBQuery.and(DBQuery.is("typeCode", "post-pcr-ampure"),
									DBQuery.in("outputContainerCodes", listContainer))).toList();
					if(expPostAmpures.size()>0) {
						String listRatioAmpure="";
						for(Experiment expPostAmpure : expPostAmpures) {
							listRatioAmpure+=expPostAmpure.experimentProperties.get("adnBeadVolumeRatio").getValue().toString()+",";
							bqInstruments.add(codeLabel.get(expPostAmpure.instrument.typeCode));
						}
						listRatioAmpure = listRatioAmpure.substring(0, listRatioAmpure.lastIndexOf(","));
						createCell(listRatioAmpure, "ampliPCRRatioAmpureAmplicon", codeColumn, rowOutput, createHelper);
					}else {
						Logger.error("No Post ampure for code container "+sampleCodeAmplicon);
						((List<String>)cv.getObject("Error")).add(readSet.code+",sampleCodeAmplicon,"+sampleCodeAmplicon+",No Post ampure,"+listContainer);
					}

					createCell(convertListString(bqInstruments), "instrumentType", codeColumn, rowOutput, createHelper);
					//Get last container after last ampure
					List<Container> listContainerAfterAmpure = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
							DBQuery.and(DBQuery.in("fromPurificationTypeCode","post-pcr-ampure"),
									DBQuery.notExists("fromTransfertTypeCode"),
									DBQuery.in("fromTransformationTypeCodes","pcr-amplification-and-purification"),
									DBQuery.in("sampleCodes", sampleCodeAmplicon),
									DBQuery.in("code", listContainer)
									)).toList();
					listContainerAfterAmpure.sort(((c1, c2) -> c1.traceInformation.creationDate.compareTo(c2.traceInformation.creationDate)));
					Container lastContainerAmpure = listContainerAfterAmpure.get(listContainerAfterAmpure.size()-1);
					if(lastContainerAmpure.qualityControlResults!=null) {
						String listQCBqDNA = lastContainerAmpure.qualityControlResults.stream().map(qc->codeLabel.get(qc.typeCode)).filter(i->i!=null).collect(Collectors.joining(","));
						createCell(listQCBqDNA, "qcList", codeColumn, rowOutput, createHelper);
						String listCommentQCAmpure = lastContainerAmpure.qualityControlResults.stream().map(qc->qc.valuation.comment).filter(c->c!=null).collect(Collectors.joining(","));
						createCell(listCommentQCAmpure, "commentEvalQCList", codeColumn, rowOutput, createHelper);

						lastContainerAmpure.qualityControlResults.sort((qc1, qc2) -> qc1.index.compareTo(qc2.index));
						for(int i=lastContainerAmpure.qualityControlResults.size()-1; i>=0; i--) {
							if(lastContainerAmpure.qualityControlResults.get(i).properties.containsKey("calculationMethod")) {
								createCell(lastContainerAmpure.qualityControlResults.get(i).properties.get("calculationMethod").getValue().toString(), "finalConcCalculMethodAmpliPCR", codeColumn, rowOutput, createHelper);
								break;
							}
						}
					}
					if(lastContainerAmpure.concentration!=null) {
						createCellNumeric(lastContainerAmpure.concentration.getValue(), "concentrationContainerBqAmplicon", codeColumn, rowOutput, createHelper);
						createCell(lastContainerAmpure.concentration.unit, "unitConcentrationContaineBqrAmplicon", codeColumn, rowOutput, createHelper);
						if(lastContainerAmpure.concentration.unit.equals("ng/µl")) {
							if(lastContainerAmpure.size!=null) {
								double size = 0;
								if(lastContainerAmpure.size.getValue() instanceof Integer) {
									size = (Integer)lastContainerAmpure.size.getValue();
								}else {
									size = (Double)lastContainerAmpure.size.getValue();
								}

								Double concNM = 0.0;
								if(lastContainerAmpure.concentration.getValue() instanceof Double) {
									Double concentration = (Double)lastContainerAmpure.concentration.getValue();
									concNM = (Double)concentration / 660 / size * 1000000;
								}else{
									Integer concentration = (Integer)lastContainerAmpure.concentration.getValue();
									concNM = (double) (concentration / 660 / size * 1000000);
								}
								createCellNumeric(concNM, "ampliPCRConcTheoAmplicon", codeColumn, rowOutput, createHelper);
							}
						}else if(lastContainerAmpure.concentration.unit.equals("nM")) {
							createCellNumeric(lastContainerAmpure.concentration.getValue(), "ampliPCRConcTheoAmplicon", codeColumn, rowOutput, createHelper);
						}
					}
					if(lastContainerAmpure.volume!=null) {
						createCellNumeric(lastContainerAmpure.volume.getValue(), "volumeContainerBqAmplicon", codeColumn, rowOutput, createHelper);
						createCell(lastContainerAmpure.volume.unit, "unitVolumeContainerBqAmplicon", codeColumn, rowOutput, createHelper);
					}
					if(lastContainerAmpure.quantity!=null) {
						createCellNumeric(lastContainerAmpure.quantity.getValue(), "quantityContainerBqAmplicon", codeColumn, rowOutput, createHelper);
						createCell(lastContainerAmpure.quantity.unit, "unitQuantityContainerBqAmplicon", codeColumn, rowOutput, createHelper);
					}
					createCell(codeLabel.get(lastContainerAmpure.state.code), "stateContainerBqAmplicon", codeColumn, rowOutput, createHelper);
					if(lastContainerAmpure.state.resolutionCodes!=null)
						createCell(convertSetCodeLabel(lastContainerAmpure.state.resolutionCodes, codeLabel), "resolutionContainerBqAmplicon", codeColumn, rowOutput, createHelper);
					createCell(codeLabel.get(lastContainerAmpure.valuation.valid.toString()), "validContainerBqAmplicon", codeColumn, rowOutput, createHelper);
					createCellDate(lastContainerAmpure.traceInformation.creationDate, "creationDateContainerBqAmplicon", codeColumn, rowOutput, createHelper);
					createCell(convertComment(lastContainerAmpure.comments), "commentContainerBqAmplicon", codeColumn, rowOutput, createHelper);

					//Get expectedSize
					if(lastContainerAmpure.contents!=null && lastContainerAmpure.contents.size()>0) {
						for(Content content : lastContainerAmpure.contents) {
							if(content.sampleCode.equals(sampleCodeAmplicon)) {
								//Check tag and tag secondaire
								boolean checkTag=false;
								if(tag!=null && secondaryTag!=null && content.properties.get("tag").getValue().toString().equals(tag) && content.properties.get("secondaryTag").getValue().toString().equals(secondaryTag)) {
									checkTag=true;
								}else if(secondaryTag!=null && tag==null && content.properties.get("secondaryTag").getValue().toString().equals(secondaryTag)) {
									checkTag=true;
								}else if(tag!=null && secondaryTag==null && content.properties.get("tag").getValue().toString().equals(tag)) {
									checkTag=true;
								}else {
									checkTag=true;
								}

								if(checkTag) {
									if(content.properties.containsKey("expectedSize")) {
										createCellNumeric(content.properties.get("expectedSize").getValue(), "expectedSize", codeColumn, rowOutput, createHelper);
									}
								}
							}
						}
					}else {
						Logger.error("No content for lastContainerAmpure");
						((List<String>)cv.getObject("Error")).add(readSet.code+",sampleCodeAmplicon,"+sampleCodeAmplicon+",No content for lastContainerAmpure,"+listContainer);
					}
					
					//Get container for taille banque final 
					//Get all container from amplif and get common size
					//Get readset sampleOnContainer properties insertSize
					/*List<Container> listContainerAmplif = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
							DBQuery.and(DBQuery.in("fromTransformationTypeCodes","pcr-amplification-and-purification"),
									DBQuery.in("sampleCodes", sampleCodeAmplicon),
									DBQuery.in("code", listContainer)
									)).toList();
					Set<String> finalSize = new HashSet<String>();
					for(Container containerAmpli : listContainerAmplif) {
						if(containerAmpli.size!=null)
							finalSize.add(containerAmpli.size.getValue().toString());
					}
					createCell(convertSetString(finalSize), "sizeFinalBq", codeColumn, rowOutput, createHelper);*/


					//Get experiment prepa flowcell
					String codeExpFC = containerFC.treeOfLife.from.experimentCode;
					Experiment expFC = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, codeExpFC);
					if(expFC!=null) {
						for(AtomicTransfertMethod atm : expFC.atomicTransfertMethods) {
							for(OutputContainerUsed ocu : atm.outputContainerUseds) {
								if(ocu.code.equals(readSet.sampleOnContainer.containerCode)) {
									createCellNumeric(ocu.experimentProperties.get("phixPercent").getValue(), "phiXPercent", codeColumn, rowOutput, createHelper);
									break;
								}
							}
						}
					}else {
						Logger.error("No experiment FC for "+codeExpFC);
						((List<String>)cv.getObject("Error")).add(readSet.code+",codeExpFC,"+codeExpFC+",No experiment FC,"+listContainer);
					}
					//Get Analyses with readSetCode
					List<Analysis> listAnalysis = MongoDBDAO.find(InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, 
							DBQuery.in("readSetCodes", readSet.code)).toList();
					List<String> listCodeRSTNeg = new ArrayList<String>();
					for(Analysis analyse : listAnalysis) {
						listCodeRSTNeg.addAll(analyse.readSetCodes);
					}
					//exclude readsetCode from listCodeRSTNeg
					listCodeRSTNeg.remove(readSet.code);
					createCell(convertListString(listCodeRSTNeg), "readSetCodesTNeg", codeColumn, rowOutput, createHelper);
				}else {
					Logger.error("Muliple path for readSet "+readSet.code);
					Logger.debug("paths "+pathContainer);
					((List<String>)cv.getObject("Error")).add(readSet.code+",codeReadSet,"+codeReadSet+",Muliple path for readSet ");
				}
			}else {
				Logger.error("NO READSET FOUND "+codeReadSet);
				((List<String>)cv.getObject("Error")).add(codeReadSet+",codeExpFC,"+codeReadSet+",NO READSET FOUND");
			}

		});

		// Write the output to a file
		try (OutputStream fileOut = new FileOutputStream("TestBilan.xls")) {
			wb.write(fileOut);
		}catch(Exception e){
			Logger.debug(e.getMessage());
		}

		//Write the error file
		createExcelFileRecap(cv);

	}

	private void getReadSetSampleOnContainerProperties(String key, ReadSet readSet, Map<String,Integer> codeColumn, Row rowOutput, CreationHelper createHelper)
	{
		String value = "";
		if(readSet.sampleOnContainer.properties.containsKey(key))
			value=readSet.sampleOnContainer.properties.get(key).getValue().toString();
		createCell(value, key, codeColumn, rowOutput, createHelper);
	}

	private void getReadSetSampleOnContainerPropertiesLabel(String key, ReadSet readSet, Map<String,Integer> codeColumn, Row rowOutput, CreationHelper createHelper, Map<String, String> codeLabel)
	{
		String value = "";
		if(readSet.sampleOnContainer.properties.containsKey(key)) {
			value=codeLabel.get(readSet.sampleOnContainer.properties.get(key).getValue().toString());
		}
		createCell(value, key, codeColumn, rowOutput, createHelper);
	}

	private void getReadSetSampleOnContainerPropertiesNumeric(String key, ReadSet readSet, Map<String,Integer> codeColumn, Row rowOutput, CreationHelper createHelper)
	{
		Object value = null;
		if(readSet.sampleOnContainer.properties.containsKey(key))
			value=readSet.sampleOnContainer.properties.get(key).getValue();
		createCellNumeric(value, key, codeColumn, rowOutput, createHelper);
	}

	private void getProperties(Map<String, PropertyValue> properties, String key, String keyColumn, Map<String,Integer> codeColumn, Row rowOutput, CreationHelper createHelper) {
		if(properties.containsKey(key)) {
			rowOutput.createCell(codeColumn.get(keyColumn).intValue()).setCellValue(createHelper.createRichTextString(properties.get(key).getValue().toString()));
		}
	}
	private void createCell(String value, String key, Map<String,Integer> codeColumn, Row rowOutput, CreationHelper createHelper)
	{
		if(value!=null) {
			if(codeColumn.containsKey(key))
				rowOutput.createCell(codeColumn.get(key).intValue()).setCellValue(createHelper.createRichTextString(value));
			else
				Logger.error("NO KEY "+key);
		}
	}

	private double getRoundValue(double value)
	{
		BigDecimal instance = new BigDecimal(Double.toString((double)value));
		instance = instance.setScale(2, RoundingMode.HALF_UP);
		double valueRoundMath = instance.doubleValue();
		return valueRoundMath;
	}
	
	private void createCellNumeric(Object value, String key, Map<String,Integer> codeColumn, Row rowOutput, CreationHelper createHelper)
	{
		if(value!=null) {
			if(codeColumn.containsKey(key)) {
				Cell cell = rowOutput.createCell(codeColumn.get(key).intValue());

				if(value instanceof Double) {
					double valueRoundMath = getRoundValue((double)value);
					//double valueRoundMath = Math.round((double)value * 100.0) / 100.0;
					cell.setCellValue(valueRoundMath);
				}
				else if(value instanceof Long) {
					cell.setCellValue((long)value);
				}
				else {
					cell.setCellValue((int)value);
				}
				cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			}else
				Logger.error("NO KEY "+key);
		}
	}
	private void createCellDate(Date date, String key, Map<String,Integer> codeColumn, Row rowOutput, CreationHelper createHelper) {
		if(date!=null) {
			if(codeColumn.containsKey(key)) {
				Cell cell = rowOutput.createCell(codeColumn.get(key).intValue());
				String pattern = "dd/MM/yyyy";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
				String dateResult = simpleDateFormat.format(date);
				cell.setCellValue(dateResult);
			}else {
				Logger.error("NO KEY "+key);
			}
		}
	}

	/* private void getTreatmentRGProperties(String key, ReadSet readSet, Map<String,Integer> codeColumn, Row rowOutput, CreationHelper createHelper)
	{
		String value="";
		if(readSet.treatments.get("ngsrg").results.get("default").containsKey(key)) 
			value=readSet.treatments.get("ngsrg").results.get("default").get(key).getValue().toString();
		createCell(value, key, codeColumn, rowOutput, createHelper);

	} */

	private void getTreatmentRGPropertiesNumeric(String key, ReadSet readSet, Map<String,Integer> codeColumn, Row rowOutput, CreationHelper createHelper)
	{
		Object value=null;
		if(readSet.treatments.get("ngsrg").results.get("default").containsKey(key)) 
			value=readSet.treatments.get("ngsrg").results.get("default").get(key).getValue();
		createCellNumeric(value, key, codeColumn, rowOutput, createHelper);

	}


	private String convertComment(List<Comment> comments) {
		if(comments!=null && comments.size()>0)
			return comments.stream().map(c->c.comment).filter(c->c!=null).collect(Collectors.joining(","));
		else
			return "";

	}

	private String convertListString(List<String> listValue)
	{
		if(listValue!=null && listValue.size()>0) {
			return String.join(",", listValue);
		}
		return "";
	}
	private String convertSetString(Set<String> listValue)
	{
		if(listValue!=null && listValue.size()>0) {
			return String.join(",", listValue);
		}
		return "";
	}

	private String convertSetCodeLabel(Set<String> listCode, Map<String,String> codeLabel)
	{
		Set<String> listLabel = new HashSet<String>();
		if(listCode!=null && listCode.size()>0) {
			for(String code : listCode) {
				listLabel.add(codeLabel.get(code));
			}
		}
		return String.join(",", listLabel);
	}

	@SuppressWarnings("unchecked")
	private void createExcelFileRecap(ContextValidation cv)
	{
		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		for(String key : cv.getContextObjects().keySet()){
			Sheet sheet = wb.createSheet(key);
			List<String> recaps = (List<String>) cv.getObject(key);
			int nbLine=0;
			for(String recap : recaps){
				//Logger.debug(recap);
				Row row = sheet.createRow(nbLine);
				String[] tabRecap = recap.split(",");
				for(int i=0;i<tabRecap.length;i++){
					row.createCell(i).setCellValue(
							createHelper.createRichTextString(tabRecap[i]));
				}
				nbLine++;
			}
		}

		// Write the output to a file
		try (OutputStream fileOut = new FileOutputStream("Error.xls")) {
			wb.write(fileOut);
		}catch(Exception e){
			Logger.debug(e.getMessage());
		}
	}


}
