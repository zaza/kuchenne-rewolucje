package com.github.zaza.kuchennerewolucje.model;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Episode {

	private static final String MARKER_RED = "https://raw.githubusercontent.com/Concept211/Google-Maps-Markers/master/images/marker_red.png";

	private static final String MARKER_PURPLE = "https://raw.githubusercontent.com/Concept211/Google-Maps-Markers/master/images/marker_purple.png";

	private static final Pattern PATTERN_ZIPCODE = Pattern.compile("\\d{2}-\\d{3} ([\\w ]+)",
			Pattern.UNICODE_CHARACTER_CLASS);
	private Map<String, Object> episode;

	public Episode(Map<String, Object> episode) {
		this.episode = episode;
	}
	
	public int getSeason() {
		return (int) episode.get("sezon");
	}

	public int getNumber() {
		return (int) episode.get("odcinek");
	}

	public String getAddress() {
		return (String) episode.get("adres");
	}

	public String getName() {
		return (String) episode.get("nazwa");
	}

	public String getUrl() {
		return (String) episode.get("url");
	}

	public boolean hasHomepage() {
		return episode.get("homepage") != null;
	}

	public String getHomepage() {
		return (String) episode.get("homepage");
	}

	public boolean isOpen() {
		return episode.get("zamkniete") == null || !((Boolean) episode.get("zamkniete"));
	}

	public boolean hasZipCode() {
		return PATTERN_ZIPCODE.matcher(getAddress()).find();
	}

	public String getCity() {
		Matcher matcher = PATTERN_ZIPCODE.matcher(getAddress());
		return matcher.find() ? matcher.group(1) : getAddress();
	}

	public String getIcon() {
		return getIcon(isOpen());
	}

	public String getIcon(boolean open) {
		return open ? MARKER_RED : MARKER_PURPLE;
	}
}
