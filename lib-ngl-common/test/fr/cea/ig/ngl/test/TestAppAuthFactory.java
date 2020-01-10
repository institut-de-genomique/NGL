package fr.cea.ig.ngl.test;

import java.util.function.Function;

import fr.cea.ig.authentication.IAuthenticator;
import fr.cea.ig.ngl.test.authentication.AuthenticatorAdmin;
import fr.cea.ig.ngl.test.authentication.AuthenticatorNobody;
import fr.cea.ig.ngl.test.authentication.AuthenticatorRead;
import fr.cea.ig.ngl.test.authentication.AuthenticatorReadWrite;
import fr.cea.ig.ngl.test.authentication.AuthenticatorWrite;
import fr.cea.ig.ngl.test.authentication.Identity;
import fr.cea.ig.play.test.ApplicationFactory;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http.Status;

/* Takes an application factory and provides ways to configure the
 * authentication.
 * Authentication could be set at runtime using some global or 
 * possibly some application related method.
 */ 
public class TestAppAuthFactory extends ApplicationFactory {

	public TestAppAuthFactory(String cf) {
		super(cf);
	}
	
	protected TestAppAuthFactory(TestAppAuthFactory f) {
		super(f);
	}
	
	@Override
	protected TestAppAuthFactory constructorClone() {
		return new TestAppAuthFactory(this);
	}
	
	// covariant overrides
	@Override
	public TestAppAuthFactory mod(Function<GuiceApplicationBuilder,GuiceApplicationBuilder> mod) {
		return (TestAppAuthFactory)super.mod(mod);
	}
	
	@Override
	public <T,U extends T> TestAppAuthFactory override(Class<T> t, Class<U> u) {
		return (TestAppAuthFactory)super.override(t,u);
	}

	@Override
	public <T> TestAppAuthFactory overrideEagerly(Class<T> t) {
		return (TestAppAuthFactory)super.overrideEagerly(t);
	}
		
	@Override
	public <T, U extends T> TestAppAuthFactory overrideEagerly(Class<T> t, Class<U> u) {
		return (TestAppAuthFactory)super.overrideEagerly(t,u);
	}
			
	public TestAppAuthFactory as(Identity i) {
		switch (i) {
		case Nobody   : return override(IAuthenticator.class, AuthenticatorNobody.class);
		case Read     : return override(IAuthenticator.class, AuthenticatorRead.class);
		case Write    : return override(IAuthenticator.class, AuthenticatorWrite.class);
		case ReadWrite: return override(IAuthenticator.class, AuthenticatorReadWrite.class);
		case Admin    : return override(IAuthenticator.class, AuthenticatorAdmin.class);
		default       : throw new RuntimeException("unhandled identity " + i); 
		}
	}
	
	public TestAppAuthFactory asWorse(Identity i) {
		return as(worsen(i));
	}

	// Test that the url is acessible for at least the given
	// permission.
	
	// Could define maps for constants.
	
	private static Identity worsen(Identity i) {
		switch (i) {
		case Nobody:    return Identity.Nobody;
		case Read:      return Identity.Nobody;
		case ReadWrite: return Identity.Read;
		case Write:     return Identity.Read;
		case Admin:     return Identity.ReadWrite;
		default:		throw new RuntimeException("no worsening defined for " + i);
		}
	}
	
	public void authURL(Identity i, String url) {
		as(i)     .ws(ws -> ws.get(url,Status.OK));
		asWorse(i).ws(ws -> ws.get(url,Status.FORBIDDEN));
	}
	
	public void authNobody(String url) {
		as(Identity.Nobody).ws(ws -> ws.get(url,Status.OK));
	}
	
	@Override
	public TestAppAuthFactory configure(String key, String value) {
		return (TestAppAuthFactory)super.configure(key,value);
	}	
	
}
