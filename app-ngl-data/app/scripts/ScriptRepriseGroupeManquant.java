package scripts;

import javax.inject.Inject;
import javax.naming.NamingException;

import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import fr.cea.ig.ngl.dao.projects.MembersProjectsDAO;
import models.laboratory.project.instance.Members;

/**
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptRepriseGroupeManquant extends ScriptWithArgs<ScriptRepriseGroupeManquant.Args> {

	public static class Args {
		public String projectName;
	}

	private MembersProjectsDAO membersProjectsDAO;

	@Inject
	public ScriptRepriseGroupeManquant(MembersProjectsDAO membersProjectsDAO) {
		super();

		this.membersProjectsDAO = membersProjectsDAO;
	}

	@Override
	public void execute(Args args) throws NamingException {
		logger.error("Start ScriptRepriseGroupeManquant");

		Members members = new Members();
		members.codeProjet = args.projectName;
		members.codeProjet = members.codeProjet.toLowerCase();
		
		this.membersProjectsDAO.saveObject(members);

		logger.error("End ScriptRepriseGroupeManquant");
	}
}