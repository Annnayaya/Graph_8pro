package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import javax.swing.JComponent;

class GisGraph extends JComponent implements Printable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6228967003510834338L;
	int mindBm;
	int maxdBm;
	double avrdBm;
	double amxdBm;
	double f2dBm;//频率成分2
	double f1dBm;//频率成分1
	double effdBm;//有效值
	int cycleSum[];
	int cycleMax[];
	int cyclef1Sum[];//频率成分1和
	int cyclef2Sum[];//频率成分2和
	int cycleeffSum[];//有效值
	D3JPanel d3p;
	Boolean printBlackWhite;
	int cycleNum;
	int phaseNum;
	boolean directRefresh;
	double oldrDeg;
	double oldtDeg;
	VolatileImage img;
	Graphics2D vg;
	public boolean drawing;
	public boolean prpsrefresh;
	boolean draw3D;
	int phaseDelay;
	int pdData[];
	float ltdataf[][];
	double rdeg;
	double tdeg;
	int oldw;
	int oldh;
	int maxSize;
	String resultString[] = {"", "", "", "", "", "", "", ""};
	String stringToDraw;
	String stringToDraw2;
	BufferedImage bi;
	BufferedImage aebi;
	int ds[];
	int ae[][];
	int aeds[];
	GisGUI clickgui;
	int columdata[];//绘制柱状图数据
	int flygraphicdata[];//飞行图数据
	double trancoumdata[];//转换数据
	boolean isflygraphic;
	boolean Waveform;//波形图
	boolean isCon;//连续图
	public static boolean negativePrint = false;
	boolean drawchange = false;
	boolean drawAz = false;//倍率更新
	int Azfault = 0;//倍率默认
	int unitfault = 0;
	
	static int unitfaults[] = {0,0,0,0,0,0,0,0};
	int flydata;

	public GisGraph(int i, int j)
	{
		this(i, j, null, null,i * j);
	}

	public GisGraph(int i, int j, int ai[], int ai1[][], int k)
	{
		mindBm = GisSignal.calMin_dBm;
		maxdBm = GisSignal.calMax_dBm;
		avrdBm = GisSignal.calMin_dBm;
		amxdBm = GisSignal.calMin_dBm;
		f2dBm = GisSignal.calMin_dBm;
		f1dBm = GisSignal.calMin_dBm;
		effdBm = GisSignal.calMin_dBm;
		cycleSum = new int[80];
		cycleMax = new int[80];
		cyclef2Sum = new int[80];
		cyclef1Sum = new int[80];
		cycleeffSum = new int[80];
		d3p = new D3JPanel();
		printBlackWhite = Boolean.valueOf(false);
		cycleNum = 64;
		phaseNum = 80;
		directRefresh = false;
		oldrDeg = 0.0D;
		oldtDeg = 0.0D;
		drawing = false;
		prpsrefresh = true;
		draw3D = true;
		phaseDelay = 0;
		rdeg = 2.3561944901923448D;
		tdeg = 0.53407075111026492D;
		//rdeg = 2.3561944901923448D;
		//tdeg = 0.0D;
		oldw = 0;
		oldh = 0;
		maxSize = 0;
		stringToDraw = ChooseLang.M[132];//通道
		stringToDraw2 = "";
		ae = ai1;//飞行图
		bi = null;
		ds = new int[i * j];
		aebi = null;
		aeds = new int[40960];
		maxSize = k;
		setPreferredSize(new Dimension(180, 150));
		phaseNum = i;
		cycleNum = j;
		columdata = new int[64];
		flygraphicdata = new int[64*10];
		trancoumdata = new double[64];
		isflygraphic = false;
		Waveform = false;
		isCon = false;

		if (ai == null)
			pdData = new int[i * j];
		else
			pdData = ai;
		
		setAngle(getWidth(), getHeight(), 0, 0);
		addMouseMotionListener(new MouseMotionListener() {

			int oldx = 0;
			int oldy = 0;
			//int cnt = 0;

			public void mouseDragged(MouseEvent mouseevent)
			{
				if (!draw3D)
					return;
				double d = (double)(oldx - mouseevent.getX()) / (double)getWidth();
				oldx = mouseevent.getX();
				rdeg += d * 3.1400000000000001D;
				if (rdeg < 0.0D)
					rdeg = 0.0D;
				if (rdeg > 3.1415926535897931D)
					rdeg = 3.1415926535897931D;
				d = (double)(oldy - mouseevent.getY()) / (double)getHeight();
				oldy = mouseevent.getY();
				tdeg -= d * 3.1400000000000001D;
				if (tdeg < 0.0D)
					tdeg = 0.0D;
				if (tdeg > 1.5707963267948966D)
					tdeg = 1.5707963267948966D;
				if (directRefresh)
					repaint();
				else
					GisXYZ.refresh = true;
			}

			public void mouseMoved(MouseEvent mouseevent)
			{
				oldx = mouseevent.getX();
				oldy = mouseevent.getY();
			}

		});
		addMouseListener(new MouseListener() {

			
			public void mouseClicked(MouseEvent mouseevent)
			{
				/*
				if(mouseevent.getClickCount() == 2){ 
					JOptionPane.showMessageDialog(null,"doubleClicked!"); 
					} 
				*/	
			}

			public void mousePressed(MouseEvent mouseevent)
			{
			}

			public void mouseReleased(MouseEvent mouseevent)
			{
			}

			public void mouseEntered(MouseEvent mouseevent)
			{
			}

			public void mouseExited(MouseEvent mouseevent)
			{
			}

		});
	}
	//mana-绘制PRPS
	public void paint(Graphics g)
	{
		drawing = true;
		if (prpsrefresh)
		{
			makeDrawable();
			vg = img.createGraphics();
			if (draw3D)//mana-绘制PRPS-3D
				redraw3D(vg, getWidth(), getHeight());
			else//mana-绘制PRPS-2D
				redraw2D(vg);
			vg.dispose();
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

	public void setAngle(int i, int j, int k, int l)
	{
		if (0 < i && 0 < j && 0 < phaseNum && 0 < cycleNum)
		{
			d3p.setScreenSize(i, j, printBlackWhite, 4D);
			d3p.setBuffSize(phaseNum + 2, cycleNum + 2);
			d3p.initCamera(rdeg, tdeg, 5.5D);
		}
	}

	private void drawdBm(Graphics2D graphics2d, int i, int j, int k, int l, String s)
	{
		String s1 = (new StringBuilder()).append(s).append(String.format("%ddBm", new Object[] {
			Integer.valueOf(i)
		})).toString();
		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int i1 = fontmetrics.stringWidth(s1);
		graphics2d.drawString(s1, l != 0 ? j - i1 : j, k);
	}

	private void drawmW(Graphics2D graphics2d, int i, int j, int k, int l, String s)
	{
		double d = Math.pow(10D, (double)i / 10D);
		int j1;
		for (j1 = 0; d < 1.0D; j1--)
			d *= 10D;

		String s1 = (new StringBuilder()).append(s).append(String.format("%1.2f*10  mW", new Object[] {
			Double.valueOf(d)
		})).toString();
		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int i1 = fontmetrics.stringWidth(s1);
		graphics2d.drawString(s1, l != 0 ? j - i1 : j, k);
		graphics2d.drawString((new StringBuilder()).append("").append(j1).toString(), l != 0 ? j - 32 : (j + i1) - 32, k - 8);
	}

	private void drawmV(Graphics2D graphics2d, int i, int j, int k, int l, String s)
	{
//memo 20230901 修改 mV 为0-5000mV
//		double d = 100.0D * 22.36 * Math.sqrt(5.0D * Math.pow(10.0D, i*1.0D/10.0D));
//		String s1 = (new StringBuilder()).append(s).append(String.format("%1.0fmV", new Object[] {
//			Double.valueOf(d)
//		})).toString();

		double d = 10000.0D * Math.sqrt(5.0D * Math.pow(10.0D, i*1.0D/10.0D));
		String s1 = (new StringBuilder()).append(s).append(String.format("%1.1fmV", new Object[] {
				Double.valueOf(d)
		})).toString();

		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int i1 = fontmetrics.stringWidth(s1);
		graphics2d.drawString(s1, l != 0 ? j - i1 : j, k);
		//graphics2d.drawString((new StringBuilder()).append("").append(j1).toString(), l != 0 ? j - 26 : (j + i1) - 26, k - 8);
	}

	//超声波单位转换倍率
	private void drawmVx100(Graphics2D graphics2d, int i, int j, int k, int l, String s)
	{
		//double d = Math.pow(10D, (double)i / 20D);//废弃公式
		//double d = 100 * Math.sqrt(5 * Math.pow(10D, (double)i/10));
		double d = 10000.0D * Math.sqrt(5.0D * Math.pow(10.0D, i*1.0D/10.0D));
		//int j1;
		//for (j1 = 0; d < 1.0D; j1--)
			//d *= 10D;

		String s1 = (new StringBuilder()).append(s).append(String.format("%1.1fmV", new Object[] {
			Double.valueOf(d)
		})).toString();
		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int i1 = fontmetrics.stringWidth(s1);
		graphics2d.drawString(s1, l != 0 ? j - i1 : j, k);
		//graphics2d.drawString((new StringBuilder()).append("").append(j1).toString(), l != 0 ? j - 26 : (j + i1) - 26, k - 8);
	}
	
	private void drawmVx10(Graphics2D graphics2d, int i, int j, int k, int l, String s)
	{
		//double d = Math.pow(10D, (double)i / 20D);//废弃公式
		//double d = 100 * Math.sqrt(5 * Math.pow(10D, (double)i/10));
		double d = 1000.0D * Math.sqrt(5.0D * Math.pow(10.0D, i*1.0D/10.0D));
		//int j1;
		//for (j1 = 0; d < 1.0D; j1--)
			//d *= 10D;

		String s1 = (new StringBuilder()).append(s).append(String.format("%1.1fmV", new Object[] {
			Double.valueOf(d)
		})).toString();
		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int i1 = fontmetrics.stringWidth(s1);
		graphics2d.drawString(s1, l != 0 ? j - i1 : j, k);
		//graphics2d.drawString((new StringBuilder()).append("").append(j1).toString(), l != 0 ? j - 26 : (j + i1) - 26, k - 8);
	}
	
	private void drawmVx1(Graphics2D graphics2d, int i, int j, int k, int l, String s)
	{
		//double d = Math.pow(10D, (double)i / 20D);//废弃公式
		//double d = 100 * Math.sqrt(5 * Math.pow(10D, (double)i/10));
		double d = 100.0D * Math.sqrt(5.0D * Math.pow(10.0D, i*1.0D/10.0D));
		//int j1;
		//for (j1 = 0; d < 1.0D; j1--)
			//d *= 10D;

		String s1 = (new StringBuilder()).append(s).append(String.format("%1.1fmV", new Object[] {
			Double.valueOf(d)
		})).toString();
		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int i1 = fontmetrics.stringWidth(s1);
		graphics2d.drawString(s1, l != 0 ? j - i1 : j, k);
		//graphics2d.drawString((new StringBuilder()).append("").append(j1).toString(), l != 0 ? j - 26 : (j + i1) - 26, k - 8);
	}
	
	private void drawpC(Graphics2D graphics2d, int i, int j, int k, int l, String s)
	{
		double d = Math.pow(1.1795D, 61 + i) + 3D;//废弃公式
		//double d = (100 * Math.sqrt(5 * Math.pow(10D, (double)i/10))) / (double)(0.707 * 5);
		String s1 = (new StringBuilder()).append(s).append(String.format("%.1fpC", new Object[] {
			Double.valueOf(d)
		})).toString();
		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int i1 = fontmetrics.stringWidth(s1);
		graphics2d.drawString(s1, l != 0 ? j - i1 : j, k);
	}
	
	private void drawdBmV(Graphics2D graphics2d, int i, int j, int k, int l, String s)
	{
//		double d1 = 100 * Math.sqrt(5 * Math.pow(10D, (double)i/10));
		double d = i+80;
		//int j1;
		//for (j1 = 0; d < 1.0D; j1--)
			//d *= 10D;

		String s1 = (new StringBuilder()).append(s).append(String.format("%1.2fdB", new Object[] {
			Double.valueOf(d)
		})).toString();
		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int i1 = fontmetrics.stringWidth(s1);
		graphics2d.drawString(s1, l != 0 ? j - i1 : j, k);
		//graphics2d.drawString((new StringBuilder()).append("").append(j1).toString(), l != 0 ? j - 26 : (j + i1) - 26, k - 8);
	}

	private void drawUnit(Graphics2D graphics2d, int i, int j, int k, int l, String s, boolean change, int unit)
	{
//		System.out.println("change              "+change);
//		System.out.println("Option.unitType               "+Option.unitType);
//		System.out.println("unit              "+unit);
		
		if(change)
		{
		switch (Option.unitType)
		{
		case 0: // '\0'
			drawdBm(graphics2d, i, j, k, l, s);
			break;

		case 1: // '\001'
			drawmW(graphics2d, i, j, k, l, s);
			break;

		case 2: // '\002'
			drawmV(graphics2d, i, j, k, l, s);
			break;

		case 3: // '\003'
			drawpC(graphics2d, i, j, k, l, s);
			break;
			
		case 4: // '\004'
			drawdBmV(graphics2d, i, j, k, l, s);
			break;
		}
		}
		else
		{
			switch(unit)
			{
			case 0:
				drawdBm(graphics2d, i, j, k, l, s);
				break;
			case 1:
				drawmW(graphics2d, i, j, k, l, s);
				break;
			case 2:
				drawmV(graphics2d, i, j, k, l, s);
				break;
			case 3:
				drawpC(graphics2d, i, j, k, l, s);
				break;
			case 4:
				drawdBmV(graphics2d, i, j, k, l, s);
				break;
			
			}
		}
	}

	public void drawUnit(Graphics2D graphics2d, int i, int j, int k, int l, String s)
	{
		switch (Option.unitType)
		{
		case 0: // '\0'
		case 1: // '\0'
		case 2: // '\0'
		case 3: // '\0'
		case 4: // '\0'
			drawmV(graphics2d, i, j, k, l, s);
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

	//超声波倍率x
	private void drawUnitxAz(Graphics2D graphics2d, int i, int j, int k, int l,String s, boolean change, int unit) 
	{
		if(change)
		{
			switch (Option.Az)
			{
			case 0: // '\0'
				drawmVx1(graphics2d, i, j, k, l, s);
				break;
			case 1: // '\0'
				drawmVx10(graphics2d, i, j, k, l, s);
				break;
			case 2: // '\0'
				drawmVx100(graphics2d, i, j, k, l, s);
				break;
			}
		}
		else
		{
			switch(unit)
			{
			case 0: // '\0'
			case 1:
			case 2:
				drawmVx1(graphics2d, i, j, k, l, s);
				break;
				/*
			case 1: // '\0'
				drawmVx10(graphics2d, i, j, k, l, s);
				break;
			case 2: // '\0'
				drawmVx100(graphics2d, i, j, k, l, s);
				break;
			*/
			}
		}
		
	}
	
	public void redraw3D(Graphics2D graphics2d, int i, int j)
	{
		redraw3D(graphics2d, i, j, 0, 0);
	}

	public void redraw3D(Graphics2D graphics2d, int i, int j, int k, int l)//mana-绘制3d谱图
	{
		int ai[][] = new int[7][5];
		int ai1[][] = new int[7][5];
		
		//坐标轴
		int Axis_s[][] = new int[4][4];
		int Axis_e[][] = new int[4][4];
		
		d3p.clearD3Screen();
		double d1 = 1.0D / (double)cycleNum;
		for (int i1 = -1; i1 < cycleNum + 1; i1++)
		{
			int i3 = -1;
			//mana-左侧收束
			d3p.rotConvScreen(i3 + 1, i1 + 1, ((double)(i3 - phaseNum / 2) + 0.5D) * d1, ((double)(i1 - cycleNum / 2) + 0.5D) * d1, -0.5D * (double)cycleNum * d1 * 0.5D, 0.0D);
			i3 = phaseNum;
			//mana-右侧收束
			d3p.rotConvScreen(i3 + 1, i1 + 1, ((double)(i3 - phaseNum / 2) + 0.5D) * d1, ((double)(i1 - cycleNum / 2) + 0.5D) * d1, -0.5D * (double)cycleNum * d1 * 0.5D, 0.0D);
		}

		for (int j3 = -1; j3 < phaseNum + 1; j3++)
		{
			int j1 = -1;
			//mana-尾侧收束
			d3p.rotConvScreen(j3 + 1, j1 + 1, ((double)(j3 - phaseNum / 2) + 0.5D) * d1, ((double)(j1 - cycleNum / 2) + 0.5D) * d1, -0.5D * (double)cycleNum * d1 * 0.5D, 0.0D);
			j1 = cycleNum;
			//mana-头侧收束
			d3p.rotConvScreen(j3 + 1, j1 + 1, ((double)(j3 - phaseNum / 2) + 0.5D) * d1, ((double)(j1 - cycleNum / 2) + 0.5D) * d1, -0.5D * (double)cycleNum * d1 * 0.5D, 0.0D);
		}

		double ad[][] = new double[cycleNum][phaseNum];
		for (int k1 = 0; k1 < cycleNum; k1++)
		{
			for (int k3 = 0; k3 < phaseNum; k3++)
			{
				int i5 = k1 * phaseNum + k3;
				if (i5 >= maxSize)
					i5 -= maxSize;
				if (i5 < 0)
					i5 += maxSize;
				double d = (double)pdData[i5] / 4095D;
				if (1.0D < d)
					d = 1.0D;
				if (d < 0.0D)
					d = 0.0D;
				ad[k1][k3] = d;
				d3p.rotConvScreen(k3 + 1, k1 + 1, ((double)(k3 - phaseNum / 2) + 0.5D) * d1, ((double)(k1 - cycleNum / 2) + 0.5D) * d1, (d - 0.5D) * (double)cycleNum * d1 * 0.5D, d);
			}
		}
		
		d3p.drawD3Screen();
		//小坐标轴X轴
		for (int x1 = 2; 1 <= x1; x1--)
		{
			for (int xn3 = -2; xn3 <= 1; xn3++)
			{
				d3p.rotConvScreen((double)(xn3-16) * (-0.25D) * (double)phaseNum * d1/4, (double)(x1-1) * (0.25D) * (double)cycleNum * d1/4, -0.25D * (double)cycleNum * d1);
				Axis_s[2-x1][xn3+2] = d3p.gx;
				Axis_e[2-x1][xn3+2] = d3p.gy;
			}
		}
		
		//小坐标轴y轴
		for (int y1 = 0; y1 <=1; y1++)
		{
			for (int yn3 = -2; yn3 <= 1; yn3++)
			{
				d3p.rotConvScreen((double)(yn3-16) * (-0.25D) * (double)phaseNum * d1/4, (double)(y1-1) * (0.25D) * (double)cycleNum * d1/4, -0.25D * (double)cycleNum * d1);
				Axis_s[1+y1][yn3+2] = d3p.gx;
				Axis_e[1+y1][yn3+2] = d3p.gy;
			}
		}
		
		//小坐标轴z轴
		for (int z1 = 0; z1 <= 1; z1++)
		{
			for (int zn3 = -2; zn3 <= 1; zn3++)
			{
				d3p.rotConvScreen((double)(zn3-16) * (-0.25D) * (double)phaseNum * d1/4, 0.25D * (double)cycleNum * d1/4, (double)(z1-2) *(0.25D) * (double)cycleNum * d1/2);
				Axis_s[2+z1][zn3+2] = d3p.gx;
				Axis_e[2+z1][zn3+2] = d3p.gy;
			}
		}
				
		d3p.drawD3Screen();
		for (int l1 = 2; -1 <= l1; l1--)
		{
			for (int l3 = -2; l3 <= 2; l3++)
			{
				d3p.rotConvScreen((double)l3 * 0.25D * (double)phaseNum * d1, (double)l1 * 0.25D * (double)cycleNum * d1, -0.25D * (double)cycleNum * d1);
				ai[2 - l1][l3 + 2] = d3p.gx;
				ai1[2 - l1][l3 + 2] = d3p.gy;
			}

		}

		for (int i2 = 0; i2 < 3; i2++)
		{
			for (int i4 = -2; i4 <= 2; i4++)
			{
				d3p.rotConvScreen((double)i4 * 0.25D * (double)phaseNum * d1, -0.5D * (double)cycleNum * d1, (double)(i2 - 1) * 0.25D * (double)cycleNum * d1);
				ai[4 + i2][i4 + 2] = d3p.gx;
				ai1[4 + i2][i4 + 2] = d3p.gy;
			}

		}

		for (int j4 = 0; j4 < 5; j4++)
		{
			int k4;
			if (j4 == 0 || j4 == 4)
				k4 = Option.gt ? 0xffffff : 0;
			else
				k4 = 0x808080;
			d3p.drawLine(ai[0][j4], ai1[0][j4], ai[4][j4], ai1[4][j4], k4);
			d3p.drawLine(ai[4][j4], ai1[4][j4], ai[6][j4], ai1[6][j4], k4);
		}

		for (int j2 = 0; j2 < 7; j2++)
		{
			int l4;
			if (j2 == 0 || j2 == 6)
				l4 = Option.gt ? 0xffffff : 0;
			else
				l4 = 0x808080;
			d3p.drawLine(ai[j2][0], ai1[j2][0], ai[j2][4], ai1[j2][4], l4);
		}
		//坐标x轴
		for (int x3 = 0; x3 < 1; x3++)
		{
			int xx3;
				xx3 = Option.gt ? 0x69C38C : 0x963C73;
			d3p.drawLine(Axis_s[x3][2], Axis_e[x3][2], Axis_s[1][2], Axis_e[1][2], xx3);
			//d3p.drawLine(Axis_s[1][x3], Axis_e[1][x3], Axis_s[2][x3], Axis_e[2][x3], xx3);
		}
		//坐标y轴
		for (int y3 = 0; y3 < 1; y3++)
		{
			int yy3;
				yy3 = Option.gt ? 0x69C38C : 0x963C73;
			d3p.drawLine(Axis_s[0][y3], Axis_e[0][y3], Axis_s[y3][2], Axis_e[y3][2], yy3);
		}
		//坐标z轴
		for (int z3 = 0; z3 < 1; z3++)
		{
			int zz3;
				zz3 = Option.gt ? 0x69C38C : 0x963C73;
			d3p.drawLine(Axis_s[z3][2], Axis_e[z3][2], Axis_s[3][2], Axis_e[3][2], zz3);
		}

		d3p.draw(graphics2d, k, l);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.LIGHT_GRAY)) : ((java.awt.Paint) (Color.DARK_GRAY)));
		if (rdeg < 1.5707963267948966D)
		{
			graphics2d.drawString(" 90'", k + ai[0][1], l + ai1[0][1] + 11);
			graphics2d.drawString("180'", k + ai[0][2], l + ai1[0][2] + 11);
			graphics2d.drawString("270'", k + ai[0][3], l + ai1[0][3] + 11);
			drawUnit(graphics2d, mindBm, k + ai[4][4] + 4, l + ai1[4][4], 0, "", drawchange, unitfault);
			drawUnit(graphics2d, maxdBm, k + ai[6][4] + 4, l + ai1[6][4], 0, "", drawchange, unitfault);

// 获取字体测量工具
			java.awt.FontMetrics fm = graphics2d.getFontMetrics();

// 1. 绘制 "幅值/Amplitude" (通常是Z轴，竖向)
			String textAmp = ChooseLang.M[402];
			int widthAmp = fm.stringWidth(textAmp);
// 原来是 -25，现在改成减去文字实际宽度再多减一点间距(例如5像素)，保证文字在轴的左边
			graphics2d.drawString(textAmp, k + Axis_s[3][2] - widthAmp - 5, l + Axis_e[3][2] + 8);

// 2. 绘制 "相位/Phase" (通常是X轴或Y轴)
			String textPhase = ChooseLang.M[403];
// 假设这个轴向右延伸，文字直接画在坐标点旁边即可，不用减宽度
			graphics2d.drawString(textPhase, k + Axis_s[0][0] + 10, l + Axis_e[0][0] + 5);

// 3. 绘制 "时间/Time" (另一条水平轴)
			String textTime = ChooseLang.M[404];
// 同样稍微加点偏移量防止贴着线
			graphics2d.drawString(textTime, k + Axis_s[1][2] + 10, l + Axis_e[1][2] + 5);		} else
		{
			graphics2d.drawString(" 90'", (k + ai[0][1]) - 25, l + ai1[0][1] + 11);
			graphics2d.drawString("180'", (k + ai[0][2]) - 25, l + ai1[0][2] + 11);
			graphics2d.drawString("270'", (k + ai[0][3]) - 25, l + ai1[0][3] + 11);
			drawUnit(graphics2d, mindBm, (k + ai[4][0]) - 4, l + ai1[4][0], 1, "", drawchange, unitfault);
			drawUnit(graphics2d, maxdBm, (k + ai[6][0]) - 4, l + ai1[6][0], 1, "", drawchange, unitfault);

// 获取字体测量工具
			java.awt.FontMetrics fm = graphics2d.getFontMetrics();

// 1. 绘制 "幅值/Amplitude" (通常是Z轴，竖向)
			String textAmp = ChooseLang.M[402];
			int widthAmp = fm.stringWidth(textAmp);
// 原来是 -25，现在改成减去文字实际宽度再多减一点间距(例如5像素)，保证文字在轴的左边
			graphics2d.drawString(textAmp, k + Axis_s[3][2] - widthAmp - 5, l + Axis_e[3][2] + 8);

// 2. 绘制 "相位/Phase" (通常是X轴或Y轴)
			String textPhase = ChooseLang.M[403];
// 假设这个轴向右延伸，文字直接画在坐标点旁边即可，不用减宽度
			graphics2d.drawString(textPhase, k + Axis_s[0][0] + 10, l + Axis_e[0][0] + 5);

// 3. 绘制 "时间/Time" (另一条水平轴)
			String textTime = ChooseLang.M[404];
// 同样稍微加点偏移量防止贴着线
			graphics2d.drawString(textTime, k + Axis_s[1][2] + 10, l + Axis_e[1][2] + 5);		}
		int k2 = (int)((double)(-phaseDelay) * 5.625D + 0.5D);
		if (k2 == 242)
			k2 = 240;
		if (k2 == 118)
			k2 = 120;
		//graphics2d.drawString((new StringBuilder()).append(ChooseLang.M[260]).append(" : ").append(k2).append("'").toString(), 4, (l + j) - 5);
		if (0 < stringToDraw2.length())
			graphics2d.drawString(stringToDraw2, (k + i) - 62, (l + j) - 5);
		else
		if (GisXYZ.realTab == 0)
			drawUnit(graphics2d, (int)(avrdBm - 0.5D), (k + i) - 4, (l + 35), 1, ChooseLang.M[401], drawchange, unitfault);
		//if (GisXYZ.realTab == 0)
			drawUnit(graphics2d, (int)(amxdBm - 0.5D), (k + i) - 4, l + 20, 1, ChooseLang.M[400], drawchange, unitfault);

				
		for (int l2 = 0; l2 < 8; l2++)
			drawOutlineString(graphics2d, resultString[l2], k + 4, l + 15 + 15 * l2, GisXYZ.anti_Color, GisXYZ.pure_Color);
		//drawInstructTriangle(graphics2d,Color.CYAN,0,(int)((j/60.0D)*(-avrdBm)-((j/60.0D)*20.0D)),i/40,(int)((j/60.0D)*(-avrdBm)-((j/60.0D)*20.0D)-(i/40)*Math.tan(Math.PI/6)),i/40,(int)((j/60.0D)*(-avrdBm)-((j/60.0D)*20.0D)+(i/40)*Math.tan(Math.PI/6)),j,3);
		//drawInstructTriangle(graphics2d,Color.CYAN,0,(int)((j/60.0D)*(-avrdBm)-((j/60.0D)*20.0D)),400/40,(int)((j/60.0D)*(-avrdBm)-((j/60.0D)*20.0D)-(400/40)*Math.tan(Math.PI/6)),400/40,(int)((j/60.0D)*(-avrdBm)-((j/60.0D)*20.0D)+(400/40)*Math.tan(Math.PI/6)),j,3);
		if (GisXYZ.realTab == 0)
			drawInstructTriangle(graphics2d, 0,(int)((j/60.0D)*(-amxdBm)-((j/60.0D)*20.0D)),400/40,(int)((j/60.0D)*(-amxdBm)-((j/60.0D)*20.0D)-(400/40)*Math.tan(Math.PI/6)),400/40,(int)((j/60.0D)*(-amxdBm)-((j/60.0D)*20.0D)+(400/40)*Math.tan(Math.PI/6)),j,3);
		drawOutlineString(graphics2d, stringToDraw, k + 4, l + 15, Color.yellow, Color.black);
	}


	
	public void redraw3D(Graphics2D graphics2d, int i, int j, int k, int l ,int ii1)//61 新增方法
	{
		int ai[][] = new int[7][5];
		int ai1[][] = new int[7][5];
		
		//坐标轴
		int Axis_s[][] = new int[4][4];
		int Axis_e[][] = new int[4][4];
		
		d3p.clearD3Screen();
		double d1 = 1.0D / (double)cycleNum;
		for (int i1 = -1; i1 < cycleNum + 1; i1++)
		{
			int i3 = -1;
			//mana-左侧收束
			d3p.rotConvScreen(i3 + 1, i1 + 1, ((double)(i3 - phaseNum / 2) + 0.5D) * d1, ((double)(i1 - cycleNum / 2) + 0.5D) * d1, -0.5D * (double)cycleNum * d1 * 0.5D, 0.0D);
			i3 = phaseNum;
			//mana-右侧收束
			d3p.rotConvScreen(i3 + 1, i1 + 1, ((double)(i3 - phaseNum / 2) + 0.5D) * d1, ((double)(i1 - cycleNum / 2) + 0.5D) * d1, -0.5D * (double)cycleNum * d1 * 0.5D, 0.0D);
		}

		for (int j3 = -1; j3 < phaseNum + 1; j3++)
		{
			int j1 = -1;
			//mana-尾侧收束
			d3p.rotConvScreen(j3 + 1, j1 + 1, ((double)(j3 - phaseNum / 2) + 0.5D) * d1, ((double)(j1 - cycleNum / 2) + 0.5D) * d1, -0.5D * (double)cycleNum * d1 * 0.5D, 0.0D);
			j1 = cycleNum;
			//mana-头侧收束
			d3p.rotConvScreen(j3 + 1, j1 + 1, ((double)(j3 - phaseNum / 2) + 0.5D) * d1, ((double)(j1 - cycleNum / 2) + 0.5D) * d1, -0.5D * (double)cycleNum * d1 * 0.5D, 0.0D);
		}

		double ad[][] = new double[cycleNum][phaseNum];
		for (int k1 = 0; k1 < cycleNum; k1++)
		{
			for (int k3 = 0; k3 < phaseNum; k3++)
			{
				int i5 = k1 * phaseNum + k3;
				if (i5 >= maxSize)
					i5 -= maxSize;
				if (i5 < 0)
					i5 += maxSize;
				double d = (double)pdData[i5] / 4095D;
				if (1.0D < d)
					d = 1.0D;
				if (d < 0.0D)
					d = 0.0D;
				ad[k1][k3] = d;
				d3p.rotConvScreen(k3 + 1, k1 + 1, ((double)(k3 - phaseNum / 2) + 0.5D) * d1, ((double)(k1 - cycleNum / 2) + 0.5D) * d1, (d - 0.5D) * (double)cycleNum * d1 * 0.5D, d);
			}
		}
		
		d3p.drawD3Screen();
		//小坐标轴X轴
		for (int x1 = 2; 1 <= x1; x1--)
		{
			for (int xn3 = -2; xn3 <= 1; xn3++)
			{
				d3p.rotConvScreen((double)(xn3-16) * (-0.25D) * (double)phaseNum * d1/4, (double)(x1-1) * (0.25D) * (double)cycleNum * d1/4, -0.25D * (double)cycleNum * d1);
				Axis_s[2-x1][xn3+2] = d3p.gx;
				Axis_e[2-x1][xn3+2] = d3p.gy;
			}
		}
		
		//小坐标轴y轴
		for (int y1 = 0; y1 <=1; y1++)
		{
			for (int yn3 = -2; yn3 <= 1; yn3++)
			{
				d3p.rotConvScreen((double)(yn3-16) * (-0.25D) * (double)phaseNum * d1/4, (double)(y1-1) * (0.25D) * (double)cycleNum * d1/4, -0.25D * (double)cycleNum * d1);
				Axis_s[1+y1][yn3+2] = d3p.gx;
				Axis_e[1+y1][yn3+2] = d3p.gy;
			}
		}
		
		//小坐标轴z轴
		for (int z1 = 0; z1 <= 1; z1++)
		{
			for (int zn3 = -2; zn3 <= 1; zn3++)
			{
				d3p.rotConvScreen((double)(zn3-16) * (-0.25D) * (double)phaseNum * d1/4, 0.25D * (double)cycleNum * d1/4, (double)(z1-2) *(0.25D) * (double)cycleNum * d1/2);
				Axis_s[2+z1][zn3+2] = d3p.gx;
				Axis_e[2+z1][zn3+2] = d3p.gy;
			}
		}
				
		d3p.drawD3Screen();
		for (int l1 = 2; -1 <= l1; l1--)
		{
			for (int l3 = -2; l3 <= 2; l3++)
			{
				d3p.rotConvScreen((double)l3 * 0.25D * (double)phaseNum * d1, (double)l1 * 0.25D * (double)cycleNum * d1, -0.25D * (double)cycleNum * d1);
				ai[2 - l1][l3 + 2] = d3p.gx;
				ai1[2 - l1][l3 + 2] = d3p.gy;
			}

		}

		for (int i2 = 0; i2 < 3; i2++)
		{
			for (int i4 = -2; i4 <= 2; i4++)
			{
				d3p.rotConvScreen((double)i4 * 0.25D * (double)phaseNum * d1, -0.5D * (double)cycleNum * d1, (double)(i2 - 1) * 0.25D * (double)cycleNum * d1);
				ai[4 + i2][i4 + 2] = d3p.gx;
				ai1[4 + i2][i4 + 2] = d3p.gy;
			}

		}

		for (int j4 = 0; j4 < 5; j4++)
		{
			int k4;
			if (j4 == 0 || j4 == 4)
				k4 = Option.gt ? 0xffffff : 0;
			else
				k4 = 0x808080;
			d3p.drawLine(ai[0][j4], ai1[0][j4], ai[4][j4], ai1[4][j4], k4);
			d3p.drawLine(ai[4][j4], ai1[4][j4], ai[6][j4], ai1[6][j4], k4);
		}

		for (int j2 = 0; j2 < 7; j2++)
		{
			int l4;
			if (j2 == 0 || j2 == 6)
				l4 = Option.gt ? 0xffffff : 0;
			else
				l4 = 0x808080;
			d3p.drawLine(ai[j2][0], ai1[j2][0], ai[j2][4], ai1[j2][4], l4);
		}
		//坐标x轴
		for (int x3 = 0; x3 < 1; x3++)
		{
			int xx3;
				xx3 = Option.gt ? 0x69C38C : 0x963C73;
			d3p.drawLine(Axis_s[x3][2], Axis_e[x3][2], Axis_s[1][2], Axis_e[1][2], xx3);
			//d3p.drawLine(Axis_s[1][x3], Axis_e[1][x3], Axis_s[2][x3], Axis_e[2][x3], xx3);
		}
		//坐标y轴
		for (int y3 = 0; y3 < 1; y3++)
		{
			int yy3;
				yy3 = Option.gt ? 0x69C38C : 0x963C73;
			d3p.drawLine(Axis_s[0][y3], Axis_e[0][y3], Axis_s[y3][2], Axis_e[y3][2], yy3);
		}
		//坐标z轴
		for (int z3 = 0; z3 < 1; z3++)
		{
			int zz3;
				zz3 = Option.gt ? 0x69C38C : 0x963C73;
			d3p.drawLine(Axis_s[z3][2], Axis_e[z3][2], Axis_s[3][2], Axis_e[3][2], zz3);
		}

		d3p.draw(graphics2d, k, l);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.LIGHT_GRAY)) : ((java.awt.Paint) (Color.DARK_GRAY)));
		if (rdeg < 1.5707963267948966D)
		{
			graphics2d.drawString(" 90'", k + ai[0][1], l + ai1[0][1] + 11);
			graphics2d.drawString("180'", k + ai[0][2], l + ai1[0][2] + 11);
			graphics2d.drawString("270'", k + ai[0][3], l + ai1[0][3] + 11);
			drawUnit(graphics2d, mindBm, k + ai[4][4] + 4, l + ai1[4][4], 0, "", drawchange, unitfault);
			drawUnit(graphics2d, maxdBm, k + ai[6][4] + 4, l + ai1[6][4], 0, "", drawchange, unitfault);

// 获取字体测量工具
			java.awt.FontMetrics fm = graphics2d.getFontMetrics();

// 1. 绘制 "幅值/Amplitude" (通常是Z轴，竖向)
			String textAmp = ChooseLang.M[402];
			int widthAmp = fm.stringWidth(textAmp);
// 原来是 -25，现在改成减去文字实际宽度再多减一点间距(例如5像素)，保证文字在轴的左边
			graphics2d.drawString(textAmp, k + Axis_s[3][2] - widthAmp - 5, l + Axis_e[3][2] + 8);

// 2. 绘制 "相位/Phase" (通常是X轴或Y轴)
			String textPhase = ChooseLang.M[403];
// 假设这个轴向右延伸，文字直接画在坐标点旁边即可，不用减宽度
			graphics2d.drawString(textPhase, k + Axis_s[0][0] + 10, l + Axis_e[0][0] + 5);

// 3. 绘制 "时间/Time" (另一条水平轴)
			String textTime = ChooseLang.M[404];
// 同样稍微加点偏移量防止贴着线
			graphics2d.drawString(textTime, k + Axis_s[1][2] + 10, l + Axis_e[1][2] + 5);		} else
		{
			graphics2d.drawString(" 90'", (k + ai[0][1]) - 25, l + ai1[0][1] + 11);
			graphics2d.drawString("180'", (k + ai[0][2]) - 25, l + ai1[0][2] + 11);
			graphics2d.drawString("270'", (k + ai[0][3]) - 25, l + ai1[0][3] + 11);
			drawUnit(graphics2d, mindBm, (k + ai[4][0]) - 4, l + ai1[4][0], 1, "", drawchange, unitfault);
			drawUnit(graphics2d, maxdBm, (k + ai[6][0]) - 4, l + ai1[6][0], 1, "", drawchange, unitfault);

// 获取字体测量工具
			java.awt.FontMetrics fm = graphics2d.getFontMetrics();

// 1. 绘制 "幅值/Amplitude" (通常是Z轴，竖向)
			String textAmp = ChooseLang.M[402];
			int widthAmp = fm.stringWidth(textAmp);
// 原来是 -25，现在改成减去文字实际宽度再多减一点间距(例如5像素)，保证文字在轴的左边
			graphics2d.drawString(textAmp, k + Axis_s[3][2] - widthAmp - 5, l + Axis_e[3][2] + 8);

// 2. 绘制 "相位/Phase" (通常是X轴或Y轴)
			String textPhase = ChooseLang.M[403];
// 假设这个轴向右延伸，文字直接画在坐标点旁边即可，不用减宽度
			graphics2d.drawString(textPhase, k + Axis_s[0][0] + 10, l + Axis_e[0][0] + 5);

// 3. 绘制 "时间/Time" (另一条水平轴)
			String textTime = ChooseLang.M[404];
// 同样稍微加点偏移量防止贴着线
			graphics2d.drawString(textTime, k + Axis_s[1][2] + 10, l + Axis_e[1][2] + 5);		}
		int k2 = (int)((double)(-phaseDelay) * 5.625D + 0.5D);
		if (k2 == 242)
			k2 = 240;
		if (k2 == 118)
			k2 = 120;
		//graphics2d.drawString((new StringBuilder()).append(ChooseLang.M[260]).append(" : ").append(k2).append("'").toString(), 4, (l + j) - 5);
		if (0 < stringToDraw2.length())
			graphics2d.drawString(stringToDraw2, (k + i) - 62, (l + j) - 5);
		else
		if (GisXYZ.realTab == 0)
			drawUnit(graphics2d, (int)(avrdBm - 0.5D), (k + i) - 4, (l + 35), 1, ChooseLang.M[401], drawchange, unitfaults[ii1]);
		//if (GisXYZ.realTab == 0)
			drawUnit(graphics2d, (int)(amxdBm - 0.5D), (k + i) - 4, l + 20, 1, ChooseLang.M[400], drawchange, unitfaults[ii1]);
		for (int l2 = 0; l2 < 8; l2++)
			drawOutlineString(graphics2d, resultString[l2], k + 4, l + 15 + 15 * l2, GisXYZ.anti_Color, GisXYZ.pure_Color);
		//drawInstructTriangle(graphics2d,Color.CYAN,0,(int)((j/60.0D)*(-avrdBm)-((j/60.0D)*20.0D)),i/40,(int)((j/60.0D)*(-avrdBm)-((j/60.0D)*20.0D)-(i/40)*Math.tan(Math.PI/6)),i/40,(int)((j/60.0D)*(-avrdBm)-((j/60.0D)*20.0D)+(i/40)*Math.tan(Math.PI/6)),j,3);
		//drawInstructTriangle(graphics2d,Color.CYAN,0,(int)((j/60.0D)*(-avrdBm)-((j/60.0D)*20.0D)),400/40,(int)((j/60.0D)*(-avrdBm)-((j/60.0D)*20.0D)-(400/40)*Math.tan(Math.PI/6)),400/40,(int)((j/60.0D)*(-avrdBm)-((j/60.0D)*20.0D)+(400/40)*Math.tan(Math.PI/6)),j,3);
		if (GisXYZ.realTab == 0)
			drawInstructTriangle(graphics2d, 0,(int)((j/60.0D)*(-amxdBm)-((j/60.0D)*20.0D)),400/40,(int)((j/60.0D)*(-amxdBm)-((j/60.0D)*20.0D)-(400/40)*Math.tan(Math.PI/6)),400/40,(int)((j/60.0D)*(-amxdBm)-((j/60.0D)*20.0D)+(400/40)*Math.tan(Math.PI/6)),j,3);
		drawOutlineString(graphics2d, stringToDraw, k + 4, l + 15, Color.yellow, Color.black);
	}

	/*
	//绘制色标指示
	public void drawInstructTriangle(Graphics g, Color color, int x1, int y1, int x2, int y2, int x3, int y3, int j, int nPoints)     
	{   
		int x[] = new int[3];//x坐标数组
	    int y[] = new int[3];//y坐标数组
	    x[0] = x1;
	    x[1] = x2;
	    x[2] = x3;
	    y[0] = y1;
	    y[1] = y2;
	    y[2] = y3; 
		
		Graphics2D graphics2d = (Graphics2D)g;
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

		graphics2d.setPaint(Color.BLACK);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.DARK_GRAY)) : ((java.awt.Paint) (Color.LIGHT_GRAY)));
		if(0 <= y1 && y1 < (int)(j/5.0D) )
		{
			GradientPaint paint0 = new GradientPaint(0,0,Color.RED,0,(int)(j/5.0D),Color.BLUE);
			GradientPaint paint01 = new GradientPaint(0,0,Color.GREEN,0,(int)(j/5.0D),Color.YELLOW);
			graphics2d.setPaint(Option.gt ? paint0 :paint01);
		}
		
		if((int)(j/5.0D) <= y1 && y1 < (int)(2*j/5.0D) )
		{
			GradientPaint paint1 = new GradientPaint(0,(int)(j/5.0D),Color.BLUE,0,(int)(2*j/5.0D),Color.GREEN);
			GradientPaint paint11 = new GradientPaint(0,(int)(j/5.0D),Color.YELLOW,0,(int)(2*j/5.0D),Color.RED);
			graphics2d.setPaint(Option.gt ? paint1 :paint11);
		}
		
		if((int)(2*j/5.0D) <= y1 && y1 < (int)(3*j/5.0D) )
		{
			GradientPaint paint2 = new GradientPaint(0,(int)(2*j/5.0D),Color.GREEN,0,(int)(3*j/5.0D),Color.YELLOW);
			GradientPaint paint21 = new GradientPaint(0,(int)(2*j/5.0D),Color.RED,0,(int)(3*j/5.0D),Color.BLUE);
			graphics2d.setPaint(Option.gt ? paint2 :paint21);
		}
		
		if((int)(3*j/5.0D) <= y1 && y1 < (int)(4*j/5.0D) )
		{
			GradientPaint paint3 = new GradientPaint(0,(int)(3*j/5.0D),Color.YELLOW,0,(int)(4*j/5.0D),Color.WHITE);
			GradientPaint paint31 = new GradientPaint(0,(int)(3*j/5.0D),Color.BLUE,0,(int)(4*j/5.0D),Color.BLACK);
			graphics2d.setPaint(Option.gt ? paint3 :paint31);
		}
	    
		if((int)(4*j/5.0D) <= y1 && y1 <= (int)(5*j/5.0D) )
		{
			GradientPaint paint4 = new GradientPaint(0,(int)(4*j/5.0D),Color.WHITE,0,(int)(5*j/5.0D),Color.BLACK);
			GradientPaint paint41 = new GradientPaint(0,(int)(4*j/5.0D),Color.BLACK,0,(int)(5*j/5.0D),Color.WHITE);
			graphics2d.setPaint(Option.gt ? paint4 :paint41);
		}
		
		g.fillPolygon(x, y, nPoints);
	    
	}  
	*/
	
	//绘制色标指示
	public void drawInstructTriangle(Graphics g, int x1, int y1, int x2, int y2, int x3, int y3, int j, int nPoints)     
		{   
			int x[] = new int[3];//x坐标数组
		    int y[] = new int[3];//y坐标数组
		    x[0] = x1;
		    x[1] = x2;
		    x[2] = x3;
		    y[0] = y1;
		    y[1] = y2;
		    y[2] = y3; 
			
			Graphics2D graphics2d = (Graphics2D)g;
			graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

			graphics2d.setPaint(Color.BLACK);
			graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.DARK_GRAY)) : ((java.awt.Paint) (Color.LIGHT_GRAY)));
			if(0 <= y1 && y1 < (int)(j/3.0D) )
			{
				GradientPaint paint0 = new GradientPaint(0,0,new Color(0xff0000),0,(int)(j/3.0D),new Color(0xffff00));
				GradientPaint paint01 = new GradientPaint(0,0,new Color(0x00ffff),0,(int)(j/3.0D),new Color(0x0000ff));
				graphics2d.setPaint(Option.gt ? paint0 :paint01);
			}
			
			if((int)(j/3.0D) <= y1 && y1 < (int)(2*j/3.0D) )
			{
				GradientPaint paint1 = new GradientPaint(0,(int)(j/3.0D),new Color(0xffff00),0,(int)(2*j/3.0D),new Color(0xffffff));
				GradientPaint paint11 = new GradientPaint(0,(int)(j/3.0D),new Color(0x0000ff),0,(int)(2*j/3.0D),new Color(0x0));
				graphics2d.setPaint(Option.gt ? paint1 :paint11);
			}
			
			if((int)(2*j/3.0D) <= y1 && y1 < (int)(3*j/3.0D) )
			{
				GradientPaint paint2 = new GradientPaint(0,(int)(2*j/3.0D),new Color(0xffffff),0,(int)(3*j/3.0D),new Color(0x0));
				GradientPaint paint21 = new GradientPaint(0,(int)(2*j/3.0D),new Color(0x0),0,(int)(3*j/3.0D),new Color(0xffffff));
				graphics2d.setPaint(Option.gt ? paint2 :paint21);
			}
			
			g.fillPolygon(x, y, nPoints);
		    
		}  
	
	void drawOutlineString(Graphics2D graphics2d, String s, int i, int j, Color color, Color color1)
	{
		graphics2d.setPaint(color1);
		graphics2d.drawString(s, i - 1, j);
		graphics2d.drawString(s, i + 1, j);
		graphics2d.drawString(s, i, j - 1);
		graphics2d.drawString(s, i, j + 1);
		graphics2d.setPaint(color);
		graphics2d.drawString(s, i, j);
	}
	
	//绘制PRPS2D图
	void redraw2D(Graphics g)
	{
		if(isflygraphic)
		{
			redraw2D(g, getWidth(), getHeight(), 0, 0);//飞行图
		}
		else
		{
			if(Waveform)
				redraw2D_Wave(g, getWidth(), getHeight(), 0, 0);//绘制波形图
			if(isCon)
				redraw2DCon(g, getWidth(), getHeight(), 0, 0);//绘制连续图
			else
				redraw2D_Colum(g, getWidth(), getHeight(), 0, 0);//绘制柱形图
		}
	}
	/*
	void redraw2D(Graphics g, int i, int j, int k, int l)
	{
		Graphics2D graphics2d = (Graphics2D)g;
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		int i2 = -1;
		for (int i1 = 0; i1 < cycleNum; i1++)//cycleNum
		{
			for (int l1 = 0; l1 < phaseNum; l1++)//phaseNum
			{
				int j2 = pdData[++i2];
				if (4095 < j2)
					j2 = 4095;
				if (j2 < 0)
					j2 = 0;
				ds[i2] = d3p.zColor(j2);
			}

		}

		if (bi == null)
			bi = new BufferedImage(64, 64, 1);
		bi.setRGB(0, 0, 64, 64, ds, 0, 64);
		//bi.setRGB(0, 0, 0, 0, ds, 0, 64);
		graphics2d.setPaint(Color.black);
		graphics2d.fillRect(0, 0, i, j);
		graphics2d.drawImage(bi, k, l, i, j, Option.gt ? Color.black : Color.white, null);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.LIGHT_GRAY)) : ((java.awt.Paint) (Color.DARK_GRAY)));
		int j1 = (int)((double)(-phaseDelay) * 5.625D + 0.5D);
		if (j1 == 242)
			j1 = 240;
		if (j1 == 118)
			j1 = 120;
		//graphics2d.drawString((new StringBuilder()).append(ChooseLang.M[260]).append(" : ").append(j1).append("'").toString(), 4, j - 5);
		if (0 < stringToDraw2.length())
			graphics2d.drawString(stringToDraw2, i - 62, (l + j) - 5);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.DARK_GRAY)) : ((java.awt.Paint) (Color.LIGHT_GRAY)));
		graphics2d.drawLine(i / 4, j / 8, i / 4, j);
		graphics2d.drawLine(i / 2, j / 8, i / 2, j);
		graphics2d.drawLine((i / 4) * 3, j / 8, (i / 4) * 3, j);
		graphics2d.drawLine(0, j / 2, i, j / 2);
		graphics2d.drawLine(0, j / 4, i, j / 4);
		graphics2d.drawLine(0, (j / 4) * 3, i, (j / 4) * 3);
		//�������һ�׼��
		
		for (double c=0; c<2*Math.PI; c+=0.001){
			//double s1=0.666666*Math.PI;
			//double s2=1.333333*Math.PI;
			double s3=0.0*Math.PI;
			//graphics2d.setColor(Color.RED);
			//graphics2d.drawLine(translateX(c), translateY(sin(c,s1)),translateX(c+0.001), translateY(sin(c+0.001,s1)));
			//graphics2d.setColor(Color.GREEN);
			//graphics2d.drawLine(translateX(c), translateY(sin(c,s2)),translateX(c+0.001), translateY(sin(c+0.001,s2)));
			//graphics2d.setColor(Color.YELLOW);
			//graphics2d.drawLine(translateX(c), translateY(sin(c,s3)),translateX(c+0.001), translateY(sin(c+0.001,s3)));
			graphics2d.drawLine((int)(c*i/Math.PI/2), (int)(j/2-(3*Math.sin(c-s3))*j/Math.PI/2),(int)((c+0.001)*i/Math.PI/2), (int)(j/2-(3*Math.sin(c+0.001-s3))*j/Math.PI/2));
		 }
		
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.LIGHT_GRAY)) : ((java.awt.Paint) (Color.DARK_GRAY)));
		
		if (GisXYZ.realTab == 0)
			drawUnit(graphics2d, (int)(avrdBm - 0.5D), (k + i) - 4, (l + j) - 5, 1, "avr : ", drawchange);
		if (GisXYZ.realTab == 0)
			drawUnit(graphics2d, (int)(amxdBm - 0.5D), (k + i) - 4, l + 55, 1, "max : ", drawchange);
		
		graphics2d.drawString("90'", i / 4 - 6, j / 10);
		graphics2d.drawString("180'", i / 2 - 10, j / 10);
		graphics2d.drawString("270'", (i / 4) * 3 - 10, j / 10);
		graphics2d.drawString("15c", i / 39, (j / 4) * 3 + 12);
		graphics2d.drawString("30c", i / 39, j / 2 + 12);
		graphics2d.drawString("45c", i / 39, j / 4 + 12);
		
		
		for (int k1 = 0; k1 < 4; k1++)
			drawOutlineString(graphics2d, resultString[k1], k + 4, l + 15 + 15 * k1, Color.white, Color.black);

		drawOutlineString(graphics2d, stringToDraw, k + 4, l + 15, Color.yellow, Color.black);
	}
	*/
	
	//飞行图
	void redraw2D(Graphics g, int i, int j, int k, int l)
	{
		Graphics2D graphics2d = (Graphics2D)g;
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (GisXYZ.pure_Color)) : ((java.awt.Paint) (GisXYZ.anti_Color)));
		graphics2d.fillRect(0, 0, i, j);
		//graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.LIGHT_GRAY)) : ((java.awt.Paint) (Color.DARK_GRAY)));
		
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.DARK_GRAY)) : ((java.awt.Paint) (Color.LIGHT_GRAY)));
		graphics2d.drawLine(i / 4, j / 8, i / 4, j);
		graphics2d.drawLine(i / 2, j / 8, i / 2, j);
		graphics2d.drawLine((i / 4) * 3, j / 8, (i / 4) * 3, j);
		graphics2d.drawLine(0, j / 2, i, j / 2);
		graphics2d.drawLine(0, j / 4, i, j / 4);
		graphics2d.drawLine(0, (j / 4) * 3, i, (j / 4) * 3);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (GisXYZ.anti_Color)) : ((java.awt.Paint) (GisXYZ.pure_Color)));	
		if (GisXYZ.realTab == 0)
			drawUnit(graphics2d, (int)(avrdBm - 0.5D), (k + i) - 4, l + 55, 1, ChooseLang.M[401], drawchange, unitfault);
		if (GisXYZ.realTab == 0)
			drawUnit(graphics2d, (int)(amxdBm - 0.5D), (k + i) - 4, (l + 55) - 15, 1, ChooseLang.M[400], drawchange, unitfault);
		//数据展示
		double[] trandata = new double[640];
		//Random rd = new Random();
		//int Max =GisSignal.calMax_dBm;
		//int Min =GisSignal.calMin_dBm;
		//double[] rdtrandata = new double[640];
		/*
		int i2 = 0;
		for (int i1 = 0; i1 < 640; i1++)
		{
			for (int k1 = 0; k1 < 64; k1++)
				if (ae[i1][k1] > i2)
					i2 = ae[i1][k1];

		}
		
		int k2 = i2;
		if (k2 == 0)
			k2 = 1;
		i2 = 40960;
		for (int j1 = 0; j1 < 64; j1++)
		{
			for (int l1 = 639; l1 >= 0; l1--)
			{
				int j2 = (ae[l1][j1] * 4095) / k2;
				if (j2 < 614)
					j2 = 0;
				if (4095 < j2)
					j2 = 4095;
				aeds[--i2] = d3p.zColor(j2);
			}

		}

		if (aebi == null)
			aebi = new BufferedImage(640, 64, 1);
		aebi.setRGB(0, 0, 640, 64, aeds, 0, 640);
		graphics2d.setPaint(Color.black);
		graphics2d.fillRect(0, 0, i, j);
		graphics2d.drawImage(aebi, k, l, i - k * 2, j - l * 2, Color.black, null);
		*/
		for(int phase = 0; phase < 640; phase++)
		{
			//flygraphicdata[phase] = 0;
			//int rdseed = rd.nextInt(640);
			
			//graphics2d.setPaint(new Color(d3p.zColor(flygraphicdata[phase])));
			//trandata[phase] = -GisSignal.Int2dBm_NO(flygraphicdata[phase]);
			trandata[phase] = -GisSignal.Int2dBm(flygraphicdata[phase]);
			//rdtrandata[phase] = trandata[phase] - rd.nextDouble()*trandata[phase];
			
			//if(flygraphicdata[phase] >= 0 && flygraphicdata[phase] <= 4095 )
			//{
				//Color c1 = new Color(255,255,255,(int)(255.D -(achange[phase])));
				//Color c2 = new Color(0,0,0,(int)(255.D -(achange[phase])));
				//graphics2d.setPaint(Option.gt ? c1:c2);
			//}
			
			//graphics2d.fillOval((int)((i/640.0D)*phase), (int)((j/73.0D)*trandata[phase]-(j/73.0D)*7.0D), 4, 4);
			graphics2d.fillOval((int)((i/640.0D)*phase), (int)((j/(double)(GisSignal.calMax_dBm-GisSignal.calMin_dBm))*trandata[phase]-(j/(double)(GisSignal.calMax_dBm-GisSignal.calMin_dBm))*(double)(GisSignal.calMax_dBm)+5.0D), 3, 3);
			//graphics2d.fillOval((int)((i/640.0D)*(rdseed--)), (int)((-j/(double)(Max-Min))*trandata[phase]-(j/(double)(Max-Min))*(double)(Max)+j-10), 3, 3);
			//graphics2d.fillOval((int)((i/640.0D)*rdseed), (int)(-(j/(double)(Max-Min))*rdtrandata[phase]*(75.0D/rdseed)-(j/(double)(Max-Min))*(double)(Max)+j), 2, 2);
			//graphics2d.fillOval((int)((i/640.0D)*rdseed), (int)((j*(Max*Min)/(double)(Max-Min))*trandata[phase]-(j*Min/(double)(Max-Min))), 4, 4);

		}
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (GisXYZ.anti_Color)) : ((java.awt.Paint) (GisXYZ.pure_Color)));		
		graphics2d.drawString("50ms", (i / 4) + 2, j - 2);
		graphics2d.drawString("100ms", (i / 2) + 2, j - 2);
		graphics2d.drawString("150ms", (i / 4)*3 + 2, j - 2);
		drawOutlineString(graphics2d, stringToDraw, k + 4, l + 15, Color.yellow, Color.black);
	}
	
	//柱形图
	void redraw2D_Colum(Graphics g, int i, int j, int k, int l)
	{
		Graphics2D graphics2d = (Graphics2D)g;
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		
		graphics2d.setPaint(Color.black);		
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (GisXYZ.pure_Color)) : ((java.awt.Paint) (GisXYZ.anti_Color)));
		graphics2d.fillRect(0, 0, i, j);
		
		if (0 < stringToDraw2.length())
			graphics2d.drawString(stringToDraw2, i - 62, (l + j) - 5);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.DARK_GRAY)) : ((java.awt.Paint) (Color.LIGHT_GRAY)));
		graphics2d.drawLine(i / 4, j / 8, i / 4, j);
		graphics2d.drawLine(i / 2, j / 8, i / 2, j);
		graphics2d.drawLine((i / 4) * 3, j / 8, (i / 4) * 3, j);
		graphics2d.drawLine(0, j / 2, i, j / 2);
		graphics2d.drawLine(0, j / 4, i, j / 4);
		graphics2d.drawLine(0, (j / 4) * 3, i, (j / 4) * 3);
						
		for (double c=0; c<2*Math.PI; c+=0.001){
			double s3=0.0*Math.PI;
			graphics2d.drawLine((int)(c*i/Math.PI/2), (int)(j/2-(3*Math.sin(c-s3))*j/Math.PI/2),(int)((c+0.001)*i/Math.PI/2), (int)(j/2-(3*Math.sin(c+0.001-s3))*j/Math.PI/2));
		 }
		
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.LIGHT_GRAY)) : ((java.awt.Paint) (Color.DARK_GRAY)));
		
		if (GisXYZ.realTab == 0)
			drawUnit(graphics2d, (int)(avrdBm - 0.5D), (k + i) - 4, l + 55, 1, ChooseLang.M[401], drawchange, unitfault);
		if (GisXYZ.realTab == 0)
			drawUnit(graphics2d, (int)(amxdBm - 0.5D), (k + i) - 4, (l + 55) - 15, 1, ChooseLang.M[400], drawchange, unitfault);
		
		graphics2d.drawString("90'", i / 4 - 6, j / 10);
		graphics2d.drawString("180'", i / 2 - 10, j / 10);
		graphics2d.drawString("270'", (i / 4) * 3 - 10, j / 10);
		if (GisXYZ.realTab == 0)
		{
			drawUnit(graphics2d, -54, 0, (j / 4) * 3 + 12, 0, "", drawchange, unitfault);
			drawUnit(graphics2d, -43, 0, j / 2 + 12, 0, "", drawchange, unitfault);
			drawUnit(graphics2d, -31, 0, j / 4 + 12, 0, "", drawchange, unitfault);
		}
		//graphics2d.drawString("-54dBm", i / 39, (j / 4) * 3 + 12);
		//graphics2d.drawString("-43dBm", i / 39, j / 2 + 12);
		//graphics2d.drawString("-31dBm", i / 39, j / 4 + 12);
		
			//绘制柱形图
			for(int ph = 0; ph < 64; ph++)
			{
				trancoumdata[ph] = -GisSignal.Int2dBm(columdata[ph]);//转换dBm
				graphics2d.setPaint(new Color(d3p.zColor(columdata[ph])));//设置颜色
				graphics2d.setStroke(new BasicStroke((float)(i/128.0f)));
				//graphics2d.setStroke(new BasicStroke((float)(i/256.0f)));
				//graphics2d.drawLine((int)((i/64.0D)*ph+(i/128.0D)), j, (int)((i/64.0D)*ph+(i/128.0D)), (int)((j/80.0D)*trancoumdata[ph]));
				//graphics2d.drawLine((int)((i/64.0D)*ph+(i/128.0D)), j, (int)((i/64.0D)*ph+(i/128.0D)), (int)((j/60.0D)*trancoumdata[ph]-(j/60.0D)*20.0D));
				graphics2d.drawLine((int)((i/64.0D)*ph+(i/128.0D)), j, (int)((i/64.0D)*ph+(i/128.0D)), (int)((j/((double)(GisSignal.calMax_dBm - GisSignal.calMin_dBm)))*trancoumdata[ph]-(j/((double)(GisSignal.calMax_dBm - GisSignal.calMin_dBm)))*((double)(GisSignal.calMax_dBm))));
			}		
		
		for (int k1 = 0; k1 < 8; k1++)
			drawOutlineString(graphics2d, resultString[k1], k + 4, l + 15 + 15 * k1, GisXYZ.anti_Color, GisXYZ.pure_Color);

		drawOutlineString(graphics2d, stringToDraw, k + 4, l + 15, Color.yellow, Color.black);
	}
	//绘制波形图
	void redraw2D_Wave(Graphics g, int i, int j, int k, int l)
	{
		Graphics2D graphics2d = (Graphics2D)g;
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		
		graphics2d.setPaint(Color.black);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (GisXYZ.pure_Color)) : ((java.awt.Paint) (GisXYZ.anti_Color)));
		graphics2d.fillRect(0, 0, i, j);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.LIGHT_GRAY)) : ((java.awt.Paint) (Color.DARK_GRAY)));
		//绘制坐标轴
		graphics2d.drawLine((int)(11*i/150.0D), (int)(11*j/150.0D), (int)(11*i/150.0D), (int)(139*j/150.0D));//纵坐标
		graphics2d.drawLine((int)(11*i/150.0D), (int)(139*j/150.0D), (int)(139*i/150.0D), (int)(139*j/150.0D));//横坐标
		
		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int width_mV = fontmetrics.stringWidth("mV");
		int width_O = fontmetrics.stringWidth("O");
		//graphics2d.drawString("O",(int)(9*i/150.0D),(int)(146*j/150.0D));
		graphics2d.drawString("O", (int)(i/15.0D - width_O)+5, (int)(l + (j/18.0D)*17.0D+5));
		graphics2d.drawString("mV", (int)(i/15.0D - width_mV)+5, (int)(l + (j/18.0D)*2.0D+5));
		//drawUnit(graphics2d, 0, (int)(i/15.0D - width_mV), (int)(l + (j/18.0D)*2.0D), 1, "", drawchange, unitfault);
		
		int vx_arrow[] = new int[4];
		int vy_arrow[] = new int[4];
		int hx_arrow[] = new int[4];
		int hy_arrow[] = new int[4];
		
		vx_arrow[0] = (int)(11*i/150.0D);
		vx_arrow[1] = (int)(11*i/150.0D-(j/300.0D)*10.0D*Math.tan(Math.PI/12));
		vx_arrow[2] = (int)(11*i/150.0D);		
		vx_arrow[3] = (int)(11*i/150.0D+(j/300.0D)*10.0D*Math.tan(Math.PI/12));
		
		vy_arrow[0] = (int)(11*j/150.0D);
		vy_arrow[1] = (int)(11*j/150.0D+(j/300.0D)*10.0D);		
		vy_arrow[2] = (int)(11*j/150.0D+(j/300.0D)*5.0D);
		vy_arrow[3] = (int)(11*j/150.0D+(j/300.0D)*10.0D);
		
		hy_arrow[0] = (int)(139*j/150.0D);
		hy_arrow[1] = (int)(139*j/150.0D-(j/300.0D)*10.0D*Math.tan(Math.PI/12));
		hy_arrow[2] = (int)(139*j/150.0D);		
		hy_arrow[3] = (int)(139*j/150.0D+(j/300.0D)*10.0D*Math.tan(Math.PI/12));
		
		hx_arrow[0] = (int)(139*i/150.0D);
		hx_arrow[1] = (int)(139*i/150.0D-(j/300.0D)*10.0D);		
		hx_arrow[2] = (int)(139*i/150.0D-(j/300.0D)*5.0D);
		hx_arrow[3] = (int)(139*i/150.0D-(j/300.0D)*10.0D);
		
		//graphics2d.drawLine((int)(11*i/150.0D), (int)(11*j/150.0D), (int)(11*i/150.0D+(j/300.0D)*10.0D*Math.tan(Math.PI/12)), (int)(11*j/150.0D+(j/300.0D)*10.0D));
		//graphics2d.drawLine((int)(11*i/150.0D), (int)(11*j/150.0D), (int)(11*i/150.0D-(j/300.0D)*10.0D*Math.tan(Math.PI/12)), (int)(11*j/150.0D+(j/300.0D)*10.0D));
		//graphics2d.drawLine((int)(11*i/150.0D), (int)(11*j/150.0D+(j/300.0D)*5.0D), (int)(11*i/150.0D+(j/300.0D)*10.0D*Math.tan(Math.PI/12)), (int)(11*j/150.0D+(j/300.0D)*10.0D));
		//graphics2d.drawLine((int)(11*i/150.0D), (int)(11*j/150.0D+(j/300.0D)*5.0D), (int)(11*i/150.0D-(j/300.0D)*10.0D*Math.tan(Math.PI/12)), (int)(11*j/150.0D+(j/300.0D)*10.0D));
		
		graphics2d.fillPolygon(vx_arrow, vy_arrow, 4);
		graphics2d.fillPolygon(hx_arrow, hy_arrow, 4);
		
		int x[] = new int[64];//波形图横坐标
		int y[] = new int[64];//纵坐标
		for(int z = 0; z < 64; z++)
		{
			x[z] = (int)(z*(2*i/150.0D)+(11*i/150.0D));
		}
		
		for(int ph = 0; ph < 64; ph++)
		{			
			trancoumdata[ph] = -GisSignal.Int2dBm(columdata[ph]);//转换dBm									
			//y[ph] = (int)((128/150.0D)*(j/60.0D)*trancoumdata[ph]-(95/150.0D)*(j/60.0D)*20.0D);
			y[ph] = (int)((139.0D/150.0D)*(j/(double)(GisSignal.calMax_dBm - GisSignal.calMin_dBm))*trancoumdata[ph]-(95.0D/150.0D)*(j/(double)(GisSignal.calMax_dBm - GisSignal.calMin_dBm))*((double)(GisSignal.calMax_dBm)));
		}
		
		//graphics2d.setStroke(new BasicStroke((float)(i/1024.0f)));
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (GisXYZ.anti_Color)) : ((java.awt.Paint) (GisXYZ.pure_Color)));
		//graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (new Color(0x23B14D))) : ((java.awt.Paint) (new Color(0xDC4EB2))));
		graphics2d.drawPolyline(x, y, 64);//绘制折线
		if (GisXYZ.realTab == 0)
			drawUnit(graphics2d, (int)(avrdBm - 0.5D), (k + i) - 4, l + 55, 1, ChooseLang.M[401], drawchange, unitfault);
		if (GisXYZ.realTab == 0)
			drawUnit(graphics2d, (int)(amxdBm - 0.5D), (k + i) - 4, (l + 55) - 15, 1, ChooseLang.M[400], drawchange, unitfault);
		drawOutlineString(graphics2d, stringToDraw, k + 4, l + 15, Color.yellow, Color.black);
	}
	
	//连续图
	void redraw2DCon(Graphics g, int i, int j, int k, int l)
	{
		Graphics2D graphics2d = (Graphics2D)g;
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		
		graphics2d.setPaint(Color.black);
		
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.BLACK)) : ((java.awt.Paint) (Color.WHITE)));
		graphics2d.fillRect(0, 0, i, j);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.DARK_GRAY)) : ((java.awt.Paint) (Color.LIGHT_GRAY)));
		graphics2d.setStroke(new BasicStroke((float)(2.0f)));
		graphics2d.drawLine((int)(i/80.0D)*3, (int)(j/18.0D)*3, (int)(i/80.0D)*3, (int)(j/18.0D)*17);//垂直线
		//graphics2d.drawLine(i/8, (j/9)*8, (i/8)*7, (j/9)*8);//水平线
		//graphics2d.drawLine(i/8, (j/9)*6, (i/8)*7, (j/9)*6);//水平线
		for(int b = 1; b < 5; b++)
		{
			graphics2d.drawLine((int)(i/80.0D)*3, (int)((j/18.0D)*4.0D*b), (int)(i/80.0D)*77, (int)((j/18.0D)*4.0D*b));//水平线
		}
		/*
		for(int a = 2; a < 8; a++)
		{
			graphics2d.drawLine((i/8)*a, (int)((j/72.0D)*63.5D), (i/8)*a, (int)((j/72.0D)*65.0D));//刻度线
		}
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.WHITE)) : ((java.awt.Paint) (Color.BLACK)));		
		graphics2d.drawString("0",  (int)((i/8.0f)*1.0f-5.0f), (int)((j/18.0f)*17.0f+5.0f));//标度
		graphics2d.drawString("10", (int)((i/8.0f)*2.0f-5.0f), (int)((j/18.0f)*17.0f+5.0f));//标度
		graphics2d.drawString("20", (int)((i/8.0f)*3.0f-5.0f), (int)((j/18.0f)*17.0f+5.0f));//标度
		graphics2d.drawString("30", (int)((i/8.0f)*4.0f-5.0f), (int)((j/18.0f)*17.0f+5.0f));//标度
		graphics2d.drawString("40", (int)((i/8.0f)*5.0f-5.0f), (int)((j/18.0f)*17.0f+5.0f));//标度
		graphics2d.drawString("50", (int)((i/8.0f)*6.0f-5.0f), (int)((j/18.0f)*17.0f+5.0f));//标度
		graphics2d.drawString("60/dB", (int)((i/8.0f)*7.0f-5.0f), (int)((j/18.0f)*17.0f+5.0f));//标度
		*/
		//graphics2d.drawString("dB", (i/400.0F)*390.0F-20.0F, (j/18)*17-6.0f);//单位
		
		//dBm2dB
		/*
		int avr = (int)((i/80)*(avrdBm/0.9 + 80/0.9));//有效值=平均值/0.9
		int max = (int)((i/80)*(amxdBm + 80));//峰值
		int f1 = (int)((i/80)*(avrdBm + 80));//频率成份1=平均值
		int f2 = (int)((i/80)*(f2dBm + 80));//频率成份2
		*/
		int Ratio = GisSignal.calMax_dBm - GisSignal.calMin_dBm + 20;
		int eff = (int)((i/(Ratio))*((effdBm - GisSignal.calMin_dBm)));//有效值=平均值/0.9
		//int avr = (int)((i/Ratio)*((effdBm - GisSignal.calMin_dBm)));//有效值=平均值/0.9
		int max = (int)((i/Ratio)*(amxdBm - GisSignal.calMin_dBm));//峰值
		//int f1 = (int)((i/Ratio)*(avrdBm - GisSignal.calMin_dBm));//频率成份1=平均值
		int f1 = (int)((i/Ratio)*(f1dBm - GisSignal.calMin_dBm));//频率成份1
		int f2 = (int)((i/Ratio)*(f2dBm - GisSignal.calMin_dBm));//频率成份2
		
		/*
		//dBm2mV		
		int avr = (int)((i/80.0D)*((avrdBm/0.9 + 80.0D)));//有效值=平均值/0.9
		int max = (int)((i/80.0D)*(amxdBm + 80.0D));//峰值
		int f1 = (int)((i/80.0D)*(avrdBm + 80.0D));//频率成份1=平均值
		int f2 = (int)((i/80.0D)*(f2dBm + 80.0D));//频率成份2
		*/
		
		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int i40 = fontmetrics.stringWidth(ChooseLang.M[40]);
		int i41 = fontmetrics.stringWidth(ChooseLang.M[41]);
		int i42 = fontmetrics.stringWidth(ChooseLang.M[42]);
		int i43 = fontmetrics.stringWidth(ChooseLang.M[43]);
		
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.WHITE)) : ((java.awt.Paint) (Color.BLACK)));
		graphics2d.drawString(ChooseLang.M[40], (i/2 - i40) + 50, (int)(l + (j/18.0D)*2.0D) - 5);
		graphics2d.drawString(ChooseLang.M[41], (i/2 - i41) + 50, (int)(l + (j/18.0D)*6.0D) - 5);
		graphics2d.drawString(ChooseLang.M[42], (i/2 - i42) + 50, (int)(l + (j/18.0D)*10.0D) - 5);
		graphics2d.drawString(ChooseLang.M[43], (i/2 - i43) + 50, (int)(l + (j/18.0D)*14.0D) - 5);
		/*
		graphics2d.setPaint(new Color(d3p.zColor(GisSignal.dBm2Int(avrdBm))));
		graphics2d.fillRect(i/8, (j/9)*1, avr, (j/18)*2);
		drawUnit(graphics2d, (int)((avrdBm + 80)/0.9 - 0.5D), (k + i/2) + 110, (l + (j/18)*2) - 5, 1, "");		
		graphics2d.setPaint(new Color(d3p.zColor(GisSignal.dBm2Int(amxdBm))));
		graphics2d.fillRect(i/8, (j/9)*3, max, (j/18)*2);
		drawUnit(graphics2d, (int)(amxdBm + 80.0D - 0.5D), (k + i/2) + 110, (l + (j/18)*6) - 5, 1, "");
		graphics2d.setPaint(new Color(d3p.zColor(GisSignal.dBm2Int(avrdBm))));
		graphics2d.fillRect(i/8, (j/9)*5, f1, (j/18)*2);
		drawUnit(graphics2d, (int)(avrdBm + 80.0D  - 0.5D), (k + i/2) + 110, (l + (j/18)*10) - 5, 1, "");
		graphics2d.setPaint(new Color(d3p.zColor(GisSignal.dBm2Int(f2dBm))));
		graphics2d.fillRect(i/8, (j/9)*7, f2, (j/18)*2);
		drawUnit(graphics2d, (int)(f2dBm + 80.0D - 0.5D), (k + i/2) + 110, (l + (j/18)*14) - 5, 1, "");
		drawOutlineString(graphics2d, stringToDraw, 4, 15, Color.yellow, Color.black);
		*/		
		
		drawUnitxAz(graphics2d, (int)(effdBm), (k + i/2) + 110, (int)(l + (j/18.0D)*2.0D) - 5, 1, "", drawAz, Azfault);
		drawUnitxAz(graphics2d, (int)(amxdBm), (k + i/2) + 110, (int)(l + (j/18.0D)*6.0D) - 5, 1, "", drawAz, Azfault);
		drawUnitxAz(graphics2d, (int)(f1dBm), (k + i/2) + 110, (int)(l + (j/18.0D)*10.0D) - 5, 1, "", drawAz, Azfault);
		drawUnitxAz(graphics2d, (int)(f2dBm), (k + i/2) + 110, (int)(l + (j/18.0D)*14.0D) - 5, 1, "", drawAz, Azfault);
		
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.WHITE)) : ((java.awt.Paint) (Color.BLACK)));
		//graphics2d.setPaint(new Color(d3p.zColor_Ultral((GisSignal.dBm2Int(avrdBm)))));
		graphics2d.fillRect((int)((i/80.0D)*3+0.5D), (int)((j/18.0D)*3.0D), eff, (int)(j/18.0D)*1);
		//drawUnit(graphics2d, (int)((avrdBm)/0.9), (k + i/2) + 110, (l + (j/18)*2) - 5, 1, "");
		
		//graphics2d.setPaint(new Color(d3p.zColor_Ultral(GisSignal.dBm2Int(amxdBm))));
		graphics2d.fillRect((int)((i/80.0D)*3+0.5D), (int)((j/18.0D)*7.0D), max, (int)(j/18.0D)*1);
		//drawUnit(graphics2d, (int)(amxdBm), (k + i/2) + 110, (l + (j/18)*6) - 5, 1, "");
		
		//graphics2d.setPaint(new Color(d3p.zColor_Ultral(GisSignal.dBm2Int(avrdBm))));
		graphics2d.fillRect((int)((i/80.0D)*3+0.5D), (int)((j/18.0D)*11.0D), f1, (int)(j/18.0D)*1);
		//drawUnit(graphics2d, (int)(avrdBm), (k + i/2) + 110, (l + (j/18)*10) - 5, 1, "");
		
		//graphics2d.setPaint(new Color(d3p.zColor_Ultral(GisSignal.dBm2Int(f2dBm))));
		graphics2d.fillRect((int)((i/80.0D)*3+0.5D), (int)((j/18.0D)*15.0D), f2, (int)(j/18.0D)*1);
		//drawUnit(graphics2d, (int)(f2dBm), (k + i/2) + 110, (l + (j/18)*14) - 5, 1, "");
		
		drawOutlineString(graphics2d, stringToDraw, 4, 15, Color.yellow, Color.black);
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
			g.setColor(Option.gt ? GisXYZ.pure_Color : GisXYZ.anti_Color);
			g.setXORMode(Option.gt ? GisXYZ.anti_Color : GisXYZ.pure_Color);
			g.fillRect(l, i1, j, k);
		}
		g.dispose();
		return 0;
	}

	public void draw(Graphics g, int i, int j, int k, int l)
	{
		if (draw3D)
		{
			setAngle(i, j, k, l);
			redraw3D((Graphics2D)(Graphics2D)g, i, j, k, l);
			oldw = i;
			oldh = j;
		} else
		{
			if(isflygraphic)
			{
				redraw2D(g, i, j, k, l);
			}
			else
			{
				if(Waveform)
				{
					redraw2D_Wave(g,i,j,k,l);//绘制波形图
				}
				if(isCon)
				{
					redraw2DCon(g,i,j,k,l);//绘制连续图
				}
				else
					redraw2D_Colum(g, i, j, k, l);//绘制柱形图
			}
			
		}
	}

}
