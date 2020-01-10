package ncbi.services;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;

// import play.libs.F.Callback;
import services.ncbi.NCBITaxon;
import services.ncbi.TaxonomyServices;
import utils.AbstractTests;

public class TaxonomyServicesTest extends AbstractTests {

	/**
	 * Test with taxonId =1358
	 * scientific name : Lactococcus lactis
	 * Lineage : cellular organisms; Bacteria; Terrabacteria group; Firmicutes; Bacilli; Lactobacillales; Streptococcaceae; Lactococcus
	 * @throws XPathExpressionException
	 */
	public void shouldGetTaxonomyInfo() throws XPathExpressionException	{
		String scientificName = TaxonomyServices.getScientificName("1358");
		Assert.assertNotNull(scientificName);
		Assert.assertEquals("Lactococcus lactis", scientificName);
		String lineage = TaxonomyServices.getLineage("1358");
		Assert.assertNotNull(lineage);
		Assert.assertEquals("cellular organisms; Bacteria; Terrabacteria group; Firmicutes; Bacilli; Lactobacillales; Streptococcaceae; Lactococcus", lineage);
	}

	public void shouldNoTaxonomyInfo() {
		String scientificName = TaxonomyServices.getScientificName("135");
		Assert.assertNull(scientificName);
		String lineage = TaxonomyServices.getLineage("135");
		Assert.assertNull(lineage);
	}

	public void shouldNoTaxonomyInfoTaxonCodeNull()	{
		Assert.assertNull(TaxonomyServices.getScientificName(null));
	}
	
	private void taxonTest(String key, Consumer<NCBITaxon> assertion) throws InterruptedException,ExecutionException {
		NCBITaxon taxon = TaxonomyServices.getNCBITaxon(key).toCompletableFuture().get();
		assertion.accept(taxon);
	}
	
	@Test
	public void shouldGetNCBITaxonTest() throws InterruptedException,ExecutionException {
		// TaxonomyServices.getNCBITaxon("1358").onRedeem(// new Callback<NCBITaxon>() {
		/*TaxonomyServices.getNCBITaxon("1358").thenAcceptAsync(
				new Consumer<NCBITaxon>() {
			@Override
			// public void invoke(NCBITaxon taxon) throws Throwable {
			public void accept(NCBITaxon taxon) {
				String scientificName = taxon.getScientificName();
				Assert.assertNotNull(scientificName);
				Assert.assertEquals("Lactococcus lactis", scientificName);
				String lineage = taxon.getLineage();
				Assert.assertNotNull(lineage);
				Assert.assertEquals("cellular organisms; Bacteria; Terrabacteria group; Firmicutes; Bacilli; Lactobacillales; Streptococcaceae; Lactococcus", lineage);

			}
		});
		Thread.sleep(10000);*/
		taxonTest("1358",
				taxon -> {
					String scientificName = taxon.getScientificName();
					Assert.assertNotNull(scientificName);
					Assert.assertEquals("Lactococcus lactis", scientificName);
					String lineage = taxon.getLineage();
					Assert.assertNotNull(lineage);
					Assert.assertEquals("cellular organisms; Bacteria; Terrabacteria group; Firmicutes; Bacilli; Lactobacillales; Streptococcaceae; Lactococcus", lineage);					
				});	
	}

	@Test
	public void shouldNotNCBITaxonTest() throws InterruptedException,ExecutionException {
		// TaxonomyServices.getNCBITaxon("135").onRedeem(
		/*TaxonomyServices.getNCBITaxon("135").thenAcceptAsync(
				//new Callback<NCBITaxon>() {
				new Consumer<NCBITaxon>() {
			@Override
			//public void invoke(NCBITaxon taxon) throws Throwable {
			public void accept(NCBITaxon taxon) {
				String scientificName = taxon.getScientificName();
				Assert.assertEquals(scientificName, "Taxon code 135 is not exists");
				String lineage = taxon.getLineage();
				Assert.assertEquals(lineage, "Taxon code 135 is not exists");
			}
		});
		Thread.sleep(10000);*/
		taxonTest("135",
				taxon -> {
					String scientificName = taxon.getScientificName();
					Assert.assertEquals("Taxon code 135 is not exists", scientificName);
					String lineage = taxon.getLineage();
					Assert.assertEquals("Taxon code 135 is not exists", lineage);
				});
	}

	// @Test
	// Behavior has changed since this test was written
	public void shouldNotNCBITaxonForNullCodeTest() throws InterruptedException,ExecutionException	{
		//TaxonomyServices.getNCBITaxon(null).onRedeem(//new Callback<NCBITaxon>() {
		/*TaxonomyServices.getNCBITaxon(null).thenAcceptAsync(
				new Consumer<NCBITaxon>() {
			@Override
			// public void invoke(NCBITaxon taxon) throws Throwable {
			public void accept(NCBITaxon taxon) {
				String scientificName = taxon.getScientificName();
				Assert.assertEquals(scientificName, "Taxon code null is not exists");
				String lineage = taxon.getLineage();
				Assert.assertEquals(lineage, "Taxon code null is not exists");
			}
		});
		Thread.sleep(10000);*/
		taxonTest(null,
				taxon -> {
					String scientificName = taxon.getScientificName();
					Assert.assertEquals("Taxon code null is not exists", scientificName);
					String lineage = taxon.getLineage();
					Assert.assertEquals("Taxon code null is not exists", lineage);
				});
	}

	@Test
	public void shouldErrorNCBITaxonTest() throws InterruptedException,ExecutionException {
		// TaxonomyServices.getNCBITaxon("0").onRedeem(//new Callback<NCBITaxon>() {
		/*TaxonomyServices.getNCBITaxon("0").thenAcceptAsync(
				new Consumer<NCBITaxon>() {
			@Override
			// public void invoke(NCBITaxon taxon) throws Throwable {
			public void accept(NCBITaxon taxon) {
				String scientificName = taxon.getScientificName();
				Assert.assertEquals(scientificName, "Taxon code 0 is on error");
				String lineage = taxon.getLineage();
				Assert.assertEquals(lineage, "Taxon code 0 is on error");
			}
		});
		Thread.sleep(10000);*/
		taxonTest("0",
				taxon -> {
					String scientificName = taxon.getScientificName();
					Assert.assertEquals("Taxon code 0 is on error",scientificName);
					String lineage = taxon.getLineage();
					Assert.assertEquals("Taxon code 0 is on error",lineage);
				});
	}
	
}
