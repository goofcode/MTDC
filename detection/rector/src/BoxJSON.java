import java.awt.Rectangle;
import java.util.ArrayList;

public class BoxJSON {
	
	private ArrayList<Boxes> list;
	
	public BoxJSON(){
		list = new ArrayList<Boxes>();
	}
	
	public void append(String fileName, Rectangle rect) {
		for(Boxes boxes : list){
			//append rect if already exist
			if(boxes.getFileName().equals(fileName)){
				boxes.append(rect);
				return;
			}
		}
		list.add(new Boxes(fileName, rect));
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[\n");
		for(int i=0; i<list.size(); i++){
			Boxes info = list.get(i);
			builder.append("  {\n");
			builder.append("    \"image_path\": \""+info.getFileName()+"\",\n\n");
			builder.append("    \"rects\": [\n");
			
			for(int j =0; j<info.getSize(); j++){
				Rectangle rect = info.getRect(j);
				builder.append("      {\n");
				builder.append("        \"x1\": " + (double)rect.getX() +",\n");
				builder.append("        \"y1\": " + (double)rect.getY() +",\n");
				builder.append("        \"x2\": " + (double)(rect.getX()+rect.getWidth()) +",\n");
				builder.append("        \"y2\": " + (double)(rect.getY()+rect.getHeight())+"\n");
				if(j != info.getSize() -1) builder.append("      },\n");
				else builder.append("      }\n");
			}
			builder.append("    ]\n");
			if(i != list.size() -1) builder.append("  },\n");
			else builder.append("  }\n");
		}
		builder.append("]\n");
		return builder.toString();		
	}
}
