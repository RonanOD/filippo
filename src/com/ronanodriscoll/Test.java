package com.ronanodriscoll;


import javax.media.*;

import javax.media.util.BufferToImage;
import javax.media.format.*;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.jmf.utils.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Test extends Frame implements JmfCaptureListener
{
  private static final long serialVersionUID = -2110888320986446576L;
  private JmfCaptureDevice _capture;
  public Test() throws NyARException
  {
    setTitle("JmfCaptureTest");
    setBounds(0, 0, 320 + 64, 240 + 64);
    JmfCaptureDeviceList dl=new JmfCaptureDeviceList();
    this._capture=dl.getDevice(0);
    if(!this._capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_RGB,320,240,30.0f)){
      if(!this._capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_YUV,320,240,30.0f)){
        throw new NyARException("No supported format");
      }
    }
    this._capture.setOnCapture(this);
  }


  public void onUpdateBuffer(Buffer i_buffer) {
    BufferToImage b2i = new BufferToImage((VideoFormat) i_buffer.getFormat());
    Image img = b2i.createImage(i_buffer);
    if (img instanceof BufferedImage) {
      img = horizontalflip((BufferedImage) img);
    } else {
      System.out.println("NOT");
    }
    Graphics g = getGraphics();
    g.drawImage(img, 32, 32, this);
  }

  public static BufferedImage horizontalflip(BufferedImage img) {  
    int w = img.getWidth();  
    int h = img.getHeight();  
    BufferedImage dimg = new BufferedImage(w, h, img.getType());  
    Graphics2D g = dimg.createGraphics();  
    g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null);  
    g.dispose();  
    return dimg;
  }    

  private void startCapture()
  {
    try {
      this._capture.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args)
  {
    try {
      Test mainwin = new Test();
      mainwin.setVisible(true);
      mainwin.startCapture();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}

