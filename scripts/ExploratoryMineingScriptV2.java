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

import addons.*;
import haven.*;

import java.util.ArrayList;

public class ExploratoryMineingScriptV2 extends Thread{
	public String scriptName = "Tunneling Script";
	
	HavenUtil m_util;
	int m_option;
	String m_modify;
	
	Coord m_p1;
	Coord m_p2;
	Coord m_direction;
	Coord m_startC;
	boolean troll = false;
	
	/*public ExploratoryMineingScriptV2(HavenUtil util, Coord p1, Coord p2){
		m_util = util;
		m_p1 = p1;
		m_p2 = p2;
	}*/
	
	public void ApocScript(HavenUtil util, int option, String modify){
		m_util = util;
		m_option = option;
		m_modify = modify;
		m_p1 = m_util.m_pos1;
		m_p2 = m_util.m_pos2;
	}
	
	Coord findDirection(Coord p1, Coord p2){
		int smallestX = p1.x;
		int largestX = p2.x;
		
		if(p2.x < p1.x){
			smallestX = p2.x;
			largestX = p1.x;
		}
		int smallestY = p1.y;
		int largestY = p2.y;
		if(p2.y < p1.y){
			smallestY = p2.y;
			largestY = p1.y;
		}
		
		p1 = new Coord(smallestX, smallestY);
		p2 = new Coord(largestX, largestY);
		
		Coord c = new Coord(m_util.getPlayerCoord());	
		
		if(c.x >= p1.x && c.x <= p2.x){
			if(c.y < p1.y && c.y < p2.y)
				return new Coord(0,1); // south
			else
				return new Coord(0,-1); // north
		}else if(c.y >= p1.y && c.y <= p2.y){
			if(c.x < p1.x && c.x < p2.x)
				return new Coord(1,0); // east
			else
				return new Coord(-1,0); // west
		}
		
		//if(p1.x <= c.x && c.x <= p2.x && p1.y <= c.y && c.y <= p2.y){
		if(false){
			if(c.x < p1.x && c.y < p1.y){
				return new Coord(1,1); // south east
			}else if(c.x > p2.x && c.y > p2.y){
				return new Coord(-1,-1); // north west
			}else if(c.x > p2.x && c.y < p1.y){
				return new Coord(-1,1); // south west
			}else if(c.x < p1.x && c.y > p2.y){
				return new Coord(1,-1); // north east
			}
		}
		
		m_util.stop = true;
		System.out.println("No direction found.");
		return new Coord(0,0);
	}
	
	void pathSafeWalk(Coord c){
		while(!m_util.stop && !m_util.getPlayerCoord().equals(c) ){
			m_util.walkTo(c);
			m_util.wait(200);
		}
	}
	
	void mineLoop(){
		while(!m_util.stop && !troll){
			Gob support = null;
			while(!m_util.stop && support == null){
				support = m_util.findClosestObject("gfx/terobjs/mining/minesupport", 60);
			}
			
			if(support == null) return;
			
			m_startC = support.getr();
			
			Coord c = m_startC.add(m_direction.abs().swap().mul(11) );
			Coord ss = c;
			
			pathSafeWalk(c);
			
			Coord startShaft = c.add(m_direction.mul(11*1) );
			Coord endShaft = c.add(m_direction.mul(11*7) );
			
			mine(startShaft, endShaft, ss);
			
			if(m_util.stop || troll) return;
			
			m_util.goToWorldCoord(endShaft.add(m_direction) );
			if(checkTrolls()) return;
			m_util.goToWorldCoord(endShaft.add(m_direction.mul(2) ) );
			if(checkTrolls()) return;
			m_util.goToWorldCoord(endShaft.add(m_direction.mul(3) ) );
			if(checkTrolls()) return;
			
			Coord startCube = c.add(m_direction.mul(11*8) );
			Coord endCube = m_startC.add(m_direction.mul(11*9) );
			
			mine(startCube, endCube, ss);
			nudge();
			
			Gob sign = null;
			while(!m_util.stop && sign == null){
				sign = m_util.findClosestObject("sign", endCube, endCube);
				if(checkTrolls()) return;
			}
			
			if(sign == null) return;
			
			helpBuild(sign);
		}
	}
	
	void nudge(){
		if(m_direction.x == 0){
			m_util.walkTo(m_util.getPlayerCoord().add(6,0) );
		}else{
			m_util.walkTo(m_util.getPlayerCoord().add(0,6) );
		}
	}
	
	boolean checkTrolls(){
		troll = m_util.findClosestObject("gfx/kritter/troll/s") != null;
		
		if(troll){
			Sound.troll.play();
			troll = true;
			return true;
		}
		
		return false;
	}
	
	void helpBuild(Gob sign){
		String signName = "Mine Support";
		
		m_util.clickWorldObject(3, sign);
		
		while(!m_util.windowOpen(signName) && !m_util.stop) m_util.wait(200);
		
		m_util.buttonActivate(signName);
		
		int hourglassCounter = 0;
		while(m_util.windowOpen(signName) && !m_util.stop){
			m_util.wait(200);
			
			if(!m_util.hasHourglass() ){
				hourglassCounter++;
				if(hourglassCounter > 10){
					m_util.buttonActivate(signName);
					hourglassCounter = 0;
				}
			}else{
				hourglassCounter = 0;
			}
		}
	}
	
	void mine(Coord from, Coord to, Coord ss){
		MineScriptV3 mine = new MineScriptV3();
		mine.m_util = m_util;
		mine.m_ignoreOre = true;
		mine.m_open = m_util.getVoidTiles(from, to);
		mine.miningDrawer = m_util.addScriptDrawer();
		mine.autoNodeMiner();
		troll = mine.troll;
	}
	
	//gfx/arch/cabin-door2
	void trollConditions(){
		m_util.setPlayerSpeed(3);
		m_util.pathDrinker = true;
		m_util.startRunFlask();
		//boolean repath = true;
		boolean safetyFound = false;
		while(!safetyFound && !m_util.stop){
			//repath = false;
			
			Coord c = m_util.getPlayerCoord().add(m_direction.inv().mul(11*60));
			
			m_util.walkToCondition(c );
			
			while(m_util.PFrunning && !m_util.stop && !safetyFound){
				m_util.wait(100);
				
				if(m_util.findClosestObject("gfx/arch/cabin-door2") != null){
					safetyFound = true;
					m_util.pathing = false;
				}else if(m_util.findClosestObject("gfx/terobjs/mining/ladder") != null){
					safetyFound = true;
					m_util.pathing = false;
				}else if(m_util.getPlayerCoord().dist(c) < 100 ){
					//repath = true;
					m_util.pathing = false;
				}
			}
		}
		
		Gob safeEject = m_util.findClosestObject("gfx/arch/cabin-door2");
		if(safeEject == null) safeEject = m_util.findClosestObject("gfx/terobjs/mining/ladder");
		
		while(m_util.PFrunning && !m_util.stop) m_util.wait(20);
		
		while(!m_util.stop){
			m_util.walkTo(safeEject);
			if(m_util.findClosestObject("gfx/kritter/troll/s") == null) return;
			m_util.wait(100);
		}
	}
	
	public void run(){
		m_util.openInventory();
		m_util.setPlayerSpeed(2);
		m_direction = new Coord(findDirection(m_p1, m_p2));
		
		if(!m_util.stop) mineLoop();
		if(troll){
			m_util.stop = false;
			trollConditions();
		}
		
		m_util.running(false);
	}
}