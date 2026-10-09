package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.*;

class GisXYZ
{
	GisGUI gui;
	GisUDP udp;
	public static GisGetIP ggip;
	GisDataProc gdp;
	Database db;
	int w;
	int h;
	int h1;
	int w1;
	int w3;
	int oldw;
	int oldh;
	public static final Font fGLb = new Font("Arial Black", 1, 40);
	//public static final Font fontG = new Font("Serif",Font.PLAIN,11);
	public static final Font fontG = new Font("BOLD",0,12);
	public static final Color anti_Color = new Color(0xffffff);//反色
	public static final Color pure_Color = new Color(0x0);//正色
	public static int hwtest = 0;
	public static boolean debug = false;
	public static int realTab = 0;
	//public static int bufferLen = 8192;
	public static int bufferLen = 5120;
	public static boolean refresh = false;
	//public static String addr = "192.168.1.1";
	GregorianCalendar time;
	SiteData currentOpenSite;//mana-项目管理-当前选择项目
	GisPeriodBuffer tempbuffer;
	GisExplorer ge;
	public boolean selectNewSite;//mana-项目管理-选择新项目
	int ref[];
	public static int initlang = 0;
	
	public static void main(String args[])
	{
		// 1. 静默并重定向 System.err，彻底防止被 exe4j、WinLauncher 等外壳包装器拦截生成 error.log
		try {
			System.setErr(new PrintStream(new OutputStream() {
				@Override
				public void write(int b) {}
				@Override
				public void write(byte[] b, int off, int len) {}
			}));
		} catch (Exception ignored) {}

		// 2. 清理当前运行目录下残留的 error.log 与 pattern.txt
		try {
			File errLog = new File("error.log");
			if (errLog.exists()) {
				errLog.delete();
			}
			File patternFile = new File("pattern.txt");
			if (patternFile.exists()) {
				patternFile.delete();
			}
		} catch (Exception ignored) {}

		// 3. 关闭 java.util.logging 根 Logger 的 ConsoleHandler，避免任何框架级错误输出到标准错误流
		try {
			Logger rootLogger = LogManager.getLogManager().getLogger("");
			if (rootLogger != null) {
				for (Handler h : rootLogger.getHandlers()) {
					rootLogger.removeHandler(h);
				}
			}
		} catch (Exception ignored) {}

		//M.ReadMsg("zh_CN.txt");
		//Option.readOption();
		ChooseLang.chooselang();
		
		/*
		try
		{
			StreamTokenizer streamtokenizer = new StreamTokenizer(new FileReader("ip.txt"));
			streamtokenizer.nextToken();
			addr = streamtokenizer.sval;
			streamtokenizer = null;
			
		}
		catch (Exception exception)
		{
			System.out.println("ip.txt file open error");
		}
		*/
		
		if (HbDesktopPane.addr == null || HbDesktopPane.addr.trim().isEmpty())
			HbDesktopPane.addr = AppGlobalConfig.getDeviceIp();
		System.out.println((new StringBuilder()).append(" device address : ").append(HbDesktopPane.addr).toString());
			(new GisXYZ()).run();

		//gismz.run();
	}

	
	
	public GisXYZ()
	{	
		JFrame.setDefaultLookAndFeelDecorated(false);
		currentOpenSite = null;
		tempbuffer = new GisPeriodBuffer();
		selectNewSite = false;
		ref = new int[8];		
		this.gdp = new GisDataProc(8, this);
		System.out.println("this.gdp   " + this.gdp);
		this.db = new Database("data");
		this.currentOpenSite = this.db.openSite(SiteChooser.selectSite(db, true));
		this.ge = new GisExplorer(this.db);
		this.ge.openSite(this.currentOpenSite);
		this.ge.setDay(System.currentTimeMillis());
		this.gui = new GisGUI(this);
		this.udp = new GisUDP(this.gdp);		
		this.udp.start();
		/*
		 * this.ggip=new GisGetIP(61823,gui); this.ggip.start();
		 */

		this.time = new GregorianCalendar();
		
		

		//61禁用 生成配置文件
		/*
		 * File fll=new File("config.properties"); if (!fll.exists()) { Properties pro;
		 * FileWriter proos; try { pro = new Properties(); proos = new
		 * FileWriter("config.properties"); pro.setProperty("ch1", "特高频");
		 * pro.setProperty("ch2","高频电流"); pro.setProperty("ch3", "地电波");
		 * pro.setProperty("ch4", "特高频"); pro.setProperty("ch5","高频电流");
		 * pro.setProperty("ch6", "地电波"); pro.setProperty("bt-clicked",
		 * "CH5-8 → CH1-4"); pro.setProperty("bt-notclicked", "CH1-4 → CH5-8");
		 * pro.store(proos,"config"); proos.close();
		 * 
		 * } catch (FileNotFoundException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } }
		 */
		
		
		//61 读取配置文件

		//61
		/*
		 * Properties pro; FileReader proos; try { pro = new Properties(); proos = new
		 * FileReader("config.properties"); pro.load(proos);
		 * GisGUI.strlabelname[0].setText(pro.getProperty("ch1", "特高频"));
		 * GisGUI.strlabelname[1].setText(pro.getProperty("ch2","高频电流"));
		 * GisGUI.strlabelname[2].setText(pro.getProperty("ch3", "地电波"));
		 * GisGUI.strlabelname[3].setText(pro.getProperty("ch4", "特高频"));
		 * GisGUI.strlabelname[4].setText(pro.getProperty("ch5","高频电流"));
		 * GisGUI.strlabelname[5].setText(pro.getProperty("ch6", "地电波"));
		 * HbDesktopPane.btclicked=pro.getProperty("bt-clicked", "CH5-8 → CH1-4");
		 * HbDesktopPane.btnotclicked=pro.getProperty("bt-notclicked", "CH1-4 → CH5-8");
		 * for (int bbs=0;bbs<6;bbs++) {
		 * GisGUI.strlabelname[bbs].setForeground(Color.RED);
		 * GisGUI.strlabelname[bbs].setFont(new Font("黑体",1,20)); } proos.close();
		 * 
		 * } catch (FileNotFoundException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); }
		 */
		
		
		
		
		
		
	}

	public void run()
	  {
		SiteData sitedata = null;
		int n = -1;
		int l1 = -1;
		int i2 = -1;
		int[] ai = new int[8];
		int i4 = 0;
		this.time.setTimeInMillis(System.currentTimeMillis());
		n = this.time.get(12);
		//n = this.time.get(13);
		int i;
		while(true){			
		try
		{	
			Thread.sleep(15L);
			/*
			if (this.udp.retry > 30 || !this.gui.dp.conButton.isSelected())
			{
				this.gui.dp.statusLabel.setText((new StringBuilder()).append(" ").append(ChooseLang.M[309]).toString());
				if (this.udp.retry > 30)
				{
					this.gui.dp.conButton.setText((new StringBuilder()).append(" ").append(ChooseLang.M[31]).append(" ").toString());
					this.gui.dp.conButton.setBackground(Color.YELLOW);
					this.gui.dp.conButton.setForeground(Color.BLUE);
				}
				this.gui.dp.conButton.setSelected(false);
				udp.guRun1 = false;
				if (this.udp.retry > 30 && this.time.get(13) != l1)
				{
					l1 = this.time.get(13);
					this.udp.guRun1 = true;
					this.gui.dp.conButton.setSelected(true);
					this.gui.dp.conButton.setForeground(Color.BLACK);
					if (this.udp.guRun2)
					{
						this.udp.sendPacket(0, 2, 0);
					}
					this.udp.retry = 0;
				}
			}
			*/
			if (this.udp.retry > 30 || !this.gui.dp.conButton1.isSelected())
			{
				this.gui.dp.statusLabel.setText((new StringBuilder()).append(" ").append(ChooseLang.M[309]).toString());
				if (this.udp.retry > 30)
				{
					this.gui.dp.conButton.setText((new StringBuilder()).append(" ").append(ChooseLang.M[31]).append(" ").toString());
					this.gui.dp.conButton.setBackground(Color.YELLOW);
					this.gui.dp.conButton.setForeground(Color.blue);
				}
				this.gui.dp.conButton1.setSelected(false);
				udp.guRun1 = false;
				if (this.udp.retry > 30 && this.time.get(13) != l1)
				{
					l1 = this.time.get(13);
					this.udp.guRun1 = true;
					this.gui.dp.conButton1.setSelected(true);
					//this.gui.dp.conButton.setForeground(Color.BLACK);
					if (this.udp.guRun2)
					{
						this.udp.sendPacket(0, 4, 0);
					}
					this.udp.retry = 0;			        
				}
			}
			
			if (this.selectNewSite)
			{
				this.selectNewSite = false;
				//this.gui.full.setEnabled(false);
				String s1 = SiteChooser.selectSite(this.db, false);
				//this.gui.full.setEnabled(true);
				if (s1 != null)
				{
					this.currentOpenSite = this.db.openSite(s1);
				}
			}
			if (this.currentOpenSite != sitedata)
			{
				sitedata = this.currentOpenSite;
				if (this.currentOpenSite != null)
				{
					this.gui.fullPane.setTitleAt(0, (new StringBuilder()).append(ChooseLang.M[21]).append("-").append(this.currentOpenSite.siteName).toString());//ѡ���ʵʱ��
				}
			}
			
			

			if (!refresh)
			{
				

				//开始读取	61
				if (this.udp.guRun1)
				{



					if (((this.gdp.gd.sync & 7) == 0) || ((this.gdp.gd.sync & 7) == 2))
						this.gui.dp.statusLabel.setText(String.format((new StringBuilder()).append(" ").append(ChooseLang.M[309]).append(" %3.2fHz").toString(), new Object[] {
							Float.valueOf(this.gdp.gd.freq)
						}));
					else
					if (((this.gdp.gd.sync & 7) == 4) || ((this.gdp.gd.sync & 7) == 6))
						this.gui.dp.statusLabel.setText(String.format((new StringBuilder()).append(" ").append(ChooseLang.M[309]).append("i%3.2fHz").toString(), new Object[] {
							Float.valueOf(this.gdp.gd.freq)
						}));
					else
					if ((this.gdp.gd.sync & 7) == 3)
						this.gui.dp.statusLabel.setText(String.format((new StringBuilder()).append(" ").append(ChooseLang.M[309]).append("v%3.2fHz").toString(), new Object[] {
							Float.valueOf(this.gdp.gd.freq)
						}));
				} else
				{
					//mana-一直输出this.gdp.gd.sync
					//System.out.println(this.gdp.gd.sync);
				}
				int k = 0;
				i = 0;
				while (i < 8) 
				{
					if (!this.gui.suspendButtonPRPS[i].isSelected())
					{
						k = 1;
						int j;
						this.gui.prps[i].resultString[1] = "";
						this.gui.prps[i].resultString[2] = "";
						this.gui.prps[i].resultString[3] = "";
						
						this.gui.prpd[i].resultStringprpd[1] = "";
						this.gui.prpd[i].resultStringprpd[2] = "";
						this.gui.prpd[i].resultStringprpd[3] = "";
						ai[0] = 0;
						ai[1] = 0;
						ai[2] = 0;
						ai[3] = 0;
						j = 0;					
						while(j < 8)
						{
							if ((this.gdp.gd.an[i].analysisResult[j] > 50) && (k < 2))
							//if (k < 4)
							{
								ai[k] = this.gdp.gd.an[i].analysisResult[j];
								//第四通道时高频CT
								
								if(i == 8 )
								{
									if (PDResult.resultStr[j] == ChooseLang.M[10] || PDResult.resultStr[j] == ChooseLang.M[11] || PDResult.resultStr[j] == ChooseLang.M[12] )
									{
										this.gui.prps[i].resultString[1] = ChooseLang.M[16];
									}
									else
									{
										this.gui.prps[i].resultString[k] = PDResult.resultStr[j];
									}
									String[] resultstringCT = this.gui.prps[i].resultString;
									resultstringCT[k] = (resultstringCT[k] + " " + this.gdp.gd.an[i].analysisResult[j] + "%");
								}
								else
								{
									this.gui.prps[i].resultString[k] = PDResult.resultStr[j];
									String[] resultstring = this.gui.prps[i].resultString;
									resultstring[k] = (resultstring[k] + " " + this.gdp.gd.an[i].analysisResult[j] + "%");
									if(i == 5 || i == 6)//超声波
									{
										this.gui.prpd[i].resultStringprpd[k] = PDResult.resultStr[j];
										String[] resultstringprpd = this.gui.prpd[i].resultStringprpd;
										resultstringprpd[k] = (resultstringprpd[k] + " " + this.gdp.gd.an[i].analysisResult[j] + "%");
									}
								}
								
								/*
								this.gui.prps[i].resultString[k] = PDResult.resultStr[j];
								String[] resultstring = this.gui.prps[i].resultString;
								resultstring[k] = (resultstring[k] + " " + this.gdp.gd.an[i].analysisResult[j] + "%");
								*/
								k++;
							}
							j++;//continue;
						}
						
						j = 1;
						if (j < 8)
			              {
			                k = j;
			                while (k < 8)
			                {
			                  if (ai[k] > ai[j])
			                  {
			                    int m = ai[k];
			                    ai[k] = ai[j];
			                    ai[j] = m;
			                    String str1 = this.gui.prps[i].resultString[j];
			                    this.gui.prps[i].resultString[j] = this.gui.prps[i].resultString[k];
			                    this.gui.prps[i].resultString[k] = str1;
			                    if(i == 2)//超声波
			                    {
				                    String str2 = this.gui.prpd[i].resultStringprpd[j];
				                    this.gui.prpd[i].resultStringprpd[j] = this.gui.prpd[i].resultStringprpd[k];
				                    this.gui.prpd[i].resultStringprpd[k] = str2;
			                    }
			                  }
			                  k++; //continue;
			                }
			                j++; //continue;
			              }
			            }
			            if ((!this.gui.suspendButtonPRPS[i].isSelected()) && (this.gui.noisegate[i].isSelected()) && (i == 7))
			            {
			              this.gui.prps[i].resultString[1] = ChooseLang.M[259];
			              this.gui.prps[i].resultString[2] = "";
			              this.gui.prps[i].resultString[3] = "";
			            }
			            if ((!this.gui.prps[i].resultString[1].contains(ChooseLang.M[15])) && (this.gdp.gd.an[i].analMax > 50) && (this.time.get(13) != i2))
			            {
			              i2 = this.time.get(13);
			              this.udp.sendPacket(0, 5, 0);
			              this.gdp.gd.an[i].analMax = 0;
			            }
			            //System.out.println(String.valueOf(i)+this.gui.prpd[i].amxdBm);//mana-测试amxdbm为
			            if(i == 5 || i == 6)
						{
			            	this.gui.prpd[i].stringTrgDraw = "";
			            	if(this.gui.prpd[i].amxdBm > -70.0d)
			            	{
			            		if(i == 2)
			            		{
									if(this.gui.prpd[2].amxdBm > this.gui.prpd[3].amxdBm)
									{
										this.gui.prpd[2].stringTrgDraw = "优先触发";
										this.gui.prpd[3].stringTrgDraw = "";
									}
			            		}
			            		else if(this.gui.prpd[3].amxdBm > this.gui.prpd[2].amxdBm)
								{
									this.gui.prpd[3].stringTrgDraw = "优先触发";
									this.gui.prpd[2].stringTrgDraw = "";
								}
								else
								{
									this.gui.prpd[i].stringTrgDraw = "";
								}
			            	}
			            	else
			            	{
			            		this.gui.prpd[i].stringTrgDraw = "";
			            	}
						}
						
			            i++; //continue;
			          }
			        }
		        this.w = this.gui.dp.dp.getWidth();
		        this.h = this.gui.dp.dp.getHeight();
		        if ((this.oldw != this.w) || (this.oldh != this.h))
		        {
		          this.oldw = this.w;this.oldh = this.h;
		          this.gui.dp.aiButton();
		        }
		        this.time.setTimeInMillis(System.currentTimeMillis());		        
		        if (this.time.get(12) != n)
		        {
		          n = this.time.get(12);
		          if (this.udp.guRun1)
		          {
		            if (hwtest == 0) 
		            {
		              this.currentOpenSite.savePeriod(this.gdp.gd.pb, this.time.getTimeInMillis());
		            }
		            this.ge.updateCurrent();    //memo 20230831 更新一天可信度图
		          }
		        }
		        int divisor = AppGlobalConfig.isSpectrogramRollingEnabled() ? 2 : 8;
		        i4 = (i4 + 1) % divisor;//刷新频率 (滚动模式约33FPS流水，整组模式约8FPS)
		        if (i4 == 0) {
		          this.gui.guiRefresh();
		        }
		        refresh = false;		        
		        this.gui.memoPanel.timer();		        
		        //System.out.println("测试时间：：" + (this.gui.dp.currenttime - this.gui.dp.starttime)/1000);
		      }	
			catch (Exception localException)
	      {
	        //localException.printStackTrace();
	      }
	      realTab = this.gui.fullPane.getSelectedIndex();
		}
	  }

	public String getipAddr(){
		return this.gui.dp.iptext.getText();
	}

	public void shutdownApplication() {
		try {
			if (gdp != null && gdp.gd != null && gdp.gd.persistentPeakService != null) {
				gdp.gd.persistentPeakService.shutdown();
			}
			if (udp != null) {
				udp.guRun1 = false;
				udp.guRun2 = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
	
	
//	public static boolean createfile(String file){
//		boolean flag = false;
//		File newf = new File(file);
//		if (!newf.exists()) {
//			try {
//				newf.createNewFile();
//			}catch (IOException e) {
//				e.printStackTrace();
//			}
//			flag=true;
//		}
//		return flag;
//	}
//	
//	public static boolean writeintxtfile(String content,String file ,boolean append){
//		boolean flag = false;
//		File thisfile = new File(file);
//		try {
//			if (!thisfile.exists()) {
//				thisfile.createNewFile();
//			}
//			FileWriter fw=new FileWriter(file,append);
//			fw.write(content);
//			fw.close();
//		}catch (IOException e) {
//			e.printStackTrace();
//		}
//		flag=true;
//		
//		return flag;
//	}
//	
//	public static boolean readintxtfile(String file){
//		boolean flag = false;
//		File thisfile = new File(file);
//		try {
//			if (!thisfile.exists()) {
//				thisfile.createNewFile();
//			}
//			FileWriter fw=new FileWriter(file,append);
//			fw.write(content);
//			fw.close();
//		}catch (IOException e) {
//			e.printStackTrace();
//		}
//		flag=true;
//		
//		return flag;
//	}
	
}
