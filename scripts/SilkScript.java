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
import java.awt.Rectangle;

public class SilkScript extends Thread{
	public String scriptName = "Silk Script";
	public String[] options = {
		"Silk Start", "Worms To Cubs", "Cook Cocoons", "Sorth Moths", "Fill Cubs With Leafs", "Full Silk With backup", "Drop All Cub Mats"
	};
	
	HavenUtil m_util;
	int m_option;
	String m_modify;
	
	Coord m_relativeCoord = new Coord(0,0);
	int m_directive;
	boolean m_treePicker = false;
	Gob m_tree = null;
	
	ItemSorter m_sortList1 = new ItemSorter();
	ItemSorter m_sortList2 = new ItemSorter();
	
	int m_cubSizeCellar = 72;
	int m_herbalistSizeCount = 54;
	
	int m_mothInvHight = 40;
	
	public void ApocScript(HavenUtil util, int option, String modify){
		m_util = util;
		m_option = option;
		m_modify = modify;
	}
	
	/*public SilkScript(HavenUtil util, int directive, boolean mod){
		m_util = util;
		//m_gob = gob;
		m_directive = directive;
		m_treePicker = mod;
	}*/
	
	void fillCubsWithLeafs(){
		
		Inventory bag = m_util.getInventory("Inventory");
		if(checkCubContent(bag) == -1){
			m_util.sendErrorMessage("Inventory clutterd, clear non-leaf items.");
			m_util.stop = true;
		}else if(m_util.countItemsInBag("gfx/invobjs/mulberryleaf") < 48){
			getLeaves();
			
			goToMantionFloor(-1);
		}else{
			goToMantionFloor(-1);
		}
		
		boolean cubsReady = false;
		boolean deposit;
		while(!cubsReady && !m_util.stop){
			cubsReady = true;
			for(int i = 0; i < m_cubSizeCellar && !m_util.stop; i++){
				Inventory inv = goToCub(i);
				if(m_util.stop) return;
				
				deposit = transferLeaves(inv);
				System.out.println("Cubs finished: "+(i+1));
				if(deposit){
					cubsReady = false;
					getLeaves();
				}
			}
		}
		
		dumpInv();
	}
	
	void dumpInv(){
		if(m_util.stop) return;
		for(Item i : m_util.getItemsFromBag()){
			m_util.dropItemOnGround(i);
		}
	}
	
	void getLeaves(){
		goToMantionFloor(0);
		Gob tree = getTree();
		
		if(m_treePicker){
			Gob LC = getDropLC(tree);
			
			getLeavesFromLC(LC);
		}else{
			int spotNum = getMantionNum();
			
			multiPick(spotNum, tree);
			
			//getLeavesFromTree(tree);
		}
		
		goInViaPF();
		//goToMantionFloor(-1);
	}
	
	void goInViaPF(){
		if(m_relativeCoord.equals(new Coord(0,0))){
			Gob gate = m_util.findClosestObject("gfx/arch/door-inn");
			//m_util.clickWorldObject(3, gate);
			while(!m_util.stop){
				m_util.walkTo(gate.getr().add(0,7));
				
				if(m_util.getPlayerCoord().equals(gate.getr().add(0,7) ) ) break;
			}
			//m_util.goToMantionFloor(0, gate);
		}else{
			Gob gate = m_util.findClosestObject("gfx/arch/door-inn", getRrelativeCoord() );
			//m_util.clickWorldObject(3, gate);
			while(!m_util.stop){
				m_util.walkTo(gate.getr().add(0,7));
				
				if(m_util.getPlayerCoord().equals(gate.getr().add(0,7) ) ) break;
			}
			//m_util.goToMantionFloor(0, gate);
		}
		
		goToMantionFloor(-1);
	}
	
	int getMantionNum(){
		Gob door = m_util.findClosestObject("gfx/arch/door-inn", 30);
		
		while(!m_util.stop && door == null){
			m_util.wait(100);
			door = m_util.findClosestObject("gfx/arch/door-inn", 30);
		}
		
		if(door == null) return -1;
		
		Coord c = door.getr();
		
		ArrayList<Gob> list = m_util.getObjects("gfx/arch/door-inn", c, c.add(36*11, 0) );
		//ArrayList<Gob> sorted = m_util.superSortGobList(list, true, true, true);
		
		int tile = 0;
		
		int id = -1;
		while(id == -1 && !m_util.stop){
			id = m_util.getTileID(c.div(11).add(0,1) );
			m_util.wait(100);
		}
		
		if(id == 4) tile = 2;
		if(id == 3) tile = 1;
		
		return 7 - (list.size() + tile);
	}
	
	Gob getLC(){
		Gob LC = m_util.findClosestObject("gfx/terobjs/lchest");
		
		while(LC == null && !m_util.stop){
			LC = m_util.findClosestObject("gfx/terobjs/lchest");
			m_util.wait(200);
		}
		
		return LC;
	}
	
	void getLeavesFromLC(Gob LC){
		Coord goTo = LC.getr().add(0,-8);
		while(!m_util.stop){
			m_util.walkTo(goTo );
			
			if(m_util.getPlayerCoord().equals(goTo) ) break;
		}
		
		m_util.clickWorldObject(3, LC);
		Inventory inv = null;
		
		while(inv == null && !m_util.stop){
			m_util.wait(100);
			inv = m_util.getInventory("Chest");
		}
		
		while(!m_util.stop){
			int leafCount = 0;
			ArrayList<Item> itemList = m_util.getItemsFromInv(inv);
			
			for(Item i : itemList){
				if(i.GetResName().equals("gfx/invobjs/mulberryleaf") ){
					leafCount++;
				}
			}
			
			if(leafCount == 48){
				for(int i = 0; i < 48; i++)
					m_util.transferItemFrom(inv, 1);
					
				return;
			}
			
			m_util.wait(200);
		}
	}
	
	boolean transferLeaves(Inventory inv){
		/*Inventory inv = null;
		
		while(inv == null && !m_util.stop) {
			m_util.wait(200);
			inv = m_util.getInventory("Cupboard");
		} m_util.wait(400);*/
		
		if(m_util.stop) return false;
		
		int itemCount =  checkCubContent(inv);
		
		if(itemCount >= 0 && itemCount < 48){
			depositLeafs(inv, itemCount);
			return true;
		}else if(itemCount > 48){
			ArrayList<Item> itemList = new ArrayList<Item>();
			itemList = m_util.getItemsFromInv(inv);
			
			for(Item i : itemList)
				m_util.dropItemOnGround(i);
			
			depositLeafs(inv, 0);
			return true;
		}
		
		return false;
	}
	
	void depositLeafs(Inventory inv, int itemCount){
		int transfer = 48 - itemCount;
		for(int i = 0; i < transfer; i++){
			m_util.transferItemTo(inv, 1);
		}
	}
	
	int checkCubContent(Inventory inv){
		ArrayList<Item> list = new ArrayList<Item>();
		list = m_util.getItemsFromInv(inv);
		
		boolean redo = true;
		while(redo && !m_util.stop){
			redo = false;
			
			for(Item i : list){
				if(  i.GetResName().contains("gfx/invobjs/missing") ){
					redo = true;
					break;
				}else if( !( i.GetResName().contains("gfx/invobjs/mulberryleaf") ) ){
					return -1;
				}
			}
			if(redo) m_util.wait(1000);
		}
		
		return list.size();
	}
	
	int checkCubContent(Inventory inv, String s){
		ArrayList<Item> list = new ArrayList<Item>();
		list = m_util.getItemsFromInv(inv);
		int count = 0;
		
		boolean redo = true;
		while(redo && !m_util.stop){
			redo = false;
			
			for(Item i : list){
				if(  i.GetResName().contains("gfx/invobjs/missing") ){
					redo = true;
				}else if(i.GetResName().contains(s) ){
					count++;
				}
			}
			if(redo) m_util.wait(1000);
		}
		
		return count;
	}
	
	Inventory goToCub(int cubNumber){
		if(cubNumber >= m_cubSizeCellar || cubNumber < 0 || m_util.stop){
			m_util.stop = true;
			return null;
		}
		
		ArrayList<Gob> cubList = getCubs();
		
		return m_util.advWalkToContainer(cubList, cubList.get(cubNumber) );
		
		/*Gob cellarStair = null;
		
		while(cellarStair == null && !m_util.stop){
			cellarStair = m_util.findClosestObject("gfx/arch/stairs-cellar");
			m_util.wait(200);
		}
		
		if(m_util.stop) return;
		
		int cellarLine = cellarStair.getr().x;
		
		cubList = getCubs();
		if(m_util.stop) return;
		Gob cub = cubList.get(cubNumber);
		
		Coord frontOfCub = new Coord( cubFrontCoord(cub) );
		
		if( !(frontOfCub.y - 20 < m_util.getPlayerCoord().y && m_util.getPlayerCoord().y < frontOfCub.y + 20) ){
			m_util.goToWorldCoord(new Coord(cellarLine, m_util.getPlayerCoord().y));
			m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, frontOfCub.y));
		}
		
		m_util.goToWorldCoord(frontOfCub);
		
		while(m_util.windowOpen("Cupboard") && !m_util.stop) m_util.wait(100);
		
		m_util.clickWorldObject(3, cub);*/
	}
	
	ArrayList<Gob> getCubs(){
		ArrayList<Gob> unsortedCubs = new ArrayList<Gob>();
		ArrayList<Gob> cubList = new ArrayList<Gob>();
		
		
		while(unsortedCubs.size() != m_cubSizeCellar && !m_util.stop){
			unsortedCubs = m_util.getObjects("gfx/terobjs/cupboard");
			m_util.wait(200);
		}
		
		cubList = m_util.superSortGobList(unsortedCubs, false, true, true);
		
		
		return cubList;
	}
	
	void goToBackupCub(int cubNumber, boolean cocoonToggle){
		ArrayList<Gob> cubList = new ArrayList<Gob>();
		
		if(cubNumber > (m_cubSizeCellar + 14) || cubNumber < m_cubSizeCellar){
			m_util.stop = true;
			return;
		}
		
		Gob cellarDoor = null;
		
		while(cellarDoor == null && !m_util.stop){
			cellarDoor = m_util.findClosestObject("gfx/arch/door-cellar");
			m_util.wait(200);
		}
		
		if(m_util.stop) return;
		
		int cellarLine = cellarDoor.getr().y;
		
		cubList = getBackupCubs();
		if(m_util.stop) return;
		Gob cub = cubList.get(cubNumber - m_cubSizeCellar);
		
		Coord frontOfCub = new Coord( cubFrontCoord(cub) );
		
		if( !(frontOfCub.x - 20 < m_util.getPlayerCoord().x && m_util.getPlayerCoord().x < frontOfCub.x + 2) ){
			if(!cocoonToggle) m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, cellarLine));
			m_util.goToWorldCoord(new Coord(frontOfCub.x, m_util.getPlayerCoord().y));
		}
		
		m_util.goToWorldCoord(frontOfCub);
		
		while(m_util.windowOpen("Cupboard") && !m_util.stop) m_util.wait(100);
		
		m_util.clickWorldObject(3, cub);
	}
	
	ArrayList<Gob> getBackupCubs(){
		ArrayList<Gob> unsortedCubs = new ArrayList<Gob>();
		ArrayList<Gob> cubList = new ArrayList<Gob>();
		
		while(unsortedCubs.size() != 15 && !m_util.stop){
			unsortedCubs = m_util.getObjects("gfx/terobjs/cupboard");
			m_util.wait(200);
		}
		
		cubList = m_util.superSortGobList(unsortedCubs, true, false, false);
		
		
		return cubList;
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
	
	void getLeavesFromTree(Gob tree){
		m_util.clickWorldObject(3, tree);
		while(!m_util.flowerMenuReady() && !m_util.stop){m_util.wait(200);}
		m_util.flowerMenuSelect("Pick leaf");
		while(m_util.countItemsInBag("gfx/invobjs/mulberryleaf") < 48 && !m_util.stop) m_util.wait(100);
	}
	
	Gob getTree(){
		Gob tree = null;
		Gob carpet = null;
		
		while(carpet == null && !m_util.stop){
			m_util.wait(200);
			carpet = m_util.findClosestObject("gfx/terobjs/furniture/carpet");
		}
		
		if(carpet == null) return null;
		
		Coord c = carpet.getr();
		
		while(tree == null && !m_util.stop){
			m_util.wait(200);
			tree = m_util.findClosestObject("gfx/terobjs/trees/mberry", 22, c);
		}
		
		/*Gob tree = m_util.findClosestObject("gfx/terobjs/trees/mberry");
		while(tree == null && !m_util.stop){
			tree = m_util.findClosestObject("gfx/terobjs/trees/mberry");
			m_util.wait(200);
		}
		*/
		return tree;
	}
	
	void setRrelativeCoord(){
		Gob tree = getTree();
		if(tree != null)
			m_relativeCoord = tree.getr().sub(m_util.getPlayerCoord());
	}
	
	Coord getRrelativeCoord(){
		Gob tree = getTree();
		
		if(tree == null) return Coord.z;
		
		return tree.getr().sub(m_relativeCoord);
	}
	
	void transferSilkworms(){
		int cubNum = 0;
		int tableNum = 0;
		
		Inventory bag = m_util.getInventory("Inventory");
		if(checkTableContent(bag) == -1){
			m_util.sendErrorMessage("Inventory clutterd, clear non-silkworm items.");
			m_util.stop = true;
		}else if(m_util.countItemsInBag("gfx/invobjs/silkworm") < 48){
			goToMantionFloor(2);
		}else{
			goToMantionFloor(-1);
			
			cubNum = transferWormsToCubs(cubNum);
			goToMantionFloor(2);
		}
		
		while(tableNum < m_herbalistSizeCount && !m_util.stop){
			tableNum = getSilkWorms(tableNum);
			cubNum = fillCubsWithSilkWorms(cubNum);
		}
	}
	
	int fillCubsWithSilkWorms(int startAtCub){
		goToMantionFloor(-1);
		int num = transferWormsToCubs(startAtCub);
		goToMantionFloor(2);
		
		return num;
	}
	
	int transferWormsToCubs(int startAtCub){
		int emptyInv = m_util.getPlayerBagItems();
		while(emptyInv > 0 && !m_util.stop){
			Inventory inv = goToCub(startAtCub);
			if(m_util.stop) return -1;
			
			/*Inventory inv = null;
			while(inv == null && !m_util.stop) {
				m_util.wait(200);
				inv = m_util.getInventory("Cupboard");
			}
			
			int count = 0;
			while(checkCubContent(inv) != 48 && count < 20 && !m_util.stop) m_util.wait(200);
			*/
			
			//if(checkCubContent(inv) == 48){
			
			int leafs = checkCubContent(inv, "gfx/invobjs/mulberryleaf");
			
			if(leafs != 48){
				m_util.sendErrorMessage("Inventory clutterd, worms will die in this cub.");
				m_util.stop = true;
				return -1;
			}
			
			int worms = checkCubContent(inv, "gfx/invobjs/silkworm");
			
			for(int i = 0; i < (12 - worms); i++){
				m_util.transferItemTo(inv, 3);
			}
			
			emptyInv -= (12 - worms);
			
			if(emptyInv >= 0) startAtCub++;
			if(emptyInv <= 0) break;
		}
		return startAtCub;
	}
	
	int getSilkWorms(int tableNum){
		/*int inventoryItems = m_util.getPlayerBagItems();
		int transferCompleate = inventoryItems / 16;
		
		while(transferCompleate < 3 && tableNum < m_herbalistSizeCount && !m_util.stop){
			goToHerbTable(tableNum);
			
			if(transferWorms())
				transferCompleate++;
				
			tableNum++;
		}*/
		int space = m_util.getPlayerBagSpace();
		
		while(tableNum < m_herbalistSizeCount && !m_util.stop){
			Inventory herbInv = goToHerbTable(tableNum);
			if(m_util.stop) return -1;
			
			int worms = m_util.getItemsFromInv(herbInv).size();
			
			space -= worms;
			
			for(int j = 0; j < worms; j++)
				m_util.transferItemFrom(herbInv, 3);
			
			if(space >= 0) tableNum++;
			if(space <= 0) break;
		}
		
		return tableNum;
	}
	
	/*boolean transferWorms(){
		Inventory inv = null;
		
		while(inv == null && !m_util.stop) {
			m_util.wait(200);
			inv = m_util.getInventory("Herbalist Table");
		}
		
		if(m_util.stop) return false;
		
		if(checkTableContent(inv) > 0){
			for(int i = 0; i < 16; i++){
				m_util.transferItemFrom(inv, 3);
			}
			return true;
		}
		
		return false;
	}*/
		
	int checkTableContent(Inventory inv){
		ArrayList<Item> list = new ArrayList<Item>();
		list = m_util.getItemsFromInv(inv);
		
		for(Item i : list){
			if( !( i.GetResName().contains("gfx/invobjs/silkworm") ) ){
				return -1;
			}
		}
		
		return list.size();
	}
	
	ArrayList<Gob> getHerbTables(){
		ArrayList<Gob> unsortedList = new ArrayList<Gob>();
		ArrayList<Gob> sortedList = new ArrayList<Gob>();
		
		while(unsortedList.size() != m_herbalistSizeCount && !m_util.stop){
			unsortedList = m_util.getObjects("gfx/terobjs/htable");
			m_util.wait(200);
		}
		
		sortedList = m_util.superSortGobList(unsortedList, false, true, true);
		
		return sortedList;
	}
	
	Inventory goToHerbTable(int tableNumber){
		if(m_util.stop) return null;
		
		ArrayList<Gob> tableList = getHerbTables();
		
		return m_util.advWalkToContainer(tableList, tableList.get(tableNumber) );
		
		/*ArrayList<Gob> tableList = new ArrayList<Gob>();
		
		Gob downStair = m_util.findClosestObject("gfx/arch/stairs-inn-d");
		int stairLine = downStair.getr().x;
		
		tableList = getHerbTables();
		Gob table = tableList.get(tableNumber);
		
		Coord frontOfTable;
		
		if(tableNumber < 8){
			frontOfTable = new Coord(table.getr().add(0,9) );
		}else if(tableNumber < 16){
			frontOfTable = new Coord(table.getr().add(0,-7) );
		}else if(tableNumber < 24){
			frontOfTable = new Coord(table.getr().add(0,9) );
		}else{
			frontOfTable = new Coord(table.getr().add(0,-7) );
		}
		
		if( !(frontOfTable.y - 11 < m_util.getPlayerCoord().y && m_util.getPlayerCoord().y < frontOfTable.y + 11) ){
			m_util.goToWorldCoord(new Coord(stairLine, m_util.getPlayerCoord().y));
			m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, frontOfTable.y));
		}
		
		m_util.goToWorldCoord(frontOfTable);
		
		m_util.clickWorldObject(3, table);*/
	}	
	
	int tableEggs(){
		ItemSorter sortedEggs = new ItemSorter();
		ArrayList<Integer> sortedContainerCount = new ArrayList<Integer>();
		int inventorySize = m_util.getPlayerBagSize();
		goToMantionFloor(-1);
		
		scanContainers("silkmothegg",false);
		
		sortedEggs = m_sortList1;
		
		System.out.println("sortedEggs size " + sortedEggs.size() );
		
		boolean first = true;
		int tablesFilled = 0;
		int transferCountAt = 0;
		int eggTransfersRemaning = m_herbalistSizeCount * 16;
		while(tablesFilled < m_herbalistSizeCount && !m_util.stop){
			int bagSpace = m_util.getPlayerBagSpace();
			
			if(bagSpace != 0){
				//int grabCount = m_util.getPlayerBagSpace();
				
				if(eggTransfersRemaning < m_util.getPlayerBagSize()) bagSpace = eggTransfersRemaning;
				
				int countAt = transferCountAt;
				int countTransfer = bagSpace;
				for(int cub = 0; cub < m_cubSizeCellar /*cubList.size()*/ && !m_util.stop; cub++){
					int transfer = sortedEggs.getContainerSortedCount(cub, countTransfer, countAt, true);
					if(transfer > 0){
						Inventory inv = goToCub(cub);
						if(m_util.stop) return -1;
						
						bagSpace = bagSpace - transfer;
						//if(bagSpace < 0) transfer = transfer + bagSpace;
						for(int k = 0; k < transfer; k++)
							m_util.transferItemFrom(inv, 3);
						transferCountAt = transferCountAt + transfer;
						
						if(bagSpace < 0) System.out.println("error in transfer to many transfered");
					}
				}
				
				/*for(int i = 0; i < 44 && !m_util.stop; i++){
					int itemTransfer = sortedContainerCount.get(i);
					if(itemTransfer > 0){
						
						Inventory inv = null;
						goToCub(i);
						
						while(inv == null && !m_util.stop){
							m_util.wait(100);
							inv = m_util.getInventory("Cupboard");
						}
						m_util.wait(1000);
						
						int invSpace = m_util.getPlayerBagSpace();
						
						for(int j = 0; j < itemTransfer && !m_util.stop; j++)
							m_util.transferItemFrom(inv, 3);
						
						int count = 0;
						while( ( invSpace - itemTransfer ) != m_util.getPlayerBagSpace() && !m_util.stop){
							m_util.wait(100);
							count++;
							if(count > 500){
								count = 0;
								for(int j = 0; j < itemTransfer; j++)
									m_util.transferItemFrom(inv, 3);
								
								while( ( invSpace - itemTransfer ) != m_util.getPlayerBagSpace()  && !m_util.stop){
									m_util.wait(500);
									if(( invSpace - itemTransfer ) > m_util.getPlayerBagSpace()){
										for(int k = 0; k < itemTransfer; k++)
											m_util.transferItemTo(inv, 1);
									}
								}
							}
						}
						
					}
				}
				cubSeriesEmptied++;*/
			}
			
			goToMantionFloor(2);
			int eggs = m_util.getPlayerBagItems();
			eggTransfersRemaning -= eggs;
			while(eggs > 0 && !m_util.stop){
				Inventory herbInv = goToHerbTable(tablesFilled);
				if(m_util.stop) return -1;
				
				//goToHerbTable(tablesFilled);
				
				/*while(herbInv == null && !m_util.stop) {
					m_util.wait(200);
					herbInv = m_util.getInventory("Herbalist Table");
				}*/
				
				int space = m_util.getInvSpace(herbInv);
				
				if(first) eggTransfersRemaning -= m_util.getItemsFromInv(herbInv).size();
				
				eggs = eggs - space;
				
				for(int j = 0; j < space; j++)
					m_util.transferItemTo(herbInv, 3);
				
				if(eggs >= 0) tablesFilled++;
			}
			first = false;
			goToMantionFloor(-1);
		}
		
		if(!m_util.stop) setTimers();
		
		return transferCountAt;
	}
	
	void scanContainers(String name, boolean genderSort){
		m_sortList1.clear();
		m_sortList2.clear();
		
		for(int cub = 0; cub < m_cubSizeCellar; cub++){
			ArrayList<Item> itemList = new ArrayList<Item>();
			Inventory inv = goToCub(cub);
			if(m_util.stop) return;
			
			/*while(inv == null && !m_util.stop){
				m_util.wait(100);
				inv = m_util.getInventory("Cupboard");
			}*/
			//m_util.wait(1000);
			
			if(m_util.stop) return;
			
			itemList = m_util.getItemsFromInv(inv);
			if(itemList.size() == 0){
				//m_sortList1.add(cub, -1);
				//m_sortList2.add(cub, -1);
			}else{
				ArrayList<Item> rearrangeList = new ArrayList<Item>();
				
				for(Item i : itemList){
					if(!i.GetResName().contains(name)){
						m_util.stop = true;
						m_util.sendErrorMessage("Non "+name+" found, haulting operation.");
						return;
					}if(!genderSort){
						m_sortList1.add(cub, i.q);
					}else{
						if(i.tooltip.contains("Male"))
							m_sortList1.add(cub, i.q);
						else
							m_sortList2.add(cub, i.q);
						
						if(i.c.y < m_mothInvHight){
							rearrangeList.add(i);
						}
					}
				}
				
				rearrangeMoths(inv, rearrangeList);
			}
		}
	}
	
	void rearrangeMoths(Inventory cubInv, ArrayList<Item> rearrangeList){
		if(cubInv == null) return;
		
		ArrayList<Coord> coords = m_util.emptyItemArrayExcludeZone(cubInv, rearrangeList, new Coord(0,0), new Coord(7,1) );
		
		for(int q = 0; q < rearrangeList.size(); q++){
			Item i = rearrangeList.get(q);
			Coord c = coords.get(q);
			
			if(c == null) continue;
			
			m_util.pickUpItem(i);
			m_util.dropItemInInv(c, cubInv);
		}
	}
	
	void scanBackup(String name){
		m_sortList2.clear();
		
		for(int cub = m_cubSizeCellar; cub < (m_cubSizeCellar + 8); cub++){
			ArrayList<Item> itemList = new ArrayList<Item>();
			Inventory inv = null;
			
			goToBackupCub(cub, false);
			
			while(inv == null && !m_util.stop){
				m_util.wait(100);
				inv = m_util.getInventory("Cupboard");
			}
			m_util.wait(1000);
			
			if(m_util.stop) return;
			
			itemList = m_util.getItemsFromInv(inv);
			/*if(itemList.size() == 0){
				m_sortList2.add(cub, -1);
			}else{*/
				for(Item i : itemList){
					if(!i.GetResName().contains(name)){
						m_util.stop = true;
						m_util.sendErrorMessage("Non "+name+" found, haulting operation.");
						return;
					}else{
						m_sortList2.add(cub, i.q);
					}
				}
			//}
		}
	}
	
	void dumpCubs(){
		goToMantionFloor(-1);
		for(int i = 0; i < m_cubSizeCellar && !m_util.stop; i++){
			Inventory inv = goToCub(i);
			if(m_util.stop) return;
			/*Inventory inv = null;
			
			while(inv == null && !m_util.stop){
				m_util.wait(100);
				inv = m_util.getInventory("Cupboard");
			}
			m_util.wait(1000);*/
			
			ArrayList<Item> itemList = new ArrayList<Item>();
			itemList = m_util.getItemsFromInv(inv);
			
			for(Item item : itemList)
				m_util.dropItemOnGround(item);
		}
	}
	
	int locatePlayer(){
		while(!m_util.stop){
			if( m_util.findClosestObject("gfx/arch/stairs-cellar") != null)
				return -1;
			if(  m_util.findClosestObject("gfx/terobjs/trees/mberry") != null && m_util.findClosestObject("gfx/arch/door-inn") != null)
				return 0;
			if(  m_util.findClosestObject("gfx/arch/door-cellar") != null && m_util.findClosestObject("gfx/arch/stairs-inn") != null && m_util.findClosestObject("gfx/arch/door-inn") != null)
				return 1;
			if(m_util.findClosestObject("gfx/arch/stairs-inn-d") != null)
				return 2;
			
			m_util.wait(200);
			//m_util.camReset();
		}
		
		return 404;
	}
	
	void goToMantionFloor(int floor){
		int startLocation = locatePlayer();
		if(locatePlayer() == floor)
			return;
			
		if(m_util.stop)	return;
		
		if(locatePlayer() != 1){
			Gob cellarStair = m_util.findClosestObject("gfx/arch/stairs-cellar");
			Gob downStair = m_util.findClosestObject("gfx/arch/stairs-inn-d");
			Gob innDoor = m_util.findClosestObject("gfx/arch/door-inn");
			
			if(cellarStair != null){
				m_util.goToWorldCoord(new Coord(cellarStair.getr().x, m_util.getPlayerCoord().y));
				m_util.clickWorldObject(3, cellarStair);
			}else if(downStair != null){
				m_util.goToWorldCoord(new Coord(downStair.getr().x + 11, m_util.getPlayerCoord().y));
				m_util.clickWorldObject(3, downStair);
			}else if(innDoor != null){
				if(m_relativeCoord.equals(new Coord(0,0))){
					Gob gate = m_util.findClosestObject("gfx/arch/door-inn");
					m_util.clickWorldObject(3, gate);
				}else{
					Gob gate = m_util.findClosestObject("gfx/arch/door-inn", getRrelativeCoord() );
					m_util.clickWorldObject(3, gate);
				}
			}
			
			while(locatePlayer() != 1 && !m_util.stop) m_util.wait(200);
		}else{
			Gob cellarDoor = m_util.findClosestObject("gfx/arch/door-cellar");
			m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, cellarDoor.getr().y));
		}
		
		if(m_util.stop)	return;
		
		if(floor == -1){
			Gob cellarDoor = m_util.findClosestObject("gfx/arch/door-cellar");
			m_util.clickWorldObject(3, cellarDoor);
			
			while(locatePlayer() != -1 && !m_util.stop) m_util.wait(200);
			//m_util.wait(500);
			//m_util.camReset();
			m_util.loadArea();
			
			return;
		}
		if(floor == 0){
			if(startLocation == 2){
				Gob cellarDoor = m_util.findClosestObject("gfx/arch/door-cellar");
				m_util.goToWorldCoord(cellarDoor.getr());
			}
			
			Gob innDoor = m_util.findClosestObject("gfx/arch/door-inn");
			m_util.clickWorldObject(3, innDoor);
			
			while(locatePlayer() != 0 && !m_util.stop) m_util.wait(200);
			//m_util.wait(500);
			//m_util.camReset();
			m_util.loadArea();
			
			setRrelativeCoord();
			
			return;
		}
		if(floor == 1){
			//m_util.wait(500);
			//m_util.camReset();
			m_util.loadArea();
			
			return;
		}
		if(floor == 2){
			Gob cellarDoor = m_util.findClosestObject("gfx/arch/door-cellar");
			Gob upStair = m_util.findClosestObject("gfx/arch/stairs-inn");
			
			m_util.goToWorldCoord(cellarDoor.getr());
			m_util.clickWorldObject(3, upStair);
			
			while(locatePlayer() != 2 && !m_util.stop) m_util.wait(200);
			//m_util.wait(500);
			//m_util.camReset();
			m_util.loadArea();
			
			return;
		}
	}
	
	void genderSorting(){
		goToMantionFloor(-1);
		scanContainers("silkmoth", true);
		Inventory bag = m_util.getInventory("Inventory");
		int cub = 0;
		
		ArrayList<Integer> maleMoth = new ArrayList<Integer>();
		ArrayList<Integer> femaleMoth = new ArrayList<Integer>();
		int cyckle = 0;
		
		while( ( m_sortList1.size() > (cyckle * 24) || m_sortList2.size() > (cyckle * 24) ) && !m_util.stop){
			//maleMoth = m_sortList1.sortContainers(cyckle, 24, 0);
			//femaleMoth = m_sortList2.sortContainers(cyckle, 24, 0);
			int totalMaleTransfer = 0;
			int totalFemaleTransfer = 0;
			
			for(int i = 0; i < m_cubSizeCellar && !m_util.stop; i++){
				int maleTransfer = m_sortList1.sortContainersV2(i, cyckle, 24, 0);
				int femaleTransfer = m_sortList2.sortContainersV2(i, cyckle, 24, 0);
				//int maleTransfer = maleMoth.get(i);
				//int femaleTransfer = femaleMoth.get(i);
				
				if(maleTransfer > 0 || femaleTransfer > 0){
					Inventory inv = goToCub(i);
					if(m_util.stop) return;
					/*Inventory inv = null;
					
					while(inv == null && !m_util.stop){
						m_util.wait(100);
						inv = m_util.getInventory("Cupboard");
					}
					m_util.wait(1000);*/
					
					if(maleTransfer > 0) targetedTransfer(maleTransfer, "Male", inv, true);
					if(femaleTransfer > 0) targetedTransfer(femaleTransfer, "Female", inv, true);
					
					totalMaleTransfer = totalMaleTransfer + maleTransfer;
					totalFemaleTransfer = totalFemaleTransfer + femaleTransfer;
				}
			}
			
			boolean next = true;
			if(totalMaleTransfer == 0 || totalFemaleTransfer == 0){
				//m_util.autoCloseWindow("Cupboard");
				Inventory inv = goToCub(cub);
				if(m_util.stop) return;
				/*Inventory inv = null;
					
				while(inv == null && !m_util.stop){
					m_util.wait(100);
					inv = m_util.getInventory("Cupboard");
				}*/
				
				if(m_util.stop) return;
				
				for(int i = 0; i < 48; i++)
					m_util.transferItemTo(inv, 1);
				
				if(next)
					cub++;
				next = !next;
				//m_util.autoCloseWindow("Cupboard");
			}
			
			int innerLoop = 0;
			while( ( ( totalMaleTransfer - innerLoop * 6 ) > 0 && ( totalFemaleTransfer - innerLoop * 6 ) > 0 ) && !m_util.stop){
				innerLoop++;
				
				Inventory inv = goToCub(cub);
				if(m_util.stop) return;
				/*Inventory inv = null;
					
				while(inv == null && !m_util.stop){
					m_util.wait(100);
					inv = m_util.getInventory("Cupboard");
				}
				m_util.wait(500);*/
				
				if(upperCubCheck(inv) && !m_util.stop){
					targetedTransfer(6, "Male", bag, false);
					targetedTransfer(6, "Female", bag, false);
				}
				
				cub++;
			}
			
			m_util.autoCloseWindow("Cupboard");
			m_util.wait(400);
			
			cyckle++;
			//maleMoth.clear();
			//femaleMoth.clear();
		}
		
		dumpInv();
	}
	
	boolean upperCubCheck(Inventory inv){
		ArrayList<Item> list = new ArrayList<Item>();
		list = m_util.getItemsFromInv(inv);
		if(m_util.stop) return false;
		
		for(Item i : list){
			if(i.c.y <= m_mothInvHight)
				return false;
			if(m_util.stop) return false;
		}
		
		return true;
	}
	
	int targetedTransfer(int transfer, String type, Inventory inv, boolean skipUpperCub){
		ArrayList<Item> highList = new ArrayList<Item>();
		ArrayList<Item> tempItems = new ArrayList<Item>();
		ArrayList<Item> testList = new ArrayList<Item>();
		
		tempItems = m_util.getItemsFromInv(inv);
		
		for(int j = 0; j < transfer; j++){
			Item highest = null;
			
			if(m_util.stop) return 100;
			
			for(Item i : tempItems){
				if(i.c.y < m_mothInvHight && skipUpperCub)
					continue;
				if( i.tooltip.contains(type) && !highList.contains(i) ){
					if(highest == null){
						highest = i;
					}else if(highest.q < i.q){
						highest = i;
					}
				}
			}
			if(highest != null)
				highList.add(highest);
		}
		
		//int invItems = m_util.getItemsFromInv(inv);
		
		if(highList.size() == 0)
			return highList.size();
		
		for(Item i : highList)
			m_util.transferItem(i);
		
		int redoCount = 0;
		while(!m_util.stop){
			m_util.wait(100);
			boolean testItems = false;
			testList = m_util.getItemsFromInv(inv);
			
			for(Item i : testList){
				if(highList.contains(i) )testItems = true;
			}
			
			redoCount++;
			if(redoCount > 50){
				redoCount = 0;
				
				for(Item i : highList){
					if(testList.contains(i)) m_util.transferItem(i);
				}
			}
			
			if(!testItems) break;
		}
		
		/*int count = 0;
		while( ( invItems - highList.size() ) == m_util.getItemsFromInv(inv).size() && !m_util.stop){
			if(count > 500 && invItems == m_util.getItemsFromInv(inv).size()){
				count = 0;
				for(Item i : highList)
					m_util.transferItem(i);
			}
			count++;
			m_util.wait(100);
		}*/
		
		return highList.size();
	}
	
	void backupEggs(int start){
		if(m_util.stop) return;
		ItemSorter backupSort = new ItemSorter();
		if(start == 0){
			goToMantionFloor(-1);
			scanContainers("silkmothegg",false);
		}
		goToMantionFloor(1);
		scanBackup("silkmothegg");
		if(m_util.stop) return;
		
		for(int i = start; i < m_sortList1.size(); i++){
			int cub = m_sortList1.get(i).container;
			int q = m_sortList1.get(i).itemQ;
			
			backupSort.add(cub, q);
		}
		
		for(int i = 0; i < m_sortList2.size(); i++){
			int cub = m_sortList2.get(i).container;
			int q = m_sortList2.get(i).itemQ;
			
			backupSort.add(cub, q);
		}
		
		int size = backupSort.size() - 512;
		
		for(int cub = m_cubSizeCellar; cub < (m_cubSizeCellar + 8); cub++){
			int dump = backupSort.getContainerSortedCount(cub, size, 0, false);
			if(dump > 0){
				goToBackupCub(cub, false);
				dumpLow(dump);
			}
		}
		
		fillHigh(backupSort);
	}
	
	void fillHigh(ItemSorter backupSort){
		int invSpace = m_util.getPlayerBagSpace();
		int backupCub = m_cubSizeCellar;
		
		for(int i = 0; i < m_cubSizeCellar; i++){
			int backupPickup = backupSort.getContainerSortedCount(i, 512, 0, true);
			if(backupPickup > 0){
				goToMantionFloor(-1);
				
				goToCub(i);
				
				invSpace = invSpace - backupPickup;
				
				if(invSpace > 0){
					safeTransfer(backupPickup, 3);
				}else if(invSpace == 0){
					safeTransfer(backupPickup, 3);
					
					backupCub = dumpIntoBackupCub(backupCub);
					
					invSpace = m_util.getPlayerBagSpace();
					
				}else if(invSpace < 0){
					safeTransfer(backupPickup + invSpace, 3);
					
					backupCub = dumpIntoBackupCub(backupCub);
					
					goToMantionFloor(-1);
				
					goToCub(i);
					
					safeTransfer(invSpace * -1, 3);
					
					invSpace = m_util.getPlayerBagSpace();
				}
			}
		}
		
		dumpIntoBackupCub(backupCub);
	}
	
	int dumpIntoBackupCub(int backupCub){
		goToMantionFloor(1);
		int inventoryItems = m_util.getPlayerBagItems();
		boolean redo = true;
		
		while(redo && !m_util.stop){
			redo = false;
			
			goToBackupCub(backupCub, false);
			
			Inventory inv = null;
			while(inv == null && !m_util.stop){
				m_util.wait(100);
				inv = m_util.getInventory("Cupboard");
			}
			//m_util.wait(1000);
			
			int cubSpace = 64 - m_util.getItemsFromInv(inv).size();
			
			int trans = cubSpace;
			if(trans > inventoryItems) trans = inventoryItems;
			
			inventoryItems = inventoryItems - cubSpace;
			
			for(int i = 0; i < trans; i++){
				m_util.transferItemTo(inv, 1);
			}
			
			if(inventoryItems > 0){
				redo = true;
				backupCub++;
			}
		}
		
		return backupCub;
	}
	
	void dumpLow(int lowCount){
		Inventory inv = null;
		ArrayList<Item> itemList = new ArrayList<Item>();
		ArrayList<Item> lowList = new ArrayList<Item>();
		
		while(inv == null && !m_util.stop){
			m_util.wait(100);
			inv = m_util.getInventory("Cupboard");
		}
		m_util.wait(1000);
		
		itemList = m_util.getItemsFromInv(inv);
		
		for(int j = 0; j < lowCount; j++){
			Item lowest = null;
			
			if(m_util.stop) return;
			
			for(Item i : itemList){
				if( !lowList.contains(i) ){
					if(lowest == null){
						lowest = i;
					}else if(lowest.q > i.q){
						lowest = i;
					}
				}
			}
			if(lowest != null)
				lowList.add(lowest);
		}
		
		for(Item k : lowList){
			m_util.dropItemOnGround(k);
		}
		
	}
	
	boolean lowQCocoon(boolean caconiCheck){
		int backupCub = (m_cubSizeCellar + 14);
		int extraction = 500;
		int backupCaccons = 250;
		
		////
		if(caconiCheck){
			goToMantionFloor(1);
			for(int bcub = backupCub; bcub > backupCub - 7; bcub--){
				goToBackupCub(bcub, true);
				Inventory inv = m_util.getInventory("Cupboard");
				while(inv == null && !m_util.stop){
					m_util.wait(100);
					inv = m_util.getInventory("Cupboard");
				}
				if(m_util.stop) return false;
				
				for(Item items : m_util.getItemsFromInv(inv)){
					if(items.GetResName().contains("silkcocoon") ){
						backupCaccons--;
					}
				}
			}
			
			goToMantionFloor(-1);
			scanContainers("silkcocoon",false);
			
			int extractionCount = m_sortList1.size() - (m_cubSizeCellar * 12 - extraction);
			
			m_sortList2 = ItemSorter.sort(m_sortList1, extractionCount, false);
		}
		////
		
		if(m_sortList2.size() <= 0) return false;
		
		if(m_util.stop) return false;
		
		int cubNum = 0;
		while(cubNum < m_cubSizeCellar && backupCaccons > 0 && !m_util.stop){
			goToMantionFloor(-1);
			
			int space = m_util.getPlayerBagSpace();
			if(space > backupCaccons) space = backupCaccons;
			while(cubNum < m_cubSizeCellar && !m_util.stop){
				int transfer = m_sortList2.getContainerCount(cubNum);
				
				if(transfer > 0){
					Inventory inv = goToCub(cubNum);
					if(m_util.stop) return false;
					
					space -= transfer;
					if(space < 0){
						transfer += space;
					}
					backupCaccons -= transfer;
					
					m_sortList2.extracted(cubNum, transfer, false);
					for(int j = 0; j < transfer; j++)
						m_util.transferItemFrom(inv, 7);
				}
				
				if(space >= 0) cubNum++;
				if(space <= 0) break;
			}
			
			///////
			
			if(m_util.stop) return false;
			goToMantionFloor(1);
			
			int emptyInv = m_util.getPlayerBagItems();
			while(backupCub > (m_cubSizeCellar + 9) && !m_util.stop){
				goToBackupCub(backupCub, true);
				Inventory inv = m_util.getInventory("Cupboard");
				while(inv == null && !m_util.stop){
					m_util.wait(100);
					inv = m_util.getInventory("Cupboard");
				}
				if(m_util.stop) return false;
				
				int items = m_util.getItemsFromInv(inv).size();;
				
				int trans = 50 - items;
				
				for(int i = 0; i < trans; i++)
					m_util.transferItemTo(inv, 1);
				
				emptyInv -= trans;
				
				if(emptyInv >= 0) backupCub--;
				if(emptyInv <= 0) break;
			}
		}
		
		goToMantionFloor(1);
		return true;
	}
	
	void safeTransfer(int transfer, int transferType){
		if(m_util.stop) return;
		Inventory inv = m_util.getInventory("Cupboard");
		while(inv == null && !m_util.stop){
			m_util.wait(100);
			inv = m_util.getInventory("Cupboard");
		}
		//m_util.wait(1000);
		if(m_util.stop) return;
		
		int inventoryItems = m_util.getPlayerBagItems();
		
		for(int j = 0; j < transfer; j++)
			m_util.transferItemFrom(inv, transferType);
		
		int count = 0;
		while( ( inventoryItems + transfer) != m_util.getPlayerBagItems() && !m_util.stop){
			m_util.wait(100);
			count++;
			if(count > 600)
				for(int j = 0; j < transfer; j++)
					m_util.transferItemFrom(inv, transferType);
		}
	}
	
	/*void dumpCocoons(int backupCub){
		if(m_util.stop) return;
		goToMantionFloor(1);
		
		goToBackupCub(backupCub, true);
		Inventory inv = null;
		while(inv == null && !m_util.stop){
			m_util.wait(100);
			inv = m_util.getInventory("Cupboard");
		}
		if(m_util.stop) return;
		
		for(int j = 0; j < 56; j++)
			m_util.transferItemTo(inv, 1);
	}*/
	
	void burnCocoon(){
		if(m_util.findClosestObject("gfx/terobjs/swheel") == null){
			m_util.sendSlenMessage("Spinner missing.");
			System.out.println("Spinner missing.");
			return;
		}
		
		//manageCauldron();
		//lightCauldron();
		if(!m_util.windowOpen("Silk Filament"))
			m_util.sendAction("craft","silkfilament");
		
		int dump = (m_cubSizeCellar + 8);
		Inventory inv = null;
		for(int cub = (m_cubSizeCellar + 14); cub >= (m_cubSizeCellar + 8); cub--){
			goToBackupCub(cub, true);
			while(inv == null && !m_util.stop){
				m_util.wait(100);
				inv = m_util.getInventory("Cupboard");
			}
			m_util.wait(1000);
			
			if( safeTransferCocoons() ){
				cookCocoons(1);
				cookCocoons(2);
				dump = dumpThreads(dump);
				cub++;
			}
		}
		
		if(m_util.getPlayerBagItems() > 0){
			cookCocoons(1);
			cookCocoons(2);
			dump = dumpThreads(dump);
		}
	}
	
	void cookCocoons(int tool){
		if(tool == 1){
			m_util.sendAction("craft","silkfilament");
			manageCauldron();
			while(!m_util.getCraftName().contains("Silk Filament") && !m_util.stop) m_util.wait(100);
			m_util.sendSlenMessage("Crafting Filaments.");
		}
		if(tool == 2){
			m_util.sendAction("craft","silkthread");
			goToSpinner();
			while(!m_util.getCraftName().contains("Silk Thread") && !m_util.stop) m_util.wait(100);
			m_util.sendSlenMessage("Crafting Threads.");
		}
		while(m_util.checkPlayerWalking() && !m_util.stop) m_util.wait(100);
		m_util.wait(200);
		m_util.craftItem(1);
		m_util.wait(200);
		m_util.craftItem(1);
		m_util.wait(200);
		m_util.craftItem(1);
		int count = 0;
		while(!m_util.stop){
			while(!m_util.hasHourglass() && !m_util.stop){
				m_util.wait(200);
				String str = new String(m_util.slenError());
				if(str.contains("You need a")){
					if(tool == 1) manageCauldron();
					if(tool == 2) goToSpinner();
					m_util.sendSlenMessage("Going to tool again.");
				}else if(str.contains("You do not have all the ingredients.")){
					m_util.sendSlenMessage("Finished Crafting.");
					return;
				}
				count++;
				if(count > 25){
					count = 0;
					m_util.craftItem(1);
				}
			}
			while(m_util.hasHourglass() && !m_util.stop) m_util.wait(200);
		}
	}
	
	int dumpThreads(int dump){
		int check = m_util.getPlayerBagItems();
		while(check > 0 && !m_util.stop){
			goToBackupCub(dump, true);
			Inventory inv = null;
			while(inv == null && !m_util.stop){
				m_util.wait(100);
				inv = m_util.getInventory("Cupboard");
			}
			if(m_util.stop) return dump;
			
			m_util.wait(1000);
			
			if(m_util.stop) return dump;
			
			check = check - m_util.getInvSpace(inv);
			
			for(int j = 0; j < 56; j++)
				m_util.transferItemTo(inv, 1);
			
			if(check > 0){
				dump++;
			}else{
				m_util.dropItemInBag(new Coord(10,10));
				m_util.transferItemTo(inv, 1);
			}
		}
		return dump;
	}
	
	boolean safeTransferCocoons(){
		int cocoonPickupCount = 25;
		if(m_util.stop) return false;
		Inventory inv = null;
		while(inv == null && !m_util.stop){
			m_util.wait(100);
			inv = m_util.getInventory("Cupboard");
		}
		m_util.wait(1000);
		if(m_util.stop) return false;
		
		int inventoryItems = m_util.getPlayerBagItems();
		ArrayList<Item> itemList = new ArrayList<Item>(m_util.getItemsFromInv(inv) );
		
		ArrayList<Item> sortedList = itemLowQSort(itemList);
		
		int transfer = 0;
		
		for(Item i : sortedList){
			if(i.GetResName().contains("silkcocoon") && (inventoryItems + transfer ) < cocoonPickupCount ){
				m_util.transferItem(i);
				transfer++;
			}
		}
		
		int count = 0;
		while( ( inventoryItems + transfer) != m_util.getPlayerBagItems() && !m_util.stop){
			m_util.wait(100);
			/*count++;
			if(count > 600)
				for(int j = 0; j < transfer; j++)
					m_util.transferItemFrom(inv, 7);*/
		}
		if( ( inventoryItems + transfer) == cocoonPickupCount) return true;
		
		return false;
	}
	
	ArrayList<Item> itemLowQSort(ArrayList<Item> itemList){
		ArrayList<Item> sortedList = new ArrayList<Item>();
		ArrayList<Item> temp = new ArrayList<Item>();
		temp = itemList;
		
		while(temp.size() > 0 && !m_util.stop){
			Item low = null;
			for(Item i : temp){
				if(low == null){
					low = i;
				}else if(i.q < low.q){
					low = i;
				}
			}
			
			sortedList.add(low);
			temp.remove(low);
		}
		
		return sortedList;
	}
	
	void lightCauldron(){
		m_util.buttonActivate("Cauldron");
		m_util.wait(50);
		m_util.buttonActivate("Cauldron");
		m_util.wait(50);
		m_util.buttonActivate("Cauldron");
		m_util.wait(50);
	}
	
	void manageCauldron(){
		goToCauldron();
		while(!m_util.windowOpen("Cauldron") && !m_util.stop) m_util.wait(200);
		if(m_util.getVmeterAmount(71, false) < 25){
			Gob barrel = m_util.findClosestObject("gfx/terobjs/barrel");
			Coord barrelCoord = new Coord(barrel.getr() );
			
			m_util.sendAction("carry");
			m_util.clickWorldObject(1, barrel);
			
			while(!m_util.checkPlayerCarry() && !m_util.stop) m_util.wait(200);
			
			goToCauldron();
			
			while(!m_util.checkPlayerWalking() && !m_util.stop) m_util.wait(100);
			while(m_util.checkPlayerWalking() && !m_util.stop) m_util.wait(200);
			
			m_util.clickWorld(3, barrelCoord);
			while(m_util.checkPlayerCarry() && !m_util.stop) m_util.wait(200);
			
			goToCauldron();
			while(!m_util.windowOpen("Cauldron") && !m_util.stop) m_util.wait(200);
		}
		
		if(m_util.getVmeterAmount(255, false) < 15){
			Gob cellarDoor = m_util.findClosestObject("gfx/arch/door-cellar");
			m_util.goToWorldCoord(new Coord(cellarDoor.getr().x+11*4, m_util.getPlayerCoord().y));
			goToMantionFloor(0);
			
			while( m_util.getObjects("gfx/arch/sign" , 30).size() == 0 && !m_util.stop) m_util.wait(200);
			
			Gob blockSign = m_util.findClosestObject("gfx/arch/sign");
			
			m_util.clickWorldObject(3, blockSign);
			
			while(!m_util.windowOpen("Palisade Cornerpost") && !m_util.stop) m_util.wait(200);
			m_util.signTransferTake("Palisade Cornerpost");
			
			goToMantionFloor(1);
			
			goToCauldron();
			Gob cauldron = m_util.findClosestObject("gfx/terobjs/cauldron");
			m_util.itemActionWorldObject(cauldron, 1);
			
			while(m_util.mouseHoldingAnItem() && !m_util.stop) m_util.wait(200);
			goToCauldron();
			while(!m_util.windowOpen("Cauldron") && !m_util.stop) m_util.wait(200);
		}
		while(!m_util.windowOpen("Cauldron") && !m_util.stop) m_util.wait(200);
		
		lightCauldron();
	}
	
	void goToSpinner(){
		Gob cellarDoor = null;
		Gob spinner = null;
		
		while( ( cellarDoor == null || spinner == null ) && !m_util.stop){
			cellarDoor = m_util.findClosestObject("gfx/arch/door-cellar");
			spinner = m_util.findClosestObject("gfx/terobjs/swheel");
			m_util.wait(200);
		}
		
		if(m_util.stop) return;
		
		if(m_util.getPlayerCoord().y != spinner.getr().y){
			m_util.goToWorldCoord(new Coord(cellarDoor.getr().x+11*4, m_util.getPlayerCoord().y));
			m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, spinner.getr().y));
		}
		
		m_util.clickWorldObject(3, spinner);
		
		if(!m_util.getPlayerCoord().equals(spinner.getr().add(5,0) ) ){
			while(!m_util.checkPlayerWalking() && !m_util.stop) m_util.wait(20);
			while(m_util.checkPlayerWalking() && !m_util.stop) m_util.wait(20);
		}
		
		//while(!m_util.windowOpen("Cauldron") && !m_util.stop) m_util.wait(200);
	}
	
	void goToCauldron(){
		Gob cellarDoor = null;
		Gob cauldron = null;
		
		while( ( cellarDoor == null || cauldron == null ) && !m_util.stop){
			cellarDoor = m_util.findClosestObject("gfx/arch/door-cellar");
			cauldron = m_util.findClosestObject("gfx/terobjs/cauldron");
			m_util.wait(200);
		}
		
		if(m_util.stop) return;
		
		if( !( cauldron.getr().x-5 <= m_util.getPlayerCoord().x && cauldron.getr().x+11*3+4 >= m_util.getPlayerCoord().x 
				&& cauldron.getr().y-5 <= m_util.getPlayerCoord().y && cauldron.getr().y+11*3 >= m_util.getPlayerCoord().y 
		) )
		{
			if( !( cellarDoor.getr().x+11*4-4 <= m_util.getPlayerCoord().x && cellarDoor.getr().x+11*4+4 >= m_util.getPlayerCoord().x ) )
				m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, cellarDoor.getr().y));
			m_util.goToWorldCoord(new Coord(cellarDoor.getr().x+11*4, m_util.getPlayerCoord().y));
			m_util.goToWorldCoord(new Coord(m_util.getPlayerCoord().x, cauldron.getr().y));
		}
		
		m_util.clickWorldObject(3, cauldron);
		//while(!m_util.windowOpen("Cauldron") && !m_util.stop) m_util.wait(200);
	}
	
	void multiLeafPicker(){
		if(m_tree == null) return;
		
		Gob LC = getDropLC(m_tree);
		
		if(LC == null){
			m_util.sendSlenMessage("No LC found.");
			return;
		}
		
		int tag = getTag(LC, m_tree);
		
		if(tag == -1) return;
		
		while(!m_util.stop){
			multiPick(tag, m_tree);
			multiDrop(tag);
		}
	}
	
	void multiPick(int tag, Gob tree){
		Coord goTo = getPositionViaTag(tag, true, tree, true);
		while(!m_util.stop){
			m_util.walkTo(goTo );
			
			if(m_util.getPlayerCoord().equals(goTo) ) break;
		}
		
		Coord goTo2 = getPositionViaTag(tag, true, tree, false);
		m_util.walkTo(goTo2 );
		
		m_util.dropHoldingItem();
		m_util.clickWorldObject(3, tree);
		while(!m_util.flowerMenuReady() && !m_util.stop){m_util.wait(200);}
		m_util.flowerMenuSelect("Pick leaf");
		int count = 0;
		while(m_util.getPlayerBagSpace() > 0 && !m_util.stop){
			if(!m_util.hasHourglass() ){
				count++;
			}else{
				count = 0;
			}
			
			if(count > 50){
				m_util.clickWorldObject(3, tree);
				while(!m_util.flowerMenuReady() && !m_util.stop){m_util.wait(200);}
				m_util.flowerMenuSelect("Pick leaf");
				count = 0;
			}
			m_util.wait(100);
		}
		m_util.goToWorldCoord(getPositionViaTag(tag, true, tree, true) );
	}
	
	void multiDrop(int tag){
		Gob LC = getDropLC(m_tree);
		
		Coord goTo = getPositionViaTag(tag, false, LC, true);
		while(!m_util.stop){
			m_util.walkTo(goTo );
			
			if(m_util.getPlayerCoord().equals(goTo) ) break;
		}
		
		Coord goTo2 = getPositionViaTag(tag, false, LC, false);
		m_util.walkTo(goTo2 );
		
		m_util.clickWorldObject(3, LC);
		Inventory inv = null;
		
		int count = 0;
		while(inv == null && !m_util.stop){
			m_util.wait(100);
			inv = m_util.getInventory("Chest");
			count++;
			if(count > 40){
				m_util.clickWorldObject(3, LC);
				count = 0;
			}
		}
		
		while(!m_util.stop){
			int space = 0;
			space = m_util.getInvSpace(inv);
			
			if(space > 0){
				for(int i = 0; i < 48; i++)
					m_util.transferItemTo(inv, 1);
			}
			
			if(m_util.getPlayerBagItems() == 0) break;
			
			m_util.wait(200);
		}
		
		m_util.goToWorldCoord(getPositionViaTag(tag, false, LC, true) );
	}
	
	Gob getDropLC(Gob tree){
		Gob LC = null;
		Coord origo = tree.getr();
		Coord c1 = origo.add(-500, -99);
		Coord c2 = origo.add(500, 0);
		ArrayList<Gob> list = m_util.getObjects("gfx/terobjs/lchest", c1, c2);
		
		for(Gob g : list){
			if(LC == null){
				if(m_util.getTileID(g.getr().div(11)) == 4) LC = g;
			}else if(LC.getr().dist(m_util.getPlayerCoord()) > g.getr().dist(m_util.getPlayerCoord())){
				if(m_util.getTileID(g.getr().div(11)) == 4) LC = g;
			}
		}
		
		return LC;
	}
	
	Coord getPositionViaTag(int tag, boolean treePos, Gob object, boolean offcet){
		if(m_util.stop) return null;
		Coord objectC = object.getr();
		
		Coord c = new Coord();
		if(treePos){
			//Coord treeC = object.getr();
			switch(tag){
				case 0:
					c = new Coord(-6,0);
					//return treeC.add(c);
					break;
				case 1:
					c = new Coord(-6,-6);
					//return treeC.add(c);
					break;
				case 2:
					c = new Coord(0,-6);
					//return treeC.add(c);
					break;
				case 3:
					c = new Coord(6,-6);
					//return treeC.add(c);
					break;
				case 4:
					c = new Coord(6,0);
					//return treeC.add(c);,
					break;
				case 5:
					c = new Coord(6,6);
					//return treeC.add(c);
					break;
				case 6:
					c = new Coord(0,6);
					//return treeC.add(c);
					break;
				case 7:
					c = new Coord(-6,6);
					//return treeC.add(c);
					break;
			}
		}else{
			//Coord LCcoord = object.getr();
			switch(tag){
				case 0:
					c = new Coord(0,8);
					//return LCcoord.add(c);
					break;
				case 1:
					c = new Coord(5,8);
					//return LCcoord.add(c);
					break;
				case 2:
					c = new Coord(-5,8);
					//return LCcoord.add(c);
					break;
				case 3:
					c = new Coord(11,5);
					//return LCcoord.add(c);
					break;
				case 4:
					c = new Coord(-11,5);
					//return LCcoord.add(c);
					break;
				case 5:
					c = new Coord(11,0);
					//return LCcoord.add(c);
					break;
				case 6:
					c = new Coord(-11,0);
					//return LCcoord.add(c);
					break;
				case 7:
					c = new Coord(-11,-5);
					//return LCcoord.add(c);
					break;
			}
		}
		
		if(offcet) c = c.mul(2);
		
		return objectC.add(c);
	}
	
	int getTag(Gob LC, Gob tree){
		int count = -1;
		/*ArrayList<Gob> allBorkas = new ArrayList<Gob>();
		int count = 0;
		ArrayList<Gob> borkaTree = m_util.getObjects("gfx/borka/s", 11, m_tree.getr() );
		ArrayList<Gob> borkaLC = m_util.getObjects("gfx/borka/s", LC.getr().add(-15,0), LC.getr().add(15,10) );
		
		allBorkas.addAll(borkaTree);
		allBorkas.addAll(borkaLC);
		
		for(Gob borka : allBorkas){
			if(borka.id != m_util.getPlayerGob().id )
				count++;
		}*/
		
		ArrayList<Gob> allBorkas = m_util.getObjects("gfx/borka/s");
		
		for(int i = 0; i < 8; i++){
			Coord treeC = getPositionViaTag(i, true, tree, false);
			Coord lcC = getPositionViaTag(i, false, LC, false);
			
			Rectangle r1 = new Rectangle(treeC.x - 1, treeC.y - 1, 2 , 2);
			Rectangle r2 = new Rectangle(lcC.x - 1, lcC.y - 1, 2 , 2);
			
			boolean blocked = false;
			for(Gob borka : allBorkas){
				if(borka.id != m_util.getPlayerGob().id ){
					Coord borkaC = borka.getr();
					
					if(r1.contains(borkaC.x, borkaC.y) || r2.contains(borkaC.x, borkaC.y)){
						blocked = true;
						break;
					}
				}
			}
			
			if(!blocked){
				count = i;
				break;
			}
		}
		
		return count;
	}
	
	void cookEmAll(){
		goToMantionFloor(1);
		int num = 1;
		while(!m_util.stop){
			System.out.println("Mantion numer " +num+ " started.");
			m_util.sendSlenMessage("Collect and burn cocoons.");
			boolean first = true;
			while(lowQCocoon(first) && !m_util.stop){
				burnCocoon();
				first = false;
			}
			
			goToMantionFloor(0);
			
			Coord c = getRrelativeCoord();
			Gob gate = m_util.findClosestObject("gfx/arch/door-inn", 30, c.add(100,0) );
			if(gate != null && !m_util.stop){
				while(!m_util.stop){
					m_util.walkTo(gate.getr().add(0,7));
					
					if(m_util.getPlayerCoord().equals(gate.getr().add(0,7) ) ) break;
				}
				
				m_util.clickWorldObject(3, gate);
				
				while(locatePlayer() != 1 && !m_util.stop) m_util.wait(200);
			}else{
				break;
			}
			
			num++;
		}
	}
	
	void cubEmAll(){
		goToMantionFloor(1);
		int num = 1;
		while(!m_util.stop){
			System.out.println("Mantion numer " +num+ " started.");
			m_util.sendSlenMessage("Transfering silkworms from herb tables.");
			transferSilkworms();
			
			goToMantionFloor(0);
			
			Coord c = getRrelativeCoord();
			Gob gate = m_util.findClosestObject("gfx/arch/door-inn", 30, c.add(100,0) );
			if(gate != null && !m_util.stop){
				while(!m_util.stop){
					m_util.walkTo(gate.getr().add(0,7));
					
					if(m_util.getPlayerCoord().equals(gate.getr().add(0,7) ) ) break;
				}
				
				m_util.clickWorldObject(3, gate);
				
				while(locatePlayer() != 1 && !m_util.stop) m_util.wait(200);
			}else{
				break;
			}
			
			num++;
		}
	}
	
	void tableAllEggs(int type){
		goToMantionFloor(1);
		int num = 1;
		while(!m_util.stop){
			if(type == 1 || type == 2){
				System.out.println("Mantion numer " +num+ " started.");
				m_util.sendSlenMessage("Filling tables with sorted eggs.");
				int backup = tableEggs();
				if(type == 3) backup = 0;
				m_util.sendSlenMessage("Sorting backup eggs.");
				//backupEggs(backup);
				m_util.sendSlenMessage("WARNING. Clearing Cubs.");
				dumpCubs();
			}
			if(type == 1 || type == 3){
				m_util.sendSlenMessage("Filling cupboards with leafes.");
				fillCubsWithLeafs();
			}
			m_sortList1.clear();
			m_sortList2.clear();
			
			goToMantionFloor(0);
			
			Coord c = getRrelativeCoord();
			Gob gate = m_util.findClosestObject("gfx/arch/door-inn", 30, c.add(100,0) );
			if(gate != null && !m_util.stop){
				while(!m_util.stop){
					m_util.walkTo(gate.getr().add(0,7));
					
					if(m_util.getPlayerCoord().equals(gate.getr().add(0,7) ) ) break;
				}
				
				m_util.clickWorldObject(3, gate);
				
				while(locatePlayer() != 1 && !m_util.stop) m_util.wait(200);
			}else{
				break;
			}
			
			num++;
		}
	}
	
	void sortAllMoths(){
		goToMantionFloor(1);
		int num = 1;
		while(!m_util.stop){
			m_util.sendSlenMessage("Sorting silkmoths.");
			genderSorting();
			
			m_sortList1.clear();
			m_sortList2.clear();
			
			goToMantionFloor(0);
			
			Coord c = getRrelativeCoord();
			Gob gate = m_util.findClosestObject("gfx/arch/door-inn", 30, c.add(100,0) );
			if(gate != null && !m_util.stop){
				while(!m_util.stop){
					m_util.walkTo(gate.getr().add(0,7));
					
					if(m_util.getPlayerCoord().equals(gate.getr().add(0,7) ) ) break;
				}
				
				m_util.clickWorldObject(3, gate);
				
				while(locatePlayer() != 1 && !m_util.stop) m_util.wait(200);
			}else{
				break;
			}
			
			if(!m_util.stop){
				for(Item it : m_util.getItemsFromBag() ){
					m_util.dropItemOnGround(it);
				}
			}
			num++;
		}
	}
	
	void sortBarrels(){
		if(bucketTest() ){
			goToMantionFloor(0);
			Coord mem = m_util.getPlayerCoord();
			Gob well = getObject("gfx/terobjs/well");
			if(!m_util.stop) m_util.objectSurf(well);
			//if(!m_util.stop) new MultiScript(m_util, well, false, false).fillInventoryBuckets(well);
			if(!m_util.stop) m_util.safeWalkTo(mem);
		}
		goToMantionFloor(1);
		Coord mem2 = m_util.getPlayerCoord();
		Gob barrel = getObject("gfx/terobjs/barrel");
		if(!m_util.stop) m_util.objectSurf(barrel);
		//if(!m_util.stop) new SmeltScript(m_util, barrel, true, false).liquidTransfer(true);
		if(!m_util.stop) m_util.safeWalkTo(mem2);
	}
	
	boolean bucketTest(){
		for(Item i : m_util.getItemsFromBag("bucket") ){
			if(m_util.getFluid(i.tooltip) < 100) return true;
		}
		return false;
	}
	
	Gob getObject(String name){
		Gob object = null;
		while(object == null && !m_util.stop){
			object = m_util.findClosestObject(name);
			m_util.wait(200);
		}
		
		return object;
	}
	
	void barrelMansions(){
		goToMantionFloor(1);
		int num = 1;
		while(!m_util.stop){
			m_util.sendSlenMessage("Fill Barrels.");
			sortBarrels();
			
			goToMantionFloor(0);
			
			Coord c = getRrelativeCoord();
			Gob gate = m_util.findClosestObject("gfx/arch/door-inn", 30, c.add(100,0) );
			if(gate != null && !m_util.stop){
				while(!m_util.stop){
					m_util.walkTo(gate.getr().add(0,7));
					
					if(m_util.getPlayerCoord().equals(gate.getr().add(0,7) ) ) break;
				}
				
				m_util.clickWorldObject(3, gate);
				
				while(locatePlayer() != 1 && !m_util.stop) m_util.wait(200);
			}else{
				break;
			}
			num++;
		}
	}
	
	void feedMe(){
		if(m_util.getHunger() > 900) return;
		goToMantionFloor(0);
		Gob chest = m_util.smallChest(null);
		Inventory chestInv = m_util.walkToContainer(chest, "Chest");
		m_util.advEater(chestInv);
		goToMantionFloor(1);
	}
	
	void setTimers(){
		m_util.createEnderTimer("•••Silk Eggs•••", 0, 0, 8);
		m_util.createEnderTimer("/¤\\ Silk Moth /¤\\", 0, 0, 40);
		
		m_util.startEnderTimer("•••Silk Eggs•••");
		m_util.startEnderTimer("/¤\\ Silk Moth /¤\\");
	}
	
	public void run(){
		m_util.openInventory();
		m_util.setPlayerSpeed(2);
		feedMe();
		
		if(m_directive == 1){
			if(m_util.getPlayerBagSpace() >= 48){
				m_util.sendSlenMessage("Full cyckle, table eggs/sort backup eggs/ fill leafes.");
				m_util.sendSlenMessage("Filling tables with sorted eggs.");
				tableEggs();
				
				m_util.sendSlenMessage("WARNING. Clearing Cubs.");
				dumpCubs();
				
				m_util.sendSlenMessage("Filling cupboards with leafes.");
				fillCubsWithLeafs();
			}else{
				m_util.sendSlenMessage("Need 2x trav inventory gear (48).");
			}
		}else if(m_directive == 2){
			m_util.sendSlenMessage("Transfering silkworms from herb tables.");
			transferSilkworms();
		}else if(m_directive == 3){
			if(m_util.getPlayerBagSpace() == 56){
				m_util.sendSlenMessage("Collect and burn cocoons.");
				boolean first = true;
				while(lowQCocoon(first) && !m_util.stop){
					burnCocoon();
					first = false;
				}
			}else{
				m_util.sendSlenMessage("Need full inventory gear (56).");
			}
		}else if(m_directive == 4){
			if(m_util.getPlayerBagSpace() >= 48){
				m_util.sendSlenMessage("Sorting silkmoths.");
				genderSorting();
			}else{
				m_util.sendSlenMessage("Need 2x trav inventory gear (48).");
			}
		}else if(m_directive == 5){
			m_util.sendSlenMessage("Filling cupboards with leafes.");
			goToMantionFloor(0);
			fillCubsWithLeafs();
		}else if(m_directive == 6){
			if(m_util.getPlayerBagSpace() >= 48){
				m_util.sendSlenMessage("Full cyckle, table eggs/sort backup eggs/ fill leafes.");
				m_util.sendSlenMessage("Filling tables with sorted eggs.");
				int backup = tableEggs();
				//
				//m_util.sendSlenMessage("Sorting backup eggs.");
				backupEggs(backup);
				
				m_util.sendSlenMessage("WARNING. Clearing Cubs.");
				dumpCubs();
				
				m_util.sendSlenMessage("Filling cupboards with leafes.");
				fillCubsWithLeafs();
			}else{
				m_util.sendSlenMessage("Need 2x trav inventory gear (48).");
			}
		}else if(m_directive == 7){
			m_util.sendSlenMessage("WARNING. Clearing Cubs.");
			dumpCubs();
		}
		
		/*else if(m_directive == 6){
			m_util.sendSlenMessage("Filling tables with sorted eggs.");
			tableEggs();
		}else if(m_directive == 7){
			m_util.sendSlenMessage("Sorting backup eggs.");
			backupEggs(0);
		}else if(m_directive == 8){
			if(m_util.getPlayerBagSize() == 56){
				m_util.sendSlenMessage("Collecting lowest quality cocoons.");
				lowQCocoon(true);
			}else{
				m_util.sendSlenMessage("Need full inventory gear (56).");
			}
		}else if(m_directive == 9){
			if(m_util.getPlayerBagSize() == 56){
				m_util.sendSlenMessage("Thread cocoons.");
				burnCocoon();
			}else{
				m_util.sendSlenMessage("Need full inventory gear (56).");
			}
		}else if(m_directive == 10){
			m_util.sendSlenMessage("Pick Leafs from mulberry tree.");
			multiLeafPicker();
		}else if(m_directive == 404){
			m_util.sendSlenMessage("WARNING. Clearing Cubs.");
			dumpCubs();
		}else if(m_directive == 11){
			if(m_util.getPlayerBagSpace() >= 48){
				tableAllEggs(1);
			}else{
				m_util.sendSlenMessage("Need 2x trav inventory gear (48).");
			}
		}else if(m_directive == 12){
			if(m_util.getPlayerBagSpace() >= 48){
				cubEmAll();
			}else{
				m_util.sendSlenMessage("Need 2x trav inventory gear (48).");
			}
		}else if(m_directive == 13){
			if(m_util.getPlayerBagSpace() == 56){
				cookEmAll();
			}else{
				m_util.sendSlenMessage("Need full inventory gear (56).");
			}
		}else if(m_directive == 14){
			if(m_util.getPlayerBagSpace() >= 48){
				sortAllMoths();
			}else{
				m_util.sendSlenMessage("Need 2x trav inventory gear (48).");
			}
		}else if(m_directive == 15){
			if(m_util.getPlayerBagSpace() >= 48){
				m_util.sendSlenMessage("Full cyckle, table eggs/sort backup eggs/ fill leafes.");
				m_util.sendSlenMessage("Filling tables with sorted eggs.");
				int backup = tableEggs();
				//
				//m_util.sendSlenMessage("Sorting backup eggs.");
				backupEggs(backup);
				
				m_util.sendSlenMessage("WARNING. Clearing Cubs.");
				dumpCubs();
				
				m_util.sendSlenMessage("Filling cupboards with leafes.");
				fillCubsWithLeafs();
			}else{
				m_util.sendSlenMessage("Need 2x trav inventory gear (48).");
			}
		}else if(m_directive == 21){
			if(m_util.getPlayerBagSpace() >= 48){
				tableAllEggs(2);
			}else{
				m_util.sendSlenMessage("Need 2x trav inventory gear (48).");
			}
		}else if(m_directive == 22){
			if(m_util.getPlayerBagSpace() >= 48){
				tableAllEggs(3);
			}else{
				m_util.sendSlenMessage("Need 2x trav inventory gear (48).");
			}
		}else if(m_directive == 30){
			if(m_util.getPlayerBagSize() >= 48){
				barrelMansions();
			}else{
				m_util.sendSlenMessage("Need 2x trav inventory gear (48).");
			}
		}*/
		
		m_util.running(false);
	}
}