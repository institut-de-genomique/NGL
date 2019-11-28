package fr.cea.ig.lfw.controllers.scripts.chunked.example;

import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;

public class StreamExample1 extends ScriptWithArgs<StreamExample1.Args> {

	public static class Args {
		public String[] args;
	}

	@Override
	public void execute(Args args) throws Exception {
		println("starting script");
		for (String arg : args.args) {
			println("computing next arg");
			Thread.sleep(3000);
			println("computed arg : " + arg);
		}
		println("script done");
	}
	
}
