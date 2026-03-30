package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

class ReportCheck extends JFrame
{
	private static final long serialVersionUID = 197307030002L;
	File outFile;
	
	class PrintProgressThread2 extends Thread//打印进程2
	{

		int gw;//宽
		int gh;//高
		String gfn;

		public void run()
		{
			int i = 0;
		      
		      GisReport localGisReport = new GisReport(gframe, outFile);
		      BufferedImage localBufferedImage = new BufferedImage(gw, gh, 1);
		      Graphics2D localGraphics2D = localBufferedImage.createGraphics();
		      localGraphics2D.setPaint(Color.white);//图像笔刷颜色
		      localGraphics2D.fillRect(0, 0, gw, gh);//填充大小
		      while (localGisReport.print(localGraphics2D, gw - gw / 5, gh - gw / 5, gw / 10, gw / 10, i++) == 0)
		      {
		        try
		        {
		          ImageIO.write(localBufferedImage, "png", new File(gfn + String.format("_%02d.png", new Object[] { Integer.valueOf(i) })));
		        }
		        catch (Exception localException)
		        {
		          localException.printStackTrace();
		        }
		        localBufferedImage = new BufferedImage(gw, gh, 1);
		        localGraphics2D = localBufferedImage.createGraphics();
		        localGraphics2D.setPaint(Color.white);
		        localGraphics2D.fillRect(0, 0, gw, gh);
		      }
		      gtpage = -2;
		}
	}

	class PrintProgressThread1 extends Thread
	{
		public void run()
		{
			PrinterJob printerjob = PrinterJob.getPrinterJob();
			printerjob.setPrintable(new GisReport(gframe, outFile));
			try
			{
				if (printerjob.printDialog())
				{
					printerjob.print();
					gtpage = -2;
				} else
				{
					gtpage = -3;
				}
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
				gtpage = -3;
			}
		}
	}

	class WaitProgressThread extends Thread//等待进程
	{
		WaitProgressThread() {}
		public void run()
		{
			gframe.setEnabled(false);
			JProgressBar jprogressbar = new JProgressBar(0, 1);
			jprogressbar.setValue(0);
			jprogressbar.setStringPainted(true);
			jprogressbar.setBorderPainted(false);
			jprogressbar.setString("Wait . . .");
			jprogressbar.setSize(200, 100);
			JFrame jframe = new JFrame("Print");
			jframe.setDefaultCloseOperation(0);
			jframe.setLayout(new BorderLayout());
			jframe.setSize(300, 70);
			jframe.setAlwaysOnTop(true);
			jframe.add(jprogressbar, "Center");
			jframe.setLocationRelativeTo(gframe);
			jframe.setVisible(true);
			jprogressbar.setMinimum(0);
			jprogressbar.setMaximum(200);
			while (gtpage == -1) 
			{
				for (int i = 0; i < 200; i++)
				{
					jprogressbar.setValue(i);
					try
					{
						Thread.sleep(1L);
					}
					catch (Exception exception) { }
				}

				int j = 200;
				while (j > 0) 
				{
					jprogressbar.setValue(j);
					try
					{
						Thread.sleep(1L);
					}
					catch (Exception exception1) { }
					j--;
				}
			}
			jprogressbar.setMaximum(gtpage);
			while (0 < gtpage) 
			{
				jprogressbar.setString((new StringBuilder()).append("Page: ").append(gppage).append(" / ").append(gtpage).toString());
				jprogressbar.setValue(gppage);
				try
				{
					Thread.sleep(10L);
				}
				catch (Exception exception2) { }
			}
			jframe.dispose();
			jframe = null;
			if (gtpage == -2)
			{
				JOptionPane.showMessageDialog(null, ChooseLang.M[130], "Report", 1);
			}
			gframe.setEnabled(true);
			removeNotify();
		}		
	}
	
	//调用屏幕键盘
	class OpenOSK implements MouseListener
	{

		@Override
		public void mouseClicked(MouseEvent e) {
			try {
				Runtime.getRuntime().exec("cmd /c osk.exe");
				 
			} catch (IOException e1) {
				e1.printStackTrace();
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
	
	
	SiteData site;
	GisXYZ gm;
	long startTime;
	long endTime;
	String userName;
	String location;
	String memo;
	boolean check[] = {
		true, true, true, true ,true, true, true, true
	};
	String subLocation[];
	boolean printBlackWhite;
	boolean print3DPRPS;
	int reportType;
	JTextField userTF;
	JTextField locaTF;
	JTextField sublTF[];
	JTextField memoTA;
	JCheckBox checkCh[];
	JCheckBox sepCh;
	JCheckBox prps3dCh;
	JCheckBox prpsbnwCh;
	String reportTypeText[];
	public boolean drawSeperate;
	ReportCheck gframe;
	int gtpage;
	int gppage;
	//文本框按钮
	JButton jkey;

	public ReportCheck(int i, long l, long l1, SiteData sitedata, boolean flag)
	{
		/*
		subLocation = (new String[] {
			ChooseLang.M[232], ChooseLang.M[233], ChooseLang.M[234], ChooseLang.M[15]
		});//通道1、2、3、噪声
		*/
		subLocation = (new String[] {
				ChooseLang.M[232], ChooseLang.M[233], ChooseLang.M[234], ChooseLang.M[235] ,ChooseLang.M[286], ChooseLang.M[287], ChooseLang.M[288], ChooseLang.M[289]
			});//通道1、2、3、3
		printBlackWhite = true;
		print3DPRPS = false;
		userTF = new JTextField(ChooseLang.M[114]);//检测人
		userTF.addMouseListener(new OpenOSK());
		locaTF = new JTextField(ChooseLang.M[115]);//检测地点
		locaTF.addMouseListener(new OpenOSK());
		sublTF = new JTextField[4];
		memoTA = new JTextField();
		checkCh = new JCheckBox[4];
		sepCh = new JCheckBox(ChooseLang.M[116]);//选择打印通道
		prps3dCh = new JCheckBox(ChooseLang.M[117]);//3D PRPS打印
		prpsbnwCh = new JCheckBox(ChooseLang.M[118]);//黑白打印
		reportTypeText = (new String[] {
			ChooseLang.M[119], ChooseLang.M[120]
		});
		drawSeperate = true;
		setTitle((new StringBuilder()).append(reportTypeText[i]).append(ChooseLang.M[121]).toString());
		reportType = i;
		site = sitedata;
		startTime = l;
		endTime = l1;
		JPanel jpanel = new JPanel(new BorderLayout(10, 10));
		JPanel jpanel1 = new JPanel(new BorderLayout(10, 10));
		JPanel jpanel2 = new JPanel(new GridLayout(1, 1, 20, 10));
		JPanel jpanel3 = new JPanel(new BorderLayout(10, 10));
		JPanel jpanel4 = new JPanel(new GridLayout(0, 1, 10, 5));
		JPanel jpanel5 = new JPanel(new GridLayout(0, 1, 10, 5));
		jpanel4.add(new JLabel(ChooseLang.M[122]));//检测时间
		jpanel4.add(new JLabel(ChooseLang.M[123]));//检测人
		jpanel4.add(new JLabel(ChooseLang.M[124]));//检测地点
		jpanel4.add(new JLabel(ChooseLang.M[125]));//检测位置
		jpanel4.add(new JLabel(""));
		jpanel4.add(new JLabel(""));
		jpanel4.add(new JLabel(""));
		jpanel4.add(new JLabel(ChooseLang.M[126]));//打印选项
		jpanel4.add(new JLabel(""));
		jpanel4.add(new JLabel(""));
		jpanel4.add(new JLabel(ChooseLang.M[127]));//图像文件
		jpanel5.add(new JLabel(String.format("%tF %tT ~ %tF %tT", new Object[] {
			Long.valueOf(l), Long.valueOf(l), Long.valueOf(l1), Long.valueOf(l1)
		})));//检测时间
		jpanel5.add(userTF);
		locaTF.setText(sitedata.siteName);//检测地点
		jpanel5.add(locaTF);
		for (int j = 0; j <  HbDesktopPane.tongdao; j++)
		{
			sublTF[j] = new JTextField(subLocation[j]);//通道1、2、3、噪声
			JPanel jpanel6 = new JPanel(new BorderLayout(0, 0));
			jpanel6.add(sublTF[j], "Center");
			jpanel6.add(new JLabel((new StringBuilder()).append("Ch").append(j + 1).append("  ").toString()), "West");
			checkCh[j] = new JCheckBox();
			if(j < HbDesktopPane.tongdao)
			{
				checkCh[j].setSelected(check[j]);//复选框
				jpanel6.add(checkCh[j], "East");
				jpanel5.add(jpanel6);
			}
		}

		sepCh.setSelected(true);
		prpsbnwCh.setSelected(false);
		prps3dCh.setSelected(true);
		jpanel5.add(sepCh);
		jpanel5.add(prps3dCh);
		jpanel5.add(prpsbnwCh);
		jpanel5.add(memoTA);
		jpanel3.add(jpanel4, "West");
		jpanel3.add(jpanel5, "Center");
		jpanel2.add(jpanel3);
		jpanel1.add(jpanel2, "Center");
		JPanel jpanel7 = new JPanel(new GridLayout(2, 1, 2, 2));
		JButton jbutton = new JButton(ChooseLang.M[128]);//保存到文件
		jpanel7.add(jbutton);
		jbutton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				saveNow();
			}

		});
		JButton jbutton1 = new JButton(ChooseLang.M[129]);//打印
		jpanel7.add(jbutton1);
		jbutton1.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent)
			{
				processNow();
			}

		});
		jpanel1.add(jpanel7, "South");
		jpanel.add(jpanel1, "Center");
		jpanel.add(new JLabel(""), "North");
		jpanel.add(new JLabel(""), "South");
		jpanel.add(new JLabel(""), "East");
		jpanel.add(new JLabel(""), "West");
		add(jpanel);
		setDefaultCloseOperation(2);
		pack();
	}

	public void saveNow()//保存到文件
	{
		userName = userTF.getText();//检测人
		location = locaTF.getText();//检测地点
		for (int i = 0; i <HbDesktopPane.tongdao; i++)
		{
			check[i] = checkCh[i].isSelected();//复选框
			subLocation[i] = sublTF[i].getText();//通道1、2、3、噪声
		}

		memo = memoTA.getText();//图像文件
		drawSeperate = sepCh.isSelected();//打印选择通道
		printBlackWhite = prpsbnwCh.isSelected();//黑白打印
		print3DPRPS = prps3dCh.isSelected();//3D PRPS打印
		char c = '\u032E';
		char c1 = '\u0480';
		JFileChooser jfilechooser = new JFileChooser();
		jfilechooser.setSelectedFile(new File((new StringBuilder()).append("GISReport_").append(site.siteName).append("_").append(String.format("%tF %tT - %tF %tT", new Object[] {
			Long.valueOf(startTime), Long.valueOf(startTime), Long.valueOf(endTime), Long.valueOf(endTime)
		})).append(".png").toString().replace(':', ';')));//报告名字.png
		if (jfilechooser.showSaveDialog(this) == 0)
		{
			Object aobj[] = {
				"814x1152", "905x1280", "1131x1600", "1810x2560"
			};
			String s = (String)JOptionPane.showInputDialog(this, "Capture Resolution", "Resolution", 1, null, aobj, aobj[0]);//捕捉分辨率
			if (s == null)
				return;
			if (s.matches("814x1152"))//图像大小814x1152
			{
				c = '\u032E';
				c1 = '\u0480';
			} else
			if (s.matches("905x1280"))//图像大小905x1280
			{
				c = '\u03B6';
				c1 = '\u0500';
			} else
			if (s.matches("1131x1600"))//图像大小1131x1600
			{
				c = '\u046B';
				c1 = '\u0640';
			} else
			if (s.matches("1810x2560"))//图像大小1810x2560
			{
				c = '\u0712';
				c1 = '\u0A00';
			}
			String s1 = jfilechooser.getSelectedFile().getAbsolutePath();
			File jfile = jfilechooser.getSelectedFile();
			if (s1.endsWith(".png") || s1.endsWith(".PNG"))
				s1 = s1.substring(0, s1.length() - 4);
			writeImage(s1, c, c1, jfile);
		}
	}

	public void processNow()
	{
		System.out.println(ChooseLang.M[131]);
		userName = userTF.getText();
		location = locaTF.getText();
		for (int i = 0; i < 8; i++)
		{
			check[i] = checkCh[i].isSelected();
			subLocation[i] = sublTF[i].getText();
		}

		memo = memoTA.getText();
		drawSeperate = sepCh.isSelected();
		printBlackWhite = prpsbnwCh.isSelected();
		print3DPRPS = prps3dCh.isSelected();
		gframe = this;
		gtpage = -1;
		gppage = 0;
		WaitProgressThread waitprogressthread = new WaitProgressThread();
		PrintProgressThread1 printprogressthread1 = new PrintProgressThread1();
		waitprogressthread.start();
		printprogressthread1.start();
	}

	public void writeImage(String s, int i, int j, File file)
	{
		outFile = file;
		gframe = this;
		gtpage = -1;
		gppage = 0;
		WaitProgressThread waitprogressthread = new WaitProgressThread();//等待进程
		PrintProgressThread2 printprogressthread2 = new PrintProgressThread2();//打印进程
		printprogressthread2.gfn = s;
		printprogressthread2.gw = i;
		printprogressthread2.gh = j;
		waitprogressthread.start();
		printprogressthread2.start();
	}
}
