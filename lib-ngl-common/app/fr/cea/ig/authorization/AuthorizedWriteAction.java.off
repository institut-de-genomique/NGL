package fr.cea.ig.authorization;

import javax.inject.Inject;

public class AuthorizedWriteAction extends AuthorizedAction {

	@Inject
	public AuthorizedWriteAction(IAuthorizator authorizator) {
		super(authorizator);
	}

	@Override
	public Permission getRequiredPermission() {
		return Permission.Write;
	}

}
