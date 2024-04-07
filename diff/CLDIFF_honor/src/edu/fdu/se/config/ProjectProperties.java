package edu.fdu.se.config;

import edu.fdu.se.global.Global;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties; 

public class ProjectProperties {
	private Map<String,String> kvMap;
	private ProjectProperties(){
		kvMap = new HashMap<>();
		Properties prop = new Properties();     
        try{
            InputStream in = new BufferedInputStream (new FileInputStream("resource/config.properties"));
            prop.load(in);  
            Iterator<String> it=prop.stringPropertyNames().iterator();
            while(it.hasNext()){
                String key=it.next();
                kvMap.put(key, prop.getProperty(key));
            }
            in.close();
        }
        catch(Exception e){
            Global.logger.warning(e.getMessage());
        }
	}
	private static ProjectProperties instance;
	
	
	public static ProjectProperties getInstance(){
		if(instance==null){
			instance = new ProjectProperties();
		}
		return instance;
	}
	
	public String getValue(String key){
		if(!this.kvMap.containsKey(key)){
			return null;
		}
		return this.kvMap.get(key);
	}
//	public static void main(String args[]){
//		ProjectProperties ins = ProjectProperties.getInstance();
//		System.out.println("a");
//	}

}
