package org.ats.jmeter.test;


import java.io.FileOutputStream;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.engine.ClientJMeterEngine;
import org.apache.jmeter.engine.DistributedRunner;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.CookiePanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.RemoteThreadsListenerTestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestDistributed  {

  @BeforeClass
  public void setup() {
    JMeterUtils.loadJMeterProperties("src/test/resources/jmeter/bin/jmeter.properties");
    JMeterUtils.setJMeterHome("src/test/resources/jmeter");
    JMeterUtils.initLogging();
  }

  @Test
  public void test() throws Exception{

    //hashtree
    HashTree hashTree = new HashTree();

    //testPlan
    TestPlan testPlan = new TestPlan("MY TEST PLAN"); 
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
    threadGroup.setNumThreads(20);
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
    ra.setAssumeSuccess(false);       
    threadGroupHashTree.add(ra);

    //add ResultCollector
    Summariser summer = null;
    String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");//$NON-NLS-1$
    if (summariserName.length() > 0) {
      summer = new Summariser(summariserName);
    }

    SampleSaveConfiguration conf = new SampleSaveConfiguration(true);        
    conf.setResponseData(false);
    conf.setResponseHeaders(false);
    conf.setRequestHeaders(false);
    conf.setSamplerData(false);
    conf.setUrl(false);
    conf.setFileName(false);
    conf.setFieldNames(false);
    conf.setHostname(false);
    conf.setIdleTime(true);

    ResultCollector logger = new ResultCollector(summer);        
    logger.setProperty(TestElement.TEST_CLASS, ResultCollector.class.getName());
    logger.setProperty(TestElement.GUI_CLASS, ViewResultsFullVisualizer.class.getName());      
    logger.setSaveConfig(conf);
    logger.setFilename("target/result.jtl");
    logger.setEnabled(true);
    logger.setName("View Results Tree");

    threadGroupHashTree.add(logger);

    SaveService.saveTree(hashTree, new FileOutputStream("target/jmeter_script.jmx"));

    hashTree.add(hashTree.getArray()[0], new RemoteThreadsListenerTestElement());
    ListenToTest listener = new ListenToTest(null, null);
    hashTree.add(hashTree.getArray()[0], listener);

    DistributedRunner distributedRunner = new DistributedRunner();
    distributedRunner.setStdout(System.out);
    distributedRunner.setStdErr(System.err);

    List<String> hosts = new ArrayList<String>();
    hosts.add("192.168.1.4");

    distributedRunner.init(hosts, hashTree);
    distributedRunner.start();
    
    String reaperRE = JMeterUtils.getPropDefault("rmi.thread.name", "^RMI Reaper$");
    Thread reaper = null;
    for(Thread t : Thread.getAllStackTraces().keySet()){
        String name = t.getName();
        if (name.matches(reaperRE)) {
          reaper = t;
        }
    }

    if (reaper != null) {
      while(reaper.getState() == State.WAITING) {
      }
    }
  }
  

  static class ListenToTest implements TestStateListener, Runnable, Remoteable {
    private final AtomicInteger started = new AtomicInteger(0); // keep track of remote tests

    //NOT YET USED private JMeter _parent;

    private final List<JMeterEngine> engines;

    /**
     * @param unused JMeter unused for now
     * @param engines List<JMeterEngine>
     */
    public ListenToTest(JMeter unused, List<JMeterEngine> engines) {
      //_parent = unused;
      this.engines=engines;
    }

    @Override
    public void testEnded(String host) {
      long now=System.currentTimeMillis();
      System.out.println("Finished remote host: " + host + " ("+now+")");
      if (started.decrementAndGet() <= 0) {
        Thread stopSoon = new Thread(this);
        stopSoon.start();
      }
    }

    @Override
    public void testEnded() {
      long now = System.currentTimeMillis();
      System.out.println("Tidying up ...    @ "+new Date(now)+" ("+now+")");
      System.out.println("... end of run");
      checkForRemainingThreads();
    }

    @Override
    public void testStarted(String host) {
      started.incrementAndGet();
      long now=System.currentTimeMillis();
      System.out.println("Started remote host:  " + host + " ("+now+")");
    }

    @Override
    public void testStarted() {
      long now=System.currentTimeMillis();
      System.out.println(JMeterUtils.getResString("running_test")+" ("+now+")");//$NON-NLS-1$
    }

    /**
     * This is a hack to allow listeners a chance to close their files. Must
     * implement a queue for sample responses tied to the engine, and the
     * engine won't deliver testEnded signal till all sample responses have
     * been delivered. Should also improve performance of remote JMeter
     * testing.
     */
    @Override
    public void run() {
      long now = System.currentTimeMillis();
      System.out.println("Tidying up remote @ "+new Date(now)+" ("+now+")");
      if (engines!=null){ // it will be null unless remoteStop = true
        System.out.println("Exitting remote servers");
        for (JMeterEngine e : engines){
          e.exit();
        }
      }
      try {
        TimeUnit.SECONDS.sleep(5); // Allow listeners to close files
      } catch (InterruptedException ignored) {
      }
      ClientJMeterEngine.tidyRMI(LoggingManager.getLoggerForClass());
      System.out.println("... end of run");
      checkForRemainingThreads();
    }

    /**
     * Runs daemon thread which waits a short while; 
     * if JVM does not exit, lists remaining non-daemon threads on stdout.
     */
    private void checkForRemainingThreads() {
      // This cannot be a JMeter class variable, because properties
      // are not initialised until later.
      final int REMAIN_THREAD_PAUSE = 
          JMeterUtils.getPropDefault("jmeter.exit.check.pause", 2000); // $NON-NLS-1$ 

      if (REMAIN_THREAD_PAUSE > 0) {
        Thread daemon = new Thread(){
          @Override
          public void run(){
            try {
              TimeUnit.MILLISECONDS.sleep(REMAIN_THREAD_PAUSE); // Allow enough time for JVM to exit
            } catch (InterruptedException ignored) {
            }
            // This is a daemon thread, which should only reach here if there are other
            // non-daemon threads still active
            System.out.println("The JVM should have exitted but did not.");
            System.out.println("The following non-daemon threads are still running (DestroyJavaVM is OK):");
            JOrphanUtils.displayThreads(false);
          }

        };
        daemon.setDaemon(true);
        daemon.start();
      } else if(REMAIN_THREAD_PAUSE<=0) {
        System.out.println("jmeter.exit.check.pause is <= 0, JMeter won't check for unterminated non-daemon threads");
      }
    }
  }
}
