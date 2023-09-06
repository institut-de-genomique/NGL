package fr.cea.ig.ngl.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.ngl.NGLApplication;
import play.cache.SyncCacheApi;
import services.taxonomy.EBITaxonomyServices;
import services.taxonomy.NCBITaxonomyServices;
import services.taxonomy.Taxon;
import services.taxonomy.TaxonomyServices;


// script qui peut etre lance dans sbt :
// project ngl-common
// testOnly fr.cea.ig.ngl.services.TaxonomyServicesTest

@RunWith(PowerMockRunner.class)
public class TaxonomyServicesTest {
	// On peut creer un leurre d'une methode publique avec mock lorsque la methode ne nous interresse pas pour les tests 
    private NGLApplication nglApp = mock(NGLApplication.class); // creation d'un leurre nglApp

    private SyncCacheApi syncCacheApi = mock(SyncCacheApi.class); // retour de nglApp.cache() qu'on ne souhaite pas explorer

    private NCBITaxonomyServices ncbiTaxonomyServices = mock(NCBITaxonomyServices.class); 
    private EBITaxonomyServices ebiTaxonomyServices = mock(EBITaxonomyServices.class); 

    private TaxonomyServices taxonomyServices = new TaxonomyServices(nglApp);

    // on verifie qu'en entrant dans la methode taxonomyServices.getTaxon() avec un parametre null, on a un retour à null
    @Test
    public void getTaxonWithNullParam() {
        Taxon taxon = taxonomyServices.getTaxon(null);
        assertTrue("Taxon devrait être null et il ne l'est pas.", taxon == null);
    }

    
    // verifie que si le taxonCode n'existe existe au ncbi  la methode taxonomyServices.getTaxon(taxonCode)  
    // retourne un taxon avec error à false   
    @Test
    public void getNCBITaxon_ok() {
        taxonomyServices.setNCBITaxonomyServices(ncbiTaxonomyServices);
        
        when(nglApp.cache()).thenReturn(syncCacheApi);           // on ne cherche pas à tester le cache d'ou l'utilisation d'un leurre qui retourne null
        when(syncCacheApi.get(anyString())).thenReturn(null);    // on teste bien l'algo dans le cas ou le taxon n'est pas recuperé dans le cache quelque soit 
                                                                 // le taxonId (de type String) donnée en argument
        
        // Rq :  la methode setObjectInCache(taxon, taxonCode) n'a pas besoin d'etre mocké car elle ne retourne rien.

        String taxonCode = "344338";                             // Exemple de taxonCode qu'on retrouve bien au NCBI mais n'importe lequel ferait l'affaire  
                                                                 // puisqu'on passe par un leurre de NCBItaxonomyService

        Taxon ncbiTaxon = new Taxon(taxonCode);
        ncbiTaxon.error = false;

        // on definit un alias : quand on rencontrera dans le code l'appel ncbiTaxonomyServices.getTaxon(taxonCode), celui-ci sera 
        // remplacé par return(taxonNCBI)
        when(ncbiTaxonomyServices.getTaxon(taxonCode)).thenReturn(ncbiTaxon);

        
        // code qui va etre executé en remplacant dans ce code les methodes par leurs alias definies ci-dessus
        Taxon taxon = taxonomyServices.getTaxon(taxonCode);
        // resultats attendus :
        assertTrue(!taxon.error);
        assertTrue(taxon.code.equals(taxonCode));
    }
    
    // verifie que si le taxonCode n'existe pas au ncbi et existe à l'ebi, la methode taxonomyServices.getTaxon(taxonCode)  
    // retourne un taxon avec error à false
    @Test
    public void getEBITaxon_ok() {
        taxonomyServices.setNCBITaxonomyServices(ncbiTaxonomyServices);
        taxonomyServices.setEBITaxonomyServices(ebiTaxonomyServices);

        when(nglApp.cache()).thenReturn(syncCacheApi);           // on ne cherche pas à tester le cache d'ou l'utilisation d'un leurre qui retourne null
        when(syncCacheApi.get(anyString())).thenReturn(null);    // on teste bien l'algo dans le cas ou le taxon n'est pas recuperé dans le cache quelque soit 
                                                                 // le taxonId (de type String) donnée en argument
        
        // Rq :  la methode setObjectInCache(taxon, taxonCode) n'a pas besoin d'etre mocké car elle ne retourne rien.

        String taxonCode = "2890055";                            // Exemple de taxonCode qui n'existait pas au NCBI mais present à l'EBI

        Taxon ncbiTaxon = new Taxon(taxonCode);
        ncbiTaxon.error = true;

        Taxon ebiTaxon = new Taxon(taxonCode);
        ebiTaxon.error = false;     
        
        // on definit un alias : quand on rencontrera dans le code l'appel ncbiTaxonomyServices.getTaxon(taxonCode), celui-ci sera 
        // remplacé par return(ncbiTaxon)
        when(ncbiTaxonomyServices.getTaxon(taxonCode)).thenReturn(ncbiTaxon);
        when(ebiTaxonomyServices.getTaxon(taxonCode)).thenReturn(ebiTaxon);

        // code qui va etre executé en remplacant dans ce code les methodes par leurs alias definies ci-dessus
        Taxon taxon = taxonomyServices.getTaxon(taxonCode);
        // resultats attendus :
        assertTrue(!taxon.error);
        assertTrue(taxon.code.equals(taxonCode));
    }
 
    
    // verifie que si le taxonCode n'existe ni au ncbi ni a l'ebi, la methode taxonomyServices.getTaxon(taxonCode)  
    // retourne un taxon avec error à true
    public void getTaxon_NotOk() {
        taxonomyServices.setNCBITaxonomyServices(ncbiTaxonomyServices);
        taxonomyServices.setEBITaxonomyServices(ebiTaxonomyServices);

        when(nglApp.cache()).thenReturn(syncCacheApi);           // on ne cherche pas à tester le cache d'ou l'utilisation d'un leurre qui retourne null
        when(syncCacheApi.get(anyString())).thenReturn(null);    // on teste bien l'algo dans le cas ou le taxon n'est pas recuperé dans le cache quelque soit 
                                                                 // le taxonId (de type String) donnée en argument
        
        // Rq :  la methode setObjectInCache(taxon, taxonCode) n'a pas besoin d'etre mocké car elle ne retourne rien.

        String taxonCode = "000000000";                            // Exemple de taxonCode qui n'existe ni au NCBI ni à l'EBI

        Taxon ncbiTaxon = new Taxon(taxonCode);
        ncbiTaxon.error = true;

        Taxon ebiTaxon = new Taxon(taxonCode);
        ebiTaxon.error = false;     
        
        // on definit un alias : quand on rencontrera dans le code l'appel ncbiTaxonomyServices.getTaxon(taxonCode), celui-ci sera 
        // remplacé par return(ncbiTaxon)
        when(ncbiTaxonomyServices.getTaxon(taxonCode)).thenReturn(ncbiTaxon);
        when(ebiTaxonomyServices.getTaxon(taxonCode)).thenReturn(ebiTaxon);

        // code qui va etre executé en remplacant dans ce code les methodes par leurs alias definies ci-dessus
        Taxon taxon = taxonomyServices.getTaxon(taxonCode);
        // resultats attendus :
        assertTrue(taxon.error);
        assertTrue(taxon.code.equals(taxonCode));
    }
}
