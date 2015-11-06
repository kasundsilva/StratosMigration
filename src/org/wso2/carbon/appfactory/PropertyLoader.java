package org.wso2.carbon.appfactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

/**
 * Created by punnadi on 8/19/15.
 */
public class PropertyLoader {
    private static final Log log = LogFactory.getLog(PropertyLoader.class);

    private final Properties configProp = new Properties();

    private static PropertyLoader propertyLoader = null;

    private PropertyLoader() {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("migration.properties");
        try {
            configProp.load(in);
            System.out.println("Read all properties from file");
        } catch (IOException e) {
            String msg = "Error occurred while loading properties file";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public static PropertyLoader getInstance() {
        if (propertyLoader == null) {
            propertyLoader = new PropertyLoader();
        }
        return propertyLoader;
    }

    public String getProperty(String key) {
        return configProp.getProperty(key);
    }

    public Set<String> getAllPropertyNames() {
        return configProp.stringPropertyNames();
    }

    public boolean containsKey(String key) {
        return configProp.containsKey(key);
    }
}
