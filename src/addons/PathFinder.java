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
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;

public class PathFinder{
	ArrayList<classRect> m_globalRect;
	ArrayList<classRect> m_openRectList;
	ArrayList<Corner> m_cornerList;
	ArrayList<classPath> m_globalCornerList;
	double m_globalPathLenth = 5000;
	classPath m_truePath = null;
	double m_delta = 0.1;
	//double m_counterMicroUpdates = 0.2;
	HavenUtil m_util;
	
	public PathFinder(HavenUtil h){
		m_util = h;
		m_globalRect = new ArrayList<classRect>();
		m_openRectList = new ArrayList<classRect>();
		m_cornerList = new ArrayList<Corner>();
		m_globalCornerList = new ArrayList<classPath>();
	}
	
	ArrayList<Point> pathFind(ArrayList<Rectangle> allObsticles, Point start, Point end){
		//System.out.println("Pathfinder started.");
		long time = System.currentTimeMillis();
		
		ArrayList<Point> path = new ArrayList<Point>();
		classPath startCorner = new classPath(start, true);
		int breakCount = 0;
		int checkCount = 0;
		int failPath = 0;
		
		sortRectangles(allObsticles);
		if(!m_util.pathing) return null;
		
		m_globalCornerList.add(startCorner);
		while(true){
			if(!m_util.pathing) return null;
			ArrayList<classPath> newPaths = new ArrayList<classPath>();
			
			//System.out.println("m_globalCornerList size "+m_globalCornerList.size() );
			
			boolean cornerAdded = false;
			for(classPath checkPath : m_globalCornerList){
				if(!m_util.pathing) return null;
				if(checkPath.checked) continue;
				if(checkPath.parrent == null) continue;
				
				breakCount = 0;
				cornerAdded = true;
				checkPath.checked = true;
				
				if(!checkLineColision(checkPath.p, end, false) ){
					updatePathing(checkPath, end);
					//System.out.println("Path found "+checkCount);
				}else{
					ArrayList<classPath> openTemp = new ArrayList<classPath>();
					ArrayList<classPath> closedCorners = setClosedCorners();
					if(!m_util.pathing) return null;
					
					openTemp.addAll( closedCorners );
					openTemp.addAll( m_globalCornerList );
					
					openCorners(checkPath, openTemp, end);
					if(!m_util.pathing) return null;
					//newPaths.addAll( closedCorners );
					
					//if(newPaths.size() > 0) break;
				}
			}
			
			//System.out.println("newPaths size "+newPaths.size() );
			//m_globalCornerList.addAll( newPaths );
			//newPaths.clear();
			
			if(!cornerAdded){
				ArrayList<Point> cornerTemp = new ArrayList<Point>();
				//System.out.println("m_globalCornerList found. " + m_globalCornerList.size() );
				failPath++;
				if(m_truePath != null) checkCount++;
				if(checkCount >= 2 || failPath >= 6) break;
				
				for(classPath reset : m_globalCornerList){
					reset.checked = false;
					reset.secondCheck = true;
					cornerTemp.add(reset.p);
				}
				
				//openCorners(new classPath(start, true), m_globalCornerList, true);
				//System.out.println("start beem " + m_globalCornerList.size() );
				//m_globalCornerList.clear();
				//m_globalCornerList.add( new classPath(start, true) );
				//m_globalCornerList.addAll( setClosedCorners() );
				//System.out.println("2nd opening " + m_globalCornerList.size() );
				for(Corner recheckPoints : m_cornerList){
					if(!cornerTemp.contains(recheckPoints.originalPoint) )
						m_globalCornerList.add(new classPath(recheckPoints.originalPoint) );
				}
				//m_globalCornerList.addAll( setClosedCorners() );
				////System.out.println("Pilot compleate. " + (System.currentTimeMillis() - time) );
				//System.out.println("m_globalCornerList size. " + m_globalCornerList.size() );
				//System.out.println("m_cornerList size. " + m_cornerList.size() );
			}
		}
		
		
		
		if(!m_util.pathing) return null;
		
		if(m_truePath != null) path.add( end );
		classPath backtrack = m_truePath;
		
		//System.out.println("backTrack " + backtrack.endLenth(end) + " first" );
		
		while(true && backtrack != null){
			//System.out.println("backTrack " + backtrack.lenth + " first check " + backtrack.first );
			if(backtrack.first) break;
			path.add(0, backtrack.p );
			backtrack = backtrack.parrent;
		}
		
		//m_util.PFrunning = false;
		////System.out.println("Pathfinder finished. " + (System.currentTimeMillis() - time) );
		return path;
	}
	
	void clearCash(){
		m_globalRect.clear();
		m_openRectList.clear();
		m_cornerList.clear();
		m_globalCornerList.clear();
		m_globalPathLenth = 5000;
		m_truePath = null;
	}
	
	void updatePathing(classPath checkPath, Point end){
		if(m_truePath == null){
			m_globalPathLenth = checkPath.endLenth(end);
			m_truePath = checkPath;
			//System.out.println("m_globalPathLenth found. " + m_globalPathLenth );
		}else if(checkPath.endLenth(end) < m_globalPathLenth){
			m_globalPathLenth = checkPath.endLenth(end);
			m_truePath = checkPath;
			//System.out.println("m_globalPathLenth update. " + m_globalPathLenth );
		}/*else{
			System.out.println("m_globalPathLenth fail. " + checkPath.endLenth(end) );
		}*/
	}
	
	ArrayList<Rectangle> test1(){
		ArrayList<Rectangle> rect = new ArrayList<Rectangle>();
		//for(classRect r : m_openRectList)
		for(classRect r : m_globalRect)
			rect.add(r.original);
		
		return rect;
	}
	
	void openCorners(classPath from, ArrayList<classPath> closedList, Point end){
		//System.out.println("opening");
		ArrayList<classPath> openTemp = new ArrayList<classPath>();
		//ArrayList<Point> openCornerTemp = new ArrayList<Point>();
		//openCornerTemp = m_globalCornerList;
		//boolean redo = false;
		//openTemp.clear();
		
		for(classPath to : closedList){
			if(!m_util.pathing) return;
			//if(to.secondCheck && from.secondCheck) continue;
			if(to.first) continue;
			if(to.checkLenth(from) + to.dist(end) >= m_globalPathLenth ) continue;
			if(to.checkLenth(from) > to.lenth) continue;
			
			//System.out.println("leanth "+to.checkLenth(from) );
			
			if(!checkLineColision(from.p, to.p, true)){
				to.update(from);
				//System.out.println("update ");
			}else{
				//if(!m_util.pathing) return;
				//if(slowOpening) openTemp.addAll( setClosedCorners() );
				//System.out.println("corner colision "+ openTemp.size() );
			}
		}
		
		//redo = false;
		//System.out.println("new Corners "+openTemp.size());
		//if(openTemp.size() > 0 /*&& firstOpening*/){
		//	openCorners(from, openTemp, end);
			//openCorners(from, openTemp);
			//System.out.println("new Corners "+openTemp.size());
			//openTemp.addAll( openCorners(from, openTemp, false) );
		//}
		
		//return openTemp;
	}
	
	boolean checkLineColision(Point start, Point end, boolean breakColision){
		//ArrayList<classRect> openRectTemp = new ArrayList<classRect>();
		//openRectTemp = m_openRectList;
		//openRectTemp = m_globalRect;
		boolean colision = false;
		Line2D.Double line = new Line2D.Double(start, end);
		
		for(classRect r : m_globalRect){
			if(!m_util.pathing) return false;
			if(line.intersects(r.shrunk)){
				addToOpenRect(r);
				if(breakColision) return true;
				//setClosedCorners();
				colision = true;
			}
		}
		
		return colision;
	}
	
	void addToOpenRect(classRect rect){
		ArrayList<classRect> openTempList = new ArrayList<classRect>();
		
		if(!m_openRectList.contains(rect))
			m_openRectList.add(rect);
		else
			return;
		
		for(classRect test : m_globalRect){
			if(!m_util.pathing) return;
			
			if(!m_openRectList.contains(test)){
				if(test.centerPoint.distance(rect.centerPoint) > test.rectDist + rect.rectDist) continue;
				
				if(rect.shrunk.intersects(test.shrunk) ){
					openTempList.add(test);
				}
			}
		}
		
		for(classRect test : openTempList)
			addToOpenRect(test);
	}
	
	ArrayList<classPath> setClosedCorners(){
		ArrayList<classPath> openTemp = new ArrayList<classPath>();
		
		for(classRect r : m_openRectList){
			if(!m_util.pathing) return null;
			
			for(Corner p : r.corners){
				if(m_cornerList.contains(p) )
					continue;
				
				boolean outside = true;
				
				for(classRect x : m_openRectList){
					if(r != x){
						if(x.original.contains(p.shrunkPoint)){
							outside = false;
							break;
						}
					}
				}
				
				if(outside){
					m_cornerList.add( p );
					openTemp.add(new classPath(p.originalPoint) );
				}
			}
		}
		
		//System.out.println("setClosedCorners "+openTemp.size());
		return openTemp;
	}
	
	void sortRectangles(ArrayList<Rectangle> allObsticles){
		for(Rectangle r : allObsticles){
			if(!m_util.pathing) return;
			m_globalRect.add(new classRect(r) );
		}
	}
	
	public class classPath{
		double lenth;
		Point p;
		classPath parrent;
		boolean checked;
		boolean first = false;
		boolean secondCheck = false;
		
		public classPath(){
		}
		
		public classPath(Point thisPoint){
			p = thisPoint;
			lenth = 5000;
			parrent = null;
			checked = false;
		}
		
		public classPath(Point thisPoint, boolean start){
			p = thisPoint;
			lenth = 0;
			parrent = null;
			checked = false;
			first = start;
			parrent = new classPath();
		}
		
		void update(classPath from){
			if(parrent == null){
				setLenth(from);
				parrent = from;
			}else if( checkLenth(from) < lenth){
				//System.out.println("update miniupdate " + checkLenth(from) + " vs original < " + lenth + " tot " + ( lenth - checkLenth(from) ) );
				setLenth(from);
				parrent = from;
				checked = false;
			}/*else{
				System.out.println("fail miniupdate " + checkLenth(from) + " vs original > " + lenth );
			}*/
		}
		
		void setLenth(classPath from){
			lenth = dist(from.p) + from.lenth;
		}
		
		double checkLenth(classPath from){
			return dist(from.p) + from.lenth;
		}
		
		double endLenth(Point end){
			return dist(end) + lenth;
		}
		
		double dist(Point from) {
			long dx = from.x - p.x;
			long dy = from.y - p.y;
			return(Math.sqrt((dx * dx) + (dy * dy)));
		}
	}
	
	public class Corner{
		Point originalPoint;
		Point2D.Double shrunkPoint;
		
		public Corner(Point p, Point2D.Double p2){
			originalPoint = p;
			shrunkPoint = p2;
		}
	}
	
	public class classRect{
		Rectangle original;
		Rectangle2D.Double shrunk;
		//Rectangle2D.Double expand;
		ArrayList<Corner> corners = new ArrayList<Corner>();
		Point2D.Float centerPoint;
		float rectDist;
		
		public classRect(Rectangle r){
			original = r;
			shrinkRect();
			//expandRect();
			getCorners();
			//getSrunkCorners();
			rectDistCalcs();
		}
		
		void shrinkRect(){
			shrunk = new Rectangle2D.Double(
				(double)original.x + (double)m_delta,
				(double)original.y + (double)m_delta,
				(double)original.width - (double)m_delta*2,
				(double)original.height - (double)m_delta*2
			);
		}
		
		/*void expandRect(){
			expand = new Rectangle2D.Double(
				(double)original.x - m_delta,
				(double)original.y - m_delta,
				(double)original.width + m_delta*2,
				(double)original.height + m_delta*2
			);
		}*/
		
		void getCorners(){
			Point c = original.getLocation();
			Point2D.Double c2 = new Point2D.Double(shrunk.x, shrunk.y);
			
			corners.add(new Corner(new Point(c), c2 ) );
			corners.add(new Corner(new Point(c.x + original.width , c.y), new Point2D.Double(c2.x + shrunk.width , c2.y) ) );
			corners.add(new Corner(new Point(c.x , c.y + original.height ), new Point2D.Double(c2.x , c2.y + shrunk.height ) ) );
			corners.add(new Corner(new Point(c.x + original.width , c.y + original.height ), new Point2D.Double(c2.x + shrunk.width , c2.y + shrunk.height ) ) );
		}
		
		/*void getCorners(){
			
			
			srunkCorners.add(new Point(c2) );
			srunkCorners.add(new Point(c2.x + shrunk.width , c2.y) );
			srunkCorners.add(new Point(c2.x , c2.y + shrunk.height ) );
			srunkCorners.add(new Point(c2.x + shrunk.width , c2.y + shrunk.height ) );
		}*/
		
		void rectDistCalcs(){
			centerPoint = new Point2D.Float();
			float xdist = (float)(original.width / 2);
			float ydist = (float)(original.height / 2);
			
			centerPoint.x = original.x + xdist;
			centerPoint.y = original.y + ydist;
			
			rectDist = xdist + ydist;
		}
	}
}