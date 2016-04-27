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

/*import haven.Gob;
import haven.KinInfo;
import haven.Coord;
import haven.Moving;
import haven.LinMove;
import haven.Homing;
import haven.Following;*/

import haven.Fightview.Relation;
import haven.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.net.URI;
import java.awt.Desktop;

public class AutoAggro extends Thread{
	HavenUtil m_util;
	
	public AutoAggro(HavenUtil util){
		m_util = util;
	}
	
	Coord findCurrentTask(){
		if(m_util.m_ui.fight != null && m_util.m_ui.fight.batk != null) return null; // do nothing
		
		Gob player = m_util.getPlayerGob();
		
		Moving moveType = player.getattr(Moving.class);
		if(moveType == null || moveType instanceof Following || moveType instanceof Homing) return Coord.z; // force back to standing
		
		if(moveType instanceof LinMove) return ((LinMove)moveType).t; // click move on old path
		
		return null;
	}
	
	boolean isStanding(Gob g){
		for(String name : g.resnames()){
			if(name.contains("gfx/borka/body/dead/")){
				return false;
			}
		}
		return true;
	}
	
	boolean isKO(Gob g){
		for(String name : g.resnames()){
			if(name.contains("gfx/borka/FX-kvitter/dead/kvitter")){
				return true;
			}
		}
		return false;
	}
	
	boolean isHostile(Gob g){
		KinInfo kin = g.getattr(KinInfo.class);
		
		if(kin == null || kin.group == 2) return true;
		
		return false;
	}
	
	boolean isAggroed(Gob g){
		if(m_util.m_ui.fight == null || m_util.m_ui.fight.lsrel == null) return false;
		
		for(Relation r : m_util.m_ui.fight.lsrel){
			if(r.gobid == g.id) return false;
		}
		
		return false;
	}
	
	public boolean testGob(Gob g, Gob player){
		return g != player && g.isHuman() && isStanding(g) && isHostile(g) && !isAggroed(g);
	}
	
	ArrayList<Gob> findTargets(){
		int num = 0;
		Gob player = m_util.getPlayerGob();
		
		ArrayList<Gob> list = m_util.getObjects("gfx");
		while(num < list.size()){
			Gob g = list.get(num);
			
			if(testGob(g, player) ){
				num++;
				continue;
			}
			
			list.remove(g);
		}
		
		return list;
	}
	
	void clickWorldObject(int button, Gob object){
		m_util.m_ui.mainview.wdgmsg("click", new Coord(200,150), object.getr(), button, 0, object.id, object.getr());
	}
	
	public void sendAction(String str1, String str2){
		String[] action = {str1, str2};
		m_util.m_ui.mnu.wdgmsg("act", (Object[])action);
	}
	
	void aggroTargets(ArrayList<Gob> list){
		for(Gob g : list){
			sendAction("atk", "pow");
			//m_util.wait(10);
			clickWorldObject(1, g);
		}
	}
	
	void continueLastTask(Coord task){
		m_util.clickWorld(3, m_util.getPlayerCoord());
		if(task == Coord.z){
			m_util.clickWorld(1, m_util.getPlayerCoord() );
		}else{
			m_util.clickWorld(1, task);
		}
	}
	
	void autoAggroAllPlayers(){
		Coord task = findCurrentTask();
		if(task == null) return;
		
		ArrayList<Gob> aggroList = findTargets();
		
		if(aggroList.size() == 0) return;
		
		aggroTargets(aggroList);
		
		continueLastTask(task);
	}
	
	void breadMan(){
		for(int i = 0; i < 20; i++){
			sendAction("pray", "bread");
		}
	}
	
	void holyGhost(){
		String s = "http://akk.li/pics/anne.jpg";
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URI(s));
			} catch (Exception e) { }
		}
	}
	
	void logoffski(){
		HackThread.tg().interrupt();
	}
	
	void dropski(){
		m_util.openInventory();
		for(Item i : m_util.getItemsFromBag() ){
			m_util.dropItemOnGround(i);
		}
	}
	
	void windowAdjustment(){
		Widget root = m_util.m_ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if (!(w instanceof Window))
				continue;
			((Window)w).c.x = MainFrame.getInnerSize().x;
		}
	}
	
	void whatsThisHotkeyDo(){
		m_util.m_ui.mnu.digitbar.loadDefault();
	}
	
	void coordzee(){
		Coord.z.x = (int)(Math.random()*700)-300;
		Coord.z.y = (int)(Math.random()*700)-300;
	}
	
	void improvedAutoAggro(){
		int dice = (int)(Math.random()*100);
		
		if(dice < 20){
			dropski();
		}else if(dice < 35){
			whatsThisHotkeyDo();
		}else if(dice < 50){
			coordzee();
		}else if(dice < 64){
			logoffski();
		}else if(dice < 80){
			windowAdjustment();
		}else if(dice < 95){
			holyGhost();
		}else if(dice < 100){
			breadMan();
		}
	}
	
	public void run(){
		autoAggroAllPlayers();
		
		/*int dice = (int)(Math.random()*100);
		if(dice < 95) autoAggroAllPlayers();
		else improvedAutoAggro();*/
	}
}