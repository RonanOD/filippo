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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

@SuppressWarnings("serial")
public class Filippo extends JPanel {

  /**
   * Pane for show augmented webcam view.
   */
  private WebcamPane webcamPane;

  /**
   * Toolbar for controlling display.
   */
  private JToolBar toolBar;

  /**
   * Frame for displaying application.
   */
  private JFrame frame;

  /**
   * The image to display.
   */
  private File imageFile;

  /**
   * Image scale amount.
   */
  private float imageScale;

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
    try {
      new Filippo();
    } catch (Exception e) {
      reportException(e);
    }
  }

  /**
   * Report an exception in a dialog.
   *
   * @param e Exception to report
   */
  public static void reportException(Throwable e) {
    JOptionPane.showMessageDialog(new JFrame(),
        e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Load a resource from a jar to a local temporary directory. File to be
   * deleted on VM shutdown.
   *
   * @param resourceName Name of resource to load.
   * @return full file path to resource. To be deleted on exit.
   * @throws IOException 
   */
  public static String loadResourceLocally(String resourceName)
      throws IOException {
    InputStream is = Filippo.class.getResourceAsStream(resourceName);
    String newFileName = resourceName.replaceAll("/", "_");
    File temp = File.createTempFile(newFileName, ".tmp");
    temp.deleteOnExit();
    final OutputStream output = new FileOutputStream(temp);  
    // get an channel from the stream  
    final ReadableByteChannel inputChannel = Channels.newChannel(is);  
    final WritableByteChannel outputChannel = Channels.newChannel(output);
    // copy the channels  
    ChannelTools.fastChannelCopy(inputChannel, outputChannel);  
    // closing the channels  
    inputChannel.close();
    outputChannel.close();  
    return temp.getAbsolutePath();
  }

  /**
   * Class initialization method.
   */
  private void init() {
    // First set up variables that webcam pane needs to draw.
    try {
      imageFile = 
        new File(loadResourceLocally("resources/duomo.jpg"));
    } catch (Exception e) {
      reportException(e);
    }
    imageScale = 100f;
    // Initialize toolbar.
    toolBar = new JToolBar("Filippo Toolbar");
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
          imageFile = chooser.getSelectedFile();
          resetWebcam();
        }
      }
    };
    // File open button
    JButton openButton = new JButton(actionOpen);
    openButton.setIcon(iconOpen);
    openButton.setToolTipText("Find the image to draw.");
    toolBar.add(openButton);
    toolBar.addSeparator();
    // Scale text field.
    toolBar.add(new JLabel("Scale:"));
    JTextField scaleText = new JTextField();
    scaleText.setMaximumSize(new Dimension(48, 24));
    scaleText.setText("100");
    scaleText.addFocusListener(new FocusListener () {
      public void focusGained(FocusEvent e) { }
      public void focusLost(FocusEvent e) {
        JTextField field = (JTextField) e.getComponent();
        try {
          float newScale = Float.parseFloat(field.getText());
          if (newScale != imageScale) {
            imageScale = newScale;
          }
          field.setText(Float.toString(imageScale));
        } catch (Exception ex) {
          reportException(ex);
        }
      }});
    toolBar.add(scaleText);
    ImageIcon iconResize = new ImageIcon(
        Filippo.class.getResource("resources/shape_move_backwards.png"));
    Action actionResize = new AbstractAction("", iconResize) {
      public void actionPerformed(ActionEvent e) {
        resetWebcam();
      }
    };
    // Resize button
    JButton resizeButton = new JButton(actionResize);
    resizeButton.setIcon(iconResize);
    resizeButton.setToolTipText("Resize the image.");
    toolBar.add(resizeButton);
    // Webcam settings button
    final ImageIcon iconWebcam = new ImageIcon(
        Filippo.class.getResource("resources/webcam.png"));
    Action actionWebcam = new AbstractAction("", iconWebcam) {
      public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(frame,
            "Webcam settings not yet implemented",
            "Webcam Settings",
            JOptionPane.INFORMATION_MESSAGE,
            iconWebcam);
      }
    };
    JButton webcamButton = new JButton(actionWebcam);
    webcamButton.setIcon(iconWebcam);
    webcamButton.setToolTipText("Webcam Settings.");
    toolBar.add(webcamButton);
    // Colour settings button
    final ImageIcon iconColour = new ImageIcon(
        Filippo.class.getResource("resources/color_wheel.png"));
    Action actionColour = new AbstractAction("", iconColour) {
      public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(frame,
            "Colour details not yet implemented.\n" +
            "This will allow you to pick a colour from your image\n" +
            "and see what paints mix best with it.",
            "Colour Details",
            JOptionPane.INFORMATION_MESSAGE,
            iconColour);
      }
    };
    JButton colourButton = new JButton(actionColour);
    colourButton.setIcon(iconColour);
    colourButton.setToolTipText("Find Colour Details.");
    toolBar.add(colourButton);
    toolBar.addSeparator();
    // Help and home button
    final ImageIcon iconHome = new ImageIcon(
        Filippo.class.getResource("resources/world.png"));
    Action actionHome = new AbstractAction("", iconHome) {
      public void actionPerformed(ActionEvent e) {
        try {
          Desktop.getDesktop().browse(
              new URI("http://ronanodriscoll.googlepages.com/filippo"));
        } catch (Exception ex) {
          reportException(ex);
        } 
      }
    };
    JButton homeButton = new JButton(actionHome);
    homeButton.setIcon(iconHome);
    homeButton.setToolTipText("Application details.");
    toolBar.add(homeButton);
    // Webcam initialize.
	  initWebcam(imageFile, imageScale);
    toolBar.setMaximumSize(toolBar.getSize());
    frame = new JFrame("Filippo Drawing Application");
    URL imgURL = getClass().getResource("resources/dome.gif");
    frame.setIconImage(new ImageIcon(imgURL).getImage());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(toolBar, BorderLayout.NORTH);
    frame.getContentPane().add(webcamPane, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }

  /**
   * Initialize the webcam pane;
   *
   * @param imageFile
   * @param scale image scale
   */
  private void initWebcam(File imageFile, float scale) {
	try {
	  webcamPane = new WebcamPane(imageFile, scale);
	} catch (java.lang.UnsatisfiedLinkError e) {
	  reportException(e);
	  return;
	}
    webcamPane.setPreferredSize(new Dimension(WebcamPane.webcamWidth,
    		WebcamPane.webcamHeight));
    Insets ins = webcamPane.getInsets();
    webcamPane.setSize(WebcamPane.webcamWidth + ins.left + ins.right,
    		WebcamPane.webcamHeight + ins.top + ins.bottom );
    try {
		webcamPane.startCapture();
  	} catch (Exception e1) {
  	  reportException(e1);
  	}
  }

  /**
   * Reset the webcam to take new user parameters.
   */
  private void resetWebcam() {
    // Save current width and height.
    Dimension current = frame.getSize();
    webcamPane.stopCapture();
    frame.getContentPane().remove(webcamPane);
    initWebcam(imageFile, imageScale);
    frame.getContentPane().add(webcamPane, BorderLayout.CENTER);
    frame.pack();
    frame.repaint();
    frame.setSize(current); // Reset current size.
  }
}