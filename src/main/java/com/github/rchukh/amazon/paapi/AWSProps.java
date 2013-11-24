package com.github.rchukh.amazon.paapi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public enum AWSProps {
	INSTANCE;
	private final String accessKeyId;
	private final String secretKeyId;
	private final String defaultAssociateTag;
	private final Properties properties;

	private AWSProps() {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("amazon.properties");
			properties = new Properties();
			properties.load(is);
			accessKeyId = properties.getProperty("amazon.accessKeyId");
			secretKeyId = properties.getProperty("amazon.secretKeyId");
			defaultAssociateTag = properties.getProperty("amazon.paapi.defaultAssociateTag");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * AWS Access Key ID
	 * 
	 * @return AWS Access Key ID
	 */
	public String getAccessKeyId() {
		return accessKeyId;
	}

	/**
	 * AWS Secret Key
	 * 
	 * @return AWS Secret Key
	 */
	public String getSecretKeyId() {
		return secretKeyId;
	}

	/**
	 * AWS Default Associate tag
	 * 
	 * @return AWS Default Associate tag
	 */
	public String getDefaultAssociateTag() {
		return defaultAssociateTag;
	}

	/**
	 * AWS properties.
	 * 
	 * @return AWS properties.
	 */
	public Properties getProperties() {
		return properties;
	}
}
