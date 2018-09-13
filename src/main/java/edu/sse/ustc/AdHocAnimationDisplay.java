package edu.sse.ustc;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class AdHocAnimationDisplay extends Panel{


    Image backIm;
    Graphics bg;

    int ww, wh;
    AdHocEnv env;
    Color bgColor;


    AdHocAnimationDisplay(AdHocEnv theEnv) {
	env = theEnv;
    }

    void setBackgroundColor(Color bgC) {
	setBackground(bgC);
    }

    public void paint(Graphics g) {
	update(g);
    }


  public void update(Graphics g) {
      while (getSize().width==0) ;
      ww = getSize().width;
      while (getSize().height==0) ;
      wh = getSize().height;
      if (backIm == null ||
	  backIm.getWidth(this) < ww ||
	  backIm.getHeight(this) < wh) {
	  backIm = createImage(ww,wh);
	  bg = backIm.getGraphics();
      }

      // clear window
      bg.clearRect(0,0,ww,wh);

      // draw each object
      env.draw(bg);

      // draw the accumulated background image
      g.drawImage(backIm, 0, 0, this);
  }

}





