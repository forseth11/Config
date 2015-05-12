package com.forseth11.Config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is for the config class to parse the config's information into a HashMap and back to a string.
 * 
 * @author Michael Forseth
 *
 */
public class ConfigMethods {
	protected static String removeOuterQuotes(String str){
		try{
			if(str != null){
				if(str != ""){
					/*System.out.println("ERRORFINDER: str: " + str);
					if(str.charAt(0) == '"'){
						System.out.println("ERRORFINDER: First \"");
					}
					if(str.charAt(str.length()-1) == '"'){
						System.out.println("ERRORFINDER: Last \"");
					}*/
					if((str.charAt(0) == '"') && (str.charAt(str.length()-1) == '"')){
						String ret = "";
						for(int i = 1; i < (str.length()-1); i++){
							ret += str.charAt(i);
						}
						return ret;
					}else{
						return str;
					}
				}else{
					return str;
				}
			}else{
				return str;
			}
		}catch(Exception e){
			return str;
		}
	}

	protected static String subSpaces(String line) {
		if(line != null){
			if(line != ""){
				boolean ln = true;
				String newLn = "";
				for(int i = 0; i < line.length(); i++){
					if(ln){
						if(line.charAt(i) != ' '){
							newLn += line.charAt(i);
							ln = false;
						}
					}else{
						newLn += line.charAt(i);
					}
				}
				return newLn;
			}else{
				return line;
			}
		}else{
			return line;
		}
	}

	protected static int countPeriods(String path) {
		if(path.contains(".")){
			return path.split("\\.").length-1;
		}else{
			return 0;
		}
	}

	protected static boolean isDownField(String path, String upField) {
		if(upField == "" && countPeriods(path) == 0){
			return true;
		}
		if(path.contains(".")){
			String upMatch = "";
			String[] split = path.split("\\.");
			for(int i = 0; i < (split.length-1); i++){
				upMatch += split[i];
			}
			if(upMatch.equalsIgnoreCase(upField.replace(".", ""))){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}

	protected static String removeUpField(String path, String upField) {
		if(upField != ""){
			if(path.startsWith(upField)){
				String newPath = "";
				for(int i = upField.length()+1; i < path.length(); i++){
					newPath += path.charAt(i);
				}
				return newPath;
			}else{
				return path;
			}
		}else{
			return path;
		}
	}
	
	protected static void writeToFile(String write, File f){
		try{
			if(!f.exists()){
				f.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(f.getAbsolutePath()));
			bw.write(write);
			bw.close();
		}catch(IOException e){
			return;
		}
	}
	
	protected static String removeEndJar(String str){
		String newStr = "";
		try{
			for(int i = 0; i < (str.length() - 4); i++){
				newStr += str.charAt(i);
			}
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
		return newStr;
	}
}
