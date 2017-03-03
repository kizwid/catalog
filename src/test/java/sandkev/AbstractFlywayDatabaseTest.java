package sandkev;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.flywaydb.core.Flyway;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:catalog/flyway.spring.xml"})
public abstract class AbstractFlywayDatabaseTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractFlywayDatabaseTest.class);

    @Resource
    protected  DataSource dataSource;
    @Resource
    protected Flyway flyway;

    @Resource
    protected JdbcTemplate jdbcTemplate;

    private static Connection conn = null;

    @Before
    public  void init() throws SQLException, IOException, URISyntaxException {
        //flyway.migrate();
        //JdbcTemplate jdbcTemplate = new JdbcTemplate((DataSource) dataSource.getConnection());
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("select * from INFORMATION_SCHEMA.TABLES");
        conn = dataSource.getConnection();
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
