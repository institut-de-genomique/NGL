package scripts;

import java.security.InvalidParameterException;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import models.laboratory.balancesheet.instance.BalanceSheet;
import models.utils.InstanceConstants;
import services.instance.balancesheet.BalanceSheetUtils;
import services.instance.balancesheet.BalanceSheetUtils.Type;
import services.instance.balancesheet.ProcessBalanceSheet;

public class ScriptProcessBalanceSheet extends Script<ScriptProcessBalanceSheet.Args> {

	public static class Args {
		public String year;
		public String type;
		public String overwrite;
	}

	@Override
	public void execute(Args args) throws Exception {
		ProcessBalanceSheet process = new ProcessBalanceSheet();
		if(args.year == null || args.type == null) {
			throw new InvalidParameterException("Parameters 'year' and 'type' are expected.");
		}
		BalanceSheet bs = BalanceSheetUtils.findBalanceSheet(args.year, args.type);
		if(bs == null) {
			Type type = Type.from(args.type);
			getLogger().debug("Start create balance Sheet");
			bs = process.createBalanceSheet(args.year, type, BalanceSheetUtils.requests(type));
		} else {
			Type type = Type.from(bs.type);
			if(args.overwrite  != null && "true".equals(args.overwrite)) {	
				getLogger().debug("Start overwrite balance Sheet");
				process.overwriteBalanceSheet(bs, type, BalanceSheetUtils.requests(type));
			} else {
				getLogger().debug("Start update balance Sheet");
				process.updateBalanceSheet(bs, type, BalanceSheetUtils.requests(type));
			}
		}
		getLogger().debug("Save balance sheet in db");
		MongoDBDAO.save(InstanceConstants.BALANCE_SHEET_COLL_NAME, bs);
		getLogger().debug("End save balance sheet");
	}

}
