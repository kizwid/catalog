package sandkev.shared.dao;

import java.util.LinkedList;
import java.util.List;

/**
 * User: kizwid
 * Date: 2012-02-28
 */
public class SimpleCriteria {
    
    //TODO: add nestable criteria with predicates
    //for the time being we will just AND them
    private List<SimpleCriterion> criteria;
    private final List<Object> values;

    public final static SimpleCriteria EMPTY_CRITERIA = new SimpleCriteria();

    public SimpleCriteria() {
        this.criteria = new LinkedList<SimpleCriterion>();
        values = new LinkedList<Object>();
    }
    
    public void addCriterion(SimpleCriterion criterion){
        criteria.add(criterion);
    }

    public List<SimpleCriterion> getCriteria() {
        return criteria;
    }

    public String resolveWhereClause(){
        List<Object> values = new LinkedList<Object>();
        StringBuilder sb = new StringBuilder();
        if( criteria.size() > 0){
            sb.append(" where ");
        }
        for (SimpleCriterion criterion : criteria) {
            if(sb.length() > 7){
                sb.append(" and "); //only support AND
            }
            sb.append(criterion.resolveSqlClause());
        }
        
        return criteria.size() > 0? sb.toString():"";
        
    }

    public String resolvePreparedWhereClause(){
        StringBuilder sb = new StringBuilder();
        values.clear();
        if( criteria.size() > 0){
            sb.append(" where ");
        }
        for (SimpleCriterion criterion : criteria) {
            if(sb.length() > 7){
                sb.append(" and "); //only support AND
            }
            sb.append(criterion.resolvePreparedSqlClause());
            values.add(criterion.resolvePreparedSqlValue());
        }
        return criteria.size() > 0? sb.toString():"";
    }
    
    public Object[] resolvePreparedWhereValues(){
        return values.toArray();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleCriteria)) return false;

        SimpleCriteria that = (SimpleCriteria) o;

        if (criteria != null ? !criteria.equals(that.criteria) : that.criteria != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = criteria != null ? criteria.hashCode() : 0;
        return result;
    }

    @Override
    public String toString() {
        return "sandkev.shared.dao.SimpleCriteria{" +
                "criteria=" + criteria +
                '}';
    }
}
