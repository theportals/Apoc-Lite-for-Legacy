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

public class BarrelFillerScript extends Thread{
	public String scriptName = "Barrel Filler";
	
	HavenUtil m_util;
	int m_option;
	String m_modify;
	
	Gob m_Vclaim;
	Coord m_p1;
	Coord m_p2;
	int barrelNum = 0;
	
	public void ApocScript(HavenUtil util, int option, String modify){
		m_util = util;
		m_option = option;
		m_modify = modify;
	}
	
	void starters(){
		Coord origo = m_Vclaim.getr();
		m_p1 = m_util.m_pos1.sub(origo);
		m_p2 = m_util.m_pos2.sub(origo);
		
		ArrayList<Item> list = m_util.getItemsFromBag();
		
		int buckets = 0;
		int wine = 0;
		for(Item i : list){
			if(i.GetResName().contains("buckete") ){
				buckets++;
			}else if(i.GetResName().contains("bucket-water") ){
				buckets++;
				if(m_util.getFluid(i.tooltip) != 100){
					m_util.itemAction(i);
					m_util.autoFlowerMenu("Empty");
					while(m_util.flowerMenuReady() && !m_util.stop) m_util.wait(200);
				}
			}else if(i.GetResName().contains("bucket-wine") ){
				wine++;
			}else if(i.GetResName().contains("glass-") ){
				wine++;
			}
			
			if(m_util.stop) return;
		}
		
		if(buckets < 10){
			m_util.sendErrorMessage("Not enough buckets to operate.");
			m_util.stop = true;
		}else if(wine < 2){
			m_util.sendErrorMessage("Wine bucket or wineglass missing.");
			m_util.stop = true;
		}
	}
	
	boolean checkBuckets(){
		ArrayList<Item> list = m_util.getItemsFromBag();
		
		int buckets = 0;
		for(Item i : list){
			if(i.GetResName().contains("bucket-water") ){
				if(m_util.getFluid(i.tooltip) == 100) buckets++;
			}
			
			if(m_util.stop) return true;
		}
		
		if(buckets < 10) return true;
		
		return false;
	}
	
	void fillBucketsFromWell(){
		Gob well = m_util.findClosestObject("gfx/terobjs/well");
		m_util.walkTo(well.getr().add(-7,0));
		bucketHandler(well, false);
	}
	
	boolean fillBarrels(){
		if(m_Vclaim == null) return true;
		Coord origo = m_Vclaim.getr();
		System.out.println(m_p1+" "+m_p2);
		Coord p1 = origo.add(m_p1);
		Coord p2 = origo.add(m_p2);
		
		ArrayList<Gob> barrels = m_util.getObjects("gfx/terobjs/barrel", p1, p2);
		ArrayList<Gob> sortedBarrels = m_util.superSortGobList(barrels, true, true, true);
		
		boolean foundEmptyBarrel = false;
		while(!m_util.stop && !foundEmptyBarrel){
			if(barrelNum >= sortedBarrels.size()){
				return true;
			}
			
			Gob barrel = sortedBarrels.get(barrelNum);
			
			if(checkAndFillBarrel(barrel)){ // if full break loop with bool
				foundEmptyBarrel = true;
			}
			
			barrelNum++;
		}
		
		return false;
	}
	
	boolean checkAndFillBarrel(Gob barrel){
		m_util.walkTo(barrel);
		
		int walkingCount = 0;
		while(!m_util.stop && !m_util.windowOpen("Barrel") ){
			m_util.wait(200);
			
			if(!m_util.checkPlayerWalking() ){
				walkingCount++;
			}else if(walkingCount > 20){
				walkingCount = 0;
				m_util.walkTo(barrel);
			}
		}
		
		String bInfo = m_util.barrelInfo();
		
		if(m_util.getFluid(bInfo) == 0){
			bucketHandler(barrel, true);
			return true;
		}
		
		m_util.autoCloseWindow("Barrel");
		return false;
	}
	
	void bucketHandler(Gob object, boolean barrelFill){
		Inventory bag = m_util.getInventory("Inventory");
		int cyckles = 0;
		int cyckleCount = 10;
		String fullBucket = "bucket-water";
		String emptyBucket = "buckete";
		Coord bucketC = null;
		ArrayList<Item> items = m_util.getItemsFromBag();
		boolean holding = m_util.mouseHoldingAnItem();
		
		String itemTypeName = emptyBucket;
		if(barrelFill) itemTypeName = fullBucket;
		
		/*for(Item container : items){
			if(!container.GetResName().contains(itemTypeName) && !m_util.stop) continue;
			
			if(cyckleCount <= cyckles) break;
			cyckles++;
			
			if(m_util.mouseHoldingAnItem() ){
				m_util.dropItemInBag(container.c);
			}else{
				bucketC = container.c;
				m_util.pickUpItem(container);
				while(!m_util.mouseHoldingAnItem() && !m_util.stop) m_util.wait(100);
			}
			
			while(m_util.mouseHoldingAnItem() && !m_util.stop){
				if(m_util.getMouseItem().GetResName().contains(itemTypeName))
					break;
				m_util.wait(100);
			}
			
			if(!m_util.stop) m_util.itemActionWorldObject(object, 0);
			
			int count = 0;
			boolean breakFull = false;
			
			while(!m_util.stop){
				if(m_util.mouseHoldingAnItem() )
					if(!m_util.getMouseItem().GetResName().contains(itemTypeName) )
						break;
				
				/*if(count > 40){
					if(!m_util.stop) m_util.itemActionWorldObject(emptyingObject, 0);
					count = 0;
				}
				count++;*//*
				
				m_util.wait(100);
			}
		}*/
		boolean disbl = m_util.disableMouseItem;
		m_util.disableMouseItem = true;
		int id = -1;
		for(Item container : items){
			if(!container.GetResName().contains(itemTypeName) && !m_util.stop) continue;
			if(cyckleCount <= cyckles) break;
			cyckles++;
			
			Coord drop = container.c;
			
			if(holding){
				bag.drop(new Coord(0,0), drop);
			}else{
				//bucketC = container.c;
				container.wdgmsg("take", new Object[]{Coord.z});
			}

			if(!m_util.stop) m_util.itemActionWorldObject(object, 0);
			
			if(barrelFill){
				int testBreak = 1;
				boolean broke = false;
				while(!m_util.checkPlayerWalking() && !m_util.stop){
					testBreak++;
					if(testBreak % 50 == 0){
						if(m_util.getMouseItem() != null && !m_util.getMouseItem().GetResName().contains(itemTypeName)){
							if(m_util.getMouseItem().id != id){
								id = m_util.getMouseItem().id;
								broke = true;
								break;
							}
						}
					}
					m_util.wait(10);
				}
				testBreak = 1;
				while(m_util.checkPlayerWalking() && !broke && !m_util.stop){
					testBreak++;
					if(testBreak % 50 == 0){
						if(m_util.getMouseItem() != null && !m_util.getMouseItem().GetResName().contains(itemTypeName)){
							if(m_util.getMouseItem().id != id){
								id = m_util.getMouseItem().id;
								break;
							}
						}
					}
					m_util.wait(10);
				}
			}else{
				while(!m_util.stop){
					if(m_util.getMouseItem() != null && !m_util.getMouseItem().GetResName().contains(itemTypeName)){
						if(m_util.getMouseItem().id != id){
							id = m_util.getMouseItem().id;
							break;
						}
					}
					m_util.wait(100);
				}
			}

			
			bag.drop(new Coord(0,0), drop);
		}
		
		//m_util.dropItemInBag(bucketC);
		while(m_util.mouseHoldingAnItem() && !m_util.stop) m_util.wait(100);
		if(!disbl) m_util.disableMouseItem = false;
	}
	
	void portToWell(){
		m_util.sendAction("theTrav", "hearth");
		while(!m_util.hasHourglass() && !m_util.stop){ m_util.wait(50);}
		while(m_util.hasHourglass() && !m_util.stop){ m_util.wait(50);}
		
		Gob hh = null;
		while(hh == null && !m_util.stop){
			hh = m_util.findClosestObject("hearth-play", 6);
			m_util.wait(500);
		}
		
		m_util.loadArea();
	}
	
	void portHome(){
		m_Vclaim = m_util.carpetIdol();
		if(m_Vclaim != null) return;
		
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
		
		m_Vclaim = null;
		while(m_Vclaim == null && !m_util.stop){
			m_Vclaim = m_util.carpetIdol();
			m_util.wait(500);
		}
		
		m_util.loadArea();
	}
	
	void drinkWine(){
		m_util.quickWine();
	}
	
	void startFiller(){
		while(!m_util.stop){
			if(checkBuckets()){ // if empty run filler
				portToWell();
				fillBucketsFromWell();
			}
			portHome();
			if(fillBarrels()){ // if barrels are all full eject
				break;
			}
		}
	}
	
	public void run(){
		m_util.setPlayerSpeed(2);
		m_util.openInventory();
		m_Vclaim = m_util.carpetIdol();
		
		if(m_Vclaim != null){
			starters();
			
			startFiller();
		}else{
			m_util.sendErrorMessage("No village idol found.");
		}
		
		m_util.running(false);
	}
}