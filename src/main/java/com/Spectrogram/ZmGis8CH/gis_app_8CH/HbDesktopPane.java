package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.*;

class HbDesktopPane extends JPanel
{
	private static final long serialVersionUID = 197307030005L;
	private static int[] chrecord = new int[8];
	public static boolean[] chselect= new boolean[8];
	public static int total = 8;
	public static int tongdao=8;//设置显示通道数
	public static volatile String addr = AppGlobalConfig.getDeviceIp();
	static File file = new File("lang" + File.separator + "save.dat");
	static File filenum = new File("lang" + File.separator + "savenum.dat");
	public static char[] readchar = new char[9];
	public static char[] save = new char[9];
	public static long starttime = 0;//开始测试时间
	public static long currenttime = 0;//当前时间
	public static Timer time;
	public static JLabel jName;
	public static JLabel jport;
	public static String btclicked;
	public static String btnotclicked;

	public static Timer timeronce;
	JLabel test_time;
	ActionListener freshtime;
	ActionListener getonce;
	JLabel dataflow;//数据流量
	private static boolean key;//键
	JRTogButton jbutton1T3; //1-3
	JRTogButton jbutton1T4; //4-6
	public JToggleButton persistentTestButton;
	public JLabel persistentTestStatusLabel;
	public JButton openTrendButton;
	private boolean isUpdatingIp = false;


	class ChannelOpenAL implements ActionListener
	{
		public int ch;
		public void actionPerformed(ActionEvent actionevent)
		{
			if(chrecord[ch] > 0)
			{
				prpsToggle[ch].setSelected(false);
				prpdToggle[ch].setSelected(false);
				chcpToggle[ch].setSelected(false);
				chrecord[ch] = 0;
			}
			else
			{
				prpsToggle[ch].setSelected(true);
				prpdToggle[ch].setSelected(true);
				chcpToggle[ch].setSelected(true);
				chrecord[ch]++;
			}

			if(chselect[ch] == true){
				jbutton1[ch].setBackground(new Color(146,146,146));
				chselect[ch] = false;
				--total;
				try {
					save = readByChars(file);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				save[0] = (char)total;
				save[ch + 1] = chselect[ch] ? (char)1 : (char)0;

				try {
					writeByChars(file,save,0,9);
				} catch (Exception e) {
					e.printStackTrace();
				}
				prpsToggle[ch].setSelected(false);
				prpdToggle[ch].setSelected(false);
				chcpToggle[ch].setSelected(false);
			}
			else if( chselect[ch] == false)
			{
				jbutton1[ch].setBackground(new Color(229,229,229));//通道背景色
				chselect[ch] = true;
				++total;
				try {
					save = readByChars(file);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				save[0] = (char)total;
				save[ch + 1] = chselect[ch] ? (char)1 : (char)0;

				try {
					writeByChars(file,save,0,9);
				} catch (Exception e) {
					e.printStackTrace();
				}
				prpsToggle[ch].setSelected(true);
				prpdToggle[ch].setSelected(true);
				chcpToggle[ch].setSelected(true);
			}
			aiButton();
		}
	}

	//61 1-4 class（注释代码保持不变）
//	class ChannelOpenAL1t3 implements ActionListener
//	{
//		public int ch;
//		public void actionPerformed(ActionEvent actionevent)
//		{
//			if (jbutton1T3.getText().equals(btnotclicked))
//			{
//				jbutton1T3.setText(btclicked);
//				System.out.println(btclicked);
//				System.out.println(jbutton1T3.getText());
//			}
//			else {
//				jbutton1T3.setText(btnotclicked);
//			}
//
//			for (int ch1=0;ch1<8;ch1++) {
//				prpsToggle[ch1].setSelected(false);
//				prpdToggle[ch1].setSelected(false);
//				chcpToggle[ch1].setSelected(false);
//				chrecord[ch1]++;
//			}
//			if(chselect[0] == true){
//				if (jbutton1T3.isSelected()) {
//					for (int ch1=0;ch1<4;ch1++) {
//						prpsToggle[ch1].setSelected(true);
//						prpdToggle[ch1].setSelected(true);
//						chcpToggle[ch1].setSelected(true);
//						chrecord[ch1]++;
//					}
//				}else {
//					for (int ch1=4;ch1<8;ch1++) {
//						prpsToggle[ch1].setSelected(true);
//						prpdToggle[ch1].setSelected(true);
//						chcpToggle[ch1].setSelected(true);
//						chrecord[ch1]++;
//					}
//				}
//			}
//
//			if(chselect[0] == true){
//				for (int ch1=0;ch1<4;ch1++) {
//					jbutton1[ch1].setBackground(new Color(146,146,146));
//					chselect[ch1] = false;
//					--total;
//					try {
//						save = readByChars(file);
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
//					save[0] = (char)total;
//					save[ch1 + 1] = chselect[ch1] ? (char)1 : (char)0;
//
//					try {
//						writeByChars(file,save,0,9);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					prpsToggle[ch1].setSelected(false);
//					prpdToggle[ch1].setSelected(false);
//					chcpToggle[ch1].setSelected(false);
//				}
//				for (int ch1=4;ch1<8;ch1++) {
//					jbutton1[ch1].setBackground(new Color(146,146,146));
//					chselect[ch1] = true;
//					--total;
//					try {
//						save = readByChars(file);
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
//					save[0] = (char)total;
//					save[ch1 + 1] = chselect[ch1] ? (char)1 : (char)0;
//
//					try {
//						writeByChars(file,save,0,9);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					prpsToggle[ch1].setSelected(true);
//					prpdToggle[ch1].setSelected(true);
//					chcpToggle[ch1].setSelected(true);
//				}
//			}
//			else if( chselect[0] == false)
//			{
//				for (int ch1=0;ch1<4;ch1++) {
//					jbutton1[ch1].setBackground(new Color(229,229,229));//通道背景色
//					chselect[ch1] = true;
//					++total;
//					try {
//						save = readByChars(file);
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
//					save[0] = (char)total;
//					save[ch1 + 1] = chselect[ch1] ? (char)1 : (char)0;
//
//					try {
//						writeByChars(file,save,0,9);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					prpsToggle[ch1].setSelected(true);
//					prpdToggle[ch1].setSelected(true);
//					chcpToggle[ch1].setSelected(true);
//				}
//				for (int ch1=4;ch1<8;ch1++) {
//					jbutton1[ch1].setBackground(new Color(229,229,229));//通道背景色
//					chselect[ch1] = false;
//					++total;
//					try {
//						save = readByChars(file);
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
//					save[0] = (char)total;
//					save[ch1 + 1] = chselect[ch1] ? (char)1 : (char)0;
//
//					try {
//						writeByChars(file,save,0,9);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					prpsToggle[ch1].setSelected(false);
//					prpdToggle[ch1].setSelected(false);
//					chcpToggle[ch1].setSelected(false);
//				}
//			}
//			aiButton();
//		}
//	}



	//61 4-6 class（注释代码保持不变）
//	class ChannelOpenAL4t6 implements ActionListener
//	{
//		public int ch;
//		public void actionPerformed(ActionEvent actionevent)
//		{
//			if(chrecord[3] > 0)
//			{
//				for (int ch1=4;ch1<8;ch1++) {
//					prpsToggle[ch1].setSelected(false);
//					prpdToggle[ch1].setSelected(false);
//					chcpToggle[ch1].setSelected(false);
//					chrecord[ch1] = 0;
//				}
//			}
//			else
//			{
//				for (int ch1=4;ch1<8;ch1++) {
//					prpsToggle[ch1].setSelected(true);
//					prpdToggle[ch1].setSelected(true);
//					chcpToggle[ch1].setSelected(true);
//					chrecord[ch1]++;
//				}
//			}
//
//			if(chselect[3] == true){
//				for (int ch1=4;ch1<8;ch1++) {
//					jbutton1[ch1].setBackground(new Color(146,146,146));
//					chselect[ch1] = false;
//					--total;
//					try {
//						save = readByChars(file);
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
//					save[0] = (char)total;
//					save[ch1 + 1] = chselect[ch1] ? (char)1 : (char)0;
//
//					try {
//						writeByChars(file,save,0,9);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					prpsToggle[ch1].setSelected(false);
//					prpdToggle[ch1].setSelected(false);
//					chcpToggle[ch1].setSelected(false);
//				}
//			}
//			else if( chselect[3] == false)
//			{
//				for (int ch1=4;ch1<8;ch1++) {
//					jbutton1[ch1].setBackground(new Color(229,229,229));//通道背景色
//					chselect[ch1] = true;
//					++total;
//					try {
//						save = readByChars(file);
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
//					save[0] = (char)total;
//					save[ch1 + 1] = chselect[ch] ? (char)1 : (char)0;
//
//					try {
//						writeByChars(file,save,0,9);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					prpsToggle[ch1].setSelected(true);
//					prpdToggle[ch1].setSelected(true);
//					chcpToggle[ch1].setSelected(true);
//				}
//			}
//			aiButton();
//		}
//	}



	class MyAL implements ActionListener
	{
		public void actionPerformed(ActionEvent actionevent)
		{
			aiButton();
		}
	}

	class WindowsModel implements KeyListener
	{
		@Override
		public void keyPressed(KeyEvent event) {
			if(event.getKeyCode() == KeyEvent.VK_F11)
				System.out.println("键盘按下");
		}

		@Override
		public void keyReleased(KeyEvent event) {
			if(event.getKeyCode() == KeyEvent.VK_F11)
				System.out.println("键盘释放");
		}

		@Override
		public void keyTyped(KeyEvent event) {
			int keyCode = event.getKeyCode();
			if(keyCode == KeyEvent.VK_F11 && key == false){
				gui.full.dispose();
				gui.full.setUndecorated(true);
				gui.full.getGraphicsConfiguration().getDevice().setFullScreenWindow(gui.full);
				gui.full.setVisible(true);
				key = true;
			}
			else if(keyCode == KeyEvent.VK_F11 && key == true)
			{
				gui.full.dispose();
				gui.full.setUndecorated(false);
				gui.full.getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
				gui.full.setVisible(true);
				key = false;
			}
			else
			{
				event.consume();
			}
		}
	}

	// 组件定义（保持不变）
	JRoundButton trgButton;
	JRoundButton siteManButton;
	JButton nb1;
	JRoundButton gjszButton;
	JRoundButton libButton;
	JLabel bgLabel;
	JDesktopPane dp;
	JPanel mainPanel;
	JLabel statusLabel;
	JToggleButton conButton;
	public static JCircleButton conButton1;
	public static JCircleButton conButtono1;
	GisGUI gui;
	Boolean isFirst;
	JToggleButton prpsToggle[];
	JToggleButton prpdToggle[];
	JToggleButton chcpToggle[];
	GisLibrary glib;
	JFrame jf;
	GisLibraryWindow glw;
	public static JTextField iptext;

	int width;
	int height;
	// 关键修改1：将原单个工具栏拆分为【顶部工具栏】和【底部工具栏】
	JToolBar jtoolbarTop;  // 第一行工具栏：放核心操作（IP、连接、通道按钮）
	JToolBar jtoolbarBottom;// 第二行工具栏：放功能按钮、状态显示、控制项

	//JButton jbutton1[];
	JRTogButton jbutton1[];
	JToggleButton jbutton_test_online;
	KeyBoard okey;
	JRoundButton quitbutton;
	JRoundButton ipbutton;

	public JRoundButton btnPreset1;
	public JRoundButton btnPreset2;
	public JRoundButton btnPreset3;
	public JRoundButton speedConfigButton;

	public HbDesktopPane(GisGUI gisgui)
	{
		isFirst = Boolean.valueOf(true);
		glib = new GisLibrary();
		jf = new JFrame(ChooseLang.M[150]);
		glw = new GisLibraryWindow(jf, glib);
		width = 0;
		height = 0;
		gui = gisgui;
		setLayout(new GridLayout(0, 1));
		mainPanel = new JPanel(new BorderLayout());
		key = false;

		// 初始化背景图（保持不变）
		try
		{
			File bgFile = new File("pics/HBEDSBG.PNG");
			if (!bgFile.exists()) {
				bgFile = new File("pics/HBEDSBG.png");
			}
			if (bgFile.exists()) {
				bgLabel = new JLabel(new ImageIcon(ImageIO.read(bgFile)), 0);
			} else {
				bgLabel = new JLabel("no image", 0);
			}
		}
		catch (Exception exception)
		{
			bgLabel = new JLabel("no image", 0);
		}
		dp = new JDesktopPane();
		dp.setBackground(new Color(255, 255, 255));
		dp.add(bgLabel, JDesktopPane.FRAME_CONTENT_LAYER, -1);
		bgLabel.setSize(1020, 731);
		bgLabel.setVisible(false);//隐藏背景图
		mainPanel.add(dp, "Center");
		dp.setVisible(true);

		// 关键修改2：初始化两个工具栏，统一设置布局和样式
		jtoolbarTop = new JToolBar("Top Toolbar", JToolBar.HORIZONTAL);  // 第一行：水平排列
		jtoolbarBottom = new JToolBar("Bottom Toolbar", JToolBar.HORIZONTAL); // 第二行：水平排列
		// 统一工具栏样式（间距、边框、浮动性）
		ToolBarStyle toolBarStyle = new ToolBarStyle();
		toolBarStyle.applyStyle(jtoolbarTop);
		toolBarStyle.applyStyle(jtoolbarBottom);

		// -------------------------- 第一行工具栏（jtoolbarTop）：核心操作组件 --------------------------
		// IP输入框（从全局配置读取默认IP）
		String str = AppGlobalConfig.getDeviceIp();
		addr = str;
		try {
			GisUDP.das = InetAddress.getByName(addr);
		} catch (Exception ignored) {
		}
		iptext = new JTextField(str);//初始化文本输入框  61
		iptext.setPreferredSize(new Dimension(100,20));
		// IP输入监听
		iptext.getDocument().addDocumentListener(new DocumentListener(){
			private void onDocumentChange() {
				if (isUpdatingIp) return;
				String s = iptext.getText().trim();
				addr = s;
				if (s.isEmpty()) {
					GisUDP.das = null;
				} else {
					try {
						GisUDP.das = InetAddress.getByName(s);
					} catch (UnknownHostException e1) {
						GisUDP.das = null;
					}
				}
				updatePresetButtonsState();
			}
			@Override
			public void changedUpdate(DocumentEvent e) { onDocumentChange(); }
			@Override
			public void insertUpdate(DocumentEvent e) { onDocumentChange(); }
			@Override
			public void removeUpdate(DocumentEvent e) { onDocumentChange(); }
		});

		iptext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTargetIp(iptext.getText().trim());
			}
		});

		iptext.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateTargetIp(iptext.getText().trim());
			}
		});

		// IP输入限制（保持不变）
		iptext.addKeyListener(new KeyAdapter(){
			public void keyTyped(KeyEvent e){
				int keyChar = e.getKeyChar();
				if(!(keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9 || keyChar == KeyEvent.VK_PERIOD)){
					e.consume();
				}
			}
		});

		// 3 个预设 IP 按钮
		btnPreset1 = new JRoundButton(AppGlobalConfig.getPresetName1(), JRoundButton.ashen);
		btnPreset2 = new JRoundButton(AppGlobalConfig.getPresetName2(), JRoundButton.ashen);
		btnPreset3 = new JRoundButton(AppGlobalConfig.getPresetName3(), JRoundButton.ashen);
		btnPreset1.setFont(GisXYZ.fontG);
		btnPreset2.setFont(GisXYZ.fontG);
		btnPreset3.setFont(GisXYZ.fontG);
		updatePresetButtonsText();

		ActionListener presetClick = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String targetIp = null;
				if (e.getSource() == btnPreset1) targetIp = AppGlobalConfig.getPresetIp1();
				else if (e.getSource() == btnPreset2) targetIp = AppGlobalConfig.getPresetIp2();
				else if (e.getSource() == btnPreset3) targetIp = AppGlobalConfig.getPresetIp3();

				if (targetIp != null && !targetIp.trim().isEmpty()) {
					updateTargetIp(targetIp.trim());
				}
			}
		};
		btnPreset1.addActionListener(presetClick);
		btnPreset2.addActionListener(presetClick);
		btnPreset3.addActionListener(presetClick);

		MouseAdapter presetRightClick = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					showPresetEditDialog((Component) e.getSource());
				}
			}
		};
		btnPreset1.addMouseListener(presetRightClick);
		btnPreset2.addMouseListener(presetRightClick);
		btnPreset3.addMouseListener(presetRightClick);

		int ipMode = AppGlobalConfig.getIpSwitchMode();
		if (ipMode == 0) {
			// 直接输入模式
			jtoolbarTop.add(iptext);
		} else if (ipMode == 1) {
			// 3个预设按钮模式
			jtoolbarTop.add(btnPreset1);
			jtoolbarTop.add(btnPreset2);
			jtoolbarTop.add(btnPreset3);
		} else {
			// 并行模式 (直接输入 + 3个预设按钮)
			jtoolbarTop.add(iptext);
			jtoolbarTop.add(btnPreset1);
			jtoolbarTop.add(btnPreset2);
			jtoolbarTop.add(btnPreset3);
		}

		// 采集速度配置按键 - 第一行
		speedConfigButton = new JRoundButton("采集速度", JRoundButton.ashen);
		speedConfigButton.setPreferredSize(new Dimension(80, 22));
		speedConfigButton.setFont(GisXYZ.fontG);
		updateSpeedButtonLabel();
		speedConfigButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!LicenseManager.isLicensed()) {
					JOptionPane.showMessageDialog(gui.full, "未检测到有效授权文件，采集速度已锁定为默认极速状态！", "提示", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				SpeedConfigDialog dlg = new SpeedConfigDialog(gui.full);
				dlg.setVisible(true);
				if (dlg.isConfirmed()) {
					updateSpeedButtonLabel();
				}
			}
		});
		// 仅在已授权时展示采集速度配置按钮，默认模式（未授权）下不显示
		if (LicenseManager.isLicensed()) {
			jtoolbarTop.add(speedConfigButton);
		}

		// 持续获取数据按钮（conButton1）- 第一行
		conButton1 = new JCircleButton();//待机
		conButton1.setPreferredSize(new Dimension(33,33));
		conButton1.setIcon(new ImageIcon("pics" + File.separator + "start.png"));
		conButton1.setSelectedIcon(new ImageIcon("pics" + File.separator + "stop.png"));
		conButton1.setToolTipText(ChooseLang.M[408]);
		jtoolbarTop.add(conButton1); // 添加到第一行
		conButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent)
			{
				gui.gm.udp.guRun1 = conButton1.isSelected();
				if (conButton1.isSelected())
				{
					gui.gm.udp.guRun2 = true;
					starttime = System.currentTimeMillis();
					time.start();
				} else
				{
					gui.gm.udp.guRun2 = false;
					time.stop();
				}
			}
		});

		// 单次获取数据按钮（conButtono1）- 第一行
		conButtono1 = new JCircleButton();
		conButtono1.setPreferredSize(new Dimension(33,33));
		conButtono1.setIcon(new ImageIcon("pics" + File.separator + "ONCE.png"));
		conButtono1.setToolTipText(ChooseLang.M[409]);
		conButtono1.setFont(GisXYZ.fontG);
		conButtono1.setVisible(true);
		jtoolbarTop.add(conButtono1); // 添加到第一行
		conButtono1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent)
			{
				//设置1秒后停止
				getonce = new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent evt) {
						conButton1.setSelected(false);
						conButtono1.setBackground(new Color(0,0,0));
						gui.gm.udp.guRun1 = conButton1.isSelected();
						gui.gm.udp.guRun2 = false;
						conButtono1.setEnabled(true);
						conButton1.setEnabled(true);
						time.stop();
						timeronce.stop();
					}
				};
				timeronce = new Timer(2100,getonce);
				conButton1.setSelected(true);
				gui.gm.udp.guRun1 = conButton1.isSelected();
				conButtono1.setEnabled(false);
				conButton1.setEnabled(false);
				if (conButton1.isSelected())
				{
					gui.gm.udp.guRun2 = true;
					starttime = System.currentTimeMillis();
					time.start();
				}
				timeronce.start();
			}
		});

		// 通道按钮（jbutton1数组）- 第一行（核心操作，与连接按钮同排）
		int ai[] = {
				49, 81, 65, 50, 87, 83, 51, 69, 68, 52,
				82, 70,49, 81, 65, 50, 87, 83, 51, 69, 68, 52,
				82, 70
		};
		prpsToggle = new JToggleButton[gui.chno];
		prpdToggle = new JToggleButton[gui.chno];
		chcpToggle = new JToggleButton[gui.chno];
		jbutton1 = new JRTogButton[gui.chno];
		save[0] = (char)4;
		save[1] = (char)1;
		save[2] = (char)1;
		save[3] = (char)1;
		save[4] = (char)1;
		save[5] = (char)1;
		save[6] = (char)1;
		save[7] = (char)1;
		save[8] = (char)1;
		MyAL myal = new MyAL();
		int i = 0;
		int k = 0;
		for (; i < gui.chno; i++)
		{
			JPanel jpanel = new JPanel(new FlowLayout(0, 0, 0));
			jpanel.setBackground(new Color(225, 245, 225));//查看通道背景色
			jbutton1[i] = new JRTogButton((new StringBuilder()).append(ChooseLang.M[i+132]).toString(),JRTogButton.gray);
			int ii = 60;
//			if(i==4 || i==5) {ii = 60;} else if (i==0 || i==1) {ii = 50;}
			jbutton1[i].setPreferredSize(new Dimension(ii,25));
			jbutton1[i].setFont(GisXYZ.fontG);
			jbutton1[i].setToolTipText(ChooseLang.M[i + 300]);

			// 通道状态初始化（保持不变）
			try {
				readchar = this.readByChars(file);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			total = (int) readchar[0];//通道数
			chselect[i] = (int)readchar[i+1] == 1;
			if(chselect[i])
			{
				jbutton1[i].setBackground(new Color(229,229,229));//通道背景色
				jbutton1[i].setSelected(true);
			}
			else
			{
				jbutton1[i].setBackground(new Color(146,146,146));
				jbutton1[i].setSelected(false);
			}

			// 通道按钮事件（保持不变）
			ChannelOpenAL channelopenal = new ChannelOpenAL();
			channelopenal.ch = i;
			jbutton1[i].addActionListener(channelopenal);
			jbutton1[i].setBorder(new EtchedBorder(1));
			jbutton1[i].setVisible(false);//PRPS不可见
			if (i<tongdao)
			{
				jpanel.add(jbutton1[i]);
				jbutton1[i].setVisible(true);
			}

			// PRPS/PRPD/CHCP按钮（隐藏，逻辑不变）
			try{
				prpsToggle[i] = new JToggleButton(new ImageIcon(ImageIO.read(new File("pics"+File.separator+"icon_prps01.png")).getScaledInstance(24, 24, 4)));
				prpsToggle[i].setSelectedIcon(new ImageIcon(ImageIO.read(new File("pics"+File.separator+"icon_prps01p.png")).getScaledInstance(24, 24, 4)));
			}catch (Exception exception1){
				prpsToggle[i] = new JToggleButton("prps");
			}
			prpsToggle[i].setMargin(new Insets(1, 1, 1, 1));
			prpsToggle[i].setMnemonic(ai[k++]);
			prpsToggle[i].setToolTipText(ChooseLang.M[304]);//PRPS
			prpsToggle[i].addActionListener(myal);
			prpsToggle[i].setSelected(true);
			prpsToggle[i].setVisible(false);
			jpanel.add(prpsToggle[i]);

			try{
				prpdToggle[i] = new JToggleButton(new ImageIcon(ImageIO.read(new File("pics"+File.separator+"icon_prpd01.png")).getScaledInstance(24, 24, 4)));
				prpdToggle[i].setSelectedIcon(new ImageIcon(ImageIO.read(new File("pics"+File.separator+"icon_prpd01p.png")).getScaledInstance(24, 24, 4)));
			}catch (Exception exception2){
				prpdToggle[i] = new JToggleButton("prpd");
			}
			prpdToggle[i].setMargin(new Insets(1, 1, 1, 1));
			prpdToggle[i].setMnemonic(ai[k++]);
			prpdToggle[i].setToolTipText(ChooseLang.M[305]);//PRPD
			prpdToggle[i].addActionListener(myal);
			prpdToggle[i].setSelected(true);
			prpdToggle[i].setVisible(false);
			jpanel.add(prpdToggle[i]);

			try{
				chcpToggle[i] = new JToggleButton(new ImageIcon(ImageIO.read(new File("pics"+File.separator+"icon_cp01.png")).getScaledInstance(24, 24, 4)));
				chcpToggle[i].setSelectedIcon(new ImageIcon(ImageIO.read(new File("pics"+File.separator+"icon_cp01p.png")).getScaledInstance(24, 24, 4)));
			}catch (Exception exception3){
				chcpToggle[i] = new JToggleButton("ctrl");
			}
			chcpToggle[i].setMargin(new Insets(1, 1, 1, 1));
			chcpToggle[i].setMnemonic(ai[k++]);
			chcpToggle[i].setToolTipText(ChooseLang.M[306]);//选项
			chcpToggle[i].addActionListener(myal);
			chcpToggle[i].setSelected(true);
			chcpToggle[i].setVisible(false);
			jpanel.add(chcpToggle[i]);

			jpanel.setBorder(new EtchedBorder(1));
			jtoolbarBottom.add(jbutton1[i]); // 通道按钮添加到第一行
		}

		// 显示所有通道按钮（隐藏，添加到第一行）
		JButton showAllBtn = new JButton(ChooseLang.M[153]);
		showAllBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent)
			{
				int j1 = dp.getWidth() / tongdao;
				int k1 = dp.getHeight() - 70;
				int i1 = 0;
				for (int l = 0; l < tongdao; l++)
				{
					if (l == 3)
						j1 = dp.getWidth() - i1;
					prpsToggle[l].setSelected(true);
					prpdToggle[l].setSelected(true);
					chcpToggle[l].setSelected(true);
					gui.prpsFrame[l].setVisible(true);
					gui.prpdFrame[l].setVisible(true);
					gui.chCPanel[l].setVisible(true);
					gui.prpsFrame[l].setSize(j1, k1 / 2);
					gui.prpsFrame[l].setLocation(i1, 0);
					gui.prpdFrame[l].setSize(j1, (k1 + 1) / 2);
					gui.prpdFrame[l].setLocation(i1, k1 / 2);
					gui.chCPanel[l].setLocation(i1, k1);
					gui.chCPanel[l].setSize(j1, 70);
					i1 += j1;
				}
			}
		});
		jtoolbarTop.add(showAllBtn);
		showAllBtn.setVisible(false);//不显示所有通道按钮

		// -------------------------- 第二行工具栏（jtoolbarBottom）：功能按钮+状态显示 --------------------------
		// 分割线（垂直）
		JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
		sep.setPreferredSize(new Dimension(5,20));
		sep.setMaximumSize(new Dimension(5,20));
		sep.setMinimumSize(new Dimension(5,20));
		jtoolbarBottom.add(sep);

		// 触发按钮（trgButton）- 第二行
		trgButton = new JRoundButton(ChooseLang.M[154], JRoundButton.ashen);
		trgButton.setPreferredSize(new Dimension(100,25));
		trgButton.setFont(GisXYZ.fontG);
		trgButton.addActionListener(new ActionListener() {//触发按钮
			public void actionPerformed(ActionEvent actionevent)
			{
				gui.gm.gdp.gd.trigger.setLocationRelativeTo(gui.full);
				gui.gm.gdp.gd.trigger.setVisible(true);
			}
		});
		jtoolbarBottom.add(trgButton); // 添加到第二行

		// 高级设置按钮（注释，保持不变）
		/*
		gjszButton = new JRoundButton(ChooseLang.M[370], JRoundButton.ashen);
		gjszButton.setPreferredSize(new Dimension(70,25));
		gjszButton.setFont(GisXYZ.fontG);
		gjszButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent)
			{
				gui.gm.gdp.gd.trigger1.setLocationRelativeTo(gui.full);
				gui.gm.gdp.gd.trigger1.setVisible(true);
			}
		});
		JSeparator sep1 = new JSeparator(SwingConstants.VERTICAL);
		sep1.setPreferredSize(new Dimension(5,20));
		sep1.setMaximumSize(new Dimension(5,20));
		sep1.setMinimumSize(new Dimension(5,20));
		jtoolbarBottom.add(sep1);
		jtoolbarBottom.add(gjszButton);
		*/

		// 站点管理按钮（siteManButton）- 第二行
		siteManButton = new JRoundButton(ChooseLang.M[155], JRoundButton.ashen);
		siteManButton.setPreferredSize(new Dimension(100,25));
		siteManButton.setFont(GisXYZ.fontG);
		siteManButton.setToolTipText(ChooseLang.M[307]);
		siteManButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent)
			{
				gui.gm.selectNewSite = true;
				SiteChooser.jfisVisible = true;
			}
		});
		jtoolbarBottom.add(siteManButton); // 添加到第二行

		// 图谱库按钮（libButton）- 第二行
		libButton = new JRoundButton(ChooseLang.M[61], JRoundButton.ashen);
		libButton.setPreferredSize(new Dimension(100,25));
		libButton.setFont(GisXYZ.fontG);
		libButton.setToolTipText(ChooseLang.M[308]);
		libButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent)
			{
				jf.add(glw);
				jf.pack();
				jf.setLocation(50, 50);
				jf.setDefaultCloseOperation(2);
				jf.setVisible(true);
			}
		});
		jtoolbarBottom.add(libButton); // 添加到第二行

		// 测试按钮（jbutton_test_online）- 第二行
		jbutton_test_online = new JToggleButton(ChooseLang.M[341]);
		jbutton_test_online.setFont(GisXYZ.fontG);
		jbutton_test_online.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(jbutton_test_online.isSelected())
				{
					jbutton_test_online.setText(ChooseLang.M[340]);
					for(int j = 0; j < tongdao; j++)
					{
						gui.prps[j].draw3D = true;
						gui.prps[j].prpsrefresh = true;
						gui.jradiobutton[j].setSelected(false);
						gui.jradiobutton2[j].setSelected(true);
						gui.prps[j].repaint();
					}
				}
				else
				{
					jbutton_test_online.setText(ChooseLang.M[341]);
					for(int i = 0; i < tongdao; i++)
					{
						gui.prps[i].draw3D = false;
						gui.prps[i].prpsrefresh = true;
						gui.jradiobutton[i].setSelected(true);
						gui.jradiobutton2[i].setSelected(false);
						gui.prps[i].repaint();
					}
				}
			}
		});
		jtoolbarTop.add(jbutton_test_online);
		jbutton_test_online.setVisible(false);//测试按钮不可见

		// 状态显示（statusLabel）- 第二行
		statusLabel = new JLabel(ChooseLang.M[156]);//下线
		statusLabel.setToolTipText(ChooseLang.M[309]);
		statusLabel.setPreferredSize(new Dimension(150, 30));
		jtoolbarTop.add(statusLabel); // 添加到第二行

		// 测试时间（test_time）- 第二行
		test_time = new JLabel();//计时时间
		test_time.setPreferredSize(new Dimension(150,20));
		dataflow = new JLabel();//数据流量
		// 时间刷新逻辑（保持不变）
		freshtime = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent evt) {
				currenttime = System.currentTimeMillis(); // 补充currenttime赋值（原代码遗漏）
				long timeDiff = (currenttime - starttime)/1000;
				String timeStr = "";
				if(timeDiff < 60)
				{
					timeStr = ChooseLang.M[342] + ": " + timeDiff + "s";
				}
				else if(60 <= timeDiff && timeDiff < 3600)
				{
					timeStr = ChooseLang.M[342] + ": " + (timeDiff/60) + "min: " + (timeDiff%60) + "s";
				}
				else if(3600 <= timeDiff && timeDiff < 86400)
				{
					timeStr = ChooseLang.M[342] + ": " + (timeDiff/3600) + "h: " + (timeDiff%3600/60) + "min: " + (timeDiff%3600%60) + "s";
				}
				else
				{
					timeStr = ChooseLang.M[342] + ": 大于1天";
				}

				// 数据流量计算（保持不变）
				double send = (double)GisUDP.sendbytenum;
				double rece = (double)GisUDP.recebytenum;
				double datamb = (send + rece)/1024.0D/1024.0D;
				String dataStr = "当前数据流量：" + String.format("%1.2fMB", datamb);

				test_time.setText(timeStr);
				dataflow.setText(dataStr);

				// 触发按钮文字更新（保持不变）
				if(gui.gm.gdp.gd.trigger.jb[0].isSelected()) trgButton.setText(ChooseLang.M[207]);
				if(gui.gm.gdp.gd.trigger.jb[1].isSelected()) trgButton.setText(ChooseLang.M[209]);
				if(gui.gm.gdp.gd.trigger.jb[3].isSelected()) trgButton.setText(ChooseLang.M[210]);

				jtoolbarBottom.repaint();
			}
		};
		time = new Timer(1000, freshtime);//测试时间计时器
		jtoolbarTop.add(test_time); // 测试时间添加到第二行
		jtoolbarTop.add(dataflow);  // 数据流量添加到第二行

		// 正反色控制（Cltr_PNColor）- 第二行
		JPanel colorCtrlPanel = new JPanel(new GridLayout(0, 2));
		ButtonGroup buttongroup = new ButtonGroup();
		JRadioButton ajradiobutton1[] = new JRadioButton[2];//调整界面色调的单选按钮

		ajradiobutton1[0] = new JRadioButton(ChooseLang.M[271]);//正常
		buttongroup.add(ajradiobutton1[0]);
		colorCtrlPanel.add(ajradiobutton1[0]);
		ajradiobutton1[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent)
			{
				Option.gt = true;
				Option.writeOption();
			}
		});

		ajradiobutton1[1] = new JRadioButton(ChooseLang.M[272]);//反色
		buttongroup.add(ajradiobutton1[1]);
		colorCtrlPanel.add(ajradiobutton1[1]);
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
		jtoolbarTop.add(colorCtrlPanel); // 正反色控制添加到第二行

		// 键盘按钮（okey）- 第二行
		okey = new KeyBoard();
		okey.setPreferredSize(new Dimension(32,16));
		okey.setVisible(false);
		okey.setBackground(null);
		okey.setToolTipText("键盘");
		okey.addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent e) {
				GisSignal.OpenOSK();
			}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
		});
		jtoolbarTop.add(okey); // 添加到第二行

		// 退出按钮（quitbutton）- 第二行
		quitbutton = new JRoundButton("退出", JRoundButton.red);
		quitbutton.setForeground(Color.WHITE);
		quitbutton.setPreferredSize(new Dimension(50,20));
		quitbutton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (gui != null && gui.gm != null) {
					gui.gm.shutdownApplication();
				} else {
					System.exit(0);
				}
			}
		});
		quitbutton.setVisible(false);
		jtoolbarTop.add(quitbutton); // 添加到第二行

		// 关键修改3：垂直排列两个工具栏，添加到mainPanel顶部
		Box toolBarBox = Box.createVerticalBox(); // 垂直盒子容器
		toolBarBox.add(jtoolbarTop);   // 第一行工具栏
		toolBarBox.add(jtoolbarBottom); // 第二行工具栏
		mainPanel.add(toolBarBox, BorderLayout.NORTH); // 垂直盒子添加到主面板顶部

		// 原代码中mainPanel.add(jtoolbar, "First")替换为上述垂直盒子

		super.add(mainPanel);

		// 初始化通道toggle按钮状态（保持不变）
		for (int j = 0; j < gui.chno; j++)
		{
			prpsToggle[j].setSelected(false);
			prpdToggle[j].setSelected(false);
		}
	}

	// -------------------------- 工具类：统一工具栏样式 --------------------------
	class ToolBarStyle {
		public void applyStyle(JToolBar toolBar) {
			toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 3)); // 左对齐，组件间距10，上下边距3
			toolBar.setFloatable(false); // 禁止工具栏拖动（可选，根据需求调整）
			toolBar.setBorder(new EtchedBorder(1)); // 边框样式
			toolBar.setMargin(new Insets(2, 5, 2, 5)); // 内边距：上下2，左右5
		}
	}

	public void updatePersistentTestState(final PersistentPeakSnapshot snapshot) {
		if (snapshot == null || persistentTestStatusLabel == null) return;
		if (persistentTestButton != null && !persistentTestButton.isSelected()) {
			persistentTestStatusLabel.setText("");
			return;
		}
		PeakSessionState state = snapshot.getSessionState();
		String stateText = "";
		switch (state) {
			case IDLE:
				stateText = "";
				break;
			case STARTING:
			case WAITING_FOR_ACQUISITION:
				stateText = ChooseLang.M.length > 412 ? ChooseLang.M[412] : "等待采集";
				break;
			case WARMING_UP:
				stateText = ChooseLang.M.length > 413 ? ChooseLang.M[413] : "预热";
				break;
			case INITIALIZING:
				stateText = ChooseLang.M.length > 414 ? ChooseLang.M[414] : "初始化";
				break;
			case RUNNING:
				stateText = (ChooseLang.M.length > 415 ? ChooseLang.M[415] : "持久测试中") + " (" + String.format(java.util.Locale.ROOT, "%.1fs", snapshot.getElapsedSeconds()) + ")";
				break;
			case FINAL_HOLD:
				stateText = "";
				break;
			case ERROR:
				stateText = ChooseLang.M.length > 418 ? ChooseLang.M[418] : "CSV写入异常";
				break;
		}
		persistentTestStatusLabel.setText(stateText);
	}

	// -------------------------- 原有方法（保持不变） --------------------------
	public void add(JComponent jcomponent)
	{
		dp.add(jcomponent);
	}

	public void paint(Graphics g)
	{
		int i = getWidth();
		int j = getHeight();
		if (width != i || height != j)
		{
			width = i;
			height = j;
			bgLabel.setBounds(0, 0, i, j);
		}
		super.paint(g);
	}

	public JMenuBar getMenuBar()
	{
		JMenuBar jmenubar = new JMenuBar();
		return jmenubar;
	}

	//显示所有通道
	void showAllWindows()
	{
		for (int i = 0; i < gui.chno; i++)
		{
			prpsToggle[i].setSelected(true);
			prpdToggle[i].setSelected(true);
			chcpToggle[i].setSelected(true);
			chrecord[i] = 1;
		}
		aiButton();
	}

	//显示可选通道
	void shownCh(){
		for (int i = 0; i < tongdao; i++)
		{
			prpsToggle[i].setSelected(chselect[i]);
			prpdToggle[i].setSelected(chselect[i]);
			chcpToggle[i].setSelected(chselect[i]);
			chrecord[i] = 1;
		}
		aiButton();
	}

	//显示上次择的通道数
	void showWindows(int ch){
		prpsToggle[ch].setSelected(!HbDesktopPane.chselect[ch]);
		prpdToggle[ch].setSelected(!HbDesktopPane.chselect[ch]);
		chcpToggle[ch].setSelected(!HbDesktopPane.chselect[ch]);
		aiButton();
	}

	void aiButton()
	{
		int k = 0;
		for (int i = 0; i < 8; i++)
		{
			Boolean boolean1 = Boolean.valueOf(prpsToggle[i].isSelected());
			Boolean boolean3 = Boolean.valueOf(prpdToggle[i].isSelected());
			Boolean boolean5 = Boolean.valueOf(chcpToggle[i].isSelected());
			if (!boolean1.booleanValue() && !boolean3.booleanValue() && boolean5.booleanValue())
			{
				boolean5 = Boolean.valueOf(false);
				chcpToggle[i].setSelected(boolean5.booleanValue());
			}
			if (boolean1.booleanValue() || boolean3.booleanValue())
				k++;
			gui.prpsFrame[i].setVisible(boolean1.booleanValue());
			gui.prpdFrame[i].setVisible(boolean3.booleanValue());
			gui.chCPanel[i].setVisible(boolean5.booleanValue());
		}

		int l;
		if (0 < k)
			l = dp.getWidth() / k;
		else
			l = 0;
		int i1 = dp.getHeight();
		int k1 = 0;
		for (int j = 0; j < gui.chno; j++)
		{
			Boolean boolean2 = Boolean.valueOf(prpsToggle[j].isSelected());
			Boolean boolean4 = Boolean.valueOf(prpdToggle[j].isSelected());
			Boolean boolean6 = Boolean.valueOf(chcpToggle[j].isSelected());
			if (!boolean2.booleanValue() && !boolean4.booleanValue())
				continue;
			if (k1 / l == k - 1)
				l = dp.getWidth() - k1;
			int j1;
			if (boolean6.booleanValue())
			{
				j1 = i1 - 60;//调整控制手柄的高度
				gui.chCPanel[j].setSize(l, 60);//调整控制手柄的高度
				gui.chCPanel[j].setLocation(k1, j1);
			} else
			{
				j1 = i1;
			}
			if (boolean2.booleanValue() && boolean4.booleanValue())
			{
				if (k == 1)
				{
					gui.prpsFrame[j].setSize(l / 2, j1);
					gui.prpsFrame[j].setLocation(0, 0);
					gui.prpdFrame[j].setSize((l + 1) / 2, j1);
					gui.prpdFrame[j].setLocation(l / 2, 0);
				} else
				{
					gui.prpsFrame[j].setSize(l, j1 / 2);
					gui.prpsFrame[j].setLocation(k1, 0);
					gui.prpdFrame[j].setSize(l, (j1 + 1) / 2);
					gui.prpdFrame[j].setLocation(k1, j1 / 2);
				}
			} else
			{
				gui.prpsFrame[j].setSize(l, j1);
				gui.prpsFrame[j].setLocation(k1, 0);
				gui.prpdFrame[j].setSize(l, j1);
				gui.prpdFrame[j].setLocation(k1, 0);
			}
			k1 += l;
		}
	}

	private char[] readByChars(File file) throws Exception{
		char[] temp = new char[9];
		if(!file.exists())
		{
			try {
				file.createNewFile();
				save[0] = (char)4;
				save[1] = (char)1;
				save[2] = (char)1;
				save[3] = (char)1;
				save[4] = (char)1;
				save[5] = (char)4;
				save[6] = (char)1;
				save[7] = (char)1;
				save[8] = (char)1;
				writeByChars(file,save,0,9);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else{
			Reader reader = new InputStreamReader(new FileInputStream(file));
			reader.read(temp);
			reader.close();
		}
		return temp;
	}

	private void writeByChars(File file,char[] temp, int offset, int len) throws Exception{
		if(!file.exists())
		{
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else{
			try {
				Writer writechar = new OutputStreamWriter(new FileOutputStream(file));
				writechar.write(temp,offset,len);
				writechar.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateTargetIp(String newIp) {
		if (newIp == null) newIp = "";
		newIp = newIp.trim();
		addr = newIp;
		if (newIp.isEmpty()) {
			GisUDP.das = null;
		} else {
			try {
				GisUDP.das = InetAddress.getByName(newIp);
				System.out.println("目标设备 IP 切换为: " + GisUDP.das);
			} catch (UnknownHostException ex) {
				GisUDP.das = null;
			}
		}

		if (iptext != null && !newIp.equals(iptext.getText().trim())) {
			isUpdatingIp = true;
			try {
				iptext.setText(newIp);
			} finally {
				isUpdatingIp = false;
			}
		}

		updatePresetButtonsState();

		// 如果当前正在采集，向新目标 IP 立即发送一次采集启动指令
		if (gui != null && gui.gm != null && gui.gm.udp != null && gui.gm.udp.guRun1) {
			gui.gm.udp.sendPacket(0, 4, 0);
		}
	}

	public void updatePresetButtonsState() {
		String current = (iptext != null && iptext.getText() != null) ? iptext.getText().trim() : (addr != null ? addr.trim() : "");
		boolean match1 = current.equals(AppGlobalConfig.getPresetIp1());
		boolean match2 = current.equals(AppGlobalConfig.getPresetIp2());
		boolean match3 = current.equals(AppGlobalConfig.getPresetIp3());

		if (btnPreset1 != null) {
			btnPreset1.setBackground(match1 ? new Color(59, 130, 246) : new Color(229, 229, 229));
			btnPreset1.setForeground(match1 ? Color.WHITE : Color.BLACK);
		}
		if (btnPreset2 != null) {
			btnPreset2.setBackground(match2 ? new Color(59, 130, 246) : new Color(229, 229, 229));
			btnPreset2.setForeground(match2 ? Color.WHITE : Color.BLACK);
		}
		if (btnPreset3 != null) {
			btnPreset3.setBackground(match3 ? new Color(59, 130, 246) : new Color(229, 229, 229));
			btnPreset3.setForeground(match3 ? Color.WHITE : Color.BLACK);
		}
	}

	public void updatePresetButtonsText() {
		FontMetrics fm = getFontMetrics(GisXYZ.fontG);
		if (btnPreset1 != null) {
			String n1 = AppGlobalConfig.getPresetName1();
			btnPreset1.setText(n1);
			int w1 = Math.max(55, (fm != null ? fm.stringWidth(n1) : n1.length() * 12) + 16);
			btnPreset1.setPreferredSize(new Dimension(w1, 22));
			btnPreset1.setToolTipText(n1 + " (IP: " + AppGlobalConfig.getPresetIp1() + "，右键可快速编辑)");
		}
		if (btnPreset2 != null) {
			String n2 = AppGlobalConfig.getPresetName2();
			btnPreset2.setText(n2);
			int w2 = Math.max(55, (fm != null ? fm.stringWidth(n2) : n2.length() * 12) + 16);
			btnPreset2.setPreferredSize(new Dimension(w2, 22));
			btnPreset2.setToolTipText(n2 + " (IP: " + AppGlobalConfig.getPresetIp2() + "，右键可快速编辑)");
		}
		if (btnPreset3 != null) {
			String n3 = AppGlobalConfig.getPresetName3();
			btnPreset3.setText(n3);
			int w3 = Math.max(55, (fm != null ? fm.stringWidth(n3) : n3.length() * 12) + 16);
			btnPreset3.setPreferredSize(new Dimension(w3, 22));
			btnPreset3.setToolTipText(n3 + " (IP: " + AppGlobalConfig.getPresetIp3() + "，右键可快速编辑)");
		}
		if (jtoolbarTop != null) {
			jtoolbarTop.revalidate();
			jtoolbarTop.repaint();
		}
	}

	private void showPresetEditDialog(Component comp) {
		int index = 1;
		if (comp == btnPreset2) index = 2;
		else if (comp == btnPreset3) index = 3;

		String curName = AppGlobalConfig.getPresetName(index);
		String curIp = AppGlobalConfig.getPresetIp(index);

		JTextField nameField = new JTextField(curName, 12);
		JTextField ipField = new JTextField(curIp, 15);

		JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
		p.add(new JLabel("预设按钮文本:"));
		p.add(nameField);
		p.add(new JLabel("预设目标 IP:"));
		p.add(ipField);

		int res = JOptionPane.showConfirmDialog(this, p, "编辑快捷预设 " + index + " 参数", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (res == JOptionPane.OK_OPTION) {
			String newName = nameField.getText().trim();
			String newIp = ipField.getText().trim();
			if (!newName.isEmpty()) {
				AppGlobalConfig.setPresetName(index, newName);
			}
			if (!newIp.isEmpty()) {
				AppGlobalConfig.setPresetIp(index, newIp);
			}
			try {
				AppGlobalConfig.saveCurrentConfig();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			updatePresetButtonsText();
			updatePresetButtonsState();
		}
	}

	public void updateSpeedButtonLabel() {
		if (speedConfigButton == null) return;
		if (!LicenseManager.isLicensed()) {
			speedConfigButton.setVisible(false);
			speedConfigButton.setText("速度:极速");
			speedConfigButton.setEnabled(false);
			speedConfigButton.setToolTipText("未检测到有效授权文件，采集速度已锁定为默认极速状态");
			return;
		}
		speedConfigButton.setVisible(true);
		speedConfigButton.setEnabled(true);
		String mode = AppGlobalConfig.getAcquisitionSpeedMode();
		int delay = AppGlobalConfig.getAcquisitionSpeedDelayMs();
		String desc;
		if ("MAX".equalsIgnoreCase(mode) || delay == 0) {
			desc = "极速";
		} else if ("FAST".equalsIgnoreCase(mode) || delay == 20) {
			desc = "快速";
		} else if ("MEDIUM".equalsIgnoreCase(mode) || delay == 80) {
			desc = "中速";
		} else if ("SLOW".equalsIgnoreCase(mode) || delay == 480) {
			desc = "慢速";
		} else {
			desc = delay + "ms";
		}
		speedConfigButton.setText("速度:" + desc);
		speedConfigButton.setToolTipText("当前采集速度: " + desc + " (延时 " + delay + "ms)，点击修改");
	}

	public void opbt1t3(){}
	public void opbt4t6(){}
}