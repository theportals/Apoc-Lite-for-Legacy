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

import haven.Fightview.Relation;
import haven.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.net.URI;
import java.awt.Desktop;

public class BlueLightning extends Thread{
	HavenUtil m_util;
	
	public BlueLightning(HavenUtil util){
		m_util = util;
	}
	
	Task findCurrentTask(){
		if(m_util.m_ui.fight == null || m_util.m_ui.fight.current == null) return null;
		if(System.currentTimeMillis() < m_util.m_ui.fight.atkc) return null;
		
		Gob target = m_util.findObjectByID(m_util.m_ui.fight.current.gobid);
		if(m_util.getPlayerCoord().dist(target.getr()) > 295) return null;
		
		if(m_util.m_ui.fight.batk != null){
			if(m_util.m_ui.fight.batk.get().name.endsWith("thunder") ) return null;
			return new Task(m_util.m_ui.fight.batk.get().name);
		}
		if(m_util.m_ui.fight.iatk != null) return new Task(m_util.m_ui.fight.iatk.get().name, 3);
		
		Gob player = m_util.getPlayerGob();
		
		Moving moveType = player.getattr(Moving.class);
		if(moveType == null) return new Task();
		if(moveType instanceof Following || moveType instanceof Homing) return null;
		
		if(moveType instanceof LinMove) return new Task( ((LinMove)moveType).t );
		
		return null;
	}
	
	void clickWorldObject(int button, Gob object){
		m_util.m_ui.mainview.wdgmsg("click", new Coord(200,150), object.getr(), button, 0, object.id, object.getr());
	}
	
	public void sendAction(String str1, String str2){
		String[] action = {str1, str2};
		m_util.m_ui.mnu.wdgmsg("act", (Object[])action);
	}
	
	void thunderTarget(Task task){
		sendAction("atk", "thunder");
		if(task.type != 2){
			Gob target = m_util.findObjectByID(m_util.m_ui.fight.current.gobid);
			if(target != null) clickWorldObject(1, target);
		}
	}
	
	String getAttack(Task t){
		/*return "knockteeth"; // khto
		return "baseaxe"; // chop
		return "cleave";
		return "pow"; // punch
		return "sting";
		return "strangle";
		return "valstr"; // vallor
		
		return "berserk"; // charge
		return "dash";
		return "feignflight";
		return "feignflight";
		return "flex";
		return "butterfly";
		return "jump";
		return "advpush"; // push advantage
		return "seize"; // seize the day
		return "slide";
		return "throwsand";
		
		return "roar"; // battle cry
		return "fcons"; // consume the flames
		return "bloodshot"; // eye
		return "fflame"; // fan of the flames
		return "skuld"; // invocation of skuld
		return "paingain"; // no pain
		return "oppknock";
		return "sidestep";
		return "sternorder";
		return "bee"; // sting like a bee
		return "toarms";*/
		
		int index = 0;
		int add = 0;
		String s = t.job;
		while(index != -1){
			s = s.substring(index + add);
			add = 1;
			index = s.indexOf('/');
		}
		return s;
	}
	
	void continueLastTask(Task task){
		m_util.clickWorld(3, m_util.getPlayerCoord());
		if(task.type == 1){
			m_util.clickWorld(1, task.to);
		}else if(task.type == 2 || task.type == 3){
			String attackType = getAttack(task);
			sendAction("atk", attackType);
			if(m_util.m_ui.mnu != null) m_util.m_ui.mnu.moveOn = true;
		}
	}
	
	void blueThunder(){
		Task task = findCurrentTask();
		
		if(task == null) return;
		
		thunderTarget(task);
		
		continueLastTask(task);
	}
	 
	public void run(){
		blueThunder();
	}
	
	private class Task{
		Coord to;
		String job;
		int type;
		
		public Task(){}
		
		public Task(Coord c){
			type = 1;
			to = c;
		}
		
		public Task(String s){
			type = 2;
			job = s;
		}
		
		public Task(String s, int i){
			type = 3;
			job = s;
		}
	}
}