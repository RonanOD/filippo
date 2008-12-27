package com.ronanodriscoll;
/* 
 * PROJECT: Filippo Drawing Program.
 * --------------------------------------------------------------------------------
 * The MIT License
 * Copyright (c) 2008 Ronan O'Driscoll
 * ronanodriscoll(at)gmail.com
 * http://code.google.com/p/filippo/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

@SuppressWarnings("serial")
public class Filippo extends JPanel {

  /**
   * Pane for show augmented webcam view.
   */
  public WebcamPane webcamPane;

  /**
   * Toolbar for controlling display.
   */
  public JToolBar toolBar;

  /**
   * Default class constructor. Starts a splash screen and then initializes
   * application.
   */
  public Filippo() {
    new Thread(new Runnable() {
	    public void run() {
		  SplashScreen splash = new SplashScreen(3000);
		  splash.showSplash();
	    }
	  }).start();
    init();
  }

  /**
   * Main method.
   * @param s array of command line arguments.
   * @throws Exception
   */
  public static void main(String s[]) {
    new Filippo();
  }

  /**
   * Class initialization method.
   */
  private void init() {
    toolBar = new JToolBar("Ronan Toolbar");
    ImageIcon iconOpen = new ImageIcon(
    		Filippo.class.getResource("resources/folder_image.png"));
    Action actionOpen = new AbstractAction("", iconOpen) {
      public void actionPerformed(ActionEvent e) {
    	JFileChooser chooser = new JFileChooser();
    	chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
	      public boolean accept(File f) {
	        return f.getName().toLowerCase().endsWith(".jpg")
	            || f.getName().toLowerCase().endsWith(".jpeg") || 
	            f.isDirectory();
	      }

	      public String getDescription() {
	        return "Jpeg Images";
	      }
	    });
        int r = chooser.showOpenDialog(new JFrame());
        if (r == JFileChooser.APPROVE_OPTION) {
         // String name = chooser.getSelectedFile()
          //System.out.println(name);
        }
      }
    };
    JButton openButton = new JButton(actionOpen);
    openButton.setIcon(iconOpen);
    openButton.setToolTipText("Find the image to draw.");
    toolBar.add(openButton);
	webcamPane = new WebcamPane();
    webcamPane.setPreferredSize(new Dimension(WebcamPane.webcamWidth,
    		WebcamPane.webcamHeight));
    Insets ins = webcamPane.getInsets();
    webcamPane.setSize(WebcamPane.webcamWidth + ins.left + ins.right,
    		WebcamPane.webcamHeight + ins.top + ins.bottom );
    try {
		webcamPane.startCapture();
	} catch (Exception e1) {
		e1.printStackTrace();
	}
    toolBar.setMaximumSize(toolBar.getSize());
    JFrame frame = new JFrame("Filippo Drawing Application");
    URL imgURL = getClass().getResource("resources/dome.gif");
    frame.setIconImage(new ImageIcon(imgURL).getImage());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(toolBar, BorderLayout.NORTH);
    frame.getContentPane().add(webcamPane, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }
}