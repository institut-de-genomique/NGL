package services.instance.parameter;

import static services.instance.InstanceFactory.newBBP11;
import static services.instance.InstanceFactory.newBarcodePosition;

import java.util.ArrayList;
import java.util.List;

import org.mongojack.DBQuery;

import com.typesafe.config.ConfigFactory;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.parameter.printer.BBP11;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import validation.ContextValidation;

public class PrinterCNS {
	
	private static final play.Logger.ALogger logger = play.Logger.of(PrinterCNS.class);
	
	public static void main(ContextValidation ctx) {	
		logger.info("Start to create Printers collection for CNS");
		logger.info("Remove Printers");
		removerPrinters(ctx);
		logger.info("Save Printers ...");
		savePrinters(ctx);
		logger.info("CNS Printers collection creation is done!");
	}
	
	private static void removerPrinters(ContextValidation ctx) {
		MongoDBDAO.delete(InstanceConstants.PARAMETER_COLL_NAME, BBP11.class, DBQuery.is("typeCode", "BBP11"));
	}	
	

	private static BBP11 bbp11CB2D(String name, String location, String ipAddress) {
		return newBBP11(name ,location, ipAddress, 9100, "2", "15", "1", true,	
					    newBarcodePosition("1", "CB 2D TUBE", 298, "A100,30,0,2,1,1,N", "b20,30,A,d4", true, true));
	}
	
	private static BBP11 bbp11_6cm(String name, String location, String ipAddress) {
		return newBBP11(name, location,        ipAddress,  9100, "2", "15", "1", true, 
			newBarcodePosition("1", "6cm code barre en dessous", 712, "A25,0,0,2,2,1,N",  "B75,28,0,1B,2,2,39,N", true,  false),
			newBarcodePosition("2", "6cm code barre en dessus",  712, "A25,42,0,2,2,1,N", "B75,0,0,1B,2,2,39,N",  false, false));
	}
	
	public static void savePrinters(ContextValidation ctx){		
		List<BBP11> lp = new ArrayList<>();
		if (ConfigFactory.load().getString("ngl.env").equals("UAT") || ConfigFactory.load().getString("ngl.env").equals("DEV")) {
			lp.add(newBBP11("BBPTESTCB", "pour test", "bbp7.tx.local", 9100, "2", "15", "1", true, 
							newBarcodePosition("1", "CB 2D TUBE", 298, "A100,30,0,2,1,1,N", "b20,30,A,d4",          true, true),
							newBarcodePosition("2", "CB 1D TUBE", 298, "A25,10,0,3,1,1,N",  "B25,55,0,1B,2,2,30,B", true, false)));
		}
		lp.add(bbp11CB2D("BBP3", "flx / tube",               "bbp3.tx.local"));
		lp.add(bbp11_6cm("BBP4", "sequence / plaque",        "bbp4.tx.local"));
		lp.add(bbp11CB2D("BBP5", "finishing / tube",         "bbp5.tx.local"));
		lp.add(bbp11CB2D("BBP7", "prod / tube",              "bbp7.tx.local"));
		lp.add(bbp11CB2D("BBP8", "bureau Nanopore",          "bbp8.tx.local"));
		lp.add(bbp11_6cm("BBP9", "Labo Solexa / plaque",     "bbp9.tx.local"));
		lp.add(bbp11CB2D("BBP10", "bureau Karine / Corinne", "bbp10.tx.local"));
		lp.add(bbp11CB2D("BBP11", "Extraction / tube",       "bbp11.tx.local"));
		lp.add(bbp11_6cm("TLP4", "pièce A17 / plaque",       "tlp4.tx.local"));

		ctx.addKeyToRootKeyName("printers");
		for (BBP11 printer : lp) {
			InstanceHelpers.save(InstanceConstants.PARAMETER_COLL_NAME, printer,ctx);
			logger.debug("printer '{}' saved...", printer.name);
		}
	}
	

}
