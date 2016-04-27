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
import java.util.List;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Color;

import haven.*;
import haven.MapView.rallyPoints;
import haven.CharWnd.Study;
import haven.IMeter.Meter;
import haven.ToolbarWnd.Slot;
import ender.timer.Timer;
import ender.timer.TimerController;

public class HavenUtil{
	public UI m_ui;
	
	//main variables
	public boolean stop = true;
	public boolean pathing = false;
	
	public boolean running = false;
	public boolean PFrunning = false;
	
	public Coord m_pos1 = Coord.z;
	public Coord m_pos2 = Coord.z;
	public Coord m_safePos = Coord.z;
	
	//draw line variables
	public boolean publicLineBoolean = false;
	public ArrayList<Rectangle> publicRects = new ArrayList<Rectangle>();
	public ArrayList<Coord> publicPoints = new ArrayList<Coord>();
	
	// hourglass variables
	public int HourglassID = -1;
	public int HourGlassValue = -1;
	public boolean miniglass = false;
	
	//script variables
	public int m_script;
	public int m_option;
	public String m_modify;
	
	//command overrides
	public boolean disableMouseItem = false;
	public boolean disableSession = false;
	
	//main client variables
	public int m_Type;
	//public boolean cleanupRunning = false;
	//public boolean landscapeRunning = false;
	//public boolean feastRunning = false;
	//public boolean seedbagRunning = false;
	public boolean autoLand = false;
	public boolean runFlaskRunning = false;
	public boolean pathDrinker = false;
	public boolean runFlask = false;
	public int drinkCount = 21;
	
	public String[][] m_foodArray = {
		{"gfx/invobjs/ants-larvae", "10", "1"},
		{"gfx/invobjs/ants-pupae", "10", "1"},
		{"gfx/invobjs/ants-aphids", "10", "1"},
		{"gfx/invobjs/meat-cow", "40", "1"},
		{"gfx/invobjs/beetroot", "20", "1"},
		{"gfx/invobjs/beetrootleaves", "10", "1"},
		{"gfx/invobjs/egg-boiled", "20", "1"},
		{"gfx/invobjs/mussel-boiled", "10", "1"},
		{"gfx/invobjs/bread", "60", "1"},
		{"gfx/invobjs/carrot", "10", "1"},
		{"gfx/invobjs/cheese-curd", "40", "1"},
		{"gfx/invobjs/egg-fried", "20", "1"},
		{"gfx/invobjs/grapes", "10", "1"},
		{"gfx/invobjs/peapod", "5", "1"},
		{"gfx/invobjs/pumpkinflesh", "20", "1"},
		{"gfx/invobjs/ratonastick", "20", "1"},
		{"gfx/invobjs/raisins", "5", "2"},
		{"gfx/invobjs/bread-bark", "50", "2"},
		{"gfx/invobjs/wurst-bearsalami", "60", "2"},
		{"gfx/invobjs/wurst-bigbearbanger", "100", "2"},
		{"gfx/invobjs/wurst-boarbaloney", "50", "2"},
		{"gfx/invobjs/wurst-boarboudin", "60", "2"},
		{"gfx/invobjs/cheese-cellarcheddar", "40", "2"},
		{"gfx/invobjs/wurst-chickenchorizo", "45", "2"},
		{"gfx/invobjs/wurst-cowchorizo", "60", "2"},
		{"gfx/invobjs/wurst-foxfuet", "60", "2"},
		{"gfx/invobjs/wurst-fox", "40", "2"},
		{"gfx/invobjs/honeybun", "25", "2"},
		{"gfx/invobjs/wurst-lambsausages", "55", "2"},
		{"gfx/invobjs/cheese-mothzarella", "40", "2"},
		{"gfx/invobjs/cheese-muskymilben", "40", "2"},
		{"gfx/invobjs/onionrings", "45", "2"},
		{"gfx/invobjs/pancake", "40", "2"},
		{"gfx/invobjs/pie-pea", "35", "2"},
		{"gfx/invobjs/wurst-piglet", "30", "2"},
		{"gfx/invobjs/bread-pumpkin", "50", "2"},
		{"gfx/invobjs/meat-pig-r", "40", "2"},
		{"gfx/invobjs/feast-roachwrap", "30", "2"},
		{"gfx/invobjs/meat-bear-r", "40", "2"},
		{"gfx/invobjs/meat-cow-r", "40", "2"},
		{"gfx/invobjs/meat-boar-r", "40", "2"},
		{"gfx/invobjs/meat-bream-r", "30", "2"},
		{"gfx/invobjs/meat-brill-r", "30", "2"},
		{"gfx/invobjs/meat-chicken-r", "40", "2"},
		{"gfx/invobjs/meat-deer-r", "40", "2"},
		{"gfx/invobjs/meat-fox-r", "40", "2"},
		{"gfx/invobjs/meat-sheep-r", "40", "2"},
		{"gfx/invobjs/meat-perch-r", "30", "2"},
		{"gfx/invobjs/meat-pike-r", "30", "2"},
		{"gfx/invobjs/meat-plaice-r", "30", "2"},
		{"gfx/invobjs/meat-rabbit-r", "40", "2"},
		{"gfx/invobjs/meat-roach-r", "30", "2"},
		{"gfx/invobjs/meat-salmon-r", "30", "2"},
		{"gfx/invobjs/meat-sturgeon-r", "30", "2"},
		{"gfx/invobjs/wurst-runningrabbit", "50", "2"},
		{"gfx/invobjs/stew-spring", "30", "2"},
		{"gfx/invobjs/salad-beet", "30", "2"},
		{"gfx/invobjs/wurst-tamegame", "60", "2"},
		{"gfx/invobjs/cheese-temmentaler", "40", "2"},
		{"gfx/invobjs/pie-wellplaiced", "40", "2"},
		{"gfx/invobjs/salad-chicken", "50", "3"},
		{"gfx/invobjs/feast-fishsticks", "30", "3"},
		{"gfx/invobjs/pie-blueberry", "35", "3"},
		{"gfx/invobjs/cake-carrot", "30", "3"},
		{"gfx/invobjs/feast-pirozhki", "50", "3"},
		{"gfx/invobjs/cheese-creamycamembert", "40", "3"},
		{"gfx/invobjs/pie-pumpkin", "40", "3"},
		{"gfx/invobjs/cake-raisinbutter", "25", "3"},
		{"gfx/invobjs/bread-brodgar", "35", "3"},
		{"gfx/invobjs/meat-eel-r", "30", "3"},
		{"gfx/invobjs/pie-apple", "35", "4"},
		{"gfx/invobjs/cheese-brodgarblue", "40", "4"},
		{"gfx/invobjs/wurst-ddd", "80", "4"},
		{"gfx/invobjs/cheese-genericgouda", "40", "5"},
		{"gfx/invobjs/feast-perchedperch", "20", "5"},
		{"gfx/invobjs/cheese-jorbonzola", "40", "5"},
		{"gfx/invobjs/feast-cavebulb", "50", "5"},
		{"gfx/invobjs/creamycock", "60", "5"},
		{"gfx/invobjs/beetedbirdbreast", "50", "5"},
		{"gfx/invobjs/cheese-harmesan", "40", "5"},
		{"gfx/invobjs/feast-bbbb", "30", "5"},
		{"gfx/invobjs/wurst-bierwurst", "50", "5"},
		{"gfx/invobjs/feast-bbb", "30", "5"},
		{"gfx/invobjs/herbs/blueberry", "10", "5"},
		{"gfx/invobjs/herbs/chantrelle", "10", "5"},
		{"gfx/invobjs/pancake-crepecitrouille", "60", "5"},
		{"gfx/invobjs/pancake-crepenoisette", "60", "5"},
		{"gfx/invobjs/cheese-midnightblue", "40", "5"},
		{"gfx/invobjs/pancake-pommaceperdue", "60", "5"},
		{"gfx/invobjs/pancake-sacrebleu", "60", "5"},
		{"gfx/invobjs/cheese-sunlitstilton", "40", "5"},
		{"gfx/invobjs/wurst-www", "200", "5"},
		{"gfx/invobjs/zestybrill", "25", "5"},
		{"gfx/invobjs/ants-empress", "10", "10"},
		{"gfx/invobjs/ants-queen", "10", "10"},
		{"gfx/invobjs/ants-soldiers", "10", "10"},
		{"gfx/invobjs/apple", "10", "10"},
		{"gfx/invobjs/meat-bear", "40", "10"},
		{"gfx/invobjs/cake-bday", "35", "10"},
		{"gfx/invobjs/herbs/bloatedbolete", "10", "10"},
		{"gfx/invobjs/meat-boar", "40", "10"},
		{"gfx/invobjs/herbs/candleberry", "10", "10"},
		{"gfx/invobjs/cheese-cavecheddar", "40", "10"},
		{"gfx/invobjs/fishyeyeball", "20", "10"},
		{"gfx/invobjs/meat-fox", "40", "10"},
		{"gfx/invobjs/mulberry", "10", "10"},
		{"gfx/invobjs/meat-rabbit", "35", "10"},
		{"gfx/invobjs/meat-chicken", "40", "10"},
		{"gfx/invobjs/meat-deer", "40", "10"},
		{"gfx/invobjs/meat-sheep", "40", "10"},
		{"gfx/invobjs/meat-pig", "40", "10"},
		{"gfx/invobjs/meat-troll", "40", "10"},
		{"gfx/invobjs/feast-rob", "30", "10"},
		{"gfx/invobjs/meat-troll-r", "40", "10"},
		{"gfx/invobjs/bread-shew", "45", "10"},
		{"gfx/invobjs/onion", "20", "10"},
	};
	
	public HavenUtil(UI u){
		m_ui = u;
	}
	
	public void stop(int button){
		if(button == 1 || button == 3){
			stop = true;
			pathing = false;
		}
	}
	
	public void forceStop(){
		stop = true;
		pathing = false;
		
		running = false;
		PFrunning = false;
		
		update();
	}
	
	public void wait(int time){
		try{
			Thread.sleep(time);
		}catch(Exception e){}
	}
	
	public void update(){
		turnBotIconOn(running);
	}
	
	public void running(boolean setRunning){
		running = setRunning;
		update();
	}
	
	public Rectangle coordToTileRect(Coord p1, Coord p2){
		p1 = p1.div(11).mul(11);
		p2 = p2.div(11).mul(11);
		
		int smallestX = p1.x;
		int largestX = p2.x + 11;
		
		int smallestY = p1.y;
		int largestY = p2.y + 11;
		
		if(p2.x < p1.x){
			smallestX = p2.x;
			largestX = p1.x + 11;
		}
		
		if(p2.y < p1.y){
			smallestY = p2.y;
			largestY = p1.y + 11;
		}
		
		return new Rectangle(smallestX, smallestY, largestX - smallestX, largestY - smallestY);
	}
	
	//////////////////////////// actions /////////////////////////////
	
	public void logOut(){
		//m_ui.close();
		int i;
		for (i = 0; i < MainFrame.threads.size(); i++) {
			if (MainFrame.threads.get(i).getUI() == m_ui)
				break;
        }
		MainFrame.instance.closeSession(i);
	}
	
	public void closeClient(){
		sendErrorMessage("Loging out character in 10 seconds.");
		int count = 0;
		stop = false;
		while(count < 20 && !stop){
			count++;
			wait(500);
			if(count == 10) sendErrorMessage("Loging out character in 5 seconds.");
		}
		if(!stop) m_ui.sess.close();
	}
	
	public void rebootClient(){
		Runtime rt = Runtime.getRuntime();
		try{
			rt.exec("cmd /c start run.bat");
		}catch(Exception e){
			
		}
		
		try{
			m_ui.sess.close();
		}catch(Exception e){
			
		}
		
		System.exit(0);
	}
	
	public void craftItem(int All){
		if(!craftMenuReady())
			return;
		
		m_ui.makeWindow.wdgmsg("make", All);
	}
	
	public void printFullTime(){
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("'Time:' HH:mm:ss");;
		System.out.println( sdf.format(cal.getTime()) );
    }
	
	public void sendSlenMessage(String str){
		m_ui.slen.error(str);
	}
	
	public void sendErrorMessage(String str){
		m_ui.cons.out.println(str);
		m_ui.slen.error(str);
		System.out.println(str);
	}
	
	public void clickWorldObject(int button, Gob object){
		if(object == null) return;
		
		m_ui.mainview.wdgmsg("click", new Coord(200,150), object.getr(), button, 0, object.id, object.getr());
	}
	
	public void clickWorld(int button, Coord c){
		m_ui.mainview.wdgmsg("click", new Coord(0, 0), c, button, 0);
	}
	
	public void clickWorld(int button, Coord c, int mod){
		if(stop) return;
		m_ui.mainview.wdgmsg("click", new Coord(0, 0), c, button, mod);
	}
	
	public void sendAction(String str){
		String[] action = {str};
		m_ui.mnu.wdgmsg("act", (Object[])action);
	}
	
	public void sendAction(String str1, String str2){
		String[] action = {str1, str2};
		m_ui.mnu.wdgmsg("act", (Object[])action);
	}
	
	public void sendAttachedMessage(String msg, String chat){
		List<HWindow> wnds = m_ui.chat.getwnds();
		
		for(HWindow w : wnds){
			if(w.title.contains(chat) ){
				w.wdgmsg("msg", msg);
			}
		}
	}
	
	public void setPlayerSpeed(int speed){
		if(stop) return;
		try{
			m_ui.spd.setspeed(speed, true);
		}catch(Exception e){
			System.out.println("Speed UI error.");
		}
	}
	
	public void placeSign(Coord c){
		if(stop) return;
		m_ui.mainview.wdgmsg("place", c, 1, 0);
	}
	
	public void itemActionWorldObject(Gob object, int mod){
		if(object == null || stop) return;
		m_ui.mainview.wdgmsg("itemact", new Coord(200,150), object.getr(), mod, object.id, object.getr());
	}
	
	public ScriptDrawer addScriptDrawer(){
		ScriptDrawer drawer = new ScriptDrawer(Coord.z, m_ui.mainview);
		if(m_ui.mainview.scriptDraw != null){
			synchronized(m_ui.mainview.scriptDraw){
				m_ui.mainview.scriptDraw = drawer;
			}
		}else{
			m_ui.mainview.scriptDraw = drawer;
		}
		
		return drawer;
	}
	
	public void removeScriptDrawer(){
		if(m_ui.mainview.scriptDraw == null) return;
		
		synchronized(m_ui.mainview.scriptDraw){
			m_ui.mainview.scriptDraw = null;
		}
	}
	
	//////////////////////////// info /////////////////////////////////
	
	public String myName(){
		return m_ui.sess.charname;
	}
	
	public boolean gameLoggedIn(){
		if(m_ui.sess == null) return false;
		
		return m_ui.sess.alive();
	}
	
	public String slenError(){
		if(m_ui.slen.catchError != null)
			return m_ui.slen.catchError;
		else
			return "";
	}
	
	public String getCursor(){
		return m_ui.root.getcurs(m_ui.mainview.mousepos).name;
	}
	
	public Gob getPlayerGob(){
		Gob player = null;
		try{
			player = m_ui.mainview.glob.oc.getgob(m_ui.mainview.playergob);
		}catch(Exception e){
			System.out.println("Player gob error in mapview.");
		}
		int count = 0;
		while(player == null && !stop){
			wait(100);
			try{
				player = m_ui.mainview.glob.oc.getgob(m_ui.mainview.playergob);
			}catch(Exception e){
				System.out.println("Player gob error in mapview.");
			}
			count++;
			if(count > 50){
				stop = true;
			}
		}
		return player;
	}
	
	public Coord getPlayerCoord(){
		try{
			return getPlayerGob().getr();
		}catch(Exception e){
			while(getPlayerGob() == null && !stop) wait(100);
			if(stop) return null;
		}
		
		return getPlayerGob().getr();
	}
	
	public boolean hasHourglass(){
		if(HourglassID == -1){
			wait(10);
			if(HourglassID == -1){
				wait(10);
				if(HourglassID == -1){
					return false;
				}
			}
		}
		
		return true;
	}
	
	public int getHourglassValue(){
		if(hasHourglass() )
			return HourGlassValue;
		else
			return -1;
	}
	
	public boolean checkPlayerWalking(){
		Gob g = getPlayerGob();
			if (g.checkWalking()){
				return true;
			}
			
		return false;
	}
	
	public boolean checkPlayerCarry(){
		Gob g = getPlayerGob();
		for(String name : g.resnames()){
			if(name.contains("/banzai/")){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkPlayerSitting(){
		Gob g = getPlayerGob();
		for(String name : g.resnames()){
			if(name.contains("/sitting/")){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean objectLifted(Gob g){
		Moving m = g.getattr(Moving.class);
		if(m != null && m instanceof Following) return true;
		
		return false;
	}
	
	public Gob getPlayerLiftedObject(){
		int playerID = getPlayerGob().id;
		
		for(Gob g : allGobs()){
			Moving m = g.getattr(Moving.class);
			if(m != null && m instanceof Following){
				//System.out.println(g.resname() );
				Following f = (Following)m;
				//System.out.println(f.tgt );
				if(f.tgt == playerID) return g;
			}
		}
		
		return null;
	}
	
	Item findFlask(){
		Item flask = getItemFromBag("waterskin");
		if(flask == null){
			flask = getItemFromBag("waterflask");
			if(flask == null){
				return null;
			}
		}
		
		return flask;
	}
	
	float waterFlaskInfo(Item i){
		if(i == null) return 100;
		String str = new String(i.tooltip);
		if(str.contains("Empty"))
			return 0;
		if(str.contains("Waterflask"))
			return Float.parseFloat(str.substring(12,15));
		if(str.contains("Waterskin"))
			return Float.parseFloat(str.substring(11,14));
		return 0;
	}
	
	String getResName(Indir<Resource> indir){
		if(indir.get() != null) {
			return indir.get().name;
		}else{
			int count = 0;
			while(indir.get() == null && count < 1000){wait(50); count++;}
			
			if(indir.get() != null) return indir.get().name;
		}
		return "";
	}
	
	public int getHunger(){
		int hunger = 2000;
		Widget root = m_ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if ((w instanceof IMeter)){
				if(((IMeter)w).bg.name.contains("hngr")){
					for(Meter m : ((IMeter)w).meters){
						
						if(m.a < 100){
							if(m.c.getRed() == 96 && m.c.getGreen() == 0 && m.c.getBlue() == 0){
								hunger =(int)( m.a * 5);
							}else if(m.c.getRed() == 255 && m.c.getGreen() == 64 && m.c.getBlue() == 0){
								hunger =(int)( 500 + m.a * 3.33);
							}else if(m.c.getRed() == 255 && m.c.getGreen() == 192 && m.c.getBlue() == 0){
								hunger =(int)( 800 + m.a * 1);
							}else if(m.c.getRed() == 0 && m.c.getGreen() == 255 && m.c.getBlue() == 0){
								hunger =(int)( 900 + m.a * 1);
							}else if(m.c.getRed() == 255 && m.c.getGreen() == 0 && m.c.getBlue() == 0){
								hunger =(int)( 1000 + m.a * 1);
							}else{
								hunger = 2000;
							}
						}
					}
				}
			}
		}
		return hunger;
	}
	
	public int getStamina(){
		int stamina = 0;
		Widget root = m_ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if ((w instanceof IMeter)){
				if(((IMeter)w).bg.name.contains("nrj")){
					for(Meter m : ((IMeter)w).meters){
						stamina = m.a;
					}
				}
			}
		}
		
		return stamina;
	}
	
	public int getFluid(String str){
		int i = 1;
		int result = 0;
		
		if(str == null) return -1;
		int lenth = str.length();
		if(lenth == 0) return -1;
		
		if(str.contains("Empty") || str.contains("empty")) return 0;
		
		char[] arr = str.toCharArray();
		
		while(i < lenth - 1 ){
			if(i >= lenth - 1 ) return -1;
			
			int askii = (int)arr[i];
			
			if(askii == 46){
				int before = (int)arr[i-1];
				int after = (int)arr[i+1];
				
				if(before >= 48 && before <= 57)
					if(after >= 48 && after <= 57)
						break;
			}
			
			i++;
		}
		
		for(int j=i-3; j<=i+1; j++){
			int askii = (int)arr[j];
			
			if(askii >= 48 && askii <= 57 ){
				result *= 10;
				result += askii - 48;
			}
		}
		
		return result;
	}
	
	public int foodTest(Item food, int type){
		String name = food.GetResName();
		
		for(int i = 0; i < m_foodArray.length; i++){
			if(m_foodArray[i][0].equals(name) ){
				if(Integer.parseInt(m_foodArray[i][2]) <= type )
					return Integer.parseInt(m_foodArray[i][1]);
			}
		}
		
		return -1;
	}
	
	public Coord viewUpperLeft(Coord center){
		Coord c = center.div(1100).mul(1100);
		return c.add(center.sub(c).div(100).sub(4,4).mul(100) );
	}
	
	public boolean insideViewBox(Coord center, Coord checkC){
		Coord ul = viewUpperLeft(center);
		Rectangle r = new Rectangle(ul.x+1, ul.y+1, 898, 898);
		return r.contains(checkC.x, checkC.y);
	}
	
	public int kinType(Gob g){
		KinInfo kin = (g.getattr(KinInfo.class));
		if(kin == null) return -1;
		return kin.group;
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
	
	public String getContainerName(Gob container){
		if(container.resname().contains("gfx/terobjs/cheeserack") ){
			return "Cheese Rack";
		}else if(container.resname().contains("gfx/terobjs/tub") ){
			return "Curding Tub";
		}else if(container.resname().contains("gfx/terobjs/cupboard") ){
			return "Cupboard";
		}else if(container.resname().contains("gfx/terobjs/lchest") ){
			return "Chest";
		}else if(container.resname().contains("gfx/terobjs/furniture/cclosed") ){
			return "Chest";
		}else if(container.resname().contains("gfx/terobjs/furniture/copen") ){
			return "Chest";
		}else if(container.resname().contains("gfx/terobjs/smelter") ){
			return "Ore Smelter";
		}else if(container.resname().contains("gfx/terobjs/fforge") ){
			return "Finery Forge";
		}else if(container.resname().contains("gfx/terobjs/htable") ){
			return "Herbalist Table";
		}else if(container.resname().contains("gfx/terobjs/sbox") ){
			return "Steel Crucible";
		}else if(container.resname().contains("basket") ){
			return "Basket";
		}else if(container.resname().contains("gfx/terobjs/furniture/coffer") ){
			return "Coffer";
		}else if(container.resname().contains("gfx/terobjs/dframe2")){
			return "Drying Frame";
		}else if(container.resname().contains("gfx/terobjs/kiln")){
			return "Kiln";
		}else if(container.resname().contains("gfx/terobjs/oven")){
			return "Oven";
		}else if(container.resname().contains("gfx/terobjs/ttub")){
			return "Tanning Tub";
		}else if(container.resname().contains("gfx/terobjs/coop")){
			return "Chicken Coop";
		}
		return "N/A";
	}
	
	public Coord objectDirection(Gob object){
		Coord c = Coord.z;
		int i = 1;
		int j = 1;
		
		int di = -1;
		int dj = 0;
		
		int segmentPassed = 0;
		
		for(int dir = 0; dir < 8; dir++){
			boolean found = false;
			String endname = "-"+Integer.toString(dir);
			
			for(String names : object.resnames() ){
				if(names.endsWith(endname) ){
					found = true;
					break;
				}
			}
			
			if(found) break;
			
			i += di;
			j += dj;
			segmentPassed++;
			
			if (segmentPassed == 2) {
				segmentPassed = 0;
				
				int buffer = di;
				di = -dj;
				dj = buffer;
			}
		}
		
		return c.add(i,j);
	}
	
	public rallyPoints getRallyPoints(){
		return m_ui.mainview.m_rally;
	}
	
	/////////////////////////// widgets /////////////////////////
	
	public boolean aimTest(){
		Widget root = m_ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if(w instanceof Aimview) return true;
		}
		return false;
	}
	
	public Aimview aimWig(){
		Widget root = m_ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if(w instanceof Aimview) return(Aimview)w;
		}
		return null;
	}
	
	public Fightview fightWig(){
		Widget root = m_ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if(w instanceof Fightview) return((Fightview)w);
		}
		
		return null;
	}
	
	public boolean checkAggro(){
		if(m_ui.fight != null && m_ui.fight.lsrel.size() > 0)
			return true;
		
		return false;
	}
	
	//////////////////////////// equipment window ////////////////////////////////
	
	public void toggleEquipment(){
		m_ui.root.wdgmsg("gk", 5);
	}
	
	public void openEquipment(){
		if(m_ui.equip == null){
			toggleEquipment();
			while(!windowOpen("Equipment") && !stop){
				wait(200);
			}
		}
	}
	
	public void closeEquipment(){
		if(m_ui.equip != null){
			toggleEquipment();
			while(windowOpen("Equipment") && !stop){
				wait(200);
			}
		}
	}
	
	public void unEquipPlayer(int slot){
		if(slot < 0 && slot > 15)
			return;
		if(m_ui.equip == null)
			return;
		
		m_ui.equip.wdgmsg("transfer", slot, new Coord(10, 10));
	}
	
	public void unEquipOneItem(String name){
		if(m_ui.equip == null)
			return;
			
		ArrayList<Item> list = equipList();
		
		for(Item i : list){
			if(i != null){
				String itemName = i.GetResName();
				
				if(itemName.contains(name) ){
					transferItem(i);
					//m_ui.equip.wdgmsg("transfer", slot, new Coord(10, 10));
					return;
				}
			}
		}
	}
	
	public ArrayList<Item> equipList(){
		if(m_ui.equip == null)
			return null;
		
		return new ArrayList<Item>(m_ui.equip.equed);
	}
	
	public Object[] equipListArray(){
		if(m_ui.equip == null)
			return null;
		
		return m_ui.equip.equed.toArray();
	}
	
	public boolean checkEquiped(String name){
		openEquipment();
		
		ArrayList<Item> list = equipList();
		
		for(Item i : list){
			if(i != null){
				String itemName = i.GetResName();
				//System.out.println(itemName);
				
				if(itemName.contains(name) ){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public int checkEquipCount(String name){
		int count = 0;
		openEquipment();
		
		ArrayList<Item> list = equipList();
		
		for(Item i : list){
			if(i != null){
				String itemName = i.GetResName();
				//System.out.println(itemName);
				
				if(itemName.contains(name) ){
					count++;
				}
			}
		}
		
		return count;
	}
	
	public void mouseEquipPlayer(){
		if(m_ui.equip == null)
			return;
		
		m_ui.equip.wdgmsg("drop", -1, new Coord(10, 10));
	}
	
	public void autoMouseEquipPlayer(Item i){
		openEquipment();
		pickUpItem(i);
		//while(!mouseHoldingAnItem() && !stop) wait(200);
		if(!stop) m_ui.equip.wdgmsg("drop", -1, new Coord(10, 10));
		//while(mouseHoldingAnItem() && !stop)	wait(200);
	}
	
	///////////////////////////  belts ////////////////////////////////
	
	public static String flaskText(int val){
		String str = "";
		if(val >= 48 && val <= 57){
			str = String.valueOf(val - 48);
		}else if(val >= 112 && val <= 123){
			str = "F"+String.valueOf(val - 111);
		}else if(val >= 96 && val <= 111){
			int i = ToolbarWnd.extendedNumpadConverter(val);
			str = "N"+ToolbarWnd.numpadIcons(i);
		}else if(val >= 69 && val <= 89){
			int i = ToolbarWnd.keypadNum(val);
			str = ToolbarWnd.keypadString(i);
		}
		
		return str;
	}
	
	public void useActionBar(int bar, int slot){
		if(bar == 0){
			if(m_ui.mnu.digitbar.layout[slot] == null){
				return;
			}
			m_ui.mnu.digitbar.layout[slot].use();
		}
		if(bar == 1){
			if(m_ui.mnu.functionbar.layout[slot] == null){
				return;
			}
			m_ui.mnu.functionbar.layout[slot].use();
		}
		if(bar == 2){
			if(m_ui.mnu.numpadbar.layout[slot] == null){
				return;
			}
			m_ui.mnu.numpadbar.layout[slot].use();
		}
		if(bar == 3){
			if(m_ui.mnu.qwertypadbar.layout[slot] == null){
				return;
			}
			m_ui.mnu.qwertypadbar.layout[slot].use();
		}
	}
	
	public boolean findFlaskToolbar(int bar, int slot){
		String quickname = "empty";
		ToolbarWnd barPad = null;
		
		if(bar == 0){
			if(slot < 0 || slot > 9) return false;
			barPad = m_ui.mnu.digitbar;
		}else if(bar == 1){
			if(slot < 0 || slot > 11) return false;
			barPad = m_ui.mnu.functionbar;
		}else if(bar == 2){
			if(slot < 0 || slot > 14) return false;
			barPad = m_ui.mnu.numpadbar;
		}else if(bar == 3){
			if(slot < 0 || slot > 9) return false;
			barPad = m_ui.mnu.qwertypadbar;
		}
		
		if(barPad == null) return false;
		
		if(barPad.layout[slot] != null)
			if(barPad.layout[slot].getres() != null)
				quickname = barPad.layout[slot].getres().name;
		
		if(!quickname.contains("waterskin") && !quickname.contains("waterflask") ){
			return false;
		}
		
		return true;
	}
	
	public void setBeltSlot(int bar, int slot, Item i){
		String quickname = "empty";
		ToolbarWnd barPad = null;
		
		if(bar == 0){
			if(slot < 0 || slot > 9) return;
			barPad = m_ui.mnu.digitbar;
		}else if(bar == 1){
			if(slot < 0 || slot > 11) return;
			barPad = m_ui.mnu.functionbar;
		}else if(bar == 2){
			if(slot < 0 || slot > 14) return;
			barPad = m_ui.mnu.numpadbar;
		}else if(bar == 3){
			if(slot < 0 || slot > 9) return;
			barPad = m_ui.mnu.qwertypadbar;
		}else if(bar == 4){
			//if(slot < 0 || slot > 9) return;
			barPad = m_ui.mnu.scriptBar;
		}
		
		if(barPad == null) return;
		
		Coord c = i.c;
		
		if(mouseHoldingAnItem() )
			dropItemInBag(c);
		else
			pickUpItem(i);
		
		int belt = barPad.belt;
		int s = barPad.getbeltslot();
		String val = "@"+s;
		barPad.layout[slot] = barPad.makeNewSlot(val, belt, slot);
		m_ui.slen.wdgmsg("setbelt", s, 0);
		barPad.setbeltslot(belt, slot, val);
		
		dropItemInBag(c);
	}
	
	public Coord flaskToCoord(int val){
		Coord c = new Coord();
		if(val >= 48 && val <= 57){
			c.x = 0;
			c.y = val - 49;
			if(val == 48) c.y = 9;
			return c;
		}else if(val >= 112 && val <= 123){
			c.x = 1;
			c.y = val - 112;
			return c;
		}else if(val >= 96 && val <= 111){
			c.x = 2;
			c.y = ToolbarWnd.extendedNumpadConverter(val);
			return c;
		}else if(val >= 65 && val <= 90){
			c.x = 3;
			c.y = ToolbarWnd.keypadNum(val);
			return c;
		}
		
		return null;
	}
	
	////////////////////////// buffs ///////////////////////////////
	
	boolean checkBuff(String name){
		if(m_ui.mainview == null){
			return false;
		}
		if(m_ui.mainview.glob == null){
			return false;
		}
		
		synchronized (m_ui.mainview.glob.buffs) {
			for(Buff b : m_ui.mainview.glob.buffs.values()) {
				Indir<Resource> ir = b.res;
				if(getResName(b.res).contains(name)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void toggleTracking(){
		sendAction("tracking");
	}
	
	public void turnTrackingOn(boolean turnon){
		if(turnon)
			if(!checkBuff("tracking"))
				toggleTracking();
		if(!turnon)
			if(checkBuff("tracking"))
				toggleTracking();
	}
	
	public void toggleCriminal(){
		sendAction("crime");
	}
	
	public void turnCriminalOn(boolean turnon){
		if(turnon)
			if(!checkBuff("crime"))
				toggleCriminal();
		if(!turnon)
			if(checkBuff("crime"))
				toggleCriminal();
	}
	
	public void toggleBotIcon(){
		int k = -4;
		
		if(checkBuff("eye")){
			m_ui.mainview.glob.buffs.remove(k);
		}else{
			Buff buff = new Buff(k, Resource.load("paginae/act/eye").indir());
			buff.major = true;
			m_ui.mainview.glob.buffs.put(k, buff);
		}
	}
	
	public void turnBotIconOn(boolean turnon){
		if(turnon)
			if(!checkBuff("eye"))
				toggleBotIcon();
		if(!turnon)
			if(checkBuff("eye"))
				toggleBotIcon();
	}
	
	public int getTW(){
		String travelName = "gfx/hud/buffs/travel";
		String drunkName = "gfx/hud/buffs/drunk0";
		synchronized (m_ui.mainview.glob.buffs) {
		for(Buff b : m_ui.sess.glob.buffs.values()) {
			if(travelName.equals(b.res.get().name) ){
				return b.ameter;
			}
		}
		}
		
		return 0;
	}
	
	public boolean getSwiming(){
		String travelName = "gfx/hud/buffs/travel";
		String drunkName = "gfx/hud/buffs/drunk0";
		String swimName = "gfx/hud/skills/swim";
		
		synchronized (m_ui.mainview.glob.buffs) {
		for(Buff b : m_ui.sess.glob.buffs.values()) {
			if(swimName.equals(b.res.get().name) ){
				return true;
			}
		}
		}
		
		return false;
	}
	
	public Coord getBudAffect(){
		Coord persentageTime = new Coord(0,0);
		String travelName = "gfx/hud/buffs/travel";
		String drunkName = "gfx/hud/buffs/drunk0";
		String budName = "gfx/hud/buffs/ganja";
		
		synchronized (m_ui.mainview.glob.buffs) {
		for(Buff b : m_ui.sess.glob.buffs.values()) {
			if(budName.equals(b.res.get().name) ){
				persentageTime.x = b.ameter;
				persentageTime.y = b.cticks;
			}
		}
		}
		
		return persentageTime;
	}
	
	public Coord getDrunkAffect(){
		Coord persentageTime = new Coord(0,0);
		String travelName = "gfx/hud/buffs/travel";
		String drunkName = "gfx/hud/buffs/drunk0";
		String budName = "gfx/hud/buffs/ganja";
		
		synchronized (m_ui.mainview.glob.buffs) {
		for(Buff b : m_ui.sess.glob.buffs.values()) {
			if(drunkName.equals(b.res.get().name) ){
				persentageTime.x = b.ameter;
				persentageTime.y = b.cticks;
			}
		}
		}
		
		return persentageTime;
	}
	
	public Coord getTeaAffect(){
		Coord persentageTime = new Coord(0,0);
		String travelName = "gfx/hud/buffs/travel";
		String drunkName = "gfx/hud/buffs/drunk0";
		String budName = "gfx/hud/buffs/ganja";
		String teaName = "gfx/hud/buffs/tea";
		
		synchronized (m_ui.mainview.glob.buffs) {
			for(Buff b : m_ui.sess.glob.buffs.values()) {
				if(teaName.equals(b.res.get().name) ){
					persentageTime.x = b.ameter;
					persentageTime.y = b.cticks;
				}
			}
		}
		
		return persentageTime;
	}
	
	////////////////////////// global objects ///////////////////////////
	
	public Gob myHearth(Coord c){
		Gob hearth = null;
		double dist = 0;
		if(c == null) c = getPlayerCoord();
		
		for(Gob g : getObjects("gfx/terobjs/hearth-play")){
			if(kinType(g) != -1 ) continue;
			
			if(hearth == null){
				hearth = g;
				dist = c.dist(g.getr() );
			}else if(dist > c.dist(g.getr() ) ){
				hearth = g;
				dist = c.dist(g.getr() );
			}
		}
		
		return hearth;
	}
	
	public boolean findObject(Gob object){
		synchronized(m_ui.mainview.glob.oc){
			for(Gob g : m_ui.mainview.glob.oc){
				if(object.id == g.id) return true;
			}
		}
		
		return false;
	}
	
	public Gob findObjectByID(int ID){
		ArrayList<Gob> list = new ArrayList<Gob>();
		synchronized(m_ui.mainview.glob.oc){
			for(Gob g : m_ui.mainview.glob.oc){
				if(g.id == ID)
					return g;
			}
		}
 
		return null;
	}
	
	public Gob getClosestObjectInArray(ArrayList<Gob> list){
		return getClosestObjectInArray(list, getPlayerCoord());
	}
	
	public Gob getClosestObjectInArray(ArrayList<Gob> list, Coord from){
		double min = 1000;
		Gob closest = null;
		for(Gob g : list){
			double dist = g.getr().dist(from);
			if(closest == null){
				min = dist;
				closest = g;
			}else if(min > dist){
				min = dist;
				closest = g;
			}
		}
		return closest;
	}
	
	public ArrayList<Gob> allGobs(){
		ArrayList<Gob> list = new ArrayList<Gob>();
		synchronized(m_ui.mainview.glob.oc){
			for(Gob g : m_ui.mainview.glob.oc){
				list.add(g);
			}
		}
		return list;
	}
	
	public Gob findClosestObject(String str){
		return findClosestObject(new String[]{str}, 0, null, null);
	}
	
	public Gob findClosestObject(String[] str){
		return findClosestObject(str, 0, null, null);
	}
	
	public Gob findClosestObject(String str, int range){
		return findClosestObject(new String[]{str}, range, null, null);
	}
	
	public Gob findClosestObject(String[] str, int range){
		return findClosestObject(str, range, null, null);
	}
	
	public Gob findClosestObject(String[] str, Coord from){
		return findClosestObject(str, 0, from, null);
	}
	
	public Gob findClosestObject(String str, Coord from){
		return findClosestObject(new String[]{str}, 0, from, null);
	}
	
	public Gob findClosestObject(String[] str, int range, Coord from){
		return findClosestObject(str, range, from, null);
	}
	
	public Gob findClosestObject(String str, int range, Coord from){
		return findClosestObject(new String[]{str}, range, from, null);
	}
	
	public Gob findClosestObject(String str, Coord p1, Coord p2){
		return findClosestObject(new String[]{str}, 0, null, coordToTileRect(p1, p2) );
	}
	
	public Gob findClosestObject(String str, Coord p1, Coord p2, Coord from){
		return findClosestObject(new String[]{str}, 0, from, coordToTileRect(p1, p2) );
	}
	
	public Gob findClosestObject(String[] str, int range, Coord from, Rectangle rect){
		Gob gob = null;
		ArrayList<Gob> list = allGobs();
		double min = Integer.MAX_VALUE;
		if(from == null) from = getPlayerCoord();
		
		for(Gob g : list){
			for(String s : str){
				if(g.resname().contains(s)){
					if(range == 0 || g.getr().dist(from) <= range){
						double dist = g.getr().dist(from);
						
						if(rect == null || rect.contains(g.getr().x, g.getr().y)){
							if(dist < min){
								gob = g;
								min = dist;
							}
						}
					}
				}
			}
		}
		
		return gob;
	}
	
	public ArrayList<Gob> getObjects(String str){
		return getObjects(new String[]{str}, 0, null, null);
	}
	
	public ArrayList<Gob> getObjects(String[] str){
		return getObjects(str, 0, null, null);
	}
	
	public ArrayList<Gob> getObjects(String str, int range){
		return getObjects(new String[]{str}, range, null, null);
	}
	
	public ArrayList<Gob> getObjects(String[] str, int range){
		return getObjects(str, range, null, null);
	}
	
	public ArrayList<Gob> getObjects(String[] str, int range, Coord from){
		return getObjects(str, range, from, null);
	}
	
	public ArrayList<Gob> getObjects(String str, int range, Coord from){
		return getObjects(new String[]{str}, range, from, null);
	}
	
	public ArrayList<Gob> getObjects(Coord p1, Coord p2){
		return getObjects(new String[]{"gfx"}, 0, null, coordToTileRect(p1, p2) );
	}
	
	public ArrayList<Gob> getObjects(String str, Coord p1, Coord p2){
		return getObjects(new String[]{str}, 0, null, coordToTileRect(p1, p2) );
	}
	
	public ArrayList<Gob> getObjects(String str, Coord p1, Coord p2, Coord from){
		return getObjects(new String[]{str}, 0, from, coordToTileRect(p1, p2) );
	}
	
	public ArrayList<Gob> getObjects(String[] str, int range, Coord from, Rectangle rect){
		ArrayList<Gob> gobs = new ArrayList<Gob>();
		ArrayList<Gob> list = allGobs();
		if(from == null) from = getPlayerCoord();
		
		for(Gob g : list){
			for(String s : str){
				if(g.resname().contains(s)){
					if(range == 0 || g.getr().dist(from) <= range){
						double dist = g.getr().dist(from);
						
						if(rect == null || rect.contains(g.getr().x, g.getr().y)){
							gobs.add(g);
						}
					}
				}
			}
		}
		
		return gobs;
	}
	
	///////////////////// get windows ///////////////////////
	
	public void togleInventory(){
		m_ui.root.wdgmsg("gk", 9);
	}
	
	public boolean isInventoryOpen(){
		return getInventory("Inventory") != null;
	}
	
	public void openInventory(){
		if(!isInventoryOpen()){
			togleInventory();
			while(!isInventoryOpen() && !stop){
				wait(200);
			}
		}
	}
	
	public Widget findWindowWidget(String name){
		for(Widget w = m_ui.root.child; w != null; w = w.next){
			if (!(w instanceof Window))
				continue;
			if(((Window)w).cap == null)
				continue;
			if(((Window)w).cap.text == null)
				continue;
			if(!((Window)w).cap.text.equals(name))
				continue;
			
			return w;
		}
		
		return null;
	}
	
	public Inventory getInventory(String name){
		Widget inv = null;
		
		Widget w = findWindowWidget(name);
		
		if(w == null) return null;
		
		for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
			if(wdg instanceof Inventory){
				inv = wdg;
			}
		}
		
		if(inv != null) return (Inventory)inv;
		
		return null;
	}
	
	public ArrayList<Inventory> getInventorys(String name){
		ArrayList<Inventory> invs = new ArrayList<Inventory>();
		
		Widget w = findWindowWidget(name);
		
		if(w == null) return null;
		
		for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
			if(wdg instanceof Inventory){
				invs.add( (Inventory)wdg );
			}
		}
		
		if(invs.size() > 0) return invs;
		
		return null;
	}
	
	public void clickButton(String name){
		Widget root = m_ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if (!(w instanceof Window))
				continue;
			if(((Window)w).cap == null)
				continue;
			if(((Window)w).cap.text == null)
				continue;
			//if(!((Window)w).cap.text.contains(name))
			//	continue;
			
			for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
				if(wdg instanceof Button){
					try{
						if(((Button)wdg).text.text.contains(name)){
							((Button)wdg).wdgmsg("activate");
							return;
						}
					}catch(Exception e){
					}
				}
			}
		}
	}
	
	public void moveAllWindowsToView(){
		Widget root = m_ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if (!(w instanceof Window))
				continue;
			((Window)w).moveWindowToView();
		}
	}
	
	public ArrayList<Shopbox> getBarterStandShopbox(){
		ArrayList<Shopbox> list = new ArrayList<Shopbox>();
		
		Widget w = findWindowWidget("Barter Stand");
		
		if(w == null) return null;
		
		for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
			if(wdg instanceof Shopbox){
				//System.out.println(((Shopbox)wdg).matsInStand);
				list.add((Shopbox)wdg);
			}
		}
		
		if(list.size() >= 5) return list;
		
		return null;
	}
	
	public void destroyWDG(Widget wdg){
		m_ui.destroy(wdg);
	}
	
	public void destroyWDG(String name){
		Widget w = findWindowWidget(name);
		
		if(w == null) return;
		
		m_ui.destroy(w);
	}
	
	public void toggleStudy(){
		m_ui.study.toggle();
	}
	
	public void openStudy(){
		if(windowOpen("Study") ) return;
		
		toggleStudy();
		while(!windowOpen("Study") && !stop) wait(200);
	}
	
	public Inventory getStudyInv(){
		Widget w = findWindowWidget("Study");
		
		if(w == null) return null;
		
		for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
			if(wdg instanceof Study){
				for(Widget studyWdg = wdg.child; studyWdg != null; studyWdg = studyWdg.next) {
					if(studyWdg instanceof Inventory){
						return (Inventory)studyWdg;
					}
				}
			}
		}
		
		return null;
	}
	
	public int getVmeterAmount(int redColor, boolean direct){
		Widget root = m_ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if (!(w instanceof Window))
				continue;
			if(((Window)w).cap == null)
				continue;
			if(((Window)w).cap.text == null)
				continue;
			
			for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
				if(wdg instanceof VMeter){
					//System.out.println(((VMeter)wdg).amount);
					//System.out.println(((VMeter)wdg).cl.getRed());
					if(((VMeter)wdg).cl.getRed() == redColor){
						if(direct){
							return ((VMeter)wdg).amount;
						}else{
							return ((VMeter)wdg).amount2;
						}
					}
				}
			}
		}
		
			// steel crucible 255 or milk
			// water tub 75  water cauldron 71
			// bark tub 165 or rennet
			// press 0 grean bar
		
		return -1;
	}
	
	public String runeStoneInfo(){
		Widget w = findWindowWidget("Runestone");
		
		if(w == null) return null;
		
		for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
			if(wdg instanceof Label){
				return ((Label)wdg).texts;
			}
			
		}
		
		return "";
	}
	
	public String getInviteText(){
		Widget w = findWindowWidget("Invitation");
		
		if(w == null) return null;
		
		for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
			if(wdg instanceof Label){
				return ((Label)wdg).texts;
			}
		}
		
		return null;
	}
	
	public String barrelInfo(){
		return barrelInfo("Barrel");
	}
	
	public String barrelInfo(String name){
		Widget w = findWindowWidget(name);
		
		if(w == null) return null;
		
		for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
			if(wdg instanceof Label){
				return ((Label)wdg).texts;
			}
		}
		
		return "";
	}
	
	public void closeWindow(String name) {
		Widget w = findWindowWidget(name);
		
		if(w == null) return;
		
		for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
			if(wdg instanceof IButton){
				wdg.wdgmsg("activate");
				return;
			}
		}
	}
	
	public void autoCloseWindow(String name){
		closeWindow(name);
		while(windowOpen(name) && !stop) wait(100);
	}
	
	public String getCraftName(){
		if( m_ui.makeWindow != null )
			return m_ui.makeWindow.title;
		
		return "N/A";
	}
	
	public boolean craftMenuReady(){
		return m_ui.makeWindow != null;
	}
	
	public ISBox findSignBox(String name){
		Widget w = findWindowWidget(name);
		
		if(w == null) return null;
		
		for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
			if(wdg instanceof ISBox)
				return (ISBox)wdg;
		}
		
		return null;
	}
	
	public ISBox findSignBox(String name, String resName){
		Widget w = findWindowWidget(name);
		
		if(w == null) return null;
		
		for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
			if(wdg instanceof ISBox){
				if( ((ISBox)wdg).res.name.contains(resName)){
					//System.out.println(( (ISBox)wdg).res.name );
					return (ISBox)wdg;
				}
			}
		}
		
		return null;
	}
	
	public boolean windowOpen(String name){
		Widget w = findWindowWidget(name);
		
		if(w == null) return false;
		
		return true;
	}
	
	public int windowID(String name){
		Widget w = findWindowWidget(name);
		
		if(w == null) return -1;
		
		return ((Window)w).id;
	}
	
	public ArrayList<Img> getImgListFrom(){
		
		Widget root = m_ui.root;
		ArrayList<Img> i = new ArrayList<Img>();
		
		for(Widget w = root.child; w != null; w = w.next){
			if (!(w instanceof Window))
				continue;
			if(((Window)w).cap == null)
				continue;
			if(((Window)w).cap.text == null)
				continue;
			if(!(
			((Window)w).cap.text.equals("Boat") ||
			((Window)w).cap.text.equals("Wagon") ||
			((Window)w).cap.text.equals("Cart")))
				continue;
			
			for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
			
				if(wdg instanceof Img){
					//System.out.println(wdg.c);
					i.add((Img)wdg);
				}
			}
		}
		return i;
	}
	
	public int getImgListSize(){
		return getImgListFrom().size();
	}
	
	public void wagCartBoat(){
		int size = 50;
		
		if(windowOpen("Boat"))
			size = 2;
		if(windowOpen("Cart"))
			size = 4;
		if(windowOpen("Wagon"))
			size = 20;
		while(!stop){
			ArrayList<Img> ImgList = getImgListFrom();
			if(ImgList.size() >= size){
				if(checkPlayerCarry()){
					for(int i = ImgList.size() - 1; i >= 0; i-- )
						imgDropoff(ImgList.get(i));
				}else{
					imgPickup(ImgList);
				}
				break;
			}else{
				wait(100);
			}
		}
	}
	
	public void imgDropoff(Img inv){
		//int mod = m_ui.modflags();
		inv.wdgmsg("click", new Coord(5,5), 1, 0);
	}
	
	public void imgPickup(ArrayList<Img> i){
		int size = 50;
		
		if(windowOpen("Boat"))
			size = 2;
		if(windowOpen("Cart"))
			size = 4;
		if(windowOpen("Wagon"))
			size = 20;
		
		if(i.size() > size)
			i.get(size).wdgmsg("click", new Coord(5,5), 1, 0);
	}
	
	public void buttonActivate(String name){ // buildSign
		Widget w = findWindowWidget(name);
		
		if(w == null) return;
		
		for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
			if(wdg instanceof Button){
				boolean tryAgain = true;
				while(tryAgain && !stop){
					tryAgain = false;
					try{
						wdg.wdgmsg("activate");
					}catch(Exception e){
						tryAgain = true;
						wait(100);
					}
				}
			}
		}
	}
	
	public String getBuildSignName(){
		Widget root = m_ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if (!(w instanceof Window))
				continue;
			if(((Window)w).cap == null)
				continue;
			if(((Window)w).cap.text == null)
				continue;
			//if(!((Window)w).cap.text.contains(name))
			//	continue;
			
			for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
				if(wdg instanceof Button){
					boolean tryAgain = true;
					while(tryAgain && !stop){
						tryAgain = false;
						try{
							if(((Button)wdg).text.text.contains("Build"))
								return ((Window)w).cap.text;
						}catch(Exception e){
							tryAgain = true;
							wait(100);
						}
					}
				}
			}
		}
		return "N/A";
	}
	
	public boolean crossRoadTravel(int signNum){
		Widget root = m_ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if (!(w instanceof Window))
				continue;
			if(((Window)w).cap == null)
				continue;
			if(((Window)w).cap.text == null)
				continue;
			//if(!((Window)w).cap.text.contains(name))
			//	continue;
			
			for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
				if(wdg instanceof Button){
					boolean tryAgain = true;
					while(tryAgain && !stop){
						tryAgain = false;
						try{
							if(((Button)wdg).text.text.contains("Travel")){
								if(signNum == 0) return true;
								
								if(((Button)wdg).c.y == (30 + 60 * (signNum-1) ) ){
									wdg.wdgmsg("activate");
									return true;
								}
							}
						}catch(Exception e){
							tryAgain = true;
							wait(100);
						}
					}
				}
			}
		}
		
		return false;
	}
	
	////////////////////////  Flower Menu  ///////////////////////////////
	
	public boolean flowerMenuReady(){
		return m_ui.flowerMenu != null;
	}
	
	public boolean flowerMenuReadyTest(){
		return flowerMenuReady();
	}
	
	public void flowerMenuSelect(String name){
		if(!flowerMenuReady()) return;
		
		for (int i = 0; i < m_ui.flowerMenu.opts.length; i++){
			if (m_ui.flowerMenu.opts[i].name.contains(name)) {
				m_ui.flowerMenu.wdgmsg(m_ui.flowerMenu, "cl", new Object[] { Integer.valueOf(m_ui.flowerMenu.opts[i].num) });
				break;
			}
		}
	}
	
	public void flowerMenuSelectReverse(String name){
		if(!flowerMenuReady()) return;
		for (int i = (m_ui.flowerMenu.opts.length - 1); i >= 0 ; i--){
			if (m_ui.flowerMenu.opts[i].name.contains(name)) {
				m_ui.flowerMenu.wdgmsg(m_ui.flowerMenu, "cl", new Object[] { Integer.valueOf(m_ui.flowerMenu.opts[i].num) });
				break;
			}
		}
	}
	
	public boolean checkFlowerMenu(String name){
		if(!flowerMenuReady()) return false;
		
		for (int i = 0; i < m_ui.flowerMenu.opts.length; i++)
			if (m_ui.flowerMenu.opts[i].name.contains(name))
				return true;
		
		return false;
	}
	
	public void closeFlowerMenu(){
		if(!flowerMenuReady()) return;
		
		m_ui.flowerMenu.wdgmsg("cl", -1);
	}
	
	public void autoFlowerMenu(String name){
		while(!flowerMenuReady() && !stop) wait(100);
		flowerMenuSelect(name);
	}
	
	public void autoCloseFlowerMenu(){
		closeFlowerMenu();
		while(flowerMenuReady() && !stop) wait(100);
	}
	
	public void safeAutoFlowerMenu(String name){
		while(!flowerMenuReady() && !stop) wait(100);
		if(!checkFlowerMenu(name) ) return;
		flowerMenuSelect(name);
	}
	
	public void autoFlowerMenuWithClose(String name){
		while(!flowerMenuReady() && !stop) wait(100);
		flowerMenuSelect(name);
		while(flowerMenuReady() && !stop) wait(100);
	}
	
	////////////////////////////// Items ////////////////////////////////
	
	public Item getItemFromBag(String name){
		Item i = null;
		Inventory inv = getInventory("Inventory");
		if(inv == null) return i;
		
		for(Widget wdg = inv.child; wdg != null; wdg = wdg.next){
			if(wdg instanceof Item){
				if(((Item)wdg).GetResName().contains(name)){
					i = (Item)wdg;
				}
			}
		}
		return i;
	}
	
	public Item getItemFromBagExclude(String name, String exclude){
		Item i = null;
		Inventory inv = getInventory("Inventory");
		if(inv == null) return i;
		
		for(Widget wdg = inv.child; wdg != null; wdg = wdg.next){
			if(wdg instanceof Item){
				String s = ((Item)wdg).GetResName();
				if(s.contains(name) && !s.contains(exclude) ){
					i = (Item)wdg;
				}
			}
		}
		return i;
	}
	
	public ArrayList<Item> getItemsFromBag(){
		Inventory inv = getInventory("Inventory");
		ArrayList<Item> list = new ArrayList<Item>();
		
		if(inv == null) return list;
		
		for(Widget wdg = inv.child; wdg != null; wdg = wdg.next){
			if(wdg instanceof Item){
				Item i = (Item)wdg;
				list.add(i);
			}
		}
		return list;
	}
	
	public ArrayList<Item> getItemsFromBag(String str){
		Inventory inv = getInventory("Inventory");
		ArrayList<Item> list = new ArrayList<Item>();
		
		if(inv == null) return list;
		
		for(Widget wdg = inv.child; wdg != null; wdg = wdg.next){
			if(wdg instanceof Item){
				Item i = (Item)wdg;
				if(i.GetResName().contains(str) )
					list.add(i);
			}
		}
		return list;
	}
	
	public ArrayList<Item> getItemsFromInv(Inventory inv){
		ArrayList<Item> list = new ArrayList<Item>();
		
		if(inv == null) return list;
		
		for(Widget wdg = inv.child; wdg != null; wdg = wdg.next){
			if(wdg instanceof Item){
				Item i = (Item)wdg;
				while(!stop && i.missing.loading){ wait(100); System.out.println("missing shitz"); }
				list.add(i);
			}
		}
		return list;
	}
	
	public ArrayList<Item> getItemsFromInv(String str, Inventory inv){
		ArrayList<Item> list = new ArrayList<Item>();
		
		if(inv == null) return list;
		
		for(Widget wdg = inv.child; wdg != null; wdg = wdg.next){
			if(wdg instanceof Item){
				Item i = (Item)wdg;
				if(i.GetResName().contains(str) )
					list.add(i);
			}
		}
		return list;
	}
	
	public Item getHighestQ(ArrayList<Item> itemList){
		Item im = null;
		
		for(Item i : itemList){
			if(im == null){
				im = i;
			}else if(im.q < i.q){
				im = i;
			}
		}
		
		return im;
	}
	
	public Item getLowestQ(ArrayList<Item> itemList){
		Item im = null;
		
		for(Item i : itemList){
			if(im == null){
				im = i;
			}else if(im.q > i.q){
				im = i;
			}
		}
		
		return im;
	}
	
	public int countItemsInBag(String name){
		return countItemsInBagExact(name, false);
	}
	
	public int countItemsInBagExact(String name, boolean exact){
		int countItems = 0;
		Inventory bag = getInventory("Inventory");
		
		if(bag == null)	return countItems;
		
		for(Widget wdg = bag.child; wdg != null; wdg = wdg.next){
			if(wdg instanceof Item){
				if(!exact && ((Item)wdg).GetResName().contains(name)){
					countItems++;
				}else if(exact && ((Item)wdg).GetResName().equals(name)){
					countItems++;
				}
			}
		}
		return countItems;
	}
	
	public int countItemsInInv(String name, Inventory inv){
		int countItems = 0;
		
		if(inv == null)	return countItems;
		
		for(Widget wdg = inv.child; wdg != null; wdg = wdg.next){
			if(wdg instanceof Item){
				if(((Item)wdg).GetResName().contains(name)){
					countItems++;
				}
			}
		}
		return countItems;
	}
	
	public Item getItemFromInventory(Inventory inv, String name){
		Item i = null;
		if(inv == null) return i;
		
		for(Widget wdg = inv.child; wdg != null; wdg = wdg.next){
			if(wdg instanceof Item){
				if(((Item)wdg).GetResName().contains(name)){
					i = (Item)wdg;
				}
			}
		}
		return i;
	}
	
	public boolean mouseHoldingAnItem(){
		if(getMouseItem() == null) return false;
		
		return true;
	}
	
	public Item getMouseItem(){
		Widget root = m_ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if(w instanceof Item) return (Item)w;
		}
		
		return null;
	}
	
	public void dropItemOnGround(Item item){
		if(item == null) return;
		item.wdgmsg("drop", new Object[]{Coord.z});
	}
	
	public void transferItem(Item item){
		if(item == null) return;
		item.wdgmsg("transfer", new Object[]{Coord.z});
	}
	
	public void itemInteract(Item item){
		if(item == null) return;
		item.wdgmsg("itemact", 0);
	}
	
	public void itemInteractShiftMod(Item item){
		if(item == null) return;
		item.wdgmsg("itemact", 1);
	}
	
	public void dropItemInBag(Coord c){
		Inventory bag = getInventory("Inventory");
		if(bag == null){
			return;
		}
		
		bag.drop(new Coord(0,0), c);
	}
	
	public void dropHoldingItem(){
		Item i = getMouseItem();
		
		if(i == null) return;
		
		i.dropon(i.parent, Coord.z);
	}
	
	public void dropItemInInv(Coord c, Inventory inv){
		if(inv == null){
			return;
		}
		
		inv.drop(new Coord(0,0), c);
	}
	
	/*public void forceDropItemInInv(Coord c, Inventory inv){
		if(inv == null){
			return;
		}
		
		inv.drop(new Coord(0,0), c);
	}*/
	
	public void useItem(Item item){
		if(item == null){
			return;
		}
		Resource res = item.res.get();
		m_ui.mnu.use(res);
	}
	
	public void itemAction(Item item){
		if(item == null){
			return;
		}
		item.wdgmsg("iact", new Coord(0,0));
	}
	
	public void clickBagItem(Item item, int button){
		if(item == null){
			return;
		}
		item.mousedown(new Coord(0,0), button);
	}
	
	public void pickUpItemDropsafe(Item i){
		if(i == null){
			return;
		}
		disableMouseItem = true;
		i.wdgmsg("take", new Object[]{Coord.z});
	}
	
	public void pickUpItem(Item i){
		if(i == null){
			return;
		}
		i.wdgmsg("take", new Object[]{Coord.z});
	}
	
	public void pickUpItem(Coord c, Inventory inv){
		ArrayList<Item> itemList = getItemsFromInv(inv);
		
		if(c == null || itemList == null){
			return;
		}
		
		Item i = null;
		
		for(Item itm : itemList){
			
			if(itm.c.equals(c) ){
				i = itm;
				break;
			}
		}
		
		pickUpItem(i);
	}
	

	public void itemAction(Coord location){
		if(!mouseHoldingAnItem())
			return;
		
		m_ui.mainview.wdgmsg("itemact", new Coord(200,150), location, 0);
	}
	
	public void itemAction(Coord location, int mod){
		if(!mouseHoldingAnItem())
			return;
		m_ui.mainview.wdgmsg("itemact", new Coord(200,150), location, mod);
	}
	
	public void transferItemTo(Inventory inv, int mod){
		inv.wdgmsg("xfer", 1, mod);
	}
	
	public void transferItemFrom(Inventory inv, int mod){
		inv.wdgmsg("xfer", -1, mod);
	}
	
	public int getPlayerBagSpace(){
		Inventory bag = getInventory("Inventory");
		
		if(bag == null)
			return -1;
		
		int items = itemCount(bag);
		int bagSize = bag.isz.x * bag.isz.y;
		
		return bagSize - items;
	}
	
	public int getInvSpace(Inventory inv){
		if(inv == null)
			return -1;
		
		int items = itemCount(inv);
		int invSize = inv.isz.x * inv.isz.y;
		
		return invSize - items;
	}
	
	public int getPlayerBagSize(){
		Inventory bag = getInventory("Inventory");
		
		if(bag == null)
			return -1;
		
		int bagSize = bag.isz.x * bag.isz.y;
		
		return bagSize;
	}
	
	public int getPlayerBagItems(){
		Inventory bag = getInventory("Inventory");
		
		if(bag == null)
			return -1;
		
		int items = itemCount(bag);
		
		return items;
	}
	
	public int itemCount(Inventory inv){
		int count = 0;
		
		for(Widget item = inv.child; item != null; item = item.next){
			if(item instanceof Item){
				int size = (item.sz.x / 30) * (item.sz.y / 30);
				count += size;
			}
		}
		return count;
		
	}
	
	public int invItemCount(Inventory inv){
		int count = 0;

		for(Widget item = inv.child; item != null; item = item.next){
			if(item instanceof Item){
				count++;
			}
		}
		return count;
	}
	
	public boolean safeItemTransferTo(Inventory inv){
		Inventory bag = getInventory("Inventory");
		
		if(bag == null || inv == null) return true;
		
		return !safeItemTransfer("gfx", bag, inv);
	}
	
	public boolean safeItemTransferFrom(Inventory inv){
		Inventory bag = getInventory("Inventory");
		
		if(bag == null || inv == null) return true;
		
		return safeItemTransfer("gfx", inv, bag);
	}
	
	public boolean safeItemTransfer(String name, Inventory invFrom, Inventory invTo){
		boolean full = false;
		ArrayList<Item> itemList = getItemsFromInv(name, invFrom);
		
		if(itemList.size() == 0) return false;
		
		ArrayList<Item> itemListSorted = new ArrayList<Item>();
		
		while(itemList.size() > 0){
			Item i = null;
			
			for(Item itm : itemList){
				if(i == null || i.sz.y < itm.sz.y)
					i = itm;
			}
			
			itemListSorted.add(i);
			itemList.remove(i);
		}
		
		ArrayList<Coord> coords = emptyItemArray(invTo, itemListSorted);
		int count = 0;
		for(int q = 0; q < itemListSorted.size(); q++){
			Item i = itemListSorted.get(q);
			
			Coord c = coords.get(q);
			
			if(c == null){
				full = true;
				continue;
			}
			count++;
			pickUpItem(i);
			dropItemInInv(c, invTo);
		}
		
		if(count > 48) System.out.println("overflow");
		return full;
	}
	
	public boolean emptyItemSlotOpen(Inventory inv, Coord slot, Item droper){
		if(inv == null)
			return false;
		
		if(droper == null)
			return false;
		
		ArrayList<Coord> invArray = new ArrayList<Coord>();
		
		ArrayList<Item> itemList = getItemsFromInv(inv);
		for(Item item : itemList){
			int x = item.c.x / 30;
			int y = item.c.y / 30;
			int width = item.sz.x / 30;
			int hight = item.sz.y / 30;
			
			for(int i = 0; i < width; i++){
				for(int j = 0; j < hight; j++){
					invArray.add(new Coord(x + i,y + j) );
				}
			}
		}
		
		return !overlapDropItem(slot.x, slot.y, droper, invArray);
	}
	
	public Coord emptyItemSlot(Inventory inv){
		if(inv == null)
			return null;
		
		Item dropItem = getMouseItem();
		
		if(dropItem == null)
			return null;
		
		ArrayList<Coord> invArray = new ArrayList<Coord>();
		
		ArrayList<Item> itemList = getItemsFromInv(inv);
		for(Item item : itemList){
			int x = item.c.x / 30;
			int y = item.c.y / 30;
			int width = item.sz.x / 30;
			int hight = item.sz.y / 30;
			
			for(int i = 0; i < width; i++){
				for(int j = 0; j < hight; j++){
					invArray.add(new Coord(x + i,y + j) );
				}
			}
		}
		
		int rangeX = inv.isz.x + 1 - (dropItem.sz.x / 30);
		int rangeY = inv.isz.y + 1 - (dropItem.sz.y / 30);
		
		for(int y = 0; y < rangeY; y++){
			for(int x = 0; x < rangeX; x++){
				if(!overlapDropItem(x, y, dropItem, invArray))
					return new Coord(1+x*31, 1+y*31);
			}
		}
		
		return null;
	}
	
	public ArrayList<Coord> emptyItemArray(Inventory inv, ArrayList<Item> dropList){
		ArrayList<Coord> dropSlots = new ArrayList<Coord>();
		
		if(inv == null)
			return null;
		
		if(dropList == null)
			return null;
		
		ArrayList<Coord> invArray = new ArrayList<Coord>();
		
		ArrayList<Item> itemList = getItemsFromInv(inv);
		for(Item item : itemList){
			int x = item.c.x / 30;
			int y = item.c.y / 30;
			int width = item.sz.x / 30;
			int hight = item.sz.y / 30;
			
			for(int i = 0; i < width; i++){
				for(int j = 0; j < hight; j++){
					invArray.add(new Coord(x + i,y + j) );
				}
			}
		}
		
		for(Item dropItem : dropList){
			if(dropItem == null){
				dropSlots.add(null);
				continue;
			}
			boolean found = false;
			int itemSizeX = (dropItem.sz.x / 30);
			int itemSizeY = (dropItem.sz.y / 30);
			int rangeX = inv.isz.x + 1 - itemSizeX;
			int rangeY = inv.isz.y + 1 - itemSizeY;
			
			for(int y = 0; y < rangeY && !found; y++){
				for(int x = 0; x < rangeX && !found; x++){
					if(!overlapDropItem(x, y, dropItem, invArray)){
						dropSlots.add(new Coord(1+x*31, 1+y*31) );
						
						for(int i = 0; i < itemSizeX; i++){
							for(int j = 0; j < itemSizeY; j++){
								invArray.add(new Coord(x + i,y + j) );
							}
						}
						
						found = true;
					}
				}
			}
			
			if(!found){
				dropSlots.add(null);
			}
		}
		
		return dropSlots;
	}
	
	public ArrayList<Coord> emptyItemArrayExcludeZone(Inventory inv, ArrayList<Item> dropList, Coord start, Coord end){
		ArrayList<Coord> invArray = new ArrayList<Coord>();
		ArrayList<Coord> dropSlots = new ArrayList<Coord>();
		
		//start at (0,0) for all
		//end at (7,7) for cubs
		
		if(inv == null)
			return null;
		
		if(dropList == null)
			return null;
		
		ArrayList<Item> itemList = getItemsFromInv(inv);
		for(Item item : itemList){
			int x = item.c.x / 30;
			int y = item.c.y / 30;
			int width = item.sz.x / 30;
			int hight = item.sz.y / 30;
			
			for(int i = 0; i < width; i++){
				for(int j = 0; j < hight; j++){
					invArray.add(new Coord(x + i,y + j) );
				}
			}
		}
		
		for(int i = start.x; i <= end.x; i++){
			for(int j = start.y; j <= end.y; j++){
				invArray.add(new Coord(i, j) );
			}
		}
		
		for(Item dropItem : dropList){
			if(dropItem == null){
				dropSlots.add(null);
				continue;
			}
			
			boolean found = false;
			int itemSizeX = (dropItem.sz.x / 30);
			int itemSizeY = (dropItem.sz.y / 30);
			int rangeX = inv.isz.x + 1 - itemSizeX;
			int rangeY = inv.isz.y + 1 - itemSizeY;
			
			for(int y = 0; y < rangeY && !found; y++){
				for(int x = 0; x < rangeX && !found; x++){
					if(!overlapDropItem(x, y, dropItem, invArray)){
						dropSlots.add(new Coord(1+x*31, 1+y*31) );
						
						for(int i = 0; i < itemSizeX; i++){
							for(int j = 0; j < itemSizeY; j++){
								invArray.add(new Coord(x + i,y + j) );
							}
						}
						
						found = true;
					}
				}
			}
			
			if(!found){
				dropSlots.add(null);
			}
		}
		
		return dropSlots;
	}
	
	public ArrayList<Coord> emptyItemArrayExcludeZoneHorizontalOpen(Inventory inv, ArrayList<Item> dropList, int start, int end){
		ArrayList<Coord> invArray = new ArrayList<Coord>();
		ArrayList<Coord> dropSlots = new ArrayList<Coord>();
		
		//start at (0,0) for all
		//end at (7,7) for cubs
		
		if(inv == null)
			return null;
		
		if(dropList == null)
			return null;
		
		ArrayList<Item> itemList = getItemsFromInv(inv);
		for(Item item : itemList){
			int x = item.c.x / 30;
			int y = item.c.y / 30;
			int width = item.sz.x / 30;
			int hight = item.sz.y / 30;
			
			for(int i = 0; i < width; i++){
				for(int j = 0; j < hight; j++){
					invArray.add(new Coord(x + i,y + j) );
				}
			}
		}
		
		for(int i = 0; i < 64; i++){
			if(i >= start && i <= end) continue;
			int x = i%inv.isz.x;
			int y = i/inv.isz.x;
			Coord c = new Coord(x,y);
			
			invArray.add(c);
		}
		
		for(Item dropItem : dropList){
			if(dropItem == null){
				dropSlots.add(null);
				continue;
			}
			
			boolean found = false;
			int itemSizeX = (dropItem.sz.x / 30);
			int itemSizeY = (dropItem.sz.y / 30);
			int rangeX = inv.isz.x + 1 - itemSizeX;
			int rangeY = inv.isz.y + 1 - itemSizeY;
			
			for(int y = 0; y < rangeY && !found; y++){
				for(int x = 0; x < rangeX && !found; x++){
					if(!overlapDropItem(x, y, dropItem, invArray)){
						dropSlots.add(new Coord(1+x*31, 1+y*31) );
						
						for(int i = 0; i < itemSizeX; i++){
							for(int j = 0; j < itemSizeY; j++){
								invArray.add(new Coord(x + i,y + j) );
							}
						}
						
						found = true;
					}
				}
			}
			
			if(!found){
				dropSlots.add(null);
			}
		}
		
		return dropSlots;
	}
	
	private boolean overlapDropItem(int x, int y, Item dropItem, ArrayList<Coord> invArray){
		int width = 1;
		int hight = 1;
		
		if(dropItem != null){
			width = dropItem.sz.x / 30;
			hight = dropItem.sz.y / 30;
		}
		
		for(int i = 0; i < width; i++){
			for(int j = 0; j < hight; j++){
				for(Coord c : invArray)
					if(c.equals(new Coord(x + i,y + j) ) ) return true;
			}
		}
		
		return false;
	}
	
	/////////////////////////// pathing /////////////////////////////
	
	public void walkTo(Gob g){
		walkTo(g, null);
	}
	
	public void walkTo(Gob g, ArrayList<Gob> ignoreList){
		if(stop) return;
		if(!PFrunning){
			pathing = true;
			PFrunning = true;
			PathWalker walk = new PathWalker(this, g);
			walk.m_ignoreGobs = ignoreList;
			walk.start();
		}
		
		while(PFrunning && !stop){
			wait(50);
		}
		if(stop) pathing = false;
	}
	
	public void walkToCondition(Gob g){
		if(stop) return;
		if(!PFrunning){
			pathing = true;
			PFrunning = true;
			PathWalker walk = new PathWalker(this, g);
			walk.start();
		}
	}
	
	public void walkTo(Coord c){
		if(stop) return;
		if(!PFrunning){
			pathing = true;
			PFrunning = true;
			PathWalker walk = new PathWalker(this, c);
			walk.start();
		}
		
		while(PFrunning && !stop){
			wait(50);
		}
		if(stop) pathing = false;
	}
	
	public void walkToCondition(Coord c){
		if(stop) return;
		if(!PFrunning){
			pathing = true;
			PFrunning = true;
			PathWalker walk = new PathWalker(this, c);
			walk.start();
		}
	}
	
	public void safeWalkTo(Coord c){
		while(!stop && !getPlayerCoord().equals(c) ){
			walkTo(c);
			wait(200);
			
			while(!stop && checkPlayerWalking() ) wait(200);
		}
	}
	
	public Coord walkToSurface(Coord c, Gob object){
		if(object == null) return null;
		
		PathWalker walk = null;
		if(!PFrunning){
			pathing = true;
			PFrunning = true;
			walk = new PathWalker(this, c);
			walk.m_surfaceGob = object;
			walk.start();
		}
		
		while(PFrunning && !stop){
			wait(50);
		}
		
		if(stop) pathing = false;
		
		if(walk != null)
			return walk.m_returnCoord;
		
		return null;
	}
	
	public void goToWorldCoord(int x, int y){
		goToWorldCoord(new Coord(x, y) );
	}
	
	public void goToWorldCoord(Coord c){
		Gob player = getPlayerGob();
		Coord mc = new Coord(getPlayerCoord());
		boolean redo = true;
		if(!mc.equals(c)){
			while(redo && !stop){
				redo = false;
				clickWorld(1, c);
				
				int redoCount = 0;
				while((checkPlayerWalking() || !getPlayerCoord().equals(c)) && !redo && !stop){
					wait(50);
					
					if(/*!player.miniwalk &&*/ !checkPlayerWalking() && !getPlayerCoord().equals(c)){
						redoCount++;
						if(redoCount > 100){
							redo = true;
							System.out.println("Error Walk");
						}
					}else{
						redoCount = 0;
					}
				}
			}
		}
	}
	
	public boolean objectSurf(Gob g){
		return objectSurf(g, new Rectangle() );
	}
	
	public boolean objectSurf(Gob g, Rectangle fattener){
		if(g == null) return false;
		
		while(!stop){
			Coord c = surfaceSpot(g, g.getr(), false, true, fattener);
			if(c == null) return false;
			walkToCondition(c);
			
			while(PFrunning && !stop){
				wait(100);
				
				if(!checkReachable(c, false) ){
					pathing = false;
				}
			}
			
			if(stop) pathing = false;
			
			if(!PFrunning && !checkPlayerWalking() && getPlayerCoord().equals(c) ) return true;
		}
		
		return true;
	}
	
	public boolean objectSurf(Rectangle objectRect, Coord location, boolean lineCheck, boolean pclaim){
		while(!stop){
			Coord c = surfaceSpot(objectRect, location, false, lineCheck, pclaim);
			if(c == null) return false;
			walkToCondition(c);
			
			while(PFrunning && !stop){
				wait(100);
				
				if(!checkReachable(c, false) ){
					pathing = false;
				}
			}
			
			if(stop) pathing = false;
			
			if(!PFrunning && !checkPlayerWalking() && getPlayerCoord().equals(c) ) return true;
		}
		
		return true;
	}
	
	/////////////////////// hitbox mechanics ///////////////////////
	
	public Coord surfaceSpot(Gob object, Coord objectsLocation, boolean boatTravel, boolean lineCheck, Rectangle fattener){
		ArrayList<Rectangle> allRect = getAllCorrectedHitboxes(boatTravel, false, new Gob[]{object});
		PathFinder pf = new PathFinder(this);
		Coord c = null;
		pathing = true;
		
		ArrayList<Point> sPoints = surfacePoints(gobToNegRect(object), objectsLocation, allRect, boatTravel, lineCheck, false, fattener);
		Coord player = getPlayerCoord();
		Point start = new Point(player.x, player.y);
		Point end = new Point(objectsLocation.x, objectsLocation.y);
		ArrayList<Point> path = pf.pathFind(allRect, start, end);
		
		//System.out.println(path);
		
		if(path != null && path.size() >= 2){
			//System.out.println("point found");
			Point p = path.get(path.size() - 2);
			c = new Coord(p.x, p.y);
		}else{
			c = player;
		}
		
		double dist = 0;
		Coord surfacePoint = null;
		
		for(Point s : sPoints){
			Coord surf = new Coord(s.x, s.y);
			
			if(surfacePoint == null){
				surfacePoint = surf;
				dist = surfacePoint.dist(c);
			}else if(surf.dist(c) < dist){
				surfacePoint = surf;
				dist = surfacePoint.dist(c);
			}
		}
		
		return surfacePoint;
	}
	
	public Coord surfaceSpot(Rectangle rect, Coord objectsLocation, boolean boatTravel, boolean lineCheck, boolean pclaim){
		ArrayList<Rectangle> allRect = getAllCorrectedHitboxes(boatTravel, false);
		PathFinder pf = new PathFinder(this);
		Coord c = null;
		pathing = true;
		
		ArrayList<Point> sPoints = surfacePoints(rect, objectsLocation, allRect, boatTravel, lineCheck, pclaim, new Rectangle());
		Coord player = getPlayerCoord();
		Point start = new Point(player.x, player.y);
		Point end = new Point(objectsLocation.x, objectsLocation.y);
		ArrayList<Point> path = pf.pathFind(allRect, start, end);
		
		//System.out.println("sPoints " + sPoints.size() );
		
		if(path != null && path.size() >= 2){
			Point p = path.get(path.size() - 2);
			c = new Coord(p.x, p.y);
		}else{
			c = player;
		}
		
		double dist = 0;
		Coord surfacePoint = null;
		
		synchronized(publicRects){
			publicRects.clear();
			for(Point s : sPoints){
				publicRects.add(new Rectangle(s.x, s.y, 1, 1) );
			}
		}
		
		for(Point s : sPoints){
			Coord surf = new Coord(s.x, s.y);
			
			if(surfacePoint == null){
				surfacePoint = surf;
				dist = surfacePoint.dist(c);
			}else if(surf.dist(c) < dist){
				surfacePoint = surf;
				dist = surfacePoint.dist(c);
			}
		}
		
		return surfacePoint;
	}
	
	public ArrayList<Point> surfacePoints(Rectangle rect, Coord c, ArrayList<Rectangle> allRect, boolean boatTravel, boolean lineCheck, boolean pclaim, Rectangle fattener){
		ArrayList<Point> sPoints = new ArrayList<Point>();
		double delta = 0.1;
		int meOffcet = -2;
		int meSize = 4;
		
		if(boatTravel){
			meOffcet = -14;
			meSize = 26;
		}
		
		Coord offcet = new Coord();
		Coord size = new Coord();
		
		/*if(!kritterFix(g, offcet, size) ){
			Resource.Neg neg = g.getneg();
			if(neg == null){
				//System.out.println("Error neg");
				return sPoints;
			}
			
			offcet = neg.bc;
			size = neg.bs;
		}*/
		
		offcet.x = rect.x;
		offcet.y = rect.y;
		size.x = rect.width;
		size.y = rect.height;
		
		offcet = offcet.add(meOffcet,meOffcet);
		size = size.add(meSize,meSize);
		
		for(int i = 0; i <= size.x; i++){
			for(int j = 0; j <= size.y; j++){
				if(i == 0 || i == size.x || j == 0 || j == size.y ){
					//Point p = new Point(g.getr().x + offcet.x + i, g.getr().y + offcet.y + j);
					Point p = new Point(c.x + offcet.x + i, c.y + offcet.y + j);
					boolean blocked = false;
					
					int id = getTileID(new Coord(p.x, p.y).div(11) );
					
					if(!boatTravel && (id == 0 || id == 255) ) continue;
					else if(boatTravel && id > 1) continue;
					
					if(pclaim && !boatTravel){
						int oid = getTileOL(new Coord(p.x, p.y).div(11) );
						if(oid == 1 || oid == 5) continue;
					}
					
					for(Rectangle r : allRect){
						Rectangle2D shrunk = new Rectangle2D.Double(
							(double)r.x + (double)delta - (double)fattener.x,
							(double)r.y + (double)delta - (double)fattener.y,
							(double)r.width - (double)delta*2 + (double)fattener.width*2,
							(double)r.height - (double)delta*2 + (double)fattener.height*2
						);
						
						if(shrunk.contains((double)p.x, (double)p.y) ){
							blocked = true;
							break;
						}
					}
					
					if(!blocked)
						if(!lineCheck || freePath(new Coord(p.x, p.y), c, false, allRect) )
							sPoints.add(p);
				}
			}
		}
		
		return sPoints;
	}
	
	public Rectangle gobToRect(Gob g){
		Resource.Neg neg = g.getneg();
		if(neg == null) return new Rectangle();
		return new Rectangle(g.getr().x + neg.bc.x, g.getr().y + neg.bc.y, neg.bs.x, neg.bs.y);
	}
	
	public Rectangle gobToNegRect(Gob g){
		return gobToNegRect(g, 0);
	}
	
	public Rectangle gobToNegRect(Gob g, int expanded){
		Coord offcet = new Coord();
		Coord size = new Coord();
		
		if(!kritterFix(g, offcet, size) ){
			Resource.Neg neg = g.getneg();
			if(neg == null){
				//System.out.println("Error neg");
				return null;
			}
			
			offcet = neg.bc.sub(expanded, expanded);
			size = neg.bs.add(expanded*2, expanded*2);
		}
		
		/*Resource.Neg neg = g.getneg();
		if(neg == null) return new Rectangle();*/
		
		return new Rectangle(offcet.x, offcet.y, size.x, size.y);
	}
	
	public ArrayList<Rectangle> getAllCorrectedHitboxes(boolean boatTravel, boolean claims){
		return getAllCorrectedHitboxes(boatTravel, claims, null);
	}
	
	public ArrayList<Rectangle> getAllCorrectedHitboxes(boolean boatTravel, boolean claims, Gob[] excludeList){
		Rectangle selfHitbox = null;
		
		if(boatTravel){
			selfHitbox = new Rectangle(-14, -14, 26, 26);
		}else{
			selfHitbox = new Rectangle(-2, -2, 4, 4);
		}
		
		return getAllHitboxes(boatTravel, claims, selfHitbox, excludeList);
	}
	
	public ArrayList<Rectangle> getAllHitboxes(boolean boatTravel, boolean claims){ // exluding self hitbox
		return getAllHitboxes(boatTravel, claims, null, null);
	}
	
	public ArrayList<Rectangle> getAllHitboxes(boolean boatTravel, boolean claims, Rectangle selfHitbox, Gob[] excludeList){ // exluding self hitbox
		ArrayList<Rectangle> hitbox = new ArrayList<Rectangle>();
		hitbox.addAll(getAllObjectRectangles(excludeList));
		
		if(selfHitbox != null) hitboxCorrect(hitbox, selfHitbox); // corrects hitbox arraylist without returning the object
		
		hitbox.addAll(getAllPathableTileRectangles(boatTravel, null, null));
		
		if(claims) hitbox.addAll(getAllClaimRectangles(boatTravel, false) );
		
		return hitbox;
	}
	
	public void hitboxCorrect(ArrayList<Rectangle> hitbox, Rectangle selfHitbox){
		for(Rectangle r : hitbox){
			//rects.add(new Rectangle(r.x + selfHitbox.x, r.y + selfHitbox.y, r.width + selfHitbox.width, r.height + selfHitbox.height));
			r.x += selfHitbox.x;
			r.y += selfHitbox.y;
			r.width += selfHitbox.width;
			r.height += selfHitbox.height;
		}
	}
	
	public ArrayList<Rectangle> getMiningHitboxes(Coord mined){
		ArrayList<Rectangle> hitbox = new ArrayList<Rectangle>();
		hitbox.addAll(getAllObjectRectangles(null));
		
		hitboxCorrect(hitbox, new Rectangle(-2, -2, 4, 4)); // corrects hitbox arraylist without returning the object
		
		hitbox.addAll(getAllPathableTileRectangles(false, new Coord[]{mined}, null));
		
		return hitbox;
	}
	
	public ArrayList<Rectangle> getAllObjectRectangles(Gob[] excludeList){
		Gob player = getPlayerGob();
		ArrayList<Rectangle> negRec = new ArrayList<Rectangle>();
		ArrayList<Gob> allGob = allGobs();
		for(Gob g : allGob ){
			String name = g.resname();
			
			if( g == player ) continue;
			
			if( name.contains("/plants/") ) continue;
			if( name.contains("/items/") ) continue;
			if( name.equals("gfx/terobjs/trees/log") ) continue;
			if( name.equals("gfx/terobjs/blood") ) continue;
			if( name.equals("gfx/terobjs/claim") ) continue;
			if( name.equals("gfx/terobjs/anthill-r") ) continue;
			if( (name.equals("gfx/terobjs/hearth-play") && g.getattr(KinInfo.class) == null) ) continue;
			if( (name.contains("/gates/") && g.GetBlob(0) == 2) ) continue;
			if(objectLifted(g) ) continue;
			
			if(excludeList != null){
				boolean exclude = false;
				for(Gob test : excludeList){
					if(g.id == test.id){
						exclude = true;
						break;
					}
				}
				if(exclude) continue;
			}
			
			Coord offcet = new Coord();
			Coord size = new Coord();
			
			if(!kritterFix(g, offcet, size) ){
				Resource.Neg neg = g.getneg();
				if(neg == null) continue;
				
				offcet = neg.bc;
				size = neg.bs;
			}
			
			if(size.x != 0){
				Rectangle rect = new Rectangle(g.getr().x + offcet.x, 
												g.getr().y + offcet.y, 
												size.x, size.y);
				negRec.add(rect);
			}
		}
		
		return negRec;
	}
	
	public ArrayList<Rectangle> getAllPathableTileRectangles(boolean boatTravel, Coord[] editList, Integer[] includeList){
		Gob player = getPlayerGob();
		ArrayList<Coord> filterdTiles = tileFilter(player.getr(), boatTravel, editList, includeList);
		return mergeFilterdTiles(filterdTiles);
	}
	
	public ArrayList<Rectangle> getAllClaimRectangles(boolean boatTravel, boolean village){
		long time = System.currentTimeMillis();
		ArrayList<Rectangle> claims = new ArrayList<Rectangle>();
		ArrayList<Rectangle> merge = new ArrayList<Rectangle>();
		if(boatTravel) return claims;
		
		Coord player = getPlayerCoord();
		Coord gc = player.div(1100).add(-1,-1);
		
		for(int My = 0; My < 3; My++){
			for(int Mx = 0; Mx < 3; Mx++){
				synchronized(m_ui.mainview.map.grids){
					MCache.Grid gd = m_ui.mainview.map.grids.get(gc.add(Mx,My) );
					if(gd == null) continue;
					
					for(MCache.Overlay lol : gd.ols) {
						if(lol.mask == 1 || lol.mask == 5 || (village && lol.mask == 4) ){
							Coord ul = gc.add(Mx,My).mul(1100).add(lol.c1.mul(11) );
							Coord sz = lol.c2.sub(lol.c1).add(new Coord(1, 1)).mul(11);
							if(lol.mask == 4)
								claims.add(new Rectangle(ul.x, ul.y, sz.x, sz.y) );
							else
								merge.add(new Rectangle(ul.x, ul.y, sz.x, sz.y) );
						}
					}
				}
			}
		}
		
		while(merge.size() > 0){
			Rectangle r = merge.get(0);
			merge.remove(r);
			
			ArrayList<Rectangle> filter = new ArrayList<Rectangle>();
			filter.add(r);
			while(true){
				Rectangle a = null;
				
				for(Rectangle k : filter){
					for(Rectangle j : merge){
						if(k.intersects(new Rectangle(j.x-1, j.y-1, j.width+2, j.height+2) ) ){
							a = j;
							break;
						}
					}
				}
				
				if(a == null) break;
				
				
				merge.remove(a);
				filter.add(a);
			}
			
			int x = 0;
			int y = 0;
			int width = 0;
			int height = 0;
			
			for(Rectangle i : filter){
				if(i != r ) r.add(i);
			}
			
			claims.add(new Rectangle(r.x-1, r.y-1, r.width+1, r.height+1) );
		}
		
		//System.out.println("Claim compleated " + (System.currentTimeMillis() - time) );
		return claims;
	}
	
	public boolean kritterFix(Gob g, Coord offcet, Coord size){ // fix for those pesky bugs
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
	
	public boolean checkReachable(Coord coordCheck, boolean boatTravel){
		double delta = 0.1;
		
		int id = getTileID(coordCheck.div(11));
		
		if(!boatTravel){
			if(id == 255 || id == 0){
				return false;
			}
		}else if(boatTravel){
			if(id > 1 || id < 0){
				return false;
			}
		}
		
		ArrayList<Rectangle> rect = getAllCorrectedHitboxes(boatTravel, false);
		//ArrayList<Rectangle2D> rect2d = ArrayList<Rectangle2D>();
		
		for(Rectangle r : rect){
			Rectangle2D shrunk = new Rectangle2D.Double(
				(double)r.x + (double)delta,
				(double)r.y + (double)delta,
				(double)r.width - (double)delta*2,
				(double)r.height - (double)delta*2
			);
			
			if(shrunk.contains((double)coordCheck.x, (double)coordCheck.y) ) return false;
			//if(r.contains(coordCheck.x, coordCheck.y) ) return false;
		}
		
		return true;
	}
	
	public boolean checkReachable(Coord coordCheck, boolean boatTravel, ArrayList<Rectangle> rect){
		double delta = 0.1;
		
		int id = getTileID(coordCheck.div(11));
		
		if(!boatTravel){
			if(id == 255 || id == 0){
				return false;
			}
		}else if(boatTravel){
			if(id > 1 || id < 0){
				return false;
			}
		}
		
		//ArrayList<Rectangle2D> rect2d = ArrayList<Rectangle2D>();
		
		for(Rectangle r : rect){
			Rectangle2D shrunk = new Rectangle2D.Double(
				(double)r.x + (double)delta,
				(double)r.y + (double)delta,
				(double)r.width - (double)delta*2,
				(double)r.height - (double)delta*2
			);
			
			if(shrunk.contains((double)coordCheck.x, (double)coordCheck.y) ) return false;
			//if(r.contains(coordCheck.x, coordCheck.y) ) return false;
		}
		
		return true;
	}
	
	public boolean freePath(Coord From, Coord To, boolean boatTravel){
		double delta = 0.1;
		
		ArrayList<Rectangle> rect = getAllCorrectedHitboxes(boatTravel, false);
		Line2D.Double line = new Line2D.Double(From.x, From.y, To.x, To.y);
		
		for(Rectangle r : rect){
			Rectangle2D shrunk = new Rectangle2D.Double(
				(double)r.x + (double)delta,
				(double)r.y + (double)delta,
				(double)r.width - (double)delta*2,
				(double)r.height - (double)delta*2
			);
			
			if(line.intersects(shrunk) && !shrunk.contains(From.x, From.y) && !shrunk.contains(To.x, To.y) ) return false;
		}
		
		return true;
	}
	
	public boolean freePath(Coord From, Coord To, boolean boatTravel, ArrayList<Rectangle> rect){
		double delta = 0.1;
		
		Line2D.Double line = new Line2D.Double(From.x, From.y, To.x, To.y);
		
		for(Rectangle r : rect){
			Rectangle2D shrunk = new Rectangle2D.Double(
				(double)r.x + (double)delta,
				(double)r.y + (double)delta,
				(double)r.width - (double)delta*2,
				(double)r.height - (double)delta*2
			);
			
			if(line.intersects(shrunk) /*&& !shrunk.contains(From.x, From.y) && !shrunk.contains(To.x, To.y)*/ ) return false;
		}
		
		return true;
	}
	
	public boolean boxFree(Coord start, Gob box){
		double delta = 0.1;
		ArrayList<Rectangle> rects = getAllHitboxes(false, false);
		
		Resource.Neg neg = box.getneg();
		
		Rectangle boxR = new Rectangle(neg.bc.x + start.x, neg.bc.y + start.y, neg.bs.x, neg.bs.y);
		
		Rectangle2D boxShrunk = new Rectangle2D.Double(
			(double)boxR.x + (double)delta,
			(double)boxR.y + (double)delta,
			(double)boxR.width - (double)delta*2,
			(double)boxR.height - (double)delta*2
		);
		
		boolean free = true;
		
		if(idRectCheck(boxR) ){
			for(Rectangle r : rects){
				Rectangle2D shrunk = new Rectangle2D.Double(
					(double)r.x + (double)delta,
					(double)r.y + (double)delta,
					(double)r.width - (double)delta*2,
					(double)r.height - (double)delta*2
				);
				
				if(boxShrunk.intersects(shrunk) ){
					free = false;
					break;
				}
			}
		}else{
			free = false;
		}
		
		//System.out.println(i + " " + j + " " + segmentLength);
		//System.out.println("X compleated. " + (System.currentTimeMillis() - time) );
		return free;
	}
	
	public boolean checkMiningCollision(Coord mined, Coord From, Coord To){
		ArrayList<Rectangle> rects = getMiningHitboxes(mined);
		double delta = 0.1;
		
		Line2D.Double line = new Line2D.Double(From.x, From.y, To.x, To.y);
		
		for(Rectangle r : rects){
			Rectangle2D shrunk = new Rectangle2D.Double(
				(double)r.x + (double)delta,
				(double)r.y + (double)delta,
				(double)r.width - (double)delta*2,
				(double)r.height - (double)delta*2
			);
			
			if(line.intersects(shrunk) ) return true;
		}
		
		return false;
	}
	
	public ArrayList<Rectangle> mergeFilterdTiles(ArrayList<Coord> tiles){
		ArrayList<Rectangle> filter = new ArrayList<Rectangle>();
		ArrayList<Coord> xlist = new ArrayList<Coord>();
		Coord prime = null;
		Coord sec = null;
		//long time = System.currentTimeMillis();
		
		//ArrayList<Coord> ysort = sortTiles(tiles, false);
		
		for(Coord c : tiles){
			if(prime == null){
				prime = c;
				sec = c;
			}else if(prime.y != c.y || !c.add(-1,0).equals(sec) ){
				if(!prime.equals(sec)){
					addToFilterRect(filter, prime, sec);
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
				addToFilterRect(filter, prime, sec);
			}else{
				xlist.add(prime);
			}
		}
		//System.out.println("X compleated. " + (System.currentTimeMillis() - time) );
		
		ArrayList<Coord> xsort = sortTiles(xlist, true);
		//System.out.println("Sort compleate. " + (System.currentTimeMillis() - time) );
		
		prime = null;
		sec = null;
		
		for(Coord c : xsort){
			if(prime == null){
				prime = c;
				sec = c;
			}else if(prime.x != c.x || !c.add(0,-1).equals(sec) ){
				addToFilterRect(filter, prime, sec);
				
				prime = c;
				sec = c;
			}else{
				sec = c;
			}
		}
		
		if(prime != null && sec != null)
			addToFilterRect(filter, prime, sec);
		//System.out.println("Y Completed. " + (System.currentTimeMillis() - time) );
		return filter;
	}
	
	public ArrayList<Coord> sortTiles(ArrayList<Coord> tiles, boolean Xsort){
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
	
	public void addToFilterRect(ArrayList<Rectangle> filter, Coord prime, Coord sec){
		Rectangle r = new Rectangle(prime.x*11-1,
									prime.y*11-1,
									(sec.x-prime.x)*11+12,
									(sec.y-prime.y)*11+12 );
		
		filter.add(r);
	}
	
	public ArrayList<Coord> tileFilter(Coord player, boolean waterPath, Coord[] editList, Integer[] includeList){
		int[][] tileList = new int[300][300];
		ArrayList<Coord> filter = new ArrayList<Coord>();
		
		Coord gc = player.div(1100).add(-1,-1);
		
		for(int My = 0; My < 3; My++){
			for(int Mx = 0; Mx < 3; Mx++){
				synchronized(m_ui.mainview.map.grids){
					MCache.Grid gd = m_ui.mainview.map.grids.get(gc.add(Mx,My) );
					
					for(int j = 0; j < 100; j++){
						for(int i = 0; i < 100; i++){
							int x = i+(Mx*100);
							int y = j+(My*100);
							if(editList != null){
								for(Coord c : editList){
									if(gc.mul(100).add(x,y).equals(c) ){
										if(includeList == null)
											tileList[x][y] = 24;
										else
											tileList[x][y] = includeList[0];
										
										break;
									}
								}
							}
							
							if(gd != null){
								tileList[x][y] = gd.tiles[i][j];
							}else{
								tileList[x][y] = -1;
							}
						}
					}
				}
			}
		}
		
		for(int Ny = 1; Ny < 299; Ny++){
			for(int Nx = 1; Nx < 299; Nx++){
				int test = tileList[Nx][Ny];
				
				if(test == -1) continue;
				
				if(includeList != null){
					boolean match = false;
					for(int includeID : includeList){
						if(test == includeID){
							match = true;
							break;
						}
					}
					if(match) continue;
				}else{
					if(waterPath && test < 2) continue;
					else if(!waterPath && test < 255 && test > 0) continue;
				}
				
				//int a = -1;
				int a = 0;
				int b = -1;
				for(int i = 0; i < 4 /*8*/; i++){
					int Bx = Nx+a;
					int By = Ny+b;
					
					int id = tileList[Bx][By];
					
					if(includeList != null){
						boolean match = false;
						for(int includeID : includeList){
							if(id == includeID){
								Coord c = gc.mul(100).add(Nx,Ny);
								filter.add(c);
								match = true;
								break;
							}
						}
						if(match) break;
					}else if(waterPath && id >= 0 && id < 2){
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
		
		return filter;
	}
	
	///////////////////////// tiles /////////////////////////
	
	public int getTileID(Coord c){ // note all tiles are taken by any subtile divided by 11 example: "Coord tileCoord = subtileCoord.div(11);"
		return m_ui.mainview.map.gettilen(c);
	}
	
	public int getTileOL(Coord tc){
		return m_ui.mainview.map.getol(tc);
	}
	
	public ArrayList<Coord> getTilesInRegionYaxies(Coord pos1, Coord pos2, int toggle){
		ArrayList<Coord> list = new ArrayList<Coord>();
		
		Coord p1 = pos1.div(11);
		Coord p2 = pos2.div(11);
		
		int smallestX = p1.x;
		int largestX = p2.x;
		if(p2.x < p1.x){
			smallestX = p2.x;
			largestX = p1.x;
		}
		int smallestY = p1.y;
		int largestY = p2.y;
		if(p2.y < p1.y){
			smallestY = p2.y;
			largestY = p1.y;
		}
		
		int i = toggle;
		for( int x = largestX; x >= smallestX ; x-- ){
			if(i > 0){
				for( int y = largestY; y >= smallestY ; y-- ){
					Coord tc = new Coord(x , y);
					list.add(tc);
				}
			}else{
				for( int y = smallestY; y <= largestY ; y++ ){
					Coord tc = new Coord(x , y);
					list.add(tc);
				}
			}
			i *= -1;
		}
		
		return list;
	}
	
	public ArrayList<Coord> getVoidTiles(Coord pos1, Coord pos2){
		ArrayList<Coord> sortL = new ArrayList<Coord>();
		
		for( Coord i : getTilesInRegion(pos1, pos2, 1) ){
			int tileID = getTileID(i);
			if(tileID == 255){
				sortL.add(i);
			}
		}
		return sortL;
	}
	
	public ArrayList<Coord> getPlowTiles(Coord pos1, Coord pos2, boolean reverse){
		ArrayList<Coord> sortL = new ArrayList<Coord>();
		ArrayList<Coord> reverseL = new ArrayList<Coord>();
		
		for( Coord i : getTilesInRegion(pos1, pos2, 1) ){
			int tileID = getTileID(i);
			if(tileID >= 10 && tileID <= 15){ //stone paving 8, plowed 9
				sortL.add(i);
			}
		}
		
		if(reverse){
			for(Coord c : sortL)
				reverseL.add(0, c);
				
			return reverseL;
		}
		
		return sortL;
	}
	
	public ArrayList<Coord> getPaveTiles(Coord pos1, Coord pos2){
		ArrayList<Coord> sortL = new ArrayList<Coord>();
		
		for( Coord i : getTilesInRegion(pos1, pos2, 1) ){
			int tileID = getTileID(i);
			if((tileID >= 13 && tileID <= 15) || tileID == 9 || tileID == 19){
				sortL.add(i);
			}
		}
		return sortL;
	}
	
	public ArrayList<Coord> getUglyTiles(Coord pos1, Coord pos2){
		ArrayList<Coord> sortL = new ArrayList<Coord>();
		
		for( Coord i : getTilesInRegion(pos1, pos2, 1) ){
			int tileID = getTileID(i);
			if(tileID >= 14 && tileID <= 15){
				sortL.add(i);
			}
		}
		return sortL;
	}
	
	public ArrayList<Coord> getFarmTiles(Coord pos1, Coord pos2){
		ArrayList<Coord> sortL = new ArrayList<Coord>();
		
		for( Coord i : getTilesInRegion(pos1, pos2, 1) ){
			int tileID = getTileID(i);
			if(tileID == 9){
				sortL.add(i);
			}
		}
		return sortL;
	}
	
	public ArrayList<Coord> getFarmTilesV2(Coord pos1, Coord pos2){
		ArrayList<Coord> sortL = new ArrayList<Coord>();
		
		for( Coord i : getTilesInRegion(pos1, pos2, 1) ){
			int tileID = getTileID(i);
			if(tileID == 9 || (tileID >= 13 && tileID <= 15) ){
				sortL.add(i);
			}
		}
		return sortL;
	}
	
	public boolean getNullTiles(Coord pos1, Coord pos2){
		ArrayList<Coord> sortL = new ArrayList<Coord>();
		
		for( Coord i : getTilesInRegion(pos1, pos2, 1) ){
			int tileID = getTileID(i);
			if(tileID == -1){
				//System.out.println("null tile found");
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<Coord> getTilesInRegion(Coord pos1, Coord pos2, int toggle){
		ArrayList<Coord> list = new ArrayList<Coord>();
		
		Coord p1 = pos1.div(11);
		Coord p2 = pos2.div(11);
		
		int smallestX = p1.x;
		int largestX = p2.x;
		if(p2.x < p1.x){
			smallestX = p2.x;
			largestX = p1.x;
		}
		int smallestY = p1.y;
		int largestY = p2.y;
		if(p2.y < p1.y){
			smallestY = p2.y;
			largestY = p1.y;
		}
		
		int i = toggle;
		for( int y = largestY; y >= smallestY ; y-- ){
			if(i > 0){
				for( int x = largestX; x >= smallestX ; x-- ){
					Coord tc = new Coord(x , y);
					list.add(tc);
				}
			}else{
				for( int x = smallestX; x <= largestX ; x++ ){
					Coord tc = new Coord(x , y);
					list.add(tc);
				}
			}
			i *= -1;
		}
		
		return list;
	}
	
	public ArrayList<Line> getLandWaterTiles(boolean returnWaterTiles){ // false will return the land tiles
		int[][] tileList = new int[300][300];
		ArrayList<Line> filter = new ArrayList<Line>();
		
		Coord player = getPlayerCoord();
		Coord gc = player.div(1100).add(-1,-1);
		
		for(int My = 0; My < 3; My++){
			for(int Mx = 0; Mx < 3; Mx++){
				synchronized(m_ui.mainview.map.grids){
					MCache.Grid gd = m_ui.mainview.map.grids.get(gc.add(Mx,My) );
					
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
		}
		
		for(int Ny = 1; Ny < 299; Ny++){
			for(int Nx = 1; Nx < 299; Nx++){
				int test = tileList[Nx][Ny];
				
				if(test == -1) continue;
				
				if(returnWaterTiles && test > 1) continue;
				else if(!returnWaterTiles && (test < 2 || 254 < test) ) continue;
				
				int a = -1;
				int b = -1;
				for(int i = 0; i < 8; i++){
					int Bx = Nx+a;
					int By = Ny+b;
					
					int id = tileList[Bx][By];
					//System.out.println("id: "+ id);
					
					if(returnWaterTiles && id > 1 && id < 255){ // grater then 1 water and smaller then void 255
						Coord c = gc.mul(100).add(Nx,Ny);
						Coord d = gc.mul(100).add(Bx,By);
						Line e = new Line(c, d);
						filter.add(e);
						break;
					}else if(!returnWaterTiles && id < 2 && id > -1){ // smaller then 2 land and bigger then void -1
						Coord c = gc.mul(100).add(Nx,Ny);
						Coord d = gc.mul(100).add(Bx,By);
						Line e = new Line(c, d);
						filter.add(e);
						break;
					}
					
					a++;
					if(a == 0 && b == 0) a++;
					if(a > 1){ b++; a = -1;}
				}
			}
		}
		
		return filter;
	}
	
	public Line closestLand(Coord from, boolean claim){
		ArrayList<Line> landCoords = getLandWaterTiles(true);
		ArrayList<Rectangle> waterRects = getAllCorrectedHitboxes(true, claim);
		
		while(landCoords.size() > 0 && !stop){
			Line closest = null;
			double dist = 0;
			for(Line l : landCoords){
				if(closest == null){
					closest = l;
					dist = l.c1.mul(11).add(5,5).dist(from);
				}else if(l.c1.mul(11).add(5,5).dist(from) < dist){
					closest = l;
					dist = l.c1.mul(11).add(5,5).dist(from);
				}
			}
			landCoords.remove(closest);
			Coord spot1 = closest.c1.mul(11).add(5,5);
			Coord spot2 = closest.c2.mul(11).add(5,5);
			
			if(checkReachable(spot1, true, waterRects)){
				return new Line(spot1, spot2);
			}
		}
		
		return null;
	}
	
	///////////////////////// sorting ////////////////////////////
	
	public ArrayList<Gob> sortGobList1(ArrayList<Gob> list){
		ArrayList<Gob> sorted = new ArrayList<Gob>();
		ArrayList<Gob> temp = new ArrayList<Gob>();
		int sY;
		int toggle = 1;
		while(list.size() > 0)
		{
			sY = largestY(list);
			for(int i = 0; i < list.size(); i++)
			{
				Gob g = list.get(i);
				if(g.getr().y == sY)
				{
					temp.add(g);
				}
			}
			
			while(temp.size() > 0)
			{
				Gob g = sortXY(temp, true, toggle);
				sorted.add(g);
				list.remove(g);
				temp.remove(g);
			}
			toggle *= -1;
		}
		
		return sorted;
	}
	
	public ArrayList<Gob> superSortGobList(ArrayList<Gob> list, boolean XnotY, boolean folowAxies, boolean innerRowFolowAxies){
		if(list == null) return null;
		ArrayList<Gob> sorted = new ArrayList<Gob>();
		ArrayList<Gob> temp = new ArrayList<Gob>();
		int toggle = 1;
		if(innerRowFolowAxies) toggle = -1;
		
		int sL;
		while(list.size() > 0)
		{
			sL = largeSmallXY(list, XnotY, folowAxies);
			
			for(int i = 0; i < list.size(); i++)
			{
				Gob g = list.get(i);
				if(g.getr().x == sL && XnotY) temp.add(g);
				if(g.getr().y == sL && !XnotY) temp.add(g);
			}
			
			while(temp.size() > 0)
			{
				Gob g = sortXY(temp, !XnotY, toggle);
				sorted.add(g);
				list.remove(g);
				temp.remove(g);
			}
			
		}
		
		return sorted;
	}
	
	public ArrayList<Gob> sortZigZag(Gob firstGob){
		ArrayList<Gob> unsorted = new ArrayList<Gob>();
		ArrayList<Gob> firstSort = new ArrayList<Gob>();
		ArrayList<Gob> secondSort = new ArrayList<Gob>();
		ArrayList<Gob> zigzagSorted = new ArrayList<Gob>();
		
		unsorted = getObjects(firstGob.resname(), firstGob.getr().add(-99,-330), firstGob.getr().add(99,5));
		
		System.out.println(unsorted.size());
		
		for(Gob i : unsorted){
			if((firstGob.getr().x - 11) < i.getr().x && i.getr().x < (firstGob.getr().x + 11)){
				firstSort.add(i);
			}else if(firstGob.getr().x - 99 < i.getr().x && i.getr().x < firstGob.getr().x + 99){
				secondSort.add(i);
			}
		}
		
		boolean flipflop = true;
		while(firstSort.size() > 0 || secondSort.size() > 0){
			if(flipflop && firstSort.size() > 0){
				Gob minGob = null;
				for(Gob i : firstSort){
					if(minGob == null){
						minGob = i;
					}else if(i.getr().y > minGob.getr().y){
						minGob = i;
					}
				}
				zigzagSorted.add(minGob);
				firstSort.remove(minGob);
			}else if(!flipflop && secondSort.size() > 0){
				Gob minGob = null;
				for(Gob i : secondSort){
					if(minGob == null){
						minGob = i;
					}else if(i.getr().y > minGob.getr().y){
						minGob = i;
					}
				}
				zigzagSorted.add(minGob);
				secondSort.remove(minGob);
			}
			flipflop = !flipflop;
		}
		
		return zigzagSorted;
	}
	
	public ArrayList<Gob> sortZigZag(ArrayList<Gob> unsorted, Gob firstGob){
		ArrayList<Gob> firstSort = new ArrayList<Gob>();
		ArrayList<Gob> secondSort = new ArrayList<Gob>();
		ArrayList<Gob> zigzagSorted = new ArrayList<Gob>();
		
		for(Gob i : unsorted){
			if((firstGob.getr().x - 11) < i.getr().x && i.getr().x < (firstGob.getr().x + 11)){
				firstSort.add(i);
			}else if(firstGob.getr().x - 99 < i.getr().x && i.getr().x < firstGob.getr().x + 99){
				secondSort.add(i);
			}
		}
		
		boolean flipflop = true;
		while(firstSort.size() > 0 || secondSort.size() > 0){
			if(flipflop && firstSort.size() > 0){
				Gob minGob = null;
				for(Gob i : firstSort){
					if(minGob == null){
						minGob = i;
					}else if(i.getr().y > minGob.getr().y){
						minGob = i;
					}
				}
				zigzagSorted.add(minGob);
				firstSort.remove(minGob);
			}else if(!flipflop && secondSort.size() > 0){
				Gob minGob = null;
				for(Gob i : secondSort){
					if(minGob == null){
						minGob = i;
					}else if(i.getr().y > minGob.getr().y){
						minGob = i;
					}
				}
				zigzagSorted.add(minGob);
				secondSort.remove(minGob);
			}
			flipflop = !flipflop;
		}
		
		return zigzagSorted;
	}
	
	private int largestY(ArrayList<Gob> list){
		int largest = list.get(0).getr().y;
		for(Gob g : list)
		{
			if(largest < g.getr().y)
			{
				largest = g.getr().y;
			}
		}
		return largest;
	}
	
	private int largeSmallXY(ArrayList<Gob> list, boolean XorY, boolean folowAxes){
		int identifier = list.get(0).getr().y;
		if(XorY) identifier = list.get(0).getr().x;
		
		for(Gob g : list)
		{
			if(identifier < g.getr().x && XorY && !folowAxes) identifier = g.getr().x;
			if(identifier > g.getr().x && XorY && folowAxes) identifier = g.getr().x;
			if(identifier < g.getr().y && !XorY && !folowAxes) identifier = g.getr().y;
			if(identifier > g.getr().y && !XorY && folowAxes) identifier = g.getr().y;
		}
		return identifier;
	}
	
	private Gob sortXY(ArrayList<Gob> list, boolean XorY, int toggle){
		Gob r = list.get(0);
		if(XorY){
			for(Gob g : list)
			{
				if(toggle == 1 && r.getr().x < g.getr().x)
				{
					r = g;
				}
				else if(toggle != 1 && r.getr().x > g.getr().x)
				{
					r = g;
				}
			}
		}else{
			for(Gob g : list)
			{
				if(toggle == 1 && r.getr().y < g.getr().y)
				{
					r = g;
				}
				else if(toggle != 1 && r.getr().y > g.getr().y)
				{
					r = g;
				}
			}
		}
		return r;
	}
	
	public Coord spiralScanRectFreeSpace(Coord start, Rectangle rectCheck, int maxDist, int increment, boolean myRect){
		Coord spot = null;
		long time = System.currentTimeMillis();
		double delta = 0.1;
		ArrayList<Rectangle> rects = getAllHitboxes(false, false);
		if(myRect) rects.add(0, gobToRect(getPlayerGob() ) );
		
		Coord check = new Coord(start);
		
		int di = increment;
		int dj = 0;
		int segmentLength = 1;
		
		int i = 0;
		int j = 0;
		int segmentPassed = 0;
		
		while(!stop){
			
			boolean colision = false;
			int id = getTileID(check.div(11) );
			if(id < 255 && id > 1){
				Rectangle2D checkShrunk = new Rectangle2D.Double(
					(double)(check.x + rectCheck.x) + (double)delta,
					(double)(check.y + rectCheck.y) + (double)delta,
					(double)rectCheck.width - (double)delta*2,
					(double)rectCheck.height - (double)delta*2
				);
				
				for(Rectangle r : rects){
					Rectangle2D shrunk = new Rectangle2D.Double(
						(double)r.x + (double)delta,
						(double)r.y + (double)delta,
						(double)r.width - (double)delta*2,
						(double)r.height - (double)delta*2
					);
					
					if(checkShrunk.intersects(shrunk) ){
						colision = true;
						break;
					}
				}
			}else{
				colision = true;
			}
			
			if(!colision){
				spot = new Coord(start.x + i, start.y + j);
				break;
			}else if(maxDist < i ){
				break;
			}
			
			//////////
			i += di;
			j += dj;
			++segmentPassed;
			
			if (segmentPassed == segmentLength) {
				segmentPassed = 0;
				
				int buffer = di;
				di = -dj;
				dj = buffer;
				
				if (dj == 0) {
					++segmentLength;
				}
			}
			///////
			check.x = start.x + i;
			check.y = start.y + j;
		}
		
		//System.out.println(i + " " + j + " " + segmentLength);
		//System.out.println("X compleated. " + (System.currentTimeMillis() - time) );
		return spot;
	}
	
	public Coord circkleScanRectFreeSpace(Coord start, Rectangle rectCheck, int maxDist, int increment, boolean myRect){
		Coord checkSpot = new Coord(start);
		double delta = 0.1;
		//long time = System.currentTimeMillis();
		ArrayList<Rectangle> rects = getAllHitboxes(false, false);
		if(myRect) rects.add(0, gobToRect(getPlayerGob() ) );
		
		//Resource.Neg neg = object.getneg();
		
		int theta = 90;
		int rad = 1;
		int loops = 0;
		
		ArrayList<Coord> ignore = new ArrayList<Coord>();
		while(!stop){
			
			boolean colision = false;
			int id = getTileID(checkSpot.div(11) );
			if(id < 255 && id > 1){
				Rectangle2D boxShrunk = new Rectangle2D.Double(
					(double)(rectCheck.x + checkSpot.x) + (double)delta,
					(double)(rectCheck.y + checkSpot.y) + (double)delta,
					(double)rectCheck.width - (double)delta*2,
					(double)rectCheck.height - (double)delta*2
				);
				
				for(Rectangle r : rects){
					Rectangle2D shrunk = new Rectangle2D.Double(
						(double)r.x + (double)delta,
						(double)r.y + (double)delta,
						(double)r.width - (double)delta*2,
						(double)r.height - (double)delta*2
					);
					
					if(boxShrunk.intersects(shrunk) ){
						colision = true;
						break;
					}
				}
			}else{
				colision = true;
			}
			
			if(!colision) break;
			ignore.add(new Coord(checkSpot));
			while(ignoreFreeSpaceCheck(ignore, checkSpot) && !stop){
				double radians = Math.toRadians(theta) * increment; 
				checkSpot.x = start.x + (int)(rad*Math.cos(radians ));
				checkSpot.y = start.y + (int)(rad*Math.sin(radians ));
				
				//theta -= (int)(180 / rad);
				theta++;
				//System.out.println(theta);
				
				if(theta > 360){
					theta = 0;
					rad++;
					loops++;
					//System.out.println(loops);
				}
			}
			
			if(loops > maxDist) return null;
		}
		
		return checkSpot;
	}
	
	public boolean ignoreFreeSpaceCheck(ArrayList<Coord> ignore, Coord c){
		for(Coord check : ignore)
			if(c.equals(check) ) return true;
		
		return false;
	}
	
	public void ejectDropObject(){
		Gob lifted = getPlayerLiftedObject();
		if(lifted == null) return;
		int breakCount = 0;
		while(!stop && checkPlayerCarry() && breakCount < 4){
			Rectangle r = gobToRect(lifted);
			Coord c = spiralScanRectFreeSpaceNew(getPlayerCoord(), r, 16, 1, true);
			if(c == null) c = spiralScanRectFreeSpaceNew(getPlayerCoord(), r, 500, 1, false);
			
			if(c == null) return;
			
			if(!PFrunning){
				//System.out.println("walking");
				pathing = true;
				PFrunning = true;
				PathWalker walk = new PathWalker(this, c);
				walk.m_surfaceGob = lifted;
				walk.start();
			}else{
				pathing = false;
			}
			
			while(PFrunning && !stop){
				wait(50);
			}
			
			if(stop) pathing = false;
			
			int liftCount = 0;
			while(checkPlayerCarry() && liftCount < 10 && !stop){
				liftCount++;
				wait(100);
			}
			
			breakCount++;
		}
	}
	
	Coord spiralScanRectFreeSpaceNew(Coord start, Rectangle rectCheck, int maxDist, int increment, boolean playerRect){
		Coord spot = null;
		double delta = 0.1;
		ArrayList<Rectangle> rects = getAllHitboxes(false, false);
		if(playerRect) rects.add(0, gobToRect(getPlayerGob() ) );
		
		Rectangle check = new Rectangle(rectCheck);
		
		int di = increment;
		int dj = 0;
		int segmentLength = 1;
		
		int i = 0;
		int j = 0;
		int segmentPassed = 0;
		
		while(!stop){
			boolean colision = false;
			
			if(idRectCheck(check) ){
				Rectangle2D checkShrunk = new Rectangle2D.Double(
					(double)check.x + (double)delta,
					(double)check.y + (double)delta,
					(double)check.width - (double)delta*2,
					(double)check.height - (double)delta*2
				);
				
				for(Rectangle r : rects){
					Rectangle2D shrunk = new Rectangle2D.Double(
						(double)r.x + (double)delta,
						(double)r.y + (double)delta,
						(double)r.width - (double)delta*2,
						(double)r.height - (double)delta*2
					);
					
					if(checkShrunk.intersects(shrunk) ){
						colision = true;
						break;
					}
				}
			}else{
				colision = true;
			}
			
			if(!colision){
				spot = new Coord(start.x + i, start.y + j);
				break;
			}else if(maxDist < i ){
				break;
			}
			
			//////////
			i += di;
			j += dj;
			++segmentPassed;
			
			if (segmentPassed == segmentLength) {
				segmentPassed = 0;
				
				int buffer = di;
				di = -dj;
				dj = buffer;
				
				if (dj == 0) {
					++segmentLength;
				}
			}
			///////
			check = new Rectangle(rectCheck.x + i, rectCheck.y + j, rectCheck.width, rectCheck.height);
		}
		
		return spot;
	}
	
	public boolean idRectCheck(Rectangle rectCheck){
		Coord c = new Coord(rectCheck.x, rectCheck.y);
		if(getTileID(c.div(11) ) != 255 && getTileID(c.div(11) ) != -1){
			return true;
		}
		
		return false;
	}
	
	///////////////////////// sign transfers ///////////////////////
	
	public void signTransfer(int updown, String name) {
		signTransfer(updown, name, "gfx");
	}
	
	public void signTransfer(int updown, String name, String isBoxName) { // negative extracts
		int multiplier = 1;
		ISBox box = findSignBox(name, isBoxName);
		
		if(updown < 0){
			multiplier = -1;
		}
		if(updown > 0){
			multiplier = 1;
			updown = updown * -1;
		}
		
		for(int i = updown; i < 0 && !stop; i++){
			boolean tryAgain = true;
			int count = 0;
			
			while(tryAgain && !stop){
				tryAgain = false;
				try{
					box.wdgmsg("xfer2", multiplier, 1); 
					//if(Config.loftarFix) wait(100);
				}catch(Exception e){
					tryAgain = true;
					wait(200);
					System.out.println("ISBox error");
					box = findSignBox(name, isBoxName);
				}
				count++;
				if(count > 100){
					count = 0;
					System.out.println("Possible stuck");
				}
			}
		}
	}
	
	public void signTransferDrop(String name) {
		if(!mouseHoldingAnItem())
			return;
		
		ISBox box = findSignBox(name);
		
		boolean tryAgain = true;
		while(tryAgain && !stop){
			tryAgain = false;
			try{
				box.wdgmsg("drop");
			}catch(Exception e){
				tryAgain = true;
				wait(200);
				box = findSignBox(name);
			}
		}
			
		
	}
	
	public void signTransferTake(String name) {
		signTransferTake(name, "gfx");
	}
	
	public void signTransferTake(String name, String extraName) {
		if(mouseHoldingAnItem())
			return;
		
		ISBox box = findSignBox(name, extraName);
		
		boolean tryAgain = true;
		while(tryAgain && !stop){
			tryAgain = false;
			try{
				box.wdgmsg("click");
			}catch(Exception e){
				tryAgain = true;
				wait(200);
				box = findSignBox(name, extraName);
			}
		}
	}
	
	public int matTransfer(int ToFrom, String signName, String extraName){
		while(!windowOpen(signName) && !stop){ wait(100);}
		
		ISBox box = findSignBox(signName, extraName);
		while(box == null && !stop){
			wait(100);
			box = findSignBox(signName, extraName);
		}
		
		while(!stop){
			wait(100);
			if(box.remain != -1 && box.avail != -1 && box.built != -1) break;
		}
		
		int trans = ToFrom;
		int emptied = 0;
		if(!stop && ToFrom > 0){
			emptied = box.remain;
			if(trans > box.remain) trans = box.remain;
		}
		if(!stop && ToFrom < 0){
			emptied = box.avail;
			if((trans * -1) > box.avail) trans = box.avail * -1;
		}
		
		if(!stop) signTransfer(trans, signName, extraName);
		
		if(ToFrom < 0) ToFrom *= -1;
		if(emptied > ToFrom) emptied = ToFrom;
		
		return emptied;
	}
	
	public int matTransfer(int ToFrom, String signName, String extraName, int ignoreWinID){ // negative sucking mats, posetive filling
		while( (!windowOpen(signName) || windowID(signName) == ignoreWinID) && !stop) wait(100);
		
		ISBox box = findSignBox(signName, extraName);
		while(box == null && !stop){
			wait(100);
			box = findSignBox(signName, extraName);
		}
		
		while(!stop){
			wait(100);
			if(box.remain != -1 && box.avail != -1 && box.built != -1) break;
		}
		
		
		int trans = ToFrom;
		int matSpace = 0;
		if(!stop && ToFrom > 0){
			matSpace = box.remain;
			if(trans > box.remain) trans = box.remain;
		}
		if(!stop && ToFrom < 0){
			matSpace = box.avail;
			if((trans * -1) > box.avail) trans = box.avail * -1;
		}
		
		if(!stop) signTransfer(trans, signName, extraName);
		
		if(ToFrom < 0) ToFrom *= -1;
		if(matSpace > ToFrom) matSpace = ToFrom;
		
		return matSpace;
	}
	
	//////////////////////// tile GPS /////////////////////////
	
	/*public Coord GPStile(Coord GPSthisLocation){
		Coord c = new Coord(getPlayerCoord() );
		ArrayList<Coord> tiles = getTilesInRegion(c.sub(2300,2300), c.add(2300,2300) , 0);
		
		for(int i = 0; i < 3; i++){
			for(Coord tc : tiles){
				if(getTileID(tc) == 7){
					if(getTileID(tc.add(1,0) ) == 6 && getTileID(tc.add(-1,0) ) == 6){
						if(getTileID(tc.add(0,1) ) == 5 && getTileID(tc.add(0,-1) ) == 5){
							Coord distFromUpperLeft = new Coord(-43, -3);
							Coord worldPos = new Coord(-59, 92);
							
							Coord upperLeft = tc.add(distFromUpperLeft ).mul(11);
							Coord offcet = GPSthisLocation.sub(upperLeft);
							
							return worldPos.mul(100).add(offcet.div(11));
						}
					}
					
					if(getTileID(tc.add(1,0) ) == 5 && getTileID(tc.add(-1,0) ) == 5){
						if(getTileID(tc.add(0,1) ) == 6 && getTileID(tc.add(0,-1) ) == 6){
							Coord distFromUpperLeft = new Coord(-23, -87);
							Coord worldPos = new Coord(-56, 92);
							
							Coord upperLeft = tc.add(distFromUpperLeft ).mul(11);
							Coord offcet = GPSthisLocation.sub(upperLeft);
							
							return worldPos.mul(100).add(offcet.div(11));
						}
					}
					
					if(getTileID(tc.add(1,0) ) == 5 && getTileID(tc.add(-1,0) ) == 5){
						if(getTileID(tc.add(0,1) ) == 4 && getTileID(tc.add(0,-1) ) == 4){
							Coord distFromUpperLeft = new Coord(-97, -2);
							Coord worldPos = new Coord(-62, 92);
							
							Coord upperLeft = tc.add(distFromUpperLeft ).mul(11);
							Coord offcet = GPSthisLocation.sub(upperLeft);
							
							return worldPos.mul(100).add(offcet.div(11));
						}
					}
				}
			}
			
			if(i == 0) GPSupdate(getPlayerCoord() );
			int count = 0;
			while(!stop && count < 25){
				count++;
			}
		}
		
		return null;
	}
	
	public void GPSupdate(Coord c){
		Coord mc = c.div(1100);
		Coord requl = mc.add(-1, -1);
		Coord reqbr = mc.add(1, 1);
		Coord cgc = new Coord(0, 0);
		for(cgc.y = requl.y; cgc.y <= reqbr.y; cgc.y++) {
			for(cgc.x = requl.x; cgc.x <= reqbr.x; cgc.x++) {
				try{
					if(m_ui.mainview.map.grids.get(cgc) == null){
						m_ui.mainview.map.request(new Coord(cgc));
					}
				}catch(Exception e){
				}
			}
		}
		try{
			m_ui.mainview.map.sendreqs();
		}catch(Exception e){
		}
		
	}
	
	public Coord unGPS(int x, int y){
		return unGPS(new Coord(x, y) );
	}
	
	public Coord unGPS(Coord GPSLocation){
		Coord c = new Coord(getPlayerCoord() );
		ArrayList<Coord> tiles = getTilesInRegion(c.sub(2300,2300), c.add(2300,2300) , 0);
		
		for(int i = 0; i < 3; i++){
			for(Coord tc : tiles){
				if(getTileID(tc) == 7){
					if(getTileID(tc.add(1,0) ) == 6 && getTileID(tc.add(-1,0) ) == 6){
						if(getTileID(tc.add(0,1) ) == 5 && getTileID(tc.add(0,-1) ) == 5){
							Coord distFromUpperLeft = new Coord(-43, -3);
							Coord worldPos = new Coord(-5900, 9200);
							
							Coord upperLeftDistToGPS = GPSLocation.sub(worldPos );
							Coord upperLeftCoord = tc.add(distFromUpperLeft );
							Coord offcet = upperLeftCoord.add(upperLeftDistToGPS);
							
							return offcet.mul(11);
						}
					}
					
					if(getTileID(tc.add(1,0) ) == 5 && getTileID(tc.add(-1,0) ) == 5){
						if(getTileID(tc.add(0,1) ) == 6 && getTileID(tc.add(0,-1) ) == 6){
							Coord distFromUpperLeft = new Coord(-23, -87);
							Coord worldPos = new Coord(-5600, 9200);
							
							Coord upperLeftDistToGPS = GPSLocation.sub(worldPos );
							Coord upperLeftCoord = tc.add(distFromUpperLeft );
							Coord offcet = upperLeftCoord.add(upperLeftDistToGPS);
							
							return offcet.mul(11);
						}
					}
					
					if(getTileID(tc.add(1,0) ) == 5 && getTileID(tc.add(-1,0) ) == 5){
						if(getTileID(tc.add(0,1) ) == 4 && getTileID(tc.add(0,-1) ) == 4){
							Coord distFromUpperLeft = new Coord(-97, -2);
							Coord worldPos = new Coord(-6200, 9200);
							
							Coord upperLeftDistToGPS = GPSLocation.sub(worldPos );
							Coord upperLeftCoord = tc.add(distFromUpperLeft );
							Coord offcet = upperLeftCoord.add(upperLeftDistToGPS);
							
							return offcet.mul(11);
						}
					}
				}
			}
			
			if(i == 0) GPSupdate(getPlayerCoord() );
			int count = 0;
			while(!stop && count < 25){
				count++;
				wait(200);
			}
		}
		
		return null;
	}*/
	
	//////////////////////// ender timer ////////////////////////
	
	public boolean checkEnderTimer(String name){
		synchronized (TimerController.getInstance().timers){
			for(Timer timer : TimerController.getInstance().timers){
				if(timer.getName().contains(name) ) return true;
			}
		}
		
		return false;
	}
	
	public void createEnderTimer(String name, int seconds, int minutes, int hours){
		if(checkEnderTimer(name) ) return;
		
		long time = seconds + minutes * 60 + hours * 3600;
		
		new Timer(time, name);
		TimerController.getInstance().save();
	}
	
	public void startEnderTimer(String name){
		synchronized (TimerController.getInstance().timers){
			for(Timer timer : TimerController.getInstance().timers){
				if(timer.getName().contains(name) ) timer.start();
			}
		}
	}
	
	public void stopEnderTimer(String name){
		synchronized (TimerController.getInstance().timers){
			for(Timer timer : TimerController.getInstance().timers){
				if(timer.getName().contains(name) ) timer.stop();
			}
		}
	}
	
	/////////////////////// minor utilitys //////////////////////
	
	public Coord PLMdirection(Coord p1, Coord p2){
		int smallestX = p1.x;
		int largestX = p2.x;
		
		if(p2.x < p1.x){
			smallestX = p2.x;
			largestX = p1.x;
		}
		int smallestY = p1.y;
		int largestY = p2.y;
		if(p2.y < p1.y){
			smallestY = p2.y;
			largestY = p1.y;
		}
		
		p1 = new Coord(smallestX, smallestY);
		p2 = new Coord(largestX, largestY);
		
		Coord c = new Coord(getPlayerCoord());	
		
		if(c.x >= p1.x && c.x <= p2.x){
			if(c.y < p1.y && c.y < p2.y)
				return new Coord(0,1); // south
			else
				return new Coord(0,-1); // north
		}else if(c.y >= p1.y && c.y <= p2.y){
			if(c.x < p1.x && c.x < p2.x)
				return new Coord(1,0); // east
			else
				return new Coord(-1,0); // west
		}
		
		//if(p1.x <= c.x && c.x <= p2.x && p1.y <= c.y && c.y <= p2.y){
		if(c.x < p1.x && c.y < p1.y){
			return new Coord(1,1); // south east
		}else if(c.x > p2.x && c.y > p2.y){
			return new Coord(-1,-1); // north west
		}else if(c.x > p2.x && c.y < p1.y){
			return new Coord(-1,1); // south west
		}else if(c.x < p1.x && c.y > p2.y){
			return new Coord(1,-1); // north east
		}
		
		System.out.println("No direction found.");
		return new Coord(0,0);
	}
	
	public Coord numberDirection(int i){
		if(i == 1)
			return new Coord(0,-1); // north
		if(i == 2)
			return new Coord(1,0); // east
		if(i == 3)
			return new Coord(0,1); // south
		if(i == 4)
			return new Coord(-1,0); // west
		
		return new Coord(0,0);
	}
	
	public Coord clockwiseRotationMatrix(Coord dir){
		int x = dir.x * 0 + dir.y * -1;
		int y = dir.x * 1 + dir.y * 0;
		
		return new Coord(x,y);
	}
	
	public Coord idolDropoffSpot(Gob Vidol, int i){
		if(Vidol == null) return new Coord();
		Coord c = numberDirection(i);
		int d = 32;
		if(i == 2) d = 43;
		if(i == 4) d = 40;
		return Vidol.getr().add(c.mul(d) );
	}
	
	/////////////////////////// utility funcions /////////////////////////////
	
	public int useCR(int button, Gob CR ){ // 0 == no CR; 1 == jumped; 2 == low TW
		if(CR == null){
			System.out.println("CR is null");
			return 0;
		}
		
		if(PFrunning){
			pathing = false;
			while(PFrunning && !stop) wait(100);
		}
		
		walkTo(CR);
		
		int count = 0;
		
		while(!crossRoadTravel(0) && !stop){
			wait(200);
			
			if(flowerMenuReady() ){
				count = 0;
				flowerMenuSelect("Travel");
				while(flowerMenuReady() && !stop) wait(100);
			}else if(!checkPlayerWalking() && !crossRoadTravel(0) ){
				count++;
				if(count > 50 ){
					if(PFrunning){
						pathing = false;
						while(PFrunning && !stop) wait(100);
					}
					walkTo(CR);
					count = 0;
				}
			}else
				count = 0;
		}
		
		//System.out.println("Traveling to "+button);
		sendSlenMessage("Traveling to "+button);
		crossRoadTravel(button);
		
		count = 0;
		while(crossRoadTravel(0) && !stop){
			wait(200);
			
			count++;
			if(count > 25 ){
				crossRoadTravel(button);
				count = 0;
			}
			
			if(slenError().contains("You are too tired of travelling.") ) return 2;
		}
		//System.out.println("Traveled.");
		
		return 1;
	}
	
	public int fillArea(int itemCount, int columCount, Coord directionOfDepositFromStartC, int columDir, String signType, String signName, String extraName, int rowSize, Coord startHere, String itemName){
		Coord dir = directionOfDepositFromStartC;
		int transferSize = 56;
		//goToWorldCoord(startHere);
		boolean firstWalk = true;
		
		while(itemCount > 0 && !stop){
			Coord firstSpot = startHere.add(dir.mul((rowSize) * 11) ).add(dir.abs().swap().mul(columDir).mul(11*columCount));//
			Gob sign = findClosestObject("sign", firstSpot, firstSpot.add(dir.mul((rowSize-1) * 11).inv() ), startHere); //
			int rowCount = rowSize;
			
			//System.out.println("rowCount " + rowCount );
			
			if(sign != null){
				//firstSpot = sign.getr();
				//System.out.println(rowCounter(sign.getr(), dir, firstSpot) );
				rowCount = rowCount - rowCounter(sign.getr(), dir, firstSpot);
			}
			
			//System.out.println("rowCount " + rowCount );
			
			
			boolean first = true;
			boolean restart = false;
			while( itemCount > 0 && rowCount > 0 && !stop){
				
				if(dir.x == 0){
					//Coord c = new Coord(firstSpot.x, getPlayerCoord().y);
					Coord c = new Coord(firstSpot.x, startHere.y);
					//if(getPlayerCoord().x != c.x) goToWorldCoord(c);
					if(getPlayerCoord().x != c.x || firstWalk) walkTo(c);
				}else{
					//Coord c = new Coord(getPlayerCoord().x, firstSpot.y);
					Coord c = new Coord(startHere.x, firstSpot.y);
					//if(getPlayerCoord().y != c.y) goToWorldCoord(c);
					if(getPlayerCoord().y != c.y || firstWalk) walkTo(c);
				}
				if(firstWalk){
					firstWalk = false;
					itemCount = countItemsInBag(itemName);
				}
				
				if(sign == null){
					first = false;
					goToWorldCoord(firstSpot.add(dir.mul(7).inv() ) );
					sendAction("bp", signType);
					wait(100);
					m_ui.mainview.wdgmsg("place", firstSpot, 1, 0);
				}else{
					if(first == true){
						first = false;
						clickWorldObject(3, sign);
						
						while(!windowOpen(signName) && !stop){
							wait(100);
							if(!findObject(sign) ){
								restart = true;
								break;
							}
						}
						if(restart) break;
					}else{
						goToWorldCoord(sign.getr().add(dir.mul(18).inv() ) );
						sendAction("bp", signType);
						wait(100);
						m_ui.mainview.wdgmsg("place", sign.getr().add(dir.mul(11).inv() ), 1, 0);
					}
				}
				
				int remain = matTransfer(itemCount, signName, extraName);
				
				if(remain < transferSize)
					itemCount = itemCount - remain;
				else
					itemCount = itemCount - transferSize;
				
				while( getObjects("sign", getPlayerCoord().add(dir.mul(5)) , getPlayerCoord().add(dir.mul(5)) ).size() == 0 && !stop ) wait(100);
				
				if(!stop) sign = findClosestObject("sign", getPlayerCoord().add(dir.mul(5)) , getPlayerCoord().add(dir.mul(5)) );
				
				rowCount--;
			}
			
			if(itemCount > 0 && !restart) columCount++;
		}
		
		if(dir.y == 0){
			Coord c = new Coord(startHere.x, getPlayerCoord().y);
			if(getPlayerCoord().x != c.x) goToWorldCoord(c);
		}else{
			Coord c = new Coord(getPlayerCoord().x, startHere.y);
			if(getPlayerCoord().y != c.y) goToWorldCoord(c);
		}
		
		return columCount;
	}
	
	public int rowCounter(Coord signC, Coord dir, Coord startC){
		return startC.sub(signC).div(11).mul(dir.abs() ).abs().sum();
	}
	
	public boolean regearSlotCheck(){
		Object[] list = equipListArray();
		
		if(list == null) return true;
		
		for(int i = 0; i < 16; i++){
			Object o = list[i];
			if(i == 6 && o != null){
				return true;
			}else if(i == 7 && o != null){
				return true;
			}else if(i == 10 && o != null){
				return true;
			}
		}
		
		return false;
	}
	
	public void reGear(int gear, Gob container){
		openEquipment();
		openInventory();
		
		if(checkGear(gear) ) return;
		
		if(stop) return;
		
		String containerName = getContainerName(container);
		Inventory chestInv = walkToContainer(container, containerName);
		
		boolean unequipLoop = true;
		while(unequipLoop && !stop){
			wait(300);
			unEquipPlayer(6);
			unEquipPlayer(7);
			unEquipPlayer(10);
			
			int count = 0;
			while(unequipLoop && count < 100 && !stop){
				unequipLoop = regearSlotCheck();
				count++;
				wait(100);
			}
		}
		
		//itmNum = invItemCount(bag) - itmNum;
		
		for(int i = 0; i < 56 && !stop; i++)
			transferItemTo(chestInv, 1);
		
		//int count = 0;
		//while(count < 20 && !stop){count++; wait(100);}
		
		int redoCount = 0;
		boolean redo = true;
		while(redoCount < 10 && redo && !stop){
			redo = false;
			
			//chestInv = getInventory("Chest");
			ArrayList<Item> itemList = new ArrayList<Item>();
			itemList = getItemsFromInv(chestInv);
			
			Item axe = null;
			Item shovel = null;
			Item merchant = null;
			Item sack1 = null;
			Item sack2 = null;
			Item flask = null;
			Item waterskin = null;
			Item metalsaw = null;
			Item bucket = null;
			Item cloakhide = null;
			Item sword = null;
			Item scythe = null;
			Item fryingpan = null;
			
			for(Item i : itemList){
				if(i.GetResName().contains("axe") ) axe = i;
				if(i.GetResName().contains("shovel") ) shovel = i;
				if(i.GetResName().contains("merchant") ) merchant = i;
				if(i.GetResName().contains("sack") && sack1 == null ) sack1 = i;
				if(i.GetResName().contains("sack") && sack1 != null) sack2 = i;
				if(i.GetResName().contains("flask") ) flask = i;
				if(i.GetResName().contains("waterskin") ) waterskin = i;
				if(i.GetResName().equals("gfx/invobjs/saw") ) metalsaw = i;
				if(i.GetResName().contains("bucket") ) bucket = i;
				if(i.GetResName().contains("cloak-hide") ) cloakhide = i;
				if(i.GetResName().contains("sword") ) sword = i;
				if(i.GetResName().contains("scythe") ) scythe = i;
				if(i.GetResName().contains("fryingpan") ) fryingpan = i;
			}
			
			if(gear == 0){ //nothing
				return;
			}else if(gear == 1){ //axe only
				if(axe == null) redo = true;
				else
					autoMouseEquipPlayer(axe);
			}else if(gear == 2){ //trav set
				if(sack1 == null || sack2 == null) redo = true;
				else{
					autoMouseEquipPlayer(sack1);
					wait(100);
					autoMouseEquipPlayer(sack2);
				}
			}else if(gear == 3){ //trav merch
				if(sack1 == null || sack2 == null || merchant == null) redo = true;
				else{
					autoMouseEquipPlayer(sack1);
					wait(100);
					autoMouseEquipPlayer(sack2);
					wait(100);
					autoMouseEquipPlayer(merchant);
				}
			}else if(gear == 4){ //shovel flask
				if(shovel == null || flask == null) redo = true;
				else{
					autoMouseEquipPlayer(shovel);
					transferItem(flask);
				}
			}else if(gear == 5){ //metal saw
				if(metalsaw == null) redo = true;
				else{
					autoMouseEquipPlayer(metalsaw);
				}
			}else if(gear == 6){ //axe shovel
				if(axe == null || shovel == null) redo = true;
				else{
					autoMouseEquipPlayer(axe);
					transferItem(shovel);
				}
			}else if(gear == 7){ //bucket flask
				if(flask == null || bucket == null || shovel == null || cloakhide == null) redo = true;
				else{
					autoMouseEquipPlayer(shovel);
					autoMouseEquipPlayer(cloakhide);
					transferItem(flask);
					transferItem(bucket);
					
				}
			}else if(gear == 8){ // trav merch sword
				if(sack1 == null || sword == null || merchant == null) redo = true;
				else{
					autoMouseEquipPlayer(sack1);
					wait(100);
					autoMouseEquipPlayer(sword);
					wait(100);
					autoMouseEquipPlayer(merchant);
				}
			}else if(gear == 9){ // farming, scythe merch water
				if(waterskin == null || bucket == null || scythe == null || merchant == null) redo = true;
				else{
					autoMouseEquipPlayer(scythe);
					wait(100);
					autoMouseEquipPlayer(merchant);
					transferItem(waterskin);
					transferItem(bucket);
				}
			}else if(gear == 10){ // farming, scythe merch
				if(scythe == null || merchant == null) redo = true;
				else{
					autoMouseEquipPlayer(scythe);
					wait(100);
					autoMouseEquipPlayer(merchant);
				}
			}else if(gear == 11){ // frying merch sack
				if(sack1 == null || fryingpan == null || merchant == null) redo = true;
				else{
					autoMouseEquipPlayer(sack1);
					wait(100);
					autoMouseEquipPlayer(fryingpan);
					wait(100);
					autoMouseEquipPlayer(merchant);
				}
			}
			
			if(redo) wait(500);
			redoCount++;
		}
	}
	
	public boolean checkGear(int gear){
		openEquipment();
		
		ArrayList<Item> list = equipList();
		
		boolean axe = false;
		boolean shovel = false;
		boolean merchant = false;
		boolean sack1 = false;
		boolean sack2 = false;
		boolean flask = false;
		boolean metalsaw = false;
		boolean bucket = false;
		boolean cloakhide = false;
		boolean sword = false;
		boolean scythe = false;
		boolean fryingpan = false;
		
		if(list == null) return true;
		
		for(Item i : list){
			if(i != null){
				String itemName = i.GetResName();
				
				if(gear == 1){ //axe only
					if(itemName.contains("axe") ) return true;
				}else if(gear == 2){ //trav set
					if(itemName.contains("sack") && sack1) sack2 = true;
					if(itemName.contains("sack") ) sack1 = true;
					
					if(sack1 && sack2) return true;
				}else if(gear == 3){ //trav merch
					if(itemName.contains("merchant") ) merchant = true;
					if(itemName.contains("sack") && sack1) sack2 = true;
					if(itemName.contains("sack") ) sack1 = true;
					
					if(merchant && sack1 && sack2) return true;
				}else if(gear == 4){ //shovel flask
					if(itemName.contains("shovel") ) shovel = true;
					if(getItemFromBag("flask") != null ) flask = true;
					
					if(shovel && flask) return true;
				}else if(gear == 5){ //metal saw
					if(itemName.equals("gfx/invobjs/saw") ) return true;
					
				}else if(gear == 6){ //axe shovel
					if(itemName.contains("axe") ) axe = true;
					if(getItemFromBag("shovel") != null ) shovel = true;
					
					if(shovel && axe) return true;
				}else if(gear == 7){ //bucket flask
					if(itemName.contains("shovel") ) shovel = true;
					if(itemName.contains("cloak-hide") ) cloakhide = true;
					if(getItemFromBag("flask") != null ) flask = true;
					if(getItemFromBag("bucket") != null ) bucket = true;
					
					if(flask && bucket && cloakhide && shovel) return true;
				}else if(gear == 8){ //trav merch sword
					if(itemName.contains("merchant") ) merchant = true;
					if(itemName.contains("sack") ) sack1 = true;
					if(itemName.contains("sword") ) sword = true;
					
					if(merchant && sack1 && sword) return true;
				}else if(gear == 9){ //trav merch sword
					if(itemName.contains("scythe") ) scythe = true;
					if(itemName.contains("merchant") ) merchant = true;
					if(getItemFromBag("flask") != null ) flask = true;
					if(getItemFromBag("bucket") != null ) bucket = true;
					
					if(scythe && merchant && flask && bucket) return true;
				}else if(gear == 10){ //trav merch sword
					if(itemName.contains("scythe") ) scythe = true;
					if(itemName.contains("merchant") ) merchant = true;
					
					if(scythe && merchant) return true;
				}else if(gear == 11){ //trav merch sword
					if(itemName.contains("sack") ) sack1 = true;
					if(itemName.contains("fryingpan") ) fryingpan = true;
					if(itemName.contains("merchant") ) merchant = true;
					
					if(sack1 && fryingpan && merchant) return true;
				}
			}
		}
		
		return false;
	}
	
	public void matsFromToBoat(boolean loadBoat, Coord dropCoord, Gob boat){
		//Gob boat = findClosestObject("boat");
		
		if(!windowOpen("Boat")){
			int count = 0;
			clickWorldObject(3, boat);
			autoFlowerMenu("Open");
			while(!windowOpen("Boat") && !stop){
				if(!checkPlayerWalking() ) count++;
				else count = 0;
				
				if(count > 30){
					count = 0;
					clickWorldObject(3, boat);
					autoFlowerMenu("Open");
				}
				wait(200);
			}
		}
		
		wagCartBoat();
		while(!checkPlayerCarry() && !stop){
			wait(200);
		}
		
		if(!stop){
			if(dropCoord.equals(new Coord(0,0)) )
				dropCoord = getPlayerCoord().add(dropoffChestCoord(boat, true) );
			
			clickWorld(3, dropCoord);
			
			int count = 0;
			while(checkPlayerCarry() && !stop){
				wait(200);
				count++;
				if(count > 25){
					count = 0;
					clickWorld(3, dropCoord);
				}
 			}
			
			Gob chest = findClosestObject("lchest", 14);
			while(chest == null && !stop){
				chest = findClosestObject("lchest", 14);
				wait(200);
			}
			
			if(chest == null || stop)
				return;
			if(!stop) clickWorldObject(3, chest);
			while(!windowOpen("Chest") && !stop) wait(200);
			
			Inventory chestInv = getInventory("Chest");
			while(chestInv == null && !stop){
				chestInv = getInventory("Chest");
				wait(200);
			}
			
			if(chestInv == null || stop)
				return;
				
			/*Inventory inv = getInventory("Inventory");
			int check = itemCount(inv);*/
			
			for(int i = 0; i < 48 && !stop; i++){
				if(!loadBoat){
					transferItemFrom(chestInv, 1);
					if(itemCount(chestInv) == 0) break;
					if(getPlayerBagSpace() == 0) break;
				}else if(loadBoat){
					transferItemTo(chestInv, 1);
					if(getInvSpace(chestInv) == 0) break;
					if(getPlayerBagItems() == 0) break;
				}
			}
			
			/*if(!loadBoat && !stop)
				while( itemCount(chestInv) > 0 && !stop) wait(200);
			if(loadBoat && !stop){
				if(check > 48) check = 48;
				while( itemCount(chestInv) < check && !stop) wait(200);
			}*/
			
			if(!stop) sendAction("carry");
			//while(!(getCursor().contains("chi")) && !stop) wait(200);
			if(!stop) clickWorldObject(1, chest);
			while(!checkPlayerCarry() && !stop) wait(200);
		}
		if(!stop) clickWorldObject(3, boat);
		while(!windowOpen("Boat") && !stop) wait(200);
		if(!stop) wagCartBoat();
		while(checkPlayerCarry() && !stop) wait(200);
	}
	
	public Coord dropoffChestCoord(Gob boat, boolean boatSize){
		Coord chestSize = new Coord(10,6);
		Coord dir = new Coord(0,0);
		int size = 10;
		
		if(!boatSize) size = 4;
		
		Coord c = new Coord(getPlayerCoord() );
		Coord p1 = new Coord(boat.getr().sub(size,size) );
		Coord p2 = new Coord(boat.getr().add(size,size) );
		
		if(c.x >= p1.x && c.x <= p2.x){
			if(c.y > boat.getr().y)
				dir = new Coord(0,1); // south
			else
				dir = new Coord(0,-1); // north
		}else if(c.y >= p1.y && c.y <= p2.y){
			if(c.x > boat.getr().x)
				dir = new Coord(1,0); // east
			else
				dir = new Coord(-1,0); // west
		}else{
			if(c.y > boat.getr().y){
				dir = dir.add(0,1); // south
			}else{
				dir = dir.add(0,-1); // north
			}
			
			if(c.x > boat.getr().x){
				dir = dir.add(1,0); // east
			}else{
				dir = dir.add(-1,0); // west
			}
		}
		
		return chestSize.mul(dir);
	}
	
	public void matsFromToBoat(boolean loadBoat, Gob boat){
		if(boat == null) return;
		boolean redo = true;
		int LCid = 0;
		
		while(!windowOpen("Boat") && redo && !stop){
			redo = false;
			int count = 0;
			if(!objectSurf(boat)){
				if(!objectSurf(gobToNegRect(boat, 2), boat.getr(), false, false))
					System.out.println("double fail.");
			}
			clickWorldObject(3, boat);
			autoFlowerMenu("Open");
			while(!windowOpen("Boat") && !stop){
				//if(!checkPlayerWalking() ) count++;
				//else count = 0;
				count++;
				if(count > 30){
					count = 0;
					redo = true;
				}
				wait(200);
			}
		}
		
		wagCartBoat();
		Gob chest = getPlayerLiftedObject();
		while(chest == null && !stop){
			wait(200);
			chest = getPlayerLiftedObject();
		}
		
		if(chest == null || stop)
			return;
		
		LCid = chest.id;
		
		if(!stop){
			redo = true;
			while(redo && !stop){
				redo = false;
				Coord dropCoord = circkleScanRectFreeSpace(getPlayerCoord(), gobToNegRect(chest), 500, 1, true);
				objectSurf(gobToNegRect(chest), dropCoord, true, false);
				clickWorld(3, dropCoord);
				
				int count = 0;
				while(checkPlayerCarry() && !stop){
					wait(200);
					count++;
					if(count > 25){
						redo = true;
						break;
					}
				}
			}
			
			Gob groundChest = null;
			while(groundChest == null && !stop){
				Gob findLC = findObjectByID(LCid);
				if(!objectLifted(findLC) ) groundChest = findLC;
				wait(50);
			}
			
			if(stop) return;
			if(!stop) clickWorldObject(3, groundChest);
			while(!windowOpen("Chest") && !stop) wait(200);
			
			Inventory chestInv = getInventory("Chest");
			while(chestInv == null && !stop){
				chestInv = getInventory("Chest");
				wait(200);
			}
			
			if(chestInv == null || stop)
				return;
				
			/*Inventory inv = getInventory("Inventory");
			int check = itemCount(inv);*/
			
			for(int i = 0; i < 48 && !stop; i++){
				if(!loadBoat){
					transferItemFrom(chestInv, 1);
					if(itemCount(chestInv) == 0) break;
					if(getPlayerBagSpace() == 0) break;
				}else if(loadBoat){
					transferItemTo(chestInv, 1);
					if(getInvSpace(chestInv) == 0) break;
					if(getPlayerBagItems() == 0) break;
				}
			}
			
			/*if(!loadBoat && !stop)
				while( itemCount(chestInv) > 0 && !stop) wait(200);
			if(loadBoat && !stop){
				if(check > 48) check = 48;
				while( itemCount(chestInv) < check && !stop) wait(200);
			}*/
			
			if(!stop) sendAction("carry");
			//while(!(getCursor().contains("chi")) && !stop) wait(200);
			if(!stop) clickWorldObject(1, groundChest);
			while(!checkPlayerCarry() && !stop) wait(200);
		}
		if(!stop) objectSurf(boat);
		if(!stop) clickWorldObject(3, boat);
		while(!windowOpen("Boat") && !stop) wait(200);
		if(!stop) wagCartBoat();
		while(checkPlayerCarry() && !stop) wait(200);
	}
	
	public void advEater(Inventory inv){
		ArrayList<Item> foodList = getItemsFromInv(inv);
		advEater(inv, foodList);
	}
	
	public void advEater(Inventory inv, ArrayList<Item> foodList){
		int stopAt = 995;
		int hunger = getHunger();
		openInventory();
		Inventory bag = getInventory("Inventory");
		ArrayList<Item> eatThis = new ArrayList<Item>();
		
		for(Item i : foodList){
			int filler = foodTest(i, 4);
			if(filler < 0 ) continue;
			
			if( (hunger + filler) < stopAt){
				hunger += filler;
				eatThis.add(i);
				
				//System.out.println("i.GetResName() "+ i.GetResName() + "   filler "+filler +"   hunger"+ hunger);
			}
			
			if(stop) return;
		}
		
		ArrayList<Coord> coords = emptyItemArray(bag, eatThis);
		
		if(eatThis == null || coords == null) return;
		
		int slot = 0;
		Coord d = new Coord();
		for(int q = 0; q < eatThis.size(); q++){
			Item i = eatThis.get(q);
			Coord c = coords.get(q);
			
			if(c != null){
				d = c;
			}
			if(d == null){
				continue;
			}
			
			pickUpItem(i);
			setBeltSlot(slot, 4, i);
			dropItemInInv(d, bag);
			useActionBar(4, slot);
			//wait(500);
			//slot++;
			if(stop) return;
		}
	}
	
	public void goToMantionFloor(int floor, Gob innDoorOutside){
		if(locatePlayer() == floor)
			return;
		
		if(stop) return;
		
		if(locatePlayer() != 1){
			Gob cellarStair = findClosestObject("gfx/arch/stairs-cellar");
			Gob downStair = findClosestObject("gfx/arch/stairs-inn-d");
			//Gob innDoor = findClosestObject("gfx/arch/door-inn");
			
			if(cellarStair != null){
				walkTo(cellarStair);
			}else if(downStair != null){
				walkTo(downStair);
			}else if(innDoorOutside == null && locatePlayer() == 0){
				System.out.println("Door not found.");
				return;
			}else if(innDoorOutside != null){
				walkTo(innDoorOutside);
			}
			
			while(locatePlayer() != 1 && !stop) wait(200);
			loadArea();
		}
		
		if(stop)	return;
		
		if(floor == -1){
			Gob cellarDoor = findClosestObject("gfx/arch/door-cellar");
			while(cellarDoor == null && !stop){
				cellarDoor = findClosestObject("gfx/arch/door-cellar");
				wait(200);
			}
			walkTo(cellarDoor);
			
			while(locatePlayer() != -1 && !stop) wait(200);
			loadArea();
			wait(500);
			//safeCamReset();
			
			return;
		}
		if(floor == 0){
			Gob innDoorInside = findClosestObject("gfx/arch/door-inn");
			while(innDoorInside == null && !stop){
				innDoorInside = findClosestObject("gfx/arch/door-inn");
				wait(200);
			}
			walkTo(innDoorInside);
			
			while(locatePlayer() != 0 && !stop) wait(200);
			loadArea();
			wait(500);
			//safeCamReset();
			
			return;
		}
		if(floor == 1){
			wait(500);
			//safeCamReset();
			
			return;
		}
		if(floor == 2){
			Gob upStair = findClosestObject("gfx/arch/stairs-inn");
			while(upStair == null && !stop){
				upStair = findClosestObject("gfx/arch/stairs-inn");
				wait(200);
			}
			walkTo(upStair);
			
			while(locatePlayer() != 2 && !stop) wait(200);
			loadArea();
			wait(500);
			//safeCamReset();
			
			return;
		}
	}
	
	public void loadArea(){
		boolean loading = true;
		while(!stop && loading){
			wait(200);
			
			Coord c = getPlayerCoord();
			
			loading = getNullTiles(c.sub(20,20) ,c.add(20,20));
		}
	}
	
	public int locatePlayer(){
		while(!stop){
			int id = getTileID(getPlayerCoord().div(11) );
			wait(100);
			if(id == 255 || id == -1) continue;
			
			if(id == 22){
				return -1;
			}else if(id == 21){
				if(findClosestObject("gfx/arch/stairs-inn-d") != null){
					return 2;
				}else if(findClosestObject("gfx/arch/door-inn") != null){
					return 1;
				}else{
					continue;
				}
			}else{
				return 0;
			}
		}
		
		return 404;
	}
	
	
	public Inventory walkToContainer(Gob container, String name){
		Inventory invCub = null;
		int redoCyckles = 3;
		
		if(windowOpen(name) ){
			autoCloseWindow(name);
		}
		
		boolean redo = true;
		int redoCount = 0;
		while(!stop && redo){
			redo = false;
			
			if(!PFrunning){
				pathing = true;
				PFrunning = true;
				PathWalker walk = new PathWalker(this, container);
				walk.start();
			}
			
			int count = 0;
			while(!stop && invCub == null && count < 40 ){
				wait(50);
				
				if(!PFrunning && !checkPlayerWalking() ) count++;
				else count = 0;
				
				invCub = getInventory(name);
			}
			
			if(stop) pathing = false;
			
			if(invCub == null){
				/*if(redoCount < redoCyckles) redo = true;
				redoCount++;*/
				redo = true;
			}
		}
		
		return invCub;
	}
	
	public void walkToWindow(Gob container, String name){
		int redoCyckles = 3;
		
		if(windowOpen(name) ){
			autoCloseWindow(name);
		}
		
		boolean redo = true;
		int redoCount = 0;
		while(!stop && redo){
			redo = false;
			
			if(!PFrunning){
				pathing = true;
				PFrunning = true;
				PathWalker walk = new PathWalker(this, container);
				walk.start();
			}
			
			int count = 0;
			while(!stop && !windowOpen(name) && count < 40 ){
				wait(50);
				
				if(!PFrunning && !checkPlayerWalking() ) count++;
				else count = 0;
			}
			
			if(stop) pathing = false;
			
			if(!windowOpen(name) ){
				if(redoCount < redoCyckles) redo = true;
				redoCount++;
			}
		}
	}
	
	public Gob carpetIdol(){
		Gob idol = findClosestObject("gfx/terobjs/vclaim");
		if(idol != null){
			Gob carpet = findClosestObject("gfx/terobjs/furniture/carpet", idol.getr().add(0,11) );
			if(carpet != null && idol.getr().add(0,11).equals(carpet.getr()) ) return idol;
		}
		
		return null;
	}
	
	public Inventory advWalkToContainer(ArrayList<Gob> containerList, Gob container){
		container = walkToView(container);
		Coord dir = null;
		int dist = 12;
		int sideCheck = 2;
		while(!stop && dir == null){
			int xCount = 0;
			int yCount = 0;
			for(Gob g : containerList){
				if(container.id == g.id) continue;
				
				if(g.getr().x == container.getr().x){
					if(g.getr().dist(container.getr()) < dist ) xCount++;
				}else if(g.getr().y == container.getr().y ){
					if(g.getr().dist(container.getr()) < dist ) yCount++;
				}
			}
			dist += 11;
			//System.out.println("xCount "+xCount+" - yCount "+yCount);
			
			if(xCount > yCount){
				dir = new Coord(1,0);
				//System.out.println("x wins");
			}else if(xCount < yCount){
				dir = new Coord(0,1);
				//System.out.println("y wins");
			}else if(dist > 600){
				dir = new Coord(0,1);
				//System.out.println("draw");
				sideCheck = 4;
			}
		}
		if(dir == null) return null;
		
		double playerDist = 0;
		Coord goToSpot = null;
		ArrayList<Rectangle> rect = getAllCorrectedHitboxes(false, false);
		for(int i = 0; i < sideCheck; i++){
			Coord neg;
			if(i == 2) dir = dir.swap();
			
			if(i % 2 == 0){
				neg = container.getneg().bc.add(2,2).add( container.getneg().bs );
			}else{
				neg = container.getneg().bc.sub(2,2);
			}
			Coord spot = container.getr().add(dir.mul(neg) );
			if(checkReachable(spot,false,rect) ){
				if(goToSpot == null){
					playerDist = getPlayerCoord().dist(spot);
					goToSpot = spot;
				}else if(goToSpot == null || playerDist > getPlayerCoord().dist(spot) ){
					playerDist = getPlayerCoord().dist(spot);
					goToSpot = spot;
				}
			}
			
			if(i == 1 && goToSpot == null) sideCheck = 4;
		}
		
		if(goToSpot == null) return null;
		
		String windowName = getContainerName(container);
		
		if(!goToSpot.equals(getPlayerCoord()) ){
			if(goToSpot.mul(dir).equals(getPlayerCoord().mul(dir) ) && freePath(getPlayerCoord(), goToSpot, false) ){
				goToWorldCoord(goToSpot);
				//System.out.println("direct");
			}else{
				while(!stop && !goToSpot.equals(getPlayerCoord()) ){
					walkTo(goToSpot);
				}
				//System.out.println("with PF");
			}
			
			while(windowOpen(windowName) && !stop ) wait(200);
			
			clickWorldObject(3, container);
		}else if(!windowOpen(windowName) ){
			clickWorldObject(3, container);
		}
		
		Inventory inv = getInventory(windowName);
		while(inv == null && !stop){
			inv = getInventory(windowName);
			wait(200);
		}
		
		return inv;
	}
	
	public boolean pickupBlocks(int quantity, Coord start, Coord end){
		int oldWindow = -1;
		int blocks = countItemsInBag("gfx/invobjs/wood");
		ArrayList<Gob> signList = getObjects("sign", start, end);
		//ArrayList<Gob> sortedList = m_util.superSortGobList(signList, true, false, true);
		
		ArrayList<Gob> oldSign = new ArrayList<Gob>();
		while(signList.size() > 0 && !stop){
			Gob sign = getClosestObjectInArray(signList);
			
			if(sign == null) return false;
			
			signList.remove(sign);
			
			if(!walkToSign(sign, oldSign) ) continue;
			int transfer = transferSignBlocks(quantity - blocks, oldWindow);
			oldWindow = windowID("Palisade Cornerpost");
			blocks = blocks + transfer;
			
			if(blocks >= quantity) break;
			
			oldSign.clear();
			oldSign.add(sign);
		}
		
		return true;
	}
	
	public boolean walkToSign(Gob sign, ArrayList<Gob> oldSign){
		String name = "Palisade Cornerpost";
		walkTo(sign, oldSign);
		
		int redo = 0;
		while(!stop){
			wait(100);
			if(windowOpen(name) ) return true;
			
			if(!findObject(sign) ) return false;
			
			if(!PFrunning && !checkPlayerWalking() ) redo++;
			else redo = 0;
			
			if(redo > 30)
				walkToSign(sign, oldSign);
		}
		
		return true;
	}
	
	public int transferSignBlocks(int transfer, int oldWindow){
		String name = "Palisade Cornerpost";
		String matName = "gfx/invobjs/small/wood";
		
		return matTransfer(-1*transfer, name, matName, oldWindow);
	}
	
	public Gob walkToView(Gob g){
		walkToView(g.getr() );
		
		return findObjectByID(g.id);
	}
	
	public void walkToView(Coord c){
		if(!insideViewCondition(c)){
			pathing = false;
			while(PFrunning && !stop ) wait(100);
			
			walkToCondition(c);
			
			while(!stop && !insideViewCondition(c) ) wait(100);
			
			pathing = false;
			while(PFrunning && !stop ) wait(100);
		}
	}
	
	public boolean insideViewCondition(Coord checkC){
		Coord ul = viewUpperLeft(getPlayerCoord() );
		Rectangle r = new Rectangle(ul.x+50, ul.y+50, 800, 800);
		return r.contains(checkC.x, checkC.y);
	}
	
	public boolean barterTransfer(boolean extract){
		while(!windowOpen("Barter Stand") && !stop) wait(100);
		wait(500);
		
		ArrayList<Shopbox> ShopBoxList = getBarterStandShopbox();
		if(ShopBoxList == null) return false;
		int space = getPlayerBagSpace();
		boolean filled = false;
		for(Shopbox box : ShopBoxList){
			if(!extract){
				if(box.priceMatsInStand > 0){
					box.wdgmsg("buy", new Object[0]);
					filled = true;
				}
			}else{
				if(box.matsInStand >= space){
					for(int i = 0; i < 56; i++)
						box.wdgmsg("ptransfer", new Object[] { Coord.z });
						
					return true;
				}else if(box.matsInStand > 0 ){
					space = space - box.matsInStand;
					for(int i = 0; i < 56; i++)
						box.wdgmsg("ptransfer", new Object[] { Coord.z });
					
					if(space <= 0)
						return true;
				}
			}
		}
		
		if(!extract){
			wait(500);
			
			boolean stoneFound = false;
			while(!stop && !stoneFound){
				for(Item i : getItemsFromBag() ){
					if(i.GetResName().contains("stone") ){
						transferItem(i);
						dropItemOnGround(i);
						stoneFound = true;
					}
				}
				
				wait(100);
			}
			autoCloseWindow("Barter Stand");
			
			return filled;
		}
		
		return false;
	}
	
	public Gob smallChest(Coord c){
		if(c == null) c = getPlayerCoord();
		String open = "gfx/terobjs/furniture/copen";
		String closed = "gfx/terobjs/furniture/cclosed";
		
		return findClosestObject(new String[]{open, closed}, c);
	}
	
	public void startRunFlask(){
		if(runFlaskRunning)
			return;
		
		pathDrinker = true;
		runFlask = true;
		RunFlaskScript script = new RunFlaskScript(this);
		
		if(script != null){
			runFlaskRunning = true;
			script.start();
		}
	}
	
	public void drinkFromContainer(Gob container, boolean waterDrinkOnly){
		String containerType = getContainerName(container);
		if(!windowOpen(containerType) ) walkToContainer(container, containerType);
		if(getStamina() < 90){
			if(!windowOpen(containerType)){
				clickWorldObject(3, container);
				while(!windowOpen(containerType) && !stop) wait(200);
			}
			waterStation(containerType);
		}
		if(getTW() > 40 && !waterDrinkOnly){
			if(!windowOpen(containerType)){
				clickWorldObject(3, container);
				while(!windowOpen(containerType) && !stop) wait(200);
			}
			wineStation(containerType);
		}
	}
	
	public void wineStation(String containerType){
		Inventory inv = getInventory(containerType);
		
		wait(300);
		
		Item wineBucket = getItemFromInventory(inv, "bucket-wine");
		Item interact = null;
		
		if(wineBucket != null){
			Coord bucketC = new Coord(wineBucket.c);
			Coord itemCoord = null;
			Item glass = getItemFromInventory(inv, "glass-winee");
			Item glassFull = getItemFromInventory(inv, "glass-winef");
			
			if(glass == null && glassFull == null){
				return;
			}
			
			if(!mouseHoldingAnItem()){
				pickUpItem(wineBucket);
			}else{
				dropItemInInv(bucketC, inv);
			}
			
			if(glass != null){
				itemInteract(glass);
				itemCoord = glass.c;
			}else if(glassFull != null){
				itemInteract(glassFull);
				itemCoord = glassFull.c;
			}
			
			wait(100);
			
			dropItemInInv(bucketC, inv);
			
			ArrayList<Item> itemList = getItemsFromInv(inv);
			for(Item i : itemList){
				if(i.c.equals(itemCoord) ) interact = i;
			}
		}else{
			interact = getItemFromInventory(inv, "glass-winef");
		}
		
		if(interact == null) return;
		
		itemAction(interact);
		int count = 0;
		while(!flowerMenuReady() && !stop){
			wait(100);
		}
		flowerMenuSelect("Drink");
		while(!hasHourglass() && !stop) wait(50);
		while(hasHourglass() && !stop) wait(50);
	}
	
	public void waterStation(String containerType){
		Inventory inv = getInventory(containerType);
		Item waterBucket = getItemFromInventory(inv, "bucket-water");
		Item flask = getItemFromInventory(inv, "waterflask");
		if(flask == null) flask = getItemFromInventory(inv, "waterskin");
		
		int count = 0;
		while(!stop && (flask == null || waterBucket == null) && count < 5){
			count++;
			wait(100);
			
			waterBucket = getItemFromInventory(inv, "bucket-water");
			flask = getItemFromInventory(inv, "waterflask");
			if(flask == null) flask = getItemFromInventory(inv, "waterskin");
		}
		
		if(waterBucket == null){
			return;
		}
		Coord bucketC = new Coord(waterBucket.c);
		
		if(flask == null){
			return;
		}
		
		if(!mouseHoldingAnItem()){
			pickUpItem(waterBucket);
		}else{
			dropItemInInv(bucketC, inv);
		}
		
		itemInteract(flask);
		
		wait(100);
		
		dropItemInInv(bucketC, inv);
		
		wait(100);
		
		itemAction(flask);
		
		while(!flowerMenuReady() && !stop) wait(200);
		flowerMenuSelect("Drink");
		while(!hasHourglass() && !stop) wait(50);
		while(hasHourglass() && !stop) wait(50);
	}
	
	public void waterTransfer(boolean fillInventoryBuckets){
		Inventory inv1;
		Inventory inv2;
		
		openInventory();
		Inventory bag = getInventory("Inventory");
		Inventory containerInv = getInventory("Cupboard");
		if(containerInv == null) containerInv = getInventory("Chest");
		
		if(containerInv == null){
			sendSlenMessage("Inventory not open for water transfer.");
			return;
		}
		
		inv1 = bag;
		inv2 = containerInv;
		
		if(!fillInventoryBuckets){
			inv2 = bag;
			inv1 = containerInv;
		}
		
		for(Item i : getItemsFromInv(inv1) ){
			if(i.GetResName().contains("bucket-water") ){
				Coord c = i.c;
				pickUpItem(i);
				
				for(Item j : getItemsFromInv(inv2) ){
					if(j.GetResName().contains("bucket") ){
						itemInteract(j);
					}
					if(j.GetResName().contains("water") ){
						itemInteract(j);
					}
				}
				dropItemInInv(c, inv1);
			}
		}
	}
	
	public void quickWater(){
		openInventory();
		Item flask = findFlask();
		fillContainer(flask, "bucket-water");
		
		Coord flaskCoord = flaskToCoord(Config.flaskNum);
		setBeltSlot(flaskCoord.x, flaskCoord.y, flask);
		useActionBar(flaskCoord.x, flaskCoord.y);
	}
	
	public void quickWine(){
		openInventory();
		Item glass = getItemFromBag("glass-wine");
		fillContainer(glass, "bucket-wine");
		
		Coord flaskCoord = flaskToCoord(Config.flaskNum);
		setBeltSlot(flaskCoord.x, flaskCoord.y, glass);
		useActionBar(flaskCoord.x, flaskCoord.y);
	}
	
	public boolean fillContainer(Item container, String bucketName){
		boolean holding = false;
		
		Inventory bag = getInventory("Inventory");
		
		Item bucket = getItemFromBag(bucketName);
		
		if(bucket == null) return false;
		
		Coord bucketC = new Coord(bucket.c);
		
		if(mouseHoldingAnItem()) holding = true;
		
		if(holding){
			dropItemInInv(bucketC, bag);
		}else if(bucket != null){
			pickUpItem(bucket);
		}
		
		if(container != null){
			itemInteract(container);
		}
		
		dropItemInInv(bucketC, bag);
		
		return true;
	}
}