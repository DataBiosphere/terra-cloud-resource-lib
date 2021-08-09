package bio.terra.cloudres.azure.resouce.manager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {

    String propFileName;
    public PropertyReader(String propFileName) {
        this.propFileName = propFileName;
    }

    public Properties getProperties() throws IOException {
        String result = "";
        Properties prop = new Properties();

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found. Check the resources folder.");
        }

        return prop;
    }
}
