package sandkev.shared.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 */
public abstract class GenericDaoAbstract<T extends Identifiable<ID>, ID extends Serializable> implements GenericDao<T,ID> {

    protected final  Class< T > type;

    public GenericDaoAbstract(){
        Type t = getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) t;
        type = (Class) pt.getActualTypeArguments()[0];

    }

}
