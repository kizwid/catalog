/**
 * Unused, left in for reference/cut-and-paste
 * 
 */


/*******   Main table Right Click context menu *********/

function initContextMenu(table) {            
	
    //only allow to users with write access
    //# if( $HasWriteAccess.bool )
        var rowContextMenu = new YAHOO.widget.ContextMenu("rowContextMenu",{trigger:table.getTbodyEl()});
        var rowMenuItems = [{text:"Create New"}, {text:"Clone Record"}, {text:"Edit Record"}, {text:"Delete Record"}];
        rowContextMenu.addItems(rowMenuItems);
        rowContextMenu.render("dashboardDetailList");       			            // Render the ContextMenu instance to the parent container of the DataTable
        rowContextMenu.clickEvent.subscribe(onContextMenuClick, table);
    //# end
}

//---------------------------------//
//      add context menu to table
//---------------------------------//
var onContextMenuClick = function(p_sType, p_aArgs, p_myDataTable) {

    var task = p_aArgs[1];
    if(task) {

        // Extract which TR element triggered the context menu
        var elRow = p_myDataTable.getTrEl(this.contextEventTarget);
	    var oRecord = p_myDataTable.getRecord(elRow);

        if(elRow) {

            switch(task.index) {

                case 0:     // Create New

                    var newRow = {Id:-1, IsTemplate:"Y", IsDeleted:"N", Version:1, LastUpdatedBy:"system", Business:"GED", BusinessDate:"&lt;YYYYMMDD&gt;", HolidayCities:"&lt;HOLIDAY_CITY&gt;"};
                    saveOrUpdateRecord(newRow);
                	break;

                case 1:     // Clone Record

                    var clone = YAHOO.lang.JSON.parse( YAHOO.lang.JSON.stringify(oRecord.getData()));
                    clone.Id = -1;
                    clone.Version = 1;
                    clone.HandleTemplate = clone.HandleTemplate + "_clone";
                    saveOrUpdateRecord(clone);

                    break;

                case 2:     // Edit Record

                    saveOrUpdateRecord(oRecord.getData());

                    break;

                case 3:     // Delete Record

                    var clone = YAHOO.lang.JSON.parse( YAHOO.lang.JSON.stringify(oRecord.getData()));
                    if(confirm("Are you sure you want to delete " + htmlDecode(clone.Id) + "?")) {
                        clone.IsDeleted = "Y";
                        saveOrUpdateRecord(clone);
                    }

                	break;

                default:
                	break;
            }
        }
    }
};


