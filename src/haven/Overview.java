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

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Comparator;

public class Overview extends Window {
	final static Text UNKNOWN = Text.render("Unknown");
	final static Text PARTY_MEMBER = Text.render("Party Member");
	
	private static final Comparator<GobList> closest = new Comparator<GobList>() {
		@Override
		public int compare(GobList gl1, GobList gl2){
			try{
				return gl1.dist - gl2.dist;
			}catch(Exception e){}
			
			return 0;
		}
	};
	private static final Comparator<GobList> furthest = new Comparator<GobList>() {
		@Override
		public int compare(GobList gl1, GobList gl2){
			try{
				return gl2.dist - gl1.dist;
			}catch(Exception e){}
			
			return 0;
		}
	};
	private static final Comparator<GobList> alphabetical = new Comparator<GobList>() {
		@Override
		public int compare(GobList gl1, GobList gl2){
			try{
				return gl1.kin.name.compareTo(gl2.kin.name);
			}catch(Exception e){}
			
			return 0;
		}
	};
	
	static final Coord minsz = new Coord(50, 0);
	boolean rsm = false;
	OverviewList OL;
	Comparator<GobList> comp;
	
	public Overview(Coord c, Coord sz, Widget parent) {
		super(c, sz, parent, "Overview");
		ui.addToDestroyList(this);
		mrgn = new Coord(1,1);
		fbtn.visible = true;
		gripbtn = true;
		cbtn.visible = false;
		OL = new OverviewList(new Coord(0, 5), new Coord(sz.x, 60), this, "");
		loadpos();
		placecbtn();
		comp = closest;
		setComp();
	}
	
	public void setComp(){
		String mode = Utils.getpref("overviewSort", "overview");
		synchronized(comp){
			if(mode.equals("closest")) {
				this.comp = closest;
			} else if(mode.equals("furthest")) {
				this.comp = furthest;
			} else if(mode.equals("alphabetical")) {
				this.comp = alphabetical;
			}
		}
	}
	
	protected void placecbtn() {
	fbtn.c = new Coord(wsz.x - 3 - Utils.imgsz(cbtni[0]).x, 3).add(mrgn.inv().add(wbox.tloff().inv()));
    }
	
	private class OverviewList extends Widget {
		private List<GobList> list;
		String cap;
		
		public OverviewList(Coord c, Coord sz, Widget parent, String cp) {
			super(c, sz, parent);
			list = new ArrayList<GobList>();
			cap = cp;
		}
		
		public void draw(GOut g) {
			int i = 0;
			list.clear();
			Coord playerC = null;
			synchronized(list) {
				synchronized (ui.sess.glob.oc) {
					for (Gob gob : ui.sess.glob.oc) {
						if(gob.id == ui.mainview.playergob){
							playerC = gob.getc();
							continue;
						}
						
						if(gob.isHuman()){
							list.add(new GobList(gob) );
						}
					}
				}
				
				/*if(!Config.hostileOverviewFilter){
					synchronized(ui.sess.glob.party.memb) {
						for(Party.Member m : ui.sess.glob.party.memb.values()) {
							if(m.getgob() == null)
								list.add(new GobList(m) );
						}
					}
				}*/
				int index = 0;
				while(true){
					if(list.size() == index) break;
					
					GobList gl = list.get(index);
					index++;
					
					KinInfo kin = null;
					if(gl.gob != null) gl.kin = gl.gob.getattr(KinInfo.class);
					
					if(Config.hostileOverviewFilter && gl.kin != null && gl.kin.group != 2){
						list.remove(gl);
						index--;
						continue;
					}
					
					if(gl.c != null && playerC != null) gl.dist = (int)( playerC.dist(gl.c) / 11);
				}
				
				synchronized(comp){
					Collections.sort(list, comp);
				}
				
				for(GobList gl : list){
					String distance = null;
					if(gl.dist != 0) distance = Integer.toString(gl.dist);
					
					if(gl.kin != null){
						g.aimage(gl.kin.rendered(), new Coord(0, i * 20 + 10), 0, 0.5);
						if(distance != null) g.atext(distance, new Coord(sz.x-20-distance.length()*5, i * 20 + 10), 0, 0.5);
						i++;
					/*} else if(gl.party) {
						g.aimage(PARTY_MEMBER.tex(), new Coord(0, i * 20 + 10), 0, 0.5);
						if(distance != null) g.atext(distance, new Coord(sz.x-20-(distance.length()*5), i * 20 + 10), 0, 0.5);
						i++;*/
					} else {
						g.aimage(UNKNOWN.tex(), new Coord(0, i * 20 + 10), 0, 0.5);
						if(distance != null) g.atext(distance, new Coord(sz.x-20-(distance.length()*5), i * 20 + 10), 0, 0.5);
						i++;
					}
				}
			}
			sz = new Coord(sz.x, i*20);
			recalcsz(sz.add(c));
			super.draw(g);
		}
		
		public boolean mousedown(Coord c, int button) {
			if(super.mousedown(c, button))
				return(true);
			GobList gl = getSel(c.y / 20);
			
			if(gl == null){
				return false;
			}else if(ui.modflags() == 4){
				ui.chat.getawnd().wdgmsg("msg","@$["+gl.id+"]");
			}else if(gl.party){
				Partyview p = getParty();
				if(p != null) p.wdgmsg("click", gl.id, button);
			}else{
				ui.mainview.wdgmsg("click", parent.c.add(c).add(this.c), gl.c, button, 0, gl.id, gl.c);
			}
			
			ui.grabmouse(null);
			return true;
		}
		
		private GobList getSel(int sel){
			synchronized(list) {
				if(sel >= list.size())
					return null;
				if(sel < 0)
					return null;
				else
					return list.get(sel);
			}
		}
	}
	
	Partyview getParty(){
		Widget root = ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if (!(w instanceof Partyview))
				continue;
			
			return (Partyview)w;
		}
		
		return null;
	}
	
	private void loadpos(){
		synchronized (Config.window_props) {
			c = new Coord(Config.window_props.getProperty("overview_pos", c.toString()));
			OL.sz = new Coord(Config.window_props.getProperty("overview_sz", OL.sz.toString()));
			pack();
		}
    }
	
    public boolean mousedown(Coord c, int button) {
		if(folded) {
			return super.mousedown(c, button);
		}
		raise();
		if(button == 1){
			ui.grabmouse(this);
			doff = c;
			if(c.isect(sz.sub(gzsz), gzsz)) {
				rsm = true;
				return true;
			}
		}
		return super.mousedown(c, button);
    }
	
    public boolean mouseup(Coord c, int button) {
		if(dm){
			Config.setWindowOpt("overview_pos", this.c.toString());
		}
		if (rsm){
			ui.grabmouse(null);
			rsm = false;
			Config.setWindowOpt("overview_sz", OL.sz.toString());
		} else {
			super.mouseup(c, button);
		}
		return (true);
    }
    
    public void mousemove(Coord c) {
		if (rsm){
			Coord d = c.sub(doff);
			OL.sz.x = OL.sz.add(d).x;
			OL.sz.x = Math.max(minsz.x, OL.sz.x);
			doff = c;
			pack();
		} else {
			super.mousemove(c);
		}
    }
	
	public void destroyMe(){
		ui.destroy(this);
	}
	
	private class GobList{
		Gob gob;
		int id;
		Coord c;
		boolean party;
		int dist;
		KinInfo kin;
		
		public GobList(Gob g){
			gob = g;
			id = g.id;
			c = g.getc();
		}
		
		public GobList(Party.Member m){
			gob = m.getgob();
			id = m.gobid;
			c = m.getc();
			party = true;
		}
	}
}
