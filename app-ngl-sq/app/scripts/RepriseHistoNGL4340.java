package scripts;

import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.dao.InstrumentDAO;
import play.Logger;

public class RepriseHistoNGL4340 extends ScriptWithArgs<RepriseHistoNGL4340.Args> {

	public static class Args {

	}

	private InstrumentDAO instrumentDAO;

	@Inject
    public RepriseHistoNGL4340(InstrumentDAO instrumentDAO) {
		this.instrumentDAO = instrumentDAO;
	}

	@Override
	public void execute(Args args) throws Exception {
		Logger.error("Début RepriseHistoNGL4340");

		List<Instrument> instrumentsList = this.instrumentDAO.findAll();

		for (int i = 0; i < instrumentsList.size(); i++) {
			Instrument instrumentBase = this.instrumentDAO.findByCode(instrumentsList.get(i).code);

			if (instrumentBase.path != null && instrumentBase.path.contains("/ig")) {
				instrumentBase.path = instrumentBase.path.replace("/ig", "/");
				Logger.error("L'instrument '" + instrumentBase.code  + "' a IG dans son path, on l'enlève.");
				this.instrumentDAO.update(instrumentBase);
			}
		};

		Logger.error("Fin RepriseHistoNGL4340");
	}
}
