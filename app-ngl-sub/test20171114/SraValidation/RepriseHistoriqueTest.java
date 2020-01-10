package SraValidation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.junit.Test;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import play.Logger;
import models.laboratory.common.instance.State;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.common.instance.AbstractSample;
import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.ExternalSample;
import models.sra.submit.common.instance.ExternalStudy;
import models.sra.submit.common.instance.Readset;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.util.SraCodeHelper;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;
import services.RepriseHistorique;
import services.SubmissionServices;
import utils.AbstractTestsSRA;
import validation.ContextValidation;

/* Pour chaque projet charger d'abord les study et samples puis les experiment et runs. 
 * Si des samples ou study sont utilisés par des experiments et qu'ils n'existent pas dans ngl_sub, 
 * ils seront declarés comme sample ou study externes.
 */
public class RepriseHistoriqueTest extends AbstractTestsSRA {
	public static String adminComment = "Creation dans le cadre d'une reprise d'historique"; 

	
	//@Test
	public void repriseHistoSamplesTest() throws IOException, SraException {
		/*File xmlSample = new File("/env/cns/submit_traces/SRA/REPRISE_HISTORIQUE_ebi/database/EBI_21_03_2017/ebi_extract_samples_1.xml");
		_repriseHistoriqueSamplesTest(xmlSample);
		xmlSample = new File("/env/cns/submit_traces/SRA/REPRISE_HISTORIQUE_ebi/database/EBI_21_03_2017/ebi_extract_samples_2.xml");
		_repriseHistoriqueSamplesTest(xmlSample);
		xmlSample = new File("/env/cns/submit_traces/SRA/REPRISE_HISTORIQUE_ebi/database/EBI_21_03_2017/ebi_extract_samples_3.xml");
		_repriseHistoriqueSamplesTest(xmlSample);
		xmlSample = new File("/env/cns/submit_traces/SRA/REPRISE_HISTORIQUE_ebi/database/EBI_21_03_2017/ebi_extract_samples_4.xml");
		_repriseHistoriqueSamplesTest(xmlSample);
		*/
	}
	
	private void _repriseHistoriqueSamplesTest(File xmlSample) throws IOException, SraException {
		String user = "william";
		RepriseHistorique repriseHistorique = new RepriseHistorique();
		try{
			List<Sample> listSamples = repriseHistorique.forSamples(xmlSample, user);
			System.out.println("retour dans repriseHistoriqueSamplesTest avec " + listSamples.size() + " samples");
			List<Sample> listSamplesToSave = new ArrayList<Sample>();
			String pattern = "TARA_([A-Z]{2,3})(_|-)";
			java.util.regex.Pattern p = Pattern.compile(pattern);
			// Verifier la validité des samples
			for (Sample sample : listSamples) {
				// enlever les samples TARA soumis par Pesant. Attention nous avons soumis des samples TARA dans le projet BCM et ALP
				// samples Pesant de la forme TARA_Y110001358 TARA_G100010276
				// samples Tara soumis par CNS de la forme TARA_BCB_ABBI ou TARA_ALP_KC
				if (sample.code.startsWith("TARA")){
					Matcher m = p.matcher(sample.code);
					if ( !m.find() ) {
						System.out.println("#### abandon du sampleCode = "+ sample.code);
						continue;
					} 
				}
						
				// enlever les samples nanopores du projet BCM qui ont un status cancelled à l'EBI :
				if ( sample.code.equals("sample_ABH")||sample.code.equals("sample_ADM")||sample.code.equals("sample_ADQ")||
					 sample.code.equals("sample_ADS")||sample.code.equals("sample_AEG")||sample.code.equals("sample_AKR")||
					 sample.code.equals("sample_ANE")||sample.code.equals("sample_ASN")||sample.code.equals("sample_AVB")||
					 sample.code.equals("sample_BAH")||sample.code.equals("sample_BAL")||sample.code.equals("sample_BAM")||
					 sample.code.equals("sample_BCN")||sample.code.equals("sample_BDF")||sample.code.equals("sample_BHH")||
					 sample.code.equals("sample_CBM")||sample.code.equals("sample_CEI")||sample.code.equals("sample_CFA")||
					 sample.code.equals("sample_CFF")||sample.code.equals("sample_CIC")||sample.code.equals("sample_CNT")||
					 sample.code.equals("sample_CRV")){
					//System.out.println("#### abandon du sampleCode = "+ sample.code);
					continue;
					 } 
						
				if (! listSamplesToSave.contains(sample)){
					listSamplesToSave.add(sample);
				}

				if (sample.code.equals("1")){
					sample.projectCode = "AWU";  // sample soumis via interface ebi pour AWU_moleculo
				}
				if (sample.code.equals("AQS_1")){
					sample.projectCode = "AQS";
				}
				if (sample.code.equals("AQG_1")){
					sample.projectCode = "AQG";
				}
				if (sample.code.equals("AGR_1")){
					sample.projectCode = "AGR";
				}
				if (sample.code.equals("AEQ_203904")){
					sample.projectCode = "AEQ";
				}
				if (sample.code.equals("SY_39416")){
					sample.projectCode = "SY";
				}
				
				if (sample.code.startsWith("AEI_")){
					sample.projectCode = "AEI";
				}
				if (sample.code.startsWith("AHH_1")){
					sample.projectCode = "AHH";
				}
				if (sample.code.equals("AFR_3055")){
					sample.projectCode = "AFR";
				}
				if (sample.code.equals("LQ_2880")){
					sample.projectCode = "LQ";
				}
				if (sample.code.equals("ASI_5888")){
					sample.projectCode = "ASI";
				}
				if (sample.code.equals("ABK_5888")){
					sample.projectCode = "ABK";
				}
				if (sample.code.equals("AKN_4875")){
					sample.projectCode = "AKN";
				}				
				if (sample.code.equals("Prokaryotic RNA MIX")){
					sample.projectCode = "BDP";
				}
				if (sample.code.startsWith("Acinetobacter sp. ADP1")){
					sample.projectCode = "AWK";
				}
				if (sample.code.startsWith("Lactococcus lactis MG1363")){
					sample.projectCode = "BCZ";
				}
				if (sample.code.equals("Escherichia coli str. K-12 substr. MG1655 RNA sequencing")){
					sample.projectCode = "BDC";
				}
				if (sample.code.equals("Bacillus subtilis subsp. subtilis str. 168 RNA sequencing")){
					sample.projectCode = "BDD";
				}
				System.out.println("dans repriseHistoriqueSamplesTest => sample : " + sample.code);
				ContextValidation contextValidation = new ContextValidation(user);
				contextValidation.setCreationMode();
				contextValidation.getContextObjects().put("type", "sra");
				sample.validate(contextValidation);
				System.out.println("\ndisplayErrors pour validationSample:" + sample.code);
				if (contextValidation.errors.size()==0) {
					//System.out.println("Sample "+ sample.code + " valide ");
				} else {
					contextValidation.displayErrors(Logger.of("SRA"));
					throw new SraException("Sample " + sample.code + " non valide");
				}
			}
			// Sauver tous les samples
			for (Sample sample : listSamplesToSave) {
				if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SAMPLE_COLL_NAME, AbstractSample.class, "code", sample.code)){	
					MongoDBDAO.save(InstanceConstants.SRA_SAMPLE_COLL_NAME, sample);
					//System.out.println ("ok pour sauvegarde dans la base du sample " + sample.code);
				}
			}
		}catch (IOException e) {
			System.out.println("Exception de type IO: " + e.getMessage());
			throw e;
		} catch (SraException e) {
			System.out.println("Exception de type SRA: " +e.getMessage());
			throw e;
		} 
	}
	
	//@Test
	public void repriseHistoriqueStudiesTest() throws IOException, SraException {
		//File xmlStudy = new File("/env/cns/submit_traces/SRA/REPRISE_HISTORIQUE_ebi/database/EBI_21_03_2017/ebi_studies.xml");
		File xmlStudy = new File("/env/cns/home/sgas/update_database/ebi_studies.xml");

		String user = "william";
		RepriseHistorique repriseHistorique = new RepriseHistorique();
		try {
			List<Study> listStudies = repriseHistorique.forStudies(xmlStudy, user);
			System.out.println("retour dans repriseHistoriqueStudiesTest");
			// Verifier la validité des studies
			for (Study study: listStudies) {
				System.out.println("dans repriseHistoriqueStudiesTest => study : '" + study.code+"'" + " et accession = '" + study.accession+"'");
				// study declaré via interface mais jamais utilisé pour soumission
				if (study.code.equals("ena-STUDY-GSC-04-12-2013-09:50:11:936-175")){
					continue;
				}
				if (study.code.equals("SY")){
					study.projectCodes.add("SY");
				}
				if (study.code.equals("project_BIK")){
					study.projectCodes.add("BIK");
				}
				if (study.code.equals("ASI")){
					study.projectCodes.add("ASI");
				}
				if (study.code.equals("AQS")){
					study.projectCodes.add("AQS");
				}
				if (study.code.equals("LQ")){
					study.projectCodes.add("LQ");
				}
				if (study.code.equals("AFR")){
					study.projectCodes.add("AFR");
				}
				if (study.code.equals("AEI")){
					study.projectCodes.add("AEI");
				}
				if (study.code.equals("AKN")){
					study.projectCodes.add("AKN");
				}
				if (study.code.equals("AGR")){
					study.projectCodes.add("AGR");
				}			
				if (study.code.equals("project_BNA")){
					study.projectCodes.add("BNA");
				}
				if (study.code.equals("AEQ")){
					study.projectCodes.add("AEQ");
				}
				if (study.code.equals("AHH")){
					study.projectCodes.add("AHH");
				}
				if (study.code.equals("ABK")){
					study.projectCodes.add("ABK");
				}
				if (study.code.equals("AQG")){
					study.projectCodes.add("AQG");
				}
				if (study.code.equals("project_BMI")){
					study.projectCodes.add("BMI");
				}				
				if (study.code.equals("project_BHQ")){
					study.projectCodes.add("BHQ");
				}
				if (study.code.equals("project_BMR")){
					study.projectCodes.add("BMR");
				}
				
				if (study.code.equals("project_BII")){
					study.projectCodes.add("BII");
				}
				
				// study moleculo du projet AWC declare via interface ebi
				if (study.code.equals("ena-STUDY-GSC-08-03-2017-14:20:01:829-5")){
					study.projectCodes.add("AWU");
				}
				
				
				ContextValidation contextValidation = new ContextValidation(user);
				contextValidation.setCreationMode();
				contextValidation.getContextObjects().put("type", "sra");
				study.validate(contextValidation);
				System.out.println("\ndisplayErrors pour validationStudy:" + study.code);
				if (contextValidation.errors.size()==0) {
					System.out.println("Study "+ study.code + " valide ");
				} else {
					contextValidation.displayErrors(Logger.of("SRA"));
					if (!study.code.equals("AEI")){
						throw new SraException("Study " + study.code + " non valide");
					}
				}
			}
		
			// Sauver tous les study
			for (Study study : listStudies) {
				if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_STUDY_COLL_NAME, AbstractStudy.class, "code", study.code)){	
					MongoDBDAO.save(InstanceConstants.SRA_STUDY_COLL_NAME, study);
					System.out.println ("ok pour sauvegarde dans la base du study " + study.code);
				}
			}
			
		} catch (IOException e) {
			System.out.println("Exception de type IO: " + e.getMessage());
			throw e;
		} catch (SraException e) {
			System.out.println("Exception de type SRA: " +e.getMessage());
			throw e;
		} 
	}
	
	//@Test
	public void repriseHistoriqueExperimentsTest() throws IOException, SraException {

		//File xmlExperiment = new File("/env/cns/submit_traces/SRA/REPRISE_HISTORIQUE_ebi/database/EBI_21_03_2017/ebi_experiments.xml");
		File xmlExperiment = new File("/env/cns/home/sgas/update_database/ebi_experiments.xml");
		String user = "william";
		RepriseHistorique repriseHistorique = new RepriseHistorique();
		try {
			List<Experiment> listExperiments = repriseHistorique.forExperiments(xmlExperiment, user);
			// Verifier la validité des experiments sans les runs:
			ContextValidation contextValidation = new ContextValidation(user);
			contextValidation.setCreationMode();
			contextValidation.getContextObjects().put("type", "sra");
			int count = listExperiments.size();
			int cp = 0;
			for (Experiment experiment : listExperiments) {
				// Ecarter l'experiment exp_KY_LOSU_2_81DHTABXX qui a ete soumis et a recu le numeros d'accession ERX240825 sans que le run
				// correspondant ait recu un numeros d'accession. => on considere que le readset KY_LOSU_2_81DHTABXX n'a pas ete soumis puisqu'aucune
				// donnée brute (fichiers associés au run n'a ete soumis).
				if (experiment.code.equals("exp_KY_LOSU_2_81DHTABXX")){
					System.out.println("experiment exp_KY_LOSU_2_81DHTABXX du projet KY ignore");
					continue;
				}
				// Ecarter l'experiment exp_BAT_EIOSW_6_C1CRCACXX.IND8 (ERX223471) qui a ete supprimé
				if (experiment.code.equals("exp_BAT_EIOSW_6_C1CRCACXX.IND8")) {
					System.out.println("exp_BAT_EIOSW_6_C1CRCACXX.IND8 du projet BAT ignore");
					continue;	
				}
				cp ++;
				
				/*System.out.println("dans repriseHistoriqueExperimentsTest => experiment : " + experiment.code);
				System.out.println("cp / count = " + cp + "/" + count);
				System.out.println("experiment.accession : " + experiment.accession);
				System.out.println("experiment.studyCode : " + experiment.studyCode);
				System.out.println("experiment.sampleCode : " + experiment.sampleCode);
				System.out.println("experiment.readSetCode : " + experiment.readSetCode);
				*/
			
				if (experiment.code.equals("exp_1.TCA.FO740SW01")||experiment.code.equals("exp_3.TCA.FL8POTV03")){
					experiment.projectCode = "YK";
				}
				if (experiment.code.equals("exp_1.TCA.FG5FMAE01") || experiment.code.equals("exp_1.TCA.FGFGJ1101")){
					experiment.projectCode = "AAZ";
				}
				
				if (experiment.code.endsWith("_ONT_R7")){
					experiment.projectCode = "BCM";
				}
				
				if (experiment.code.startsWith("AKN") && experiment.studyCode.equals("AKN")){
					experiment.projectCode = "AKN";
				}
				if (experiment.code.startsWith("AEI") && experiment.studyCode.equals("AEI")){
					experiment.projectCode = "AEI";
				}
				if (experiment.code.equals("AEIBSAOSS")){
					experiment.readSetCode = "AEI_BSAOSS_5_30WHCAAXX";
				}				
				if (experiment.code.equals("AEIFPAOSS")){
					experiment.readSetCode = "AEI_FPAOSS_6_305RRAAXX";
				}				
				if (experiment.code.equals("AEIDQAOSS")){
					experiment.readSetCode = "AEI_DQAOSS_4_313J7AAXX";
				}				
				if (experiment.code.equals("AEIABAOSS")){
					experiment.readSetCode = "AEI_ABAOSS_2_20F54AAXX";
				}				
				if (experiment.code.equals("AEIADAOSS")){
					experiment.readSetCode = "AEI_ADAOSS_4_20F54AAXX";
				}				
				if (experiment.code.equals("AEIDRAOSS")){
					experiment.readSetCode = "AEI_DRAOSS_5_313J7AAXX";
				}				
				if (experiment.code.equals("AEIACAOSS")){
					experiment.readSetCode = "AEI_ACAOSS_3_20F54AAXX";
				}				
				if (experiment.code.equals("AEIBTAOSS")){
					experiment.readSetCode = "AEI_BTAOSS_6_30WHCAAXX";
				}				
				if (experiment.code.equals("AEIAEAOSS")){
					experiment.readSetCode = "AEI_AEAOSS_5_20F54AAXX";
				}				
				if (experiment.code.equals("AEIHBAOSS")){
					experiment.readSetCode = "AEI_HBAOSS_6_62F0BAAXX";
				}				
				if (experiment.code.equals("AEICAAOSS")){
					experiment.readSetCode = "AEI_CAAOSS_5_30WH4AAXX";
				}				
				if (experiment.code.equals("AEIBVAOSS")){
					experiment.readSetCode = "AEI_BVAOSS_7_30WHCAAXX";
				}				
																																															
				if (experiment.code.equals("AEIBRAOSS")){
					experiment.readSetCode = "AEI_BRAOSS_4_30WHCAAXX";
				}				
				if (experiment.code.equals("AEIBQAOSS")){
					experiment.readSetCode = "AEI_BQAOSS_3_30WHCAAXX";
				}				
				if (experiment.code.equals("AEIFQAOSS")){
					experiment.readSetCode = "AEI_FQAOSS_3_42DP4AAXX";
				}				
				if (experiment.code.equals("AEIHAAOSS")){
					experiment.readSetCode = "AEI_HAAOSS_5_62F0BAAXX";
				}
				if (experiment.code.equals("LQHOSS")){
					experiment.readSetCode = "LQ_HOSS_8_20EG4AAXX";
				}				
				if (experiment.code.equals("LQIOSS")){
					experiment.readSetCode = "LQ_IOSS_7_20EG4AAXX";
				}	
				
				// problemes :
				if (experiment.code.equals("SYBOSS")){
					experiment.readSetCode = "SY_BOSS_1_305E0AAXX";
					// 1 readset correspondant à la partie BOSS du run bio 090422_HELIUM_305E0AAXX
				}				
				if (experiment.code.equals("SYCOSS")){
					experiment.readSetCode = "SY_COSS_2_305E0AAXX|SY_COSS_4_305E0AAXX|SY_COSS_5_305E0AAXX|SY_COSS_6_305E0AAXX"; 
					// 4 readsets correspondant à la partie COSS du run bio 090422_HELIUM_305E0AAXX
				}				
				if (experiment.code.equals("SYDOSS")){
					experiment.readSetCode = "SY_DOSS_3_305E0AAXX";
					// 1 readset correspondant à la partie DOSS du run bio 090422_HELIUM_305E0AAXX
				}	
				if (experiment.code.equals("AQGAOSS")){  
					experiment.readSetCode = "AQG_AOSS_5_61LFRAAXX";
				}
				if (experiment.code.equals("AHHAOSS")){
					//experiment.readSetCode = "AHH_AOSS_1_2_3_4_42L9WAAXX";
					experiment.readSetCode = "AHH_AOSS_1_42L9WAAXX|AHH_AOSS_2_42L9WAAXX|AHH_AOSS_3_42L9WAAXX|AHH_AOSS_4_42L9WAAXX";
					// 4 readset correspondant au run 090724_AZOTE_42L9WAAXX_AHHAOSS
				}	
				if (experiment.code.equals("AHHBOSS")){
					experiment.readSetCode = "AHH_BOSS_5_42L9WAAXX|AHH_BOSS_6_42L9WAAXX";  
					// 2 readset correspondant au run 090724_AZOTE_42L9WAAXX_AHHBOSS 
				}
				if (experiment.code.equals("AHHCOSS")){
					experiment.readSetCode = "AHH_COSS_8_42L9WAAXX|AHH_COSS_8_42L9WAAXX";
					// 2 readset correspondant au run 090724_AZOTE_42L9WAAXX_AHHCOSS
				}
				
				// Ex de données nanopore avec readspec :
				if (experiment.code.equals("exp_AWK_ONT_20Kb_R7")){
					experiment.readSetCode = "AWK_K_ONT_1_MN2064006_A";
				}
				if (experiment.code.equals("exp_AWK_ONT_20Kb_R7.3")){
					experiment.readSetCode = "AWK_M_ONT_1_FAA43210_A|AWK_H_ONT_1_FAA43204_A|AWK_H_ONT_1_FAA17573_A";
					//3 readset correspondant à 3 runs rattachés au meme experiment.
				}				
				if (experiment.code.equals("exp_AWK_ONT_8Kb_R7")){ 
					experiment.readSetCode = "AWK_G_ONT_1_MN2064525_A";
				}
				
				
				// Dans le cadre du projet BCM, 85 run soumis avec probleme : plusieurs runs associés au meme exp
				
				// projetBCM , 3 readsets associés à meme exp avec en plus erreur sur nom de run avec MK au lieu de MN
				if (experiment.code.equals("exp_ABH_ONT_R7")){ 
					experiment.readSetCode = "BCM_ABH_ONT_1_FAA54955_A|BCM_ABH_ONT_1_FAA56049_A|BCM_ABH_ONT_1_FAA61302_A";
				}
				// projet BCM, 5 readsets associés à meme exp avec en plus erreur sur nom de run avec MK au lieu de MN
				if (experiment.code.equals("exp_ADM_ONT_R7")){ 
					experiment.readSetCode = "BCM_ADM_ONT_1_FAA62005_A|BCM_ADM_ONT_1_FAA83469_A|BCM_ADM_ONT_1_FAA84613_A|BCM_ADM_ONT_1_FAA84613_B|BCM_ADM_ONT_1_FAA84655_B";
				}
				// projet BCM, 2 readsets associés à meme exp avec en plus erreur sur nom de run avec MK au lieu de MN
				if (experiment.code.equals("exp_ADQ_ONT_R7")){ 
					experiment.readSetCode = "BCM_ADQ_ONT_1_FAA61996_A|BCM_ADQ_ONT_1_FAA62118_A";
				}				
				// projet BCM, 5 readsets associés à meme exp avec en plus erreur sur nom de run avec MK au lieu de MN
				if (experiment.code.equals("exp_ADS_ONT_R7")){ 
					experiment.readSetCode = "BCM_ADS_ONT_1_FAA63405_A|BCM_ADS_ONT_1_FAA85313_A|BCM_ADS_ONT_1_FAA85374_A|BCM_ADS_ONT_1_FAA85374_B|BCM_ADS_ONT_1_FAA85382_A";
				}
				// projet BCM,
				if (experiment.code.equals("exp_AEG_ONT_R7")){ 
					experiment.readSetCode = "BCM_AEG_ONT_1_FAA61303_A";
				}
				// projet BCM,
				if (experiment.code.equals("exp_AKR_ONT_R7")){ 
					experiment.readSetCode = "BCM_AKR_ONT_1_FAA62042_A";
				}
				// projet BCM,
				if (experiment.code.equals("exp_ANE_ONT_R7")){ 
					experiment.readSetCode = "BCM_ANE_ONT_1_FAA63738_A|BCM_ANE_ONT_1_FAA85663_A|BCM_ANE_ONT_1_FAA85828_A|BCM_ANE_ONT_1_FAA86381_A|BCM_ANE_ONT_1_FAA87212_A";
				}
				// projet BCM,
				if (experiment.code.equals("exp_ASN_ONT_R7")){ 
					experiment.readSetCode = "BCM_ASN_ONT_1_FAA62151_A|BCM_ASN_ONT_1_FAA63407_A|BCM_ASN_ONT_1_FAA63910_A";
				}
				// projet BCM,
				if (experiment.code.equals("exp_AVB_ONT_R7")){ 
					experiment.readSetCode = "BCM_AVB_ONT_1_FAA70272_A";
				}
				// projet BCM,
				if (experiment.code.equals("exp_BAH_ONT_R7")){ 
					experiment.readSetCode = "BCM_BAH_ONT_1_FAA64493_A|BCM_BAH_ONT_1_FAA67952_A";
				}
				// projet BCM,
				if (experiment.code.equals("exp_BAL_ONT_R7")){ 
					experiment.readSetCode = "BCM_BAL_ONT_1_FAA62115_A";
				}
				// projet BCM, 10 readset associés au meme experiment
				if (experiment.code.equals("exp_BAM_ONT_R7")){ 
					experiment.readSetCode = "BCM_BAM_ONT_1_FAA46213_A|BCM_BAM_ONT_1_FAA46235_A|BCM_BAM_ONT_1_FAA46504_A|BCM_BAM_ONT_1_FAA47108_A|BCM_BAM_ONT_1_FAA47108_B|BCM_BAM_ONT_1_FAA47137_A|BCM_BAM_ONT_1_FAA47137_B|BCM_BAM_ONT_1_FAA47138_A|BCM_BAM_ONT_1_FAA57509_A|BCM_BAM_ONT_1_FAA57511_A|BCM_BAM_ONT_1_FAA59903_A|BCM_BAM_ONT_1_FAA60062_A|BCM_BAM_ONT_1_FAA60062_B|BCM_BAM_ONT_1_FAA61618_A";
				}
				// projet BCM,
				if (experiment.code.equals("exp_BCN_ONT_R7")){ 
					experiment.readSetCode = "BCM_BCN_ONT_1_FAA65515_A|BCM_BCN_ONT_1_FAA70264_A|BCM_BCN_ONT_1_FAA70841_A";
				}
				if (experiment.code.equals("exp_BDF_ONT_R7")){ 
					experiment.readSetCode = "BCM_BDF_ONT_1_FAA63782_A|BCM_BDF_ONT_1_FAA64638_A";
				}
				if (experiment.code.equals("exp_BHH_ONT_R7")){ 
					experiment.readSetCode = "BCM_BHH_ONT_1_FAA62099_A|BCM_BHH_ONT_1_FAA64754_A";
				}
				if (experiment.code.equals("exp_CBM_ONT_R7")){ 
					experiment.readSetCode = "BCM_CBM_ONT_1_FAA68545_A|BCM_CBM_ONT_1_FAA69054_A|BCM_CBM_ONT_1_FAA70265_A";
				}
				if (experiment.code.equals("exp_CEI_ONT_R7")){ 
					experiment.readSetCode = "BCM_CEI_ONT_1_FAA68110_A|BCM_CEI_ONT_1_FAA68413_A|BCM_CEI_ONT_1_FAA68413_B";
				}
				if (experiment.code.equals("exp_CFA_ONT_R7")){ 
					experiment.readSetCode = "BCM_CFA_ONT_1_FAA52002_A|BCM_CFA_ONT_1_FAA54985_A|BCM_CFA_ONT_1_FAA56751_A|BCM_CFA_ONT_1_FAA56757_A|BCM_CFA_ONT_1_FAA57368_A";
				}
				if (experiment.code.equals("exp_CFF_ONT_R7")){ 
					experiment.readSetCode = "BCM_CFF_ONT_1_FAA61280_A";
				}
				if (experiment.code.equals("exp_CIC_ONT_R7")){ 
					experiment.readSetCode = "BCM_CIC_ONT_1_FAA62158_B|BCM_CIC_ONT_1_FAA81239_A|BCM_CIC_ONT_1_FAA83576_B|BCM_CIC_ONT_1_FAA83642_A|BCM_CIC_ONT_1_FAA83642_B";
				}
				if (experiment.code.equals("exp_CNT_ONT_R7")){ 
					experiment.readSetCode = "BCM_CNT_ONT_1_FAA62083_A";
				}
				if (experiment.code.equals("exp_CRV_ONT_R7")){
					experiment.readSetCode = "BCM_CRV_ONT_1_FAA76621_A|BCM_CRV_ONT_1_FAA81631_A|BCM_CRV_ONT_1_FAA86951_A|BCM_CRV_ONT_1_FAA87932_A|BCM_CRV_ONT_1_FAA88037_A|BCM_CRV_ONT_1_FAA89620_A|BCM_CRV_ONT_1_FAA95819_A|BCM_CRV_ONT_1_FAA99560_A|BCM_CRV_ONT_1_FAA99566_A|BCM_CRV_ONT_1_FAA99616_A|BCM_CRV_ONT_1_FAA99643_A|BCM_CRV_ONT_1_FAA99955_A|BCM_CRV_ONT_1_FAD07097_A|BCM_CRV_ONT_1_FAD07097_B|BCM_CRV_ONT_1_FAD07266_A|BCM_CRV_ONT_1_FAD14584_A|BCM_CRV_ONT_1_FAD14737_A";
				}
				//....
				// end projet BCM
				// end probleme
					
				if (experiment.code.equals("BAT_EIOSW_6_C1CRCACXX.IND8_replacement")){
					experiment.readSetCode = "BAT_EIOSW_6_C1CRCACXX.IND8";
				}																												
				if (experiment.code.startsWith("AQS") && experiment.studyCode.equals("AQS")){
					experiment.projectCode = "AQS";
				}
				if (experiment.code.startsWith("LQ") && experiment.studyCode.equals("LQ")){
					experiment.projectCode = "LQ";
				}
				if (experiment.code.startsWith("SY") && experiment.studyCode.equals("SY")){
					experiment.projectCode = "SY";
				}
				if (experiment.code.startsWith("AHH") && experiment.studyCode.equals("AHH")){
					experiment.projectCode = "AHH";
				}
				if (experiment.code.startsWith("AGR") && experiment.studyCode.equals("AGR")){
					experiment.projectCode = "AGR";
				}
				if (experiment.code.startsWith("AQG") && experiment.studyCode.equals("AQG")){
					experiment.projectCode = "AQG";
				}
				if (experiment.code.startsWith("AFR") && experiment.studyCode.equals("AFR")){
					experiment.projectCode = "AFR";
				}
				if (! experiment.code.equals("exp_4.GAC.AEH_NOTM_GDU6DPD04.RLMID5") && !experiment.code.equals("exp_4.GAC.AEH_DOTS_GE62K7O04")
					&& !experiment.code.equals("exp_3.TCA.AEH_BOTS_FYX1OO003") && !experiment.code.equals("exp_1.TCA.AEH_HOTS_GFWZGIZ01")
					&& !experiment.code.equals("exp_1.TCA.AEH_DOTS_F7I4VNB01") && !experiment.code.equals("exp_1.TCA.AEH_BOTS_FXW012U01")
					&& !experiment.code.equals("AKNDOTS_TCA") && !experiment.code.equals("AKNCOTS_TCA")
					&& !experiment.code.equals("exp_2.TCA.AEH_IOTS_GDH4XDG02")&& !experiment.code.equals("exp_1.TCA.AEH_COTS_F27IL3E01")
					&& !experiment.code.equals("exp_2.TCA.AEH_COTS_F2Z3HNU02") && !experiment.code.equals("exp_2.TCA.AEH_BOTS_F1GT3LX02")
					&& !experiment.code.equals("exp_4.TCA.AEH_BOTS_FQU3J4004") && !experiment.code.equals("exp_4.TCA.AEH_BOTS_FQU3J4004")
					&& !experiment.code.equals("AQSAOTS_GAC") && !experiment.code.equals("AQSBOTS_GAC") 
					&& !experiment.code.equals("AKNAOTS_TCA") && !experiment.code.equals("AKNBOTS_TCA") 
					&& !experiment.code.equals("AGRAOTS_TCA") && !experiment.code.equals("AGRBOTS_TCA")
					&& !experiment.code.equals("AGRGOTS_TCA") && !experiment.code.equals("AGRFOTS_TCA")
					&& !experiment.code.equals("AGRCOTS_TCA") 
					&& !experiment.code.equals("AFRCOTS_TCA") && !experiment.code.equals("AFRBOTS_TCA")
					&& !experiment.code.equals("AFRAOTS_TCA") && !experiment.code.equals("AFRDOTS_TCA")
					&&!experiment.code.equals("exp_2.TCA.AEQ_AOTS_FQ8SXUV02")&& !experiment.code.equals("exp_BED_EXTOSU_6_FC00309.IND6")
					// donnees moleculo du projet AWU soumises via interface ebi en mars 2017
					&& !experiment.code.equals("ena-EXPERIMENT-GSC-08-03-2017-14:55:06:573-1")
					&&!experiment.code.equals("ena-EXPERIMENT-GSC-08-03-2017-14:55:06:573-2")
					&& !experiment.code.equals("ena-EXPERIMENT-GSC-08-03-2017-14:55:06:573-3")
					&& !experiment.code.equals("ena-EXPERIMENT-GSC-08-03-2017-14:55:06:573-4")
					&& !experiment.code.equals("ena-EXPERIMENT-GSC-08-03-2017-14:55:06:573-5")
					&& !experiment.code.equals("ena-EXPERIMENT-GSC-08-03-2017-14:55:06:573-6")
					&&!experiment.code.equals("ena-EXPERIMENT-GSC-08-03-2017-14:55:06:573-7")
					&&!experiment.code.equals("ena-EXPERIMENT-GSC-08-03-2017-14:55:06:573-8")
					&&!experiment.code.equals("ena-EXPERIMENT-GSC-08-03-2017-14:55:06:573-9")
					&& !experiment.code.equals("ena-EXPERIMENT-GSC-08-03-2017-14:55:06:573-10")
					&& !experiment.code.equals("ena-EXPERIMENT-GSC-08-03-2017-14:55:06:573-11")
					&&!experiment.code.equals("ena-EXPERIMENT-GSC-08-03-2017-14:55:06:574-12")
					&&!experiment.code.equals("ena-EXPERIMENT-GSC-08-03-2017-14:55:06:574-13")
					&&!experiment.code.equals("ena-EXPERIMENT-GSC-08-03-2017-14:55:06:574-14")

					// Donnees oxford nanopores projet AWK soumis avec des readspec :
					&& !experiment.code.equals("exp_AWK_ONT_20Kb_R7.3")
					&& !experiment.code.equals("exp_AWK_ONT_8Kb_R7")
					&& !experiment.code.equals("exp_AWK_ONT_20Kb_R7")
					&& !experiment.code.equals("exp_CRV_ONT_R9")
					){
					experiment.validateLight(contextValidation);
					//System.out.println("\ndisplayErrors pour validationExperiment:" + experiment.code);
				}
				if (contextValidation.errors.size()==0) {
					//System.out.println("Experiment "+ experiment.code + " valide ");
				} else {
					contextValidation.displayErrors(Logger.of("SRA"));
					throw new SraException("Experiment " + experiment.code + " non valide");
				}
			}
			
			
			// Sauver tous les experiments et les samples et study externes et mettre à jour readset pour soumission en distinguant illumina, LS454 et nanopore
			int cp_pb = 0;
			System.out.println("#########Nbre d'exp = " + listExperiments.size());
			for (Experiment experiment : listExperiments) {
				//System.out.println("experiment = " + experiment.code);
				if (StringUtils.isNotBlank(experiment.sampleAccession)) {
					//System.out.println("experiment.sampleAccession = " + experiment.sampleAccession);
					if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SAMPLE_COLL_NAME, AbstractSample.class, "accession", experiment.sampleAccession)){
						//System.out.println("sample existe bien dans base");
						AbstractSample absSample = MongoDBDAO.findOne(InstanceConstants.SRA_SAMPLE_COLL_NAME, models.sra.submit.common.instance.AbstractSample.class, DBQuery.in("accession", experiment.sampleAccession));
						//System.out.println("absSample = " + absSample.code);

						if(StringUtils.isBlank(experiment.sampleCode)){
							experiment.sampleCode = absSample.code;
						} else {
							if (! experiment.sampleCode.equals(absSample.code)){
								//System.out.println("exp.sampleCode="+experiment.sampleCode+" et exp.sampleAC="+experiment.sampleAccession +" alors que dans database sample.code= "+ absSample.code +" sample.AC = "+absSample.accession);
								//System.out.println("######Remplacement dans l'experiment de "+experiment.sampleCode+" par "+ absSample.code);
								experiment.sampleCode = absSample.code;
							}
						}
					} else {
						// creation de l'externalSample et sauvegarde dans database :
						//System.out.println("creation d'un externalSample" + experiment.sampleCode);
						ExternalSample externalSample = new ExternalSample(); // objet avec state.code = submitted
						externalSample.accession = experiment.sampleAccession;
						externalSample.code = SraCodeHelper.getInstance().generateExternalSampleCode(externalSample.accession);
						experiment.sampleCode = externalSample.code;
						externalSample.state = new State("F-SUB", user);			
						externalSample.traceInformation.setTraceInformation(user);							
						externalSample.adminComment = adminComment;
						MongoDBDAO.save(InstanceConstants.SRA_SAMPLE_COLL_NAME, externalSample);
					}	
				} else {
					if (StringUtils.isNotBlank(experiment.sampleCode)) {
						if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SAMPLE_COLL_NAME, AbstractSample.class, "code", experiment.sampleCode)){	
							AbstractSample absSample = MongoDBDAO.findOne(InstanceConstants.SRA_SAMPLE_COLL_NAME, models.sra.submit.common.instance.AbstractSample.class, DBQuery.in("code", experiment.sampleCode));
							experiment.sampleAccession = absSample.accession;
						}
					}
				}
				
				if (StringUtils.isNotBlank(experiment.studyAccession)) {
					if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_STUDY_COLL_NAME, AbstractStudy.class, "accession", experiment.studyAccession)){	
						//System.out.println("study existe bien dans base pour '" + experiment.studyAccession +"'");
						AbstractStudy absStudy = MongoDBDAO.findOne(InstanceConstants.SRA_STUDY_COLL_NAME, models.sra.submit.common.instance.AbstractStudy.class, DBQuery.in("accession", experiment.studyAccession));
						//System.out.println("absStudy = " + absStudy.code);
						if(StringUtils.isBlank(experiment.studyCode)){
							experiment.studyCode = absStudy.code;
						} else {
							if (! experiment.studyCode.equals(absStudy.code)){
								//System.out.println("!!!***Remplacement dans l'experiment de "+experiment.studyCode+" par "+ absStudy.code);
							}
						}
					} else {
						// creation de l'externalStudy et sauvegarde dans database :
						//System.out.println("creation d'un externalStudy" + experiment.studyCode);
						ExternalStudy externalStudy = new ExternalStudy(); // objet avec state.code = submitted
						externalStudy.accession = experiment.studyAccession;
						externalStudy.code = SraCodeHelper.getInstance().generateExternalStudyCode(externalStudy.accession);
						experiment.studyCode = externalStudy.code;
						externalStudy.state = new State("F-SUB", user);			
						externalStudy.traceInformation.setTraceInformation(user);							
						externalStudy.adminComment = adminComment;
						MongoDBDAO.save(InstanceConstants.SRA_STUDY_COLL_NAME, externalStudy);
					}	
				} else {
					if (StringUtils.isNotBlank(experiment.studyCode)) {
						if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_STUDY_COLL_NAME, AbstractStudy.class, "code", experiment.studyCode)){	
							AbstractStudy absStudy = MongoDBDAO.findOne(InstanceConstants.SRA_STUDY_COLL_NAME, models.sra.submit.common.instance.AbstractStudy.class, DBQuery.in("code", experiment.studyCode));
							experiment.studyAccession = absStudy.accession;
						}
					}
				}	
				
				if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, "code", experiment.code)){	
					MongoDBDAO.save(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment);
					//System.out.println ("ok pour sauvegarde dans la base de l'experiment " + experiment.code);
				}
				String typeReadset = "";
				//System.out.println("experiment.typePlatform = " + experiment.typePlatform);
				//System.out.println("experiment.readSetCode = " + experiment.readSetCode);
				//System.out.println("experiment.projectCode = " + experiment.projectCode);

				if (experiment.typePlatform.equalsIgnoreCase("ls454")){
					typeReadset = "ls454";
				} else {
					if (experiment.typePlatform.equalsIgnoreCase("illumina")) {
						typeReadset = "illumina";
					} else if (experiment.typePlatform.equalsIgnoreCase("oxford_nanopore")) {
						System.out.println("platform nanopore pour experiment=" + experiment.code+ " et readset=" + experiment.readSetCode);
						typeReadset = "oxford_nanopore";
					} else {
						throw new SraException("experiment.typePlatform non gere : " + experiment.typePlatform);
					}
					// Mettre à jour la collection de readSet de ngl_seq dans le cas d'illumina et nanopore
					if (!MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, "code", experiment.readSetCode)){	
						//throw new SraException("ReadSet " + experiment.readSetCode + " absent de la collection des readset de ngl_seq ???");
						cp_pb++; 
						System.out.println("##############pb ReadSet " + cp_pb + " pour experiment.studyCode = "+experiment.studyCode +" experiment.code=" +  experiment.code + ", experiment.readSetCode="+ experiment.readSetCode + " absent de la collection des readset de ngl_seq ???");					

						// Traiter le cas des concatenation de readset
						String separator= "\\|";
						String [] nameReadSet = experiment.readSetCode.split(separator);
						for(int i =0; i < nameReadSet.length ; i++) {			
							if (! MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, "code", nameReadSet[i])){	
								System.out.println(nameReadSet[i] + " absent de la database ??????");
							} else {
								MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
								DBQuery.is("code", nameReadSet[i]),
								DBUpdate.set("submissionState.code", "F-SUB").set("traceInformation.modifyUser", user).set("traceInformation.modifyDate", new Date()));
							}

						}
											
					} else {
						MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
							DBQuery.is("code", experiment.readSetCode),
							DBUpdate.set("submissionState.code", "F-SUB").set("traceInformation.modifyUser", user).set("traceInformation.modifyDate", new Date()));
						//System.out.println("Mise à jour des readSet de ngl");
					}
				}
				
				// Inserer dans la collection des readSet de ngl-sub (readset non gerés dans ng_seq)
				Readset readset = new Readset();
				//System.out.println("Creation du readset dans ngl_sub" + experiment.readSetCode);
				readset.code = experiment.readSetCode;
				readset.type = typeReadset;
				readset.experimentCode = experiment.code;
				//System.out.println("dans ngl_sub :readsetCode = " + readset.code + ", readset_type = " + readset.type +", readsetExpCode = " + readset.experimentCode );
				if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_READSET_COLL_NAME, Readset.class, "code", experiment.readSetCode)){	
					try {
						MongoDBDAO.save(InstanceConstants.SRA_READSET_COLL_NAME, readset);	
					} catch (Exception e) {
						System.out.println("Exception : " + e.getMessage());
						throw e;
					}
				
				//System.out.println(" ok Creation du readset " + readset.code);
				}
			}
		
		} catch (IOException e) {
			System.out.println("Exception de type IO: " + e.getMessage());
			throw e;
		} catch (SraException e) {
			System.out.println("Exception de type SRA: " +e.getMessage());
			throw e;
		} 
	}
	
		
	//@Test
	public void _repriseHistoriqueRunsTest() throws IOException, SraException {
		//File xmlRun = new File("/env/cns/submit_traces/SRA/REPRISE_HISTORIQUE_ebi/database/EBI_21_03_2017/ebi_runs.xml");
		File xmlRun = new File("/env/cns/home/sgas/update_database/ebi_runs.xml");
		String user = "william";
		RepriseHistorique repriseHistorique = new RepriseHistorique();
		try {
			List<Run> listRuns = repriseHistorique.forRuns(xmlRun, user);
			System.out.println("retour dans repriseHistoriqueRunsTest  avec nbre runs = " + listRuns.size());
			// Verifier la validité des runs :
			for (Run run : listRuns) {
				System.out.println("dans repriseHistoriqueRunsTest => run : " + run.code);
				//System.out.println("run.accession : " + run.accession);
				//system.out.println("run.experimentCode : " + run.expCode);
				
				// Ecarter le run run_BAT_EIOSW_6_C1CRCACXX.IND8 (ERX248937) qui a ete supprimé
				if (run.code.equals("run_BAT_EIOSW_6_C1CRCACXX.IND8")) {
					System.out.println("run_BAT_EIOSW_6_C1CRCACXX.IND8 du projet BAT ignore");
					continue;	
				}
				ContextValidation contextValidation = new ContextValidation(user);
				contextValidation.setCreationMode();
				System.out.println("run.accession : " + run.accession);

				contextValidation.getContextObjects().put("type", "sra");
				System.out.println("run.experimentCode : " + run.expCode);

				run.validateLight(contextValidation);
				System.out.println("\ndisplayErrors pour validationRun:" + run.code);
				if (contextValidation.errors.size()==0) {
					System.out.println("Run "+ run.code + " valide ");
				} else {
					System.out.println("Run " + run.code + " abandon, non valide");
					contextValidation.displayErrors(Logger.of("SRA"));
					continue;
				}
				if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, "code", run.expCode)){
					System.out.println("Recuperation de l'experiment "+ run.expCode);
					Experiment experiment= MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, models.sra.submit.sra.instance.Experiment.class, run.expCode);			
					experiment.run = run;
					MongoDBDAO.save(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, experiment);	
					//System.out.println ("ok pour sauvegarde dans la base de l'experiment " + experiment.code + " avec son run "+ run.code);
				} else {
					System.out.println ("Probleme pour sauvegarde dans la base de l'experiment " + run.expCode +" avec son run " + run.code);
				}
			}
		} catch (IOException e) {
			System.out.println("Exception de type IO: " + e.getMessage());
			throw e;
		} catch (SraException e) {
			System.out.println("Exception de type SRA: " +e.getMessage());
			throw e;
		} 
	}
}
