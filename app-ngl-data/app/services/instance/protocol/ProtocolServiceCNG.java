package services.instance.protocol;

import static java.util.Arrays.asList;
import static services.instance.InstanceFactory.newPSV;
import static services.instance.InstanceFactory.newProtocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.protocol.instance.Protocol;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import validation.ContextValidation;

public class ProtocolServiceCNG {	

	private static final play.Logger.ALogger logger = play.Logger.of(ProtocolServiceCNG.class);
	
	private final static String institute = "CNG";
	
	public static void main(ContextValidation ctx) {	

		logger.info("Start to create protocols collection for "+institute+"...");
		logger.info("Remove protocol");
		removeProtocols(ctx);
		logger.info("Save protocols ...");
		saveProtocols(ctx);
		logger.info(institute+" Protocols collection creation is done!");
	}
	
	private static void removeProtocols(ContextValidation ctx) {
		MongoDBDAO.delete(InstanceConstants.PROTOCOL_COLL_NAME, Protocol.class, DBQuery.empty());
	}

	// FDS 29/08/2019: les arguments path, version et cat ("?","1","production") de la methode newProtocol, sont inutilises dans l'application...
	public static void saveProtocols(ContextValidation ctx){		
		List<Protocol> lp = new ArrayList<>();
		
		//----------Experiences de transformation-------------------------------------
		lp.add(newProtocol("PrepFC_CBot_ptr_sox139_1","PrepFC_CBot_ptr_sox139_1","?","1","production", 
				asList("prepa-flowcell")));
		
		// 27/06/2016 ajout "protocole_FC_ordonnée"
		lp.add(newProtocol("protocole-FC-ordered","protocole_FC_ordonnée","?","1","production", 
				asList("prepa-fc-ordered")));
		
		lp.add(newProtocol("1a-sop-ill-pcrfree","1A_SOP_ILL_PCRfree_270116", "?","1","production",
				asList("prep-pcr-free",
						"labchip-migration-profile" )));
		
		lp.add(newProtocol("1a-sop-ill-pcrfree-dap-plate","1A_SOP_ILL_PCRfree_DAPplate", "?","1","production",
				asList("prep-pcr-free",
						"labchip-migration-profile" )));
		
		// 10/08/2016 protocole  pour toutes les experiences du processus X5_WG NANO
		// 01/09/2016 aussi pour "labchip-migration-profile"
		lp.add(newProtocol("1a-sop-ill-nano-240214","1A_SOP_ILL_NANO_240214", "?","1","production",
				asList("prep-wg-nano",
					   "pcr-and-purification",
					   "labchip-migration-profile")));
		
		lp.add(newProtocol("sop-1","SOP 1","?","1","production", 
				asList("denat-dil-lib")));
		
		// protocole quand le labo n'a pas "encore" fourni de nom specifique...

		lp.add(newProtocol("sop-en-attente","SOP en attente","?","1","production", 
				asList("normalization-and-pooling",
					   "aliquoting",
					   "tubes-to-plate",
					   "plate-to-tubes",
					   "plates-to-plate",
					   "x-to-plate")));

		// 12/12/2016 protocoles pour RNA
		lp.add(newProtocol("2a-ill-ssmrna-010616","2A_ILL_ssmRNA_010616","?","1","production", 
				asList("library-prep",
					   "pcr-and-purification")));
		
		// 05/12/2016 library-prep
		lp.add(newProtocol("2a-ill-sstotalrna-170816","2A_ILL_ssTotalRNA_170816","?","1","production", 
				asList("library-prep",
					   "pcr-and-purification")));
		
		// 29/11/2017 NGL-1717 ajout "Truseq RNA v2"
		lp.add(newProtocol("truseq-rna-v2","Truseq RNA v2","?","1","production", 
				asList("library-prep",
					   "pcr-and-purification")));	
						
		// 26/09/2016 ajout protocole "normalisation" dédié a l'experience lib-normalization"; 19/07/2017 ajout additional-normalization
		lp.add(newProtocol("normalization","normalisation","?","1","production", 
				asList("lib-normalization",
					    "additional-normalization")));
		
		// 27/09/2016 ajout protocole "protocole_pool" dédié a l'experience "pool plaque a plaque"
		lp.add(newProtocol("protocol-pool","protocole_pool","?","1","production", 
				asList("pool")));
		
		// 27/09/2016 ajout protocole "protocole_dépôt_illumina" dédié a l'experience "illumina-depot"
		lp.add(newProtocol("protocol-illumina-depot","protocole_dépôt_illumina","?","1","production", 
				asList("illumina-depot")));
		
		// 23/03/2017 ajout protocole pour Chromium
		lp.add(newProtocol("chromium-genome-protocol-v1","chromium genome protocol v1","?","1","production", 
				asList("chromium-gem-generation",
					   "wg-chromium-lib-prep")));
		
		lp.add(newProtocol("chromium-genome-protocol-v2","chromium genome protocol v2","?","1","production", 
				asList("chromium-gem-generation",
					   "wg-chromium-lib-prep")));
		
		// 30/03/2017 ajout protocoles pour Nanopore
		//cdna-synthesis, 18/06/2019 suppression caractere blanc final excedentaire dans name
		lp.add(newProtocol("1d-strand-switching","1D strand switching","?","1","production",
				asList("cdna-synthesis")));
		
		//nanopore-frg 
		lp.add(newProtocol("mechanical-fragmentation","fragmentation mécanique","?","1","production",
				asList("nanopore-frg")));
		
		lp.add(newProtocol("enzymatic-fragmentation","fragmentation enzymatique","?","1","production",
				asList("nanopore-frg")));
		
		//nanopore-dna-reparation
		lp.add(newProtocol("ffpe-reparation","réparation FFPE","?","1","production",
				asList("nanopore-dna-reparation")));
		
		//nanopore-library")
		lp.add(newProtocol("R9-1D-ligation","R9-1D ligation","?","1","production",
				asList("nanopore-library"),
				concatMap(newPSV("libraryProtocol","R9-1D ligation"))));					// =>outputContainerUseds.contents.properties
		
		lp.add(newProtocol("R9-1D-transposition","R9-1D transposition","?","1","production", 
				asList("nanopore-library"),
				concatMap(newPSV("libraryProtocol","R9-1D transposition"))));				// =>outputContainerUseds.contents.properties
		
		lp.add(newProtocol("R9-Long-Read 1D","R9-Long Read 1D","?","1","production",
				asList("nanopore-library"),
				concatMap(newPSV("libraryProtocol","R9-Long Read 1D"))));					// =>outputContainerUseds.contents.properties
		
		lp.add(newProtocol("R9-Long-Read 2D","R9-Long Read 2D","?","1","production",
				asList("nanopore-library"),
				concatMap(newPSV("libraryProtocol","R9-Long Read 2D"))));					// =>outputContainerUseds.contents.properties
		
		lp.add(newProtocol("R9-Low-input","R9-Low input","?","1","production", 
				asList("nanopore-library"),
				concatMap(newPSV("libraryProtocol","R9-Low input"))));						// =>outputContainerUseds.contents.properties
		
		lp.add(newProtocol("R9-2D","R9-2D","?","1","production",
				asList("nanopore-library"),
				concatMap(newPSV("libraryProtocol","R9-2D"))));								// =>outputContainerUseds.contents.properties
		
		lp.add(newProtocol("1D2-library","Banque 1D²","?","1","production",
				asList("nanopore-library"),
				concatMap(newPSV("libraryProtocol","Banque 1D²"))));						// =>outputContainerUseds.contents.properties
		
		// 26/08/2019 NGL-2652 ajout 3 protocoles => "nanopore-library","nanopore-depot"
		// 29/08/2019                             => "cdna-synthesis", "nanopore-frg", "nanopore-dna-reparation"
		lp.add(newProtocol("cdna-pcr-sequencing-sqk-pcs-109)","cDNA-PCR Sequencing (SQK-PCS109)","?", "1", "production",
				asList("cdna-synthesis",
						"nanopore-frg",
						"nanopore-dna-reparation",
						"nanopore-library",
						"nanopore-depot"),
				concatMap(newPSV("libraryProtocol", "cDNA-PCR Sequencing (SQK-PCS109)")))); // =>outputContainerUseds.contents.properties
		
		lp.add(newProtocol("direct-cdna-sequencing-sqk-dcs109","Direct cDNA Sequencing (SQK-DCS109)","?", "1", "production",
				asList("cdna-synthesis",
						"nanopore-frg",
						"nanopore-dna-reparation",
						"nanopore-library",
						"nanopore-depot"),
				concatMap(newPSV("libraryProtocol", "Direct cDNA Sequencing (SQK-DCS109)")))); // =>outputContainerUseds.contents.properties
		
		lp.add(newProtocol("direct-rna-sequencing-sqk-rna002","Direct RNA Sequencing (SQK-RNA002)","?", "1", "production",
				asList("cdna-synthesis",
						"nanopore-frg",
						"nanopore-dna-reparation",
						"nanopore-library",
						"nanopore-depot"),
				concatMap(newPSV("libraryProtocol", "Direct RNA Sequencing (SQK-RNA002)")))); // =>outputContainerUseds.contents.properties
		
		
		//nanopore-depot
		lp.add(newProtocol("R9-depot","R9-dépôt","?","1","production",
				asList("nanopore-depot")));
		
		lp.add(newProtocol("R9-depot-SpotON","R9-dépôt-SpotON","?","1","production",
				asList("nanopore-depot")));
		
		lp.add(newProtocol("R9-on-bead-depot","R9-dépôt sur billes","?","1","production",
				asList("nanopore-depot")));
		
		lp.add(newProtocol("R9-on-bead-spotOn-depot","R9-dépôt-SpotON sur billes","?","1","production",
				asList("nanopore-depot")));
		
		lp.add(newProtocol("PromethION_DEV","PromethION_DEV","?","1","production",
				asList("nanopore-depot")));
		
		// 12/07/2017 ajout protocoles pour Capture
		lp.add(newProtocol("1-dna-a-2-sop-agil-capture-sciclone","1_DNA_A_2_SOP_AGIL_Capture_Sciclone","?","1","production",
				asList("fragmentation",
					   "sample-prep",
					   "pcr-and-purification",
					   "capture",
					   "pcr-and-indexing",
					   "fluo-quantification",
					   "labchip-migration-profile")));  
		
		// 18/06/2018 NGL-2129
		lp.add(newProtocol("1-dna-a-1-sop-sp-capture-3ug-bravo","1_DNA_A_1_SOP_SP_Capture_3µg_Bravo","?","1","production",
				asList("fragmentation",
					   "sample-prep",
					   "pcr-and-purification"))); 
		
		// 18/06/2018 NGL-2129
		lp.add(newProtocol("1-dna-a-1-sop-sp-capture-200ng-bravo","1_DNA_A_1_SOP_SP_Capture_200ng_Bravo","?","1","production",
				asList("fragmentation",
					   "sample-prep",
					   "pcr-and-purification"))); 
		
		// 18/06/2018 NGL-2129
		lp.add(newProtocol("1-dna-a-1-sop-postcapture-bravo","1_DNA_A_1_SOP_PostCapture_Bravo ","?","1","production",
				asList("capture",
					   "pcr-and-indexing",
					   "fluo-quantification",          //pas demandé explicitement
					   "labchip-migration-profile"))); //pas demandé explicitement
		
		// 06/04/2018 ajout protocoles pour smallRNASeq
		lp.add(newProtocol("nebnext-small-rna-library-prep","NEBNext Small RNA Library Prep","?","1","production",
				asList("small-rnaseq-lib-prep")));
		
		lp.add(newProtocol("qiaseq-mirna-library-prep","QIAseq miRNA Library","?","1","production", 
				asList("small-rnaseq-lib-prep")));
		
		// 09/04/2018 ajout protocoles pour BisSeq
		lp.add(newProtocol("nugen-ovation-ultralow-methyl-seq-system-1-96","NUGEN Ovation Ultralow Methyl-Seq System 1-96","?","1","production",
				asList("bisseq-lib-prep")));
		
		// 04/06/2018 ajout protocoles pour OxBisSeq
		lp.add(newProtocol("oxbisseq-v3-1","OxBisSeq_v3.1","?","1","production",
				asList("oxbisseq-and-bisseq-lib-prep")));
		
		// 29/10/2018 ajout protocoles pour processus RNAseq (DEV) NGL-1226
		lp.add(newProtocol("ssmrna-seq-truseq", "ssmRNAseq Truseq","?","1","production",
				asList("frg-cdna-indexing",
					   "dev-pcr-amplification")));
		
		lp.add(newProtocol("sstotrna-seq-truseq", "sstotRNAseq Truseq","?","1","production",
				asList("frg-cdna-indexing",
					   "dev-pcr-amplification")));
		
		lp.add(newProtocol("mrnaseq-truseq","mRNAseqTruseq","?","1","production",
				asList("cdna-synthesis")));
		
		lp.add(newProtocol("mrnaseq-smarter-ultra-low-v4","mRNAseq Smarter Ultra Low v4","?","1","production",
				asList("cdna-synthesis")));
		
		lp.add(newProtocol("nextera-xt", "Nextera XT","?","1","production",
				asList("lib-and-pcr")));
		
		// 02/05/2019 ajout protocoles ATACseq NGL-1725/NGL-2607
		lp.add(newProtocol("atac-seq-greenleaf", "ATACSeq Greenleaf","?","1","production",
				asList("permeabilization-transposition-purification")));
		
		lp.add(newProtocol("fast-atac", "Fast ATAC","?","1","production",
				asList("permeabilization-transposition-purification")));
		
		lp.add(newProtocol("omni-atac", "Omni ATAC","?","1","production",
				asList("permeabilization-transposition-purification")));
		
		lp.add(newProtocol("pcr-amplification-and-purification","Amplification PCR + purification","?","1","production", 
				asList("pcr-amplif-and-purif-atac-chip-seq")));
		
		lp.add(newProtocol("qpcr-nb-cycle-setting-protocol", "qPCR (détermination nb cycles PCR)","?","1","production",
				asList("qpcr-nb-cycle-setting")));
		
		
		
		
		//------------Experiences de Control Qualité------------------------------
		lp.add(newProtocol("7-sop-miseq","7_SOP_Miseq","?","1","production", 
				asList("miseq-qc")));
		
		lp.add(newProtocol("3a-kapa-qPCR-240715","3A_KAPA_qPCR_240715", "?","1","production",
				asList("qpcr-quantification")));
		
		// 01/09/2016 ajout 
		lp.add(newProtocol("labchip-gx","LabChiP_GX", "?","1","production",
				asList("labchip-migration-profile")));
		
		// 27/02/2017 ajout protocole pour Bioanalyzer 
		lp.add(newProtocol("bioanalyzer","BioAnalyzer", "?","1","production",
				asList("bioanalyzer-migration-profile")));
		
		// 04/12/2018 ajout protocole Qubit HS
		lp.add(newProtocol("qubit-hs","Qubit HS", "?","1","production",
				asList("fluo-quantification")));
		
		// 23/07/2019 ajout protocole Qubit
		lp.add(newProtocol("qubit","Qubit", "?","1","production",
				asList("fluo-quantification")));
		
		//------------Experiences de Purification-----------------------------
		// 30/03/2017 ajout Protocole pour Sizing 
		lp.add(newProtocol("nanopore-sizing-ptr","sizing nanopore","?","1","production", 
				asList("nanopore-sizing")));	
		
		
		for(Protocol protocole:lp){
			InstanceHelpers.save(InstanceConstants.PROTOCOL_COLL_NAME, protocole,ctx);
			logger.debug("protocol '"+protocole.name + "' saved..." );
		}
	}
	
	/*
	protocole	Smarter V4	Ovation RNAseq system v2	TruSeq Stranded poly A	TruSeq Stranded Proc	Smarter Stranded	Indac
	rnaLibProtocol	smarterV4	ovationRNAseqSystemV2	truseqStrandedPolyA	truseqStrandedProk	smarterStranded	indac
	strandOrientation	?	?	reverse	reverse	forward	reverse
	cDNAsynthesisType	?	?	?	?	?	?
	 */
	@SafeVarargs
	private static Map<String, PropertyValue> concatMap(Map<String, PropertyValue>... map) {
		Map<String, PropertyValue> mapFinal = new HashMap<>(map.length); // <String, PropertyValue<?>>(map.length);
		for (int i = 0 ; i < map.length; i++)
			mapFinal.putAll(map[i]);
		return mapFinal;
	}
		
}