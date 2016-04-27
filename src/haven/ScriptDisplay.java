/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.awt.image.BufferedImage;

import addons.AutoCompilation;

public class ScriptDisplay extends Window{
	public static ScriptDisplay instance;
	public static Coord initPos = new Coord(350, 350);
	public final static int ID = 667;
	BotList BL;
	BotList OL;
	private List<Bot> info = new ArrayList<Bot>();
	private Button run;
	private Button stop;
	private Button forceStop;
	private TextEntry option, modify;
	private final Object LOCK = new Object();
	int mainScript = 1;
	String mainOption = "1";
	String mainModify = "1";
	private boolean soakClear = false;
	
	private int iconNum = 0;
	Indir<Resource> res = Resource.load("paginae/scripticons/script"+iconNum).indir();
	protected static BufferedImage[] charUp = new BufferedImage[] {
	Resource.loadimg("gfx/hud/new/upup"),
	Resource.loadimg("gfx/hud/new/updown")};
	protected static BufferedImage[] charDown = new BufferedImage[] {
	Resource.loadimg("gfx/hud/new/downup"),
	Resource.loadimg("gfx/hud/new/downdown")};
	Item botItem;
	
	static int mainID = 0;
	
	public Widget create(Coord c, Widget parent, Object[] args){
		return(new ScriptDisplay(c, parent));
	}
	
	public ScriptDisplay(Coord c, Widget parent){
		super(c, new Coord(400, 350), parent, "Script UI");
		visible = false;
		loadScriptData();
		BL = new BotList(new Coord(10, 5), new Coord(180, 280), this, info, "scripts") {
			public void changed(Bot b) {
				if(b != null){
					OL.updateList(new ArrayList<Bot>(b.sublist) );
					//mainScript = b.scriptName;
					/*
					if(InfoWindow.instance != null){
						ui.m_util.m_script = mainScript - 1;
					}*/
					OL.repop();
					
				}
			}
		};
		OL = new BotList(new Coord(210, 5), new Coord(180, 280), this, null , "options") {
			public void changed(Bot b) {
				if(b != null){
					soakClear = true;
					option.settext(Integer.toString(getIndex() + 1) );
					
					//option.settext(Integer.toString( ) );
					/*soakClear = true;
					option.settext(Integer.toString(b.script) );
					
					if(InfoWindow.instance != null){
						ui.m_util.m_option = b.script;
					}*/
				}
			}
		};
		run = new Button(new Coord(5, 290), 120, this, "Run") { public void click() {
			runScript();
		} };
		stop  = new Button(new Coord(140, 290), 120, this, "Stop") { public void click() {
			ui.m_util.stop(1);
		} };
		forceStop  = new Button(new Coord(275, 290), 120, this, "Force Stop") { public void click() {
			ui.m_util.forceStop();
		} };
		
		new Button(new Coord(5, 325), 60, this, "Compile") { public void click() {
			AutoCompilation.compile();
		} };
		new Button(new Coord(80, 325), 60, this, "Update") { public void click() {
			AutoCompilation.buildConfRun();
			loadScripts();
		} };
		
		canhastrash = false;
		Inventory hotinv = new Inventory(new Coord(160, 320), new Coord(1, 1), this);
		botItem = new Item(Coord.z, res, -1, hotinv, null, -1){
			public boolean mousedown(Coord c, int button){
				if(ui.mnu != null){
					String s = scriptToString();
					if(s != null){
						ui.mnu.setDrag(ui.mnu.digitbar.makeNewSlot("#" + res.get().name + "#" + s, 0, 0) );
						ui.grabmouse(ui.mnu);
					}
				}
				return true;
			};
		};
		new IButton(new Coord(195, 320 ), this, charUp[0], charUp[1]) { public void click() {
			iconNum++;
			if(iconNum > 16) iconNum = 0;
			botItem.res = Resource.load("paginae/scripticons/script"+iconNum).indir();
		} };
		new IButton(new Coord(195, 335 ), this, charDown[0], charDown[1]) { public void click() {
			iconNum--;
			if(iconNum < 0) iconNum = 16;
			botItem.res = Resource.load("paginae/scripticons/script"+iconNum).indir();
		} };
		
		new Label(new Coord(220, 310), this, "Option:");
		new Label(new Coord(310, 310), this, "Modify:");
		option = new TextEntry(new Coord(220, 325), new Coord(40, 20), this, "1") {
			public void change(String text){
				if(soakClear){
					soakClear = false;
				}else{
					OL.clearSelection();
				}
				mainOption = text;
				ui.m_util.m_option = parsInt(text);
			}
		};
		modify = new TextEntry(new Coord(270, 325), new Coord(120, 20), this, "1") {
			public void change(String text) {
				mainModify = text;
				ui.m_util.m_modify = text;
			}
		};
		
		BL.repop();
		OL.repop();
	}
	
	public void loadScripts(){
		loadScriptData();
		BL.updateList(new ArrayList<Bot>(info) );
		OL.updateList(new ArrayList<Bot>() );
	}
	
	void runScript(){
		if(BL.sel == null) return;
		
		String cls = BL.sel.className;
		AutoCompilation.runClass(cls, ui.m_util, ui.m_util.m_option, ui.m_util.m_modify);
	}
	
	String scriptToString(){
		if(BL.sel == null) return null;
		
		String s = BL.sel.className;
		int o = OL.getIndex() + 1;
		try{
		if(o == -1) o = Integer.parseInt(option.text);
		}catch(Exception e){}
		String m = modify.text;
		return s + "!" + o + "!" + m;
	}
	
	int parsInt(String str){
		try{
			return Integer.parseInt(str);
		}catch(Exception e){}
		
		return 1;
	}
	
	void loadScriptData(){
		info.clear();
		try{
			File load = new File("./scripts/compiled/script.conf");
			if(!load.exists() ){
				//System.out.println("script.conf not found");
				return;
			}
			
			FileInputStream fstream = new FileInputStream(load);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String strLine;
			Bot b = null;
			
			String scrt = "";
			while((strLine = br.readLine()) != null){
				try{
					String[] parts = strLine.split(":");
					
					if(!scrt.equals(parts[0]) ){
						b = new Bot();
						scrt =  parts[0];
						b.name = parts[0];
						b.className = parts[1];
						b.txt = Text.render(b.name);
						
						info.add(b);
					}else if(b != null){
						Bot sub = new Bot();
						sub.name = parts[1];
						sub.txt = Text.render(sub.name);
						
						b.sublist.add(sub);
					}
				}catch(Exception e){
					System.out.println(e);
				}
			}
			
			br.close();
			
		}catch(IOException e){
			System.out.println(e);
		}
	}
	
	private class BotList extends Widget {
		private List<Bot> list = new ArrayList<Bot>();
		Scrollbar sb = null;
		int h;
		Bot sel;
		String cap;
		int id;
		
		public BotList(Coord c, Coord sz, Widget parent, List<Bot> l, String cp){
			super(c, sz, parent);
			h = sz.y / 20;
			//h = sz.y;
			sel = null;
			sb = new Scrollbar(new Coord(sz.x, 0), sz.y, this, 0, 4);
			if(l != null) list = l;
			cap = cp;
			mainID++;
			id = mainID;
		}
		
		public void draw(GOut g){
			g.chcolor(Color.BLACK);
			g.frect(Coord.z, sz);
			g.chcolor();
			synchronized(LOCK) {
				if(list.size() == 0) {
					g.atext("No "+cap+" loaded.", sz.div(2), 0.5, 0.5);
				} else {
					for(int i = 0; i < h; i++){
						if(i + sb.val >= list.size())
							continue;
						Bot b = list.get(i + sb.val);
						if(b == sel) {
							g.chcolor(96, 96, 96, 255);
							g.frect(new Coord(0, i * 20), new Coord(sz.x, 20));
							g.chcolor();
						}
						g.aimage(b.txt.tex(), new Coord(25, i * 20 + 10), 0, 0.5);
						g.chcolor();
					}
				}
			}
			super.draw(g);
		}
		
		public void repop(){
			sb.val = 0;
			synchronized(LOCK) {
			sb.max = list.size() - h;
			}
		}
		
		public boolean mousewheel(Coord c, int amount) {
			sb.ch(amount);
			return(true);
		}
		
		public void selectUpdate(int update){
			synchronized(LOCK) {
				/*for(Bot b : list){
					if(b.script == update){
						select(b);
					}
				}*/
			}
		}
		
		public void select(Bot b) {
			this.sel = b;
			changed(this.sel);
		}
		
		public boolean mousedown(Coord c, int button) {
			if(super.mousedown(c, button))
			return(true);
			synchronized(LOCK) {
			if(button == 1) {
				int sel = (c.y / 20) + sb.val;
				if(sel >= list.size() ){
					sel = -1;
				}
				if(sel < 0){
					select(null);
				}else{
					select(list.get(sel));
				}
				return(true);
			}
			}
			return(false);
		}
		
		public void updateList(List<Bot> l){
			synchronized(LOCK) {
				list.clear();
				list = l;
			}
		}
		
		public void updateListSel(List<Bot> l){
			synchronized(LOCK) {
				list.clear();
				list = l;
			}
			try{
				int id = sel.id;
				sel = list.get(id);
			}catch(Exception e){}
		}
		
		public void clearSelection(){
			sel = null;
		}
		
		public void changed(Bot b) {
		}
		
		public int getIndex(){
			if(sel == null) return -1;
			
			return list.indexOf(sel);
		}
	}
	
	public class Bot {
		public List<Bot> sublist = new ArrayList<Bot>();
		
		int id;
		String name;
		String className;
		
		Text txt;
	}
	
	String scriptName(String script, String option, String modify){
		String scriptText = "";
		
		Bot bot = null;
		for(Bot b : info){
			if(b.className.equals(script)){
				bot = b;
				scriptText += "\n"+b.name;
				break;
			}
		}
		if(bot == null) return scriptText;
		
		int o = Integer.parseInt(option) - 1;
		if(o >= 0 && o < bot.sublist.size() ){
			scriptText += "\nScript: "+bot.sublist.get(o).name;
		}else{
			scriptText += "\nOption: "+option;
		}
		
		scriptText += "\nModify: "+modify;
		
		return scriptText;
	}
	
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == cbtn){
			hide();
		}else{
			//super.wdgmsg(sender, msg, args);
		}
	}
}
