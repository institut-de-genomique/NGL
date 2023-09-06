package fr.cea.ig.lfw.controllers.scripts.chunked.example;

import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;

public class StreamExample0 extends ScriptWithArgs<StreamExample0.Args> {

	public static class Args {} 
	
	@Override
	public void execute(Args args) throws Exception {
		final int waitDuration = 3000;
		println("starting");
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<1024; i++)
			sb.append("X");
		println(sb.toString());
		for (int i=0; i<5; i++) {
			println("waiting " + waitDuration + "(" + i +")");
			Thread.sleep(waitDuration);
		}
		println("done");
	}

}
