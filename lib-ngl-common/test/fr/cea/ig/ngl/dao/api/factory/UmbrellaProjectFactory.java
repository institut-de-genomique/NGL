package fr.cea.ig.ngl.dao.api.factory;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.project.instance.UmbrellaProject;

/**
 * Factory pour l'entité "UmbrellaProject".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class UmbrellaProjectFactory {
    
    /**
     * Méthode permettant de générer un objet "UmbrellaProject" aléatoire.
     * 
     * @param withId Booléen permettant de savoir si l'objet généré doit avoir un id ou pas.
     * 
     * @return Un objet "UmbrellaProject" aléatoire.
     */
    public static UmbrellaProject getRandomUmbrellaProject(boolean withId) {
        UmbrellaProject up = new UmbrellaProject();

        if (withId) {
            up._id = UUID.randomUUID().toString();
        }

        up.code = UUID.randomUUID().toString();
        up.categoryCode = UUID.randomUUID().toString();
        up.description = UUID.randomUUID().toString();
        up.name = UUID.randomUUID().toString();

        TraceInformation ti = new TraceInformation(TestUtils.CURRENT_USER);
        up.traceInformation = ti;

        return up;
    }

    /**
	 * Méthode permettant de générer une liste d'umbrella projets aléatoires.
	 * 
	 * @return Une liste d'umbrella projets générés.
	 */
    public static List<UmbrellaProject> getRandomUmbrellaProjectsList() {
		List<UmbrellaProject> umbrellaProjectList = new ArrayList<>();
		
		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			umbrellaProjectList.add(getRandomUmbrellaProject(true));
		}
		
		return umbrellaProjectList;
	}
}