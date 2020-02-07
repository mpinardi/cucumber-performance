package cucumber.perf.api.plan;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;

import org.junit.Test;


public class PlanPathTest {


    @Test
    public void can_parse_empty_plan_path() {
    	try {
    		PlanPath.parse("");
    	} catch (Exception e)
    	{
    		assertEquals(e.getMessage(), "planIdentifier may not be empty");
    	}
    }

    @Test
    public void can_parse_root_package() {
        URI uri = PlanPath.parse("classpath:/");
        assertEquals(uri.getScheme(), "classpath");
        assertEquals(uri.getSchemeSpecificPart(), "/");
    }

    @Test
    public void can_parse_eclipse_plugin_default_glue() {
        // The eclipse plugin uses `classpath:` as the default
        URI uri = PlanPath.parse("classpath:");

        
        assertEquals(uri.getScheme(), "classpath");
        assertEquals(uri.getSchemeSpecificPart(), "/");
      
    }

    @Test
    public void can_parse_classpath_form() {
        URI uri = PlanPath.parse("classpath:/path/to/file.plan");

        
        assertEquals(uri.getScheme(), "classpath");
        assertEquals(uri.getSchemeSpecificPart(), "/path/to/file.plan");
      
    }

    @Test
    public void can_parse_classpath_directory_form() {
        URI uri = PlanPath.parse("classpath:/path/to");

        
        assertEquals(uri.getScheme(), "classpath");
        assertEquals(uri.getSchemeSpecificPart(), "/path/to");
      
    }

    @Test
    public void can_parse_absolute_file_form() {
        URI uri = PlanPath.parse("file:/path/to/file.plan");
        assertEquals(uri.getScheme(), "file");
        assertEquals(uri.getSchemeSpecificPart(), "/C:/path/to/file.plan");
    }

    @Test
    public void can_parse_absolute_directory_form() {
        URI uri = PlanPath.parse("file:/path/to");
        assertEquals(uri.getScheme(), "file");
        assertEquals(uri.getSchemeSpecificPart(), "/C:/path/to");
    }

    @Test
    public void can_parse_relative_file_form() {
        URI uri = PlanPath.parse("file:path/to/file.plan");
        assertEquals(uri.getScheme(), "file");
        assertTrue(uri.getSchemeSpecificPart().endsWith("path/to/file.plan"));
      
    }

    @Test
    public void can_parse_absolute_path_form() {
        URI uri = PlanPath.parse("/path/to/file.plan");
        assertEquals(uri.getScheme(), "file");
        // Use File to work out the drive letter on windows.
        File file = new File("/path/to/file.plan");
        assertEquals(uri.getSchemeSpecificPart(), file.toURI().getSchemeSpecificPart());
    }

    @Test
    public void can_parse_relative_path_form() {
        URI uri = PlanPath.parse("path/to/file.plan");    
        assertEquals(uri.getScheme(), "file");
        assertTrue(uri.getSchemeSpecificPart().endsWith("path/to/file.plan"));
    }

    @Test
    public void can_parse_windows_path_form() {
        URI uri = PlanPath.parse("path\\to\\file.plan");
        assertEquals(uri.getScheme(), "file");
        assertTrue(uri.getSchemeSpecificPart().contains( "path/to/file.plan"));
    }

    @Test
    public void can_parse_windows_absolute_path_form() {
        URI uri = PlanPath.parse("C:\\path\\to\\file.plan");
        assertEquals(uri.getScheme(), "file");
        assertTrue(uri.getSchemeSpecificPart().contains("/C:/path/to/file.plan"));
    }

    @Test
    public void can_parse_whitespace_in_path() {
        URI uri = PlanPath.parse("path/to the/file.plan");
        assertEquals(uri.getScheme(), "file");
        assertTrue(uri.getSchemeSpecificPart().contains( "path/to the/file.plan"));
    }

    @Test
    public void can_parse_windows_file_path_with_standard_file_separator() {
        URI uri = PlanPath.parse("C:/path/to/file.plan");
        assertEquals(uri.getScheme(), "file");
        assertEquals(uri.getSchemeSpecificPart(), "/C:/path/to/file.plan");
    }

 
}