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

import haven.MCache.Overlay;
import haven.MapView.Grabber;
import haven.MapView.GrabberException;

public class Landwindow extends Window implements MapView.Grabber{
	Label text;
	Label text2;
	boolean dm = false;
	Coord sc;
	Coord c1;
	Coord c2;
	MCache.Overlay ol;
	MCache map;
	private static final String fmt = "Selected area: (%d x %d) = %d m²";
	
	static{
		Widget.addtype("ui/land", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args){
				return new Landwindow(c, parent);
			}
		});
	}
	
	public Landwindow(Coord c, Widget parent){
		super(c, new Coord(200, 30), parent, "Land management");
		this.map = this.ui.sess.glob.map;
		this.ui.mainview.enol(new int[] { 0, 1, 16 });
		this.ui.mainview.grab(this);
		this.text = new Label(Coord.z, this, String.format("Selected area: (%d x %d) = %d m²", new Object[] { Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0) }));
		this.text2 = new Label(Coord.z.add(0,20), this, "");
	}
	
	public void destroy(){
		this.ui.mainview.disol(new int[] { 0, 1, 16 });
		this.ui.mainview.release(this);
		if (this.ol != null) {
			this.ol.destroy();
		}
		super.destroy();
	}
	
	public void mmousedown(Coord mc, int button){
		if (button != 1) {
			throw new MapView.GrabberException();
		}
		Coord c = mc.div(MCache.tilesz);
		if (this.ol != null) {
			this.ol.destroy();
		}
		this.ol = map.new Overlay(c, c, 65536);
		this.sc = c;
		this.dm = true;
		this.ui.grabmouse(this.ui.mainview);
	}
	
	public void mmouseup(Coord mc, int button){
		this.dm = false;
		this.ui.grabmouse(null);
		if (button != 1) {
			throw new MapView.GrabberException();
		}
	}
	
	public void mmousemove(Coord mc){
		if(!this.dm){
			Gob mouseGob = ui.mainview.gobAtMouse;
			String mouseGobName = "";
			int mouseGobAmount = 0;
			if(mouseGob != null && this.c1 != null && this.c2 != null){
				mouseGobName = mouseGob.resname();
				mouseGobAmount = ui.m_util.getObjects(mouseGobName, this.c1.mul(11), this.c2.mul(11) ).size();
				this.text2.settext(String.format("%s in area: %d", mouseGobName, mouseGobAmount ));
			}else{
				this.text2.settext("");
			}
			
			return;
		}
		Coord c1 = mc.div(MCache.tilesz);
		Coord c2 = new Coord(0, 0);
		Coord c3 = new Coord(0, 0);
		if (c1.x < this.sc.x){
			c2.x = c1.x;
			c3.x = this.sc.x;
		}else{
			c2.x = this.sc.x;
			c3.x = c1.x;
		}
		if (c1.y < this.sc.y){
			c2.y = c1.y;
			c3.y = this.sc.y;
		} else {
			c2.y = this.sc.y;
			c3.y = c1.y;
		}
		this.ol.update(c2, c3);
		this.c1 = c2;
		this.c2 = c3;
		
		c1.x = (c3.x - c2.x + 1);
		c1.y = (c3.y - c2.y + 1);
		int i = c1.x * c1.y;
		
		this.text.settext(String.format("Selected area: (%d x %d) = %d m²", new Object[] { Integer.valueOf(c1.x), Integer.valueOf(c1.y), Integer.valueOf(i) }));
	}
	
	public void uimsg(String msg, Object... args){
		if (msg == "reset"){
			this.ol.destroy();
			this.ol = null;
			this.c1 = (this.c2 = null);
		}
	}
}