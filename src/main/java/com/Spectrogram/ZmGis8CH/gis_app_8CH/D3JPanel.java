package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;

class D3JPanel
{

	int bitmap[];
	MemoryImageSource source;
	Image image;
	Boolean printBlackWhite;
	double camera_th;
	double camera_pi;
	double camera_r;
	double scale;
	double gz;
	int screen_maxx;
	int screen_maxy;
	int map_maxx;
	int map_maxy;
	int gx;
	int gy;
	double cos_th;
	double sin_th;
	double cos_pi;
	double sin_pi;
	double cam_rr;
	int drawMethod;
	int mapc[][];
	int scrx[][];
	int scry[][];
	/*
	static int zColorN = 4;
	static int zColorMap1[] = {
		0, 0xffffff, 0xffff00, 0xff0000
	};
	static int zColorMap2[] = {
		0xffffff, 0, 255, 0xff0000
	};
	 */
	
	static int zColorN = 4;
	static int zColorMap1[] = {
		0, 0xffffff, 0xffff00, 0xff0000
	};
	static int zColorMap2[] = {
		0xffffff, 0, 0x0000ff, 0x00ffff
	};
	/*
	static int zColorN = 3;
	static int zColorMap1[] = {
		0xffffff, 0xffff00, 0xff0000
	};
	static int zColorMap2[] = {
		0, 0x0000ff, 0x00ffff
	};
	*/
	/*
	static int zColorN = 6;
	static int zColorMap1[] = {
		0, 0xffffff, 0xffff00,0x00ff00, 0x0000ff, 0xff0000
	};//黑，白，黄，绿，蓝，红
	static int zColorMap2[] = {
		0xffffff, 0, 0x0000ff, 0xff00ff, 0xffff00, 0x00ffff
	};//白，黑，蓝，洋红，黄，绿
	*/
	D3JPanel()
	{
		printBlackWhite = Boolean.valueOf(false);
		screen_maxx = 0;
		screen_maxy = 0;
	}

	public int RGB(int i, int j, int k)
	{
		return i << 16 | j << 8 | k;
	}

	public int GetRValue(int i)
	{
		return i >> 16 & 0xff;
	}

	public int GetGValue(int i)
	{
		return i >> 8 & 0xff;
	}

	public int GetBValue(int i)
	{
		return i & 0xff;
	}

	public int max(int i, int j)
	{
		if (i < j)
			return j;
		else
			return i;
	}

	public int min(int i, int j)
	{
		if (i < j)
			return i;
		else
			return j;
	}

	void setBuffSize(int i, int j)
	{
		map_maxx = i;
		map_maxy = j;
		mapc = new int[map_maxx][map_maxy];
		scrx = new int[map_maxx][map_maxy];
		scry = new int[map_maxx][map_maxy];
	}

	void setScreenSize(int i, int j, Boolean boolean1, double d)
	{
		printBlackWhite = boolean1;
		if (screen_maxx == i && screen_maxy == j)
			return;
		screen_maxx = i;
		screen_maxy = j;
		bitmap = new int[screen_maxx * screen_maxy];
		source = new MemoryImageSource(screen_maxx, screen_maxy, bitmap, 0, screen_maxx);
		source.setAnimated(true);
		image = (new Container()).createImage(source);
		if (screen_maxx < screen_maxy)
			scale = (double)screen_maxx * d;
		else
			scale = (double)screen_maxy * d;
	}

	public void initCamera(double d, double d1, double d2)
	{
		for (; d < 0.0D; d += 6.2831853071795862D);
		for (; 6.2831853071795862D <= d; d -= 6.2831853071795862D);
		drawMethod = (int)((d * 4D) / 3.1415926535897931D);
		cos_th = Math.cos(-d);
		sin_th = Math.sin(-d);
		cos_pi = Math.cos(-d1);
		sin_pi = Math.sin(-d1);
		cam_rr = d2;
	}
//用于超声波
	int zColor_Ultral(int i)
	{
		if (i < 0)
			i = 0;
		if (4982 < i)
			i = 4982;
		if (printBlackWhite.booleanValue())
			return RGB(i / 20, i / 20, i / 20);
		int i2 = 4982 / (zColorN - 1);
		int j = i / i2;
		int k = i % i2;
		if (j == zColorN - 1)
		{
			return Option.gt ? zColorMap1[j] : zColorMap2[j];
		} else
		{
			int l = Option.gt ? zColorMap1[j] : zColorMap2[j];
			int i1 = Option.gt ? zColorMap1[j + 1] : zColorMap2[j + 1];
			int j1 = (GetRValue(l) * (i2 - k) + GetRValue(i1) * k) / i2;
			int k1 = (GetGValue(l) * (i2 - k) + GetGValue(i1) * k) / i2;
			int l1 = (GetBValue(l) * (i2 - k) + GetBValue(i1) * k) / i2;
			return RGB(j1, k1, l1);
		}
	}
	
	int zColor(int i)
	{
		if (i < 0)
			i = 0;
		if (4000 < i)
			i = 4000;
		
		if (printBlackWhite.booleanValue())
			return RGB(i / 16, i / 16, i / 16);
		int i2 = 4000 / (zColorN - 1);
		int j = i / i2;
		int k = i % i2;
		if (j == zColorN - 1)
		{
			return Option.gt ? zColorMap1[j] : zColorMap2[j];
		} else
		{
			int l = Option.gt ? zColorMap1[j] : zColorMap2[j];
			int i1 = Option.gt ? zColorMap1[j + 1] : zColorMap2[j + 1];
			int j1 = (GetRValue(l) * (i2 - k) + GetRValue(i1) * k) / i2;
			int k1 = (GetGValue(l) * (i2 - k) + GetGValue(i1) * k) / i2;
			int l1 = (GetBValue(l) * (i2 - k) + GetBValue(i1) * k) / i2;
			return RGB(j1, k1, l1);
		}
	}


	void rotConvScreen(double d, double d1, double d2)
	{
		d = -d;
		double d3 = cos_th * d - sin_th * d1;
		double d5 = sin_th * d + cos_th * d1;
		double d6 = -cos_pi * d2 - sin_pi * d3;
		double d4 = -sin_pi * d2 + cos_pi * d3;
		d = cam_rr - d4;
		d1 = d5 / d;
		d2 = d6 / d;
		gx = (int)(d1 * scale + (double)(screen_maxx / 2));
		gy = (int)(d2 * scale + (double)(screen_maxy / 2));
		gz = d4;
	}

	void rotConvScreen(int i, int j, double d, double d1, double d2, double d3)  //绘制3dprps波形图//mana
	{
		
		 rotConvScreen(d, d1, d2); scrx[i][j] = gx; scry[i][j] = gy; mapc[i][j] =
		 zColor((int)(d3 * 4095D));
		 
	}

	void drawOverPixel(int i, int j, int k)  //绘制坐标图
	{
		if (0 <= i && i < screen_maxx && 0 <= j && j < screen_maxy)
		{
			int l = j * screen_maxx + i;
			int i1 = bitmap[l];
			int j1 = Option.gt ? max(i1 >> 16 & 0xff, k >> 16 & 0xff) : min(i1 >> 16 & 0xff, k >> 16 & 0xff);
			int k1 = Option.gt ? max(i1 >> 8 & 0xff, k >> 8 & 0xff) : min(i1 >> 8 & 0xff, k >> 8 & 0xff);
			int l1 = Option.gt ? max(i1 & 0xff, k & 0xff) : min(i1 & 0xff, k & 0xff);
			bitmap[l] = 0xff000000 | j1 << 16 | k1 << 8 | l1;
		}
	}

	void drawPixel(int i, int j, int k)
	{
		if (0 <= i && i < screen_maxx && 0 <= j && j < screen_maxy)
			bitmap[j * screen_maxx + i] = 0xff000000 | k;
	}

	void drawPixel(int i, int j, int k, int l, int i1)
	{
		if (0 <= i && i < screen_maxx && 0 <= j && j < screen_maxy)
			bitmap[j * screen_maxx + i] = 0xff000000 | k << 16 | l << 8 | i1;
	}

	void drawLine(int i, int j, int k, int l, int i1)
	{
		int j1 = k - i;
		int k1 = l - j;
		int j3 = GetRValue(i1);
		int k3 = GetGValue(i1);
		int l3 = GetBValue(i1);
		if (Math.abs(j1) < Math.abs(k1))
		{
			if (k1 < 0)
			{
				int l1 = i;
				i = k;
				k = l1;
				l1 = j;
				j = l;
				l = l1;
			}
			double d = i;
			double d2 = (double)j1 / (double)k1;
			for (int i2 = j; i2 <= l; i2++)
			{
				if (0.0D <= d && d < (double)screen_maxx)
				{
					int l2 = (int)d;
					double d4 = (1.0D - d) + (double)l2;
					double d6 = Option.gt ? 0.0D : 255D * (1.0D - d4);
					int i4 = (int)((double)j3 * d4 + d6);
					int k4 = (int)((double)k3 * d4 + d6);
					int i5 = (int)((double)l3 * d4 + d6);
					drawOverPixel(l2, i2, RGB(i4, k4, i5));
					d4 = d - (double)l2;
					d6 = Option.gt ? 0.0D : 255D * (1.0D - d4);
					i4 = (int)((double)j3 * d4 + d6);
					k4 = (int)((double)k3 * d4 + d6);
					i5 = (int)((double)l3 * d4 + d6);
					drawOverPixel(l2 + 1, i2, RGB(i4, k4, i5));
				}
				d += d2;
			}

		} else
		{
			if (j1 < 0)
			{
				int j2 = i;
				i = k;
				k = j2;
				j2 = j;
				j = l;
				l = j2;
			}
			double d1 = j;
			double d3 = (double)k1 / (double)j1;
			for (int k2 = i; k2 <= k; k2++)
			{
				if (0.0D <= d1 && d1 < (double)screen_maxy)
				{
					int i3 = (int)d1;
					double d5 = (1.0D - d1) + (double)i3;
					double d7 = Option.gt ? 0.0D : 255D * (1.0D - d5);
					int j4 = (int)((double)j3 * d5 + d7);
					int l4 = (int)((double)k3 * d5 + d7);
					int j5 = (int)((double)l3 * d5 + d7);
					drawOverPixel(k2, i3, RGB(j4, l4, j5));
					d5 = d1 - (double)i3;
					d7 = Option.gt ? 0.0D : 255D * (1.0D - d5);
					j4 = (int)((double)j3 * d5 + d7);
					l4 = (int)((double)k3 * d5 + d7);
					j5 = (int)((double)l3 * d5 + d7);
					drawOverPixel(k2, i3 + 1, RGB(j4, l4, j5));
				}
				d1 += d3;
			}

		}
	}

	void drawD3Line(double d, double d1, double d2, double d3, double d4, double d5, int i)
	{
		rotConvScreen(d, d1, d2);
		int j = gx;
		int l = gy;
		rotConvScreen(d3, d4, d5);
		int k = gx;
		int i1 = gy;
		drawLine(j, l, k, i1, i);
	}

	public void drawTriangle(int i, int j, int k, int l, int i1, int j1, int k1, 
			int l1, int i2)
	{
		if (i1 < j)
		{
			int j2 = i;
			i = l;
			l = j2;
			j2 = j;
			j = i1;
			i1 = j2;
			j2 = k;
			k = j1;
			j1 = j2;
		}
		if (l1 < i1)
		{
			int k2 = l;
			l = k1;
			k1 = k2;
			k2 = i1;
			i1 = l1;
			l1 = k2;
			k2 = j1;
			j1 = i2;
			i2 = k2;
		}
		if (i1 < j)
		{
			int l2 = i;
			i = l;
			l = l2;
			l2 = j;
			j = i1;
			i1 = l2;
			l2 = k;
			k = j1;
			j1 = l2;
		}
		int i4 = i1 - j;
		double d;
		double d9;
		double d10;
		double d11;
		if (i4 != 0)
		{
			double d4 = 1.0D / (double)i4;
			d = (double)(l - i) * d4;
			d9 = (double)((j1 >> 16 & 0xff) - (k >> 16 & 0xff)) * d4;
			d10 = (double)((j1 >> 8 & 0xff) - (k >> 8 & 0xff)) * d4;
			d11 = (double)((j1 & 0xff) - (k & 0xff)) * d4;
		} else
		{
			d = d9 = d10 = d11 = 0.0D;
		}
		i4 = l1 - j;
		double d1;
		double d12;
		double d13;
		double d14;
		if (i4 != 0)
		{
			double d5 = 1.0D / (double)i4;
			d1 = (double)(k1 - i) * d5;
			d12 = (double)((i2 >> 16 & 0xff) - (k >> 16 & 0xff)) * d5;
			d13 = (double)((i2 >> 8 & 0xff) - (k >> 8 & 0xff)) * d5;
			d14 = (double)((i2 & 0xff) - (k & 0xff)) * d5;
		} else
		{
			d1 = d12 = d13 = d14 = 0.0D;
		}
		double d3;
		double d2 = d3 = i;
		double d21;
		double d18 = d21 = GetRValue(k);
		double d22;
		double d19 = d22 = GetGValue(k);
		double d23;
		double d20 = d23 = GetBValue(k);
		for (int j3 = j; j3 <= l1; j3++)
		{
			if (j3 == i1)
			{
				int j4 = l1 - i1;
				if (j4 != 0)
				{
					double d6 = 1.0D / (double)j4;
					d = (double)(k1 - l) * d6;
					d9 = (double)(GetRValue(i2) - GetRValue(j1)) * d6;
					d10 = (double)(GetGValue(i2) - GetGValue(j1)) * d6;
					d11 = (double)(GetBValue(i2) - GetBValue(j1)) * d6;
				} else
				{
					d = d9 = d10 = d11 = 0.0D;
				}
				d2 = l;
				d18 = GetRValue(j1);
				d19 = GetGValue(j1);
				d20 = GetBValue(j1);
			}
			int k3;
			int l3;
			double d15;
			double d16;
			double d17;
			double d24;
			double d25;
			double d26;
			if (d2 < d3)
			{
				k3 = (int)(d2 + 0.5D);
				l3 = (int)(d3 + 0.5D);
				double d7 = 1.0D / (double)(l3 - k3);
				d15 = (d21 - d18) * d7;
				d16 = (d22 - d19) * d7;
				d17 = (d23 - d20) * d7;
				d24 = d18;
				d25 = d19;
				d26 = d20;
			} else
			{
				k3 = (int)(d3 + 0.5D);
				l3 = (int)(d2 + 0.5D);
				double d8 = 1.0D / (double)(l3 - k3);
				d15 = (d18 - d21) * d8;
				d16 = (d19 - d22) * d8;
				d17 = (d20 - d23) * d8;
				d24 = d21;
				d25 = d22;
				d26 = d23;
			}
			if (0 <= j3 && j3 < screen_maxy)
			{
				if (k3 < 0)
				{
					k3 = 0;
					d24 += d15 * (double)(-k3);
					d25 += d16 * (double)(-k3);
					d26 += d17 * (double)(-k3);
				}
				if (screen_maxx <= l3)
					l3 = screen_maxx - 1;
				int k4 = j3 * screen_maxx + k3;
				for (int i3 = k3; i3 <= l3; i3++)
				{
					bitmap[k4++] = 0xff000000 | (int)d24 << 16 | (int)d25 << 8 | (int)d26;
					d24 += d15;
					d25 += d16;
					d26 += d17;
				}

			}
			d2 += d;
			d3 += d1;
			d18 += d9;
			d19 += d10;
			d20 += d11;
			d21 += d12;
			d22 += d13;
			d23 += d14;
		}

	}

	void drawRectangle(int i, int j, int k, int l, int i1)
	{
		for (int j1 = i; j1 <= k; j1++)
		{
			drawPixel(j1, j, i1);
			drawPixel(j1, l, i1);
		}

		for (int k1 = j; k1 < l; k1++)
		{
			drawPixel(i, k1, i1);
			drawPixel(k, k1, i1);
		}

	}

	void clearD3Screen()
	{
		Arrays.fill(bitmap, Option.gt ? 0xff000000 : -1);
		byte byte0 = 3;
		for (int i = 0; i < byte0; i++)
		{
			int j = 238 - ((i + 1) * 238) / (byte0 + 1);
			j = RGB(j, j, j);
			drawRectangle(i, i, screen_maxx - 1 - i, screen_maxy - 1 - i, j);
		}

	}

	void drawSquareSpace(int i, int j)
	{
		drawTriangle(scrx[i][j], scry[i][j], mapc[i][j], scrx[i + 1][j], scry[i + 1][j], mapc[i + 1][j], scrx[i][j + 1], scry[i][j + 1], mapc[i][j + 1]);
		drawTriangle(scrx[i + 1][j], scry[i + 1][j], mapc[i + 1][j], scrx[i + 1][j + 1], scry[i + 1][j + 1], mapc[i + 1][j + 1], scrx[i][j + 1], scry[i][j + 1], mapc[i][j + 1]);
	}

	void drawD3Screen()
	{
label0:
		switch (drawMethod)
		{
		case 0: // '\0'
			for (int i = map_maxx - 2; 0 <= i; i--)
			{
				for (int i2 = 0; i2 < map_maxy - 1; i2++)
					drawSquareSpace(i, i2);

			}

			break;

		case 1: // '\001'
			int j2 = 0;
			do
			{
				if (j2 >= map_maxy - 1)
					break label0;
				for (int j = map_maxx - 2; 0 <= j; j--)
					drawSquareSpace(j, j2);

				j2++;
			} while (true);

		case 2: // '\002'
			int k2 = 0;
			do
			{
				if (k2 >= map_maxy - 1)
					break label0;
				for (int k = 0; k < map_maxx - 1; k++)
					drawSquareSpace(k, k2);

				k2++;
			} while (true);

		case 3: // '\003'
			int l = 0;
			do
			{
				if (l >= map_maxx - 1)
					break label0;
				for (int l2 = 0; l2 < map_maxy - 1; l2++)
					drawSquareSpace(l, l2);

				l++;
			} while (true);

		case 4: // '\004'
			int i1 = 0;
			do
			{
				if (i1 >= map_maxx - 1)
					break label0;
				for (int i3 = map_maxy - 2; 0 <= i3; i3--)
					drawSquareSpace(i1, i3);

				i1++;
			} while (true);

		case 5: // '\005'
			int j3 = map_maxy - 2;
			do
			{
				if (0 > j3)
					break label0;
				for (int j1 = 0; j1 < map_maxx - 1; j1++)
					drawSquareSpace(j1, j3);

				j3--;
			} while (true);

		case 6: // '\006'
			int k3 = map_maxy - 2;
			do
			{
				if (0 > k3)
					break label0;
				for (int k1 = map_maxx - 2; 0 <= k1; k1--)
					drawSquareSpace(k1, k3);

				k3--;
			} while (true);

		default:
			int l1 = map_maxx - 2;
			do
			{
				if (0 > l1)
					break label0;
				for (int l3 = map_maxy - 2; 0 <= l3; l3--)
					drawSquareSpace(l1, l3);

				l1--;
			} while (true);
		}
	}

	public void draw(Graphics g, int i, int j)
	{
		source.newPixels(0, 0, screen_maxx, screen_maxy);
		g.drawImage(image, i, j, null);
	}

}
