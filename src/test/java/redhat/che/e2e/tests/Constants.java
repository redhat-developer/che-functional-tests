/******************************************************************************* 
 * Copyright (c) 2017 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package redhat.che.e2e.tests;

public class Constants {
	// Properties
	public static final String CHE_SERVER_PROPERTY_NAME = "cheServerURL";
	public static final String PRESERVE_WORKSPACE_PROPERTY_NAME = "preserveWorkspace";
	
	public static final String CHE_STARTER_PROPERTY_NAME = "cheStarterURL";
	public static final String OPENSHIFT_MASTER_URL_PROPERTY_NAME = "openShiftMasterURL";
	public static final String OPENSHIFT_TOKEN_PROPERTY_NAME = "openShiftToken";

	// Current values
	public static final String CHE_SERVER = getProperty(CHE_SERVER_PROPERTY_NAME, null);
	public static final String PRESERVE_WORKSPACE = getProperty(PRESERVE_WORKSPACE_PROPERTY_NAME, "false");
	public static final String CHE_STARTER_URL = getProperty(CHE_STARTER_PROPERTY_NAME, null);
	public static final String OPENSHIFT_MASTER_URL = getProperty(OPENSHIFT_MASTER_URL_PROPERTY_NAME, null);
	public static final String OPENSHIFT_TOKEN = getProperty(OPENSHIFT_TOKEN_PROPERTY_NAME, null);
	public static final String PROJECT_REPO = "https://github.com/mlabuda/vertx-with-che.git";
	public static final String PROJECT_REPO_BRANCH = "master";
	public static final String DESCRIPTION = PROJECT_REPO + "#" + PROJECT_REPO_BRANCH;
	public static final String PROJECT_NAME = "vertx-with-che";
	public static final String TEST_FILE = "MainVerticleTest.java";
	public static final String[] PATH_TO_TEST_FILE = new String[] { PROJECT_NAME, "src", "test", "java", "io", "vertx",
			"starter", TEST_FILE };

	
	// Path to resources
	public static final String WORKSPACE_JSON = "src/main/resources/create-ws.json";
	public static final String PROJECT_JSON = "src/main/resources/create-project.json";
	public static final String CREATE_WORKSPACE_REQUEST_JSON = "src/main/resources/create-ws_che-starter_request.json";

	
	private static String getProperty(String propertyName, String defaultValue) {
		String property = System.getProperty(propertyName);
		if (property == null || property.isEmpty()) {
			return defaultValue;
		} else {
			return property;
		}
	}
}
