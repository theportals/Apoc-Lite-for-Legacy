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

package addons;

import java.util.ArrayList;
import java.awt.Rectangle;

import haven.Coord;
import haven.Inventory;
import haven.Item;

public class AutoFeast extends Thread{
	int m_type;
	HavenUtil m_util;
	
	public AutoFeast(HavenUtil util, int type){
		m_util = util;
		m_type = type;
	}
	
	void autoFeast(){
		if(m_util.stop) return;
		ArrayList<Item> itemList = new ArrayList<Item>();
		ArrayList<Item> sortedFoodList = new ArrayList<Item>();
		
		Inventory tableInv = m_util.getInventory("Table");
		
		if(tableInv == null) return;
		if(!testTable(tableInv) ) return;
		
		buttonClick("Feast");
		itemList = m_util.getItemsFromInv(tableInv);
		
		sortedFoodList = sortItems(itemList);
		for(Item i : sortedFoodList){
			if(!findItem(i, tableInv) ) continue;
			i.wdgmsg("take", new Object[]{Coord.z});
			
			while(findItem(i, tableInv) && !m_util.stop) m_util.wait(100);
			if(m_util.stop || m_util.getHunger() > 980) return;
		}
		
		autoFeast();
	}
	
	boolean testTable(Inventory tableInv){
		return m_util.itemCount(tableInv) > 0;
	}
	
	boolean findItem(Item i, Inventory tableInv){
		for(Item j : m_util.getItemsFromInv(tableInv) ){
			if(j == i) return true;
		}
		return false;
	}
	
	void buttonClick(String windowName){
		m_util.clickButton(windowName);
		m_util.wait(50);
		m_util.clickButton(windowName);
		m_util.wait(50);
		m_util.clickButton(windowName);
		m_util.wait(50);
	}
	
	ArrayList<Item> sortItems(ArrayList<Item> itemList){
		ArrayList<Item> sortedList = new ArrayList<Item>();
		
		while(itemList.size() > 0 && !m_util.stop){
			Item sorting = null;
			
			for(Item i : itemList){
				if(sorting == null){
					sorting = i;
				}
				if(sorting.c.x > i.c.x){
					sorting = i;
				}
				if(sorting.c.y > i.c.y && sorting.c.x == i.c.x){
					sorting = i;
				}
			}
			
			sortedList.add(sorting);
			itemList.remove(sorting);
		}
		
		return sortedList;
	}
	
	public void run(){
		autoFeast();
		m_util.running(false);
		//m_util.feastRunning = false;
	}
}