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

import haven.*;

public class MainScript{
	public static void flaskScript(){
		HavenUtil util = UI.instance.m_util;
		if(!util.runFlaskRunning){
			RunFlaskScript rfs = new RunFlaskScript(util);
			
			if(rfs != null){
				util.runFlaskRunning = true;
				rfs.start();
			}
			
		}
	}
	
	public static void multiTool(){
		int modify = UI.instance.modflags();
		Gob object = UI.instance.mainview.gobAtMouse;
		if(object == null) return;
		int type = ObjectType(object);
		
		if(type == 0){
			int range = 1;
			if(modify == 4) range = 1000;
			
			cleanupItems(range, object);
		}else if(type == 1){
			if(modify == 1)
				object.animalTag = false;
			else if(modify == 4)
				object.animalTag = true;
		}
	}
	
	private static int ObjectType(Gob object){
		String name = object.resname();
		
		if(name.contains("/items/"))
			return 0;
		if(name.equals("gfx/kritter/hen/cdv") || name.equals("gfx/kritter/hen/cock-dead") || name.equals("gfx/kritter/hare/cdv"))
			return 0;
		if(name.equals("gfx/kritter/sheep/s") || name.equals("gfx/kritter/pig/s") || name.equals("gfx/kritter/cow/s"))
			return 1;
		
		return -1;
	}
	
	public static void cleanupItems(int areaSize, Gob object){
		HavenUtil util = UI.instance.m_util;
		
		if(!util.running && object != null){
			Coord pickupCoord = UI.instance.mainview.mousepos;
			Coord c1 = pickupCoord.add(-11*areaSize,-11*areaSize);
			Coord c2 = pickupCoord.sub(-11*areaSize,-11*areaSize);
			
			CleanupScript cs = new CleanupScript(util, c1, c2, object, new Coord(0,0) );
			
			if(cs != null){
				util.running = true;
				util.stop = false;
				util.update();
				cs.start();
			}
		}
	}
	
	public static void autoLand(){
		HavenUtil util = UI.instance.m_util;
		
		if(!util.running){
			AutoLandscape al = new AutoLandscape(util, util.m_pos1, util.m_pos2, util.m_Type);
			
			if(al != null){
				util.running = true;
				util.stop = false;
				util.update();
				al.start();
			}
		}
	}
	
	public static void autoFeast(){
		HavenUtil util = UI.instance.m_util;
		
		if(!util.running){
			AutoFeast af = new AutoFeast(util, util.m_Type);
			
			if(af != null){
				util.running = true;
				util.stop = false;
				util.update();
				af.start();
			}
		}
	}
	
	public static void seedbagScript(boolean transfer){
		HavenUtil util = UI.instance.m_util;
		
		if(!util.running){
			SeedbagScript sbs = new SeedbagScript(util, transfer);
			
			if(sbs != null){
				util.running = true;
				util.stop = false;
				util.update();
				sbs.start();
			}
		}
	}
}