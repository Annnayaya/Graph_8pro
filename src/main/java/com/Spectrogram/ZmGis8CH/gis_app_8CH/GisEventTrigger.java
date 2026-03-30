package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

class GisEventTrigger extends JFrame
{	
	private static final long serialVersionUID = 197307030010L;
	GisChannelData gd;
	double cnt_trg[];
	double max_trg[];
	double avg_trg[];
	int triggerGap;
	final String autoTrgSelText[];
	final String channelText[];
	JComboBox<String> autoTrgSel;
	JComboBox<String> Channel_Select;
	JRoundButton saveTimer;
	JRadioButton jb[];
	int prevTrigger;
	long allTriggerEndTime;
	long allTriggerStartTime;
	boolean autoStarted;
	final double trgAmp[] = {
		0.0D, 1.1000000000000001D, 1.5D, 2D, 2.5D, 3D
	};
	int ampSel;
	boolean enableSaving;
	boolean alwaysSaving;
	double triggerFreq;
	int trgType;
	int iGET_oldProcTime;

	public GisEventTrigger(GisChannelData gischanneldata)
	{
		triggerGap = 80;
		autoTrgSelText = (new String[] {
			ChooseLang.M[202], ChooseLang.M[203], ChooseLang.M[204], ChooseLang.M[205], ChooseLang.M[206]
		});
		channelText = (new String[]{
				"通道1","通道2","通道3","通道4","所有通道"
		});
		saveTimer = new JRoundButton("[ 00:00 ]",JRoundButton.ashen);
		saveTimer.setPreferredSize(new Dimension(70,25));
		prevTrigger = 0;
		allTriggerEndTime = 0L;
		allTriggerStartTime = 0L;
		autoStarted = false;
		ampSel = 2;
		enableSaving = true;
		alwaysSaving = false;
		triggerFreq = 1.0D;
		trgType = 0;
		iGET_oldProcTime = 0;
		gd = gischanneldata;
		cnt_trg = new double[gischanneldata.chNo];
		max_trg = new double[gischanneldata.chNo];
		avg_trg = new double[gischanneldata.chNo];
		JPanel jpanel = new JPanel();
		JPanel jpanel1 = new JPanel(new GridLayout(3, 0));
		ButtonGroup buttongroup = new ButtonGroup();
		jb = new JRadioButton[4];
		autoTrgSel = new JComboBox<String>(autoTrgSelText);
		autoTrgSel.setFont(GisXYZ.fontG);
		autoTrgSel.setSelectedItem(autoTrgSelText[2]);
		autoTrgSel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				jb[0].setSelected(true);
				setTriggerAuto(autoTrgSel.getSelectedIndex());
				prevTrigger = 0;
			}

		});
		jb[0] = new JRadioButton(ChooseLang.M[207]);
		jb[0].setFont(GisXYZ.fontG);
		jb[0].setSelected(true);
		buttongroup.add(jb[0]);
		jb[0].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				setTriggerAuto(autoTrgSel.getSelectedIndex());
				prevTrigger = 0;
			}

		});
		JPanel jpanel2 = new JPanel(new BorderLayout());
		jpanel2.add(jb[0], "West");
		//jpanel2.add(autoTrgSel, "Center");
		jpanel1.add(jpanel2);
		saveTimer = new JRoundButton("[ 00:00 ]",JRoundButton.ashen);
		saveTimer.setPreferredSize(new Dimension(70,25));
		saveTimer.setFont(GisXYZ.fontG);
		saveTimer.setToolTipText(ChooseLang.M[208]);
		saveTimer.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				jb[1].setSelected(true);
				setTriggerAll(true);
			}

		});
		jb[1] = new JRadioButton(ChooseLang.M[209]);
		jb[1].setFont(GisXYZ.fontG);
		buttongroup.add(jb[1]);
		jb[1].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				setTriggerAll(prevTrigger != 1);
				prevTrigger = 1;
			}

		});
		jpanel2 = new JPanel(new BorderLayout());
		jpanel2.add(jb[1], "West");
		jpanel2.add(saveTimer, "Center");
		jpanel1.add(jpanel2);
		jb[3] = new JRadioButton(ChooseLang.M[210]);//无触发
		jb[3].setFont(GisXYZ.fontG);
		buttongroup.add(jb[3]);
		jb[3].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				setTriggerDisabled();
				prevTrigger = 3;
			}

		});
		
		jpanel2 = new JPanel(new BorderLayout());
		jpanel2.add(jb[3], "Center");
		jpanel1.add(jpanel2);
		jpanel1.setBorder(BorderFactory.createTitledBorder(ChooseLang.M[211]));
		jpanel.add(jpanel1);
		jpanel1 = new JPanel(new GridLayout(4, 0));
		JLabel channelLabel = new JLabel(ChooseLang.M[273]);
		channelLabel.setFont(GisXYZ.fontG);
		jpanel1.add(channelLabel);
		Channel_Select = new JComboBox<String>(channelText);
		Channel_Select.setFont(GisXYZ.fontG);
		Channel_Select.setSelectedItem(channelText[4]);
		jpanel1.add(Channel_Select);
		buttongroup = new ButtonGroup();
		JRadioButton ajradiobutton[] = new JRadioButton[5];
		ajradiobutton[0] = new JRadioButton("dBm");
		buttongroup.add(ajradiobutton[0]);
		jpanel1.add(ajradiobutton[0]);
		ajradiobutton[0].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				//Option.unitType = 0;
				//Option.writeOption();
			}

		});
		ajradiobutton[1] = new JRadioButton("mW");
		buttongroup.add(ajradiobutton[1]);
		jpanel1.add(ajradiobutton[1]);
		ajradiobutton[1].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				//Option.unitType = 1;
				//Option.writeOption();
			}

		});
		ajradiobutton[2] = new JRadioButton("mV");
		buttongroup.add(ajradiobutton[2]);
		jpanel1.add(ajradiobutton[2]);
		ajradiobutton[2].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				//Option.unitType = 2;
				//Option.writeOption();
			}

		});
		ajradiobutton[3] = new JRadioButton("pC");
		buttongroup.add(ajradiobutton[3]);
		jpanel1.add(ajradiobutton[3]);
		ajradiobutton[3].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				//Option.unitType = 3;
				//Option.writeOption();
			}

		});
		ajradiobutton[4] = new JRadioButton("dB");
		buttongroup.add(ajradiobutton[4]);
		jpanel1.add(ajradiobutton[4]);
		ajradiobutton[4].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				//Option.unitType = 4;
				//Option.writeOption();
			}

		});
		for(int i = 0; i < 5; i++)
		{
			ajradiobutton[i].setFont(GisXYZ.fontG);
		}
		ajradiobutton[0].setFont(GisXYZ.fontG);
		ajradiobutton[Option.unitType].setSelected(true);
		jpanel1.setBorder(BorderFactory.createTitledBorder(ChooseLang.M[212]));//设置PRPS单位
		//jpanel.add(jpanel1);
		jpanel1 = new JPanel(new GridLayout(2, 0));
		buttongroup = new ButtonGroup();
		JRadioButton ajradiobutton1[] = new JRadioButton[2];//调整界面色调的单选按钮
		
		ajradiobutton1[0] = new JRadioButton(ChooseLang.M[271]);//正常
		buttongroup.add(ajradiobutton1[0]);
		jpanel1.add(ajradiobutton1[0]);
		ajradiobutton1[0].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				Option.gt = true;
				Option.writeOption();
			}

		});
		ajradiobutton1[1] = new JRadioButton(ChooseLang.M[272]);//反色
		buttongroup.add(ajradiobutton1[1]);
		jpanel1.add(ajradiobutton1[1]);
		ajradiobutton1[1].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				Option.gt = false;
				Option.writeOption();
			}

		});
		for(int j = 0; j < 2; j++)
		{
			ajradiobutton1[j].setFont(GisXYZ.fontG);
		}
		ajradiobutton1[Option.gt ? 0 : 1].setSelected(true);
		jpanel1.setBorder(BorderFactory.createTitledBorder(ChooseLang.M[270]));//ͼ����ɫ�趨��
		//jpanel.add(jpanel1);
		add(jpanel);
		setTitle(ChooseLang.M[213]);
		pack();
		setResizable(false);
		setAlwaysOnTop(true);
	}

	public void setTriggerAuto(int i)
	{
		trgType = 0;
		ampSel = i + 1;
		enableSaving = true;
		alwaysSaving = false;
		System.out.println((new StringBuilder()).append("trigger selected : auto ").append(trgAmp[i + 1]).toString());
	}

	public void setTriggerNorm()
	{
		trgType = 1;
		enableSaving = false;
		alwaysSaving = true;
		System.out.println((new StringBuilder()).append("trigger selected : norm ").append(gd.mmi.ref[0]).append("/").append(gd.mmi.ref[1]).append("/").append(gd.mmi.ref[2]).append("/").append(gd.mmi.ref[3]).toString());
	}

	public void setTriggerDisabled()
	{
		enableSaving = false;
		alwaysSaving = false;
		System.out.println("trigger selected : disabled");
	}

	public void setTriggerAll(boolean flag)
	{
		if (flag)
		{
			String as[] = {
				ChooseLang.M[214], ChooseLang.M[215], ChooseLang.M[216], ChooseLang.M[217], ChooseLang.M[218], ChooseLang.M[219], ChooseLang.M[220]
			};
			long al[] = {
				30000L, 60000L, 0x493e0L, 0x927c0L, 0x1b7740L, 0x36ee80L, 0L
			};
			String s = (String)JOptionPane.showInputDialog(this, ChooseLang.M[221], ChooseLang.M[222], 3, null, as, as[0]);
			if (s == null)
				return;
			int i = 2;
			for (int j = 0; j < as.length; j++)
				if (as[j].matches(s))
					i = j;

			System.out.println((new StringBuilder()).append(s).append(" = ").append(al[i]).toString());
			allTriggerStartTime = System.currentTimeMillis();
			if (al[i] == 0L)
				allTriggerEndTime = 0L;
			else
				allTriggerEndTime = al[i] + System.currentTimeMillis();
			updateAllTrgTimer();
			/*
			if (!gd.mmi.gui.dp.conButton.isSelected())
			{
				gd.mmi.gui.dp.conButton.doClick();
				autoStarted = true;
			} else
			{
				autoStarted = false;
			}
			*/
			//这里由于改用自定义按钮
			if (!gd.mmi.gui.dp.conButton1.isSelected())
			{
				gd.mmi.gui.dp.conButton1.doClick();
				autoStarted = true;
			} else
			{
				autoStarted = false;
			}
		}
		enableSaving = true;
		alwaysSaving = true;
		System.out.println((new StringBuilder()).append("trigger selected : all ").append(allTriggerEndTime).toString());
	}

	public void updateAllTrgTimer()
	{
		System.out.println("updating all-trigger timer");
		if (allTriggerEndTime == 0L)
			saveTimer.setText("[ --:-- ]");
		else
		if (allTriggerEndTime <= System.currentTimeMillis())
		{
			saveTimer.setText("[ 00:00 ]");
			JOptionPane.showMessageDialog(this, ChooseLang.M[223]);
			jb[0].setSelected(true);
			setTriggerAuto(2);
			if (autoStarted)
			{
				gd.mmi.gui.dp.conButton.doClick();				
				autoStarted = false;
			}
			ReportCheck reportcheck = new ReportCheck(1, allTriggerStartTime, allTriggerEndTime, gd.mmi.currentOpenSite, true);
			reportcheck.setLocationRelativeTo(gd.mmi.gui.full);
			reportcheck.setVisible(true);
		} else
		{
			long l = allTriggerEndTime - System.currentTimeMillis();
			l /= 1000L;
			int i = (int)l / 60;
			int j = (int)l % 60;
			saveTimer.setText(String.format("[ %02d:%02d ]", new Object[] {
				Integer.valueOf(i), Integer.valueOf(j)
			}));
		}
	}

	public int triggerProc()
	{
		int j = 0;
		for (int i = 0; i < gd.chNo; i++)
		{
			boolean flag = false;
			if (trgType != 0)
				continue;
			if ((double)gd.an[i].sig_cnt > cnt_trg[i])
				flag = true;
			if ((double)gd.an[i].sig_max > max_trg[i])
				flag = true;
			if ((double)gd.an[i].sig_avg > avg_trg[i])
				flag = true;
			if (flag)
			{
				j |= 1 << i;
				cnt_trg[i] = (double)gd.an[i].sig_cnt * trgAmp[ampSel];
				max_trg[i] = (double)gd.an[i].sig_max * trgAmp[ampSel];
				avg_trg[i] = (double)gd.an[i].sig_avg * trgAmp[ampSel];
				continue;
			}
			double d = 1.0D - triggerFreq * 0.001D;
			cnt_trg[i] *= d;
			max_trg[i] *= d;
			avg_trg[i] *= d;
			if (cnt_trg[i] < 10D)
				cnt_trg[i] = 10D;
			if (max_trg[i] < 10D)
				max_trg[i] = 10D;
			if (avg_trg[i] < 10D)
				avg_trg[i] = 10D;
		}

		int k = (int)(System.currentTimeMillis() / 1000L);
		if (iGET_oldProcTime < k)
		{
			iGET_oldProcTime = k;
			if (alwaysSaving)
			{
				updateAllTrgTimer();
				if (enableSaving)
					return j | 0x10;
			}
		}
		if (alwaysSaving && enableSaving)
			return 0;
		if (triggerGap > 0)
		{
			triggerGap--;
			j = 0;
		}
		if (j > 0)
			triggerGap = 80;
		if (enableSaving)
			return j;
		else
			return 0;
	}
}
