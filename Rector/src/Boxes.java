import java.awt.Rectangle;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Boxes {

	private String fileName;
	private ArrayList<Rectangle> boxes;
	
	public Boxes(String fileName) {
		this.fileName = fileName;
		boxes = new ArrayList<Rectangle>();
	}
	public Boxes(String fileName, Rectangle rect){
		this(fileName);
		boxes.add(rect);
	}
	
	public void append(Rectangle rect){
		boxes.add(rect);
	}
	
	public String getFileName(){return fileName;}
	public int getSize(){ return boxes.size();} 
	public Rectangle getRect(int index){return boxes.get(index);}
}
