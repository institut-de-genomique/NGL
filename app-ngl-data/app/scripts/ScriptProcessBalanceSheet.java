package scripts;

import java.security.InvalidParameterException;

import javax.inject.Inject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
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

	private final NGLApplication app;

	@Inject
	public ScriptProcessBalanceSheet(NGLApplication app) {
		this.app = app;
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
			bs = process.createBalanceSheet(args.year, type, BalanceSheetUtils.requests(type));
		} else {
			Type type = Type.from(bs.type);
			if(args.overwrite  != null && "true".equals(args.overwrite)) {	
				process.overwriteBalanceSheet(bs, type, BalanceSheetUtils.requests(type));
			} else {
				process.updateBalanceSheet(bs, type, BalanceSheetUtils.requests(type));
			}
		}
		MongoDBDAO.save(InstanceConstants.BALANCE_SHEET_COLL_NAME, bs);
	}

}
