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

import java.util.Collection;
import java.util.Iterator;

public class Aimview extends Widget{
	static final Resource.Image bg;
    static final Resource.Image fg;
    public int val;
	public static int idCounter = 0;
	public int id = 0;
	
	static{
		Resource.Image image = null;
		Resource.Image image1 = null;
		Resource res = Resource.load("ui/aim", 5);
		res.loadwait();
		for(Resource.Image img : res.layers(Resource.imgc)) {
			if(img.id == 0)
				image = img;
			else if(img.id == 1)
				image1 = img;
		}
		bg = image;
		fg = image1;
		Widget.addtype("ui/aim", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new Aimview(c, parent));
			}
		});
	}
	
	public Aimview(Coord coord, Widget parent){
		super(coord.add(bg.sz.div(2).inv()), bg.sz, parent);
		idCounter++;
		id = idCounter;
        val = 0;
	}

	public void draw(GOut gout)
	{
		gout.image(bg.tex(), bg.o);
		int i = ((10000 - val) * fg.sz.y) / 10000;
		gout.image(fg.tex(), fg.o, fg.o.add(new Coord(0, i)), fg.sz.add(0, -i));
	}
	
    public void uimsg(String s, Object aobj[]){
        if(s == "aim"){
            val = ((Integer)aobj[0]).intValue();
		}
	}
}
