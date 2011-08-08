package com.maestrodev.maestro.test.setup;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;

public class SetupTest extends com.maestrodev.maestro.test.selenium.SetupTest
{
    @BeforeTest
    @Parameters( { "browser", "seleniumHost", "seleniumPort", "baseUrl" } )
    public void before( String browser, String seleniumHost, int seleniumPort, String baseUrl )
    {
        super.before( browser, seleniumHost, seleniumPort, baseUrl );
    }
}
