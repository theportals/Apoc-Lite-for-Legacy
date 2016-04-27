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

import haven.*;
import addons.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import haven.Party.Member;
import haven.BuddyWnd.Buddy;

public class AutoOath extends Thread{
	public String scriptName = "Auto Oath";
	public String[] options = {
		"Auto Oath Green and Light Blue", "Auto Oath Without Filter", "Auto Oath For 30sec",
	};
	
	HavenUtil m_util;
	boolean m_toggle;
	boolean m_unload;
	int m_option;
	static final Pattern findName = Pattern.compile("(.+)\\shas.+");
	int m_myID = 0;
	int m_winID = -2;
	int[] m_groupTypes = new int[]{1, 4};
	
	public void ApocScript(HavenUtil util, int option, String modify){
		m_util = util;
		m_option = option;
	}
	
	/*public AutoOath(HavenUtil util, int modify){
		m_util = util;
		//m_toggle = toggle;
		//m_unload = unload;
		m_modify = modify;
	}*/
	
	String getInviteText(){
		Widget root = m_util.m_ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if (!(w instanceof Window))
				continue;
			if(((Window)w).cap == null)
				continue;
			if(((Window)w).cap.text == null)
				continue;
			if(!((Window)w).cap.text.equals("Invitation"))
				continue;
			
			for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
				if(wdg instanceof Label){
					return ((Label)wdg).texts;
				}
			}
		}
		
		return null;
	}
	
	Partyview getParty(){
		Widget root = m_util.m_ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if (!(w instanceof Partyview))
				continue;
			
			return (Partyview)w;
		}
		
		return null;
	}
	
	String getName(String str){
		Matcher m = findName.matcher(str);
		String name = null;
		if(m.find() ){
			name = m.group(1);
			//System.out.println(name);
		}
		
		return name;
	}
	
	boolean groupCheck(int group){
		for(int i : m_groupTypes){
			if(group == i) return true;
		}
		
		return false;
	}
	
	boolean kinCheck(String name){
		if(name == null) return false;
		
		if(name != null){
			for(BuddyWnd.Buddy b : m_util.m_ui.uiThread.buddyWnd.buddies){
				if(m_groupTypes == null || b.name.text.equals(name) && groupCheck(b.group)/*( b.group == 1 || b.group == 4)*/ ){
					System.out.println("-- " + time() + " invite accepted from: " + name);
					return true;
				}
			}
		}
		System.out.println("====== " + time() + " invite REJECTED from: " + name);
		m_util.autoCloseWindow("Invitation");
		
		return false;
	}
	
	String time(){
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("'Time:' HH:mm:ss");;
		return sdf.format(cal.getTime());
	}
	
	/*boolean partyNameCheck(Party.Member m, String name){ // wont work
		Gob gob = m.getgob();
		if(gob == null)
			return false;
		KinInfo kin = gob.getattr(KinInfo.class);
		if(kin == null)
			return false;
		if(name.equals(kin.name) )
			return true;
		
		return false;
	}*/
	
	void invitePerson(String name){
		m_util.clickButton("Yes");
		Partyview p = null;
		while(p == null && !m_util.stop){
			p = getParty();
			m_util.wait(50);
			//System.out.println("pating for party ");
		}
		
		if(p == null) return;
		int partyBreakCount = 0;
		boolean inviteSent = false;
		while(partyBreakCount < 50 && !m_util.stop){
			if(p.party.leader != null){
				int id = p.party.leader.gobid;
				if(id != m_myID){
					m_util.sendAction("gov","join");
					p.wdgmsg("click", id, 1);
					m_util.clickWorld(3, Coord.z);
					partyBreakCount = 1000;
					inviteSent = true;
				}
			}
			/*for(Party.Member m : m_util.m_ui.sess.glob.party.memb.values()) {
				//System.out.println("member list " + m.gobid);
				if(m.gobid != m_myID && ){
					//System.out.println("oathing this guy ");
					//if(partySize() < 3 ){
						m_util.sendAction("gov","join");
						p.wdgmsg("click", m.gobid, 1);
						m_util.clickWorld(3, Coord.z);
						partyBreakCount = 1000;
						inviteSent = true;
					//}
				}
			}*/
			partyBreakCount++;
			m_util.wait(50);
		}
		
		int waitCount = 0;
		while(inviteSent && waitCount < 100 && !m_util.stop){
			waitCount++;
			m_util.wait(50);
			if(m_util.windowOpen("Invitation")){
				String s = getInviteText();
				if(s != null && s.contains("did not swear the oath.") ) break;
				if(s != null && s.contains("sworn allegiance.") ) break;
			}
		}
		
		if(!m_util.stop) p.wdgmsg("leave");
		m_winID = m_util.windowID("Invitation");
		m_util.autoCloseWindow("Invitation");
		//System.out.println("End ");
		
		while( partySize() > 1 && !m_util.stop){
			m_util.wait(50);
		}
		
		//System.out.println("end of party ");
	}
	
	int partySize(){
		return m_util.m_ui.sess.glob.party.memb.size();
	}
	
	void autoOath(){
		while(!m_util.stop){
			if(m_util.windowOpen("Invitation") && m_winID != m_util.windowID("Invitation")){
				m_winID = m_util.windowID("Invitation");
				String s = getInviteText();
				if(s != null && s.contains("party. Do you wish to do so?") ){
					String name = getName(s);
					if(kinCheck(name))
						invitePerson(name);
				}else{
					m_util.autoCloseWindow("Invitation");
				}
			}
			m_util.wait(50);
		}
	}
	
	public void run(){
		m_myID = m_util.getPlayerGob().id;
		
		if(m_option == 2) m_groupTypes = null;
		if(m_option == 3) new stopWatch().start();
		
		autoOath();
		
		m_util.running(false);
	}
	
	private class stopWatch extends Thread{
		public void run(){
			int count = 0;
			while(!m_util.stop && count < 300){
				m_util.wait(100);
				count++;
			}
			
			m_util.stop = true;
		}
	}
}