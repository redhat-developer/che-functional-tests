/******************************************************************************* 
 * Copyright (c) 2017 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
*/
package redhat.che.functional.tests;

import com.redhat.arquillian.che.provider.CheWorkspaceProvider;
import com.redhat.arquillian.che.resource.CheWorkspace;
import org.apache.log4j.Logger;
import org.arquillian.extension.recorder.screenshooter.Screenshooter;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Sleeper;

import redhat.che.functional.tests.fragments.EditorPart;
import redhat.che.functional.tests.fragments.Project;
import redhat.che.functional.tests.fragments.infoPanel.ConsolesPanel;
import redhat.che.functional.tests.fragments.infoPanel.InfoPanel;
import redhat.che.functional.tests.fragments.infoPanel.WorkspaceStatusPage;
import redhat.che.functional.tests.fragments.topmenu.MainMenuPanel;
import redhat.che.functional.tests.fragments.topmenu.WorkspaceTopMenu;
import redhat.che.functional.tests.utils.Constants;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(Arquillian.class)
public abstract class AbstractCheFunctionalTest {
    private static final Logger LOG = Logger.getLogger(AbstractCheFunctionalTest.class);

    @Drone
    protected WebDriver driver;

    @FindByJQuery("#gwt-debug-projectTree div[project='/" + Constants.VERTX_PROJECT_NAME + "']")
    protected Project vertxProject;

    @FindByJQuery("#gwt-debug-projectTree div[project='/" + Constants.NODEJS_PROJECT_NAME + "']")
    protected Project nodejsProject;

    @FindBy(id = "gwt-debug-editorMultiPartStack-contentPanel")
    protected EditorPart editorPart;

    @FindByJQuery("#gwt-debug-popup-container")
    private WebElement workspacePopupContainer;

    @FindByJQuery("#gwt-debug-popup-container:contains('Workspace is running')")
    private WebElement workspacePopupIsRunning;

    @FindBy(id = "username")
    private WebElement usernameField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(id = "kc-login")
    private WebElement loginButton;
    
    @FindBy(id = "gwt-debug-infoPanel")
    protected InfoPanel infoPanel;
    
    @FindBy(id = "gwt-debug-mainMenuPanel")
    protected MainMenuPanel mainMenuPanel;
    
    @FindBy(id = "menu-lock-layer-id")
    private WorkspaceTopMenu workspaceTopMenu;
    
    @FindBy(id = "ide-loader-progress-bar")
    private WebElement loaderProgressBar;
    
    @FindBy(id = "codenvyIdeWorkspaceViewImpl")
    private WebElement ideElement;

    @ArquillianResource
    private static CheWorkspace workspace;

    @ArquillianResource
    static CheWorkspaceProvider provider;

    @ArquillianResource
    private static Screenshooter screenshooter;

    public static final String bayesianErrorNotVisible = "Known expected bug : https://github.com/openshiftio/openshift.io/issues/2063";
    public static final String bayesianErrorExpectedURL = "prod-preview.openshift.io";
    private static final String SCREENSHOTS_DIRECTORY_PATH = "./target/screenshots/";

    void openBrowser() {
        openBrowser(workspace);
    }

    private void openBrowser(CheWorkspace wkspc) {
        LOG.info("Opening browser");
        driver.get(wkspc.getIdeLink());
//        driver.manage().window().maximize(); // Causes crash with Selenium on Xvfb - no window manager present
        try {
            Graphene.waitGui().withTimeout(10, TimeUnit.SECONDS).until().element(usernameField).is().visible();
            login();
        } catch (WebDriverException e) {
            LOG.error("Login failed", e);
            // Discard in case the login page was not displayed
        }
        waitForWorkspaceToLoad();
    }

    private void login() {
        LOG.info("Logging in");
        usernameField.sendKeys(Constants.OSIO_USERNAME);
        passwordField.sendKeys(Constants.OSIO_PASSWORD);
        loginButton.click();
    }

	private void waitForWorkspaceToLoad() {
		LOG.info("Waiting for workspace to get up to state.");
		waitForLoaderToDisappear();
		waitForWorkspaceIsRunning();
		waitUntilAllVisiblePopupsDisappear();
	}
    
	private void waitForLoaderToDisappear() {
		System.out.println(loaderProgressBar.isDisplayed());
		Graphene.waitModel().until().element(loaderProgressBar).is().visible();
		Graphene.waitModel().until().element(ideElement).is().visible();
	}

	private void waitForWorkspaceIsRunning() {
		mainMenuPanel.clickWorkspace();
		workspaceTopMenu.showStatus();
		ConsolesPanel consolesPanel = infoPanel.getConsolesPanel();
		consolesPanel.activateTab("Workspace Status");
		WorkspaceStatusPage workspaceStatusPage = consolesPanel.getWorkspaceStatusPage();
		Graphene.waitModel().withTimeout(25, TimeUnit.SECONDS).until(d -> workspaceStatusPage.isWorkspaceRunning());
	}

	private void waitUntilAllVisiblePopupsDisappear() {
        Graphene.waitGui().withTimeout(1, TimeUnit.MINUTES).until(webDriver -> {
            List<WebElement> children = getNumberOfPopupsVisible();
            return children.isEmpty();
        });
    }

    private List<WebElement> getNumberOfPopupsVisible() {
        LOG.trace("Gathering children");
        List<WebElement> children = workspacePopupContainer.findElements(By.xpath(".//*"));
        LOG.trace("Size:" + children.size());
        return children;
    }

    public static boolean isProdPreview() {
        return CheWorkspaceProvider.getConfiguration().getOsioUrlPart().equals(bayesianErrorExpectedURL);
    }

    /*===============================*
     * Screenshooter custom commands *
     *===============================*/
    //TODO: Create issue on Arquillian screenshooter repo to ask for this feature

    public void takeScreenshot() {
        this.takeScreenshot(null);
    }

    public void takeScreenshot(String fileName) {
        if (fileName != null) {
            if (!fileName.isEmpty()) {
                screenshooter.takeScreenshot(getUnusedFileName(fileName));
                return;
            }
        }
        screenshooter.takeScreenshot(getUnusedFileName(this.getClass().getSimpleName()));
    }

    private String getUnusedFileName(String fileName) {
        String path;
        String response;
        long attempt = 0;
        do {
            response = fileName + (attempt==0 ? "" : "_" + String.valueOf(attempt)) + ".png";
            path = SCREENSHOTS_DIRECTORY_PATH + response;
            LOG.debug("Testing file:"+path);
            attempt++;
        } while (new File(path).exists());
        return response;
    }

    /*===============================*/

}
