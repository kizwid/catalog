package sandkev.web;


import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: sandkev
 * Date: 2012-10-31
 */
/*
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:spring/jdbc.spring.xml"
        })
*/
public class ControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(ControllerTest.class);
    public static final int PORT = 9195;
    public static final String HOST = "localhost";
    public static final String CONTEXTPATH = "/tesla-key-mapper";
    public static final String URL_BASE = "http://" + HOST + ":" + PORT + CONTEXTPATH +"/control?";
    private Executor executor;
    private static WebServer webServer;
    private StringWriter sw;
    private WebClient webClient;

/*
    @Resource
    private DataSource dataSourceGrimis;
*/


    @Before
    public void setUp() throws Exception {

        //just start for 1st test (or reuse running instance if using jetty:run)
        if(checkPort(PORT)){
            //this is only used to run the test manually
            executor = Executors.newSingleThreadExecutor();
            CountDownLatch latch = new CountDownLatch(1);
            webServer = new WebServer(PORT,CONTEXTPATH,latch); //,"src/test/resources/WEB-INF"
            executor.execute(webServer);
            latch.await();
            System.out.println("****** started webserver **********");
            System.out.println(URL_BASE);
        }

        webClient = new WebClient();
        setCredentials(webClient, "t", "t");
        webClient.setJavaScriptEnabled(false);

    }

    @Test
    public void canLoadDashboard() throws Exception {
        final HtmlPage page = webClient.getPage(URL_BASE + "Action=Dashboard&User=unitTest");
        assertEquals("Tesla Market Data Key Mapper", page.getTitleText());

        final String pageAsXml = page.asXml();
        assertTrue(pageAsXml.contains("function initDashboard()"));
        //assertTrue(pageAsXml.contains("var data = [{\"Id\":1,\"Business\":\"GED\"")); //some data

        final String pageAsText = page.asText();
        //assertTrue(pageAsText.contains("Id"));
        //assertTrue(pageAsText.contains("Business"));


        System.out.println("*****\n\npage as text\n" + pageAsText);

        webClient.closeAllWindows();
    }

    @Test
    public void runForever() throws InterruptedException {

/*
        KeyMappingDAOSql dao = new KeyMappingDAOSql(dataSourceGrimis);
        {
            KeyMapping keyMapping = createMapping();
            dao.write(keyMapping);
            System.out.println(dao.findById(keyMapping.getId()));
        }

        for (KeyMapping keyMapping : (List<KeyMapping>)dao.findAllByIsTemplate(true)) {
            System.out.println(keyMapping);
        }
*/


        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }

/*
    @Test
    public void canUpdateRecords() throws Exception {
//        //this only works when we write response via model + view handler
//        setCredentials(webClient, "admin", "admin");
//        final HtmlPage page = webClient.getPage(URL_BASE + "Action=Update&User=unitTest&id=1&business=FOO&handleType=TEST&handleTemplate=TEST&version=1&isTemplate=Y&isDeleted=N&analyticMode=FOO&analyticType=BAR&analyticVersion=&assetNames=&assetNamesAlias=&businessDate=&dataMode=&domesticCurrency=&foreignCurrencies=&holidayCities=&methodology=&objectSubType=&objectType=&listItem=a&listItem=b&listItem=c&listItem=d&listItem=e");
//        final String pageAsXml = page.asXml();
//        assertTrue(pageAsXml.contains("{\"Id\":\"1\",\"Business\":\"FOO\"")); //some data

        StringWriter sw = new StringWriter();
        get(URL_BASE + "Action=Update&User=unitTest&id=1&business=FOO&handleType=TEST&handleTemplate=TEST&version=1&isTemplate=Y&isDeleted=N&analyticMode=FOO&analyticType=BAR&analyticVersion=&assetNames=&assetNamesAlias=&businessDate=&dataMode=&domesticCurrency=&foreignCurrencies=&holidayCities=&methodology=&objectSubType=&objectType=&listItem=a&listItem=b&listItem=c&listItem=d&listItem=e",
                new PrintWriter(sw));
        String page = sw.toString().trim();
        logger.info(page.substring(0, Math.min(500, page.length())));
        assertTrue(page.contains("{\"Id\":\"1\",\"Business\":\"FOO\"")); //some data
    }

    @Test(expected = FailingHttpStatusCodeException.class)
    public void throwsExceptionWhenTryToUpdateStaleRecord() throws Exception {
        final HtmlPage page = webClient.getPage(URL_BASE + "Action=Update&User=unitTest&id=1&business=FOO&handleType=TEST&handleTemplate=TEST&version=1&isTemplate=Y&isDeleted=N&analyticMode=FOO&analyticType=BAR&analyticVersion=&assetNames=&assetNamesAlias=&businessDate=&dataMode=&domesticCurrency=&foreignCurrencies=&holidayCities=&methodology=&objectSubType=&objectType=&listItem=a&listItem=b&listItem=c&listItem=d&listItem=e");
        //second attempt should fail
        webClient.getPage(URL_BASE + "Action=Update&User=unitTest&id=1&business=FOO&handleType=TEST&handleTemplate=TEST&version=1&isTemplate=Y&isDeleted=N&analyticMode=FOO&analyticType=BAR&analyticVersion=&assetNames=&assetNamesAlias=&businessDate=&dataMode=&domesticCurrency=&foreignCurrencies=&holidayCities=&methodology=&objectSubType=&objectType=&listItem=a&listItem=b&listItem=c&listItem=d&listItem=e");
    }

    @Test(expected = FailingHttpStatusCodeException.class)
    public void throwsExceptionWhenTryToUpdateWithoutAuthority() throws Exception {
        setCredentials(webClient, "readonly", "readonly");
        final HtmlPage page = webClient.getPage(URL_BASE + "Action=Update&User=unitTest&id=2&business=GED&handleType=CURVE&handleTemplate=TEST&version=1&isTemplate=Y&isDeleted=N&analyticMode=FOO&analyticType=BAR&analyticVersion=&assetNames=&assetNamesAlias=&businessDate=&dataMode=&domesticCurrency=&foreignCurrencies=&holidayCities=&methodology=&objectSubType=&objectType=&listItem=a&listItem=b&listItem=c&listItem=d&listItem=e");
    }
*/

    private static void setCredentials(WebClient webClient, String username, String password){
        String base64encodedUsernameAndPassword = base64Encode(username + ":" + password);
        webClient.removeRequestHeader("Authorization");
        webClient.addRequestHeader("Authorization", "Basic " + base64encodedUsernameAndPassword);
    }

    private static String base64Encode(String stringToEncode){
        return DatatypeConverter.printBase64Binary(stringToEncode.getBytes());
    }


    //sometimes we run this using jetty:run
    private boolean checkPort(int port) {
        boolean success;
        try {
            Server server = new Server(port);
            server.start();
            success = true;
            server.stop();
        }   catch (Exception e){
            success=false;
        }
        return success;
    }

    public static void get(String sUrl, PrintWriter pw) throws Exception {

        URL url = new URL(sUrl);

        // open url connection
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // set up url connection to get retrieve information back
        con.setRequestMethod( "GET" );
        con.setDoInput( true );

        // stuff the Authorization request header
        String encodedPassword = ( "t" + ":" + "t" );
        con.setRequestProperty( HttpHeaders.AUTHORIZATION,
                "Basic " + base64Encode(encodedPassword) );

        //read response
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        for (; ; ) {
            String sLine = br.readLine();
            if (sLine == null) break;
            pw.println(sLine);
        }
        br.close();
        pw.flush();
    }

}
