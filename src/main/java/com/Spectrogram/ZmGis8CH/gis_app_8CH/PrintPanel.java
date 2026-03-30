package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;

class PrintPanel
	implements Printable
{

	ArrayList<PRPDGraph> prpd = new ArrayList<PRPDGraph>();
	ArrayList<GisGraph> prps = new ArrayList<GisGraph>();
	private int type;
	private int ch;
	public static boolean negativePrint = true;
	
	public void add(PRPDGraph prpdgraph)
	{
		prpd.add(prpdgraph);
	}

	public void add(GisGraph gisgraph)
	{
		prps.add(gisgraph);
	}

	public void setPrint(int i, int j)
	{
		if (i > 1)
			i = 1;
		if (j > 4)
			j = 4;
		if (i < 0)
			i = 0;
		if (j < 0)
			j = 0;
		type = i;
		ch = j;
		System.out.println((new StringBuilder()).append(i).append(" / ").append(j).toString());
	}

	public int print(Graphics g, PageFormat pageformat, int i)
	{
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
			g.setColor(Color.black);
			g.setXORMode(Color.white);
			g.fillRect(l, i1, j, k);
		}
		g.dispose();
		return 0;
	}

	public void draw(Graphics g, int i, int j, int k, int l)
	{
		g.setColor(Color.black);
		g.fillRect(k, l, i, j);
		if (ch == 8)
		{
			int k1 = i / 2 - k / 2;
			int l1 = j / 2 - l / 2;
			for (int i2 = 0; i2 < 8; i2++)
			{
				int i1 = k / 2;
				int j1 = l / 2;
				BufferedImage bufferedimage1 = new BufferedImage(k1, l1, 1);
				Graphics g2 = bufferedimage1.getGraphics();
				if (type == 0)
					((PRPDGraph)prpd.get(i2)).draw(g2, k1, l1, 0, 0);
				else
					((GisGraph)prps.get(i2)).draw(g2, k1, l1, 0, 0);
				i1 = (i2 & 1) * (k1 + k / 2) + k;
				j1 = (i2 >> 1) * (l1 + l / 2) + l;
				g.drawImage(bufferedimage1, i1, j1, null);
			}

		} else
		{
			BufferedImage bufferedimage = new BufferedImage(i, j, 1);
			Graphics g1 = bufferedimage.getGraphics();
			if (type == 0)
				((PRPDGraph)prpd.get(ch)).draw(g1, i, j, 0, 0);
			else
				((GisGraph)prps.get(ch)).draw(g1, i, j, 0, 0);
			g.drawImage(bufferedimage, k, l, null);
		}
	}

	public void savePng(File file)
	{
		BufferedImage bufferedimage = new BufferedImage(1024, 768, 1);
		draw(bufferedimage.getGraphics(), 1024, 768, 10, 10);
		try
		{
			ImageIO.write(bufferedimage, "PNG", file);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public void exportCSV(File file)
	{
		try
		{
			PrintWriter printwriter = new PrintWriter(new FileOutputStream(file));
			if (ch == 8)
			{
				if (type == 1)
				{
					for (int i = 0; i < 8; i++)
					{
						int ai[] = ((GisGraph)prps.get(i)).pdData;
						int l2 = -1;
						for (int l = 0; l < 80; l++)
						{
							for (int l1 = 0; l1 < 64; l1++)
								printwriter.print((new StringBuilder()).append(ai[++l2]).append(" ").toString());

							printwriter.println();
						}

						printwriter.println();
						printwriter.println();
					}

				} else
				{
					for (int j = 0; j < 8; j++)
					{
						int ai1[][] = ((PRPDGraph)prpd.get(j)).prpd;
						for (int i1 = 0; i1 < 64; i1++)
						{
							for (int i2 = 0; i2 < 64; i2++)
								printwriter.print((new StringBuilder()).append(ai1[i2][i1]).append(" ").toString());

							printwriter.println();
						}

						printwriter.println();
						printwriter.println();
					}

				}
			} else
			{
				int k = ch;
				if (type == 1)
				{
					int ai2[] = ((GisGraph)prps.get(k)).pdData;
					int i3 = -1;
					for (int j1 = 0; j1 < 80; j1++)
					{
						for (int j2 = 0; j2 < 64; j2++)
							printwriter.print((new StringBuilder()).append(ai2[++i3]).append(",").toString());

						printwriter.println();
					}

					printwriter.println();
					printwriter.println();
				} else
				{
					int ai3[][] = ((PRPDGraph)prpd.get(k)).prpd;
					for (int k1 = 0; k1 < 64; k1++)
					{
						for (int k2 = 0; k2 < 64; k2++)
							printwriter.print((new StringBuilder()).append(ai3[k2][k1]).append(",").toString());

						printwriter.println();
					}

					printwriter.println();
					printwriter.println();
				}
			}
			printwriter.flush();
			printwriter.close();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

}
