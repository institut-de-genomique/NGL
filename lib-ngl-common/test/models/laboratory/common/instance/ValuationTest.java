package models.laboratory.common.instance;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class ValuationTest {
	
	private List<TransientValuation> getOrderedHistory(Valuation valuation) {
		return valuation.history.stream().sorted((tValA, tValB) -> tValA.index - tValB.index).collect(Collectors.toList());
	}
	
	@Test
	public final void createHistoryFrom() {
		Date dateA = new Date(0);
		Date dateB = new Date(1);
		Date dateC = new Date(2);
		Valuation valuationA = new Valuation("userA", TBoolean.UNSET, dateA);
		Valuation valuationB = valuationA.createHistoryFrom(new Valuation("userB", TBoolean.UNSET, dateB), "user historized");
		assertEquals(valuationA, valuationB);
		assertNull(valuationB.history);
		assertEquals(valuationB.user, "userA");
		assertEquals(valuationB.valid, TBoolean.UNSET);
		assertEquals(valuationB.date, dateA);
		assertNotEquals(valuationB.user, "userB");
		assertNotEquals(valuationB.date, dateB);
		
		Valuation valuationC = valuationA.createHistoryFrom(new Valuation("userC", TBoolean.TRUE, dateC), "user historized");
		assertEquals(valuationA, valuationC);
		assertNotNull(valuationC.history);
		assertEquals(valuationC.history.size(), 2);
		assertEquals(valuationC.user, "user historized");
		assertEquals(valuationC.valid, TBoolean.UNSET);
		assertNotEquals(valuationC.date, dateA);
		assertNotEquals(valuationC.date, dateB);
		assertNotEquals(valuationC.date, dateC);
		List<TransientValuation> tValuations = getOrderedHistory(valuationC);
		assertEquals(tValuations.get(0).user, "userC");
		assertEquals(tValuations.get(0).valid, TBoolean.TRUE);
		assertEquals(tValuations.get(0).date, dateC);
		assertEquals(tValuations.get(1).user, "user historized");
		assertEquals(tValuations.get(1).valid, TBoolean.UNSET);
		assertEquals(tValuations.get(1).date, valuationC.date);
	}

	@Test
	public final void testWithValues() {
		Date dateA = new Date(0);
		Date dateB = new Date(1);
		Date dateC = new Date(2);
		
		// si la validation ne change pas, on met juste à jour le user et la date
		Valuation valuationA = new Valuation("userA", TBoolean.UNSET, dateA);
		Valuation valuationB = valuationA.withValues("userB", TBoolean.UNSET, dateB);
		assertEquals(valuationA, valuationB);
		assertEquals(valuationA.user, "userB");
		assertEquals(valuationA.date, dateB);
		assertNull(valuationA.history);
		
		// si la validation change, on crée une nouvelle validation avec un historique
		Valuation valuationC = valuationB.withValues("userC", TBoolean.TRUE, dateC);
		assertNotEquals(valuationB, valuationC);
		assertEquals(valuationC.user, "userC");
		assertEquals(valuationC.date, dateC);
		assertNotNull(valuationC.history);
		assertEquals(valuationC.history.size(), 2);
		List<TransientValuation> tValuations = getOrderedHistory(valuationC);
		assertEquals(tValuations.get(0).user, "userB");
		assertEquals(tValuations.get(0).valid, TBoolean.UNSET);
		assertEquals(tValuations.get(0).date, dateB);
		assertEquals(tValuations.get(1).user, "userC");
		assertEquals(tValuations.get(1).valid, TBoolean.TRUE);
		assertEquals(tValuations.get(1).date, dateC);
	}

	@Test
	public final void testWithValuesFrom() {
		Date dateA = new Date(0);
		Date dateB = new Date(1);
		Date dateC = new Date(2);
		
		// si la validation ne change pas, on met juste à jour le user et la date
		Valuation valuationA = new Valuation("userA", TBoolean.UNSET, dateA);
		Valuation valuationB = valuationA.withValuesFrom(new Valuation("userB", TBoolean.UNSET, dateB));
		assertEquals(valuationA, valuationB);
		assertEquals(valuationA.user, "userB");
		assertEquals(valuationA.date, dateB);
		assertNull(valuationA.history);
		
		// si la validation change, on crée une nouvelle validation avec un historique
		Valuation valuationC = valuationB.withValuesFrom(new Valuation("userC", TBoolean.TRUE, dateC));
		assertNotEquals(valuationB, valuationC);
		assertEquals(valuationC.user, "userC");
		assertEquals(valuationC.date, dateC);
		assertNotNull(valuationC.history);
		assertEquals(valuationC.history.size(), 2);
		List<TransientValuation> tValuations = getOrderedHistory(valuationC);
		assertEquals(tValuations.get(0).user, "userB");
		assertEquals(tValuations.get(0).valid, TBoolean.UNSET);
		assertEquals(tValuations.get(0).date, dateB);
		assertEquals(tValuations.get(1).user, "userC");
		assertEquals(tValuations.get(1).valid, TBoolean.TRUE);
		assertEquals(tValuations.get(1).date, dateC);
	}

	@Test
	public final void testAsValidatedStringDate() {
		Date date1 = new Date(0);
		Date date2 = new Date(1);
		// si la validation ne change pas, on met juste à jour le user et la date
		Valuation valuationTrue = new Valuation("user", TBoolean.TRUE, date1);
		Valuation valuationTrueValidated = valuationTrue.asValidated("user validated", date2);
		assertEquals(valuationTrue, valuationTrueValidated);
		assertEquals(valuationTrueValidated.user, "user validated");
		assertEquals(valuationTrueValidated.valid, TBoolean.TRUE);
		assertEquals(valuationTrueValidated.date, date2);
		assertNull(valuationTrueValidated.history);
		
		// si la validation change, on crée une nouvelle validation avec un historique
		Valuation valuationUnset = new Valuation("user", TBoolean.UNSET, date1);
		Valuation valuationUnsetValidated = valuationUnset.asValidated("user validated", date2);
		assertNotEquals(valuationUnset, valuationUnsetValidated);
		assertEquals(valuationUnsetValidated.user, "user validated");
		assertEquals(valuationUnsetValidated.valid, TBoolean.TRUE);
		assertEquals(valuationUnsetValidated.date, date2);
		assertNotNull(valuationUnsetValidated);
		assertEquals(valuationUnsetValidated.history.size(), 2);
		List<TransientValuation> tUnsetValuations = getOrderedHistory(valuationUnsetValidated);
		assertEquals(tUnsetValuations.get(0).user, "user");
		assertEquals(tUnsetValuations.get(0).valid, TBoolean.UNSET);
		assertEquals(tUnsetValuations.get(0).date, date1);
		assertEquals(tUnsetValuations.get(1).user, "user validated");
		assertEquals(tUnsetValuations.get(1).valid, TBoolean.TRUE);
		assertEquals(tUnsetValuations.get(1).date, date2);
		
		Valuation valuationFalse = new Valuation("user", TBoolean.FALSE, date1);
		Valuation valuationFalseValidated = valuationFalse.asValidated("user validated", date2);
		assertNotEquals(valuationFalse, valuationFalseValidated);
		assertEquals(valuationFalseValidated.user, "user validated");
		assertEquals(valuationFalseValidated.valid, TBoolean.TRUE);
		assertEquals(valuationFalseValidated.date, date2);
		assertNotNull(valuationFalseValidated);
		assertEquals(valuationFalseValidated.history.size(), 2);
		List<TransientValuation> tFalseValuations = getOrderedHistory(valuationFalseValidated);
		assertEquals(tFalseValuations.get(0).user, "user");
		assertEquals(tFalseValuations.get(0).valid, TBoolean.FALSE);
		assertEquals(tFalseValuations.get(0).date, date1);
		assertEquals(tFalseValuations.get(1).user, "user validated");
		assertEquals(tFalseValuations.get(1).valid, TBoolean.TRUE);
		assertEquals(tFalseValuations.get(1).date, date2);
	}

	@Test
	public final void testAsValidatedString() {
		Date date = new Date(0);
		// si la validation ne change pas, on met juste à jour le user et la date
		Valuation valuationTrue = new Valuation("user", TBoolean.TRUE, date);
		Valuation valuationTrueValidated = valuationTrue.asValidated("user validated");
		assertEquals(valuationTrue, valuationTrueValidated);
		assertEquals(valuationTrueValidated.user, "user validated");
		assertEquals(valuationTrueValidated.valid, TBoolean.TRUE);
		assertNotEquals(valuationTrueValidated.date, date);
		assertNull(valuationTrueValidated.history);
		
		// si la validation change, on crée une nouvelle validation avec un historique
		Valuation valuationUnset = new Valuation("user", TBoolean.UNSET, date);
		Valuation valuationUnsetValidated = valuationUnset.asValidated("user validated");
		assertNotEquals(valuationUnset, valuationUnsetValidated);
		assertEquals(valuationUnsetValidated.user, "user validated");
		assertEquals(valuationUnsetValidated.valid, TBoolean.TRUE);
		assertNotEquals(valuationUnsetValidated.date, date);
		assertNotNull(valuationUnsetValidated.history);
		assertEquals(valuationUnsetValidated.history.size(), 2);
		List<TransientValuation> tUnsetValuations = getOrderedHistory(valuationUnsetValidated);
		assertEquals(tUnsetValuations.get(0).user, "user");
		assertEquals(tUnsetValuations.get(0).valid, TBoolean.UNSET);
		assertEquals(tUnsetValuations.get(0).date, date);
		assertEquals(tUnsetValuations.get(1).user, "user validated");
		assertEquals(tUnsetValuations.get(1).valid, TBoolean.TRUE);
		assertNotEquals(tUnsetValuations.get(1).date, date);
		
		Valuation valuationFalse = new Valuation("user", TBoolean.FALSE, date);
		Valuation valuationFalseValidated = valuationFalse.asValidated("user validated");
		assertNotEquals(valuationFalse, valuationFalseValidated);
		assertEquals(valuationFalseValidated.user, "user validated");
		assertEquals(valuationFalseValidated.valid, TBoolean.TRUE);
		assertNotEquals(valuationFalseValidated.date, date);
		assertNotNull(valuationFalseValidated.history);
		assertEquals(valuationFalseValidated.history.size(), 2);
		List<TransientValuation> tFalseValuations = getOrderedHistory(valuationFalseValidated);
		assertEquals(tFalseValuations.get(0).user, "user");
		assertEquals(tFalseValuations.get(0).valid, TBoolean.FALSE);
		assertEquals(tFalseValuations.get(0).date, date);
		assertEquals(tFalseValuations.get(1).user, "user validated");
		assertEquals(tFalseValuations.get(1).valid, TBoolean.TRUE);
		assertNotEquals(tUnsetValuations.get(1).date, date);
	}

	@Test
	public final void testAsInvalidatedStringDate() {
		Date date1 = new Date(0);
		Date date2 = new Date(1);
		// si la validation ne change pas, on met juste à jour le user et la date
		Valuation valuationFalse = new Valuation("user", TBoolean.FALSE, date1);
		Valuation valuationFalseInvalidated = valuationFalse.asInvalidated("user invalidated", date2);
		assertEquals(valuationFalse, valuationFalseInvalidated);
		assertEquals(valuationFalseInvalidated.user, "user invalidated");
		assertEquals(valuationFalseInvalidated.valid, TBoolean.FALSE);
		assertEquals(valuationFalseInvalidated.date, date2);
		assertNull(valuationFalseInvalidated.history);
		
		// si la validation change, on crée une nouvelle validation avec un historique
		Valuation valuationUnset = new Valuation("user", TBoolean.UNSET, date1);
		Valuation valuationUnsetInvalidated = valuationUnset.asInvalidated("user invalidated", date2);
		assertNotEquals(valuationUnset, valuationUnsetInvalidated);
		assertEquals(valuationUnsetInvalidated.user, "user invalidated");
		assertEquals(valuationUnsetInvalidated.valid, TBoolean.FALSE);
		assertEquals(valuationUnsetInvalidated.date, date2);
		assertNotNull(valuationUnsetInvalidated);
		assertEquals(valuationUnsetInvalidated.history.size(), 2);
		List<TransientValuation> tUnsetValuations = getOrderedHistory(valuationUnsetInvalidated);
		assertEquals(tUnsetValuations.get(0).user, "user");
		assertEquals(tUnsetValuations.get(0).valid, TBoolean.UNSET);
		assertEquals(tUnsetValuations.get(0).date, date1);
		assertEquals(tUnsetValuations.get(1).user, "user invalidated");
		assertEquals(tUnsetValuations.get(1).valid, TBoolean.FALSE);
		assertEquals(tUnsetValuations.get(1).date, date2);
		
		Valuation valuationTrue = new Valuation("user", TBoolean.TRUE, date1);
		Valuation valuationTrueInvalidated = valuationTrue.asInvalidated("user invalidated", date2);
		assertNotEquals(valuationFalse, valuationTrueInvalidated);
		assertEquals(valuationTrueInvalidated.user, "user invalidated");
		assertEquals(valuationTrueInvalidated.valid, TBoolean.FALSE);
		assertEquals(valuationTrueInvalidated.date, date2);
		assertNotNull(valuationTrueInvalidated);
		assertEquals(valuationTrueInvalidated.history.size(), 2);
		List<TransientValuation> tFalseValuations = getOrderedHistory(valuationTrueInvalidated);
		assertEquals(tFalseValuations.get(0).user, "user");
		assertEquals(tFalseValuations.get(0).valid, TBoolean.TRUE);
		assertEquals(tFalseValuations.get(0).date, date1);
		assertEquals(tFalseValuations.get(1).user, "user invalidated");
		assertEquals(tFalseValuations.get(1).valid, TBoolean.FALSE);
		assertEquals(tFalseValuations.get(1).date, date2);
	}

	@Test
	public final void testAsInvalidatedString() {
		Date date = new Date(0);
		// si la validation ne change pas, on met juste à jour le user et la date
		Valuation valuationFalse = new Valuation("user", TBoolean.FALSE, date);
		Valuation valuationFalseInvalidated = valuationFalse.asInvalidated("user invalidated");
		assertEquals(valuationFalse, valuationFalseInvalidated);
		assertEquals(valuationFalseInvalidated.user, "user invalidated");
		assertEquals(valuationFalseInvalidated.valid, TBoolean.FALSE);
		assertNotEquals(valuationFalseInvalidated.date, date);
		assertNull(valuationFalseInvalidated.history);
		
		// si la validation change, on crée une nouvelle validation avec un historique
		Valuation valuationUnset = new Valuation("user", TBoolean.UNSET, date);
		Valuation valuationUnsetInvalidated = valuationUnset.asInvalidated("user invalidated");
		assertNotEquals(valuationUnset, valuationUnsetInvalidated);
		assertEquals(valuationUnsetInvalidated.user, "user invalidated");
		assertEquals(valuationUnsetInvalidated.valid, TBoolean.FALSE);
		assertNotEquals(valuationUnsetInvalidated.date, date);
		assertNotNull(valuationUnsetInvalidated);
		assertEquals(valuationUnsetInvalidated.history.size(), 2);
		List<TransientValuation> tUnsetValuations = getOrderedHistory(valuationUnsetInvalidated);
		assertEquals(tUnsetValuations.get(0).user, "user");
		assertEquals(tUnsetValuations.get(0).valid, TBoolean.UNSET);
		assertEquals(tUnsetValuations.get(0).date, date);
		assertEquals(tUnsetValuations.get(1).user, "user invalidated");
		assertEquals(tUnsetValuations.get(1).valid, TBoolean.FALSE);
		assertNotEquals(tUnsetValuations.get(1).date, date);
		
		Valuation valuationTrue = new Valuation("user", TBoolean.TRUE, date);
		Valuation valuationTrueInvalidated = valuationTrue.asInvalidated("user invalidated");
		assertNotEquals(valuationFalse, valuationTrueInvalidated);
		assertEquals(valuationTrueInvalidated.user, "user invalidated");
		assertEquals(valuationTrueInvalidated.valid, TBoolean.FALSE);
		assertNotEquals(valuationTrueInvalidated.date, date);
		assertNotNull(valuationTrueInvalidated);
		assertEquals(valuationTrueInvalidated.history.size(), 2);
		List<TransientValuation> tFalseValuations = getOrderedHistory(valuationTrueInvalidated);
		assertEquals(tFalseValuations.get(0).user, "user");
		assertEquals(tFalseValuations.get(0).valid, TBoolean.TRUE);
		assertEquals(tFalseValuations.get(0).date, date);
		assertEquals(tFalseValuations.get(1).user, "user invalidated");
		assertEquals(tFalseValuations.get(1).valid, TBoolean.FALSE);
		assertNotEquals(tUnsetValuations.get(1).date, date);
	}

	@Test
	public final void testAddResolution() {
		final String resolutionCode = "test";
		Valuation valuation = new Valuation("user", TBoolean.UNSET);
		
		assertNull(valuation.resolutionCodes);
		
		// init resolution codes
		valuation.addResolution(resolutionCode);
		
		assertNotNull(valuation.resolutionCodes);
		assertFalse(valuation.resolutionCodes.isEmpty());
		assertEquals(valuation.resolutionCodes.size(), 1);
		assertTrue(valuation.resolutionCodes.contains(resolutionCode));
		
		final String resolutionCode2 = "test2";
		
		// add second resolution code
		valuation.addResolution(resolutionCode2);
		
		assertNotNull(valuation.resolutionCodes);
		assertFalse(valuation.resolutionCodes.isEmpty());
		assertEquals(valuation.resolutionCodes.size(), 2);
		assertTrue(valuation.resolutionCodes.contains(resolutionCode));
		assertTrue(valuation.resolutionCodes.contains(resolutionCode2));
	}

	@Test
	public final void testIs() {
		Valuation valuationFalse = new Valuation("user", TBoolean.FALSE);
		
		assertTrue(valuationFalse.is(TBoolean.FALSE));
		assertFalse(valuationFalse.is(TBoolean.TRUE));
		assertFalse(valuationFalse.is(TBoolean.UNSET));
		assertFalse(valuationFalse.is(null));
		
		Valuation valuationTrue = new Valuation("user", TBoolean.TRUE);
		
		assertTrue(valuationTrue.is(TBoolean.TRUE));
		assertFalse(valuationTrue.is(TBoolean.FALSE));
		assertFalse(valuationTrue.is(TBoolean.UNSET));
		assertFalse(valuationTrue.is(null));
		
		Valuation valuationUnset = new Valuation("user", TBoolean.UNSET);
		
		assertTrue(valuationUnset.is(TBoolean.UNSET));
		assertFalse(valuationUnset.is(TBoolean.TRUE));
		assertFalse(valuationUnset.is(TBoolean.FALSE));
		assertFalse(valuationUnset.is(null));
	}

	@Test
	public final void testIsnt() {
		Valuation valuationFalse = new Valuation("user", TBoolean.FALSE);
		
		assertFalse(valuationFalse.isnt(TBoolean.FALSE));
		assertTrue(valuationFalse.isnt(TBoolean.TRUE));
		assertTrue(valuationFalse.isnt(TBoolean.UNSET));
		assertTrue(valuationFalse.isnt(null));
		
		Valuation valuationTrue = new Valuation("user", TBoolean.TRUE);
		
		assertFalse(valuationTrue.isnt(TBoolean.TRUE));
		assertTrue(valuationTrue.isnt(TBoolean.FALSE));
		assertTrue(valuationTrue.isnt(TBoolean.UNSET));
		assertTrue(valuationTrue.isnt(null));
		
		Valuation valuationUnset = new Valuation("user", TBoolean.UNSET);
		
		assertFalse(valuationUnset.isnt(TBoolean.UNSET));
		assertTrue(valuationUnset.isnt(TBoolean.TRUE));
		assertTrue(valuationUnset.isnt(TBoolean.FALSE));
		assertTrue(valuationUnset.isnt(null));
	}

	@Test
	public final void testIsEquivalentOf() {
		Valuation valuationFalse = new Valuation("user", TBoolean.FALSE);
		Valuation valuationTrue = new Valuation("user", TBoolean.TRUE);
		Valuation valuationUnset = new Valuation("user", TBoolean.UNSET);
		
		Valuation valuationFalse2 = new Valuation("user2", TBoolean.FALSE);
		Valuation valuationTrue2 = new Valuation("user2", TBoolean.TRUE);
		Valuation valuationUnset2 = new Valuation("user2", TBoolean.UNSET);
		
		assertTrue(valuationFalse.isEquivalentOf(valuationFalse2));
		assertFalse(valuationFalse.isEquivalentOf(valuationTrue2));
		assertFalse(valuationFalse.isEquivalentOf(valuationUnset2));
		
		assertTrue(valuationTrue.isEquivalentOf(valuationTrue2));
		assertFalse(valuationTrue.isEquivalentOf(valuationFalse2));
		assertFalse(valuationTrue.isEquivalentOf(valuationUnset2));
		
		assertTrue(valuationUnset.isEquivalentOf(valuationUnset2));
		assertFalse(valuationUnset.isEquivalentOf(valuationTrue2));
		assertFalse(valuationUnset.isEquivalentOf(valuationFalse2));
	}

	@Test
	public final void testIsntEquivalentOf() {
		Valuation valuationFalse = new Valuation("user", TBoolean.FALSE);
		Valuation valuationTrue = new Valuation("user", TBoolean.TRUE);
		Valuation valuationUnset = new Valuation("user", TBoolean.UNSET);
		
		Valuation valuationFalse2 = new Valuation("user2", TBoolean.FALSE);
		Valuation valuationTrue2 = new Valuation("user2", TBoolean.TRUE);
		Valuation valuationUnset2 = new Valuation("user2", TBoolean.UNSET);
		
		assertFalse(valuationFalse.isntEquivalentOf(valuationFalse2));
		assertTrue(valuationFalse.isntEquivalentOf(valuationTrue2));
		assertTrue(valuationFalse.isntEquivalentOf(valuationUnset2));
		
		assertFalse(valuationTrue.isntEquivalentOf(valuationTrue2));
		assertTrue(valuationTrue.isntEquivalentOf(valuationFalse2));
		assertTrue(valuationTrue.isntEquivalentOf(valuationUnset2));
		
		assertFalse(valuationUnset.isntEquivalentOf(valuationUnset2));
		assertTrue(valuationUnset.isntEquivalentOf(valuationTrue2));
		assertTrue(valuationUnset.isntEquivalentOf(valuationFalse2));
	}

}
