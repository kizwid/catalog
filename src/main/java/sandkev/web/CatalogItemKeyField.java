package sandkev.web;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents fields in the catalog.
 */
public enum CatalogItemKeyField {
    Id,
    Name,
    Season,
    Article,
    IsDeleted,
    IsTemplate,
    LastUpdatedBy,
    LastUpdatedTime,
    Version,
    getAsStringArray,
    getFieldsAsDelimitedString;

    public static String[] getAsStringArray() {
        List<String> values = new ArrayList<String>();
        for (CatalogItemKeyField field : CatalogItemKeyField.values()) {
            values.add(field.name());
        }
        String[] result = new String[values.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = values.get(i);

        }
        return result;
    }
    public static String getFieldsAsDelimitedString() {
        return StringUtils.join(getAsStringArray(),",");
    }

    public static CatalogItemKeyField fromFieldName(String key) {
        return CatalogItemKeyField.valueOf(key);  //To change body of created methods use File | Settings | File Templates.
    }
}
