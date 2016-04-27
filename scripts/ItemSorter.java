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

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ArrayList;

public class ItemSorter{
	
	LinkedList<ItemRef> m_refList;
	
	public ItemSorter(){
		m_refList = new LinkedList<ItemRef>();
	}
	
	public void add(int container, int itemQ){
		boolean added = false;
		ListIterator<ItemRef> li = m_refList.listIterator(0);
		
		ItemRef ir = new ItemRef(container, itemQ);
		
		while(li.hasNext() && !added){
			if(li.next().itemQ < itemQ){
				li.previous();
				li.add(ir);
				added = true;
			}
		}
		if(!added){
			m_refList.add(ir);
		}
	}
	
	public ArrayList<ItemRef> getQualityList(int start, int end){
		ArrayList<ItemRef> topQList = new ArrayList<ItemRef>();
		
		if(start < 0) start = 0;
		if(m_refList.size() < end)
			end = m_refList.size();
		if(m_refList.size() <= start)
			return topQList;
		
		for(int i = start; i < end; i++){
			if(m_refList.get(i).itemQ >= 0)
				topQList.add(m_refList.get(i) );
		}
		
		return topQList;
	}
	
	int getSmallestContainerNum(){
		int containerSmall = 1000000;
		
		for(ItemRef ir : m_refList){
			if(containerSmall > ir.container)
				containerSmall = ir.container;
		}
		return containerSmall;
	}
	
	int getLargestContainerNum(){
		int containerLarge = 0;
		
		for(ItemRef ir : m_refList){
			if(containerLarge < ir.container)
				containerLarge = ir.container;
		}
		return containerLarge;
	}
	
	public ArrayList<Integer> sortContainers(int batch, int batchSize, int batchSizeShrunk){
		ArrayList<ItemRef> QList = new ArrayList<ItemRef>();
		ArrayList<Integer> sortedContainerCount = new ArrayList<Integer>();
		
		int start = batch * batchSize;
		int end = start + ( batchSize - batchSizeShrunk);
		
		QList = getQualityList(start, end);
		
		int small = getSmallestContainerNum();
		int large = getLargestContainerNum();
		
		for(int cont = small; cont <= large; cont++){
			int itemCount = 0;
			for(ItemRef ir : QList){
				if(cont == ir.container)
					itemCount++;
			}
			
			sortedContainerCount.add(itemCount);
		}
		
		return sortedContainerCount;
	}
	
	public int sortContainersV2(int container, int batch, int batchSize, int batchSizeShrunk){
		ArrayList<ItemRef> QList = new ArrayList<ItemRef>();
		
		int start = batch * batchSize;
		int end = start + ( batchSize - batchSizeShrunk);
		
		QList = getQualityList(start, end);
		
		int itemCount = 0;
		for(ItemRef ir : QList){
			if(container == ir.container)
				itemCount++;
		}
		
		return itemCount;
	}
	
	public int getContainerSortedCount(int container, int sortSize, int skipTo, boolean highestQuality){
		ArrayList<ItemRef> QList = new ArrayList<ItemRef>();
		int start;
		int end;
		if(highestQuality){
			start = skipTo;
			end = start + sortSize;
		}else{
			end = m_refList.size() - skipTo;
			start = end - sortSize;
		}
		
		//System.out.println("start "+start);
		//System.out.println("end "+end);
		
		QList = getQualityList(start, end);
		
		int itemCount = 0;
		for(ItemRef ir : QList){
			if(container == ir.container)
				itemCount++;
		}
		
		return itemCount;
	}
	
	public void extracted(int container, int extractionCount, boolean highQ){
		while(extractionCount > 0){
			ItemRef i = null;
			
			for(ItemRef ir : m_refList){
				if(container == ir.container){
					if(i == null){
						i = ir;
					}else if(highQ && ir.itemQ > i.itemQ){
						i = ir;
					}else if(!highQ && ir.itemQ < i.itemQ){
						i = ir;
					}
				}
			}
			
			if(i != null) m_refList.remove(i);
			extractionCount--;
		}
	}
	
	public int getContainerCount(int container){
		int itemCount = 0;
		
		for(ItemRef ir : m_refList){
			if(container == ir.container)
				itemCount++;
		}
		
		return itemCount;
	}
	
	public static ItemSorter sort(ItemSorter list, int sortSize, boolean highQ){
		ItemSorter is = new ItemSorter();
		
		for(int i = 0; highQ && i < sortSize && i < list.size(); i++){
			ItemRef ir = list.get(i);
			is.add(ir.container, ir.itemQ);
		}
		
		for(int i = 0; !highQ && i < sortSize && i < list.size(); i++){
			ItemRef ir = list.get(list.size() -1 - i);
			is.add(ir.container, ir.itemQ);
		}
		
		return is;
	}
	
	public void clear(){
		m_refList.clear();
	}
	
	public ItemRef get(int i){
		return m_refList.get(i);
	}
	
	public int size(){
		return m_refList.size();
	}
	
	public class ItemRef{ //ItemSorter.ItemRef ir = new ItemSorter.ItemRef(item, container);
		public int itemQ;
		public int container;
		
		public ItemRef(int c, int q){
			container = c; itemQ = q; 
		}
	}
	
}