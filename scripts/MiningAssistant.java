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
import java.awt.Rectangle;

public class MiningAssistant extends Thread{
	public String scriptName = "Mining Assistant";
	public String[] options = {
		"Tunnel Assistant", "Mining assistant", "Mining assistant Queue 1", "Mining assistant Queue 2", "Mining assistant Queue 3", "Mining Assistant (without HF move)"
	};
	
	HavenUtil m_util;
	int m_option;
	String m_modify;
	
	static int m_haulCount = 0;
	Gob m_boat;
	Gob m_Vclaim;
	Gob m_hearth = null;
	Coord m_direction = new Coord(0, 1);
	Coord m_supportDirection = new Coord(0, 1);
	int m_pickupCount = 0;
	Coord m_p1;
	Coord m_p2;
	boolean m_hearthMoverAcctive = false;
	int m_cubNumNugget = 0;
	int m_cubNumDream = 0;
	boolean m_fullAssistant = true;
	boolean m_HFmoveEnabled = true;
	int m_refreshments = -1;
	boolean m_needScan = true;
	boolean m_dreamBlock = false;
	boolean m_disableHearthMove = false;
	int m_queue;
	
	int m_cubNumWater = 0;
	int m_cubNumFood = 0;
	
	public void ApocScript(HavenUtil util, int option, String modify){
		m_util = util;
		m_option = option;
		m_modify = modify;
		m_p1 = m_util.m_pos1;
		m_p2 = m_util.m_pos2;
	}
	
	/*public MiningAssistant(HavenUtil util, Coord p1, Coord p2, int option, boolean s){
		m_util = util;
		m_p1 = p1;
		m_p2 = p2;
		m_fullAssistant = !s;
		m_option = option;
	}*/
	
	void MainOp(){
		m_supportDirection = new Coord(findDirection(m_p1, m_p2));
		boolean startAtMine = checkLocation();
		System.out.println(m_supportDirection);
		//System.out.println(startAtMine);
		boolean blocksTransporting = false;
		
		if(!startAtMine){
			if(checkMissing() ){
				m_util.sendSlenMessage("Somethings missing. Need boat, chest and HF.");
				return;
			}
			
			m_hearth = getMyHearth();
			if(m_hearth == null) return;
			
			if(!m_util.stop) dropBoatSurface(m_hearth, m_direction);
			if(!m_util.stop && m_util.countItemsInBag("wood") > 0) blockDump(true);
			if(!m_util.stop && m_needScan){
				m_pickupCount = blockScan();
				m_needScan = false;
			}
			if(!m_util.stop) fieldAssistance(m_direction);
			if(!m_util.stop) pickUpBoat();
			if(!m_util.stop) Vport(true);
			
			if(!m_util.stop){
				m_haulCount++;
				System.out.println("Haul Count "+ m_haulCount);
			}
		}
		
		while(!m_util.stop){
			if(!m_util.stop) loadIdol();
			if(!m_util.stop) queueDrop(m_queue);
			if(!m_util.stop) gotoDropoff();
			if(!m_util.stop) dumpOreRefreshLoad();
			if(!m_util.stop) blocksTransporting = blockCollect(m_pickupCount);
			if(m_util.getTW() > 78 && !m_util.stop) fillTWHome();
			if(!m_util.stop) pickUpBoat();
			
			if(!m_util.stop) m_hearth = Hearth();
			if(!m_util.stop) dropBoatSurface(m_hearth, m_direction);
			if(!m_util.stop) blockDump(blocksTransporting);
			if(!m_util.stop && m_needScan){
				m_pickupCount = blockScan();
				m_needScan = false;
			}
			if(!m_util.stop) fieldAssistance(m_direction);
			if(!m_util.stop) pickUpBoat();
			if(!m_util.stop) Vport(true);
			
			if(!m_util.stop){
				m_haulCount++;
				System.out.println("Haul Count "+ m_haulCount);
			}
		}
	}
	
	Gob getMyHearth(){
		return getMyHearth(1000);
	}
	
	Gob getMyHearth(double dist){
		ArrayList<Gob> fires = m_util.getObjects("hearth-play");
		Gob HF = null;
		
		for(Gob g : fires){
			if(m_util.kinType(g) == -1 && m_util.getPlayerCoord().dist(g.getr() ) < dist ){
				HF = g;
				break;
			}
		}
		
		return HF;
	}
	
	boolean checkMissing(){
		boolean HF = getMyHearth() == null;
		boolean chest = m_util.findClosestObject("gfx/terobjs/lchest") == null;
		boolean boat = m_util.findClosestObject("boat") == null;
		
		if(HF || chest || boat) return true;
		
		return false;
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
		if(m_fullAssistant){
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
	
	int blockScan(){
		String extraName = "gfx/invobjs/small/wood";
		String signName = "Palisade Cornerpost";
		
		Gob hearth = getMyHearth();
		if(hearth == null) return 0;
		
		Coord direction = new Coord(0,1);
		
		Coord c = hearth.getr().add(direction.mul(22) );
		
		Gob sign = m_util.findClosestObject("sign", c, c);
		
		if(sign == null){
			dumpNuggets();
			return 3;
		}else{
			m_util.walkTo(sign);
			
			ISBox box = m_util.findSignBox(signName, extraName);
			while(box == null && !m_util.stop){
				m_util.wait(100);
				box = m_util.findSignBox(signName, extraName);
				
				if(box != null && !m_util.stop)
					if(box.remain + box.avail + box.built <= 0) box = null;
			}
			
			if(box == null) return -1;
			int available = (int)(box.avail / 20);
			//System.out.println("available"+available);
			
			dumpNuggets();
			return 3 - available;
		}
	}
	
	boolean blockCollect(int pickupCount){
		if(m_boat == null) return false;
		// collect the blocks here based on number
		boolean blocksPickedUp = false;
		int pickNuggets = m_pickupCount;
		
		for(int i = 0; i < pickupCount; i++){
			int pickup = 20;
			m_pickupCount--;
			blocksPickedUp = true;
			
			if(i == pickupCount - 1 && m_dreamBlock){
				pickup++;
			}
			
			pickupBlocks(pickup);
			if(i < pickupCount - 1){
				m_util.matsFromToBoat(true, m_boat);
			}
			if(i == 2) break;
		}
		
		if(m_dreamBlock && !blocksPickedUp && m_fullAssistant) pickupBlocks(1);
		if(m_fullAssistant) pickupDreamNugget(pickNuggets*2, m_dreamBlock);
		
		return blocksPickedUp;
	}
	
	void pickupNuggets(int pickNuggets){
		Coord c = m_Vclaim.getr();
		if(pickNuggets == 0) return;
		ArrayList<Gob> cubList = m_util.getObjects("gfx/terobjs/cupboard", c.add(-20*11, 22), c.add(-6*11,11) );
		int nuggets = 0;
		
		for(int i = m_cubNumNugget; i < cubList.size(); i++){
			m_cubNumNugget = i;
			Gob cub = cubList.get(i);
			Inventory cubInv = m_util.advWalkToContainer(cubList, cub);
			m_util.wait(300);
			ArrayList<Item> itemList = m_util.getItemsFromInv(cubInv);
			
			for(Item itm : itemList){
				if(itm.GetResName().contains("nugget") && nuggets < pickNuggets){
					nuggets++;
					m_util.transferItem(itm);
				}
			}
			
			if(nuggets == pickNuggets) break;
		}
		
		m_util.walkTo(c.add(-22,33) );
	}
	
	boolean pickupBlocks(int quantity){
		int oldWindow = -1;
		if(m_Vclaim == null) return false;
		Coord c = m_Vclaim.getr().add(-5*11, 2*11);
		pathSafeWalk(c.add(0,11) );
		
		int blocks = m_util.countItemsInBag("gfx/invobjs/wood");
		ArrayList<Gob> signList = m_util.getObjects("sign", c.add(0,0), c.add(-30*11,6*11) );
		//ArrayList<Gob> sortedList = m_util.superSortGobList(signList, true, false, true);
		
		ArrayList<Gob> oldSign = new ArrayList<Gob>();
		while(signList.size() > 0 && !m_util.stop){
			Gob sign = m_util.getClosestObjectInArray(signList);
			
			if(sign == null) return false;
			
			signList.remove(sign);
			
			if(!walkToSign(sign, oldSign) ) continue;
			int transfer = transferSignBlocks(quantity - blocks, oldWindow);
			oldWindow = m_util.windowID("Palisade Cornerpost");
			blocks = blocks + transfer;
			
			if(blocks >= quantity) break;
			
			oldSign.clear();
			oldSign.add(sign);
		}
		
		return true;
	}
	
	boolean walkToSign(Gob sign){
		return walkToSign(sign, new ArrayList<Gob>());
	}
	
	boolean walkToSign(Gob sign, ArrayList<Gob> oldSign){
		String name = "Palisade Cornerpost";
		m_util.walkTo(sign, oldSign);
		
		int redo = 0;
		while(!m_util.stop){
			m_util.wait(100);
			if(m_util.windowOpen(name) ) return true;
			
			if(!m_util.findObject(sign) ) return false;
			
			if(!m_util.PFrunning && !m_util.checkPlayerWalking() ) redo++;
			else redo = 0;
			
			if(redo > 30)
				walkToSign(sign, oldSign);
		}
		
		return true;
	}
	
	int transferSignBlocks(int transfer, int oldWindow){
		String name = "Palisade Cornerpost";
		String matName = "gfx/invobjs/small/wood";
		
		return m_util.matTransfer(-1*transfer, name, matName, oldWindow);
	}
	
	void blockDump(boolean blocksTransporting){
		// dump boath blocks if true
		if(!blocksTransporting && !m_dreamBlock) return;
		
		dumpNuggets();
		m_dreamBlock = false;
		
		if(!blocksTransporting) return;
		
		Coord direction = new Coord(0,1);
		m_boat = m_util.findClosestObject("boat");
		if(m_hearth == null) return;
		
		Coord c = m_hearth.getr().add(direction.mul(22) );
		
		for(int i = 0; i < 3; i++){
			if(!m_util.stop && m_util.getPlayerBagItems() > 0) dumpInvSign(c);
			if(i < 2) m_util.matsFromToBoat(false, m_hearth.getr().add(direction.mul(9)), m_boat);
		}
	}
	
	public void refreshDump(){
		Gob chest = m_util.findClosestObject("lchest");
		
		dumpInv();
		
		if(chest == null) return;
		
		Inventory chestInv = m_util.walkToContainer(chest, "Chest");
		m_util.wait(750);
		
		ArrayList<Item> itemList = m_util.getItemsFromBag();
		ArrayList<Item> bucketList = new ArrayList<Item>();
		ArrayList<Item> foodList = new ArrayList<Item>();
		
		for(Item i : itemList){
			if(i.GetResName().contains("bucket") ){
				//m_util.transferItem(i);
				bucketList.add(i);
			}
		}
		
		for(Item i : itemList){
			if(m_util.foodTest(i, 3) > 0){
				//m_util.transferItem(i);
				foodList.add(i);
			}
		}
		
		specialTransfer(chestInv, foodList, 1);
		specialTransfer(chestInv, bucketList, 3);
	}
	
	void specialTransfer(Inventory chestInv, ArrayList<Item> itemList, int type){
		Coord a = null;
		Coord b = null;
		
		if(type == 1){
			a = new Coord(0,0);
			b = new Coord(5,1);
		}else if(type == 2){
			a = new Coord(0,0);
			b = new Coord(5,4);
		}else if(type == 3){
			a = new Coord(0,0);
			b = new Coord(5,5);
		}
		
		if(chestInv == null || itemList == null) System.out.println("chestInv: "+chestInv+"  itemList: "+itemList);
		
		ArrayList<Coord> coords = m_util.emptyItemArrayExcludeZone(chestInv, itemList, a, b );
		
		if(coords == null) System.out.println("coords: "+coords);
		
		for(int q = 0; q < itemList.size(); q++){
			Item i = itemList.get(q);
			Coord c = coords.get(q);
			
			if(c == null) continue;
			
			m_util.pickUpItem(i);
			m_util.dropItemInInv(c, chestInv);
		}
	}
	
	void dumpNuggets(){
		Gob chest = m_util.findClosestObject("lchest");
		
		if(chest == null) return;
		
		Inventory chestInv = m_util.walkToContainer(chest, "Chest");
		
		m_util.wait(750);
		
		ArrayList<Item> itemList = m_util.getItemsFromBag();
		ArrayList<Item> dreamList = new ArrayList<Item>();
		
		boolean block = false;
		boolean dreamCheck = false;
		
		if(m_dreamBlock) block = true;
		
		for(Item i : itemList){
			if(i.GetResName().contains("nugget")){
				m_util.transferItem(i);
			}
			if(i.GetResName().contains("dream") ){
				//m_util.transferItem(i);
				dreamList.add(i);
			}
			if(i.GetResName().contains("wood") && block){
				//m_util.transferItem(i);
				dreamList.add(i);
				block = false;
			}
		}
		
		specialTransfer(chestInv, dreamList, 2);
		
		ArrayList<Item> scanList = m_util.getItemsFromInv(chestInv);
		for(Item i : scanList){
			if(i.GetResName().contains("buckete") ){
				m_refreshments = 0;
				//System.out.println("buckete");
			}
			if(i.GetResName().contains("dream") ){
				dreamCheck = true;
			}
		}
		
		if(!dreamCheck) m_dreamBlock = true;
	}
	
	void dumpInvSign(Coord c){
		String extraName = "gfx/invobjs/small/wood";
		String signName = "Palisade Cornerpost";
		
		Gob sign = m_util.findClosestObject("sign", c, c);
		
		if(sign == null){
			m_util.sendAction("bp", "palisade-cp");
			m_util.wait(100);
			m_util.placeSign(c);
		}else{
			m_util.clickWorldObject(3, sign);
		}
		
		while(!m_util.windowOpen(signName) && !m_util.stop){m_util.wait(100);}
		
		while(!m_util.stop && m_util.countItemsInBag("wood") > 0){
			ISBox box = m_util.findSignBox(signName, extraName);
			while(box == null && !m_util.stop){
				m_util.wait(100);
				box = m_util.findSignBox(signName, extraName);
			}
			int rema = 0;
			if(!m_util.stop) rema = box.remain;
			if(!m_util.stop && rema > 0) m_util.signTransfer(28, signName, extraName);
			
			m_util.wait(500);
		}
	}
	
	boolean checkLocation(){
		if( m_util.carpetIdol() != null ) return true;
		return false;
	}
	
	void Vport(boolean boat){
		String error = new String("You are too tired of travelling.");
		m_util.sendAction("theTrav", "village");
		while(!m_util.hasHourglass() && !m_util.stop){ m_util.wait(50);}
		Coord start = m_util.getPlayerCoord();
		while(start.dist(m_util.getPlayerCoord()) < 5 && !m_util.stop){
			m_util.wait(50);
			
			if(m_util.slenError().contains(error)){
				m_util.sendSlenMessage("Refreshing");
				refreshPort(boat);
				start = m_util.getPlayerCoord();
			}
		}
		
		m_util.wait(1000);
		//m_util.camReset();
	}
	
	void queueDrop(int queueNum){
		if(m_Vclaim == null || queueNum < 1) return;
		//m_util.wait(1000);
		Coord c = m_Vclaim.getr();
		Gob myGob = m_util.getPlayerGob();
		
		Coord queueSpot = c.add(11*3 + 11*(queueNum-1), 11*3);
		
		int noQueue = 0;
		while(noQueue < 5 && !m_util.stop){
			if(!myGob.getr().equals(queueSpot) && !m_util.PFrunning ){
				m_util.walkToCondition(queueSpot);
			}
			
			if(!queueCheck(queueNum, myGob.id, c) && (m_util.checkPlayerWalking() || myGob.getr().equals(queueSpot)) ) noQueue++;
			else noQueue = 0;
			
			m_util.wait(100);
		}
		m_util.pathing = false;
	}
	
	boolean queueCheck(int queueNum, int myID, Coord c){
		if(m_Vclaim == null) return false;
		
		ArrayList<Gob> players = m_util.getObjects("gfx/borka/s");
		for(Gob g : players){
			if(g.getr().x < (c.x + 33 + 11 * (queueNum-1) ) && g.getr().y < c.y + 11*10+5 && g.id != myID && helmCheck(g) ){
				return true;
			}
		}
		
		return false;
	}
	
	boolean helmCheck(Gob player){
		for(String s : player.resnames() ){
			if(s.contains("helm-miners") ) return true;
		}
		
		return false;
	}
	
	void refreshPort(boolean boat){
		Gob hearth = getMyHearth();
		if(hearth == null) return;
		
		m_util.walkTo(hearth.getr());
		
		if(!m_util.stop && boat) dropBoatSurface(hearth, m_direction);
		
		//fillTWField();
		fillTW();
		
		if(!m_util.stop && boat) pickUpBoat();
		m_util.sendAction("theTrav", "village");
		while(!m_util.hasHourglass() && !m_util.stop){ m_util.wait(50);}
	}
	
	void fillTWHome(){
		Gob chest = m_util.smallChest(null);
		
		m_util.drinkFromContainer(chest, false);
	}
	
	void fillTW(){
		Gob chest = m_util.findClosestObject("lchest");
		
		m_util.drinkFromContainer(chest, false);
	}
	
	void pickUpBoat(){
		if(m_boat == null) return;
		m_util.objectSurf(m_boat);
		m_util.sendAction("carry");
		m_util.clickWorldObject(1, m_boat);
		while(!m_util.checkPlayerCarry() && !m_util.stop)m_util.wait(200);
	}
	
	void dumpOreRefreshLoad(){
		Coord c = new Coord(m_Vclaim.getr().add(-44,99));
		Coord d = new Coord(m_Vclaim.getr().add(-55,110));
		
		for(int i = 0; i < 3; i++){
			if(m_util.getPlayerBagItems() > 0){
				m_util.goToWorldCoord(c);
				while( !m_util.stop && m_util.findClosestObject("gfx/borka/s", d, d.add(-50,10)) != null ) m_util.wait(200);
				m_util.goToWorldCoord(d);
				dumpInv();
			}
			gotoDropoff();
			
			if(i < 2){
				m_util.walkToView(m_boat.getr() );
				m_util.matsFromToBoat(false, m_boat);
				//m_util.matsFromToBoat(false, new Coord(0,0), m_boat);
			}
		}
	}
	
	public void foodRefresh(){
		if(m_Vclaim == null) return;
		Coord p1 = m_Vclaim.getr().add(5*11,0);
		Coord p2 = m_Vclaim.getr().add(25*11,0);
		ArrayList<Gob> cubs = m_util.getObjects("gfx/terobjs/cupboard", p1, p2);
		cubs = m_util.superSortGobList(cubs, true, true, true);
		
		for(int cb = m_cubNumFood; cb < cubs.size(); cb++){
			m_cubNumFood = cb;
			Gob cub = cubs.get(cb);
			Inventory inv = m_util.advWalkToContainer(cubs, cub);
			
			m_util.wait(500);
			
			for(Item i : m_util.getItemsFromInv(inv) ){
				if(m_refreshments > 0 && m_util.foodTest(i ,3) > 0){
					if(m_util.getHunger() + m_util.foodTest(i ,3) < 999 ){
						m_util.itemAction(i);
						m_util.autoFlowerMenuWithClose("Eat");
					}else{
						m_refreshments--;
						m_util.transferItem(i);
					}
				}
			}
			
			if(m_refreshments == 0 && m_util.getHunger() > 899) return;
		}
		
		m_cubNumFood = 0;
		
		System.out.println("Lacking food dude.");
	}
	
	/*void waterRefresh(){
		if(m_Vclaim == null) return;
		Coord p1 = m_Vclaim.getr().add(5*11,-22);
		Coord p2 = m_Vclaim.getr().add(25*11,-22);
		ArrayList<Gob> cubs = m_util.getObjects("gfx/terobjs/cupboard", p1, p2);
		cubs = m_util.superSortGobList(cubs, true, true, true);
		
		for(Gob cub : cubs){
			m_util.advWalkToContainer(cubs, cub);
			
			m_util.wait(500);
			
			ItemToolScript ts = new ItemToolScript(m_util, true, true);
			ts.waterTransfer();
			m_util.running(true);
			
			if(waterTest()) return;
		}
	}*/
	
	public void waterRefresh(){
		if(m_Vclaim == null) return;
		Coord p1 = m_Vclaim.getr().add(5*11,-22);
		Coord p2 = m_Vclaim.getr().add(25*11,-22);
		
		ArrayList<Gob> cubs = m_util.getObjects("gfx/terobjs/cupboard", p1, p2);
		cubs = m_util.superSortGobList(cubs, true, true, true);
		
		for(int cb = m_cubNumWater; cb < cubs.size(); cb++){
			m_cubNumWater = cb;
			Gob cub = cubs.get(cb);
			Inventory cubInv = m_util.advWalkToContainer(cubs, cub);
			
			int i = enoughWater(cubInv);
			if(i > 1){
				m_util.waterTransfer(true);
			}
			
			if(i == 3) return;
		}
		
		m_cubNumWater = 0;
		
		System.out.println("Lacking water dude.");
	}
	
	int enoughWater(Inventory cubInv){
		int water = 0;
		
		m_util.wait(500);
		ArrayList<Item> invCheck = m_util.getItemsFromInv(cubInv);
		
		for(Item i : invCheck){
			if(i.GetResName().contains("bucket") ){
				water = water + m_util.getFluid(i.tooltip);
			}
		}
		//System.out.println("cub water " + water);
		if(water == 0) return 1;
		
		if(water >= (m_util.countItemsInBag("bucket") * 100) ) return 3;
		
		return 2;
	}
	
	/*boolean waterTest(){
		int falseCount = 0;
		
		while(!m_util.stop && falseCount < 5){
			boolean redo = false;
			m_util.wait(200);
			
			ArrayList<Item> bagCheck = m_util.getItemsFromBag();
			
			for(Item i : bagCheck){
				if(i.GetResName().contains("bucket") ){
					if(m_util.getFluid(i.tooltip) < 100){
						falseCount++;
						redo = true;
						break;
					}
				}
			}
			
			if(!redo) break;
		}
		
		if(falseCount == 5) return false;
		
		return true;
	}*/
	
	void dumpInv(){
		if(m_util.stop) return;
		//Inventory inv = m_util.getInventory("Inventory");
		for(Item i : m_util.getItemsFromBag() ){
			if(!i.GetResName().equals("gfx/invobjs/ore-iron") ) continue;
			
			m_util.dropItemOnGround(i);
		}
	}
	
	public Gob Hearth(){
		Gob hh = getMyHearth(110);
		
		if(hh == null){
			m_util.sendAction("theTrav", "hearth");
			while(!m_util.hasHourglass() && !m_util.stop){ m_util.wait(50);}
			Coord start = m_util.getPlayerCoord();
			//while(m_util.hasHourglass() && !m_util.stop){ m_util.wait(50);}
			while(start.dist(m_util.getPlayerCoord()) < 5 && !m_util.stop) m_util.wait(50);
			//m_util.camReset();
		}else{
			m_util.walkTo(hh.getr());
		}
		
		while(hh == null && !m_util.stop){
			hh = getMyHearth(110);
			m_util.wait(500);
		}
		
		return hh;
	}
	
	void dropBoatSurface(Gob hearth, Coord direction){
		Coord c = hearth.getr().add(direction.mul(-22, -22));
		if(!m_util.stop) m_util.clickWorld(3, c);
		
		int count = 0;
		while(m_util.checkPlayerCarry() && !m_util.stop){
			m_util.wait(200);
			count++;
			if(count > 20){
				m_util.clickWorld(3, c);
				count = 0;
			}
		}
		m_boat = m_util.findClosestObject("boat");
	}
	
	void loadIdol(){
		m_Vclaim = null;
		
		while(m_Vclaim == null && !m_util.stop){
			m_Vclaim = m_util.carpetIdol();
			m_util.wait(500);
		}
		
		if(m_Vclaim == null)
			return;
	}
	
	void gotoDropoff(){
		dropBoat();
	}
	
	void dropBoat(){
		Coord c = m_Vclaim.getr().add(0,32);
		pathSafeWalk(c);
		
		m_util.clickWorldObject(3, m_Vclaim);
		
		int count = 0;
		while(m_util.checkPlayerCarry() && !m_util.stop){
			m_util.wait(200);
			count++;
			if(count > 20){
				m_util.clickWorldObject(3, m_Vclaim);
				count = 0;
			}
		}
		
		m_boat = m_util.findClosestObject("boat");
	}
	
	void smartCollect(){
		//System.out.println("start collect");
		boolean trollFound = false;
		String s = "gfx/terobjs/items/ore-iron";
		Gob closest = m_util.findClosestObject(s);
		CleanupScript CSS = new CleanupScript(m_util, m_util.getPlayerCoord().add(-1000,-1000), m_util.getPlayerCoord().add(1000,1000), closest, Coord.z);
		CSS.start();
		
		while(!m_util.stop && m_util.running){
			m_util.wait(200);
			if(m_util.findClosestObject("gfx/kritter/troll/s") != null){
				m_util.stop = true;
				trollFound = true;
			}
		}
		if(trollFound) m_util.stop = false;
		m_util.running(true);
		checkTrolls();
		//System.out.println("end collect");
	}
	
	void fieldAssistance(Coord m_direction){
		if(m_hearth == null) return;
		
		for(int i = 0; i < 3 && m_pickupCount != 3; i++){
			if(m_refreshments >= 0){
				refreshTasks();
				m_refreshments = -1;
			}
			
			while(!m_util.stop){
				if(m_hearth == null) return;
				
				Coord spot = findSupportsSpot();
				
				if(m_hearthMoverAcctive) moveHF();
				
				checkTrolls();
				
				if(m_fullAssistant){
					if(spot != null && m_pickupCount < 3) placeSupport(spot);
					
					if(m_pickupCount == 3 && spot != null){
						//m_util.walkTo(m_hearth.getr() );
						if(!m_util.stop) smartCollect();
						//if(!m_util.stop) pathSafeWalk( m_hearth.getr() );
						break;
					}
					
					if(getAvailableOreAmount() >= m_util.getPlayerBagSpace() ){
						if(!m_util.stop) smartCollect();
						//if(!m_util.stop) m_util.walkTo( m_hearth.getr() );
						//if(!m_util.stop) pathSafeWalk( m_hearth.getr() );
						break;
					}
				}else{
					if(spot != null){
						placeSupport(spot);
						m_pickupCount = 1;
						return;
					}
				}
				
				checkTrolls();
				
				//if(m_hearthMoverAcctive) moveHF();
				
				if(!m_util.getPlayerCoord().equals(m_hearth.getr() ) ) m_util.walkTo(m_hearth.getr() );
				
				m_util.wait(500);
			}
			m_boat = m_util.findClosestObject("boat");
			//if(i < 2 && !m_util.stop && m_util.getPlayerBagItems() > 0 && m_pickupCount != 3) m_util.matsFromToBoat(true, m_hearth.getr().add(m_direction.mul(9)), m_boat);
			if(i < 2 && !m_util.stop && m_util.getPlayerBagItems() > 0 && m_pickupCount != 3) m_util.matsFromToBoat(true, m_boat);
		}
	}
	
	void refreshTasks(){
		refreshLoadLC();
		Vport(false);
		getVidol();
		
		waterRefresh();
		foodRefresh();
		
		m_hearth = Hearth();
		refreshDump();
	}
	
	public void refreshLoadLC(){
		int foodCount = 0;
		
		Inventory chestInv = m_util.getInventory("Chest");
		
		if(!m_util.windowOpen("Chest") ){
			Gob chest = m_util.findClosestObject("lchest");
			
			if(chest == null) return;
			
			chestInv = m_util.walkToContainer(chest, "Chest");
			
			m_util.wait(500);
			
			if(chestInv == null) return;
		}
		
		ArrayList<Item> itemList = m_util.getItemsFromInv(chestInv);
		
		if(itemList == null) return;
		
		for(Item i : itemList){
			if(i.GetResName().contains("bucket-water") || i.GetResName().contains("buckete") ){
				m_util.transferItem(i);
				//System.out.println("bucket");
			}else if(m_util.foodTest(i, 3) > 0){
				foodCount++;
			}
		}
		
		m_refreshments = 18 - foodCount;
	}
	
	void checkTrolls(){
		boolean troll = m_util.findClosestObject("gfx/kritter/troll/s") != null;
		
		if(troll){
			System.out.println("trolls found");
			pathSafeWalk(m_hearth.getr());
			while(!m_util.stop) m_util.wait(200);
		}
	}
	
	void moveHF(){
		if(m_util.stop) return;
		if(!m_HFmoveEnabled) return;
		if(m_dreamBlock) return;
		Coord spot = null;
		
		//System.out.println("HF mover true");
		
		m_hearthMoverAcctive = false;
		
		Coord c = new Coord(m_hearth.getr().x, m_hearth.getr().y);
		
		if(m_fullAssistant){
			m_dreamBlock = true;
			spot = getHFspot();
			
			if(spot == null){
				m_HFmoveEnabled = false;
				return;
			}
		}else{
			Coord dist = new Coord(36*11, 36*11);
			spot = c.add(dist.mul(m_supportDirection) );
		}
		
		getDreamMats();
		
		/*if(m_fullAssistant){
			m_util.walkTo(m_hearth.getr() );
			m_boat = m_util.findClosestObject("boat");
			pickUpBoat();
		}*/
		
		if(m_fullAssistant) moveSign(c, true);
		while(!buildShit(spot, false) && !m_util.stop) m_util.wait(100);
		m_util.walkTo(spot);
		m_hearth = getMyHearth();
		if(m_fullAssistant) moveSign(c, false);
		//buildShit(spot, false);
		
		//m_util.walkTo(spot);
		
		suckGarbage(spot, spot, 35);
		
		//if(m_fullAssistant) dropBoatSurface(m_hearth, m_direction);
		if(m_fullAssistant) moveCrap(c);
	}
	
	void moveCrap(Coord oldCoord){
		pathSafeWalk(m_hearth.getr() );
		dumpInv();
		moveBoat(oldCoord);
		moveChest(oldCoord);
		//moveSign(oldCoord);
	}
	
	void moveChest(Coord oldCoord){
		Coord c = m_hearth.getr().add(22,0);
		moveSign(oldCoord, true);
		Gob chest = m_util.findClosestObject("lchest");
		moveObject(chest, c);
		moveSign(oldCoord, false);
	}
	
	void moveBoat(Coord oldCoord){
		//m_boat = m_util.findClosestObject("boat");
		//Coord c = hearth.getr().add(m_direction.mul(-20, -20));
		Coord c = m_hearth.getr().add(0,-20);
		moveSign(oldCoord, true);
		bashOldFire();
		moveObject(m_boat, c);
		moveSign(oldCoord, false);
	}
	
	void bashOldFire(){
		Gob pow = m_util.findClosestObject("gfx/terobjs/pow");
		m_util.sendAction("destroy");
		m_util.clickWorldObject(1, pow);
		
		int count = 0;
		while(m_util.findObject(pow) && count < 100 && !m_util.stop){
			m_util.wait(100);
			count++;
		}
	}
	
	void moveSign(Coord oldCoord, boolean pickup){
		Coord direction = new Coord(0,1);
		Coord cOld = oldCoord.add(direction.mul(22) );
		/*while(!m_util.stop){
			Gob sign = m_util.findClosestObject("sign", c, c);
			if(sign == null){
				return;
			}
			
			while(!m_util.windowOpen("Palisade Cornerpost") && !m_util.stop){
				m_util.walkToCondition(sign);
				int count = 0;
				while(m_util.PFrunning ){
					m_util.wait(50);
					if(!m_util.checkPlayerWalking() ){
						count++;
						if(count > 40){
							count = 0;
							m_util.pathing = false;
						}
					}else{
						count = 0;
					}
					if(m_util.stop) m_util.pathing = false;
				}
				
				count = 0;
				while(!m_util.stop){
					m_util.wait(50);
					
					if(!m_util.checkPlayerWalking() ){
						count++;
						if(count > 40){
							break;
						}
					}else{
						count = 0;
					}
					
					if(m_util.windowOpen("Palisade Cornerpost") ) break;
				}
			}
			
			transferSignBlocks(28);
		}*/
		
		if(pickup){
			pathSafeWalk(oldCoord.add(0,11) );
			Gob sign = m_util.findClosestObject("sign", cOld, cOld);
			if(sign == null){
				return;
			}
			
			m_util.clickWorldObject(3, sign);
			
			while(!m_util.windowOpen("Palisade Cornerpost") && !m_util.stop) m_util.wait(200);
			
			transferSignBlocks(28, -1);
			
			m_util.autoCloseWindow("Palisade Cornerpost");
		}else{
			pathSafeWalk(m_hearth.getr().add(0,11) );
			Coord d = m_hearth.getr().add(direction.mul(22) );
			
			if(!m_util.stop) suckGarbage(d, m_hearth.getr(), 10);
			
			if(!m_util.stop) dumpInvSign(d);
			m_util.autoCloseWindow("Palisade Cornerpost");
		}
	}
	
	void moveObject(Gob object, Coord dropOff){
		if(object == null) return;
		
		m_util.walkTo(object);
		m_util.sendAction("carry");
		m_util.clickWorldObject(1, object);
		while(!m_util.checkPlayerCarry() && !m_util.stop)m_util.wait(200);
		if(m_util.flowerMenuReady() ) m_util.closeFlowerMenu();
		
		Hearth();
		
		//m_util.walkTo(m_hearth.getr() );
		
		//Coord c = m_hearth.getr().add(22,0);
		if(!m_util.stop) m_util.clickWorld(3, dropOff);
		
		int count = 0;
		while(m_util.checkPlayerCarry() && !m_util.stop){
			m_util.wait(200);
			count++;
			if(count > 20){
				m_util.clickWorld(3, dropOff);
				count = 0;
			}
		}
	}
	
	void getDreamMats(){
		if(!m_fullAssistant){
			if(branchCheck()) return;
			
			Item w = m_util.getItemFromBag("wood");
			if(!m_util.stop) m_util.itemAction(w);
			if(!m_util.stop) m_util.autoFlowerMenu("Split");
			while(m_util.flowerMenuReady() && !m_util.stop) m_util.wait(100);
			return;
		}
		
		if(!m_util.windowOpen("Chest") ){
			Gob chest = m_util.findClosestObject("lchest");
			
			m_util.clickWorldObject(3, chest);
		}
		
		Inventory chestInv = null;
		while(chestInv == null && !m_util.stop){
			m_util.wait(200);
			chestInv = m_util.getInventory("Chest");
		}
		m_util.wait(2000);
		
		if(chestInv == null) return;
		
		ArrayList<Item> itemList = m_util.getItemsFromInv(chestInv);
		boolean dream = true;
		boolean block = true;
		int branch = 0;
		for(Item i : itemList){
			if(i.GetResName().contains("dream") && dream){
				dream = false;
				m_util.transferItem(i);
			}else if(i.GetResName().contains("branch") && branch < 5){
				branch++;
				m_util.transferItem(i);
			}
		}
		
		if(branch == 5) return;
		
		for(Item i : itemList){
			if(i.GetResName().contains("wood") && block){
				m_util.wait(2000);
				ArrayList<Item> bagList = m_util.getItemsFromBag();
				for(Item j : bagList){
					if(j.GetResName().contains("branch") && block){
						m_util.transferItem(i);
					}
				}
				block = false;
				if(!m_util.stop) m_util.itemAction(i);
				if(!m_util.stop) m_util.autoFlowerMenu("Split");
				while(m_util.flowerMenuReady() && !m_util.stop) m_util.wait(100);
			}
		}
	}
	
	boolean branchCheck(){
		ArrayList<Item> itemList = m_util.getItemsFromBag();
		
		int branchCount = 0;
		
		for(Item i : itemList){
			if(i.GetResName().contains("branch") ){
				branchCount++;
			}
		}
		
		if(branchCount >= 5) return true;
		
		return false;
	}
	
	int getAvailableOreAmount(){
		ArrayList<Gob> players = m_util.getObjects("gfx/borka/s");
		ArrayList<Gob> notMe = new ArrayList<Gob>();
		
		int oreCount = 0;
		ArrayList<Gob> scan = m_util.getObjects("gfx/terobjs/items/ore-iron");
		
		for(Gob g : scan){
			for(Gob p : players){
				if(!playerBlocked(g, p) ){
					oreCount++;
					break;
				}
			}
		}
		return oreCount;
	}
	
	boolean playerBlocked(Gob object, Gob player){
		if(m_util.getPlayerGob().id == player.id) return false;
		
		Coord c = player.getr();
		Rectangle rect = new Rectangle(c.x - 3, c.y - 3, 6, 6);
		
		return rect.contains(object.getr().x, object.getr().y);
	}
	
	Coord findSupportsSpot(){
		boolean hearthMove = false;
		Coord buildSpot = null;
		Coord dist = new Coord(99,99);
		ArrayList<Gob> supportList = m_util.getObjects("gfx/terobjs/mining/minesupport");
		
		if(!m_fullAssistant){
			Gob dirGob = null;
			
			for(Gob g : supportList){
				Coord c = g.getr();
				
				if(c.x == m_hearth.getr().x || c.y == m_hearth.getr().y){
					if(dirGob == null){
						dirGob = g;
					}else if(m_supportDirection.sum() > 0 && dirGob.getr().mul(m_supportDirection.abs() ).sum() < c.mul(m_supportDirection.abs() ).sum() ){
						dirGob = g;
					}else if(m_supportDirection.sum() < 0 && dirGob.getr().mul(m_supportDirection.abs() ).sum() > c.mul(m_supportDirection.abs() ).sum() ){
						dirGob = g;
					}
				}
			}
			
			if(dirGob != null){
				supportList.clear();
				supportList.add(dirGob);
			}
		}
		
		for(Gob support : supportList){
			if(m_fullAssistant && ignoreSupport(support) ) continue;
			
			Coord spot = support.getr().add(m_supportDirection.mul(0,1).mul(dist));
			if( supportSpotFound(spot) ){
				
				if(m_util.insideViewBox(m_util.getPlayerCoord(), spot) ){
					return spot;
				}else{
					hearthMove = true;
				}
				//return null;
			}
			
			spot = support.getr().add(m_supportDirection.mul(1,0).mul(dist));
			if( supportSpotFound(spot ) ){
				
				if(m_util.insideViewBox(m_util.getPlayerCoord(), spot) ){
					return spot;
				}else{
					hearthMove = true;
				}
				//return null;
			}
			
			spot = support.getr().add(m_supportDirection.mul(dist));
			if( supportSpotFound(spot ) ){
				
				if(m_util.insideViewBox(m_util.getPlayerCoord(), spot) ){
					return spot;
				}else{
					hearthMove = true;
				}
				//return null;
			}
		}
		
		if(hearthMove && !m_disableHearthMove) m_hearthMoverAcctive = true;
		
		return null;
	}
	
	boolean ignoreSupport(Gob support){
		return m_util.findClosestObject("sign", 13, support.getr() ) != null;
	}
	
	boolean supportSpotFound(Coord scanC){
		boolean spotFree = m_util.findClosestObject("gfx/terobjs/mining/minesupport", scanC, scanC) == null;
		boolean signTest = m_util.findClosestObject("sign", scanC, scanC) == null;
		boolean tileFree = m_util.getTileID(scanC.div(11) ) == 24;
		
		if(spotFree && signTest && tileFree) return true;
		
		return false;
	}
	
	void placeSupport(Coord spot){
		if(m_fullAssistant){	
			dumpInv();
			getMats();
		}
		while(!buildShit(spot, true) && !m_util.stop) m_util.wait(100);
		//m_util.walkTo(m_hearth.getr() );
	}
	
	public void suckGarbage(Coord suckC, Coord dropC, int rad){
		String s = "gfx/terobjs/items/ore-iron";
		while(oreScan(s, suckC, dropC, rad) > 0 && !m_util.stop){
			Gob closest = m_util.findClosestObject(s);
			CleanupScript CSS = new CleanupScript(m_util, suckC.add(rad, rad), suckC.sub(rad, rad), closest, dropC);
			CSS.run();
			m_util.running(true);
			
			m_util.walkTo(dropC );
			safeDumpInv();
			
			m_util.wait(500);
		}
	}
	
	int oreScan(String s, Coord suckC, Coord dropC, int rad){
		ArrayList<Gob> list = getObjects(s, suckC.add(rad, rad), suckC.sub(rad, rad));
		int oreCount = 0;
		
		for(Gob ore : list){
			Coord c = ore.getr();
			if(!c.equals(dropC) ){
				oreCount++;
			}
		}
		
		return oreCount;
	}
	
	ArrayList<Gob> getObjects(String str, Coord p1, Coord p2){
		ArrayList<Gob> list = new ArrayList<Gob>();
		
		int smallestX = p1.x;
		int largestX = p2.x;
		
		int smallestY = p1.y;
		int largestY = p2.y;
		
		if(p2.x < p1.x){
			smallestX = p2.x;
			largestX = p1.x;
		}
		
		if(p2.y < p1.y){
			smallestY = p2.y;
			largestY = p1.y;
		}
		
		Rectangle rect = new Rectangle(smallestX, smallestY, largestX - smallestX, largestY - smallestY);
		
		synchronized(m_util.m_ui.mainview.glob.oc){
			for(Gob g : m_util.m_ui.mainview.glob.oc){
				if(rect.contains(g.getr().x, g.getr().y))
					if(g.resname().contains(str))
						list.add(g);
			}
		}
		
		return list;
	}
	
	ArrayList<Item> ignore = new ArrayList<Item>();
	void safeDumpInv(){
		Inventory inv = m_util.getInventory("Inventory");
		while(!m_util.stop){
			for(Item i : m_util.getItemsFromBag() ){
				if(!i.GetResName().equals("gfx/invobjs/ore-iron") || ignore.contains(i) ) continue;
				
				ignore.add(i);
				m_util.dropItemOnGround(i);
			}
			
			m_util.dropHoldingItem();
			m_util.wait(300);
			if(m_util.countItemsInBag("ore-iron") <= 0) break;
		}
	}
	
	void safeTransferBlocks(){
		int count = 0;
		transferSignBlocks(20, -1);
		while(!m_util.stop){
			m_util.wait(100);
			if(m_util.countItemsInBag("wood") == 20) break;
			count++;
			if(count > 100){
				count = 0;
				int blocks = 20 - m_util.countItemsInBag("wood");
				transferSignBlocks(blocks, -1);
			}
		}
	}
	
	void getMats(){
		//get blocks
		m_pickupCount++;
		Coord direction = new Coord(0,1);
		Coord c = m_hearth.getr().add(direction.mul(22) );
		Gob sign = m_util.findClosestObject("sign", c, c);
		if(sign == null){
			m_pickupCount = 3;
			return;
		}
		//if(!m_util.windowOpen("Palisade Cornerpost") ) m_util.clickWorldObject(3, sign);
		walkToSign(sign);
		safeTransferBlocks();
		//get nuggets
		Gob chest = m_util.findClosestObject("lchest");
		
		m_util.clickWorldObject(3, chest);
		
		Inventory chestInv = null;
		while(chestInv == null && !m_util.stop){
			m_util.wait(200);
			chestInv = m_util.getInventory("Chest");
		}
		
		m_util.wait(750);
		
		if(chestInv == null) return;
		
		ArrayList<Item> itemList = m_util.getItemsFromInv(chestInv);
		int nuggetCount = 0;
		boolean dreamCheck = false;
		for(Item i : itemList){
			if(i.GetResName().contains("nugget") && nuggetCount < 2){
				nuggetCount++;
				m_util.transferItem(i);
			}
			if(i.GetResName().contains("buckete") ){
				m_refreshments = 0;
			}
			if(i.GetResName().contains("dream") ){
				dreamCheck = true;
			}
		}
		
		if(!dreamCheck) m_dreamBlock = true;
	}
	
	boolean buildShit(Coord spot, boolean support){
		String buildname = "minesupport";
		String signName = "Mine Support";
		
		if(!support){
			buildname = "hearth";
			signName = "Hearth Fire";
		}
		Coord c = spot.add(m_supportDirection.inv().mul(7) );
		if(m_util.getTileID(c.div(11) ) == 255 ){
			Coord d = spot.add(m_supportDirection.inv().mul(7).mul(1,0) );
			if(m_util.getTileID(d.div(11) ) == 255 ){
				c = spot.add(m_supportDirection.inv().mul(7).mul(0,1) );
			}else{
				c = d;
			}
		}
		
		if(!m_fullAssistant && !support){
			Coord d = spot.add(m_supportDirection.swap().mul(7) );
			if(m_util.getTileID(d.div(11) ) == 255 ){
				c = spot.add(m_supportDirection.swap().mul(7).mul(-1,-1) );
			}else{
				c = d;
			}
		}
		
		pathSafeWalk(c);
		
		if(c == null) return true;
		Coord drop = c.add(m_supportDirection.inv() );
		if(support) suckGarbage(spot, drop, 7);
		
		if(m_util.getPlayerCoord().equals(c) ){
			m_util.sendAction("bp", buildname);
			m_util.placeSign(spot);
		}
		
		int count = 0;
		int breakCount = 0;
		while(!m_util.windowOpen(signName) && !m_util.stop){
			m_util.wait(200);
			count++;
			
			if(count > 10){
				m_util.sendAction("bp", buildname);
				m_util.placeSign(spot);
				count = 0;
				breakCount++;
				if(breakCount > 3) return false;
			}
		}
		
		ArrayList<Item> itemList = m_util.getItemsFromBag();
		for(Item i : itemList)
			m_util.transferItem(i);
		
		m_util.wait(200);
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
		
		return true;
	}
	
	void ShaftAssistant(){
		m_supportDirection = new Coord(findDirection(m_p1, m_p2));
		boolean startAtMine = checkLocation();
		System.out.println(m_supportDirection);
		//System.out.println(startAtMine);
		boolean blocksTransporting = false;
		
		if(!startAtMine){
			m_hearth = getMyHearth();
			if(m_hearth == null) return;
			if(scanInvForMats(true) ) fieldAssistance(m_direction);
			if(!m_util.stop) WineVport();
		}
		
		while(!m_util.stop){
			if(!m_util.stop) getVidol();
			if(!m_util.stop) m_util.walkTo(m_Vclaim.getr().add(-33,22) );
			if(!m_util.stop) scanInvForMats(false);
			//if(m_util.getTW() > 78 && !m_util.stop) fillTW();
			
			if(!m_util.stop) m_hearth = Hearth();
			
			if(!m_util.stop) fieldAssistance(m_direction);
			
			if(!m_util.stop) WineVport();
			
			if(!m_util.stop){
				m_haulCount++;
				System.out.println("Support placed "+ m_haulCount);
			}
		}
	}
	
	public void WineVport(){
		if(m_util.getTW() > 89 ) drinkWine();
		
		while(!m_util.stop){
			m_util.sendAction("theTrav", "village");
			while(!m_util.hasHourglass() && !m_util.stop){ m_util.wait(50);}
			while(m_util.hasHourglass() && !m_util.stop){ m_util.wait(50);}
			
			m_util.wait(500);
			
			String error = new String("You are too tired of travelling.");
			String str = new String(m_util.slenError());
			
			if(str.contains(error)){
				drinkWine();
			}else
				break;
		}
	}
	
	void drinkWine(){
		m_util.quickWine();
		waitForHourglass();
	}
	
	void waitForHourglass(){
		while(!m_util.hasHourglass() && !m_util.stop) m_util.wait(50);
		while(m_util.hasHourglass() && !m_util.stop) m_util.wait(50);
	}
	
	boolean scanInvForMats(boolean field){
		m_pickupCount = 0;
		
		ArrayList<Item> itemList = m_util.getItemsFromBag();
		
		int nuggetCount = 0;
		int woodCount = 0;
		int branchCount = 0;
		boolean dream = false;
		boolean wine = false;
		
		for(Item i : itemList){
			if(i.GetResName().contains("nugget") ){
				nuggetCount++;
			}else if(i.GetResName().contains("wood") ){
				woodCount++;
			}else if(i.GetResName().contains("dream") ){
				dream = true;
			}else if(i.GetResName().contains("glass-winef") ){
				wine = true;
			}else if(i.GetResName().contains("branch") ){
				branchCount++;
			}
		}
		
		int blockPickup = 21;
		
		if(branchCount >= 5){
			blockPickup--;
			woodCount++;
		}
		
		if(field && (woodCount < 20 || nuggetCount < 2) ) return false;
		if(field) return true;
		
		if(woodCount < 21) pickupBlocks(blockPickup);
		
		if(!dream || nuggetCount < 2) pickupDreamNugget(2 - nuggetCount, !dream);
		if(!wine) getWine();
		
		return true;
	}
	
	void pickupDreamNugget(int pickNuggets, boolean pickDream){
		if(m_Vclaim == null) return;
		Coord c = m_Vclaim.getr();
		if(pickNuggets == 0 && !pickDream) return;
		ArrayList<Gob> unsortedList = m_util.getObjects("gfx/terobjs/cupboard", c.add(-20*11, 22), c.add(-5*11,0) );
		ArrayList<Gob> cubList = m_util.superSortGobList(unsortedList, true, false, true);
		int nuggets = 0;
		
		int cubNum = 0;
		
		if(m_cubNumNugget < m_cubNumDream) cubNum = m_cubNumNugget;
		else cubNum = m_cubNumDream;
		
		for(int i = cubNum; i < cubList.size(); i++){
			if((!pickDream || m_cubNumDream > i) && (nuggets >= pickNuggets || m_cubNumNugget > i)) continue;
			
			Gob cub = cubList.get(i);
			Inventory cubInv = m_util.advWalkToContainer(cubList, cub);
			m_util.wait(300);
			ArrayList<Item> itemList = m_util.getItemsFromInv(cubInv);
			
			if(itemList == null) return;
			
			for(Item itm : itemList){
				if(pickDream && itm.GetResName().contains("dream")){
					m_util.transferItem(itm);
					pickDream = false;
					m_cubNumDream = i;
				}else if(itm.GetResName().contains("nugget") && nuggets < pickNuggets){
					nuggets++;
					m_util.transferItem(itm);
					m_cubNumNugget = i;
				}
			}
		}
		
		if(nuggets == pickNuggets && !pickDream) return;
		
		m_util.stop = true;
		return;
		
		//m_util.walkTo(c.add(-22,33) );
	}
	
	public void getWine(){
		Gob chest = m_util.smallChest(null);
		
		Inventory chestInv = m_util.walkToContainer(chest, "Chest");
		Item glass = m_util.getItemFromBag("wine");
		
		if(chestInv == null || glass == null) return;
		
		ArrayList<Item> itemList = m_util.getItemsFromInv(chestInv);
		for(Item i : itemList){
			if(i.GetResName().contains("bucket-wine")){
				Coord bc = i.c;
				m_util.pickUpItem(i);
				m_util.itemInteract(glass);
				m_util.dropItemInInv(bc, chestInv);
			}
		}
	}
	
	public void getVidol(){
		m_Vclaim = null;
		
		while(m_Vclaim == null && !m_util.stop){
			m_Vclaim = m_util.carpetIdol();
			m_util.wait(500);
		}
	}
	
	void pathSafeWalk(Coord c){
		m_util.safeWalkTo(c);
		/*while(!m_util.stop && !m_util.getPlayerCoord().equals(c) ){
			m_util.walkTo(c);
			m_util.wait(200);
		}*/
	}
	
	/*void rectTest(){
		Rectangle rectCheck = new Rectangle(m_util.m_safePos.x-44, m_util.m_safePos.y-44, 88,88);
		Coord placeSpot = m_util.spiralScanRectFreeSpace(m_util.m_safePos, rectCheck, 80, false);
		m_util.sendAction("bp", "brodinn");
		m_util.placeSign(placeSpot);
	}*/
	
	Coord getHFspot(){
		Coord dist = new Coord(1000*11, 1000*11);
		//m_hearth = m_util.getPlayerGob();
		Coord c = new Coord(m_hearth.getr().x, m_hearth.getr().y);
		Coord offsetSpot = c.add(dist.mul(m_supportDirection) );
		//Coord offsetSpot = m_util.m_safePos.add(dist.mul(m_supportDirection) );
		
		ArrayList<Gob> supportList = m_util.getObjects("gfx/terobjs/mining/minesupport");
		
		while(!m_util.stop && supportList.size() > 0){
			Gob support = m_util.getClosestObjectInArray(supportList, offsetSpot);
			Coord supportC = support.getr();
			
			//Rectangle rectCheck = new Rectangle(supportC.x-20, supportC.y-42, 60,78);
			Rectangle rectCheck = new Rectangle(-20, -42, 60,78);
			Coord placeSpot = m_util.spiralScanRectFreeSpace(supportC, rectCheck, 70, 11, false);
			if(placeSpot != null){
				//m_util.walkTo(placeSpot);
				/*m_util.sendAction("bp", "brodinn");
				m_util.placeSign(placeSpot);*/
				
				return placeSpot;
			}
			
			supportList.remove(support);
		}
		
		return null;
	}
	
	public void run(){
		m_util.setPlayerSpeed(2);
		m_util.openInventory();
		
		m_fullAssistant = m_option != 1;
		
		if(m_option >= 2 && m_option <= 5){
			m_queue = m_option - 1;
			MainOp();
		}else if(m_option == 1){
			ShaftAssistant();
		}else if(m_option == 6){
			m_needScan = false;
			m_disableHearthMove = true;
			m_queue = 0;
			MainOp();
		}
		
		m_util.running(false);
	}
}