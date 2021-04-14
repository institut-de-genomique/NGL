package services.description.sample;

import static services.description.DescriptionFactory.newImportType;
import static services.description.DescriptionFactory.newPropertiesDefinition;
import static services.description.DescriptionFactory.newValues;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.common.description.dao.MeasureUnitDAO;
import models.laboratory.sample.description.ImportCategory;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.dao.ImportCategoryDAO;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;
import services.description.declaration.cns.MetaBarCoding;
import services.description.declaration.cns.Nanopore;
import services.description.declaration.cns.BanqueIllumina;

public class ImportServiceCNS extends AbstractImportService {

	public List<ImportCategory> getImportCategories() {
		List<ImportCategory> l = new ArrayList<>();
		//		l.add(saveImportCategory("Sample Import", "sample-import"));
		l.add(new ImportCategory("sample-import", "Sample Import"));
		return l;
	}

	@Override
	public void saveImportCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		DAOHelpers.saveModels(ImportCategory.class, getImportCategories(), errors);
	}

	public List<ImportType> getImportTypes() {
		List<ImportType> l = new ArrayList<>();
		ImportCategoryDAO icfind = ImportCategory.find.get();
		List<Institute> CNS = DescriptionFactory.getInstitutes(Constants.CODE.CNS);

		l.add(newImportType("Import bidon", "import-bidon",   icfind.findByCode("sample-import"), getImportBidonDefinitions(), CNS));

		//import-type for lims import
		l.add(newImportType("Defaut", 		"default-import", icfind.findByCode("sample-import"), getImportDefaultLimsDefinitions(), CNS));
		l.add(newImportType("Banque", 		"library", 		  icfind.findByCode("sample-import"), getLibraryPropertyDefinitions(), CNS));
		l.add(newImportType("Tara",        	"tara-default",   icfind.findByCode("sample-import"), getTaraPropertyDefinitions(), CNS));
		l.add(newImportType("Banque tara", 	"tara-library",   icfind.findByCode("sample-import"), getLibraryTaraPropertyDefinitions(), CNS));

		//import-type for NGL import
		l.add(newImportType("Reception Tara Pacific", "reception-tara-pacific", icfind.findByCode("sample-import"), getTaraPacReceptionPropertyDefinitions(), CNS));
		l.add(newImportType("Update Tara Pacific",    "update-tara-pacific",    icfind.findByCode("sample-import"), getTaraPacUpdatePropertyDefinitions(), CNS));

		l.add(newImportType("Reception d'échantillon biologique",           "biological-sample-reception",         icfind.findByCode("sample-import"), getBiologicalSamplePropertyDefinitions(), CNS));
		l.add(newImportType("Import échantillon biologique_projet DE NOVO", "de-novo-biological-sample-reception", icfind.findByCode("sample-import"), getDeNovoBiologicalSamplePropertyDefinitions(), CNS));

		l.add(newImportType("Reception d'ADN",                             "dna-reception",                 	  icfind.findByCode("sample-import"), getDNAReceptionPropertyDefinitions(),       CNS));
		l.add(newImportType("Reception d'ADN_projet DE NOVO",              "de-novo-dna-reception",            	  icfind.findByCode("sample-import"), getDenovoDNAReceptionPropertyDefinitions(), CNS));
		l.add(newImportType("Réception Lib. MGI", 							"mgi-library-reception", 			  icfind.findByCode("sample-import"), getMGILibraryReception(),					  CNS));

		l.add(newImportType("Reception d'Amplicon",                        "amplicon-reception",            	  icfind.findByCode("sample-import"), getAmpliconReceptionPropertyDefinitions(), CNS));
		l.add(newImportType("Import échantillons Amplicons à 'recibler' ", "amplicon-to-amplify-reception",		  icfind.findByCode("sample-import"), getAmpliconToAmplifyReceptionPropertyDefinitions(), CNS));
		l.add(newImportType("Reception d'ARN", 							   "rna-reception",                 	  icfind.findByCode("sample-import"), getRNAReceptionPropertyDefinitions(), CNS));
		l.add(newImportType("Reception Lib. ARN", 						   "rna-library-reception", 			  icfind.findByCode("sample-import"), getRNALibraryReceptionPropertyDefinitions(), CNS));
		l.add(newImportType("Reception Lib. ADN", 						   "dna-library-reception",				  icfind.findByCode("sample-import"), getDNALibraryReceptionPropertyDefinitions(), CNS));
		l.add(newImportType("Reception Lib. ADN sans index", 			   "dna-library-without-index-reception", icfind.findByCode("sample-import"), getDNALibraryWithoutIndexReceptionPropertyDefinitions(), CNS));
		l.add(newImportType("Reception Pool Lib.", 						   "pool-library-reception", 			  icfind.findByCode("sample-import"), getPoolLibraryReceptionPropertyDefinitions(), CNS));
		l.add(newImportType("Reception Lib. Amplicons", 				   "amplicon-library-reception", 		  icfind.findByCode("sample-import"), getAmpliconLibraryReceptionPropertyDefinitions(), CNS));
		l.add(newImportType("Import échantillons avec tag secondaire", "secondary-tag-sample-reception", icfind.findByCode("sample-import"), getSecondaryTagSampleReceptionPropertyDefinitions(), CNS));
		l.add(newImportType("Reception ADN (et SAG) Tara", "dna-reception-tara", icfind.findByCode("sample-import"), getTaraSagReceptionPropertyDefinitions(), CNS));
		l.add(newImportType("Import séquences extérieures", "external-readsets-import", icfind.findByCode("sample-import"), getExternalReadSetsReceptionPropertyDefinitions(), CNS));
		//Tara microPlastic
		l.add(newImportType("Reception Tara MP", 						   "reception-tara-mp", 			  icfind.findByCode("sample-import"), getTaraMPReceptionPropertyDefinitions(), CNS));
		l.add(newImportType("Update Tara MP", 						   "update-tara-mp", 			  icfind.findByCode("sample-import"), getTaraMPUpdatePropertyDefinitions(), CNS));
		
		
		
		return l;
	}

	@Override
	public void saveImportTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		DAOHelpers.saveModels(ImportType.class, getImportTypes(), errors);
	}

	private static List<PropertyDefinition> getImportBidonDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Type processus banque","libProcessTypeCode",LevelService.getLevels(Level.CODE.Content), String.class, false, getLibProcessTypeCodeValues(), "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Protocole banque","libraryProtocol",LevelService.getLevels(Level.CODE.Content), String.class, false, getLibraryProtocolValues(), "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Protocole bq RNA","rnaLibProtocol",LevelService.getLevels(Level.CODE.Content), String.class, false, getRnaLibraryProtocolValues(), "single"));

		return propertyDefinitions;
	}

	private static List<Value> getRnaLibraryProtocolValues() {
		return DescriptionFactory.newValues(
				"Smarter V4",
				"Ovation RNAseq system v2",
				"Smarter DEV",
				"TruSeq Stranded poly A",
				"TruSeq Stranded Proc",
				"Smarter Stranded",
				"RNA NEB_U2 Stranded Proc",
				"RNA NEB_U2 Stranded PolyA",
				"NEXTflex Small RNA",
				"Smarter smRNA",
				"InDA-C Ovation Universal RNA-Seq");


	}

	private static List<Value> getLibraryProtocolValues() {
		return DescriptionFactory.newValues(
				"R9-1D ligation",
				"R9-1D transposition",
				"R9-Long Read 1D",
				"R9-Long Read 2D",
				"R9-Low input",
				"R9-2D",
				"Banque 1D²",
				"MAP005",
				"MAP005 sur billes",
				"MAP006 low input",
				"MAP006",
				"R9-Long Read",
				"R9-1D",
				"direct RNAsequencing",
				"cDNA-PCR Sequencing",
				"Direct cDNA Sequencing",
				"Bq Super low cost",
				"Bq low cost",
				"Bq NEB Next Ultra II",
				"Bq NEB Reagent",
				"Bq Swift 1S",
				"Bq SAG LC",
				"Bq PCR free",
				"Bq Accel-NGS 2S plus DNA",
				"Bq Ovation Ultralow Systeme",
				"Bq QIAseq Ultralow Input",
				"Chromium 10x",
				"1D Genomic DNA by Ligation (SQK-LSK109) - PromethION",
				"1D Genomic DNA by Ligation (SQK-LSK109) - Flongle",
				"1D Genomic DNA by Ligation (SQK-LSK110)",
				"1D Genomic DNA by ligation (Kit 9 chemistry) - PromethION",
				"1D Genomic DNA by ligation (SQK-LSK108) - PromethION",
				"1D gDNA selecting for long reads (SQK-LSK108)",
				"1D gDNA selecting for long reads et long reads without BluePippin (SQK-LSK108)",
				"1D gDNA selecting for long reads (SQK-LSK109)",
				"1D gDNA long reads without BluePippin (SQK-LSK108)",
				"1D Native barcoding genomic DNA (with EXP-NBD103 and SQK-LSK108)",
				"1D Native barcoding genomic DNA (with EXP-NBD103 and SQK-LSK109)",
				"1D Native barcoding genomic DNA (with EXP-NBD104 and SQK-LSK109)",
				"cDNA-PCR Sequencing (SQK-PCS108)",
				"Direct cDNA Sequencing (SQK-DCS108)",
				"Direct RNA sequencing (SQK-RNA001)",
				"1D cDNA by ligation (SQK-LSK108)",
				"LSK109",
				"Bq Hi-C",
				"Bq Single Cell cDNA",
				"cDNA-PCR Sequencing (SQK-PCS109)",
				"Direct cDNA Sequencing (SQK-DCS109)",
				"Direct RNA sequencing (SQK-RNA002)",
				"metaB_primerFusion_DEV",
				"PCR Sequencing Kit (SQK-PSK004)",
				"PCR-cDNA Barecoding (SQK-PCB109)",
				"Rapid Barcoding Sequencing (SQK-RBK004)",
				"Rapid PCR Barcoding Kit (SQK-RPB004)",
				"Rapid Sequencing (SQK-RAD004)"
	
		);
	}

	// import automatique depuis Ancien LIMS
	private static List<Value> getLibProcessTypeCodeValues(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("A", "A - Mate-pair"));
		values.add(DescriptionFactory.newValue("B", "B - Multiplex-pairee"));
		values.add(DescriptionFactory.newValue("C", "C - Multiplex-mate-pair"));
		values.add(DescriptionFactory.newValue("D", "D - Digestion"));
		values.add(DescriptionFactory.newValue("E", "E - Paire chevauchant"));
		values.add(DescriptionFactory.newValue("F",	"F - Paire chevauchant multiplex"));
		values.add(DescriptionFactory.newValue("G", "G - Capture simple"));
		values.add(DescriptionFactory.newValue("H", "H - Capture multiplex"));
		values.add(DescriptionFactory.newValue("K", "K - Mate-pair clip"));
		values.add(DescriptionFactory.newValue("L", "L - Moleculo-like"));
		values.add(DescriptionFactory.newValue("M", "M - Multiplex"));
		values.add(DescriptionFactory.newValue("MI", "MI - Moleculo Illumina"));
		values.add(DescriptionFactory.newValue("P", "P - Pairee"));
		values.add(DescriptionFactory.newValue("S", "S - Simple"));
		values.add(DescriptionFactory.newValue("U", "U - Simple ou Paire"));
		values.add(DescriptionFactory.newValue("W", "W - Simple ou Paire multiplex"));
		values.add(DescriptionFactory.newValue("Z", "Z - BAC ENDs Illumina"));
		values.add(DescriptionFactory.newValue("DF", "DF - Hi-C"));
		values.add(DescriptionFactory.newValue("DI", "DI - Allegro Targeted Genotyping avec UMI"));
		
		return values;

	}




	private static List<PropertyDefinition> getImportDefaultLimsDefinitions() throws DAOException {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();

		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, true, null, null, "single", 1, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Code LIMS", "limsCode", LevelService.getLevels(Level.CODE.Container,Level.CODE.Sample),Integer.class, false, "single"));

		propertyDefinitions.add(newPropertiesDefinition("Taille associée au taxon", "taxonSize", LevelService.getLevels(Level.CODE.Content,Level.CODE.Sample),Long.class, false,MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), mufind.findByCode("pb"), mufind.findByCode("pb"), "single"));
		propertyDefinitions.add(newPropertiesDefinition("Taille d'insert", "insertSize", LevelService.getLevels(Level.CODE.Sample),Double.class, false,MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), mufind.findByCode("kb"), mufind.findByCode("kb"), "single"));
		propertyDefinitions.add(newPropertiesDefinition("Souche", "strain", LevelService.getLevels(Level.CODE.Sample),String.class, false, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Site clone", "cloneSite", LevelService.getLevels(Level.CODE.Sample),String.class, false, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Fragmenté", "isFragmented", LevelService.getLevels(Level.CODE.Sample),Boolean.class, false, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Adaptateurs", "isAdapters", LevelService.getLevels(Level.CODE.Sample),Boolean.class, false, "single"));

		return propertyDefinitions;
	}
	private static List<PropertyDefinition> getLibraryPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.addAll(getImportDefaultLimsDefinitions());
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.Content), String.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Catégorie Tag", "tagCategory", LevelService.getLevels(Level.CODE.Content), String.class, true, getTagCategories(), "single"));

		return propertyDefinitions;
	}

	private static List<Value> getTagCategories(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("SINGLE-INDEX", "SINGLE-INDEX"));
		values.add(DescriptionFactory.newValue("DUAL-INDEX", "DUAL-INDEX"));
		values.add(DescriptionFactory.newValue("MID", "MID"));
		values.add(DescriptionFactory.newValue("POOL-INDEX", "POOL-INDEX"));
		return values;	
	}

	private static List<PropertyDefinition> getTaraPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.addAll(getImportDefaultLimsDefinitions());
		propertyDefinitions.add(newPropertiesDefinition("Station TARA", "taraStation", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Integer.class, true, getTaraStationValues(), "single"));
		propertyDefinitions.add(newPropertiesDefinition("Nom Profondeur TARA", "taraDepth", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, true,getTaraDepthCodeValues(true), "single"));
		propertyDefinitions.add(newPropertiesDefinition("Profondeur Tara océans (et ACE)", "taraDepthCode", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, true, getTaraDepthCodeValues(false),"single"));
		propertyDefinitions.add(newPropertiesDefinition("Nom Filtre TARA", "taraFilter", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, true,getTaraFilterCodeValues(true), "single"));
		propertyDefinitions.add(newPropertiesDefinition("Filtre TARA", "taraFilterCode", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, true, getTaraFilterCodeValues(false),"single"));
		propertyDefinitions.add(newPropertiesDefinition("Iteration TARA", "taraIteration", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Materiel TARA", "taraSample", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Code Barre TARA", "taraBarCode", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, "single"));
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getTaraPacReceptionPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, true, null, 
				null, "single", 1, true, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Code Barre TARA", "taraBarCode", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, true,
				null, null, "single", 2, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Destination finale", "finalDestination", LevelService.getLevels(Level.CODE.Container), String.class, true, null, 
				getTaraPacificAndMPFinalDestination(), "single", 3, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Protocole TARA Pacific ou MP", "taraProtocol", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, true, null, 
				getTaraPacificAndMPProtocolValues(), "single", 4, true, null, null));
		/*keep when all data are not migrated*/
		propertyDefinitions.add(newPropertiesDefinition("Station TARA", "taraStation", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Integer.class, false, null, 
				null, "single", 5, true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, true, null, 
				null, "single", 5, true, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Environnement TARA Pacific", "taraEnvironment", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getTaraPacificEnvironmentValues(), "single", 6, true, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Provenance", "origin", LevelService.getLevels(Level.CODE.Container), String.class, false, null, 
				null, "single", 7, true, null, null));

		//Nouvelles prop d'import
		propertyDefinitions.add(newPropertiesDefinition("Ile", "taraIsland", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, "single", 8, true, null, null));		
		propertyDefinitions.add(newPropertiesDefinition("Colonie / Poisson", "taraColony", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, "single", 9, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Site", "taraSite", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, "single", 10, true, null, null));		

		propertyDefinitions.add(newPropertiesDefinition("OA", "taraOA", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, "single", 12, true, null, null));		

		propertyDefinitions.add(newPropertiesDefinition("Réplicat", "replicate", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getTaraPacificAndMPReplicateValues(), "single", 11, true, null, null));		

		return propertyDefinitions;
	}

	// Propriétés spécifiques projet Tara microplastique NGL-2667 NGL2630
	private static List<PropertyDefinition> getTaraMPReceptionPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//Container
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), 
				Date.class, true,null, null, "single", 1, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Destination finale", "finalDestination", LevelService.getLevels(Level.CODE.Container),
				String.class, true, null, getTaraPacificAndMPFinalDestination(), "single", 15, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Provenance", "origin", LevelService.getLevels(Level.CODE.Container), 
				String.class, false, null, null, "single", 2, true, null, null));

		
		//Sample
		propertyDefinitions.add(newPropertiesDefinition("Code Barre TARA", "taraBarCode", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), 
				String.class, true,null, null, "single", 3, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Offshore", "mpOffshore", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content),
				String.class, false,null, null, "single", 4, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("River", "mpRiver", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), 
				String.class, false,null, null, "single", 5, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Site", "taraSite", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), 
				String.class, false, null, null, "single", 6, true, null, null));		
		propertyDefinitions.add(newPropertiesDefinition("MP depth", "mpDepth", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), 
				String.class, false,null, null, "single", 7, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Protocole TARA Pacific ou MP", "taraProtocol", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content),
				String.class, false, null,getTaraPacificAndMPProtocolValues(), "single", 8, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Réplicat", "replicate", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), 
				String.class, false, null, getTaraPacificAndMPReplicateValues(), "single", 9, true, null, null));	
		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), 
				Boolean.class, true, null, null, "single", 10, true, null, null));
		
	
		return propertyDefinitions;
	}

	// Propriétés spécifiques projet Tara microplastique NGL-2667 NGL2630
	private static List<PropertyDefinition> getTaraMPUpdatePropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();


		//Container	
		//receptionDate Prop que l'on ne retrouve pas dans receptionConfig
		//A certainement été ajoutée pour vérifier qu'à la mise à jour du container la date de reception est bien renseignée
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, true, null,
				null, "single", 1, true, null, null));
	
		propertyDefinitions.add(newPropertiesDefinition("Destination finale", "finalDestination", LevelService.getLevels(Level.CODE.Container),
				String.class, false, null, getTaraPacificAndMPFinalDestination(), "single", 14, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Provenance", "origin", LevelService.getLevels(Level.CODE.Container), 
				String.class, false, null, null, "single", 15, true, null, null));

		
		//Sample
	
		propertyDefinitions.add(newPropertiesDefinition("Offshore", "mpOffshore", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content),
				String.class, false,null, null, "single", 2, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("River", "mpRiver", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), 
				String.class, false,null, null, "single", 3, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Site", "taraSite", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), 
				String.class, false, null, null, "single", 4, true, null, null));		
		propertyDefinitions.add(newPropertiesDefinition("MP depth", "mpDepth", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), 
				String.class, false,null, null, "single", 5, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Code Barre TARA", "taraBarCode", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content),
				String.class, true,null, null, "single", 6, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Protocole TARA Pacific ou MP", "taraProtocol", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content),
				String.class, false, null,getTaraPacificAndMPProtocolValues(), "single", 7, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Réplicat", "replicate", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), 
				String.class, false, null, getTaraPacificAndMPReplicateValues(), "single", 8, true, null, null));	
		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), 
				Boolean.class, false, null, null, "single", 13, true, null, null));
		
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getTaraPacUpdatePropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//Prop que l'on ne retrouve pas dans receptionConfig
		//A certainement été ajouté pour vérifier qu'à la mise à jour du container la date de reception est bien renseignée
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, true, null,
				null, "single", 1, true, null, null));

		

		/*keep when all data are not migrated*/
		propertyDefinitions.add(newPropertiesDefinition("Station TARA", "taraStation", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Integer.class, false, null, 
				null, "single", 2, true, null, null));
		/* OLD VERSION		
		propertyDefinitions.add(newPropertiesDefinition("Station TARA", "taraStation", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Integer.class, true, null, 
				getTaraPacificStationValues(), "single", 2, true, null, null));
		 */
		propertyDefinitions.add(newPropertiesDefinition("Filtre TARA", "taraFilterCode", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getTaraPacificFilterValues(), "single", 3, true, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Environnement TARA Pacific", "taraEnvironment", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getTaraPacificEnvironmentValues(), "single", 4, true, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Protocole TARA Pacific ou MP", "taraProtocol", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getTaraPacificAndMPProtocolValues(), "single", 5, true, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Destination finale", "finalDestination", LevelService.getLevels(Level.CODE.Container), String.class, false, null, 
				null, "single", 6, true, null, null));

		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, false, null, 
				null, "single",71, true, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Provenance", "origin", LevelService.getLevels(Level.CODE.Container), String.class, false, null, 
				null, "single", 8, true, null, null));

		//Nouvelles prop to update
		propertyDefinitions.add(newPropertiesDefinition("Ile", "taraIsland", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, "single", 9, true, null, null));		
		propertyDefinitions.add(newPropertiesDefinition("Colonie / Poisson", "taraColony", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, "single", 10, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Site", "taraSite", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, "single", 11, true, null, null));		

		propertyDefinitions.add(newPropertiesDefinition("OA", "taraOA", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, "single", 12, true, null, null));		

		/* OLD VERSION		
		propertyDefinitions.add(newPropertiesDefinition("Ile / Env", "taraIsland", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getTaraPacificIslandValues(), "single", 9, true, null, null));		
		propertyDefinitions.add(newPropertiesDefinition("Colonie / Poisson", "taraColony", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getTaraPacificColonyValues(), "single", 10, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Site", "taraSite", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getTaraPacificSiteValues(), "single", 11, true, null, null));		
		 */


		propertyDefinitions.add(newPropertiesDefinition("Réplicat", "replicate", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getTaraPacificAndMPReplicateValues(), "single", 12, true, null, null));		

		return propertyDefinitions;
	}

	//Les valeurs d'origine sont incrémentées de nouvelles valeurs spécifiques de Tara-µplastic
	private static List<Value> getTaraPacificAndMPReplicateValues() {
		List<Value> values = new ArrayList<>();

		for(int i = 1; i <= 15 ; i++){
			values.add(DescriptionFactory.newValue("R"+i, "R"+i));
		}
		for(int i = 1; i <= 9 ; i++){
			values.add(DescriptionFactory.newValue("R0"+i, "R0"+i));
		}

		values.add(DescriptionFactory.newValue("part 1", "part 1"));
		values.add(DescriptionFactory.newValue("part 2", "part 2"));
		values.add(DescriptionFactory.newValue("part 3", "part 3"));
		values.add(DescriptionFactory.newValue("parts 1-3", "parts 1-3"));
		values.add(DescriptionFactory.newValue("P01", "P01"));
		values.add(DescriptionFactory.newValue("P02", "P02"));
		values.add(DescriptionFactory.newValue("P03", "P03"));
		values.add(DescriptionFactory.newValue("P04", "P04"));
		values.add(DescriptionFactory.newValue("P05", "P05"));
		//TaraPac µplastiques 
		
		//values.add(DescriptionFactory.newValue("T", "T")); // Supprimé car notion ajoutée sur protocole
		//values.add(DescriptionFactory.newValue("G", "G")); // Supprimé car notion ajoutée sur protocole
		//NGL-2667
		values.add(DescriptionFactory.newValue("PEB", "PEB"));
		values.add(DescriptionFactory.newValue("PET", "PET"));
		values.add(DescriptionFactory.newValue("POM", "POM"));
		values.add(DescriptionFactory.newValue("PEF", "PEF"));
		values.add(DescriptionFactory.newValue("NYL", "NYL"));

		for(int i = 1; i <= 9 ; i++){
			values.add(DescriptionFactory.newValue("P0"+i, "P0"+i));
		}
		for(int i = 10; i <= 60 ; i++){
			values.add(DescriptionFactory.newValue("P"+i, "P"+i));
		}
		return values;		
	}

	//Les valeurs d'origine sont incrémentées de nouvelles valeurs spécifiques de Tara-µplastic
	private static List<Value> getTaraPacificAndMPFinalDestination() {
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("Genoscope", "Genoscope"));
		values.add(DescriptionFactory.newValue("Roscoff", "Roscoff"));
		values.add(DescriptionFactory.newValue("Monaco", "Monaco"));
		values.add(DescriptionFactory.newValue("Criobe", "Criobe"));
		values.add(DescriptionFactory.newValue("Monaco/Nice", "Monaco/Nice"));
		values.add(DescriptionFactory.newValue("New York", "New York"));
		values.add(DescriptionFactory.newValue("Oregon", "Oregon"));
		values.add(DescriptionFactory.newValue("Italy", "Italy"));
		values.add(DescriptionFactory.newValue("Villefranche", "Villefranche"));
		values.add(DescriptionFactory.newValue("Ohio", "Ohio"));
		values.add(DescriptionFactory.newValue("Weizmann", "Weizmann"));
		values.add(DescriptionFactory.newValue("LSCE", "LSCE"));
		values.add(DescriptionFactory.newValue("to-check", "to check"));
		values.add(DescriptionFactory.newValue("Nice", "Nice"));
		values.add(DescriptionFactory.newValue("Banyuls", "Banyuls"));
		values.add(DescriptionFactory.newValue("LOCEAN", "LOCEAN"));
		values.add(DescriptionFactory.newValue("KAUST", "KAUST"));
		//*plastic
		return values;	
	}

	//Les valeurs d'origine sont incrémentées de nouvelles valeurs spécifiques de Tara-µplastic
	private static List<Value> getTaraPacificAndMPProtocolValues(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("CORE", "IMG-(core)_Coral"));
		values.add(DescriptionFactory.newValue("CS10", "SEQ-(10g)a_Coral"));
		values.add(DescriptionFactory.newValue("CS40", "SEQ-(40g)_Coral"));
		values.add(DescriptionFactory.newValue("CS10", "SEQ-(10g)b_Coral"));
		values.add(DescriptionFactory.newValue("CS4L", "SEQ-(4g-lysing)_Coral"));
		values.add(DescriptionFactory.newValue("CS4", "SEQ-(4g)_Coral"));
		values.add(DescriptionFactory.newValue("CTAX", "IMG-(5g)_Coral"));
		values.add(DescriptionFactory.newValue("CREP", "IMG-(10g)_Coral"));
		values.add(DescriptionFactory.newValue("CTEM", "IMG-(1g)_Coral"));
		values.add(DescriptionFactory.newValue("MUC", "SEQ_Fish-Mucus"));
		values.add(DescriptionFactory.newValue("GT", "SEQ_Fish-Tract"));
		values.add(DescriptionFactory.newValue("GIL", "SEQ_Fish-Gill"));
		values.add(DescriptionFactory.newValue("FIN", "SEQ_Fish-Fin"));
		values.add(DescriptionFactory.newValue("OTO", "IMG_Fish-Otolith"));
		values.add(DescriptionFactory.newValue("SGS", "SEQ-IMG_SeaGrass"));
		values.add(DescriptionFactory.newValue("SGI", "IMG_SeaGrass"));
		values.add(DescriptionFactory.newValue("BDI", "IMG_BenthicDino"));
		values.add(DescriptionFactory.newValue("BDS", "SEQ_BenthicDino"));
		values.add(DescriptionFactory.newValue("S<02>", "SEQ_CW<0.22"));
		values.add(DescriptionFactory.newValue("S<02", "SEQ-(FeCl3)_W<0.22"));
		values.add(DescriptionFactory.newValue("FCM", "IMG-(gluta+poloxamer)_W<20"));
		values.add(DescriptionFactory.newValue("LIVE", "IMG-(live)_CW<20"));
		values.add(DescriptionFactory.newValue("SCG", "SEQ-IMG-(GB)_W<20"));
		values.add(DescriptionFactory.newValue("SEM", "IMG-(dry)_W0.22-20"));
		values.add(DescriptionFactory.newValue("FISH", "IMG-FISH-(dry)_W0.22-20"));
		values.add(DescriptionFactory.newValue("S023", "SEQ-(50L-or-15min)_W0.22-3"));
		values.add(DescriptionFactory.newValue("S320", "SEQ-(50L-or-15min)_W3-20"));
		values.add(DescriptionFactory.newValue("H20", "IMG-(gluta+pfa)_N20-2000"));
		values.add(DescriptionFactory.newValue("LIVE20", "IMG-(live)_N20-2000"));
		values.add(DescriptionFactory.newValue("S20", "SEQ-(500mL-or-15min)_N20-2000"));
		values.add(DescriptionFactory.newValue("E20", "SEQ-IMG-(ethanol)_N20-2000"));
		values.add(DescriptionFactory.newValue("SCG20", "SEQ-IMG-(GB)_N20-2000"));
		values.add(DescriptionFactory.newValue("HPLC", "BGC-PIGMENTS_W>0.7"));
		values.add(DescriptionFactory.newValue("CARB", "BGC-CARBONATE_W<>"));
		values.add(DescriptionFactory.newValue("NUT", "BGC-NUTRIENTS_W<>"));
		values.add(DescriptionFactory.newValue("SSED", "SEQ-SED"));
		values.add(DescriptionFactory.newValue("SSED-shield", "SSED-shield"));
		values.add(DescriptionFactory.newValue("SSED-unknown", "SSED-unknown"));	
		values.add(DescriptionFactory.newValue("SSED-etoh", "SSED-etoh"));

		values.add(DescriptionFactory.newValue("F20", "IMG-(formol)_N20-2000"));
		values.add(DescriptionFactory.newValue("L20", "IMG-(lugol)_N20-2000"));
		values.add(DescriptionFactory.newValue("F200", "IMG-(formol)_N>200"));
		values.add(DescriptionFactory.newValue("S300", "SEQ-(500mL-or-15min)_N300-2000"));
		values.add(DescriptionFactory.newValue("E300", "SEQ-IMG-(ethanol)_N300-2000"));
		values.add(DescriptionFactory.newValue("F300", "IMG-(formol)_N>300"));
		values.add(DescriptionFactory.newValue("F2000", "IMG-(formol)_N>2000"));
		values.add(DescriptionFactory.newValue("IRON", "BGC-IRON_W<>"));
		values.add(DescriptionFactory.newValue("SAL", "BGC-SALINITY_W<>"));
		values.add(DescriptionFactory.newValue("ABS", "BGC-AEROSOL_A>0.7"));
		values.add(DescriptionFactory.newValue("AI", "IMG-AEROSOL_A>0.7"));
		values.add(DescriptionFactory.newValue("AS", "SEQ-AEROSOL_A>0.7"));
		values.add(DescriptionFactory.newValue("CDIV", "CDIV"));
		values.add(DescriptionFactory.newValue("CCA", "Crustose Coralline algae"));

		values.add(DescriptionFactory.newValue("CCA-A", "CCA-A"));
		values.add(DescriptionFactory.newValue("CCA-D", "CCA-D"));
		values.add(DescriptionFactory.newValue("CCA-F", "CCA-F"));
		values.add(DescriptionFactory.newValue("CCA-S", "CCA-S"));

		values.add(DescriptionFactory.newValue("E2000", "SEQ-IMG-ethanol>2000"));
		//new 17/11/2017
		values.add(DescriptionFactory.newValue("TMETAL", "TMETAL"));
		values.add(DescriptionFactory.newValue("S03>", "S03>"));
		values.add(DescriptionFactory.newValue("S3", "S3"));

		values.add(DescriptionFactory.newValue("SCB", "SCB"));
		values.add(DescriptionFactory.newValue("SCB20-(FSW)", "SCB20-(FSW)"));
		values.add(DescriptionFactory.newValue("SCB20-(FSW+GB)", "SCB20-(FSW+GB)"));
		values.add(DescriptionFactory.newValue("SCB20-(TC)", "SCB20-(TC)"));
		values.add(DescriptionFactory.newValue("SCB200-(FSW)", "SCB200-(FSW)"));
		values.add(DescriptionFactory.newValue("SCB200-(TC)", "SCB200-(TC)"));
		values.add(DescriptionFactory.newValue("E300", "E300"));
		values.add(DescriptionFactory.newValue("SWA", "SEAWATER AEROSOLS"));
		
		values.add(DescriptionFactory.newValue("UF10-20", "UF10-20"));
		values.add(DescriptionFactory.newValue("UF3-20", "UF3-20"));
		values.add(DescriptionFactory.newValue("UF02-20", "UF02-20"));
		values.add(DescriptionFactory.newValue("UF<02", "UF<02"));
		//New NGL-3135
		values.add(DescriptionFactory.newValue("FOR-F", "FOR-F"));
		values.add(DescriptionFactory.newValue("MLG-A", "MLG-A"));
		values.add(DescriptionFactory.newValue("POC-A", "POC-A"));
		
		//TaraPac µplastiques
	//GS 05/09/2019 NGL-2661 proto supprimé, jamais utilisé	values.add(DescriptionFactory.newValue("S325", "S325"));
		values.add(DescriptionFactory.newValue("S325-T", "S325-T"));
		values.add(DescriptionFactory.newValue("S325-G", "S325-G"));
		values.add(DescriptionFactory.newValue("S023-T", "S023-T"));
		values.add(DescriptionFactory.newValue("S023-G", "S023-G"));
		values.add(DescriptionFactory.newValue("S25", "S25"));
		values.add(DescriptionFactory.newValue("S300P", "S300P"));
		values.add(DescriptionFactory.newValue("CG", "CG"));
		values.add(DescriptionFactory.newValue("HPK", "HPK"));
		values.add(DescriptionFactory.newValue("HPS", "HPS"));
		
		values.add(DescriptionFactory.newValue("Undefined", "Undefined"));

		return values;	
	}



	private static List<Value> getTaraPacificEnvironmentValues(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("AW", "Ambient Water"));
		values.add(DescriptionFactory.newValue("IW", "Interstitial Water"));
		values.add(DescriptionFactory.newValue("OA", "Ocean Atmosphere"));
		values.add(DescriptionFactory.newValue("CSW", "Coral Surounding Water"));
		values.add(DescriptionFactory.newValue("SRF", "Surface"));
		values.add(DescriptionFactory.newValue("AIR", "AIR"));
		values.add(DescriptionFactory.newValue("SED", "SED"));

		return values;	
	}



	private static List<Value> getTaraPacificFilterValues(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("S<02", "< 0,2 µm"));
		values.add(DescriptionFactory.newValue("S023", "0,2-3 µm"));
		values.add(DescriptionFactory.newValue("S320", "3-20 µm"));
		values.add(DescriptionFactory.newValue("S20", "20-200 µm"));
		values.add(DescriptionFactory.newValue("S300", "> 300µm"));
		values.add(DescriptionFactory.newValue("ASEQ", "ASEQ"));
		return values;	
	}

	/**
	 * Common property definitions of DNAReception
	 * @return list of property definitions
	 * @throws DAOException
	 */
	private static List<PropertyDefinition> commonDNAReceptionPropertyDefinitions() throws DAOException {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//		propertyDefinitions.add(newPropertiesDefinition(name, 						code, 				  levels, 														 type, 			required, requiredState, values, propertyValueType, displayOrder, editable, defaultValue, displayFormat)
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", 		"receptionDate", 	  LevelService.getLevels(Level.CODE.Container), 				 Date.class, 	true, 	  null, 		 null, 	 "single", 		  	1, 			  false, 	null, 		  null));
		propertyDefinitions.add(newPropertiesDefinition("META",              		"meta", 			  LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content), Boolean.class, true, 	  null, 		 null,   "single", 		  	2, 			  true, 	null, 		  null));
		propertyDefinitions.add(newPropertiesDefinition("% GC théorique",           "theoricalGCPercent", LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content), Double.class,  false, 	  null, 		 null,   "single",        	1, 			  false, 	null,         null));

		//		propertyDefinitions.add(newPropertiesDefinition(name, 								   code, 				  		levels, 													   type, 		  required, requiredState, values, 														measureCategory, 													   displayMeasureUnit, 			      saveMeasureUnit, 				     propertyValueType, displayOrder, editable, defaultValue, displayFormat)
		propertyDefinitions.add(newPropertiesDefinition("Code éch. témoin négatif extraction", "extractionBlankSampleCode", LevelService.getLevels(Level.CODE.Content), 				   String.class,  false, 	null,		   null, 														null, 																   null, 							  null,								 "single", 			22, 		  true, 	null,		  null));
		propertyDefinitions.add(newPropertiesDefinition("Contrôle négatif", 				   "negativeControl", 			LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content),  Boolean.class, false, 	null, 		   null, 														null,																   null,							  null,								 "single", 			14,    		  true, 	null,		  null));
		propertyDefinitions.add(newPropertiesDefinition("Taille associée au taxon", 		   "taxonSize", 		  		LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content), Long.class, 	  false,    null,		   null,   														MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), mufind.findByCode("pb"), mufind.findByCode("pb"), "single", 		 	1, 		   	  false, 	null, 		  null));
		propertyDefinitions.add(newPropertiesDefinition("Méthode préparation ADN (ou ARN)",    "dnaTreatment", 				LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content),  String.class,  false, 	null,		   DescriptionFactory.newValues("MDA","size selection using ampure","EtOH-reconcentrated","WGA", "SAG","plug"), 						null, 							  										null, 							  null,                              "single", 			18, 		  true, 	null,		  null));
		return propertyDefinitions;
	}


	private static List<PropertyDefinition> getDNAReceptionPropertyDefinitions() throws DAOException {

		List<PropertyDefinition> propertyDefinitions = commonDNAReceptionPropertyDefinitions();
		propertyDefinitions.add(newPropertiesDefinition("Nom organisme / collaborateur", "collabScientificName", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 17, false, null,null));		
		return propertyDefinitions;
	}


	private static List<PropertyDefinition> getDenovoDNAReceptionPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = commonDNAReceptionPropertyDefinitions();
		propertyDefinitions.add(newPropertiesDefinition("Nom organisme / collaborateur", "collabScientificName", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, true, null, 
				null, null,null,null,"single", 17, false, null,null));		
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getMGILibraryReception() throws DAOException{
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date de réception","receptionDate",LevelService.getLevels(Level.CODE.Container),Date.class,true,
				null,null,"single",1,false,null,null));
		//TODO a valider avec Julie
		List<Value> libProcessTypeCodes = new ArrayList<>();
		libProcessTypeCodes.add(DescriptionFactory.newValue("DG", "DG - Bq MGI DNA universal"));
		libProcessTypeCodes.add(DescriptionFactory.newValue("DH", "DH - Bq MGI DNA PCR free"));
		propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Content), String.class, true, 
				null,libProcessTypeCodes,null,null,null,"single", 18, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.Content), String.class, true, 
				null, null, null,null,null,"single", 14, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Liste tags primaires", "expectedPrimaryTags", LevelService.getLevels(Level.CODE.Content), String.class, true, 
				null, null, null,null,null,"single", 16, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.Content), String.class, true, 
				null,getTagCategories(), null,null,null,"single", 15, false, null,null));
		
		return propertyDefinitions;
	}



	private static List<PropertyDefinition> getTaraSagReceptionPropertyDefinitions() throws DAOException {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();

		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, true, null, null, "single", 1, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, true, null, null, "single", 2, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("% GC théorique", "theoricalGCPercent", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Double.class, false, null, null, "single", 1, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Taille associée au taxon", "taxonSize", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Long.class, false, null, 
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), mufind.findByCode("pb"), mufind.findByCode("pb"), "single", 1, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Nom organisme / collaborateur", "collabScientificName", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 17, false, null,null));		

		propertyDefinitions.add(newPropertiesDefinition("Méthode préparation ADN (ou ARN)", "dnaTreatment", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null,
				DescriptionFactory.newValues("MDA","size selection using ampure","EtOH-reconcentrated","WGA", "SAG","plug"), null, null, null,"single", 18, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Code Barre TARA", "taraBarCode", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, true,
				null, null, "single", 2, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Station TARA", "taraStation", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Integer.class, true, getTaraStationValues(), "single"));
		propertyDefinitions.add(newPropertiesDefinition("Profondeur Tara océans (et ACE)", "taraDepthCode", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, true, getTaraDepthCodeValues(false),"single"));

		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getBiologicalSamplePropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date de réception",              "receptionDate",        LevelService.getLevels(Level.CODE.Container),                  Date.class,    true,  null, null,                   "single", 1, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("META",                           "meta",                 LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content), Boolean.class, true,  null, null,                   "single", 2, true,  null, null));
		propertyDefinitions.add(newPropertiesDefinition("Nom organisme / collaborateur", "collabScientificName", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content),  String.class,  false, null, null, null, null, null, "single", 5, false, null, null));
		//propertyDefinitions.add(newPropertiesDefinition("% GC théorique", "theoricalGCPercent", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Double.class, false, null, null, "single", 3, false, null, null));
		//	propertyDefinitions.add(newPropertiesDefinition("Taille associée au taxon", "taxonSize", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Long.class, false, null, 
		//		null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), MeasureUnit.find.findByCode("pb"), MeasureUnit.find.findByCode("pb"), "single", 4, false, null, null));

		List<Value> sizeFractionValues = newValues("<0.2 µm",
				"0.2-2 µm",
				"0.2-3 µm",
				">0.2 µm",
				"0.22-3 µm",
				"0.22-200 µm",
				"<0.45 µm",
				">0.45 µm",
				">0.70 µm",
				"2-20 µm",
				">2 µm",
				"3-20 µm",
				">3 µm",
				"5-1000 µm",
				">20 µm",
				"20-200 µm",
				"20-250 µm",
				"200-2000 µm",
				////NGL-2680 Ajout pour projet ACE
				"0.2-40 µm",
				"sterivex"
				);

		List<Value> depthOrLayerValues = newValues("-790 m",
				"-750 m",
				"-75 m",
				"-40 m",
				"-50 m à 0 m",
				"0 m",
				"0.1 cm",
				"0-1 cm",
				"1-2 cm",
				"1-3 cm",
				"2-3 cm",
				"3-4 cm",
				"3-5 cm",
				"4-5 cm",
				"5-6 cm",
				"5-7 cm",
				"6-7 cm",
				"7-8 cm",
				"8-9 cm",
				"9-10 cm",
				"5-10 cm",
				"10-12.5 cm",
				"12.5-15 cm",
				"10-15 cm",
				"15-17.5 cm",
				"15-20 cm",
				"15-21 cm",
				"15-22 cm",
				"15-23 cm",
				"15-27 cm",
				"17.5-20 cm",
				"20-22.5 cm",
				"22.5-25 cm",
				"25-27.5 cm",
				"27.5-30 cm",
				"15-30 cm",
				"30-32.5 cm",
				"32.5-35 cm",
				"30-35 cm",
				"30-45 cm",
				"35-40 cm",
				"40-45 cm",			
				"0-10 m",
				"0-50 m",
				"0-200 m",
				"Voir fraction de taille");

		propertyDefinitions.add(newPropertiesDefinition("Fraction de taille",  "sizeFraction",    LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content), String.class,  false, null, sizeFractionValues, null, null, null, "single", 6, false, null, null));		
		propertyDefinitions.add(newPropertiesDefinition("Profondeur / Couche", "depthOrLayer",    LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content), String.class,  false, null, depthOrLayerValues, null, null, null, "single", 6, false, null, null));		
		propertyDefinitions.add(newPropertiesDefinition("Contrôle négatif",    "negativeControl", LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content), Boolean.class, false, null, null,               null, null, null, "single", 5, true,  null, null));		
		//NGL-2680 Ajout pour ACE
		propertyDefinitions.add(newPropertiesDefinition("Profondeur Tara océans (et ACE)", "taraDepthCode", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, getTaraDepthCodeValues(false),"single"));

		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getDeNovoBiologicalSamplePropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, true, null, null, "single", 1, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, true, null, null, "single", 2, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Nom organisme / collaborateur", "collabScientificName", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, true, null, 
				null, null,null,null,"single", 3, false, null,null));		
		propertyDefinitions.add(newPropertiesDefinition("Origine", "origin", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 4, false, null,null));		

		propertyDefinitions.add(newPropertiesDefinition("Contrôle négatif", "negativeControl", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, false, null, 
				null, null,null,null,"single", 5, true, null,null));		

		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getAmpliconReceptionPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, true, null, null, "single", 1, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, true, null, null, "single", 2, true, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Amorces", "amplificationPrimers", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getAmplificationPrimers(), null, null, null,"single", 2, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Région ciblée", "targetedRegion", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getTargetedRegion(), null, null, null,"single", 3, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Taille amplicon attendue", "expectedAmpliconSize", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null,null,null,null,"single", 16, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Issu du type d'échantillon", "fromSampleTypeCode", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				DescriptionFactory.newValues("DNA","RNA","aerosol"),null,null,null,"single", 16, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Nom organisme / collaborateur", "collabScientificName", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 17, false, null,null));		
		//inject with drools rule
		propertyDefinitions.add(newPropertiesDefinition("Contrôle négatif", "negativeControl", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, false, null, 
				null, null,null,null,"single", 20, true, null,null));	

		propertyDefinitions.add(newPropertiesDefinition("Code éch. témoin négatif PCR (1)", "tagPcrBlank1SampleCode", LevelService.getLevels(Level.CODE.Content), String.class, false, null,
				null, null, null, null,"single", 21, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Code éch. témoin négatif PCR (2)", "tagPcrBlank2SampleCode", LevelService.getLevels(Level.CODE.Content), String.class, false, null,
				null, null, null, null,"single", 22, true, null,null));

		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getAmpliconToAmplifyReceptionPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate",
				LevelService.getLevels(Level.CODE.Container), Date.class, true, null, null, "single", 1, false, null,
				null));
		propertyDefinitions.add(
				newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content),
						Boolean.class, true, null, null, "single", 2, true, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Amorces (origine)", "originAmplificationPrimers",
				LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content), String.class, true, null,
				getAmplificationPrimers(), null, null, null, "single", 5, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Région ciblée (origine)", "originTargetedRegion",
				LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content), String.class, true, null,
				getTargetedRegion(), null, null, null, "single", 3, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Taille amplicon attendue (origine)",
				"originExpectedAmpliconSize", LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content),
				String.class, true, null, null, null, null, null, "single", 6, true, null, null));
		propertyDefinitions
		.add(newPropertiesDefinition("Issu du type d'échantillon (origine)", "fromOriginSampleTypeCode",
				LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content), String.class, true, null,
				DescriptionFactory.newValues("DNA", "ARN"), null, null, null, "single", 4, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Nom organisme / collaborateur", "collabScientificName",
				LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content), String.class, false, null, null, null,
				null, null, "single", 17, false, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Contrôle négatif", "negativeControl",
				LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content), Boolean.class, false, null, null, null,
				null, null, "single", 20, true, null, null));

		return propertyDefinitions;
	}


	private List<PropertyDefinition> getRNAReceptionPropertyDefinitions() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, true, null, null, "single", 1, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, true, null, null, "single", 2, false, null, null));	
		propertyDefinitions.add(newPropertiesDefinition("Nom organisme / collaborateur", "collabScientificName", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 17, false, null,null));		

		propertyDefinitions.add(newPropertiesDefinition("Code éch. témoin négatif extraction", "extractionBlankSampleCode", LevelService.getLevels(Level.CODE.Content), String.class, false, null,
				null, null, null, null,"single", 19, true, null,null));


		propertyDefinitions.add(newPropertiesDefinition("Contrôle négatif", "negativeControl", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, false, null, 
				null, null,null,null,"single", 20, true, null,null));	
		return propertyDefinitions;
	}

	private List<PropertyDefinition> getRNALibraryReceptionPropertyDefinitions() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, true, null, null, "single", 1, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, true, null, null, "single", 2, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.Content), String.class, true, null, 
				null, null,null,null,"single", 14, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.Content), String.class, true, null, 
				getTagCategories(), null,null,null,"single", 15, false, null,null));		
		propertyDefinitions.add(newPropertiesDefinition("Orientation brin synthétisé","strandOrientation", LevelService.getLevels(Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 16, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Méthode synthèse cDNA","cDNAsynthesisType", LevelService.getLevels(Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 17, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Protocole bq RNA","rnaLibProtocol", LevelService.getLevels(Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 18, false, null,null));

		return propertyDefinitions;
	}

	private List<PropertyDefinition> getDNALibraryWithoutIndexReceptionPropertyDefinitions() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();

		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, true, null, null, "single", 1, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, true, null, null, "single", 2, false, null, null));

		propertyDefinitions.add(newPropertiesDefinition("% GC théorique", "theoricalGCPercent", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Double.class, false, null, null, "single", 6, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Taille associée au taxon", "taxonSize", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Long.class, false, null, 
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), mufind.findByCode("pb"), mufind.findByCode("pb"), "single", 7, false, null, null));

		return propertyDefinitions;
	}

	private List<PropertyDefinition> getDNALibraryReceptionPropertyDefinitions() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();

		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, true, null, null, "single", 1, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, true, null, null, "single", 2, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.Content), String.class, true, null, 
				null, null,null,null,"single", 14, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.Content), String.class, true, null, 
				getTagCategories(), null,null,null,"single", 15, false, null,null));		
		propertyDefinitions.add(newPropertiesDefinition("% GC théorique", "theoricalGCPercent", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Double.class, false, null, null, "single", 6, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Taille associée au taxon", "taxonSize", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Long.class, false, null, 
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), mufind.findByCode("pb"), mufind.findByCode("pb"), "single", 7, false, null, null));

		return propertyDefinitions;
	}

	private List<PropertyDefinition> getAmpliconLibraryReceptionPropertyDefinitions() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();

		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, true, null, null, "single", 1, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, true, null, null, "single", 2, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.Content), String.class, true, null, 
				null, null,null,null,"single", 14, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.Content), String.class, true, null, 
				getTagCategories(), null,null,null,"single", 15, false, null,null));		

		propertyDefinitions.add(newPropertiesDefinition("Nom organisme / collaborateur", "collabScientificName", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 17, false, null,null));		
		propertyDefinitions.add(newPropertiesDefinition("Contrôle négatif", "negativeControl", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, false, null, 
				null, null,null,null,"single", 20, true, null,null));	

		List<Value> libProcessTypeCodes = new ArrayList<>();
		libProcessTypeCodes.addAll(MetaBarCoding.getBanqueProcessTypeMetaTA());
		libProcessTypeCodes.addAll(MetaBarCoding.getBanqueProcessTypeMetaTB());
		libProcessTypeCodes.addAll(Nanopore.getLibProcessTypeCodeValues());


		propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Content), String.class, true, null,
				libProcessTypeCodes,null,null,null,"single", 18, false, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Taille théorique sizing", "expectedSize", LevelService.getLevels(Level.CODE.Content), String.class, false, null,
				DescriptionFactory.newValues("280-310 (F300)","400-550 (ITS2)","450-550 (W500)","550-650 (W600)","500-650","550-700 (ITS2)","600-700 (W700)","650-750 (W700)","650-700 (W700)","650-800","700-800 (W800)","750-800", "autre"),  MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),mufind.findByCode( "pb"),mufind.findByCode( "pb"),"single", 20, true, null,null));

		propertyDefinitions.add(newPropertiesDefinition("Amorces", "amplificationPrimers", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getAmplificationPrimers(),null, null, null,"single", 3, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Région ciblée", "targetedRegion", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getTargetedRegion(), null, null, null,"single", 4, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Taille amplicon attendue", "expectedAmpliconSize", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null,null,null,null,"single", 5, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Issu du type d'échantillon", "fromSampleTypeCode", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				DescriptionFactory.newValues("DNA","RNA"),null,null,null,"single", 6, true, null, null));

		return propertyDefinitions;
	}
	
	/* TO DO OLD VALUES need clean
	private static List<Value> getAmplificationPrimers() {
		return DescriptionFactory.newValues("Fuhrman primer", "V9 primer", "16S primer + Fuhrman primer", "ITS2 primer",
				"ITSintfor2 / ITS-Reverse", "SYM_VAR_5.8S2 / SYM_VAR_REV", "ITSD / ITS2REV", "CP23S primers",
				"5.8S F1 / R1", "18S_V4 primer", "COI primer m1COIintF / jgHCO2198", "COI primer LCO1490/ HC02198",
				"Sneed2015 27F / 519Rmodbio", "16SV4V5 Archae", "16SV5V6 Prok", "18SV1V2 Metazoaire",
				"16SV4 Procaryote", "16SFL", "18S FungiF390/FungiR1");
	}
	 */

	private static List<Value> getAmplificationPrimers() {
		return DescriptionFactory.newValues("16S FL 27F/1390R + Fuhrman primers",
				"16S FL 27F/1390R",
				"16S FL 27F/1492R",
				"16S FL 27F/1492R + Fuhrman primers",
				"16S FL W18/W02",
				"16S V1V2V3 Prok Sneed2015 27F/519Rmodbio",
				"16S V4 Prok 515FF/806R",
				"16S V4V5 Archae 517F/958R",
				"16S V5V6 Prok 784F/1061R", 
				"16S V3 F342/V4R802",
				"18S FungiF390/FungiR1",
				"18S V1V2 Metazoaire SSUF04/SSURmod",
				"18S V4 Euk V4f (TAReukF1)/V4r (TAReukR)",
				"18S V9 1389F/1510R",
				"5.8S F1/R1",
				"COI primers LCOI1490/HC022198",
				"COI primers m1COIintF/jgHCO2198",
				"CP23S primers",
				"Fuhrman primers",
				"ITS2/SYM_VAR_5.8S2/SYM_VAR_REV",
				"ITSD/ITS2REV",
				"ITSintfor2/ITS-Reverse",
				"ITS3f/ITS4_KYO1",
				"16S V3F479/V4R888",
				"18S V4F515F/V4R951",
				"A-18Sfull_length F / B-18Sfull_length R",
				"12S MiFish-U F / 12S MiFish-U R",
				"Marinefish-12SrRNA F / Marinefish-12SrRNA R",
				"Mix12S_Marinefish-MiFish F / Mix12S_Marinefish-MiFish R"
				);
	}


	/*
	private static List<Value> getOriginAmplificationPrimers() {
		return DescriptionFactory.newValues("Fuhrman primer", "V9 primer", "16S primer + Fuhrman primer",
				"ITSintfor2 / ITS-Reverse", "SYM_VAR_5.8S2 / SYM_VAR_REV", "ITSD / ITS2REV", "CP23S primers",
				"5.8S F1 / R1", "18S_V4 primer", "COI primer m1COIintF / jgHCO2198", "Sneed2015 27F / 519Rmodbio",
				"16SV4V5 Archae", "16SV5V6 Prok", "18SV1V2 Metazoaire", "16SV4 Procaryote", "16SFL");
	}
	 */
	private static List<Value> getTargetedRegion() {
		return DescriptionFactory.newValues("16S_V4V5", "18S_V9", "16S_Full Length + 16S_V4V5", "ITS2", "CP23S",
				"18S_V4", "COI", "16S_V1V2V3", "16S_V3V4", "16S_V5V6", "18S_V1V2", "16S_V4", "5.8S","16SFL",
				"18S_V7V8","18SFL","12S");
	}
	/*
	private static List<Value> getOriginTargetedRegion() {
		return DescriptionFactory.newValues("16S_V4V5", "18S_V9", "16S_Full Length + 16S_V4V5", "ITS2", "CP23S",
				"18S_V4", "COI", "16S_V1V2V3", "16S_V5V6", "18S_V1V2", "16S_V4","5.8S", "16SFL");
	}
	 */	
	private List<PropertyDefinition> getPoolLibraryReceptionPropertyDefinitions() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();

		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, true, null, null, "single", 1, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.Container), String.class, true, null, null, "single", 15, false, null, null));

		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, true, null, null, "single", 2, false, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.Content), String.class, true, null, 
				null, null,null,null,"single", 3, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.Content), String.class, true, null, 
				getTagCategories(), null,null,null,"single", 4, false, null,null));		
		propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Content), String.class, true, null, null, 
				null,null,null,"single", 5, false, null, null));

		propertyDefinitions.add(newPropertiesDefinition("% GC théorique", "theoricalGCPercent", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Double.class, false, null, null, "single", 6, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Taille associée au taxon", "taxonSize", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Long.class, false, null, 
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), mufind.findByCode("pb"), mufind.findByCode("pb"), "single", 7, false, null, null));

		propertyDefinitions.add(newPropertiesDefinition("Orientation brin synthétisé","strandOrientation", LevelService.getLevels(Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 8, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Méthode synthèse cDNA","cDNAsynthesisType", LevelService.getLevels(Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 9, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Protocole bq RNA","rnaLibProtocol", LevelService.getLevels(Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 10, false, null,null));

		propertyDefinitions.add(newPropertiesDefinition("Amorces", "amplificationPrimers", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getAmplificationPrimers(), null, null, null,"single", 11, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Région ciblée", "targetedRegion", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				getTargetedRegion(), null, null, null,"single", 12, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Taille amplicon attendue", "expectedAmpliconSize", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null,null,null,null,"single", 13, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Issu du type d'échantillon", "fromSampleTypeCode", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				DescriptionFactory.newValues("DNA","RNA"),null,null,null,"single", 14, true, null, null));

		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getSecondaryTagSampleReceptionPropertyDefinitions()
	{
		List<Value> libProcessTypeCodes = BanqueIllumina.getLibProcessDJ();
		
	
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, true, null, null, "single", 1, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Content), String.class, true, null,libProcessTypeCodes, null,null,null,"single", 8, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, true, null, null, "single", 12, false, null, null));		
		propertyDefinitions.add(newPropertiesDefinition("Nom organisme / collaborateur", "collabScientificName", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, null, null,null,null,"single", 14, false, null,null));							
		propertyDefinitions.add(newPropertiesDefinition("Taille associée au taxon", "taxonSize", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Long.class, false, null, null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), mufind.findByCode("pb"), mufind.findByCode("pb"), "single", 15, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("% GC théorique", "theoricalGCPercent", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Double.class, false, null, null, "single", 16, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Méthode préparation ADN (ou ARN)","dnaTreatment",LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content),String.class,false,null,DescriptionFactory.newValues("MDA","plug","WGA","SAG","size selection using ampure","EtOH-reconcentrated"),null,null,null,"single",17,true,null,null));
		propertyDefinitions.add(newPropertiesDefinition("Code du Tag secondaire", "secondaryTag", LevelService.getLevels(Level.CODE.Content), String.class, true, null,	null, null,null,null,"single", 19, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Catégorie Tag", "secondaryTagCategory", LevelService.getLevels(Level.CODE.Content), String.class, true, null,getTagCategories(), null,null,null,"single", 20, false, null,null));	
		return propertyDefinitions ;
	}
	
	

	private List<PropertyDefinition> getExternalReadSetsReceptionPropertyDefinitions() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("META", "meta", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), Boolean.class, true, null, null, "single", 5, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Nom organisme / collaborateur", "collabScientificName", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, null, null,null,null,"single", 17, false, null,null));	
		propertyDefinitions.add(newPropertiesDefinition("Taille associée au taxon", "taxonSize", LevelService.getLevels(Level.CODE.Content,Level.CODE.Sample),Long.class,false,MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), mufind.findByCode("pb"), mufind.findByCode("pb"), "single"));
		propertyDefinitions.add(newPropertiesDefinition("% GC théorique","theoricalGCPercent", LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content), Double.class,false,null,null,"single",1,false,null,null));
		propertyDefinitions.add(newPropertiesDefinition("Méthode préparation ADN (ou ARN)","dnaTreatment",LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content),String.class,false,null,DescriptionFactory.newValues("MDA","plug","WGA","SAG","size selection using ampure","EtOH-reconcentrated"),null,null,null,"single",18,true,null,null));
		return propertyDefinitions;
	}
	

	private static List<Value> getTaraStationValues(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("3", "Station3"));
		values.add(DescriptionFactory.newValue("4", "Station4"));
		values.add(DescriptionFactory.newValue("5", "Station5"));
		values.add(DescriptionFactory.newValue("6", "Station6"));
		values.add(DescriptionFactory.newValue("7", "Station7"));
		values.add(DescriptionFactory.newValue("9", "Station9"));
		values.add(DescriptionFactory.newValue("11", "Station11"));
		values.add(DescriptionFactory.newValue("16", "Station16"));
		values.add(DescriptionFactory.newValue("18", "Station18"));
		values.add(DescriptionFactory.newValue("19", "Station19"));
		values.add(DescriptionFactory.newValue("20", "Station20"));
		values.add(DescriptionFactory.newValue("22", "Station22"));
		values.add(DescriptionFactory.newValue("23", "Station23"));
		values.add(DescriptionFactory.newValue("24", "Station24"));
		values.add(DescriptionFactory.newValue("25", "Station25"));
		values.add(DescriptionFactory.newValue("26", "Station26"));
		values.add(DescriptionFactory.newValue("30", "Station30"));
		values.add(DescriptionFactory.newValue("31", "Station31"));
		values.add(DescriptionFactory.newValue("32", "Station32"));
		values.add(DescriptionFactory.newValue("33", "Station33"));
		values.add(DescriptionFactory.newValue("34", "Station34"));
		values.add(DescriptionFactory.newValue("36", "Station36"));
		values.add(DescriptionFactory.newValue("37", "Station37"));
		values.add(DescriptionFactory.newValue("38", "Station38"));
		values.add(DescriptionFactory.newValue("39", "Station39"));
		values.add(DescriptionFactory.newValue("40", "Station40"));
		values.add(DescriptionFactory.newValue("41", "Station41"));
		values.add(DescriptionFactory.newValue("42", "Station42"));
		values.add(DescriptionFactory.newValue("43", "Station43"));
		values.add(DescriptionFactory.newValue("44", "Station44"));
		values.add(DescriptionFactory.newValue("45", "Station45"));
		values.add(DescriptionFactory.newValue("46", "Station46"));
		values.add(DescriptionFactory.newValue("47", "Station47"));
		values.add(DescriptionFactory.newValue("48", "Station48"));
		values.add(DescriptionFactory.newValue("49", "Station49"));
		values.add(DescriptionFactory.newValue("50", "Station50"));
		values.add(DescriptionFactory.newValue("51", "Station51"));
		values.add(DescriptionFactory.newValue("52", "Station52"));
		values.add(DescriptionFactory.newValue("57", "Station57"));
		values.add(DescriptionFactory.newValue("58", "Station58"));
		values.add(DescriptionFactory.newValue("64", "Station64"));
		values.add(DescriptionFactory.newValue("65", "Station65"));
		values.add(DescriptionFactory.newValue("66", "Station66"));
		values.add(DescriptionFactory.newValue("67", "Station67"));
		values.add(DescriptionFactory.newValue("68", "Station68"));
		values.add(DescriptionFactory.newValue("70", "Station70"));
		values.add(DescriptionFactory.newValue("72", "Station72"));
		values.add(DescriptionFactory.newValue("76", "Station76"));
		values.add(DescriptionFactory.newValue("78", "Station78"));
		values.add(DescriptionFactory.newValue("82", "Station82"));
		values.add(DescriptionFactory.newValue("84", "Station84"));
		values.add(DescriptionFactory.newValue("85", "Station85"));
		values.add(DescriptionFactory.newValue("98", "Station98"));
		values.add(DescriptionFactory.newValue("100", "Station100"));
		values.add(DescriptionFactory.newValue("102", "Station102"));
		values.add(DescriptionFactory.newValue("109", "Station109"));
		values.add(DescriptionFactory.newValue("1000", "Moorea"));
		values.add(DescriptionFactory.newValue("10001", "Villefranche"));
		values.add(DescriptionFactory.newValue("10002", "Elat"));
		values.add(DescriptionFactory.newValue("10003", "Toulon"));
		values.add(DescriptionFactory.newValue("111", "Station111"));
		values.add(DescriptionFactory.newValue("122", "Station122"));
		values.add(DescriptionFactory.newValue("123", "Station123"));
		values.add(DescriptionFactory.newValue("124", "Station124"));
		values.add(DescriptionFactory.newValue("125", "Station125"));
		values.add(DescriptionFactory.newValue("8", "Station8"));
		values.add(DescriptionFactory.newValue("10", "Station10"));
		values.add(DescriptionFactory.newValue("14", "Station14"));
		values.add(DescriptionFactory.newValue("15", "Station15"));
		values.add(DescriptionFactory.newValue("17", "Station17"));
		values.add(DescriptionFactory.newValue("21", "Station21"));
		values.add(DescriptionFactory.newValue("93", "Station93"));
		values.add(DescriptionFactory.newValue("95", "Station95"));
		values.add(DescriptionFactory.newValue("97", "Station97"));
		values.add(DescriptionFactory.newValue("106", "Station106"));
		values.add(DescriptionFactory.newValue("110", "Station110"));
		values.add(DescriptionFactory.newValue("113", "Station113"));
		values.add(DescriptionFactory.newValue("114", "Station114"));
		values.add(DescriptionFactory.newValue("115", "Station115"));
		values.add(DescriptionFactory.newValue("116", "Station116"));
		values.add(DescriptionFactory.newValue("117", "Station117"));
		values.add(DescriptionFactory.newValue("118", "Station118"));
		values.add(DescriptionFactory.newValue("128", "Station128"));
		values.add(DescriptionFactory.newValue("131", "Station131"));
		values.add(DescriptionFactory.newValue("135", "Station135"));
		values.add(DescriptionFactory.newValue("112", "Station112"));
		values.add(DescriptionFactory.newValue("53", "Station53"));
		values.add(DescriptionFactory.newValue("54", "Station54"));
		values.add(DescriptionFactory.newValue("56", "Station56"));
		values.add(DescriptionFactory.newValue("62", "Station62"));
		values.add(DescriptionFactory.newValue("71", "Station71"));
		values.add(DescriptionFactory.newValue("80", "Station80"));
		values.add(DescriptionFactory.newValue("81", "Station81"));
		values.add(DescriptionFactory.newValue("83", "Station83"));
		values.add(DescriptionFactory.newValue("86", "Station86"));
		values.add(DescriptionFactory.newValue("87", "Station87"));
		values.add(DescriptionFactory.newValue("89", "Station89"));
		values.add(DescriptionFactory.newValue("90", "Station90"));
		values.add(DescriptionFactory.newValue("92", "Station92"));
		values.add(DescriptionFactory.newValue("94", "Station94"));
		values.add(DescriptionFactory.newValue("88", "Station88"));
		values.add(DescriptionFactory.newValue("91", "Station91"));
		values.add(DescriptionFactory.newValue("12", "Station12"));
		values.add(DescriptionFactory.newValue("1", "Station1"));
		values.add(DescriptionFactory.newValue("2", "Station2"));
		values.add(DescriptionFactory.newValue("96", "Station96"));
		values.add(DescriptionFactory.newValue("99", "Station99"));
		values.add(DescriptionFactory.newValue("119", "Station119"));
		values.add(DescriptionFactory.newValue("120", "Station120"));
		values.add(DescriptionFactory.newValue("121", "Station121"));
		values.add(DescriptionFactory.newValue("126", "Station126"));
		values.add(DescriptionFactory.newValue("127", "Station127"));
		values.add(DescriptionFactory.newValue("129", "Station129"));
		values.add(DescriptionFactory.newValue("132", "Station132"));
		values.add(DescriptionFactory.newValue("133", "Station133"));
		values.add(DescriptionFactory.newValue("134", "Station134"));
		values.add(DescriptionFactory.newValue("136", "Station136"));
		values.add(DescriptionFactory.newValue("137", "Station137"));
		values.add(DescriptionFactory.newValue("138", "Station138"));
		values.add(DescriptionFactory.newValue("139", "Station139"));
		values.add(DescriptionFactory.newValue("140", "Station140"));
		values.add(DescriptionFactory.newValue("141", "Station141"));
		values.add(DescriptionFactory.newValue("142", "Station142"));
		values.add(DescriptionFactory.newValue("143", "Station143"));
		values.add(DescriptionFactory.newValue("144", "Station144"));
		values.add(DescriptionFactory.newValue("145", "Station145"));
		values.add(DescriptionFactory.newValue("146", "Station146"));
		values.add(DescriptionFactory.newValue("147", "Station147"));
		values.add(DescriptionFactory.newValue("148", "Station148"));
		values.add(DescriptionFactory.newValue("149", "Station149"));
		values.add(DescriptionFactory.newValue("151", "Station151"));
		values.add(DescriptionFactory.newValue("152", "Station152"));
		values.add(DescriptionFactory.newValue("153", "Station153"));
		values.add(DescriptionFactory.newValue("150", "Station150"));
		values.add(DescriptionFactory.newValue("130", "Station130"));
		values.add(DescriptionFactory.newValue("155", "Station155"));
		values.add(DescriptionFactory.newValue("158", "Station158"));
		values.add(DescriptionFactory.newValue("163", "Station163"));
		values.add(DescriptionFactory.newValue("168", "Station168"));
		values.add(DescriptionFactory.newValue("173", "Station173"));
		values.add(DescriptionFactory.newValue("175", "Station175"));
		values.add(DescriptionFactory.newValue("178", "Station178"));
		values.add(DescriptionFactory.newValue("180", "Station180"));
		values.add(DescriptionFactory.newValue("188", "Station188"));
		values.add(DescriptionFactory.newValue("189", "Station189"));
		values.add(DescriptionFactory.newValue("191", "Station191"));
		values.add(DescriptionFactory.newValue("193", "Station193"));
		values.add(DescriptionFactory.newValue("194", "Station194"));
		values.add(DescriptionFactory.newValue("196", "Station196"));
		values.add(DescriptionFactory.newValue("201", "Station201"));
		values.add(DescriptionFactory.newValue("205", "Station205"));
		values.add(DescriptionFactory.newValue("206", "Station206"));
		values.add(DescriptionFactory.newValue("208", "Station208"));
		values.add(DescriptionFactory.newValue("209", "Station209"));
		values.add(DescriptionFactory.newValue("210", "Station210"));
		return values;

	}

	private static List<Value> getTaraDepthCodeValues(boolean reverse){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("COR", "Coral"));
		values.add(DescriptionFactory.newValue("CTL", "CTL"));
		values.add(DescriptionFactory.newValue("DCM", "Deep Chlorophyl Maximum"));
		values.add(DescriptionFactory.newValue("DOP", "DCM and OMZ Pool"));
		values.add(DescriptionFactory.newValue("DSP", "DCM and Surface Pool"));
		values.add(DescriptionFactory.newValue("IZZ", "IntegratedDepth"));
		values.add(DescriptionFactory.newValue("MES", "Meso"));
		values.add(DescriptionFactory.newValue("MXL", "MixedLayer"));
		values.add(DescriptionFactory.newValue("NSI", "NightSampling@25mt0"));
		values.add(DescriptionFactory.newValue("NSJ", "NightSampling@25mt24"));
		values.add(DescriptionFactory.newValue("NSK", "NightSampling@25mt48"));
		values.add(DescriptionFactory.newValue("OBL", "OBLIQUE"));
		values.add(DescriptionFactory.newValue("OMZ", "Oxygen Minimum Zone"));
		values.add(DescriptionFactory.newValue("OTH", "Other"));
		values.add(DescriptionFactory.newValue("PFA", "PF1"));
		values.add(DescriptionFactory.newValue("PFB", "PF2"));
		values.add(DescriptionFactory.newValue("PFC", "PF3"));
		values.add(DescriptionFactory.newValue("PFD", "PF4"));
		values.add(DescriptionFactory.newValue("PFE", "PF5"));
		values.add(DescriptionFactory.newValue("PFF", "PF6"));
		values.add(DescriptionFactory.newValue("PFG", "P1a"));
		values.add(DescriptionFactory.newValue("PFH", "P1b"));
		values.add(DescriptionFactory.newValue("PFI", "B2B1"));
		values.add(DescriptionFactory.newValue("PFJ", "B4B3"));
		values.add(DescriptionFactory.newValue("PFK", "B6B5"));
		values.add(DescriptionFactory.newValue("PFL", "B8B7"));
		values.add(DescriptionFactory.newValue("PFM", "B10B9"));
		values.add(DescriptionFactory.newValue("SKT", "Skeleton and Tissues"));
		values.add(DescriptionFactory.newValue("SOD", "Surface OMZ and DCM Pool"));
		values.add(DescriptionFactory.newValue("SOP", "Surface and OMZ Pool"));
		values.add(DescriptionFactory.newValue("SUR", "Surface"));
		values.add(DescriptionFactory.newValue("SWR", "Sweet water rinse"));
		values.add(DescriptionFactory.newValue("SXL", "Sub-MixedLayer@100m"));
		values.add(DescriptionFactory.newValue("ZZZ", "DiscreteDepth"));

		if(reverse){
			List<Value> rValues = new ArrayList<>();
			for(Value v : values){
				rValues.add(DescriptionFactory.newValue(v.name, v.code));
			}
			values = rValues;
		}

		return values;

	}

	private static List<Value> getTaraFilterCodeValues(boolean reverse){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("AACC", "0-0.2"));
		values.add(DescriptionFactory.newValue("AAZZ", "0-inf"));
		values.add(DescriptionFactory.newValue("BBCC", "0.1-0.2"));
		values.add(DescriptionFactory.newValue("CCEE", "0.2-0.45"));
		values.add(DescriptionFactory.newValue("CCII", "0.2-1.6"));
		values.add(DescriptionFactory.newValue("CCKK", "0.22-3"));
		values.add(DescriptionFactory.newValue("EEGG", "0.45-0.8"));
		values.add(DescriptionFactory.newValue("EEOO", "0.45-8"));
		values.add(DescriptionFactory.newValue("GGKK", "0.8-3"));
		values.add(DescriptionFactory.newValue("GGMM", "0.8-5"));
		values.add(DescriptionFactory.newValue("GGQQ", "0.8-20"));
		values.add(DescriptionFactory.newValue("GGRR", "0.8-200"));
		values.add(DescriptionFactory.newValue("GGSS", "0.8-180"));
		values.add(DescriptionFactory.newValue("GGZZ", "0.8-inf"));
		values.add(DescriptionFactory.newValue("IIQQ", "1.6-20"));
		values.add(DescriptionFactory.newValue("KKQQ", "3-20"));
		values.add(DescriptionFactory.newValue("KKZZ", "3-inf"));
		values.add(DescriptionFactory.newValue("MMQQ", "5-20"));
		values.add(DescriptionFactory.newValue("QQRR", "20-200"));
		values.add(DescriptionFactory.newValue("QQSS", "20-180"));
		values.add(DescriptionFactory.newValue("SSUU", "180-2000"));
		values.add(DescriptionFactory.newValue("SSZZ", "180-inf"));
		values.add(DescriptionFactory.newValue("TTZZ", "300-inf"));
		values.add(DescriptionFactory.newValue("YYYY", "pool"));
		values.add(DescriptionFactory.newValue("ZZZZ", "inf-inf"));

		if(reverse){
			List<Value> rValues = new ArrayList<>();
			for(Value v : values){
				rValues.add(DescriptionFactory.newValue(v.name, v.code));
			}
			values = rValues;
		}
		return values;

	}


	private static List<PropertyDefinition> getLibraryTaraPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.addAll(getTaraPropertyDefinitions());
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.Content), String.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Catégorie Tag", "tagCategory", LevelService.getLevels(Level.CODE.Content), String.class, true, getTagCategories(), "single"));

		return propertyDefinitions;
	}

	public static List<Institute> getInstitutes(Constants.CODE...codes) throws DAOException {
		List<Institute> institutes = new ArrayList<>();
		for (Constants.CODE code : codes) {
			institutes.add(Institute.find.get().findByCode(code.name()));
		}
		return institutes;
	}

}
