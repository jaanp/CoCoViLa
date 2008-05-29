/**
 * 
 */
package ee.ioc.cs.vsle.table;

import java.io.*;
import java.util.*;

import ee.ioc.cs.vsle.api.Package;

/**
 * Class for caching tables and providing access to the tables from outer packages
 */
public class TableManager {

    private static Map<Package, Map<String, Table>> tablesByPackages = new HashMap<Package, Map<String, Table>>();
    
    private static final FilenameFilter FILENAME_FILTER = new FilenameFilter() {

        @Override
        public boolean accept( File dir, String name ) {
            
            return name.toLowerCase().endsWith(".tbl");
        }
    };
    
    /**
     * Calls parser and caches tables for the specified package
     * 
     * @param pack
     * @param tableId
     * @return
     */
    public synchronized static IStructuralExpertTable getTable( Package pack, String tableId ) {
        
        if( !tablesByPackages.containsKey( pack ) ) {
            
            Map<String, Table> tables = new HashMap<String, Table>();
            
            File folder = new File( pack.getPath() );
            
            if( !folder.isDirectory() ) {
                folder = folder.getParentFile();
            }
            
            File[] tableFiles = folder.listFiles( FILENAME_FILTER );
            
            for ( int i = 0; i < tableFiles.length; i++ ) {
                
                //db.p( "Parsing table(s) from: " + tableFiles[i].getAbsolutePath() );
                
                tables.putAll( TableParser.parse( tableFiles[i] ) );
            }
            
            //this line can be reached if no errors occurred during parsing 
            tablesByPackages.put( pack, tables );
        }
        
        IStructuralExpertTable table = tablesByPackages.get( pack ).get( tableId );
        
        if( table != null ) {
            return table;
        }
        
        throw new TableException( "No such table: " + tableId );
    }
    
    /**
     * Assumes that tables have been modified and forces to parses them again
     * 
     * @param pack
     */
    public synchronized static void updateTables( Package pack ) {
        
        Map<String, Table> tables = tablesByPackages.remove( pack );
        
        if( tables != null ) {
            for ( Table table : tables.values() ) {
                table.destroy();
            }
            tables.clear();
        }
    }

}
