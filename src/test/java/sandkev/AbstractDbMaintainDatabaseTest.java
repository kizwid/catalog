package sandkev;

import org.apache.commons.dbcp.BasicDataSource;
import org.dbmaintain.DbMaintainer;
import org.dbmaintain.MainFactory;
import org.dbmaintain.structure.clean.DBCleaner;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import sandkev.util.EncryptedPropertyPlaceholderConfigurer;
import sandkev.util.Ut;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:catalog/dao.spring.xml"})
public abstract class AbstractDbMaintainDatabaseTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDbMaintainDatabaseTest.class);

    protected static EncryptedPropertyPlaceholderConfigurer commonProperties;
    protected static DataSource dataSource;
    private static Connection conn = null;

    protected static DbMaintainer dbMaintainer;
    protected static DBCleaner dbCleaner;
    protected static MainFactory dbMaintainMainFactory;


    static {
        try {
            String resource = "catalog.properties";
            Properties dsProps = Ut.load(resource);
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName(dsProps.getProperty("jdbc.driver"));
            ds.setUrl(dsProps.getProperty("jdbc.url"));
            ds.setUsername(dsProps.getProperty("jdbc.username"));
            ds.setPassword(dsProps.getProperty("jdbc.password"));
            ds.setValidationQuery(dsProps.getProperty("jdbc.validationQuery"));
            dataSource = ds;

            org.springframework.core.io.Resource[] locations =
                    new org.springframework.core.io.Resource[]{
                            new ClassPathResource(resource)
                    };

            commonProperties = new EncryptedPropertyPlaceholderConfigurer();
            commonProperties.setLocations(locations);
            commonProperties.setLocalOverride(true);

            Properties properties = Ut.load("catalog/dbmaintain.properties");
            Properties p = new Properties();
            p.setProperty("baseDir", new File(Ut.file(".").getParentFile(),"classes").getCanonicalPath().replace("%20", " ") + "/");

            commonProperties.processProperties(properties, p);
            dbMaintainMainFactory = new MainFactory(properties);
            conn = dataSource.getConnection();

        }catch (Exception e){
            logger.error("Failed to initialise class", e);
        }

    }

    @Before
    public void setup() throws SQLException, IOException, URISyntaxException {
        initDataBase();
    }

    protected static void initDataBase() throws URISyntaxException, IOException, SQLException {
        createDBMainainer().updateDatabase(false);
        createDBCleaner().cleanDatabase();
    }
    private static DBCleaner createDBCleaner() throws URISyntaxException, IOException, SQLException {
        dbCleaner = dbMaintainMainFactory.createDBCleaner();
        return dbCleaner;
    }

    private static DbMaintainer createDBMainainer() throws URISyntaxException, IOException, SQLException {
        dbMaintainer = dbMaintainMainFactory.createDbMaintainer();
        return dbMaintainer;
    }


    @AfterClass
    public static void tearDown() throws Exception {

        if(conn==null){
            return;
        }

        writedatabaseToFile();

    }

    private static void writedatabaseToFile() throws DatabaseUnitException, SQLException, IOException {
        //capture final state of database
        IDatabaseConnection connection = new DatabaseConnection(conn);
        DatabaseConfig config = connection.getConfig();
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
        // full database export
        IDataSet fullDataSet = connection.createDataSet();
        FlatXmlDataSet.write(fullDataSet, new FileOutputStream("target/full-dataset-dbmaintain.xml"));
    }

}
