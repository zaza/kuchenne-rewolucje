package com.github.zaza.kuchennerewolucje;

import java.io.File;
import java.io.IOException;

import org.geojson.FeatureCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

class FeatureCollectionReader {

	private static final Logger LOG = LoggerFactory.getLogger(FeatureCollectionReader.class);

	static FeatureCollection read(File file) {
		try {
			return new ObjectMapper().readValue(file, FeatureCollection.class);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			return new FeatureCollection();
		}
	}

}
