package com.github.zaza.kuchennerewolucje.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;

public class EpisodeTest {

	@Test
	public void hasZipCode() throws Exception {
		assertTrue(newEpisode("adres", "Flisacka 35B, 34-460 Gmina Szczawnica").hasZipCode());
		assertTrue(newEpisode("adres", "Michała Ogińskiego 5, 03-318 Warszawa").hasZipCode());
		assertTrue(newEpisode("adres", "Maratońska 57, 94-102 Łódź").hasZipCode());
		assertTrue(newEpisode("adres", "03-318 Warszawa").hasZipCode());
		assertFalse(newEpisode("adres", "Warszawa").hasZipCode());
		assertFalse(newEpisode("adres", "Gmina Szczawnica").hasZipCode());
	}

	@Test
	public void getCity() throws Exception {
		assertEquals("Gmina Szczawnica", newEpisode("adres", "Flisacka 35B, 34-460 Gmina Szczawnica").getCity());
		assertEquals("Warszawa", newEpisode("adres", "Michała Ogińskiego 5, 03-318 Warszawa").getCity());
		assertEquals("Łódź", newEpisode("adres", "Maratońska 57, 94-102 Łódź").getCity());
		assertEquals("Warszawa", newEpisode("adres", "03-318 Warszawa").getCity());
		assertEquals("Warszawa", newEpisode("adres", "Warszawa").getCity());
		assertEquals("Gmina Szczawnica", newEpisode("adres", "Gmina Szczawnica").getCity());
	}

	@Test
	public void isOpen() throws Exception {
		assertTrue(new Episode(new HashMap<>()).isOpen());
		assertTrue(newEpisode("zamkniete", false).isOpen());
		assertFalse(newEpisode("zamkniete", true).isOpen());
	}

	private static Episode newEpisode(String key, Object value) {
		return new Episode(Collections.singletonMap(key, value));
	}
}
