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

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ScriptDrawer extends Widget{
	ArrayList<drawList> dList = new ArrayList<drawList>();
	Coord oc;
	
	public ScriptDrawer(Coord c, Widget parent){
		super(parent.ui, c, c);
		
	}
	
	public static Coord m2s(Coord c) {
		return(new Coord((c.x * 2) - (c.y * 2), c.x + c.y));
    }
	
	public void clear(){
		synchronized(dList){
			dList.clear();
		}
	}
	
	public void addText(Coord c, String text, Color col){
		synchronized(dList){
			dList.add(new drawText(c, text, col) );
		}
	}
	
	public void addLine(Coord c1, Coord c2, double thick, Color col){
		synchronized(dList){
			dList.add(new drawLine(c1, c2, thick, col) );
		}
	}
	
	public void addBox(Coord c1, Coord c2, double thick, Color col){
		synchronized(dList){
			dList.add(new drawBox(c1, c2, thick, col) );
		}
	}
	
	public void addRect(Coord c1, Coord c2, Color col){
		synchronized(dList){
			dList.add(new drawRect(c1, c2, col) );
		}
	}
	
	public void draw(GOut g){
		oc = ui.mainview.getOC();
		
		synchronized(dList){
			for(drawList d : dList){
				d.draw(g);
			}
		}
		
		g.chcolor();
	}
	
	public class drawList{
		public drawList(){ }
		
		public void draw(GOut g){}
	}
	
	public class drawText extends drawList{
		Coord c_c;
		String c_text;
		Color c_col;
		
		public drawText(Coord c, String text, Color col){
			c_c = c;
			c_text = text;
			c_col = col;
		}
		
		public void draw(GOut g){
			g.chcolor(c_col);
			
			g.atext(c_text, m2s(c_c).add(oc), 0, 0);
		}
	}
	
	public class drawLine extends drawList{
		Coord c_c1;
		Coord c_c2;
		double c_thick;
		Color c_col;
		
		public drawLine(Coord c1, Coord c2, double thick, Color col){
			c_c1 = c1;
			c_c2 = c2;
			c_thick = thick;
			c_col = col;
		}
		
		public void draw(GOut g){
			g.chcolor(c_col);
			
			g.line(m2s(c_c1).add(oc), m2s(c_c2).add(oc),c_thick);
		}
	}
	
	public class drawBox extends drawList{
		Coord c_c1;
		Coord c_c2;
		double c_thick;
		Color c_col;
		
		public drawBox(Coord c1, Coord c2, double thick, Color col){
			c_c1 = c1;
			c_c2 = c2;
			c_thick = thick;
			c_col = col;
		}
		
		public void draw(GOut g){
			g.chcolor(c_col);
			g.line(m2s(c_c1).add(oc), m2s(new Coord(c_c1.x, c_c2.y)).add(oc),c_thick);
			g.line(m2s(c_c1).add(oc), m2s(new Coord(c_c2.x, c_c1.y)).add(oc),c_thick);
			g.line(m2s(c_c2).add(oc), m2s(new Coord(c_c1.x, c_c2.y)).add(oc),c_thick);
			g.line(m2s(c_c2).add(oc), m2s(new Coord(c_c2.x, c_c1.y)).add(oc),c_thick);
		}
	}
	
	public class drawRect extends drawList{
		Coord c_c1;
		Coord c_c2;
		Color c_col;
		
		public drawRect(Coord c1, Coord c2, Color col){
			c_c1 = c1;
			c_c2 = c2;
			c_col = col;
		}
		
		public void draw(GOut g){
			g.chcolor(c_col);
			
			g.frect(m2s(c_c1).add(oc),
				m2s(new Coord(c_c2.x, c_c1.y)).add(oc),
				m2s(c_c2).add(oc),
				m2s(new Coord(c_c1.x, c_c2.y)).add(oc));
		}
	}
}
