/*
 * This file is code made for modifying the Haven and Hearth client.
 * Copyright (c) 2012-2015 Xcom (Sahand Hesar) <sahandhesar@gmail.com>
 *  
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this code.  If not, see <http://www.gnu.org/licenses/>.
*/

package addons;

import haven.Config;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.net.URLClassLoader;
import java.net.URL;

import java.io.*;
import java.lang.reflect.Method;

import javax.tools.*;
import java.lang.reflect.*;

public class AutoCompilation extends Thread{
	private static int m_runMod = 0;
	int errorCounts = 0;
	
	public AutoCompilation(){}
	
	public static void compile(){
		m_runMod = 1;
		new AutoCompilation().start();
	}
	
	@SuppressWarnings("unchecked")
	public void compileRun() {
		System.setProperty("java.home", Config.javaPath);
		File dest = new File("./scripts/compiled");
		errorCounts = 0;
		
		dest.mkdirs();

		DiagnosticListener listener = new DiagnosticListener(){
			public void report(Diagnostic diagnostic){
				errorCounts++;
				System.err.println("[javac]  "+ diagnostic.getSource() +" "+ diagnostic.getLineNumber());
				System.err.println("[javac]  "+ diagnostic.getMessage(null));
				System.err.println();
			}
		};
		
		List<File> sourceFileList = getFiles();
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		
		if(compiler == null){
			System.err.println("JDK path not set, please set the installed main directory path of the JDK in options/addons.");
			System.err.println("Example: C:\\Program Files\\Java\\jdk1.8.0_31\\");
			return;
		}
		
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		try{
			fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(dest) );
		}catch(Exception e){}
		
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFileList);
		
		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, listener, null, null, compilationUnits);
		
		if(task.call()){
			System.out.println("COMPILE SUCCESSFULL.");
			buildConfRun();
		}else{
			System.out.println("Errors: " + errorCounts);
		}
	}
	
	static public void runClass(String className, HavenUtil util, String option, String modifier){
		try{
			int opt = Integer.parseInt(option);
			runClass(className, util, opt, modifier);
		}catch(Exception e){}	
	}
	
	@SuppressWarnings("unchecked")
	static public void runClass(String className, HavenUtil util, int option, String modifier){
		if(util.running) return;
		
		util.running = true;
		util.stop = false;
		util.update();
		
		try{
			Class params[] = {HavenUtil.class, int.class, String.class};
			Object parameters[] = {util, option, modifier};
			File root = new File("./scripts/compiled");
			
			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
			Class cls = Class.forName(className, true, classLoader);
			
			Object instance = cls.newInstance();
			Method thisMethod = cls.getDeclaredMethod("ApocScript", params);
			thisMethod.invoke(instance, parameters );
			
			((Thread)instance).start();
		}catch(NoSuchMethodException e){
			System.out.println(e);
		}catch(ClassNotFoundException e){
			System.out.println(e);
		}catch (NoClassDefFoundError e){
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	static List<File> getFiles(){
		File[] files = new File("./scripts").listFiles();
		List<File> sourceFileList = new ArrayList<File>();
		
		showFiles(files, sourceFileList);
		return sourceFileList;
	}

	static void showFiles(File[] files, List<File> sourceFileList) {
		for(File file : files){
			if(file.isDirectory()){
				System.out.println("Directory: " + file.getName());
				if(file.getName().contains("compiled") ) continue;
				showFiles(file.listFiles(), sourceFileList);
			}else{
				System.out.println("File: " + file.getName());
				if(file.getName().endsWith(".java") ) sourceFileList.add(file);
			}
		}
	}
	
	public static void buildConf(){
		m_runMod = 2;
		new AutoCompilation().start();
	}
	
	@SuppressWarnings("unchecked")
	public static void buildConfRun(){
		List<String> confData = new ArrayList<String>();
		try{
			Class params[] = {HavenUtil.class, int.class, String.class};
			File root = new File("./scripts/compiled");
			
			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
			
			List<String> stringClasses = confFiles();
			
			for(String className : stringClasses){
				Class cls = Class.forName(className, true, classLoader);
				Object instance = null;
				
				
				
				try{
					instance = cls.newInstance();
					cls.getMethod("ApocScript", params);
				}catch(InstantiationException e){
					//System.out.println(e);
					continue;
				}catch(NoSuchMethodException e){
					//System.out.println(e);
					continue;
				}
				
				if(instance == null) continue;
				
				String[] fieldValue = null;
				String name = "";
				try{
					Field sField = cls.getField("scriptName");
					name = (String)sField.get(instance);
				}catch(NoSuchFieldException e){
					continue;
				}
				
				try{
					Field sField = cls.getField("options");
					fieldValue = (String[]) sField.get(instance);
				}catch(NoSuchFieldException e){ }
				
				confData.add(name +":"+ className);
				
				if(fieldValue != null){
					for(String s : fieldValue){
						confData.add(name +":"+ s);
					}
				}
			}
		}catch(ClassNotFoundException e){
			//System.out.println(e);
		}catch (Exception e){
			e.printStackTrace();
		}
		
		saveToConfig(confData);
	}
	
	static List<String> confFiles(){
		File[] files = new File("./scripts/compiled").listFiles();
		List<String> stringNames = new ArrayList<String>();
		String folderString = "";
		
		showConfFiles(files, folderString, stringNames);
		return stringNames;
	}
	
	static void showConfFiles(File[] files, String folderString, List<String> stringNames){
		for(File file : files){
			if(file.isDirectory() ){
				//System.out.println("Directory: " + file.getName());
				showConfFiles(file.listFiles(), (file.getName() + "."), stringNames);
			}else{
				//System.out.println("File: " + file.getName());
				if(file.getName().endsWith(".class") ) stringNames.add(folderString + file.getName().replace(".class", "") );
			}
		}
	}
	
	static void saveToConfig(List<String> confData){
		try {
			File file = null;
			file = new File("./scripts/compiled/script.conf");
			
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			for(String str : confData){
				bw.write(str);
				bw.newLine();
			}
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
		if(m_runMod == 1) compileRun();
		if(m_runMod == 2) buildConfRun();
	}
}