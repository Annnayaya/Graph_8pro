package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.SystemColor;
import java.awt.geom.Ellipse2D;

import javax.swing.JToggleButton;

public class JCircleButton extends JToggleButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8971035876317740922L;
	Shape shape;
	Color bgColor = SystemColor.control;

	public JCircleButton() {
	    this("", null);
	}

	public JCircleButton(String label) {
	    this(label, null);
	}

	public JCircleButton(String label, Color bgColor) {
	    super(label); // 调用父类构造方法
	    if (bgColor != null) {
	      this.bgColor = bgColor;
	    }
	    Dimension size = this.getPreferredSize();
	    size.width = size.height = Math.max(size.width, size.height);
	    this.setPreferredSize(size); // 设置宽高等距
	    this.setContentAreaFilled(false); // 不绘制内容区域
	    this.setBorderPainted(false); // 不绘制边框
	    this.setFocusPainted(false); // 不绘制焦点状态
	}

	protected void paintComponent(Graphics g) {
	    // 如果鼠标按下，isArmed()方法返回true
	    if (this.getModel().isSelected()) {
	      g.setColor(java.awt.SystemColor.controlHighlight);
	    } else {
	      g.setColor(java.awt.SystemColor.controlShadow);
	      g.setColor(this.bgColor); // 设置背景颜色
	    }
	    g.fillOval(0, 0, this.getSize().width, this.getSize().height); // 绘制圆形背景区域
	    //g.setColor(java.awt.SystemColor.controlShadow); // 设置边框颜色
	    //g.drawOval(0, 0, this.getSize().width, this.getSize().height); // 绘制边框线
	    super.paintComponent(g);
	}

	public boolean contains(int x, int y) {
	    if ((shape == null) || (!shape.getBounds().equals(this.getBounds()))) {
	      this.shape = new Ellipse2D.Float(0, 0, this.getWidth(), this
	          .getHeight());
	    }
	    return shape.contains(x, y);
	}
}
