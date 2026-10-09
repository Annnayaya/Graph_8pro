package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JToggleButton;

public class JRTogButton extends JToggleButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8765599725508517498L;
	/* 决定圆角的弧度 */
    public static int radius = 10;
    public Color COLOR1, COLOR2;
    public static int gray = 4;
    public static int pink = 3;

	public static int ashen = 2;

	public static int sky = 1;

	public static int stone = 0;
    /* 粉红 */
    public static Color pink1 = new Color(240, 240, 240);
    public static Color pink2 = new Color(254, 173, 240);
    /* 灰白 */
    public static Color ashen1 = new Color(250, 250, 250);
    public static Color ashen2 = new Color(197, 197, 197);
    /* 深宝石蓝 */
    public static Color stone1 = new Color(79, 125, 184);
    public static Color stone2 = new Color(89, 200, 240);
    /* 淡天蓝色 */
    public static Color sky1 = new Color(240, 240, 240);
    public static Color sky2 = new Color(121, 230, 230);
    
    public static Color gray1 = new Color(250, 250, 250);
    public static Color gray2 = new Color(170, 170, 170);
    /*光标进入按钮判断*/
    private boolean hover;
    
    public JRTogButton()
    {
    	this("", stone);
    }
    public JRTogButton(String name)
    {
    	this(name, stone);
    }
    public JRTogButton(String name, int style)
    {
		super.setText(name);
		if (style == pink)
		{
		    COLOR1 = pink1;
		    COLOR2 = pink2;
		}
		if (style == ashen)
		{
		    COLOR1 = ashen1;
		    COLOR2 = ashen2;
		}
		if (style == stone)
		{
		    COLOR1 = stone1;
		    COLOR2 = stone2;
		}
		if (style == sky)
		{
		    COLOR1 = sky1;
		    COLOR2 = sky2;
		}
		if (style == gray)
		{
			COLOR1 = gray1;
		    COLOR2 = gray2;
		}
		paintcolor(COLOR1, COLOR2);
	}
    private void paintcolor(Color COLOR1, Color COLOR2)
    {
		setMargin(new Insets(0, 0, 0, 0));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setBorderPainted(false);
		setForeground(Color.black);
		setFocusPainted(false);
		setContentAreaFilled(false);
		addMouseListener(new MouseAdapter()
		{
		    @Override
		    public void mouseEntered(MouseEvent e)
		    {
				hover = true;
				repaint();
		    }
		    @Override
		    public void mouseExited(MouseEvent e)
		    {
				hover = false;
				repaint();
		    }
		});
	}
    
    /* 报警闪烁状态 */
    private boolean alarmFlashRed = false;

    public void setAlarmFlashRed(boolean flashRed) {
        if (this.alarmFlashRed != flashRed) {
            this.alarmFlashRed = flashRed;
            if (flashRed) {
                setForeground(Color.WHITE);
            } else {
                setForeground(Color.BLACK);
            }
            repaint();
        }
    }

    public boolean isAlarmFlashRed() {
        return alarmFlashRed;
    }
    
    @Override
    protected void paintComponent(Graphics g)
    {
		Graphics2D g2d = (Graphics2D) g.create();
		int height = getHeight();
		int with = getWidth();
		float tran = 0.5F;
		if (!hover)
		{/* 鼠标离开/进入时的透明度改变量 */
		    tran = 0.5F;
		}
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		/* GradientPaint是颜色渐变类 */
		GradientPaint p1;
		GradientPaint p2;
		if (alarmFlashRed)
		{
		    p1 = new GradientPaint(0, 0, new Color(254, 202, 202), 0, height, new Color(153, 27, 27), true);
		    p2 = new GradientPaint(0, 1, new Color(254, 226, 226, 180), 0, height, new Color(153, 27, 27, 200), true);
		    tran = 1F;
		}
		else if (this.getModel().isSelected())//JToggleButton
		{
		    p1 = new GradientPaint(0, 0, new Color(0, 0, 0), 0, height, new Color(100, 100, 100), true);
		    p2 = new GradientPaint(0, 1, new Color(0, 0, 0, 120), 0, height, new Color(255, 255, 255, 250), true);
		    tran = 1F;
		}
		else
		{
		    p1 = new GradientPaint(0, 0, new Color(100, 100, 100), 0, height, new Color(0, 0, 0), true);
		    p2 = new GradientPaint(0, 1, new Color(255, 255, 255, 250), 0, height, new Color(0, 0, 0, 120), true);
		}
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tran));
		RoundRectangle2D.Float r2d = new RoundRectangle2D.Float(0, 0, with - 1, height - 1, radius, radius);
		// 最后两个参数数值越大，按钮越圆，以下同理
		Shape clip = g2d.getClip();
		g2d.clip(r2d);
		Color c1 = alarmFlashRed ? new Color(239, 68, 68) : COLOR1;
		Color c2 = alarmFlashRed ? new Color(185, 28, 28) : COLOR2;
		GradientPaint gp = new GradientPaint(0.0F, 0.0F, c1, 0.0F, height / 2, c2, true);
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, with, height);
		g2d.setClip(clip);
		g2d.setPaint(p1);
		g2d.drawRoundRect(0, 0, with - 3, height - 3, radius, radius);
		g2d.setPaint(p2);
		g2d.drawRoundRect(1, 1, with - 3, height - 3, radius, radius);
		g2d.dispose();
		super.paintComponent(g);
    }

}
