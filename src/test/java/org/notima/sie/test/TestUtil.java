package org.notima.sie.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.notima.sie.SIEUtil;

public class TestUtil {

	String[] testStrings = new String[] {
		"\"1\" \"ANSV\"",
		"2 \"TOMY\" \"6\" \"106\"",
		"1 \"Testar 3.5\\\" disketter.\"",
		"1 ANSV 6 106",
		"1 ANSV \"6\" 108"
	};

	@Test
	public void testSplitQuotedPair() {
		List<String> result = SIEUtil.split(testStrings[0]);
		assertEquals(2, result.size());
		assertEquals("1", result.get(0));
		assertEquals("ANSV", result.get(1));
	}

	@Test
	public void testSplitMixedQuoted() {
		List<String> result = SIEUtil.split(testStrings[1]);
		assertEquals(4, result.size());
		assertEquals("2", result.get(0));
		assertEquals("TOMY", result.get(1));
		assertEquals("6", result.get(2));
		assertEquals("106", result.get(3));
	}

	@Test
	public void testSplitEscapedQuoteInString() {
		List<String> result = SIEUtil.split(testStrings[2]);
		assertEquals(2, result.size());
		assertEquals("1", result.get(0));
		assertEquals("Testar 3.5\" disketter.", result.get(1));
	}

	@Test
	public void testSplitUnquoted() {
		List<String> result = SIEUtil.split(testStrings[3]);
		assertEquals(4, result.size());
		assertEquals("1", result.get(0));
		assertEquals("ANSV", result.get(1));
		assertEquals("6", result.get(2));
		assertEquals("106", result.get(3));
	}

	@Test
	public void testSplitPartiallyQuoted() {
		List<String> result = SIEUtil.split(testStrings[4]);
		assertEquals(4, result.size());
		assertEquals("1", result.get(0));
		assertEquals("ANSV", result.get(1));
		assertEquals("6", result.get(2));
		assertEquals("108", result.get(3));
	}

	@Test
	public void testRemoveQuotes() {
		assertEquals("hello", SIEUtil.removeQuotes("\"hello\""));
		assertEquals("with space", SIEUtil.removeQuotes("\"with space\""));
		assertEquals("escaped\"quote", SIEUtil.removeQuotes("\"escaped\\\"quote\""));
		assertNull(SIEUtil.removeQuotes(null));
	}
}
