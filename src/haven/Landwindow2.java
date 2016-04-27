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

import haven.BuddyWnd.GroupSelector;

public class Landwindow2 extends Window{
	Widget bn;
	Widget be;
	Widget bs;
	Widget bw;
	Widget buy;
	Widget reset;
	Widget dst;
	BuddyWnd.GroupSelector group;
	Label area;
	Label cost;
	boolean dm = false;
	Coord c1;
	Coord c2;
	Coord cc1;
	Coord cc2;
	MCache.Overlay ol;
	MCache map;
	int[] bflags = new int[8];
	Landwindow2.PermBox[] perms = new Landwindow2.PermBox[3];
	private static final String fmt = "Area: %d m²";
	
	static{
		Widget.addtype("ui/land2", new WidgetFactory() {
			public Widget create(Coord coord, Widget widget, Object aobj[]){
				Coord coord1 = (Coord)aobj[0];
				Coord coord2 = (Coord)aobj[1];
				return new Landwindow2(coord, widget, coord1, coord2);
			}
		});
	}
	
	private void fmtarea()
	{
		this.area.settext(String.format("Area: %d m²", new Object[] { Integer.valueOf((this.c2.x - this.c1.x + 1) * (this.c2.y - this.c1.y + 1)) }));
	}
	
	private void updatecost()
	{
		this.cost.settext(String.format("Cost: %d", new Object[] { Integer.valueOf(((this.cc2.x - this.cc1.x + 1) * (this.cc2.y - this.cc1.y + 1) - (this.c2.x - this.c1.x + 1) * (this.c2.y - this.c1.y + 1)) * 10) }));
	}
	
	private void updflags()
	{
		int i = this.bflags[this.group.group];
		for (Landwindow2.PermBox localPermBox : this.perms) {
			localPermBox.a = ((i & localPermBox.fl) != 0);
		}
	}
	
	private class PermBox extends CheckBox{
		int fl;
		
		PermBox(Coord paramCoord, Widget paramWidget, String paramString, int paramInt)
		{
			super(paramCoord, paramWidget, paramString);
			this.fl = paramInt;
		}
		
		public void changed(boolean paramBoolean)
		{
			int i = 0;
			for (PermBox localPermBox : Landwindow2.this.perms) {
				if (localPermBox.a) {
					i |= localPermBox.fl;
				}
			}
			Landwindow2.this.wdgmsg("shared", new Object[] { Integer.valueOf(Landwindow2.this.group.group), Integer.valueOf(i) });
		}
	}
	
	public Landwindow2(Coord paramCoord1, Widget paramWidget, Coord paramCoord2, Coord paramCoord3){
		super(paramCoord1, new Coord(0, 0), paramWidget, "Stake");
		this.cc1 = (this.c1 = paramCoord2);
		this.cc2 = (this.c2 = paramCoord3);
		this.map = this.ui.sess.glob.map;
		this.ui.mainview.enol(new int[] { 0, 1, 16 });
		//tmp111_108 = this.map;tmp111_108.getClass();this.ol = new MCache.Overlay(tmp111_108, this.cc1, this.cc2, 65536);
		this.ol = map.new Overlay(this.cc1, this.cc2, 0x10000);
		this.area = new Label(Coord.z, this, "a");
		this.cost = new Label(new Coord(0, 15), this, "Cost: 0");
		fmtarea();
		this.bn = new Button(new Coord(60, 40), Integer.valueOf(80), this, "Extend North");
		this.be = new Button(new Coord(120, 65), Integer.valueOf(80), this, "Extend East");
		this.bs = new Button(new Coord(60, 90), Integer.valueOf(80), this, "Extend South");
		this.bw = new Button(new Coord(0, 65), Integer.valueOf(80), this, "Extend West");
		new Label(new Coord(0, 120), this, "Assign permissions to memorized people:");
		this.group = new BuddyWnd.GroupSelector(new Coord(0, 135), this, 0)
		{
			protected void changed(int paramAnonymousInt)
			{
				super.changed(paramAnonymousInt);
				Landwindow2.this.updflags();
			}
		};
		this.perms[0] = new Landwindow2.PermBox(new Coord(0, 155), this, "Trespassing", 1);
		this.perms[1] = new Landwindow2.PermBox(new Coord(80, 155), this, "Theft", 2);
		this.perms[2] = new Landwindow2.PermBox(new Coord(160, 155), this, "Vandalism", 4);
		this.buy = new Button(new Coord(0, 190), Integer.valueOf(60), this, "Buy");
		this.reset = new Button(new Coord(80, 190), Integer.valueOf(60), this, "Reset");
		this.dst = new Button(new Coord(160, 190), Integer.valueOf(60), this, "Declaim");
		pack();
	}
	
	public void destroy()
	{
		this.ui.mainview.disol(new int[] { 0, 1, 16 });
		this.ol.destroy();
		super.destroy();
	}
	
	public void uimsg(String paramString, Object... paramVarArgs)
	{
		if (paramString == "upd")
		{
			Coord localCoord1 = (Coord)paramVarArgs[0];
			Coord localCoord2 = (Coord)paramVarArgs[1];
			this.c1 = localCoord1;
			this.c2 = localCoord2;
			fmtarea();
			updatecost();
		}
		else if (paramString == "shared")
		{
			int i = ((Integer)paramVarArgs[0]).intValue();
			int j = ((Integer)paramVarArgs[1]).intValue();
			this.bflags[i] = j;
			if (i == this.group.group) {
				updflags();
			}
		}
	}
	
	public void wdgmsg(Widget paramWidget, String paramString, Object... paramVarArgs){
		int multiplier = 1;
		if(ui.modflags() == 1) multiplier = 10;
		if(ui.modflags() == 2) multiplier = 50;
		
		if (paramWidget == this.bn)
		{
			this.cc1 = this.cc1.add(0, -1 * multiplier);
			this.ol.update(this.cc1, this.cc2);
			updatecost();
			return;
		}
		if (paramWidget == this.be)
		{
			this.cc2 = this.cc2.add(1 * multiplier, 0);
			this.ol.update(this.cc1, this.cc2);
			updatecost();
			return;
		}
		if (paramWidget == this.bs)
		{
			this.cc2 = this.cc2.add(0, 1 * multiplier);
			this.ol.update(this.cc1, this.cc2);
			updatecost();
			return;
		}
		if (paramWidget == this.bw)
		{
			this.cc1 = this.cc1.add(-1 * multiplier, 0);
			this.ol.update(this.cc1, this.cc2);
			updatecost();
			return;
		}
		if (paramWidget == this.buy)
		{
			wdgmsg("take", new Object[] { this.cc1, this.cc2 });
			return;
		}
		if (paramWidget == this.reset)
		{
			this.ol.update(this.cc1 = this.c1, this.cc2 = this.c2);
			updatecost();
			return;
		}
		if (paramWidget == this.dst)
		{
			wdgmsg("declaim", new Object[0]);
			return;
		}
		super.wdgmsg(paramWidget, paramString, paramVarArgs);
	}
}