import java.awt.Rectangle;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RectMaker {
	
	private BoxJSON boxJSON;
	
	public RectMaker() {
		boxJSON = new BoxJSON();
	}
	
	public void appendRect(File img, int x1, int y1, int x2, int y2){
		boxJSON.append(img.getName(), new Rectangle(x1, y1, x2-x1, y2-y1));
		System.out.println(x1 + ", " + y1 + " -> " + x2 +", " + y2);
	}
	
	public void saveJSON(){
		try {
			String fileName = new SimpleDateFormat("yyyyMMddHHmm'.json'").format(new Date());
			FileWriter writer = new FileWriter(fileName);
			
			writer.write(boxJSON.toString());
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
