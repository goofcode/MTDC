import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ImageGetter {

	private ArrayList<File> imageFiles;
	private int currentImageIndex;
	
	public ImageGetter(String directoryPath){
		File imageFolder = new File(directoryPath);
		imageFiles = new ArrayList<File>(Arrays.asList(imageFolder.listFiles()));
		currentImageIndex = 0;
	}
	
	public File getCurrentImage(){
		return imageFiles.get(currentImageIndex);
	}
	public File getNextImage() throws Exception{
		if(currentImageIndex == imageFiles.size() -1)
			throw new Exception("Last File");
		
		return imageFiles.get(++currentImageIndex);
	}
	public File getPreviousImage() throws Exception{
		if(currentImageIndex == 0)
			throw new Exception("First File");
		
		return imageFiles.get(--currentImageIndex);
	}
}
