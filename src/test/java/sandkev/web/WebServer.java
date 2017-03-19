package sandkev.web;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URLDecoder;
import java.util.concurrent.CountDownLatch;

/**
 * User: sandkev
 * Date: 2012-02-22
 */
public class WebServer implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final String DEFAULT_WEBAPPDIR = "src/main/webapp";
    private final CountDownLatch latch;
    private final Server server;
    private final int httpPort;
    private final String contextPath;
    private final String webAppDir;

    public static final int DEFAULT_PORT = 9191;
    public static final String DEFAULT_CONTEXTPATH = "/catalog";

    public WebServer(int httpPort, String contextPath, CountDownLatch latch) {
        this(httpPort, contextPath, latch, DEFAULT_WEBAPPDIR);
    }

    public WebServer(int httpPort, String contextPath, CountDownLatch latch, String webAppDir) {
        this.latch = latch;
        this.httpPort = httpPort;
        this.contextPath = contextPath;
        this.webAppDir = webAppDir;
        server = new Server();
    }

    public void run() {

        try {
            SelectChannelConnector connector = new SelectChannelConnector();
            connector.setPort(httpPort);
            server.addConnector(connector);

            //TODO: use reliable path to webAppPath
            //final URL warUrl = Thread.currentThread().getContextClassLoader().getResource(WEBAPPDIR);
            File testClasses = new File(URLDecoder.decode(ClassLoader.getSystemClassLoader().getResource(".").getFile()));
            File base = testClasses.getParentFile().getParentFile();
            File webAppDir = new File(base + File.separator + this.webAppDir);
            final String webAppPath = webAppDir.getAbsolutePath();

            WebAppContext webAppContext;
            ResourceHandler resourceHandler;
            if( !webAppDir.exists()){
                throw new IllegalStateException( "missing webApp dir " + webAppDir);
            }

            logger.info("using regular webAppDir from file system [{}]",webAppPath);
            webAppContext = new WebAppContext();
            webAppContext.setDescriptor(webAppPath + "/WEB-INF/web.xml");
            webAppContext.setResourceBase(webAppPath);
            webAppContext.setContextPath(contextPath);
            webAppContext.setParentLoaderPriority(true);

            Constraint constraint = new Constraint();
            constraint.setName(Constraint.__BASIC_AUTH);//__FORM_AUTH  __BASIC_AUTH
            constraint.setRoles(new String[]{"tester","support","admin" });
            constraint.setAuthenticate(true);

            ConstraintMapping cm = new ConstraintMapping();
            cm.setConstraint(constraint);
            cm.setPathSpec("/*");

            SecurityHandler sh = new SecurityHandler();
            final HashUserRealm userRealm = new HashUserRealm("default", webAppPath + "/WEB-INF/realm.properties");
            sh.setUserRealm(userRealm); //tomcat:tomcat,tomcat
//                userRealm.put("t", "t");
//                userRealm.addUserToRole("t", "tester");
//                userRealm.addUserToRole("t", "support");
            sh.setUserRealm(userRealm);
            sh.setConstraintMappings(new ConstraintMapping[]{cm});
            webAppContext.setSecurityHandler(sh);

            resourceHandler = new ResourceHandler();
            resourceHandler.setWelcomeFiles(new String[]{"index.html"});
            resourceHandler.setResourceBase(webAppPath);


            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{resourceHandler, webAppContext, new DefaultHandler()});
            server.setHandler(handlers);

            server.start();
            latch.countDown();//notify caller
            server.join();
            server.stop();
            logger.info("server stopped");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Server Interrupted. Stopping", e);
        } catch (Exception e) {
            logger.error("Server Stopping", e);
        }
    }

    public void stop() throws Exception {
        server.stop();
    }

    public static void main(String[] args) throws InterruptedException {

        String env = System.getProperty("appEnv", "dev");
        System.setProperty("appEnv", env);
        logger.info("starting webServer for appEnv [{}]",env);

        CountDownLatch latch = new CountDownLatch(1);
        WebServer webServer = new WebServer(DEFAULT_PORT, DEFAULT_CONTEXTPATH, latch);
        webServer.run();

    }

}
