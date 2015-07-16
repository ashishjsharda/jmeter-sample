package org.ats.jmeter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.CookiePanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jmeter.timers.gui.ConstantTimerGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jorphan.collections.HashTree;

public class JmeterUtil {
	
	public static HashTree initHashTree(String logFilePath, String jmeterHomeDir, String testPlanName, int numberThread, int ramup, int numberCount){
		HashTree hashTree = new HashTree();
		
		//testPlan
		TestPlan testPlan = new TestPlan(testPlanName);
		testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
		testPlan.setProperty(TestElement.GUI_CLASS, TestPlan.class.getName());
		
		// Loop Controller
        LoopController loopController = new LoopController();
        loopController.setLoops(numberCount);
        loopController.setFirst(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();
        
        
     // Thread Group
        ThreadGroup threadGroup = new ThreadGroup();        
        threadGroup.setNumThreads(numberThread);
        threadGroup.setRampUp(ramup);
        threadGroup.setSamplerController(loopController);
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        hashTree.add(testPlan);        
        HashTree threadGroupHashTree = hashTree.add(testPlan, threadGroup);
		
		return threadGroupHashTree;
		
	}
	
	
	public static HashTree generateHashTree(String logFilePath, String jmeterHomeDir, String testPlanName, int numberThread, int ramup, int numberCount) {

        
        HashTree threadGroupHashTree = initHashTree(logFilePath, jmeterHomeDir, testPlanName, numberThread, ramup, numberCount);
        
        CookieManager cookie = new CookieManager();
        
        cookie.setProperty(TestElement.TEST_CLASS, CookieManager.class.getName());
        cookie.setProperty(TestElement.GUI_CLASS, CookiePanel.class.getName());
        cookie.setName("HTTP Cookie Manager");
        cookie.setEnabled(true);
        cookie.setClearEachIteration(false);
        threadGroupHashTree.add(cookie);
        
        
        //httpSampler
        HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();   
        httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSampler.setEnabled(true);
        httpSampler.setCookieManager(cookie);        
        httpSampler.setName("HTTP Request");
        httpSampler.setDomain("www.codeproject.com");
        httpSampler.setPort(443);
        httpSampler.setProtocol("https");
        httpSampler.setPath("/script/Membership/LogOn.aspx?rp=%2f%3floginkey%3dfalse");
        httpSampler.setMethod("POST");
        httpSampler.addArgument("FormName", "MenuBarForm");
        httpSampler.addArgument("Email", "kakalot8x08@gmail.com");
        httpSampler.addArgument("Password", "tititi");
        httpSampler.setFollowRedirects(true);        
        httpSampler.setUseKeepAlive(true);
        
        threadGroupHashTree.add(httpSampler);        
         
        //add ResponseAssertion
        ResponseAssertion ra = new ResponseAssertion(); 
        ra.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
        ra.setProperty(TestElement.GUI_CLASS, AssertionGui.class.getName());
        
        
        ra.setName("Response Assertion");
        ra.setEnabled(true);
        ra.setTestFieldResponseData();
        ra.setToContainsType();
        ra.addTestString("kakalot");
        ra.addTestString("1111111");
        ra.setAssumeSuccess(false);       
        threadGroupHashTree.add(ra);
        
		
      //add ResultCollector
        
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");//$NON-NLS-1$
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        String logFile = jmeterHomeDir + "//log_file.jtl";
        ResultCollector logger = new ResultCollector(summer);        
        logger.setProperty(TestElement.TEST_CLASS, ResultCollector.class.getName());
        logger.setProperty(TestElement.GUI_CLASS, ViewResultsFullVisualizer.class.getName());        
        SampleSaveConfiguration conf = new SampleSaveConfiguration(true);        
        
        logger.setSaveConfig(conf);
        logger.setFilename(logFile);
        logger.setEnabled(true);
        logger.setName("View Results Tree");
        
        threadGroupHashTree.add(logger);
        
        //add constant time
        
        ConstantTimer constantTimer = new ConstantTimer();
        constantTimer.setProperty(TestElement.TEST_CLASS, ConstantTimer.class.getName());
        constantTimer.setProperty(TestElement.GUI_CLASS, ConstantTimerGui.class.getName());
        constantTimer.setName("ContantTimer");
        constantTimer.setDelay("5000");
        threadGroupHashTree.add(constantTimer);        
		return threadGroupHashTree;	
	
	}
	
	
}
