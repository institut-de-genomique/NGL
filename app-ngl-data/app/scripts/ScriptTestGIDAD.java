package scripts;

import javax.inject.Inject;
import javax.naming.NamingException;

import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import fr.cea.ig.ngl.dao.projects.MembersProjectsDAO;

/**
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptTestGIDAD extends ScriptWithArgs<ScriptTestGIDAD.Args> {

	public static class Args {
		public Integer minGID;

		public Integer maxGID;
	}

	private MembersProjectsDAO membersProjectsDAO;

	@Inject
	public ScriptTestGIDAD(MembersProjectsDAO membersProjectsDAO) {
		super();

		this.membersProjectsDAO = membersProjectsDAO;
	}

	@Override
	public void execute(Args args) throws NamingException {
		logger.debug("Start test GID AD");
		
		if (args.minGID == null || args.maxGID == null) {
			println("Il manque au moins un paramètre (minGID et / ou maxGID).");
		} else if (args.minGID > args.maxGID) {
			println("minGID doit etre inferieur a maxGID.");
		} else if (args.minGID == args.maxGID) {
			println("minGID ne doit pas etre egal a maxGID.");
		} else {
			int gidChosen = this.membersProjectsDAO.getFirstGidAvailable(args.minGID, args.maxGID, args.minGID, args.maxGID);

			println("Le premier GID dispo est : " + gidChosen);
		}

		logger.debug("End test GID AD");
	}
}