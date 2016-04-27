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


/*
	WARNING! do not use this outdated code as a good source for scripting.
	This is specially old and messy code. DO NOT EVEN TRY TO LOOK INSIDE.
	
	Note to compile this script collection you must have the following files:
	ItemSorter.java, CraftScript.java and PlowScript.java in the scripts folder.
*/

import haven.*;
import addons.*;

import java.util.ArrayList;
import java.awt.Rectangle;

import haven.MapView.rallyPoints;

public class FarmScriptV2 extends Thread{
	public String scriptName = "Farming Script";
	public String[] options = {
		"Flour Wheat",
		"Fill Troughs",
		"Regular Farmer",
		"Rally Farmer",
		"Just Replant",
		"Replant From LCs",
		"Simple Farmer",
		"Simple Farmer Instant Replant",
		"Carrot Field Replant",
		"Wax Collector",
		"Farm Buds",
		"Regular Farmer Exclude Fiber Straw Pickup",
		"Press Grapes From LCs",
		"Cook Pepper From LCs",
		"Cook Bones Into Glue From LCs",
	};
	
	public ArrayList<Gob> m_farmList;
	ArrayList<Coord> m_fieldCoords = new ArrayList<Coord>();
	boolean m_sortPumpkins;
	boolean m_replant;
	boolean m_dumpInv = false;
	boolean m_dumpLC;
	boolean m_sortReplantOn;
	public Coord m_p1;
	public Coord m_p2;
	public Coord m_pos1;
	public Coord m_pos2;
	boolean m_resume;
	int m_pumpkinQuality;
	String m_memCrops = null;
	boolean m_cleanup = true;
	boolean m_highQplant = true;
	
	HavenUtil m_util;
	
	int m_option;
	String m_modify;
	
	public void ApocScript(HavenUtil util, int option, String modify){
		m_util = util;
		m_option = option;
		m_modify = modify;
	}
	
	void harvestCrops(){
		m_util.openInventory();
		coordSorting();
		int bagSize = m_util.getPlayerBagSize();
		int bagSpace = m_util.getPlayerBagSpace();
		int inventoryItems = 0;
		
		if(bagSize == 30){
			bagSize = 24;
			bagSpace = bagSpace - 6;
			if(bagSpace < 0) bagSpace = 0;
			inventoryItems = (bagSize - bagSpace);
		}else if(bagSize == 56){
			bagSize = 48;
			bagSpace = bagSpace - 8;
			if(bagSpace < 0) bagSpace = 0;
			inventoryItems = (bagSize - bagSpace);
		}else{
			inventoryItems = (bagSize - bagSpace);
		}
		
		Coord c = new Coord(0,0);
		
		m_farmList = getFarmList();
		
		for(int i = 0; i < m_farmList.size() && !m_util.stop; i++){
			Gob g = (Gob) m_farmList.get(i);
			int harvestQuantity = farmable(g.resname(), 0, false);
			
			boolean newCrops = false;
			
			if(m_memCrops == null){
				m_memCrops = g.resname();
				//System.out.println("null set");
			}else if(!m_memCrops.equals(g.resname()) ){
				m_memCrops = g.resname();
				newCrops = true;
				//System.out.println("New Crop");
			}
			
			if( ( (inventoryItems + harvestQuantity) > bagSize || newCrops) && !m_dumpLC && !m_replant && !m_sortPumpkins){
				if(m_dumpInv){
					if(m_util.getPlayerBagItems() > 0){
						if(c.equals(new Coord(0,0) ) ) c = m_util.getPlayerCoord();
						grapeCheck(true);
						teaCheck(c);
						tobaccoCheck(c);
						unloadInventory(c,1,true);
						inventoryItems = 0;
						triggerCleanup(c, newCrops, g.getr());
					}
				}else{
					m_util.sendErrorMessage("Full inventory");
					break;
				}
			}
			
			c = new Coord(g.getr());
			
			if(m_util.stop)
				return;
			
			if(m_util.getStamina() < 60 && !m_replant) drinkWater();
			
			inventoryItems += harvestQuantity;
			m_util.sendAction("harvest");
			m_util.clickWorldObject(1, g);
			
			boolean notharvested = true;
			while(notharvested && !m_util.stop){
				ArrayList<Gob> checklist = getFarmList();
				
				notharvested = false;
				for(Gob o: checklist)
				{
					if(o.id == g.id)
					{
						notharvested = true;
					}
				}
				
				m_util.wait(100);
			}
			
			if(m_sortPumpkins && !m_util.stop){
				pumpkinFarm();
			}
			
			if(m_replant){
				//System.out.println("replant triggerd");
				plantSeed(m_util.getPlayerCoord().div(11).mul(11), true);
			}
		}
		
		if(m_dumpInv && m_util.getPlayerBagItems() > 0){
			if(c.equals(new Coord(0,0) ) ) c = m_util.getPlayerCoord();
			grapeCheck(true);
			teaCheck(c);
			tobaccoCheck(c);
			unloadInventory(c,1,true);
			inventoryItems = 0;
			triggerCleanup(c, true, Coord.z);
		}
	}
	
	void triggerCleanup(Coord c, boolean pickupAll, Coord nextSpot){
		if(!m_cleanup) return;
		//pickupAll = true;
		//System.out.println("Cleanup Triggerd");
		
		Gob lc = m_util.findClosestObject("lchest", new Coord(m_pos1.x-11, c.y) , new Coord(m_pos1.x, m_pos2.y), c);
		if(lc == null) return;
		
		//System.out.println("LC found");
		Coord tileC = lc.getr().add(11, 0).div(11).mul(11).add(5,5);
		Rectangle r = new Rectangle(tileC.x, tileC.y - 56, 79, 57);
		
		if(!r.contains(nextSpot.x, nextSpot.y) ) pickupAll = true;
		
		boolean clean = false;
		int pickupItemCount = 0;
		Gob fib = null;
		ArrayList<Gob> obj = m_util.getObjects(m_pos1, m_pos2);
		for(Gob g : obj){
			String s = g.resname();
			if(s.contains("gfx/terobjs/items/flaxfibre") || s.contains("gfx/terobjs/items/straw") || s.contains("gfx/terobjs/items/flower-poppy") ){
				if(r.contains(g.getr().x, g.getr().y) ){
					if(pickupAll){
						clean = true;
						fib = g;
						//System.out.println("found item");
						break;
					}else{
						pickupItemCount++;
					}
				}
			}
		}
		
		//System.out.println("pickupItemCount " + pickupItemCount);
		if(pickupItemCount >= m_util.getPlayerBagSpace() ) clean = true;
		
		if(!clean) return;
		
		//System.out.println("Cleaning");
		CleanupScript cleanup = new CleanupScript(m_util, tileC, tileC.add(77, -55), fib, lc.getr().add(10,0) );
		cleanup.run();
		m_util.running(true);
		
		//unloadInventory(c,-1, false);
		unloadHighest(c, "gfx");
		
		//m_util.goToWorldCoord(m_util.getPlayerCoord().add(20, 0) );
	}
	
	void unloadHighest(Coord c, String itemName){
		//if(m_util.countItemsInBag("seed-fir") == 0) return;
		
		Gob lc = m_util.findClosestObject("lchest", new Coord(m_pos1.x-11, c.y) , new Coord(m_pos1.x, m_pos2.y), c);
		//go to container
		
		if(!m_util.stop) m_util.clickWorldObject(3, lc);
		
		m_util.goToWorldCoord(lc.getr().add(10,0) );
		
		if(!m_util.stop) m_util.clickWorldObject(3, lc);
		
		Inventory chestInv = null;
		while(chestInv == null && !m_util.stop){
			m_util.wait(200);
			chestInv = m_util.getInventory("Chest");
		}
		
		if(m_util.stop) return;
		m_util.wait(500);
		ArrayList<Item> chestList = m_util.getItemsFromInv(chestInv);
		ArrayList<Item> bagItems = m_util.getItemsFromBag(itemName);
		ArrayList<Item> allItems = new ArrayList<Item>();
		ArrayList<Item> dropLowest = new ArrayList<Item>();
		ArrayList<Item> transferList = new ArrayList<Item>();
		
		allItems.addAll(bagItems);
		allItems.addAll(chestList);
		
		while(allItems.size() > 48 && !m_util.stop){
			Item lowest = m_util.getLowestQ(allItems);
			dropLowest.add(lowest);
			allItems.remove(lowest);
		}
		
		for(Item transfer : bagItems){
			if(!dropLowest.contains(transfer) ) transferList.add(transfer);
		}
		
		for(Item im : dropLowest){
			m_util.dropItemOnGround(im);
		}
		
		for(Item transfer : transferList){
			m_util.transferItem(transfer);
		}
		
		m_util.autoCloseWindow("Chest");
	}
	
	void tobaccoCheck(Coord c){
		ArrayList<Item> itemList = new ArrayList<Item>();
		itemList = m_util.getItemsFromBag();
		
		boolean found = false;
		for(Item i : itemList){
			if(i.GetResName().equals("gfx/invobjs/tobacco-fresh")){
				//m_util.dropItemOnGround(i);
				found = true;
				break;
			}
		}
		
		if(!found) return;
		
		unloadHighest(c, "gfx/invobjs/tobacco-fresh");
	}
	
	void tobaccoDrop(){
		ArrayList<Item> itemList = m_util.getItemsFromBag();
		
		for(Item i : itemList){
			if(i.GetResName().equals("gfx/invobjs/tobacco-fresh")){
				m_util.dropItemOnGround(i);
			}
		}
	}
	
	boolean checkTeaContent(){
		ArrayList<Item> itemList = new ArrayList<Item>();
		Inventory inv = m_util.getInventory("Inventory");
		itemList = m_util.getItemsFromInv(inv);
		
		for(Item i : itemList){
			if(i.GetResName().equals("gfx/invobjs/tea-fresh")){
				return true;
			}
		}
		return false;
	}
	
	private void teaCheck(Coord c){
		Inventory bag = m_util.getInventory("Inventory");
		ArrayList<Item> itemList = new ArrayList<Item>();
		
		if(!checkTeaContent()) return;
		
		unloadHighest(c, "gfx/invobjs/tea-fresh");
		
		//System.out.println("doing tea");
		
		/*Gob lc = m_util.findClosestObject("chest", c, c.add(-100,200), c);
		if(lc == null)
			return;
		
		Inventory chest = null;
		
		m_util.clickWorldObject(3, lc);
		//m_util.wait(1000);
		m_util.clickWorldObject(3, lc);
		
		while(chest == null && !m_util.stop){
			m_util.wait(200);
			chest = m_util.getInventory("Chest");
		}
		
		itemList = m_util.getItemsFromInv(bag);
		boolean redo = true;
		int count = 0;
		while(redo && count < 10 && !m_util.stop){
			redo = false;
			for(Item i : itemList){
				if(i.GetResName().equals("gfx/invobjs/tea-fresh")){
					try{
						m_util.transferItem(i);
					}catch(Exception e){}
					//System.out.println("tresfer");
				}
			}
			m_util.wait(500);
			
			itemList.clear();
			itemList = m_util.getItemsFromInv(bag);
			
			for(Item i : itemList){
				if(i.GetResName().equals("gfx/invobjs/tea-fresh")){
					redo = true;
					//System.out.println("tee found again");
					break;
				}
			}
			
			count++;
		}*/
		
		//while(m_util.getItemsFromInv(bag).size() != 0 && !m_util.stop) m_util.wait(200);
	}
	
	boolean grapeCheck(boolean toggle){
		ArrayList<Item> itemList = new ArrayList<Item>();
		Inventory inv = m_util.getInventory("Inventory");
		itemList = m_util.getItemsFromInv(inv);
		
		for(Item i : itemList){
			if(i.GetResName().equals("gfx/invobjs/grapes")){
				if(toggle){
					makeWine();
					return true;
				}else
					return true;
			}
		}
		return false;
	}
	
	void makeWine(){
		int invert = 1;
		Inventory invPress = null;
		Gob press = m_util.findClosestObject("gfx/terobjs/winepress", m_pos1.add(0,-100), m_pos2.add(0,100) );
		if(press == null) return;
		Coord mem = new Coord(m_util.getPlayerCoord());
		
		if(press.getr().y < m_util.getPlayerCoord().y ) invert = invert * -1;
		
		m_util.clickWorld(3, m_util.getPlayerCoord() );
		m_util.walkTo(press.getr().add(0,-7*invert) );
		m_util.clickWorldObject(3, press);
		
		while(invPress == null && !m_util.stop){
			m_util.wait(200);
			invPress = m_util.getInventory("Winepress");
		}
		
		while(grapeCheck(false) && !m_util.stop){
			for(int i = 0; i < 25 && !m_util.stop; i++)
				m_util.transferItemTo(invPress, 1);
			
			int pf = press.frame;
			boolean presswine = true;
			boolean toggleOn = false;
			m_util.buttonActivate("Winepress");
			while(presswine && !m_util.stop){
				if(pf != press.frame && !toggleOn){
					pf = press.frame;
					toggleOn = true;
				}else if(pf != press.frame && toggleOn){
					presswine = false;
				}
				m_util.wait(200);
			}
			
			emptyPress(press, invert);
			invPress = m_util.getInventory("Winepress");
			
			for(int i = 0; i < 25 && !m_util.stop; i++)
				m_util.transferItemFrom(invPress, 1);
		}
		
		m_util.goToWorldCoord(mem);
	}
	
	void emptyPress(Gob press, int invert){
		if(m_util.getVmeterAmount(0, false) > 75){
			//System.out.println("1");
			Inventory invChest = null;
			Inventory invChest2 = null;
			Inventory invPress = null;
			
			Gob schest = m_util.findClosestObject("gfx/terobjs/furniture/cclosed", m_pos1.add(0,-100), m_pos2.add(0,100), press.getr());
			if(schest == null) return;
			
			Coord c = new Coord( ( press.getr().x + schest.getr().x ) / 2, press.getr().y - 20*invert);
			
			m_util.goToWorldCoord(c);
			m_util.clickWorldObject(3, schest);
			
			while(invChest == null && !m_util.stop){
				m_util.wait(200);
				invChest = m_util.getInventory("Chest");
			}
			
			Item bucket = null;
			int count = 0;
			
			while(bucket == null && !m_util.stop){
				m_util.wait(200);
				bucket = m_util.getItemFromInventory(invChest, "buckete");
				count++;
				if(count > 40){
					m_util.goToWorldCoord(c);
					m_util.clickWorldObject(3, press);
					
					while(invPress == null && !m_util.stop){
						m_util.wait(200);
						invPress = m_util.getInventory("Winepress");
					}
					return;
				}
			}
			
			if(bucket == null) return;
			Coord bucketC = new Coord(bucket.c);
			
			m_util.pickUpItem(bucket);
			while(!m_util.mouseHoldingAnItem() && !m_util.stop)	m_util.wait(100);
			
			m_util.goToWorldCoord(c);
			m_util.itemActionWorldObject(press, 0);
			
			while(!m_util.stop){
				if(m_util.mouseHoldingAnItem())
					if(m_util.getMouseItem().GetResName().endsWith("bucket-grapejuice"))
						break;
				m_util.wait(100);
			}
			
			m_util.goToWorldCoord(c);
			m_util.clickWorldObject(3, schest);
			
			while(invChest2 == null && !m_util.stop){
				m_util.wait(200);
				invChest2 = m_util.getInventory("Chest");
			}
			m_util.wait(1000);
			
			/*ArrayList<Item> items = new ArrayList<Item>(m_util.getItemsFromInv(invChest2));
			
			for(Item i : items)
				m_util.itemInteract(i);*/
			
			m_util.dropItemInInv(bucketC, invChest2);
			
			while(m_util.mouseHoldingAnItem() && !m_util.stop) m_util.wait(100);
			
			m_util.goToWorldCoord(c);
			m_util.clickWorldObject(3, press);
			
			while(invPress == null && !m_util.stop){
				m_util.wait(200);
				invPress = m_util.getInventory("Winepress");
			}
		}
	}
	
	/*void dropItem(Item x){
		Inventory i = m_util.getInventory("Inventory");
		m_util.dropItemOnGround(x);
		while(0 != m_util.itemCount(i) && !m_util.stop){
			m_util.wait(200);
			System.out.println("Item inventory stuck " + x.q);
		}
	}*/
	
	void goTo(Coord c){
		Gob player = m_util.getPlayerGob();
		Coord mc = new Coord(m_util.getPlayerCoord());
		
		m_util.clickWorld(1, c);
		
		while(m_util.getPlayerCoord().equals(mc) && !m_util.stop){
			m_util.wait(200);
			System.out.println("Im stuck");
		}
		while(m_util.checkPlayerWalking() && !m_util.stop){
			m_util.wait(200);
			System.out.println("Im moving");
		}
	}
	
	/*int checkPumpkinQuality(Item x){
		Inventory i = m_util.getInventory("Inventory");
		int count = 0;
		while(0 == m_util.itemCount(i) && !m_util.stop){
			m_util.wait(200);
			count++;
			if(count > 20){break; }
		}
		System.out.println("Item Q:" + x.q);
		if(!m_util.stop){return x.q;}
		return -1;
	}*/
	
	void coordSorting(){
		Coord p1 = m_p1.div(11);
		Coord p2 = m_p2.div(11);
		
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
		
		m_p1 = (new Coord(smallestX, smallestY)).mul(11);
		m_p2 = (new Coord(largestX, largestY)).mul(11).add(10,10);
	}
	
	private ArrayList<Gob> getFarmList(){
		ArrayList<Gob> objects = m_util.getObjects(m_p1, m_p2);
		ArrayList<Gob> plants = new ArrayList<Gob>();
		ArrayList<Gob> sortedPlants = new ArrayList<Gob>();
		
		for(Gob g: objects){
			int stage = getPlantStage(g);
			if(farmable(g.resname(), stage, true) > 0){
				plants.add(g);
			}
		}
		
		if(!m_replant)
			sortedPlants = m_util.superSortGobList(plants, false, false, false);
		else
			sortedPlants = m_util.sortGobList1(plants);
		
		return sortedPlants;
	}
	
	public int farmable(String s, int stage, boolean stageCheck)
	{
		if(s.equals("gfx/terobjs/plants/carrot") && ( stage == 3 || stage == 4 || !stageCheck) )
		{
			return 3;
		}
		if(s.equals("gfx/terobjs/plants/beetroot") && ( stage == 3 || !stageCheck) )
		{
			return 6;
		}
		if(s.equals("gfx/terobjs/plants/wheat") && ( stage == 3 || !stageCheck) )
		{
			return 3;
		}
		if(s.equals("gfx/terobjs/plants/hemp") && ( stage == 4 || !stageCheck) )
		{
			return 3;
		}
		if(s.equals("gfx/terobjs/plants/flax") && ( stage == 3 || !stageCheck) )
		{
			return 3;
		}
		if(s.equals("gfx/terobjs/plants/peas") && ( stage == 4 || !stageCheck) )
		{
			return 3;
		}
		if(s.equals("gfx/terobjs/plants/hops") && ( stage == 3 || !stageCheck) )
		{
			return 3;
		}
		if(s.equals("gfx/terobjs/plants/pumpkin") && ( stage == 6 || !stageCheck) )
		{
			return 16;
		}
		if(s.equals("gfx/terobjs/plants/onion") && ( stage == 3 || !stageCheck) )
		{
			return 3;
		}
		if(s.equals("gfx/terobjs/plants/poppy") && ( stage == 4 || !stageCheck) )
		{
			return 3;
		}
		if(s.equals("gfx/terobjs/plants/tea") && ( stage == 3 || !stageCheck) )
		{
			return 4;
		}
		if(s.equals("gfx/terobjs/plants/wine") && ( stage == 3 || !stageCheck) )
		{
			return 3;
		}
		if(s.equals("gfx/terobjs/plants/tobacco") && ( stage == 4 || !stageCheck) )
		{
			return 5;
		}
		if(s.equals("gfx/terobjs/plants/pepper") && ( stage == 3 || !stageCheck) )
		{
			return 3;
		}
		
		return -1;
	}
	
	public boolean seedTest(String s){
		if(s.equals("gfx/invobjs/seed-carrot") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/seed-grape") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/seed-hemp") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/seed-hops") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/seed-poppy") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/seed-pumpkin") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/seed-tea") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/seed-wheat") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/seed-pepper") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/seed-tobacco") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/carrot") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/flaxseed") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/onion") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/beetroot") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/peapod") )
		{
			return true;
		}
		/*if(s.equals("gfx/invobjs/pumpkin") )
		{
			return true;
		}
		if(s.equals("gfx/invobjs/grapes") )
		{
			return true;
		}*/
		return false;
	}
	
	private void unloadInventory(Coord c, int invert, boolean invCheck){
		//System.out.println("yes unload");
		//Coord p1 = new Coord(m_p2.x + 6 , m_p1.y);
		//Coord p2 = m_p2.add(6,0);
		Inventory bag = m_util.getInventory("Inventory");
		Gob lc = m_util.findClosestObject("chest", c, c.add(145*invert,200), c);
		
		if(lc == null)
			return;
		
		Inventory chest = null;
		
		if(m_util.getInventory("Chest") != null){
			m_util.clickWorld(1, m_util.getPlayerCoord());
			//m_util.clickWorld(1, m_util.getPlayerCoord());
			
			while(m_util.getInventory("Chest") != null && !m_util.stop) m_util.wait(200);
		}
		
		if(!m_util.stop) m_util.clickWorldObject(3, lc);
		//m_util.wait(1000);
		if(!m_util.stop) m_util.clickWorldObject(3, lc);
		
		while(chest == null && !m_util.stop){
			m_util.wait(200);
			chest = m_util.getInventory("Chest");
		}
		
		tobaccoDrop();
		chest = weerdBeet(lc, chest);
		
		/*for(int i = 0; i < 48 && !m_util.stop; i++){
			m_util.transferItemTo(chest, 1);
			//m_util.wait(100);
		}*/
		m_util.wait(500);
		//if(m_util.getPlayerBagItems() == 0) return;
		ArrayList<Item> itemList = dropWrongSeedLC(chest);
		seedTransferHighest(itemList, chest);
		if(invCheck) while(m_util.getItemsFromInv(bag).size() != 0 && !m_util.stop) m_util.wait(200);
	}
	
	ArrayList<Item> dropWrongSeedLC(Inventory chest){
		ArrayList<Item> itemList = new ArrayList<Item>();
		String[] types = getSeedNames();
		
		for(Item i : m_util.getItemsFromInv(chest) ){
			if(!testDropSeed(i, types) ){
				m_util.dropItemOnGround(i);
			}else{
				itemList.add(i);
			}
		}
		
		return itemList;
	}
	
	boolean testDropSeed(Item i, String[] types){
		for(String s : types){
			if(s.equals("null") || i.GetResName().equals(s) ){
				return true;
			}
		}
		return false;
	}
	
	String[] getSeedNames(){
		Item i = m_util.getItemFromBag("gfx");
		String s = "null";
		
		if(i != null) s = i.GetResName();
		
		switch(s){
			case "gfx/invobjs/seed-carrot":
				return new String[]{"gfx/invobjs/seed-carrot", "gfx/invobjs/carrot"};
			case "gfx/invobjs/carrot":
				return new String[]{"gfx/invobjs/seed-carrot", "gfx/invobjs/carrot"};
			case "gfx/invobjs/beetroot":
				return new String[]{"gfx/invobjs/beetroot", "gfx/invobjs/beetrootleaves"};
			case "gfx/invobjs/beetrootleaves":
				return new String[]{"gfx/invobjs/beetroot", "gfx/invobjs/beetrootleaves"};
			case "gfx/invobjs/seed-grape":
				return new String[]{"gfx/invobjs/seed-grape", "gfx/invobjs/grapes"};
			case "gfx/invobjs/grapes":
				return new String[]{"gfx/invobjs/seed-grape", "gfx/invobjs/grapes"};
			case "gfx/invobjs/seed-tea":
				return new String[]{"gfx/invobjs/seed-tea", "gfx/invobjs/tea-fresh"};
			case "gfx/invobjs/tea-fresh":
				return new String[]{"gfx/invobjs/seed-tea", "gfx/invobjs/tea-fresh"};
		}
		
		return new String[]{s};
	}
	
	void seedTransferHighest(ArrayList<Item> chestList, Inventory chestInv){
		if(m_util.stop) return;
		
		ArrayList<Item> bagItems = m_util.getItemsFromBag();
		ArrayList<Item> allItems = new ArrayList<Item>();
		ArrayList<Item> dropLowest = new ArrayList<Item>();
		ArrayList<Item> transferList = new ArrayList<Item>();
		
		allItems.addAll(bagItems);
		allItems.addAll(chestList);
		
		while(allItems.size() > 48 && !m_util.stop){
			Item lowest = getLowestQ(allItems);
			dropLowest.add(lowest);
			allItems.remove(lowest);
		}
		
		for(Item transfer : bagItems){
			if(!dropLowest.contains(transfer) ) transferList.add(transfer);
		}
		
		for(Item im : dropLowest){
			m_util.dropItemOnGround(im);
		}
		
		for(Item transfer : transferList){
			m_util.transferItem(transfer);
		}
		
		//m_util.autoCloseWindow("Chest");
	}
	
	public Item getLowestQ(ArrayList<Item> itemList){
		Item im = null;
		
		for(Item i : itemList){
			if(im == null){
				im = i;
			}else if(trashQlist(i) ){
				im = i;
				break;
			}else if(im.q > i.q){
				im = i;
			}
		}
		
		return im;
	}
	
	boolean trashQlist(Item i){
		String s = i.GetResName();
		return s.equals("gfx/invobjs/tea-fresh") || s.equals("gfx/invobjs/beetrootleaves");
	}
	
	Inventory weerdBeet(Gob lc, Inventory chest){
		Coord mem = m_util.getPlayerCoord();
		
		if(m_util.getItemFromBag("beetrootweird") == null ){
			if(!m_util.mouseHoldingAnItem() ){
				return chest;
			}else if(!m_util.getMouseItem().GetResName().contains("beetrootweird") ){
				return chest;
			}
		}
		
		System.out.println("Weird beetroot detected, dumping into water chest.");
		
		Gob schest = m_util.findClosestObject("gfx/terobjs/furniture/cclosed", m_p1.add(0,-1000), m_p2);
		if(schest == null) return chest;
		
		m_util.goToWorldCoord(schest.getr().add(0,4) );
		m_util.clickWorldObject(3, schest);
		
		chest = null;
		while(chest == null && !m_util.stop){
			m_util.wait(200);
			chest = m_util.getInventory("Chest");
		}
		
		if(m_util.mouseHoldingAnItem()){
			if(!m_util.getMouseItem().GetResName().contains("beetrootweird") ){
				Item weird = m_util.getItemFromBag("beetrootweird");
				m_util.dropItemInBag(weird.c);
			}
			
			Coord dropItemC = m_util.emptyItemSlot(chest);
			if(dropItemC != null) m_util.dropItemInInv(dropItemC, chest);
		}else{
			Item weird = m_util.getItemFromBag("beetrootweird");
			m_util.transferItem(weird);
		}
		
		m_util.goToWorldCoord(mem);
		
		m_util.clickWorldObject(3, lc);
		
		chest = null;
		while(chest == null && !m_util.stop){
			m_util.wait(200);
			chest = m_util.getInventory("Chest");
		}
		
		return chest;
	}
	
	private void waitForHolding(){
		while(!m_util.mouseHoldingAnItem() && !m_util.stop){
			m_util.wait(100);
		}
	}
	
	private void plantSeed(Coord plantTile, boolean stamina){
		m_util.clickWorld(3, plantTile);
		if(!m_util.mouseHoldingAnItem() && !m_util.stop){
		
			Item seed = m_util.getItemFromBagExclude("seed", "bag");
			
			int count = 0;
			while(seed == null && !m_util.stop && count < 20){
				seed = m_util.getItemFromBagExclude("seed", "bag");
				if(seed == null) seed = m_util.getItemFromBag("carrot");
				if(seed == null) seed = m_util.getItemFromBag("onion");
				if(seed == null) seed = m_util.getItemFromBag("beetroot");
				if(seed == null) seed = m_util.getItemFromBag("peapod");
				m_util.wait(100);
				//System.out.println("stuck in here");
				count++;
			}
			
			//System.out.println(seed);
			
			if(seed == null){
				return;	
			}
			
			m_util.pickUpItem(seed);
			//seedCoord = new Coord(seed.c);
			while(!m_util.mouseHoldingAnItem() && !m_util.stop){ m_util.wait(50); }
		}
		
		//if(m_util.getTileID(plantTile.div(11)) != 9)
		//	return;
		int flop = 1;
		boolean redoPlant = true;
		
		while(redoPlant && !m_util.stop){
			//System.out.println("replant cyckle on");
			redoPlant = false;
			Coord errorSpot = plantTile.add(5, 5 + 3 * flop);
			flop *= -1;
			
			//System.out.println("errorSpot" + errorSpot);
			m_util.clickWorld(1, errorSpot);
			m_util.itemAction(plantTile, 1);
			//m_util.itemAction(plantTile, m_pause);
			
			int count = 0;
			
			while(m_util.getObjects("plants", plantTile, plantTile).size() == 0 && m_util.mouseHoldingAnItem() && !m_util.stop){
				m_util.wait(50);
				//m_util.itemAction(plantTile);
				
				if(m_util.getPlayerCoord().equals(errorSpot ) && !m_util.checkPlayerWalking() ){
					count++;
					if(count > 10){
						if(flop == 1) triggerHandPlow(plantTile, stamina);
						redoPlant = true;
						break;
					}
				}
			}
		}
		/*while(m_util.getObjects("plants", plantTile, plantTile).size() == 0 && m_util.mouseHoldingAnItem() && !m_util.stop){
			m_util.wait(10);
			//m_util.itemAction(plantTile);
			count++;
			if(count > 4000){
				//m_util.dropItemInBag(seedCoord);
				break;
			}
		}*/
		
		
		//Item mouseItem = m_util.getMouseItem();
		/*
		while(m_util.mouseHoldingAnItem() && count < 10 && !m_util.stop){
			m_util.itemAction(g.getr());
			m_util.wait(500);
			count++;
		}
		*/
		/*while(mouseItem == m_util.getMouseItem() && !m_util.stop){
			m_util.itemAction(g.getr());
			m_util.wait(100);
			count++;
			if(count == 200){
				m_util.dropItemInBag(seed.c);
			}
		}*/
	
		
		if(m_util.getStamina() < 35 && (stamina || waterFlaskTest()) ){
			m_util.quickWater();
		}
	}
	
	boolean waterFlaskTest(){
		Item flask = m_util.getItemFromBag("flask");
		Item water = m_util.getItemFromBag("bucket-water");
		return flask != null && water != null;
	}
	
	void triggerHandPlow(Coord plantTile, boolean stamina){
		if(m_util.getStamina() < 60){
			if(stamina){
				m_util.quickWater();
			}else{
				drinkWater();
			}
		}
		
		m_util.sendAction("plow");
		m_util.clickWorld(1, plantTile.add(5,5) );
		
		while(!m_util.hasHourglass() && !m_util.stop) m_util.wait(50);
		while(m_util.hasHourglass() && !m_util.stop) m_util.wait(50);
		
		m_util.clickWorld(3, m_util.getPlayerCoord() );
	}
	
	void pumpkinFarm(){
		int count = 0;

		Item pumpkin = m_util.getItemFromBag("pumpkin");
		while(pumpkin == null && !m_util.stop){
			m_util.wait(50);
			pumpkin = m_util.getItemFromBag("pumpkin");
		}
		
		if(pumpkin != null){
			count = 0;
			// m_util.clickWorld(3, m_util.getPlayerCoord());
			/*while(m_util.getCursor().contains("harvest")){
				m_util.wait(100);
				count++;
				if(count > 100){ return; }
			}*/
			
			int q = pumpkin.q;
			if( q >= m_pumpkinQuality ){
				int dropOffx = (m_p1.x + m_p2.x)/2;
				int dropOffy;
				
				if(m_p2.y > m_p1.y)
					dropOffy = m_p2.y + 11 * (1 + q - m_pumpkinQuality);
				else
					dropOffy = m_p1.y + 11 * (1 + q - m_pumpkinQuality);
					
				Coord dropSpot = new Coord(dropOffx, dropOffy);
				m_util.clickWorld(3, m_util.getPlayerCoord());
				m_util.goToWorldCoord(dropSpot);
				m_util.dropItemOnGround(pumpkin);
			}else{
				m_util.dropItemOnGround(pumpkin);
			}
			m_util.sendAction("harvest");
			//m_util.waitForCursor("harvest");
		}
	}
	
	public ArrayList<Coord> getSeedingTiles(Coord p1, Coord p2){
		ArrayList<Coord> tileList = new ArrayList<Coord>();
		ArrayList<Coord> seedingTiles = new ArrayList<Coord>();
		coordSorting();
		tileList = m_util.getFarmTilesV2(p1, p2);
		
		//System.out.println("tileList"+tileList.size());
		
		for(Coord i : tileList){
			if(m_util.getObjects("plants", i.mul(11), i.mul(11)).size() == 0)
				seedingTiles.add(i);
		}
		return seedingTiles;
	}
	
	boolean replantFarm(){
		m_util.openInventory();
		ArrayList<Coord> seedingTiles = new ArrayList<Coord>();
		seedingTiles = getSeedingTiles(m_p1, m_p2);
		
		int count = 0;
		for(Coord i : seedingTiles){
			count++;
			plantSeed(i.mul(11) ,false);
			if(!m_util.mouseHoldingAnItem() || m_util.stop){
				m_util.wait(500);
				if(!m_util.mouseHoldingAnItem() || m_util.stop){
					m_util.wait(500);
					if(!m_util.mouseHoldingAnItem() || m_util.stop)
						break;
				}
			}
		}
		//System.out.println(count +" count : size "+ seedingTiles.size());
		return(seedingTiles.size() == count);
	}
	
	Inventory goToChest(Gob chest, int invert){
		if(!chest.getr().add(-10 * invert,0).equals(m_util.getPlayerCoord() ) || !m_util.windowOpen("Chest")){
			m_util.goToWorldCoord(chest.getr().add(-10 * invert,0) );
			while(m_util.windowOpen("Chest") && !m_util.stop) m_util.wait(100);
			
			m_util.clickWorld(3, m_util.getPlayerCoord() );
			m_util.clickWorldObject(3, chest);
		}
		Inventory chestInv = null;
		
		while(chestInv == null && !m_util.stop){
			m_util.wait(100);
			chestInv = m_util.getInventory("Chest");
		}m_util.wait(200);
		
		return chestInv;
	}
	
	boolean plowTest(Coord p1, Coord p2){
		ArrayList<Coord> tileList = new ArrayList<Coord>();
		tileList = m_util.getTilesInRegion(p1, p2, 1);
		
		for(Coord i : tileList){
			int tile = m_util.getTileID(i);
			if(tile != 9 && tile != 4)
				return true;
		}
		return false;
	}
	
	void sortReplant(){
		coordSorting();
		m_util.openInventory();
		m_util.setPlayerSpeed(2);
		
		if(m_util.windowOpen("Chest") ){
			m_util.autoCloseWindow("Chest");
		}
		
		ArrayList<Gob> chestList = new ArrayList<Gob>();
		ArrayList<ItemSorter> globalSortList = new ArrayList<ItemSorter>();
		ArrayList<Coord> fieldCoords = new ArrayList<Coord>();
		boolean first = true;
		
		fieldCoords = multiField(true);
		
		//System.out.println("test "+fieldCoords.get(0));
		//System.out.println("test "+fieldCoords.get(1));
		
		for(int i = 0; i < fieldCoords.size(); i+=2){
			ArrayList<Gob> unsortedChests = new ArrayList<Gob>();
			ArrayList<Gob> cL = new ArrayList<Gob>();
			
			Coord p2 = new Coord( fieldCoords.get(i) );
			Coord p1 = new Coord( fieldCoords.get(i+1) );
			Coord c = new Coord(p2.x+5, p1.y);
			Coord d = p2.add(5,0);
			
			unsortedChests = m_util.getObjects("chest",c ,d);
			
			cL = m_util.sortGobList1(unsortedChests);
			
			for(Gob chst : cL)
				chestList.add(chst);
		}
		
		Item memItem = null;
		Item newItem = null;
		
		ArrayList<Item> itemList = new ArrayList<Item>();
		ItemSorter sortList = new ItemSorter();
		
		for(int chest = 0; chest < chestList.size() && !m_util.stop; chest++){
			Gob g = chestList.get(chest);
			if(first){
				m_util.walkTo(g.getr().add(-10,0) );
				first = false;
			}
			Inventory inv = goToChest(g ,1);
			
			if(m_util.stop) return;
			
			itemList = m_util.getItemsFromInv(inv);
			
			if(itemList.size() == 0){
				if(memItem != null ){
					ItemSorter is = new ItemSorter();
					
					for(int iter = 0; iter < sortList.size(); iter++){
						is.add(sortList.get(iter).container , sortList.get(iter).itemQ );
					}
					
					globalSortList.add(is);
					
					sortList.clear();
					memItem = null;
					//System.out.println("reset on empty chest");
				}
			}else{
				for(Item i : itemList){
					if( seedTest( i.GetResName() ) ){
						newItem = i;
						//System.out.println(newItem.GetResName() );
						break;
					}
				}
				
				if(memItem == null ){
					memItem = newItem;
				}else if( !memItem.GetResName().equals(newItem.GetResName()) ) {
					ItemSorter is = new ItemSorter();
					
					for(int iter = 0; iter < sortList.size(); iter++){
						is.add(sortList.get(iter).container , sortList.get(iter).itemQ );
					}
					
					globalSortList.add(is);
					sortList.clear();
					memItem = itemList.get(0);
				}
				
				for(Item i : itemList){
					if( seedTest( i.GetResName() ) ){
						sortList.add(chest, i.q);
					}
				}
			}
		}
		
		if(sortList.size() > 0) globalSortList.add(sortList);
		
		//for(int i = 0; i < globalSortList.size(); i++)
			//System.out.println(globalSortList.get(i).size() );
		
		Gob pg = m_util.findClosestObject("plow", m_p1, m_p2.add(11,1000));
		
		if(pg == null) return;
		
		if(m_util.stop) return;
		
		Coord originalPlowSpot = new Coord( pg.getr() );
		
		for(int i = 0; i < fieldCoords.size(); i+=2){
			Coord p1 = new Coord( fieldCoords.get(i) );
			Coord p2 = new Coord( fieldCoords.get(i+1) );
			if(plowTest(p1, p2)){
				PlowScript pl = new PlowScript();
				pl.m_farmToggle = true;
				pl.m_util = m_util;
				pl.plower(p1, p2);
			}
		}
		
		if(m_util.checkPlayerCarry()){
			m_util.clickWorld(3, originalPlowSpot);
			m_util.wait(200);
			m_util.clickWorld(3, originalPlowSpot);
			
			while( m_util.checkPlayerCarry() && !m_util.stop)m_util.wait(200);
			m_util.wait(200);
			
			m_util.goToWorldCoord(m_util.getPlayerCoord().add(0,-5));
		}
		
		
		for(ItemSorter sL : globalSortList){
			//System.out.println("sL "+sL.size() );
			boolean planting = true;
			int invSpace = m_util.getPlayerBagSpace();
			int skipTo = 0;
			int skipToTemp = 0;
			
			chestCoordReset(sL, chestList);
			
			/*m_util.clickWorld(1, m_p1);
			m_util.wait(5000);
			m_util.clickWorld(1, m_p2);
			m_util.wait(5000);*/
			
			while(planting && !m_util.stop){
				if(m_util.getStamina() < 70) drinkWater();
				int plantNum = m_util.getPlayerBagSpace();
				//System.out.println("plantNum "+plantNum);
				
				if(getSeedingTiles(m_p1, m_p2).size() < m_util.getPlayerBagSpace()) plantNum = getSeedingTiles(m_p1, m_p2).size();
				
				for(int chest = 0; chest < chestList.size() && !m_util.stop; chest++){
					//System.out.println("skipToTemp "+skipToTemp);
					//System.out.println("skipTo "+skipTo);
					int seedTransfer = sL.getContainerSortedCount(chest, plantNum, skipTo, m_highQplant);
					//System.out.println("seedTransfer "+seedTransfer);
					if(seedTransfer > 0 && invSpace > 0){
						Gob g = chestList.get(chest);
						goToChest(g ,1);
						invSpace = invSpace - seedTransfer;
						if(invSpace < 0) seedTransfer = seedTransfer + invSpace;
						safeTransfer(seedTransfer);
						skipToTemp = skipToTemp + seedTransfer;
					}
				}
				skipTo = skipToTemp;
				
				//System.out.println("planting");
				
				planting = !replantFarm();
				
				//System.out.println("planting "+planting);
				
				invSpace = m_util.getPlayerBagSpace();
			}
			
			m_util.dropHoldingItem();
		}
	}
	
	void chestCoordReset(ItemSorter sortList, ArrayList<Gob> chestList){
		int max = -1;
		int min = -1;
		
		for(int i = 0; i < sortList.size(); i++){
			int cont = sortList.get(i).container;
			
			if(max == -1){
				max = cont;
			}else if(max < cont){
				max = cont;
			}
			
			if(min == -1){
				min = cont;
			}else if(min > cont){
				min = cont;
			}
		}
		
		//System.out.println("min "+min);
		//System.out.println("max "+max);
		
		//System.out.println("m_pos1 "+m_pos1);
		//System.out.println("m_pos2 "+m_pos2);
		
		Coord c = chestList.get(max).getr().add(0, -9);
		Coord d = new Coord(chestList.get(max).getr().x, m_pos1.y-9);
		
		//System.out.println(c);
		//System.out.println(d);
		
		Gob bonusChest = m_util.findClosestObject("chest", c, d, c);
		
		//System.out.println("size "+m_util.getObjects("chest", c, d ).size() );
		
		int p1y = m_pos1.y;
		
		if(bonusChest != null){
			p1y = bonusChest.getr().add(0,9).y;
			//System.out.println("bonusChest "+bonusChest.getr());
		}//else
			//System.out.println("non bonus ");
		
		//System.out.println("p1y "+p1y);
		
		m_p2 = new Coord(m_p2.x, chestList.get(min).getr().y);
		
		m_p1 = new Coord(m_p1.x, p1y);
	}
	
	void safeTransfer(int transfer){
		ArrayList<Item> itemList = new ArrayList<Item>();
		ArrayList<Item> transferList = new ArrayList<Item>();
		
		if(m_util.stop || transfer == 0) return;
		
		Inventory inv = null;
		while(inv == null && !m_util.stop){
			m_util.wait(100);
			inv = m_util.getInventory("Chest");
		}
		m_util.wait(200);
		
		if(m_util.stop) return;
		
		itemList = m_util.getItemsFromInv(inv);
		
		for(int j = 0; j < transfer && !m_util.stop; j++){
			Item highest = null;
			
			for(Item i : itemList){
				if( seedTest( i.GetResName() ) && !transferList.contains(i)){
					if(highest == null){
						highest = i;
					}else if(highest.q < i.q && m_highQplant){
						highest = i;
					}else if(highest.q > i.q && !m_highQplant){
						highest = i;
					}
				}
			}
			if(highest != null)
				transferList.add(highest);
		}
		
		if(transferList.size() == 0) return;
		
		int inventoryItems = m_util.getPlayerBagItems();
		
		for(Item i : transferList)
			if(!m_util.stop) m_util.transferItem(i);
		
		
		/*int count = 0;
		while( ( inventoryItems + transfer) != m_util.getPlayerBagItems() && !m_util.stop){
			m_util.wait(100);
			count++;
			if(count > 600){
				count = 0;
				for(Item i : transferList)
					if(!m_util.stop) m_util.transferItem(i);
			}
		}*/
	}
	
	void drinkWater(){
		//System.out.println("start drinkwater");
		Gob schest = m_util.findClosestObject("gfx/terobjs/furniture/cclosed", m_pos1.add(0,-11*1000), m_pos2, new Coord(m_pos2.x, m_pos1.y) );
		if(schest == null) return;
		Coord mem = new Coord(m_util.getPlayerCoord());
		
		Inventory bag = m_util.getInventory("Inventory");
		
		m_util.clickWorld(3, m_util.getPlayerCoord() );
		m_util.goToWorldCoord(schest.getr().add(0,4));
		
		Coord dropItemC = m_util.emptyItemSlot(bag);
		if(dropItemC != null) m_util.dropItemInBag(dropItemC);
		
		m_util.clickWorldObject(3, schest);
		//System.out.println("drinkwater 1");
		
		Inventory inv = null;
		while(inv == null && !m_util.stop){
			m_util.wait(200);
			inv = m_util.getInventory("Chest");
		}
		
		m_util.wait(400);
		Item waterBucket = m_util.getItemFromInventory(inv, "bucket-water");
		
		if(waterBucket == null){
			return;
		}
		Coord bucketC = new Coord(waterBucket.c);
		
		Item flask = m_util.getItemFromInventory(inv, "waterflask");
		
		/*boolean holding = false;
		Item mouseItem = null;
		if(!m_util.mouseHoldingAnItem()){
			m_util.pickUpItem(waterBucket);
			while(!m_util.mouseHoldingAnItem() && !m_util.stop)	m_util.wait(200);
			holding = false;
		}else{
			mouseItem = m_util.getMouseItem();
			m_util.dropItemInInv(bucketC, inv);
			while(mouseItem == m_util.getMouseItem() && !m_util.stop) m_util.wait(200);
			holding = true;
		}*/
		//System.out.println("drinkwater 2");
		m_util.pickUpItem(waterBucket);
		
		m_util.itemInteract(flask);
		
		m_util.wait(100);
		
		m_util.dropItemInInv(bucketC, inv);
		
		/*if(!holding){
			while(m_util.mouseHoldingAnItem() && !m_util.stop) m_util.wait(100);
		}else{
			while(!(mouseItem.GetResName().equals( m_util.getMouseItem().GetResName() ) ) && !m_util.stop ) m_util.wait(200);
		}*/
		//System.out.println("drinkwater a2");
		m_util.itemAction(flask);
		//System.out.println("drinkwater a3");
		int count = 0;
		while(!m_util.flowerMenuReady() && !m_util.stop){
			m_util.wait(200);
			count++;
			if(count > 25){
				//System.out.println("Reclick flask for flower.");
				m_util.itemAction(flask);
				count = 0;
			}
		}
		//System.out.println("drinkwater b3");
		m_util.flowerMenuSelect("Drink");
		//System.out.println("drinkwater 4");
		while(!m_util.hasHourglass() && !m_util.stop) m_util.wait(50);
		while(m_util.hasHourglass() && !m_util.stop) m_util.wait(50);
		//System.out.println("drinkwater 5");
		
		if(dropItemC != null){
			m_util.pickUpItem(dropItemC, bag);
			while(!m_util.mouseHoldingAnItem() && !m_util.stop)	m_util.wait(200);
		}
		//System.out.println("end drinkwater");
		//m_util.goToWorldCoord(mem);
	}
	
	void manageChests(int chestDirective){
		int dumpNum = 0;
		boolean first = true;
		coordSorting();
		m_util.openInventory();
		m_util.setPlayerSpeed(2);
		ArrayList<Gob> unsortedChests = new ArrayList<Gob>();
		ArrayList<Gob> chestList = new ArrayList<Gob>();
		int invSpace = m_util.getPlayerBagSpace();
		int invSize = m_util.getPlayerBagSize();
		int troughNum = 0;
		int wheatCount = invSize - invSpace;
		int chestSide = 1;
		
		if(chestDirective == 6) chestSide = -1;
		Coord c = new Coord(m_p2.x+5, m_p1.y);
		Coord d = m_p2.add(5,0);
		
		unsortedChests = m_util.getObjects("chest",c ,d);
		chestList =  m_util.superSortGobList(unsortedChests, false, true, true);
		
		//System.out.println(chestList.size());
		
		if(!m_util.windowOpen("Wheat Flour") && chestDirective == 1)
			m_util.sendAction("craft","flour");
		
		if(!m_util.windowOpen("Boiled Pepper Drupes") && chestDirective == 5)
			m_util.sendAction("craft","boiledpepper");
		
		if(!m_util.windowOpen("Bone Glue") && chestDirective == 6)
			m_util.sendAction("craft","boneglue");
		
		boolean redoCyckle = true;
		while(redoCyckle && !m_util.stop){
			redoCyckle = false;
			
			for(int chestNum = 0; chestNum < chestList.size() && !m_util.stop; chestNum++){
				Gob g = chestList.get(chestNum);
				Inventory inv = goToChest(g ,chestSide);
				
				if(m_util.stop) return;
				
				if(chestDirective == 1){
					m_util.wait(100);
					ArrayList<Item> itemList = new ArrayList<Item>();
					itemList = m_util.getItemsFromInv(inv);
					
					for(Item i : itemList){
						if( i.GetResName().contains("gfx/invobjs/seed-wheat") ){
							m_util.transferItem(i);
							wheatCount++;
						}
					}
					
					if(invSize < wheatCount || chestNum == chestList.size() - 1){
						if(invSize != wheatCount){
							if(invSize > wheatCount && chestNum == chestList.size() - 1) chestNum++;
							chestNum--;
						}
						
						wheatCount = 0;
						
						drinkWater();
						grindFlouwer();
						//m_util.wait(400);
						
						for(int dumpChest = dumpNum; dumpChest < chestList.size() && !m_util.stop; dumpChest++){
							dumpNum = dumpChest;
							Gob dumpLC = chestList.get(dumpChest);
							Inventory reinv = goToChest(dumpLC, 1);
							invSpace = m_util.getInvSpace(reinv);
							int itemCount = m_util.getPlayerBagItems();
							
							for(int i = 0; i < 28 && !m_util.stop; i++)
								m_util.transferItemTo(reinv, 1);
							
							if(itemCount <= invSpace){
								break;
							}
						}
					}
				}else if(chestDirective == 2){
					//if(inv == null) inv = goToChest(g, 1);
					int itemCount = m_util.getItemsFromInv(inv).size();
					
					//System.out.println(itemCount);
					invSpace = invSpace - itemCount;
					for(int i = 0; i < 48 && !m_util.stop; i++)
							m_util.transferItemFrom(inv, 1);
					if(invSpace <= 0){
						chestNum--;
						troughNum = emptyIntoTrough(troughNum);
						invSpace = m_util.getPlayerBagSpace();
					}
				}else if(chestDirective == 3){
					ArrayList<Item> itemList = new ArrayList<Item>();
					itemList = m_util.getItemsFromInv(inv);
					
					for(Item i : itemList){
						m_util.dropItemOnGround(i);
					}
				}else if(chestDirective == 4){
					m_util.wait(100);
					ArrayList<Item> itemList = new ArrayList<Item>();
					itemList = m_util.getItemsFromInv(inv);
					boolean found = false;
					for(Item i : itemList){
						if( i.GetResName().contains("gfx/invobjs/grapes") ){
							m_util.transferItem(i);
							found = true;
						}
					}
					
					if(found) drinkWater();
					if(grapeCheck(true)) chestNum--;
					
					Inventory reinv = goToChest(g, 1);
					for(int i = 0; i < 28 && !m_util.stop; i++)
						m_util.transferItemTo(reinv, 1);
				}else if(chestDirective == 5){
					m_util.wait(100);
					ArrayList<Item> itemList = new ArrayList<Item>();
					itemList = m_util.getItemsFromInv(inv);
					
					for(Item i : itemList){
						if( i.GetResName().equals("gfx/invobjs/seed-pepper") && (wheatCount < invSize - 1) ){
							m_util.transferItem(i);
							wheatCount++;
						}
					}
					
					if( (invSize - 1) <= wheatCount || chestNum == chestList.size() - 1){
						if( (invSize - 1) != wheatCount){
							if( (invSize - 1) > wheatCount && chestNum == chestList.size() - 1) chestNum++;
							chestNum--;
						}
						
						wheatCount = 0;
						
						cauldronCoocker(false);
						
						for(int dumpChest = dumpNum; dumpChest < chestList.size() && !m_util.stop; dumpChest++){
							dumpNum = dumpChest;
							Gob dumpLC = chestList.get(dumpChest);
							Inventory reinv = goToChest(dumpLC, 1);
							invSpace = m_util.getInvSpace(reinv);
							int itemCount = m_util.getPlayerBagItems();
							
							for(int i = 0; i < 48 && !m_util.stop; i++)
								m_util.transferItemTo(reinv, 1);
							
							if(itemCount <= invSpace){
								break;
							}
						}
					}
				}else if(chestDirective == 6){
					m_util.wait(100);
					ArrayList<Item> itemList = new ArrayList<Item>();
					itemList = m_util.getItemsFromInv(inv);
					
					for(Item i : itemList){
						if( i.GetResName().equals("gfx/invobjs/bone") ){
							m_util.transferItem(i);
							wheatCount++;
						}else if( i.GetResName().equals("gfx/invobjs/antlers-deer") ){
							m_util.transferItem(i);
							wheatCount++;
						}else if( i.GetResName().equals("gfx/invobjs/tooth-bear") ){
							m_util.transferItem(i);
							wheatCount++;
						}else if( i.GetResName().equals("gfx/invobjs/tusk") ){
							m_util.transferItem(i);
							wheatCount++;
						}else if( i.GetResName().equals("gfx/invobjs/wishbone") ){
							m_util.transferItem(i);
							wheatCount++;
						}
					}
					
					if(invSize < wheatCount || chestNum == chestList.size() - 1){
						if(invSize != wheatCount){
							if(invSize > wheatCount && chestNum == chestList.size() - 1) chestNum++;
							chestNum--;
						}
						
						cauldronCoocker(true);
						
						wheatCount = m_util.countItemsInBag("invobjs");
						wheatCount = wheatCount - m_util.countItemsInBag("gfx/invobjs/boneglue");
						
						for(int dumpChest = dumpNum; dumpChest < chestList.size() && !m_util.stop; dumpChest++){
							dumpNum = dumpChest;
							Gob dumpLC = chestList.get(dumpChest);
							Inventory reinv = goToChest(dumpLC, chestSide);
							invSpace = m_util.getInvSpace(reinv);
							
							int itemCount = m_util.countItemsInBag("gfx/invobjs/boneglue");
							for(Item i : m_util.getItemsFromBag()){
								if( i.GetResName().contains("gfx/invobjs/boneglue") ){
									m_util.transferItem(i);
								}
							}
							if(itemCount <= invSpace){
								break;
							}
						}
					}
				}
			}
			
			if(chestDirective == 2){
				troughNum = emptyIntoTrough(troughNum);
				
				if(!checkLastTrough(troughNum) && m_util.getPlayerBagItems() > 0){
					if(first){
						redoCyckle = true;
						first = false; 
						System.out.println("Redoing trought cyckle");
					}
				}
			}
		}
	}
	
	void cauldronCoocker(boolean glue){
		Coord mem = new Coord(m_util.getPlayerCoord());
		Gob cauldron = m_util.findClosestObject("gfx/terobjs/cauldron");
		if(cauldron == null) return;
		
		int inv = 1;
		if(glue) inv = -1;
		Coord cauldronStation = cauldron.getr().add(0,inv*7);
		
		m_util.walkTo(cauldronStation);
		cookAtCauldron(cauldron, glue);
		
		Inventory bag = m_util.getInventory("Inventory");
		Coord dropItemC = m_util.emptyItemSlot(bag);
		if(dropItemC != null) m_util.dropItemInBag(dropItemC);
		
		m_util.walkTo(mem);
	}
	
	void cookAtCauldron(Gob cauldron, boolean glue){
		int type = 1;
		if(glue) type = 7;
		CraftScript CS = new CraftScript();
		CS.m_util = m_util;
		CS.processMats(type);
	}
	
	int emptyIntoTrough(int num){
		boolean redo = true;
		while(redo && !m_util.stop){
			redo = false;
			dumpFood(num);
			//System.out.println("num" + num);
			if(m_util.mouseHoldingAnItem()){
				//System.out.println("num" + num);
				num++;
				redo = true;
			}
			
			if( getTroughs().size() <= num ) num = 0;
		}
		
		return num;
	}
	
	void dumpFood(int num){
		Gob trough = getTroughs().get(num);
		m_util.goToWorldCoord(trough.getr().add(16,0) );
		while(m_util.windowOpen("Trough") && !m_util.stop){ m_util.wait(100);}
		
		dumpTrough(trough);
	}
	
	boolean checkLastTrough(int num){
		ArrayList<Gob> troughs = getTroughs();
		Gob trough = troughs.get(num);
		m_util.goToWorldCoord(trough.getr().add(16,0) );
		
		if(troughFull(trough) ) return true;
		
		return false;
	}
	
	private void pickUpAnyItem(){
		Item ite = m_util.getItemFromBag("invobjs");
		if(ite == null){
			//m_util.stop = true;
			return;
		}
		
		m_util.pickUpItem(ite);
		while(!m_util.mouseHoldingAnItem() && !m_util.stop){
			m_util.wait(200);
		}
	}
	
	public void dumpTrough(Gob trough){
		m_util.openInventory();
		pickUpAnyItem();
		
		Item j = m_util.getMouseItem();
		int count = 0;
		
		if(j == null) return;
		
		if( troughFull(trough) ) return;
		
		while(m_util.mouseHoldingAnItem() && !m_util.stop){
			m_util.itemActionWorldObject(trough, 1);
			
			if(j == m_util.getMouseItem()){
				//System.out.println(count);
				count++;
				if(count > 10){
					if( troughFull(trough) ) return;
					count = 0;
				}
			}else{
				//System.out.println("boom");
				j = m_util.getMouseItem();
				count = 0;
			}
			
			m_util.wait(130);
		}
	}
	
	boolean troughFull(Gob trough){
		m_util.clickWorldObject(3, trough);
		//System.out.println("Trough 1.");
		//while(!m_util.windowOpen("Trough") && !m_util.stop){ m_util.wait(100);}
		while(m_util.getVmeterAmount(255, true) == -1 && !m_util.stop){ m_util.wait(100);}
		//System.out.println("Trough 2.");
		if(m_util.getVmeterAmount(255, true) < 100){
			//System.out.println("Trough failed.");
			return false;
		}
		return true;
	}
	
	void grindFlouwer(){
		int seeds = m_util.countItemsInBag("gfx/invobjs/seed-wheat");
		
		if(seeds > 1){
			if(!m_util.windowOpen("Wheat Flour"))
				m_util.sendAction("craft","flour");
			goToQuern();
			m_util.wait(200);
			m_util.craftItem(1);
			int count = 0;
			while(/*!m_util.hasHourglass() && */!m_util.stop){
				m_util.wait(200);
				String str = new String(m_util.slenError());
				if(str.contains("You need a quern to make a wheat flour.")){
					goToQuern();
					m_util.sendSlenMessage("Going to Quern again.");
				}else if(str.contains("You do not have all the ingredients.")){
					m_util.sendSlenMessage("Finished Crafting.");
					break;
				}
				
				if(!m_util.hasHourglass() ) count++;
				if(count > 10){
					count = 0;
					m_util.craftItem(1);
					m_util.sendSlenMessage("Recraft.");
					//System.out.println("Recraft");
				}
				
				dropHoldingItemInBag();
			}
			/*while(m_util.hasHourglass() && !m_util.stop){
				m_util.wait(200);
				dropHoldingItemInBag();
			}*/
			
			m_util.clickWorld(1, m_util.getPlayerCoord());
			m_util.wait(200);
		}
	}
	
	void goToQuern(){
		Gob player = m_util.getPlayerGob();
		
		Gob g = m_util.findClosestObject("gfx/terobjs/quern", m_p1.add(0,-1000), m_p2);
		
		m_util.clickWorldObject(3, g);
		int count = 0;
		while(!m_util.checkPlayerWalking() && count < 100 && !m_util.stop){
			m_util.wait(50);
			count++;
		}
		while(m_util.checkPlayerWalking() && !m_util.stop){ m_util.wait(200);}
		m_util.wait(500);
	}
	
	void dropHoldingItemInBag(){
		if(m_util.mouseHoldingAnItem()){
			Coord c = m_util.emptyItemSlot(m_util.getInventory("Inventory") );
			if(c != null) m_util.dropItemInBag(c );
			
			/*int bagSize = m_util.getPlayerBagSize();
			Coord c = new Coord();
			
			m_util.wait(500);
			Inventory bag = m_util.getInventory("Inventory");
			Item item = m_util.getMouseItem();
			
			if(bagSize == 24) c = new Coord(6*30 - 10, 4*30 - 10);
			if(bagSize == 30) c = new Coord(6*30 - 10, 5*30 - 10);
			if(bagSize == 35) c = new Coord(7*30 - 10, 5*30 - 10);
			if(bagSize == 42) c = new Coord(7*30 - 10, 6*30 - 10);
			if(bagSize == 48) c = new Coord(8*30 - 10, 6*30 - 10);
			if(bagSize == 56) c = new Coord(8*30 - 10, 7*30 - 10);
			
			m_util.dropItemInBag(c);
			while(m_util.mouseHoldingAnItem() && !m_util.stop) m_util.wait(200);*/
		}
	}
	
	ArrayList<Gob> getTroughs(){
		ArrayList<Gob> unsortedTroughs = new ArrayList<Gob>();
		ArrayList<Gob> sortedTroughs = new ArrayList<Gob>();
		
		unsortedTroughs = m_util.getObjects("trough", m_p1.add(0,-1000), m_p2);
		sortedTroughs = m_util.sortGobList1(unsortedTroughs);
		
		return sortedTroughs;
	}
	
	ArrayList<Coord> multiField(boolean emptyRows){
		ArrayList<Coord> innerFieldCoords = new ArrayList<Coord>();
		ArrayList<Coord> filterdInnerFieldCoords = new ArrayList<Coord>();
		coordSorting();
		
		Gob memoryPlant = null;
		
		for(int y = m_p2.div(11).y; y >= m_p1.div(11).y; y--){
			ArrayList<Gob> plants = new ArrayList<Gob>();
			
			Coord p1 = new Coord(m_p1.x, y*11);
			Coord p2 = new Coord(m_p2.x, y*11+10);
			
			plants = m_util.getObjects("plants", p1, p2);
			Gob plantCheck = null;
			
			if(!emptyRows){
				for(Gob p : plants){
					if(plantCheck == null){
						plantCheck = p;
					}
					if( !plantCheck.resname().equals( p.resname() ) ){
						m_util.stop = true;
						return filterdInnerFieldCoords;
					}
				}
				
				if(plantCheck != null){
					if(memoryPlant == null){
						memoryPlant = plantCheck;
						innerFieldCoords.add(p2);
					}else if( !memoryPlant.resname().equals( plantCheck.resname() ) ){
						innerFieldCoords.add(p1.add(0,11) );
						
						memoryPlant = plantCheck;
						innerFieldCoords.add(p2);
					}
				}else if(memoryPlant != null){
					innerFieldCoords.add(p1.add(0,11) );
					memoryPlant = null;
				}
				
				if(memoryPlant != null && y == m_p1.div(11).y) innerFieldCoords.add(p1);
			}else{
				ArrayList<Gob> ignoreFirstStage = new ArrayList<Gob>();
				
				for(Gob stageCheck : plants)
					if( getPlantStage(stageCheck) > 0)
						ignoreFirstStage.add(stageCheck);
				
				if(y == m_p2.div(11).y){
					if(ignoreFirstStage.size() > 0){
						memoryPlant = plants.get(0);
					}else{
						innerFieldCoords.add(p2);
					}
				}else if(ignoreFirstStage.size() > 0 && memoryPlant == null){
					memoryPlant = ignoreFirstStage.get(0);
					innerFieldCoords.add(p1.add(0,11) );
				}else if(ignoreFirstStage.size() == 0 && memoryPlant != null){
					innerFieldCoords.add(p2);
					memoryPlant = null;
				}
				
				if(memoryPlant == null && y == m_p1.div(11).y) innerFieldCoords.add(p1);
			
			}
			
		}
		
		if(emptyRows){
			for(int i = 0; i < innerFieldCoords.size(); i+=2){
				Coord c1 = new Coord( innerFieldCoords.get(i) );
				Coord c2 = new Coord( innerFieldCoords.get(i+1) );
				
				if(getSeedingTiles(c1, c2).size() > 0 ){
					filterdInnerFieldCoords.add(c1);
					filterdInnerFieldCoords.add(c2);
				}
			}
			
			return filterdInnerFieldCoords;
		}
		
		for(int i = 0; i < innerFieldCoords.size(); i+=2){
			ArrayList<Gob> growingPlants = new ArrayList<Gob>();
			boolean growingCheck = true;
			
			Coord c1 = new Coord( innerFieldCoords.get(i) );
			Coord c2 = new Coord( innerFieldCoords.get(i+1) );
			
			growingPlants = m_util.getObjects("plants", c1, c2);
			
			for(Gob g: growingPlants){
				int stage = getPlantStage(g);
				
				if(farmable(g.resname(), stage, true) == -1){
					growingCheck = false;
					break;
				}
			}
			
			if(growingCheck){
				filterdInnerFieldCoords.add(c1);
				filterdInnerFieldCoords.add(c2);
			}
		}
		
		return filterdInnerFieldCoords;
	}
	
	public void areaSelectOnPlayer(){
		Coord setP1 = new Coord();
		Coord setP2 = new Coord();
		
		Coord origin = new Coord(m_util.getPlayerCoord().div(11));
		
		int cyckle = 0;
		while(!m_util.stop){
			if( m_util.getTileID(origin.add(cyckle,0)) == 8 )
				break;
			
			//System.out.println(  m_util.getTileID(origin.add(cyckle,0)) );
			cyckle--;
			
			if(cyckle < -1000){m_util.stop = true; System.out.println("Area to large 1");}
		}
		setP1.x = origin.x + cyckle + 1;
		
		cyckle = 0;
		while(!m_util.stop){
			if( m_util.getTileID(origin.add(cyckle,0)) == 8 )
				break;
			
			cyckle++;
			
			if(cyckle > 1000){m_util.stop = true; System.out.println("Area to large 2");}
		}
		setP2.x = origin.x + cyckle - 1;
		
		cyckle = 0;
		while(!m_util.stop){
			if( m_util.getTileID(origin.add(0,cyckle)) == 8 )
				break;
			cyckle--;
			
			if(cyckle < -1000){m_util.stop = true; System.out.println("Area to large 3");}
		}
		setP1.y = origin.y + cyckle + 1;
		
		cyckle = 0;
		while(!m_util.stop){
			if( m_util.getTileID(origin.add(0,cyckle)) == 8 )
				break;
			cyckle++;
			
			if(cyckle > 1000){m_util.stop = true; System.out.println("Area to large 4");}
		}
		setP2.y = origin.y + cyckle - 1;
		
		m_p1 = setP1.mul(11);
		m_p2 = setP2.mul(11);
		
		m_pos1 = setP1.mul(11);
		m_pos2 = setP2.mul(11).add(10,10);
	}
	
	void emptyLC(){
		for(int i = 0; i < m_fieldCoords.size(); i+=2){
			m_p1 = new Coord( m_fieldCoords.get(i) );
			m_p2 = new Coord( m_fieldCoords.get(i+1) );
			
			manageChests(3);
		}
	}
	
	public int getPlantStage(Gob plant){
		/*ResDrawable dw = plant.getattr(ResDrawable.class);
		if(dw != null)
		{
			if(dw.getsdt() != null)
			{
				if(dw.getsdt().blob.length > 0)
					return dw.getsdt().blob[0];
			}
		}*/
		
		return plant.GetBlob(0);
	}
	
	void beehivePlantation(){
		coordSorting();
		m_util.openInventory();
		
		m_farmList = getFarmList();
		
		for(int i = 0; i < m_farmList.size() && !m_util.stop; i++){
			Gob g = (Gob) m_farmList.get(i);
		
		//while(m_farmList.size() > 0 && !m_util.stop){
			//Gob g = m_util.getClosestObjectInArray(m_farmList);
			
			if(m_util.stop)
				return;
			
			int invSpace = m_util.getPlayerBagSpace();
			
			m_util.sendAction("harvest");
			m_util.clickWorldObject(1, g);
			
			boolean notharvested = true;
			while(notharvested && !m_util.stop){
				ArrayList<Gob> checklist = getFarmList();
				
				notharvested = false;
				for(Gob o: checklist)
				{
					if(o.id == g.id)
					{
						notharvested = true;
					}
				}
				
				m_util.wait(100);
			}
			
			while(m_util.getPlayerBagSpace() > invSpace - 3 && !m_util.stop) m_util.wait(100);
			
			plantBestDumpRest(m_util.getPlayerCoord() );
			
			if(m_util.getStamina() < 40){
				m_util.quickWater();
			}
			
			//m_farmList.remove(g);
		}
	}
	
	void plantBestDumpRest(Coord plantTile){
		int invSpace = m_util.getPlayerBagSpace();
		int dropSize = 0;
		
		if(invSpace == 2) dropSize = 1;
		if(invSpace == 1) dropSize = 2;
		if(invSpace == 0) dropSize = 3;
		
		ArrayList<Item> ignore = dropLowest(dropSize);
		
		//if(m_util.getTileID(plantTile.div(11)) != 9) return;
		
		pickupHighest(ignore);
		
		plantWithPlow(plantTile);
		
		/*m_util.itemAction(plantTile, 0);
		
		int count = 0;
		while(m_util.getObjects("plants", plantTile, plantTile).size() == 0 && m_util.mouseHoldingAnItem() && !m_util.stop){
			m_util.wait(10);
			count++;
			if(count > 4000){
				break;
			}
		}*/
		
		//pickupHighest();
	}
	
	public void plantWithPlow(Coord plantTile){
		int flop = 1;
		boolean redoPlant = true;
		
		while(redoPlant && !m_util.stop){
			//System.out.println("replant cyckle on");
			redoPlant = false;
			Coord errorSpot = plantTile.div(11).mul(11).add(5, 5 + 3 * flop);
			flop *= -1;
			
			//System.out.println("errorSpot" + errorSpot);
			m_util.clickWorld(1, errorSpot);
			m_util.itemAction(plantTile, 0);
			//m_util.itemAction(plantTile, m_pause);
			
			int count = 0;
			
			while(m_util.getObjects("plants", plantTile, plantTile).size() == 0 && m_util.mouseHoldingAnItem() && !m_util.stop){
				m_util.wait(50);
				//m_util.itemAction(plantTile);
				
				if(m_util.getPlayerCoord().equals(errorSpot ) && !m_util.checkPlayerWalking() ){
					count++;
					if(count > 10){
						if(flop == 1) triggerHandPlow(plantTile, true);
						redoPlant = true;
						break;
					}
				}
			}
		}
	}
	
	ArrayList<Item> dropLowest(int num){
		Inventory inv = m_util.getInventory("Inventory");
		ArrayList<Item> dropList = new ArrayList<Item>();
		ArrayList<Item> tempList = new ArrayList<Item>();
		tempList = m_util.getItemsFromInv(inv);
		
		for(int j = 0; j < num; j++){
			Item lowest = null;
			
			for(Item i : tempList){
				String name = i.GetResName();
				
				if(!seedTest(i.GetResName() ) ) continue;
				
				if(!dropList.contains(i) ){
					if(lowest == null){
						lowest = i;
					}else if(lowest.q > i.q && m_highQplant){
						lowest = i;
					}else if(lowest.q < i.q && !m_highQplant){
						lowest = i;
					}
				}
			}
			
			dropList.add(lowest);
		}
		
		for(Item i : dropList)
			m_util.dropItemOnGround(i);
		
		return dropList;
	}
	
	public void pickupHighest(ArrayList<Item> ignore){
		ArrayList<Item> plantList = new ArrayList<Item>();
		Inventory inv = m_util.getInventory("Inventory");
		Item highest = null;
		
		m_util.clickWorld(3, m_util.getPlayerCoord() );
		if(!m_util.mouseHoldingAnItem() ){
			plantList = m_util.getItemsFromInv(inv);
			
			for(Item i : plantList){
				if(!seedTest(i.GetResName() ) || ignore.contains(i) ) continue;
				
				if(highest == null){
					highest = i;
				}else if(highest.q < i.q && m_highQplant){
					highest = i;
				}else if(highest.q > i.q && !m_highQplant){
					highest = i;
				}
			}
			
			m_util.pickUpItem(highest);
			while(!m_util.mouseHoldingAnItem() && !m_util.stop) m_util.wait(50);
			
		}
	}
	
	void beeHiveHarvester(){
		Coord startC = m_util.getPlayerCoord();
		m_util.openInventory();
		m_util.setPlayerSpeed(0);
		
		Coord bucketC = null;
		Item bucket = null;
		
		while(!m_util.stop){
			if(m_util.getPlayerBagSpace() < 8){
				dumpWax(startC);
			}
			
			ArrayList<Gob> list = m_util.getObjects("gfx/terobjs/bhive", m_p1, m_p2); 
			//ArrayList<Coord> hives = new ArrayList<Coord>();
			
			if(list.size() == 0){
				m_util.goToWorldCoord(startC );
				m_util.goToWorldCoord(startC.add(11,0) );
				m_util.goToWorldCoord(startC.add(11,11) );
				m_util.goToWorldCoord(startC.add(0,11) );
			}
			
			for(Gob bh : list){
				//hives.add(h.getr() );
			//}
			//for(Coord c : hives){
				//Gob bh = m_util.findClosestObject("gfx/terobjs/bhive", c.sub(11,11), c.add(11,11));
				m_util.clickWorldObject(3, bh);
				m_util.goToWorldCoord(startC );
				m_util.goToWorldCoord(startC.add(11,0) );
				m_util.goToWorldCoord(startC.add(11,11) );
				m_util.goToWorldCoord(startC.add(0,11) );
				
				if(m_util.flowerMenuReady() ){
					int count = 0;
					m_util.flowerMenuSelect("Harvest Wax");
					while(m_util.flowerMenuReady() && count < 50 && !m_util.stop){
						m_util.wait(200);
						//m_util.flowerMenuSelect("Harvest Wax");
						count++;
					}
					count = 0;
					while(count < 20 && !m_util.stop){
						m_util.wait(50);
						//m_util.flowerMenuSelect("Harvest Wax");
						if(!m_util.checkPlayerWalking()) count++;
					}
					m_util.wait(500);
				}
				
				if(honeyTest(bh.getr() ) ){
					if(!m_util.mouseHoldingAnItem() ){
						bucket = m_util.getItemFromBag("bucket");
						if(bucket != null){
							bucketC = new Coord(bucket.c);
							m_util.pickUpItem(bucket);
							while(!m_util.mouseHoldingAnItem() && !m_util.stop)	m_util.wait(100);
						}
					}
					
					if(bucket != null){
						int count = 0;
						while( honeyTest(bh.getr()) && count < 10 && !m_util.stop){
							m_util.itemActionWorldObject(bh, 0);
							m_util.wait(500);
							count++;
						}
						count = 0;
						while(count < 20 && !m_util.stop){
							m_util.wait(50);
							//m_util.flowerMenuSelect("Harvest Wax");
							if(!m_util.checkPlayerWalking()) count++;
						}
						ArrayList<Item> inter = m_util.getItemsFromBag();
						
						for(Item i : inter){
							m_util.itemInteract(i);
						}
						
						m_util.dropItemInBag(bucketC);
					}
				}
			}
		}
		m_util.setPlayerSpeed(2);
	}
	
	boolean m_dropWax = true;
	void dumpWax(Coord startC){
		if(!m_dropWax) return;
		
		ArrayList<Gob> LCs = m_util.getObjects("gfx/terobjs/lchest", 165);
		boolean emptied = false;
		for(Gob lc : LCs){
			Inventory inv = m_util.walkToContainer(lc, "Chest");
			
			int space = m_util.getInvSpace(inv);
			int waxCount = 0;
			
			ArrayList<Item> itemList = m_util.getItemsFromBag();
			for(Item i : itemList){
				if(i.GetResName().contains("beeswax") ){
				//if(i.GetResName().contains("carrot") ){
					waxCount++;
					m_util.transferItem(i);
				}
			}
			
			if(space >= waxCount){
				emptied = true;
				break;
			}
		}
		
		if(!emptied){
			System.out.println("All LCs full of wax, cant drop off.");
			m_dropWax = false;
		}
		m_util.walkTo(startC);
	}
	
	boolean honeyTest(Coord p){
		return m_util.findClosestObject("gfx/terobjs/bhived", p.sub(11,11), p.add(11,11)) != null;
	}
	
	ArrayList<Integer> cropStage = new ArrayList<Integer>();
	
	int changeStage(){
		ArrayList<Integer> checkStage = new ArrayList<Integer>();
		int stageCount = 0;
		ArrayList<Gob> plants = m_util.getObjects("plants", m_p1, m_p2);
		ArrayList<Gob> checklist = m_util.superSortGobList(plants, false, false, false);
		
		for(Gob g : checklist){
			checkStage.add(getPlantStage(g) );
		}
		
		if(cropStage.size() != checkStage.size() ){
			System.out.println("New crop data, plants found: " + checkStage.size() );
		}else{
			for(int i = 0; i < cropStage.size() && i < checkStage.size(); i++){
				if(checkStage.get(i) != cropStage.get(i) ) stageCount++;
			}
		}
		
		cropStage.clear();
		cropStage = checkStage;
		
		return stageCount;
	}
	
	void beehiveDance(Coord bh){
		ArrayList<Coord> sides = new ArrayList<Coord>();
		m_util.setPlayerSpeed(0);
		
		
		sides.add( bh.add(-6,-6) );
		sides.add( bh.add(-6,6) );
		sides.add( bh.add(6,6) );
		sides.add( bh.add(6,-6) );
		
		Coord farthest = null;
		for(Coord c : sides){
			if(farthest == null){
				farthest = c;
			}else if( c.dist(m_util.getPlayerCoord() ) > farthest.dist(m_util.getPlayerCoord() ) ){
				farthest = c;
			}
		}
		
		boolean started = false;
		for(Coord c : sides){
			if(started || !farthest.equals(c) ){
				started = true;
				m_util.goToWorldCoord(c);
			}
		}
	}
	
	void beeHiveCoolDown(){
		Gob bh = m_util.findClosestObject("gfx/terobjs/bhive", m_p1, m_p2);
		Coord c = bh.getr().add(-5,-5);
		Coord d = c.add(11,0);
		
		m_util.sendAction("carry");
		//while(!(m_util.getCursor().contains("chi")) && !m_util.stop) { m_util.wait(200); }
		m_util.clickWorldObject(1, bh);
		while(!m_util.checkPlayerCarry() && !m_util.stop){m_util.wait(200);}
		
		while(!m_util.stop){
			m_util.goToWorldCoord(c);
			m_util.goToWorldCoord(d);
		}
	}
	
	private void farmBuds(){
		m_util.setPlayerSpeed(0);
		Coord c = m_util.getPlayerCoord();
		
		while(!m_util.stop){
			ArrayList<Gob> plants = m_util.getObjects("gfx/terobjs/plants/hemp", m_p1, m_p2);
			
			for(Gob g : plants){
				if(getPlantStage(g) == 3){
					m_util.sendAction("harvest");
					m_util.clickWorldObject(1, g);
					gobRemovePause(g);
				}
			}
			
			if(m_util.getPlayerBagSpace() < 3) unloadInventory(m_util.getPlayerCoord(), 1, false);
			
			m_util.clickWorld(3, c);
			//m_util.clickWorld(3, c);
			m_util.goToWorldCoord(c);
			m_util.goToWorldCoord(c.add(0,-10) );
		}
	}
	
	public void gobRemovePause(Gob g){
		boolean serch = true;
		
		while(serch && !m_util.stop){
			serch = false;
			m_util.wait(100);
			
			ArrayList<Gob> getAll = m_util.getObjects(g.resname() );
			
			for(Gob a : getAll){
				if(a.id == g.id){ serch = true;  break;}
			}
		}
	}
	
	ArrayList<Coord> getSeeding(Coord p1, Coord p2){
		ArrayList<Coord> tileList = new ArrayList<Coord>();
		ArrayList<Coord> seedingTiles = new ArrayList<Coord>();
		coordSorting();
		tileList = m_util.getFarmTiles(p1, p2);
		
		//System.out.println("tileList"+tileList.size());
		
		for(Coord i : tileList){
			if(m_util.getObjects("plants", i.mul(11), i.mul(11)).size() == 0)
				seedingTiles.add(i);
		}
		return seedingTiles;
	}
	
	void fullFarm3(){
		areaSelectOnPlayer();
		
		m_fieldCoords = multiField(false);
		
		//if(m_dumpLC || getSeedingTiles(m_p1, m_p2).size() == 0) emptyLC();
		
		m_dumpInv = true;
		
		for(int i = 0; i < m_fieldCoords.size(); i+=2){
			m_p1 = new Coord( m_fieldCoords.get(i) );
			m_p2 = new Coord( m_fieldCoords.get(i+1) );
			
			harvestCrops();
		}
		
		//m_util.goToWorldCoord(m_util.getPlayerCoord().add(-5,0));
		
		m_p1 = m_pos1;
		m_p2 = m_pos2;
		
		sortReplant();
	}
	
	void LCfeast(Coord c){
		Gob lc = m_util.findClosestObject("gfx/terobjs/lchest", 33, c);
		if(lc == null) return;
		
		Inventory inv = m_util.walkToContainer(lc, "Chest");
		m_util.advEater(inv);
	}
	
	void rallyFarm(){
		rallyPoints rally = m_util.getRallyPoints();
		Coord feedCoord = null;
		
		for(rallyPoints r : rally.rally){
			if(m_util.stop) return;
			Coord dest = r.c;
			int type = r.type;
			
			if(type == 1){
				m_util.safeWalkTo(dest);
				
			if(m_util.getHunger() < 480){
				m_util.logOut();
				return;
			}
				fullFarm3();
			}else if(type == 2){
				feedCoord = dest;
			}
			
			if(feedCoord != null && m_util.getHunger() < 650){
				m_util.safeWalkTo(feedCoord);
				LCfeast(feedCoord);
			}
		}
	}
	
	public void run(){
		if(m_option == 1){
			areaSelectOnPlayer();
			manageChests(1);
		}
		if(m_option == 2){
			areaSelectOnPlayer();
			manageChests(2);
		}
		if(m_option == 3){
			m_cleanup = true;
			fullFarm3();
		}
		if(m_option == 4){
			rallyFarm();
		}
		if(m_option == 5){
			replantFarm();
		}
		if(m_option == 6){
			sortReplant();
		}
		if(m_option == 7){
			m_cleanup = false;
			m_dumpInv = true;
			harvestCrops();
		}
		if(m_option == 8){
			m_replant = true;
			m_cleanup = false;
			m_dumpInv = true;
			harvestCrops();
		}
		if(m_option == 9){
			m_replant = true;
			beehivePlantation();
		}
		if(m_option == 10){
			beeHiveHarvester();
		}
		if(m_option == 11){
			m_util.openInventory();
			farmBuds();
		}
		if(m_option == 12){
			m_cleanup = false;
			fullFarm3();
		}
		if(m_option == 13){
			areaSelectOnPlayer();
			manageChests(4);
		}
		if(m_option == 14){
			manageChests(5);
		}
		if(m_option == 15){
			Gob cauldron = m_util.findClosestObject("gfx/terobjs/cauldron");
			if(cauldron != null){
				m_p2 = cauldron.getr().div(11).mul(11).add(-33,-22);
				m_p1 = m_p2.add(0,-11*10).div(11).mul(11);
				manageChests(6);
			}
		}
		
		m_util.running(false);
	}
}