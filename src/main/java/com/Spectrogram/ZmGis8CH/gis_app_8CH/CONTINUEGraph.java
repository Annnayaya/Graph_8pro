package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Random;

import javax.swing.JComponent;
//无用类
public class CONTINUEGraph extends JComponent implements Printable {

	private static final long serialVersionUID = 1L;
	int conData[];
	int cycleSum32[];
	int cycleNum;
	int phaseNum;
	D3JPanel d3p;
	Boolean printBlackWhite;
	Graphics2D vg;
	VolatileImage img;
	double oldrDeg;
	double oldtDeg;
	double rdeg;
	double tdeg;
	int oldw;
	int oldh;
	int ds[];
	BufferedImage bi;
	public static boolean negativePrint = false;
	public boolean drawing;
	public boolean congraphicrefresh;
	double avrdBm;
	double amxdBm;
	double f1dBm;
	double f2dBm;
	Random rad;

	public CONTINUEGraph(int i,int j,int ai[],int k)
	{
		//congraph = ai;
		d3p = new D3JPanel();
		cycleSum32 = new int[80];
		phaseNum = i;
		cycleNum = j;
		printBlackWhite = Boolean.valueOf(false);
		drawing = false;
		congraphicrefresh = true;
		oldrDeg = 0.0D;
		oldtDeg = 0.0D;
		rdeg = 2.3561944901923448D;
		tdeg = 0.53407075111026492D;
		oldw = 0;
		oldh = 0;
		avrdBm = GisSignal.calMin_dBm;
		amxdBm = GisSignal.calMin_dBm;
		f2dBm = GisSignal.calf2_dBm;
		rad = new Random();
		if(ai == null)
		{
			conData = new int[i*j];
		}
		else
			conData = ai;
		setAngle(this.getWidth(),this.getHeight(),0,0);
	}
	//复写
	public void paint(Graphics g)
	{
		drawing = true;
		int i = getWidth();
		int j = getHeight();
		if (congraphicrefresh)
		{
			img = createVolatileImage(i, j);
			makeDrawable();
			redraw2D(img.getGraphics());
		}
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
		if (getWidth() != oldw || getHeight() != oldh || img == null)
		{
			setAngle(getWidth(), getHeight(), 0, 0);
			img = createVolatileImage(getWidth(), getHeight());
			if (vg != null)
			{
				vg.dispose();
				vg.finalize();
			}
			vg = img.createGraphics();
			oldw = getWidth();
			oldh = getHeight();
			oldrDeg = rdeg;
			oldtDeg = tdeg;
		}
		if (oldrDeg != rdeg || oldtDeg != tdeg)
		{
			setAngle(getWidth(), getHeight(), 0, 0);
			oldrDeg = rdeg;
			oldtDeg = tdeg;
		}
	}

	
	private void drawdBm(Graphics2D graphics2d, int i, int j, int k, int l, String s)
	{
		
		String s1= (new StringBuilder()).append(s).append(String.format("%ddB", new Object[] {
			Integer.valueOf(i)
		})).toString();
		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int i1 = fontmetrics.stringWidth(s1);
		graphics2d.drawString(s1, l != 0 ? j - i1 : j, k);
		//graphics2d.drawLine(j/8, (k/9)*7, (80/j)*(-i), (k/9)*7);
		int num = rad.nextInt(100)+1;
		if(num > j/8)
		{
		//graphics2d.drawLine(j/8+10, (k/9)*7, num, (k/9)*7);
		}
	}
	
	private void drawUnit(Graphics2D graphics2d, int i, int j, int k, int l, String s)
	{
		switch (Option.unitType)
		{
		case 0: // '\0'
			drawdBm(graphics2d, i, j, k, l, s);
			break;
			/*
		case 1: // '\001'
			drawmW(graphics2d, i, j, k, l, s);
			break;

		case 2: // '\002'
			drawmV(graphics2d, i, j, k, l, s);
			break;

		case 3: // '\003'
			drawpC(graphics2d, i, j, k, l, s);
			break;
			*/
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
	void redraw2D(Graphics g, int i, int j, int k, int l)
	{
		Graphics2D graphics2d = (Graphics2D)g;
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		
		
		/*
		int i2 = -1;
		
		for (int i1 = 0; i1 < cycleNum; i1++)
		{
			for (int l1 = 0; l1 < phaseNum; l1++)
			{
				int j2 = conData[++i2];
				if (4095 < j2)
					j2 = 4095;
				if (j2 < 0)
					j2 = 0;
				ds[i2] = d3p.zColor(j2);
			}

		}
		*/

		//if (bi == null)
			//bi = new BufferedImage(64, 64, 1);
		//bi.setRGB(0, 0, 64, 64, cons, 0, 64);
		//graphics2d.setPaint(Color.black);
		//graphics2d.drawImage(bi, k, l, i, j, Option.gt ? Color.black : Color.white, null);
		
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.DARK_GRAY)) : ((java.awt.Paint) (Color.LIGHT_GRAY)));
		
		graphics2d.drawLine(i/8, j/9, i/8, (j/9)*8);
		graphics2d.drawLine(i/8, (j/9)*8, (i/8)*7, (j/9)*8);
		graphics2d.setStroke(new BasicStroke((j/9)*7/10.0f));
		//graphics2d.drawLine(i/8, (j/9)*7, (i/8)*7, (j/9)*7);
		//graphics2d.drawLine(i/8, (j/9)*7, (int)((80/i)*(-avrdBm)), (j/9)*7);
		//graphics2d.setPaint(d3p.zColor((int)avrdBm));
		
		int avr = (int)(((7*i-40)/480)*(avrdBm/0.9 + 80/0.9));//有效值=平均值/0.9
		int max = (int)(((7*i-40)/480)*(amxdBm + 80));//峰值
		int f1 = (int)(((7*i-40)/480)*(avrdBm + 80));//频率成份1=平均值
		int f2 = (int)(((7*i-40)/480)*(f2dBm + 80));//频率成份2
		
		graphics2d.setPaint(Color.blue);
		graphics2d.drawLine(i/8+10, (j/9)*2, avr-10, (j/9)*2);
		drawUnit(graphics2d, (int)(avrdBm/0.9 + 80.0D/0.9 - 0.5D), (k + (i/8)*5) - 4, (l + (j/18)*3) - 5, 1, ChooseLang.M[40]);
		graphics2d.setPaint(Color.cyan);
		graphics2d.drawLine(i/8+10, (j/9)*4, max-10, (j/9)*4);
		drawUnit(graphics2d, (int)(amxdBm + 80.0D - 0.5D), (k + (i/8)*5) - 4, (l + (j/18)*7) - 5, 1, ChooseLang.M[41]);
		graphics2d.setPaint(Color.gray);
		graphics2d.drawLine(i/8+10, (j/9)*6, f1-10, (j/9)*6);
		drawUnit(graphics2d, (int)(avrdBm + 80.0D  - 0.5D), (k + (i/8)*5) - 4, (l + (j/18)*11) - 5, 1, ChooseLang.M[42]);
		graphics2d.setPaint(Color.green);
		graphics2d.drawLine(i/8+10, (j/9)*8, f2-10, (j/9)*8);
		drawUnit(graphics2d, (int)(f2dBm + 80.0D - 0.5D), (k + (i/8)*5) - 4, (l + (j/18)*15) - 5, 1, ChooseLang.M[43]);
		
		
	}
	public void setAngle(int i,int j,int k,int l)
	{
		if(0 < i && 0 < j && 0 < cycleNum && 0 < phaseNum)			
		{
			d3p.setScreenSize(i, j, printBlackWhite, 4D);
			d3p.setBuffSize(phaseNum + 2, cycleNum + 2);
		}
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
