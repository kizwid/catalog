
// _table: a YAHOO.widget.DataTable
// _breadcrumb: an HTML element which will be populated will a filter breadcrumb trail. 
// _defaultQuery = a simple key/value JSON object with columnName/columnValue which will serve as the default, even if filterQuery.clear() is called. 
function decorateTable(_table, _breadcrumb, _filterTextBox, _hiddenCols, _defaultQuery) {
	
	var decoratedTable = _table;
	
	var defaultQuery = _defaultQuery;
	
	functionalTools.init();
	
	var hiddenCols = ["IsTemplate"].concat(_hiddenCols).makeSet();   //["IsDeleted"];

	decoratedTable['breadcrumb'] = _breadcrumb;
	
	decoratedTable['filterTextBox'] = _filterTextBox;
	
	decoratedTable['filterQuery'] = {
			
			query: [],
			getQuery: function() { return this.query.makeSet(); },
			reset: function() { this.query = (defaultQuery || []); return this.query;},
			clear: function(key) { 
				this.query = this.query.filter(allButKey(key)); return this.query;  },
			getQueryValue: function(key) { var found = this.containsKey(key); return (found ? found['value'] : ''); },
			containsKey: function(key) {  
				return ( this.query.filter( getByKey(key) ).length > 0); },
			append: function(newJsonPair) { 
				
				var key = newJsonPair['key'];
				if(this.containsKey(key)){
					this.clear(key);
				} 
				
				this.query.push(newJsonPair);		
				return this.query; 
			}
		};  
	
	decoratedTable['refreshDataSource'] = function(q) {
    	
    	var query = q || [];
    	
    	decoratedTable.getDataSource().sendRequest(query, {
    		success: decoratedTable.onDataReturnInitializeTable, 
    		failure: decoratedTable.onDataReturnInitializeTable,
            argument: decoratedTable.getState(),
    		scope: decoratedTable
    		});
	}
	
    //---------------------------------//
    //      add Header Context Menu
    //---------------------------------//
    
	var onHeaderContextMenuClick = function(p_sType, p_aArgs, p_myDataTable) {  
	
        var task = p_aArgs[1];
        
    	var elCol = p_myDataTable.getThEl( this.contextEventTarget );
	    var selectedColumnName = p_myDataTable.getColumn(elCol).getField();                  
        
	    if(task) {

            if(elCol) {
            	
                switch(task.index) {
              
                    case 0:     // Filter Record - do nothing; delegate to submenu
                    	break;

                    case 1:     // Hide column	
                    	
                    	hiddenCols.push(selectedColumnName);
                    	hiddenCols.sort();
                    	p_myDataTable.hideColumn(selectedColumnName);
                    	
                    	break;
                    	
                    case 2:     // Show column	- do nothing; delegate to submenu
                    	break;
                    	
                    default:
                    	break;
            	}
            }		
        }
	};
    
    var onHeaderContextMenuBeforeShow = function(p_sType, p_aArgs, p_myDataTable) {
    
        var elCol = p_myDataTable.getThEl( this.contextEventTarget );
	    var selectedColumn = p_myDataTable.getColumn(elCol);            

	    // set up Filter Submenu
		filterSubMenu['selectedColumn'] = selectedColumn;// we assign this as it will be used later by the click event
        filterSubMenu.clearContent();
        
        var undecorated = getDistinctValueSetFromRecordSet( selectedColumn.getField(), p_myDataTable.getRecordSet() );
        var filteredValues = decorateForSubMenu( undecorated );
        
        if(!filteredValues || filteredValues.length === 0) {
        	filterSubMenu.addItem({text:'(No values)'});
        } else if(filteredValues.length > 15) {
			filterSubMenu.addItem({text:'Too many values to display'});
		} else {
			filterSubMenu.addItems( filteredValues.sort() );
		}
        
        // set up Show/Hide Submenu
        showHideSubMenu.clearContent();
        showHideSubMenu.addItems( decorateForSubMenu(hiddenCols) );
        
		filterSubMenu.render();
		showHideSubMenu.render();
		
    };
    
    function updateBreadCrumbs() {
    	
    	if(decoratedTable['breadcrumb']) {
    		
    		var filters = decoratedTable['filterQuery'].getQuery();
    		var str = (filters && filters.length > 0) ? 'Active filters: ' : '';
    		
    		for(var i = 0; i < filters.length; i++){
    			str += [filters[i]['key'], '=', filters[i]['value'], ' ', getDeleteLink(filters[i]['key']), ((i+1 < filters.length) ? '\, ' : '')].join('');
    		}
    		
    		decoratedTable['breadcrumb']['innerHTML'] = str;
    	}
    	
    	return true;
    }
    
    
    function getDeleteLink(sKey) {
    	return ["<button onclick=\"updateFilter('", decoratedTable.getId(),"','",sKey,"'); return true;\">clear</button>"].join('');
    }
    
    var onFilterSubMenuClick = function(p_sType, p_aArgs, p_myDataTable) {
    
        var task = p_aArgs[1];
        
        if(task) {

        	if(this.getItems().length === 1) {  return;  }
        	
        	var selectedColumn = this['selectedColumn'];
        	var selectedColumnName = selectedColumn.getField();
        	var selectedValue = task.value;
        	
			// Reset sort 
			var tableState = p_myDataTable.getState();
			tableState.sortedBy = {key:selectedColumnName, dir:YAHOO.widget.DataTable.CLASS_ASC}; 	
			
			var filterQuery = decoratedTable['filterQuery'];
			var query = filterQuery.append( {key:selectedColumnName, value:selectedValue} );
				
			decoratedTable.refreshDataSource(query);
		}            
    
    }
    
    var onShowHideSubMenuClick = function(p_sType, p_aArgs, p_myDataTable) {
     
        var task = p_aArgs[1];
        
        // the task is 'show column'
        if(task) {  
        	
        	p_myDataTable.showColumn(task.value);
        	hiddenCols = hiddenCols.filter(function(col) { return col !== task.value; });
        	hiddenCols.sort();
        }
    };
    
    var headerContextMenuName = decoratedTable.getId() + "headerContextMenu";
	var headerContextMenu = new YAHOO.widget.ContextMenu(headerContextMenuName, { trigger:decoratedTable.getTheadEl() });
    var filterSubMenu = new YAHOO.widget.ContextMenu((decoratedTable.getId() + "filterSubMenu"), { trigger:headerContextMenuName });
    var showHideSubMenu = new YAHOO.widget.ContextMenu((decoratedTable.getId() + "showHideSubMenu"), { trigger:headerContextMenuName });
    
    headerContextMenu.clearContent();
    headerContextMenu.addItems( [{text:"Filter By", submenu: filterSubMenu }, {text: "Hide Column"}, {text:"Show Column", submenu: showHideSubMenu }] );
    
    headerContextMenu.render(decoratedTable.getContainerEl());       			            // Render the ContextMenu instance to the parent container of the DataTable

    headerContextMenu.clickEvent.subscribe(onHeaderContextMenuClick, decoratedTable);
	headerContextMenu.beforeShowEvent.subscribe(onHeaderContextMenuBeforeShow, decoratedTable);  // assign this to the top-level menu listener because the contextEventTarget is null for the submenu.
	
	filterSubMenu.clickEvent.subscribe(onFilterSubMenuClick, decoratedTable);
	
	showHideSubMenu.clickEvent.subscribe(onShowHideSubMenuClick, decoratedTable);
	
	if(decoratedTable['breadcrumb']){
		decoratedTable.subscribe("beforeRenderEvent", updateBreadCrumbs);
	}
	
	decoratedTable['hideColumns'] = function() {
		hiddenCols.map( function(col){ decoratedTable.hideColumn(col); } );
	}
	
	decoratedTable['showColumns'] = function() {
		hiddenCols.map( function(col){ decoratedTable.showColumn(col); } );
	}
	
	decoratedTable.hideColumns();
	
	decoratedTable['isDecorated'] = true;   // may be useful in differentiating a regular from a decorated version.
	
	tableMap.push({key:decoratedTable.getId(), value:decoratedTable});
	
	return decoratedTable;

}

//returns a set of values for the given key
//sKey- a String key
//jsonArray - a JSON Array
function getDistinctValueSetFromJsonArray(sKey, jsonArray) {
	
	var allValues = [];
	for(var i = 0; i < jsonArray.length; i++) {
		allValues.push( jsonArray[i][sKey] );
	}
	
	return allValues.makeSet();
}

//returns a set of values for the given key
//sKey- a String key
//oRecordSet - a YAHOO.widget.RecordSet   
function getDistinctValueSetFromRecordSet(sKey, oRecordSet) {
	
	var allValues = [];
	for(var i = 0; i < oRecordSet.getLength(); i++) {
		allValues.push( oRecordSet.getRecord(i).getData(sKey) );
	}
	
	return allValues.makeSet();
}


//takes a simple array of strings and returns an array of JSON objects like:
//{text: string, value: string}
//this is the format best suited for Yahoo UI MenuItem objects.
function decorateForSubMenu(arrayIn) {
	
	var decorated = [];
	for(var i = 0; i < arrayIn.length; i++) {

		var stringedVersion = arrayIn[i] + ''; 
		
		if(!isBlank(stringedVersion)) {
			decorated.push( {text: stringedVersion, value: stringedVersion } );
		}
	}

	return decorated;
}

function arrayToString(jsonArray) {
	
	var str = '';
	
	for(var i = 0; i < jsonArray.length; i++) {
		
		for(key in jsonArray[i]){
			str += 'Key: ' + key + ', Value: ' + jsonArray[i][key] + ';';
		}
		str += '\n'
	}
	
	return str;
}

// tableMap - holds a global data of table id --> table
// an array but will be used as a map via JSON objects of {key:key, value:values}
var tableMap = [];  


function getTableById(id) {
	return tableMap.filter(getByKey(id))[0]['value'];
}

