package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JPanel;


public class KeyBoard extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1090471423584051664L;

	ImageIcon icon = new ImageIcon("pics" + File.separator + "keyboarda.png");
	 /*光标进入按钮判断*/
    private boolean hover;
	public KeyBoard() {

	  }

	  public void paintComponent(Graphics g) {
		  Graphics2D g2d = (Graphics2D) g.create();
		  RoundRectangle2D rect = new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),7,7);
		  Shape clip = g2d.getClip();
		  g2d.setClip(rect);
		  g2d.setClip(clip);
		   int x = 0, y = 0;
		   float tran = 1F;
			if (!hover)
			{/* 鼠标离开/进入时的透明度改变量 */
			    tran = 0.8F;
			}
	   this.addMouseListener(new MouseAdapter(){
		   @Override
		    public void mouseEntered(MouseEvent e)
		    {
			   hover = true;
			   icon = new ImageIcon("pics" + File.separator + "keyboardb.png");
			   repaint();
		    }
		    @Override
		    public void mouseExited(MouseEvent e)
		    {
				hover = false;
				icon = new ImageIcon("pics" + File.separator + "keyboarda.png");
				repaint();
		    }
		   
	   });
	   //setBackground(new Color(197, 197, 197));
	   
	   g2d.drawImage(icon.getImage(), x, y, getSize().width,getSize().height, this);// 图片会自动缩放
	   
	   //g2d.drawImage(icon.getImage(), x, y,this);//图片不会自动缩放
	  }

}
