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

// WARNING! do not use this outdated code as a good source for scripting.

import haven.*;
import addons.*;

import java.util.ArrayList;

public class PlowScript extends Thread{
	public String scriptName = "Wooden Plower Script";
	HavenUtil m_util;
	public boolean m_farmToggle;
	Coord pos1, pos2;
	int m_pause = 185;
	
	ArrayList<Coord> m_plowList = new ArrayList<Coord>();
	Gob m_plowGob;
	Coord m_originalPlowSpot = new Coord();
	Coord m_oldTile = new Coord();
	
	int m_option;
	String m_modify;
	
	public void ApocScript(HavenUtil util, int option, String modify){
		m_util = util;
		m_option = option;
		m_modify = modify;
	}
	
	public void run(){
		plower(m_util.m_pos1, m_util.m_pos2);
		
		m_util.running(false);
	}
	
	public void plower(Coord p1, Coord p2){
		sortCoords(p1, p2);
		int plowListSize = 1;
		boolean reverse = false;
		
		if(findPlow()){
			Coord lastFixCoord = new Coord(0,0);
			int breakCount = 0;
			boolean first = true;
			
			while(plowListSize > 0 && !m_util.stop){
				getPlowTiles(reverse);
				if(!first || m_plowList.size() < 1) plowListSize = m_plowList.size();
				//System.out.println("Plow list size: "+plowListSize);
				
				pickUpPlow(false);
				
				for(int i = 0; i < plowListSize && !m_util.stop; i++)
				{
					pickUpPlow(true);
					Coord tc = new Coord(m_plowList.get(i));
					//if(!lastFixCoord.equals(tc))
					plowSpot(tc);
					m_oldTile =  tc;
				}
				
				//if(plowListSize > 0)
				//	lastFixCoord = m_plowList.get(plowListSize - 1);
				
				m_util.wait(1000);
				
				m_plowList.clear();
				breakCount++;
				if(breakCount >= 5)
					break;
				
				if(!first) reverse = !reverse;
				
				first = false;
			}
			
			pickUpPlow(false);
			
			if(!m_util.stop && !m_farmToggle){
				//System.out.println("Finished plowing, going home.");
				//m_util.goToWorldCoord(m_originalPlowSpot.add(0,-5));
				if(!m_util.stop) m_util.clickWorld(3, m_originalPlowSpot);
				m_util.wait(200);
				if(!m_util.stop) m_util.clickWorld(3, m_originalPlowSpot);
			}
		}
	}
	
	boolean findPlow(){
		if(!m_farmToggle)
			m_plowGob = m_util.findClosestObject("plow");
		else
			m_plowGob = m_util.findClosestObject("plow", pos1, pos2.add(11,1000));
		if(m_plowGob != null){
			m_originalPlowSpot = m_plowGob.getr();
			return true;
		}
		return false;
	}
	
	void getPlowTiles(boolean reverse){
		m_plowList = m_util.getPlowTiles(pos1, pos2, reverse);
	}
	
	void plowSpot(Coord tc){
		Coord c = new Coord();
		Coord d = new Coord();
		Coord e = new Coord();
		Coord f = new Coord();
		boolean plowable = true;
		
		
		if(tileTest(tc.add(0,1) ) ){
			c = tc.mul(11);
			d = c.add(5,5);
			e = d.add(0,5);
			f = d.add(0,6);
		}  else if(tileTest(tc.add(1,0) ) ){
			c = tc.mul(11);
			d = c.add(5,5);
			e = d.add(5,0);
			f = d.add(6,0);
		} else if(tileTest(tc.add(-1,0) ) ){
			c = tc.mul(11);
			d = c.add(5,5);
			e = d.add(-5,0);
			f = d.add(-6,0);
		} else if( tileTest(tc.add(0,-1) ) ) {
			c = tc.mul(11);
			d = c.add(5,5);
			e = d.add(0,-5);
			f = d.add(0,-6);
		}else {plowable = false; }
		
		// -------------
		
		if (plowable){
			boolean redo = true;
			while(redo && !m_util.stop ){
				boolean redo2 = true;
				while(redo2 && !m_util.stop){
					redo2 = false;
					if(!fastMove(d)){
						pickUpPlow(false);
						
						if(!tc.equals(m_oldTile) && m_util.getTileID(m_oldTile) != 9 ){
							plowSpot(m_oldTile);
						}
						pickUpPlow(false);
						
						m_util.walkTo(d);
					}
					
					m_util.clickWorld(3, e);
					
					m_util.wait(m_pause);
					
					if(!d.equals(m_util.getPlayerCoord())){
						pickUpPlow(false);
						redo2 = true;
					}
					
				}
				
				m_util.wait(30);
				m_util.clickWorldObject(3, m_plowGob);
				m_util.wait(m_pause+30);
				
				m_util.clickWorld(1, f);
				
				int frameCounter = 0;
				
				int plowCount = 0;
				int count = 0;
				while(!m_util.stop){
					redo = false;
					if(m_util.slenError().contains("This plow is too poor condition to be used.")){
						repairPlow();
						redo = true;
						break;
					}
					
					m_util.wait(10);
					
					if(plowWalking() ){
						int pause = 300;
						if(pause > 0) m_util.wait(pause);
						break;
					}else if(count > 70){
						if(m_util.checkPlayerCarry() ){
							redo = true;
							break;
						}
						count = 0;
						m_util.clickWorldObject(3, m_plowGob);
						m_util.wait(m_pause+100);
						m_util.clickWorld(1, f);
					}
					count++;
				}
			}
		}
	}
	
	boolean plowWalking(){
		for(String s : m_plowGob.resnames() ){
			if(s.contains("/walking/") )
				return true;
		}
		
		return false;
	}
	
	boolean fastMove(Coord c){
		Coord mc = new Coord(m_util.getPlayerCoord());
		boolean redo = true;
		Coord error = new Coord(m_util.getPlayerCoord());
		
		if(!mc.equals(c)){
			if(!m_util.freePath(m_util.getPlayerCoord(), c, false) ){
				m_util.getPlayerGob().miniwalk = true;
				m_util.pathing = false;
				while(m_util.PFrunning && !m_util.stop) m_util.wait(100);
				m_util.walkToCondition(c);
				
				int redoCount = 0;
				int liftCount = 0;
				int errorCount = 0;
				while(m_util.PFrunning && !m_util.stop){
					m_util.wait(50);
					if(!m_util.getPlayerGob().miniwalk) liftCount++;
					if(liftCount > 3 && !m_util.checkPlayerCarry()) return false;
					
					if(!m_util.getPlayerGob().miniwalk && !m_util.checkPlayerWalking() && !m_util.getPlayerCoord().equals(c)){
						redoCount++;
						if(redoCount > 1000)
							break;
					}else{
						redoCount = 0;
					}
					
					if(m_util.getPlayerCoord().equals(error) ) errorCount++;
					if(errorCount > 50) return false;
				}
				
				if(!m_util.getPlayerCoord().equals(c) ) redo = true;
			}else{
				while(redo && !m_util.stop){
					redo = false;
					m_util.getPlayerGob().miniwalk = true;
					m_util.clickWorld(1, c);
					
					int redoCount = 0;
					int liftCount = 0;
					int errorCount = 0;
					while((m_util.checkPlayerWalking() || !m_util.getPlayerCoord().equals(c)) && !m_util.stop){
						m_util.wait(50);
						if(!m_util.getPlayerGob().miniwalk){
							liftCount++;
						}
						
						if(liftCount > 3 && !m_util.checkPlayerCarry()) return false;
						
						if(!m_util.getPlayerGob().miniwalk && !m_util.checkPlayerWalking() && !m_util.getPlayerCoord().equals(c)){
							redoCount++;
							if(redoCount > 1000)
								redo = true;
						}else{
							redoCount = 0;
						}
						
						if(m_util.getPlayerCoord().equals(error) ) errorCount++;
						if(errorCount > 50) return false;
					}
				}
			}
		}
		if(!m_util.checkPlayerCarry()) return false;
		return true;
	}
	
	void repairPlow(){
		m_util.openInventory();
		if(m_util.countItemsInBag("wood") < 1) getBlocks();
		
		m_util.sendAction("repair");
		while(!(m_util.getCursor().contains("wrench")) && !m_util.stop) { m_util.wait(200); }
		Coord c = new Coord(m_util.getPlayerCoord());
		m_util.clickWorldObject(1, m_plowGob);
		while(m_util.getCursor().contains("wrench") && !m_util.stop) { m_util.wait(200); }
		
		int count = 0;
		while(m_util.getPlayerCoord().equals(c) && !m_util.stop && count < 5) { 
			m_util.wait(200); 
			count++;
		}
		while(m_util.checkPlayerWalking() && !m_util.stop) { m_util.wait(200); }
		
		m_util.sendSlenMessage("Plow repared.");
		
		pickUpPlow(false);
	}
	
	void sortCoords(Coord p1, Coord p2){
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
		
		pos1 = new Coord(smallestX, smallestY);
		pos2 = new Coord(largestX, largestY);
	}
	
	void getBlocks(){
		ArrayList<Gob> list = new ArrayList<Gob>();
		list = m_util.getObjects("sign", pos1 ,pos2.add(0,1000));
		goToClosestSign(list);
		signTransferToFrom();
		
		m_util.closeWindow("Palisade Cornerpost");
		while(m_util.windowOpen("Palisade Cornerpost") && !m_util.stop){ m_util.wait(100);}
	}
	
	void signTransferToFrom(){
		while(!m_util.windowOpen("Palisade Cornerpost") && !m_util.stop){ m_util.wait(100);}
		m_util.signTransfer(-2, "Palisade Cornerpost");
	}
	
	void goToClosestSign(ArrayList<Gob> list){
		Gob sign = null;
		double min = 1000;
		
		//System.out.println("List size: "+list.size());
		
		for(Gob i : list){
			if(m_util.getPlayerCoord().dist(i.getr()) < min){
				sign = i;
				min = m_util.getPlayerCoord().dist(i.getr());
			}
		}
		
		m_util.clickWorld(3, m_util.getPlayerCoord() );
		m_util.wait(200);
		m_util.walkTo(sign);
	}
	
	boolean	tileTest(Coord c){
		int tileID = m_util.getTileID(c);
		if(tileID >= 9 && tileID <=15){return true; }
		return false;
	}
	
	void pickUpPlow(boolean fast){
		if(!m_util.checkPlayerCarry()){
			if(!m_util.stop) m_util.sendAction("carry");
			
			if(!m_util.stop) m_util.clickWorldObject(1, m_plowGob);
			if(fast)
				m_util.wait(m_pause+60);
			else
				while( !m_util.checkPlayerCarry() && !m_util.stop){ m_util.wait(200); }
		}
	}
	
	double getPlowDistance(){
		return m_util.getPlayerCoord().dist(m_plowGob.getr() );
	}
}