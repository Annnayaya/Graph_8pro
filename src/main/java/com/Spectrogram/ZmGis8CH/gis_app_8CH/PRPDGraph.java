package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import javax.swing.JComponent;
import javax.swing.Timer;

class PRPDGraph extends JComponent implements Printable
{
	private static final long serialVersionUID = 197307030003L;
	D3JPanel d3p;
	Boolean printBlackWhite;
	int prpd[][];
	int phaseNum;
	int cycleNum;
	boolean directRefresh;
	int oldw;
	int oldh;
	boolean prpdrefresh;
	VolatileImage img;
	boolean draw3D;
	boolean draw2D;
	boolean drawCon;
	boolean drawUltral;
	boolean Waveform;//波形图
	boolean drawColum;//柱状图
	BufferedImage bi;
	int ds[];
	double oldrDeg;
	double oldtDeg;
	Graphics2D vg;
	public boolean drawing;
	public boolean refreshed;
	int phaseDelay;
	double rdeg;
	double tdeg;
	int blink;
	String resultString;
	String resultStringprpd[] = {"", "", "", ""};
	String stringToDraw;
	String stringToDraw2;
	String stringTrgDraw;//触发字符串
	public static boolean negativePrint = false;
	GisGraph drawunit;
	double avrdBm;
	double amxdBm;
	double f1dBm;
	double f2dBm;
	private volatile boolean persistentPeakMode = false;
	private volatile boolean persistentPeakReady = false;
	private volatile double persistentPeakDbm = Double.NaN;
	private boolean isAlarming = false;

	public void setAlarming(boolean alarming) {
		this.isAlarming = alarming;
	}

	public boolean isAlarming() {
		return isAlarming;
	}

	public void applyPersistentPeak(PersistentPeakSnapshot snapshot, int channel) {
		if (snapshot == null) {
			this.persistentPeakReady = false;
			this.persistentPeakDbm = Double.NaN;
			return;
		}
		this.persistentPeakReady = snapshot.isValid(channel) && !Double.isNaN(snapshot.getPublishedDbm(channel));
		this.persistentPeakDbm = snapshot.getPublishedDbm(channel);
	}

	public void clearPersistentPeak() {
		this.persistentPeakReady = false;
		this.persistentPeakDbm = Double.NaN;
	}

	public void setPersistentPeakMode(boolean mode) {
		this.persistentPeakMode = mode;
	}

	public boolean isPersistentPeakMode() {
		return persistentPeakMode;
	}

	public double getDisplayedPeakDbm() {
		boolean usePersistentPeak = (persistentPeakMode || AppGlobalConfig.isSpectrogramPeakFollowFiltered())
				&& AppGlobalConfig.isPersistentPeakEnabled();
		if (usePersistentPeak && persistentPeakReady && !Double.isNaN(persistentPeakDbm)) {
			return persistentPeakDbm;
		}
		return amxdBm;
	}
	int Countprpd;//放电次数
	int cycleSum32[];
	int cycleMax[];
	int cyclef1Sum[];
	int cyclef2Sum[];
	int ultralpdata[];
	double trandata_ultral[];
	int ultral[];
	int columdata[];//绘制柱状图数据
	double trancoumdata[];//转换数据
	final static int n = 10000;
	double EX;
	double EY;
	int movestep;
	float sycfreq;//同步频率
	Timer time;
	
	boolean drawchange = false;//改变单位
	int unitfault = 0;//默认单位
	

	public PRPDGraph(int i,int j)
	{
		this(i, j, null, null);
	}
	
	public PRPDGraph(int i, int j, int ai[][], int ai1[])
	{
		d3p = new D3JPanel();
		printBlackWhite = Boolean.valueOf(false);
		phaseNum = 64;
		cycleNum = 64;
		directRefresh = false;
		oldw = 0;
		oldh = 0;
		prpdrefresh = true;
		draw3D = false;
		draw2D = true;
		drawCon =false;
		drawUltral = false;
		Waveform = false;
		drawColum = false;
		bi = null;
		ds = new int[4096];
		oldrDeg = 0.0D;
		oldtDeg = 0.0D;
		drawing = false;
		refreshed = true;
		phaseDelay = 0;
		rdeg = 2.3561944901923448D;
		tdeg = 0.53407075111026492D;
		blink = 0;
		resultString = "";
		stringToDraw = ChooseLang.M[132];
		stringTrgDraw = "";
		stringToDraw2 = "";
		prpd = ai;
		ultral = ai1;
		phaseNum = i;
		cycleNum = j;
		
		avrdBm = GisSignal.calMin_dBm;
		amxdBm = GisSignal.calMin_dBm;
		f2dBm = GisSignal.calMin_dBm;
		Countprpd = 0;
		cycleSum32 = new int[80];
		cycleMax = new int[80];
		cyclef1Sum = new int[80];
		cyclef2Sum = new int[80];
		ultralpdata = new int[320];
		trandata_ultral = new double[320];
		columdata = new int[64];//柱状图数据
		trancoumdata = new double[64];//转换柱状图数据	
		EX = 0.0D;
		EY = 0.0D;
		movestep = 0;
		sycfreq=50.0F;//初始50Hz;
		
		setPreferredSize(new Dimension(200, 200));
		setAngle(getWidth(), getHeight(), 0, 0);
		addMouseMotionListener(new MouseMotionListener() {

			int oldx;
			int oldy;
			//int cnt;

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
				GisGUI.count = 0;
			}
		});
		addMouseListener(new MouseListener() {


			public void mouseClicked(MouseEvent mouseevent)
			{
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
	/*
	ActionListener taskPerformer = new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent e) {				
			movestep++;	
			if(movestep >= 320)
				movestep = 0;
		}			
	};
	time =new Timer(25, taskPerformer);//正弦波移动定时器
	//time =new Timer(500, taskPerformer);//测试正弦波移动定时器
	 */
	public void paint(Graphics g)
	{
		int i = getWidth();
		int j = getHeight();
		if (oldh != j || oldw != i || rdeg != oldrDeg || tdeg != oldtDeg)
		{
			img = createVolatileImage(i, j);
			makeDrawable();
			prpdrefresh = true;
			oldw = i;
			oldh = j;
			oldrDeg = rdeg;
			oldtDeg = tdeg;
		}
		if (prpdrefresh)
		{
			if (draw3D)
			{
				redraw3D((Graphics2D)(Graphics2D)img.getGraphics());
			}
			if(draw2D)
			{
				redraw2D(img.getGraphics());
			}
			if(drawCon)
			{
				redraw2DCon(img.getGraphics());
			}
			if(drawUltral)
			{
				redraw2DUltral(img.getGraphics());
			}
			if(Waveform)
			{
				redraw2D_Wave(img.getGraphics());
			}
			if(drawColum)
			{
				redraw2D_Colum(img.getGraphics());
			}
		}
			
		g.drawImage(img, 0, 0, this);
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

	private void drawUnit(Graphics2D graphics2d, int i, int j, int k, int l, String s)
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
	
	private void drawUnit(Graphics2D graphics2d, int i, int j, int k, int l, String s, boolean change, int unit)
	{
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
	
	private void drawdBm(Graphics2D graphics2d, int i, int j, int k, int l, String s)
	{
		
		String s1= (new StringBuilder()).append(s).append(String.format("%ddBm", new Object[] {
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
		double d = 100.0D * Math.sqrt(5.0D * Math.pow(10.0D, i*1.0D/10.0D));
		String s1 = (new StringBuilder()).append(s).append(String.format("%1.2fmV", new Object[] {
			Double.valueOf(d)
		})).toString();
		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int i1 = fontmetrics.stringWidth(s1);
		graphics2d.drawString(s1, l != 0 ? j - i1 : j, k);
	}

	private void drawpC(Graphics2D graphics2d, int i, int j, int k, int l, String s)
	{
		double d = Math.pow(1.1795D, 61 + i) + 3D;
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
		double d1 = 100 * Math.sqrt(5 * Math.pow(10D, (double)i/10));
		double d = 20*Math.log10(d1);
		//int j1;
		//for (j1 = 0; d < 1.0D; j1--)
			//d *= 10D;

		String s1 = (new StringBuilder()).append(s).append(String.format("%1.2fdBmV", new Object[] {
			Double.valueOf(d)
		})).toString();
		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int i1 = fontmetrics.stringWidth(s1);
		graphics2d.drawString(s1, l != 0 ? j - i1 : j, k);
		//graphics2d.drawString((new StringBuilder()).append("").append(j1).toString(), l != 0 ? j - 26 : (j + i1) - 26, k - 8);
	}

	private void drawUnitString(Graphics2D graphics2d, String s1, int j, int k, int l)
	{
		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int i1 = fontmetrics.stringWidth(s1);
		graphics2d.drawString(s1, l != 0 ? j - i1 : j, k);
	}

	private void drawUnitDouble(Graphics2D graphics2d, double val, int j, int k, int l, String s, boolean change, int unit)
	{
		if (Double.isNaN(val)) {
			drawUnitString(graphics2d, s + "--", j, k, l);
			return;
		}
		boolean isDbm = (change && Option.unitType == 0) || (!change && unit == 0);
		if (isDbm) {
			String s1 = s + PeakValueFormatter.formatForUi(val) + "dBm";
			drawUnitString(graphics2d, s1, j, k, l);
		} else {
			drawUnit(graphics2d, (int)(val - 0.5D), j, k, l, s, change, unit);
		}
	}
	
	void redraw2D(Graphics g)
	{
		redraw2D(g, getWidth(), getHeight(), 0, 0);
	}
	//连续图
	void redraw2DCon(Graphics g)
	{
		redraw2DCon(g, getWidth(), getHeight(), 0, 0);
	}
	//超声波
	void redraw2DUltral(Graphics g)
	{
		redraw2DUltral(g, getWidth(), getHeight(), 0, 0);
	}
	//波形图
	void redraw2D_Wave(Graphics g)
	{
		redraw2D_Wave(g, getWidth(), getHeight(), 0, 0);
	}
	
	//柱状图
	void redraw2D_Colum(Graphics g)
	{
		redraw2D_Colum(g, getWidth(), getHeight(), 0, 0);
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
		if (isAlarming && s != null && s.equals(stringToDraw)) {
			graphics2d.setPaint(Color.BLACK);
			graphics2d.drawString("【幅值报警】", i - 1, j + 15);
			graphics2d.drawString("【幅值报警】", i + 1, j + 15);
			graphics2d.drawString("【幅值报警】", i, j + 15 - 1);
			graphics2d.drawString("【幅值报警】", i, j + 15 + 1);
			graphics2d.setPaint(Color.RED);
			graphics2d.drawString("【幅值报警】", i, j + 15);
		}
	}
	
	public double dBm2mV(double i)
	{
		double d = 100 * Math.sqrt(5 * Math.pow(10D, i/10));
		return d;

	}
	void redraw2D(Graphics g, int i, int j, int k, int l)
	{
		Graphics2D graphics2d = (Graphics2D)g;
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		int i2 = 0;
		for (int i1 = 0; i1 < 64; i1++)
		{
			for (int k1 = 0; k1 < 64; k1++)
				if (prpd[i1][k1] > i2)
					i2 = prpd[i1][k1];

		}

		int k2 = i2;
		if (k2 == 0)
			k2 = 1;
		i2 = 4096;
		for (int j1 = 0; j1 < 64; j1++)
		{
			for (int l1 = 63; l1 >= 0; l1--)
			{
				int j2 = (prpd[l1][j1] * 4095) / k2;
				//if (j2 < 0)
					//j2 = 0;
				if (j2 < 614)
					j2 = 0;
				
				//if (j2 < 2048)
					//j2 = 0;
				
				if (4095 < j2)
					j2 = 4095;
				ds[--i2] = d3p.zColor(j2);
			}

		}

		if (bi == null)
			bi = new BufferedImage(64, 64, 1);
			//bi = new BufferedImage(128, 128, 1);
		bi.setRGB(0, 0, 64, 64, ds, 0, 64);
		graphics2d.setPaint(Color.black);
		graphics2d.fillRect(0, 0, i, j);
		graphics2d.drawImage(bi, k, l, i - k * 2, j - l * 2, Color.black, null);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.DARK_GRAY)) : ((java.awt.Paint) (Color.LIGHT_GRAY)));
		graphics2d.drawLine(i / 4, 0, i / 4, j - j / 15);
		graphics2d.drawLine(i / 2, 0, i / 2, j - j / 15);
		graphics2d.drawLine((i / 4) * 3, 0, (i / 4) * 3, j - j / 15);
		graphics2d.drawLine(0, j / 2, i, j / 2);
		graphics2d.drawLine(0, j / 4, i, j / 4);
		graphics2d.drawLine(0, (j / 4) * 3, i, (j / 4) * 3);
		//������������

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
		graphics2d.drawString("90'", i / 4 - 6, j - j / 40);
		graphics2d.drawString("180'", i / 2 - 10, j - j / 40);
		graphics2d.drawString("270'", (i / 4) * 3 - 10, j - j / 40);
		graphics2d.drawString("-54dBm", i / 39, (j / 4) * 3 + 12);
		graphics2d.drawString("-43dBm", i / 39, j / 2 + 12);
		graphics2d.drawString("-31dBm", i / 39, j / 4 + 12);
		drawOutlineString(graphics2d, stringToDraw, 4, 15, Color.yellow, Color.black);
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
		int avr = (int)((i/80)*((avrdBm + 80)/0.9D));//有效值=平均值/0.9
		int max = (int)((i/80)*(getDisplayedPeakDbm() + 80));//峰值
		int f1 = (int)((i/80)*(f1dBm + 80));//频率成份1=平均值
		int f2 = (int)((i/80)*(f2dBm + 80));//频率成份2
		
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
		graphics2d.drawString(ChooseLang.M[40], (i/2 - i40) + 60, (int)(l + (j/18.0D)*2.0D) - 5);
		graphics2d.drawString(ChooseLang.M[41], (i/2 - i41) + 60, (int)(l + (j/18.0D)*6.0D) - 5);
		graphics2d.drawString(ChooseLang.M[42], (i/2 - i42) + 60, (int)(l + (j/18.0D)*10.0D) - 5);
		graphics2d.drawString(ChooseLang.M[43], (i/2 - i43) + 60, (int)(l + (j/18.0D)*14.0D) - 5);
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
		drawUnit(graphics2d, (int)(avrdBm), (k + i/2) + 110, (int)(l + (j/18.0D)*2.0D) - 5, 1, "");
		drawUnit(graphics2d, (int)(getDisplayedPeakDbm()), (k + i/2) + 110, (int)(l + (j/18.0D)*6.0D) - 5, 1, "");
		drawUnit(graphics2d, (int)(f1dBm), (k + i/2) + 110, (int)(l + (j/18.0D)*10.0D) - 5, 1, "");
		drawUnit(graphics2d, (int)(f2dBm), (k + i/2) + 110, (int)(l + (j/18.0D)*14.0D) - 5, 1, "");
		
		graphics2d.setPaint(new Color(d3p.zColor_Ultral((GisSignal.dBm2Int(avrdBm)))));
		graphics2d.fillRect((int)(i/80.0D)*3, (int)((j/18.0D)*3.0D), avr, (int)(j/18.0D)*1);
		//drawUnit(graphics2d, (int)((avrdBm)/0.9), (k + i/2) + 110, (l + (j/18)*2) - 5, 1, "");
		
		graphics2d.setPaint(new Color(d3p.zColor_Ultral(GisSignal.dBm2Int(getDisplayedPeakDbm()))));
		graphics2d.fillRect((int)(i/80.0D)*3, (int)((j/18.0D)*7.0D), max, (int)(j/18.0D)*1);
		//drawUnit(graphics2d, (int)(amxdBm), (k + i/2) + 110, (l + (j/18)*6) - 5, 1, "");
		
		graphics2d.setPaint(new Color(d3p.zColor_Ultral(GisSignal.dBm2Int(avrdBm))));
		graphics2d.fillRect((int)(i/80.0D)*3, (int)((j/18.0D)*11.0D), f1, (int)(j/18.0D)*1);
		//drawUnit(graphics2d, (int)(avrdBm), (k + i/2) + 110, (l + (j/18)*10) - 5, 1, "");
		
		graphics2d.setPaint(new Color(d3p.zColor_Ultral(GisSignal.dBm2Int(f2dBm))));
		graphics2d.fillRect((int)(i/80.0D)*3, (int)((j/18.0D)*15.0D), f2, (int)(j/18.0D)*1);
		//drawUnit(graphics2d, (int)(f2dBm), (k + i/2) + 110, (l + (j/18)*14) - 5, 1, "");
		
		drawOutlineString(graphics2d, stringToDraw, 4, 15, Color.yellow, Color.black);
	}
	//超声波
	void redraw2DUltral(Graphics g, int i, int j, int k, int l)
	{
		Graphics2D graphics2d = (Graphics2D)g;
		float kfreq = sycfreq/50.0f;//同步系数
		//float kfreq = 0.5f;//测试同步系数
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		
		graphics2d.setPaint(Color.black);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.BLACK)) : ((java.awt.Paint) (Color.WHITE)));
		graphics2d.fillRect(0, 0, i, j);
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.WHITE)) : ((java.awt.Paint) (Color.BLACK)));
		/*
		graphics2d.drawString("22.36 mV",(int)((i/200.0D)*0.0D),(int)((j/140.0D)*10.0D));//刻度
		graphics2d.drawString("0.224 mV",(int)((i/200.0D)*0.0D),(int)((j/140.0D)*30.0D));
		graphics2d.drawString("0.002 mV",(int)((i/200.0D)*0.0D),(int)((j/140.0D)*50.0D));
		graphics2d.drawString("0",(int)((i/200.0D)*10.0D),(int)((j/140.0D)*75.0D));
		graphics2d.drawString("-0.002 mV",(int)((i/200.0D)*0.0D),(int)((j/140.0D)*90.0D));
		graphics2d.drawString("-0.224 mV",(int)((i/200.0D)*0.0D),(int)((j/140.0D)*110.0D));
		graphics2d.drawString("-22.36 mV",(int)((i/200.0D)*0.0D),(int)((j/140.0D)*130.0D));
		*/
		graphics2d.drawString("105 mV",(int)((i/200.0D)*0.0D),(int)((j/140.0D)*10.0D));//刻度
		graphics2d.drawString("70 mV",(int)((i/200.0D)*0.0D),(int)((j/140.0D)*30.0D));
		graphics2d.drawString("35 mV",(int)((i/200.0D)*0.0D),(int)((j/140.0D)*50.0D));
		graphics2d.drawString("0",(int)((i/200.0D)*10.0D),(int)((j/140.0D)*75.0D));
		graphics2d.drawString("-35 mV",(int)((i/200.0D)*0.0D),(int)((j/140.0D)*90.0D));
		graphics2d.drawString("-70 mV",(int)((i/200.0D)*0.0D),(int)((j/140.0D)*110.0D));
		if (persistentPeakMode)
		{
			if (persistentPeakReady && !Double.isNaN(persistentPeakDbm))
			{
				drawUnitDouble(graphics2d, persistentPeakDbm, (k + i) - 4, l + 20, 1, ChooseLang.M[400], false, 0);
			} else
			{
				drawUnitString(graphics2d, ChooseLang.M[400] + "--", (k + i) - 4, l + 20, 1);
			}
		} else
		{
			drawUnit(graphics2d, (int)(amxdBm - 0.5D), (k + i) - 4, l + 20, 1, ChooseLang.M[400]);
		}		
		
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.DARK_GRAY)) : ((java.awt.Paint) (Color.LIGHT_GRAY)));
								
		for(int x = 0; x < 7; x++)
		{
			graphics2d.drawLine((int)((i/200.0D)*15.0D),(int)((j/140.0D)*(x*20.0D+10.0D)),(int)((i/200.0D)*14.0D),(int)((j/140.0D)*(x*20.0D+10.0D)));//单位刻度
		}		
						
		//绘制正弦曲线
		EY=((j/140.0D)*120)/2; 
		//int EX=(int)(((i/200.0D)*680)/32/Math.PI);//周期及频率		
		EX=((i/200.0D)*2720)/128/kfreq/Math.PI;//周期及频率
		double h1=128.0*Math.PI/n; //绘图精度及周期
		double x1=-64*Math.PI;//绘图范围[-64pi,64pi]
		double y1=Math.sin(x1),x2,y2;		
		for (int z = 0; z < n; z++)
		{
			
			x2=x1+h1; 
			y2=Math.sin(x2); 
			//graphics2d.drawLine((int)((x1*EX)+(i/200)*15),(int)((j/140.0)*70.0-(int)(EY*y1)),(int)((x2*EX)+(i/200)*15),(int)((j/140.0D)*70.0D-(int)(y2*EY)));
			graphics2d.drawLine((int)((x1*EX)+(i/200.0D)*15.0D+((((i/200.0D)*170.0D)/256.0D)*movestep/kfreq)),(int)((j/140.0D)*70.0D-(int)(EY*y1)),(int)((x2*EX)+(i/200.0D)*15.0D+((((i/200.0D)*170.0D)/256.0D)*movestep/kfreq)),(int)((j/140.0D)*70.0D-(y2*EY)));						
			//graphics2d.drawLine((int)((x1*EX)+(i/200.0D)*15.0D+((((i/200.0D)*170.0D)/256)*0)),(int)((j/140.0D)*70.0D-(int)(EY*y1)),(int)((x2*EX)+(i/200.0D)*15.0D+((((i/200.0D)*170.0D)/256)*0)),(int)((j/140.0D)*70.0D-(y2*EY)));
			x1=x2; 
			y1=y2; 
		}
		
		//数据展示		
		for(int phase = 0; phase < 320; phase++)
		{			
			//graphics2d.setStroke(new BasicStroke((i/200)*1.0f));
			graphics2d.setPaint(new Color(d3p.zColor_Ultral(ultralpdata[phase])));
			trandata_ultral[phase] = -GisSignal.Int2dBm_NO(ultralpdata[phase]);
			//trandata_ultral[phase] =-( (double)(ultralpdata[phase] * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double)GisSignal.calMin_dBm);
			//int xstart=(int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D);
			//int xend=(int)(((((i/200.0D)*170.0D)/256.0D)*(phase+1)/kfreq) + (i/200.0D)*15.0D);
			//int ystart=(int)(j*(phase%2)*1.0D+((j/140.0D)*trandata[phase] - 10.0D*(j/140.0D)));
			//int yend=(int)(j*((phase+1)%2)*1.0D-((j/140.0D)*trandata[phase+1] - 10.0D*(j/140.0D)));
			//正半轴
			//(-80dBm,-20dBm)~~(0.002mV,22.36mV)
			//graphics2d.drawLine((int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)((j/140.0D)*70.0D), (int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)(j*0.0D+((j/140.0D)*trandata[phase] - 10.0D*(j/140.0D))));
			//(-80dBm,-7dBm)~~(0.002mV,100mV)
			//graphics2d.drawLine((int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)((j/166.0D)*83.0D - 1.0D), (int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)(j*0.0D+((j/166.0D)*trandata[phase] + 3.0D*(j/166.0D))));
			graphics2d.drawLine((int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)((j/166.0D)*83.0D), (int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)(j*0.0D+((j/166.0D)*trandata_ultral[phase] + 3.0D*(j/166.0D))));
			//graphics2d.drawLine(xstart,ystart ,xend ,yend );
			//负半轴
			//(-80dBm,-20dBm)~~(0.002mV,22.36mV)
			//graphics2d.drawLine((int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)((j/140.0D)*70.0D), (int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)(j*1.0D-((j/140.0D)*trandata[phase] - 10.0D*(j/140.0D))));
			//(-80dBm,-7dBm)~~(0.002mV,100mV)
			//graphics2d.drawLine((int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)((j/166.0D)*83.0D + 1.0D), (int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)(j*1.0D-((j/166.0D)*trandata[phase] + 3.0D*(j/166.0D))));
			graphics2d.drawLine((int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)((j/166.0D)*83.0D), (int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)(j*1.0D-((j/166.0D)*trandata_ultral[phase] + 3.0D*(j/166.0D))));
		}
		
		/*
		for(int phase = 0; phase < 320; phase++)
		{			
			//graphics2d.setStroke(new BasicStroke((i/200)*1.0f));
			graphics2d.setPaint(new Color(d3p.zColor(ultralpdata[phase])));
			trandata[phase] = -GisSignal.Int2dBm(ultralpdata[phase]);
			//正半轴
			//graphics2d.drawLine((int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)((j/140.0D)*70.0D - 1.0D), (int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)((j/140.0D)*trandata[phase] - 10.0D*(j/140.0D)));
			//负半轴
			//graphics2d.drawLine((int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)((j/140.0D)*70.0D + 1.0D), (int)(((((i/200.0D)*170.0D)/256.0D)*phase/kfreq) + (i/200.0D)*15.0D), (int)(-(j/140.0D)*trandata[phase] + 150.0D*(j/140.0D)));
		}
		*/			
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.DARK_GRAY)) : ((java.awt.Paint) (Color.LIGHT_GRAY)));
		graphics2d.drawLine((int)((i/200.0D)*15.0D), (int)((j/140.0D)*0.0D), (int)((i/200.0D)*15.0D), (int)((j/140.0D)*140.0D));//纵轴
		//graphics2d.drawLine((int)((i/200.0D)*185.0D), (int)((j/140.0D)*0.0D), (int)((i/200.0D)*185.0D), (int)((j/140.0D)*140.0D));//纵轴测试
		graphics2d.drawLine((int)((i/200.0D)*0.0D), (int)((j/140.0D)*70.0D), (int)((i/200.0D)*200.0D), (int)((j/140.0D)*70.0D));//横轴
		drawOutlineString(graphics2d, stringToDraw, 4, 15, Color.yellow, Color.black);//通道标题
	}
	//波形图
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
		graphics2d.drawLine((int)(11*i/150.0D), (int)(139*j/150.0D), (int)(143*i/150.0D), (int)(139*j/150.0D));//横坐标
		
		graphics2d.drawLine((int)(43*i/150.0D), (int)(138*j/150.0D), (int)(43*i/150.0D), (int)(140*j/150.0D));//横坐标
		graphics2d.drawLine((int)(75*i/150.0D), (int)(138*j/150.0D), (int)(75*i/150.0D), (int)(140*j/150.0D));//横坐标
		graphics2d.drawLine((int)(107*i/150.0D), (int)(138*j/150.0D), (int)(107*i/150.0D), (int)(140*j/150.0D));//横坐标
		
		graphics2d.drawString("90'", (int)(42*i/150.0D), (int)(146*j/150.0D));
		graphics2d.drawString("180'", (int)(74*i/150.0D), (int)(146*j/150.0D));
		graphics2d.drawString("270'", (int)(106*i/150.0D), (int)(146*j/150.0D));
		
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
		//垂直箭头
		vx_arrow[0] = (int)(11*i/150.0D);
		vx_arrow[1] = (int)(11*i/150.0D-(j/300.0D)*10.0D*Math.tan(Math.PI/12));
		vx_arrow[2] = (int)(11*i/150.0D);		
		vx_arrow[3] = (int)(11*i/150.0D+(j/300.0D)*10.0D*Math.tan(Math.PI/12));
		
		vy_arrow[0] = (int)(11*j/150.0D);
		vy_arrow[1] = (int)(11*j/150.0D+(j/300.0D)*10.0D);		
		vy_arrow[2] = (int)(11*j/150.0D+(j/300.0D)*5.0D);
		vy_arrow[3] = (int)(11*j/150.0D+(j/300.0D)*10.0D);
		//水平箭头
		hy_arrow[0] = (int)(139*j/150.0D);
		hy_arrow[1] = (int)(139*j/150.0D-(j/300.0D)*10.0D*Math.tan(Math.PI/12));
		hy_arrow[2] = (int)(139*j/150.0D);		
		hy_arrow[3] = (int)(139*j/150.0D+(j/300.0D)*10.0D*Math.tan(Math.PI/12));
		
		hx_arrow[0] = (int)(143*i/150.0D);
		hx_arrow[1] = (int)(143*i/150.0D-(j/300.0D)*10.0D);		
		hx_arrow[2] = (int)(143*i/150.0D-(j/300.0D)*5.0D);
		hx_arrow[3] = (int)(143*i/150.0D-(j/300.0D)*10.0D);
		
		//graphics2d.drawLine((int)(11*i/150.0D), (int)(11*j/150.0D), (int)(11*i/150.0D+(j/300.0D)*10.0D*Math.tan(Math.PI/12)), (int)(11*j/150.0D+(j/300.0D)*10.0D));
		//graphics2d.drawLine((int)(11*i/150.0D), (int)(11*j/150.0D), (int)(11*i/150.0D-(j/300.0D)*10.0D*Math.tan(Math.PI/12)), (int)(11*j/150.0D+(j/300.0D)*10.0D));
		//graphics2d.drawLine((int)(11*i/150.0D), (int)(11*j/150.0D+(j/300.0D)*5.0D), (int)(11*i/150.0D+(j/300.0D)*10.0D*Math.tan(Math.PI/12)), (int)(11*j/150.0D+(j/300.0D)*10.0D));
		//graphics2d.drawLine((int)(11*i/150.0D), (int)(11*j/150.0D+(j/300.0D)*5.0D), (int)(11*i/150.0D-(j/300.0D)*10.0D*Math.tan(Math.PI/12)), (int)(11*j/150.0D+(j/300.0D)*10.0D));
		
		graphics2d.fillPolygon(vx_arrow, vy_arrow, 4);
		graphics2d.fillPolygon(hx_arrow, hy_arrow, 4);
		
		int x[] = new int[65];//波形图横坐标
		int y[] = new int[65];//纵坐标
		x[64] = (int)(64*(2*i/150.0D)+(11*i/150.0D));
		y[64] = (int)((139.0D/150.0D)*(j/(double)(GisSignal.calMax_dBm - GisSignal.calMin_dBm))*(double)(-GisSignal.calMin_dBm)-(95.0D/150.0D)*(j/(double)(GisSignal.calMax_dBm - GisSignal.calMin_dBm))*((double)(GisSignal.calMax_dBm)));
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
		graphics2d.drawPolyline(x, y, 65);//绘制折线
		
		boolean usePersistentPeak = (persistentPeakMode || AppGlobalConfig.isSpectrogramPeakFollowFiltered())
				&& AppGlobalConfig.isPersistentPeakEnabled();
		if (usePersistentPeak)
		{
			if (persistentPeakReady && !Double.isNaN(persistentPeakDbm))
			{
				drawUnitDouble(graphics2d, persistentPeakDbm, (k + i) - 4, (l + 55) - 15, 1, ChooseLang.M[400], drawchange, unitfault);
			} else
			{
				drawUnitString(graphics2d, ChooseLang.M[400] + "--", (k + i) - 4, (l + 55) - 15, 1);
			}
		} else
		{
			drawUnit(graphics2d, (int)(amxdBm - 0.5D), (k + i) - 4, (l + 55) - 15, 1, ChooseLang.M[400], drawchange, unitfault);
		}
		
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
		
		//if (GisXYZ.realTab == 0)
			//drawUnit(graphics2d, (int)(avrdBm - 0.5D), (k + i) - 4, l + 55, 1, ChooseLang.M[401], drawchange, unitfault);
		//if (GisXYZ.realTab == 0)
			//drawUnit(graphics2d, (int)(amxdBm - 0.5D), (k + i) - 4, (l + 55) - 15, 1, ChooseLang.M[400], drawchange, unitfault);
		
		
		graphics2d.drawString("90'", i / 4 - 6, j / 10);
		graphics2d.drawString("180'", i / 2 - 10, j / 10);
		graphics2d.drawString("270'", (i / 4) * 3 - 10, j / 10);
		//if (GisXYZ.realTab == 0)
		//{
			//drawUnit(graphics2d, -54, 0, (j / 4) * 3 + 12, 0, "", drawchange, unitfault);
			//drawUnit(graphics2d, -43, 0, j / 2 + 12, 0, "", drawchange, unitfault);
			//drawUnit(graphics2d, -31, 0, j / 4 + 12, 0, "", drawchange, unitfault);
		//}
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
			drawOutlineString(graphics2d, resultStringprpd[k1], k + 4, l + 15 + 15 * k1, GisXYZ.anti_Color, GisXYZ.pure_Color);

		drawOutlineString(graphics2d, stringToDraw, k + 4, l + 15, Color.yellow, Color.black);
		drawOutlineString(graphics2d, stringTrgDraw, (k + i) - 50, l + 15, Color.red, Color.black);
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

	public void redraw3D(Graphics2D graphics2d)
	{
		//mana-draw prpd
		redraw3D(graphics2d, getWidth(), getHeight(), 0, 0);
	}

	public void redraw3D(Graphics2D graphics2d, int i, int j)
	{
		redraw3D(graphics2d, i, j, 0, 0);
	}

	public void redraw3D(Graphics2D graphics2d, int i, int j, int k, int l)
	{
		int ai[][] = new int[7][5];
		int ai1[][] = new int[7][5];
		
		//坐标轴
		int Axis_s[][] = new int[4][4];
		int Axis_e[][] = new int[4][4];
		d3p.clearD3Screen();
		double d1 = 0.90000000000000002D / (double)cycleNum;
		graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		graphics2d.setPaint(Color.black);
		graphics2d.fillRect(k, l, i, j);
		graphics2d.setPaint(Color.gray);
		//int i5 = cycleNum;
		int k4 = 0;
		for (int i1 = 0; i1 < 64; i1++)
		{
			for (int l2 = 0; l2 < 64; l2++)
				if (prpd[i1][l2] > k4)
					k4 = prpd[i1][l2];

		}

		int l5 = k4 + GisXYZ.bufferLen / 512;
		for (int j1 = -1; j1 < cycleNum + 1; j1++)
		{
			int i3 = -1;
			d3p.rotConvScreen(i3 + 1, j1 + 1, ((double)(i3 - phaseNum / 2) + 0.5D) * d1, ((double)(j1 - cycleNum / 2) + 0.5D) * d1, -0.5D * (double)cycleNum * d1 * 0.5D, 0.0D);
			i3 = phaseNum;
			d3p.rotConvScreen(i3 + 1, j1 + 1, ((double)(i3 - phaseNum / 2) + 0.5D) * d1, ((double)(j1 - cycleNum / 2) + 0.5D) * d1, -0.5D * (double)cycleNum * d1 * 0.5D, 0.0D);
		}

		for (int j3 = -1; j3 < phaseNum + 1; j3++)
		{
			int k1 = -1;
			d3p.rotConvScreen(j3 + 1, k1 + 1, ((double)(j3 - phaseNum / 2) + 0.5D) * d1, ((double)(k1 - cycleNum / 2) + 0.5D) * d1, -0.5D * (double)cycleNum * d1 * 0.5D, 0.0D);
			k1 = cycleNum;
			d3p.rotConvScreen(j3 + 1, k1 + 1, ((double)(j3 - phaseNum / 2) + 0.5D) * d1, ((double)(k1 - cycleNum / 2) + 0.5D) * d1, -0.5D * (double)cycleNum * d1 * 0.5D, 0.0D);
		}

		double ad[][] = new double[cycleNum][phaseNum];
		for (int l1 = 0; l1 < cycleNum; l1++)
		{
			for (int k3 = 0; k3 < phaseNum; k3++)
			{
				int l4 = prpd[k3][l1];
				if (l4 < 0)
					l4 = 0;
				double d = (double)l4 / (double)l5;
				if (1.0D < d)
					d = 1.0D;
				ad[k3][l1] = d;
				if (0 < k3 && 0 < l1)
				{
					if (d < ad[k3 - 1][l1 - 1])
						d = ad[k3 - 1][l1 - 1];
					if (d < ad[k3][l1 - 1])
						d = ad[k3][l1 - 1];
					if (d < ad[k3 - 1][l1])
						d = ad[k3 - 1][l1];
				}
				d3p.rotConvScreen(k3 + 1, phaseNum - l1, ((double)(k3 - phaseNum / 2) + 0.5D) * d1, (((double)(cycleNum - 1 - l1) + 0.5D) - (double)(cycleNum / 2)) * d1, (d - 0.5D) * (double)cycleNum * d1 * 0.5D, d);
			}

		}

		d3p.drawD3Screen();
		//x轴
		for (int x1 = 2; 1 <= x1; x1--)
		{
			for (int xn3 = -2; xn3 <= 1; xn3++)
			{
				d3p.rotConvScreen((double)(xn3-16) * (-0.25D) * (double)phaseNum * d1/4, (double)(x1-1) * (0.25D) * (double)cycleNum * d1/4, -0.25D * (double)cycleNum * d1);
				Axis_s[2-x1][xn3+2] = d3p.gx;
				Axis_e[2-x1][xn3+2] = d3p.gy;
			}
		}
		
		//y轴
		for (int y1 = 0; y1 <=1; y1++)
		{
			for (int yn3 = -2; yn3 <= 1; yn3++)
			{
				d3p.rotConvScreen((double)(yn3-16) * (-0.25D) * (double)phaseNum * d1/4, (double)(y1-1) * (0.25D) * (double)cycleNum * d1/4, -0.25D * (double)cycleNum * d1);
				Axis_s[1+y1][yn3+2] = d3p.gx;
				Axis_e[1+y1][yn3+2] = d3p.gy;
			}
		}
		
		//z轴
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
		for (int i2 = 2; -1 <= i2; i2--)
		{
			for (int l3 = -2; l3 <= 2; l3++)
			{
				d3p.rotConvScreen((double)l3 * 0.25D * (double)phaseNum * d1, (double)i2 * 0.25D * (double)cycleNum * d1, -0.25D * (double)cycleNum * d1);
				ai[2 - i2][l3 + 2] = d3p.gx;
				ai1[2 - i2][l3 + 2] = d3p.gy;
			}

		}

		for (int j2 = 0; j2 < 3; j2++)
		{
			for (int i4 = -2; i4 <= 2; i4++)
			{
				d3p.rotConvScreen((double)i4 * 0.25D * (double)phaseNum * d1, -0.5D * (double)cycleNum * d1, (double)(j2 - 1) * 0.25D * (double)cycleNum * d1);
				ai[4 + j2][i4 + 2] = d3p.gx;
				ai1[4 + j2][i4 + 2] = d3p.gy;
			}

		}

		for (int j4 = 0; j4 < 5; j4++)
		{
			int j5;
			if (j4 == 0 || j4 == 4)
				j5 = Option.gt ? 0xffffff : 0;
			else
				j5 = 0x808080;
			d3p.drawLine(ai[0][j4], ai1[0][j4], ai[4][j4], ai1[4][j4], j5);
			d3p.drawLine(ai[4][j4], ai1[4][j4], ai[6][j4], ai1[6][j4], j5);
		}

		for (int k2 = 0; k2 < 7; k2++)
		{
			int k5;
			if (k2 == 0 || k2 == 6)
				k5 = Option.gt ? 0xffffff : 0;
			else
				k5 = 0x808080;
			d3p.drawLine(ai[k2][0], ai1[k2][0], ai[k2][4], ai1[k2][4], k5);
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

		d3p.draw(graphics2d, k, l);//绘制prpd
		graphics2d.setPaint(Option.gt ? ((java.awt.Paint) (Color.LIGHT_GRAY)) : ((java.awt.Paint) (Color.DARK_GRAY)));
		if (rdeg < 1.5707963267948966D)
		{
			graphics2d.drawString(" 90'", k + ai[0][1], l + ai1[0][1] + 11);
			graphics2d.drawString("180'", k + ai[0][2], l + ai1[0][2] + 11);
			graphics2d.drawString("270'", k + ai[0][3], l + ai1[0][3] + 11);
			graphics2d.drawString("25%", (k + ai[1][0]) - 31, l + ai1[1][0] + 4);
			graphics2d.drawString("50%", (k + ai[2][0]) - 31, l + ai1[2][0] + 4);
			graphics2d.drawString("75%", (k + ai[3][0]) - 31, l + ai1[3][0] + 4);

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
			graphics2d.drawString("25%", k + ai[1][4] + 7, l + ai1[1][4] + 4);
			graphics2d.drawString("50%", k + ai[2][4] + 7, l + ai1[2][4] + 4);
			graphics2d.drawString("75%", k + ai[3][4] + 7, l + ai1[3][4] + 4);

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
		
		String s1 = (new StringBuilder()).append(ChooseLang.M[280]).append(":").append(String.format("%d", new Object[] {
				Integer.valueOf(Countprpd)
			})).toString();
		Font font = graphics2d.getFont();
		FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
		int i1 = fontmetrics.stringWidth(s1);
		graphics2d.drawString(s1, (k + i) - 4 -i1, l +20);
		
		if (blink == 0)
			graphics2d.setPaint(new Color(0x808080));
		else
			graphics2d.setPaint(new Color(0xa0a0a0));
		blink = 1 - blink;
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
			g.setColor(Color.black);
			g.setXORMode(Color.white);
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
		} 		
		if (draw2D)
		{
			redraw2D(g, i, j, k, l);
		}
		if(drawCon)
		{
			redraw2DCon(g, i, j, k, l);
		}
		if(drawUltral)
		{
			redraw2DUltral(g, i, j, k, l);
		}
		if(Waveform)
		{
			redraw2D_Wave(g, i, j, k, l);
		}
		if(drawColum)
		{
			redraw2D_Colum(g, i, j, k, l);
		}
	}
}
