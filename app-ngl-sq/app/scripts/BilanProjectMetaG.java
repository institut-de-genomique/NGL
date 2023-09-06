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
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

/**
 * Script permettant de générer des bilans projet type MetaG NGL-3283 destiné au collaborateur et au matériel et méthode
 * Prend également en entrée une liste de code ReadSet
 * Les bilans MetaG contiennent entre autre les infos de préparation ADN, de readSet, de l'échantillon bio, échantillon parent...
 * Le bilan MetaG contiendra en plus les infos de préparation de la banque ADN
 * @author ejacoby
 *
 */
public class BilanProjectMetaG extends ScriptWithExcelBody{

	private final CodeLabelAPI codeLabelAPI;
	private final ResolutionConfigurationAPI resolutionConfigurationAPI;
	private final ProtocolsAPI protocolsAPI;

	@Inject
	public BilanProjectMetaG(CodeLabelAPI codeLabelAPI, ResolutionConfigurationAPI resolutionConfigurationAPI, ProtocolsAPI protocolsAPI) {
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
			String codeReadSet = rowRS.getCell(0).getStringCellValue();
			Logger.debug("ReadSet "+codeReadSet);
			//Find ReadSet in database
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
			//Create row
			if(readSet!=null) {
				Row rowOutput = sheet.createRow(rowRS.getRowNum()+1);


				//ReadSet information
				createCell(readSet.code, "readSetCode", codeColumn, rowOutput, createHelper);
				createCell(codeLabel.get(readSet.state.code), "readsetState", codeColumn, rowOutput, createHelper);
				createCell(readSet.runCode, "runCode", codeColumn, rowOutput, createHelper);
				createCell(codeLabel.get(readSet.runTypeCode), "runTypeCode", codeColumn, rowOutput, createHelper);
				createCellDate(readSet.runSequencingStartDate, "runSequencingStartDate", codeColumn, rowOutput, createHelper);
				createCell(readSet.laneNumber.toString(), "laneNumber", codeColumn, rowOutput, createHelper);
				createCell(codeLabel.get(readSet.productionValuation.valid.toString()), "readSetValidQC", codeColumn, rowOutput, createHelper);
				createCell(convertSetCodeLabel(readSet.productionValuation.resolutionCodes,codeLabel), "readSetCompteRenduQC", codeColumn, rowOutput, createHelper);
				createCell(readSet.productionValuation.comment, "readSetEvalDetails", codeColumn, rowOutput, createHelper);
				createCell(codeLabel.get(readSet.bioinformaticValuation.valid.toString()), "readSetValidBioinfo", codeColumn, rowOutput, createHelper);
				createCell(convertSetCodeLabel(readSet.bioinformaticValuation.resolutionCodes,codeLabel), "readSetCompteRenduBioinfo", codeColumn, rowOutput, createHelper);

				//INFO ech. biologique
				getReadSetSampleOnContainerProperties("sizeFraction", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("depthOrLayer", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("fromSampleCode", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerPropertiesLabel("fromSampleTypeCode", readSet, codeColumn, rowOutput, createHelper,codeLabel);
				getReadSetSampleOnContainerProperties("fromProjectCode", readSet, codeColumn, rowOutput, createHelper);
				createCell(readSet.sampleOnContainer.referenceCollab, "referenceCollab", codeColumn, rowOutput, createHelper);
				createCell(readSet.sampleOnContainer.taxonCode, "taxonCode", codeColumn, rowOutput, createHelper);
				createCell(readSet.sampleOnContainer.ncbiScientificName, "ncbiScientificName", codeColumn, rowOutput, createHelper);

				//INFO ech. courant ADN
				createCell(readSet.sampleOnContainer.sampleCode, "sampleCode", codeColumn, rowOutput, createHelper);
				createCell(codeLabel.get(readSet.sampleOnContainer.sampleTypeCode), "sampleTypeCode", codeColumn, rowOutput, createHelper);
				createCell(readSet.sampleOnContainer.projectCode, "projectCode", codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("dnaTreatment", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("extractionBlankSampleCode", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerPropertiesLabel("libProcessTypeCode", readSet, codeColumn, rowOutput, createHelper,codeLabel);

				//INFO ech banque ADN
				getReadSetSampleOnContainerPropertiesNumeric("frgInputQuantity", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerPropertiesNumeric("libraryInputQuantity", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerPropertiesLabel("libraryProtocol", readSet, codeColumn, rowOutput, createHelper,codeLabel);
				//Taille banque finale
				createCellNumeric(readSet.sampleOnContainer.properties.get("insertSize").getValue(), "sizeFinalBq", codeColumn, rowOutput, createHelper);

				createCellNumeric(readSet.sampleOnContainer.percentage, "runDepotPercent", codeColumn, rowOutput, createHelper);
				createCellNumeric((double)readSet.sampleOnContainer.containerConcentration.getValue()*1000, "runLaneConcentration", codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("novaseqFlowcellMode", readSet, codeColumn, rowOutput, createHelper);
				getReadSetSampleOnContainerProperties("devProdContext", readSet, codeColumn, rowOutput, createHelper);

				getTreatmentRGPropertiesNumeric("nbUsefulCycleRead1",readSet, codeColumn, rowOutput, createHelper);
				getTreatmentRGPropertiesNumeric("nbUsefulCycleRead2",readSet, codeColumn, rowOutput, createHelper);
				getTreatmentRGPropertiesNumeric("nbCluster",readSet, codeColumn, rowOutput, createHelper);

				String tag = null;
				if(readSet.sampleOnContainer.properties.containsKey("tag"))
					tag=readSet.sampleOnContainer.properties.get("tag").getValue().toString();
				if(tag!=null) {
					createCell(tag, "tag", codeColumn, rowOutput, createHelper);
					createCell(tag, "tagIteration1", codeColumn, rowOutput, createHelper);
					createCell(tag, "tagIteration2", codeColumn, rowOutput, createHelper);
				}

				//INFO PARENT ech. biologique

				//Get parent container 
				String sampleCodeParent = readSet.sampleOnContainer.properties.get("fromSampleCode").getValue().toString();
				//Cas MetaG parent NONE, NONE, NONE
				Container containerParent = MongoDBDAO.findOne(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
						DBQuery.and(DBQuery.notExists("fromPurificationTypeCode"),
								DBQuery.notExists("fromTransfertTypeCode"),
								DBQuery.or(DBQuery.size("fromTransformationTypeCodes", 0),
										DBQuery.notExists("fromTransformationTypeCodes")),
								DBQuery.in("sampleCodes", sampleCodeParent)
								));

				if(containerParent!=null) {
					createCell(containerParent.code, "codeContainerParent", codeColumn, rowOutput, createHelper);
					createCell(convertComment(containerParent.comments), "commentContainerParent", codeColumn, rowOutput, createHelper);
					createCell(codeLabel.get(containerParent.state.code), "stateContainerParent", codeColumn, rowOutput, createHelper);
					createCell(convertSetCodeLabel(containerParent.state.resolutionCodes,codeLabel), "resolutionContainerParent", codeColumn, rowOutput, createHelper);
					createCellDate(containerParent.traceInformation.creationDate, "creationDateContainerParent", codeColumn, rowOutput, createHelper);
					createCell(containerParent.traceInformation.createUser, "creationUserContainerParent", codeColumn, rowOutput, createHelper);
					createCell(containerParent.traceInformation.createUser, "creationUserContainerParent", codeColumn, rowOutput, createHelper);
					createCell(containerParent.support.storageCode, "storageContainerParent", codeColumn, rowOutput, createHelper);
				}



				//Info COURANT ech. ADN
				//Get container ADN
				Container containerADN=null;
				String sampleCode = readSet.sampleOnContainer.sampleCode;
				//recherche du container ADN a partir code sample courant avec NONE,NONE,NONE ou ExtracADN,NONE,NONE si extraction faite chez nous
				//FROM extractionDNA,NONE,NONE
				containerADN = MongoDBDAO.findOne(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
						DBQuery.and(DBQuery.notExists("fromPurificationTypeCode"),
								DBQuery.notExists("fromTransfertTypeCode"),
								DBQuery.in("fromTransformationTypeCodes","dna-rna-extraction"),
								DBQuery.in("sampleCodes", sampleCode)
								));
				if(containerADN==null){
					//OR FROM NONE, NONE, NONE
					containerADN = MongoDBDAO.findOne(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
							DBQuery.and(DBQuery.notExists("fromPurificationTypeCode"),
									DBQuery.notExists("fromTransfertTypeCode"),
									DBQuery.or(DBQuery.size("fromTransformationTypeCodes", 0),
											DBQuery.notExists("fromTransformationTypeCodes"),
											DBQuery.regex("fromTransformationTypeCodes", Pattern.compile("^ext-to-.+$"))),
									DBQuery.in("sampleCodes", sampleCode)
									));
				}
				if(containerADN!=null) {
					createCell(containerADN.code, "codeContainerDNA", codeColumn, rowOutput, createHelper);
					createCell(convertComment(containerADN.comments), "commentContainerDNA", codeColumn, rowOutput, createHelper);
					createCell(convertSetCodeLabel(containerADN.fromTransformationTypeCodes,codeLabel), "fromTransformationType", codeColumn, rowOutput, createHelper);


					//Get experiment extraction ADN/ARN
					List<Experiment> expExtractions = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, 
							DBQuery.and(DBQuery.is("typeCode", "dna-rna-extraction"),
									DBQuery.in("outputContainerCodes",containerADN.code))).toList();
					if(expExtractions.size()==1) {
						Experiment expExtraction = expExtractions.iterator().next();
						createCell(codeLabel.get(expExtraction.protocolCode), "extractionProtocolExperimentADN", codeColumn, rowOutput, createHelper);
					}else {
						Logger.error("No experiment or multiple experiment extraction ADN for DNA container "+containerADN.code);
						((List<String>)cv.getObject("Error")).add(readSet.code+",containerDNA,"+containerADN.code+",No experiment or multiple experiment extraction ADN for DNA container,"+expExtractions);
					}

					//Get instrumentUsedTypeCode QC du container ADN
					if(containerADN.qualityControlResults!=null && containerADN.qualityControlResults.size()>0) {
						//String listQC = containerADN.qualityControlResults.stream().map(qc->codeLabel.get(qc.instrumentUsedTypeCode)).filter(i->i!=null).collect(Collectors.joining(","));
						//createCell(listQC, "instrumentTypeQCDNA", codeColumn, rowOutput, createHelper);

						List<String> dnaQCs = new ArrayList<String>();
						for(QualityControlResult qc : containerADN.qualityControlResults) {
							Experiment expQC = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, qc.code);
							if(expQC!=null) {
								dnaQCs.add(codeLabel.get(expQC.instrument.typeCode));
							}
						}
						createCell(convertListString(dnaQCs), "instrumentTypeQCDNA", codeColumn, rowOutput, createHelper);

						String listCommentQC = containerADN.qualityControlResults.stream().map(qc->qc.valuation.comment).filter(c->c!=null).collect(Collectors.joining(","));
						createCell(listCommentQC, "listCommentEvalQCDNA", codeColumn, rowOutput, createHelper);
						containerADN.qualityControlResults.sort((qc1, qc2) -> qc1.index.compareTo(qc2.index));

						//On prend le premier dosage fluo
						for(int i=0; i<containerADN.qualityControlResults.size(); i++) {
							if(containerADN.qualityControlResults.get(i).properties.containsKey("calculationMethod")) {
								createCell(containerADN.qualityControlResults.get(i).properties.get("calculationMethod").getValue().toString(), "finalConcCalculMethodDNA", codeColumn, rowOutput, createHelper);
								//Create colonne rendementFirstFluo
								QualityControlResult qcr = containerADN.qualityControlResults.get(i);
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
								createCell(codeLabel.get(containerADN.qualityControlResults.get(i).valuation.valid.toString()), "validFirstFluo", codeColumn, rowOutput, createHelper);
								break;
							}
						}
					}
					if(containerADN.concentration!=null) {
						createCellNumeric(containerADN.concentration.getValue(), "concentrationContainerDNA", codeColumn, rowOutput, createHelper);
						createCell(containerADN.concentration.unit, "unitConcentrationContainerDNA", codeColumn, rowOutput, createHelper);
					}
					if(containerADN.volume!=null) {
						createCellNumeric(containerADN.volume.getValue(), "volumeContainerDNA", codeColumn, rowOutput, createHelper);
						createCell(containerADN.volume.unit, "unitVolumeContainerDNA", codeColumn, rowOutput, createHelper);
					}
					if(containerADN.quantity!=null) {
						createCellNumeric(containerADN.quantity.getValue(), "quantityContainerDNA", codeColumn, rowOutput, createHelper);
						createCell(containerADN.quantity.unit, "unitQuantityContainerDNA", codeColumn, rowOutput, createHelper);
					}
					createCell(codeLabel.get(containerADN.state.code), "stateContainerDNA", codeColumn, rowOutput, createHelper);
					if(containerADN.state.resolutionCodes!=null)
						createCell(convertSetCodeLabel(containerADN.state.resolutionCodes, codeLabel), "resolutionContainerDNA", codeColumn, rowOutput, createHelper);
					createCell(codeLabel.get(containerADN.valuation.valid.toString()), "validContainerDNA", codeColumn, rowOutput, createHelper);

					//INFO Banque ADN 

					//GET PATH
					//Recherche des experiment Fragmentation, Banque illumina, Amplification/PCR, Ampure PostPCR
					String codeContainerFC = readSet.sampleOnContainer.containerCode;
					Container containerFC = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, codeContainerFC);
					List<String> pathContainer = new ArrayList<String>();
					for(String path : containerFC.treeOfLife.paths) {
						if(path.contains(","+containerADN.code+",")) {
							pathContainer.add(path.substring(1));
						}
					}

					List<String> bqInstruments = new ArrayList<String>();
					if(pathContainer.size()==1) {
						List<String> listContainer = Arrays.asList(pathContainer.iterator().next().split(","));
						//Search Fragmentation experiment
						List<Experiment> expFragmentations = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
								DBQuery.and(DBQuery.is("typeCode", "fragmentation"),
										DBQuery.in("outputContainerCodes", listContainer))).toList();
						if(expFragmentations.size()==1) {
							Experiment expFrag = expFragmentations.iterator().next();
							//TODO valider avec Julie que c'est bien la propriété recherchée fragProperty
							if(expFrag.instrumentProperties.containsKey("covarisProgram"))
								createCell(expFrag.instrumentProperties.get("covarisProgram").getValue().toString(), "fragProperty", codeColumn, rowOutput, createHelper);
							if(expFrag.instrumentProperties.containsKey("program"))
								createCell(expFrag.instrumentProperties.get("program").getValue().toString(), "fragProperty", codeColumn, rowOutput, createHelper);

							bqInstruments.add(codeLabel.get(expFrag.instrument.typeCode));
						}else {
							Logger.error("Multiple experiment Fragmentation for code container "+containerADN.code);
							((List<String>)cv.getObject("Error")).add(readSet.code+",containerADN,"+containerADN.code+",Multiple experiment Fragmentation,"+listContainer);
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
							Logger.error("Multiple experiment Bq DNA for code container "+containerADN.code);
							((List<String>)cv.getObject("Error")).add(readSet.code+",containerADN,"+containerADN.code+",Multiple experiment Bq DNA,"+listContainer);
						}
						//Search Amplification/PCR experiment
						List<Experiment> expAmpliPCRs = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class,
								DBQuery.and(DBQuery.is("typeCode", "pcr-amplification-and-purification"),
										DBQuery.in("outputContainerCodes", listContainer))).toList();
						if(expAmpliPCRs.size()==1) {
							Experiment expAmpliPCR = expAmpliPCRs.iterator().next();
							createCell(codeLabel.get(expAmpliPCR.protocolCode), "ampliPCRProtocolDNA", codeColumn, rowOutput, createHelper);
							createCell(expAmpliPCR.experimentProperties.get("dnaPolymerase").getValue().toString(), "ampliPCRDNAPolymeraseDNA", codeColumn, rowOutput, createHelper);
							createCellNumeric(expAmpliPCR.experimentProperties.get("nbCycles").getValue(), "ampliPCRNbCycles", codeColumn, rowOutput, createHelper);
							bqInstruments.add(codeLabel.get(expAmpliPCR.instrument.typeCode));
							//Get inputContainerUsed from containerADN

							for(AtomicTransfertMethod atm : expAmpliPCR.atomicTransfertMethods) {
								//GET ATM OneToOne
								InputContainerUsed icuAmpli = atm.inputContainerUseds.get(0);
								OutputContainerUsed ocuAmpli = atm.outputContainerUseds.get(0);
								for(Content content : icuAmpli.contents) {
									if(content.sampleCode.equals(sampleCode)) {
										//Check tag
										boolean checkTag=false;
										if(tag!=null && content.properties.get("tag").getValue().toString().equals(tag)) {
											checkTag=true;
										}else if(tag == null) {
											checkTag=true;
										}
										if(checkTag) {
											if(icuAmpli.experimentProperties.containsKey("inputVolume"))
												createCellNumeric(icuAmpli.experimentProperties.get("inputVolume").getValue(), "ampliPCRInputVolumeDNA", codeColumn, rowOutput, createHelper);
											if(icuAmpli.experimentProperties.containsKey("inputQuantity"))
												createCellNumeric(icuAmpli.experimentProperties.get("inputQuantity").getValue(), "ampliPCRInputQuantityDNA", codeColumn, rowOutput, createHelper);
											if(icuAmpli.experimentProperties.containsKey("nbPCR"))
												createCellNumeric(icuAmpli.experimentProperties.get("nbPCR").getValue(), "ampliPCRNbPCRDNA", codeColumn, rowOutput, createHelper);
											if(ocuAmpli.experimentProperties.containsKey("PCRvolume"))
												createCellNumeric(ocuAmpli.experimentProperties.get("PCRvolume").getValue(), "ampliPCRVolumePCRDNA", codeColumn, rowOutput, createHelper);
											break;

										}
									}
								}
							}

						}else {
							Logger.error("Multiple experiment ampli PCR for code container "+containerADN.code);
							((List<String>)cv.getObject("Error")).add(readSet.code+",containerADN,"+containerADN.code+",Multiple experiment ampli PCR,"+listContainer);
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
							createCell(listRatioAmpure, "ampliPCRRatioAmpureDNA", codeColumn, rowOutput, createHelper);
						}else {
							Logger.error("No Post ampure for code container "+containerADN.code);
							((List<String>)cv.getObject("Error")).add(readSet.code+",containerADN,"+containerADN.code+",No Post ampure,"+listContainer);
						}

						createCell(convertListString(bqInstruments), "instrumentType", codeColumn, rowOutput, createHelper);
						//Get last container after last ampure
						List<Container> listContainerAfterAmpure = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
								DBQuery.and(DBQuery.in("fromPurificationTypeCode","post-pcr-ampure"),
										DBQuery.notExists("fromTransfertTypeCode"),
										DBQuery.in("fromTransformationTypeCodes","pcr-amplification-and-purification"),
										DBQuery.in("sampleCodes", sampleCode),
										DBQuery.in("code", listContainer)
										)).toList();
						listContainerAfterAmpure.sort(((c1, c2) -> c1.traceInformation.creationDate.compareTo(c2.traceInformation.creationDate)));
						Container lastContainerAmpure = listContainerAfterAmpure.get(listContainerAfterAmpure.size()-1);
						String listQCBqDNA = lastContainerAmpure.qualityControlResults.stream().map(qc->codeLabel.get(qc.typeCode)).filter(i->i!=null).collect(Collectors.joining(","));
						createCell(listQCBqDNA, "qcList", codeColumn, rowOutput, createHelper);
						String listCommentQCAmpure = lastContainerAmpure.qualityControlResults.stream().map(qc->qc.valuation.comment).filter(i->i!=null).collect(Collectors.joining(","));
						createCell(listCommentQCAmpure, "commentEvalQCList", codeColumn, rowOutput, createHelper);
						Logger.debug("get last container ampure "+lastContainerAmpure.code);
						lastContainerAmpure.qualityControlResults.sort((qc1, qc2) -> qc1.index.compareTo(qc2.index));
						for(int i=lastContainerAmpure.qualityControlResults.size()-1; i>=0; i--) {
							if(lastContainerAmpure.qualityControlResults.get(i).properties.containsKey("calculationMethod")) {
								createCell(lastContainerAmpure.qualityControlResults.get(i).properties.get("calculationMethod").getValue().toString(), "finalConcCalculMethodAmpliPCR", codeColumn, rowOutput, createHelper);
								break;
							}
						}

						if(lastContainerAmpure.concentration!=null) {
							createCellNumeric(lastContainerAmpure.concentration.getValue(), "concentrationContainerBqDNA", codeColumn, rowOutput, createHelper);
							createCell(lastContainerAmpure.concentration.unit, "unitConcentrationContainerBqDNA", codeColumn, rowOutput, createHelper);
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
										concNM = (double)concentration / 660 / size * 1000000;
									}else{
										Integer concentration = (Integer)lastContainerAmpure.concentration.getValue();
										concNM = (double) concentration / 660 / size * 1000000;
									}
									createCellNumeric(concNM, "ampliPCRConcTheoDNA", codeColumn, rowOutput, createHelper);
								}

							}else if(lastContainerAmpure.concentration.unit.equals("nM")) {
								createCellNumeric(lastContainerAmpure.concentration.getValue(), "ampliPCRConcTheoDNA", codeColumn, rowOutput, createHelper);
							}
						}
						if(lastContainerAmpure.volume!=null) {
							createCellNumeric(lastContainerAmpure.volume.getValue(), "volumeContainerBqDNA", codeColumn, rowOutput, createHelper);
							createCell(lastContainerAmpure.volume.unit, "unitVolumeContainerBqDNA", codeColumn, rowOutput, createHelper);
						}
						if(lastContainerAmpure.quantity!=null) {
							createCellNumeric(lastContainerAmpure.quantity.getValue(), "quantityContainerBqDNA", codeColumn, rowOutput, createHelper);
							createCell(lastContainerAmpure.quantity.unit, "unitQuantityContainerBqDNA", codeColumn, rowOutput, createHelper);
						}
						createCell(codeLabel.get(lastContainerAmpure.state.code), "stateContainerBqDNA", codeColumn, rowOutput, createHelper);
						if(lastContainerAmpure.state.resolutionCodes!=null)
							createCell(convertSetCodeLabel(lastContainerAmpure.state.resolutionCodes,codeLabel), "resolutionContainerBqDNA", codeColumn, rowOutput, createHelper);
						createCell(codeLabel.get(lastContainerAmpure.valuation.valid.toString()), "validContainerBqDNA", codeColumn, rowOutput, createHelper);
						createCellDate(lastContainerAmpure.traceInformation.creationDate, "creationDateContainerBqDNA", codeColumn, rowOutput, createHelper);
						createCell(convertComment(lastContainerAmpure.comments), "commentContainerBqDNA", codeColumn, rowOutput, createHelper);


						//Get expectedSize
						if(lastContainerAmpure.contents!=null && lastContainerAmpure.contents.size()>0) {
							for(Content content : lastContainerAmpure.contents) {
								if(content.sampleCode.equals(sampleCode)) {
									//Check tag and tag secondaire
									boolean checkTag=false;
									if(tag!=null && content.properties.get("tag").getValue().toString().equals(tag) ) {
										checkTag=true;
									}else if(tag==null) {
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
							((List<String>)cv.getObject("Error")).add(readSet.code+",sampleCodeDNA,"+sampleCode+",No content for lastContainerAmpure,"+listContainer);
						}

						//Get container for taille banque final 
						//Get all container from amplif and get common size
						//Remove get readSet.sampleOnContainer properties insertSize
						/*List<Container> listContainerAmplif = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
							DBQuery.and(DBQuery.in("fromTransformationTypeCodes","pcr-amplification-and-purification"),
									DBQuery.in("sampleCodes", sampleCode),
									DBQuery.in("code", listContainer)
									)).toList();
					Set<String> finalSize = new HashSet<String>();
					for(Container containerAmpli : listContainerAmplif) {
						if(containerAmpli.size!=null)
							finalSize.add(containerAmpli.size.getValue().toString());
					}
					createCell(convertSetString(finalSize), "sizeFinalBq", codeColumn, rowOutput, createHelper);*/


					}else {
						//TODO Recap erreur fichier
						Logger.error("ERROR multiple path");
						((List<String>)cv.getObject("Error")).add(readSet.code+",codeReadSet,"+codeReadSet+",Muliple path for readSet ");
					}



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
						((List<String>)cv.getObject("Error")).add(readSet.code+",codeExpFC,"+codeExpFC+",No experiment FC");
					}

				}else {
					Logger.error("No container DNA for readSet "+readSet.code);
					((List<String>)cv.getObject("Error")).add(readSet.code+",sampleCodeDNA,"+sampleCode+",No container DNA for readSet");
				}

			}else {
				Logger.error("NO READSET FOUND "+codeReadSet);
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


	private void createCell(String value, String key, Map<String,Integer> codeColumn, Row rowOutput, CreationHelper createHelper)
	{
		if(value!=null) {
			if(codeColumn.containsKey(key))
				rowOutput.createCell(codeColumn.get(key).intValue()).setCellValue(createHelper.createRichTextString(value));
			else
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
					//cell.setCellValue(new BigDecimal((double)value).setScale(2, RoundingMode.HALF_UP).doubleValue());
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
	private void getTreatmentRGProperties(String key, ReadSet readSet, Map<String,Integer> codeColumn, Row rowOutput, CreationHelper createHelper)
	{
		String value="";
		if(readSet.treatments.get("ngsrg").results.get("default").containsKey(key)) 
			value=readSet.treatments.get("ngsrg").results.get("default").get(key).getValue().toString();
		createCell(value, key, codeColumn, rowOutput, createHelper);

	}

	private void getTreatmentRGPropertiesNumeric(String key, ReadSet readSet, Map<String,Integer> codeColumn, Row rowOutput, CreationHelper createHelper)
	{
		Object value=null;
		if(readSet.treatments.get("ngsrg").results.get("default").containsKey(key)) 
			value=readSet.treatments.get("ngsrg").results.get("default").get(key).getValue();
		createCellNumeric(value, key, codeColumn, rowOutput, createHelper);

	}


	private String convertComment(List<Comment> comments) {
		if(comments!=null && comments.size()>0)
			return comments.stream().map(c->c.comment).collect(Collectors.joining(","));
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
