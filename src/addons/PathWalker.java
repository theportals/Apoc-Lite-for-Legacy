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
import java.awt.geom.Rectangle2D;
import java.awt.Point;
import java.awt.Color;

import haven.Coord;
import haven.Gob;
import haven.Resource;
import haven.KinInfo;
import haven.Item;
import haven.MCache;
import haven.Moving;
import haven.Following;
import haven.FlowerMenu;
import haven.ScriptDrawer;
import haven.Config;

public class PathWalker extends Thread{
	HavenUtil m_util;
	PathFinder pf;
	Gob m_gob;
	Coord m_c;
	int m_oldFrame = 0;
	ArrayList<Gob> m_memGobs = new ArrayList<Gob>();
	ArrayList<Rectangle> m_memRect = new ArrayList<Rectangle>();
	//double m_nextDist;
	//public boolean m_surface = false;
	public Gob m_surfaceGob = null;
	public int m_dropType = 1;
	public int m_modflag = 0;
	public boolean m_itemAction = false;
	public boolean m_place = false;
	public FlowerMenu m_flower;
	public int m_flowerOption;
	public String[] m_action;
	public Coord m_returnCoord;
    ArrayList<Rectangle> m_critterGobs = new ArrayList<Rectangle>();
	public boolean m_pclaims = false;
	ScriptDrawer m_drawer;
	
	ArrayList<Gob> m_ignoreGobs = null;
	
	Rectangle m_inside = null;
	
	public PathWalker(HavenUtil h){
		m_util = h;
		pf = new PathFinder(m_util);
		//m_nextDist = nextDist;
	}
	
	public PathWalker(HavenUtil h, Gob g){
		if(g == null){
			Thread.dumpStack();
			m_util.stop = true;
			m_util.pathing = false;
		}
		m_util = h;
		pf = new PathFinder(m_util);
		m_gob = g;
		m_c = null;
	}
	
	public PathWalker(HavenUtil h, Coord c){
		m_util = h;
		pf = new PathFinder(m_util);
		m_gob = null;
		m_c = c;
	}
	
	public void run(){
		if(m_gob != null){
			//to(m_gob);
			toSurface(m_gob.getr(), m_gob);
		}else if(m_c != null){
			if(m_surfaceGob != null ) toSurface(m_c, m_surfaceGob);
			else to(m_c);
		}else{
			m_util.PFrunning = false;
			m_util.update();
		}
	}
	
	public void stopPF(){
		//m_util.publicLineBoolean = false;
		removePathfindDrawer();
		m_util.stop = true;
		m_util.pathing = false;
	}
	
	public void setFlower(FlowerMenu f, int o){
		m_flower = f;
		m_flowerOption = o;
	}
	
	void to(Coord c){
		m_util.update();
		Point end = new Point(c.x, c.y);
		boolean rePath = true;
		int breakCount = 0;
		Coord breakCoord = new Coord();
		
		boolean waterPath = boatTest();
		
		//System.out.println("waterPath "+waterPath);
		ArrayList<Rectangle> rects = new ArrayList<Rectangle>();
		while(/*rePath && */m_util.pathing){
			ArrayList<Point> path = new ArrayList<Point>();
			ArrayList<Coord> pathCoord = new ArrayList<Coord>();
            ArrayList<Rectangle> allRect = new ArrayList<Rectangle>();
			//rePath = false;
			Coord p = m_util.getPlayerCoord();
			Point start = new Point(p.x, p.y);
			
			getAllNegs(rects, null, start, end, waterPath, rePath);
            allRect.addAll(rects);
            allRect.addAll(m_critterGobs);
			if(m_util.pathing && m_inside == null && getInside(allRect, start, waterPath)) continue;
			if(m_util.pathing && m_inside != null) start = insideFix(p, path, allRect, waterPath);
			if(m_util.pathing) path = pf.pathFind(allRect, start, end);
			if(m_util.pathing && m_inside != null) path.add(0, start);
			if(path != null && m_util.pathing){
				pathCoord = coordConverter(path);
				showPath(pathCoord, allRect);
				//while(m_util.pathing) m_util.wait(10);
				int ignoreLast = 0;
				if(m_action != null){
					m_util.clickWorld(3, m_util.getPlayerCoord() );
					ignoreLast = 1;
				}else if(m_itemAction){
					ignoreLast = 1;
				}
				rePath = walkPath(pathCoord, waterPath, ignoreLast);
				
				if(m_action != null && m_util.pathing && !rePath){
					m_util.m_ui.mnu.wdgmsg("act", (Object[])m_action);
					m_util.clickWorld(1, c);
				}else if(m_itemAction){
					m_util.m_ui.mainview.wdgmsg("itemact", Coord.z.add(200,200), c, m_modflag);
				}
			}
			if(rePath){
				clear();
				if(breakCoord.equals(m_util.getPlayerCoord()) )
					breakCount++;
				else
					breakCount = 0;
				
				if(breakCount > 4) break;
				breakCoord = m_util.getPlayerCoord();
			}else{
				break;
			}
		}
		
		//m_util.publicLineBoolean = false;
		removePathfindDrawer();
		m_util.PFrunning = false;
		m_util.update();
	}
	
	void to(Gob g){
		if(g != null){
			m_util.update();
			Coord c = g.getc();
			Point end = new Point(c.x, c.y);
			int ignoreLast = 1;
			
			boolean rePath = false;
			int breakCount = 0;
			Coord breakCoord = new Coord();
			
			boolean waterPath = boatTest();
			
			//System.out.println("waterPath "+waterPath);
			ArrayList<Rectangle> rects = new ArrayList<Rectangle>();
            
			while(/*rePath && */m_util.pathing){
				ArrayList<Point> path = new ArrayList<Point>();
				ArrayList<Coord> pathCoord = new ArrayList<Coord>();
                ArrayList<Rectangle> allRect = new ArrayList<Rectangle>();
                
				//rePath = false;
				Coord p = m_util.getPlayerCoord();
				Point start = new Point(p.x, p.y);
				
				if(doorEdit(end, g)){
					getAllNegs(rects, null, start, end, waterPath, rePath);
				}else{
					if(innEdit(allRect, g, end) ) ignoreLast = 0;
					//System.out.println("ignoreLast "+ignoreLast);
					getAllNegs(rects, g, start, end, waterPath, rePath);
				}
                allRect.addAll(rects);
                allRect.addAll(m_critterGobs);
                
				if(m_util.pathing && m_inside == null && getInside(allRect, start, waterPath)) continue;
				if(m_util.pathing && m_inside != null) start = insideFix(p, path, allRect, waterPath);
				if(m_util.pathing) path = pf.pathFind(allRect, start, end);
				if(m_util.pathing && m_inside != null) path.add(0, start);
				
				if(path != null && m_util.pathing){
					pathCoord = coordConverter(path);
					showPath(pathCoord, allRect);
					
					if(m_action != null) m_util.clickWorld(3, m_util.getPlayerCoord() );
					
					rePath = walkPath(pathCoord, waterPath, ignoreLast);
					if(m_util.pathing && !rePath){
						if(m_action != null){
							m_util.m_ui.mnu.wdgmsg("act", (Object[])m_action);
							m_util.clickWorldObject(1, g);
						}else if(m_itemAction){
							m_util.m_ui.mainview.wdgmsg("itemact", Coord.z.add(200,200), g.getr(), m_modflag, g.id, g.getr());
						}else if(g.resname().contains("gfx/terobjs/herbs/") ){
							//ArrayList<Coord> herb = new ArrayList<Coord>();
							//herb.add(pathCoord.get(pathCoord.size()-1) );
							m_util.clickWorld(1, c);
							m_util.wait(100);
							if(!m_util.flowerMenuReady() ) m_util.clickWorldObject(3, g);
							//rePath = walkPath(herb, 0);
							if(!rePath){
								while(!m_util.flowerMenuReady() && m_util.pathing) m_util.wait(100);
								if(m_util.pathing) m_util.flowerMenuSelect("Pick");
								gobRemovePause(g);
							}
						}else if(m_flower != null){
							m_flower.wdgmsg("cl", m_flowerOption);
						}else{
							m_util.clickWorld(1, c);
							m_util.clickWorldObject(3, g);
						}
					}
				}
				
				if(rePath){
					clear();
					if(breakCoord.equals(m_util.getPlayerCoord()) )
						breakCount++;
					else
						breakCount = 0;
					
					if(breakCount > 4) break;
					breakCoord = m_util.getPlayerCoord();
				}else{
					break;
				}
			}
		}
		
		//m_util.publicLineBoolean = false;
		removePathfindDrawer();
		m_util.PFrunning = false;
		m_util.update();
	}
	
	public void gobRemovePause(Gob g){
		while(m_util.pathing){
			m_util.wait(100);
			
			if(!m_util.findObject(g) ) return;
		}
	}
	
	void toSurface(Coord c, Gob g){
		//c = c.div(11).mul(11).add(5,5);
		
		/*Gob g = null;
		for(Gob all : m_util.allGobs() ){
			if(all.getc().equals(m_util.getPlayerCoord() ) && all.id != m_util.getPlayerGob().id ){
				g = all;
				break;
			}
		}*/
		
		m_returnCoord = null;
		
		if(g != null/* && m_util.boxFree(c, g) */){
			m_util.update();
			//Coord c = g.getc();
			Point end = new Point(c.x, c.y);
			int ignoreLast = 0;
			
			boolean rePath = false;
			int breakCount = 0;
			Coord breakCoord = new Coord();
			
			boolean waterPath = boatTest();
			
			//System.out.println("waterPath "+waterPath);
			ArrayList<Rectangle> rects = new ArrayList<Rectangle>();
            
			while(/*rePath && */m_util.pathing){
				ArrayList<Point> path = new ArrayList<Point>();
				ArrayList<Coord> pathCoord = new ArrayList<Coord>();
                ArrayList<Rectangle> allRect = new ArrayList<Rectangle>();
                
				//rePath = false;
				Coord p = m_util.getPlayerCoord();
				Point start = new Point(p.x, p.y);
				
				getAllNegs(rects, null, start, end, waterPath, rePath);
                allRect.addAll(rects);
                allRect.addAll(m_critterGobs);
                
				if(m_util.pathing && m_inside == null && getInside(allRect, start, waterPath)) continue;
				if(m_util.pathing && m_inside != null) start = insideFix(p, path, allRect, waterPath);
				if(m_util.pathing) path = pf.pathFind(allRect, start, end);
				if(m_util.pathing) path = addSurface(path, g, c, allRect);
				if(m_util.pathing && m_inside != null) path.add(0, start);
				
				if(path != null && m_util.pathing){
					pathCoord = coordConverter(path);
					showPath(pathCoord, allRect);
					rePath = walkPath(pathCoord, waterPath, ignoreLast);
					if(m_util.pathing && !rePath){
						/*if(m_dropType == 1)
							m_util.clickWorld(3, c);
						else if(m_dropType == 2)
							m_util.m_ui.mainview.wdgmsg("place", c, 1, 0);*/
						/*if(g.resname().contains("gfx/terobjs/herbs/") ){
							//ArrayList<Coord> herb = new ArrayList<Coord>();
							//herb.add(pathCoord.get(pathCoord.size()-1) );
							m_util.clickWorld(1, c);
							m_util.wait(100);
							if(!m_util.flowerMenuReady() ) m_util.clickWorldObject(3, g);
							//rePath = walkPath(herb, 0);
							if(!rePath){
								while(!m_util.flowerMenuReady() && m_util.pathing) m_util.wait(100);
								if(m_util.pathing) m_util.flowerMenuSelect("Pick");
								m_util.gobRemovePause(g);
							}
						}else{
							m_util.clickWorld(1, c);
							m_util.clickWorldObject(3, g);
						}*/
						
						if(m_action != null){
							m_util.m_ui.mnu.wdgmsg("act", (Object[])m_action);
							m_util.clickWorldObject(1, g);
						}else if(m_itemAction){
							m_util.m_ui.mainview.wdgmsg("itemact", Coord.z.add(200,200), g.getr(), m_modflag, g.id, g.getr());
						}else if(g.resname().contains("gfx/terobjs/herbs/") ){
							//ArrayList<Coord> herb = new ArrayList<Coord>();
							//herb.add(pathCoord.get(pathCoord.size()-1) );
							m_util.clickWorld(1, c);
							m_util.wait(100);
							if(!m_util.flowerMenuReady() ) m_util.clickWorldObject(3, g);
							//rePath = walkPath(herb, 0);
							if(!rePath){
								while(!m_util.flowerMenuReady() && m_util.pathing) m_util.wait(100);
								if(m_util.pathing) m_util.flowerMenuSelect("Pick");
								gobRemovePause(g);
							}
						}else if(m_flower != null){
							m_flower.wdgmsg("cl", m_flowerOption);
						}else if(m_place){
							m_util.m_ui.mainview.wdgmsg("place", c, 1, 0);
						}else{
							m_util.clickWorld(1, c);
							m_util.clickWorldObject(3, g);
						}
					}
				}
				
				if(rePath){
					clear();
					if(breakCoord.equals(m_util.getPlayerCoord()) )
						breakCount++;
					else
						breakCount = 0;
					
					if(breakCount > 4) break;
					breakCoord = m_util.getPlayerCoord();
				}else{
					if(pathCoord.size() > 0)
						m_returnCoord = pathCoord.get(pathCoord.size() - 1);
					break;
				}
			}
		}
		
		//m_util.publicLineBoolean = false;
		removePathfindDrawer();
		m_util.PFrunning = false;
		m_util.update();
	}
	
	boolean getInside(ArrayList<Rectangle> allRect, Point start, boolean boatTravel){
		Point end = new Point();
		
		if(tileBlockTest(start, boatTravel) ){
			m_util.wait(200);
			return true;
		}
		
		for(Rectangle r : allRect){
			double delta = 0.1;
		
			Rectangle2D shrunk = new Rectangle2D.Double(
				(double)r.x + (double)delta,
				(double)r.y + (double)delta,
				(double)r.width - (double)delta*2,
				(double)r.height - (double)delta*2
			);
			
			if(shrunk.contains(start)){
				m_inside = r;
				break;
			}
		}
		
		return false;
	}
	
	Point insideFix(Coord p, ArrayList<Point> path, ArrayList<Rectangle> allRect, boolean boatTravel){
		ArrayList<Point> plist = surface(m_inside, null, allRect, boatTravel, false);
		ArrayList<Coord> pcList = coordConverter(plist);
		
		Coord surf = null;
		double dist = 0;
		
		for(Coord c : pcList){
			if(surf == null){
				surf = c;
				dist = p.dist(c);
			}else if(p.dist(c) < dist){
				surf = c;
				dist = p.dist(c);
			}
		}
		
		//System.out.println("1 " + plist.size() + " " + pcList.size() + " " + m_inside.resname() + " " + m_util.gobToNegRect(m_inside) );
		if(surf != null){
			Point surfPoint = new Point(surf.x, surf.y);
			//path.add(0, surfPoint);
			return surfPoint;
		}
		
		return new Point(p.x, p.y);
	}
	
	ArrayList<Point> surfaceGob(Rectangle rect, Coord c, ArrayList<Rectangle> allRect, boolean boatTravel, boolean lineCheck){
		ArrayList<Point> surfacePoints = new ArrayList<Point>();
		int meOffcet = -2;
		int meSize = 4;
		double delta = 0.1;
		
		if(boatTravel){
			meOffcet = -14;
			meSize = 26;
		}
		
		Coord offcet = new Coord();
		Coord size = new Coord();
		
		offcet.x = rect.x;
		offcet.y = rect.y;
		size.x = rect.width;
		size.y = rect.height;
		
		offcet = offcet.add(meOffcet,meOffcet);
		size = size.add(meSize,meSize);
		
		for(int i = 0; i <= size.x; i++){
			for(int j = 0; j <= size.y; j++){
				if(i == 0 || i == size.x || j == 0 || j == size.y ){
					//Point p = new Point(g.getc().x + offcet.x + i, g.getc().y + offcet.y + j);
					Point p = new Point(c.x + offcet.x + i, c.y + offcet.y + j);
					boolean blocked = false;
					
					for(Rectangle r : allRect){
						Rectangle2D shrunk = new Rectangle2D.Double(
							(double)r.x + (double)delta,
							(double)r.y + (double)delta,
							(double)r.width - (double)delta*2,
							(double)r.height - (double)delta*2
						);
						
						if(shrunk.contains((double)p.x, (double)p.y) ){
							blocked = true;
							break;
						}else if(tileBlockTest(p, boatTravel) ){
							blocked = true;
							break;
						}
					}
					
					if(!blocked)
						if(!lineCheck || m_util.freePath(new Coord(p.x, p.y), c, false) )
							surfacePoints.add(p);
				}
			}
		}
		
		return surfacePoints;
	}
	
	ArrayList<Point> addSurface(ArrayList<Point> path, Gob g, Coord c, ArrayList<Rectangle> allRect){
		ArrayList<Point> ignore = new ArrayList<Point>();
		ArrayList<Point> plist = surfaceGob(m_util.gobToNegRect(g), c, allRect, false, true);
		Point last;
		boolean lower = false;
		
		if(path.size() >= 3){
			last = path.get(path.size() - 3);
		}else{
			Coord st = m_util.getPlayerCoord();
			last = new Point(st.x, st.y);
			path.clear();
			lower = true;
		}
		
		for(int surf = 0; surf < plist.size(); surf++){
			Point to = null;
			double dist = 0;
			
			for(Point p : plist){
				if(to == null && !ignore.contains(p)){
					to = p;
					dist = dist(p, last);
				}else if(dist > dist(p, last) && !ignore.contains(p)){
					to = p;
					dist = dist(last, p);
				}
			}
			
			/*m_util.publicPoints.clear();
			m_util.publicPoints = coordConverter(surfacePoints);
			
			m_util.publicLineBoolean = true;*/
			
			ignore.add(to);
			ArrayList<Point> surfacePath = null;
			if(to != null){
				surfacePath = pf.pathFind(allRect, last, to);
			}
			
			//System.out.println(surfacePath);
			
			if(surfacePath != null){
				if(!lower){
					path.remove(path.size() - 1);
					path.remove(path.size() - 1);
				}
				
				path.addAll(surfacePath);
				//path.add(new Point(1,1));
				return path;
			}
		}
		
		return path;
	}
	
	double dist(Point from, Point to){
		long dx = from.x - to.x;
		long dy = from.y - to.y;
		return(Math.sqrt((dx * dx) + (dy * dy)));
	}
	
	ArrayList<Point> surface(Rectangle rect, Coord c, ArrayList<Rectangle> allRect, boolean boatTravel, boolean lineCheck){
		ArrayList<Point> surfacePoints = new ArrayList<Point>();
		double delta = 0.1;
		
		/*int meOffcet = -2;
		int meSize = 4;
		
		if(boatTravel){
			meOffcet = -14;
			meSize = 26;
		}*/
		
		Coord offcet = new Coord();
		Coord size = new Coord();
		
		offcet.x = rect.x;
		offcet.y = rect.y;
		size.x = rect.width;
		size.y = rect.height;
		
		/*offcet = offcet.add(meOffcet,meOffcet);
		size = size.add(meSize,meSize);*/
		
		for(int i = 0; i <= size.x; i++){
			for(int j = 0; j <= size.y; j++){
				if(i == 0 || i == size.x || j == 0 || j == size.y ){
					//Point p = new Point(g.getc().x + offcet.x + i, g.getc().y + offcet.y + j);
					//Point p = new Point(c.x + offcet.x + i, c.y + offcet.y + j);
					Point p = new Point(offcet.x + i, offcet.y + j);
					boolean blocked = false;
					
					for(Rectangle r : allRect){
						Rectangle2D shrunk = new Rectangle2D.Double(
							(double)r.x + (double)delta,
							(double)r.y + (double)delta,
							(double)r.width - (double)delta*2,
							(double)r.height - (double)delta*2
						);
						
						if(shrunk.contains((double)p.x, (double)p.y) ){
							blocked = true;
							break;
						}else if(tileBlockTest(p, boatTravel) ){
							blocked = true;
							break;
						}
					}
					
					if(!blocked)
						if(!lineCheck || m_util.freePath(new Coord(p.x, p.y), c, false) )
							surfacePoints.add(p);
				}
			}
		}
		
		return surfacePoints;
	}
	
	boolean tileBlockTest(Point p, boolean waterPath){
		int test = m_util.getTileID(new Coord(p.x, p.y).div(11));
		if(waterPath && test < 2) return false;
		else if(!waterPath && test < 255 && test > 0) return false;
		
		return true;
	}
	
	boolean boatTest(){
		Gob boat = m_util.findClosestObject("boat");
		Gob player = m_util.getPlayerGob();
		if(boat == null) return false;
		return (m_util.checkPlayerSitting() && player.getc().equals(boat.getc()) );
	}
	
	public void clear(){
        m_critterGobs.clear();
		m_inside = null;
		pf.clearCash();
	}
	
	boolean walkPath(ArrayList<Coord> pathCoord, boolean waterPath, int ignoreLast){
		Gob boat = null;
		if(waterPath) boat = m_util.findClosestObject("boat");
		for(int i = 0; i < (pathCoord.size() - ignoreLast); i++){
			Coord c = pathCoord.get(i);
			Coord next = null;
			
			if(i+1 < pathCoord.size()-1)
				 next = pathCoord.get(i+1);
			
			if(waterPath){
				if(!goToWaterCoord(c, boat) ) return true; // repath on true
			}else{
				//if(!goToCombatCoord(c, next)) return true; // repath on true
				//if(!goToWorldCoord(c, next) ) return true; // repath on true
				if(!goToWorldCoordRC(c, next) ) return true; // repath on true
			}
			if(!m_util.pathing) return false;
		}
		return false;
	}
	
	void showPath(ArrayList<Coord> pathCoord, ArrayList<Rectangle> rects){
		m_drawer = addPathfindDrawer();
		if(Config.pathfinderRectangles){
			for(Rectangle r : rects){
				m_drawer.addBox(new Coord(r.x, r.y), new Coord(r.x + r.width, r.y + r.height), 2, Color.BLUE);
			}
		}
		if(Config.pathfinderLine){
			ArrayList<Coord> draw = new ArrayList<Coord>(pathCoord);
			draw.add(0, m_util.getPlayerCoord() );
			Coord f = null;
			for(Coord c : draw){
				if(f == null){
					f = new Coord(c);
					continue;
				}
				m_drawer.addLine(c, f, 2, Color.RED);
				f = new Coord(c);
			}
		}
	}
	
	ArrayList<Coord> coordConverter(ArrayList<Point> path){
		ArrayList<Coord> coords = new ArrayList<Coord>();
		
		for(Point p: path)
			coords.add(new Coord(p.x, p.y) );
		
		return coords;
	}
	
	boolean ignoreGob(Gob g){
		if(m_ignoreGobs == null) return false;
		
		for(Gob ig : m_ignoreGobs){
			if(ig.id == g.id) return true;
		}
		
		return false;
	}
	
	void getAllNegs(ArrayList<Rectangle> allRect, Gob target, Point start, Point end, boolean waterPath, boolean repath){
		ArrayList<Gob> cubWallSigns = new ArrayList<Gob>();
		long time = System.currentTimeMillis();
		int meOffcet = -2;
		int meSize = 4;
		m_inside = null;
		
		if(waterPath){
			meOffcet = -14;
			meSize = 26;
		}
		
		Gob player = m_util.getPlayerGob();
		ArrayList<Rectangle> negRec = new ArrayList<Rectangle>();
		synchronized(m_util.m_ui.mainview.glob.oc){
			for(Gob g : m_util.m_ui.mainview.glob.oc){
				String name = g.resname();
				
				if(ignoreGob(g) ) continue;
				
				if(repath && m_memGobs.contains(g) ) continue;
				
				Moving m = g.getattr(Moving.class);
				if(m != null && m instanceof Following) continue;
				
				if( g == target ) continue;
				if( g == player ) continue;
				if( g.getc().equals(player.getc() ) ) continue;
				//if( name.contains("/tiles/") ) continue;
				if( name.contains("/plants/") ) continue;
				if( name.contains("/items/") ) continue;
				if( name.equals("gfx/terobjs/trees/log") ) continue;
				if( name.equals("gfx/terobjs/blood") ) continue;
				//if( name.equals("gfx/terobjs/herbs/chantrelle") ) continue;
				//if( name.equals("gfx/terobjs/anthill-r") ) continue;
				if( (name.equals("gfx/terobjs/hearth-play") && g.getattr(KinInfo.class) == null) ) continue;
				if( (name.contains("/gates/") && g.GetBlob(0) == 2) ) continue;
				
				if(name.contains("gfx/kritter/") || name.contains("gfx/borka/s") ){
					Rectangle r = getRect(g, meOffcet, meSize);
					if(r != null && deltaFix(r, g, start, end) ) m_critterGobs.add(r);
					
					continue;
				}else{
					m_memGobs.add(g);
				}
				
				if( cubWallSignTest(name) ){
					Rectangle rect = new Rectangle(g.getc().x + -5 + meOffcet, 
													g.getc().y + -5 + meOffcet, 
													10 + meSize, 
													10 + meSize);
					if(deltaFix(rect, g, start, end))
						cubWallSigns.add(g);
					
					continue;
				}
				
				Rectangle r = getRect(g, meOffcet, meSize);
				if(r != null && deltaFix(r, g, start, end) ) allRect.add(r);
				
				if(!m_util.pathing) return;
			}
		}
		
		//int rad = 75;
		//ArrayList<Coord> tiles = m_util.getTilesInRegion(player.getc().sub(rad*11,rad*11), player.getc().add(rad*11,rad*11) , 0);
		//ArrayList<Coord> filterdTiles = firstFilter(tiles, waterPath);
		ArrayList<Coord> filterdTiles = firstFilter2(player.getc(), waterPath);
		ArrayList<Rectangle> secondFilter = mergeFilterdTiles(filterdTiles, true, waterPath);
		
		ArrayList<Coord> filterdGobs = firstFilterGobs(cubWallSigns, waterPath);
		ArrayList<Rectangle> thirdFilter = mergeFilterdTiles(filterdGobs, false, waterPath);
		secondFilter.addAll(thirdFilter);
		
		ArrayList<Rectangle> tilesAdded = new ArrayList<Rectangle>();
		/*for(Rectangle r : secondFilter){
			boolean add = true;
			
			if(repath){
				for(Rectangle test : m_memRect){
					if(test.getLocation().equals(r.getLocation() ) ){
						add = false; 
						break;
					}
				}
			}
			
			if(add){
				m_memRect.add(r);
				tilesAdded.add(r);
			}
		}*/
		
		if(m_pclaims) allRect.addAll( m_util.getAllClaimRectangles(waterPath, false) );
		
		allRect.addAll(secondFilter);
		
		//System.out.println("Neg compleate. " + (System.currentTimeMillis() - time) );
		//return negRec;
	}
	
	Rectangle getRect(Gob g, int meOffcet, int meSize){
		Coord offcet = new Coord();
		Coord size = new Coord();
		Rectangle rect = null;
		
		if(!kritterFix(g, offcet, size) ){
			Resource.Neg neg = g.getneg();
			if(neg == null){
				return null;
			}
			
			offcet = neg.bc;
			size = neg.bs;
		}
		
		if(size.x != 0){
			rect = new Rectangle(g.getc().x + offcet.x + meOffcet, 
											g.getc().y + offcet.y + meOffcet, 
											size.x + meSize, 
											size.y + meSize);
		}
		
		return rect;
	}
	
	boolean deltaFix(Rectangle r, Gob g, Point start, Point end){
		double delta = 0.1;
		
		Rectangle2D shrunk = new Rectangle2D.Double(
			(double)r.x + (double)delta,
			(double)r.y + (double)delta,
			(double)r.width - (double)delta*2,
			(double)r.height - (double)delta*2
		);
		
		/*if(shrunk.contains(start) ){
			System.out.println("inside found");
			m_inside = r;
		}*/
		
		if(shrunk.contains(end)){
			return false;
		}
		
		return true;
	}
	
	boolean cubWallSignTest(String name){
		if( name.contains("gfx/arch/walls/") ){
			return true;
		}else if( name.contains("gfx/arch/sign") ){
			return true;
		}else if( name.contains("gfx/terobjs/cupboard") ){
			return true;
		}else if( name.contains("gfx/terobjs/cheeserack") ){
			return true;
		}
		
		return false;
	}
	
	boolean goToWorldCoord(Coord c, Coord next){
		Gob player = m_util.getPlayerGob();
		Coord mc = new Coord(m_util.getPlayerCoord());
		//boolean redo = true;
		//long time = System.currentTimeMillis();
		
		if(!mc.equals(c)){
			m_util.clickWorld(1, c);
			
			int reclick = 0;
			int miniCount = 0;
			/*while(m_util.getPlayerCoord().equals(mc) && !m_util.getPlayerCoord().equals(c) && m_util.pathing){
				//while(m_util.pathing && miniCount < 20){m_util.wait(10); miniCount++; }
				m_util.wait(10);
				if(reclick > 200){
					m_util.clickWorld(1, c);
					reclick = 0;
				}
				reclick++;
			}*/
			int count = 0;
			int redoCount = 0;
			miniCount = 0;
			
			boolean frameError = false;
			int frameErrorCount = 0;
			int errorClick = 101;
			
			//System.out.println("moving time. " + (System.currentTimeMillis() - time) );
			//time = System.currentTimeMillis();
			while((m_util.checkPlayerWalking() || frameError || !m_util.getPlayerCoord().equals(c)) && m_util.pathing){
				//while(m_util.pathing && miniCount < 5){m_util.wait(10); miniCount++; }
				m_util.wait(10);
				
				//////// unstable test code
				/*
				if(next != null) if(!frameError && c.dist(next) > 10 && c.dist(m_util.getPlayerCoord() ) <= 10 ) break;
				
				if(frameError && m_util.getPlayerCoord().equals(c)){
					int countPause = 0;
					while((frameError && m_util.getPlayerCoord().equals(c)) && m_util.pathing) m_util.wait(10); 
					while(countPause < 5 && m_util.pathing){ m_util.wait(10); countPause++; }
				}
				
				if(m_oldFrame == player.frame){
					frameErrorCount++;
					if(frameErrorCount > 11){
						if(errorClick > 100){
							m_util.clickWorld(1, c);
							errorClick = 0;
						}
						errorClick++;
						frameError = true;
					}
				}else{
					errorClick = 101;
					frameError = false;
					frameErrorCount = 0;
				}
				m_oldFrame = player.frame;
				*/
				//////////
				
				if(!m_util.checkPlayerWalking() && !m_util.getPlayerCoord().equals(c)){
					redoCount++;
					if(redoCount > 30){
						//redo = true;
						//System.out.println("Error Path");
						return false;
					}
				}else{
					redoCount = 0;
				}
			}
			
			//System.out.println("walk time. " + (System.currentTimeMillis() - time) );
		}
		return true;
	}
	
	boolean goToWorldCoordRC(Coord c, Coord next){
		Gob player = m_util.getPlayerGob();
		
		if(!player.getr().equals(c)){
			m_util.clickWorld(1, c);
			
			int redoCount = 0;
			while((m_util.checkPlayerWalking() || !m_util.getPlayerCoord().equals(c)) && m_util.pathing){
				m_util.wait(10);
				
				if(/*!player.miniwalk &&*/ !m_util.checkPlayerWalking() && !m_util.getPlayerCoord().equals(c)){
					redoCount++;
					if(redoCount > 30){
						return false;
					}
				}else{
					redoCount = 0;
				}
			}
		}
		return true;
	}
	
	/*boolean goToCombatCoord(Coord c, Coord next){
		System.out.println("combat pathing");
		long time = System.currentTimeMillis();
		
		Gob player = m_util.getPlayerGob();
		Coord mc = new Coord(m_util.getPlayerCoord());
		//boolean redo = true;
		if(!mc.equals(c)){
			m_util.clickWorld(1, c);
			
			int reclick = 0;
			int miniCount = 0;
			while(m_util.getPlayerCoord().equals(mc) && !m_util.getPlayerCoord().equals(c) && m_util.pathing){
				//while(m_util.pathing && miniCount < 20){m_util.wait(10); miniCount++; }
				m_util.wait(10);
				if(reclick > 200){
					m_util.clickWorld(1, c);
					reclick = 0;
				}
				reclick++;
			}
			int count = 0;
			int redoCount = 0;
			miniCount = 0;
			boolean frameError = false;
			int frameErrorCount = 0;
			int errorClick = 101;
			double split = m_nextDist;
			
			if(next != null) if(next.dist(c) < m_nextDist){ split = next.dist(c)/2; System.out.println("split " + split); }
			
			System.out.println("walk time. " + (System.currentTimeMillis() - time) );
			while((	frameError || !m_util.getPlayerCoord().equals(c)) && m_util.pathing){
				//while(m_util.pathing && miniCount < 5){m_util.wait(10); miniCount++; }
				m_util.wait(10);
				
				drinkWalker();
				
				if(!m_util.checkPlayerWalking() && m_util.getPlayerCoord().equals(c)) return true;
				
				if(m_util.getPlayerCoord().dist(c) < split)
					if(!frameError) return true;
				//if(!frameError) m_util.clickWorld(1, next);
				
				if(m_oldFrame == player.frame){
					frameErrorCount++;
					if(frameErrorCount > 15){
						if(errorClick > 100){
							m_util.clickWorld(1, c);
							errorClick = 0;
						}
						errorClick++;
						frameError = true;
					}
				}else{
					errorClick = 101;
					frameError = false;
					frameErrorCount = 0;
				}
				m_oldFrame = player.frame;
				
				if(!m_util.checkPlayerWalking() && !m_util.getPlayerCoord().equals(c)){
					redoCount++;
					if(redoCount > 50){
						//redo = true;
						System.out.println("Error Path");
						return false;
					}
				}else{
					redoCount = 0;
				}
			}
		}
		return true;
	}*/
	
	int m_drinkCount = 200;
	
	/*void drinkWalker(Coord c){
		if(!m_util.pathDrinker) return;
		if(!m_util.checkPlayerWalking() && !m_util.getPlayerCoord().equals(c)) return;
		if(!m_util.isInventoryOpen()) return;
		
		Item flask = m_util.findFlask();
		if(flask.olcol == null) return;
		//Item flask = m_util.setupFlask();
		
		if(m_util.waterFlaskInfo(flask) < 0.1 && m_util.pathing ) m_util.fillFlask(flask);
		
		if(!m_util.hasHourglass() && m_util.getStamina() < 80 && m_util.pathing && m_drinkCount > 20){
			m_util.useActionBar();
			//System.out.println("drink");
		}
		
		if(m_util.hasHourglass() )
			m_drinkCount = 0;
		else
			m_drinkCount++;
	}*/
	
	boolean goToWaterCoord(Coord c, Gob boat){
		Gob player = m_util.getPlayerGob();
		
		Coord mc = new Coord(m_util.getPlayerCoord());
		//boolean redo = true;
		if(!mc.equals(c)){
			//boat.miniwalk = true;
			m_util.clickWorld(1, c);
			
			int reclick = 0;
			int miniCount = 0;
			/*while(m_util.getPlayerCoord().equals(mc) && !m_util.getPlayerCoord().equals(c) && m_util.pathing){
				//while(m_util.pathing && miniCount < 20){m_util.wait(10); miniCount++; }
				m_util.wait(10);
				if(reclick > 200){
					m_util.clickWorld(1, c);
					reclick = 0;
				}
				reclick++;
			}*/
			int count = 0;
			int redoCount = 0;
			miniCount = 0;
			
			while(( boat.GetBlob(0) == 1 || !m_util.getPlayerCoord().equals(c)) && m_util.pathing){
				//while(m_util.pathing && miniCount < 5){m_util.wait(10); miniCount++; }
				m_util.wait(10);
				if(/*!boat.miniwalk &&*/ boat.GetBlob(0) == 0 && !m_util.getPlayerCoord().equals(c)){
					redoCount++;
					if(redoCount > 50){
						//redo = true;
						//System.out.println("Error Path");
						return false;
					}
				}else{
					redoCount = 0;
				}
			}
		}
		return true;
	}
	
	boolean doorEdit(Point end, Gob g){
		if(g.resname().equals("gfx/arch/cabin-door2m") ){
			if(!playerIndoors() ){
				end.x = end.x + 1;
				end.y = end.y + 7;
				return true;
			}else{
				end.x = end.x - 3;
				end.y = end.y - 7;
				return true;
			}
		}
		if(g.resname().equals("gfx/arch/cabin-door2") ){
			if(!playerIndoors() ){
				end.x = end.x + 7;
				end.y = end.y + 1;
				return true;
			}else{
				end.x = end.x - 7;
				end.y = end.y - 3;
				return true;
			}
		}
		if(g.resname().equals("gfx/terobjs/ridges/cavein-n") ){
			end.x = end.x + 5;
			end.y = end.y + 16;
			return true;
		}
		if(g.resname().equals("gfx/terobjs/ridges/caveout-n") ){
			end.x = end.x;
			end.y = end.y - 11;
			return true;
		}
		if(g.resname().equals("gfx/terobjs/ridges/caveout-w") ){
			/*end.x = end.x;
			end.y = end.y - 11;
			return true;*/
		}
		
		return false;
	}
	
	boolean innEdit(ArrayList<Rectangle> allRect, Gob g, Point end){
		if(g.resname().equals("gfx/arch/door-inn") ){
			Coord offcet = new Coord();
			Coord size = new Coord();
			
			Resource.Neg neg = g.getneg();
			if(neg == null){
				//System.out.println("Error neg"); 
				return false;
			}
			
			offcet = neg.bc;
			size = neg.bs;
			
			Rectangle rect = new Rectangle(g.getc().x + offcet.x + -2, 
													g.getc().y + offcet.y + -2, 
													size.x + 4, 
													size.y + 4);
			allRect.add(rect);
			
			if(!playerIndoors() ){
				end.x = g.getc().x + 1;
				end.y = g.getc().y + 7;
			}else{
				end.x = g.getc().x - 2;
				end.y = g.getc().y - 7;
			}
			
			return true;
		}
		
		return false;
	}
	
	boolean playerIndoors(){
		while(m_util.pathing){
			int id = m_util.getTileID(m_util.getPlayerCoord().div(11) );
			
			if(id == -1){
				m_util.wait(100);
				continue;
			}
			
			if(id == 22 || id == 21){
				return true;
			}else{
				break;
			}
		}
		
		return false;
	}
	
	ArrayList<Coord> firstFilter(ArrayList<Coord> tiles, boolean waterPath){
		//System.out.println("Tile size. " + tiles.size() );
		
		ArrayList<Coord> filter = new ArrayList<Coord>();
		for(Coord c : tiles){
			int test = m_util.getTileID(c);
			if(waterPath && test < 2) continue;
			else if(!waterPath && test < 255 && test > 0) continue;
			//if(!filter.contains(c) ) continue;
			
			int a = -1;
			int b = -1;
			for(int i = 0; i < 8; i++){
				int id = m_util.getTileID(c.add(a,b) );
				
				if(waterPath && id < 2){
					filter.add(c);
					break;
				}else if(!waterPath && id < 255 && id > 0){
					filter.add(c);
					break;
				}
				
				a++;
				if(a == 0 && b == 0) a++;
				if(a > 1){ b++; a = -1;}
			}
		}
		
		return filter;
	}
	
	ArrayList<Coord> firstFilter2(Coord player, boolean waterPath){
		//System.out.println("Tile size. " + tiles.size() );
		int[][] tileList = new int[300][300];
		ArrayList<Coord> filter = new ArrayList<Coord>();
		
		Coord gc = player.div(1100).add(-1,-1);
		
		for(int My = 0; My < 3; My++){
			for(int Mx = 0; Mx < 3; Mx++){
				MCache.Grid gd = m_util.m_ui.mainview.map.grids.get(gc.add(Mx,My) );
				
				for(int i = 0; i < 100; i++){
					for(int j = 0; j < 100; j++){
						if(gd != null){
							tileList[j+(Mx*100)][i+(My*100)] = gd.tiles[j][i];
						}else{
							tileList[j+(Mx*100)][i+(My*100)] = -1;
						}
						//System.out.println("tileList: "+ gd.tiles[j][i]);
					}
				}
			}
		}
		
		for(int Ny = 1; Ny < 299; Ny++){
			for(int Nx = 1; Nx < 299; Nx++){
			//for(Coord c : tiles){
				//int test = getTileID(c);
				int test = tileList[Nx][Ny];
				
				if(test == -1) continue;
				if(waterPath && test < 2) continue;
				else if(!waterPath && test < 255 && test > 0) continue;
				//if(!filter.contains(c) ) continue;
				//System.out.println("test: "+ test);
				
				int a = 0;
				int b = -1;
				for(int i = 0; i < 4; i++){
					//int id = getTileID(c.add(a,b) );
					int Bx = Nx+a;
					int By = Ny+b;
					
					/*if(Bx < 0 || By < 0 || Bx > 299 || By > 299){
						a++;
						if(a == 0 && b == 0) a++;
						if(a > 1){ b++; a = -1;}
						continue;
					}*/
					
					int id = tileList[Bx][By];
					//System.out.println("id: "+ id);
					
					if(waterPath && id >= 0 && id < 2){
						Coord c = gc.mul(100).add(Nx,Ny);
						filter.add(c);
						break;
					}else if(!waterPath && id < 255 && id > 0){
						Coord c = gc.mul(100).add(Nx,Ny);
						filter.add(c);
						break;
					}
					
					/*a++;
					if(a == 0 && b == 0) a++;
					if(a > 1){ b++; a = -1;}*/
					
					int temp = b;
					b = a;
					a = temp * -1;
				}
			}
		}
		
		
		//System.out.println("filter size: "+filter.size() );
		return filter;
	}
	
	ArrayList<Coord> firstFilterGobs(ArrayList<Gob> cubWallSigns, boolean waterPath){
		ArrayList<Coord> gobCoords = new ArrayList<Coord>();
		
		for(Gob g : cubWallSigns){
			gobCoords.add(g.getc() );
		}
		
		return sortTiles(gobCoords, false);
	}
	
	ArrayList<Rectangle> mergeFilterdTiles(ArrayList<Coord> tiles, boolean tileFilter, boolean waterPath){
		ArrayList<Rectangle> filter = new ArrayList<Rectangle>();
		ArrayList<Coord> xlist = new ArrayList<Coord>();
		Coord prime = null;
		Coord sec = null;
		int checkDist = -1;
		
		if(!tileFilter) checkDist = -11;
		
		//ArrayList<Coord> ysort = sortTiles(tiles, false);
		
		for(Coord c : tiles){
			if(prime == null){
				prime = c;
				sec = c;
			}else if(prime.y != c.y || !c.add(checkDist,0).equals(sec) ){
				if(!prime.equals(sec)){
					addToFilterRect(filter, prime, sec, tileFilter, waterPath);
				}else{
					xlist.add(prime);
				}
				prime = c;
				sec = c;
			}else{
				sec = c;
			}
			
		}
		
		if(prime != null && sec != null){
			if(!prime.equals(sec)){
				addToFilterRect(filter, prime, sec, tileFilter, waterPath);
			}else{
				xlist.add(prime);
			}
		}
		
		ArrayList<Coord> xsort = sortTiles(xlist, true);
		
		prime = null;
		sec = null;
		
		for(Coord c : xsort){
			if(prime == null){
				prime = c;
				sec = c;
			}else if(prime.x != c.x || !c.add(0,checkDist).equals(sec) ){
				addToFilterRect(filter, prime, sec, tileFilter, waterPath);
				
				prime = c;
				sec = c;
			}else{
				sec = c;
			}
		}
		
		if(prime != null && sec != null)
			addToFilterRect(filter, prime, sec, tileFilter, waterPath);
		
		return filter;
	}
	
	void addToFilterRect(ArrayList<Rectangle> filter, Coord prime, Coord sec, boolean tileFilter, boolean waterPath){
		Rectangle r;
		
		if(tileFilter){
			r = new Rectangle(prime.x*11-1,
										prime.y*11-1,
										(sec.x-prime.x)*11+12,
										(sec.y-prime.y)*11+12 );
		}else{
			int meOffcet = -2;
			int meSize = 4;
			
			if(waterPath){
				meOffcet = -14;
				meSize = 26;
			}
			
			r = new Rectangle(prime.x - 5 + meOffcet, 
											prime.y - 5 + meOffcet, 
											(sec.x-prime.x) + 10 + meSize, 
											(sec.y-prime.y) + 10 + meSize);
		}
		
		filter.add(r);
	}
	
	ArrayList<Coord> sortTiles(ArrayList<Coord> tiles, boolean Xsort){
		ArrayList<Coord> list = new ArrayList<Coord>(tiles);
		ArrayList<Coord> temp = new ArrayList<Coord>();
		ArrayList<Coord> sorted = new ArrayList<Coord>();
		
		while(list.size() > 0){
			Coord small = null;
			
			for(Coord c : list){
				if(small == null){
					small = c;
				}else if(Xsort && c.x <= small.x){
					small = c;
				}else if(!Xsort && c.y <= small.y){
					small = c;
				}
			}
			
			for(Coord c : list){
				if(Xsort && c.x == small.x){
					temp.add(c);
				}else if(!Xsort && c.y == small.y){
					temp.add(c);
				}
			}
			
			while(temp.size() > 0){
			
				Coord t = null;
				for(Coord c : temp){
					if(t == null){
						t = c;
					}else if(!Xsort && c.x < t.x){
						t = c;
					}else if(Xsort && c.y < t.y){
						t = c;
					}
				}
				sorted.add(t);
				list.remove(t);
				temp.remove(t);
			}
		}
		
		return sorted;
	}
	
	/*boolean editFilter(Gob g, Coord offcet, Coord size){
		String name = g.resname();
		if( name.contains("/terobjs/") ){
			if( name.contains("/ridges/") ){
				if( name.contains("/grass/") ){
					if( name.equals("gfx/terobjs/ridges/grass/ss") ){
						size.x = 14;
						size.y = 7;
						offcet.x = -10;
						offcet.y = -4;
						return true;
					}
					if( name.equals("gfx/terobjs/ridges/grass/e") ){
						size.x = 5;
						size.y = 21;
						offcet.x = -2;
						offcet.y = -11;
						return true;
					}
					if( name.equals("gfx/terobjs/ridges/grass/e2s") ){
						size.x = 23;
						size.y = 22;
						offcet.x = 0;
						offcet.y = -21;
						return true;
					}
					if( name.equals("gfx/terobjs/ridges/grass/es") ){
						size.x = 7;
						size.y = 16;
						offcet.x = -5;
						offcet.y = -6;
						return true;
					}
					if( name.equals("gfx/terobjs/ridges/grass/ee") ){
						size.x = 7;
						size.y = 14;
						offcet.x = -4;
						offcet.y = -10;
						return true;
					}
				}
				if( name.contains("/mountain/") ){
					if( name.equals("gfx/terobjs/ridges/mountain/we") ){
						size.x = 22;
						size.y = 11;
						offcet.x = -11;
						offcet.y = 0;
						return true;
					}
					if( name.equals("gfx/terobjs/ridges/mountain/ws") ){
						size.x = 22;
						size.y = 11;
						offcet.x = -11;
						offcet.y = -11;
						return true;
					}
					if( name.equals("gfx/terobjs/ridges/mountain/ne") ){
						size.x = 11;
						size.y = 22;
						offcet.x = -11;
						offcet.y = -11;
						return true;
					}
					if( name.equals("gfx/terobjs/ridges/mountain/ns") ){
						size.x = 11;
						size.y = 22;
						offcet.x = 0;
						offcet.y = -11;
						return true;
					}
					if( name.equals("gfx/terobjs/ridges/mountain/ss") ){
						size.x = 10;
						size.y = 11;
						offcet.x = -10;
						offcet.y = 0;
						return true;
					}
					if( name.equals("gfx/terobjs/ridges/mountain/ee") ){
						size.x = 11;
						size.y = 10;
						offcet.x = 0;
						offcet.y = -10;
						return true;
					}
				}
			}
			if( name.contains("/furniture/") ){
				if( name.equals("gfx/terobjs/furniture/coffer") ){
					size.x = 10;
					size.y = 8;
					offcet.x = -5;
					offcet.y = -4;
					return true;
				}
				if( name.equals("gfx/terobjs/furniture/leanto") ){
					size.x = 21;
					size.y = 10;
					offcet.x = -9;
					offcet.y = -5;
					return true;
				}
				if( name.equals("gfx/terobjs/furniture/wardrobe") ){
					size.x = 20;
					size.y = 9;
					offcet.x = -8;
					offcet.y = -3;
					return true;
				}
				if( name.equals("gfx/terobjs/furniture/bed-sturdy") ){
					size.x = 18;
					size.y = 13;
					offcet.x = -8;
					offcet.y = -5;
					return true;
				}
				if( name.equals("gfx/terobjs/furniture/cclosed") || name.equals("gfx/terobjs/furniture/copen" ) ){
					size.x = 7;
					size.y = 4;
					offcet.x = -3;
					offcet.y = -3;
					return true;
				}
			}
			if( name.equals("gfx/terobjs/vclaim") ){
				size.x = 27;
				size.y = 8;
				offcet.x = -12;
				offcet.y = -4;
				return true;
			}
			if( name.equals("gfx/terobjs/htable") ){
				size.x = 7;
				size.y = 12;
				offcet.x = -2;
				offcet.y = -5;
				return true;
			}
			if( name.equals("gfx/terobjs/crate") ){
				size.x = 14;
				size.y = 7;
				offcet.x = -6;
				offcet.y = -3;
				return true;
			}
			if( name.equals("gfx/terobjs/trough") ){
				size.x = 21;
				size.y = 10;
				offcet.x = -7;
				offcet.y = -4;
				return true;
			}
			if( name.equals("gfx/terobjs/dframe2") ){
				size.x = 4;
				size.y = 18;
				offcet.x = -3;
				offcet.y = -8;
				return true;
			}
			if( name.equals("gfx/terobjs/mining/ladder") ){
				size.x = 9;
				size.y = 2;
				offcet.x = -4;
				offcet.y = -1;
				return true;
			}
			if( name.equals("gfx/terobjs/lbox") ){
				size.x = 10;
				size.y = 15;
				offcet.x = -6;
				offcet.y = -7;
				return true;
			}
		}
		if( name.contains("/arch/") ){
			if( name.contains("/gates/") ){
				if( name.contains("-ns") ){
					size.x = 10;
					size.y = 21;
					offcet.x = -5;
					offcet.y = -11;
					return true;
				}
				if( name.contains("-we") ){
					size.x = 21;
					size.y = 10;
					offcet.x = -11;
					offcet.y = -5;
					return true;
				}
			}
			if( name.equals("gfx/arch/sign") ){
				size.x = 10;
				size.y = 10;
				offcet.x = -5;
				offcet.y = -5;
				return true;
			}
			if( name.equals("gfx/arch/stairs-inn") ){
				size.x = 12;
				size.y = 23;
				offcet.x = -7;
				offcet.y = -11;
				return true;
			}
			if( name.equals("gfx/arch/door-inn") ){
				size.x = 19;
				size.y = 5;
				offcet.x = -11;
				offcet.y = -2;
				return true;
			}
		}
		if( name.contains("/kritter/") ){
			if( name.equals("gfx/kritter/plow/s") ){
				size.x = 6;
				size.y = 6;
				offcet.x = -3;
				offcet.y = -3;
				return true;
			}
			if( name.equals("gfx/kritter/cow/s") ){
				size.x = 11;
				size.y = 11;
				offcet.x = -4;
				offcet.y = -4;
				return true;
			}
			if( name.equals("gfx/kritter/cart/s") ){
				size.x = 10;
				size.y = 10;
				offcet.x = -5;
				offcet.y = -5;
				return true;
			}
			if( name.equals("gfx/kritter/rat/s") ){
				for(String secondName : g.resnames() )
					if( secondName.equals("gfx/kritter/dragonfly/dragonfly") )
						return false;
				size.x = 1;
				size.y = 1;
				offcet.x = 0;
				offcet.y = 0;
				return true;
			}
			if( name.equals("gfx/kritter/deer/s") ){
				size.x = 11;
				size.y = 11;
				offcet.x = -4;
				offcet.y = -4;
				return true;
			}
			if( name.equals("gfx/kritter/bear/s") ){
				size.x = 16;
				size.y = 16;
				offcet.x = -8;
				offcet.y = -8;
				return true;
			}
			if( name.equals("gfx/kritter/fox/s") ){
				size.x = 7;
				size.y = 7;
				offcet.x = -3;
				offcet.y = -3;
				return true;
			}
			if( name.equals("gfx/kritter/troll/s") ){
				size.x = 16;
				size.y = 16;
				offcet.x = -8;
				offcet.y = -8;
				return true;
			}
		}
		if( name.equals("gfx/borka/s") ){
			for(String secondName : g.resnames() ){
				if( secondName.contains("gfx/borka/body") )
					return false;
				/*if( secondName.equals("gfx/terobjs/blood") ){
					size.x = 0;
					size.y = 0;
					offcet.x = 0;
					offcet.y = 0;
					return true;
				}*/
			/*}
			size.x = 1;
			size.y = 1;
			offcet.x = 0;
			offcet.y = 0;
			return true;
		}
		
		return false; // edit size 42
	}*/
	
	boolean kritterFix(Gob g, Coord offcet, Coord size){ // fix for those pesky bugs
	
		if(g.resname().contains("gfx/borka/s") && !g.isHuman()){
			offcet.x = 0;
			offcet.y = 0;
			size.x = 1;
			size.y = 1;
			
			return true;
		}else if(g.resname().contains("gfx/arch/sign") ){
			offcet.x = -5;
			offcet.y = -5;
			size.x = 10;
			size.y = 10;
			
			return true;
		}else if(g.resname().contains("gfx/kritter/rat/s") ){
			for(String s : g.resnames() ){
				if(s.contains("gfx/kritter/dragonfly") || s.contains("gfx/kritter/moth") ){
					offcet.x = 0;
					offcet.y = 0;
					size.x = 0;
					size.y = 0;
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	public ScriptDrawer addPathfindDrawer(){
		ScriptDrawer drawer = new ScriptDrawer(Coord.z, m_util.m_ui.mainview);
		if(m_util.m_ui.mainview.pathfindDraw != null){
			synchronized(m_util.m_ui.mainview.pathfindDraw){
				m_util.m_ui.mainview.pathfindDraw = drawer;
			}
		}else{
			m_util.m_ui.mainview.pathfindDraw = drawer;
		}
		
		return drawer;
	}
	
	public void removePathfindDrawer(){
		if(m_util.m_ui.mainview.pathfindDraw == null) return;
		
		synchronized(m_util.m_ui.mainview.pathfindDraw){
			m_util.m_ui.mainview.pathfindDraw = null;
		}
	}
}