package ee.ioc.cs.vsle.util;

import java.io.*;
import java.net.*;

import javax.swing.*;

/**
 * User: Ando
 * Date: 28.03.2005
 * Time: 21:45:37
 */
public class FileFuncs {
	public static String getFileContents(File file) {
		String fileString = new String();
		if( file != null && file.exists() && !file.isDirectory() ) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(file));
				String lineString = new String();

				while ((lineString = in.readLine()) != null) {
					fileString += lineString+"\n";
				}
				in.close();
			} catch (IOException ioe) {
				db.p("Couldn't open file "+ file.getAbsolutePath());
			}
		}
		return fileString;
	}

	/**
	 * Writes the text to the specified file. The file is created if it
	 * does not exist yet. A newline is appended to the text.
	 * @param file the output file
	 * @param text the text to be written
	 * @return true on success, false on error
	 */
	public static boolean writeFile(File file, String text) {
		boolean status = false;
		if (file != null && !file.isDirectory()) {
			PrintWriter out = null;
			try {
				out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				out.println(text);
				out.close();
				out = null;
				status = true;
			} catch (Exception e) {
				db.p(e);
				db.p("Couldn't write to file "+ file.getAbsolutePath());
			} finally {
				if (out != null) {
					out.close();
					out = null;
				}
			}
		}
		return status;
	}

    public static void writeFile( String prog, String mainClassName, String ext, String dir, boolean append ) {
        try {
            if (!dir.endsWith(File.separator)) {
                dir += File.separator;
            }
            String path = dir + mainClassName + "." + ext;
            File file = new File( path );

            if( !append && file.exists() ) {
                file.delete();
            }

            PrintWriter out = new PrintWriter( new BufferedWriter(new FileWriter( path, append ) ) );

            out.println( prog );
            out.close();
        } catch ( Exception e ) {
            db.p( e );
        }
    }
    
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
    
    public static URL getResource( String res, boolean checkFileIfNull ) {
    	
    	URL url = Thread.currentThread().getContextClassLoader().getResource( res );
    	
    	if( url != null || !checkFileIfNull ) {
    		return url;
    	}
    	
    	File file = new File( res );
    	
    	if( file.exists() ) {
    		try {
				return file.toURI().toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
    	}
    	
    	return null;
    }

    public static ImageIcon getImageIcon( String icon, boolean isAbsolutePath ) {
    	
    	if (!isAbsolutePath) {
    		URL url = getResource( icon, false );
    		
    		if( url != null )
    		{
    			return new ImageIcon( url );
    		}
    	}
    	
    	return new ImageIcon( icon );
    }
    
    
    /**
     * Makes sure that all file separators in path are valid for current OS.
     * If the path is already valid the same string is returned.
     * 
     * @param path a string representing a path name that possibly contains
     * path name separators of a different platform
     * @return a string where foreign path name separators are replaced with
     * the separators of current platform; or the original string if it did not
     * contain any foreign path name separators
     */
    public static String preparePathOS( String path ) {
        return File.separatorChar == '/'
                ? path.replace('\\', '/')
                : path.replace('/', '\\');
    }

}
