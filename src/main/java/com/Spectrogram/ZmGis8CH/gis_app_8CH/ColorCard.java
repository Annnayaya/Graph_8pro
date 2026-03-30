package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.VolatileImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JComponent;

class ColorCard extends JComponent implements Printable {

	public static boolean negativePrint = false;
	public boolean drawing;
	VolatileImage img;
	Graphics2D vg;
	
	public ColorCard(int i,int j)
	{
		this(i, j, null);
	}
	
	public ColorCard(int i, int j, Object object) {
		
	}
	public void paint(Graphics g)
	{
		drawing = true;
		int i = getWidth();
		int j = getHeight();

			img = createVolatileImage(i, j);
			makeDrawable();
			redraw2D(img.getGraphics());
		g.drawImage(img, 0, 0, this);
		drawing = false;
		if (GisXYZ.hwtest != 0)
		{
			Graphics2D graphics2d = (Graphics2D)g;
			AlphaComposite alphacomposite = AlphaComposite.getInstance(3, 0.25F);
			graphics2d.setComposite(alphacomposite);
			graphics2d.setFont(GisXYZ.fGLb);
			graphics2d.setColor(Color.WHITE);
			graphics2d.drawString("H/W", getWidth() / 2 - 40, getHeight() / 2 - 10);
			graphics2d.drawString("TEST", getWidth() / 2 - 60, getHeight() / 2 + 40);
		}
		g.dispose();
	}
	
	public void makeDrawable() 
	{
		if (img == null)
		{
			img = createVolatileImage(getWidth(), getHeight());
			if (vg != null)
			{
				vg.dispose();
				vg.finalize();
			}
			vg = img.createGraphics();
		}
	}
	
	public void draw(Graphics g, int i, int j, int k, int l)
	{
		redraw2D(g,i,j,k,l);
	}
	
	void redraw2D(Graphics g)
	{
		redraw2D(g,this.getWidth(),this.getHeight(),0,0);
	}
	/*
	void redraw2D(Graphics g, int i, int j, int k, int l)
	{
		Graphics2D graphics2d = (Graphics2D)g;
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

		graphics2d.setPaint(Color.BLACK);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.DARK_GRAY)) : ((java.awt.Paint) (Color.LIGHT_GRAY)));
		graphics2d.fillRect(0, 0, i, j);

		GradientPaint paint0 = new GradientPaint(0,0,Color.RED,0,(int)(j/5.0D),Color.BLUE);
		GradientPaint paint01 = new GradientPaint(0,0,Color.GREEN,0,(int)(j/5.0D),Color.YELLOW);
		graphics2d.setPaint(Option.gt ? paint0 :paint01);
		graphics2d.fillRect(0, 0, 15, (int)(j/5.0D));
		
		GradientPaint paint1 = new GradientPaint(0,(int)(j/5.0D),Color.BLUE,0,(int)(2*j/5.0D),Color.GREEN);
		GradientPaint paint11 = new GradientPaint(0,(int)(j/5.0D),Color.YELLOW,0,(int)(2*j/5.0D),Color.RED);
		graphics2d.setPaint(Option.gt ? paint1 :paint11);
		graphics2d.fillRect(0, (int)(j/5.0D), 15, (int)(2*j/5.0D));
		
		GradientPaint paint2 = new GradientPaint(0,(int)(2*j/5.0D),Color.GREEN,0,(int)(3*j/5.0D),Color.YELLOW);
		GradientPaint paint21 = new GradientPaint(0,(int)(2*j/5.0D),Color.RED,0,(int)(3*j/5.0D),Color.BLUE);
		graphics2d.setPaint(Option.gt ? paint2 :paint21);
		graphics2d.fillRect(0, (int)(2*j/5.0D), 15, (int)(3*j/5.0D));
		
		GradientPaint paint3 = new GradientPaint(0,(int)(3*j/5.0D),Color.YELLOW,0,(int)(4*j/5.0D),Color.WHITE);
		GradientPaint paint31 = new GradientPaint(0,(int)(3*j/5.0D),Color.BLUE,0,(int)(4*j/5.0D),Color.BLACK);
		graphics2d.setPaint(Option.gt ? paint3 :paint31);
		graphics2d.fillRect(0, (int)(3*j/5.0D), 15, (int)(4*j/5.0D));
		
		GradientPaint paint4 = new GradientPaint(0,(int)(4*j/5.0D),Color.WHITE,0,(int)(5*j/5.0D),Color.BLACK);
		GradientPaint paint41 = new GradientPaint(0,(int)(4*j/5.0D),Color.BLACK,0,(int)(5*j/5.0D),Color.WHITE);
		graphics2d.setPaint(Option.gt ? paint4 :paint41);
		graphics2d.fillRect(0, (int)(4*j/5.0D), 15, (int)(5*j/5.0D));
		
	}
	*/
	void redraw2D(Graphics g, int i, int j, int k, int l)
	{
		Graphics2D graphics2d = (Graphics2D)g;
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

		graphics2d.setPaint(Color.BLACK);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.DARK_GRAY)) : ((java.awt.Paint) (Color.LIGHT_GRAY)));
		graphics2d.fillRect(0, 0, i, j);

		GradientPaint paint0 = new GradientPaint(0,0,new Color(0xff0000),0,(int)(j/3.0D),new Color(0xffff00));
		GradientPaint paint01 = new GradientPaint(0,0,new Color(0x00ffff),0,(int)(j/3.0D),new Color(0x0000ff));
		graphics2d.setPaint(Option.gt ? paint0 :paint01);
		graphics2d.fillRect(0, 0, 15, (int)(j/3.0D));
		
		GradientPaint paint1 = new GradientPaint(0,(int)(j/3.0D),new Color(0xffff00),0,(int)(2*j/3.0D),new Color(0xffffff));
		GradientPaint paint11 = new GradientPaint(0,(int)(j/3.0D),new Color(0x0000ff),0,(int)(2*j/3.0D),new Color(0x0));
		graphics2d.setPaint(Option.gt ? paint1 :paint11);
		graphics2d.fillRect(0, (int)(j/3.0D), 15, (int)(2*j/3.0D));
		
		GradientPaint paint2 = new GradientPaint(0,(int)(2*j/3.0D),new Color(0xffffff),0,(int)(3*j/3.0D),new Color(0x0));
		GradientPaint paint21 = new GradientPaint(0,(int)(2*j/3.0D),new Color(0x0),0,(int)(3*j/3.0D),new Color(0xffffff));
		graphics2d.setPaint(Option.gt ? paint2 :paint21);
		graphics2d.fillRect(0, (int)(2*j/3.0D), 15, (int)(3*j/3.0D));
		
	}
		
	@Override
	public int print(Graphics g, PageFormat pageformat, int i)
			throws PrinterException {
		if (i > 0)
			return 1;
		int j = (int)pageformat.getImageableWidth();
		int k = (int)pageformat.getImageableHeight();
		int l = (int)pageformat.getImageableX();
		int i1 = (int)pageformat.getImageableY();
		k = (int)((double)k * 0.84999999999999998D);
		j -= 2 * l;
		k -= 2 * i1;
		if (j > k)
			k = j;
		else
			j = k;
		draw(g, j, k, l, i1);
		if (negativePrint)
		{
			g.setColor(Option.gt ? Color.black : Color.white);
			g.setXORMode(Option.gt ? Color.white : Color.black);
			g.fillRect(l, i1, j, k);
		}
		g.dispose();
		return 0;
	}

}
