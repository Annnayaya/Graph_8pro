package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
class Graph
{

	Graph()
	{
	}

	public static void drawLineGraph(double d, short aword0[], int i, int j, Graphics2D graphics2d, double d1, 
			double d2, double d3, float f, float f1, Color color)
	{
		float f2 = (float)d1;
		float f3 = (float)d;
		graphics2d.setPaint(Color.gray);
		graphics2d.draw(new java.awt.geom.Line2D.Double(d2, f2, d3, f2));
		graphics2d.setPaint(color);
		GeneralPath generalpath = new GeneralPath();
		generalpath.moveTo((float)d2, (float)(-aword0[i]) * f3 * f + f2);
		int k1 = (int)((double)j / ((d3 - d2) * (double)f1));
		if (k1 == 0)
			k1 = 1;
		float f4 = (float)(d3 - d2) / (float)j;
		boolean flag1 = false;
		int j1 = (i + j) - k1;
		if (k1 > 1)
		{
			for (int k = i + 1; k <= j1; k += k1)
			{
				int j2 = k;
				int l1 = aword0[k];
				int k2 = k - i;
				int i2 = aword0[k];
				j2 = k - i;
				for (int i1 = 0; i1 < k1; i1++)
				{
					short word0 = aword0[k + i1];
					if (l1 < word0)
					{
						l1 = word0;
						flag1 = true;
						k2 = (k + i1) - i;
					}
					if (i2 > word0)
					{
						i2 = word0;
						flag1 = false;
						j2 = (k + i1) - i;
					}
				}

				if (f3 * f < 0.0F)
				{
					l1 = -l1;
					i2 = -i2;
				}
				float f5 = (float)(-l1) * f3 * f + f2;
				float f6 = (float)(-i2) * f3 * f + f2;
				if (flag1)
				{
					generalpath.lineTo((float)(d2 + (double)((float)j2 * f4)), f6);
					generalpath.lineTo((float)(d2 + (double)((float)k2 * f4)), f5);
				} else
				{
					generalpath.lineTo((float)(d2 + (double)((float)k2 * f4)), f5);
					generalpath.lineTo((float)(d2 + (double)((float)j2 * f4)), f6);
				}
			}

		} else
		{
			for (int l = i + 1; l <= j1; l++)
				generalpath.lineTo((float)(d2 + (double)((float)(l - i) * f4)), (float)(-aword0[l]) * f3 * f + f2);

		}
		graphics2d.draw(generalpath);
	}

	public static void drawBoolGraph(boolean aflag[], int i, int j, Graphics2D graphics2d, float f, float f1, double d, 
			double d1, float f2, Color color)
	{
		if (f > f1)
		{
			float f4 = f;
			f = f1;
			f1 = f4;
		}
		if (d > d1)
		{
			double d2 = d;
			d = d1;
			d1 = d2;
		}
		graphics2d.setPaint(Color.lightGray);
		graphics2d.draw(new java.awt.geom.Line2D.Double(d, (f + f1) / 2.0F, d1, (f + f1) / 2.0F));
		graphics2d.setPaint(color);
		GeneralPath generalpath = new GeneralPath();
		if (aflag[i])
			generalpath.moveTo((float)d, f);
		else
			generalpath.moveTo((float)d, f1);
		int i1 = (int)((double)j / ((d1 - d) * (double)f2));
		if (i1 == 0)
			i1 = 1;
		float f5 = (float)(d1 - d) / (float)j;
		for (int k = i + 1; k <= (i + j) - i1; k += i1)
		{
			boolean flag1 = false;
			boolean flag3 = false;
			int l;
			for (l = 0; l < i1; l++)
				if (aflag[k + l])
					flag1 = true;
				else
					flag3 = true;

			if (flag1)
				generalpath.lineTo((float)d + (float)((k + l) - i) * f5, f);
			if (flag3)
				generalpath.lineTo((float)d + (float)((k + l) - i) * f5, f1);
		}

		graphics2d.draw(generalpath);
	}

	public static void drawHGuideLine(double d, Graphics2D graphics2d, double d1, double d2, double d3, float f, Color color)
	{
		graphics2d.setPaint(color);
		graphics2d.draw(new java.awt.geom.Line2D.Float((float)d2, (float)(d * (double)f + d1), (float)d3, (float)(d * (double)f + d1)));
	}

	public static void drawVGuideLine(int i, int j, float f, Graphics2D graphics2d, float f1, float f2, float f3, float f4, 
			float f5, Color color)
	{
		if (f1 > f2)
		{
			float f7 = f1;
			f1 = f2;
			f2 = f7;
		}
		float f8 = (f5 * (f2 - f1)) / (float)j;
		graphics2d.setPaint(color);
		float f6 = (f - (float)i) * f8 + f1;
		graphics2d.draw(new java.awt.geom.Line2D.Float(f6, f3, f6, f4));
	}
}
