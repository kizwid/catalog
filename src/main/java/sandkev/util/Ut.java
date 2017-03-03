package sandkev.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by kevin on 09/05/2015.
 */
public class Ut {

    public static InputStream is(String resource) throws IOException{
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    }

    public static Properties load(String resource) throws IOException{
        InputStream is = is(resource);
        try {
            Properties properties = new Properties();
            properties.load(is);
            return properties;
        }finally {
            close(is);
        }
    }

    private static void close(Closeable closeable) {
        try {
            if(closeable!=null)closeable.close();
        } catch (IOException ignore) {
        }
    }

    public static File file(String resource) {
        return new File(Thread.currentThread().getContextClassLoader().getResource(resource).getFile());
    }
}


