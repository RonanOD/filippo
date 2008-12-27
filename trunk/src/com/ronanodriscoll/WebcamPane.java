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
import java.awt.*;
import javax.swing.JPanel;
import javax.vecmath.*;

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.java3d.utils.*;


/**
 * Java3Dサンプルプログラム
 * 単一マーカー追跡用のBehaviorを使って、背景と１個のマーカーに連動した
 * TransformGroupを動かします。
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

  private final String CARCODE_FILE = "Data/patt.hiro";

  private final String PARAM_FILE = "Data/camera_para.dat";

  //NyARToolkit関係
  private NyARSingleMarkerBehaviorHolder nya_behavior;

  private J3dNyARParam ar_param;

  //universe関係
  private Canvas3D canvas;

  private Locale locale;

  private VirtualUniverse universe;

/**
 * @param args
 */
  public static void main(String[] args) {
    try {
      WebcamPane frame = new WebcamPane();
      frame.setVisible(true);
      Insets ins = frame.getInsets();
      frame.setSize(webcamWidth + ins.left + ins.right,
          webcamHeight + ins.top + ins.bottom );
      frame.startCapture();
      //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void onUpdate(boolean i_is_marker_exist, Transform3D i_transform3d) {
    if (i_is_marker_exist) {
      /*
       * TODO:Please write your behavior operation code here.
        * マーカーの姿勢を元に他の３Dオブジェクトを操作するときは、ここに処理を書きます。*/
    }
  }

  public void startCapture() throws Exception {
    nya_behavior.start();
  }

  public WebcamPane() {
    try {
      //NyARToolkitの準備
      NyARCode ar_code = new NyARCode(16, 16);
      ar_code.loadARPattFromFile(CARCODE_FILE);
      ar_param = new J3dNyARParam();
      ar_param.loadARParamFromFile(PARAM_FILE);
      ar_param.changeScreenSize(320, 240);
      //localeの作成とlocateとviewの設定
      universe = new VirtualUniverse();
      locale = new Locale(universe);
      canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
      View view = new View();
      ViewPlatform viewPlatform = new ViewPlatform();
      view.attachViewPlatform(viewPlatform);
      view.addCanvas3D(canvas);
      view.setPhysicalBody(new PhysicalBody());
      view.setPhysicalEnvironment(new PhysicalEnvironment());
  
      //視界の設定(カメラ設定から取得)
      Transform3D camera_3d = ar_param.getCameraTransform();
      view.setCompatibilityModeEnable(true);
      view.setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
      view.setLeftProjection(camera_3d);
  
      //視点設定(0,0,0から、Y軸を180度回転してZ+方向を向くようにする。)
      TransformGroup viewGroup = new TransformGroup();
      Transform3D viewTransform = new Transform3D();
      viewTransform.rotY(Math.PI);
      viewTransform.setTranslation(new Vector3d(0.0, 0.0, 0.0));
      viewGroup.setTransform(viewTransform);
      viewGroup.addChild(viewPlatform);
      BranchGroup viewRoot = new BranchGroup();
      viewRoot.addChild(viewGroup);
      locale.addBranchGraph(viewRoot);
  
      //バックグラウンドの作成
      Background background = new Background();
      BoundingSphere bounds = new BoundingSphere();
      bounds.setRadius(10.0);
      background.setApplicationBounds(bounds);
      background.setImageScaleMode(Background.SCALE_FIT_ALL);
      background.setCapability(Background.ALLOW_IMAGE_WRITE);
      BranchGroup root = new BranchGroup();
      root.addChild(background);
  
      //TransformGroupで囲ったシーングラフの作成
      TransformGroup transform = new TransformGroup();
      transform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      transform.addChild(createSceneGraph());
      root.addChild(transform);
  
      //NyARToolkitのBehaviorを作る。(マーカーサイズはメートルで指定すること)
      nya_behavior = new NyARSingleMarkerBehaviorHolder(ar_param, 30f, 
          ar_code, 0.08);
      //Behaviorに連動するグループをセット
      nya_behavior.setTransformGroup(transform);
      nya_behavior.setBackGround(background);
      //出来たbehaviorをセット
      root.addChild(nya_behavior.getBehavior());
      nya_behavior.setUpdateListener(this);
  
      //表示ブランチをLocateにセット
      locale.addBranchGraph(root);
  
      //ウインドウの設定
      setLayout(new BorderLayout());
      add(canvas, BorderLayout.CENTER);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Alpha blended visible texture.
   */
  private Node createSceneGraph() {
    TransformGroup tg = new TransformGroup();
    Transform3D mt = new Transform3D();
    mt.setTranslation(new Vector3d(0.00, 0.0, 20 * 0.001));
    tg.setTransform(mt);
    float scale = 50f * 0.001f;
    Appearance polygon1Appearance = new Appearance();
    polygon1Appearance.setTransparencyAttributes(new TransparencyAttributes(
        TransparencyAttributes.BLENDED, ALPHA_BLEND_AMOUNT));
    QuadArray polygon1 = new QuadArray(4, 
            QuadArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);
    polygon1.setCoordinate(0, new Point3f(0f * scale, 0f * scale, 0f * scale));
    polygon1.setCoordinate(1, new Point3f(2f * scale, 0f * scale, 0f * scale));
    polygon1.setCoordinate(2, new Point3f(2f * scale, 3f * scale, 0f * scale));
    polygon1.setCoordinate(3, new Point3f(0f * scale, 3f * scale, 0f * scale));
    TexCoord2f[] texCoords = {
          new TexCoord2f(0.0f,0.0f),
          new TexCoord2f(1.0f,0.0f), 
          new TexCoord2f(1.0f,1.0f),
          new TexCoord2f(0.0f,1.0f)
    };
    polygon1.setTextureCoordinates(0, 0, texCoords);
    Texture texImage = new TextureLoader(
      getClass().getResource("resources/duomo.jpg"), this).getTexture();
    polygon1Appearance.setTexture (texImage);
    tg.addChild(new Shape3D (polygon1, polygon1Appearance));
    return tg;
  }
}
