package org.ats.config;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;



public class AppConfig {
  private static PropertiesConfiguration configuration = null;
  static {
    try {
      configuration = new PropertiesConfiguration("./src/main/resources/config.pros");
      configuration.setReloadingStrategy(new FileChangedReloadingStrategy());
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
  }
  
  public static synchronized String getProperty(final String key) {
    return (String) configuration.getProperty(key);
  }
  public static synchronized String getProperty( String key, String defaultValue){
    String value = getProperty(key);
    return value != null ? value : defaultValue;
  }

  public static void main(String[] args) {
    while (true) {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println(AppConfig.getProperty("user","defaultUser"));
    }
  }
}


