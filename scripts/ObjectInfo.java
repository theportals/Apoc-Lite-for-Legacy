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

import haven.Gob.Overlay;

import java.util.ArrayList;
import java.util.Iterator;

public class ObjectInfo extends Thread{
	public String scriptName = "Info";
	
	HavenUtil m_util;
	int m_option;
	String m_modify;
	
	public void ApocScript(HavenUtil util, int option, String modify){
		m_util = util;
		m_option = option;
		m_modify = modify;
	}
	
	public ArrayList<Overlay> GetOverlays(Gob g){
		ArrayList<Overlay> list = new ArrayList<Overlay>();
		for(Iterator<Overlay> i = g.ols.iterator(); i.hasNext();) {
			Overlay ol = i.next();
			list.add(ol);
		}
		
		return list;
	}
	
	public String GetType(Gob g){
		Drawable d = (Drawable)g.getattr(Drawable.class);
		ResDrawable dw = (ResDrawable)g.getattr(ResDrawable.class);
		
		if ((dw != null) && (d != null)){
			return dw.sdt.toString();
		}
		
		return "";
	}
	
	public void run(){
		if(m_util.m_ui.mainview.gobAtMouse != null){
			Gob gob = m_util.m_ui.mainview.gobAtMouse;
			Resource.Neg neg = gob.getneg();
			System.out.println("==================");
			System.out.println("Name      "+ gob.resname());
			System.out.println("Coord     "+ gob.getr());
			System.out.println("Size bs   "+ neg.bs);
			System.out.println("Offset bc "+ neg.bc);
			System.out.println("Gob SC "+ gob.sc);
			System.out.println("ID "+ gob.id);
			System.out.println("Attr: " + gob.attr);
			System.out.println("Indir: " + gob.getres().indir() );
			
			for(String name : gob.resnames() )
			System.out.println("names: "+ name );
			
			System.out.println("blob 0: "+ gob.GetBlob(0) );
			System.out.println("type: "+ GetType(gob) );
			ArrayList<Gob.Overlay> ol = GetOverlays(gob);
			System.out.println("ol size: "+ol.size() );
			
			if(ol.size() > 0){
				Gob.Overlay o = ol.get(0);
				System.out.println(Utils.int16d(o.sdt.blob, 2));
			}
		}else{
			System.out.println("Vmet indirect: "+m_util.getVmeterAmount(255, false) );
			System.out.println("Vmet direct: "+m_util.getVmeterAmount(255, true) );
			System.out.println("Tile ID: "+m_util.getTileID(m_util.m_ui.mainview.mousepos.div(11)));
			System.out.println("Tile OL: "+m_util.getTileOL(m_util.m_ui.mainview.mousepos.div(11)));
		}
		System.out.println();
		
		m_util.running(false);
	}
}