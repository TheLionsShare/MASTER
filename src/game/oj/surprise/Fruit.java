package game.oj.surprise;

public class Fruit {
	
	int left, top, bottom, right; 
	int rate;
	
	public Fruit(int l, int r, int drop){
		left = l;
		right = r;
		rate = drop;
	}
	
	public void drop(){
		top += rate;
		bottom += rate;
	}
	
}
