/**
 * Copyrighted as an unpublished work 2016 D&B.
 * Proprietary and Confidential.  Use, possession and disclosure subject to license agreement.
 * Unauthorized use, possession or disclosure is a violation of D&B's legal rights and may result
 * in suit or prosecution.
 */
package com.dnb.services.plus.sdk.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * @author WalesR
 * 
 * Utility class to identify and insert references to similar records  
 * It expects the source file to be in csv format with 3 columns - refId, httpCode, JsonString
 * 
 */
public class DedupFile {

    private static final Logger LOGGER = Logger.getLogger(DedupFile.class);
    
    public static void main(String[] args) throws Exception {
        
        if (args.length == 2) {
            DedupFile d = new DedupFile();
            d.dedup(args[0], args[1]);
        }
        else {
            System.err.println("Dedup <sourcefile.csv> <destfile.csv>");
        }
    }    
    
    /**
     * Dedup sourceFile and write to destFile 
     * by making 2 passes of sourceFile
     */    
    public void dedup(String sourceFile, String destFile) throws Exception {
        
        Map <String, List <String>> map = new HashMap <String, List <String>> ();
        
        // first pass - create hashmap of references
        processFile(sourceFile, destFile, map, true);

        // second pass - write new csv file with new customerReferenceIDs array 
        processFile(sourceFile, destFile, map, false);
    }
    
    /**
     * This method does one of two things
     * - creates a HashMap of Refential Ids if firstPass is true
     * - create a destination file with new reference Ids if on secondPass  
     */      
    private void processFile(String sourceFile, String destFile, Map <String, List <String>> map, boolean isFirstPass) {

      BufferedWriter out = null;
        
        try {            
            if(!isFirstPass) {
                out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(destFile), "UTF-8"));
            }
            
            BufferedReader in = new BufferedReader(new FileReader(sourceFile));
            String line = "";
            while ((line = in.readLine()) != null) {
                String[] parts = line.split(",");
                if(parts.length < 3) {
                    // csv file must have at least 3 columns
                    LOGGER.error("CSV file must contain at least 3 columns");
                    continue;
                }
                String refId = parts[0];
                String httpResp = parts[1];
                int i = line.indexOf(parts[2]);
                String jsonStr = line.substring(i);
                LOGGER.debug("refId: " + refId + ", httpResp: " + httpResp + ", json: " + jsonStr);
                if(!"200".equals(httpResp)) {
                    if(!isFirstPass) {
                        out.write(line);
                        out.newLine();                                            
                    }
                	continue;
                }
                
                JsonParser parser = new JsonParser();
                JsonObject jsonObject = (JsonObject) parser.parse(jsonStr);

                // get duns element
                JsonElement jsonElement = jsonObject.get("duns");
                if(null == jsonElement) {
                    LOGGER.error("'duns' element not found in Json string! RefId: " + refId);
                    if(!isFirstPass) {
                        out.write(refId + "," + httpResp + "," + jsonObject.toString());
                        out.newLine();                                            
                    }
                    continue;
                }
                String duns = (String) jsonElement.getAsString();
                Type listType = new TypeToken <List<String>>() {}.getType();
                Gson gson = new GsonBuilder().serializeNulls().create();                
                List<String> customerReferenceIDs = gson.fromJson(jsonObject.get("customerRefs"), listType);
                if(null == customerReferenceIDs) {
                    LOGGER.error("'customerRefs' element not found in Json string! RefId: " + refId);
                    if(!isFirstPass) {
                        out.write(refId + "," + httpResp + "," + jsonObject.toString());
                        out.newLine();                                            
                    }
                    continue;
                }
                Collections.sort(customerReferenceIDs);                
                
                String keyName = duns + customerReferenceIDs.toString();
                List <String> refIds = map.get(keyName);
                if(isFirstPass) {
                    // build HashMap if we're on the first pass
                    if(null == refIds) {
                        refIds = new ArrayList <String> (); 
                    }
                    refIds.add(refId);
                    map.put(keyName, refIds);                    
                }
                else {
                    // write Json to file on second pass 
                    JsonArray otherReferences = new JsonArray();
                    for(String ref : refIds) {
                        if(!ref.equals(refId)) { // don't write reference to self
                            otherReferences.add(ref);
                        }
                    }
                               
                    // add inputFileReferenceIDs array to JsonObject if references exist
                    if(otherReferences.size() > 0) {
                        jsonObject.add("inputFileReferenceIDs", otherReferences);
                    }
                    out.write(refId + "," + httpResp + "," + jsonObject.toString());
                    out.newLine();                    
                }
            }            
            in.close();
                        
        } catch (Exception e) {
            LOGGER.error("Error processing CSV file!", e);
            throw new ExceptionInInitializerError(e);
        }           
        finally {
            try {out.close();} catch (Exception ex) {/*ignore*/}
        }        
    }
}

