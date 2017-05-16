
public class Main {

	public static void main(String[] args) {
		
		ImageGetter imageGetter = new ImageGetter(".\\img\\");
		RectMaker rectMaker = new RectMaker();
		MainFrame mainFrame = new MainFrame(imageGetter, rectMaker);
		
	}

}
