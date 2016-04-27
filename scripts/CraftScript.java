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


public class CraftScript extends Thread{
	public String scriptName = "Craft Script";
	public String[] options = {
		"Craft", "Inverted Craft", "Mussle Dropper", "Butcher Animals", "Intestless Butcher",
	};
	HavenUtil m_util;
	int m_option;
	Gob m_gob;
	Coord m_origin;
	int m_dumpSize;
	int m_pickupSize;
	int m_boneSize;
	int m_rad;
	boolean m_intestDrop = false;
	ArrayList<Item> m_dropList = new ArrayList<Item>();
	boolean m_dropMussle = false;
	
	public void ApocScript(HavenUtil util, int option, String modify){
		m_util = util;
		m_option = option;
	}
	
	cubData getCubData(int Xoffset, int Yoffset, int size, Coord dir){
		cubData cubInfo = new cubData();
		Coord firstCubLocation = new Coord();
		Coord offset = new Coord(Xoffset, Yoffset);
		
		firstCubLocation = m_origin.add(offset.mul(11));
		
		cubInfo.firstCub = firstCubLocation;
		cubInfo.cubSize = size;
		cubInfo.dir = dir;
		
		int realSize = getCubSize(cubInfo);
		cubInfo.setRealSize(realSize);
		
		return cubInfo;
	}
	
	int getCubSize(cubData cubInfo){
		Coord p1 = cubInfo.firstCub;
		int size = cubInfo.cubSize;
		Coord dir = cubInfo.dir;
		
		Coord p2 = p1.add(dir.mul( (size - 1) * 11) );
		
		return m_util.getObjects("gfx/terobjs/cupboard", p1, p2).size();
	}
	
	ArrayList<Gob> getCubs(cubData cubInfo){
		ArrayList<Gob> unsortedCubs = new ArrayList<Gob>();
		ArrayList<Gob> cubList = new ArrayList<Gob>();
		
		Coord p1 = cubInfo.firstCub;
		int size = cubInfo.cubSize;
		Coord dir = cubInfo.dir;
		
		Coord p2 = p1.add(dir.mul( (size - 1) * 11) );
		
		unsortedCubs = m_util.getObjects("gfx/terobjs/cupboard", p1, p2);
		
		boolean ysort = true;
		boolean xsort = true;
		
		if(dir.x == -1) xsort = false;
		if(dir.y == -1) ysort = false;
		
		cubList = m_util.superSortGobList(unsortedCubs, true, xsort, ysort);
		
		return cubList;
	}
	
	void goToCub(int cubNumber, cubData cubInfo){
		ArrayList<Gob> cubList = new ArrayList<Gob>();
		
		if(m_util.stop) return;
		
		cubList = getCubs(cubInfo);
		
		if(m_util.stop) return;
		
		if(cubList.size() == 0){
			m_util.sendErrorMessage("Failed to locate crafting containers.");
			m_util.stop = true;
			return;
		}
		
		Gob cub = cubList.get(cubNumber);
		
		m_util.advWalkToContainer(cubList, cub);
	}
	
	void goToContainer(Gob con, Coord dir){
		Coord neg = con.getneg().bc.sub(2,2);
		if(con.getr().mul(dir.swap()).x < m_util.getPlayerCoord().mul(dir.swap()).x || con.getr().mul(dir.swap()).y < m_util.getPlayerCoord().mul(dir.swap()).y) neg = neg.inv();
		
		Coord c = new Coord(con.getr().add(dir.swap().mul(neg)) );
		if(!m_util.stop) m_util.goToWorldCoord(c);
	}
	
	Coord getContainerDirection(Gob con, ArrayList<Gob> cubList){
		int xCount = 0;
		int yCount = 0;
		
		for(Gob g : cubList )
			if(g.getr().x == con.getr().x)
				xCount++;
		
		for(Gob g : cubList )
			if(g.getr().y == con.getr().y)
				yCount++;
		
		if(xCount == 1 && yCount == 1 ){
			m_util.sendSlenMessage("Only one container found.");
			return new Coord(0,0); // no direction
		}else if(xCount == yCount){
			m_util.sendSlenMessage("Unclear container direction.");
			return new Coord(0,0); // no direction
		}else if(xCount > yCount){
			return new Coord(0,-1); // x direction layout
		}else if(xCount < yCount){
			return new Coord(1,0); // y direction layout
		}else
		
		return new Coord(0,0);
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
	
	void fillStamina(){
		Gob chest = m_util.findClosestObject("gfx/terobjs/furniture/cclosed");
		m_util.drinkFromContainer(chest, true);
	}
	
	void manageTools(int tool){
	
		if(tool == 1){ // caulderon //
			manageCauldron(true);
		}else if(tool == 7){ // caulderon glue //
			manageCauldron(false);
		}else if(tool == 2){ // pow //
			managePow();
		}else if(tool == 3){ // crucible //
			manageCrucible();
		}else{
			goToTool();
		}
	}
	
	void managePow(){
		boolean pickup = false;
		Gob powTest = null;
		
		ArrayList<Gob> powList = m_util.getObjects("gfx/terobjs/pow", m_origin, m_origin );
		for(Gob g : powList){
			if(powTest == null){
				powTest = g;
			}else if(g.resname().equals("gfx/terobjs/pow") ){
				powTest = g;
			}else if(g.resname().equals("gfx/terobjs/powf") ){
				powTest = g;
			}
		}
		
		if(powTest == null || powTest.resname().equals("gfx/terobjs/powb") ){
			goToTool();
			if(m_util.getPlayerBagSpace() < 7){
				pickup = true;
				Gob cub = m_util.findClosestObject("gfx/terobjs/cupboard", m_origin.add(22, 22), m_origin.add(22, 22));
				m_util.clickWorldObject(3, cub);
				
				Inventory cubInv = null;
				while(cubInv == null && !m_util.stop ){
					m_util.wait(200);
					cubInv = m_util.getInventory("Cupboard");
				}
				
				for(int i = 0 ; i < 7; i++)
					m_util.transferItemTo(cubInv, 1);
				
				goToTool();
			}
			
			Gob blockSign = m_util.findClosestObject("gfx/arch/sign");

			m_util.clickWorldObject(3, blockSign);

			while(!m_util.windowOpen("Palisade Cornerpost") && !m_util.stop) m_util.wait(200);
			
			m_util.signTransfer(-1, "Palisade Cornerpost", "gfx");
			
			Item i = null;
			while(i == null && !m_util.stop){
				m_util.wait(200);
				i = m_util.getItemFromBag("gfx/invobjs/wood");
			}
			
			m_util.itemAction(i);
			m_util.autoFlowerMenu("Split");
			while(m_util.flowerMenuReady() && !m_util.stop) m_util.wait(200);
			
			m_util.sendAction("bp", "pow");
			m_util.placeSign(m_origin);
			while(!m_util.windowOpen("Pile of Wood") && !m_util.stop) m_util.wait(200);
			
			m_util.signTransfer(5, "Pile of Wood", "gfx");
			
				m_util.buttonActivate("Pile of Wood");
				m_util.wait(50);
				m_util.buttonActivate("Pile of Wood");
				m_util.wait(50);
				m_util.buttonActivate("Pile of Wood");
				m_util.wait(50);
			
			while(m_util.windowOpen("Pile of Wood") && !m_util.stop) m_util.wait(200);
			
			Gob pow = null;
			while(pow == null && !m_util.stop){
				m_util.wait(200);
				
				ArrayList<Gob> powList2 = m_util.getObjects("gfx/terobjs/pow", m_origin, m_origin );
				for(Gob g : powList2){
					if(g.resname().equals("gfx/terobjs/pow") ){
						pow = g;
					}
				}
			}
			m_gob = pow;
			
			m_util.goToWorldCoord(m_util.getPlayerCoord().add(0,11) );
			
			if(pickup){
				Gob cub = m_util.findClosestObject("gfx/terobjs/cupboard", m_origin.add(22, 22), m_origin.add(22, 22));
				m_util.clickWorldObject(3, cub);
				
				Inventory cubInv = null;
				while(cubInv == null && !m_util.stop ){
					m_util.wait(200);
					cubInv = m_util.getInventory("Cupboard");
				}
				
				for(int j = 0 ; j < 7; j++)
					m_util.transferItemFrom(cubInv, 1);
				
				goToTool();
			}
		}
		
		if( !m_gob.resname().equals("gfx/terobjs/powf") ){
			goToTool();
			m_util.autoFlowerMenu("Light My Fire");
			
			while(!m_util.hasHourglass() && !m_util.stop){ m_util.wait(50);}
			while(m_util.hasHourglass() && !m_util.stop){ m_util.wait(50);}
			
			Gob pow = null;
			while(pow == null && !m_util.stop){
				m_util.wait(200);
				pow = m_util.findClosestObject("gfx/terobjs/powf", m_origin, m_origin);
			}
			m_gob = pow;
		}
		
		goToTool();
	}
	
	void manageCrucible(){
		if(!m_util.windowOpen("Crucible")){
			goToTool();
			while(!m_util.windowOpen("Crucible") && !m_util.stop) m_util.wait(200);
		}
		
		if(m_util.getVmeterAmount(255, false) < 6 ){
			Gob cub = m_util.findClosestObject("gfx/terobjs/cupboard", m_origin.add(-22, 22), m_origin.add(-22, 22));
			m_util.clickWorldObject(3, cub);
			
			Inventory cubInv = null;
			while(cubInv == null && !m_util.stop ){
				m_util.wait(200);
				cubInv = m_util.getInventory("Cupboard");
			}
			
			Item i = m_util.getItemFromInventory(cubInv, "gfx/invobjs/coal");
			m_util.pickUpItem(i);
			while(!m_util.mouseHoldingAnItem() && !m_util.stop) m_util.wait(200);
			
			goToTool();
			m_util.itemActionWorldObject(m_gob, 1);
			while(m_util.mouseHoldingAnItem() && !m_util.stop) m_util.wait(200);
			
			goToTool();
			while(!m_util.windowOpen("Crucible") && !m_util.stop) m_util.wait(200);
		}
		
		m_util.buttonActivate("Crucible");
		m_util.wait(50);
		m_util.buttonActivate("Crucible");
		m_util.wait(50);
		m_util.buttonActivate("Crucible");
		m_util.wait(50);
	}
	
	void goToTool(){
		int i = 1;
		if(m_util.getTileID(m_util.getPlayerCoord().div(11) ) == 22){
			if(m_util.getPlayerCoord().x < m_origin.x ) i = -1;
			
			m_util.walkTo(m_origin.add(7*i,0) );
		}else{
			if(m_util.getPlayerCoord().y < m_origin.y ) i = -1;
			
			m_util.walkTo(m_origin.add(0,7*i) );
		}
		if(m_gob != null)
			m_util.clickWorldObject(3, m_gob);
	}
	
	boolean toolCheck(int tool){
		String toolRes = "N/A";
		
		if(tool == 1 || tool == 7){
			toolRes = "gfx/terobjs/cauldron";
		}else if(tool == 2){
			toolRes = "gfx/terobjs/pow";
		}else if(tool == 3){
			toolRes = "gfx/terobjs/alloyer";
		}else if(tool == 4){
			toolRes = "gfx/terobjs/anvil";
		}else if(tool == 5){
			toolRes = "gfx/terobjs/mgrind";
		}else if(tool == 6){
			toolRes = "gfx/terobjs/churn";
		}
		
		if(!m_gob.resname().contains(toolRes) ) return false;
		
		return true;
	}
	
	void manageCauldron(boolean barrelFill){
		
		if(!m_util.windowOpen("Cauldron")){
			goToTool();
			while(!m_util.windowOpen("Cauldron") && !m_util.stop) m_util.wait(200);
		}
		
		while(m_util.getVmeterAmount(71, false) < 20 && !m_util.stop){
			if(barrelFill){
				barrelCauldron();
			}else{
				//wickerBasketCauldron();
				//bucketCauldron();
			}
		}
		
		if(m_util.getVmeterAmount(255, false) < 10){
			Gob blockSign = m_util.findClosestObject("gfx/arch/sign");
			
			m_util.clickWorldObject(3, blockSign);
			
			while( !m_util.windowOpen("Palisade Cornerpost") && !m_util.windowOpen("Cellar Door") && !m_util.stop) m_util.wait(200);
			
			if(m_util.windowOpen("Palisade Cornerpost") ){
				m_util.signTransferTake("Palisade Cornerpost");    
			}else if(m_util.windowOpen("Cellar Door") ){
				m_util.signTransferTake("Cellar Door", "gfx/invobjs/small/wood");
			}else{
				System.out.println("Failed to get fuel for cauldron.");
			}
			
			goToTool();
			
			m_util.itemActionWorldObject(m_gob, 1);
			
			while(m_util.mouseHoldingAnItem() && !m_util.stop) m_util.wait(200);
			goToTool();
			while(!m_util.windowOpen("Cauldron") && !m_util.stop) m_util.wait(200);
		}
		
		m_util.buttonActivate("Cauldron");
		m_util.wait(50);
		m_util.buttonActivate("Cauldron");
		m_util.wait(50);
		m_util.buttonActivate("Cauldron");
		m_util.wait(50);
	}
	
	/*void wickerBasketCauldron(){
		Gob well = m_util.findClosestObject("gfx/terobjs/well");
		if(well == null){
			return;
		}
		
		Inventory bag = m_util.getInventory("Inventory");
		Coord dropItemC = m_util.emptyItemSlot(bag);
		if(dropItemC != null) m_util.dropItemInBag(dropItemC);
		
		Item bucket = null;
		Gob chest = m_util.findClosestObject("gfx/terobjs/sbasket");
		if(!m_util.windowOpen("Basket")){
			m_util.clickWorldObject(m_util.MOUSE_RIGHT_BUTTON, chest);
			while(bucket == null && !m_util.stop){
				m_util.wait(200);
				Inventory inv = m_util.getInventory("Basket");
				if(inv == null) continue;
				bucket = m_util.getItemFromInventory(inv, "bucket");
			}
		}
		
		Coord bucketC = new Coord(bucket.c);
		m_util.pickUpItem(bucket);
		
		while(!m_util.mouseHoldingAnItem() && !m_util.stop)	m_util.wait(200);
		
		Item handItem = m_util.getMouseItem();
		for(int i = 0; i < 3; i++){
			MultiScript MS = new MultiScript(m_util, well, false, false);
			MS.fillBucket(well, false);
			
			m_util.itemActionWorldObject(m_gob, 0);
			while(!m_util.checkPlayerWalking() && !m_util.stop ) m_util.wait(20);
			while(m_util.checkPlayerWalking() && !m_util.stop ) m_util.wait(20);
		}
		
		Inventory inv = null;
		if(!m_util.windowOpen("Basket")){
			m_util.clickWorldObject(3, chest);
			while(inv == null && !m_util.stop){
				m_util.wait(200);
				inv = m_util.getInventory("Basket");
			}
		}
		
		m_util.dropItemInInv(bucketC, inv);
		
		ArrayList<Item> itemList = new ArrayList<Item>();
		
		goToTool();
		while(!m_util.windowOpen("Cauldron") && !m_util.stop) m_util.wait(200);
	}*/
	
	void bucketCauldron(){
		Coord waterCoord = m_origin.add(55,11);
		
		Item bucket = null;
		Gob chest = m_util.findClosestObject("gfx/terobjs/furniture/cclosed");
		if(!m_util.windowOpen("Chest")){
			m_util.clickWorldObject(3, chest);
			while(bucket == null && !m_util.stop){
				m_util.wait(200);
				Inventory inv = m_util.getInventory("Chest");
				if(inv == null) continue;
				bucket = m_util.getItemFromInventory(inv, "buckete");
			}
		}
		
		Coord bucketC = new Coord(bucket.c);
		m_util.pickUpItem(bucket);
		
		while(!m_util.mouseHoldingAnItem() && !m_util.stop)	m_util.wait(200);
		
		for(int i = 0; i < 3; i++){
			m_util.itemAction(waterCoord);
			while(!m_util.checkPlayerWalking() && !m_util.stop ) m_util.wait(200);
			while(m_util.checkPlayerWalking() && !m_util.stop ) m_util.wait(200);
			
			m_util.itemActionWorldObject(m_gob, 0);
			while(!m_util.checkPlayerWalking() && !m_util.stop ) m_util.wait(200);
			while(m_util.checkPlayerWalking() && !m_util.stop ) m_util.wait(200);
		}
		
		m_util.goToWorldCoord(m_util.getPlayerCoord().add(0,11) );
		
		Inventory inv = null;
		if(!m_util.windowOpen("Chest")){
			m_util.clickWorldObject(3, chest);
			while(inv == null && !m_util.stop){
				m_util.wait(200);
				inv = m_util.getInventory("Chest");
			}
		}
		
		m_util.dropItemInInv(bucketC, inv);
		
		ArrayList<Item> itemList = new ArrayList<Item>();
		
		boolean check = false;
		while(!check && !m_util.stop){
			itemList = m_util.getItemsFromInv(inv);
			for(Item i : itemList){
				if(i.c.equals(bucketC ) ){
					if(!i.GetResName().contains("buckete") ){
						m_util.itemAction(i);
						m_util.autoFlowerMenu("Empty");
					}
					check = true;
					break;
				}
			}
			m_util.wait(200);
		}
		
		goToTool();
		while(!m_util.windowOpen("Cauldron") && !m_util.stop) m_util.wait(200);
	}
	
	void barrelCauldron(){
		Gob barrel = null;
		Coord barrelCoord = null;
		
		while(!m_util.stop){
			barrel = m_util.findClosestObject("gfx/terobjs/barrel", m_origin.add(22,11), m_origin.add(11*11, 22), m_origin.sub(0, 1000) );
			if(barrel == null){
				barrel = m_util.findClosestObject("gfx/terobjs/barrel", m_origin.add(-22,11), m_origin.add(-11*11, 22), m_origin.sub(0, 1000) );
				m_util.goToWorldCoord(m_origin.add(-11,11) );
				if(barrel == null){
					m_util.sendErrorMessage("Station out of water.");
					m_util.stop = true;
					return;
				}
			}else{
				m_util.goToWorldCoord(m_origin.add(11,11) );
			}
			barrelCoord = new Coord(barrel.getr() );
			
			if(removeBarrel(barrel) ){
				m_util.sendAction("carry");
				m_util.clickWorldObject(1, barrel);
				
				while(!m_util.checkPlayerCarry() && !m_util.stop) m_util.wait(200);
				
				m_util.clickWorld(3, barrelCoord.add(0,-22) );
				
				while(m_util.checkPlayerCarry() && !m_util.stop) m_util.wait(200);
				
				m_util.goToWorldCoord(barrelCoord );
				
			}else
				break;
		}
		
		m_util.sendAction("carry");
		m_util.clickWorldObject(1, barrel);
		
		while(!m_util.checkPlayerCarry() && !m_util.stop) m_util.wait(200);
		
		m_util.clickWorldObject(3, m_gob);
		
		while(!m_util.checkPlayerWalking() && !m_util.stop) m_util.wait(100);
		while(m_util.checkPlayerWalking() && !m_util.stop) m_util.wait(200);
		m_util.wait(1000);
		
		m_util.clickWorld(3, barrelCoord);
		while(m_util.checkPlayerCarry() && !m_util.stop) m_util.wait(200);
		
		goToTool();
		while(!m_util.windowOpen("Cauldron") && !m_util.stop) m_util.wait(200);
	}
	
	boolean removeBarrel(Gob barrel){
		m_util.clickWorldObject(3, barrel);
		while(!m_util.windowOpen("Barrel") && !m_util.stop){ m_util.wait(100);}
		String str = new String(m_util.barrelInfo());
		
		if(m_util.getFluid(str) == 0) return true;
		
		return false;
	}
	
	void processMats(int tool){
		while(!m_util.stop){
			if(tool > 0) manageTools(tool);
			m_util.wait(500);
			m_util.craftItem(1);
			int count = 0;
			
			while(!m_util.hasHourglass() && !m_util.stop){
				m_util.wait(200);
				String str = new String(m_util.slenError());
				if(str.contains("You need a") ){
					manageTools(tool);
					m_util.sendSlenMessage("Going to tool again.");
				}else if(str.contains("There needs to") ){
					manageTools(tool);
					m_util.sendSlenMessage("Going to tool again.");
				}else if(str.contains("You do not have all the ingredients.") ){
					m_util.sendSlenMessage("Finished Crafting.");
					m_util.clickWorld(1, m_util.getPlayerCoord());
					m_util.wait(200);
					m_util.clickWorld(1, m_util.getPlayerCoord());
					m_util.wait(200);
					m_util.clickWorld(1, m_util.getPlayerCoord());
					m_util.wait(200);
					m_util.clickWorld(1, m_util.getPlayerCoord());
					m_util.wait(200);
					return;
				}
				count++;
				if(count > 50){
					count = 0;
					m_util.craftItem(1);
				}
			}
			
			while(m_util.hasHourglass() && !m_util.stop){
				m_util.wait(200);
				String str = new String(m_util.slenError());
				if(m_util.getStamina() < 33){
					m_util.clickWorld(1, m_util.getPlayerCoord());
					m_util.wait(200);
					m_util.clickWorld(1, m_util.getPlayerCoord());
					m_util.wait(200);
					m_util.clickWorld(1, m_util.getPlayerCoord());
					m_util.wait(200);
					
					if(tool == 0) m_util.goToWorldCoord(m_origin.add(0,11) );
					fillStamina();
					if(tool == 0) m_util.goToWorldCoord(m_origin.add(0,11) );
					//redo = true;
				}
			}
			
		}
	}
	
	void dropHoldingItemInBag(){
		if(m_util.mouseHoldingAnItem()){
			int bagSize = m_util.getPlayerBagSize();
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
			while(m_util.mouseHoldingAnItem() && !m_util.stop) m_util.wait(200);
		}
	}
	
	int dumpCraftebles(int dump, cubData cubInfo, craftData cData){
		Inventory bag = m_util.getInventory("Inventory");
		int craftedSize = cData.prodSize();
		
		int check = m_util.countItemsInBag(cData.prodName) * craftedSize;
		
		if(cData.secondProd.itemName != null) check += m_util.countItemsInBag(cData.secondProd.itemName );
		
		if(m_util.mouseHoldingAnItem() ) check += craftedSize;
		
		while(check > 0 && !m_util.stop){
			goToCub(dump, cubInfo);
			
			if(m_util.stop) return dump;
			
			Inventory inv = null;
			while(inv == null && !m_util.stop){
				m_util.wait(100);
				inv = m_util.getInventory("Cupboard");
			}
			if(m_util.stop) return dump;
			m_util.wait(400);
			check = check - m_util.getInvSpace(inv);
			
			ArrayList<Item> itemList = m_util.getItemsFromInv(bag);
			for(Item i : itemList){
				if(i.GetResName().contains(cData.prodName ) ){
					m_util.transferItem(i);
				}
				if(cData.secondProd.itemName != null)
					if(i.GetResName().contains(cData.secondProd.itemName ) )
						m_util.transferItem(i);
			}
			
			if(check > 0){
				dump++;
			}else if(m_util.mouseHoldingAnItem() ){
				m_util.dropItemInBag(new Coord(10,10));
				m_util.transferItemTo(inv, 1);
			}
			
			if(dump >= cubInfo.realSize) dump = 0;
		}
		return dump;
	}
	
	boolean pickupMats(ArrayList<MatClass> matInfo, cubData cubInfo){
		Inventory bag = m_util.getInventory("Inventory");
		boolean keepCrafting = true;
		int maxPrio = 0;
		
		if(matInfo.size() > 0) maxPrio = matInfo.get(0).maxPriority + 1;
		
		for(int prio = 0; prio < maxPrio; prio++){
			boolean priorityFound = false;
			int cub = 0;
			boolean matSerch = true;
			int breakCyckle = 0;
			
			if(!m_util.stop) matSerch = matTransfer(bag, matInfo, cub, prio, false);
			
			while(matSerch && breakCyckle < 2 && !m_util.stop){
				boolean viableCub = false;
				
				for(MatClass m : matInfo){
					if(cub >= m.cubNum && m.matSize > 0 && m.priority == prio){
						viableCub = true;
					}
				}
				
				if(viableCub){
					priorityFound = true;
					
					goToCub(cub, cubInfo);
					Inventory inv = null;
					while(inv == null && !m_util.stop){
						m_util.wait(100);
						inv = m_util.getInventory("Cupboard");
					}
					m_util.wait(400);
					
					if(!m_util.stop) matSerch = matTransfer(inv, matInfo, cub, prio, true);
				}
				if(!matSerch) break;
				
				cub++;
				
				if(cub >= cubInfo.realSize){
					if(!priorityFound) break;
					
					cub = 0;
					for(MatClass m : matInfo){
						m.cubNum = 0;
					}
					breakCyckle++;
					if(breakCyckle > 1)
						keepCrafting = false;
				}
			}
		}
		
		for(MatClass m : matInfo)
			m.reset();
		
		return keepCrafting;
	}
	
	ArrayList<MatClass> getMatData(craftData cData){
		ArrayList<MatClass> matInfo = new ArrayList<MatClass>();
		Inventory bag = m_util.getInventory("Inventory");
		
		int craftQuantity = getCraftQuantity(cData);
		
		for(craftData.itemInfo info : cData.io){
			int amount = info.amount;
			int priority = info.priority;
			String itemName = info.itemName;
			
			int quant = amount * craftQuantity;
			
			matInfo.add(new MatClass(itemName, quant, 0, priority) );
		}
		
		if(matInfo.size() > 0) matInfo.get(0).setMaxPrio( cData.getHighestPriority() );
		
		return matInfo;
	}
	
	int getCraftQuantity(craftData cData){
		Inventory bag = m_util.getInventory("Inventory");
		boolean bucketTest = false;
		
		if(cData.prodName.contains("nugget") ){
			return (int)Math.floor((double)(m_util.getPlayerBagSize()-1) / (double)10 );
		}
		
		if(cData.prodName.contains("bar-bronze") ){
			return (int)Math.floor((double)(m_util.getPlayerBagSize()-3) / (double)3 );
		}
		
		if(cData.prodName.contains("gfx/invobjs/butter") ){
			return (int)Math.floor((double)m_util.getPlayerBagSize() / (double)14 ) * 10;
		}
		
		if(cData.prodName.contains("gfx/invobjs/cheese-tray") ){
			Inventory inv = m_util.getInventory("Inventory");
			
			int amount = inv.isz.x;
			if(inv.isz.y < 5) amount -= 2;
			
			return amount;
		}
		
		for(craftData.itemInfo info : cData.io){
			String itemName = info.itemName;
			
			if(itemName.contains("bucket") ){
				bucketTest = true;
				break;
			}
		}
		
		int bagSpace = m_util.getPlayerBagSize() - cData.spaceReduce(bag);
		int craftSize = cData.getTotalCraftSize();
		
		if(bucketTest){
			int quant = 0;
			
			while(!m_util.stop){
				int bucketSpace = getBucketSpace(quant, cData);
				
				if(craftSize * (quant + 1) + bucketSpace > bagSpace) return quant;
				
				quant++;
			}
		}
		
		
		return (int)Math.floor((double)bagSpace / (double)craftSize );
		
	}
	
	int getBucketSpace(int quant, craftData cData){
		int size = 0;
		
		for(craftData.itemInfo info : cData.io){
			String itemName = info.itemName;
			int amount = info.amount;
			
			if(itemName.contains("bucket") ){
				size += 4 * (int)Math.ceil((double)(amount * quant) / (double)100);
			}
		}
		
		return size;
	}
	
	boolean matTransfer(Inventory inv, ArrayList<MatClass> matInfo, int cub, int prio, boolean transfer){
		ArrayList<Item> itemList = new ArrayList<Item>();
		ArrayList<Item> matList = new ArrayList<Item>();
		ArrayList<Item> bucketList = new ArrayList<Item>();
		
		itemList = m_util.getItemsFromInv(inv);
		
		for(Item i : itemList){
			for(MatClass m : matInfo){
				if(i.GetResName().contains(m.matRes) && m.matSize > 0 && m.priority == prio && !matList.contains(i) ){
					if(i.GetResName().contains("bucket") ){
						bucketList.add(i);
						m.matSize -= m_util.getFluid(i.tooltip);
					}else{
						matList.add(i);
						m.matSize--;
						m.cubNum = cub;
					}
				}
			}
		}
		
		if(transfer){
			fillBuckets(bucketList, matInfo, inv, cub);
			
			for(Item i : matList){
				m_util.transferItem(i);
			}
		}
		
		for(MatClass m : matInfo){
			if(m.matSize > 0 && m.priority == prio){
				return true;
			}
		}
		
		return false;
	}
	
	void fillBuckets(ArrayList<Item> bucketList, ArrayList<MatClass> matInfo, Inventory inv, int cub){
		Inventory bag = m_util.getInventory("Inventory");
		ArrayList<Item> invBuckets = m_util.getItemsFromInv("bucket", bag);
		ArrayList<Item> bucketUsed = new ArrayList<Item>();
		
		for(MatClass m : matInfo){
			ArrayList<Item> list = new ArrayList<Item>();
			int added = 0;
			
			if(m.matRes.contains("bucket") ){
				int bucketCount = (int)Math.ceil((double)m.reset / (double)100 );
			
				for(Item bucket : invBuckets){ // full
					if(bucket.GetResName().contains(m.matRes) && !bucketUsed.contains(bucket) && added < bucketCount ){
						added++;
						list.add(bucket);
						bucketUsed.add(bucket);
					}
				}
				
				for(Item bucket : invBuckets){ // empty
					if(bucket.GetResName().contains("buckete") && !bucketUsed.contains(bucket) && added < bucketCount ){
						added++;
						list.add(bucket);
						bucketUsed.add(bucket);
					}
				}
				
				for(Item bucket : bucketList){ // external
					if(added < bucketCount ){
						m.cubNum = cub;
						added++;
						m_util.transferItem(bucket);
						
						boolean found = false;
						
						while(!m_util.stop){
							m_util.wait(300);
							ArrayList<Item> temp = m_util.getItemsFromInv("bucket", bag);
							
							for(Item b : temp){
								if(!invBuckets.contains(b) ){
									list.add(b);
									bucketUsed.add(b);
									found = true;
								}
							}
							if(found) break;
						}
					}else{
						m.cubNum = cub;
						Coord c = bucket.c;
						m_util.pickUpItem(bucket);
						for(Item b : list){
							m_util.itemInteract(b);
						}
						m_util.dropItemInInv(c, inv);
					}
				}
			}
		}
	}
	
	void meatAnimals(){
		int dumpNum = 0;
		int dumpNumBones = 0;
		
		cubData dumpMeatInfo = getCubData(1, 1, 50, new Coord(0,1));
		cubData dumpBoneInfo = getCubData(-3, 1, 50, new Coord(0,1));
		
		if(m_util.getTileID(m_util.getPlayerCoord().div(11) ) == 22){
			dumpMeatInfo = getCubData(1, 1, 50, new Coord(1,0));
			dumpBoneInfo = getCubData(1, -1, 50, new Coord(1,0));
		}
		
		craftData cData = new craftData();
		cData.dumpAll();
		
		while(!m_util.stop){
			boolean finished = false;
			while(!finished && !m_util.stop){
				finished = AnimalButcher(dumpNumBones, dumpBoneInfo);
				
				if(finished) break;
				if(m_util.stop) return;
				
				while(m_util.getPlayerBagSpace() > 1 && !m_util.hasHourglass() && !m_util.stop) m_util.wait(100);
				
				while(m_util.getPlayerBagSpace() > 1 && m_util.hasHourglass() && !m_util.stop){
					m_util.wait(100);
					if(m_intestDrop) dumpIntests();
				}
				
				if(!m_util.stop) dumpNum = dumpCraftebles(dumpNum, dumpMeatInfo, cData );
			}
			
			if(!m_util.stop) dumpNumBones = dumpCraftebles(dumpNumBones, dumpBoneInfo, cData );
			
			if(getNextCarcas()) break;
		}
		
		m_util.walkTo(m_origin);
	}
	
	void dumpIntests(){
		Inventory bag = m_util.getInventory("Inventory");
		ArrayList<Item> itemList = m_util.getItemsFromInv(bag);
		
		for(Item i : itemList){
			if(i.GetResName().contains("intestines") && !m_dropList.contains(i) ){
				m_dropList.add(i);
				m_util.dropItemOnGround(i);
			}
		}
	}
	
	boolean getNextCarcas(){
		Gob cadaver = m_util.findClosestObject("/cdv");
		
		if(cadaver == null) return true;
		
		if(!m_util.stop) m_util.walkTo(cadaver);
		if(!m_util.stop) m_util.sendAction("carry");
		if(!m_util.stop) m_util.clickWorldObject(1, cadaver);
		while(!m_util.checkPlayerCarry() && !m_util.stop) m_util.wait(200);
		
		m_util.walkTo(m_origin.add(0,11) );
		m_util.closeFlowerMenu();
		
		m_util.clickWorld(3, m_origin);
		
		while(m_util.checkPlayerCarry() && !m_util.stop) m_util.wait(200);
		m_gob = cadaver;
		
		return false;
	}
	
	boolean AnimalButcher(int dumpNumBones, cubData dumpBoneInfo){
		if(m_util.getTileID(m_util.getPlayerCoord().div(11) ) == 22){
			m_util.walkTo(m_gob.getr().add(12,0) );
		}else{
			m_util.walkTo(m_gob.getr().add(0,12) );
		}
		m_util.clickWorldObject(3, m_gob);
		
		while(!m_util.flowerMenuReady() && !m_util.stop) m_util.wait(100);
		
		if(m_util.checkFlowerMenu("Skin") ){
			m_util.autoFlowerMenu("Skin");
			
			while(!m_util.hasHourglass() && !m_util.stop) m_util.wait(100);
			while(m_util.hasHourglass() && !m_util.stop) m_util.wait(100);
			
			dumpHide(dumpNumBones, dumpBoneInfo);
			
			return AnimalButcher(dumpNumBones, dumpBoneInfo);
		}else if(m_util.checkFlowerMenu("Collect Bones")){
			m_util.autoFlowerMenu("Collect Bones");
			
			while(!m_util.hasHourglass() && !m_util.stop) m_util.wait(100);
			while(m_util.hasHourglass() && !m_util.stop) m_util.wait(100);
			
			return true;
		}
		
		m_util.autoFlowerMenu("Butcher");
		
		return false;
	}
	
	void dumpHide(int dump, cubData cubInfo){
		while(!m_util.stop){
			goToCub(dump, cubInfo);
			
			if(m_util.stop) return;
			
			Inventory inv = null;
			while(inv == null && !m_util.stop){
				m_util.wait(100);
				inv = m_util.getInventory("Cupboard");
			}
			if(m_util.stop) return;
			m_util.wait(400);
			
			ArrayList<Item> dropList = m_util.getItemsFromBag();
			ArrayList<Coord> Clist = m_util.emptyItemArray(inv, dropList);
			boolean fin = true;
			
			for(int j = 0; j < 56; j++)
				m_util.transferItemTo(inv, 1);
			
			for(Coord c : Clist){
				if(c == null){
					fin = false;
					break;
				}
			}
			
			if(fin) return;
			
			dump++;
			if(dump >= cubInfo.realSize) dump = 0;
		}
	}
	
	void genericCrafter(){
		ArrayList<MatClass> matInfo = new ArrayList<MatClass>();
		int dumpNum = 0;
		
		String s = m_util.getCraftName();
		
		if(s.contains("N/A")){
			m_util.sendErrorMessage("No craftables selected.");
			return;
		}
		
		cubData pickupInfo = getCubData(2, 3, 50, new Coord(0,1));
		cubData dumpInfo = getCubData(-2, 3, 50, new Coord(0,1));
		
		if(m_util.getTileID(m_util.getPlayerCoord().div(11) ) == 22){
			pickupInfo = getCubData(1, -1, 50, new Coord(1,0));
			dumpInfo = getCubData(1, 1, 50, new Coord(1,0));
		}
		
		craftData cData = getCraftData(s);
		
		if(cData == null){
			m_util.sendErrorMessage("Crafting recipe not added.");
			return;
		}
		
		if(m_dropMussle) dumpBoildMussles();
		dumpNum = dumpCraftebles(dumpNum, dumpInfo, cData);
		
		matInfo = getMatData(cData);
		
		if(!toolCheck(cData.tool) && cData.tool != 0 ){
			m_util.sendErrorMessage("Wrong tool, try again.");
			return;
		}
		
		boolean crafting = true;
		while(crafting && !m_util.stop){
			crafting = pickupMats(matInfo, pickupInfo);
			processMats(cData.tool);
			if(m_dropMussle) dumpBoildMussles();
			dumpNum = dumpCraftebles(dumpNum, dumpInfo, cData);
			
			int count = 0;
			while(!m_util.stop && count < 20){m_util.wait(100); count++; }
		}
	}
	
	void invertedCrafter(){
		ArrayList<MatClass> matInfo = new ArrayList<MatClass>();
		int dumpNum = 0;
		
		String s = m_util.getCraftName();
		
		if(s.contains("N/A")){
			m_util.sendErrorMessage("No craftables selected.");
			return;
		}
		cubData pickupInfo = getCubData(-2, 3, 50, new Coord(0,1));
		cubData dumpInfo = getCubData(2, 3, 50, new Coord(0,1));
		
		if(m_util.getTileID(m_util.getPlayerCoord().div(11) ) == 22){
			pickupInfo = getCubData(1, -1, 50, new Coord(1,0));
			dumpInfo = getCubData(1, 1, 50, new Coord(1,0));
		}
		
		craftData cData = getCraftData(s);
		
		if(cData == null){
			m_util.sendErrorMessage("Crafting recipe not added.");
			return;
		}
		
		if(m_dropMussle) dumpBoildMussles();
		dumpNum = dumpCraftebles(dumpNum, dumpInfo, cData);
		
		matInfo = getMatData(cData);
		
		if(!toolCheck(cData.tool) && cData.tool != 0 ){
			m_util.sendErrorMessage("Wrong tool, try again.");
			return;
		}
			
		boolean crafting = true;
		while(crafting && !m_util.stop){
			crafting = pickupMats(matInfo, pickupInfo);
			processMats(cData.tool);
			if(m_dropMussle) dumpBoildMussles();
			dumpNum = dumpCraftebles(dumpNum, dumpInfo, cData);
			
			int count = 0;
			while(!m_util.stop && count < 20){m_util.wait(100); count++; }
		}
	}
	
	void dumpBoildMussles(){
		ArrayList<Item> itemList = m_util.getItemsFromBag();
		Coord c = new Coord(m_util.getPlayerCoord());
		
		m_util.goToWorldCoord(c.add(0,11) );
		
		for(Item i : itemList){
			if(i.GetResName().contains("gfx/invobjs/mussel-boiled") ){
				m_util.dropItemOnGround(i);
			}
		}
		
		m_util.goToWorldCoord(c );
	}
	
	Gob getTool(){
		String[] tools = new String[]{"gfx/terobjs/anvil", "gfx/terobjs/mgrind", "gfx/terobjs/cauldron", "gfx/terobjs/pow", "gfx/terobjs/alloyer", "gfx/terobjs/churn"};
		if(m_option == 4 || m_option == 5) tools = new String[]{"/cdv"};
		Gob g = m_util.findClosestObject(tools);
		
		if(g != null) m_origin = g.getr();
		
		return g;
	}
	
	public void run(){
		m_util.openInventory();
		m_util.setPlayerSpeed(2);
		m_gob = getTool();
		
		if(m_gob == null){
			m_util.sendErrorMessage("No tool found.");
		}else if(m_option == 1){
			genericCrafter();
		}else if(m_option == 2){
			invertedCrafter();
		}else if(m_option == 3){
			m_dropMussle = true;
			genericCrafter();
		}else if(m_option == 4){
			meatAnimals();
		}else if(m_option == 5){
			m_intestDrop = true;
			meatAnimals();
		}
		
		m_util.running(false);
	}
	
	public class MatClass{
		String matRes;
		int matSize;
		int cubNum;
		int priority;
		int maxPriority;
		int reset;
		
		public MatClass(String s, int n, int c, int p){
			matRes = s;
			matSize = n;
			reset = n;
			cubNum = c;
			priority = p;
		}
		
		void reset(){
			matSize = reset;
		}
		
		void setMaxPrio(int p){
			maxPriority = p;
		}
	}
	
	public class cubData{
		Coord firstCub;
		int cubSize;
		Coord dir;
		int realSize = 0;
		
		public cubData(){
		}
		
		public cubData(Coord f, int n, Coord d){
			firstCub = f;
			cubSize = n;
			dir = d;
		}
		
		void setRealSize(int r){
			realSize = r;
		}
		
	}
	
	public class craftData{
		ArrayList<itemInfo> io;
		Coord prodSize;
		String prodName;
		itemInfo secondProd = new itemInfo(1, 0, 1, null);
		int tool;
		
		public craftData(){
			io = new ArrayList<itemInfo>();
		}
		
		void dumpAll(){
			productInfo(1, 1, "gfx");
		}
		
		void productInfo(int x, int y, String n){
			prodSize = new Coord(x, y);
			prodName = n;
		}
		
		void addIngreadient(int size, int quantity, int priority, String name){
			if(name.contains("bucket") ) size = 0;
			
			itemInfo ing = new itemInfo(size, quantity, priority, name);
			io.add(ing);
		}
		
		int ioSize(){
			return io.size();
		}
		
		int prodSize(){
			//if(prodSize.x == 0 && prodSize.y == 0) return 1;
			return prodSize.x * prodSize.y;
		}
		
		int spaceReduce(Inventory bag){
			return bag.isz.x * (prodSize.y - 1) + prodSize.x + secondProd.amount;
		}
		
		void addSecondProd(int a, String n){
			secondProd.amount = a;
			secondProd.itemName = n;
		}
		
		int getTotalCraftSize(){
			int tot = 0;
			
			for(itemInfo iI : io){
				int itemSpace = iI.size * iI.amount;
				tot = tot + itemSpace;
			}
			
			return tot;
		}
		
		int getHighestPriority(){
			int high = -1;
			for(itemInfo iI : io){
				if(iI.priority > high) high = iI.priority;
			}
			
			return high;
		}
		
		public class itemInfo{
			int size;
			int amount;
			int priority;
			String itemName;
			
			public itemInfo(int s, int a, int p, String n){
				size = s;
				amount = a;
				priority = p;
				itemName = n;
			}
		}
	}
	
	/// add stuff here ///
	
	craftData getCraftData(String s){
		craftData data = new craftData();
		//System.out.println(s+" serching.");
		
		// tool free //
		
		data.tool = 0;
		
		// Food PANCAKES
		
		if(s.equals("Sacrebleu") ){
			data.productInfo(1, 1, "gfx/invobjs/pancake-sacrebleu");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/pancake");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/blueberry");
			data.addIngreadient(0, 1, 0, "gfx/invobjs/bucket-milk");
			
			return data;
		}else if(s.contains("Noisette") ){
			data.productInfo(1, 1, "gfx/invobjs/pancake-crepenoisette");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/pancake");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/seed-hazel");
			data.addIngreadient(0, 1, 0, "gfx/invobjs/bucket-honey");
			
			return data;
		}else if(s.contains("Citroulle") ){
			data.productInfo(1, 1, "gfx/invobjs/pancake-crepecitroulle");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/pancake");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/pumpkinflesh");
			data.addIngreadient(0, 1, 0, "gfx/invobjs/butter");
			
			return data;
		}else if(s.equals("Pommace Perdue") ){
			data.productInfo(1, 1, "gfx/invobjs/pancake-pommaceperdue");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/pancake");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/apple");
			data.addIngreadient(0, 1, 0, "gfx/invobjs/bucket-honey");
			
			return data;
		}
		
		// Food dough form
		
		if(s.equals("Dough") ){
			data.productInfo(1, 1, "gfx/invobjs/dough");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flour");
			data.addIngreadient(0, 10, 0, "gfx/invobjs/bucket-water");
			
			return data;
		}else if(s.equals("Bark Bread") ){
			data.productInfo(1, 1, "gfx/invobjs/dough-bread-bark");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/bark");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flour");
			data.addIngreadient(0, 10, 0, "gfx/invobjs/bucket-water");
			
			return data;
		}else if(s.equals("Pumpkin Bread") ){
			data.productInfo(1, 1, "gfx/invobjs/dough-bread-pumpkin");
			data.addIngreadient(1, 2, 1, "gfx/invobjs/seed-pumpkin");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/pumpkinflesh");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flour");
			data.addIngreadient(0, 10, 0, "gfx/invobjs/bucket-water");
			
			return data;
		}else if(s.equals("Apple Pie Dough") ){
			data.productInfo(1, 1, "gfx/invobjs/dough-pie-apple");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/butter");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/apple");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flour");
			data.addIngreadient(0, 5, 0, "gfx/invobjs/bucket-water");
			
			return data;
		}else if(s.equals("Apple Pie Dough") ){
			data.productInfo(1, 1, "gfx/invobjs/dough-pie-apple");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/butter");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/apple");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flour");
			data.addIngreadient(0, 5, 0, "gfx/invobjs/bucket-water");
			
			return data;
		}else if(s.equals("Blueberry Pie") ){
			data.productInfo(1, 1, "gfx/invobjs/dough-pie-blueberry");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/blueberry");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flour");
			data.addIngreadient(0, 5, 0, "gfx/invobjs/bucket-water");
			
			return data;
		}else if(s.equals("Pea Pie") ){
			data.productInfo(1, 1, "gfx/invobjs/dough-pie-pea");
			data.addIngreadient(1, 3, 1, "gfx/invobjs/peapod");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flour");
			data.addIngreadient(0, 5, 0, "gfx/invobjs/bucket-water");
			
			return data;
		}else if(s.equals("Pumpkin Pie") ){
			data.productInfo(1, 1, "gfx/invobjs/dough-pie-pumpkin");
			data.addIngreadient(1, 2, 1, "gfx/invobjs/pumpkinflesh");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flour");
			data.addIngreadient(0, 5, 0, "gfx/invobjs/bucket-water");
			
			return data;
		}else if(s.equals("Carrot Cake Dough") ){
			data.productInfo(1, 1, "gfx/invobjs/dough-cake-carrot");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/butter");
			data.addIngreadient(1, 2, 1, "gfx/invobjs/carrot");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flour");
			data.addIngreadient(0, 5, 0, "gfx/invobjs/bucket-water");
			
			return data;
		}else if(s.equals("Chantrelle & Onion Pirozhki") ){
			data.productInfo(1, 1, "gfx/invobjs/dough-pirozhki");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/dough");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/onion");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/chantrelle");
			
			return data;
		}else if(s.equals("Honey Bun Dough") ){
			data.productInfo(1, 1, "gfx/invobjs/dough-bun-honey");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flour");
			data.addIngreadient(0, 1, 0, "gfx/invobjs/bucket-honey");
			data.addIngreadient(0, 2, 0, "gfx/invobjs/bucket-water");
			
			return data;
		}else if(s.equals("Raisin Butter-Cake Dough") ){
			data.productInfo(1, 1, "gfx/invobjs/dough-cake-raisinbutter");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flour");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/butter");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/raisins");
			data.addIngreadient(0, 5, 0, "gfx/invobjs/bucket-water");
			
			return data;
		}
		
		// Food random
		
		if(s.equals("Chicken Salad") ){
			data.productInfo(1, 1, "gfx/invobjs/salad-chicken");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/egg-boiled");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/meat-chicken-r");
			data.addIngreadient(1, 2, 0, "gfx/invobjs/beetrootleaves");
			
			return data;
		}else if(s.equals("Stinging Salad") ){
			data.productInfo(1, 1, "gfx/invobjs/salad-beet");
			data.addIngreadient(1, 2, 1, "gfx/invobjs/herbs/stingingnettle");
			data.addIngreadient(1, 2, 1, "gfx/invobjs/beetroot");
			data.addIngreadient(1, 2, 1, "gfx/invobjs/beetrootleaves");
			data.addIngreadient(0, 1, 0, "gfx/invobjs/bucket-vinegar");
			
			return data;
		}else if(s.equals("Perched Perch") ){
			data.productInfo(1, 1, "gfx/invobjs/feast-perchedperch");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/carrot");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/onion");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/branch");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/meat-perch");
			
			return data;
		}else if(s.equals("Birchbark Bream") ){
			data.productInfo(1, 1, "gfx/invobjs/feast-bbb");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/meat-bream");
			data.addIngreadient(1, 2, 0, "gfx/invobjs/onion");
			data.addIngreadient(1, 3, 0, "gfx/invobjs/peapod");
			data.addIngreadient(1, 2, 0, "gfx/invobjs/birchbark");
			
			return data;
		}
		
		// Curios
		
		if(s.equals("Bark Boat") ){
			data.productInfo(1, 1, "gfx/invobjs/arrow-bone");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/branch");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/flaxfibre");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/berchbark");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/bark");
			
			return data;
		}else if(s.equals("Cone Cow") ){
			data.productInfo(1, 1, "gfx/invobjs/conecow");
			data.addIngreadient(1, 4, 0, "gfx/invobjs/branch");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/seed-fir");
			
			return data;
		}else if(s.equals("Enthroned Toad") ){
			data.productInfo(1, 2, "gfx/invobjs/enthronedtoad");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/frog");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/herbs/royaltoadstool");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/herbs/frogscrown");
			
			return data;
		}else if(s.equals("Feather Duster") ){
			data.productInfo(1, 2, "gfx/invobjs/featherduster");
			data.addIngreadient(1, 5, 1, "gfx/invobjs/feather-chicken");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flaxfibre");
			data.addIngreadient(2, 1, 0, "gfx/invobjs/wood");
			
			return data;
		}else if(s.equals("Leather Ball") ){
			data.productInfo(1, 1, "gfx/invobjs/leatherball");
			data.addIngreadient(1, 4, 0, "gfx/invobjs/flaxfibre");
			data.addIngreadient(1, 4, 0, "gfx/invobjs/leather");
			
			return data;
		}else if(s.equals("Porcelain Doll") ){
			data.productInfo(1, 2, "gfx/invobjs/porcelaindoll");
			data.addIngreadient(1, 4, 0, "gfx/invobjs/clay-bone");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/flaxfibre");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/linencloth");
			
			return data;
		}else if(s.equals("Primitive Doll") ){
			data.productInfo(1, 2, "gfx/invobjs/flisa");
			data.addIngreadient(2, 1, 0, "gfx/invobjs/wood");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/axe");
			data.addIngreadient(2, 1, 0, "gfx/invobjs/saw");
			
			return data;
		}else if(s.equals("Prism") ){
			data.productInfo(1, 1, "gfx/invobjs/prism");
			data.addIngreadient(1, 3, 0, "gfx/invobjs/rawglass");
			
			return data;
		}else if(s.equals("Rattle-Tattle-Talisman") ){
			data.productInfo(1, 1, "gfx/invobjs/rattletattle");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/flaxfibre");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/branch");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/bone");
			data.addIngreadient(1, 2, 0, "gfx/invobjs/stone-feldspar");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/leather");
			
			return data;
		}else if(s.equals("Seer's Bones") ){
			data.productInfo(2, 1, "gfx/invobjs/seersbones");
			data.addIngreadient(1, 2, 0, "gfx/invobjs/dream");
			data.addIngreadient(1, 5, 0, "gfx/invobjs/bone");
			
			return data;
		}else if(s.equals("Seer's Tea Leaves") ){
			data.productInfo(1, 1, "gfx/invobjs/seerstealeaves");
			data.addIngreadient(1, 2, 0, "gfx/invobjs/dream");
			data.addIngreadient(1, 2, 0, "gfx/invobjs/tea-green");
			data.addIngreadient(1, 2, 0, "gfx/invobjs/tea-black");
			
			return data;
		}else if(s.equals("Shewbread") ){
			data.productInfo(1, 1, "gfx/invobjs/dough-bread-shew");
			data.addIngreadient(1, 3, 1, "gfx/invobjs/beeswax");
			data.addIngreadient(1, 4, 1, "gfx/invobjs/flour");
			data.addIngreadient(0, 5, 0, "gfx/invobjs/bucket-water");
			
			return data;
		}else if(s.equals("Simple Idol") ){
			data.productInfo(1, 1, "gfx/invobjs/simpleidol");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/clay");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/stone");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/stone-feldspar");
			
			return data;
		}else if(s.equals("Straw Doll") ){
			data.productInfo(1, 2, "gfx/invobjs/strawdoll");
			data.addIngreadient(1, 2, 0, "gfx/invobjs/flaxfibre");
			data.addIngreadient(1, 4, 0, "gfx/invobjs/straw");
			
			return data;
		}else if(s.equals("Stuffed Bear") ){
			data.productInfo(4, 1, "gfx/invobjs/ernst");
			data.addIngreadient(4, 1, 0, "gfx/invobjs/hide-prep-bear");
			data.addIngreadient(1, 4, 1, "gfx/invobjs/wool");
			
			return data;
		}else if(s.equals("Tiny Abacus") ){
			data.productInfo(1, 1, "gfx/invobjs/tinyabacus");
			data.addIngreadient(4, 1, 0, "gfx/invobjs/board");
			data.addIngreadient(1, 4, 1, "gfx/invobjs/branch");
			data.addIngreadient(1, 3, 1, "gfx/invobjs/nugget-tin");
			data.addIngreadient(1, 2, 1, "gfx/invobjs/nugget-castiron");
			
			return data;
		}else if(s.equals("Toy Chariot") ){
			data.productInfo(4, 1, "gfx/invobjs/toychariot");
			data.addIngreadient(1, 5, 0, "gfx/invobjs/clay-acre");
			
			return data;
		}
		
		// Random stuff
		
		if(s.equals("Bone Arrow") ){
			data.productInfo(1, 2, "gfx/invobjs/arrow-bone");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/bone");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/branch");
			
			return data;
		}else if(s.equals("Bone Clay") ){
			data.productInfo(1, 1, "gfx/invobjs/clay-bone");
			data.addIngreadient(1, 5, 0, "gfx/invobjs/ash");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/clay-cave");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/stone-feldspar");
			
			return data;
		}else if(s.equals("Cheese Tray") ){
			data.productInfo(1, 2, "gfx/invobjs/cheese-tray");
			data.addIngreadient(4, 1, 0, "gfx/invobjs/board");
			
			return data;
		}
		
		// caulderon //
		
		data.tool = 1;
		
		if(s.equals("Butter-steamed Cavebulb") ){
			data.productInfo(1, 1, "gfx/invobjs/feast-cavebulb");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/butter");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/herbs/cavebulb");
			
			return data;
		}else if(s.equals("Ring of Brodgar") ){
			data.productInfo(1, 1, "gfx/invobjs/dough");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flour");
			data.addIngreadient(1, 2, 1, "gfx/invobjs/seed-poppy");
			data.addIngreadient(0, 1, 0, "gfx/invobjs/bucket-honey");
			data.addIngreadient(0, 5, 0, "gfx/invobjs/bucket-water");
			return data;
		}else if(s.equals("Boiled Egg") ){
			data.productInfo(1, 1, "gfx/invobjs/egg-boiled");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/egg-chicken");
			
			return data;
		}else if(s.equals("Rennet") ){
			data.productInfo(1, 1, "gfx/invobjs/jar-rennet");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/jar");
			data.addIngreadient(0, 3, 0, "gfx/invobjs/bucket-vinegar");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/intestines");
			
			return data;
		}else if(s.equals("Boiled Mussel") ){
			data.productInfo(1, 1, "gfx/invobjs/mussel-boiled");
			data.addSecondProd(4, "gfx/invobjs/pearl");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/mussel");
			
			return data;
		}else if(s.equals("Creamy Cock") ){
			data.productInfo(2, 1, "gfx/invobjs/feast-cc");
			data.addIngreadient(2, 1, 1, "gfx/invobjs/cock-dead");
			data.addIngreadient(1, 2, 2, "gfx/invobjs/cheese-creamycamembert");
			data.addIngreadient(1, 2, 2, "gfx/invobjs/peapod");
			data.addIngreadient(1, 1, 2, "gfx/invobjs/butter");
			data.addIngreadient(0, 10, 0, "gfx/invobjs/bucket-milk");
			
			return data;
		}else if(s.equals("Tea") ){
			data.productInfo(1, 1, "gfx/invobjs/pot-hot");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/pot-tea");
			data.addIngreadient(1, 2, 0, "gfx/invobjs/tea");
			
			return data;
		}else if(s.equals("Boiled Peppper Drupes") ){
			data.productInfo(1, 1, "gfx/invobjs/seed-pepper-boiled");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/seed-pepper");
			
			return data;
		}else if(s.equals("Batter") ){
			data.productInfo(1, 1, "gfx/invobjs/jar-batter");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/egg-chicken");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flour");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/butter");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/jar");
			data.addIngreadient(1, 10, 0, "bucket-milk");
			
			return data;
		}else if(s.equals("Hardened Leather") ){
			data.productInfo(1, 1, "gfx/invobjs/leather-hardened");
			data.addIngreadient(1, 2, 0, "gfx/invobjs/beeswax");
			data.addIngreadient(1, 3, 0, "gfx/invobjs/leather");
			
			return data;
		}else if(s.equals("Zesty Brill") ){
			data.productInfo(1, 1, "gfx/invobjs/zestybrill");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/beetroot");
			data.addIngreadient(1, 2, 1, "gfx/invobjs/peapod");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/onion");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/meat-brill");
			data.addIngreadient(0, 1, 0, "gfx/invobjs/bucket-vinegar");
			
			return data;
		}else if(s.equals("Goldbeater's Skin") ){
			data.productInfo(1, 1, "gfx/invobjs/goldskin");
			data.addIngreadient(1, 2, 1, "gfx/invobjs/intestines");
			data.addIngreadient(0, 3, 0, "gfx/invobjs/bucket-vinegar");
			
			return data;
		}else if(s.equals("Boiled Pepper Drupes") ){
			data.productInfo(1, 1, "gfx/invobjs/seed-pepper-boiled");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/seed-pepper");
			
			return data;
		}else if(s.equals("Spring Stew") ){
			data.productInfo(1, 1, "gfx/invobjs/stew-spring");
			data.addIngreadient(1, 2, 0, "gfx/invobjs/carrot");
			data.addIngreadient(1, 2, 0, "gfx/invobjs/peapod");
			
			return data;
		}
		
		data.tool = 7;
		
		if(s.equals("Bone Glue") ){
			data.productInfo(1, 1, "gfx/invobjs/boneglue");
			data.addIngreadient(1, 10, 0, "gfx/invobjs/bone");
			
			return data;
		}
		
		// pile of wood //
		
		data.tool = 2;
		
		if(s.equals("Roasted Meat") ){
			data.productInfo(1, 1, "-r");
			data.addIngreadient(1, 1, 0, "meat-");
			
			return data;
		}else if(s.equals("Beeted Bird Breast") ){
			data.productInfo(1, 1, "gfx/invobjs/beetedbirdbreast");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/meat-chicken");
			data.addIngreadient(1, 2, 1, "gfx/invobjs/beetroot");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/onion");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/butter");
			data.addIngreadient(1, 4, 1, "gfx/invobjs/seed-pepper-dried");
			data.addIngreadient(0, 2, 0, "gfx/invobjs/bucket-wine");
			data.addIngreadient(0, 2, 0, "gfx/invobjs/bucket-vinegar");
			
			return data;
		}else if(s.equals("Rat on a Stick") ){
			data.productInfo(1, 1, "gfx/invobjs/ratonastick");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/rat");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/branch");
			
			return data;
		}else if(s.equals("Fried Egg") ){
			data.productInfo(1, 1, "gfx/invobjs/egg-fried");
			data.addIngreadient(1, 1, 0, "gfx/invobjs/egg-chicken");
			
			return data;
		}else if(s.equals("Pancake") ){
			data.productInfo(1, 1, "gfx/invobjs/pancake");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/egg-chicken");
			data.addIngreadient(1, 1, 1, "gfx/invobjs/flour");
			data.addIngreadient(0, 2, 0, "gfx/invobjs/bucket-milk");
			
			return data;
		}
		
		// crucible //
		
		data.tool = 3;
		
		if(s.equals("Nuggets") ){
			data.productInfo(1, 1, "nugget-");
			data.addIngreadient(1, 1, 0, "bar-");
			
			return data;
		}else if(s.equals("Tin Soldier") ){
			data.productInfo(1, 1, "tinsoldier");
			data.addIngreadient(1, 8, 0, "nugget-tin");
			
			return data;
		}else if(s.equals("Bar of Bronze") ){
			data.productInfo(1, 1, "bar-bronze");
			data.addIngreadient(1, 1, 0, "bar-tin");
			data.addIngreadient(1, 2, 0, "bar-copper");
			
			return data;
		}else if(s.equals("Bar") ){
			data.productInfo(1, 1, "bar-");
			data.addIngreadient(1, 10, 0, "nugget-");
			
			return data;
		}
		
		// anvil //
		
		data.tool = 4;
		
		if(s.equals("Wrought Iron") ){
			data.productInfo(1, 1, "bar-");
			data.addIngreadient(1, 1, 0, "bloom");
			
			return data;
		}else if(s.equals("Bronze Steed") ){
			data.productInfo(1, 1, "bronzesteed");
			data.addIngreadient(1, 1, 0, "bar-bronze");
			
			return data;
		}else if(s.equals("Soldier's Sword") ){
			data.productInfo(1, 2, "sword");
			data.addIngreadient(2, 1, 0, "wood");
			data.addIngreadient(1, 2, 0, "bar-steel");
			
			return data;
		}else if(s.equals("Völva's Wand") ){
			data.productInfo(1, 3, "volvaswand");
			data.addIngreadient(1, 3, 0, "bar-castiron");
			data.addIngreadient(1, 1, 0, "bar-bronze");
			data.addIngreadient(1, 2, 0, "dream");
			
			return data;
		}else if(s.equals("Seer's Bowl") ){
			data.productInfo(1, 1, "seersbowl");
			data.addIngreadient(1, 4, 0, "nugget-copper");
			data.addIngreadient(1, 2, 0, "dream");
			
			return data;
		}
		
		// churn //
		
		data.tool = 6;
		
		if(s.equals("Butter") ){
			data.productInfo(1, 1, "gfx/invobjs/butter");
			data.addIngreadient(0, 10, 0, "bucket-milk");
			
			return data;
		}
		
		// grinder //
		
		data.tool = 5;
		
		if(s.equals("Bear Salami") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			data.addIngreadient(1, 2, 0, "meat-bear");
			
			return data;
		}else if(s.equals("Bierwurst") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			
			return data;
		}else if(s.equals("Big Bear Banger") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			data.addIngreadient(1, 2, 0, "meat-bear");
			data.addIngreadient(1, 2, 0, "meat-pig");
			
			return data;
		}else if(s.equals("Boar Baloney") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			data.addIngreadient(1, 3, 0, "meat-pig");
			
			return data;
		}else if(s.equals("Boar Boudin") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			data.addIngreadient(1, 1, 0, "meat-cow");
			data.addIngreadient(1, 1, 0, "meat-pig");
			
			return data;
		}else if(s.equals("Chicken Chorizo") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			data.addIngreadient(1, 1, 0, "meat-sheep");
			data.addIngreadient(1, 1, 0, "meat-chicken");
			
			return data;
		}else if(s.equals("Cow Chorizo") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			data.addIngreadient(1, 2, 0, "meat-cow");
			
			return data;
		}else if(s.equals("Delicious Deer Dog") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			data.addIngreadient(1, 3, 0, "meat-deer");
			
			return data;
		}else if(s.equals("Fox Fuet") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			data.addIngreadient(1, 1, 0, "meat-fox");
			data.addIngreadient(1, 2, 0, "meat-pig");
			
			return data;
		}else if(s.equals("Fox Wurst") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			data.addIngreadient(1, 2, 0, "meat-fox");
			
			return data;
		}else if(s.equals("Lamb Sausages") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			data.addIngreadient(1, 1, 0, "meat-sheep");
			data.addIngreadient(1, 1, 0, "meat-pig");
			
			return data;
		}else if(s.equals("Piglet Wursts") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			data.addIngreadient(1, 1, 0, "meat-pig");
			
			return data;
		}else if(s.equals("Running Rabbit Sausage") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			data.addIngreadient(1, 1, 0, "meat-rabbit");
			data.addIngreadient(1, 2, 0, "meat-fox");
			
			return data;
		}else if(s.equals("Tame Game Liverwurst") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			data.addIngreadient(1, 1, 0, "meat-rabbit");
			data.addIngreadient(1, 1, 0, "meat-cow");
			
			return data;
		}else if(s.equals("Wonderful Wilderness Wurst") ){
			data.productInfo(1, 1, "wurst-");
			data.addIngreadient(1, 1, 0, "intestines");
			data.addIngreadient(1, 2, 0, "meat-rabbit");
			data.addIngreadient(1, 2, 0, "meat-pig");
			data.addIngreadient(1, 2, 0, "meat-deer");
			data.addIngreadient(1, 1, 0, "meat-fox");
			data.addIngreadient(1, 1, 0, "meat-bear");
			
			return data;
		}
		
		System.out.println(s+" not found.");
		
		return null;
	}
}