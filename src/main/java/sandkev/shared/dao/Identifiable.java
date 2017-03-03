package sandkev.shared.dao;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: kevsanders
 * Date: 27/01/2013
 * Time: 21:27
 * To change this template use File | Settings | File Templates.
 */
public interface Identifiable<ID extends Serializable> extends Serializable {
    ID getId();
    void setId(ID id);
}
