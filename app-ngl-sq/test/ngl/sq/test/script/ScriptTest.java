package ngl.sq.test.script;

import static org.junit.Assert.*;

import java.util.function.Consumer;

import org.junit.Test;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import ngl.sq.Global;
import play.libs.ws.WSResponse;

class Script0 extends ScriptNoArgs {

	@Override
	public void execute() throws Exception {
		printfln("hello");
	}
	
}

class Script1 extends Script<Script1.Args> {
	
	public static class Args {
		public int[] ints;
	}

	@Override
	public void execute(Args args) throws Exception {
		printfln("hello from script 1");
	}
	
}

public class ScriptTest {

	static Consumer<String> assertStartWith(String start) {
		return s -> assertEquals(start, s.substring(0, start.length())); 
	}
	
	static void assertResponse(WSResponse r, Consumer<String> p) {
		System.out.println(r.getBody());
		String[] lines = r.getBody().split("\n");
		p.accept(lines[0]);
	}
	
	@Test
	public void testScript_0() throws Exception {
		Global.af.runWs((app,ws) -> {
			assertResponse(ws.get("/scripts/run/ngl.sq.test.script.Script0"), 
					       l -> assertEquals("hello", l));
		});
	}
	
	@Test
	public void testScript_1() throws Exception {
		Global.af.runWs((app,ws) -> {
			assertResponse(ws.get("/scripts/run/sponge.bob.ngl.sq.test.script.Script0"),
					       assertStartWith("not found"));
		});
	}
	
	@Test
	public void testScript_2() throws Exception {
		Global.af.runWs((app,ws) -> {
			assertResponse(ws.get("/scripts/run/ngl.sq.test.script.Script1?ints=1&ints=42"), 
					       l -> assertEquals("hello from script 1", l));
		});
	}
		
}
