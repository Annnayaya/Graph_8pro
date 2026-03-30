package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeModel;
class GisLibraryWindow extends JPanel
{
	private static final long serialVersionUID = 197307030009L;
	Database db;
	GisGraph grp;
	JPanel cpanel;
	JTabbedPane ctrPane;
	JButton expButton;
	boolean expanded;
	JTree dataTree;
	JFrame pFrame;
	JList selList;
	SiteData loadedSite;
	int drawCh;
	JCheckBox phaseShiftCheck;
	JCheckBox noiseGateCheck;
	JButton saveToLib;
	GisDenoiser denoiser;
	JLabel listTitle;
	JList<LibFile> libList;
	GisLibrary glib;
	int rawBuffer[][];
	boolean denoise1[] = {true, true, true, true, true, true, true, true};
	boolean denoise2[] = {true, true, true, true, true, true, true, true};
	int phaseShift[];
	int xbuf[][];
	long oldPsclSelTime;
	PRPDGraph gprpd;
	boolean drawgprpd;
	JPanel containprps;
	JPanel containprpd;
	JRadioButton jradiobutton_Ultrasound;
	
	class Disp3DPRPS implements ActionListener
	{

		public void actionPerformed(ActionEvent actionevent)
		{
			drawgprpd = false;
			containprpd.setVisible(false);//设置PRPS容器不可见
			containprps.setVisible(false);//这里得先设置不可见然后可见后才能正常显示
			cpanel.removeAll();
			//绘图
			grp.rdeg = 2.3561944901923448D;
			grp.tdeg = 0.53407075111026492D;
			grp.draw3D = true;
			grp.prpsrefresh = true;
			grp.repaint();
			//组织结构，显示组件
			containprps.add(grp);
			cpanel.add(containprps);
			cpanel.add(ctrPane);			
			containprps.setVisible(true);
		}
	}

	class Disp2DPRPS implements ActionListener{

		public void actionPerformed(ActionEvent actionevent)
		{
			drawgprpd = false;
			containprpd.setVisible(false);//设置PRPS容器不可见
			containprps.setVisible(false);//这里得先设置不可见然后可见后才能正常显示
			cpanel.removeAll();
			//绘图
			grp.draw3D = false;
			grp.isflygraphic = false;
			grp.Waveform = false;
			grp.prpsrefresh = true;
			grp.repaint();
			//组织结构，显示组件
			containprps.add(grp);
			cpanel.add(containprps);
			cpanel.add(ctrPane);			
			containprps.setVisible(true);
		}
	}
	//绘制柱状图
	class Disp2DNOFLY implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent actionevent) {
			drawgprpd = false;
			containprpd.setVisible(false);//设置PRPS容器不可见	
			containprps.setVisible(false);//这里得先设置不可见然后可见后才能正常显示
			cpanel.removeAll();
			//绘图
			grp.draw3D = false;
			grp.isflygraphic = true;
			grp.prpsrefresh = true;
			grp.repaint();
			//组织结构，显示组件
			containprps.add(grp);
			cpanel.add(containprps);
			cpanel.add(ctrPane);
			containprps.setVisible(true);
			
		}
	}
	//绘制超声波
	class DispUltrasond implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent actionevent) {
			//由于两种图谱不在同一个类中，单选按钮加入同一个组，必须把两种图谱放入不同的容器，然后设置容器的可见性，才能正常绘制正确的图谱
			drawgprpd = true;//绘制PRPD图
			containprps.setVisible(false);//设置PRPD容器不可见
			containprpd.setVisible(false);//这里得先设置不可见然后可见后才能正常显示
			cpanel.removeAll();
			//绘图
			gprpd.draw2D = false;
			gprpd.drawCon = false;
			gprpd.draw3D = false;			
			gprpd.drawUltral = true;
			gprpd.prpdrefresh = true;
			gprpd.repaint();
			//组织结构，显示组件
			containprpd.add(gprpd);
			cpanel.add(containprpd);
			cpanel.add(ctrPane);
			containprpd.setVisible(true);			
		}
		
	}

	public GisLibraryWindow(JFrame jframe, GisLibrary gislibrary)
	{
		ctrPane = new JTabbedPane();
		expButton = new JButton();
		expanded = true;
		selList = new JList();
		loadedSite = null;
		drawCh = 0;
		phaseShiftCheck = new JCheckBox("pShft");
		noiseGateCheck = new JCheckBox("nGate");
		//saveToLib = new JButton("Lib");
		saveToLib = new JButton("导入");
		denoiser = new GisDenoiser(8);
		listTitle = new JLabel(ChooseLang.M[264]);
		rawBuffer = new int[8][5120];
		phaseShift = new int[8];
		xbuf = new int[8][5120];
		oldPsclSelTime = 0L;
		db = new Database("data");
		pFrame = jframe;
		glib = gislibrary;
		setLayout(new BorderLayout());
		cpanel = new JPanel(new GridLayout(1, 2));
		
		grp = new GisGraph(64, 80);//实例化PRPS库
		grp.directRefresh = true;
		grp.stringToDraw = "";
		grp.setPreferredSize(new Dimension(240, 240));
		
		gprpd = new PRPDGraph(64,80);//实例化PRPD库
		gprpd.directRefresh = true;
		gprpd.setPreferredSize(new Dimension(240, 240));
		
		containprps = new JPanel(new GridLayout(1,0));//PRPS容器
		containprpd = new JPanel(new GridLayout(1,0));//PRPD容器
		containprps.add(grp);
		containprpd.add(gprpd);
		containprps.setVisible(true);//默认显示PRPS
		containprpd.setVisible(false);
		cpanel.add(containprps);
		//cpanel.add(grp);
		drawgprpd = false;
		dataTree = new JTree(new DefaultTreeModel(db));
		dataTree.addTreeSelectionListener(new TreeSelectionListener() {

			SiteData oldSite;
			long oldDate;
			public void valueChanged(TreeSelectionEvent treeselectionevent)
			{
				Object aobj[] = treeselectionevent.getPath().getPath();
				if (aobj.length > 2)
				{
					System.out.println("libwin:loading site");
					if (oldSite != (SiteData)aobj[1])
						loadedSite = db.openSite(((SiteData)aobj[1]).siteName);
					oldSite = loadedSite;
					System.out.println("libwin:loading list");
					if (oldDate != ((PeriodBlock)aobj[2]).etime)
						loadedSite.getEventList(((PeriodBlock)aobj[2]).stime, ((PeriodBlock)aobj[2]).etime);
					oldDate = ((PeriodBlock)aobj[2]).etime;
					int j = 0;
					for (int k = loadedSite.evList.size(); j < k; j++)
						((PRPSCell)(PRPSCell)loadedSite.evList.get(j)).showDate = true;

					System.out.println("libwin:updating list");
					selList.setModel(loadedSite.evList);
					listTitle.setText((new StringBuilder()).append(loadedSite.siteName).append(" / ").append(String.format("%tF ~ %tF", new Object[] {
						Long.valueOf(((PeriodBlock)aobj[2]).stime), Long.valueOf(((PeriodBlock)aobj[2]).etime)
					})).toString());
				}
			}
		});
		JScrollPane jscrollpane = new JScrollPane(dataTree, 22, 30);
		JPanel jpanel = new JPanel(new BorderLayout());
		//listModelSort(glib);
		libList = new JList<LibFile>(glib);
		
		jpanel.add(new JScrollPane(libList), "Center");
		JPanel jpanel1 = new JPanel();
		jpanel1.setLayout(new BoxLayout(jpanel1, 1));
		ButtonGroup buttongroup = new ButtonGroup();
		JRadioButton jradiobutton = new JRadioButton("2D");
		JRadioButton jradiobutton1 = new JRadioButton("3D");
		JRadioButton jradiobutton_fly = new JRadioButton(ChooseLang.M[330]);//飞行图
		jradiobutton_Ultrasound = new JRadioButton(ChooseLang.M[332]);//超声波
		jradiobutton_fly.setVisible(false);
		jradiobutton_Ultrasound.setVisible(false);
		buttongroup.add(jradiobutton);
		buttongroup.add(jradiobutton1);
		buttongroup.add(jradiobutton_fly);
		buttongroup.add(jradiobutton_Ultrasound);
		jradiobutton1.setSelected(true);
		jpanel1.add(jradiobutton);
		jpanel1.add(jradiobutton1);
		jpanel1.add(jradiobutton_fly);
		jpanel1.add(jradiobutton_Ultrasound);
		jpanel.add(jpanel1, "East");
		jradiobutton.addActionListener(new Disp2DPRPS());
		jradiobutton1.addActionListener(new Disp3DPRPS());
		jradiobutton_fly.addActionListener(new Disp2DNOFLY());
		jradiobutton_Ultrasound.addActionListener(new DispUltrasond());
		libList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent listselectionevent)
			{
				if (listselectionevent.getValueIsAdjusting())
				{
					return;
				} else
				{
					if(drawgprpd)//绘制PRPD
					{
						glib.loadData_PRPD(libList.getSelectedIndex(), gprpd);
						gprpd.prpdrefresh = true;
						grp.prpsrefresh = false;
						gprpd.repaint();
					}
					else//绘制PRPS
					{
						
						glib.loadData(libList.getSelectedIndex(), grp);
						grp.prpsrefresh = true;
						gprpd.prpdrefresh = false;
						grp.repaint();
					}
					return;
				}
			}
		});
		ctrPane.addTab(ChooseLang.M[61], jpanel);
		ctrPane.addTab(ChooseLang.M[155], jscrollpane);
		JPanel jpanel2 = new JPanel(new BorderLayout());
		jpanel2.add(listTitle, "North");
		listTitle.setBorder(BorderFactory.createEtchedBorder(1));
		jpanel2.add(new JScrollPane(selList), "Center");
		JPanel jpanel3 = new JPanel();
		jpanel3.setLayout(new BoxLayout(jpanel3, 1));
		JRadioButton ajradiobutton[] = new JRadioButton[HbDesktopPane.tongdao];
		ButtonGroup buttongroup1 = new ButtonGroup();
		for (int i = 0; i < HbDesktopPane.tongdao; i++)//显示2个通道选项
		{
			ajradiobutton[i] = new JRadioButton((new StringBuilder()).append("通道").append(i + 1).toString());
			buttongroup1.add(ajradiobutton[i]);
			jpanel3.add(ajradiobutton[i]);
		}

		ajradiobutton[0].setSelected(true);
		jpanel3.add(phaseShiftCheck);
		jpanel3.add(noiseGateCheck);
		phaseShiftCheck.setSelected(true);
		phaseShiftCheck.setVisible(false);//相位偏移不可见
		noiseGateCheck.setSelected(true);
		noiseGateCheck.setVisible(false);//噪声门限不可见
		jpanel3.add(saveToLib);//保存数据到库
		phaseShiftCheck.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				if(jradiobutton_Ultrasound.isSelected())
				{
					reloadGrp_PRPD();
				}
				else
				{
					reloadGrp();
				}
			}
		});
		noiseGateCheck.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				if(jradiobutton_Ultrasound.isSelected())
				{
					reloadGrp_PRPD();
				}
				else
				{
					reloadGrp();
				}
			}

		});
		ajradiobutton[HbDesktopPane.tongdao-1].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				drawCh = HbDesktopPane.tongdao-1;
				if(jradiobutton_Ultrasound.isSelected())
				{
					reloadGrp_PRPD();
				}
				else
				{
					reloadGrp();
				}
			}

		});
		
		saveToLib.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				String s = "lib"+File.separator;
				String s1 = JOptionPane.showInputDialog(ChooseLang.M[265]);
				if (s1 == null)
					return;
				s = (new StringBuilder()).append(s).append(s1).toString();
				(new File("lib"+File.separator)).mkdir();
				try
				{
					DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream((new StringBuilder()).append(s).append(".gd1").toString())));
					int ai[] = new int[10240];
					int ai1[] = new int[4096];
					for (int j = 0; j < 5120; j++)
						dataoutputstream.writeShort(grp.pdData[j]);

					for (int k = 0; k < 10240; k++)
					{
						ai[k] = 0;
						dataoutputstream.writeShort(ai[k]);
					}

					for (int l = 0; l < 4096; l++)
					{
						ai1[l] = 0;
						dataoutputstream.writeShort(ai1[l]);
					}

					dataoutputstream.writeShort(grp.mindBm);
					dataoutputstream.writeShort(0);
					dataoutputstream.writeShort(grp.phaseDelay);
					dataoutputstream.flush();
					dataoutputstream.close();
					glib.add(glib.size(), new LibFile((new StringBuilder()).append(s1).append(".gd1").toString()));
				}
				catch (Exception exception)
				{
					exception.printStackTrace();
				}
			}
		});
		jpanel2.add(jpanel3, "East");
		//ctrPane.addTab("Waveform", jpanel2);/
		ctrPane.addTab("从项目导入典型图谱", jpanel2);
		ctrPane.setPreferredSize(new Dimension(320, 320));
		cpanel.add(ctrPane);
		add(cpanel, "Center");
		add(expButton, "East");
		expButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				if (expanded)
				{
					expanded = false;
					cpanel.remove(ctrPane);
					cpanel.setLayout(new GridLayout(1, 1));
					Dimension dimension = cpanel.getSize();
					dimension.setSize(dimension.getWidth() / 2D, dimension.getHeight());
					grp.setPreferredSize(new Dimension((int)dimension.getWidth(), (int)dimension.getHeight()));
					cpanel.setSize(dimension);
					cpanel.validate();
					pFrame.pack();
				} else
				{
					expanded = true;
					cpanel.setLayout(new GridLayout(1, 2));
					cpanel.add(ctrPane);
					Dimension dimension1 = cpanel.getSize();
					grp.setPreferredSize(new Dimension((int)dimension1.getWidth(), (int)dimension1.getHeight()));
					dimension1.setSize(dimension1.getWidth() * 2D, dimension1.getHeight());
					cpanel.setSize(dimension1);
					cpanel.validate();
					pFrame.pack();
				}
			}
		});
		expButton.setPreferredSize(new Dimension(20, 50));
		expButton.getInsets().set(0, 0, 0, 0);
		selList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent listselectionevent)
			{
				if (listselectionevent.getValueIsAdjusting())
				{
					return;
				} else
				{					
					if(jradiobutton_Ultrasound.isSelected())
					{
						reloadGrp_PRPD();
					}
					else
					{
						reloadGrp();
					}
					
					return;
				}
			}

		});
	}

	void reloadGrp()
	{
		PRPSCell prpscell = (PRPSCell)(PRPSCell)selList.getSelectedValue();
		if (prpscell == null)
		{
			for (int i = 0; i < 5120; i++)
				grp.pdData[i] = 0;//飞行图数据
			
			for (int j = 0; j < 8; j++)
				grp.resultString[j] = "";//判断结果
			
			for(int k = 0; k < 64; k++)
				grp.columdata[k] = 0;//柱状图数据

			grp.stringToDraw = "";
		} else
		{
			psclSel(prpscell.time);
			
			grp.stringToDraw = (new StringBuilder()).append(loadedSite.siteName).append(" / ").append(String.format("%tF %tT", new Object[] {
				Long.valueOf(prpscell.time), Long.valueOf(prpscell.time)
			})).toString();
			
		}
		grp.prpsrefresh = true;
		grp.repaint();
	}

	void psclSel(long l)
	{
		int k = 0;
		int ai[];
		int ai2[];
		oldPsclSelTime = l;
		ai = new int[8];
		int ai1[][] = new int[8][];
		ai2 = new int[280];
		int ai3[][] = new int[8][];//柱形数据存储数组
		int ai4[][] = new int[8][];//超声波数据存储数组
		for (int i = 0; i < 8; i++)
		{
			ai1[i] = xbuf[i];
		}
		//程序运行后下面的执行代码变量的数据到上面代码中，左边赋值于右边变量
		ai1[drawCh] = grp.pdData;//PRPS图数据
		ai3[drawCh] = grp.columdata;//柱形图数据
		ai4[drawCh] = gprpd.ultralpdata;//超声波数据
		loadedSite.getEvent(l, rawBuffer[0], rawBuffer[1], rawBuffer[2], rawBuffer[3], rawBuffer[4], rawBuffer[5], rawBuffer[6], rawBuffer[7], ai2);
		for (int j = 0; j < 8; j++)
		{
			denoiser.preDefinedOffset[j] = ai2[j + 4];
			if (!phaseShiftCheck.isSelected())
				ai2[j] = 0;
			denoise2[j] = noiseGateCheck.isSelected();
		}

		grp.phaseDelay = ai2[drawCh];
		denoiser.findOffset = false;
		denoiser.gdProc(rawBuffer, ai1, null,denoise1, denoise2, 3, ai2, 79, true);
		
		 //拆分5120个数据成80组比较，为生成柱状图准备
	    int[][][] an = new int[8][80][64];
	    for(int i = 0; i < 8; i++)
	    {
	    	for(int cycle = 0; cycle < 80; cycle++)
			{
		    	for(int j = 0; j < 64; j++)
		    	{    		
	    			an[i][cycle][j] = ai1[i][cycle*64 + j];
	    		}
	    	}
	    	/*
	    	int ln = 80;
	    	for(int kn = 0; kn < 64; kn++)
	    	{
	    		if(an[drawCh][--ln][kn] > an[drawCh][ln-1][kn])
	    		{
	    			System.arraycopy(an[drawCh][ln], 0, ai3[drawCh], 0, 64);//复制最新一组数据到柱形显示图
	    			//System.arraycopy(an[i][ln], 0, array_columprpd[0], 0, 64);//复制最新一组数据到柱形显示图
	    		}
	    	}
	    	if(l < 1)
	    		l = 80;
	    	*/
	    	//计算柱形图
	    	for(int kn = 0; kn < 64; kn++)
	    	{
	    		int k3 = 0;
	    		for(int m = 0; m < 80; m++)
	    		{
	    			
	    			int k4 = an[i][m][kn];
	    			if(k3 < k4)
	        		{
	        			k3 = k4;        			
	        		} 
	    			ai3[drawCh][kn] = k3;
	    		}
	    	}
	    	
	    }
	    
		//System.arraycopy(ai1[drawCh], 5056, ai3[drawCh], 0, 64);//复制最新一组数据到柱形显示图
		System.arraycopy(ai1[drawCh], 4800, ai4[drawCh], 0, 320);
		grp.mindBm = (int)(denoiser.offset_dBm[drawCh] - 0.5D);
		grp.maxdBm = GisSignal.calMax_dBm;
		grp.resultString[0] = "";
		grp.resultString[1] = "";
		grp.resultString[2] = "";
		grp.resultString[3] = "";
		ai[0] = 0;
		ai[1] = 0;
		ai[2] = 0;
		ai[3] = 0;
		for (int j = 0; j < 8; j++) 
		{
		      if ((ai2[(drawCh * 32 + 8 + j)] > 10) && (k < 8))
		      {
		        ai[k] = ai2[(drawCh * 32 + 8 + j)];
		        if(drawCh == 3)
		        {
			        if (PDResult.resultStr[j] == ChooseLang.M[10] || PDResult.resultStr[j] == ChooseLang.M[11] || PDResult.resultStr[j] == ChooseLang.M[12] )
			        	grp.resultString[k] = ChooseLang.M[16];
					else
						grp.resultString[k] = PDResult.resultStr[j];
					String[] resultstringCT = grp.resultString;
					resultstringCT[k] = (resultstringCT[k] + " " + ai2[(drawCh * 32 + 8 + j)] + "%");
		        }
		        else
		        {
		        	grp.resultString[k] = PDResult.resultStr[j]; 
			        int tmp391_389 = k; 
			        String[] tmp391_386 = grp.resultString;
			        tmp391_386[tmp391_389] = (tmp391_386[tmp391_389] + " " + ai2[(drawCh * 32 + 8 + j)] + "%");
		        }
		        //grp.resultString[k] = PDResult.resultStr[j]; 
		        //int tmp391_389 = k; 
		        //String[] tmp391_386 = grp.resultString;
		        //tmp391_386[tmp391_389] = (tmp391_386[tmp391_389] + " " + ai2[(drawCh * 32 + 8 + j)] + "%");
		        k++;
		      }
		 }
	    for (int j = 1; j < 8; j++) 
	    {
	      for (k = j; k < 8; k++) 
	      {
	        if (ai[k] > ai[j])
	        {
	          int m = ai[k];
	          ai[k] = ai[j];
	          ai[j] = m;
	          String str = grp.resultString[j];
	          grp.resultString[j] = grp.resultString[k];
	          grp.resultString[k] = str;
	        }
	      }
		}
	}

	void reloadGrp_PRPD()
	{
		PRPSCell prpscell = (PRPSCell)(PRPSCell)selList.getSelectedValue();
		if (prpscell == null)
		{
			for(int i = 0; i < 320; i++)
				gprpd.ultralpdata[i] = 0;//超声波数据
	
		} else
		{
			psclSel(prpscell.time);
			
			grp.stringToDraw = (new StringBuilder()).append(loadedSite.siteName).append(" / ").append(String.format("%tF %tT", new Object[] {
				Long.valueOf(prpscell.time), Long.valueOf(prpscell.time)
			})).toString();
			
		}
		gprpd.prpdrefresh = true;
		gprpd.repaint();
	}

	void listModelSort(DefaultListModel<LibFile> defaultListModel)
	{
		ArrayList<LibFile> list = new ArrayList(Arrays.asList(defaultListModel.toArray()));
		Collections.sort(list, new Comparator<LibFile>(){

			@Override
			public int compare(LibFile o1, LibFile o2) {
				int index1 = Integer.parseInt(o1.fileName.substring(0, o1.fileName.indexOf(".")));
				int index2 = Integer.parseInt(o2.fileName.substring(0, o2.fileName.indexOf(".")));
				//int ret = o1.fileName.substring(0, o1.fileName.indexOf(".")).compareTo(o2.fileName.substring(0, o2.fileName.indexOf(".")));

				if(index1 > index2)
				{
					return 1;
				}
				else if(index1 < index2)
				{
					return -1;
				}
				else
				{
					return 0;
				}

			}
			
		});
		
		/*
		for(int i = 1; i < list.size(); i++)
		{
			String s =list.get(i).toString();
			String[] groups = s.split(File.separator + ".");
			if(groups.length > 0)
			{
				if(!groups[0].isEmpty())
				{
					for(int j = 0; j < i; j++ )
					{
						String s1 =list.get(j).toString();
						String[] groups1 = s1.split(File.separator + ".");
						if(groups1.length > 0)
						{
							if(!groups[0].isEmpty())
							{
								int index = Integer.parseInt(groups[0]);
								int index1 = Integer.parseInt(groups1[0]);
								if(index1 < index)
								{
									list.set(j, s1);
								}
								else
								{
									list.set(j+1, s1);
								}
							}
							else
							{
								break;
							}
						}
						else
						{
							break;
						}
					}
				}
				else
				{
					break;
				}
			}
			else
			{
				break;
			}
			//System.out.println("列表项"+(i+1)+s);
		}
		*/
	}
}
