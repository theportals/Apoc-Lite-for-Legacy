import addons.*;
import haven.*;

public class Template extends Thread{
	public String scriptName = "Template script";
	public String[] options = {
		"Template 1", "Template 2", "Template 3",
	};
	
	HavenUtil m_util;
	int m_option;
	String m_modify;
	
	public void ApocScript(HavenUtil util, int option, String modify){
		m_util = util;
		m_option = option;
		m_modify = modify;
	}
	
	public void run(){
		switch(m_option){
			case 1: System.out.println("Template run test 1"); break;
			case 2: System.out.println("Template run test 2"); break;
			case 3: System.out.println("Template run test 3"); break;
			case 4: System.out.println("Template run test 4 and " + m_modify); break;
		}
		
		m_util.running(false);
	}
}