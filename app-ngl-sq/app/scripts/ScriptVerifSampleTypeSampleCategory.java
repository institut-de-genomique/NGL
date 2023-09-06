package scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;

/**
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptVerifSampleTypeSampleCategory extends ScriptWithArgs<Object> {

	private List<SampleCategory> getSampleCategories() {
		List<SampleCategory> l = new ArrayList<>();
		
		l.add(new SampleCategory("cloned-DNA",            "ADN Cloné"));
		l.add(new SampleCategory("IP-sample",             "Matériel Immunoprécipité"));
		l.add(new SampleCategory("amplicon",              "Amplicon"));
		l.add(new SampleCategory("default",               "Défaut"));
		l.add(new SampleCategory("unknown",               "Inconnu"));
		l.add(new SampleCategory("DNA",                   "ADN"));
		l.add(new SampleCategory("RNA",                   "ARN"));
		l.add(new SampleCategory("cDNA",                  "cDNA"));
		l.add(new SampleCategory("DNAplug",               "ADN en plug"));
		l.add(new SampleCategory("FAIRE",                 "FAIRE")); 
		l.add(new SampleCategory("methylated-base-DNA",   "Methylated Base DNA (MBD)")); 
		l.add(new SampleCategory("bisulfite-DNA",         "Bisulfite DNA")); 
		l.add(new SampleCategory("control",               "Control"));
		l.add(new SampleCategory("environmental-samples", "Prélèvements environnementaux"));

		return l;
	}

	private void repriseAllSampleTypeCategoryType() {
		List<SampleCategory> sampleCategories = getSampleCategories();
		List<SampleType> sampleTypeList = SampleType.find.get().findAll();

		sampleCategories.forEach(sc -> {
			List<SampleType> stList = sampleTypeList.stream().filter(st2 -> st2.category.code.equals(sc.code)).collect(Collectors.toList());
			
			Query[] queryList = new Query[stList.size() + 1];
			queryList[0] = DBQuery.is("categoryCode", sc.code);
			
			int i = 1;

			for (int j = 0; j < stList.size(); j++) {
				SampleType st = stList.get(j);

				queryList[i++] = DBQuery.notEquals("typeCode", st.code);
			}

			List<Sample> sampleList = MongoDBDAO.find(
				InstanceConstants.SAMPLE_COLL_NAME,
				Sample.class,
				DBQuery.and(queryList)
			).toList();

			for (int k = 0; k < sampleList.size(); k++) {
				Sample s = sampleList.get(k);
				Optional<String> optCat = sampleTypeList.stream().filter(st -> st.code.equals(s.typeCode)).map(st -> st.category.code).findFirst();
	
				if (optCat.isPresent()) {
					s.categoryCode = optCat.get();
	
					Logger.error("Mise à jour de '" + s.code + "' avec comme nouvelle catégorie '" + s.categoryCode + "'");
	
					MongoDBDAO.save(InstanceConstants.SAMPLE_COLL_NAME, s);
				} else {
					Logger.error("Erreur - Catégorie non trouvée");
				}
			}
		});	
	}

	@Override
	public void execute(Object obj) throws Exception {
		Logger.error("Début Vérif Sample");

		repriseAllSampleTypeCategoryType();

		Logger.error("Fin Vérif Sample");
	}
}