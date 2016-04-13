package com.supermap.desktop.netservices;

import java.util.ResourceBundle;

import com.supermap.desktop.properties.Properties;

public class NetServicesProperties extends Properties {
	public static final String NETSERVICES = "resources.NetServices";

	public static final String getString(String key) {
		return getString(NETSERVICES, key);
	}

	public static final String getString(String baseName, String key) {
		String result = "";

		ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName, getLocale());
		if (resourceBundle != null) {
			result = resourceBundle.getString(key);
		}
		return result;
	}
}
