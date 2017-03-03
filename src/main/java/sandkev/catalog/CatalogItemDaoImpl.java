package sandkev.catalog;

import org.springframework.jdbc.core.RowMapper;
import sandkev.shared.dao.GenericDaoAbstractSpringJdbc;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by kevin on 09/05/2015.
 */
public class CatalogItemDaoImpl extends GenericDaoAbstractSpringJdbc<CatalogItem, Long> implements CatalogItemDao {

    public CatalogItemDaoImpl(DataSource dataSource){
        super(dataSource,
                "select " +
                        "*" +
                " from public.catalog c",
                "catalog_id"
                );
    }

    @Override
    protected RowMapper<CatalogItem> createRowMapper() {
        return new RowMapper<CatalogItem>() {
            //@Override
            public CatalogItem mapRow(ResultSet rs, int i) throws SQLException {
                return new CatalogItem(
                         rs.getLong("catalog_id")
                        ,rs.getString("catalog_name")
                        ,rs.getString("season")
                        ,rs.getString("magazine")
                        ,rs.getString("article")
                );
            }
        };
    }

    //@Override
    public void save(CatalogItem entity) {
        if(entity.getId() == -1){
            CatalogItem existing = findById(entity.getId());
            if(existing!=null){
                entity.setId(existing.getId());
            }else {
                entity.setId(nextId());

                jdbcTemplate.update(
                        dialectFriendlySql("insert into catalog (catalog_id, catalog_name, season, magazine, article) values( ?, ?, ?, ?, ?)")
                        ,entity.getId()
                        ,entity.getName()
                        ,entity.getSeason()
                        ,entity.getMagazine()
                        ,entity.getArticle()
                );
            }
        }
    }

    private Long nextId() {
        return jdbcTemplate.queryForObject(dialectFriendlySql("select catalog_seq.nextval from dual"), Long.class);
    }

}
