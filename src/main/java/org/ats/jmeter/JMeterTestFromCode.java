package org.ats.jmeter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.engine.DistributedRunner;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.CookiePanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.ResultSaver;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jmeter.timers.gui.ConstantTimerGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jorphan.collections.HashTree;


public class JMeterTestFromCode {

    public static void main(String[] args){
    	testSaveJmx();
    }

    public static void testSaveJmx() {

		StandardJMeterEngine jm = new StandardJMeterEngine();
		JMeterUtils.loadJMeterProperties("src/test/resources/bin/jmeter.properties");
		JMeterUtils.setJMeterHome("src/test/resources");
		JMeterUtils.setLocale(new Locale("ignoreResources"));

		//hashtree
		HashTree hashTree = new HashTree();
		
		//testPlan
		TestPlan testPlan = new TestPlan("MY TEST PLAN");	
//		testPlan.setComment("");
		testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
		testPlan.setProperty(TestElement.GUI_CLASS, TestPlan.class.getName());
		
        
        // Loop Controller
        LoopController loopController = new LoopController();
        loopController.setLoops(1);
        loopController.setFirst(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();
        
        
        
     // Thread Group
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("Example Thread Group");
        threadGroup.setNumThreads(100);
        threadGroup.setRampUp(5);
        threadGroup.setSamplerController(loopController);
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        
        
        hashTree.add(testPlan);        
        HashTree threadGroupHashTree = hashTree.add(testPlan, threadGroup);
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
        httpSampler.setName("Login codeproject post");
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
       // ra.addTestString("1111111");
        ra.setAssumeSuccess(false);       
        threadGroupHashTree.add(ra);
        
		
      //add ResultCollector
        
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");//$NON-NLS-1$
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        String logFile = "target/file.jtl";
        ResultCollector logger = new ResultCollector(summer);        
        logger.setProperty(TestElement.TEST_CLASS, ResultCollector.class.getName());
        logger.setProperty(TestElement.GUI_CLASS, ViewResultsFullVisualizer.class.getName());        
        SampleSaveConfiguration conf = new SampleSaveConfiguration(true);        
        conf.setResponseData(false);
        logger.setSaveConfig(conf);
        
        logger.setFilename(logFile);
        logger.setEnabled(true);
        logger.setName("View Results Tree");
        
        threadGroupHashTree.add(logger);
        
        
//        ResultSaver resultSaver = new ResultSaver();
//        resultSaver.setEnabled(true);
//        resultSaver.setName("Save Responses to a file");
//        
//        threadGroupHashTree.add(resultSaver);
        //add constant time
        
//        ConstantTimer constantTimer = new ConstantTimer();
//        constantTimer.setProperty(TestElement.TEST_CLASS, ConstantTimer.class.getName());
//        constantTimer.setProperty(TestElement.GUI_CLASS, ConstantTimerGui.class.getName());
//        constantTimer.setName("ContantTimer");
//        constantTimer.setDelay("5000");
//        threadGroupHashTree.add(constantTimer);
		try {
			SaveService.saveTree(hashTree, new FileOutputStream(
					"target/example1.jmx"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block1
			e.printStackTrace();
		}
		
		 jm.configure(hashTree);
	     jm.run();

	}
    public static void testPost(){
    	


        // Engine
         StandardJMeterEngine jm = new StandardJMeterEngine();
        // jmeter.properties
        JMeterUtils.loadJMeterProperties("C://tmp//jmeter.properties");
        JMeterUtils.setJMeterHome("C://tmp");
        JMeterUtils.setLocale(new Locale("ignoreResources"));
        HashTree hashTree = new HashTree();     
        
        
        //reponse asertion
        ResponseAssertion ra = new ResponseAssertion();        
        ra.setTestFieldResponseData();
        ra.setToContainsType();
        ra.addTestString("kakalot8x08");
        

        // HTTP Sampler
        
        HTTPSampler httpSampler = new HTTPSampler();
        httpSampler.setName("TestCodeproject");
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
        
        
      
        
        //httpSampler.addTestElement(ra);
        
       	
        
        
        //httpSampler.addTestElement(ra);
        
        
       
        
        
        
        
        
        
         // Loop Controller
        
        
        
        TestElement loopCtrl = new LoopController();
        ((LoopController)loopCtrl).setLoops(1);
        ((LoopController)loopCtrl).addTestElement(httpSampler); 
       // ((LoopController)loopCtrl).addTestElement(ra); 
        ((LoopController)loopCtrl).setFirst(true);
        
        
        
        

        // Thread Group
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("TestGoogle");
        
        //SetupThreadGroup threadGroup = new SetupThreadGroup();
        
        threadGroup.setNumThreads(1);
        threadGroup.setRampUp(1);
        
        threadGroup.setSamplerController((LoopController)loopCtrl);
       // threadGroup.addTestElement(ra);
        
        
        
        
        
        
        

        // Test plan
        TestPlan testPlan = new TestPlan("MY TEST PLAN");
        
        testPlan.addTestElement(ra);
        
        
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");//$NON-NLS-1$
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        String logFile = "C://tmp//file.jtl";
        ResultCollector logger = new ResultCollector(summer);
        
        SampleSaveConfiguration conf = new SampleSaveConfiguration(true);
        conf.setResponseData(true);
        logger.setSaveConfig(conf);
        logger.setFilename(logFile);        
        
        hashTree.add("threadGroup", threadGroup);
        hashTree.add("testPlan", testPlan);
        hashTree.add("loopCtrl", loopCtrl);
        
        hashTree.add("httpSampler", httpSampler);        
        //httpSampler.add("httpSampler",logger);
        //hashTree.add(hashTree.getArray()[0], logger);
        //hashTree.add("ctrlogger", logger);
         hashTree.add(hashTree.getArray()[0], logger);
         CookieManager cookieManager = new CookieManager();
         hashTree.add("cookieManage", cookieManager);       
         //hashTree.add("ra", ra);
         System.err.println(hashTree.getArray()[0]);
         try {
			SaveService.saveTree(hashTree, new FileOutputStream("C://tmp/example1.jmx"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block1
			e.printStackTrace();
		}
        
		// run distribute
//        JMeterUtils.setProperty(DistributedRunner.RETRIES_NUMBER, "1");
//        JMeterUtils.setProperty(DistributedRunner.CONTINUE_ON_FAIL, "true");
//        
//        
//		 DistributedRunner distributedRunner = new DistributedRunner();
//		 
//		 
//		 
//		 
//		 List<String> hosts = new ArrayList<>();
//		// hosts.add("192.168.184.1");
//		 hosts.add("192.168.184.4");
//		 hosts.add("192.168.184.5");
//		 
//		 distributedRunner.init(hosts, hashTree);
//		
////		 List<String> hosts1 = Arrays.asList("192.168.184.4");
//		 distributedRunner.start(hosts);
//		 
//		 distributedRunner.shutdown(hosts);
//		 distributedRunner.stop();
		// distributedRunner.exit(hosts);
		 
//		 distributedRunner.stop();
 	
		
		//

        jm.configure(hashTree);
        //jm.run();
		// System.exit(0);
        
		 System.out.println("END OF SCRIPT");
    
     
    	
    }
    public static void testGet(){

        // Engine
    //    StandardJMeterEngine jm = new StandardJMeterEngine();
        // jmeter.properties
        JMeterUtils.loadJMeterProperties("C://tmp//jmeter.properties");
        JMeterUtils.setJMeterHome("C://tmp");
        JMeterUtils.setLocale(new Locale("ignoreResources"));
        HashTree hashTree = new HashTree();     

        // HTTP Sampler
        
        HTTPSampler httpSampler = new HTTPSampler();
        httpSampler.setName("TestGoogle");
        httpSampler.setDomain("www.google.com");
        httpSampler.setPort(80);
        httpSampler.setPath("/");
        httpSampler.setMethod("GET");
        
        
        
         // Loop Controller
        TestElement loopCtrl = new LoopController();
        ((LoopController)loopCtrl).setLoops(1);
        ((LoopController)loopCtrl).addTestElement(httpSampler);
        ((LoopController)loopCtrl).setFirst(true);
        
        

        // Thread Group
        SetupThreadGroup threadGroup = new SetupThreadGroup();
        threadGroup.setNumThreads(1);
        threadGroup.setRampUp(1);
        threadGroup.setSamplerController((LoopController)loopCtrl);

        // Test plan
        TestPlan testPlan = new TestPlan("MY TEST PLAN");
        
        
        
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");//$NON-NLS-1$
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        String logFile = "C://tmp//file.jtl";
        ResultCollector logger = new ResultCollector(summer);
        
        SampleSaveConfiguration conf = new SampleSaveConfiguration(true);
//        conf.setResponseData(false);
        logger.setSaveConfig(conf);
        logger.setFilename(logFile);        
        
        
        hashTree.add("testPlan", testPlan);
        hashTree.add("loopCtrl", loopCtrl);
        hashTree.add("threadGroup", threadGroup);
        hashTree.add("httpSampler", httpSampler);        
        //httpSampler.add("httpSampler",logger);
        //hashTree.add(hashTree.getArray()[0], logger);
        //hashTree.add("ctrlogger", logger);
         hashTree.add(hashTree.getArray()[0], logger);
        System.err.println(hashTree.getArray()[0]);
        
		// run distribute
        JMeterUtils.setProperty(DistributedRunner.RETRIES_NUMBER, "1");
        JMeterUtils.setProperty(DistributedRunner.CONTINUE_ON_FAIL, "true");
        
        
		 DistributedRunner distributedRunner = new DistributedRunner();
		 
		 
		 
		 
		 
		 
		 List<String> hosts = new ArrayList<String>();
		// hosts.add("192.168.184.1");
		 hosts.add("192.168.184.4");
		 hosts.add("192.168.184.5");
		 
		 distributedRunner.init(hosts, hashTree);
		
//		 List<String> hosts1 = Arrays.asList("192.168.184.4");
		 distributedRunner.start(hosts);
		 
		 distributedRunner.shutdown(hosts);
		 distributedRunner.stop();
		// distributedRunner.exit(hosts);
		 
//		 distributedRunner.stop();
 	
		
		//

//        jm.configure(hashTree);
//        jm.run();
		// System.exit(0);
		 System.out.println("END OF SCRIPT");
    
    }
}
