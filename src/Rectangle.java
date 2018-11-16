package cn.sym.day10;

public class Rectangle {

	private double length;
	private double width;
	int i;
	
	public Rectangle(double l, double w) {
		length = l;
		width = w;
	}
	
	public double area() {
		int i;
		return length * width;
	}
	
	public double perimater() {
		return 2 * (length + width);
	}
	
	public static int sum() {
		int i;
		int sum = 0;
		for(int j = 1; j <= 100; j++) {
			sum += j;
		}
		return sum;
	}
}
