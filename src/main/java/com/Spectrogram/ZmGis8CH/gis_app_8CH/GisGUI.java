package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EtchedBorder;


class GisGUI
{
	Dimension sd;
	JFrame full;
	GisXYZ gm;
	GisGraph prps[];
	PRPDGraph prpd[];
	ColorCard colorcard[];//比色卡
	JPanel prpsFrame[];
	JPanel prpdFrame[];//mana-PRPD绘制区域
	JPanel chCPanel[];
	JPanel description[];
	JLabel ph_jlabel[];//相位
	JLabel ga_jlabel[];//增益
	JLabel th_jlabel[];//阀值
	JLabel trglabel[];//优先触发
	MemoPanel memoPanel;//mana-界面左下当前时间
	HbDesktopPane dp;
	int chno;
	int drawcnt;
	JToggleButton suspendButtonPRPS[];
	JToggleButton suspendButtonPRPD[];
	JScrollBar phaseScrollBar[];
	JCheckBox noisegate[];
	JCheckBox ultrasonic[];
	JScrollBar offsetAdjustScrollBar[];
	JScrollBar gainScrollBar[];//增益滑动条
	JTabbedPane fullPane;
	JRoundButton phaseSelButton[];
	JRoundButton offsetSelButton[];
	JRoundButton gainSelButton[];//增益按钮
	JRadioButton jradiobutton2[];//PRPS3D
	JRadioButton jradiobutton[];//PRPS2D
	JRadioButton jradiobutton_fly[];//飞行图
	JRadioButton jradiobutton_waveform[];//波形图
	JRadioButton jradiobutton_prpscon[];//连续图prps
	JRadioButton jradiobutton1[];//PRPD2D
	JRadioButton jradiobutton3[];//PRPD3D
	JRadioButton jradiobutton_con[];//PRPDCon
	JRadioButton jradiobutton_ultral[];//超声波
	JRadioButton jradiobutton_prpdwave[];//波形图
	JRadioButton jradiobutton_columprpd[];//柱状图prpd
	private Boolean oldclick = false;
	public static JLabel strlabelname[];//61 通道标签

	Boolean chclicked[];
	JPanel jpanel;
	JDialog popdialog;
	JPanel popjpanel;
	public static int count = 0;
	JScrollPane Hprpdscrollpane[];//Prpd水平滚动条
	JPanel prpdjpanel[];//prpd绘图框架
	GisChannelData gettrigframe;
	//Scanner scan;
	int selectunit = 0;//选择单位的通道
	public static int index = 4;
	static JPopupMenu jpopmenuzoomprps;
	static JMenuItem jmenuitemzoomprps;
	static JPopupMenu jpopmenuzoomprpd;
	static JMenuItem jmenuitemzoomprpd;
	static int selectch;

	String[] unitsettext;
	String[] sAz;//增益x1,x10,x100
	JComboBox<String>[] unitsetJComboBox;//单位多选框
	JComboBox<String>[] jAz;//增益
	public static int[] indexgroup;
	public static int[] indexAz;
	Object[] getObject;
	int unitchannel = 0;
	int Az = 0;

	JCheckBox addjcheckbox[];

	class Disp3DPRPD implements ActionListener
	{

		int ch = 0;
		public Disp3DPRPD(int i)
		{
			ch = i;
		}

		public void actionPerformed(ActionEvent actionevent)
		{
			prpd[ch].rdeg = 2.3561944901923448D;
			prpd[ch].tdeg = 0.53407075111026492D;
			prpd[ch].draw3D = true;
			prpd[ch].draw2D = false;
			prpd[ch].prpdrefresh = true;
			prpd[ch].repaint();

		}
	}

	class Disp2DPRPD implements ActionListener
	{
		int ch = 0;
		public Disp2DPRPD(int i)
		{
			ch = i;
		}
		public void actionPerformed(ActionEvent actionevent)
		{
			prpd[ch].draw2D = true;
			prpd[ch].prpdrefresh = true;
			prpd[ch].repaint();
		}
	}

	class Disp2DConGraphic implements ActionListener
	{
		int ch = 0;
		public Disp2DConGraphic(int i)
		{
			ch = i;
		}

		@Override
		public void actionPerformed(ActionEvent actionevent) {
			//prpd[ch].draw3D = false;
			prpd[ch].prpdrefresh = true;
			prpd[ch].drawCon = true;
			prpd[ch].draw2D = false;
			prpd[ch].repaint();

		}

	}

	class Disp3DPRPS implements ActionListener
	{

		int ch= 0;
		public Disp3DPRPS(int i)
		{
			ch = i;
		}
		public void actionPerformed(ActionEvent actionevent)
		{
			prps[ch].rdeg = 2.3561944901923448D;
			prps[ch].tdeg = 0.53407075111026492D;
			prps[ch].draw3D = true;
			prps[ch].prpsrefresh = true;
			prps[ch].repaint();

		}
	}

	class Disp2DPRPS implements ActionListener
	{

		int ch = 0;
		public Disp2DPRPS(int i)
		{
			ch = i;
		}
		public void actionPerformed(ActionEvent actionevent)
		{
			prps[ch].draw3D = false;
			prps[ch].prpsrefresh = true;
			prps[ch].repaint();

		}
	}

	//侦听2D连续测量图
	class Disp2DCONGRAPH implements ActionListener
	{
		int ch = 0;
		public Disp2DCONGRAPH(int i)
		{
			ch = i;
		}

		@Override
		public void actionPerformed(ActionEvent actionevent) {

		}

	}

	class SetOAScroll implements AdjustmentListener
	{

		int ch = 0;
		public SetOAScroll(int i)
		{
			ch = i;
		}
		public void adjustmentValueChanged(AdjustmentEvent adjustmentevent)
		{
			if (!GisSignal.isAuto[ch])
			{
				int i = offsetAdjustScrollBar[ch].getValue();
				offsetSelButton[ch].setText(String.format("%03d", new Object[] {
					Integer.valueOf(i)
				}));
			}
		}
	}

	class SetOSButton implements ActionListener
	{

		int ch = 0;
		public SetOSButton(int i)
		{
			ch = i;
		}
		public void actionPerformed(ActionEvent actionevent)
		{
			if (GisSignal.isAuto[ch])
			{
				GisSignal.isAuto[ch] = false;
				offsetSelButton[ch].setText(String.format("%03d", new Object[] {
					Integer.valueOf(offsetAdjustScrollBar[ch].getValue())
				}));
				offsetAdjustScrollBar[ch].getValue();
			} else
			{
				GisSignal.isAuto[ch] = true;
				offsetSelButton[ch].setText("AUTO");
			}
		}
	}

	class SetPScroll implements AdjustmentListener
	{

		int ch = 0;
		public SetPScroll(int i)
		{
			ch = i;
		}
		public void adjustmentValueChanged(AdjustmentEvent adjustmentevent)
		{
			int i = phaseScrollBar[ch].getValue();
			if (i == 0)
				phaseSelButton[ch].setText(ChooseLang.M[25]);
			else
			if (i == -21)
				phaseSelButton[ch].setText(ChooseLang.M[26]);
			else
			if (i == -43)
				phaseSelButton[ch].setText(ChooseLang.M[27]);
			else
				phaseSelButton[ch].setText(String.format("%d", new Object[] {
					Integer.valueOf((int)(-5.625D * (double)i))
				}) + "°");
		}
	}

	class SetPSButton implements ActionListener
	{

		int ch  = 0;
		public SetPSButton(int i)
		{
			ch = i;
		}
		public void actionPerformed(ActionEvent actionevent)
		{
			int i = phaseScrollBar[ch].getValue();
			byte byte0;
			if (i == 0)
			{
				byte0 = -21;
				phaseSelButton[ch].setText(ChooseLang.M[26]);
			} else
			if (i == -21)
			{
				byte0 = -43;
				phaseSelButton[ch].setText(ChooseLang.M[27]);
			} else
			{
				byte0 = 0;
				phaseSelButton[ch].setText(ChooseLang.M[25]);
			}
			phaseScrollBar[ch].setValue(byte0);
		}
	}
	//增益滚动条
	class SetGScroll implements AdjustmentListener
	{

		int ch = 0;
		public SetGScroll(int i)
		{
			ch = i;
		}
		public void adjustmentValueChanged(AdjustmentEvent adjustmentevent)
		{
			if (!GisSignal.isAuto[ch])
			{
				int i = gainScrollBar[ch].getValue();
				gainSelButton[ch].setText(String.format("%2d", new Object[] {
					Integer.valueOf(i)
				})+"dB");
			}
		}
	}
	//增益设置按钮
	class SetGSButton implements ActionListener
	{
		int ch  = 0;
		public SetGSButton(int i)
		{
			ch = i;
		}

		@Override
		public void actionPerformed(ActionEvent actionevent)
		{
			int i = gainScrollBar[ch].getValue();
			byte byte0;
			if (i == 0)
			{
				byte0 = 6;
				gainSelButton[ch].setText("0dB");
			} else
			if (i == 6)
			{
				byte0 = 13;
				gainSelButton[ch].setText("6dB");
			} else
			{
				byte0 = 0;
				gainSelButton[ch].setText("13dB");
			}
			gainScrollBar[ch].setValue(byte0);
		}

	}

	class PRPDACC implements ActionListener
	{
		int ch = 0;
		public PRPDACC(int i)
		{
			ch = i;
		}
		public void actionPerformed(ActionEvent actionevent)
		{
			gm.gdp.gd.rtb[ch].refreshAccum = ((JCheckBox)(JCheckBox)actionevent.getSource()).isSelected();
		}
	}
	//61 修改单位
	class UnitSelect implements ItemListener
	{
		int ch = 0;
		public UnitSelect(int i)
		{
			ch = i;
		}
		@Override
		public void itemStateChanged(ItemEvent itemevent) {

				getObject[ch] = itemevent.getItem();
				unitchannel = ch;
				//for(int j = 0; j < 8; j++)
				//{
					//Option.ch[j] = false;
				//}
				if(getObject[ch].toString() == "dBm")
				{
					GisGraph.unitfaults[ch] = 0;
					Option.unitType = 0;
					Option.writeOption();
				}
				if(getObject[ch].toString() == "mW")
				{
					GisGraph.unitfaults[ch] = 1;
					Option.unitType = 1;
					Option.writeOption();
				}
				if(getObject[ch].toString() == "mV")
				{
					GisGraph.unitfaults[ch] = 2;
					Option.unitType = 2;
					Option.writeOption();
				}
				if(getObject[ch].toString() == "pC")
				{
					GisGraph.unitfaults[ch] = 3;
					Option.unitType = 3;
					Option.writeOption();
				}
				if(getObject[ch].toString() == "dB")
				{
					GisGraph.unitfaults[ch] = 4;
					Option.unitType = 4;
					Option.writeOption();
				}

		}
	}
	//增益
	class AzSelect implements ItemListener
	{
		int ch = 0;
		public AzSelect(int i)
		{
			ch = i;
		}
		@Override
		public void itemStateChanged(ItemEvent itemevent) {

				getObject[ch] = itemevent.getItem();
				Az = ch;
				//for(int j = 0; j < 4; j++)
				//{
					//Option.ch[j] = false;
				//}
				if(getObject[ch].toString() == "x1")
				{

					Option.Az = 0;
					//Option.writeOption();
				}
				if(getObject[ch].toString() == "x10")
				{
					Option.Az = 1;
					//Option.writeOption();
				}
				if(getObject[ch].toString() == "x100")
				{
					Option.Az = 2;
					//Option.writeOption();
				}
		}
	}

	//双击侦听PRPS
	class PRPSClicked implements MouseListener
	{
		int ch = 0;
		public PRPSClicked(int i)
		{
			ch = i;
		}

		public void mouseClicked(MouseEvent mouseevent){
			chclicked[ch] = Boolean.valueOf(!getOldclick());
			count = mouseevent.getClickCount();
				if(chclicked[ch] && count == 2 && mouseevent.getButton() == MouseEvent.BUTTON1){
					//JOptionPane.showMessageDialog(null,"doubleClicked!");

					for(int k = 0 ; k < 8; k++){
						prpsFrame[k].setVisible(!chclicked[ch]);
						prpdFrame[k].setVisible(!chclicked[ch]);
						chCPanel[k].setVisible(!chclicked[ch]);
						dp.jbutton1[k].setEnabled(false);
						setOldclick(true);
					}
					prpsFrame[ch].setSize(jpanel.getWidth(),jpanel.getHeight()-dp.jtoolbarBottom.getHeight()-memoPanel.getHeight()-60);
					prpsFrame[ch].setLocation(0, 0);
					prpsFrame[ch].setVisible(chclicked[ch]);
					chCPanel[ch].setSize(jpanel.getWidth(),60);
					chCPanel[ch].setLocation(0, jpanel.getHeight()-dp.jtoolbarBottom.getHeight()-memoPanel.getHeight()-60);
					chCPanel[ch].setVisible(chclicked[ch]);
					dp.add(prpsFrame[ch]);
					dp.add(chCPanel[ch]);
				}

				if((!chclicked[ch]) && count == 2 && mouseevent.getButton() == MouseEvent.BUTTON1)
				{
					for(int j = 0; j < 8; j++){
						dp.jbutton1[j].setEnabled(true);
						//dp.showWindows(j);//显示被选中的通道
						dp.shownCh();
					}
					setOldclick(false);
				}

		}

		public void mouseEntered(MouseEvent mouseevent) {

		}

		public void mouseExited(MouseEvent mouseevent) {

		}

		public void mousePressed(MouseEvent mouseevent) {

		}

		public void mouseReleased(MouseEvent arg0) {

		}

	}

	//zoomprps
	class RightPRPSClicked implements MouseListener
		{
			//int ch = 0;
			//public RightPRPSClicked(int i)
			//{
				//ch = i;
			//}

			public void mouseClicked(MouseEvent mouseevent){


			}

			public void mouseEntered(MouseEvent mouseevent) {

			}

			public void mouseExited(MouseEvent mouseevent) {

			}

			public void mousePressed(MouseEvent mouseevent) {
				chclicked[selectch] = Boolean.valueOf(!getOldclick());
				count = mouseevent.getClickCount();
					if(chclicked[selectch] && mouseevent.getButton() == MouseEvent.BUTTON1){
						//JOptionPane.showMessageDialog(null,"doubleClicked!");

						for(int k = 0 ; k < 8; k++){
							prpsFrame[k].setVisible(!chclicked[selectch]);
							prpdFrame[k].setVisible(!chclicked[selectch]);
							chCPanel[k].setVisible(!chclicked[selectch]);
							dp.jbutton1[k].setEnabled(false);
							setOldclick(true);
						}
						prpsFrame[selectch].setSize(jpanel.getWidth(),jpanel.getHeight()-dp.jtoolbarBottom.getHeight()-memoPanel.getHeight()-60);
						prpsFrame[selectch].setLocation(0, 0);
						prpsFrame[selectch].setVisible(chclicked[selectch]);
						chCPanel[selectch].setSize(jpanel.getWidth(),60);
						chCPanel[selectch].setLocation(0, jpanel.getHeight()-dp.jtoolbarBottom.getHeight()-memoPanel.getHeight()-60);
						chCPanel[selectch].setVisible(chclicked[selectch]);
						dp.add(prpsFrame[selectch]);
						dp.add(chCPanel[selectch]);
					}

					if((!chclicked[selectch]) && mouseevent.getButton() == MouseEvent.BUTTON1)
					{
						for(int j = 0; j < 8; j++){
							dp.jbutton1[j].setEnabled(true);
							//dp.showWindows(j);//显示被选中的通道
							dp.shownCh();
						}
						setOldclick(false);
					}
			}

			public void mouseReleased(MouseEvent arg0) {

			}

		}

	//zoomprpd
	class RightPRPDClicked implements MouseListener
			{
				//int ch = 0;
				//public RightPRPSClicked(int i)
				//{
					//ch = i;
				//}

				public void mouseClicked(MouseEvent mouseevent){


				}

				public void mouseEntered(MouseEvent mouseevent) {

				}

				public void mouseExited(MouseEvent mouseevent) {

				}

				public void mousePressed(MouseEvent mouseevent) {
					chclicked[selectch] = Boolean.valueOf(!getOldclick());
					count = mouseevent.getClickCount();
						if(chclicked[selectch] && mouseevent.getButton() == MouseEvent.BUTTON1){
							//JOptionPane.showMessageDialog(null,"doubleClicked!");
							for(int k = 0 ; k < 8; k++){
								prpsFrame[k].setVisible(!chclicked[selectch]);
								prpdFrame[k].setVisible(!chclicked[selectch]);
								chCPanel[k].setVisible(!chclicked[selectch]);
								dp.jbutton1[k].setEnabled(false);
								setOldclick(true);
							}
							prpdFrame[selectch].setSize(jpanel.getWidth(),jpanel.getHeight()-dp.jtoolbarBottom.getHeight()-memoPanel.getHeight()-60);
							prpdFrame[selectch].setLocation(0, 0);
							prpdFrame[selectch].setVisible(chclicked[selectch]);
							chCPanel[selectch].setSize(jpanel.getWidth(),60);
							chCPanel[selectch].setLocation(0, jpanel.getHeight()-dp.jtoolbarBottom.getHeight()-memoPanel.getHeight()-60);
							chCPanel[selectch].setVisible(chclicked[selectch]);
							dp.add(prpdFrame[selectch]);
							dp.add(chCPanel[selectch]);
						}

						if((!chclicked[selectch]) && mouseevent.getButton() == MouseEvent.BUTTON1)
						{

							for(int j = 0; j < 8; j++){
								dp.jbutton1[j].setEnabled(true);
								//dp.showWindows(j);//显示被选中的通道
								dp.shownCh();
							}
							setOldclick(false);
						}
				}

				public void mouseReleased(MouseEvent arg0) {

				}

			}

	//双击侦听PRPD
	class PRPDClicked implements MouseListener
		{
			int ch = 0;
			public PRPDClicked(int i)
			{
				ch = i;
			}

			public void mouseClicked(MouseEvent mouseevent){
				chclicked[ch] = Boolean.valueOf(!getOldclick());
				count = mouseevent.getClickCount();
					if(chclicked[ch] && count == 2 && mouseevent.getButton() == MouseEvent.BUTTON1){
						//JOptionPane.showMessageDialog(null,"doubleClicked!");
						for(int k = 0 ; k < 8; k++){
							prpsFrame[k].setVisible(!chclicked[ch]);
							prpdFrame[k].setVisible(!chclicked[ch]);
							chCPanel[k].setVisible(!chclicked[ch]);
							dp.jbutton1[k].setEnabled(false);
							setOldclick(true);
						}
						prpdFrame[ch].setSize(jpanel.getWidth(),jpanel.getHeight()-dp.jtoolbarBottom.getHeight()-memoPanel.getHeight()-60);
						prpdFrame[ch].setLocation(0, 0);
						prpdFrame[ch].setVisible(chclicked[ch]);
						chCPanel[ch].setSize(jpanel.getWidth(),60);
						chCPanel[ch].setLocation(0, jpanel.getHeight()-dp.jtoolbarBottom.getHeight()-memoPanel.getHeight()-60);
						chCPanel[ch].setVisible(chclicked[ch]);
						dp.add(prpdFrame[ch]);
						dp.add(chCPanel[ch]);
					}

					if((!chclicked[ch]) && count == 2 && mouseevent.getButton() == MouseEvent.BUTTON1)
					{
						for(int j = 0; j < 8; j++){
							dp.jbutton1[j].setEnabled(true);
							//dp.showWindows(j);//显示被选中的通道
							dp.shownCh();
						}
						setOldclick(false);
					}

			}

			public void mouseEntered(MouseEvent mouseevent) {

			}

			public void mouseExited(MouseEvent mouseevent) {

			}

			public void mousePressed(MouseEvent mouseevent) {

			}

			public void mouseReleased(MouseEvent arg0) {

			}

		}

	//PPRS右键缩放
	class myzoomPRPS implements MouseListener
		{

			int ch = 0;
			public myzoomPRPS(int i)
			{
				ch = i;
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3)
	            {
						jpopmenuzoomprps.show(prps[ch], e.getX(),e.getY());
						selectch = ch;

	            }

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}
		}

	//PPRD右键缩放
	class myzoomPRPD implements MouseListener
		{

			int ch = 0;
			public myzoomPRPD(int i)
			{
				ch = i;
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3)
	            {
						jpopmenuzoomprpd.show(prpd[ch], e.getX(),e.getY());
						selectch = ch;

	            }

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}
		}

	//刷新通道显示
	public void guiRefresh()
	{
		drawcnt++;
		index = gm.gdp.gd.trigger.Channel_Select.getSelectedIndex();//选择的通道
		for (int i = 0; i < chno; i++)
		{
			if (prps[i].isVisible())
			{

				indexgroup[i] = this.unitsetJComboBox[i].getSelectedIndex();//选择单位
				indexAz[i] = this.jAz[i].getSelectedIndex();//选择的增益
				/*
				for(int j = 1; j < chno; j++)
				{
					prps[j].drawchange = false;
					prps[j].prpsrefresh = true;
					prps[j].repaint();
					colorcard[j].repaint();
				}
				*/
				/*
				if(index <= 3)
				{
					prps[i].drawchange = false;
					prps[index].drawchange = true;
					prps[i].prpsrefresh = true;
					prps[i].repaint();
					colorcard[i].repaint();
				}
				else if(index == 4)
				{
					prps[i].drawchange = true;
					prps[i].prpsrefresh = true;
					prps[i].repaint();
					colorcard[i].repaint();
				}
				*/
					prps[i].drawchange = false;

					switch(indexgroup[unitchannel])
					{
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
					case 8:
						prps[unitchannel].drawchange = true;
						//this.unitsetJComboBox[i].setSelectedIndex(0);
						break;
					}
					prps[i].drawAz = false;
					switch(indexAz[Az])
					{
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
						prps[Az].drawAz = true;
						//this.unitsetJComboBox[i].setSelectedIndex(0);
						break;
					}
					prps[i].prpsrefresh = true;
					//mana-System.out.println("i:"+i+"    "+prps[i].prpsrefresh);
					prps[i].repaint();
					colorcard[i].repaint();
				}




				//if(index == 4)
				//{
					//prps[i].drawchange = true;
					//prps[i].prpsrefresh = true;
					//prps[i].repaint();
					//colorcard[i].repaint();
				//}

			if (drawcnt >= 1 && prpd[i].isVisible())
			{

				indexgroup[i] = this.unitsetJComboBox[i].getSelectedIndex();//选择单位
				prpd[i].drawchange = false;
				switch(indexgroup[unitchannel])
				{
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
				case 8:
					prpd[unitchannel].drawchange = true;
					//this.unitsetJComboBox[i].setSelectedIndex(0);
					break;
				}
				prpd[i].prpdrefresh = true;
				prpd[i].repaint();
				colorcard[i].repaint();
			}
		}

		if (drawcnt >= 8)
			drawcnt = 0;
	}

	@SuppressWarnings("unchecked")
	public GisGUI(GisXYZ gismmi)
	{
		int i = 0;
		//JPanel jpanel;
		chno = 8;
		drawcnt = 0;
		suspendButtonPRPS = new JToggleButton[8];
		suspendButtonPRPD = new JToggleButton[8];
		phaseScrollBar = new JScrollBar[chno];
		noisegate = new JCheckBox[chno];
		ultrasonic = new JCheckBox[chno];//超声波复选框
		offsetAdjustScrollBar = new JScrollBar[chno];
		gainScrollBar = new JScrollBar[chno];//增益滑动条
		phaseSelButton = new JRoundButton[8];
		offsetSelButton = new JRoundButton[8];
		gainSelButton = new JRoundButton[8];//增益按钮
		jradiobutton2 = new JRadioButton[8];
		jradiobutton = new JRadioButton[8];
		jradiobutton_fly = new JRadioButton[8];
		jradiobutton_waveform = new JRadioButton[8];
		jradiobutton_prpscon = new JRadioButton[8];
		jradiobutton1 = new JRadioButton[8];
		jradiobutton3 = new JRadioButton[8];
		jradiobutton_con = new JRadioButton[8];
		jradiobutton_ultral = new JRadioButton[8];
		jradiobutton_prpdwave = new JRadioButton[8];
		jradiobutton_columprpd = new JRadioButton[8];
		strlabelname=new JLabel[8];


		ph_jlabel = new JLabel[8];
		ga_jlabel = new JLabel[8];
		th_jlabel = new JLabel[8];
		trglabel = new JLabel[8];

		chclicked = new Boolean[8];
		String as[] = {
			"0", "10", "20", "30", "40", "50", "60", "70", "80", "90"
		};

		gm = gismmi;
		JFrame.setDefaultLookAndFeelDecorated(false);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		sd = toolkit.getScreenSize();
		fullPane = new JTabbedPane();
		jpanel = new JPanel(new BorderLayout());
		Hprpdscrollpane = new JScrollPane[8];
		//scan = new Scanner();
		unitsettext = (new String[]{
				"dBm","mW","mV","pC","dB"
		});
		sAz = (new String[]{
				"x1","x10","x100"
		});//倍率
		unitsetJComboBox = new JComboBox[8];
		jAz = new JComboBox[8];//增益
		indexAz = new int[8];//增益
		indexgroup = new int[8];


		JPanel jpanel1 = new JPanel(new BorderLayout());
		//full = new JFrame((new StringBuilder()).append(ChooseLang.M[1]).append(GisXYZ.addr).append(" )").toString());
		full = new JFrame((new StringBuilder()).append(ChooseLang.M[1]).toString());//设置框架标题
		try
		{
			full.setIconImage(ImageIO.read(new File("pics/Logo1.png")));
			//full.setIconImage(ImageIO.read(new File("pics/icon_128.png")));
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		dp = new HbDesktopPane(this);
		memoPanel = new MemoPanel(gismmi);
		jpanel.add(dp, "Center");
		jpanel.add(dp.getMenuBar(), "North");
		jpanel.add(memoPanel, "South");
		//full.setSize(1136, 768);
		//获取屏幕边界
		Insets screenInsets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(full.getGraphicsConfiguration());
		int taskHeight = screenInsets.bottom;
		//full.setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize().width, java.awt.Toolkit.getDefaultToolkit().getScreenSize().height - taskHeight);
		prps = new GisGraph[chno];
		prpsFrame = new JPanel[chno];
		colorcard = new ColorCard[chno];
		prpd = new PRPDGraph[chno];
		prpdFrame = new JPanel[chno];
		chCPanel = new JPanel[8];
		description = new JPanel[8];//滑动条描述
		prpdjpanel = new JPanel[chno];
		getObject = new Object[chno];//单位选择对象

		addjcheckbox = new JCheckBox[chno];//累积

		//初始化4通道变量
		for (;i < chno;i++)
		{
		prps[i] = new GisGraph(64, 80, gismmi.gdp.gd.bufferd[i], gismmi.gdp.gd.rtb[i].AedispPRPD , 5120);
		colorcard[i] = new ColorCard(64,80);
		prps[i].ltdataf = gismmi.gdp.gd.an[i].ltm.ltdataf;
		//prps[i].stringToDraw += i + 1;
		prps[i].stringToDraw = "";
		prps[i].stringToDraw += ChooseLang.M[i + 132];
		prps[i].stringToDraw += "PRPS";
		this.ultrasonic[i] = new JCheckBox(ChooseLang.M[330]);
		this.ultrasonic[i].setSelected(false);
		this.selectunit = i;
		prpsFrame[i] = new JPanel(new BorderLayout());
		colorcard[i].setPreferredSize(new Dimension(15,283));
		if (i < chno - 1)
		{
			prpsFrame[i].setBorder(BorderFactory.createEtchedBorder());
		}
		else
		{
			prpsFrame[i].setBorder(BorderFactory.createTitledBorder(ChooseLang.M[15]));//Noise����
		}
		prpsFrame[i].add(colorcard[i],BorderLayout.WEST);
		prpsFrame[i].add(prps[i]);//mana-添加PRPS绘制区域
		dp.add(prpsFrame[i]);//mana-添加PRPS区域
		prpsFrame[i].setSize(256, 283);
		prpsFrame[i].setLocation(i * 256, 0);

		ButtonGroup buttongroup = new ButtonGroup();
		jradiobutton[i] = new JRadioButton("2D");//PRPS2D显示
		jradiobutton2[i] = new JRadioButton("3D");//PRPS3D显示
		jradiobutton_prpscon[i] = new JRadioButton(ChooseLang.M[331]);//连续图
		jradiobutton_fly[i] = new JRadioButton(ChooseLang.M[330]);//飞行图
		jradiobutton_waveform[i] = new JRadioButton(ChooseLang.M[333]);//波形图
		jradiobutton[i].setFont(GisXYZ.fontG);
		jradiobutton2[i].setFont(GisXYZ.fontG);
		jradiobutton_fly[i].setFont(GisXYZ.fontG);
		jradiobutton_waveform[i].setFont(GisXYZ.fontG);
		jradiobutton_prpscon[i].setFont(GisXYZ.fontG);
		buttongroup.add(jradiobutton[i]);
		buttongroup.add(jradiobutton2[i]);
		buttongroup.add(jradiobutton_fly[i]);
		buttongroup.add(jradiobutton_waveform[i]);
		buttongroup.add(jradiobutton_prpscon[i]);

		jradiobutton_waveform[i].setVisible(false);
		/*
		if(i == 2)
		{
			jradiobutton[i].setVisible(false);
			jradiobutton2[i].setVisible(false);
			jradiobutton_prpscon[i].setSelected(true);

			colorcard[i].setVisible(false);
			prps[i].stringToDraw = "";
			//prps[j].stringToDraw += ChooseLang.M[j + 132];
			//prps[j].stringToDraw += j + 1;
			prps[i].stringToDraw += ChooseLang.M[331];
			//prps[j].Waveform = true;//显示波形图
			prps[i].draw3D = false;
			prps[i].isflygraphic = false;
			prps[i].isCon = true;
			prps[i].prpsrefresh = true;
			prps[i].repaint();
		}
		else
		{
			jradiobutton2[i].setSelected(true);//默认3D
			jradiobutton_fly[i].setVisible(false);
		}
		*/
		//3、4通道显示超声波
//		if(i == 5 || i == 6)
		if(false)
		{
			jradiobutton[i].setVisible(false);
			jradiobutton2[i].setVisible(false);
			jradiobutton_prpscon[i].setSelected(true);

			colorcard[i].setVisible(false);
			prps[i].stringToDraw = "";
			//prps[j].stringToDraw += ChooseLang.M[j + 132];
			//prps[j].stringToDraw += j + 1;
			prps[i].stringToDraw += ChooseLang.M[331];
			//prps[j].Waveform = true;//显示波形图
			prps[i].draw3D = false;
			prps[i].isflygraphic = false;
			prps[i].isCon = true;
			prps[i].prpsrefresh = true;
			prps[i].repaint();
		}
		else
		{
			jradiobutton2[i].setSelected(true);//默认3D
			jradiobutton_fly[i].setVisible(false);
		}

		//jradiobutton.addActionListener(new Disp2DPRPS(i));//2DPRPS侦听
		jradiobutton[i].addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionevent) {

				for(int j = 0; j < 8; j++){
					if(jradiobutton[j].isSelected()){
						colorcard[j].setVisible(true);
						prps[j].draw3D = false;
						prps[j].stringToDraw = "";
						prps[j].stringToDraw += ChooseLang.M[j + 132];
						prps[j].stringToDraw += "PRPS";
						prps[j].isflygraphic = false;
						prps[j].Waveform = false;
						prps[j].prpsrefresh = true;
						prps[j].repaint();
					}
				}
			}
		});//2DPRPS侦听
		//jradiobutton2[i].addActionListener(new Disp3DPRPS(i));//3DPRPS侦听
		jradiobutton2[i].addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent actionevent) {
				for(int j = 0; j< 8; j++){
					if(jradiobutton2[j].isSelected()){
						colorcard[j].setVisible(true);
						prps[j].rdeg = 2.3561944901923448D;
						prps[j].tdeg = 0.53407075111026492D;
						prps[j].draw3D = true;
						prps[j].stringToDraw = "";
						prps[j].stringToDraw += ChooseLang.M[j + 132];
						prps[j].stringToDraw += "PRPS";
						prps[j].prpsrefresh = true;
						prps[j].repaint();
					}
				}
			}

		});//3DPRPS侦听

		jradiobutton_fly[i].addActionListener(new ActionListener(){//飞行图侦听

			@Override
			public void actionPerformed(ActionEvent actionevent) {
				for(int j = 0; j< 8; j++){
					if(jradiobutton_fly[j].isSelected()){
						colorcard[j].setVisible(false);
						prps[j].stringToDraw = "";
						//prps[j].stringToDraw += ChooseLang.M[j + 132];
						//prps[j].stringToDraw += j + 1;
						prps[j].stringToDraw += ChooseLang.M[330];
						prps[j].isflygraphic = true;//显示飞行图
						prps[j].draw3D = false;
						prps[j].Waveform = false;
						prps[j].prpsrefresh = true;
						prps[j].repaint();
					}
				}
			}

		});
		//波形图侦听
		jradiobutton_waveform[i].addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent actionevent) {
				for(int j = 0; j< 8; j++){
					if(jradiobutton_waveform[j].isSelected()){
						colorcard[j].setVisible(false);
						prps[j].stringToDraw = "";
						prps[j].stringToDraw += ChooseLang.M[j + 132];
						//prps[j].stringToDraw += j + 1;
						prps[j].stringToDraw += ChooseLang.M[333];
						prps[j].Waveform = true;//显示波形图
						prps[j].draw3D = false;
						prps[j].isflygraphic = false;
						prps[j].prpsrefresh = true;
						prps[j].repaint();
					}
				}
			}

		});
		//连续图侦听
		jradiobutton_prpscon[i].addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent actionevent) {
				for(int j = 0; j< 8; j++){
					if(jradiobutton_prpscon[j].isSelected()){
						colorcard[j].setVisible(false);
						prps[j].stringToDraw = "";
						//prps[j].stringToDraw += ChooseLang.M[j + 132];
						//prps[j].stringToDraw += j + 1;
						prps[j].stringToDraw += ChooseLang.M[331];
						//prps[j].Waveform = true;//显示波形图
						prps[j].draw3D = false;
						prps[j].isflygraphic = false;
						prps[j].isCon = true;
						prps[j].prpsrefresh = true;
						prps[j].repaint();
					}
				}
			}

		});

		/*
		if(i > 1 && i < 3)//3通道默认超声波2D
		{
			prps[i].prpsrefresh = true;
			prps[i].draw3D = false;
			prps[i].isflygraphic = false;
			prps[i].Waveform = false;
			jradiobutton[i].setSelected(true);
			jradiobutton2[i].setVisible(false);
			prps[i].stringToDraw = "";
			prps[i].stringToDraw += ChooseLang.M[i + 132];
			prps[i].stringToDraw += "PRPS";
			prps[i].repaint();
		}
		else
		{
			jradiobutton_fly[i].setVisible(false);//其他通道飞行图不显示

		}
		if(i < 3)
		{
			jradiobutton_waveform[i].setVisible(false);//波形图不显示除高频CT
		}
		*/
		JPanel jpanel2 = new JPanel(new FlowLayout(0));
		//JPanel jpanel4 = new JPanel(new GridLayout(1, 3));
		JPanel jpanel4 = new JPanel();
		jpanel4.add(jradiobutton[i]);
		jpanel4.add(jradiobutton2[i]);
		/*
		if(i == 2)
		{
			jpanel4.add(jradiobutton_prpscon[i]);
		}
		*/
		//3、4通道连续图单选框
//		if(i == 5 || i == 6)
		if(false)
		{
			jpanel4.add(jradiobutton_prpscon[i]);
		}

		jpanel4.add(jradiobutton_fly[i]);
		jpanel4.add(jradiobutton_waveform[i]);
		jpanel2.add(jpanel4);

		JLabel unitlabel = new JLabel(ChooseLang.M[60]);
		JLabel Az = new JLabel("增益：");
		unitlabel.setFont(GisXYZ.fontG);
		Az.setFont(GisXYZ.fontG);
		unitsetJComboBox[i] = new JComboBox<String>(unitsettext);
		unitsetJComboBox[i].setFont(GisXYZ.fontG);
		unitsetJComboBox[i].setSelectedItem(unitsettext[0]);
		//倍率或增益
		jAz[i] = new JComboBox<String>(sAz);
		jAz[i].setFont(GisXYZ.fontG);
		jAz[i].setSelectedItem(sAz[0]);
		/*
		if(i == 2)
		{
			unitsetJComboBox[i].setSelectedItem(unitsettext[2]);
			prps[i].unitfault = 2;//默认mV
		}
		*/
		//3、4通道默认单位及下拉菜单
//		if(i == 5 || i == 6)
		if(false)
		{
			unitsetJComboBox[i].setSelectedItem(unitsettext[2]);
			prps[i].unitfault = 2;//默认mV
			jpanel2.add(Az);
			jpanel2.add(jAz[i]);
		}
		jAz[i].addItemListener(new AzSelect(i));
		unitsetJComboBox[i].addItemListener(new UnitSelect(i));
		if(i == 0 || i == 1|| i == 2|| i == 3|| i == 4 || i == 5|| i == 6|| i == 7)
		{
		jpanel2.add(unitlabel);
		jpanel2.add(unitsetJComboBox[i]);


		//61 添加通道显示
//		if(i<=5) {
//			strlabelname[i]=new JLabel("ch1");          			//61 数据说明
//			strlabelname[i].setAlignmentX(Component.RIGHT_ALIGNMENT);
//			jpanel2.add(strlabelname[i]);
//		}


		}

		//jpanel2.add(this.ultrasonic[i]);
		//this.ultrasonic[i].setVisible(true);
		suspendButtonPRPS[i] = new JToggleButton(ChooseLang.M[20]);
		suspendButtonPRPS[i].setVisible(false);//暂停键不可用
		//suspendButtonPRPS[i].setPreferredSize(new Dimension(75, 30));//设置暂停，实时键的大小
		suspendButtonPRPS[i].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				for (int j = 0; j < 8; j++)
				{
					if (suspendButtonPRPS[j].isSelected() == true)
					{
						suspendButtonPRPS[j].setText(ChooseLang.M[21]);//实时
						suspendButtonPRPD[j].setSelected(true);
						suspendButtonPRPD[j].setText(ChooseLang.M[21]);
					} else
					{
						suspendButtonPRPS[j].setText(ChooseLang.M[20]);//暂停
						suspendButtonPRPD[j].setSelected(false);
						suspendButtonPRPD[j].setText(ChooseLang.M[20]);
					}
				}
			}
		});
		jpanel2.add(suspendButtonPRPS[i]);
		prpsFrame[i].add(jpanel2, "South");

		jpanel4.setBorder(new EtchedBorder(1));
		prpd[i] = new PRPDGraph(64, 64, gismmi.gdp.gd.rtb[i].dispPRPD, gismmi.gdp.gd.buffer_utral[i]);
		//prpd[i].stringToDraw += i + 1;
		prpd[i].stringToDraw = "";
		/*
		if(i == 0 || i == 1 || i == 3)
		{
			prpd[i].stringToDraw += ChooseLang.M[i + 132];
			prpd[i].stringToDraw += "PRPD";
		}
		else
		{
			prpd[i].stringToDraw += ChooseLang.M[334];
		}
	    */
		//1、2通道字符显示
		if(i == 0 || i == 1|| i == 2|| i == 3|| i == 4 || i == 5|| i == 6|| i == 7)
		{
			prpd[i].stringToDraw += ChooseLang.M[i + 132];
			prpd[i].stringToDraw += "PRPD";
		}
		else
		{
			prpd[i].stringToDraw += ChooseLang.M[334];
		}

		prpdFrame[i] = new JPanel(new BorderLayout());
		if (i < chno - 1)
		{
			prpdFrame[i].setBorder(BorderFactory.createEtchedBorder());
		}
		else
		{
			prpdFrame[i].setBorder(BorderFactory.createTitledBorder(ChooseLang.M[15]));
		}
		//prpdjpanel[i] = new JPanel(new GridLayout(1, 0));
		//prpdjpanel[i].setPreferredSize(new Dimension(800,64));
		//prpdjpanel[i].add(prpd[i],"WEST");
		//Hprpdscrollpane[i] = new JScrollPane(prpdjpanel[i],JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//prpdFrame[i].add(Hprpdscrollpane[i]);

		prpdFrame[i].add(prpd[i], "Center");//mana-添加PRPD绘制区域
		dp.add(prpdFrame[i]);//mana-添加PRPD区域
		prpdFrame[i].setSize(256, 283);
		prpdFrame[i].setLocation(i * 256, 283);
		ButtonGroup buttongroup1 = new ButtonGroup();
		jradiobutton1[i] = new JRadioButton("2D");//2D显示选项按钮
		jradiobutton3[i] = new JRadioButton("3D");//3D显示选项按钮
		jradiobutton1[i].setVisible(false);
		jradiobutton3[i].setVisible(false);
		jradiobutton_columprpd[i] = new JRadioButton(ChooseLang.M[334]);//prpd相位幅值图
		jradiobutton_con[i] = new JRadioButton(ChooseLang.M[331]);//连续图选项按钮
		jradiobutton_ultral[i] = new JRadioButton(ChooseLang.M[332]);//超声波
		jradiobutton_prpdwave[i] = new JRadioButton(ChooseLang.M[333]);//波形图PRPD
		jradiobutton1[i].setFont(GisXYZ.fontG);
		jradiobutton3[i].setFont(GisXYZ.fontG);
		jradiobutton_con[i].setFont(GisXYZ.fontG);
		jradiobutton_ultral[i].setFont(GisXYZ.fontG);
		jradiobutton_prpdwave[i].setFont(GisXYZ.fontG);
		jradiobutton_columprpd[i].setFont(GisXYZ.fontG);
		buttongroup1.add(jradiobutton1[i]);
		buttongroup1.add(jradiobutton3[i]);
		buttongroup1.add(jradiobutton_columprpd[i]);
		buttongroup1.add(jradiobutton_con[i]);
		buttongroup1.add(jradiobutton_ultral[i]);
		buttongroup1.add(jradiobutton_prpdwave[i]);
		//jradiobutton1[i].setSelected(true);
		//3、4通道初始化
//		if(i == 5 || i == 6)
		if(false)
		{
			//prpd[i].draw2D = false;
			//prpd[i].draw3D = false;
			//prpd[i].drawCon = false;
			//prpd[i].drawUltral = false;
			//prpd[i].Waveform = false;
			prpd[i].drawColum = true;
			jradiobutton_columprpd[i].setSelected(true);
			prpd[i].unitfault = 2;
		}
		else
		{
//			prpd[i].draw2D = false;
//			prpd[i].draw3D = true;
//			jradiobutton3[i].setSelected(true);
			prpd[i].draw2D = true;
			prpd[i].draw3D = false;
			jradiobutton1[i].setSelected(true);

		}

		//jradiobutton1.addActionListener(new Disp2DPRPD(i));
		//jradiobutton3.addActionListener(new Disp3DPRPD(i));
		//jradiobutton_con.addActionListener(new Disp2DConGraphic(i));

		JPanel jpanel3 = new JPanel(new FlowLayout(0));
		//JPanel jpanel5 = new JPanel(new GridLayout(1, 4));
		JPanel jpanel5 = new JPanel();
		jpanel5.add(jradiobutton1[i]);
		jpanel5.add(jradiobutton3[i]);
		jpanel5.add(jradiobutton_columprpd[i]);//相位幅值柱形图
		jpanel5.add(jradiobutton_con[i]);//连续图
		jpanel5.add(jradiobutton_ultral[i]);//超声波
		jpanel5.add(jradiobutton_prpdwave[i]);//波形图
		jradiobutton_ultral[i].setVisible(false);//不绘制原始波形
		/*
		if(i == 2)
		{
			jradiobutton1[i].setVisible(false);//不绘制次数幅值相位图2D
			jradiobutton3[i].setVisible(false);//不绘制次数幅值相位图3D
			jradiobutton_con[i].setVisible(false);//连续图不显示
		}
		else
		{
			jradiobutton_columprpd[i].setVisible(false);
			jradiobutton_con[i].setVisible(false);
			if(i == 3)
			{
				jradiobutton_prpdwave[i].setVisible(true);
			}
			else
			{
				jradiobutton_prpdwave[i].setVisible(false);
			}

		}
		*/
		//3、4通道初始化
//		if(i == 5 || i == 6)
		if(false)
		{
			jradiobutton1[i].setVisible(false);//不绘制次数幅值相位图2D
			jradiobutton3[i].setVisible(false);//不绘制次数幅值相位图3D
			jradiobutton_con[i].setVisible(false);//连续图不显示
		}
		else
		{
			jradiobutton_columprpd[i].setVisible(false);
			jradiobutton_con[i].setVisible(false);
			//if(i == 3)
			//{
				jradiobutton_prpdwave[i].setVisible(true);
			//}
			//else
			//{
				jradiobutton_prpdwave[i].setVisible(false);
			//}

		}

		jpanel3.add(jpanel5);

		//2D
		jradiobutton1[i].addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				for(int j = 0;j < 8; j++){
					if(jradiobutton1[j].isSelected())
					{
						prpd[j].prpdrefresh = true;
						prpd[j].draw3D = false;
						prpd[j].drawCon = false;
						prpd[j].drawUltral = false;
						prpd[j].drawColum = false;
						prpd[j].Waveform = false;
						prpd[j].draw2D = true;
						prpd[j].stringToDraw = "";
						prpd[j].stringToDraw += ChooseLang.M[j + 132];
					    prpd[j].stringToDraw += "PRPD";
						prpd[j].repaint();
					}
				}
			}

		});
		//3D
		jradiobutton3[i].addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				for(int j = 0;j < 8; j++){
					if(jradiobutton3[j].isSelected())
					{
						prpd[j].prpdrefresh = true;
						prpd[j].draw2D = false;
						prpd[j].drawCon = false;
						prpd[j].drawUltral = false;
						prpd[j].drawColum = false;
						prpd[j].Waveform = false;
						prpd[j].draw3D = true;
						prpd[j].stringToDraw = "";
						prpd[j].stringToDraw += ChooseLang.M[j + 132];
					    prpd[j].stringToDraw += "PRPD";
						prpd[j].repaint();
					}
				}
			}

		});
		//连续
		jradiobutton_con[i].addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				for(int j = 0;j < 8; j++){
					if(jradiobutton_con[j].isSelected())
					{
						prpd[j].prpdrefresh = true;
						prpd[j].draw2D = false;
						prpd[j].draw3D = false;
						prpd[j].drawUltral = false;
						prpd[j].drawColum = false;
						prpd[j].Waveform = false;
						prpd[j].drawCon = true;
						prpd[j].stringToDraw = "";
						prpd[j].stringToDraw += ChooseLang.M[331];
						prpd[j].repaint();
					}
				}
			}
		});
		//超声波
		/*
		if(i > 1 && i < 3)//3通道默认超声波
		{
			prpd[i].prpdrefresh = true;
			prpd[i].draw2D = false;
			prpd[i].draw3D = false;
			prpd[i].drawCon = false;
			prpd[i].drawUltral = true;
			jradiobutton_ultral[i].setSelected(true);
			jradiobutton3[i].setVisible(false);//超声波不显示3D
			prpd[i].stringToDraw = "";
			prpd[i].stringToDraw += ChooseLang.M[332];
			prpd[i].repaint();
		}
		else
		{
			jradiobutton_con[i].setVisible(false);//其他通道连续图不显示
			jradiobutton_ultral[i].setVisible(false);//其他通道超声波不显示

		}
		*/
		jradiobutton_ultral[i].addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				for(int j = 0;j < 8; j++){
					if(jradiobutton_ultral[j].isSelected())
					{
						prpd[j].prpdrefresh = true;
						prpd[j].draw2D = false;
						prpd[j].draw3D = false;
						prpd[j].drawCon = false;
						prpd[j].drawColum = false;
						prpd[j].Waveform = false;
						prpd[j].drawUltral = true;
						prpd[j].stringToDraw = "";
						prpd[j].stringToDraw += ChooseLang.M[332];
						prpd[j].repaint();
					}
				}
			}

		});
		//波形图
		jradiobutton_prpdwave[i].addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				for(int j = 0;j < 8; j++){
					if(jradiobutton_prpdwave[j].isSelected())
					{
						prpd[j].prpdrefresh = true;
						prpd[j].draw2D = false;
						prpd[j].draw3D = false;
						prpd[j].drawCon = false;
						prpd[j].drawUltral = false;
						prpd[j].drawColum = false;
						prpd[j].Waveform = true;
						prpd[j].stringToDraw = "";
						prpd[j].stringToDraw += ChooseLang.M[333];
						prpd[j].repaint();
					}
				}

			}

		});

		//prpd相位幅值图
		jradiobutton_columprpd[i].addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				for(int j = 0;j < 8; j++){
					if(jradiobutton_columprpd[j].isSelected())
					{
						prpd[j].prpdrefresh = true;
						prpd[j].draw2D = false;
						prpd[j].draw3D = false;
						prpd[j].drawCon = false;
						prpd[j].drawUltral = false;
						prpd[j].Waveform = false;
						prpd[j].drawColum = true;
						prpd[j].stringToDraw = "";
						prpd[j].stringToDraw += ChooseLang.M[334];
						prpd[j].repaint();
					}
				}

			}

		});

		//设置飞行图和PRPS显示字符
		this.ultrasonic[i].addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				for(int j = 0; j < 8; j++){
					if(ultrasonic[j].isSelected()){
						prps[j].stringToDraw = "";
						prps[j].stringToDraw += ChooseLang.M[j + 132];
						//prps[j].stringToDraw += j + 1;
						prps[j].stringToDraw += ChooseLang.M[330];
						ultrasonic[j].setSelected(true);
						jradiobutton[j].setSelected(true);//2D
						jradiobutton2[j].setVisible(false);//3D
						prps[j].isflygraphic = true;//显示飞行图
						prps[j].draw3D = false;
					}
					else if(!jradiobutton2[j].isVisible()){
						prps[j].stringToDraw = "";
						prps[j].stringToDraw += ChooseLang.M[j + 132];
						//prps[j].stringToDraw += j + 1;
						prps[j].stringToDraw += "PRPS";
						ultrasonic[j].setSelected(false);
						jradiobutton2[j].setSelected(true);//3D
						jradiobutton[j].setSelected(false);
						jradiobutton2[j].setVisible(true);//3D
						prps[j].isflygraphic = false;//显示柱状图
						prps[j].draw3D = true;
					}
				}
			}

		});
		/*
		if(i > 1){
			prps[i].stringToDraw += ChooseLang.M[330];
			ultrasonic[i].setSelected(true);
			ultrasonic[i].setVisible(false);
			jradiobutton[i].setSelected(true);//2D
			jradiobutton2[i].setVisible(false);//3D
			prps[i].draw3D = false;
		}
		*/
		/*
		JCheckBox jcheckbox = new JCheckBox(ChooseLang.M[22]);
		jcheckbox.setFont(GisXYZ.fontG);
		jcheckbox.setSelected(false);
		jpanel3.add(jcheckbox);
		jcheckbox.addActionListener(new PRPDACC(i));
		*/
		addjcheckbox[i] = new JCheckBox(ChooseLang.M[22]);
		addjcheckbox[i].setFont(GisXYZ.fontG);
		addjcheckbox[i].setSelected(false);
		/*
		if(i == 0 || i == 1 || i == 3)
		{
			jpanel3.add(addjcheckbox[i]);
		}
		*/
		if(i == 0 || i == 1|| i == 2|| i == 3|| i == 4 || i == 5|| i == 6|| i == 7)
		{
			jpanel3.add(addjcheckbox[i]);
		}

		addjcheckbox[i].addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {

				for(int j = 0; j < 8; j++)
				{
					if(addjcheckbox[j].isSelected())
					{
						gm.gdp.gd.rtb[j].refreshAccum = true;
						gm.gdp.gd.fb[j].refreshflydata = true;
					}
					else
					{
						gm.gdp.gd.rtb[j].refreshAccum = false;
						gm.gdp.gd.fb[j].refreshflydata = false;
					}
				}
			}

		});

		suspendButtonPRPD[i] = new JToggleButton(ChooseLang.M[20]);
		suspendButtonPRPD[i].setVisible(false);//暂停键不可见
		//suspendButtonPRPD[i].setPreferredSize(new Dimension(75, 30));//设置暂停键的大小
		suspendButtonPRPD[i].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				for (int j = 0; j < 8; j++)
					if (suspendButtonPRPD[j].isSelected())
					{
						suspendButtonPRPD[j].setText(ChooseLang.M[21]);
						suspendButtonPRPS[j].setSelected(true);
						suspendButtonPRPS[j].setText(ChooseLang.M[21]);
					} else
					{
						suspendButtonPRPD[j].setText(ChooseLang.M[20]);
						suspendButtonPRPS[j].setSelected(false);
						suspendButtonPRPS[j].setText(ChooseLang.M[20]);
					}

			}

		});
		jpanel3.add(suspendButtonPRPD[i]);
		/*
		trglabel[i] = new JLabel("优先触发");
		trglabel[i].setForeground(Color.red);
		jpanel3.add(trglabel[i]);
		if(0 <= i && i <= 1)
		{
			trglabel[i].setVisible(false);
		}
		if(i == 2)
		{
			if(prpd[i].amxdBm > prpd[i + 1].amxdBm)
			{
				trglabel[i].setVisible(false);
			}
		}
		if(i == 3)
		{
			if(prpd[i - 1].amxdBm < prpd[i].amxdBm)
			{
				trglabel[i].setVisible(false);
			}
		}
		*/
		prpdFrame[i].add(jpanel3, "South");
		jpanel5.setBorder(new EtchedBorder(1));
		chCPanel[i] = new JPanel(new GridLayout(3, 2));//3行、2列
		chCPanel[i].setBorder(BorderFactory.createEtchedBorder());
		//噪声复选框
		if (i < chno - 1)
		{
			noisegate[i] = new JCheckBox(ChooseLang.M[23]);//噪声门限
			noisegate[i].setVisible(true);//噪声门限
			noisegate[i].setEnabled(false);//61 测量噪声可见
		}
		else
		{
			noisegate[i] = new JCheckBox(ChooseLang.M[24]);//测量噪声
			noisegate[i].setVisible(true);//61 测量噪声可见
			noisegate[i].setSelected(false);
			prpsFrame[i].setBorder(BorderFactory.createEtchedBorder());//Ĭ����ʾ��ܲ��ǲ�������
			prpdFrame[i].setBorder(BorderFactory.createEtchedBorder());
			noisegate[i].addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent itemevent)
				{
					Boolean boolean1 = Boolean.valueOf(noisegate[chno - 1].isSelected());
					for (int j = 0; j < chno - 1; j++)
					{
						noisegate[j].setSelected(boolean1.booleanValue());
						noisegate[j].setEnabled(boolean1.booleanValue());
					}

					if (boolean1.booleanValue())
					{
						prpsFrame[chno - 1].setBorder(BorderFactory.createTitledBorder(ChooseLang.M[15]));
						prpdFrame[chno - 1].setBorder(BorderFactory.createTitledBorder(ChooseLang.M[15]));
					} else
					{
						prpsFrame[chno - 1].setBorder(BorderFactory.createEtchedBorder());
						prpdFrame[chno - 1].setBorder(BorderFactory.createEtchedBorder());
					}
				}
			});
		}
		noisegate[i].setSelected(false);//61 默认噪声选择
//		noisegate[i].setSelected(false);//61 默认噪声不选择
		noisegate[i].setVisible(true);//61 默认噪声显示
//		noisegate[i].setVisible(false);//61 默认噪声显示

		jpanel3.add(noisegate[i]);

		//相位偏移
		phaseScrollBar[i] = new JScrollBar(0, 0, 10, -64, 10);//相位偏移
		phaseScrollBar[i].setToolTipText(ChooseLang.M[310]);
		phaseScrollBar[i].setBackground(new Color(0x9DADFF));
		//phaseScrollBar[i].setBackground(Color.white);
		phaseSelButton[i] = new JRoundButton(ChooseLang.M[25],JRoundButton.ashen);//A
		phaseSelButton[i].setFont(GisXYZ.fontG);
		phaseSelButton[i].setToolTipText(ChooseLang.M[311]);//A,B,C
		phaseSelButton[i].setPreferredSize(new Dimension(70, 10));
		JPanel jpanel6 = new JPanel(new BorderLayout());

		ph_jlabel[i] =new JLabel(ChooseLang.M[310]+":  ");
		ph_jlabel[i].setFont(GisXYZ.fontG);
		jpanel6 = new JPanel(new BorderLayout());
		jpanel6.add(ph_jlabel[i], "West");
		jpanel6.add(phaseSelButton[i], "East");
		jpanel6.add(phaseScrollBar[i], "Center");
		phaseSelButton[i].addActionListener(new SetPSButton(i));
		phaseScrollBar[i].addAdjustmentListener(new SetPScroll(i));
		chCPanel[i].add(jpanel6);
		jpanel6 = new JPanel(new BorderLayout());//空置
		chCPanel[i].add(jpanel6);

		//增益:水平放置，初始值：0，“跨度/扩展大小”：0滚动条所能达到的最大数值(max-extent)，最小值：0，最大值：13dB
		gainScrollBar[i] = new JScrollBar(JScrollBar.HORIZONTAL,0,0,0,13);
		gainScrollBar[i].setToolTipText(ChooseLang.M[44]);
		gainScrollBar[i].setBackground(new Color(0x9DADFF));
		gainSelButton[i] = new JRoundButton("0dB",JRoundButton.ashen);
		gainSelButton[i].setFont(GisXYZ.fontG);
		gainSelButton[i].setToolTipText(ChooseLang.M[44]);
		gainSelButton[i].setPreferredSize(new Dimension(70,10));
		jpanel6 = new JPanel(new BorderLayout());
		ga_jlabel[i] = new JLabel(ChooseLang.M[44] + ":  ");
		ga_jlabel[i].setFont(GisXYZ.fontG);
//		if(i > 4)
//			jpanel6.add(ga_jlabel[i], "West");
		jpanel6.add(gainSelButton[i], "East");
		jpanel6.add(gainScrollBar[i],"Center");
		gainSelButton[i].addActionListener(new SetGSButton(i));
		gainScrollBar[i].addAdjustmentListener(new SetGScroll(i));
		//超高频无增益
//		if(i > 4)
//			chCPanel[i].add(jpanel6);

		//chCPanel[i].add(jpanel6);
		jpanel6 = new JPanel(new BorderLayout());

		//if(i < 2)
			//chCPanel[i].add(jpanel6);
		//jpanel6 = new JPanel(new BorderLayout());//空置
		//chCPanel[i].add(jpanel6);
		//gainSelButton[i].setVisible(false);//不可见
		//gainScrollBar[i].setVisible(false);

		//阀值
		offsetAdjustScrollBar[i] = new JScrollBar(0, GisSignal.calMid_dBm, 2, GisSignal.calMin_dBm, GisSignal.calMax_dBm);
		offsetAdjustScrollBar[i].setToolTipText(ChooseLang.M[312]);
		offsetAdjustScrollBar[i].setBackground(new Color(0x9DADFF));
		if (GisSignal.isAuto[i])
		{
			offsetSelButton[i] = new JRoundButton("AUTO",JRoundButton.ashen);
		}
		else
		{
			offsetSelButton[i] = new JRoundButton((new StringBuilder()).append("").append(GisSignal.calMid_dBm).toString(),JRoundButton.ashen);
			offsetSelButton[i].setFont(GisXYZ.fontG);
		}
		offsetSelButton[i].setPreferredSize(new Dimension(70, 10));
		offsetSelButton[i].setToolTipText(ChooseLang.M[313]);
		jpanel6 = new JPanel(new BorderLayout());
		th_jlabel[i] = new JLabel(ChooseLang.M[314] + ":  ");
		th_jlabel[i].setFont(GisXYZ.fontG);
		jpanel6.add(th_jlabel[i], "West");
		jpanel6.add(offsetSelButton[i], "East");
		jpanel6.add(offsetAdjustScrollBar[i], "Center");
		offsetSelButton[i].addActionListener(new SetOSButton(i));
		offsetAdjustScrollBar[i].addAdjustmentListener(new SetOAScroll(i));
		chCPanel[i].add(jpanel6);

		//description[i] = new JPanel(new GridLayout(3, 0));
		//JPanel jpanel7 = new JPanel(new BorderLayout());
		//jpanel7.add(offsetSelButton[i]);

		//dp.add(description[i]);
		dp.add(chCPanel[i]);//mana-添加相位与幅值设置滚动条
		chCPanel[i].setSize(256, 80);
		chCPanel[i].setLocation(i * 256, 566);
		chCPanel[i].setVisible(false);
		prpdFrame[i].setVisible(false);
		prpsFrame[i].setVisible(false);

		jpopmenuzoomprps = new JPopupMenu();
		jmenuitemzoomprps = new JMenuItem(ChooseLang.M[33]);
		jmenuitemzoomprps.setFont(GisXYZ.fontG);
		jmenuitemzoomprps.addMouseListener(new RightPRPSClicked());
		jpopmenuzoomprps.add(jmenuitemzoomprps);
		prps[i].add(jpopmenuzoomprps);
		prps[i].addMouseListener(new myzoomPRPS(i));//右键侦听

		jpopmenuzoomprpd = new JPopupMenu();
		jmenuitemzoomprpd = new JMenuItem(ChooseLang.M[33]);
		jmenuitemzoomprpd.addMouseListener(new RightPRPDClicked());
		jpopmenuzoomprpd.add(jmenuitemzoomprpd);
		prpd[i].add(jpopmenuzoomprpd);
		prpd[i].addMouseListener(new myzoomPRPD(i));

		prps[i].addMouseListener(new PRPSClicked(i));
		prpd[i].addMouseListener(new PRPDClicked(i));

		}
		fullPane.setFont(GisXYZ.fontG);
		fullPane.addTab(ChooseLang.M[21], jpanel);//实时
		fullPane.addTab(ChooseLang.M[28], gismmi.ge);//数据浏览器
		//fullPane.addTab("测试", scan);
		//fullPane.setTabPlacement(fullPane.LEFT);
		//fullPane.setBackground(Color.LIGHT_GRAY);
		//fullPane.setForeground(Color.BLUE);
		full.add(fullPane);
		full.setDefaultCloseOperation(3);
		full.setLocationByPlatform(true);
		//full.setLocation((sd.width - java.awt.Toolkit.getDefaultToolkit().getScreenSize().width) / 2, (sd.height - java.awt.Toolkit.getDefaultToolkit().getScreenSize().height) / 2);
		//full.setLocation((java.awt.Toolkit.getDefaultToolkit().getScreenSize().width - full.getWidth()) / 2, (java.awt.Toolkit.getDefaultToolkit().getScreenSize().height - taskHeight - full.getHeight()) / 2);
		//full.setAlwaysOnTop(true);
		//full.setResizable(true);
		//这里设置全屏
		//full.setExtendedState(JFrame.MAXIMIZED_BOTH);//初始最大化
		if (sd.height>768 && sd.width>1366)
		//full.setUndecorated(true);//frame框体不显示
		//full.getGraphicsConfiguration().getDevice().setFullScreenWindow(full);
		full.setSize(1366, 768);
		full.setVisible(true);


		dp.shownCh();//初始化上次已选择的通道
		//dp.showAllWindows();//初始化显示所有通道
		}

	//这里双击使用
	public Boolean getOldclick() {
		return oldclick;
	}

	public void setOldclick(Boolean oldclick) {
		this.oldclick = oldclick;
	}
}