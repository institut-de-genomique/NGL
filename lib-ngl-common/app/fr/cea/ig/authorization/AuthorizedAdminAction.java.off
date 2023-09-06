package fr.cea.ig.authorization;

import javax.inject.Inject;

public class AuthorizedAdminAction extends AuthorizedAction {

	@Inject
	public AuthorizedAdminAction(IAuthorizator authorizator) {
		super(authorizator);
	}

	@Override
	public Permission getRequiredPermission() {
		return Permission.Admin; 
	}

}
