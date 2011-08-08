package test.java.com.maestrodev.maestro.web.test.beta;


import static com.maestrodev.maestro.web.CustomizedThreadSafeSeleniumSession.getSession;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.maestrodev.maestro.web.test.SetupSelenium;
import com.thoughtworks.selenium.Selenium;

@Test( groups = { "beta" } )
public class BetaTest extends SetupSelenium
{
    
    private static final String BUILD_AGENT_DESCRIPTION = "Beta Testing Build Agent";
    
    private static final String BUILD_AGENT_URL = "http://localhost:9023/continuum-buildagent/xmlrpc";
    
    private static final String DEPLOYMENT_SERVER_DESCRIPTION = "Beta Testing Tomcat Server";
    
    private static final String TIMEOUT = "30000";
    
    @Test
    public void testBetaConfigurationMode() throws Exception
    {
        Selenium selenium = getSession().getSelenium();
        
        selenium.open( "/maestro/beta" );
        waitForPageToLoad();
        
        loginAndSetSpeed( "500" );

        assertEquals( selenium.getTitle(), "Maestro - Beta Configuration", "This test must run in beta mode." );
        
        String mode = selenium.getText( "//div[@id='mode']" );
        if ( !mode.endsWith( "beta" ) )
        {
            //click to force to beta mode
            selenium.click( "//button[@id='switch_mode']" ); 
        }
        
        try 
        {
            //add build agent
            addBuildAgent( selenium );
            
            //add deployment server
            addDeploymentServer( selenium );
            
            testInBetaMode( selenium );
            
            selenium.open( "/maestro/beta" );
            waitForPageToLoad();
            selenium.click( "//button[@id='switch_mode']" ); 
            
            testInProductionMode( selenium );
        }
        finally
        {
            //remove build agent
            removeBuildAgent( selenium );
            
            //remove deployment server
            removeDeploymentServer( selenium );
            
            //change back to beta
            selenium.open( "/maestro/beta" );
            if ( !mode.endsWith( "beta" ) )
            {
                //click to force to beta mode
                selenium.click( "//button[@id='switch_mode']" ); 
            }
        }
    }
    
    private void addBuildAgent( Selenium selenium ) throws Exception
    {
        selenium.open( "/maestro/infrastructure" );
        
        String buildAgentXpath = "//td[@title='Continuum']/following-sibling::td[contains(@title,'" + BUILD_AGENT_DESCRIPTION + "')]";
        
        removeBuildAgent( selenium );
        
        // add BUILD build agent
        selenium.click( "//a[@id='btnAddStandaloneServer']/span" );
        
        //wait for Add StandAlone Server to displayed
        waitForElementVisible( "//*[@id='ui-dialog-title-modal-addStandaloneServer-form']" );
        
        selenium.select( "type", "label=Build Agent" );
        selenium.select( "usage", "label=Build" );
        selenium.type( "address", BUILD_AGENT_URL );
        selenium.type( "xpath=//input[@id='description']", BUILD_AGENT_DESCRIPTION );
        selenium.click( "xpath=//input[@id='createButton']" );
        selenium.waitForPageToLoad( TIMEOUT );
        
        //confirm to have been added
        assertTrue( selenium.isElementPresent( buildAgentXpath ) );
    }
    
    private void removeBuildAgent( Selenium selenium ) throws Exception
    {
        String buildAgentXpath = "//td[@title='Continuum']/following-sibling::td[contains(@title,'" + BUILD_AGENT_DESCRIPTION + "')]";
        String deleteBtnXpath = "xpath=//preceding::td[contains(@title,'" + BUILD_AGENT_DESCRIPTION + "')]/following::td/div/ul/li/button[@title='Delete']";
        String dialogXpath = "//div/span[contains(text(),'Delete Standalone Server')]";
        
        selenium.open( "/maestro/infrastructure" );
        
        if ( selenium.isElementPresent( buildAgentXpath ) )
        {
            // remove build agent
            selenium.click( deleteBtnXpath );
            
            waitForElementVisible( dialogXpath );
            selenium.click( "xpath=//button/span[text()='Yes']" );
            
            waitForElementNotVisible( dialogXpath );
        }
        
        //check to have been removed
        assertFalse( selenium.isElementPresent( buildAgentXpath ) );
    }
    
    private void addDeploymentServer( Selenium selenium ) throws Exception
    {
        String tomcatLabel = "Tomcat6x";
        
        selenium.open( "/maestro/infrastructure" );
        
        removeDeploymentServer( selenium ); 
        
        selenium.click( "//a[@id='btnAddStandaloneServer']/span" );
        
        // wait for add dialog to display
        waitForElementVisible( "//*[@id='ui-dialog-title-modal-addStandaloneServer-form']" );

        selenium.select( "type", "label=" + tomcatLabel );

        //admin console port field is not visible
        assertFalse( selenium.isVisible( "//td[text()='Admin Console Port']" ) );
        selenium.type( "//input[@id='baseUrl']", "http://localhost" );
        selenium.type( "port", "8095" );
        selenium.type( "//input[@id='username' and @name='username']", "tomcatusername" );
        selenium.type( "//input[@id='password' and @name='password']", "tomcatpassword" );
        selenium.type( "//input[@id='description']", DEPLOYMENT_SERVER_DESCRIPTION );
        selenium.select( "platform", "label=Linux" );
        selenium.click( "createButton" );
        selenium.waitForPageToLoad( TIMEOUT );
        
        //confirmed to  have been added
        assertTrue( selenium.isTextPresent( DEPLOYMENT_SERVER_DESCRIPTION ) );
    }
    
    private void removeDeploymentServer( Selenium selenium ) throws Exception
    {
        String tomcatLabel = "Tomcat6x";
        String tomcatXpath = "//td[@title='" + tomcatLabel + "']/following-sibling::td[contains(@title, '" + DEPLOYMENT_SERVER_DESCRIPTION + "')]";
        String deleteBtnXpath = "xpath=//preceding::td[contains(@title,'" + DEPLOYMENT_SERVER_DESCRIPTION + "')]/following::td/div/ul/li/button[@title='Delete']";
        String dialogXpath = "//div/span[contains(text(),'Delete Standalone Server')]";
    
        selenium.open( "/maestro/infrastructure" );
        
        //check if tomcat server is already added
        if ( selenium.isElementPresent( tomcatXpath ) )
        {
            // remove tomcat server
            selenium.click( deleteBtnXpath );
            
            waitForElementVisible( dialogXpath );
            selenium.click( "xpath=//button/span[text()='Yes']" );
            
            waitForElementNotVisible( dialogXpath );
        }

        //check to have been removed
        assertFalse( selenium.isElementPresent( tomcatXpath ) );
    }
    
    private void testInBetaMode( Selenium selenium )
    {
        //console page
        selenium.open( "/maestro/console" );
        assertEquals( selenium.getTitle(), "Maestro - Console");
        
        //console tab
        assertTrue( selenium.isElementPresent( "//li[@id='mNav-console']/a/span[text()='Console']" ) );
        selenium.open( "/maestro/build" );
        assertTrue( selenium.isElementPresent( "//li[@id='mNav-console']/a/span[text()='Console']" ) );
        selenium.open( "/maestro/test" );
        assertTrue( selenium.isElementPresent( "//li[@id='mNav-console']/a/span[text()='Console']" ) );
        
        //search component
        assertTrue( selenium.isElementPresent( "//form[@id='searchBox']" ) );
        
        //admin maintenance task tab
        selenium.open( "/maestro/admin" );
        assertTrue( selenium.isElementPresent( "//li[@id='tablist-2']/a[text()='Maintenance Tasks']" ) );
        
        //disconnect deployment server button
        selenium.open( "/maestro/infrastructure" );
        //get deployment id id
        String deploymentServerId = selenium.getAttribute( "//td[@title='" + DEPLOYMENT_SERVER_DESCRIPTION + "']/..@id" );
        assertTrue( selenium.isElementPresent( "//button[@id='disconnect-" + deploymentServerId + "']" ) );
        
        //disconnectStandAloneServer
        //get buildagent id
        String buildAgentId = selenium.getAttribute( "//td[@title='" + BUILD_AGENT_DESCRIPTION + "']/..@id" );
        assertTrue( selenium.isElementPresent( "//button[@id='disconnect-" + buildAgentId + "']" ) );
        
        //disconnectMaestroServer
        String maestroServerId = selenium.getAttribute( "//td[@title='Maestro Console']/..@id" );
        assertTrue( selenium.isElementPresent( "//button[@id='disconnect-" + maestroServerId + "']" ) );
    }
    
    private void testInProductionMode( Selenium selenium )
    {
        //console page
        selenium.open( "/maestro/console" );
        assertEquals( selenium.getTitle(), "Maestro - Build");
        
        //console tab
        assertFalse( selenium.isElementPresent( "//li[@id='mNav-console']/a/span[text()='Console']" ) );
        selenium.open( "/maestro/build" );
        assertFalse( selenium.isElementPresent( "//li[@id='mNav-console']/a/span[text()='Console']" ) );
        selenium.open( "/maestro/test" );
        assertFalse( selenium.isElementPresent( "//li[@id='mNav-console']/a/span[text()='Console']" ) );
        
        //search component
        assertFalse( selenium.isElementPresent( "//form[@id='searchBox']" ) );
        
        //admin maintenance task tab
        selenium.open( "/maestro/admin" );
        assertFalse( selenium.isElementPresent( "//li[@id='tablist-2']/a[text()='Maintenance Tasks']" ) );
        
        //disconnect deployment server button
        selenium.open( "/maestro/infrastructure" );
        //get deployment id id
        String deploymentServerId = selenium.getAttribute( "//td[@title='" + DEPLOYMENT_SERVER_DESCRIPTION + "']/..@id" );
        assertFalse( selenium.isElementPresent( "//button[@id='disconnect-" + deploymentServerId + "']" ) );
        
        //disconnectStandAloneServer
        //get buildagent id
        String buildAgentId = selenium.getAttribute( "//td[@title='" + BUILD_AGENT_DESCRIPTION + "']/..@id" );
        assertFalse( selenium.isElementPresent( "//button[@id='disconnect-" + buildAgentId + "']" ) );
        
        //disconnectMaestroServer
        String maestroServerId = selenium.getAttribute( "//td[@title='Maestro Console']/..@id" );
        assertFalse( selenium.isElementPresent( "//button[@id='disconnect-" + maestroServerId + "']" ) );
    }
}
