package haven;

public class ThreadUI{

    public Thread thread;
    public UI ui;
	public CharWnd charWnd;
	public BuddyWnd buddyWnd;
	//private Tex avatar = null;
	private Avaview av;
	
	public ThreadUI() {
    }
	
    public ThreadUI(Thread thread, UI ui) {
        this.thread = thread;
        this.ui = ui;
		ui.uiThread = this;
    }
	
	void editThread(Thread thread, UI ui) {
        this.thread = thread;
        this.ui = ui;
		ui.uiThread = this;
	}
	
    public Thread getThread() {
        return thread;
    }

    public UI getUI() {
        return ui;
    }
	
	public Tex getAvatar() {
		if (av != null) {
			Gob avaGob = null;
			try{
				avaGob = ui.sess.glob.oc.getgob(av.avagob);
			}catch(Exception e){
				System.out.println(e);
			}
			
			Avatar ava = null;
			
			if(avaGob != null)
				ava = avaGob.getattr(Avatar.class);
			if(ava != null)
				return ava.rend;        
		}
		
		return null;
    }
	
	public void setAvatar(Avaview av) {
		this.av = av;
	}
}