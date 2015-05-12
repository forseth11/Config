package com.forseth11.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.forseth11.Config.ConfigMethods;

/**
 * Configs class makes it easy to create
 * edit and save configs.
 * 
 * @author Michael Forseth
 *
 */
public class Config {
	private File configFile;
	private HashMap<String, String> strings = new HashMap<String, String>();
	private HashMap<String, Integer> ints = new HashMap<String, Integer>();
	private HashMap<String, Long> longs = new HashMap<String, Long>();
	private HashMap<String, Double> doubles = new HashMap<String, Double>();
	private HashMap<String, ArrayList<String>> list = new HashMap<String, ArrayList<String>>();
	private HashMap<String, PathType> paths = new HashMap<String, PathType>();
	
	/**
	 * This creates a new instance of a config.
	 * It takes a file and it parses the values of the config
	 * into the program.  The file must already exist. The extension can be anything you like.
	 * 
	 * @param configFile
	 */
	public Config(File configFile){
		if(configFile == null){
			return;
		}
		
		if(configFile.exists()){
			this.configFile = configFile;
		}else{
			return;
		}
		
		reloadConfig();
	}

	/**
	 * Reload config will NOT save the config.  It will take what the current file has in it
	 * and parse it's values into the program.
	 * 
	 * @return If the reload has succeeded it will return true 
	 */
	public boolean reloadConfig() {
		strings = new HashMap<String, String>();
		ints = new HashMap<String, Integer>();
		longs = new HashMap<String, Long>();
		doubles = new HashMap<String, Double>();
		list = new HashMap<String, ArrayList<String>>();
		paths = new HashMap<String, PathType>();
		
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader br = null;
		try{
			String currentLine;
			br = new BufferedReader(new FileReader(configFile));
			while((currentLine = br.readLine()) != null){
				lines.add(currentLine.replace("	", "    "));
			}
		} catch(IOException e){
			e.printStackTrace();
			return false;
		}finally{
			try{
				if(br != null){
					br.close();
				}
			}catch(IOException ex){
				ex.printStackTrace();
				return false;
			}
		}
		try{
			parseConfig(lines, "");
		}catch(ArrayIndexOutOfBoundsException e){
			return false;
		}
		
		/*System.out.println("\n\n");
		for(String str : strings.keySet()){
			System.out.println(str + ": " + strings.get(str));
		}
		for(String str : ints.keySet()){
			System.out.println(str + ": " + ints.get(str));
		}
		for(String str : longs.keySet()){
			System.out.println(str + ": " + longs.get(str));
		}
		for(String str : doubles.keySet()){
			System.out.println(str + ": " + doubles.get(str));
		}
		for(String str : list.keySet()){
			String list = "";
			for(String s : this.list.get(str)){
				list += (s + " | ");
			}
			System.out.println(str + ": " + list + ".");
		}
		System.out.println("\n\n");
		for(String str : paths.keySet()){
			System.out.println(str + " Type: " + paths.get(str).toString());
		}*/
		return true;
	}

	private int parseConfig(ArrayList<String> lines, String upField) throws ArrayIndexOutOfBoundsException{
		boolean list = false;
		int skip = 0;
		String listField = "";
		ArrayList<String> stringList = new ArrayList<String>();
		int count = 0;
		for(String line : lines){
			if(skip > 0){
				skip--;
			}else{
				if(list){
					if(ConfigMethods.subSpaces(line).equals("}") && list){
						this.list.put(upField + listField, stringList);
						addPath(upField, listField, PathType.LIST);
						stringList = new ArrayList<String>();
						list = false;
						listField = "";
					}else{
						stringList.add(ConfigMethods.subSpaces(ConfigMethods.removeOuterQuotes(line)));
					}
				}else{
					if(ConfigMethods.subSpaces(line).equals("}")){
						return count+1;
					}else if(line.contains("=")){
						String[] ls = line.split("=");
						String lc = "";
						String field = ConfigMethods.subSpaces(ls[0]);
						for(int i = 1; i < ls.length; i++){
							lc += ls[i];
						}
						if(lc.equalsIgnoreCase("list{") || lc.equalsIgnoreCase("list")){
							list = true;
							listField = field;
						}else{
							if(lc.charAt(0) == '"'){
								strings.put(upField + field, ConfigMethods.removeOuterQuotes(lc));
								addPath(upField, field, PathType.STRING);
							}else if(lc.contains(".")){
								double i = 0;
								try{
									i = Double.parseDouble(lc);
									doubles.put(upField + field, i);
									addPath(upField, field, PathType.DOUBLE);
								}catch(Exception ex){
									strings.put(upField + field, lc);
									addPath(upField, field, PathType.STRING);
								}
							}else{
								int i = 0;
								try{
									i = Integer.parseInt(lc);
									ints.put(upField + field, i);
									addPath(upField, field, PathType.INT);
								}catch(Exception e){
									long o = 0;
									try{
										o = Long.parseLong(lc);
										longs.put(upField + field, o);
										addPath(upField, field, PathType.LONG);
									}catch(Exception ex){
										strings.put(upField + field, lc);
										addPath(upField, field, PathType.STRING);
									}
								}
							}
						}
					}else if(line.contains("{")){
						if(line.endsWith("{")){
							String field = line.split("\\{")[0];
							field = ConfigMethods.subSpaces(field);
							upField = ConfigMethods.subSpaces(upField);
							ArrayList<String> newLines = new ArrayList<String>();
							for(int i = (count+1); i < lines.size(); i++){
								newLines.add(lines.get(i));
							}
							addPath(upField, field, PathType.FIELD);
							skip = parseConfig(newLines, upField + field + ".");
						}
					}
				}
			}
			count++;
		}
		return 0;
	}

	private void addPath(String upField, String field, PathType pt) {
		if(!this.paths.containsKey(upField)){
			paths.put(upField + field, pt);
		}
	}
	
	/**
	 * This method takes all current changes made in the config via
	 * this application and parses it into a string then saves it to
	 * the config file.
	 * 
	 * 
	 * @return It will return true if it succeeds.
	 */
	public boolean save(){
		ArrayList<String> lines = getLines(0, "");
		String content = "";
		for(String line : lines){
			content += line + "\n";
		}
		try{
			if(!configFile.exists()){
				configFile.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(configFile.getAbsolutePath()));
			bw.write(content);
			bw.close();
			return true;
		}catch(IOException e){
			return false;
		}
	}

	private ArrayList<String> getLines(int start, String upField){
		ArrayList<String> ret = new ArrayList<String>();
		for(String path : paths.keySet()){
			PathType pt = paths.get(path);
			if(ConfigMethods.countPeriods(path) == start && ConfigMethods.isDownField(path, upField)){
				if(pt.equals(PathType.INT)){
					ret.add(ConfigMethods.removeUpField(path, upField) + "=" + this.ints.get(path));
				}else if(pt.equals(PathType.LONG)){
					ret.add(ConfigMethods.removeUpField(path, upField) + "=" + this.longs.get(path));
				}else if(pt.equals(PathType.DOUBLE)){
					ret.add(ConfigMethods.removeUpField(path, upField) + "=" + this.doubles.get(path));
				}else if(pt.equals(PathType.STRING)){
					ret.add(ConfigMethods.removeUpField(path, upField) + "=\"" + this.strings.get(path)+"\"");
				}else if(pt.equals(PathType.LIST)){
					ret.add(ConfigMethods.removeUpField(path, upField) + "=list{");
					for(String s : this.list.get(path)){
						ret.add("  " + s);
					}
					ret.add("}");
				}else if(pt.equals(PathType.FIELD)){
					if(path.contains(".")){
						String[] split = path.split("\\.");
						ret.add(split[split.length-1] + "{");
					}else{
						ret.add(path + "{");
					}
					for(String str : getLines(start+1, path)){
						ret.add("  " + str);
					}
					ret.add("}");
				}
			}
		}
		return ret;
	}
	
	/**
	 * This method gets a string from the config
	 * based on the string passed to the method.
	 * 
	 * @param field
	 * @return value from the selected field.
	 * @throws FieldNotFoundException if the field does not exist in the config.
	 */
	public String getString(String field) throws FieldNotFoundException{
		if(strings.containsKey(field)){
			String str = strings.get(field);
			str = str.replace("%3", "\"");
			str = str.replace("%2", "=");
			str = str.replace("%1", "\n");
			str = str.replace("%0", "%");
			return str;
		}else{
			throw new FieldNotFoundException();
		}
	}
	
	/**
	 * This method gets an integer from the config
	 * based on the string passed to the method.
	 * 
	 * @param field
	 * @return returns the integer for the given field.
	 * @throws FieldNotFoundException if the field does not exist in the config.
	 */
	public int getInt(String field) throws FieldNotFoundException{
		if(ints.containsKey(field)){
			return ints.get(field);
		}else{
			throw new FieldNotFoundException();
		}
	}
	
	/**
	 * This method gets a long from the config
	 * based on the field passed to the method.
	 * If there is no long found by the field name
	 * it will take an integer if there is one with the same
	 * path and name.
	 * 
	 * @param field
	 * @return returns the long for the given field.
	 * @throws FieldNotFoundException if the field does not exist in the config.
	 */
	public long getLong(String field) throws FieldNotFoundException{
		if(longs.containsKey(field)){
			return longs.get(field);
		}else if(ints.containsKey(field)){
			return getInt(field);
		}else{
			throw new FieldNotFoundException();
		}
	}
	
	/**
	 * This method gets a double from the config
	 * based on the field passed to the method.
	 * If it can not find a double by the field name
	 * it will try to find an int by the field name.
	 * 
	 * 
	 * @param field
	 * @return returns a double for the given field
	 * @throws FieldNotFoundException if the field does not exist in the config.
	 */
	public double getDouble(String field) throws FieldNotFoundException{
		if(doubles.containsKey(field)){
			return doubles.get(field);
		}else if(ints.containsKey(field)){
			return getInt(field);
		}else{
			throw new FieldNotFoundException();
		}
	}
	
	/**
	 * This method gets an ArrayList<String> from the config
	 * based on the field passed to the method.
	 * 
	 * @param field
	 * @return array list from given field.
	 * @throws FieldNotFoundException if the field does not exist in the config.
	 */
	public ArrayList<String> getStringList(String field) throws FieldNotFoundException{
		if(list.containsKey(field)){
			return list.get(field);
		}else{
			throw new FieldNotFoundException();
		}
	}
	
	/**
	 * This will get the sub sections and fields for the section you pass to this method.
	 * 
	 * @param section
	 * @return It returns this as a HashMap<String, PathType> containing the sub sections and sub fields as the keys and then the PathType for each key.
	 * @throws FieldNotFoundException if the section does not exist in the config.
	 */
	public HashMap<String, PathType> getConfigSectionWithTypes(String section) throws FieldNotFoundException{
		if(paths.containsKey(section)){
			HashMap<String, PathType> returns = new HashMap<String, PathType>();
			for(String pa : paths.keySet()){
				if(pa.startsWith(section)){
					String st = ConfigMethods.removeUpField(pa, section);
					if(!st.contains(".")){
						returns.put(st, paths.get(pa));
					}
				}
			}
			return returns;
		}else{
			throw new FieldNotFoundException();
		}
	}
	
	/**
	 * This will get the sub sections and fields for the section you pass to this method.
	 * 
	 * @param section
	 * @return It returns an array list of the sub sections and fields for the section you passed in.
	 * @throws FieldNotFoundException if the section does not exist in the config.
	 */
	public ArrayList<String> getConfigSection(String section) throws FieldNotFoundException{
		if(paths.containsKey(section)){
			ArrayList<String> returns = new ArrayList<String>();
			for(String pa : paths.keySet()){
				if(pa.startsWith(section)){
					String st = ConfigMethods.removeUpField(pa, section);
					if(!st.contains(".")){
						returns.add(st);
					}
				}
			}
			return returns;
		}else{
			throw new FieldNotFoundException();
		}
	}
	
	/**
	 * Create path creates a configuration path. 
	 * It is not recommended that you use this method.
	 * It is better to set a value because it will automatically
	 * create the path if needed.  By manually creating a path
	 * you may mess up a config in doing so.
	 * 
	 * @deprecated
	 * @param path is the path you want to create.
	 * @param pathType is the pathType you want the path to be. 
	 */
	public void createPath(String path, PathType pathType) {
		String pathBuild = "";
		for(String pa : path.split("\\.")){
			if(pathBuild == ""){
				pathBuild += pa;
			}else{
				pathBuild += ("." + pa);
			}
			if(!this.paths.containsKey(pathBuild)){
				if(path.equals(pathBuild)){
					this.paths.put(pathBuild, pathType);
				}else{
					this.paths.put(pathBuild, PathType.FIELD);
				}
			}
		}
	}
	
	/**
	 * This method is used to set OR create a string in this config.
	 * Usage: set("Example.path", "StringValue");
	 * 
	 * @param path
	 * @param string
	 */
	public void set(String path, String string){
		string = string.replace("%", "%0");
		string = string.replace("\n", "%1");
		string = string.replace("=", "%2");
		string = string.replace("\"", "%3");
		if(strings.containsKey(path)){
			strings.put(path, string);
		}else{
			createPath(path, PathType.STRING);
			strings.put(path, string);
		}
	}
	
	/**
	 * This method is used to set OR create an integer in this config.
	 * Usage: set("Example.path", 5);
	 * 
	 * @param path
	 * @param string
	 */
	public void set(String path, int integerValue){
		if(ints.containsKey(path)){
			ints.put(path, integerValue);
		}else{
			createPath(path, PathType.INT);
			ints.put(path, integerValue);
		}
	}
	
	/**
	 * This method is used to set OR create a long in this config.
	 * Usage: set("Example.path", 1408643603085);
	 * 
	 * @param path
	 * @param string
	 */
	public void set(String path, long longValue){
		if(longs.containsKey(path)){
			longs.put(path, longValue);
		}else{
			createPath(path, PathType.LONG);
			longs.put(path, longValue);
		}
	}
	
	/**
	 * This method is used to set OR create a double in this config.
	 * Usage: set("Example.path", 351.65365);
	 * 
	 * @param path
	 * @param string
	 */
	public void set(String path, double doubleValue){
		if(doubles.containsKey(path)){
			doubles.put(path, doubleValue);
		}else{
			createPath(path, PathType.DOUBLE);
			doubles.put(path, doubleValue);
		}
	}
	
	/**
	 * This method is used to set OR create a string list in this config.
	 * Usage: set("Example.path", ArrayList<String>);
	 * 
	 * @param path
	 * @param string
	 */
	public void set(String path, ArrayList<String> list){
		if(this.list.containsKey(path)){
			this.list.put(path, list);
		}else{
			createPath(path, PathType.LIST);
			this.list.put(path, list);
		}
	}
}
