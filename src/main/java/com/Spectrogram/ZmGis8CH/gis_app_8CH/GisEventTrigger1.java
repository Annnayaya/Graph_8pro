package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

class GisEventTrigger1 extends JFrame
{	
	private static final long serialVersionUID = 197307030010L;
	final String autoTrgSelText1[];
	final String channelText1[];
	JComboBox<String> autoTrgSel1;
	JComboBox<String> Channel_Select1;
	JRoundButton saveTimer1;
	JRadioButton jb1[];
	int prevTrigger1;
	boolean autoStarted1;
	int ampSel1;
	boolean enableSaving1;
	boolean alwaysSaving1;
	int trgType1;

	public GisEventTrigger1(GisChannelData gischanneldata)
	{
		autoTrgSelText1 = (new String[] {
			ChooseLang.M[202], ChooseLang.M[203], ChooseLang.M[204], ChooseLang.M[205], ChooseLang.M[206]
		});
		channelText1 = (new String[]{
				"通道1","通道2","通道3","通道4","所有通道"
		});
		saveTimer1 = new JRoundButton("[ 00:00 ]",JRoundButton.ashen);
		saveTimer1.setPreferredSize(new Dimension(70,25));			
		JPanel jpanel = new JPanel();
		JPanel jpanel1 = new JPanel(new GridLayout(3, 0));
		ButtonGroup buttongroup = new ButtonGroup();
		jb1 = new JRadioButton[4];
		autoTrgSel1 = new JComboBox<String>(autoTrgSelText1);
		autoTrgSel1.setFont(GisXYZ.fontG);
		autoTrgSel1.setSelectedItem(autoTrgSelText1[2]);
		autoTrgSel1.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				jb1[0].setSelected(true);
				setTriggerAuto(autoTrgSel1.getSelectedIndex());
				prevTrigger1 = 0;
			}

		});
		jb1[0] = new JRadioButton(ChooseLang.M[380]);
		jb1[0].setFont(GisXYZ.fontG);
		jb1[0].setSelected(true);
		buttongroup.add(jb1[0]);
		jb1[0].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				setTriggerAuto(autoTrgSel1.getSelectedIndex());
				prevTrigger1 = 0;
			}

		});
		JPanel jpanel2 = new JPanel(new BorderLayout());
		jpanel2.add(jb1[0], "West");
		jpanel1.add(jpanel2);
		
		jb1[1] = new JRadioButton(ChooseLang.M[381]);
		jb1[1].setFont(GisXYZ.fontG);
		buttongroup.add(jb1[1]);
		jb1[1].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				prevTrigger1 = 1;
			}

		});
		jpanel2 = new JPanel(new BorderLayout());
		jpanel2.add(jb1[1], "West");
		jpanel1.add(jpanel2);
		
		jb1[2] = new JRadioButton(ChooseLang.M[383]);
		jb1[2].setFont(GisXYZ.fontG);
		buttongroup.add(jb1[2]);
		jb1[2].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent)
			{
				setTriggerDisabled();
				prevTrigger1 = 2;
			}
		});		
		jpanel2 = new JPanel(new BorderLayout());
		jpanel2.add(jb1[2], "Center");
		jpanel1.add(jpanel2);
		
		
		jb1[3] = new JRadioButton(ChooseLang.M[383]);
		jb1[3].setFont(GisXYZ.fontG);
		buttongroup.add(jb1[3]);
		jb1[3].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent)
			{
				setTriggerDisabled();
				prevTrigger1 = 3;
			}
		});		
		jpanel2 = new JPanel(new BorderLayout());
		jpanel2.add(jb1[3], "Center");
		jpanel1.add(jpanel2);
		
		
		jpanel1.setBorder(BorderFactory.createTitledBorder(ChooseLang.M[384]));
		jpanel.add(jpanel1);
		jpanel1 = new JPanel(new GridLayout(4, 0));
		Channel_Select1 = new JComboBox<String>(channelText1);
		Channel_Select1.setFont(GisXYZ.fontG);
		Channel_Select1.setSelectedItem(channelText1[4]);
		jpanel1.add(Channel_Select1);
		buttongroup = new ButtonGroup();
		
		add(jpanel);
		pack();
		setResizable(false);
		setAlwaysOnTop(true);
	}

	public void setTriggerAuto(int i)
	{
		trgType1 = 0;
		ampSel1 = i + 1;
		enableSaving1 = true;
		alwaysSaving1 = false;
		
	}

	public void setTriggerNorm()
	{
		trgType1 = 1;
		enableSaving1 = false;
		alwaysSaving1 = true;
		
	}

	public void setTriggerDisabled()
	{
		enableSaving1 = false;
		alwaysSaving1 = false;
		
	}


}
