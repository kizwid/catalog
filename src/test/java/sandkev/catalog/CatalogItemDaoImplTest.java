package sandkev.catalog;

import org.junit.Test;
import sandkev.AbstractDatabaseTest;
import sandkev.AbstractDbMaintainDatabaseTest;
import sandkev.AbstractFlywayDatabaseTest;

import static org.junit.Assert.*;

/**
 * Created by kevsa on 06/02/2017.
 */
public class CatalogItemDaoImplTest
//        extends AbstractDatabaseTest/*AbstractFlywayDatabaseTest*/
//        extends AbstractFlywayDatabaseTest
        extends AbstractDbMaintainDatabaseTest
        {

    @Test
    public void canSaveAndRetrieve() {

        CatalogItemDao dao = new CatalogItemDaoImpl(dataSource);
        CatalogItem catalogItem = new CatalogItem("Kevin", "Summer", "One", "Cool");
        dao.save(catalogItem);

        CatalogItem checkById = dao.findById(catalogItem.getId());
        assertEquals(catalogItem, checkById);

        for (int n = 0; n < 10; n++) {
            dao.save( new CatalogItem("Item-" + n, "a", "b", "c"));
        }

    }

}