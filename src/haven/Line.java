package haven;

public class Line {
	public int x1, y1, x2, y2;
	public Coord c1, c2;
	public static Line z = new Line(0, 0, 0, 0);
	
	public Line(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.c1 = new Coord(x1, y1);
		this.c2 = new Coord(x2, y2);
	}
	
	public Line(Coord c1, Coord c2) {
		this(c1.x, c1.y, c2.x, c2.y);
	}
	
	public Line() {
		this(0, 0, 0, 0);
	}
}
