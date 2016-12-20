package com.github.zaza.kuchennerewolucje.model;

import java.util.Map;

public class Episode {
	private Map<String, Object> episode;

	public Episode(Map<String, Object> episode) {
		this.episode = episode;
	}
	
	public String getAddress() {
		return (String) episode.get("adres");
	}
	
	public String getName() {
		return (String) episode.get("name");
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
}
