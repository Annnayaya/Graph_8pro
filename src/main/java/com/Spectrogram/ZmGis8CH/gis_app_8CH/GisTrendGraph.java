package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.image.VolatileImage;

import javax.swing.JComponent;

class GisTrendGraph extends JComponent
{
	private static final long serialVersionUID = 197307030007L;
	class TrendMouseListener implements MouseListener
	{

		public void mouseClicked(MouseEvent mouseevent)
		{
			int i = ((mouseevent.getX() - 10) * 1440) / (getWidth() - 20);
			if (i < 0)
				i = 0;
			cursorStart = cursorEnd = i;
			GisTrendGraph.this.mousePressed = false;
			repaint();
			ge.applySelection(cursorStart, cursorEnd);
		}

		public void mousePressed(MouseEvent mouseevent)
		{
		}

		public void mouseReleased(MouseEvent mouseevent)
		{
			if (GisTrendGraph.this.mousePressed)
				ge.applySelection(cursorStart, cursorEnd);
			GisTrendGraph.this.mousePressed = false;
		}

		public void mouseEntered(MouseEvent mouseevent)
		{
		}

		public void mouseExited(MouseEvent mouseevent)
		{
		}

	}

	class TrendMouseMListener implements MouseMotionListener
	{

		public void mouseDragged(MouseEvent mouseevent)
		{
			int i = ((mouseevent.getX() - 10) * 1440) / (getWidth() - 20);
			if (i < 0)
				i = 0;
			if (!mousePressed)
				cursorStart = i;
			else
				cursorEnd = i;
			mousePressed = true;
			repaint();
		}

		public void mouseMoved(MouseEvent mouseevent)
		{
			mousePressed = false;
		}

	}
	GisExplorer ge;
	int oldw;
	int oldh;
	boolean refresh;
	VolatileImage graphImg;
	VolatileImage finalImg;
	int channelVisible;
	boolean valueVisible[];
	boolean avgMinMaxVis[] = {
		true, false, false
	};
	int cursorStart;
	int cursorEnd;
	Color selColor;
	Color selColor2;
	Color grpColor[];
	boolean mousePressed;
	static float centerValue[] = new float[37];
	static float dispRange[] = new float[37];

	public GisTrendGraph(GisExplorer gisexplorer)
	{
		oldw = 0;
		oldh = 0;
		refresh = true;
		channelVisible = 0;
		valueVisible = new boolean[37];
		cursorStart = 0;
		cursorEnd = 0;
		selColor = new Color(200, 200, 255, 64);
		selColor2 = new Color(160, 160, 224, 128);
		grpColor = new Color[37];
		mousePressed = false;
		ge = gisexplorer;
		addMouseMotionListener(new TrendMouseMListener());
		addMouseListener(new TrendMouseListener());
		valueVisible[0] = true;
		valueVisible[1] = true;
		valueVisible[4] = true;
		centerValue[0] = dispRange[0] = 500F;
		centerValue[1] = dispRange[1] = 1500F;
		centerValue[4] = dispRange[4] = 1500F;
		for (int i = 5; i < 37; i++)
		{
			dispRange[i] = 50F;
			centerValue[i] = 50F;
		}

		grpColor[5] = new Color(64, 0, 240);
		grpColor[6] = new Color(0, 192, 0);
		grpColor[7] = new Color(255, 0, 0);
		grpColor[8] = new Color(200, 0, 160);
		grpColor[12] = Color.lightGray;
		grpColor[0] = Color.orange;
		grpColor[1] = Color.cyan;
		grpColor[4] = Color.pink;
	}

	public void paint(Graphics g)
	{
		int i = getWidth();
		int j = getHeight();
		if (oldh != j || oldw != i)
		{
			graphImg = createVolatileImage(i - 20, j - 20);
			finalImg = createVolatileImage(i - 20, j - 20);
			refresh = true;
			oldw = i;
			oldh = j;
		}
		if (refresh)
		{
			refresh = false;
			redraw(graphImg.getGraphics());
		}
		drawCursor(finalImg.getGraphics());
		g.drawImage(finalImg, 10, 10, this);
		g.dispose();
	}

	public void viewChannel(int i)
	{
		channelVisible = i;
		refresh = true;
		repaint();
	}

	void drawCursor(Graphics g)
	{
		Graphics2D graphics2d = (Graphics2D)g;
		int i = getWidth() - 20;
		int j = getHeight() - 20;
		graphics2d.drawImage(graphImg, 0, 0, this);
		int k;
		int l;
		if (cursorStart < cursorEnd)
		{
			k = cursorStart;
			l = cursorEnd;
		} else
		{
			l = cursorStart;
			k = cursorEnd;
		}
		l = (i * ((l - k) + 1)) / 1440;
		if (l <= 1)
		{
			l = 1;
			graphics2d.setPaint(selColor2);
		} else
		{
			graphics2d.setPaint(selColor);
		}
		graphics2d.fillRect((i * k) / 1440, 0, l, j);
		graphics2d.dispose();
	}

	void setSelected(int i, int j)
	{
		cursorStart = i;
		cursorEnd = j;
		repaint();
	}

	void redraw(Graphics g)      //memo 20230831 redraw趋势图
	{
		Graphics2D graphics2d = (Graphics2D)g;
		graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2d.setStroke(new BasicStroke(1.0F, 1, 1));
		int i = getWidth() - 20;
		int j = getHeight() - 20;
		graphics2d.setPaint(Color.white);
		graphics2d.fillRect(0, 0, i, j);
		graphics2d.setPaint(Color.lightGray);
		graphics2d.drawRect(0, 0, i - 1, j - 1);
		Color color = new Color(224, 224, 224);
		float f = (float)i / 24F;
		for (int k = 0; k < 24; k += 2)
		{
			int k1 = (int)(f * (float)k);
			graphics2d.setPaint(color);
			graphics2d.drawLine(k1, 0, k1, j);
			graphics2d.setPaint(Color.lightGray);
			graphics2d.drawString(String.format("%02d", new Object[] {
				Integer.valueOf(k)
			}), k1 + 2, 12);
		}

		f = (float)j / 100F;
		for (int l = 25; l < 100; l += 25)
		{
			int l1 = (int)(f * (float)l);
			graphics2d.setPaint(color);
			graphics2d.drawLine(0, l1, i, l1);
			graphics2d.setPaint(Color.lightGray);
			graphics2d.drawString((new StringBuilder()).append(String.format("%3d", new Object[] {
				Integer.valueOf(100 - l)
			})).append("%").toString(), 2, l1 + 12);
		}

		f = (float)i / 1440F;
		int j2 = (int)(f + 0.5F);
		if (j2 < 1)
			j2 = 1;
		graphics2d.setPaint(new Color(128, 128, 255));
		for (int i1 = 0; i1 < 1440; i1++)
			if (ge.currentSite.loadedPrpdPos[i1] >= 0)
				graphics2d.fillRect((int)(f * (float)i1), j - 3, j2, 2);

		int k2 = channelVisible;
		for (int j1 = 0; j1 < 37; j1++)
		{
			if (!valueVisible[j1])
				continue;
			for (int i2 = 0; i2 < 3; i2++)
				if (avgMinMaxVis[i2])
				{
					//SiteData  = ge.currentSite;
					drawTrendGraph(graphics2d, k2, j1, j1, i2, grpColor[j1], i, j, SiteData.loadedHistory);      //memo 20230831 绘制趋势图
				}

		}

		if (ge.currentSite != null)
		{
			graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			graphics2d.setPaint(Color.blue);
			graphics2d.setFont(graphics2d.getFont().deriveFont(1));
			graphics2d.drawString((new StringBuilder()).append(ge.currentSite.siteName).append("  (").append(String.format("%tF", new Object[] {
				Long.valueOf(ge.currentSite.loadedDate)
			})).append(")").toString(), 25, 25);
		}
		graphics2d.dispose();
	}

	public static void drawTrendGraph(Graphics2D graphics2d, int i, int j, int k, int l, Color color, int i1, int j1, 
			int ai[][][][])
	{
		boolean flag = false;
		int j2 = ai.length;
		float f2 = (float)i1 / (float)(j2 - 1);
		float f3 = (float)j1 / 2.0F;
		float f4 = centerValue[k];
		float f5 = dispRange[k];
		float f6 = 1.0F;
		f5 = f3 / f5;
		GeneralPath generalpath = new GeneralPath();
		if (i < 8)
		{
			for (int k1 = 0; k1 < j2; k1++)
			{
				float f = (float)ai[k1][i][j][l] * f6;
				if (f >= 0.0F)
				{
					f = (float)j1 - (f - f4) * f5 - f3;
					if (f > (float)j1)
						f = j1;
					if (f < (float)j1 - f3 - f3)
						f = (float)j1 - f3 - f3;
					if (!flag)
						generalpath.moveTo(f2 * (float)k1, f);
					generalpath.lineTo(f2 * (float)k1, f);
					flag = true;
				} else
				{
					flag = false;
				}
			}

		} else
		{
			for (int l1 = 0; l1 < j2; l1++)
			{
				int i2 = 0;
				float f1 = -1F;
				for (; i2 < 8; i2++)
					if (f1 < (float)ai[l1][i2][j][l])
						f1 = ai[l1][i2][j][l];

				f1 *= f6;
				if (f1 >= 0.0F)
				{
					f1 = (float)j1 - (f1 - f4) * f5 - f3;
					if (f1 > (float)j1)
						f1 = j1;
					if (f1 < (float)j1 - f3 - f3)
						f1 = (float)j1 - f3 - f3;
					if (!flag)
						generalpath.moveTo(f2 * (float)l1, f1);
					generalpath.lineTo(f2 * (float)l1, f1);
					flag = true;
				} else
				{
					flag = false;
				}
			}

		}
		graphics2d.setPaint(color);
		graphics2d.draw(generalpath);
	}

}
