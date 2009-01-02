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
package com.ronanodriscoll;

import java.awt.BorderLayout;
import javax.media.j3d.*;

import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.image.TextureLoader;
import java.io.File;

import javax.swing.JPanel;
import javax.vecmath.*;

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.java3d.utils.*;


/**
 * Webcam panel. 
 *
 */
@SuppressWarnings("serial")
public class WebcamPane extends JPanel
  implements NyARSingleMarkerBehaviorListener {
 /**
  * Webcam screen width.  
  */
  public static int webcamWidth = 320;
 /**
  * Webcam screen height.     
  */
  public static int webcamHeight = 240;
 /**
  * Amount of alpha blend of image.
  */
  private static final float ALPHA_BLEND_AMOUNT =  0.5f;

  private final String CARCODE_FILE = "resources/patt.hiro";

  private final String PARAM_FILE = "resources/camera_para.dat";

  private NyARSingleMarkerBehaviorHolder nya_behavior;

  private J3dNyARParam ar_param;

  private Canvas3D canvas;

  private Locale locale;

  private VirtualUniverse universe;

  /**
   * The image to display.
   */
  private File imageFile;

  /**
   * Image scale amount.
   */
  private float imageScale;

  /**
   * Webcam pane constructor.
   *
   * @param image Image file
   */
  public WebcamPane(File image, float scaleAmount) {
    try {
      this.imageFile = image;
      this.imageScale = scaleAmount;
      //NyARToolkit Setup
      NyARCode ar_code = new NyARCode(16, 16);      
      ar_code.loadARPattFromFile(Filippo.loadResourceLocally(CARCODE_FILE));
      ar_param = new J3dNyARParam();
      ar_param.loadARParamFromFile(Filippo.loadResourceLocally(PARAM_FILE));
      ar_param.changeScreenSize(320, 240);
      universe = new VirtualUniverse();
      locale = new Locale(universe);
      canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
      View view = new View();
      ViewPlatform viewPlatform = new ViewPlatform();
      view.attachViewPlatform(viewPlatform);
      view.addCanvas3D(canvas);
      view.setPhysicalBody(new PhysicalBody());
      view.setPhysicalEnvironment(new PhysicalEnvironment());

      Transform3D camera_3d = ar_param.getCameraTransform();
      view.setCompatibilityModeEnable(true);
      view.setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
      view.setLeftProjection(camera_3d);
      TransformGroup viewGroup = new TransformGroup();
      Transform3D viewTransform = new Transform3D();
      viewTransform.rotY(Math.PI);
      viewTransform.setTranslation(new Vector3d(0.0, 0.0, 0.0));
      viewGroup.setTransform(viewTransform);
      viewGroup.addChild(viewPlatform);
      BranchGroup viewRoot = new BranchGroup();
      viewRoot.addChild(viewGroup);
      locale.addBranchGraph(viewRoot);
      Background background = new Background();
      BoundingSphere bounds = new BoundingSphere();
      bounds.setRadius(10.0);
      background.setApplicationBounds(bounds);
      background.setImageScaleMode(Background.SCALE_FIT_ALL);
      background.setCapability(Background.ALLOW_IMAGE_WRITE);
      BranchGroup root = new BranchGroup();
      root.addChild(background);
      TransformGroup transform = new TransformGroup();
      transform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      transform.addChild(createSceneGraph());
      root.addChild(transform);
      nya_behavior = new NyARSingleMarkerBehaviorHolder(ar_param, 30f, 
          ar_code, 0.08);
      nya_behavior.setTransformGroup(transform);
      nya_behavior.setBackGround(background);
      root.addChild(nya_behavior.getBehavior());
      nya_behavior.setUpdateListener(this);
      locale.addBranchGraph(root);
  
      setLayout(new BorderLayout());
      add(canvas, BorderLayout.CENTER);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

/* (non-Javadoc)
   * @see jp.nyatla.nyartoolkit.java3d.utils.NyARSingleMarkerBehaviorListener#onUpdate(boolean, javax.media.j3d.Transform3D)
   */
  public void onUpdate(boolean i_is_marker_exist, Transform3D i_transform3d) {
    if (i_is_marker_exist) {
      /*
       * TODO:Please write your behavior operation code here.
       **/
    }
  }

  public void startCapture() throws Exception {
    nya_behavior.start();
  }

  public void stopCapture() {
    nya_behavior.stop();
  }

  /**
   * Alpha blended visible texture.
   */
  private Node createSceneGraph() {
    TransformGroup tg = new TransformGroup();
    Transform3D mt = new Transform3D();
    mt.setTranslation(new Vector3d(0.00d, 0.00d, 20 * 0.001d));
    tg.setTransform(mt);
    float scale = imageScale * 0.001f;
    Appearance imageAppearance = new Appearance();
    imageAppearance.setTransparencyAttributes(new TransparencyAttributes(
        TransparencyAttributes.BLENDED, ALPHA_BLEND_AMOUNT));
    QuadArray imagePoly = new QuadArray(4, 
            QuadArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);
    imagePoly.setCoordinate(0, new Point3f(0f * scale, 0f * scale, 0f * scale));
    imagePoly.setCoordinate(1, new Point3f(2f * scale, 0f * scale, 0f * scale));
    imagePoly.setCoordinate(2, new Point3f(2f * scale, 3f * scale, 0f * scale));
    imagePoly.setCoordinate(3, new Point3f(0f * scale, 3f * scale, 0f * scale));
    TexCoord2f[] texCoords = {
          new TexCoord2f(0.0f,0.0f),
          new TexCoord2f(1.0f,0.0f), 
          new TexCoord2f(1.0f,1.0f),
          new TexCoord2f(0.0f,1.0f)
    };
    imagePoly.setTextureCoordinates(0, 0, texCoords);
    Texture texImage = new TextureLoader(
      imageFile.getAbsolutePath(), this).getTexture();
    imageAppearance.setTexture (texImage);
    tg.addChild(new Shape3D (imagePoly, imageAppearance));
    return tg;
  }
}