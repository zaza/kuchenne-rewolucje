package com.github.zaza.kuchennerewolucje.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;

public class EpisodeTest {

	@Test
	public void isOpen() throws Exception {
		assertTrue(new Episode(new HashMap<>()).isOpen());
		assertTrue(new Episode(Collections.singletonMap("zamkniete", false)).isOpen());
		assertFalse(new Episode(Collections.singletonMap("zamkniete", true)).isOpen());
	}
}
