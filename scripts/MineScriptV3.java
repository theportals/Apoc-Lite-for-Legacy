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

import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashSet;
import java.awt.Rectangle;
import java.awt.Color;

import haven.MapView.rallyPoints;

import static haven.MCache.tilesz;
import haven.MCache;

public class MineScriptV3 extends Thread{
	public String scriptName = "Mining Script";
	public String[] options = {
		"Start miner", "Start miner (ignore ore tiles)", "Set Bonfire", "Clear Bonfire", "Clear memory",
	};
	
	static int m_freeTiles = 8;
	static int m_tooFree = 17;
	
	HavenUtil m_util;
	int m_dir;
	boolean m_enableRefill = true;
	Coord m_safeSpot;
	Coord m_lastMinedTile;
	boolean m_trollDisabled = false;
	Coord m_origo;
	boolean troll = false;
	
	HashSet<Integer> m_hash = new HashSet<Integer>();
	ArrayList<Item> m_ignore = new ArrayList<Item>();
	
	ArrayList<Coord> m_open = new ArrayList<Coord>();
	ArrayList<Coord> m_closed = new ArrayList<Coord>();
	ArrayList<Coord> m_ore = new ArrayList<Coord>();
	ArrayList<Coord> loadList = new ArrayList<Coord>();
	
	int m_tiles = 0;
	int m_dubCount = 0;
	int m_oreCount = 0;
	int m_stoneTile = 0;
	
	boolean m_ignoreOre = false;
	
	boolean m_updateDrawing = true;
	ScriptDrawer miningDrawer;
	
	int m_option;
	String m_modify;
	
	public void ApocScript(HavenUtil util, int option, String modify){
		m_util = util;
		m_option = option;
		m_modify = modify;
	}
	
	/*public MineScriptV3(HavenUtil util, int dir, Coord Csafe, boolean troll, boolean refill, Coord origo){
		m_util = util;
		m_dir = dir;
		m_safeSpot = tilify(Csafe);
		m_trollDisabled = troll;
		m_enableRefill = refill;
		m_origo = origo;
	}*/
	
	void starters(){
		//m_util.miningSafeSpot = null;
		miningDrawer = m_util.addScriptDrawer();
		m_util.openInventory();
		m_util.setPlayerSpeed(1);
		
		if(m_util.getRallyPoints().rally.size() == 0 || !setSafeSpot()) m_util.stop = true;
		
		if(m_origo == null) m_util.sendSlenMessage("WARNING! no bonfire set, save/load disabled.");
	}
	
	boolean setSafeSpot(){
		if(m_trollDisabled) return true;
		m_safeSpot = m_util.getRallyPoints().rally.get(0).c;
		
		if(m_safeSpot.dist(m_util.getPlayerCoord() ) > 6000 ){
			m_util.sendErrorMessage("Safe spot invalid.");
			return false;
		}
		
		return true;
	}
	
	void autoNodeMiner(){
		int saveCounter = 0;
		while((m_open.size() > 0 || m_closed.size() > 0) && !m_util.stop){
			if(!refreshMiner()) return;
			
			Coord tile = getBestTile();
			
			if(tile == null && m_closed.size() > 0){
				if(m_open.size() == 0){
					m_updateDrawing = false;
					int count = 0;
					moveOutOfWay();
					while(count < 50 && !m_util.stop){
						count++;
						m_util.wait(100);
						trollAttack();
						if(m_util.stop) return;
					}
					//System.out.println("Waiting...");
				}
				fixOpen(null);
				continue;
			}
			
			int info = mineTile(tile);
			
			infoOperator(info, tile);
			
			updateDrawBoxes();
			
			saveCounter++;
			if( (info == 0 || info == 3 || info == 4) && saveCounter % 100 == 0){
				saveData();
				if(saveCounter > 1000001 ) saveCounter = 0;
			}
			}
	}
	
	void updateDrawBoxes(){
		if(!m_updateDrawing) return;
		
		miningDrawer.clear();
		for(Coord tc : m_open){
			Coord ul = tc.mul(11).add(3,3);
			miningDrawer.addBox(ul, ul.add(5,5), 2, Color.GREEN);
		}
		
		for(Coord tc : m_closed){
			Coord ul = tc.mul(11).add(3,3);
			miningDrawer.addBox(ul, ul.add(5,5), 2, Color.RED);
		}
	}
	
	void fixOpen(Coord tile){
		if(m_closed.size() == 0 || (tile != null && !tileNextToClosed(tile)) )
			return;
		
		m_open.addAll(m_closed);
		m_closed.clear();
	}
	
	void infoOperator(int info, Coord tile){
		if(m_util.stop) return;
		
		boolean oreBool = true;
		switch(info){
			case 1:
			case 2:
				m_closed.add(tile);
				m_open.remove(tile);
				break;
			case 4:
				m_dubCount++;
				oreBool = dubTest(tile);
			case 3:
				if(oreBool && !m_ignoreOre){
					addTiles(tile);
					m_ore.add(tile);
				}
			case 0:
				m_tiles++;
				m_open.remove(tile);
				fixOpen(tile);
				m_updateDrawing = true;
				break;
		}
	}
	
	///
	
	// 1 blocked
	// 2 support failed
	// 3 success with ore
	// 4 dub tile
	// 0 success
	
	void moveOutOfWay(){
		Gob support = m_util.findClosestObject("gfx/terobjs/mining/minesupport");
		if(support == null) return;
		
		Coord c = support.getr();
		
		if(c.dist(m_util.getPlayerCoord() ) > 50 ) m_util.clickWorld(1, c);
	}
	
	boolean tileNextToClosed(Coord tile){
		int i = 0;
		int j = 1;
		for(int a = 0; a < 4; a++){
			Coord v = tile.add(i,j);
			for(Coord c : m_closed)
				if(c.equals(v) )
					return true;
			
			int temp = i;
			i = j * -1;
			j = temp;
		}
		
		return false;
	}
	
	boolean dubTest(Coord tile){
		int i = 0;
		int j = 1;
		for(Coord c : m_ore){
			for(int a = 0; a < 4; a++){
				Coord tcs = tile.add(i,j);
				
				if(m_util.getTileID(tcs) != 255 && tcs.equals(c) )
					return true;
				
				int temp = i;
				i = j * -1;
				j = temp;
			}
		}
		
		return false;
	}
	
	int mineTile(Coord tile){
		Coord tileCoord = tilify(tile.mul(11));
		Coord sideSpot = findSide(tile);
		if(sideSpot == null){
			return 1;
		}
		
		int walkNum = goToWorldCoord(sideSpot, tileCoord);
		
		if(walkNum == 1){
			sideSpot = findSide(tile);
			walkNum = goToWorldCoord(sideSpot, tileCoord);
			
			if(walkNum == 1)
				return 1;
		}
		if(walkNum == 2)
			return 2;
		
		int mineNum = mineThisTile(tileCoord);
		
		if(mineNum == 2)
			return 2;
		if(mineNum == 3)
			return 3;
		if(mineNum == 4)
			return 4;
		
		return 0;
	}
	
	Coord findSide(Coord tile){
		int i = 0;
		int j = 1;
		Coord sideTC = null;
		double dist = 0;
		
		ArrayList<Rectangle> rectList = m_util.getMiningHitboxes(m_lastMinedTile);
		
		if(!checkSupportSafety(tile.mul(11)) ) return sideTC;
		
		for(int a = 0; a < 4; a++){
			Coord tcs = tile.add(i,j);
			if(m_util.getTileID(tcs) != 255){
				Coord spot = findMiningSpot(tile, tcs);
				if(m_util.checkReachable(spot, false, rectList)){
					if(sideTC == null){
						sideTC = spot;
						dist = spot.dist(m_util.getPlayerCoord() );
					}else if(spot.dist(m_util.getPlayerCoord()) < dist ){
						sideTC = spot;
						dist = spot.dist(m_util.getPlayerCoord() );
					}
				}
			}
			
			int temp = i;
			i = j * -1;
			j = temp;
		}
		
		return sideTC;
	}
	
	Coord findMiningSpot(Coord mineThisTile, Coord fromThisTile){
		Coord to = mineThisTile.mul(11).add(5,5);
		
		Coord subCoords = fromThisTile.sub(mineThisTile).mul(100000);
		Coord multiplier = subCoords.div(subCoords.sub(1,1));
		Coord dir = new Coord(6,6);
		
		return to.add(dir.mul(multiplier) );
	}
	
	void addTiles(Coord tile){
		/*ArrayList<Coord> list = new ArrayList<Coord>();
		//list = m_util.getVoidTiles(tile.add(-1,-1).mul(11), tile.add(1,1).mul(11));
		
		for(Coord c : list){
			if(!c.equals(tile) ){
				boolean found = false;
				for(Coord check : m_open){
					if(check.equals(c) ){
						found = true;
						break;
					}
				}
				
				if(!found)
					m_open.add(c);
			}
		}*/
		
		int i = 0;
		int j = 1;
		
		for(int a = 0; a < 4; a++){
			Coord tcs = tile.add(i,j);
			if(m_util.getTileID(tcs) == 255){
				boolean found = false;
				for(Coord check : m_open){
					if(check.equals(tcs) ){
						found = true;
						break;
					}
				}
				
				if(!found)
					m_open.add(tcs);
			}
			
			int temp = i;
			i = j * -1;
			j = temp;
		}
	}
	
	/*Coord getBestTile(){
		Coord best = null;
		double dist = 0;
		double supportDist = 0;
		double fullDist = 0;
		
		for(Coord c : m_open){
			Coord rc = tilify(c.mul(11));
			
			Gob support = m_util.findClosestObject("gfx/terobjs/mining/minesupport", rc);
			
			if(support != null){
				supportDist = rc.dist(support.getr() );
			}else{
				supportDist = 0;
			}
			
			fullDist = rc.dist(m_util.getPlayerCoord() ) + supportDist;
			
			if(best == null){
				best = c;
				dist = fullDist;
			}else if(fullDist < dist){
				best = c;
				dist = fullDist;
			}
		}
		
		return best;
	}*/
	
	Coord getBestTile(){
		Coord best = null;
		double dist = 0;
		int[][] tileList = new int[300][300];
		ArrayList<Coord> filter = new ArrayList<Coord>();
		
		Coord gc = m_util.getPlayerCoord().div(1100).add(-1,-1);
		
		for(int My = 0; My < 3; My++){
			for(int Mx = 0; Mx < 3; Mx++){
				synchronized(m_util.m_ui.mainview.map.grids){
					MCache.Grid gd = m_util.m_ui.mainview.map.grids.get(gc.add(Mx,My) );
					
					for(int i = 0; i < 100; i++){
						for(int j = 0; j < 100; j++){
							if(gd != null){
								tileList[j+(Mx*100)][i+(My*100)] = gd.tiles[j][i];
							}else{
								tileList[j+(Mx*100)][i+(My*100)] = -1;
							}
						}
					}
				}
			}
		}
		
		int free = 0;
		int size = 2;
		for(Coord c : m_open){
			Coord rc = tilify(c.mul(11));
			Coord gcSub = c.div(100).sub(gc);
			Coord gcMod = c.mod(new Coord(100,100));
			
			Coord range = new Coord(gcSub.mul(100).add(gcMod) );
			/*System.out.println(gcSub);
			System.out.println(gcMod);
			System.out.println(range);*/
			
			int freeCount = 0;
			
			for(int x = range.sub(size,size).x; x <= range.add(size,size).x; x++){
				for(int y = range.sub(size,size).y; y <= range.add(size,size).y; y++){
					if(x >= 0 && x < 300 && y >= 0 && y < 300 && tileList[x][y] != 255) freeCount++;
				}
			}
			
			//if(freeCount >= m_tooFree) System.out.println(freeCount);
			//System.out.println(freeCount);
			
			if(best == null){
				best = c;
				free = freeCount;
				dist = rc.dist(m_util.getPlayerCoord() );
				//System.out.println(free);
			}else if( (freeCount > free && (free < m_freeTiles || freeCount >= m_tooFree )) || (free < m_tooFree && freeCount >= m_freeTiles && dist > rc.dist(m_util.getPlayerCoord() ) ) ){
				best = c;
				free = freeCount;
				dist = rc.dist(m_util.getPlayerCoord() );
				//System.out.println(free);
			}
		}
		
		return best;
	}
	
	boolean stoneTest(){
		ArrayList<Gob> list = m_util.getObjects("gfx/terobjs/items/stone", 2);
		for(Gob g : list){
			if(!ignoreContains(g) ){
				//stoneBellSound();
				return true;
			}
		}
		
		return false;
	}
	
	boolean ignoreContains(Gob g){
		if(!m_hash.contains(g.id) ){
			m_hash.add(g.id);
			return true;
		}
		
		return false;
	}
	
	boolean dropGarbage(){
		boolean found = false;
		Inventory inv = m_util.getInventory("Inventory");
		for(Widget item = inv.child; item != null; item = item.next){
			if(item instanceof Item){
				Item it = (Item)item;
				if(!(m_ignore.contains(it))){
					if(it.GetResName().contains("ore-iron")){
						m_util.dropItemOnGround((Item)item);
						//oreBellSound();
						found = true;
						m_oreCount++;
					}
					if(it.GetResName().contains("petrifiedseashell")){
						m_util.dropItemOnGround((Item)item);
					}
					if(it.GetResName().contains("catgold")){
						m_util.dropItemOnGround((Item)item);
					}
					if(it.GetResName().contains("strangecrystal")){
						m_util.dropItemOnGround((Item)item);
					}
				}
				m_ignore.add((Item)item);
			}
		}
		
		return found;
	}
	
	void oreBellSound(){
		//Sound.ore.play();
	}
	
	void stoneBellSound(){
		//Sound.tap.play();
	}
	
	int mineThisTile(Coord miningSpot){
		boolean stoneCheck = true;
		boolean redoTile = true;
		String str = new String();
		String error = new String("You are too tired to mine.");
		int type = 4;
		
		trollAttack();
		
		while(redoTile && !m_util.stop){
			redoTile = false;
			
			if(!checkSupportSafety(miningSpot)){
				return 2;
			}
			
			while( m_util.getTileID(miningSpot.div(11) ) == 255 && m_util.hasHourglass() && !m_util.stop) m_util.wait(200);
			
			if(!clickMine(miningSpot) ){
				return 2;
			}
			
			m_util.miniglass = true;
			boolean drinkBreak = false;
			int count = 0;
			boolean voidTest = false;
			
			while(m_util.getTileID(miningSpot.div(11) ) == 255 && !drinkBreak && ( m_util.hasHourglass() || m_util.miniglass ) && !m_util.stop){
				if(m_util.miniglass){
					count++;
					if(count > 75){
						drinkBreak = true;
						redoTile = true;
						voidTest = true;
					}
				}
				
				trollAttack();
				
				m_util.wait(100);
				
				if(!m_util.stop && dropGarbage()){
					stoneCheck = false;
					type = 3;
				}
				if(stoneCheck){
					if(stoneTest()){
						type = 0;
						stoneCheck = false;
						m_stoneTile++;
					}
				}
				
				str = m_util.slenError();
				if(str.contains(error) ){
					m_util.sendSlenMessage("Filling stamina.");
					m_util.quickWater();
					waitForHourglass();
					redoTile = true;
					drinkBreak = true;
				}
			}
			
			if(voidTest && testDugOut(miningSpot)) redoTile = false;
			
			trollAttack();
		}
		
		m_ignore.clear();
		m_hash.clear();
		
		m_lastMinedTile = miningSpot;
		return type;
	}
	
	void waitForHourglass(){
		while(!m_util.hasHourglass() && !m_util.stop) m_util.wait(50);
		while(m_util.hasHourglass() && !m_util.stop) m_util.wait(50);
	}
	
	boolean testDugOut(Coord miningSpot){
		int count = 0;
		
		m_util.clickWorld(1, miningSpot);
		while(!m_util.stop && !m_util.checkPlayerWalking() && !m_util.getPlayerCoord().equals(miningSpot)){
			m_util.wait(20);
			count++;
			if(count > 100){
				count = 0;
				m_util.clickWorld(1, miningSpot);
			}
		}
		
		m_util.wait(500);
		
		while(m_util.checkPlayerWalking() && !m_util.stop) m_util.wait(100);
		
		m_util.wait(500);
		
		if(m_util.getPlayerCoord().equals(miningSpot)) return true;
		
		return false;
	}
	
	boolean clickMine(Coord c){
		boolean tileInSupportedArea = supportTest(c);
		
		if(tileInSupportedArea && !m_util.checkPlayerWalking() && !m_util.stop){
			m_util.clickWorld(1, c);
			m_util.sendAction("mine");
			m_util.clickWorld(1, c);
			m_util.clickWorld(3, c);
			return true;
		}
		return false;
	}
	
	int goToWorldCoord(Coord c, Coord safeCheck){
		if(c == null) return 1;
		Gob player = m_util.getPlayerGob();
		Coord mc = m_util.getPlayerCoord();
		
		if(!mc.equals(c)){
		
			trollAttack();
			if(m_util.stop) return 0;
			
			if(m_util.checkMiningCollision(m_lastMinedTile, mc, c) ){
				m_util.walkToCondition(c);
				
				while(m_util.PFrunning && !m_util.stop){
					m_util.wait(100);
					
					if(!m_util.checkReachable(c, false) ){
						m_util.pathing = false;
						return 1;
					}else if(!checkSupportSafety(safeCheck)){
						m_util.pathing = false;
						return 2;
					}
				}
				
				if(m_util.stop) m_util.pathing = false;
				
				return 0;
			}
			
			if(!m_util.stop) m_util.clickWorld(1, c);
			
			int count = 0;
			while((!m_util.getPlayerCoord().equals(c) || m_util.checkPlayerWalking()) && !m_util.stop){
				m_util.wait(100);
				trollAttack();
				if(m_util.stop) return 1;
				
				if(!checkSupportSafety(safeCheck)){
					return 2;
				}
				
				if(!m_util.checkPlayerWalking() && !m_util.getPlayerCoord().equals(mc) ){
					count++;
					if(count > 5){
						m_util.walkToCondition(c);
						
						while(m_util.PFrunning && !m_util.stop){
							m_util.wait(100);
							
							if(!m_util.checkReachable(c, false) ){
								m_util.pathing = false;
								return 1;
							}else if(!checkSupportSafety(safeCheck)){
								m_util.pathing = false;
								return 2;
							}
						}
						
						if(m_util.stop) m_util.pathing = false;
					}
				}else{
					count = 0;
				}
			}
		}
		
		return 0;
	}
	
	boolean checkSupportSafety(Coord c){
		Coord checkC = tilify(c);
		Coord player = m_util.getPlayerCoord();
		Coord ul = m_util.viewUpperLeft(player).add(101,101);
		Rectangle r = new Rectangle(ul.x, ul.y, 698, 698);
		
		if(r.contains(checkC.x, checkC.y) ){
			if(!supportTest(c)) return false;
		}
		
		return true;
	}
	
	boolean supportTest(Coord c){
		int minesupportRadius = 100;
		ArrayList<Gob> supports = m_util.getObjects("gfx/terobjs/mining/minesupport");
		
		for(Gob g : supports){
			double dist = tilify(c).dist(g.getr() );
			if(dist < minesupportRadius ) return true;
		}
		
		return false;
	}
	
	private Coord tilify(Coord c){
		c = c.div(tilesz);
		c = c.mul(tilesz);
		c = c.add(tilesz.div(2));
		return(c);
    }
	
	///
	
	boolean refreshMiner(){
		Inventory inv = m_util.getInventory("Inventory");
		Item food = getFoodItem(inv);
		boolean refilled = false;
		
		dropHoldingGarbage();
		
		if(waterContent() < 110){
			refillMiner();
			refilled = true;
		}
		
		if(m_util.getHunger() < 899){
			if(food != null){
				m_util.itemAction(food);
				m_util.autoFlowerMenu("Eat");
			}else if(!refilled && m_util.getHunger() < 600){
				refillMiner();
			}
		}
		
		if(m_util.getHunger() < 510 || waterContent() < 100){
			System.out.println("Out of food or water.");
			return false;
		}
		
		return true;
	}
	
	Item getFoodItem(Inventory inv){
		ArrayList<Item> foodList = m_util.getItemsFromInv(inv);
		for(Item i : foodList){
			if(m_util.foodTest(i ,3) > 0 ){
				return i;
			}
		}
		
		return null;
	}
	
	void dropHoldingGarbage(){
		if(!m_util.mouseHoldingAnItem() ) return;
		int count = 0;
		if(m_util.getMouseItem().GetResName().contains("bucket") ){
			Inventory bag = m_util.getInventory("Inventory");
			Coord dropItemC = m_util.emptyItemSlot(bag);
			if(dropItemC != null){
				m_util.dropItemInBag(dropItemC);
				while(m_util.mouseHoldingAnItem() && count < 15 && !m_util.stop){ m_util.wait(200); count++; }
			}
			return;
		}
		
		Inventory bag = m_util.getInventory("Inventory");
		Coord dropItemC = m_util.emptyItemSlot(bag);
		if(dropItemC != null){
			m_util.dropItemInBag(dropItemC);
			while(m_util.mouseHoldingAnItem() && count < 15 && !m_util.stop){ m_util.wait(200); count++; }
		}
	}
	
	int waterContent(){
		ArrayList<Item> bagCheck = m_util.getItemsFromBag();
		int water = 0;
		
		for(Item i : bagCheck){
			if(i.GetResName().contains("bucket-water") ){
				water = water + m_util.getFluid(i.tooltip);
			}
		}
		
		return water;
	}
	
	void refillMiner(){
		if(!m_enableRefill) return;
		
		Coord memCoord = m_util.getPlayerCoord();
		
		Gob chest = m_util.findClosestObject("lchest");
		
		if(chest == null) return;
		
		m_util.setPlayerSpeed(2);
		
		Inventory chestInv = m_util.walkToContainer(chest, "Chest");
		
		LCmanage(chestInv);
		
		m_util.walkTo(memCoord);
		
		m_util.setPlayerSpeed(1);
	}
	
	void LCmanage(Inventory chestInv){
		Inventory inv = m_util.getInventory("Inventory"); 
		Item food = getFoodItem(chestInv);
		Item buckete = m_util.getItemFromInventory(chestInv, "bucket-water");
		ArrayList<Item> chestList = m_util.getItemsFromInv(chestInv);
		ArrayList<Item> bagList = m_util.getItemsFromInv(inv);
		
		int foodCount = 0;
		
		if(buckete != null){
			for(Item i : chestList){
				if(i.GetResName().contains("bucket-water") ){
					Coord c = i.c;
					m_util.pickUpItem(i);
					
					for(Item j : bagList){
						if(j.GetResName().contains("bucket") || j.GetResName().contains("flask") ){
							m_util.itemInteract(j);
						}
					}
					m_util.dropItemInInv(c, chestInv);
				}
			}
		}
		
		if(m_util.stop) return;
		
		for(Item i : bagList){
			if(m_util.foodTest(i ,3) > 0){
				foodCount++;
			}
		}
		
		if(food != null && !m_util.stop){
			for(Item i : chestList){
				if(m_util.stop) return;
				if(m_util.foodTest(i ,3) > 0){
					if(foodCount < 8){
						m_util.transferItem(i);
						foodCount++;
					}else if(m_util.getHunger() + m_util.foodTest(i ,3) < 1000){
						m_util.itemAction(i);
						if(m_util.stop) return;
						m_util.autoFlowerMenu("Eat");
						if(m_util.stop) return;
						while(m_util.flowerMenuReady() && !m_util.stop) m_util.wait(200);
					}
				}
			}
		}
	}
	
	///
	
	void loadData(){
		if(m_origo == null){
			startTiles();
			return;
		}
		
		loadList.clear();
		
		try{
			File file = new File("./scriptConf/nodeMiner.save");
			
			if(!file.exists()){
				startTiles();
				return;
			}
			
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String strLine;
			String str;
			
			while((strLine = br.readLine()) != null){
				Coord c = null;
				try{
					str = strLine.replaceAll("[ ()]", "");
					String[] parts = str.split(",");
					c = new Coord(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
				}catch(Exception e){}
				
				addCoord(c);
			}
			
			br.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
		if(m_open.size() == 0){
			startTiles();
		}else{
			updateDrawBoxes();
		}
	}
	
	void addCoord(Coord c){
		if(c == null) return;
		
		if(addedLoader(c) ) return;
		
		Coord add = c.add(m_origo.div(11) );
		m_open.add(add);
		addOreTile(add);
	}
	
	boolean addedLoader(Coord c){
		for(Coord t : loadList){
			if(c.equals(t) ){
				return true;
			}
		}
		
		loadList.add(c);
		
		return false;
	}
	
	void addOreTile(Coord tile){
		int i = 0;
		int j = 1;
		for(int a = 0; a < 4; a++){
			Coord tcs = tile.add(i,j);
			if(m_util.getTileID(tcs) != 255)
				m_ore.add(tcs);
			
			int temp = i;
			i = j * -1;
			j = temp;
		}
	}
	
	void startTiles(){
		m_open = m_util.getVoidTiles(m_util.m_pos1, m_util.m_pos2);
		
		updateDrawBoxes();
	}
	
	void saveData(){
		if(m_origo == null) return;
		m_open.addAll(m_closed);
		
		try {
			File file = new File("./scriptConf/nodeMiner.save");
			
			if(m_open.size() == 0){
				file.delete();
				return;
			}else if(!file.exists()){
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			boolean first = true;
			for(Coord c : m_open){
				if(!first) bw.newLine();
				first = false;
				
				bw.write(c.sub(m_origo.div(11) ).toString());
			}
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void clearMemory(){
		try{
			File file = new File("./scriptConf/nodeMiner.save");
			file.delete();
		}catch(Exception e){}
	}
	
	///
	
	void trollAttack(){
		if(!m_trollDisabled){
			if(m_util.getObjects("troll", 100).size() > 0 || m_util.getObjects("flaxfibre", 100).size() > 0){
				Sound.troll.play();
				troll = true;
				m_util.pathDrinker = true;
				m_util.startRunFlask();
				//m_util.minerSafety = false;
				m_util.setPlayerSpeed(3);
				
				pathSafeWalk(m_safeSpot);
				
				m_util.stop = true;
			}
		}
	}
	
	void pathSafeWalk(Coord c){
		boolean breakSquare = false;
		ArrayList<Coord> list = null;
		m_hash.clear();
		
		/*while(!m_util.stop && !m_util.getPlayerCoord().equals(c) ){
			m_util.walkTo(c);
			m_util.wait(100);
		}*/
		
		while((!m_util.getPlayerCoord().equals(c) || m_util.checkPlayerWalking()) && !breakSquare && !m_util.stop){
			m_util.wait(100);
			
			m_util.walkToCondition(c);
			
			while(m_util.PFrunning && !m_util.stop){
				m_util.wait(100);
				
				if(list != null){
					m_util.pathing = false;
					breakSquare = true;
				}else{
					list = scanTrollSquare();
				}
			}
			
			if(m_util.stop) m_util.pathing = false;
		}
		
		if(breakSquare){
			//System.out.println("found");
			trollRunning(list);
		}
	}
	
	ArrayList<Coord> scanTrollSquare(){
		ArrayList<Coord> list = new ArrayList<Coord>();
		ArrayList<Rectangle> rects = new ArrayList<Rectangle>();
		ArrayList<Gob> supports = m_util.getObjects("gfx/terobjs/mining/minesupport");
		boolean first = true;
		boolean scan = false;
		
		for(Gob support : supports){
			if(newSupport(support) ){
				scan = true;
			}
		}
		
		if(!scan) return null;
		
		rects = m_util.getAllCorrectedHitboxes(false, false);
		//System.out.println(supports.size() );
		
		for(Gob support : supports){
			Coord c = support.getr().add(-11,11);
			if(viewTest(c) /*&& m_util.checkReachable(c, false, rects)*/ ){
				int i = 0;
				int j = 1;
				int k = 1;
				int l = 0;
				Coord a = c;
				for(int q = 0; q < 4; q++){
					Coord b = c.add(i*11*20, j*-11*20);
					
					int temp = i;
					i = j;
					j = k;
					k = l;
					l = temp;
					
					if(!viewTest(b) /*|| !m_util.checkReachable(b, false, rects)*/ || !m_util.freePath(a, b, false, rects) ){
						//System.out.println("box error" + list.size() );
						list.clear();
						break;
					}
					
					list.add(a);
					a = new Coord(b);
				}
				
				//System.out.println("box" + list.size() );
				
				if(list.size() == 4){
					//System.out.println("box found" );
					return list;
				}
			}
		}
		
		return null;
	}
	
	boolean newSupport(Gob g){
		if(!m_hash.contains(g.id) ){
			m_hash.add(g.id);
			return true;
		}
		
		return false;
	}
	
	boolean viewTest(Coord c){
		Coord ul = m_util.viewUpperLeft(m_util.getPlayerCoord() );
		Rectangle r = new Rectangle(ul.x+20, ul.y+20, 860, 860);
		return r.contains(c.x, c.y);
	}
	
	void trollRunning(ArrayList<Coord> list){
		m_closed.clear();
		for(Coord c : list)
			m_closed.add(c.div(11) );
		
		updateDrawBoxes();
		
		Coord location = null;
		double dist = 0;
		int counter = 0;
		int count = 0;
		for(Coord c : list){
			if(location == null){
				location = c;
				dist = m_util.getPlayerCoord().dist(c);
			}else if(m_util.getPlayerCoord().dist(c) > dist){
				location = c;
				dist = m_util.getPlayerCoord().dist(c);
				counter = count;
			}
			count++;
		}
		
		while(!m_util.stop){
			boolean breakLocation = false;
			while((!m_util.getPlayerCoord().equals(location) || m_util.checkPlayerWalking()) && !breakLocation && !m_util.stop){
				m_util.wait(100);
				
				m_util.walkToCondition(location);
				
				while(m_util.PFrunning && !m_util.stop){
					m_util.wait(100);
					
					if(!m_util.checkReachable(location, false) ){
						m_util.pathing = false;
						breakLocation = true;
					}
				}
				
				if(m_util.stop) m_util.pathing = false;
			}
			
			while(!(m_util.getObjects("troll", 100).size() > 0) && !m_util.stop){
				m_util.wait(50);
			}
			
			counter++;
			if(counter >= list.size() ){
				counter = 0;
				Sound.troll.play();
			}
			
			location = list.get(counter);
		}
	}
	
	///
	
	void miner(){
		starters();
		
		loadData();
		
		if(!m_util.stop) autoNodeMiner();
		
		m_util.removeScriptDrawer();
		
		if(troll) m_util.setPlayerSpeed(2);
		
		saveData();
	}
	
	void setCenterObject(boolean setOrigo){
		if(setOrigo){
			Gob bonfire = m_util.findClosestObject("bonfire");
			if(bonfire == null) return;
			m_origo = bonfire.getr();
		}else{
			m_origo = null;
		}
	}
	
	public void run(){
		switch(m_option){
			case 1:
				miner();
				break;
			case 2:
				m_ignoreOre = true;
				miner();
				break;
			case 3:
				setCenterObject(true);
				break;
			case 4:
				setCenterObject(false);
				break;
			case 5:
				clearMemory();
				break;
		}
		
		System.out.println("Tiles : " + m_tiles + "   Stone Tiles: "+ m_stoneTile + "    Dub Count: " + m_dubCount + "    Total Ore Count: " + m_oreCount);
		
		m_util.running(false);
	}
}