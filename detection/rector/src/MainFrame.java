import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	private JPanel imagePanel;

	private JPanel buttonPanel;
	private JButton leftButton;
	private JButton rightButton;
	private JButton saveButton;
	private JButton undoButton;

	private RectMaker rectMaker;
	private ImageGetter imageGetter;

	private File currentImg;
	private int imgWidth, imgHeight;

	public MainFrame(ImageGetter imageGetter, RectMaker rectMaker) {

		this.imageGetter = imageGetter;
		this.rectMaker = rectMaker;

		setTitle("Rect Maker");
		setLayout(new BorderLayout());

		/* img panel */
		imagePanel = new JPanel(new BorderLayout());
		attachImage(imageGetter.getCurrentImage());
		ImagePanelMouseListener imagePanelMouseListener = new ImagePanelMouseListener();
		imagePanel.addMouseListener(imagePanelMouseListener);
		imagePanel.addMouseMotionListener(imagePanelMouseListener);
		add(imagePanel, BorderLayout.CENTER);

		/* button panel */
		buttonPanel = new JPanel();
		leftButton = new JButton("<");
		rightButton = new JButton(">");
		saveButton = new JButton("Save");
		undoButton = new JButton("Ctrl + z");
		leftButton.addActionListener(new arrowButtonButtonListener(true));
		rightButton.addActionListener(new arrowButtonButtonListener(false));
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rectMaker.saveJSON();
			}
		});
		undoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});

		buttonPanel.setLayout(new GridLayout(1, 4));
		buttonPanel.add(leftButton);
		buttonPanel.add(rightButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(undoButton);

		add(buttonPanel, BorderLayout.NORTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		pack();
		setLocation(400,400);
		setVisible(true);
	}

	private void attachImage(File img){
		try{
			currentImg = img;

			BufferedImage bufferedImage = ImageIO.read(img);
			ImageIcon imageIcon = new ImageIcon(ImageIO.read(img));

			imagePanel.removeAll();

			imagePanel.add(new JLabel(imageIcon), BorderLayout.CENTER);

			imgWidth = bufferedImage.getWidth();
			imgHeight = bufferedImage.getHeight();
			imagePanel.setSize(imgWidth, imgHeight);


			imagePanel.revalidate();
			imagePanel.repaint();
			System.out.println(img.getName()+"("+bufferedImage.getWidth() + "*" + bufferedImage.getHeight()+")");
			
		}
		catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
	}

	class ImagePanelMouseListener implements MouseListener,MouseMotionListener{

		private int startX, startY;
		private int endX, endY;


		@Override
		public void mouseReleased(MouseEvent e) {

			if(e.getX() < 0) endX = 0;
			else if(e.getX() > imgWidth) endX = imgWidth;
			else endX = e.getX();

			if(e.getY() < 0) endY = 0;
			else if(e.getY() > imgHeight) endY = imgHeight;
			else endY = e.getY();

			rectMaker.appendRect(currentImg, startX, startY, endX, endY);
		}
		@Override
		public void mousePressed(MouseEvent e) {
			startX = e.getX(); startY = e.getY();
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			Graphics graphics = imagePanel.getGraphics();

			graphics.drawLine(startX, startY, startX, e.getY());
			graphics.drawLine(startX, startY, e.getX(), startY);
			graphics.drawLine(startX, e.getY(), e.getX(), e.getY());
			graphics.drawLine(e.getX(), startY, e.getX(), e.getY());
			imagePanel.repaint();
		}
		@Override
		public void mouseClicked(MouseEvent e) {
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		@Override
		public void mouseMoved(MouseEvent e) {
		}
	}
	class arrowButtonButtonListener implements ActionListener{

		private boolean isLeft;

		public arrowButtonButtonListener(boolean isLeft) {
			this.isLeft = isLeft; 
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try{
				if(isLeft) attachImage(imageGetter.getPreviousImage());
				else attachImage(imageGetter.getNextImage());
			}
			catch(Exception ex){
				JOptionPane.showMessageDialog(null, ex.getMessage());
			}
		}

	}
}
