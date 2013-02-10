package net.sourceforge.retroweaver.tests;

public class CharacterTest extends AbstractTest{

	public void testConstants() {
		assertTrue("MIN_HIGH_SURROGATE", Character.MIN_HIGH_SURROGATE == '\ud800');
		assertTrue("MAX_HIGH_SURROGATE", Character.MAX_HIGH_SURROGATE == '\udbff');
		assertTrue("MIN_LOW_SURROGATE", Character.MIN_LOW_SURROGATE == '\udc00');
		assertTrue("MAX_LOW_SURROGATE", Character.MAX_LOW_SURROGATE == '\udfff');
		assertTrue("MIN_SURROGATE", Character.MIN_SURROGATE == '\ud800');
		assertTrue("MAX_SURROGATE", Character.MAX_SURROGATE == '\udfff');
		assertEquals("MIN_SUPPLEMENTARY_CODE_POINT", Character.MIN_SUPPLEMENTARY_CODE_POINT, 0x10000);
		assertEquals("MIN_CODE_POINT", Character.MIN_CODE_POINT, 0x0);
		assertEquals("MAX_CODE_POINT", Character.MAX_CODE_POINT, 0x10ffff);
		assertEquals("SIZE", Character.SIZE, 0x10);
	}

	public void testSurrogates() {
		assertTrue("isHighSurrogate min", Character.isHighSurrogate(Character.MIN_HIGH_SURROGATE));
		assertTrue("isHighSurrogate max", Character.isHighSurrogate(Character.MAX_HIGH_SURROGATE));
		assertTrue("isLowSurrogate min", Character.isLowSurrogate(Character.MIN_LOW_SURROGATE));
		assertTrue("isLowSurrogate max", Character.isLowSurrogate(Character.MAX_LOW_SURROGATE));		
	}

}