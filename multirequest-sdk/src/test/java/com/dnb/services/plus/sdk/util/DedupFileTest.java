package com.dnb.services.plus.sdk.util;

import java.io.File;

import org.junit.Test;


/**
 * @author WalesR
 */
public class DedupFileTest {
    
    @Test
    public void testFileCreated() throws Exception {
        
        String sourceFilePath = (new String()).getClass().getResource("/sample-extmatch-with-ref.csv").getFile();
        
        File tempFile = File.createTempFile("sample-extmatch-with-ref-deduped", ".csv");
        String destFilePath = tempFile.getAbsolutePath();
        
        DedupFile d = new DedupFile();
        d.dedup(sourceFilePath, destFilePath);    
        tempFile.deleteOnExit();
    }    
}
