package fr.cea.ig.authorization;

import javax.inject.Inject;

public class AuthorizedReadAction extends AuthorizedAction {

	@Inject
	public AuthorizedReadAction(IAuthorizator authorizator) {
		super(authorizator);
	}

	@Override
	public Permission getRequiredPermission() {
		return Permission.Read;
	}

}
