package sandkev.shared.dao;

import java.util.Collections;
import java.util.List;

/**
 * User: kizwid
 * Date: 2012-02-27
 */
public class SimpleCriterion {

    public static enum Operator{
        EQUALS,STARTSWITH,CONTAINS,ENDSWITH,
        NOTEQUALS,NOTSTARTSWITH,NOTCONTAINS,NOTENDSWITH
    } //IN,GREATERTHAN,LESSTHAN,GREATERTHANEQUALS,LESSTHANEQUALS
    public static enum DataType {STRING,NUMBER,DATE,UNKNOWN}
    private final String field;
    private final Operator operator;
    private final Object value;

    private final String operatorString;
    private final Object useValue;
    private final DataType dataType;
    private final boolean caseSensitive;

    public static final List<SimpleCriterion> EMPTY_CRITERIA = Collections.emptyList();

    public SimpleCriterion(String field, Operator operator, Object value) {
        this(field, operator, value, true, DataType.UNKNOWN);
    }
    public SimpleCriterion(String field, Operator operator, Object value, boolean caseSensitive, DataType dataType) {
        this.operator = operator;
        this.value = value;
        this.caseSensitive = caseSensitive;
        this.dataType = dataType;
        if(this.dataType.equals(DataType.STRING) && caseSensitive == false){
            this.field = "upper(" + field + ")";
        }else{
            this.field = field;
        }

        switch (operator){
            case EQUALS:
                operatorString ="=";
                useValue = value;
                break;
            case STARTSWITH:
                operatorString ="like";
                useValue = value + "%";
                break;
            case ENDSWITH:
                operatorString ="like";
                useValue = "%" + value;
                break;
            case CONTAINS:
                operatorString ="like";
                useValue =  "%" + (value == null || value.toString().length() == 0? "":  value + "%");  //prevent %%
                break;
            case NOTEQUALS:
                operatorString ="!=";
                useValue = value;
                break;
            case NOTSTARTSWITH:
                operatorString ="not like";
                useValue = value + "%";
                break;
            case NOTENDSWITH:
                operatorString ="not like";
                useValue = "%" + value;
                break;
            case NOTCONTAINS:
                operatorString ="not like";
                useValue =  "%" + (value == null || value.toString().length() == 0? "": value + "%");  //prevent %%
                break;
            default:
                throw new IllegalArgumentException("Unhandled operator "+ operator);
        }


    }

    public String getField() {
        return field;
    }

    public Operator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }
    
    public String resolvePreparedSqlClause(){
        return field + " " + operatorString + " ? ";
    }

    public String resolveSqlClause(){
        return field + " " + operatorString + " " + wrapSqlValue(useValue);
    }

    private String wrapSqlValue(Object useValue) {
        String wrappedValue;
        switch (dataType) {
            //TODO: currently we only support String
            //case DATE:
            //    wrappedValue = "'" + FormatUtil.formatSqlDateTime((Date) value) + "'";
            //    break;
            //case NUMBER:
            //    wrappedValue = String.valueOf(useValue);
            //    break;
            case DATE:
            case UNKNOWN:
            case NUMBER:
            case STRING:
                wrappedValue = "'" + useValue.toString().replace("'","''") + "'";
                if(this.dataType.equals(DataType.STRING) && caseSensitive == false){
                    wrappedValue = "upper(" + wrappedValue + ")";
                }
                break;
            default:
                throw new IllegalArgumentException("Unhandled dataType "+ dataType);
        }
        return wrappedValue;
    }

    public Object resolvePreparedSqlValue(){
        return useValue;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleCriterion)) return false;

        SimpleCriterion that = (SimpleCriterion) o;

        if (field != null ? !field.equals(that.field) : that.field != null) return false;
        if (operator != null ? !operator.equals(that.operator) : that.operator != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "sandkev.shared.dao.SimpleCriterion{" +
                "field='" + field + '\'' +
                ", operator='" + operator + '\'' +
                ", value=" + value +
                '}';
    }

}
