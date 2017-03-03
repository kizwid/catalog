package sandkev.shared.dao;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: kevsanders
 * Date: 26/12/2012
 * Time: 18:07
 * To change this template use File | Settings | File Templates.
 */
public class TableAndSchemaExtractor {

    /**
     * A regular expression that is used to get the table name
     * from a SQL 'select' statement.
     * This  pattern matches a string that starts with any characters,
     * followed by the case-insensitive word 'from',
     * followed by a table name of the form 'foo' or 'schema.foo',
     * followed by any number of remaining characters.
     */
    private static final Pattern TABLE_MATCH_PATTERN =
            Pattern.compile(".*\\s+from\\s+(\\w+(\\.\\w+)?).*",
            Pattern.CASE_INSENSITIVE);


    public static void main(String[] args) {
        getTableNameFromSql("select * from public.test t where t.id = 0");
        getTableNameFromSql("SELECT * FROM PUBLIC.TEST t where t.id = 0");
        getTableNameFromSql("select * from test t where t.id = 0");
        getTableNameFromSql("select a,b,c as d,e f from public.test t where t.id = (select id from other");
    }

    public static String getTableNameFromSql(String query) {
        Matcher m = TABLE_MATCH_PATTERN.matcher(query);
        if(m.matches()){
            return m.group(1);
        }else {
            return "";
        }

    }


}
