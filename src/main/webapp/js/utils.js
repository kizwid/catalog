

// trims leading and trailing spaces
String.prototype['trim'] = function (){
	return this.replace('/^\s+|\s+$/g', '');
};

function isBlank(input) {
	
	if(!input) { return true; }
	
	if(typeof input === 'number') {   return false;   	} 
	else if (typeof input === 'string') { return (input.trim() === '');	}
}


function keys(jsonIn) {  
//TODO   
}

function values(jsonIn) {  
//TODO  
}

function htmlEncode(str) {
	return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/%/,'&#37;');
}

function htmlDecode(str) {
	return String(str).replace(/&amp/g, '&').replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&#37;/, '%');
}


//oRecord - a YAHOO.widget.Record
//not much introspective functionality, so we need known keys.  The column names should suffice...
//unused
function getAllFieldsAsQueryParam(oRecord) {

	var sQueryParams = '';
	for(var i = 0; i < cols.length; i++ ) {
		sQueryParams += ('&' + cols[i]["key"] + '=' + oRecord.getData(cols[i]["key"]));
	}
	
	return sQueryParams;
}


function popupMessage(message, title) {

	var w=window.open('','name','height=200,width=800');
	var tmp = w.document;
	tmp.write('<html><head><title>' + title + '</title>');
	tmp.write('</head><body><p>' + title + '</p>');
	tmp.write('<p>' + message + '</p>');
	tmp.write('</body></html>');
	tmp.close();
}

function popupPage(htmlRoot, page) {

    //we have a problem on some PCs running MSIE 7
    //hence
    if( navigator.appVersion.indexOf("MSIE 7") > 0){

        copyToClipboard( page);

    }else{

        window.open( htmlRoot + page,
            "_blank",
            "height=200,width=800,menubar=no,toolbar=no,location=no,status=no,scrollbars=yes,resizable=yes");

    };

}



function copyToClipboard(s) {
    var message;
    if( window.clipboardData && clipboardData.setData )
    {
        clipboardData.setData("Text", s);
        message = 'copied to clipboard:\n' + s;
    } else{
        message = 'copy to clipboard not allowed:\n' + s;
    };
    alert( message);
}

// uses getKeys function, defined in functional-tools.js
function populateFilter(columnArray, source) {
	
	var sourceSelect = document.getElementById(source);
	
	if(sourceSelect) {

		var columnNameArray = columnArray.map(getKeys);
		
		for(var i = 0; i < columnNameArray.length; i++) {

			var opt = document.createElement("option");
			opt.value = columnNameArray[i];
			opt.text = columnNameArray[i].replace(/([A-Z])/g, ' $1');
			if(columnNameArray[i] === "Name") {   opt.selected = "selected";   }
			
			sourceSelect.options.add(opt);		
		}
	}
}


