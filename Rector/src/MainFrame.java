import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.w3c.dom.css.Rect;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

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
	
	public MainFrame(ImageGetter imageGetter, RectMaker rectMaker) {
		
		this.imageGetter = imageGetter;
		this.rectMaker = rectMaker;
		
		setTitle("Rect Maker");
		setLayout(new BorderLayout());
		
		imagePanel = new JPanel(new BorderLayout());
		attachImage(imageGetter.getCurrentImage());
		imagePanel.addMouseListener(new MouseListener() {
			private int startX, startY;
			
			@Override
			public void mouseReleased(MouseEvent e) {
				rectMaker.appendRect(currentImg, startX, startY, e.getX(), e.getY() );
			}
			@Override
			public void mousePressed(MouseEvent e) {
				startX = e.getX(); startY = e.getY();
			}
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {
			}
		});
		add(imagePanel, BorderLayout.CENTER);

		buttonPanel = new JPanel();
		leftButton = new JButton("<");
		rightButton = new JButton(">");
		saveButton = new JButton("Save");
		undoButton = new JButton("Ctrl + z");
		leftButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					attachImage(imageGetter.getPreviousImage());
				}
				catch(Exception ex){
					JOptionPane.showMessageDialog(null, ex.getMessage());
				}
			}
		});
		rightButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					attachImage(imageGetter.getNextImage());
				}
				catch(Exception ex){
					JOptionPane.showMessageDialog(null, ex.getMessage());
				}
			}
		});
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
		//setSize(640, 480);
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
			imagePanel.setSize(bufferedImage.getWidth(), bufferedImage.getHeight());
			imagePanel.revalidate();
			imagePanel.repaint();
		}
		 catch (IOException ioEx) {
			ioEx.printStackTrace();
		 }
	}
}
