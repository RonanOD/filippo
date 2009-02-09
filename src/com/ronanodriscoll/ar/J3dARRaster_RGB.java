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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.media.format.VideoFormat;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.rasterreader.INyARBufferReader;
import jp.nyatla.nyartoolkit.jmf.utils.JmfNyARRaster_RGB;

/**
 * Copy of J3dNyARRaster_RGB.
 *
 * @author ronan
 */
public class J3dARRaster_RGB extends JmfNyARRaster_RGB {
  private ImageComponent2D imc2d;

  private byte[] i2d_buf;

  private BufferedImage bufferd_image;

  /**
   * Constructor
   * @param i_cparam
   * @param i_format
   * @throws NyARException
   */
  public J3dARRaster_RGB(NyARParam i_cparam,VideoFormat i_format)
      throws NyARException {
    super(i_cparam.getScreenSize(),i_format);
    if(this._reader.getBufferType() != 
        INyARBufferReader.BUFFERFORMAT_BYTE1D_B8G8R8_24){
      throw new NyARException();
    }
    this.bufferd_image = new BufferedImage(
        this._size.w, this._size.h, BufferedImage.TYPE_3BYTE_BGR);
    i2d_buf = 
        ((DataBufferByte) bufferd_image.getRaster().getDataBuffer()).getData();
    this.imc2d = new ImageComponent2D(ImageComponent2D.FORMAT_RGB,
        this.bufferd_image, true, true);
    imc2d.setCapability(ImageComponent.ALLOW_IMAGE_WRITE);
  }

  public void setBuffer(javax.media.Buffer i_buffer) throws NyARException {
    this._reader.changeBuffer(i_buffer);
    synchronized (this){
      byte[] src=(byte[])this._reader.getBuffer();
      final int length = this._size.w * 3;
      int src_idx = 0;
      int dest_idx = (this._size.h - 1) * length;     
      for (int i = 0; i < this._size.h; i++) {
        System.arraycopy(src,src_idx, this.i2d_buf, dest_idx, length);
        src_idx += length;
        dest_idx -= length;
      }
      // Flip the image to remove the mirror image effect.
      flip(src, false);
    }
    return;
  }

  private void flip(byte[] src, boolean vertical) {
    int bytesPerPixel = 3 / this._size.w;
    int destBytesPerLine = this._size.w * bytesPerPixel;
    for (int srcY = 0; srcY < this._size.h; srcY++) {
      for (int srcX = 0; srcX < this._size.w; srcX++) {
        int destX = 0, destY = 0, destIndex = 0, srcIndex = 0;
        if (vertical) {
          destX = srcX;
          destY = this._size.h - srcY - 1;
        } else {
          destX = this._size.w - srcX - 1;
          destY = srcY;
        }
        destIndex = (destY * destBytesPerLine) + (destX * bytesPerPixel);
        srcIndex = (srcY * 3) + (srcX * bytesPerPixel);
        System.arraycopy(src, srcIndex, this.i2d_buf, destIndex, bytesPerPixel);
      }
    }
  }  

  public void renewImageComponent2D() {
    this.imc2d = new ImageComponent2D(
        ImageComponent2D.FORMAT_RGB, this.bufferd_image, true, true);
    this.imc2d.setCapability(ImageComponent.ALLOW_IMAGE_WRITE);
  }

  public ImageComponent2D getImageComponent2D() {
    return this.imc2d;
  }
}
