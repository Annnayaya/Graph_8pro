package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class SiteChooser
{

	static boolean siteSelected = false;
	static boolean jfisVisible = true;
	static Database db;
	static JFrame jf;
	static JTextArea text;
	static JList siteList;
	static JPopupMenu jpopupmenu;
	static JMenuItem jmenuitem;
	static int chooselist;
	static JScrollPane textPane;
	static JComboBox<String> languagechooser;
	static JComboBox<String> chnumchooser;
	
	static JLabel uselang;
	static JLabel usechnum;
	//static String[] lang = {ChooseLang.M[0],"English","한국어"};
	static int choose;	
	static int chnum;
	static File file = new File("lang"+File.separator+"flag.dat");
	static File filenum = new File("lang"+File.separator+"flagnum.dat");
	static Object option[];
	static JOptionPane joptionpanelang = new JOptionPane();
	static JOptionPane joptionpaneinput = new JOptionPane();
	static JRoundButton jbutton;
	static OpenListener openlistener;
	static OpenDatabase opendatabase;
	
	SiteChooser()
	{
	}

	public static class myJListListener extends MouseAdapter  {
		//e.getButton() 返回值有 1，2，3。1代表鼠标左键，3代表鼠标右键	 
	    //jList.getSelectedIndex() 返回的是选中的JList中的项数。
	    //if语句的意思也就是，在JList 中点击了右键而且JList选中了某项，显示右键菜单
	    //e.getX() , e.getY() 返回的是鼠标目前的位置！也就是在目前鼠标的位置上弹出右键
	 
	@SuppressWarnings("unused")
	public void mouseClicked(MouseEvent e) {
				
				chooselist =siteList.getSelectedIndex();
	            if(e.getButton() == MouseEvent.BUTTON3 && siteList.getSelectedIndex() != -1)
	            {
	            		int index =siteList.locationToIndex(e.getPoint());
	            		//jpopupmenu.show(e.getComponent(), e.getX(), e.getY());
	                    jpopupmenu.show(siteList,e.getX(),e.getY());
	                    //System.out.println("选择列="+ chooselist);
	            }	            					 
	      }
	      
		/*
	 public void mousePressed(MouseEvent e){
		 int index = siteList.locationToIndex(e.getPoint());
		 siteList.setSelectedIndex(index);
		 if(SwingUtilities.isRightMouseButton(e)){
			 jpopupmenu.show(siteList,e.getX(),e.getY());
		 }
	 }
	 */
	}
	
	//打开存在的数据库
	public static class  OpenDatabase implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser jfilechooser = new JFileChooser();
			GisFileFilter gisfilefilter = new GisFileFilter();
			jfilechooser.addChoosableFileFilter(gisfilefilter);
			jfilechooser.setFileFilter(gisfilefilter);
			
			String s2 = SiteData.pathName;
			
			int n = jfilechooser.showOpenDialog(jfilechooser);

			if(n == 0)
			{
				
				//导入数据
				String s1 = jfilechooser.getSelectedFile().getAbsolutePath();
				GisDataImport gisdataimport = new GisDataImport(s1, s2);
				gisdataimport.setLocationRelativeTo(jf);
				//刷新数据列表
				RefreshList refresh = new RefreshList();
				refresh.start();
				
			}
			else
			{
				return;
			}
			
		}		
	}
	
	//打开数据库侦听
	public static class OpenListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent event) {
			SiteChooser.siteSelected = true;
		}
		
	}
	
	//刷新数据列表
	public static class RefreshList extends Thread
	{
		@SuppressWarnings("unchecked")
		public void run()
		{	
			//System.out.println("刷新列表");
			GisDataImport.setisComplete(false);
			do
			{
				//System.out.println("刷新列表do");
				//这里做一个延时，为了下面刷新列表取静态变量
				for(int i = 50; i > 0; i--)

				if(GisDataImport.getisComplete())
				{					 			
					  //System.out.println("刷新列表if");
					  SiteChooser.db.getAvailableSites();
					  SiteChooser.siteList.setListData(db.sites.toArray());
					  SiteData sd1[] = SiteChooser.db.getAvailableSiteArray();
					  SiteChooser.siteList.setListData(sd1);
					  SiteChooser.siteList.setSelectedIndex(sd1.length - 1);
					  ((SiteData)siteList.getSelectedValue()).siteName = sd1[sd1.length - 1].siteName;
					  jbutton.removeActionListener(opendatabase);
					  jbutton.addActionListener(openlistener);
					  //这里做一个延时，为了下面刷新列表取静态变量						
					  for(int j = 50; j > 0; j--)
						  
					  jbutton.doClick();
				}
			}
			while(!GisDataImport.getisComplete());
			
		}
	}
	
	//调用屏幕键盘
	public static void OpenOSK()
	{
		try {
			Runtime.getRuntime().exec("cmd /c osk.exe");
			 
		} catch (IOException e1) {
			e1.printStackTrace();
		}	
	}
	
	public static String selectSite(Database database, boolean flag)
	{
		db = database;
		siteSelected = false;
		JFrame.setDefaultLookAndFeelDecorated(false);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension dimension = toolkit.getScreenSize();
		option =(new Object[] {ChooseLang.M[4],ChooseLang.M[5]});
		jf = new JFrame(ChooseLang.M[100]);
		jf.setResizable(false);//窗口固定大小
		
		
		uselang = new JLabel(ChooseLang.M[2],4);
		uselang.setFont(GisXYZ.fontG);
		//uselang.setVisible(false);//语言标签不可见
		opendatabase = new OpenDatabase();
		try {
			choose = readFileByChars(file);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//语言选择列表
		languagechooser = new JComboBox<String>();
		languagechooser.setFont(GisXYZ.fontG);
		languagechooser.setPreferredSize(new Dimension(80,20));
		languagechooser.setMaximumRowCount(5);
		for(int j = 0; j < ChooseLang.A.length; j++){
			languagechooser.addItem(ChooseLang.A[j]);
		}
		languagechooser.setSelectedIndex(choose);
		//languagechooser.setVisible(false);//语言选择列表不可见
		languagechooser.addItemListener(new ItemListener(){

			@SuppressWarnings("unchecked")
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
				    JComboBox<String> languagechooser = (JComboBox<String>) e.getSource();
				    choose = languagechooser.getSelectedIndex();					   
					   //JOptionPane.showMessageDialog(jf, ChooseLang.M[3]);
					   int ok = JOptionPane.showConfirmDialog(jf, ChooseLang.M[3], "", JOptionPane.OK_CANCEL_OPTION);
					   JPasswordField passwordfield = new JPasswordField();
						Object[] message = {ChooseLang.M[360],passwordfield};
						JOptionPane passwordpane = new JOptionPane(message,JOptionPane.INFORMATION_MESSAGE);
						JDialog dialog = passwordpane.createDialog(jf, ChooseLang.M[361]);
						//OpenOSK();
						dialog.show(true);				
						String password = passwordfield.getText();
						//String password = JOptionPane.showInputDialog(jf,"请输入管理员密码！");
						if(!password.equals(""))
						{
							if(password.equals("jyadmin"))//确定更改语言并关闭窗口，否则不更改
							   {
								 if(JOptionPane.OK_OPTION == ok)//确定更改语言并关闭窗口，否则不更改
								   {
									   try {
											writeFileByChars(file,choose);
											languagechooser.setSelectedIndex(choose);
										} catch (IOException e1) {
											e1.printStackTrace();
										}
									   jf.dispose();
									   System.exit(0);
								   }
								   else
								   {
									   return;
								   }
								
							   }
						 if(!password.equals("jyadmin"))
							 {
								   JOptionPane.showMessageDialog(jf, ChooseLang.M[362]);
								   return;
							 }
						}
						else
						{
							JOptionPane.showMessageDialog(jf, ChooseLang.M[363]);
							return;
						}
					}
					   
				else {
						return;
				   }
				
			}
			
		});
		
		usechnum = new JLabel(ChooseLang.M[384],4);			
		usechnum.setFont(GisXYZ.fontG);
		//usechnum.setVisible(false);//通道标签不可见
		opendatabase = new OpenDatabase();
		try {
			chnum = readFileByChars(filenum);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//语言选择列表
		chnumchooser = new JComboBox<String>();
		chnumchooser.setFont(GisXYZ.fontG);
		chnumchooser.setMaximumRowCount(5);
		chnumchooser.setPreferredSize(new Dimension(80,20));
		for(int j = 0; j < ChooseLang.CH.length; j++){
			chnumchooser.addItem(ChooseLang.CH[j]);
		}
		chnumchooser.setSelectedIndex(chnum);
		HbDesktopPane.tongdao=8;
		//languagechooser.setVisible(false);//语言选择列表不可见
		chnumchooser.addItemListener(new ItemListener(){

			@SuppressWarnings("unchecked")
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
				    JComboBox<String> chnumchooser = (JComboBox<String>) e.getSource();
				      chnum = chnumchooser.getSelectedIndex();					   
					   int ok = JOptionPane.showConfirmDialog(jf, ChooseLang.M[4], "", JOptionPane.OK_CANCEL_OPTION);
					   JPasswordField passwordfield = new JPasswordField();
						Object[] message = {ChooseLang.M[360],passwordfield};
						JOptionPane passwordpane = new JOptionPane(message,JOptionPane.INFORMATION_MESSAGE);
						JDialog dialog = passwordpane.createDialog(jf, ChooseLang.M[361]);
						//OpenOSK();
						dialog.show(true);				
						String password = passwordfield.getText();
						//String password = JOptionPane.showInputDialog(jf,"请输入管理员密码！");
						if(!password.equals(""))
						{
							if(password.equals("jy123"))//确定更改通道数并关闭窗口，否则不更改
							   {
								 if(JOptionPane.OK_OPTION == ok)//确定更改通道数并关闭窗口，否则不更改
								   {
									   try {
											writeFileByChars(filenum,chnum);
											chnumchooser.setSelectedIndex(chnum);
										} catch (IOException e1) {
											e1.printStackTrace();
										}
									   jf.dispose();
									   System.exit(0);
								   }
								   else
								   {
									   return;
								   }
								
							   }
						 if(!password.equals("jy123"))
							 {
								   JOptionPane.showMessageDialog(jf, ChooseLang.M[362]);
								   return;
							 }
						}
						else
						{
							JOptionPane.showMessageDialog(jf, ChooseLang.M[363]);
							return;
						}
					}
					  
				else {
						return;
				   }
				
			}
			
		});


		try {
			// 1. 定义文件路径
			File iconFile = new File("pics/Logo1.png");
			// 2. 检查文件是否存在
			if (iconFile.exists()) {
				// 只有文件真的存在时，才尝试读取
				jf.setIconImage(ImageIO.read(iconFile));
				System.out.println("成功加载图标: " + iconFile.getAbsolutePath());
			} else {
				// 如果文件不存在，打印提示，但不报错，程序继续运行
				System.out.println("提示: 未找到图标文件 " + iconFile.getAbsolutePath() + "，将使用默认Java图标。");
			}
		} catch (Exception exception) {
			// 即使发生其他错误，也不要打断程序
			System.out.println("图标加载过程出错: " + exception.getMessage());
		}


		text = new JTextArea();
		text.setEditable(false);
		textPane = new JScrollPane(text);
		textPane.setPreferredSize(new Dimension(400, 300));
		SiteData asitedata[] = db.getAvailableSiteArray();
		siteList = new JList(asitedata);
		siteList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent listselectionevent)
			{
				SiteData sitedata = (SiteData)(SiteData)SiteChooser.siteList.getSelectedValue();
				if (sitedata == null)
				{
					SiteChooser.text.setText("");
					return;
				}
				SiteChooser.text.setText((new StringBuilder()).append("-").append(ChooseLang.M[101]).append(":").append(sitedata.siteName).append("\n\n").toString());
				if (sitedata.savedDay != null && sitedata.savedDay.length > 0)
				{
					SiteChooser.text.append(String.format((new StringBuilder()).append("-").append(ChooseLang.M[102]).append(" : %tF\n").toString(), new Object[] {
						Long.valueOf(sitedata.savedDay[0])
					}));
					SiteChooser.text.append(String.format((new StringBuilder()).append("-").append(ChooseLang.M[103]).append(" : %tF\n").toString(), new Object[] {
						Long.valueOf(sitedata.savedDay[sitedata.savedDay.length - 1])
					}));
					Enumeration enumeration = sitedata.children();
					SiteChooser.text.append((new StringBuilder()).append("\n\n  - ").append(ChooseLang.M[104]).append("\n\n").toString());
					for (; enumeration.hasMoreElements(); SiteChooser.text.append((new StringBuilder()).append(" ").append(enumeration.nextElement().toString()).append("\n").toString()));
				} else
				{
					SiteChooser.text.append((new StringBuilder()).append("-").append(ChooseLang.M[102]).append(" : N/A\n").toString());
					SiteChooser.text.append((new StringBuilder()).append("-").append(ChooseLang.M[103]).append(" : N/A\n").toString());
				}
				SiteChooser.text.setCaretPosition(0);
			}

		});
		JScrollPane jscrollpane = new JScrollPane(siteList);
		siteList.setSelectedIndex(0);
		jpopupmenu = new JPopupMenu();
		jmenuitem = new JMenuItem(ChooseLang.M[108]);
		jmenuitem.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				
			}

			@Override
			public void mouseEntered(MouseEvent e) {
		
			}

			@Override
			public void mouseExited(MouseEvent e) {
	
			}

			@SuppressWarnings("unchecked")
			@Override
			public void mousePressed(MouseEvent e) {
				int ok = JOptionPane.showConfirmDialog(jf, ChooseLang.M[108], "", JOptionPane.OK_CANCEL_OPTION);				
				/*
				try {
					Runtime.getRuntime().exec("cmd /c osk.exe");
					 
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				*/
				JPasswordField passwordfield = new JPasswordField();
				Object[] message = {ChooseLang.M[360],passwordfield};
				JOptionPane passwordpane = new JOptionPane(message,JOptionPane.INFORMATION_MESSAGE);
				JDialog dialog = passwordpane.createDialog(jf, ChooseLang.M[361]);
				//OpenOSK();
			    dialog.show(true);				
				String password = passwordfield.getText();
				//String password = JOptionPane.showInputDialog(jf,"请输入管理员密码！");
				if(!password.equals(""))
				{
					//if(JOptionPane.OK_OPTION ==  && password == "admin")//确定更改语言并关闭窗口，否则不更改
					if(password.equals("admin"))//确定更改语言并关闭窗口，否则不更改
					   {
						String s1 = SiteData.pathName;
						String s2 = ((SiteData)siteList.getSelectedValue()).siteName;
						File file = new File(s1 + s2);
						if( SiteChooser.db.deleteSite(file))
						{
							SiteChooser.db.getAvailableSites();
							SiteChooser.siteList.setListData(db.sites.toArray());					
							System.out.println("删除列ID=" + chooselist + "成功");
						}
						else{
							System.out.println("删除列ID=" + chooselist + "失败");
						}
						
					   }
				 if(!password.equals("admin"))
					 {
						   JOptionPane.showMessageDialog(jf, ChooseLang.M[362]);
						   return;
					 }
				}
				else
				{
					JOptionPane.showMessageDialog(jf, ChooseLang.M[363]);
					return;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			
			}
			
		});
		
		jpopupmenu.add(jmenuitem);
		siteList.add(jpopupmenu);
		siteList.addMouseListener(new myJListListener());//右键侦听
		
		JPanel jpanel = new JPanel(new GridLayout(1,0,0,0));
		JPanel jpanel1 = new JPanel();
		JPanel jpanel2 = new JPanel();
		jpanel.setPreferredSize(new Dimension(20,35));
		jpanel.add(jpanel1);
		jpanel.add(jpanel2);
		jbutton = new JRoundButton(ChooseLang.M[105],JRoundButton.ashen);//打开项目
		JRoundButton jbutton1 = new JRoundButton(ChooseLang.M[106],JRoundButton.ashen);//添加项目
		jbutton.setFont(GisXYZ.fontG);
		jbutton1.setFont(GisXYZ.fontG);
		jbutton.setPreferredSize(new Dimension(135,25));//设置按钮的大小
		jbutton1.setPreferredSize(new Dimension(135,25));
		openlistener = new OpenListener();
		jbutton.addActionListener(openlistener);
		
		jbutton1.addActionListener(new ActionListener() {//添加项目

			@SuppressWarnings({ "static-access", "unchecked" })
			public void actionPerformed(ActionEvent actionevent)
			{
				
				//OpenOSK();
				String s = joptionpaneinput.showInputDialog(jf,ChooseLang.M[107]);
				//joptionpaneinput.addMouseListener(new OpenOSK());
				
				if (s == null)
				{
					return;
				} else
				{
					SiteChooser.db.openSite(s);
					SiteData asitedata1[] = SiteChooser.db.getAvailableSiteArray();
					SiteChooser.siteList.setListData(asitedata1);
					SiteChooser.siteList.setSelectedIndex(asitedata1.length - 1);					
					return;
				}
			}

		});
		
		
		jpanel1.add(uselang);
		jpanel1.add(languagechooser);	
		jpanel1.add(usechnum);
		jpanel1.add(chnumchooser);	
		jpanel2.add(jbutton1);
		//jpanel2.add(jbutton2);//删除项目
		jpanel2.add(jbutton);

		jf.setLayout(new BorderLayout());
		jf.add(jscrollpane, "West");
		jf.add(textPane, "Center");
		jf.add(jpanel, "South");
		jscrollpane.setPreferredSize(new Dimension(200, 300));
		jf.pack();
		if (flag)
			jf.setDefaultCloseOperation(3);
		else
			jf.setDefaultCloseOperation(1);
		jf.setLocationByPlatform(true);
		jf.setLocation((java.awt.Toolkit.getDefaultToolkit().getScreenSize().width - jf.getWidth()) / 2, (java.awt.Toolkit.getDefaultToolkit().getScreenSize().height - jf.getHeight()) / 2);
		if(!flag)
		{
			jf.setVisible(true);
			jfisVisible = false;
		}
		else
		{
			jf.setVisible(false);
			jfisVisible = true;
		}
		//while(jf.isVisible())
		while(jfisVisible||jf.isVisible())
		{
			if(flag)				
				jbutton.doClick();//这里跳过第一个界面，转到运行界面”站点的管理在运行界面执行“
			//if (jf.isVisible())
			if (!jf.isVisible())
				break;
			if (siteSelected)
			{
				//如果站点不为空
				if (siteList.getSelectedValue() != null && ((SiteData)siteList.getSelectedValue()).siteName != null)
				{
					siteSelected = false;
					break;
					
				}
				else
				{
					jbutton.removeActionListener(openlistener);
					jbutton.addActionListener(opendatabase);
					jbutton.doClick();
				}
				siteSelected = false;
			}
			try
			{
				Thread.sleep(30L);
			}
			catch (Exception exception1) { }
		} 
		jf.dispose();
		try
		{
			return ((SiteData)siteList.getSelectedValue()).siteName;
		}
		catch (Exception exception2){}
		return null;
	}
	
	public static int readFileByChars(File file) throws IOException{
		//File file = new File("lang\\flag.dat");
		int temp = 0;
		if(!file.exists())
		{
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else{
			Reader reader = new InputStreamReader(new FileInputStream(file));
			temp = reader.read();
			//while((temp = reader.read()) != -1){
				//System.out.println(temp);
			//}
			reader.close();
		}
		return temp;
	}
	
	public static void writeFileByChars(File file,int chars) throws IOException{
		//File file = new File(str);
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
				Writer write = new OutputStreamWriter(new FileOutputStream(file));
				write.write(chars);
				write.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
