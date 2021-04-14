package fr.cea.ig.ngl.test;

import static play.inject.Bindings.bind;

import java.util.function.Function;

import fr.cea.ig.authentication.IAuthenticator;
import fr.cea.ig.authorization.IAuthorizator;
import fr.cea.ig.ngl.test.authentication.AuthenticatorAdmin;
import fr.cea.ig.ngl.test.authentication.AuthenticatorNobody;
import fr.cea.ig.ngl.test.authentication.AuthenticatorRead;
import fr.cea.ig.ngl.test.authentication.AuthenticatorReadWrite;
import fr.cea.ig.ngl.test.authentication.AuthenticatorWrite;
import fr.cea.ig.ngl.test.authorization.TestAuthorizator;
import play.inject.guice.GuiceApplicationBuilder;

public class TestAuthConfig {

	public static final Function<GuiceApplicationBuilder,GuiceApplicationBuilder> asNobody = b -> 
		b.overrides(bind(IAuthenticator.class).to(AuthenticatorNobody.class))
		 .overrides(bind(IAuthorizator.class) .to(TestAuthorizator.class));
	
	public static final Function<GuiceApplicationBuilder,GuiceApplicationBuilder> asRead = b -> 
		b.overrides(bind(IAuthenticator.class).to(AuthenticatorRead.class))
		 .overrides(bind(IAuthorizator.class) .to(TestAuthorizator.class));
	
	public static final Function<GuiceApplicationBuilder,GuiceApplicationBuilder> asWrite = b -> 
		b.overrides(bind(IAuthenticator.class).to(AuthenticatorWrite.class))
		 .overrides(bind(IAuthorizator.class) .to(TestAuthorizator.class));
	
	public static final Function<GuiceApplicationBuilder,GuiceApplicationBuilder> asReadWrite = b -> 
		b.overrides(bind(IAuthenticator.class).to(AuthenticatorReadWrite.class))
		 .overrides(bind(IAuthorizator.class) .to(TestAuthorizator.class));

	public static final Function<GuiceApplicationBuilder,GuiceApplicationBuilder> asAdmin = b -> 
		b.overrides(bind(IAuthenticator.class).to(AuthenticatorAdmin.class))
		 .overrides(bind(IAuthorizator.class) .to(TestAuthorizator.class));

}
