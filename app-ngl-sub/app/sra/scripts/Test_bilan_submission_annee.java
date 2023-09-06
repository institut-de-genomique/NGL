package sra.scripts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;


import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import models.sra.submit.sra.instance.Experiment;

/*
 * Script à utiliser pour recharger les md5 d'une soumission a partir de NGL.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.Test_bilan_submission_annee}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class Test_bilan_submission_annee extends ScriptNoArgs {
	private static final play.Logger.ALogger logger = play.Logger.of(Test_bilan_submission_annee.class);
	private final ExperimentAPI     experimentAPI;
	
	@Inject
	public Test_bilan_submission_annee(ExperimentAPI experimentAPI
				) {
		this.experimentAPI = experimentAPI;

		
	}

    public class Infos {
    	public int annee = 0;
		public int count_readset = 0;
		public int count_readsetRepriseHisto = 0;

		public Infos(int annee, int count_readset) {
			this.annee = annee;
			this.count_readset = count_readset;
			this.count_readsetRepriseHisto = 0;
		}
		public Infos(int annee) {
			this.annee = annee;
		}	
		
		public boolean equals(Object obj) {
			return  (obj instanceof Infos) &&
					(((Infos)obj).annee == this.annee);
		}


    }
    
    static class SortByYear implements Comparator<Infos> {
        @Override
        public int compare(Infos a, Infos b) {
			return new Integer(a.annee).compareTo(new Integer(b.annee));
        }
    }
    
    
    
	@Override
	public void execute() throws Exception {
		//List<Experiment> list_experiment = new ArrayList();
		//list_experiment.add(experimentAPI.get("exp_AHX_AHTIOSF_3_D2C56ACXX.IND7"));

		Iterable<Experiment> list_experiment=  experimentAPI.dao_all();
		Calendar cal = Calendar.getInstance(); 
		List<Infos>  bilan = new ArrayList<Infos>();
		for (Experiment exp: list_experiment) {
			if(exp.firstSubmissionDate != null) {
				Date date = new Date(exp.firstSubmissionDate.getTime());
				cal.setTime(date);
				int annee =  cal.getWeekYear();
				//println("annee de soumission = " +  annee);
				Infos infos = new Infos(annee);
				if (bilan.contains(infos)){
					bilan.get(bilan.indexOf(infos)).count_readset++;
					if(StringUtils.isNotBlank(exp.adminComment)) {
						bilan.get(bilan.indexOf(infos)).count_readsetRepriseHisto++;
					}
				} else {
					infos.count_readset++;
					if(StringUtils.isNotBlank(exp.adminComment)) {
						infos.count_readsetRepriseHisto++;
					}
					bilan.add(infos);
				}
			}
		}
//		Exemple sans passer par des lambda:
//		Comparator< ? extends Infos> x; // n'importe quel type qui etend Infos 
//		Comparator< ? super Infos> y;// n'importe quel type qui est une super class de Infos 
//		Comparator<Infos> x; // n'importe quel type qui etend Infos 
//
//		Collections.sort(bilan, new Comparator <Infos>() {
//			@Override
//			public int compare(Infos o1, Infos o2) {
//				return new Integer(o1.count_readset).compareTo(new Integer(o2.count_readset));
//			}});
		
		List<Infos> listInfos = (bilan);
		Collections.sort(listInfos, new SortByYear());
		int count_all_soumissions = 0;
		//listInfos.sort((a,b)->Integer.compare(a.annee, b.annee)); 
		for(Infos infos : listInfos) {
			printfln("Annee '" + infos.annee + "' nbre de readsets soumis " + infos.count_readset + " (dont " + infos.count_readsetRepriseHisto + " repriseHisto)");
			count_all_soumissions += infos.count_readset;
		}
		
		printfln("Nbre total de readsets soumis " + count_all_soumissions);
	}


		
		
}