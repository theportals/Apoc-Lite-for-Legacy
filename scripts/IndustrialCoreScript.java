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
import java.io.*;

import haven.Gob;
import haven.Coord;
import haven.ISBox;
import haven.Inventory;
import haven.Item;
import haven.Shopbox;

public class IndustrialCoreScript extends Thread{
	public String scriptName = "Industrial Smelter";
	public String[] options = {
		"Run Smelters", "Run Forges", "Sort WI", "Sort Cast", "Run Smelters (fill cubs)", "Clear Memory"
	};
	
	HavenUtil m_util;
	int m_option;
	String m_modify;
	
	boolean m_toggel;
	int m_num;
	Coord m_upperSpot = new Coord();
	Coord m_upperSpotForge = new Coord();
	ArrayList<Gob> m_smelterList = new ArrayList<Gob>();
	ArrayList<Gob> m_forgeList = new ArrayList<Gob>();
	ArrayList<Gob> m_cubList = new ArrayList<Gob>();
	ArrayList<Gob> m_cubListBloom = new ArrayList<Gob>();
	ArrayList<Gob> m_cubListNugget = new ArrayList<Gob>();
	ArrayList<Gob> m_cubListBars = new ArrayList<Gob>();
	Coord m_restCast = new Coord();
	int m_coalCub = 0;
	int m_coalCubForge = 0;
	int m_bloomCub = 0;
	int m_nuggetCub = 0;
	int m_barsCub = 0;
	boolean m_barLoop = false;
	int m_rowSize = 0;
	int m_rowForgeSize = 0;
	ArrayList<sRow> m_rowManager = new ArrayList<sRow>();
	ArrayList<sRow> m_rowManagerForge = new ArrayList<sRow>();
	
	boolean m_fillCubsDirectly = false;
	
	boolean m_highQSmelt = false;
	
	public void ApocScript(HavenUtil util, int option, String modify){
		m_util = util;
		m_option = option;
		m_modify = modify;
	}
	
	/*public IndustrialCoreScript(HavenUtil util, int directive, boolean toggle, int num){
		m_util = util;
		m_toggel = toggle;
		m_directive = directive;
		m_num = num;
	}*/
	
	void loadRows(){
		m_smelterList = getSmelters();
		m_forgeList = getForges();
		
		Coord p1 = new Coord(m_upperSpot.add(-11*33,-11));
		Coord p2 = new Coord(m_upperSpot.add(11,-11));
		m_cubList = getCubs(p1, p2);
		
		p1 = new Coord(m_upperSpotForge.add(-11*3,11*13));
		p2 = new Coord(m_upperSpotForge.add(-11*3,11*26));
		m_cubListBloom = getCubs(p1, p2);
		
		p1 = new Coord(m_upperSpot.add(11*3,11*1));
		p2 = new Coord(m_upperSpot.add(11*3,11*25));
		m_cubListNugget = getCubs(p1, p2);
		
		p1 = new Coord(m_upperSpotForge.add(-11*0,11*13));
		p2 = new Coord(m_upperSpotForge.add(-11*0,11*26));
		m_cubListBars = getCubs(p1, p2);
		
		m_restCast = new Coord(m_upperSpotForge.add(-11*3,11*2));
		
		if(m_util.stop) return;
		
		for(int i = 0; i < (m_smelterList.size() / m_rowSize); i++){
			m_rowManager.add(new sRow(0,0,0));
		}
		for(int i = 0; i < (m_forgeList.size() / m_rowForgeSize); i++){
			m_rowManagerForge.add(new sRow(0,0,0));
		}
		
		loadSettings();
		//System.out.println("m_forgeList "+m_forgeList.size());
		//System.out.println("m_rowForgeSize "+m_rowForgeSize);
	}
	
	ArrayList<Gob> getCubs(Coord p1, Coord p2){
		ArrayList<Gob> unsortedCubs = new ArrayList<Gob>();
		ArrayList<Gob> cubList = new ArrayList<Gob>();
		
		
		//while(unsortedCubs.size() != 44 && !m_util.stop){
			unsortedCubs = m_util.getObjects("gfx/terobjs/cupboard", p1, p2);
		//	m_util.wait(200);
		//}
		
		cubList = m_util.superSortGobList(unsortedCubs, true, false, true);
		
		return cubList;
	}
	
	ArrayList<Gob> getSmelters(){
		ArrayList<Gob> unsortedList = new ArrayList<Gob>();
		ArrayList<Gob> filterdList = new ArrayList<Gob>();
		ArrayList<Gob> sortedList = new ArrayList<Gob>();
		
		while(filterdList.size() != 48 && !m_util.stop){
			unsortedList.clear();
			filterdList.clear();
			sortedList.clear();
			m_util.wait(200);
			
			unsortedList = m_util.getObjects("gfx/terobjs/smelter");
			
			Gob northEastGob = null;
			for(Gob g : unsortedList){
				if(northEastGob == null){
					northEastGob = g;
				}else if(northEastGob.getr().y >= g.getr().y && northEastGob.getr().x <= g.getr().x){
					northEastGob = g;
				}
			}
			
			m_upperSpot = northEastGob.getr().sub(0,22);
			
			for(Gob g : unsortedList){
				if(g.getr().y > m_upperSpot.y)
					filterdList.add(g);
			}
			
			Gob rowGob = null;
			for(Gob g : filterdList){
				if(rowGob == null){
					rowGob = g;
				}
				if(rowGob.getr().x == g.getr().x){
					m_rowSize++;
				}
			}
			
			//System.out.println(m_rowSize);
		}
		
		sortedList = m_util.superSortGobList(filterdList, true, false, true);
		
		if(m_highQSmelt){
			int QsmelterAmount = 12;
			
			ArrayList<Gob> Qlist = new ArrayList<Gob>();
			int start = sortedList.size() - m_rowSize;
			
			for(int Qnum = start; Qnum < start + QsmelterAmount; Qnum++){
				Gob Qsmelter = sortedList.get(Qnum);
				Qlist.add(Qsmelter);
			}
			
			m_rowSize  = QsmelterAmount;
			sortedList.clear();
			sortedList.addAll(Qlist);
			System.out.println(Qlist.size() );
		}
		
		return sortedList;
	}
	
	ArrayList<Gob> getForges(){
		ArrayList<Gob> unsortedList = new ArrayList<Gob>();
		ArrayList<Gob> filterdList = new ArrayList<Gob>();
		ArrayList<Gob> sortedList = new ArrayList<Gob>();
		
		while(filterdList.size() != 20 && !m_util.stop){
			unsortedList.clear();
			filterdList.clear();
			sortedList.clear();
			m_util.wait(200);
			
			unsortedList = m_util.getObjects("gfx/terobjs/fforge");
			
			Gob northEastGob = null;
			for(Gob g : unsortedList){
				if(northEastGob == null){
					northEastGob = g;
				}else if(northEastGob.getr().y >= g.getr().y && northEastGob.getr().x <= g.getr().x){
					northEastGob = g;
				}
			}
			
			m_upperSpotForge = northEastGob.getr().sub(0,33);
			
			for(Gob g : unsortedList){
				if(g.getr().y > m_upperSpotForge.y)
					filterdList.add(g);
			}
			
			Gob rowGob = null;
			for(Gob g : filterdList){
				if(rowGob == null){
					rowGob = g;
				}
				if(rowGob.getr().x == g.getr().x){
					m_rowForgeSize++;
				}
			}
			
			//System.out.println(m_rowForgeSize);
		}
		
		sortedList = m_util.superSortGobList(filterdList, true, true, true);
		
		if(m_highQSmelt){
			int QforgeAmount = 5;
			
			ArrayList<Gob> Qlist = new ArrayList<Gob>();
			//int start = sortedList.size() - m_rowSize;
			int start = 0;
			
			for(int Qnum = start; Qnum < start + QforgeAmount; Qnum++){
				Gob Qforge = sortedList.get(Qnum);
				Qlist.add(Qforge);
			}
			
			m_rowForgeSize  = QforgeAmount;
			sortedList.clear();
			sortedList.addAll(Qlist);
			System.out.println(Qlist.size() );
		}
		
		return sortedList;
	}
	
	Gob goToSmelter(int num){
		int xmul = 12;
		int smelterSize = 12;
		int rowWidth = 32;
		
		//System.out.println(num);
		
		if(m_util.stop) return null;
		
		if(num < 0 || num >= m_smelterList.size())
			return null;
		
		Gob smelter = m_smelterList.get(num);
		
		if(smelter.getr().x > m_util.getPlayerCoord().x){
			xmul = xmul * -1;
		}
		
		if(m_upperSpot.add(-11*13-3,0).x > m_util.getPlayerCoord().x){
			//m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
			m_util.goToWorldCoord(new Coord(smelter.getr().add(xmul,0).x, m_util.getPlayerCoord().y) );
		}else if(smelter.getr().add(rowWidth, 0).x < m_util.getPlayerCoord().x || smelter.getr().sub(rowWidth, 0).x > m_util.getPlayerCoord().x){
			m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
			m_util.goToWorldCoord(new Coord(smelter.getr().add(xmul,0).x, m_util.getPlayerCoord().y) );
		}else if(smelter.getr().add(smelterSize, 0).x > m_util.getPlayerCoord().x && smelter.getr().sub(smelterSize, 0).x < m_util.getPlayerCoord().x){
			//m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
			m_util.goToWorldCoord(new Coord(smelter.getr().add(xmul,0).x, m_util.getPlayerCoord().y) );
		}
		
		m_util.goToWorldCoord(smelter.getr().add(xmul,0) );
		if(m_util.stop) return null;
		
		while(m_util.windowOpen("Ore Smelter") && !m_util.stop){ m_util.wait(50);}
		m_util.clickWorldObject(3, smelter);
		//while(m_util.getInventory("Ore Smelter") == null && !m_util.stop){ m_util.wait(100);}
		return smelter;
	}
	
	Gob goToForge(int num){
		int xmul = 7;
		int forgeSize = 7;
		int rowWidth = 26;
		
		//System.out.println(num);
		
		if(m_util.stop) return null;
		
		if(num < 0 || num >= m_forgeList.size())
			return null;
		
		Gob forge = m_forgeList.get(num);
		
		if(forge.getr().x > m_util.getPlayerCoord().x){
			xmul = xmul * -1;
		}
		
		if(m_upperSpot.sub(13*11-1,0).x < m_util.getPlayerCoord().x || m_upperSpot.sub(18*11+1,0).x > m_util.getPlayerCoord().x){
			m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
			m_util.goToWorldCoord(new Coord(forge.getr().add(xmul,0).x, m_util.getPlayerCoord().y) );
		}else if(forge.getr().add(rowWidth, 0).x < m_util.getPlayerCoord().x || forge.getr().sub(rowWidth, 0).x > m_util.getPlayerCoord().x){
			m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
			m_util.goToWorldCoord(new Coord(forge.getr().add(xmul,0).x, m_util.getPlayerCoord().y) );
		}else if(forge.getr().add(forgeSize, 0).x > m_util.getPlayerCoord().x && forge.getr().sub(forgeSize, 0).x < m_util.getPlayerCoord().x){
			//m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
			m_util.goToWorldCoord(new Coord(forge.getr().add(xmul,0).x, m_util.getPlayerCoord().y) );
		}
		
		m_util.goToWorldCoord(forge.getr().add(xmul,0) );
		if(m_util.stop) return null;
		
		while(m_util.windowOpen("Finery Forge") && !m_util.stop){ m_util.wait(50);}
		m_util.clickWorldObject(3, forge);
		//while(m_util.getInventory("Ore Smelter") == null && !m_util.stop){ m_util.wait(100);}
		return forge;
	}
	
	boolean fillEmptySmelter(int type){
		String windowName = "Ore Smelter";
		String smeltRes = "gfx/invobjs/ore-iron";
		int size = 25;
		
		if(type == 2){
			windowName = "Finery Forge";
			smeltRes = "gfx/invobjs/bar";
			size = 9;
		}
		
		while(!m_util.windowOpen(windowName) && !m_util.stop){ m_util.wait(100);}
		if(m_util.stop) return true;
		
		Inventory invSmelter = m_util.getInventory(windowName);
		//ArrayList<Item> itemList = new ArrayList<Item>();
		while(invSmelter == null && !m_util.stop){
			m_util.wait(100);
			invSmelter = m_util.getInventory(windowName);
		}
		int itemListSize = m_util.getItemsFromInv(invSmelter).size();
		int bagOre = m_util.countItemsInBag(smeltRes);
		
		for(int i = 0; i < (size - itemListSize); i++){
			m_util.transferItemTo(invSmelter, 1);
		}
		
		if((bagOre - (size - itemListSize) ) < 1)
			return false;
		
		return true;
	}
	
	void coalSmelter(Gob smelter){
		while(m_util.getVmeterAmount(255, false) == -1 && !m_util.stop) m_util.wait(50);
		if(m_util.stop) return;
		
		if(m_util.getVmeterAmount(255, false) == 0){
			Item i = m_util.getItemFromBag("gfx/invobjs/coal");
			
			int items = m_util.countItemsInBag("gfx/invobjs/coal");
			
			if(!m_util.stop) m_util.pickUpItem(i);
			if(!m_util.stop) m_util.itemActionWorldObject(smelter, 0);
			
			while(m_util.countItemsInBag("gfx/invobjs/coal") == items || m_util.mouseHoldingAnItem() ) m_util.wait(200);
		}
	}
	
	void lightSmelter(int type){
		String windowName = "Ore Smelter";
		
		if(type == 2){
			windowName = "Finery Forge";
		}
		
		while(!m_util.windowOpen(windowName) && !m_util.stop){ m_util.wait(100);}
		if(m_util.stop) return;
		
		m_util.buttonActivate(windowName);
		m_util.wait(50);
		m_util.buttonActivate(windowName);
		m_util.wait(50);
		m_util.buttonActivate(windowName);
		m_util.wait(50);
		m_util.buttonActivate(windowName);
		m_util.wait(50);
		m_util.buttonActivate(windowName);
	}
	
	boolean emptyFinishedSmelter(int type){
		String windowName = "Ore Smelter";
		String waitRes = "gfx/invobjs/iron-ore";
		String dropRes = "gfx/invobjs/stone";
		String pickupRes = "gfx/invobjs/bar-";
		int typeSize = 25;
		
		if(type == 2){
			windowName = "Finery Forge";
			waitRes = "gfx/invobjs/bar";
			dropRes = "gfx/invobjs/slag";
			pickupRes = "gfx/invobjs/bloom";
			typeSize = 9;
		}
		
		while(!m_util.windowOpen(windowName) && !m_util.stop){ m_util.wait(100);}
		if(m_util.stop) return true;
		
		boolean invCheck = true;
		
		ArrayList<Item> itemList = new ArrayList<Item>();
		
		int count = 0;
		while(invCheck && count < 100 && !m_util.stop){
			invCheck = false;
			
			m_util.wait(50);
			Inventory invSmelter = m_util.getInventory(windowName);
			itemList = m_util.getItemsFromInv(invSmelter);
			
			if(itemList == null || itemList.size() < typeSize){
				invCheck = true;
				//System.out.println("IC detected a smelter with less then "+typeSize+" items. Item count: " + itemList.size());
			}
			count++;
		}
		
		if(m_util.stop)
			return true;
		
		int bagOre = m_util.getPlayerBagItems();
		int testOre = bagOre;
		
		for(Item i : itemList){
			if(i.GetResName().contains(pickupRes) || i.GetResName().contains("gfx/invobjs/nugget-")){
				bagOre++;
			}
		}
		
		if( m_util.getPlayerBagSize() <= bagOre)
			return false;
		
		for(Item i : itemList){
			if(i.GetResName().contains(dropRes) )
				m_util.dropItemOnGround(i);
			if(i.GetResName().contains(pickupRes) || i.GetResName().contains("gfx/invobjs/nugget-")){
				m_util.transferItem(i);
			}
		}
		
		return true;
	}
	
	void getCoal(int type){
		int coal = 0;
		ArrayList<Item> itemList = new ArrayList<Item>();
		
		int rSize = m_rowSize;
		if(type == 4) rSize = m_rowForgeSize;
		if(m_util.stop) return;
		
		m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
		
		while(coal < rSize && !m_util.stop){
			if(type == 1) goToCub(m_coalCub, type);
			if(type == 4) goToCub(m_coalCubForge, type);
			
			Inventory inv = null;
			while(inv == null && !m_util.stop){
				m_util.wait(100);
				inv = m_util.getInventory("Cupboard");
			}
			
			if(m_util.stop) return;
			
			boolean invCheck = true;
			while(invCheck && !m_util.stop){
				invCheck = false;
				m_util.wait(100);
				
				itemList = m_util.getItemsFromInv(inv);
				
				for(Item i : itemList){
					if(i.GetResName().contains("gfx/invobjs/missing"))
						invCheck = true;
				}
			}
			
			if(m_util.stop) return;
			
			for(Item i : itemList){
				if(coal < rSize && i.GetResName().contains("gfx/invobjs/coal")){
					m_util.transferItem(i);
					coal++;
				}
			}
			
			if(coal >= rSize)
				return;
			
			if(type == 1){
				m_coalCub++;
				
				if(m_coalCub >= m_cubList.size() ) m_coalCub = 0;
			}else if(type == 4){
				m_coalCubForge++;
				
				if(m_coalCubForge >= m_cubList.size() ) m_coalCubForge = 0;
			}
		}
	}
	
	void storeMats(int type){
		int items = m_util.getPlayerBagItems();
		Coord cMem = m_util.getPlayerCoord();
		boolean first = true;
		//ArrayList<Item> itemList = new ArrayList<Item>();
		
		if(m_util.stop) return;
		
		if(items > 0){
			if(type == 1 || type == 4) m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
			
			if(type == 3){
				m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
				m_util.goToWorldCoord(new Coord(m_upperSpot.add(40,0).x, m_util.getPlayerCoord().y));
			}
			
			while(!m_util.stop){
				if(type == 1) goToCub(m_coalCub, 1);
				if(type == 2) goToCub(m_bloomCub, 2);
				if(type == 3) goToCub(m_nuggetCub, 3);
				if(type == 4) goToCub(m_coalCub, 4);
				if(type == 5) goToCub(m_barsCub, 5);
				
				if(first){
					items = m_util.getPlayerBagItems();
					first = false;
				}
				
				Inventory inv = null;
				while(inv == null && !m_util.stop){
					m_util.wait(100);
					inv = m_util.getInventory("Cupboard");
				}
				
				m_util.wait(500);
				
				if(m_util.stop) return;
				
				items = items - m_util.getInvSpace(inv);
				
				for(int i = 0; i < 56 && !m_util.stop; i++)
					m_util.transferItemTo(inv, 1);
				
				if(items <= 0){
					if(type == 1) m_util.walkTo(cMem);
					return;
				}
				
				if(type == 1){
					m_coalCub--;
					
					if(m_coalCub < 0 ) m_coalCub = m_cubList.size()-1;
				}else if(type == 2){
					m_bloomCub++;
					
					if(m_bloomCub >= m_cubListBloom.size() ) m_bloomCub = 0;
				}else if(type == 3){
					m_nuggetCub++;
					
					if(m_nuggetCub >= m_cubListNugget.size() ) m_nuggetCub = 0;
				}else if(type == 4){
					m_coalCubForge--;
					
					if(m_coalCubForge < 0 ) m_coalCubForge = m_cubList.size()-1;
				}else if(type == 5){
					m_barsCub++;
					
					if(m_barsCub >= m_cubListBars.size() ) m_barsCub = 0;
				}
			}
		}
	}
	
	void goToCub(int cubNumber, int type){
		if(m_util.stop) return;
		Gob cub = null;
		if(type == 1) cub = m_cubList.get(cubNumber);
		if(type == 2) cub = m_cubListBloom.get(cubNumber);
		if(type == 3) cub = m_cubListNugget.get(cubNumber);
		if(type == 4) cub = m_cubList.get(m_cubList.size() - 1 - cubNumber);
		if(type == 5) cub = m_cubListBars.get(cubNumber);
		
		if(cub == null) return;
		
		Coord front = new Coord(cubFrontCoord(cub) );
		
		if(!m_util.stop) m_util.goToWorldCoord(front);
		while(m_util.windowOpen("Cupboard") && !m_util.stop) m_util.wait(100);
		m_util.clickWorldObject(3, cub);
	}
	
	public Coord cubFrontCoord(Gob cub){
		if( !cub.resname().equals("gfx/terobjs/cupboard") )
			return new Coord(0,0);
		
		int blob = cub.GetBlob(0);
		Coord cubCoard = new Coord(cub.getr() );
		
		if(blob == 1 || blob == 2)
			return cubCoard.add(0,7);
		if(blob == 4 || blob == 8)
			return cubCoard.add(-7,0);
		if(blob == 16 || blob == 32)
			return cubCoard.add(0,-7);
		if(blob == 64 || blob == -128)
			return cubCoard.add(7,0);
		
		return new Coord(0,0);
	}
	
	void manageRestCast(boolean dumpRests){
		Gob cub = m_util.findClosestObject("cupboard", m_restCast, m_restCast);
		
		Coord front = cubFrontCoord(cub);
		if(m_util.getPlayerCoord().x < front.x) front = front.add(-14,0);
		
		m_util.goToWorldCoord(front);
		
		while(m_util.windowOpen("Cupboard") && !m_util.stop){ m_util.wait(100);}
		m_util.clickWorldObject(3, cub);
		
		Inventory inv = null;
		while(inv == null && !m_util.stop){
			m_util.wait(100);
			inv = m_util.getInventory("Cupboard");
		}
		
		if(m_util.stop) return;
		
		for(int i = 0; i < 56; i++){
			if(dumpRests) m_util.transferItemTo(inv, 1);
			else m_util.transferItemFrom(inv, 1);
		}
	}
	
	boolean antiSign(){
		String signName = "Metal Cauldron";
		
		if(m_util.getPlayerBagSpace() <= 0 || m_util.stop) return true;
		
		m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpotForge.y));
		if(m_util.getPlayerBagSpace() <= 0 || m_util.stop) return true;
		
		m_util.goToWorldCoord(m_upperSpotForge.add(-4*11, 0) );
		if(m_util.getPlayerBagSpace() <= 0 || m_util.stop) return true;
		
		ArrayList<Gob> list = m_util.getObjects("sign", m_upperSpotForge.add(-4*11, 0), m_upperSpotForge.add(-42*11, 26*11) );
		ArrayList<Gob> signs = m_util.superSortGobList(list, true, false, true);
		int space = m_util.getPlayerBagSpace();
		
		for(Gob s : signs){
			if(m_util.getPlayerCoord().y > s.getr().y - 7 ){
				m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpotForge.y));
				//m_util.goToWorldCoord(new Coord(s.getr().x, m_util.getPlayerCoord().y));
			}
			
			m_util.clickWorldObject(3, s);
			space = space - (6 - castTransfer(-6, signName));
			m_util.autoCloseWindow(signName);
			if(space <= 0 || m_util.stop) break;
		}
		
		if(m_util.getPlayerBagItems() > 0) {
			m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpotForge.y));
			boolean dropped = false;
			for(Item i : m_util.getItemsFromBag()){
				if(!i.GetResName().equals("gfx/invobjs/bar-castiron") ){
					m_util.dropItemOnGround(i);
					dropped = true;
				}
			}
			if(dropped){
				while(m_util.getPlayerBagSpace() <= 0 && !m_util.stop) m_util.wait(100);
				return antiSign();
			}
			
			return true;
		}
		
		m_util.stop = true;
		return false;
	}
	
	int castTransfer(int ToFrom, String signName){
		while(!m_util.windowOpen(signName) && !m_util.stop){ m_util.wait(100);}
		String extraName = "gfx/invobjs/bar";
		
		if(signName.contains("Brickwall Cornerpost") ){
			extraName = "gfx/invobjs/bar-wroughtiron";
			//ToFrom = 10;
		}
		
		ISBox box = m_util.findSignBox(signName, extraName);
		while(box == null && !m_util.stop){
			m_util.wait(100);
			box = m_util.findSignBox(signName, extraName);
			if(box != null)
				if(box.remain + box.avail + box.built <= 0) box = null;
		}
		int emptied = 0;
		if(!m_util.stop) emptied = box.remain;
		if(!m_util.stop) m_util.signTransfer(ToFrom, signName, extraName);
		return emptied;
	}
	
	int m_colum = 0;
	void signMetalsV2(){
		if(m_util.stop) return;
		String itemName = "gfx/invobjs/bar-";
		
		m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpotForge.y));
		int itemCount = m_util.countItemsInBag(itemName);
		
		if(itemCount == 0) return;
		
		String signType = "cauldron";
		String signName = "Metal Cauldron";
		String extraName = "gfx/invobjs/bar-toolmetal";
		Coord dir = new Coord(0,1);
		int rowSize = 25;
		int offsetY = 11;
		int offsetX = -5*11;
		int columDir = -1;
		
		Coord startC = new Coord(m_upperSpotForge.add(offsetX, offsetY) );
		
		m_colum = m_util.fillArea(itemCount, m_colum, dir, columDir, signType, signName, extraName, rowSize, startC, itemName);
		
		m_util.goToWorldCoord(m_upperSpotForge.add(-4*11,0));
	}
	
	void signMetalsWrought(){
		if(m_util.stop) return;
		Coord mem = m_util.getPlayerCoord();
		//m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
		
		String signType = "brickwall-cp";
		String signName = "Brickwall Cornerpost";
		String extraName = "gfx/invobjs/bar-wroughtiron";
		String itemName = "gfx/invobjs/bar-wroughtiron";
		Coord dir = new Coord(0,1);
		int rowSize = 8;
		int offsetY = 30*11;
		int offsetX = -20*11;
		int columDir = 1;
		
		Coord startC = new Coord(m_upperSpotForge.add(offsetX, offsetY) );
		
		m_util.walkTo(startC.add(m_colum*11, 0) );
		int itemCount = m_util.countItemsInBag(itemName);
		m_colum = m_util.fillArea(itemCount, m_colum, dir, columDir, signType, signName, extraName, rowSize, startC, itemName);
		m_util.walkTo(mem);
		
		//m_util.goToWorldCoord(m_upperSpot.add(-18*11,0));
	}
	
	void signMetals(){
		if(m_util.stop) return;
		String signType = "cauldron";
		String signName = "Metal Cauldron";
		Coord dir = new Coord(0,-1);
		int rowSize = 25;
		int offset = 11;
		
		Coord startC = new Coord(m_upperSpotForge.add(-4*11,11*rowSize+offset));
		
		m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
		if(m_upperSpotForge.add(-4*11,0).x < m_util.getPlayerCoord().x) m_util.goToWorldCoord(m_upperSpotForge.add(-4*11,0) );
		
		int itemCount = m_util.getPlayerBagItems();
		
		while(!m_util.stop){
			Gob sign = m_util.findClosestObject("sign", new Coord(startC.x, m_upperSpot.y), startC, new Coord(startC.x, m_upperSpot.y) );
			int nextRowSize = m_util.getObjects("sign", new Coord(startC.add(-11,0).x, m_upperSpot.y), startC.add(-11,0) ).size();
			
			//int rowCount = rowSize - m_util.getObjects("sign", new Coord(startC.x, m_upperSpot.y), startC).size();
			int rowCount;
			
			if(sign == null){
				rowCount = rowSize;
			}else{
				rowCount = rowSize - rowCounter(sign.getr(), dir, startC);
				if(nextRowSize == 0) rowCount++;
			}
			
			itemCount = fillRow(itemCount, sign, dir, signType, signName, rowCount, startC);
			
			if(itemCount <= 0)
				break;
			
			startC = startC.sub(11,0);
		}
	}
	
	int rowCounter(Coord signC, Coord dir, Coord startC){
		return startC.sub(signC).div(11).mul(dir.abs() ).abs().sum() + 1;
		
	}
	
	int fillRow(int itemCount, Gob sign, Coord dir, String signType, String signName, int rowCount, Coord firstSpot){
		boolean first = true;
		
		if(m_util.stop) return -1;
		
		if(sign != null) firstSpot = sign.getr();
		
		while( itemCount > 0 && rowCount > 0 && !m_util.stop){
			
			if(dir.x == 0){
				Coord c = new Coord(firstSpot.x, m_util.getPlayerCoord().y);
				if(m_util.getPlayerCoord().x != c.x) m_util.goToWorldCoord(c);
			}else{
				Coord c = new Coord(m_util.getPlayerCoord().x, firstSpot.y);
				if(m_util.getPlayerCoord().y != c.y) m_util.goToWorldCoord(c);
			}
			
			if(sign == null){
				first = false;
				m_util.goToWorldCoord(firstSpot.add(dir.mul(7) ) );
				m_util.sendAction("bp", signType);
				m_util.wait(100);
				//m_util.m_ui.mainview.wdgmsg("place", firstSpot, m_util.MOUSE_LEFT_BUTTON, 0);
				m_util.placeSign(firstSpot);
			}else{
				if(first == true){
					m_util.clickWorldObject(3, sign);
					first = false;
				}else{
					m_util.goToWorldCoord(sign.getr().add(dir.mul(18) ) );
					m_util.sendAction("bp", signType);
					m_util.wait(100);
					//m_util.m_ui.mainview.wdgmsg("place", sign.getr().add(dir.mul(11) ), m_util.MOUSE_LEFT_BUTTON, 0);
					m_util.placeSign(sign.getr().add(dir.mul(11) ) );
				}
			}
			
			itemCount = itemCount - matTransfer(6, signName);
			
			while( m_util.getObjects("sign", m_util.getPlayerCoord().sub(dir.mul(5)) , m_util.getPlayerCoord().sub(dir.mul(5)) ).size() == 0 && !m_util.stop ) m_util.wait(100);
			
			if(!m_util.stop) sign = m_util.findClosestObject("sign", m_util.getPlayerCoord().sub(dir.mul(5)) , m_util.getPlayerCoord().sub(dir.mul(5)) );
			
			rowCount--;
		}
		
		return itemCount;
	}
	
	int matTransfer(int ToFrom, String signName){
		while(!m_util.windowOpen(signName) && !m_util.stop){ m_util.wait(100);}
		String extraName = "gfx/invobjs/bar";
		
		if(signName.contains("Brickwall Cornerpost") ){
			extraName = "gfx/invobjs/bar-wroughtiron";
			ToFrom = 10;
		}
		
		ISBox box = m_util.findSignBox(signName, extraName);
		while(box == null && !m_util.stop){
			m_util.wait(100);
			box = m_util.findSignBox(signName, extraName);
		}
		int emptied = 0;
		if(!m_util.stop) emptied = box.remain;
		if(!m_util.stop) m_util.signTransfer(ToFrom, signName, extraName);
		return emptied;
	}
	
	boolean getOreFromGround(){
	
		int oreCount = m_util.getObjects("gfx/terobjs/items/ore-iron", m_upperSpot.sub( 1000, 0 ), m_upperSpot.add( 100, 0 ) ).size();
		//int player = m_util.getObjects("gfx/borka/s", m_upperSpot.sub( 1000, 0 ), m_upperSpot.add( 100, 0 ) ).size();
		
		//System.out.println(oreCount);
		
		if(oreCount > 0){
			m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
			Coord mem = new Coord(m_util.getPlayerCoord());
			smartCollect();
			/*m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
			String s = "";
			CleanupScript CS = new CleanupScript(m_util, m_upperSpot.sub( 1000, 22 ), m_upperSpot.add( 100, 22 ), s, new Coord(0,0) );
			CS.run();
			m_util.running(true);*/
			/*if(!mem.equals(m_util.getPlayerCoord() ) ) */
			if(m_util.countItemsInBag("gfx/invobjs/ore-iron") > 0) return true;
		}else{
			noJobb();
		}
		
		return false;
	}
	
	void smartCollect(){
		String s = "gfx/terobjs/items/ore-iron";
		Gob closest = m_util.findClosestObject(s);
		CleanupScript CSS = new CleanupScript(m_util, m_util.getPlayerCoord().add(-1000,-1000), m_util.getPlayerCoord().add(1000,1000), closest, Coord.z);
		CSS.run();
		m_util.running(true);
	}
	
	void dumpRestOre(){
		m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
		Inventory inv = m_util.getInventory("Inventory");
		for(Item i : m_util.getItemsFromInv(inv))
			m_util.dropItemOnGround(i);
	}
	
	void openOperation(sRow openRow, int taskRow){
		int taskSystem = openRow.task;
		
		//System.out.println("openOperation "+taskRow+" - "+openRow.task );
		if(taskSystem == 0){
			openRow.task++;
			System.out.println("Opening row "+taskRow+" for operation "+openRow.task+".");
		}
		
		if(taskSystem == 1){
			boolean checkLeft = true;
			if(!getOreFromGround() ) return;
			
			while(checkLeft && openRow.smelter < m_rowSize && !m_util.stop){
				goToSmelter(openRow.smelter + taskRow*m_rowSize);
				checkLeft = fillEmptySmelter(1);
				if(checkLeft) openRow.smelter++;
			}
			
			if(openRow.smelter >= m_rowSize){
				dumpRestOre();
				
				if(m_util.windowOpen("Ore Smelter") ){
					m_util.autoCloseWindow("Ore Smelter");
				}
				
				openRow.smelter = 0;
				openRow.time = System.currentTimeMillis() + 15 *60000 + 0 *1000; // min *60000 + sec *1000
				openRow.task = 2;
			}
		}else if(taskSystem == 2){
			getCoal(1);
			
			for(int i = openRow.smelter; i < m_rowSize; i++){
				Gob g = goToSmelter(i + taskRow*m_rowSize);
				coalSmelter(g);
			}
			
			storeMats(1);
			
			openRow.smelter = 0;
			openRow.task = 3;
		}else if(taskSystem == 3){
			for(int i = openRow.smelter; i < m_rowSize; i++){
				goToSmelter(i + taskRow*m_rowSize);
				lightSmelter(1);
			}
			
			if(m_util.windowOpen("Ore Smelter") ){
				m_util.autoCloseWindow("Ore Smelter");
			}
			
			openRow.smelter = 0;
			openRow.time = System.currentTimeMillis() + 5 *60000 + 35 *1000; // min *60000 + sec *1000
			openRow.task = 4;
		}else if(taskSystem == 4){
			while(openRow.smelter < m_rowSize && !m_util.stop){
				boolean checkLeft = true;
				while(checkLeft && openRow.smelter < m_rowSize && !m_util.stop){
					goToSmelter(openRow.smelter + taskRow*m_rowSize);
					checkLeft = emptyFinishedSmelter(1);
					if(checkLeft) openRow.smelter++;
				}
				
				if(m_util.windowOpen("Ore Smelter") ){
					m_util.autoCloseWindow("Ore Smelter");
				}
				
				if(!m_fillCubsDirectly) signMetalsV2();
				/*if(m_util.countItemsInBag("nugget") > 1)*/ storeMats(3);
			}
			
			openRow.smelter = 0;
			openRow.task = 1;
		}
	}
	
	void openOperationForge(sRow openRow, int taskRow){
		int taskSystem = openRow.task;
		
		//System.out.println("openOperation "+taskRow+" - "+openRow.task );
		if(taskSystem == 0){
			openRow.task++;
			System.out.println("Opening row "+taskRow+" for operation "+openRow.task+".");
		}
		
		if(taskSystem == 1){
			boolean checkLeft = true;
			manageRestCast(false);
			if(!antiSign() ) return;
			
			while(checkLeft && openRow.smelter < m_rowForgeSize && !m_util.stop){
				goToForge(openRow.smelter + taskRow*m_rowForgeSize);
				checkLeft = fillEmptySmelter(2);
				if(checkLeft) openRow.smelter++;
			}
			
			if(openRow.smelter >= m_rowForgeSize){
				//dumpRestOre();
				manageRestCast(true);
				openRow.smelter = 0;
				openRow.time = System.currentTimeMillis() + 5 *60000 + 35 *1000; // min *60000 + sec *1000
				openRow.task = 2;
			}
		}else if(taskSystem == 2){
			getCoal(4);
			
			for(int i = openRow.smelter; i < m_rowForgeSize; i++){
				Gob g = goToForge(i + taskRow*m_rowForgeSize);
				coalSmelter(g);
			}
			
			storeMats(4);
			
			openRow.smelter = 0;
			openRow.task = 3;
		}else if(taskSystem == 3){
			for(int i = openRow.smelter; i < m_rowForgeSize; i++){
				goToForge(i + taskRow*m_rowForgeSize);
				lightSmelter(2);
			}
			
			openRow.smelter = 0;
			openRow.time = System.currentTimeMillis() + 5 *60000 + 35 *1000; // min *60000 + sec *1000
			openRow.task = 4;
		}else if(taskSystem == 4){
			while(openRow.smelter < m_rowForgeSize && !m_util.stop){
				boolean checkLeft = true;
				while(checkLeft && openRow.smelter < m_rowForgeSize && !m_util.stop){
					goToForge(openRow.smelter + taskRow*m_rowForgeSize);
					checkLeft = emptyFinishedSmelter(2);
					if(checkLeft) openRow.smelter++;
				}
				//signMetals();
				storeMats(2);
			}
			
			openRow.smelter = 0;
			openRow.task = 1;
		}
	}
	
	void noJobb(){
		int count = 0;
		
		if(m_util.getPlayerCoord().x < m_upperSpot.add(-20*11,0).x ){
			m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
			m_util.goToWorldCoord( m_upperSpot.add(-10*11,0) );
		}
		
		if(m_util.windowOpen("Ore Smelter") ){
			m_util.autoCloseWindow("Ore Smelter");
		}
		if(m_util.windowOpen("Finery Forge") ){
			m_util.autoCloseWindow("Finery Forge");
		}
		
		feed();
		//m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, m_upperSpot.y));
		//m_util.goToWorldCoord(m_upperSpot);
		
		/*while(count < 1 && !m_util.stop){
			count++;
			m_util.goToWorldCoord(m_util.getPlayerCoord().add(0,-4));
			m_util.goToWorldCoord(m_util.getPlayerCoord().add(0,4));
		}*/
		
		while(count < 5 && !m_util.stop){ m_util.wait(100); count++;}
	}
	
	void feed(){
		if(m_util.getHunger() < 550){
			Coord mem = m_util.getPlayerCoord();
			Gob vclaim = m_util.carpetIdol();
			if(vclaim == null) return;
			
			m_util.walkTo(vclaim.getr().add(55,11) );
			
			feedMEEE(vclaim);
			
			m_util.walkTo(mem);
		}
	}
	
	void feedMEEE(Gob vclaim){
		Gob chest = m_util.smallChest(null);
		
		m_util.drinkFromContainer(chest, false);
		
		ArrayList<Gob> list = m_util.getObjects("gfx/terobjs/cupboard", vclaim.getr().add(5*11, 0), vclaim.getr().add(25*11, 0) );
		ArrayList<Gob> allCubs = m_util.superSortGobList(list, true, true, true);
		
		for(Gob cub : allCubs){
			Inventory inv = m_util.advWalkToContainer(allCubs, cub);
			if(inv == null) return;
			
			m_util.advEater(inv);
			
			if(m_util.getHunger() > 930) return;
		}
	}
	
	void acctiveManager(int type){
		while(!m_util.stop){
			sRow nextTask = null;
			ArrayList<sRow> manager = new ArrayList<sRow>();
			
			if(type == 1) manager = m_rowManager;
			else manager = m_rowManagerForge;
			
			for(sRow sr : manager){
				if(timeCheck(sr) || sr.task == 2){
					if(nextTask == null){
						nextTask = sr;
					}else if(nextTask.task < sr.task){
						nextTask = sr;
					}else if(nextTask.task == 1 && nextTask.smelter < sr.smelter){
						nextTask = sr;
					}
				}
			}
			
			if(nextTask != null){
				if(type == 1) openOperation(nextTask, manager.indexOf(nextTask) );
				else openOperationForge(nextTask, manager.indexOf(nextTask) );
				
				if(!m_util.stop) saveSettings();
			}else{
				noJobb();
			}
			
			m_util.wait(200);
			
			String s = "Task percentage countdown: ";
			String add = "";
			for(sRow sr : manager){
				add = timerPers(sr) + "%  " + add + " ";
			}
			
			int oreCount = m_util.getObjects("gfx/terobjs/items/ore-iron", m_upperSpot.sub(200,0) , m_upperSpot.add(40, 0) ).size();
			s = s + add + oreCount;
			m_util.sendSlenMessage(s);
		}
	}
	
	void saveSettings(){
		try {
			//String content = "This is the content to write into file";
			//System.out.println(new File(".").getAbsolutePath());
			
			File file = new File("./scriptConf/industrialCore.save");
			//System.out.println(file.getAbsolutePath() );
			
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write("icInfo");
			
			for(sRow r : m_rowManager){
				bw.newLine();
				bw.write(Long.toString(r.time));
				bw.newLine();
				bw.write(Integer.toString(r.smelter));
				bw.newLine();
				bw.write(Integer.toString(r.task));
			}
			
			bw.newLine();
			bw.write("endSmelters");
			
			for(sRow r : m_rowManagerForge){
				bw.newLine();
				bw.write(Long.toString(r.time));
				bw.newLine();
				bw.write(Integer.toString(r.smelter));
				bw.newLine();
				bw.write(Integer.toString(r.task));
			}
			
			bw.close();
			
			//System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void loadSettings(){
		try{
			//System.out.println(new File(".").getAbsolutePath());
			
			File file = new File("./scriptConf/industrialCore.save");
			//System.out.println(file.getAbsolutePath() );
			
			if(!file.exists()){
				System.out.println("File doesn't exist");
				return;
			}
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String strLine = br.readLine();
			if(strLine == null || !strLine.equals("icInfo")) return;
			
			//m_rowManager.clear();
			int num = 0;
			while((strLine = br.readLine()) != null){
				if(strLine.equals("endSmelters")) break;
				if(num >= m_rowManager.size()) break;
				sRow s = m_rowManager.get(num);
				num++;
				
				s.time = Long.parseLong(strLine);
				strLine = br.readLine();
				s.smelter = Integer.parseInt(strLine);
				strLine = br.readLine();
				s.task = Integer.parseInt(strLine);
			}
			
			//m_rowManagerForge.clear();
			num = 0;
			while((strLine = br.readLine()) != null){
				if(num >= m_rowManagerForge.size()) break;
				sRow f = m_rowManagerForge.get(num);
				num++;
				
				f.time = Long.parseLong(strLine);
				strLine = br.readLine();
				f.smelter = Integer.parseInt(strLine);
				strLine = br.readLine();
				f.task = Integer.parseInt(strLine);
			}
			
			br.close();
			
		}catch(IOException e){
			e.printStackTrace();
			//return false;
		}
		
		//return true;
	}
	
	void clearMemory(){
		try{
			//System.out.println(new File(".").getAbsolutePath());
			
			File file = new File("./scriptConf/industrialCore.save");
			//System.out.println(file.getAbsolutePath() );
			
			if(!file.exists()){
				System.out.println("File doesn't exist");
				return;
			}
			
			if(file.delete())
    			System.out.println(file.getName() + " is deleted!");
			
		}catch(Exception e){
			e.printStackTrace();
			//return false;
		}
	}
	
	int timerPers(sRow sr){
		int pers = 0;
		long timeCheck = 0;
		
		if(sr.time == 0 || sr.task == 1) return pers;
		
		if(sr.task == 2 || sr.task == 3) timeCheck = 15 *60000 + 0 *1000;
		if(sr.task == 4) timeCheck = 5 *60000 + 35 *1000;
		
		long remaningTime = sr.time - System.currentTimeMillis();
		//System.out.println("remaningTime "+remaningTime);
		//System.out.println("remaningTime2 "+((double)remaningTime / (double)timeCheck * (double)100));
		if(remaningTime < 0 ) return 0;
		
		pers = (int)((double)remaningTime / (double)timeCheck * (double)100);
		
		return pers;
	}
	
	boolean timeCheck(sRow sr){
		return sr.time < System.currentTimeMillis();
	}
	
	int oreScan(){
		return m_util.getObjects("gfx/terobjs/items/ore-iron", 100).size();
	}
	
	void getBars(int type){
		String barName = "-castiron";
		if(type == 2){
			barName = "-wroughtiron";
		}
		
		int space = 0;
		boolean first = true;
		//ArrayList<Item> itemList = new ArrayList<Item>();
		
		if(m_util.stop) return;
		
		if(0 < m_util.getPlayerBagSpace() ){
			m_util.goToWorldCoord(new Coord(m_upperSpot.x + 4 - 15*11, m_util.getPlayerCoord().y));
			
			while(!m_util.stop){
				goToCub(m_barsCub, 5);
				
				if(first){
					space = m_util.getPlayerBagSpace();
					first = false;
				}
				
				Inventory inv = null;
				while(inv == null && !m_util.stop){
					m_util.wait(100);
					inv = m_util.getInventory("Cupboard");
				}
				
				m_util.wait(500);
				
				if(m_util.stop) return;
				
				for(Item i : m_util.getItemsFromInv(inv) ){
					if(i.GetResName().contains(barName) ){
						m_util.transferItem(i);
						space--;
					}
				}
				
				if(space <= 0 || m_barLoop){
					return;
				}
				
				m_barsCub++;
				
				if(m_barsCub >= m_cubListBars.size() ){
					m_barsCub = 0;
					m_barLoop = true;
				}
			}
		}
	}
	
	void sortCastIron(){
		while(!m_util.stop && !m_barLoop){
			getBars(1);
			signMetalsV2();
		}
	}
	
	void sortWroughtIron(){
		while(!m_util.stop && !m_barLoop){
			getBars(2);
			signMetalsWrought();
		}
	}
	
	void transferWI(){
		Gob vidol = m_util.carpetIdol();
		if(vidol == null) return;
		
		Coord signSpot = vidol.getr().add(4*11, 39*11);
		Coord barterSpot = vidol.getr().add(22*11, -46*11);
		Coord midSpot = vidol.getr().add(4*11+4, -1*11);
		
		while(!m_util.stop){
			m_util.safeWalkTo(signSpot);
			
			fillInv(signSpot);
			
			m_util.safeWalkTo(midSpot);
			
			m_util.safeWalkTo(barterSpot);
			
			sellToBarter();
			
			m_util.safeWalkTo(midSpot);
		}
	}
	
	void fillInv(Coord signSpot){
		String signName = "Brickwall Cornerpost";
		
		if(m_util.getPlayerBagSpace() <= 0 || m_util.stop) return;
		
		for(Item i : m_util.getItemsFromBag() ){
			if(i.GetResName().contains("stone") )
				m_util.dropItemOnGround(i);
		}
		
		ArrayList<Gob> list = m_util.getObjects("sign", signSpot.add(0, 11), signSpot.add(-33*11, 9*11) );
		ArrayList<Gob> signs = m_util.superSortGobList(list, true, false, true);
		int space = m_util.getPlayerBagSpace();
		
		for(Gob s : signs){
			if(m_util.getPlayerCoord().y > s.getr().y - 7 ){
				m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, signSpot.y));
				//m_util.goToWorldCoord(new Coord(s.getr().x, m_util.getPlayerCoord().y));
			}
			
			m_util.clickWorldObject(3, s);
			space = space - (10 - castTransfer(-10, signName));
			m_util.autoCloseWindow(signName);
			if(space <= 0 || m_util.stop) return;
		}
		
		if(m_util.getPlayerBagItems() > 0) return;
		
		m_util.stop = true;
		return;
	}
	
	void sellToBarter(){
		Gob barter = m_util.findClosestObject("gfx/terobjs/vstand");
		
		if(barter == null){
			m_util.stop = true;
			return;
		}
		
		//m_util.objectSurf(barter);
		
		m_util.clickWorldObject(3, barter);
		
		ArrayList<Shopbox> shop = null;
		while(shop == null && !m_util.stop){
			shop = m_util.getBarterStandShopbox();
			m_util.wait(200);
		}
		
		if(shop == null) return;
		
		m_util.wait(500);
		
		if(m_util.countItemsInBag("gfx/invobjs/bar-wroughtiron") < 50){
			m_util.stop = true;
			return;
		}
		
		for(Shopbox s : shop){
			s.wdgmsg("buy", new Object[0]);
		}
	}
	
	public void run(){
		m_util.openInventory();
		m_util.setPlayerSpeed(2);
		
		if(m_option == 1){
			loadRows();
			acctiveManager(1);
		}else if(m_option == 2){
			loadRows();
			acctiveManager(2);
		}else if(m_option == 3){
			loadRows();
			sortWroughtIron();
		}else if(m_option == 4){
			loadRows();
			sortCastIron();
		}else if(m_option == 5){
			m_fillCubsDirectly = true;
			loadRows();
			acctiveManager(1);
		}else if(m_option == 6){
			clearMemory();
		}
		
		m_util.running(false);
	}
	
	
	public class sRow{ //ItemSorter.ItemRef ir = new ItemSorter.ItemRef(item, container);
		public long time = 0; //System.currentTimeMillis();
		public int smelter;
		public int task;
		
		public sRow(long i, int j, int k){
			time = i; smelter = j; task = k;
		}
	}
}