package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.TextField;

import javax.swing.JPanel;
import javax.swing.JScrollPane;


@SuppressWarnings("serial")
public class Scanner extends JPanel {

	JPanel jshowprpd;
	TextField textfield;
	PRPDGraph prpd;
	GisRealtimeBuffer gisrtb;
	CONTINUEGraph congraph;
	JScrollPane jscroll;
	JPanel jpscroll;
	int ai[];
	int k;
	public Scanner()
	{
		textfield = new TextField("输入");
		jshowprpd = new JPanel(new GridLayout(1, 0));
		gisrtb = new GisRealtimeBuffer(ai);
		prpd = new PRPDGraph(64,64,gisrtb.dispPRPD,null);
		prpd.setPreferredSize(new Dimension(200,100));
		prpd.refreshed = true;
		prpd.draw3D = false;
		prpd.drawUltral = true;
		prpd.repaint();
		jshowprpd.add(prpd);
		
		textfield.setText("Hello");
		textfield.setPreferredSize(new Dimension(80,60));
		
		
		congraph = new 	CONTINUEGraph(64, 64, ai, k);
		congraph.setPreferredSize(new Dimension(1800,200));
		jpscroll = new JPanel(new GridLayout(1,0));
		jpscroll.add(congraph);
		jpscroll.setPreferredSize(new Dimension(800,200));
		jscroll = new JScrollPane(jpscroll, JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
		
		this.add(jscroll,"North");
		//this.add(jshowprpd,"South");
		
	}
}
