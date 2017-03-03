package sandkev.shared.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 */
public interface GenericDao<T extends Identifiable<ID>, ID extends Serializable> {
    void delete(SimpleCriteria criteria);
    void deleteById(ID primaryKey);
    T findById(ID primaryKey);
    List<T> find(SimpleCriteria criteria);
    void save(T entity);
    void saveAll(Collection<T> entities);
    boolean exists(ID primaryKey);
}
