package ngl.sq;

import static ngl.sq.Global.devapp;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import play.test.TestServer;

// type="text/javascript" attribute on <script> is obsolete in HTML5 as is type="text/css" on <style> and <link rel="stylesheet">.
// HTMLUNIT emits warnings for those.
// 

public class TestBrowser {
	
	//@Test
	public void runInBrowser() throws IOException {
		TestServer server = new TestServer(3333,devapp());
		// Would like to run CHROME as it is installed by default on CEA/CNS computers.
		// play.test.Helpers.running(server, play.test.Helpers.HTMLUNIT, browser -> {
		// play.test.Helpers.running(server, play.test.Helpers.FIREFOX, browser -> {
		// play.test.Helpers.running(server, new org.openqa.selenium.chrome.ChromeDriver(), browser -> {
		play.test.Helpers.running(server, new org.openqa.selenium.htmlunit.HtmlUnitDriver(true), browser -> {
		//play.test.Helpers.running(server, new org.openqa.selenium.firefox.FirefoxDriver(), browser -> {
	        browser.goTo("/");
	        // assertEquals("Welcome to Play!", browser.$("#title").text());
	        // browser.$("a").click();
	        assertEquals("/", browser.url());
	        // ts.stop();
	    });
	}

}
