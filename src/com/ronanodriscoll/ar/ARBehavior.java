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

import java.util.Enumeration;

import javax.media.j3d.Background;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupOnElapsedTime;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;
import jp.nyatla.nyartoolkit.detector.NyARSingleDetectMarker;
import jp.nyatla.nyartoolkit.java3d.utils.J3dNyARRaster_RGB;
import jp.nyatla.nyartoolkit.java3d.utils.NyARSingleMarkerBehaviorListener;

/**
 * Behaviour class copied from 
 * jp.nyalta.nyartoolkit.java3d.utils.NyARSingleMarkerBehaviorHolder
 * @author ronan
 */
public class ARBehavior extends Behavior {
  private NyARTransMatResult trans_mat_result = new NyARTransMatResult();

  private NyARSingleDetectMarker related_nya;

  private TransformGroup trgroup;

  private Background back_ground;

  private J3dNyARRaster_RGB raster;

  private WakeupCondition wakeup;

  private NyARSingleMarkerBehaviorListener listener;

  public void initialize()
  {
    wakeupOn(wakeup);
  }

  /**
   * Behavior class constructor.
   *
   * @param i_back_ground
   * @param i_related_ic2d
   */
  public ARBehavior(NyARSingleDetectMarker i_related_nya,
      J3dNyARRaster_RGB i_related_raster, float i_rate) {
    super();
    wakeup = new WakeupOnElapsedTime((int) (1000 / i_rate));
    related_nya = i_related_nya;
    trgroup = null;
    raster = i_related_raster;
    back_ground = null;
    listener = null;
    this.setSchedulingBounds(new BoundingSphere(new Point3d(), 100.0));
  }

  public void setRelatedBackGround(Background i_back_ground)
  {
    synchronized (raster) {
      back_ground = i_back_ground;
    }
  }

  public void setRelatedTransformGroup(TransformGroup i_trgroup)
  {
    synchronized (raster) {
      trgroup = i_trgroup;
    }
  }

  public void setUpdateListener(NyARSingleMarkerBehaviorListener i_listener)
  {
    synchronized (raster) {
      listener = i_listener;
    }
  }

  /**
   * いわゆるイベントハンドラ
   */
  public void processStimulus(Enumeration criteria)
  {
    try {
      synchronized (raster) {
        Transform3D t3d = null;
        boolean is_marker_exist = false;
        if (back_ground != null) {
          raster.renewImageComponent2D();// In case of DirectX
          back_ground.setImage(raster.getImageComponent2D());
        }
        if (raster.hasData()) {
          is_marker_exist = related_nya.detectMarkerLite(raster, 100);
          if (is_marker_exist)
          {
            final NyARTransMatResult src = this.trans_mat_result;
            related_nya.getTransmationMatrix(src);
            Matrix4d matrix = new Matrix4d(
                -src.m00, -src.m10, src.m20, 0,
                -src.m01, -src.m11, src.m21, 0,
                -src.m02, -src.m12, src.m22, 0,
                 -src.m03,-src.m13, src.m23, 1);
            matrix.transpose();
            t3d = new Transform3D(matrix);
            if (trgroup != null) {
              trgroup.setTransform(t3d);
            }
          }
        }
        if (listener != null) {
          listener.onUpdate(is_marker_exist, t3d);
        }
      }
      wakeupOn(wakeup);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}