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

import java.util.ArrayList;
import java.awt.Rectangle;
import java.util.HashSet;
import java.io.*;

import haven.*;
import addons.*;
import haven.MapView.rallyPoints;

public class CurioScript extends Thread{
	public String scriptName = "Curio Script";
	public String[] options = {
		"Area 1", "Area 2", "Area 3", "Area 4", "Area 5",
	};
	HavenUtil m_util;
	int m_option;
	double m_breakDist = 30;
	
	int m_quickFix = 0;
	int m_westMem = 0;
	int m_eastMem = 0;
	
	ArrayList<pathClass> m_mainPathList = new ArrayList<pathClass>();
	pathClass[] m_pathLoader = new pathClass[10];
	
	scannerClass m_scan = new scannerClass();
	pathClass m_mainPath = new pathClass();
	
	boolean m_backup = false;
	boolean m_claim = false;
	
	public void ApocScript(HavenUtil util, int option, String modify){
		m_util = util;
		m_option = option;
	}
	
	void startFunctions(){
		fileConfig();
		
		m_util.turnCriminalOn(true);
		m_util.turnTrackingOn(true);
		
		m_util.openInventory();
		m_util.setPlayerSpeed(2);
		
		m_scan.start();
		areaEncoder();
		
		m_util.startRunFlask();
	}
	
	void checkStartLocation(){
		if(m_util.carpetIdol() == null ){
			Coord preJump = hearth();
			
			loadArea(preJump);
		}
	}
	
	void areaEncoder(){
		int codec = m_option;
		while(codec > 0 && !m_util.stop){
			int check = codec % 10;
			
			pathLoader(check);
			
			codec/=10;
		}
	}
	
	void scanner(){
		ArrayList<Gob> allGobs = m_util.allGobs();
		synchronized(m_mainPath){
			m_mainPath.redFound = false;
			
			for(Gob g : allGobs){
				if(g.resname().contains("gfx/terobjs/herbs/") ){
					if(checkID(g) ){
						m_mainPath.herbList.add(g);
					}
				}else if(g.resname().equals("gfx/borka/s")){
					KinInfo kin = g.getattr(KinInfo.class);
					
					if(g.isHuman() && g.id != m_util.getPlayerGob().id && (kin == null || kin.group == 2) ){
						if(checkID(g) ){
							System.out.println("--PLAYER-- found. Location: "+ m_mainPath.pathName +" Checkpoint: "+ m_mainPath.checkPointNum);
						}
						m_mainPath.redFound = true;
						m_mainPath.herbList.clear();
						m_mainPath.filterdHerbList.clear();
					}
				}else if(g.resname().equals("gfx/kritter/boar/s")){
					if(checkID(g) && !m_mainPath.boatTravel){
						System.out.println("Boar found. Location: "+ m_mainPath.pathName +" Checkpoint: "+ m_mainPath.checkPointNum);
					}
				}else if(g.resname().equals("gfx/kritter/bear/s")){
					if(checkID(g) ){
						System.out.println("==Bear== found. Location: "+ m_mainPath.pathName +" Checkpoint: "+ m_mainPath.checkPointNum);
					}
				}
			}
			
			if(m_mainPath.checkPointReached() ){
				m_quickFix = 0;
				m_mainPath.nextCheckPoint();
			}
		}
	}
	
	boolean checkID(Gob g){
		if(!m_mainPath.garbageList.contains(g.id) ){
			m_mainPath.garbageList.add(g.id);
			return true;
		}
		
		return false;
	}
	
	void startManager(){
		if(m_mainPathList.size() == 0) return;
		
		int pathNum = 0;
		while(!m_util.stop){
			m_mainPath = m_mainPathList.get(pathNum);
			m_mainPath.scannerAcctive = false;
			
			mainMoveLooper();
			
			pathNum++;
			if(m_mainPathList.size() == pathNum) pathNum = 0;
		}
	}
	
	void mainMoveLooper(){
		boolean unloadRedoCheck = true;
		boolean homeUnloadCheck = true;
		
		while(!m_util.stop && unloadRedoCheck && homeUnloadCheck){
			startMover();
			
			while(!m_util.stop && m_mainPath.moverAcctive){
				m_util.wait(50);
				mover();
			}
			
			endMover();
			
			unloadRedoCheck = m_mainPath.redoOnUnload;
			homeUnloadCheck = m_mainPath.homeUnload;
		}
	}
	
	void startMover(){
		m_mainPath.moverAcctive = true;
		if(!m_mainPath.fieldStart || m_mainPath.homeUnload){
			dumpCurios();
			typeInventory();
			refreshments();
		}else if(m_mainPath.fieldStart){
			fieldRefreshments();
		}
		m_mainPath.homeUnload = false;
		
		Coord preJump = m_util.getPlayerCoord();
		if(m_util.stop) return;
		
		dropPickupBoat(true);
		
		useCR();
		loadArea(preJump);
		
		m_mainPath.scannerAcctive = true;
		
		boatTraveler();
		m_mainPath.updateCheckpoint();
	}
	
	void mover(){
		synchronized(m_mainPath){
			redCheck();
			
			herbFiltering();
			
			work();
			
			unloadIntoLC();
			
			moveBreaker();
		}
	}
	
	void endMover(){
		Coord preJump = null;
		
		if(m_util.stop) return;
		
		if(!m_mainPath.crossRoadChain || m_mainPath.homeUnload){
			if(m_mainPath.boatTravel){
				preJump = boatHearth();
			}else{
				preJump = hearth();
			}
			
			loadArea(preJump);
			
			dropPickupBoat(false);
			
			dumpCurios();
			
			LCemptying();
		}
		
		if(m_mainPath.lastCheckPointReached || !m_mainPath.redoOnUnload ){
			m_mainPath.clear();
		}else{
			m_mainPath.miniClear();
		}
	}
	
	void dropPickupBoat(boolean pickup){
		if(!m_mainPath.boatPickup) return;
		
		if(pickup){
			Gob boat = m_util.findClosestObject("boat");
			m_util.objectSurf(boat);
			if(!m_util.stop) m_util.sendAction("carry");
			if(!m_util.stop) m_util.clickWorldObject(1, boat);
			while(!m_util.checkPlayerCarry() && !m_util.stop){m_util.wait(200);}
		}else{
			Gob hearth = m_util.myHearth(null);
			if(hearth == null) return;
			m_util.clickWorld(3, hearth.getr().add(0,-22) );
			int count = 0;
			int dropper = 0;
			while(dropper < 4 && !m_util.stop) {
				count++;
				if(count > 5){
					count = 0;
					m_util.clickWorld(3, hearth.getr().add(0,-22) );
				}
				
				if(!m_util.checkPlayerCarry() ){
					dropper++;
				}else{
					dropper = 0;
				}
				
				m_util.wait(500);
			}
		}
	}
	
	void LCemptying(){
		if(!m_mainPath.boatFiller && m_mainPath.boatFills > 0) return;
		
		while(m_mainPath.boatFills > 0 && !m_util.stop){
			m_mainPath.boatFills--;
			
			Coord preJump = m_util.getPlayerCoord();
			
			if(!m_mainPath.boatPickup){
				if(m_util.stop) return;
				
				useCR();
				
				loadArea(preJump);
				
				m_mainPath.scannerAcctive = true;
				
				if(m_mainPath.redFound){
					m_util.pathing = false;
					m_util.sendErrorMessage("Red found on checkpoint: " + m_mainPath.checkPointNum );
					hearth();
					m_util.stop = true;
				}
			}
			
			Gob boat = m_util.findClosestObject("boat");
			
			for(int i = 0; i < 2; i++)
				m_util.matsFromToBoat(false, boat);
			
			if(!m_mainPath.boatPickup){
				preJump = hearth();
				
				loadArea(preJump);
			}
			
			dumpCurios();
		}
	}
	
	void typeInventory(){
		Gob coffer = m_util.findClosestObject("gfx/terobjs/furniture/coffer");
		if(m_mainPath.inventoryType == 1){
			m_util.runFlask = false;
			m_util.reGear(3, coffer);
		}else if(m_mainPath.inventoryType == 2){
			m_util.runFlask = true;
			m_util.reGear(7, coffer);
		}
		Inventory coff = m_util.getInventory("Coffer");
		if(coff != null){
			ArrayList<Item> list = m_util.getItemsFromInv(coff);
			for(Item i : list){
				if(i.GetResName().contains("leech") ){
					m_util.dropItemOnGround(i);
				}
			}
		}
		m_util.closeEquipment();
		m_util.walkTo(m_util.getPlayerCoord().add(0,10) );
		ArrayList<Item> containers = m_util.getItemsFromBag();
		boolean bucketFill = false;
		for(Item i : containers){
			if(i.GetResName().contains("bucket") && m_util.getFluid(i.tooltip) < 100) bucketFill = true;
		}
		if(bucketFill) fillWater();
	}
	
	Coord getCRoffcet(){
		while(!m_util.stop){
			ArrayList<Gob> list = m_util.getObjects("gfx/terobjs/crossroads");
			if(list != null){
				for(Gob crossRoad : list){
					Coord c = crossRoad.getr();
					Gob carpet = m_util.findClosestObject("gfx/terobjs/furniture/carpet", 5, c.add(0, -11) );
					if(carpet != null) return c;
				}
			}
			m_util.wait(200);
		}
		return new Coord();
	}
	
	void fillWater(){
		Coord CRCoord = getCRoffcet();
		
		Coord p1 = CRCoord.add(3*11, -12*11);
		Coord p2 = CRCoord.add(3*11, -5*11);
		
		ArrayList<Gob> unsortedCubs = m_util.getObjects("cupboard", p1, p2);
		ArrayList<Gob> sortedCubs = m_util.superSortGobList(unsortedCubs, true, false, true);
		
		for(Gob cub : sortedCubs){
			Inventory cubInv = m_util.advWalkToContainer(sortedCubs, cub);
			
			int i = enoughWater(cubInv);
			if(i > 1){
				m_util.waterTransfer(false);
			}
			
			if(i == 3) return;
		}
		
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
		if(water == 0) return 1;
		
		if(water >= 100) return 3;
		
		return 2;
	}
	
	void refreshments(){
		if(m_util.getTW() > m_mainPath.travWear ){
			refillBars(false);
		}else if(m_util.getStamina() < 85 ){
			refillBars(true);
		}
		
		if(m_util.getHunger() < 600 ){
			hungerStation();
		}
	}
	
	void fieldRefreshments(){
		if(m_util.getStamina() < 85 ){
			fieldWater(1);
		}
	}
	
	void hungerStation(){
		Coord CRCoord = getCRoffcet();
		
		Coord p1 = CRCoord.add(3*11, -12*11);
		Coord p2 = CRCoord.add(3*11, -5*11);
		
		ArrayList<Gob> unsortedCubs = m_util.getObjects("cupboard", p1, p2);
		ArrayList<Gob> sortedCubs = m_util.superSortGobList(unsortedCubs, true, false, false);
		
		for(Gob cub : sortedCubs){
			if(m_util.stop) return;
			
			if(m_util.windowOpen("Cupboard") ){
				m_util.autoCloseWindow("Cupboard");
				while(m_util.windowOpen("Cupboard") && !m_util.stop) m_util.wait(100);
			}
			
			Inventory cubInv = m_util.advWalkToContainer(sortedCubs, cub);
			
			ArrayList<Item> itemList = m_util.getItemsFromInv(cubInv);
			
			for(Item i : itemList){
				if(m_util.getHunger() < 900){
					m_util.itemAction(i);
					m_util.autoFlowerMenu("Eat");
					while(m_util.flowerMenuReady() && !m_util.stop) m_util.wait(200);
				}
			}
			
			if(m_util.getHunger() >= 900 ) return;
		}
	}
	
	void refillBars(boolean waterOnly){
		m_util.ejectDropObject();
		
		Gob chest = m_util.smallChest(null);
		
		if(chest == null || m_util.stop) return;
		
		int count = 100;
		while(!m_util.stop && !m_util.windowOpen("Chest") ){
			m_util.wait(100);
			count++;
			if(count > 100){
				count = 0;
				m_util.objectSurf(chest);
				m_util.clickWorldObject(3, chest);
			}
		}
		
		m_util.drinkFromContainer(chest, waterOnly);
		
		dropPickupBoat(true);
	}
	
	void fieldWater(int type){
		Gob container = null;
		String contName = null;
		
		if(type == 1){
			contName = "Chest";
			Coord CRCoord = getCRoffcet();
			
			container = m_util.smallChest(CRCoord);
		}else if(type == 2){
			contName = "Chest";
			container = m_util.findClosestObject("gfx/terobjs/furniture/cclosed");
		}
		
		if(container == null || m_util.stop) return;
		
		walkToGob(container);
		while(!m_util.stop && !m_util.windowOpen(contName) ) m_util.wait(100);
		
		m_util.drinkFromContainer(container, true);
	}
	
	void useCR(){
		int num = m_mainPath.crNumber;
		if(num > 5) num -= 5;
		
		Coord spot = getCRoffcet();
		
		if(m_mainPath.crNumber <= 5) spot = spot.add(0,-22);
		
		Gob crossRoad = m_util.findClosestObject("gfx/terobjs/crossroads", spot );
		
		for(int i = 0; i < 2; i++){
			if(m_util.useCR(num, crossRoad) == 2){
				refillBars(false);
			}else{
				return;
			}
		}
	}
	
	void loadArea(Coord preJump){
		if(m_util.stop) return;
		
		m_util.loadArea();
		
		wiggle();
		
		Gob crossRoad = m_util.findClosestObject("gfx/terobjs/crossroads");
		while(!m_util.stop && crossRoad == null ){
			crossRoad = m_util.findClosestObject("gfx/terobjs/crossroads");
			m_util.wait(100);
		}
		if(crossRoad == null) return;
		m_mainPath.centerCalcCR = crossRoad.getr();
	}
	
	void wiggle(){
		int count = 0;
		m_util.clickWorld(1, m_util.getPlayerCoord() );
		while(!m_util.checkPlayerWalking() && !m_util.stop){
			count++;
			m_util.wait(20);
			if(count > 100){
				wiggle();
				return;
			}
		}
		while(m_util.checkPlayerWalking() && !m_util.stop) m_util.wait(20);
		
		m_util.clickWorld(1, m_util.getPlayerCoord().add(0,-1) );
		
		while(!m_util.objectDirection(m_util.getPlayerGob() ).equals(new Coord(0,-1) ) && !m_util.stop){
			m_util.wait(100);
		}
	}
	
	void boatTraveler(){
		if(m_mainPath.boatTravel){
			Gob boat = m_util.findClosestObject("boat");
			
			if(m_mainPath.boatPickup){
				Coord c = m_mainPath.firstCheckPoint();
				m_util.objectSurf(m_util.gobToNegRect(boat), c, false, false);
				m_util.clickWorld(3, c);
				while(m_util.checkPlayerCarry() && !m_util.stop) m_util.wait(500);
			}
			
			m_util.objectSurf(boat);
			m_util.clickWorldObject(3, boat);
			m_util.autoFlowerMenu("Avast!");
			while(!m_util.checkPlayerSitting() && !m_util.stop) m_util.wait(500);
		}
	}
	
	void redCheck(){
		if(m_mainPath.portOnRed && m_mainPath.redFound){
			m_util.pathing = false;
			m_util.sendErrorMessage("Red found on checkpoint: " + m_mainPath.checkPointNum );
			hearth();
			m_util.stop = true;
		}
	}
	
	void herbFiltering(){
		for(Gob g : m_mainPath.herbList){
			if(herbFilter(g) ) m_mainPath.filterdHerbList.add(g);
		}
		
		m_mainPath.herbList.clear();
	}
	
	void work(){
		if(m_mainPath.unloading) return;
		
		if(!m_mainPath.working && !m_mainPath.redFound){
			if(m_mainPath.filterdHerbList.size() > 0){
				m_mainPath.walking = false;
				m_mainPath.working = true;
				m_mainPath.pickCurio = 1;
				
				m_mainPath.closestCurio = m_util.getClosestObjectInArray(m_mainPath.filterdHerbList );
				m_mainPath.filterdHerbList.remove(m_mainPath.closestCurio);
				walkToCoord(m_mainPath.closestCurio.getr() );
			}else if(!m_mainPath.walking){
				m_mainPath.walking = true;
				walkToCoord(m_mainPath.checkPoint);
			}else if(!m_util.PFrunning){
				m_mainPath.walking = false;
			}else if(m_quickFix < 10){
				m_quickFix++;
				if(m_quickFix == 9) m_mainPath.walking = false;
			}
		}else if(!m_mainPath.redFound){
			if(!m_util.PFrunning && m_mainPath.pickCurio == 1){
				if(m_util.getPlayerCoord().equals(m_mainPath.closestCurio.getr() ) ){
					m_mainPath.pickCurio = 2;
					
					if(m_util.getPlayerBagSpace() <= 2) m_mainPath.homeUnload = true;
					m_util.runFlask = false;
					m_util.clickWorldObject(3, m_mainPath.closestCurio);
					//
					while(!m_util.stop && !m_util.flowerMenuReady() ) m_util.wait(50);
					m_util.flowerMenuSelect("Pick");
					m_util.runFlask = true;
					//
				}else{
					m_mainPath.working = false;
				}
			}else if(!m_util.PFrunning && !m_util.findObject(m_mainPath.closestCurio ) ){
				m_mainPath.working = false;
			}else if(!m_util.PFrunning && m_util.findObject(m_mainPath.closestCurio ) ){
				if(!m_util.hasHourglass() ){
					m_mainPath.pickCurio++;
					if(m_mainPath.pickCurio > 100) m_mainPath.pickCurio = 1;
				}else{
					m_mainPath.pickCurio = 2;
				}
			}
		}else if(!m_mainPath.walking){
			m_mainPath.walking = true;
			walkToCoord(m_mainPath.checkPoint);
		}else if(!m_util.PFrunning){
			m_mainPath.walking = false;
		}else if(m_quickFix < 10){
			m_quickFix++;
			if(m_quickFix == 9) m_mainPath.walking = false;
		}
	}
	
	void unloadIntoLC(){
		if(!m_mainPath.boatFiller || m_mainPath.working || (!m_mainPath.homeUnload && !m_mainPath.unloading) ) return;
		
		if(m_mainPath.redFound){
			m_mainPath.homeUnload = true;
			m_mainPath.unloading = false;
		}if(m_mainPath.homeUnload && !m_mainPath.unloading && m_mainPath.boatFills < 2){
			Line l = m_util.closestLand(m_util.getPlayerCoord(), m_claim);
			if(l == null) return;
			m_mainPath.walkingCoord = l.c1;
			m_mainPath.ejectCoord = l.c2;
			walkToCoord(m_mainPath.walkingCoord);
			m_mainPath.walking = true;
			m_mainPath.unloading = true;
			m_mainPath.homeUnload = false;
		}else if(!m_util.PFrunning && m_mainPath.walking && m_mainPath.unloading){
			if(m_mainPath.walkingCoord.equals(m_util.getPlayerCoord() )){
				m_mainPath.walking = false;
			}else{
				walkToCoord(m_mainPath.walkingCoord);
			}
		}else if(!m_mainPath.walking && m_mainPath.unloading){
			Gob boat = m_util.findClosestObject("boat");
			m_util.clickWorld(1, m_mainPath.ejectCoord, 2);
			while(m_util.checkPlayerSitting() && !m_util.stop) m_util.wait(500);
			m_util.wait(500);
			while(m_mainPath.boatFills < 2 && !m_util.stop){
				m_mainPath.boatFills++;
				m_util.matsFromToBoat(true, boat);
				if(m_util.getPlayerBagSpace() > 0) break;
			}
			boatTraveler();
			
			if(m_util.getPlayerBagSpace() == 0) m_mainPath.homeUnload = true;
			
			m_mainPath.unloading = false;
		}
	}
	
	void moveBreaker(){
		if( ( m_mainPath.lastCheckPointReached && m_mainPath.checkPointReached() ) || (m_mainPath.homeUnload && !m_mainPath.working) ){
			m_mainPath.moverAcctive = false;
		}
	}
	
	void dumpCurios(){
		if(m_util.stop) return;
		
		if(m_util.getPlayerBagItems() == 0) return;
		Inventory bag = m_util.getInventory("Inventory");
		
		Coord CRCoord = getCRoffcet();
		
		for(int i = 0; i < 2; i++){
			if(m_util.getItemFromBag("mussel") == null && i == 0) continue;
			int rowMod = 1;
			
			Coord p1 = CRCoord.add(-2*11 * rowMod, -12*11);
			Coord p2 = CRCoord.add(-2*11 * rowMod, 30*11);
			
			if(i != 0){
				p1 = CRCoord.add(3*11, -5*11);
				p2 = CRCoord.add(3*11, 15*11);
			}
			
			ArrayList<Gob> unsortedCubs = m_util.getObjects("cupboard", p1, p2);
			ArrayList<Gob> sortedCubs = m_util.superSortGobList(unsortedCubs, true, true, true);
			
			int k;
			if(i == 1) k = m_westMem;
			else  k = m_eastMem;
			
			boolean redo = true;
			int breakCount = 0;
			while(redo && breakCount < 2 && !m_util.stop){
				redo = false;
				for(int j = k; j < sortedCubs.size(); j++){
					Gob cub = sortedCubs.get(j);
					
					if(m_util.stop) return;
					
					if(m_util.windowOpen("Cupboard") ){
						m_util.autoCloseWindow("Cupboard");
						while(m_util.windowOpen("Cupboard") && !m_util.stop) m_util.wait(100);
					}
					
					if(( m_util.getItemFromBag("mussel") == null && i == 0) || bagCheck() == 0 ) break;
					
					if(( m_util.getItemFromBag("mussel") == null && i == 0) || bagCheck() == 0) break;
					
					Inventory cubInv = m_util.advWalkToContainer(sortedCubs, cub);
					
					if(( m_util.getItemFromBag("mussel") == null && i == 0) || bagCheck() == 0) break;
					
					if(i == 1) m_westMem = j;
					else  m_eastMem = j;
					
					if(bagCheck() > 0){
						if(i == 0){
							ArrayList<Item> itemList = m_util.getItemsFromInv(bag);
							for(Item itm : itemList){
								if(itm.GetResName().contains("gfx/invobjs/mussel")){
									m_util.transferItem(itm);
								}
							}
						}else{
							ArrayList<Item> itemList = m_util.getItemsFromInv(bag);
							for(Item itm : itemList){
								if(!itm.GetResName().contains("bucket") && !itm.GetResName().contains("flask")){
									m_util.transferItem(itm);
								}
							}
						}
					}
					
					if(j == sortedCubs.size() - 1 ){
						redo = true;
						breakCount++;
						k = 0;
					}
				}
			}
			
			if(breakCount >= 2 && i == 1) m_util.stop = true; 
		}
	}
	
	int bagCheck(){
		ArrayList<Item> list = m_util.getItemsFromBag();
		int itemCount = 0;
		for(Item itm : list){
			if(!itm.GetResName().contains("bucket") && !itm.GetResName().contains("flask")){
				itemCount++;
			}
		}
		
		return itemCount;
	}
	
	Coord boatHearth(){
		Coord exitPoint = m_mainPath.lastCheckPoint();
		while(!m_util.getPlayerCoord().equals(exitPoint) && !m_util.stop){
			walkToCoord(exitPoint);
			while(m_util.PFrunning && !m_util.stop){ m_util.wait(50);}
		}
		m_util.clickWorld(1, m_util.getPlayerCoord().add(10,10), 2);
		
		if(m_mainPath.boatPickup){
			Gob boat = m_util.findClosestObject("boat");
			if(!m_util.stop) m_util.sendAction("carry");
			if(!m_util.stop) m_util.clickWorldObject(1, boat);
			while(!m_util.checkPlayerCarry() && !m_util.stop){m_util.wait(200);}
		}
		
		return hearth();
	}
	
	Coord hearth(){
		m_util.runFlask = false;
		m_util.pathing = false;
		m_util.sendAction("theTrav", "hearth");
		
		while(!m_util.hasHourglass() && !m_util.stop){ m_util.wait(50);}
		Coord hearthPoint = m_util.getPlayerCoord();
		m_mainPath.scannerAcctive = false;
		while(m_util.hasHourglass() && !m_util.stop){ m_util.wait(50);}
		int count = 0;
		while(hearthPoint.dist(m_util.getPlayerCoord() ) < 20 && !m_util.stop){
			m_util.wait(50);
			count++;
			if(count > 100){
				return hearth();
			}
		}
		
		return hearthPoint;
	}
	
	void walkToCoord(Coord to){
		m_util.pathing = false;
		while(m_util.PFrunning && !m_util.stop)	m_util.wait(50);
		
		if(!m_util.PFrunning){
			m_util.pathing = true;
			m_util.PFrunning = true;
			PathWalker walk = new PathWalker(m_util, to);
			walk.start();
		}
	}
	
	void walkToGob(Gob to){
		m_util.pathing = false;
		while(m_util.PFrunning && !m_util.stop)	m_util.wait(50);
		
		if(!m_util.PFrunning){
			m_util.pathing = true;
			m_util.PFrunning = true;
			PathWalker walk = new PathWalker(m_util, to);
			walk.start();
		}
	}
	
	boolean specialAction(){
		if(m_mainPath.specialAcct == 1){
			return mountainSpecials();
		}
		
		return false;
	}
	
	boolean mountainSpecials(){
		if(m_mainPath.checkPointNum == 1){
			if(m_util.getPlayerCoord().equals(m_mainPath.checkPoint) ){
				ArrayList<Gob> felds = m_util.getObjects("gfx/terobjs/herbs/feldspar", 100);
				
				m_mainPath.filterdHerbList.addAll(felds);
				return false;
			}else{
				return true;
			}
		}else if(m_mainPath.checkPointNum == 5){
			fieldWater(2);
		}
		
		return false;
	}
	
	Rectangle calcIgnoreZone(Coord a, Coord b){
		int smallestX = a.x;
		int largestX = b.x;
		
		int smallestY = a.y;
		int largestY = b.y;
		
		if(b.x < a.x){
			smallestX = b.x;
			largestX = a.x;
		}
		
		if(b.y < a.y){
			smallestY = b.y;
			largestY = a.y;
		}
		
		return new Rectangle(smallestX, smallestY, largestX - smallestX, largestY - smallestY);
	}
	
	boolean herbFilter(Gob herb){
		String name = herb.resname();
		
		for(Rectangle r : m_mainPath.ignoreZone){
			Coord c = herb.getr();
			Rectangle zone = new Rectangle(r.x + m_mainPath.centerCalcCR.x , r.y + m_mainPath.centerCalcCR.y , r.width, r.height);
			
			if(zone.contains(c.x, c.y) ) return false;
		}
		
		if(m_mainPath.curioType == 1){
			if(name.contains("gfx/terobjs/herbs/mussel") ) return true;
			if(name.contains("gfx/terobjs/herbs/flotsam") ) return true;
			
		}else if(m_mainPath.curioType == 2){
			if(name.contains("gfx/terobjs/herbs/edelweiss") ) return true;
			if(name.contains("gfx/terobjs/herbs/frogscrown") ) return true;
			if(name.contains("gfx/terobjs/herbs/glimmermoss") ) return true;
			
		}else if(m_mainPath.curioType == 3){
			if(name.contains("gfx/terobjs/herbs/chimingbluebell") ) return true;
			if(name.contains("gfx/terobjs/herbs/royaltoadstool") ) return true;
			if(name.contains("gfx/terobjs/herbs/bloatedbolete") ) return true;
			
			//if(name.contains("gfx/terobjs/herbs/ladysmantle") ) return true;
			
		}else if(m_mainPath.curioType == 4){
			if(name.contains("gfx/terobjs/herbs/cavebulb") ) return true;
			if(name.contains("gfx/terobjs/herbs/caveclay") ) return true;
			
		}else if(m_mainPath.curioType == 5){
			if(name.contains("gfx/terobjs/herbs/mussel") ) return true;
			if(name.contains("gfx/terobjs/herbs/flotsam") ) return true;
			if(name.contains("gfx/terobjs/herbs/grayclay") ) return true;
		}else if(m_mainPath.curioType == 6){
			if(name.contains("gfx/terobjs/herbs/chimingbluebell") )	return true;
			if(name.contains("gfx/terobjs/herbs/bloatedbolete") ) return true;
		}
		
		return false;
	}
	
	void fileLoader(int area){
		pathClass path = new pathClass();
		pathClass load = m_pathLoader[area];
		
		path.pathName = load.pathName;
		path.pathList = load.pathList;
		path.curioType = load.curioType;
		
		path.portOnRed = load.portOnRed;
		
		path.crNumber = load.crNumber;
		path.boatTravel = load.boatTravel;
		path.boatPickup = load.boatPickup;
		path.boatFiller = load.boatFiller;
		path.redoOnUnload = load.redoOnUnload;
		path.crossRoadChain = load.crossRoadChain;
		
		path.inventoryType = load.inventoryType;
		
		path.ignoreZone = load.ignoreZone;
		
		m_mainPathList.add(0, path);
	}
	
	void pathLoader(int area){
		if(!m_backup){
			fileLoader(area);
		}
	}
	
	public void run(){
		if(m_option == 10000){
			Gob cr = m_util.findClosestObject("gfx/terobjs/crossroads");
			
			if(cr != null){
				m_util.m_safePos = cr.getr();
				m_util.sendSlenMessage("Crossroad coord set to: " + m_util.m_safePos);
			}else{
				m_util.sendErrorMessage("No crossroad found.");
			}
		}else if(m_option == 100000){
			Coord spot = m_util.getPlayerCoord().sub(m_util.m_safePos);
			System.out.println("(" + spot.x + ", " + spot.y + ")");
		}else if(m_option == 1000000){
			System.out.println();
			rallyPoints rally = m_util.getRallyPoints();
			for(rallyPoints r : rally.rally){
				Coord spot = r.c.sub(m_util.m_safePos);
				System.out.println("Coord=(" + spot.x + ", " + spot.y + ")");
			}
		}else{
			startFunctions();
			
			checkStartLocation();
			
			startManager();
		}
		
		m_util.pathing = false;
		m_util.running(false);
	}
	
	private class scannerClass extends Thread{
		public void run(){
			while(!m_util.stop){
				m_util.wait(200);
				if(m_mainPath.scannerAcctive) scanner();
			}
		}
	}
	
	private class pathClass extends Thread{
		boolean scannerAcctive;
		boolean moverAcctive;
		
		String pathName = "";
		Coord checkPoint;
		int checkPointNum;
		boolean lastCheckPointReached;
		HashSet<Integer> garbageList;
		//ArrayList<Integer> garbageList;
		ArrayList<Coord> pathList;
		ArrayList<Rectangle> ignoreZone;
		Coord centerCalcCR;
		
		ArrayList<Gob> herbList;
		ArrayList<Gob> filterdHerbList;
		Gob closestCurio;
		boolean working;
		boolean walking;
		Coord walkingCoord;
		Coord ejectCoord;
		int pickCurio;
		boolean homeUnload;
		boolean unloading;
		
		boolean redFound;
		
		int crNumber;
		int curioType;
		boolean portOnRed;
		boolean boatTravel;
		boolean boatPickup;
		boolean boatFiller;
		boolean fieldStart;
		boolean redoOnUnload;
		boolean crossRoadChain;
		int inventoryType;
		
		int specialAcct;
		int boatFills = 0;
		int travWear = 89;
		
		public pathClass(){
			checkPoint = Coord.z;
			checkPointNum = 0;
			lastCheckPointReached = false;
			garbageList = new HashSet<Integer>();
			pathList = new ArrayList<Coord>();
			ignoreZone = new ArrayList<Rectangle>();
			centerCalcCR = Coord.z;
			
			herbList = new ArrayList<Gob>();
			filterdHerbList = new ArrayList<Gob>();
			closestCurio = null;
			working = false;
			walking = false;
			pickCurio = 1;
			homeUnload = false;
			unloading = false;
			
			scannerAcctive = false;
			moverAcctive = false;
			redFound = false;
			
			crNumber = -1;
			curioType = -1;
			portOnRed = false;
			boatTravel = false;
			boatPickup = false;
			boatFiller = false;
			fieldStart = false;
			redoOnUnload = false;
			crossRoadChain = false;
			
			specialAcct = 0;
		}
		
		void setPath(ArrayList<Coord> data){
			pathList = data;
		}
		
		boolean checkPointReached(){
			return m_util.getPlayerCoord().dist(checkPoint) < m_breakDist;
		}
		
		void updateCheckpoint(){
			if(checkPointNum == 0){
				checkPoint = centerCalcCR.add(pathList.get(checkPointNum) );
				checkPointNum++;
				walking = false;
				if(pathList.size() <= checkPointNum) lastCheckPointReached = true;
			}else{
				walking = false;
				checkPoint = centerCalcCR.add(pathList.get(checkPointNum) );
			}
		}
		
		void nextCheckPoint(){
			if(specialAction() ){
				//special condisions
			}else if(!lastCheckPointReached){
				checkPoint = centerCalcCR.add(pathList.get(checkPointNum) );
				checkPointNum++;
				walking = false;
				if(pathList.size() <= checkPointNum) lastCheckPointReached = true;
			}
		}
		
		Coord firstCheckPoint(){
			return centerCalcCR.add( pathList.get(0) );
		}
		
		Coord lastCheckPoint(){
			return centerCalcCR.add( pathList.get(pathList.size() - 1 ) );
		}
		
		void clear(){
			checkPoint = Coord.z;
			checkPointNum = 0;
			lastCheckPointReached = false;
			
			garbageList.clear();
			herbList.clear();
			filterdHerbList.clear();
			
			closestCurio = null;
			working = false;
			walking = false;
			pickCurio = 1;
			homeUnload = false;
			redFound = false;
			
			boatFills = 0;
		}
		
		void miniClear(){
			garbageList.clear();
			herbList.clear();
			filterdHerbList.clear();
			
			closestCurio = null;
			working = false;
			walking = false;
			pickCurio = 1;
			redFound = false;
		}
	}
	
	void fileConfig(){
		try{
			File file = new File("scriptConf/curioRunner.save");
			
			if(!file.exists()){
				System.out.println("File not found. Loading backup.");
				m_backup = true;
				return;
			}
			
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String strLine;
			String str;
			pathClass path = new pathClass();
			
			while((strLine = br.readLine()) != null && !m_util.stop){
				strLine = strLine.toLowerCase();
				str = strLine.replaceAll("[ ()]", "");
				String[] parts = str.split("=");
				
				try{
					if(strLine.contains("//") ){
						// skip
					}else if(strLine.contains("area") ){
						path = new pathClass();
						int area = Integer.parseInt(parts[1]);
						m_pathLoader[area] = path;
					}else if(strLine.contains("coord")){
						String[] coordStrings = parts[1].split(",");
						Coord c = new Coord(Integer.parseInt(coordStrings[0]), Integer.parseInt(coordStrings[1]));
						path.pathList.add(c);
					}else if(strLine.contains("name")){
						path.pathName = parts[1];
					}else if(strLine.contains("curio type")){
						int type = Integer.parseInt(parts[1]);
						path.curioType = type;
					}else if(strLine.contains("cr number")){
						int i = Integer.parseInt(parts[1]);
						path.crNumber = i;
					}else if(strLine.contains("boat travel")){
						boolean b = parts[1].contains("true");
						path.boatTravel = b;
					}else if(strLine.contains("boat pickup")){
						boolean b = parts[1].contains("true");
						path.boatPickup = b;
					}else if(strLine.contains("boat filler")){
						boolean b = parts[1].contains("true");
						path.boatFiller = b;
					}else if(strLine.contains("port on red")){
						boolean b = parts[1].contains("true");
						path.portOnRed = b;
					}else if(strLine.contains("redo on unload")){
						boolean b = parts[1].contains("true");
						path.redoOnUnload = b;
					}else if(strLine.contains("cross road chain")){
						boolean b = parts[1].contains("true");
						path.crossRoadChain = b;
					}else if(strLine.contains("inventory type")){
						int i = Integer.parseInt(parts[1]);
						path.inventoryType = i;
					}else if(strLine.contains("ignore zone")){
						String[] coordStrings = parts[1].split(",");
						Coord c = new Coord(Integer.parseInt(coordStrings[0]), Integer.parseInt(coordStrings[1]));
						Coord d = new Coord(Integer.parseInt(coordStrings[2]), Integer.parseInt(coordStrings[3]));
						path.ignoreZone.add(calcIgnoreZone(c, d) );
					}else if(strLine.contains("claim") ){
						boolean b = parts[1].contains("true");
						m_claim = b;
					}
					
				}catch(Exception e){
					m_util.stop = true;
					System.out.println("File corrupted");
					return;
				}
			}
			
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}