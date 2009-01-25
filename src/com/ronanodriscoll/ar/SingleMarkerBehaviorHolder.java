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
package com.ronanodriscoll.ar;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.media.Buffer;
import javax.media.format.VideoFormat;
import javax.media.j3d.Background;
import javax.media.j3d.Behavior;
import javax.media.j3d.TransformGroup;
import javax.media.util.BufferToImage;
import javax.media.util.ImageToBuffer;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.detector.NyARSingleDetectMarker;
import jp.nyatla.nyartoolkit.java3d.utils.J3dNyARRaster_RGB;
import jp.nyatla.nyartoolkit.java3d.utils.NyARSingleMarkerBehaviorListener;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureDevice;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureDeviceList;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureListener;

/**
 * This class provides the connection between Java3D, JMF and NyARToolkit.
 * Copied from jp.nyalta.nyartoolkit.java3d.utils.NyARSingleMarkerBehaviorHolder
 *
 * @author ronan
 */
public class SingleMarkerBehaviorHolder implements JmfCaptureListener {
  /**
   * The AR param.
   */
  private NyARParam _cparam;

  /**
   * Webcam capture class.
   */
  private JmfCaptureDevice _capture;

  /**
   * RGB raster class for Java3D. Note in Japanese: As a maximum of 3 threads
   * can be shared, this takes exclusive access.
   */
  private J3dNyARRaster_RGB _nya_raster;

  /**
   * Single marker detection class.
   */
  private NyARSingleDetectMarker _nya;

  /**
   * Behaviour holder.
   */
  private ARBehavior _nya_behavior;

  /**
   * Class Constructor.
   * @param i_cparam
   * @param i_rate
   * @param i_ar_code
   * @param i_marker_width
   * @throws NyARException
   */
  public SingleMarkerBehaviorHolder(NyARParam i_cparam, float i_rate,
      NyARCode i_ar_code, double i_marker_width) throws NyARException {
    this._nya_behavior = null;
    final NyARIntSize scr_size = i_cparam.getScreenSize();
    this._cparam = i_cparam;
    // Webcam capture setup.
    JmfCaptureDeviceList devlist=new JmfCaptureDeviceList();
    this._capture=devlist.getDevice(0);
    //TODO Use WebcamPane settings here.
    if(!_capture.setCaptureFormat(
        JmfCaptureDevice.PIXEL_FORMAT_RGB,320, 240, 15f)){
      if(!_capture.setCaptureFormat(JmfCaptureDevice.PIXEL_FORMAT_YUV,
          320, 240,15f)){
        throw new NyARException("Can't find correct capture format.");
      }   
    }
    this._capture.setCaptureFormat(scr_size.w, scr_size.h,15f);
    this._capture.setOnCapture(this);   
    this._nya_raster = new J3dNyARRaster_RGB(
        this._cparam,this._capture.getCaptureFormat());
    this._nya = new NyARSingleDetectMarker(
        this._cparam, i_ar_code, i_marker_width);
    this._nya_behavior = new ARBehavior(this._nya, this._nya_raster, i_rate);
  }

  /**
   * @return behavior
   */
  public Behavior getBehavior() {
    return this._nya_behavior;
  }

  /**
   * Use to configure the i_back_ground capture image
   * The i_back_ground ALLOW_IMAGE_WRITE attribution needs to be set.
   * @param i_back_ground
   */
  public void setBackGround(Background i_back_ground) {
    // Exclusive control before calling?
    this._nya_behavior.setRelatedBackGround(i_back_ground);
  }

  /**
   * Use this method to combine with the marker's TransformGroup's coordinate
   * system.
   */
  public void setTransformGroup(TransformGroup i_trgroup) {
    // Exclusive control before calling?
    this._nya_behavior.setRelatedTransformGroup(i_trgroup);
  }

  /**
   * This listener is called after recalculating the coordinate system.
   * @param i_listener
   */
  public void setUpdateListener(NyARSingleMarkerBehaviorListener i_listener) {
    // Exclusive control before calling?
    this._nya_behavior.setUpdateListener(i_listener);
  }

  /**
   * Class update call back function therefore avoid calling...
   * Previous advice comes from NYArtoolkit class. Not so in NyarToolkitLinkTest
   * TODO:Check this with Nyalta.
   */
  public void onUpdateBuffer(Buffer i_buffer) {
    try {
      synchronized (this._nya_raster) {
        // This code flips the image in the buffer to stop mirror effect.
        BufferToImage b2i = 
          new BufferToImage((VideoFormat) i_buffer.getFormat());
        Image img = b2i.createImage(i_buffer);
        if (img instanceof BufferedImage) {
          img = horizontalflip((BufferedImage) img);
        } else {
          throw new RuntimeException("Image not buffered.");
        }
        i_buffer = ImageToBuffer.createBuffer(img, 15f);
        this._nya_raster.setBuffer(i_buffer);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Flip a buffered image.
   *
   * @param img
   * @return flipped image.
   */
  public static BufferedImage horizontalflip(BufferedImage img) {  
    int w = img.getWidth();  
    int h = img.getHeight();  
    BufferedImage dimg = new BufferedImage(w, h, img.getType());  
    Graphics2D g = dimg.createGraphics();  
    g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null);  
    g.dispose();  
    return dimg;
  }

  public void start() throws NyARException {
    this._capture.start();
  }

  public void stop() {
    this._capture.stop();
  }
}
