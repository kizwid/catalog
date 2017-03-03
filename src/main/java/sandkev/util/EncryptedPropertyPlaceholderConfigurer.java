package sandkev.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jndi.JndiTemplate;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringValueResolver;

import javax.naming.NamingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;

/**
 * User: kizwid
 * Date: 2012-04-26
 */
public class EncryptedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    private final static Logger logger = LoggerFactory.getLogger(EncryptedPropertyPlaceholderConfigurer.class);

    //cryptography key is passed in by what ever starts system (or sure-fire plugin for automated tests)
    private final static String ENCRYPT_KEY = System.getProperty("encrypt.key", "EFEB34A0247110600B98B9CEBE96AB7E"); //default value so I can run test manually


    //allow dbMaintain to resolve placeHolders
    public void processProperties(Properties props) throws IOException {
        Properties p = new Properties();
        super.loadProperties(p);
        StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(p);
        Enumeration propertyNames = props.propertyNames();
      		while (propertyNames.hasMoreElements()) {
      			String propertyName = (String) propertyNames.nextElement();
      			String propertyValue = props.getProperty(propertyName);
      			String convertedValue = valueResolver.resolveStringValue(propertyValue);
                  //update resolved values
      			if (convertedValue != null) {
      				props.setProperty(propertyName, convertedValue);
      			}
      		}
    }

    //for Jndi based properties
    private String jndiPrefix = "java:comp/env/";
    private JndiTemplate jndiTemplate = new JndiTemplate();
    private Cryptography crypt;

    public EncryptedPropertyPlaceholderConfigurer(){
        super();
        try {
            this.crypt = new Cryptography("secret");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    protected  String convertPropertyValue(String originalValue){
        final String ENCRYPT_MARKER_START="ENC(";
        final String ENCRYPT_MARKER_END=")";
        if(originalValue.startsWith(ENCRYPT_MARKER_START) && originalValue.endsWith(ENCRYPT_MARKER_END)){
            logger.debug("decrypting value [{}]", originalValue);
            try {
                final String encrypted =
                        originalValue.substring(ENCRYPT_MARKER_START.length(),
                                originalValue.length()-ENCRYPT_MARKER_END.length());

                final String decrypted;
                decrypted = crypt.decrypt(encrypted);
                return decrypted;
            } catch (Exception e) {
                logger.error("unable to decrypt value [{}]", new Object[]{originalValue}, e);
                throw new IllegalArgumentException(String.format("unable to decrypt value [%s]", originalValue),e);
            }
        } else {
            return originalValue;
        }
    }

    /*if a jndi property has been set then use it*/
    @Override
    protected String resolvePlaceholder(String placeholder, Properties props) {
        String value;
        value = resolveJndiPlaceholder(placeholder);
        if (value == null) {
            value = super.resolvePlaceholder(placeholder, props);
        }
        return value;
    }

    private String resolveJndiPlaceholder(String placeholder) {
        try {
            return jndiTemplate.lookup(jndiPrefix + placeholder, String.class);
        } catch (NamingException e) {
            // ignore
        }
        return null;
    }

    public void setLocation(Resource location) {
        super.setLocation(fixLocation(location));
    }

    public void setLocations(Resource[] locations) {
        //for each location that still has unresolved placeholders
        //look for the appropriate value in jndi
        Resource[] fixedLocations = new Resource[locations.length];
        for (int n = 0; n < locations.length; n++) {
            fixedLocations[n]=fixLocation(locations[n]);
        }
        super.setLocations(fixedLocations);
    }

    private Resource fixLocation(Resource location) {
        if( location instanceof ClassPathResource){
            String path = ((ClassPathResource)location).getPath();
            String fixedPath = parseStringValue(path, new Properties(), new HashSet());
            location = new ClassPathResource(fixedPath);
        }
        return location;
    }

    //allow dbMaintain to resolve placeholders
    public void processProperties(Properties props, Properties p) throws IOException {
        super.loadProperties(p);
        StringValueResolver resolver = new PlaceholderResolvingStringValueResolver(p);
        Enumeration propertyNames = props.propertyNames();
        while (propertyNames.hasMoreElements()){
            String propertyName = (String)propertyNames.nextElement();
            String propertyValue = props.getProperty(propertyName);
            String convertedValue = resolver.resolveStringValue(propertyValue);
            if(convertedValue!=null){
                props.setProperty(propertyName, convertedValue);
            }
        }
    }

    private class PlaceholderResolvingStringValueResolver implements StringValueResolver {

   		private final PropertyPlaceholderHelper helper;

   		private final PropertyPlaceholderHelper.PlaceholderResolver resolver;

   		public PlaceholderResolvingStringValueResolver(Properties props) {
   			this.helper = new PropertyPlaceholderHelper(
   					"${", "}", ",", true);
   			this.resolver = new PropertyPlaceholderConfigurerResolver(props);
   		}

   		public String resolveStringValue(String strVal) throws BeansException {
   			String value = this.helper.replacePlaceholders(strVal, this.resolver);
   			return (value.equals(null) ? null : value);
   		}
   	}
    private class PropertyPlaceholderConfigurerResolver implements PropertyPlaceholderHelper.PlaceholderResolver {

   		private final Properties props;

   		private PropertyPlaceholderConfigurerResolver(Properties props) {
   			this.props = props;
   		}

   		public String resolvePlaceholder(String placeholderName) {
   			return EncryptedPropertyPlaceholderConfigurer.super.resolvePlaceholder(placeholderName, props, SYSTEM_PROPERTIES_MODE_FALLBACK);
   		}
   	}


    private class Cryptography {
        public Cryptography(String secret)throws UnsupportedEncodingException{
            //TODO:implement
        }

        public String decrypt(String encrypted) {
            return encrypted;//TODO: implement
        }
    }
}
