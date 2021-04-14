package fr.cea.ig.ngl.test;

import java.util.function.Function;

import play.inject.guice.GuiceApplicationBuilder;
import rules.services.Rules6Component;
import rules.services.test.TestRules6Component;

public class TestAppWithDroolsFactory extends TestAppAuthFactory {

	public TestAppWithDroolsFactory(String configFileName) {
		super(configFileName);
	}

	public TestAppWithDroolsFactory(TestAppAuthFactory f) {
		super(f);
	}

	public TestAppWithDroolsFactory bindRulesComponent() {
		return overrideEagerly(Rules6Component.class, TestRules6Component.class);
	}

	@Override
	public <T> TestAppWithDroolsFactory overrideEagerly(Class<T> t) {
		return (TestAppWithDroolsFactory) super.overrideEagerly(t);
	}
	
	@Override
	public <T,U extends T> TestAppWithDroolsFactory overrideEagerly(Class<T> t, Class<U> u) {
		return (TestAppWithDroolsFactory)super.overrideEagerly(t,u);
	}
	
	@Override
	public TestAppWithDroolsFactory mod(Function<GuiceApplicationBuilder,GuiceApplicationBuilder> mod) {
		return (TestAppWithDroolsFactory)super.mod(mod);
	}
	
	@Override
	public TestAppWithDroolsFactory configure(String key, String value) {
		return (TestAppWithDroolsFactory)super.configure(key,value);
	}
	
	@Override
	protected TestAppWithDroolsFactory constructorClone() {
		return new TestAppWithDroolsFactory(this);
	}

	@Override
	public <T, U extends T> TestAppWithDroolsFactory override(Class<T> t, Class<U> u) {
		return (TestAppWithDroolsFactory) super.override(t, u);
	}
	
}
