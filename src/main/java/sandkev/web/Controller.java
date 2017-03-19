package sandkev.web;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import sandkev.catalog.CatalogItem;
import sandkev.catalog.CatalogItemDao;
import sandkev.shared.dao.SimpleCriteria;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.*;

import static sandkev.web.CatalogItemKeyField.getAsStringArray;


/**
 * User: sandkev
 * Date: 2012-02-02
 */
@RequestMapping
public class Controller implements org.springframework.web.servlet.mvc.Controller {

	private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    public static final String ANONYMOUS = "anonymous";
    public static final String VIEW_DASHBOARD = "dashboard";
    public static final String VIEW_LOGIN = "login";
    public static final String PARAM_USER = "User";
    public static final String PARAM_WRITE_ACCESS = "HasWriteAccess";
    public static final String EMPTY_ATTRIBUTE = "-";

    public final String buildVersion;
    public final String buildTimestamp;
    public final String appEnv;
    public final String readAccessRole;
    public final String writeAccessRole;


    public enum Action { Dashboard, Update, Login, GetData }
    public static final String[] COLUMNS = new String[]{"Id","Name","Season","Magazine","Article"};
    private final CatalogItemDao dao;


    public Controller(CatalogItemDao dao, Map<String, String> buildInfo) {
        this.dao = dao;
        buildVersion = buildInfo.get("Build.Version");
        buildTimestamp = buildInfo.get("Build.Timestamp");
        appEnv = buildInfo.get("appEnv");
        readAccessRole = buildInfo.get("readAccessRole");
        writeAccessRole = buildInfo.get("writeAccessRole");
    }

    //@Override
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, Object> model = new HashMap<String, Object>();
        Action action = Action.valueOf(StringUtils.defaultIfEmpty(request.getParameter("Action"), Action.Dashboard.name()));
        
        logger.info("Action [{}] from [{}]", new Object[]{action, request.getRemoteHost()});

        String user = getUser(request, model);
        if(ANONYMOUS.equals(user)){
            //TODO: store intended action and params before redirect to login
            return new ModelAndView(VIEW_LOGIN, model);
        }

        Map<String, String[]> params = request.getParameterMap();
        PrintWriter pw = response.getWriter();
        switch(action) {
            case Dashboard:
                dashboard(request, model);
                return new ModelAndView(VIEW_DASHBOARD, model);
            case GetData:
                try {
                    List<CatalogItem> filtered = new LinkedList<CatalogItem>();
                    List<CatalogItem> catalogItems = dao.find(SimpleCriteria.EMPTY_CRITERIA);
                    if(catalogItems==null){
                        catalogItems = Collections.emptyList();
                    }
                    if(catalogItems.size()==0){
                        for (int n = 0; n < 10; n++) {
                            dao.save( new CatalogItem("Item-" + n, "a", "b", "c"));
                        }
                        catalogItems = dao.find(SimpleCriteria.EMPTY_CRITERIA);
                    }

                    //read filter from request
                    String[] filterColumns = params.get("filterColumn");
                    String[] filterTexts = params.get("filterText");

                    //apply filter (TODO:pass criteria to DAO)
                    if(filterColumns != null){

                        //convert criteria into map
                        Map<String, String> criteria = new HashMap<String, String>();
                        for(int n = 0; n <filterColumns.length; n++){
                            criteria.put(filterColumns[n], filterTexts[n]);
                        }

                        for (CatalogItem entity : catalogItems) {
                            //convert mapping to same format that is used in the UI view
                            Map<String, String> rowData = CatalogItemToSimpleDataMap(entity);
                            boolean matchAll = true;
                            for (Map.Entry<String, String> entry : criteria.entrySet()) {
                                String filter = entry.getValue();
                                String value = rowData.get(entry.getKey());
                                if(!value.contains(filter)){
                                    matchAll = false;
                                    break;
                                }
                            }
                            if(matchAll){
                                filtered.add(entity);
                            }
                        }
                    }else {
                        filtered.addAll(catalogItems);
                    }
                    
                    
                    
                } catch (Throwable e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    pw.print(e);
                    logger.error("GetData failed for request: " + paramMapToString(params));
                }
                return null;
                
            default:
		        dashboard(request, model);
		        return new ModelAndView(VIEW_DASHBOARD, model);  
        }

    }

    private String paramMapToString(Map<String,String[]> parameterMap) {
    	
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            if(sb.length()>0)sb.append(",");
            sb.append(entry.getKey());
            sb.append(ArrayUtils.toString(entry.getValue()));
        }
        return sb.toString();
    }

    private CatalogItem createEntityFromRequestParams(Map<String, String[]> params) {
        
    	Map<String, String> attributes = new HashMap<String, String>();
        for (Map.Entry<String, String[]> ent : params.entrySet()) {
        	
            try{
                String key = ent.getKey();
                CatalogItemKeyField field = CatalogItemKeyField.fromFieldName(key);
                final String value = ent.getValue()[0];
                if(StringUtils.isBlank(value) || EMPTY_ATTRIBUTE.equals(value)){
                    continue;//don't add null/empty fields
                }
                attributes.put(field.name(), value);
            }catch (Exception ex){
                //ignore - not a MarketDataKey field
            }
        }
        
        CatalogItem entity = new CatalogItem(
                Long.valueOf(params.get("Id")[0])
                ,params.get("Name")[0]
                ,params.get("Season")[0]
                ,params.get("Magazine")[0]
                ,params.get("Article")[0]
        );
        return entity;
    }

    private String getUser(HttpServletRequest request, Map<String, Object> model) {
    	
        final Principal userPrincipal = request.getUserPrincipal();
        String user = readParam(model, request, PARAM_USER, ANONYMOUS);
        if(userPrincipal == null || userPrincipal.getName().trim().length() == 0){
            logger.warn("userPrincipal not supplied: using fallback user " + user);
        }else{
            user = userPrincipal.getName();
            model.put(PARAM_USER, user);
            model.put(PARAM_WRITE_ACCESS, new Boolean(request.isUserInRole(writeAccessRole)));
        }
        
        return user;
    }

    private String readParam(Map<String, Object> model, HttpServletRequest request, String param, String defaultValue) {

        String value = (String)model.get(param);
        if(value == null){
            value = request.getParameter(param);
        }
        if( value != null && value.startsWith("$")) {
            value = null;
        }
        if(value == null || value.trim().length() == 0){
            value = defaultValue;
        }
        model.put(param, value);
        return value;
    }

    private Map<String, Object> dashboard(HttpServletRequest request, Map<String, Object> model) throws NoSuchAlgorithmException {

        List<CatalogItem> catalogItems = dao.find(SimpleCriteria.EMPTY_CRITERIA);

        if(catalogItems.size()==0){
            for (int n = 0; n < 10; n++) {
                dao.save( new CatalogItem("Item-" + n, "a", "b", "c"));
            }
            catalogItems = dao.find(SimpleCriteria.EMPTY_CRITERIA);
        }

        model.put("keyMappingColumnJson", toCatalogItemColumnJson(COLUMNS));
        model.put("keyMappings", catalogItems);
        model.put("Version", buildVersion + "[" + buildTimestamp + "]");
        model.put("Env", appEnv);
        
        return model;
    }

    private String toCatalogItemColumnJson(String[] columns) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (String column : columns) {
            if(sb.length() > 1)sb.append(',');
            sb.append('{');
            sb.append("key")
                    .append(':')
                    .append("\"")
                    .append(column)
                    .append("\"");
            sb.append(", allowHTML: true, emptyCellValue: \"" + EMPTY_ATTRIBUTE + "\"");  //,formatter:htmlEncode
            sb.append(", sortable: true");
            sb.append(", className: 'noWrapRow'");
            sb.append(", label:\"").append(column.replaceAll("([A-Z])", " $1")).append("\"");
            sb.append('}');
        }
        return sb.append(']').toString();
    }
    
    private Map<String, String> CatalogItemToSimpleDataMap(CatalogItem entity) {

        Map<String, String> rowMap = new HashMap<>();
        rowMap.put("Id", safeString(entity.getId()));
        rowMap.put("Name", safeString(entity.getName()));
        rowMap.put("Season", safeString(entity.getSeason()));
        rowMap.put("Magazine", safeString(entity.getMagazine()));
        rowMap.put("Article", safeString(entity.getArticle()));
        
        return rowMap;
    }
    
    private void rowDataMapToJson(Map<String, String> rowData, String[] columns, StringBuilder sb) {
    	
        sb.append('{');
        boolean first = true;
        for (String column : columns) {
            final String text = rowData.get(column.trim());
            String wrap = StringUtils.isNumeric(text) && StringUtils.isNotBlank(text)?"":"\"";
            if(!first)sb.append(',');
            sb.append("\"").append(column.trim()).append("\"");
            sb.append(':');
            sb.append(wrap).append(jsonEncode(text)).append(wrap);
            first = false;
        }
        sb.append('}');
    }
    
    private void toCSV(Map<String, String> data, String[] columns, StringBuilder sb) {
        
    	for (int i = 0; i < columns.length; i++) {
    		sb.append(data.get(columns[i])).append((i + 1 < columns.length) ? "," : "\n");
		}
    }
    
    private void toCSV(String[] data, StringBuilder sb) {
    	
    	for (int i = 0; i < data.length; i++) {
    		sb.append(data[i]).append((i + 1 < data.length) ? "," : "\n");
		}
    }    
    
    private String safeString(Object value){
        return value == null ? "" : String.valueOf(value);
    }

    
    private String jsonEncode(String text){
        return safeString(text)
                .replace("\"","&quot;")  
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">","&gt;")
                .replace("'", "&#39;")
                .replace("%", "&#37;")
                ;
    }

}
