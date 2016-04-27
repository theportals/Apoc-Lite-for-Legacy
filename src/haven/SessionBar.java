package haven;

import java.awt.Color;
import java.util.List;

public class SessionBar extends WindowTrans{
	public static final Text.Foundry nfnd = new Text.Foundry("SansSerif", 10);
	public final static int ID = 1000;
	public static Coord initPos = new Coord(340, 10);
	private final static Tex bg = Resource.loadtex("gfx/hud/bgtex");
	private static final Tex missing = Resource.loadtex("gfx/hud/equip/missing");
	private static final Tex lock = Resource.loadtex("gfx/hud/lockch");
	private static final Coord unborder = new Coord(2, 2);
	private static final Coord dasz = new Coord(74, 74);
	private Color color = Color.WHITE;
	private static final Coord avasz = new Coord(40,40);
	private static final Coord lockC = new Coord(15,15);
	private static final int BORDER = 5;
	private static final int LEFT_OFFSET = 25;
	private static long time = 0;
	
	static {
		Widget.addtype("sessionbar", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new SessionBar(c, parent));
			}
		});
	}
	
	public SessionBar(Coord c, Widget parent) {
		super(c, new Coord(LEFT_OFFSET+avasz.x+2*BORDER, 25), parent, "sessionbar");
		cbtn.visible = false;
		fbtn.c = new Coord(0, 1);
		fbtn.show();
		mrgn = new Coord(0, 0);
	}
	
	public void draw(GOut g) {
		super.draw(g);
		
		recalcsz(null);
		
		List<ThreadUI> sess = MainFrame.getSessionList();
		for (int i = 0; i < sess.size(); i++)
			drawAvatar(g, i, sess.get(i));
	}
	
	private void drawAvatar(GOut g, int index, ThreadUI sess) {
		UI ui = sess.getUI();
		if (ui == null)
			return;
		
		boolean current = MainFrame.getCurrentThreadUI() == sess;
		
		if(folded) return;
		
		if(ui.mainview != null && ui.sess.alive()){
			if(!current) ui.mainview.checkmappos();
			ui.mainview.updateMap();
			ui.root.sessionPulse();
		}
		
		Tex at = sess.getAvatar();
		
		int avoffset = LEFT_OFFSET+index*(avasz.x+2*BORDER);
		
		// display empty box for login screen
		
		GOut g2 = g.reclip(Window.wbox.tloff().add(unborder.inv()).add(avoffset, 0), avasz);
		
		if (at == null) {
			g.chcolor(color);
			Window.wbox.draw(g, Coord.z.add(avoffset, 0), avasz.add(Window.wbox.bisz()).add(unborder.mul(2).inv()));
			g.image(missing, Coord.z.add(avoffset+BORDER, BORDER), avasz);
			//g.chcolor();
			//return;
		}else{
			// g2.image(Equipory.bg, new Coord(Equipory.bg.sz().x / 2 - asz.x / 2, 20).inv().add(off));
			int yo = (20 * avasz.y) / dasz.y;
			Coord tsz = new Coord((at.sz().x * avasz.x) / dasz.x, (at.sz().y * avasz.y) / dasz.y);
			g2.image(bg, new Coord(tsz.x / 2 - avasz.x / 2, yo).inv(), tsz);
			g2.image(at, new Coord(tsz.x / 2 - avasz.x / 2, yo).inv(), tsz);
		}
		
		if(ui.m_util != null && ui.m_util.disableSession){
			g.image(lock, Coord.z.add(BORDER+avoffset, BORDER), lockC);
		}
		
		if (current) {
			g2.chcolor(Color.RED);
			g2.rect(new Coord(1, 1), avasz.sub(1,0));
		}
		
		g.chcolor(color);
		
		Window.wbox.draw(g, Coord.z.add(avoffset, 0), avasz.add(Window.wbox.bisz()).add(unborder.mul(2).inv()));
		g.chcolor();        
	}
	
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == cbtn)
			ui.destroy(this);
		if(sender == fbtn)
			super.wdgmsg(sender, msg, args);
		if(sender == addn)
			super.wdgmsg(sender, msg, args);
		if(sender == dotn)
			super.wdgmsg(sender, msg, args);
	}
	
	private int getClickedAvatarIndex(Coord c) {
		int index = (c.x-LEFT_OFFSET)/(2*BORDER+avasz.x);
		
		if (c.x <= LEFT_OFFSET || index >= MainFrame.instance.getSessionCount())
			return -1;
		/*if (c.y <= 0 || c.y >= 48)
			return -1;*/
		
		return index;
	}
	
	@Override
	protected void recalcsz(Coord max) {
		if(folded) {
			wsz.x = 15;
			wsz.y = 15;
		}else {
			wsz.x = LEFT_OFFSET + (avasz.x+2*BORDER)*MainFrame.instance.getSessionCount();
			wsz.y = avasz.y+2*BORDER;
		}
		sz = asz = wsz;
	}
	
	@Override
	public boolean mouseup(Coord c, int button) {
		if(button == 1) {
			int i = getClickedAvatarIndex(c);
			
			if (i >= 0)
				MainFrame.switchToSession(i);
			
			ui.grabmouse(null);
		}else if (button == 3) {
			if(time > System.currentTimeMillis() ){
				int i = getClickedAvatarIndex(c);
				if (i >= 0){
					if(UI.instance.modflags() == 2){
						MainFrame.closeSession(i);
					}else if(UI.instance.modflags() == 1){
						List<ThreadUI> sess = MainFrame.getSessionList();
						ThreadUI t = sess.get(i);
						t.getUI().m_util.clickButton("Yes");
					}else{
						List<ThreadUI> sess = MainFrame.getSessionList();
						ThreadUI t = sess.get(i);
						t.getUI().m_util.disableSession = !t.getUI().m_util.disableSession;
					}
				}
				
				ui.grabmouse(null);
			}
			time = System.currentTimeMillis() + 300;
		}
		super.mouseup(c, button);
		
		return (true);
	}
	
	@Override
	public void mousemove(Coord c) {
		if(dm) {
			this.c = this.c.add(c.add(doff.inv()));
			List<ThreadUI> sesList = MainFrame.getSessionList();
			
			for (ThreadUI tui : sesList) {
				if (tui != null && tui.ui.sessBar != null)
					tui.ui.sessBar.c = this.c;
			}
		} else {
			super.mousemove(c);
		}
	}
	
	@Override
	public Object tooltip(Coord c, boolean again) {
		//Object ret = super.tooltip(c, again);
		Tex tooltip = null;
		String name = null;
		
		int i = getClickedAvatarIndex(c);
		List<ThreadUI> sess = MainFrame.getSessionList();
		
		if (i >= 0)
			name = sess.get(i).ui.sess != null ? sess.get(i).ui.sess.charname : null;
		
		if(name != null)
			tooltip = new TexI(Utils.outline2(nfnd.render(name, Color.GREEN).img, Color.BLACK));
		
		if(tooltip != null)
			return(tooltip);
		else
			return("");
	}
}