package sandkev.web;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Kevin
 * Date: 01/10/13
 * Time: 23:43
 * To change this template use File | Settings | File Templates.
 */
public enum TeslaMarketDataKeyField {
    AnalyticMode,
    AnalyticType,
    AnalyticVersion,
    AssetNames,
    AssetNamesAlias,
    Business,
    BusinessDate,
    DataMode,
    DomesticCurrency,
    ForeignCurrencies,
    HandleTemplate,
    HandleType,
    HolidayCities,
    Id,
    IsDeleted,
    IsTemplate,
    LastUpdatedBy,
    LastUpdatedTime,
    Methodology,
    ObjectSubType,
    ObjectType,
    Version,
    getAsStringArray,
    getFieldsAsDelimitedString;

    public static String[] getAsStringArray() {
        List<String> values = new ArrayList<String>();
        for (TeslaMarketDataKeyField field : TeslaMarketDataKeyField.values()) {
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

    public static TeslaMarketDataKeyField fromFieldName(String key) {
        return TeslaMarketDataKeyField.valueOf(key);  //To change body of created methods use File | Settings | File Templates.
    }
}
