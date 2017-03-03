package sandkev.shared.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kevsanders
 * Date: 21/12/2012
 * Time: 23:32
 * To change this template use File | Settings | File Templates.
 */
public abstract class GenericDaoAbstractSpringJdbc<T extends Identifiable<ID>, ID extends Serializable> extends GenericDaoAbstract<T,ID> implements GenericDao<T,ID> {

    private final static Logger logger = LoggerFactory.getLogger(GenericDaoAbstractSpringJdbc.class);
    protected final JdbcTemplate jdbcTemplate;
    protected final String sqlSelectAll;
    protected final String sqlSelectUniqueEntity;
    protected final String groupBy;
    protected final String orderBy;
    protected final String databaseDialect;
    protected final String schema;

    public GenericDaoAbstractSpringJdbc(DataSource dataSource, String sqlSelectAll, String... idColumns){
        this(dataSource, sqlSelectAll, "", "", idColumns);
    }
    public GenericDaoAbstractSpringJdbc(DataSource dataSource, String sqlSelectAll, String groupBy, String orderBy, String... idColumns){
        super();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.groupBy = groupBy;
        this.orderBy = orderBy;
        this.schema = extractSchema(TableAndSchemaExtractor.getTableNameFromSql(sqlSelectAll));
        this.databaseDialect = readDatabaseDialect(dataSource);
        this.sqlSelectAll = dialectFriendlySql(sqlSelectAll);
        StringBuilder sb = new StringBuilder(this.sqlSelectAll);
        sb.append(" where ");
        boolean first = true;
        for(String field: idColumns){
            if(!first){
                sb.append(" and ");
            }
            sb.append(field).append(" = ? ");
        }
        sb.append(groupBy).append(orderBy);
        this.sqlSelectUniqueEntity = sb.toString();

    }

    //@Override
    public void delete(SimpleCriteria criteria) {
        int fromPos = fromPosition();
        String sql = "delete " + sqlSelectUniqueEntity.substring(fromPos) +
                criteria.resolveWhereClause();
        jdbcTemplate.update(sql);
    }

    //@Override
    public void deleteById(ID primaryKey) {
        int fromPos = fromPosition();
        String sql = "delete " + sqlSelectUniqueEntity.substring(fromPos);
        int count = jdbcTemplate.update(sql, primaryKey);
        if(count != 1){
            throw new IllegalStateException("expected to delete 1 item but deleted " + count);
        }
    }

   // @Override
    public T findById(ID primaryKey) {
        List<T> entities = jdbcTemplate.query(sqlSelectUniqueEntity,
                idToObjectArray(primaryKey),
                createRowMapper());
        if(entities.size() > 1){
            throw new IllegalStateException("found multiple values with same primaryKey");
        }
        return entities.size() == 0 ? null : entities.get(0);
    }

    protected abstract RowMapper<T> createRowMapper();


   // @Override
    public List<T> find(SimpleCriteria criteria) {
        final String sql = sqlSelectAll +
                criteria.resolveWhereClause() +
                groupBy +
                orderBy;
        try{
            return jdbcTemplate.query(sql, createRowMapper());
        }catch (DataAccessException e){
            logger.warn("failed to find[" + sql + "] with error: ", e);
            return Collections.emptyList();
        }
    }

    //@Override
    public void saveAll(Collection<T> entities) {
        for (T entity : entities) {
            save(entity);
        }
    }

    //@Override
    public boolean exists(ID primaryKey) {
        int fromPos = fromPosition();
        String sql = "select count(*) " + sqlSelectUniqueEntity.substring(fromPos);
        int count = jdbcTemplate.update(sql, primaryKey);
        return count > 0;
    }

    private Object[] idToObjectArray(ID id) {
   		if (id instanceof Object[])
   			return (Object[]) id;
   		else
   			return new Object[]{id};
   	}

    private int fromPosition() {
        return sqlSelectUniqueEntity.toLowerCase().indexOf(" from ");
    }

    private String extractSchema(String tableNameFromSql) {
        int dot = tableNameFromSql.indexOf(".");
        if(dot >= 0){
            return tableNameFromSql.substring(0, dot);
        }else {
            return "";
        }
    }

    private String readDatabaseDialect(DataSource dataSource){
        String dialect = "Unknown";
        Connection conn = null;
        try{
            conn = dataSource.getConnection();
            dialect = conn.getMetaData().getDatabaseProductName();
        } catch (Exception e){
            logger.error("failed to read DatabaseProductName", e);
        }finally {
            try{conn.close();}catch (Exception ex){}
        }
        return dialect;
    }

    public String dialectFriendlySql(String sql){
        if("HSQL Database Engine".equals(databaseDialect)){
            String fixed = sql.replace(schema+".", "");
            fixed = fixed.replace("to_timestamp(?,'YYYY-MM-DD HH24:MI:SS.FF')", "CAST(? AS TIMESTAMP)");
            return fixed;
        }else {
            return sql;
        }

    }

    private String getQueryClauses(final Map<String, Object> params,
                                   final Map<String, Object> orderParams) {
        final StringBuilder queryString = new StringBuilder();
        if ((params != null) && !params.isEmpty()) {
            queryString.append(" where ");
            for (final Iterator<Map.Entry<String, Object>> it = params
                    .entrySet().iterator(); it.hasNext(); ) {
                final Map.Entry<String, Object> entry = it.next();
                if (entry.getValue() instanceof Boolean) {
                    queryString.append(entry.getKey()).append(" is ")
                            .append(entry.getValue()).append(" ");
                } else {
                    if (entry.getValue() instanceof Number) {
                        queryString.append(entry.getKey()).append(" = ")
                                .append(entry.getValue());
                    } else {
                        // string equality
                        queryString.append(entry.getKey()).append(" = '")
                                .append(entry.getValue()).append("'");
                    }
                }
                if (it.hasNext()) {
                    queryString.append(" and ");
                }
            }
        }
        if ((orderParams != null) && !orderParams.isEmpty()) {
            queryString.append(" order by ");
            for (final Iterator<Map.Entry<String, Object>> it = orderParams
                    .entrySet().iterator(); it.hasNext(); ) {
                final Map.Entry<String, Object> entry = it.next();
                queryString.append(entry.getKey()).append(" ");
                if (entry.getValue() != null) {
                    queryString.append(entry.getValue());
                }
                if (it.hasNext()) {
                    queryString.append(", ");
                }
            }
        }
        return queryString.toString();
    }

}

