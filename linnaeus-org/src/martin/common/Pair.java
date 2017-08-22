package martin.common;

public class Pair<E> {
	private E x,y;
	
	public Pair(E x, E y){
		this.x = x;
		this.y = y;
	}

	public E getX() {
		return x;
	}

	public E getY() {
		return y;
	}

	public void setX(E x) {
		this.x = x;
	}

	public void setY(E y) {
		this.y = y;
	}
	
	public String toString(){
		return "(" + x.toString() + "," + y.toString() + ")";
	}
}
