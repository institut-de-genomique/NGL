package models.laboratory.common.instance;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import workflows.Workflows;

public class StateTest {

	public static final String USER = "whatever";
	public static final String
		SN0 = "A",
		SN1 = "B",
		SN2 = "C";
	
	private static final void assertStateHistory(State s, String... names) {
		assertEquals("expected history size", names.length, s.historical.size());
		for (int i=0; i<names.length; i++) {
			String hcode = null;
			for (TransientState t : s.historical)
				if (t.index.intValue() == i)
					hcode = t.code;
			assertEquals("history[" + i + "]", names[i], hcode);
		}
	}
	
	@Test
	public void historyI_0() {
		State previous = new State(SN0, USER);
		State next     = new State(SN1, USER).createHistory(previous);
		assertStateHistory(next, SN0, SN1);
	}
	
	@Test
	public void historyI_1() {
		State previous = new State(SN0, USER);
		previous       = new State(SN1, USER).createHistory(previous);
		State next     = new State(SN2, USER).createHistory(previous);
		assertStateHistory(next, SN0, SN1, SN2);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void historyS_0() {
		State previous = new State(SN0, USER);
		State next     = Workflows.updateHistoricalNextState(previous, new State(SN1, USER));
		assertStateHistory(next, SN0, SN1);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void historyS_1() {
		State previous = new State(SN0, USER);
		previous       = Workflows.updateHistoricalNextState(previous, new State(SN1, USER));
		State next     = Workflows.updateHistoricalNextState(previous, new State(SN2, USER));
		assertStateHistory(next, SN0, SN1, SN2);
	}
	
}
