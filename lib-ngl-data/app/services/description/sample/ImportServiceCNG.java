package services.description.sample;

import static services.description.DescriptionFactory.newImportType;
import static services.description.DescriptionFactory.newPropertiesDefinition;
import static services.description.DescriptionFactory.newValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.sample.description.ImportCategory;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.dao.ImportCategoryDAO;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;

public class ImportServiceCNG extends AbstractImportService {
	
	public List<ImportCategory> getImportCategories() {
		List<ImportCategory> l = new ArrayList<>();
//		l.add(newImportCategory("Sample Import", "sample-import"));
		l.add(new ImportCategory("sample-import", "Sample Import"));
		return l;
	}
	
	@Override
	public  void saveImportCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		DAOHelpers.saveModels(ImportCategory.class, getImportCategories(), errors);
	}
	
	public List<ImportType> getImportTypes() {
		List<ImportType> l = new ArrayList<>();
		List<Institute> CNG = DescriptionFactory.getInstitutes(Constants.CODE.CNG); // 28/03/2018 
		ImportCategoryDAO icfind = ImportCategory.find.get();
				
		//pour import automatique depuis la base Solexa
		l.add(newImportType("Defaut", "default-import", icfind.findByCode("sample-import"), getSampleCNGPropertyDefinitions(), CNG));
		
		//pour import depuis le fichier LIMS ModulBio
		l.add(newImportType("Import aliquots tubes",   "tube-from-bank-reception",  icfind.findByCode("sample-import"), getBankReceptionPropertyDefinitions(), CNG));
		l.add(newImportType("Import aliquots plaques", "plate-from-bank-reception", icfind.findByCode("sample-import"), getBankReceptionPropertyDefinitions(), CNG));
		
		//pour import de librairies de collaborateurs externes
		// FDS 20/06/2017 NGL-1472
		l.add(newImportType("Import librairies indexées (non poolées)", "library-idx-reception",icfind.findByCode("sample-import"), getLibraryReceptionPropertyDefinitions(true), CNG));
		// FDS 22/11/2017 NGL-1703
		l.add(newImportType("Import librairies indexées (poolées)", "library-idx-pool-reception",icfind.findByCode("sample-import"), getLibraryReceptionPropertyDefinitions(true), CNG));
		// FDS 05/03/2018 NGL-1907 ; 08/11/2019 correction label=> ajout 'indexées'
		l.add(newImportType("Import librairies indexées (poolées) SANS démultiplexage", "no-demultiplexing-lib-pool-reception",icfind.findByCode("sample-import"), getLibraryNodemultiplexReceptionPropertyDefinitions(),CNG));
		// FDS 07/11/2019 NGL-2692
		//import de librairies indexées pour labo internes=> ont besoin du code Aliquot
		l.add(newImportType("Import librairies indexées (non poolées) d'équipes internes", "library-idx-reception-internal-team",icfind.findByCode("sample-import"), getInternalLibraryReceptionPropertyDefinitions(), CNG));
		
		// GA/FDS 14/06/2017 CONTOURNEMENT de la creation des libProcessTypecodes dans NGLBI ce qui pose des problemes dans le cas ISOPROD
		// creer un ImportType bidon pour declarer la propriété libProcessTypecodes et sa liste de valeurs...
		l.add(newImportType("Import bidon", "import-bidon", icfind.findByCode("sample-import"), getLibProcessTypecodePropertyDefinitions(), CNG));
		return l;
	}
	
	@Override
	public void saveImportTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		DAOHelpers.saveModels(ImportType.class, getImportTypes(), errors);
	}

	private static List<PropertyDefinition> getBankReceptionPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Gender", "gender", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				Arrays.asList(newValue("0","unknown"),newValue("1","male"),newValue("2","female")), null,null,null,"single", 17, false, null,null));	
		
		// FDS 14/03/2017 NGL-1776 ajout propriété bankIntegrityNumber venant du LIMS Modulbio . pas necessaire de mettre (Level.CODE.Container)=> voir GA...
		propertyDefinitions.add(newPropertiesDefinition("Bank Integrity Number", "bankIntegrityNumber", LevelService.getLevels(Level.CODE.Content), Double.class, false, null, 
				null, null, null, null,"single", 18, false, null,null));	
	
		// FDS 14/03/2017 NGL-1903 prise en compte du la colonne "Organisme" du fichier importé
		propertyDefinitions.add(newPropertiesDefinition("Nom organisme / collaborateur", "collabScientificName", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 19, false, null,null));
		
		// FDS 21/08/2018 NGL-2206 prise en compte du la colonne "Origin"
		propertyDefinitions.add(newPropertiesDefinition("Origine", "origin", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 20, false, null,null));
		
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getSampleCNGPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Code LIMS", "limsCode", LevelService.getLevels(Level.CODE.Sample),Integer.class, true, "single"));
		return propertyDefinitions;
	}
	
	// FDS 20/06/2017 NGL-1472
	private static List<PropertyDefinition> getLibraryReceptionPropertyDefinitions (boolean isIndexed) throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		// propriétés communes Librairies
		propertyDefinitions.add(newPropertiesDefinition("Gender", "gender", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				Arrays.asList(newValue("0","unknown"),newValue("1","male"),newValue("2","female")), null,null,null,"single", 1, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Date de réception", "receptionDate", LevelService.getLevels(Level.CODE.Container), Date.class, false, null, 
				null, "single", 2, false, null, null));	
		propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Content), String.class, true, null, 
				getExtLibProcessTypecodesValues(), null, null, null,"single", 3, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Nom organisme / collaborateur", "collabScientificName", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 4, false, null,null));		
		
		// FDS 20/02/2019 NGL-2259 prise en compte du la colonne "Origin"
		propertyDefinitions.add(newPropertiesDefinition("Origine", "origin", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, false, null, 
				null, null,null,null,"single", 5, false, null,null));
			
		// propriétés pour librairies indexees
		if (isIndexed) {
			propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.Content), String.class, true, null, 
					null, null,null,null,"single", 5, false, null,null));
			propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.Content), String.class, true, null, 
					getTagCategories(), null,null,null,"single", 6, false, null,null));	
		}
		
		return propertyDefinitions;
	}
	
	//FDS 07/11/2019 NGL-2692 cas des équipes internes => propriétés supplémentaires
	private static List<PropertyDefinition> getInternalLibraryReceptionPropertyDefinitions() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		// en interne on n'importe que des librairies indexées !!
		propertyDefinitions.addAll(getLibraryReceptionPropertyDefinitions(true));
		
		// 1 seule propriété supplémentaire pour l'instant: sampleAliquoteCode
		// niveau Content uniquement !!!, obligatoire
		propertyDefinitions.add(newPropertiesDefinition("code Aliquot", "sampleAliquoteCode", LevelService.getLevels(Level.CODE.Content), String.class, true, null, null, 
				null,null,null,"single", 10, false, null, null));
		
		return propertyDefinitions;
	}
	
	//FDS 05/03/2018 NGL-1907 cas du projet PALEO: librairie indexees et poolees mais on l'Equipe Joe ne doit pas faire le demultiplexage.
	private static List<PropertyDefinition> getLibraryNodemultiplexReceptionPropertyDefinitions() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		// propriétés communes Librairies (sauf index !!!!)
		propertyDefinitions.addAll(getLibraryReceptionPropertyDefinitions(false));
		
		// 1 seule propriété dans laquelle sont concaténées les séquences des index attendus
		propertyDefinitions.add(newPropertiesDefinition("Séquence index attendus", "expectedSequences", LevelService.getLevels(Level.CODE.Content), String.class, true, null, null, 
				null,null,null,"single", 10, false, null, null));
		
		return propertyDefinitions;
	}
	
	
	// FDS 20/06/2017 ajouté pour NGL-1472
	private static List<Value> getTagCategories(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("SINGLE-INDEX", "SINGLE-INDEX"));
		values.add(DescriptionFactory.newValue("DUAL-INDEX", "DUAL-INDEX"));
		values.add(DescriptionFactory.newValue("MID", "MID"));
		values.add(DescriptionFactory.newValue("POOL-INDEX", "POOL-INDEX"));
		return values;	
	}
	

	private static List<PropertyDefinition> getLibProcessTypecodePropertyDefinitions() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Type processus banque","libProcessTypeCode",LevelService.getLevels(Level.CODE.Content), String.class, false, getLibProcessTypeCodeValues(), "single"));
	
		return propertyDefinitions;
	}
	
	// Ajouter les mêmes valeurs à l'IDENTIQUE dans ProcessServiceCNG, si nécessaire 
	// Ajouter les mêmes valeurs à l'IDENTIQUE plus bas dans getExtLibProcessTypecodesValues, si nécessaire
	private static List<Value> getLibProcessTypeCodeValues(){
        List<Value> values = new ArrayList<>();
        
         // codes for Capture Sequencing
         //  Mettre (DefCapxxx) dans le label tant que NGL-1569 pas resolu (stocker le path des fichier definition capture)
         //  Ajouter aussi à l'identique dans ProcessServiceCNG/getCaptureLibProcessTypeCodeValues, si necessaire
         values.add(DescriptionFactory.newValue("CA","CA - DefCap008_Rg"));
         values.add(DescriptionFactory.newValue("CB","CB - DefCap005_Ex"));
         values.add(DescriptionFactory.newValue("CC","CC - DefCap006_Ex"));
         values.add(DescriptionFactory.newValue("CD","CD - DefCap004_Rg"));
         values.add(DescriptionFactory.newValue("CE","CE - DefCap003_Ex"));
         values.add(DescriptionFactory.newValue("CF","CF - DefCap002_Ex"));
         values.add(DescriptionFactory.newValue("CG","CG - DefCap001_Ex"));
         values.add(DescriptionFactory.newValue("CH","CH - DefCap009_Ex"));
         values.add(DescriptionFactory.newValue("CI","CI - DefCap010_Ex"));
         values.add(DescriptionFactory.newValue("CJ","CJ - DefCap011_Ex"));
         values.add(DescriptionFactory.newValue("CK","CK - DefCap007_Ex"));
         values.add(DescriptionFactory.newValue("CL","CL - DefCapLUPA"));
         values.add(DescriptionFactory.newValue("CM","CM - DefCap012_Rg"));
         values.add(DescriptionFactory.newValue("CN","CN - DefCapINRA1_Rg"));
         values.add(DescriptionFactory.newValue("CO","CO - DefCapCAPSEQAN"));
         values.add(DescriptionFactory.newValue("CP","CP - Agilent : V5 (DefCap013_Ex)")); 
         values.add(DescriptionFactory.newValue("CQ","CQ - DefCap014_Rg"));
         values.add(DescriptionFactory.newValue("CR","CR - DefCap015_Ex"));
         values.add(DescriptionFactory.newValue("CS","CS - Agilent : V5+UTR (DefCap016_Ex)"));
         values.add(DescriptionFactory.newValue("CT","CT - CapNimGenV3_017_Ex"));
         values.add(DescriptionFactory.newValue("CV","CV - DefCap018_Ex"));
         values.add(DescriptionFactory.newValue("CW","CW - DefCap019_Rg"));
         values.add(DescriptionFactory.newValue("CX","CX - DefCap020_Ex"));
         values.add(DescriptionFactory.newValue("CY","CY - DefCap021"));
         values.add(DescriptionFactory.newValue("CZ","CZ - Agilent : V6 (DefCap022)"));
         values.add(DescriptionFactory.newValue("CAA","CAA - Agilent : V6+UTR (DefCap023)"));
         values.add(DescriptionFactory.newValue("CAB","CAB - DefCap024"));
         values.add(DescriptionFactory.newValue("CAC","CAC - Agilent : V6+Cosmic (DefCap025)"));
         values.add(DescriptionFactory.newValue("CAD","CAD - Roche-Nimblegen : MedExome (DefCap026)")); //  ajout 28/02/2018
         values.add(DescriptionFactory.newValue("CAE","CAE - Roche-Nimblegen : MedExome+Mitome (DefCap027)"));
         values.add(DescriptionFactory.newValue("CAF","CAF - Chromium Whole Exome (DefCap028)"));       // NGL-1584 ajout
         values.add(DescriptionFactory.newValue("CAG","CAG - SureSelectXTcustom(PRME) (DefCap029)"));   // NGL-2040 ajout
         values.add(DescriptionFactory.newValue("CAH","CAH - Agilent : V7 (DefCap030)"));               // NGL-2186 ajout
         values.add(DescriptionFactory.newValue("CAI","CAI - cfDNA Agilent V6 (DefCap022)"));           // NGL-2260 ajout
         values.add(DescriptionFactory.newValue("CAJ","CAJ - Custom capture Bis cfDNASeq (DefCap031)"));// NGL-2260 ajout
         values.add(DescriptionFactory.newValue("CAK","CAK - xGen Pan-Cancer Panel"));                  // NGL-2877 ajout 
         
         // codes for DNA sequencing
         values.add(DescriptionFactory.newValue("DA","DA - DNASeq"));
         values.add(DescriptionFactory.newValue("DB","DB - MatePairSeq"));
         values.add(DescriptionFactory.newValue("DC","DC - Dnase-ISeq"));
         values.add(DescriptionFactory.newValue("DD","DD - PCR-NANO DNASeq"));  // !! aussi dans ProcessServiceCNG / getX5WgNanoLibProcessTypeCodeValues()
         values.add(DescriptionFactory.newValue("DE","DE - Chromium WG"));      // !! aussi dans ProcessServiceCNG / getWgChromiumLibProcessTypeCodeValues()
         values.add(DescriptionFactory.newValue("DF","DF - Ancient DNASeq"));   // NGL-1712 ajout 22/11/2017 
         values.add(DescriptionFactory.newValue("DG","DG - cfDNASeq"));         // NGL-1981 ajout
         values.add(DescriptionFactory.newValue("DH","DH - Meta16S"));          // NGL-2180 ajout
         values.add(DescriptionFactory.newValue("DI","DI - Bis cfDNASeq"));     // NGL-2260 ajout
         values.add(DescriptionFactory.newValue("DJ","DJ - WGS Flex"));         // NGL-2431 ajout
         values.add(DescriptionFactory.newValue("DK","DK - DNA MPRA plasmide"));// NGL-2522 ajout
         values.add(DescriptionFactory.newValue("DL","DL - Illumina DNA PCR-Free Prep, Tagmentation"));// NGL-3055 ajout
         values.add(DescriptionFactory.newValue("DM","DM - DNASeq Low-pass"));  // NGL-3258
         
         // codes for various sequencing
         values.add(DescriptionFactory.newValue("FA","FA - MeDipSeq"));
         values.add(DescriptionFactory.newValue("FB","FB - ChipSeq"));
         values.add(DescriptionFactory.newValue("FC","FC - MeDipSeq/Depl"));
         values.add(DescriptionFactory.newValue("FD","FD - BisSeq"));
         values.add(DescriptionFactory.newValue("FE","FE - FAIRESeq"));
         values.add(DescriptionFactory.newValue("FF","FF - MBDSeq"));
         values.add(DescriptionFactory.newValue("FG","FG - GROSeq"));
         values.add(DescriptionFactory.newValue("FHB","FHB - oxBisSeq_B")); // 09/06/2018 NGL-1728: subdiviser en 2
         values.add(DescriptionFactory.newValue("FHO","FHO - oxBisSeq_O")); // 09/06/2018 NGL-1728: subdiviser en 2
         values.add(DescriptionFactory.newValue("FH","FH - oxBisSeq"));     // 24/08/2018 NGL-2216: laisser le FH d'origine
         values.add(DescriptionFactory.newValue("FI","FI - ATACSeq"));
         values.add(DescriptionFactory.newValue("FJ","FJ - RRBSeq"));       // SUPSQCNG-497: ajout 06/11/2017 car manquant
         values.add(DescriptionFactory.newValue("FK","FK - QMPSeq"));       // NGL-2039 ajout
         values.add(DescriptionFactory.newValue("FKB","FKB - BsQMPSeq"));   // NGL-2693 ajout 09/10/2019
         values.add(DescriptionFactory.newValue("FKO","FKO - OxQMPSeq"));   // NGL-2693 ajout 09/10/2019
         values.add(DescriptionFactory.newValue("HIC","HIC - HiC"));
         
         // codes for RNA sequencing
         // en cas d'ajout, ajouter aussi a l'identique dans ProcessServiceCNG / getRNALibProcessTypeCodeValues()
         values.add(DescriptionFactory.newValue("RA","RA - RNASeq"));
         values.add(DescriptionFactory.newValue("RB","RB - smallRNASeq"));
         values.add(DescriptionFactory.newValue("RC","RC - ssRNASeq"));
         values.add(DescriptionFactory.newValue("RD","RD - ssmRNASeq"));
         values.add(DescriptionFactory.newValue("RE","RE - sstRNASeq"));
         values.add(DescriptionFactory.newValue("RF","RF - sstRNASeqGlobin"));
         values.add(DescriptionFactory.newValue("RG","RG - mRNASeq"));
         values.add(DescriptionFactory.newValue("RH","RH - sstRNASeqGold"));
         values.add(DescriptionFactory.newValue("RI","RI - single cell mRNASeq Chromium")); // NGL-2383 ajout 14/01/2019
         values.add(DescriptionFactory.newValue("RJ","RJ - mRNA MPRA")); // NGL-2625 ajout 24/07/2019
         
         // codes pour librairies de virus
         values.add(DescriptionFactory.newValue("VAA","VAA - SARS-CoV2 Paragon Genomics amplification")); // NGL-2977 ajout 09/06/2020 (05/01/2021=> CoV2 au lieu de Cov2) 
         values.add(DescriptionFactory.newValue("VAB","VAB - SARS-CoV2 Agilent Capture")); // NGL-3197 ajout 05/01/2021
         values.add(DescriptionFactory.newValue("VAC","VAC - SARS-CoV2 CovidSeq-illumina")); // NGL-3259 ajout 18/02/2021
         
         //historique dans SolexaProd, conserver ???
         values.add(DescriptionFactory.newValue("UN","UN - UNKNOWN"));
        return values;
    } 
	
	// Liste pour import depuis fichier Externe: ATTENTION, Explications utilisateur du fichier d'import a maintenir en coherence avec cette liste
	// Utilisé pour l'import de librairies venant de collaborateurs exterieurs ET pour les process internes pas encore intégrés dans NGL...
	private static List<Value> getExtLibProcessTypecodesValues(){
        List<Value> values = new ArrayList<>();
        
        // 04/07/2017 ajout des codes pour certaines librairies externes RNA
        values.add(DescriptionFactory.newValue("RA","RA - RNASeq"));
        values.add(DescriptionFactory.newValue("RB","RB - smallRNASeq"));
        values.add(DescriptionFactory.newValue("RC","RC - ssRNASeq"));
        values.add(DescriptionFactory.newValue("RD","RD - ssmRNASeq"));
        values.add(DescriptionFactory.newValue("RE","RE - sstRNASeq"));
        values.add(DescriptionFactory.newValue("RF","RF - sstRNASeqGlobin"));
        values.add(DescriptionFactory.newValue("RG","RG - mRNASeq"));
        values.add(DescriptionFactory.newValue("RH","RH - sstRNASeqGold"));
        values.add(DescriptionFactory.newValue("RI","RI - single cell mRNASeq Chromium")); // NGL-2383 ajout 14/01/2019
        values.add(DescriptionFactory.newValue("RJ","RJ - mRNA MPRA")); // NGL-2625 ajout 24/07/2019
        
        // 12/09/2017 ajout des codes pour import certaines librairies DNA externes 
        values.add(DescriptionFactory.newValue("DA","DA - DNASeq"));
        values.add(DescriptionFactory.newValue("DB","DB - MatePairSeq"));
        values.add(DescriptionFactory.newValue("DC","DC - Dnase-ISeq"));
        values.add(DescriptionFactory.newValue("DD","DD - PCR-NANO DNASeq"));
        values.add(DescriptionFactory.newValue("DE","DE - Chromium WG"));      
        values.add(DescriptionFactory.newValue("DF","DF - Ancient DNASeq"));   // NGL-1712 ajout 22/11/2017
        values.add(DescriptionFactory.newValue("DG","DG - cfDNASeq"));         // NGL-1981 ajout
        values.add(DescriptionFactory.newValue("DH","DH - Meta16S"));          // NGL-2180 ajout
        values.add(DescriptionFactory.newValue("DI","DI - Bis cfDNASeq"));     // NGL-2260 ajout
        values.add(DescriptionFactory.newValue("DJ","DJ - WGS Flex"));         // NGL-2431 ajout
        values.add(DescriptionFactory.newValue("DK","DK - DNA MPRA plasmide"));// NGL-2522 ajout
        values.add(DescriptionFactory.newValue("DL","DL - Illumina DNA PCR-Free Prep, Tagmentation"));// NGL-3055 ajout
        
        // 04/10/2017 ajout des codes pour import certaines Capture externes
        values.add(DescriptionFactory.newValue("CAF","CAF - Chromium Whole Exome (DefCap028)"));       // NGL-1584 ajout
        values.add(DescriptionFactory.newValue("CAD","CAD - Roche-NimbleGen : MedExome (DefCap026)")); //          ajout 28/02/2018
        values.add(DescriptionFactory.newValue("CAI","CAI - cfDNA Agilent V6 (DefCap022)"));           // NGL-2260 ajout
        values.add(DescriptionFactory.newValue("CAJ","CAJ - Custom capture Bis cfDNASeq (DefCap031)"));// NGL-2260 ajout
        values.add(DescriptionFactory.newValue("CAK","CAK - xGen Pan-Cancer Panel"));                  // NGL-2877 ajout 
        
        // codes for various sequencing
        values.add(DescriptionFactory.newValue("FD","FD - BisSeq"));		// NGL-2884 ajout 18/02/2020
        values.add(DescriptionFactory.newValue("FI","FI - ATACSeq"));		// NGL-2973 ajout 02/06/2020
        values.add(DescriptionFactory.newValue("FJ","FJ - RRBSeq"));		// NGL-2299 ajout
        values.add(DescriptionFactory.newValue("FK","FK - QMPSeq"));		// NGL-2299 ajout
        values.add(DescriptionFactory.newValue("FKB","FKB - BsQMPSeq"));	// NGL-2693 ajout 09/10/2019
        values.add(DescriptionFactory.newValue("FKO","FKO - OxQMPSeq"));	// NGL-2693 ajout 09/10/2019
        values.add(DescriptionFactory.newValue("FB","FB - ChipSeq"));
        values.add(DescriptionFactory.newValue("HIC","HIC - HiC"));         // NGL-2992 ajout 23/06/2020
        
        // 08/06/2020 ajout des codes pour import librairies de virus
        values.add(DescriptionFactory.newValue("VAA","VAA - SARS-CoV2 Paragon Genomics amplification")); // NGL-2977 ajout 09/06/2020 (05/01/2021=> CoV2 au lieu de Cov2)
        values.add(DescriptionFactory.newValue("VAB","VAB - SARS-CoV2 Agilent Capture")); // NGL-3197 ajout 05/01/2021
        values.add(DescriptionFactory.newValue("VAC","VAC - SARS-CoV2 CovidSeq-illumina")); // NGL-3259 ajout 18/02/2021
        
        return values;
	}
}
