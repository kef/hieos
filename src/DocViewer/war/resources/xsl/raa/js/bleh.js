window.onload = function(){
    var tables = document.getElementsByTagName( "table" );
    for ( var t = 0; t < tables.length; t++ ) {
        var rows = tables[t].getElementsByTagName( "tr" );
        for ( var i = 1; i < rows.length; i += 2 )
            YAHOO.util.Dom.addClass( rows[i], "odd" );
    }
}