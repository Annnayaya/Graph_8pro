package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.tools.JavaCompiler;

import com.Spectrogram.ZmGis8CH.gis_app_8CH.ping.CheckBoxListRenderer;

//为了配合1、2通道特高频，3、4通道超声波查看数据，这里重新构建的历史数据查看窗口类，源类在scr.rar文件下GisExplorer.java
class GisExplorer extends JPanel {
    private static final long serialVersionUID = 197307030001L;
    Database        db;
    SiteData[]      sd;
    JPanel          contentPanel;
    JPanel          topPanel;
    JPanel          bottomPanel;
    MemoPanel       memoPanel;
    GisCalendar     calendar;
    GisTrendGraph   trend;
    JPanel          innerPanel;
    JPanel          aeinnerPanel;//AE图谱
    JList<PRPDCell> prpdList;
    JList<PRPSCell> eventList;
    JList<PRPDCell> aeprpdList;
    JList<PRPSCell> aeeventList;
    JTree           dataTree;
    JFrame          localJFrame;
    PRPDGraph[]     prpdgrp     = new PRPDGraph[8];
    GisGraph[]      prpsgrp     = new GisGraph[8];
    PRPDGraph[]     aeprpdgrp   = new PRPDGraph[8];
    GisGraph[]      aeprpsgrp   = new GisGraph[8];
    SiteData        currentSite = null;
    GisDenoiser     denoiser;
    JCheckBox       noiseGateCheck;
    JCheckBox       phaseShiftCheck;
    public static boolean boolNoise = true;
    PrintPanel                 pp                = new PrintPanel();
    DefaultListModel<PRPDCell> pdcl              = new DefaultListModel<PRPDCell>();
    GregorianCalendar          tcal              = new GregorianCalendar();
    int[][]                    rawBuffer         = new int[8][5120];
    boolean[]                  denoise1          = {true, true, true, true, true, true, true, true};
    boolean[]                  denoise2          = {true, true, true, true, true, true, true, true};
    int[]                      phaseShift        = new int[8];
    long                       oldPsclSelTime    = 0L;
    int                        currSelCh         = 8;
    int                        trendSelTimeStart = 0;
    int                        trendSelTimeEnd   = 0;
    JCheckBox[]                jcheckbox         = new JCheckBox[8];
    Boolean[]                  boolean1          = new Boolean[8];
    JPanel[]                   jpanelcheckbox    = new JPanel[8];
    public static int ii = 0;

    JTabbedPane viewTab;//查看历史数据Tab
    JPanel      UHFPanel;
    JPanel      AEPanel;

    JPanel graphjpanel;
    JPanel conjpanel;//控制面板

    GisFlyBuffer gisflybuffer[];

    //全部选中按钮
    JButton selectAllButton;

    private static final int select_limit = 100;

    //导入数据
    class ImportData implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            JFileChooser jfilechooser = new JFileChooser();
            GisFileFilter gisfilefilter = new GisFileFilter();
            jfilechooser.addChoosableFileFilter(gisfilefilter);
            jfilechooser.setFileFilter(gisfilefilter);

            String s2 = SiteData.pathName;

            int n = jfilechooser.showOpenDialog(jfilechooser);
            if (n == 0) {
                String s1 = jfilechooser.getSelectedFile().getAbsolutePath();
                GisDataImport gisdataimport = new GisDataImport(s1, s2);
                gisdataimport.setLocationRelativeTo(GisExplorer.this.contentPanel);
                //刷新数据树
                RefreshExplorer refresh = new RefreshExplorer();
                refresh.start();
			/*
			GisZipFile ungiszipfile = new GisZipFile();
			try {
				ungiszipfile.unZip(s1,s2);
				db = new Database("data");
				DefaultTreeModel dt = new DefaultTreeModel(db);
				dt.reload();
				dataTree.setModel(dt);				
				//dataTree.updateUI();
			} catch (IOException e) {
				e.printStackTrace();
			}
			*/
            } else {
                return;
            }

        }

    }

    //无用类
    class ExportThread extends Thread {
        @Override
        public void run() {
            JProgressBar jpb = new JProgressBar(0, 1);
            jpb.setValue(0);
            jpb.setStringPainted(true);
            jpb.setBorderPainted(false);
            jpb.setSize(200, 60);
            JFrame jframe = new JFrame("Export");
            jframe.setDefaultCloseOperation(0);
            jframe.setLayout(new BorderLayout());
            jframe.setSize(300, 60);
            jframe.setAlwaysOnTop(true);
            jframe.add(jpb, "Center");
            jframe.setLocationRelativeTo(GisExplorer.this.contentPanel);
            jframe.setResizable(false);
            jframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            jframe.setVisible(true);
            jpb.setMinimum(1);
            jpb.setMaximum(100);
            for (int k = 1; k < 101; k++) {
                jpb.setValue(k);
                try {
                    Thread.sleep(250L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            jframe.dispose();
            jframe = null;
        }

    }

    //数据树刷新线程
    class RefreshExplorer extends Thread {
        public void run() {
            GisDataImport.setisComplete(false);
            do {
                //这里做一个延时，为了下面刷新列表取静态变量
                for (int i = 10; i > 0; i--)

                    if (GisDataImport.getisComplete()) {
                        db = new Database("data");
                        DefaultTreeModel dt = new DefaultTreeModel(db);
                        dt.reload();
                        dataTree.setModel(dt);
                    }
            }
            while (!GisDataImport.getisComplete());
            //GisDataImport.isComplete = false;
        }
    }

    //导出数据
    class ExportData implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser jfilechooser = new JFileChooser();
            GisFileFilter giszipFile = new GisFileFilter();//实例化文件过滤
            jfilechooser.addChoosableFileFilter(giszipFile);//增加过滤文件选择器可用
            jfilechooser.setFileFilter(giszipFile);//设置过滤文件类型
            jfilechooser.setSelectedFile(new File((new StringBuilder()).append("数据库.gisdata").toString()));//设置导出压缩文件的名称

            String s2 = SiteData.pathName;//获取数据库的路径
            int n = jfilechooser.showSaveDialog(jfilechooser);

            if (n == 0) {
                String s1 = jfilechooser.getSelectedFile().getAbsolutePath();//得到选择的路径
                GisDataExport gisdataexport = new GisDataExport(s2, s1);
                gisdataexport.setLocationRelativeTo(GisExplorer.this.contentPanel);
                //gisdataexport.setLocationRelativeTo(innerPanel);
                //gisdataexport.setVisible(true);
			/*
			GisZipFile giszipfile = new GisZipFile();
			ExportThread expthread = new ExportThread();
			expthread.start();
			try {
				giszipfile.Zip(s2, s1);
			} catch (IOException e) {
				e.printStackTrace();
			}
			*/
            } else {
                return;
            }
        }

    }

    public static void main(String[] paramArrayOfString)//������
    {
        new GisExplorer(new Database("data")).run();
    }

    public GisExplorer(Database paramDatabase) {
        super(new BorderLayout());

        this.db = paramDatabase;
        this.sd = this.db.getAvailableSiteArray();
        this.denoiser = new GisDenoiser(8);
        this.denoiser.findOffset = false;

        JFrame.setDefaultLookAndFeelDecorated(false);

        this.contentPanel = new JPanel(new BorderLayout());

        this.UHFPanel = new JPanel(new BorderLayout());//UHF面板
        this.AEPanel = new JPanel(new BorderLayout());//AE面板
        this.topPanel = new JPanel(new BorderLayout());//前面板
        this.bottomPanel = new JPanel();               //底部面板
        this.graphjpanel = new JPanel(new BorderLayout());//图像显示panel
        this.conjpanel = new JPanel(new BorderLayout());//图像显示panel
        this.calendar = new GisCalendar(this);//日历
        setPreferredSize(new Dimension(1020, 750));
        DataTreeInit();//数据树初始化
        this.trend = new GisTrendGraph(this);//趋势
        //this.topPanel.add(calendar, "West");
        this.topPanel.add(trend, "Center");
        this.topPanel.setBorder(new EtchedBorder(1));
        this.viewTab = new JTabbedPane();
        UHFInit();//UHF初始化
        AEInit();//AE初始化

        gisflybuffer = new GisFlyBuffer[8];
    
    /*
    this.contentPanel.add(localJScrollPane1, "West");//PRPD
    this.contentPanel.add(localJScrollPane2, "East");//PRPS
    this.innerPanel = new JPanel(new GridLayout(2, 4));
    this.contentPanel.add(this.innerPanel, "Center");
    */
        for (int i = 0; i < 8; i++) {
            //PRPD
            this.prpdgrp[i] = new PRPDGraph(64, 64, new int[64][64], null);
            PRPDGraph prpdgraph = this.prpdgrp[i];

            //PRPS
            this.prpsgrp[i] = new GisGraph(64, 80, new int[5120], new int[640][64], 5120);
            GisGraph gisgraph = this.prpsgrp[i];

            if (i == 0 || i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6 || i == 7)//mana-reportpage-draw1TO8CHprpsprpd
            {
                prpdgraph.stringToDraw = "";
                prpdgraph.stringToDraw = (prpdgraph.stringToDraw + "" + ChooseLang.M[i + 132] + " PRPD");
                this.prpdgrp[i].directRefresh = true;

                this.pp.add(this.prpdgrp[i]);

                gisgraph.stringToDraw = "";
                gisgraph.stringToDraw = (gisgraph.stringToDraw + "" + ChooseLang.M[i + 132] + " PRPS");
                this.prpsgrp[i].directRefresh = true;

                this.pp.add(this.prpsgrp[i]);
            }

            //AEPRPD
            this.aeprpdgrp[i] = new PRPDGraph(64, 64, new int[64][64], null);
            PRPDGraph aeprpdgraph = this.aeprpdgrp[i];

            //AEPRPS
            this.aeprpsgrp[i] = new GisGraph(64, 80, new int[5120], new int[640][64], 5120);
            GisGraph aegisgraph = this.aeprpsgrp[i];

            if (i == 5 || i == 6) {
                aeprpdgraph.unitfault = 2;//单位默认是mV
                aeprpdgraph.draw3D = false;
                aeprpdgraph.draw2D = false;
                aeprpdgraph.drawCon = false;
                aeprpdgraph.drawUltral = false;
                aeprpdgraph.Waveform = false;
                aeprpdgraph.drawColum = true;
                aeprpdgraph.stringToDraw = "";
                aeprpdgraph.stringToDraw = (aeprpdgraph.stringToDraw + "" + ChooseLang.M[i + 132]);
                this.aeprpdgrp[i].directRefresh = true;

                //this.pp.add(this.prpdgrp[i]);
                aegisgraph.draw3D = false;
                aegisgraph.isflygraphic = false;
                aegisgraph.isCon = true;
                aegisgraph.stringToDraw = "";
                aegisgraph.stringToDraw = (aegisgraph.stringToDraw + "" + ChooseLang.M[i + 132]);
                this.aeprpsgrp[i].directRefresh = true;

                //this.pp.add(this.prpsgrp[i]);
            }
        }
        viewChannel(0);//UHF通道显示
        viewAEChannel(3);//AE通道显示

        DrawUHFPRPD();//UHFPRPD图绘制
        DrawUHFPRPS();//UHFPRPS图绘制
        DrawAEPRPD();//AEPRPD图绘制
        DrawAEPRPS();//AEPRPS图绘制

        JPanel localJPanel1 = new JPanel(new BorderLayout());

        this.phaseShiftCheck = new JCheckBox(ChooseLang.M[230]);//相位
        this.noiseGateCheck = new JCheckBox(ChooseLang.M[23]);//噪声门限
        phaseShiftCheck.setFont(GisXYZ.fontG);
        noiseGateCheck.setFont(GisXYZ.fontG);

        this.phaseShiftCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                GisExplorer.this.psclSel(GisExplorer.this.oldPsclSelTime);
            }
        });
        this.noiseGateCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                GisExplorer.this.psclSel(GisExplorer.this.oldPsclSelTime);
                GisExplorer.boolNoise = GisExplorer.this.noiseGateCheck.isSelected();
                System.out.println(GisExplorer.boolNoise);
            }
        });
        this.noiseGateCheck.setSelected(true);
        this.phaseShiftCheck.setSelected(true);

        this.noiseGateCheck.setVisible(true);//设置默认不显示
        this.phaseShiftCheck.setVisible(false);//设置默认不显示
        //localJPanel5.add(this.noiseGateCheck);//不增加噪声门限按钮
        //localJPanel5.add(this.phaseShiftCheck);//不增加相位偏移按钮

        //add(this.contentPanel, "Center");
        this.graphjpanel.add(this.viewTab);
        this.graphjpanel.add(this.conjpanel, "South");
        add(this.graphjpanel, "Center");//图谱显示面板
        //add(this.viewTab,"Center");
        add(this.topPanel, "North");//趋势图
        add(localJPanel1, "South");//打印工具面板

        this.bottomPanel.setLayout(new FlowLayout(0));
        localJPanel1.add(this.bottomPanel, "West");

        DrawTrend();//绘制趋势图

        JPanel localJPanel8 = new JPanel(new GridLayout(2, 8, 2, 2));
        localJPanel8.setBorder(new TitledBorder(new EtchedBorder(1), ChooseLang.M[250]));//����ģ��
        JRoundButton localJButton = new JRoundButton(ChooseLang.M[251], JRoundButton.ashen);//打印PRPD
        localJButton.setFont(GisXYZ.fontG);
        //localJPanel8.add(localJButton);
        localJButton.addActionListener(new ActionListener()//打印PRPD侦听
        {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                PrinterJob localPrinterJob = PrinterJob.getPrinterJob();
                GisExplorer.this.pp.setPrint(0, GisExplorer.this.currSelCh);
                localPrinterJob.setPrintable(GisExplorer.this.pp);
                try {
                    if (localPrinterJob.printDialog()) {
                        localPrinterJob.print();
                    }
                } catch (Exception localException) {
                    localException.printStackTrace();
                }
            }
        });
        localJButton = new JRoundButton(ChooseLang.M[252], JRoundButton.ashen);//Print Event��ӡ���
        localJButton.setFont(GisXYZ.fontG);
        //localJPanel8.add(localJButton);
        localJButton.addActionListener(new ActionListener()//Print Event��ӡ�������
        {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                PrinterJob localPrinterJob = PrinterJob.getPrinterJob();
                GisExplorer.this.pp.setPrint(1, GisExplorer.this.currSelCh);
                localPrinterJob.setPrintable(GisExplorer.this.pp);
                try {
                    if (localPrinterJob.printDialog()) {
                        localPrinterJob.print();
                    }
                } catch (Exception localException) {
                    localException.printStackTrace();
                }
            }
        });
        localJButton = new JRoundButton(ChooseLang.M[253], JRoundButton.ashen);//Save PRPD����PRPD
        localJButton.setFont(GisXYZ.fontG);
        localJPanel8.add(localJButton);
        localJButton.addActionListener(new ActionListener()//Save PRPD����PRPD����
        {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                GisExplorer.ExampleFileFilter exampleFileFilter = new GisExplorer.ExampleFileFilter();
                exampleFileFilter.addExtension("csv");
                exampleFileFilter.setDescription("CSV Excel File");
                JFileChooser localJFileChooser = new JFileChooser();
                localJFileChooser.setFileFilter(exampleFileFilter);
                int i = localJFileChooser.showSaveDialog(GisExplorer.this.contentPanel);
                if (i == 0) {
                    GisExplorer.this.pp.setPrint(0, GisExplorer.this.currSelCh);
                    File localFile = localJFileChooser.getSelectedFile();
                    if ((!localFile.getName().endsWith(".csv")) && (!localFile.getName().endsWith(".CSV"))) {
                        localFile = new File(localFile.getParent(), localFile.getName() + ".csv");
                    }
                    GisExplorer.this.pp.exportCSV(localFile);
                }
            }
        });
        localJButton = new JRoundButton(ChooseLang.M[254], JRoundButton.ashen);//Save Event�����¼�
        localJButton.setFont(GisXYZ.fontG);
        localJPanel8.add(localJButton);
        localJButton.addActionListener(new ActionListener()//Save Event�����¼�����
        {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {

                GisExplorer.ExampleFileFilter exampleFileFilter = new GisExplorer.ExampleFileFilter();
                exampleFileFilter.addExtension("csv");
                exampleFileFilter.setDescription("CSV Excel File");
                JFileChooser localJFileChooser = new JFileChooser();
                localJFileChooser.setFileFilter(exampleFileFilter);

                int i = localJFileChooser.showSaveDialog(GisExplorer.this.contentPanel);
                if (i == 0) {
                    GisExplorer.this.pp.setPrint(1, GisExplorer.this.currSelCh);
                    File localFile = localJFileChooser.getSelectedFile();
                    if ((!localFile.getName().endsWith(".csv")) && (!localFile.getName().endsWith(".CSV"))) {
                        localFile = new File(localFile.getParent(), localFile.getName() + ".csv");
                    }
                    GisExplorer.this.pp.exportCSV(localFile);
                }
            }
        });
        localJButton = new JRoundButton(ChooseLang.M[255], JRoundButton.ashen);//捕捉PRPD
        localJButton.setFont(GisXYZ.fontG);
        localJPanel8.add(localJButton);
        localJButton.addActionListener(new ActionListener()//��׽PRPD����
        {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                GisExplorer.ExampleFileFilter exampleFileFilter = new GisExplorer.ExampleFileFilter();
                exampleFileFilter.addExtension("png");
                exampleFileFilter.setDescription("PNG Images");
                JFileChooser localJFileChooser = new JFileChooser();
                localJFileChooser.setFileFilter(exampleFileFilter);
                int i = localJFileChooser.showSaveDialog(contentPanel);
                if (i == 0) {
                    GisExplorer.this.pp.setPrint(0, GisExplorer.this.currSelCh);
                    File localFile = localJFileChooser.getSelectedFile();
                    if ((!localFile.getName().endsWith(".png")) && (!localFile.getName().endsWith(".PNG"))) {
                        localFile = new File(localFile.getParent(), localFile.getName() + ".png");
                    }
                    GisExplorer.this.pp.savePng(localFile);
                }
            }
        });
        localJButton = new JRoundButton(ChooseLang.M[256], JRoundButton.ashen);//Capture Event捕捉事件
        localJButton.setFont(GisXYZ.fontG);
        localJPanel8.add(localJButton);
        localJButton.addActionListener(new ActionListener()//Capture Event捕捉事件侦听
        {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                GisExplorer.ExampleFileFilter exampleFileFilter = new GisExplorer.ExampleFileFilter();
                exampleFileFilter.addExtension("png");
                exampleFileFilter.setDescription("PNG Images");
                JFileChooser localJFileChooser = new JFileChooser();
                localJFileChooser.setFileFilter(exampleFileFilter);
                int i = localJFileChooser.showSaveDialog(GisExplorer.this.contentPanel);
                if (i == 0) {
                    GisExplorer.this.pp.setPrint(1, GisExplorer.this.currSelCh);
                    File localFile = localJFileChooser.getSelectedFile();
                    if ((!localFile.getName().endsWith(".png")) && (!localFile.getName().endsWith(".PNG"))) {
                        localFile = new File(localFile.getParent(), localFile.getName() + ".png");
                    }
                    GisExplorer.this.pp.savePng(localFile);
                }
            }
        });
        //注释掉打印一天报告
//    localJButton = new JRoundButton(ChooseLang.M[257],JRoundButton.ashen);//打印一天报告
//    localJButton.setVisible(true);
//    localJButton.setFont(GisXYZ.fontG);
//    localJPanel8.add(localJButton);
//    localJButton.addActionListener(new ActionListener()//61 打印一天报告侦听
//    {
//      public void actionPerformed(ActionEvent paramAnonymousActionEvent)
//      {
//        long l1 = GisExplorer.this.currentSite.loadedDate;      //1天    日期
//
//        GregorianCalendar localGregorianCalendar = new GregorianCalendar();   //公历
//        localGregorianCalendar.setTimeInMillis(l1);
//        localGregorianCalendar.set(11, 0);
//        localGregorianCalendar.set(12, 0);
//        localGregorianCalendar.set(13, 0);
//        localGregorianCalendar.set(14, 0);
//
//
//          l1 = localGregorianCalendar.getTimeInMillis();
//
//          long l2 = l1 + 86400000L - 1L;
//        //打印png图片报告
//        /*
//        ReportCheck localReportCheck = new ReportCheck(0, l1, l2, GisExplorer.this.currentSite, true);
//        localReportCheck.setLocationRelativeTo(GisExplorer.this.contentPanel);
//        localReportCheck.setVisible(true);
//        */
//        //打印word文档报告
//        GisWordReport localwordreport = new GisWordReport(0, l1, l2, GisExplorer.this.currentSite);
//        localwordreport.setLocationRelativeTo(GisExplorer.this.contentPanel);
//        localwordreport.setVisible(true);
//
//      }
//    });
        localJButton = new JRoundButton(ChooseLang.M[258], JRoundButton.ashen);//打印部分报告
        localJButton.setVisible(true);
        localJButton.setFont(GisXYZ.fontG);
        localJPanel8.add(localJButton);
        localJButton.addActionListener(new ActionListener()//打印部分报告侦听
        {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
//        if (GisExplorer.this.trendSelTimeEnd - GisExplorer.this.trendSelTimeStart < 4) {
//          return;
//        }

//         final List<PRPSCell> selectedValuesList = GisExplorer.this.eventList.getSelectedValuesList();
//         if(selectedValuesList == null || selectedValuesList.isEmpty()) {
//            JOptionPane.showMessageDialog(graphjpanel, "请选择需要打印的时间戳！");
//            return;
//        }

                final ListModel<PRPSCell> model = GisExplorer.this.eventList.getModel();
                PRPSCell prpscell = null;
                List<Long> selectedList = new ArrayList<>();
                for (int i = 0; i < model.getSize(); i++) {
                    prpscell = GisExplorer.this.eventList.getModel().getElementAt(i);
                    if (prpscell.isSelected()) {
                        selectedList.add(prpscell.time);
                    }
                }
                if (selectedList.isEmpty()) {
                    JOptionPane.showMessageDialog(graphjpanel, "请选择需要打印的时间戳！");
                    return;
                }


                long l1 = GisExplorer.this.currentSite.loadedDate;

                int i = GisExplorer.this.trendSelTimeStart / 60;
                int j = GisExplorer.this.trendSelTimeStart % 60;

                GregorianCalendar localGregorianCalendar = new GregorianCalendar();
                localGregorianCalendar.setTimeInMillis(l1);
                localGregorianCalendar.set(11, i);
                localGregorianCalendar.set(12, j);
                localGregorianCalendar.set(13, 0);
                localGregorianCalendar.set(14, 0);
                l1 = localGregorianCalendar.getTimeInMillis();

                i = GisExplorer.this.trendSelTimeEnd / 60;
                j = GisExplorer.this.trendSelTimeEnd % 60;

                localGregorianCalendar.set(11, i);
                localGregorianCalendar.set(12, j);
                localGregorianCalendar.set(13, 0);
                localGregorianCalendar.set(14, 0);
                long l2 = localGregorianCalendar.getTimeInMillis();
                //打印png图片报告
        /*
        ReportCheck localReportCheck = new ReportCheck(1, l1, l2, GisExplorer.this.currentSite, true);
        localReportCheck.setLocationRelativeTo(GisExplorer.this.contentPanel);
        localReportCheck.setVisible(true);
        */
                //打印word文档报告
                final long startTime = selectedList.get(0);
                final long endTime = selectedList.get(selectedList.size() - 1);

                GisWordReport localwordreport = new GisWordReport(1, startTime, endTime, GisExplorer.this.currentSite);
                localwordreport.setLocationRelativeTo(GisExplorer.this.contentPanel);
                localwordreport.setVisible(true);

            }
        });

        //导出数据压缩文件
        localJButton = new JRoundButton(ChooseLang.M[343], JRoundButton.ashen);
        //localJButton.setFont(GisXYZ.fontG);
        localJPanel8.add(localJButton);
        localJButton.addActionListener(new ExportData());

        //导入数据解压缩文件
        localJButton = new JRoundButton(ChooseLang.M[344], JRoundButton.ashen);
        //localJButton.setFont(GisXYZ.fontG);
        localJPanel8.add(localJButton);
        localJButton.addActionListener(new ImportData());
        localJPanel1.add(localJPanel8, "East");                     //打印工具

        this.memoPanel = new MemoPanel(this);
        localJPanel1.add(this.memoPanel, "South");

        this.topPanel.add(calendar, "East");
        //view4Channel();
    }

    public void pdclSel(long paramLong)//ѡ��PRPD��ʷͼ����ʾ
    {
        this.tcal.setTimeInMillis(paramLong);
        int i = this.tcal.get(11) * 60 + this.tcal.get(12);


        this.tcal.setTimeInMillis(paramLong + 60000L);

        int j = this.tcal.get(11) * 60 + this.tcal.get(12);
        if (j < i) {
            j = 1440;
        }
        applySelection(j, j);
    }

    //UHF
    public void psclSel(long paramLong)//PRPS
    {
        oldPsclSelTime = paramLong;
        int[] arrayOfInt1 = new int[8];
        int[][] arrayOfInt = new int[8][];
        int[][] array_colum = new int[8][];//柱形图数据
        int[][] array_columprpd = new int[8][];//
        int[][] array_utral = new int[8][];//超声波数据
        int[] arrayOfInt2 = new int[280];

        arrayOfInt[0] = this.prpsgrp[0].pdData;
        arrayOfInt[1] = this.prpsgrp[1].pdData;
        arrayOfInt[2] = this.prpsgrp[2].pdData;
        arrayOfInt[3] = this.prpsgrp[3].pdData;
        arrayOfInt[4] = this.prpsgrp[4].pdData;
        arrayOfInt[5] = this.prpsgrp[5].pdData;
        arrayOfInt[6] = this.prpsgrp[6].pdData;
        arrayOfInt[7] = this.prpsgrp[7].pdData;

        array_colum[0] = this.prpsgrp[0].columdata;
        array_colum[1] = this.prpsgrp[1].columdata;
        array_colum[2] = this.prpsgrp[2].columdata;
        array_colum[3] = this.prpsgrp[3].columdata;
        array_colum[4] = this.prpsgrp[4].columdata;
        array_colum[5] = this.prpsgrp[5].columdata;
        array_colum[6] = this.prpsgrp[6].columdata;
        array_colum[7] = this.prpsgrp[7].columdata;

        array_columprpd[0] = this.prpdgrp[0].columdata;
        array_columprpd[1] = this.prpdgrp[1].columdata;
        array_columprpd[2] = this.prpdgrp[2].columdata;
        array_columprpd[3] = this.prpdgrp[3].columdata;
        array_columprpd[4] = this.prpdgrp[4].columdata;
        array_columprpd[5] = this.prpdgrp[5].columdata;
        array_columprpd[6] = this.prpdgrp[6].columdata;
        array_columprpd[7] = this.prpdgrp[7].columdata;

        array_utral[0] = this.prpdgrp[0].ultralpdata;
        array_utral[1] = this.prpdgrp[1].ultralpdata;
        array_utral[2] = this.prpdgrp[2].ultralpdata;
        array_utral[3] = this.prpdgrp[3].ultralpdata;
        array_utral[4] = this.prpdgrp[4].ultralpdata;
        array_utral[5] = this.prpdgrp[5].ultralpdata;
        array_utral[6] = this.prpdgrp[6].ultralpdata;
        array_utral[7] = this.prpdgrp[7].ultralpdata;

        this.currentSite.getEvent(paramLong, this.rawBuffer[0], this.rawBuffer[1], this.rawBuffer[2], this.rawBuffer[3], this.rawBuffer[4], this.rawBuffer[5], this.rawBuffer[6], this.rawBuffer[7], arrayOfInt2);//mana-读取历史谱图文件
        for (int i = 0; i < 8; i++) {
            this.denoiser.preDefinedOffset[i] = arrayOfInt2[(i + 8)];
            if (!this.phaseShiftCheck.isSelected()) {
                arrayOfInt2[i] = 0;
            }//
            this.denoise2[i] = this.noiseGateCheck.isSelected();
            this.prpsgrp[i].phaseDelay = arrayOfInt2[i];

            this.prpsgrp[i].mindBm = ((int) (GisSignal.Int2dBm(arrayOfInt2[(i + 8)]) - 0.5D));
            this.prpsgrp[i].maxdBm = GisSignal.calMax_dBm;
        }
        this.denoiser.gdProc(this.rawBuffer, arrayOfInt, null, this.denoise1, this.denoise2, 3, arrayOfInt2, 79, true);

        //拆分5120个数据成80组比较，为生成柱状图准备
        int[][][] an = new int[8][80][64];
        for (int i = 0; i < 8; i++) {
            for (int cycle = 0; cycle < 80; cycle++) {
                for (int j = 0; j < 64; j++) {
                    an[i][cycle][j] = arrayOfInt[i][cycle * 64 + j];

                }
            }
            for (int k = 0; k < 64; k++) {
                int k3 = 0;
                for (int m = 0; m < 80; m++) {

                    int k4 = an[i][m][k];
                    if (k3 < k4) {
                        k3 = k4;
                    }
                    array_colum[i][k] = array_columprpd[i][k] = k3;
                }
            }

            //计算最大值
            int i4 = 0;//最大值
            for (int j = 0; j < 5120; j++) {
                int k3 = this.prpsgrp[i].pdData[j];
                if (i4 < k3)
                    i4 = k3;
                this.prpsgrp[i].amxdBm = GisExplorer.this.prpdgrp[i].amxdBm = (double) (i4 * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double) GisSignal.calMin_dBm;
                //this.prpsgrp[i].pdData[j] = 0;
                k3 = 0;
            }

        }


        //System.arraycopy(arrayOfInt[0], 5056, array_colum[0], 0, 64);//复制最新一组数据到柱形显示图
        //System.arraycopy(arrayOfInt[1], 5056, array_colum[1], 0, 64);//复制最新一组数据到柱形显示图
        //System.arraycopy(arrayOfInt[2], 5056, array_colum[2], 0, 64);//复制最新一组数据到柱形显示图
        //System.arraycopy(arrayOfInt[3], 5056, array_colum[3], 0, 64);//复制最新一组数据到柱形显示图

        System.arraycopy(arrayOfInt[0], 4800, array_utral[0], 0, 320);//复制超声波数据
        System.arraycopy(arrayOfInt[1], 4800, array_utral[1], 0, 320);//复制超声波数据
        System.arraycopy(arrayOfInt[2], 4800, array_utral[2], 0, 320);//复制超声波数据
        System.arraycopy(arrayOfInt[3], 4800, array_utral[3], 0, 320);//复制超声波数据
        System.arraycopy(arrayOfInt[4], 4800, array_utral[4], 0, 320);//复制超声波数据
        System.arraycopy(arrayOfInt[5], 4800, array_utral[5], 0, 320);//复制超声波数据
        System.arraycopy(arrayOfInt[6], 4800, array_utral[6], 0, 320);//复制超声波数据
        System.arraycopy(arrayOfInt[7], 4800, array_utral[7], 0, 320);//复制超声波数据

        //System.arraycopy(arrayOfInt[0], 5056, array_columprpd[0], 0, 64);//复制最新一组数据到柱形显示图
        //System.arraycopy(arrayOfInt[1], 5056, array_columprpd[1], 0, 64);//复制最新一组数据到柱形显示图
        //System.arraycopy(arrayOfInt[2], 5056, array_columprpd[2], 0, 64);//复制最新一组数据到柱形显示图
        //System.arraycopy(arrayOfInt[3], 5056, array_columprpd[3], 0, 64);//复制最新一组数据到柱形显示图

        for (int i = 0; i < 8; i++) {
            this.prpsgrp[i].mindBm = ((int) (this.denoiser.offset_dBm[i] - 0.5D));

            int k = 1;
            this.prpsgrp[i].resultString[0] = "";
            this.prpsgrp[i].resultString[1] = "";
            this.prpsgrp[i].resultString[2] = "";
            this.prpsgrp[i].resultString[3] = "";
            this.prpsgrp[i].resultString[4] = "";
            this.prpsgrp[i].resultString[5] = "";
            this.prpsgrp[i].resultString[6] = "";
            this.prpsgrp[i].resultString[7] = "";
            arrayOfInt1[0] = 0;
            arrayOfInt1[1] = 0;
            arrayOfInt1[2] = 0;
            arrayOfInt1[3] = 0;
            arrayOfInt1[4] = 0;
            arrayOfInt1[5] = 0;
            arrayOfInt1[6] = 0;
            arrayOfInt1[7] = 0;
            for (int j = 0; j < 8; j++) {
                if ((arrayOfInt2[(i * 32 + 8 + j)] > 10) && (k < 8)) {
                    arrayOfInt1[k] = arrayOfInt2[(i * 32 + 8 + j)];

                    if (i == 8) {
                        if (this.prpsgrp[i].resultString[j] == ChooseLang.M[10] || PDResult.resultStr[j] == ChooseLang.M[11] || PDResult.resultStr[j] == ChooseLang.M[12])
                            this.prpsgrp[i].resultString[1] = ChooseLang.M[16];
                        else
                            this.prpsgrp[i].resultString[k] = PDResult.resultStr[j];
                        String[] resultstringCT = this.prpsgrp[i].resultString;
                        resultstringCT[k] = (resultstringCT[k] + " " + arrayOfInt2[(i * 32 + 8 + j)] + "%");

                    } else {
                        this.prpsgrp[i].resultString[k] = PDResult.resultStr[j];
                        String[] resultstring = this.prpsgrp[i].resultString;
                        resultstring[k] = (resultstring[k] + " " + arrayOfInt2[(i * 32 + 8 + j)] + "%");
                    }

                    //this.prpsgrp[i].resultString[k] = PDResult.resultStr[j];
                    //String[] prpsgrp1 = this.prpsgrp[i].resultString;
                    //prpsgrp1[k] = (prpsgrp1[k] + " " + arrayOfInt2[(i * 32 + 8 + j)] + "%");//��ѯprps�����ʾ�ŵ�����
                    k++;
                }
            }
            for (int j = 1; j < 8; j++) {
                for (k = j; k < 8; k++) {
                    if (arrayOfInt1[k] > arrayOfInt1[j]) {
                        int m = arrayOfInt1[k];
                        arrayOfInt1[k] = arrayOfInt1[j];
                        arrayOfInt1[j] = m;
                        String str = this.prpsgrp[i].resultString[j];
                        this.prpsgrp[i].resultString[j] = prpsgrp[i].resultString[k];
                        this.prpsgrp[i].resultString[k] = str;
                    }
                }
            }
            if ((this.noiseGateCheck.isSelected()) && (i == 8))//如果第4通道是噪音i==3
            {
                this.prpsgrp[i].resultString[1] = ChooseLang.M[259];
                this.prpsgrp[i].resultString[2] = "";
                this.prpsgrp[i].resultString[3] = "";
            }
        }
        for (int i = 0; i < 8; i++) {
            this.prpsgrp[i].repaint();
        }
    }

    //UHF
    public void psclSel(int paramInt)//PRPS
    {
        this.tcal.setTimeInMillis(this.currentSite.loadedDate);
        this.tcal.set(11, paramInt / 60);//ʱ
        this.tcal.set(12, paramInt % 60);//��
        this.tcal.set(13, 0);//��

        long l1 = this.tcal.getTimeInMillis();

        PRPSCell prpscell = (PRPSCell) this.eventList.getSelectedValue();
        if ((prpscell != null) &&
                (prpscell.time - l1 < 60000L) && (prpscell.time - l1 > 0L)) {
            return;
        }

        int j = this.currentSite.evList.size();//�б��е������PRPS�б?
        for (int i = 0; i < j; i++) {
            long l2 = ((PRPSCell) this.currentSite.evList.get(i)).time;
            if ((l2 - l1 < 60000L) && (l2 - l1 > 0L))//С��60000/1000/60=1�֣��Ҵ���1����
            {
                psclSel(((PRPSCell) this.currentSite.evList.get(i)).time);//����
                if (this.eventList.getSelectedIndex() != i) {
                    //不做处理，之前是左边选，所以才需要做处理
                    this.eventList.setSelectedIndex(i);//ѡ�񵥸���Ԫ
                    this.eventList.ensureIndexIsVisible(i);//��������ӿ��е��б?ʹָ����Ԫ��ȫ�ɼ�
                }
                return;
            }
            if (l1 + 60000L < l2) {
                i = j;
                break;
            }
        }
        this.eventList.setSelectedIndices(new int[0]);//��ѡ����Ϊ��������ָ��������ļ���???
    }

    //AE
    public void aepsclSel(long paramLong)//PRPS
    {
        oldPsclSelTime = paramLong;
        int[] arrayOfInt1 = new int[8];
        int[][] arrayOfInt = new int[8][];
        int[][] array_colum = new int[8][];//柱形图数据
        int[][] array_columprpd = new int[8][];//
        int[][] array_utral = new int[8][];//超声波数据
        int[] arrayOfInt2 = new int[280];

        int[][] array_flyprps = new int[8][];//飞行图

        for (int a = 0; a < 8; a++) {
            arrayOfInt[a] = this.aeprpsgrp[a].pdData;
            array_colum[a] = this.aeprpsgrp[a].columdata;
            array_columprpd[a] = this.aeprpdgrp[a].columdata;
            array_utral[a] = this.aeprpdgrp[a].ultralpdata;

            //arraybuffer_utral[a] = this.aeprpsgrp[a]
            //array_flyprps[a] = this.prpsgrp[a].flygraphicdata;
        }

        this.currentSite.getEvent(paramLong, this.rawBuffer[0], this.rawBuffer[1], this.rawBuffer[2], this.rawBuffer[3], this.rawBuffer[4], this.rawBuffer[5], this.rawBuffer[6], this.rawBuffer[7], arrayOfInt2);
        for (int i = 0; i < 8; i++) {
            this.denoiser.preDefinedOffset[i] = arrayOfInt2[(i + 8)];
            if (!this.phaseShiftCheck.isSelected()) {
                arrayOfInt2[i] = 0;
            }//
            this.denoise2[i] = this.noiseGateCheck.isSelected();
            this.aeprpsgrp[i].phaseDelay = arrayOfInt2[i];

            this.aeprpsgrp[i].mindBm = ((int) (GisSignal.Int2dBm(arrayOfInt2[(i + 8)]) - 0.5D));
            this.aeprpsgrp[i].maxdBm = GisSignal.calMax_dBm;
        }
        this.denoiser.gdProc(this.rawBuffer, arrayOfInt, null, this.denoise1, this.denoise2, 3, arrayOfInt2, 79, true);


        //拆分5120个数据成80组比较，为生成柱状图准备
        int[][][] an = new int[8][80][64];
        int[][] arraybuffer_utral = new int[8][640];//飞行图数据缓存
        int[][] buffer_640 = new int[8][640];//飞行图640数据
        for (int i = 0; i < 8; i++) {

            for (int cycle = 0; cycle < 80; cycle++) {
                for (int j = 0; j < 64; j++) {
                    an[i][cycle][j] = arrayOfInt[i][cycle * 64 + j];
                    //System.out.println("通道" + i + "阵列数据：" + arrayOfInt[i][cycle*64 + j]);
                }
            }
            //计算柱形图
            for (int k = 0; k < 64; k++) {
                int k3 = 0;
                for (int m = 0; m < 80; m++) {

                    int k4 = an[i][m][k];
                    if (k3 < k4) {
                        k3 = k4;
                    }
                    array_colum[i][k] = array_columprpd[i][k] = k3;
                }
            }

            //计算最大值
            int i4 = 0;//最大值
            for (int j = 0; j < 5120; j++) {
                int k3 = this.aeprpsgrp[i].pdData[j];
                if (i4 < k3)
                    i4 = k3;
                this.aeprpsgrp[i].amxdBm = GisExplorer.this.aeprpdgrp[i].amxdBm = (double) (i4 * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double) GisSignal.calMin_dBm;
                //this.aeprpsgrp[i].pdData[j] = 0;
                k3 = 0;
            }

            //计算有效值
            int temp[];
            temp = new int[10];
            int eff = 0;
            int f1 = 0;
            int f2 = 0;
            int i6 = 0;
            for (int m = 0; m < 80; m++) {
                for (int l = 1; l < 7; l++) {
                    GisSignal.dftn(an[i][m], temp, 64, l);
                    eff += temp[l] * temp[l];
                }
                int i5 = (int) (Math.sqrt((double) (eff)));
                if (i6 < i5) {
                    i6 = i5;
                }
                eff = i6;
                int i7 = temp[1];
                int i8 = temp[2];
                if (f1 < i7 || f2 < i8) {
                    f1 = i7;
                    f2 = i8;
                }
            }

            this.aeprpsgrp[i].effdBm = (double) (eff * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double) GisSignal.calMin_dBm;
            //计算频率成分1
            this.aeprpsgrp[i].f1dBm = (double) (f1 * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double) GisSignal.calMin_dBm;
            //计算频率成分2
            this.aeprpsgrp[i].f2dBm = (double) (f2 * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double) GisSignal.calMin_dBm;
            //计算飞行图数据
            for (int fly = 0; fly < 640; fly++) {
                arraybuffer_utral[i][fly] = arrayOfInt[i][4480 + fly];
            }

            int k2 = 0;
            int k1 = 0;
            int n = 576;

            for (; k1 < 640; k1++) {
                buffer_640[i][k1] = arraybuffer_utral[i][n];
                n++;
                if (n > 640 - 64 * k2 - 1) {
                    k2++;
                    if (k2 > 9)
                        break;
                    n = 576 - 64 * k2;
                }

            }

            gisflybuffer[i] = new GisFlyBuffer(buffer_640[i]);
            gisflybuffer[i].fbpro();
            //array_flyprps[i] = gisflybuffer[i].accflydata;
            this.aeprpsgrp[i].flygraphicdata = gisflybuffer[i].accflydata;
        }

        System.arraycopy(arrayOfInt[0], 4800, array_utral[0], 0, 320);//复制超声波数据
        System.arraycopy(arrayOfInt[1], 4800, array_utral[1], 0, 320);//复制超声波数据
        System.arraycopy(arrayOfInt[2], 4800, array_utral[2], 0, 320);//复制超声波数据
        System.arraycopy(arrayOfInt[3], 4800, array_utral[3], 0, 320);//复制超声波数据
        System.arraycopy(arrayOfInt[4], 4800, array_utral[4], 0, 320);//复制超声波数据
        System.arraycopy(arrayOfInt[5], 4800, array_utral[5], 0, 320);//复制超声波数据
        System.arraycopy(arrayOfInt[6], 4800, array_utral[6], 0, 320);//复制超声波数据
        System.arraycopy(arrayOfInt[7], 4800, array_utral[7], 0, 320);//复制超声波数据

        for (int i = 0; i < 8; i++) {
            this.aeprpsgrp[i].mindBm = ((int) (this.denoiser.offset_dBm[i] - 0.5D));

            int k = 1;
            this.aeprpdgrp[i].resultStringprpd[0] = "";
            this.aeprpdgrp[i].resultStringprpd[1] = "";
            this.aeprpdgrp[i].resultStringprpd[2] = "";
            this.aeprpdgrp[i].resultStringprpd[3] = "";
            this.aeprpdgrp[i].resultStringprpd[4] = "";
            this.aeprpdgrp[i].resultStringprpd[5] = "";
            this.aeprpdgrp[i].resultStringprpd[6] = "";
            this.aeprpdgrp[i].resultStringprpd[7] = "";
            arrayOfInt1[0] = 0;
            arrayOfInt1[1] = 0;
            arrayOfInt1[2] = 0;
            arrayOfInt1[3] = 0;
            arrayOfInt1[4] = 0;
            arrayOfInt1[5] = 0;
            arrayOfInt1[6] = 0;
            arrayOfInt1[7] = 0;
            for (int j = 0; j < 8; j++) {
                if ((arrayOfInt2[(i * 32 + 8 + j)] > 10) && (k < 8)) {
                    arrayOfInt1[k] = arrayOfInt2[(i * 32 + 8 + j)];

                    if (i == 8) {
                        if (this.aeprpdgrp[i].resultStringprpd[j] == ChooseLang.M[10] || PDResult.resultStr[j] == ChooseLang.M[11] || PDResult.resultStr[j] == ChooseLang.M[12])
                            this.aeprpdgrp[i].resultStringprpd[1] = ChooseLang.M[16];
                        else
                            this.aeprpdgrp[i].resultStringprpd[k] = PDResult.resultStr[j];
                        String[] resultstringCT = this.aeprpdgrp[i].resultStringprpd;
                        resultstringCT[k] = (resultstringCT[k] + " " + arrayOfInt2[(i * 32 + 8 + j)] + "%");

                    } else {
                        this.aeprpdgrp[i].resultStringprpd[k] = PDResult.resultStr[j];
                        String[] resultstring = this.aeprpdgrp[i].resultStringprpd;
                        resultstring[k] = (resultstring[k] + " " + arrayOfInt2[(i * 32 + 8 + j)] + "%");
                    }

                    //this.prpsgrp[i].resultString[k] = PDResult.resultStr[j];
                    //String[] prpsgrp1 = this.prpsgrp[i].resultString;
                    //prpsgrp1[k] = (prpsgrp1[k] + " " + arrayOfInt2[(i * 32 + 8 + j)] + "%");//��ѯprps�����ʾ�ŵ�����
                    k++;
                }
            }
            for (int j = 1; j < 8; j++) {
                for (k = j; k < 8; k++) {
                    if (arrayOfInt1[k] > arrayOfInt1[j]) {
                        int m = arrayOfInt1[k];
                        arrayOfInt1[k] = arrayOfInt1[j];
                        arrayOfInt1[j] = m;
                        String str = this.aeprpdgrp[i].resultStringprpd[j];
                        this.aeprpdgrp[i].resultStringprpd[j] = aeprpdgrp[i].resultStringprpd[k];
                        this.aeprpdgrp[i].resultStringprpd[k] = str;
                    }
                }
            }
            if ((this.noiseGateCheck.isSelected()) && (i == 8))//如果第4通道是噪音i==3
            {
                this.aeprpdgrp[i].resultStringprpd[1] = ChooseLang.M[259];
                this.aeprpdgrp[i].resultStringprpd[2] = "";
                this.aeprpdgrp[i].resultStringprpd[3] = "";
            }
        }
        for (int i = 0; i < 8; i++) {
            this.aeprpdgrp[i].repaint();
        }
    }

    //AE
    public void aepsclSel(int paramInt)//PRPS
    {
        this.tcal.setTimeInMillis(this.currentSite.loadedDate);
        this.tcal.set(11, paramInt / 60);//ʱ
        this.tcal.set(12, paramInt % 60);//��
        this.tcal.set(13, 0);//��

        long l1 = this.tcal.getTimeInMillis();

        PRPSCell prpscell = (PRPSCell) this.aeeventList.getSelectedValue();
        if ((prpscell != null) &&
                (prpscell.time - l1 < 60000L) && (prpscell.time - l1 > 0L)) {
            return;
        }

        int j = this.currentSite.evList.size();//�б��е������PRPS�б?
        for (int i = 0; i < j; i++) {
            long l2 = ((PRPSCell) this.currentSite.evList.get(i)).time;
            if ((l2 - l1 < 60000L) && (l2 - l1 > 0L))//С��60000/1000/60=1�֣��Ҵ���1����
            {
                aepsclSel(((PRPSCell) this.currentSite.evList.get(i)).time);//����
                if (this.aeeventList.getSelectedIndex() != i) {
                    this.aeeventList.setSelectedIndex(i);//ѡ�񵥸���Ԫ
                    this.aeeventList.ensureIndexIsVisible(i);//��������ӿ��е��б?ʹָ����Ԫ��ȫ�ɼ�
                }
                return;
            }
            if (l1 + 60000L < l2) {
                i = j;
                break;
            }
        }
        this.aeeventList.setSelectedIndices(new int[0]);//��ѡ����Ϊ��������ָ��������ļ���???
    }


    class GisButtonListener implements ActionListener//查看通道侦听
    {
        GisButtonListener() {
        }

        @Override
        public void actionPerformed(ActionEvent paramActionEvent) {
            String str = paramActionEvent.getActionCommand();
            if (str.equals(ChooseLang.M[232])) {//通道1
                viewChannel(0);
            } else if (str.equals(ChooseLang.M[233])) {//通道2
                viewChannel(1);
            } else if (str.equals(ChooseLang.M[234])) {//通道3
                viewChannel(2);
            } else if (str.equals(ChooseLang.M[235])) {//通道4
                viewChannel(3);
            } else if (str.equals(ChooseLang.M[236])) {//所有通道
                viewChannel(8);
            } else if (str.equals(ChooseLang.M[286])) {//所有通道
                viewChannel(4);
            } else if (str.equals(ChooseLang.M[287])) {//所有通道
                viewChannel(5);
            } else if (str.equals(ChooseLang.M[288])) {//所有通道
                viewChannel(6);
            } else if (str.equals(ChooseLang.M[289])) {//所有通道
                viewChannel(7);
            }
        }
    }


    public void viewChannel(int paramInt) {
        this.currSelCh = paramInt;
        if (paramInt < 8) {
            this.innerPanel.removeAll();
            this.innerPanel.setLayout(new GridLayout(0, 2, 1, 1));
            this.innerPanel.add(this.prpdgrp[paramInt]);
            this.innerPanel.add(this.prpsgrp[paramInt]);
        } else {
            this.innerPanel.removeAll();
            this.innerPanel.setLayout(new GridLayout(2, 8, 1, 1));
            for (int i = 0; i < 8; i++) {
                this.innerPanel.add(prpdgrp[i]);
                this.innerPanel.add(prpsgrp[i]);
            }
        }
        this.innerPanel.revalidate();
        this.innerPanel.repaint();
        this.trend.viewChannel(paramInt);
    }


    public void viewAEChannel(int paramInt) {
        this.currSelCh = paramInt;
        if (paramInt < 8) {
            this.aeinnerPanel.removeAll();
            this.aeinnerPanel.setLayout(new GridLayout(0, 2, 1, 1));
            this.aeinnerPanel.add(this.aeprpdgrp[paramInt]);
            this.aeinnerPanel.add(this.aeprpsgrp[paramInt]);
        } else {
            this.aeinnerPanel.removeAll();
            this.aeinnerPanel.setLayout(new GridLayout(2, 8, 1, 1));
            for (int i = 0; i < 8; i++) {
                this.aeinnerPanel.add(aeprpdgrp[i]);
                this.aeinnerPanel.add(aeprpsgrp[i]);
            }
        }
        this.aeinnerPanel.revalidate();
        this.aeinnerPanel.repaint();

        this.trend.viewChannel(paramInt);
    }

  
  /*  
  public void viewNChannel(int i){
	  
	  boolean1[i] = Boolean.valueOf(jcheckbox[i].isSelected());
	  this.currSelCh = i;
	  //this.innerPanel.removeAll();
	  if(!boolean1[i]){
		  this.innerPanel.remove(jpanelcheckbox[i]);
	  }
	  if((boolean1[0] && boolean1[1] && boolean1[2] && boolean1[3]) || !(boolean1[0] && boolean1[1] && boolean1[2] && boolean1[3])){
		  for(int j = 0; j < i+1; j++){
			  if(boolean1[j]){
				  this.innerPanel.setLayout(new GridLayout(1,j,1,1));
				  jpanelcheckbox[j].add(this.prpdgrp[j]);
				  jpanelcheckbox[j].add(this.prpsgrp[j]);
				  this.innerPanel.add(jpanelcheckbox[j]);
				  
			  }else{
				  this.innerPanel.remove(jpanelcheckbox[j]);
			  }
		  }
	  }
	  this.innerPanel.revalidate();
	  this.innerPanel.repaint();	    
	  this.trend.viewChannel(i);
  }
  
  public void view4Channel(){
	  this.currSelCh = 4;

		this.innerPanel.removeAll();
		this.innerPanel.setLayout(new GridLayout(1, 4, 1, 1));
		for(int k = 0; k < 8; k++){
			jpanelcheckbox[k].add(this.prpdgrp[k]);
			jpanelcheckbox[k].add(this.prpsgrp[k]);
			this.innerPanel.add(jpanelcheckbox[k]);
		}
	    this.innerPanel.revalidate();
	    this.innerPanel.repaint();	    
	   //this.trend.view4Channel();
  }
  */


    public void updateCurrent() {
        this.prpdList.getMaxSelectionIndex();//更新选择的指针
        this.eventList.getMaxSelectionIndex();
        this.prpdList.getLastVisibleIndex();//更新选择的指针
        this.eventList.getMaxSelectionIndex();
        int i;
        if (this.pdcl.size() > 0) {
            i = ((PRPDCell) this.pdcl.lastElement()).min + 1;
            for (; i < 1440; i++) {
                if (this.currentSite.loadedPrpdPos[i] >= 0) {
                    this.pdcl.addElement(new PRPDCell(i, this.currentSite.loadedPrpdPos[i], ""));
                }
            }
        }

        ((DefaultTreeModel) this.dataTree.getModel()).nodeStructureChanged(this.currentSite);


        this.trend.refresh = true;
        this.trend.repaint();

        this.calendar.dc.update();
    }


    public void setDay(long paramLong)    //memo 20230831 点击数据获取一天数据
    {
        this.currentSite.load1day(paramLong);

        this.pdcl.clear();
        for (int i = 0; i < 1440; i++) {
            if (this.currentSite.loadedPrpdPos[i] >= 0) {
                this.pdcl.addElement(new PRPDCell(i, this.currentSite.loadedPrpdPos[i], ""));
            }
        }
        this.currentSite.getEventList(paramLong, paramLong);
        //设置日期，将一键勾选设置为灰色
        GisExplorer.this.selectAllButton.setEnabled(false);
        eventList.setModel(this.currentSite.evList);//超高频
        aeeventList.setModel(this.currentSite.evList);//超声波
        this.calendar.selectDay(paramLong);
        this.trend.refresh = true;
        this.trend.repaint();
    }


    public void applySelection(int paramInt1, int paramInt2)//Ӧ��ѡ��PRPD���
    {
        if (this.pdcl.size() == 0) {
            return;
        }
        if (paramInt1 > paramInt2) {
            int i = paramInt1;
            paramInt1 = paramInt2;
            paramInt2 = i;
        }
        this.trendSelTimeStart = paramInt1;
        this.trendSelTimeEnd = paramInt2;

        int j = 0;
        int k = 0;

        int i = 0;
        for (int m = this.pdcl.size(); i < m; i++) {
            if (paramInt1 > ((PRPDCell) this.pdcl.get(i)).min) {
                j = i;
            }
            if (paramInt2 > ((PRPDCell) this.pdcl.get(i)).min) {
                k = i;
            }
        }
        this.prpdList.setSelectionInterval(j, k);

        this.prpdList.ensureIndexIsVisible(j);
        this.prpdList.ensureIndexIsVisible(k);
        this.prpdList.repaint();
    }


    public void openSite(SiteData paramSiteData)//����ݿ�
    {
        this.currentSite = paramSiteData;
    }


    public void run() {
        this.currentSite = this.sd[0];
        setDay(System.currentTimeMillis());

        //JFrame localJFrame = new JFrame("Gis Data Explorer");
        localJFrame = new JFrame("Gis Data Explorer");

        localJFrame.add(this);

        localJFrame.pack();
        localJFrame.setDefaultCloseOperation(3);
        localJFrame.setVisible(true);
    }


    class ExampleFileFilter extends FileFilter//�����ļ�
    {
        String d;
        String ext;

        ExampleFileFilter() {
        }

        public boolean accept(File paramFile) {
            return paramFile.getName().toLowerCase().endsWith(this.ext);
        }

        public String getDescription() {
            return this.d;
        }

        public void addExtension(String paramString) {
            this.ext = paramString.toLowerCase();
        }

        public void setDescription(String paramString) {
            this.d = paramString;
        }
    }


    //UHF初始化
    public void UHFInit() {
        JPanel localJPanel1 = new JPanel(new GridLayout(1, 0));  //显示通道选项总布局
        JPanel localJPanel2 = new JPanel(new GridLayout(2, 0));  //PRPD布局
        JPanel localJPanel3 = new JPanel(new GridLayout(2, 0));  //PRPS布局

        JPanel localJPanel6 = new JPanel(new GridLayout(1, 3, 5, 0));
        //localJPanel6.setBorder(new EtchedBorder(1));

        localJPanel6.setBorder(new TitledBorder(new EtchedBorder(1), ChooseLang.M[306]));
        localJPanel2.setBorder(new TitledBorder(new EtchedBorder(1), " PRPD "));
        localJPanel3.setBorder(new TitledBorder(new EtchedBorder(1), " PRPS "));

        GisExplorer.GisButtonListener localGisButtonListener = new GisExplorer.GisButtonListener();//打印绘图通道侦听
        ButtonGroup localButtonGroup3 = new ButtonGroup();
        JPanel localJPanelview = new JPanel(new GridLayout(3, 0));               //通道显示布局

        //新建查看通道单选按钮
        JRadioButton localJRadioButton5 = new JRadioButton(ChooseLang.M[232]);//通道1查看
        JRadioButton localJRadioButton6 = new JRadioButton(ChooseLang.M[233]);//通道2查看
        JRadioButton localJRadioButton7 = new JRadioButton(ChooseLang.M[234]);//通道3查看
        JRadioButton localJRadioButton8 = new JRadioButton(ChooseLang.M[235]);//通道4查看
        JRadioButton localJRadioButtono5 = new JRadioButton(ChooseLang.M[286]);//通道5查看
        JRadioButton localJRadioButtono6 = new JRadioButton(ChooseLang.M[287]);//通道6查看
        JRadioButton localJRadioButtono7 = new JRadioButton(ChooseLang.M[288]);//通道7查看
        JRadioButton localJRadioButtono8 = new JRadioButton(ChooseLang.M[289]);//通道8查看
        JRadioButton localJRadioButton9 = new JRadioButton(ChooseLang.M[236]);//所有通道查看
//    localJRadioButtono7.setVisible(false);
//    localJRadioButtono8.setVisible(false);
//    localJRadioButton9.setVisible(false);


        if (HbDesktopPane.tongdao == 1) {
            localJRadioButton6.setVisible(false);
            localJRadioButton7.setVisible(false);
            localJRadioButton8.setVisible(false);
            localJRadioButton9.setVisible(false);
            localJRadioButtono5.setVisible(false);
            localJRadioButtono6.setVisible(false);
            localJRadioButtono7.setVisible(false);
            localJRadioButtono8.setVisible(false);

        }

        if (HbDesktopPane.tongdao == 2) {
            localJRadioButton7.setVisible(false);
            localJRadioButton8.setVisible(false);
            localJRadioButton9.setVisible(false);
            localJRadioButtono5.setVisible(false);
            localJRadioButtono6.setVisible(false);
            localJRadioButtono7.setVisible(false);
            localJRadioButtono8.setVisible(false);
        }
        if (HbDesktopPane.tongdao == 3) {
            localJRadioButton8.setVisible(false);
            localJRadioButton9.setVisible(false);
            localJRadioButtono5.setVisible(false);
            localJRadioButtono6.setVisible(false);
            localJRadioButtono7.setVisible(false);
            localJRadioButtono8.setVisible(false);
        }
        localJRadioButton5.setFont(GisXYZ.fontG);
        localJRadioButton6.setFont(GisXYZ.fontG);
        localJRadioButton7.setFont(GisXYZ.fontG);
        localJRadioButton8.setFont(GisXYZ.fontG);
        localJRadioButton9.setFont(GisXYZ.fontG);
        localJRadioButtono5.setFont(GisXYZ.fontG);
        localJRadioButtono6.setFont(GisXYZ.fontG);
        localJRadioButtono7.setFont(GisXYZ.fontG);
        localJRadioButtono8.setFont(GisXYZ.fontG);
        localJRadioButton5.setSelected(true);

        //查看通道单选按钮侦听
        localJRadioButton5.addActionListener(localGisButtonListener);
        localJRadioButton6.addActionListener(localGisButtonListener);
        localJRadioButton7.addActionListener(localGisButtonListener);
        localJRadioButton8.addActionListener(localGisButtonListener);
        localJRadioButton9.addActionListener(localGisButtonListener);
        localJRadioButtono5.addActionListener(localGisButtonListener);
        localJRadioButtono6.addActionListener(localGisButtonListener);
        localJRadioButtono7.addActionListener(localGisButtonListener);
        localJRadioButtono8.addActionListener(localGisButtonListener);
        //查看通道单选按钮
        localButtonGroup3.add(localJRadioButton5);
        localButtonGroup3.add(localJRadioButton6);
        localButtonGroup3.add(localJRadioButton7);
        localButtonGroup3.add(localJRadioButton8);
        localButtonGroup3.add(localJRadioButtono5);
        localButtonGroup3.add(localJRadioButtono6);
        localButtonGroup3.add(localJRadioButtono7);
        localButtonGroup3.add(localJRadioButtono8);
        localButtonGroup3.add(localJRadioButton9);

        localJPanelview.add(localJRadioButton5);
        localJPanelview.add(localJRadioButton6);
        localJPanelview.add(localJRadioButton7);
        localJPanelview.add(localJRadioButton8);
        localJPanelview.add(localJRadioButtono5);
        localJPanelview.add(localJRadioButtono6);
        localJPanelview.add(localJRadioButtono7);
        localJPanelview.add(localJRadioButtono8);
        localJPanelview.add(localJRadioButton9);

        localJPanel6.add(localJPanelview);

        //PRPD
        ButtonGroup localButtonGroup1 = new ButtonGroup();
        JRadioButton localJRadioButton1 = new JRadioButton("2D");
        JRadioButton localJRadioButton2 = new JRadioButton("3D");
        localButtonGroup1.add(localJRadioButton1);
        localButtonGroup1.add(localJRadioButton2);
        localJPanel2.add(localJRadioButton1);
        localJPanel2.add(localJRadioButton2);
        localJRadioButton1.setSelected(true);
        localJPanel6.add(localJPanel2);

        localJRadioButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                for (int i = 0; i < 8; i++) {
                    GisExplorer.this.prpdgrp[i].draw2D = ((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected();
                    GisExplorer.this.prpdgrp[i].draw3D = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.prpdgrp[i].drawCon = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.prpdgrp[i].drawUltral = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.prpdgrp[i].Waveform = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.prpdgrp[i].drawColum = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.prpdgrp[i].stringToDraw = "";
                    GisExplorer.this.prpdgrp[i].stringToDraw += ChooseLang.M[i + 132];
                    GisExplorer.this.prpdgrp[i].stringToDraw += "PRPD";
                    GisExplorer.this.prpdgrp[i].prpdrefresh = true;
                    GisExplorer.this.prpdgrp[i].repaint();
                }
            }
        });
        localJRadioButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                for (int i = 0; i < 8; i++) {
                    GisExplorer.this.prpdgrp[i].rdeg = 2.356194490192345D;
                    GisExplorer.this.prpdgrp[i].tdeg = 0.5340707511102649D;
                    GisExplorer.this.prpdgrp[i].draw3D = ((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected();
                    GisExplorer.this.prpdgrp[i].draw2D = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.prpdgrp[i].drawCon = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.prpdgrp[i].drawUltral = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.prpdgrp[i].Waveform = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.prpdgrp[i].drawColum = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.prpdgrp[i].stringToDraw = "";
                    GisExplorer.this.prpdgrp[i].stringToDraw += ChooseLang.M[i + 132];
                    GisExplorer.this.prpdgrp[i].stringToDraw += "PRPD";
                    GisExplorer.this.prpdgrp[i].prpdrefresh = true;
                    GisExplorer.this.prpdgrp[i].repaint();
                }
            }
        });

        //PRPS
        ButtonGroup localButtonGroup2 = new ButtonGroup();
        JRadioButton localJRadioButton3 = new JRadioButton("2D");
        JRadioButton localJRadioButton4 = new JRadioButton("3D");
        localButtonGroup2.add(localJRadioButton3);
        localButtonGroup2.add(localJRadioButton4);
        localJPanel3.add(localJRadioButton3);
        localJPanel3.add(localJRadioButton4);
        localJRadioButton4.setSelected(true);
        localJPanel6.add(localJPanel3);

        localJRadioButton3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                for (int i = 0; i < 8; i++) {
                    GisExplorer.this.prpsgrp[i].draw3D = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.prpsgrp[i].stringToDraw = "";
                    GisExplorer.this.prpsgrp[i].stringToDraw += ChooseLang.M[i + 132];
                    GisExplorer.this.prpsgrp[i].stringToDraw += "PRPS";
                    GisExplorer.this.prpsgrp[i].isflygraphic = false;
                    GisExplorer.this.prpsgrp[i].prpsrefresh = true;
                    GisExplorer.this.prpsgrp[i].repaint();
                }
            }
        });

        localJRadioButton4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                for (int i = 0; i < 8; i++) {
                    GisExplorer.this.prpsgrp[i].rdeg = 2.356194490192345D;
                    GisExplorer.this.prpsgrp[i].tdeg = 0.5340707511102649D;
                    GisExplorer.this.prpsgrp[i].draw3D = ((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected();
                    GisExplorer.this.prpsgrp[i].stringToDraw = "";
                    GisExplorer.this.prpsgrp[i].stringToDraw += ChooseLang.M[i + 132];
                    GisExplorer.this.prpsgrp[i].stringToDraw += "PRPS";
                    GisExplorer.this.prpsgrp[i].prpsrefresh = true;
                    GisExplorer.this.prpsgrp[i].repaint();
                }
            }
        });


        localJPanel1.add(localJPanel6);

        this.prpdList = new JList<PRPDCell>(this.pdcl);
        this.prpdList.setToolTipText(ChooseLang.M[320]);//PRPD列表项
        this.eventList = new JList<PRPSCell>();
        this.eventList.setAutoscrolls(false);
        this.eventList.setToolTipText(ChooseLang.M[321]);//PRPS列表项

        ((DefaultListCellRenderer) this.prpdList.getCellRenderer()).setHorizontalAlignment(0);
        this.prpdList.setCellRenderer(new TimeListRenderer(this));
        this.eventList.setCellRenderer(new CheckBoxListRenderer());
        // 跟踪最后选中的字符串项

//    this.eventList.setSelectionModel(new DefaultListSelectionModel(){
//          @Override
//          public void setSelectionInterval(int index0, int index1) {
//              if(super.isSelectedIndex(index0)) {
//                  super.removeSelectionInterval(index0, index1);
//              }else{
//                 super.addSelectionInterval(index0, index1);
//              }
//          }
//    });
        // 添加鼠标点击事件监听器
        this.eventList.addMouseListener(new MouseAdapter() {
            private int lastSelectedIndex = -1;

            @Override
            public void mousePressed(MouseEvent e) {
                int index = eventList.locationToIndex(e.getPoint());
                if (index != -1) {
                    PRPSCell clickedItem = currentSite.evList.getElementAt(index);

                    // 获取复选框区域的边界（假设复选框宽度为 20）
                    Rectangle cellBounds = eventList.getCellBounds(index, index);
                    Rectangle checkboxBounds = new Rectangle(
                            cellBounds.x, cellBounds.y, 20, cellBounds.height
                    );

                    // 判断点击的是复选框区域还是字符串区域
                    if (checkboxBounds.contains(e.getPoint())) {
                        // 点击复选框区域：切换复选框状态
                        clickedItem.setSelected(!clickedItem.isSelected());
                        final DefaultListModel<PRPSCell> evList = currentSite.evList;
                        int selected = 0;
                        for (int i = 0; i < evList.getSize(); i++) {
                            PRPSCell prpscell = GisExplorer.this.eventList.getModel().getElementAt(i);
                            if (prpscell.isSelected) {
                                selected ++;
                            }
//                            if(selected > 1){
//                                break;
//                            }
                        }
                        System.out.println("selected:" + selected);
                        if (selected == 1) {
                            GisExplorer.this.selectAllButton.setEnabled(true);
                        }else{
                            GisExplorer.this.selectAllButton.setEnabled(false);
                        }
                        if(selected > 100){
                            clickedItem.setSelected(false);
                            JOptionPane.showMessageDialog(graphjpanel, "单次打印最多选择100条记录！");
                        }
                        System.out.println("复选框状态切换: " + clickedItem.getStr() + " -> " + clickedItem.isSelected());
                    } else {
                        // 点击字符串区域：触发字符串点击事件
//                      JOptionPane.showMessageDialog(graphjpanel, "你点击了: " + item.getStr());

                        // 点击字符串区域：单选模式，更新最后选中的字符串项
                        if (lastSelectedIndex != -1 && currentSite.evList.size() >= lastSelectedIndex) {
                            // 取消之前选中项的背景色
                            currentSite.evList.getElementAt(lastSelectedIndex).setSelectedBackground(false);
                        }
//                      final boolean selected = clickedItem.isSelected();
//                      clickedItem.setSelected(!selected);
//                      clickedItem.setSelected(selected);
                        System.out.println("index :" + index);
                        if(index > 1){
                            eventList.setSelectedIndex(index-1);
                            eventList.setSelectedIndex(index);
                        }else{
                            eventList.setSelectedIndex(index+1);
                            eventList.setSelectedIndex(index);
                        }


//                      eventList.setSelectedIndex(index);
//                      setSelectedIndex
                        // 设置当前选中项的背景色
                        clickedItem.setSelectedBackground(true);
                        lastSelectedIndex = index;
                        System.out.println("最后选中的字符串项: " + clickedItem.getStr());

                    }
                    eventList.repaint(); // 刷新列表
                }
            }
        });


//    ((DefaultListCellRenderer)this.eventList.getCellRenderer()).setHorizontalAlignment(0);
        JScrollPane localJScrollPane1 = new JScrollPane(this.prpdList, 22, 31);
        JScrollPane localJScrollPane2 = new JScrollPane(this.eventList, 22, 31);
        localJScrollPane1.setPreferredSize(new Dimension(90, 200));
        localJScrollPane2.setPreferredSize(new Dimension(190, 200));

        JLabel textLabel = new JLabel(ChooseLang.M[405]);
        textLabel.setFont(new Font("黑体", Font.BOLD, 10 ));
        textLabel.setForeground(Color.red);

        JPanel buttonPanel = new JPanel();

        selectAllButton = new JButton(ChooseLang.M[406]);
        JButton deselectAllButton = new JButton(ChooseLang.M[407]);
        // 一键勾选按钮的事件监听器
        selectAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PRPSCell selectedCell ;
                int seleced = 0;
                final int size = currentSite.evList.size();
                for (int i = 0; i < size; i++) {
                    final PRPSCell cell = currentSite.evList.getElementAt(i);
                    if(cell.isSelected()){
                        for (int j = 0; j < select_limit && (i+j) < size; j++) {
                            seleced ++;
                            selectedCell = currentSite.evList.getElementAt(i+j);
                            selectedCell.setSelected(true);
                        }
                        break;
                    }
                    System.out.println("复选框状态切换: " + cell.getStr() + " -> " + cell.isSelected());
                }
                if(seleced > 1){
                    selectAllButton.setEnabled(false);
                    eventList.repaint(); // 刷新列表
                }else if (seleced == 1){
                    selectAllButton.setEnabled(true);
                } else {
                    JOptionPane.showMessageDialog(graphjpanel, ChooseLang.M[410]);
                }
            }
        });

        // 一键取消按钮的事件监听器
        deselectAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < currentSite.evList.size(); i++) {
                    final PRPSCell cell = currentSite.evList.getElementAt(i);
                    cell.setSelected(false);
                    System.out.println("复选框状态切换: " + cell.getStr() + " -> " + cell.isSelected());
                }
                selectAllButton.setEnabled(false);
                eventList.repaint(); // 刷新列表
            }
        });
        // 将按钮添加到按钮面板
        buttonPanel.add(textLabel, BorderLayout.NORTH);
        buttonPanel.add(selectAllButton);
        buttonPanel.add(deselectAllButton);

        // 创建主面板，使用 BorderLayout 布局
        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.add(buttonPanel, BorderLayout.NORTH); // 按钮面板放在顶部
        dataPanel.add(localJScrollPane2, BorderLayout.CENTER); // 列表放在中心

//    this.UHFPanel.add(localJScrollPane1, "West");
        this.UHFPanel.add(dataPanel, "East");
        this.innerPanel = new JPanel(new GridLayout(2, 4));//mana-report绘图区域
        this.UHFPanel.add(this.innerPanel, "Center");
        this.UHFPanel.add(localJPanel1, "South");
        this.viewTab.addTab("Channel", UHFPanel);
    }


    //AE初始化
    public void AEInit() {
        //AE
        JPanel localJPanel1 = new JPanel(new GridLayout(0, 4));
        JPanel localJPanel2 = new JPanel(new GridLayout(2, 0));
        JPanel localJPanel3 = new JPanel(new GridLayout(2, 0));

        JPanel localJPanel6 = new JPanel(new GridLayout(1, 3, 5, 0));
        //localJPanel6.setBorder(new EtchedBorder(1));

        localJPanel6.setBorder(new TitledBorder(new EtchedBorder(1), ChooseLang.M[306]));
        localJPanel2.setBorder(new TitledBorder(new EtchedBorder(1), ""));
        localJPanel3.setBorder(new TitledBorder(new EtchedBorder(1), ""));

        GisExplorer.GisButtonListener localGisButtonListener = new GisExplorer.GisButtonListener();//打印绘图通道侦听
        ButtonGroup localButtonGroup3 = new ButtonGroup();
        JPanel localJPanelview = new JPanel(new GridLayout(2, 0));

        //新建查看通道单选按钮
        JRadioButton localJRadioButton5 = new JRadioButton(ChooseLang.M[234]);//通道3查看
        JRadioButton localJRadioButton6 = new JRadioButton(ChooseLang.M[235]);//通道4查看
        localJRadioButton5.setFont(GisXYZ.fontG);
        localJRadioButton6.setFont(GisXYZ.fontG);
        localJRadioButton5.setSelected(true);

        //查看通道单选按钮侦听
        localJRadioButton5.addActionListener(localGisButtonListener);
        localJRadioButton6.addActionListener(localGisButtonListener);

        //查看通道单选按钮
        localButtonGroup3.add(localJRadioButton5);
        localButtonGroup3.add(localJRadioButton6);
        localJPanelview.add(localJRadioButton5);
        localJPanelview.add(localJRadioButton6);
        localJPanel6.add(localJPanelview);

        //PRPD
        ButtonGroup localButtonGroup1 = new ButtonGroup();
        JRadioButton localJRadioButton1 = new JRadioButton(ChooseLang.M[334]);//相位幅值
        JRadioButton localJRadioButton2 = new JRadioButton(ChooseLang.M[333]);//波形图
        localJRadioButton1.setFont(GisXYZ.fontG);
        localJRadioButton2.setFont(GisXYZ.fontG);
        localButtonGroup1.add(localJRadioButton1);
        localButtonGroup1.add(localJRadioButton2);
        localJPanel2.add(localJRadioButton1);
        localJPanel2.add(localJRadioButton2);
        localJRadioButton1.setSelected(true);
        localJPanel6.add(localJPanel2);

        localJRadioButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                for (int i = 0; i < 8; i++) {
                    GisExplorer.this.aeprpdgrp[i].draw2D = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.aeprpdgrp[i].draw3D = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.aeprpdgrp[i].drawCon = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.aeprpdgrp[i].drawUltral = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.aeprpdgrp[i].Waveform = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.aeprpdgrp[i].drawColum = ((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected();
                    GisExplorer.this.aeprpdgrp[i].stringToDraw = "";
                    GisExplorer.this.aeprpdgrp[i].stringToDraw += ChooseLang.M[334];
                    GisExplorer.this.aeprpdgrp[i].prpdrefresh = true;
                    GisExplorer.this.aeprpdgrp[i].repaint();
                }
            }
        });
        localJRadioButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                for (int i = 0; i < 8; i++) {
                    GisExplorer.this.aeprpdgrp[i].draw2D = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.aeprpdgrp[i].draw3D = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.aeprpdgrp[i].drawCon = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.aeprpdgrp[i].drawUltral = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.aeprpdgrp[i].Waveform = ((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected();
                    GisExplorer.this.aeprpdgrp[i].drawColum = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.aeprpdgrp[i].stringToDraw = "";
                    GisExplorer.this.aeprpdgrp[i].stringToDraw += ChooseLang.M[334];
                    GisExplorer.this.aeprpdgrp[i].prpdrefresh = true;
                    GisExplorer.this.aeprpdgrp[i].repaint();
                }
            }
        });

        //PRPS
        ButtonGroup localButtonGroup2 = new ButtonGroup();
        JRadioButton localJRadioButton3 = new JRadioButton(ChooseLang.M[331]);//连续图
        JRadioButton localJRadioButton4 = new JRadioButton(ChooseLang.M[330]);//飞行图
        localJRadioButton3.setFont(GisXYZ.fontG);
        localJRadioButton4.setFont(GisXYZ.fontG);
        localButtonGroup2.add(localJRadioButton3);
        localButtonGroup2.add(localJRadioButton4);
        //localJPanel3.add(localJRadioButton3);
        //localJPanel3.add(localJRadioButton4);
        localJRadioButton3.setSelected(true);
        localJPanel6.add(localJPanel3);

        localJRadioButton3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                for (int i = 0; i < 8; i++) {
                    GisExplorer.this.aeprpsgrp[i].draw3D = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.aeprpsgrp[i].isflygraphic = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.aeprpsgrp[i].isCon = ((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected();
                    GisExplorer.this.aeprpsgrp[i].stringToDraw = "";
                    GisExplorer.this.aeprpsgrp[i].stringToDraw += ChooseLang.M[331];
                    GisExplorer.this.aeprpsgrp[i].prpsrefresh = true;
                    GisExplorer.this.aeprpsgrp[i].repaint();
                }
            }
        });

        localJRadioButton4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                for (int i = 0; i < 8; i++) {
                    GisExplorer.this.aeprpsgrp[i].draw3D = (!((JRadioButton) paramAnonymousActionEvent.getSource()).isSelected());
                    GisExplorer.this.aeprpsgrp[i].stringToDraw = "";
                    GisExplorer.this.aeprpsgrp[i].stringToDraw = ChooseLang.M[330];
                    GisExplorer.this.aeprpsgrp[i].isflygraphic = true;
                    GisExplorer.this.aeprpsgrp[i].prpsrefresh = true;
                    GisExplorer.this.aeprpsgrp[i].repaint();
                }
            }
        });


        localJPanel1.add(localJPanel6);


        this.aeprpdList = new JList<PRPDCell>(this.pdcl);
        //this.aeprpdList.setToolTipText(ChooseLang.M[320]);//PRPD列表项
        this.aeeventList = new JList<PRPSCell>();
        //this.aeeventList.setToolTipText(ChooseLang.M[321]);//PRPS列表项

        ((DefaultListCellRenderer) this.aeprpdList.getCellRenderer()).setHorizontalAlignment(0);
        this.aeprpdList.setCellRenderer(new TimeListRenderer(this));
        ((DefaultListCellRenderer) this.aeeventList.getCellRenderer()).setHorizontalAlignment(0);
        JScrollPane localJScrollPane5 = new JScrollPane(this.aeprpdList, 22, 31);
        JScrollPane localJScrollPane6 = new JScrollPane(this.aeeventList, 22, 31);
        localJScrollPane5.setPreferredSize(new Dimension(90, 200));
        localJScrollPane6.setPreferredSize(new Dimension(90, 200));

        this.AEPanel.add(localJScrollPane5, "West");
        this.AEPanel.add(localJScrollPane6, "East");
        this.aeinnerPanel = new JPanel(new GridLayout(2, 4));
        this.AEPanel.add(this.aeinnerPanel, "Center");
        this.AEPanel.add(localJPanel1, "South");
        //this.viewTab.addTab(ChooseLang.M[134], AEPanel);  //AE谱图显示添加到选项卡
    }


    //data数据初始化
    public void DataTreeInit() {
        this.dataTree = new JTree(new DefaultTreeModel(this.db));//��ݿ����νṹ
        this.dataTree.addTreeSelectionListener(new TreeSelectionListener()//������ݿ�ѡ��
        {
            public void valueChanged(TreeSelectionEvent treeselectionevent) {
                Object aobj[] = treeselectionevent.getPath().getPath();
                if (aobj.length > 1) {
                    GisExplorer.this.openSite(GisExplorer.this.db.openSite(((SiteData) aobj[1]).siteName));
                    if (aobj.length > 2) {
                        GisExplorer.this.calendar.dateSelected(((PeriodBlock) aobj[2]).etime);
                    } else {
                        GisExplorer.this.calendar.dateSelected(System.currentTimeMillis());
                    }
                    GisExplorer.this.repaint();
                }

            }
        });
        JScrollPane localJScrollPane3 = new JScrollPane(this.dataTree, 22, 31);
        localJScrollPane3.setPreferredSize(new Dimension(225, 50));
        this.topPanel.add(localJScrollPane3, "West");
    }


    //UHFPRPD历史数据绘制
    public void DrawUHFPRPD() {
        //绘制历史图
        this.prpdList.addListSelectionListener(new ListSelectionListener() {
            int[][] temp = new int[64][64];

            @SuppressWarnings("deprecation")
            @Override
            public void valueChanged(ListSelectionEvent paramAnonymousListSelectionEvent) {
                if (paramAnonymousListSelectionEvent.getValueIsAdjusting()) {
                    return;
                }

//                final PRPSCell selectedValue = GisExplorer.this.eventList.getSelectedValue();
                Object[] arrayOfObject = GisExplorer.this.prpdList.getSelectedValues();
//                if(arrayOfObject.length > 0){
//                    final PRPDCell prddCell = (PRPDCell)arrayOfObject[0];
//                    final String prsdStr = selectedValue.getStr();
//                    final String[] prsdStrSplit = prsdStr.split(":");
//                    final String prddStr = prddCell.str;
//                    final String[] prddStrSplit = prddStr.split(":");
//                    if(!prsdStrSplit[0].equals(prddStrSplit[0])||!prsdStrSplit[1].equals(prddStrSplit[1])){
//                        return;
//                    }
//                }


                int m = arrayOfObject.length;
                //去掉左侧选项的影响
//                if (m > 0) {
//                    GisExplorer.this.trend.setSelected(((PRPDCell) arrayOfObject[0]).min, ((PRPDCell) arrayOfObject[(m - 1)]).min);
//
//                    GisExplorer.this.psclSel(((PRPDCell) arrayOfObject[0]).min);//���ӵ����
//                } else {
//                    GisExplorer.this.trend.setSelected(0, 0);
//                }
                for (int i = 0; i < 8; i++) {
                    GisExplorer.this.prpdgrp[i].Countprpd = 0;
                    for (int j = 0; j < m; j++) {
                        // memo 20230824 输出PRPD2D
                        GisExplorer.this.currentSite.getPRPD(((PRPDCell) arrayOfObject[j]).min, i, this.temp);
                    }
                    GisRealtimeBuffer.filter(this.temp, GisExplorer.this.prpdgrp[i].prpd);
                    for (int j = 0; j < 64; j++) {
                        for (int k = 0; k < 64; k++) {
                            this.temp[j][k] = 0;
                            GisExplorer.this.prpdgrp[i].Countprpd += GisExplorer.this.prpdgrp[i].prpd[j][i];//放电次数
                            GisExplorer.this.prpdgrp[i].Countprpd /= 3.5;
                        }
                    }
                    if (arrayOfObject.length > 0) {
                        GisExplorer.this.trendSelTimeStart = ((PRPDCell) arrayOfObject[0]).min;
                        GisExplorer.this.trendSelTimeEnd = ((PRPDCell) arrayOfObject[(m - 1)]).min;
                    } else {
                        GisExplorer.this.trendSelTimeStart = 0;
                        GisExplorer.this.trendSelTimeEnd = 0;
                    }
                    GisExplorer.this.prpdgrp[i].prpdrefresh = true;
                    GisExplorer.this.prpdgrp[i].repaint();
                    GisExplorer.this.prpdList.repaint();
                }
            }
        });
    }


    //UHFPRPS历史数据绘制
    public void DrawUHFPRPS() {
        //绘制历史图
        this.eventList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent paramAnonymousListSelectionEvent) {
                if (paramAnonymousListSelectionEvent.getValueIsAdjusting()) {
                    return;
                }

                // 获取事件源（JList）
                JList<PRPSCell> sourceList = (JList<PRPSCell>) paramAnonymousListSelectionEvent.getSource();

                // 获取选中项的索引
                int selectedIndex = sourceList.getSelectedIndex();
//                System.out.println("selectedIndex:"+selectedIndex);

//                // 根据索引获取选中项的数据
//                if (selectedIndex != -1) {
//                    PRPSCell cell = sourceList.getModel().getElementAt(selectedIndex);
////                    System.out.println("当前选中的数据: " + cell.getStr());
//                } else {
//                    System.out.println("没有选中任何数据");
//                }

                final ListModel<PRPSCell> model = GisExplorer.this.eventList.getModel();
                PRPSCell prpscell = null;

                int groundSelectIndex = -1;
                for (int i = 0; i < model.getSize(); i++) {

                    prpscell = GisExplorer.this.eventList.getModel().getElementAt(i);
                    if (prpscell.isSelectedBackground()) {
                        groundSelectIndex = i;
                        break;
                    }
                }
//                System.out.println("groundSelectIndex:"+groundSelectIndex);
                if(groundSelectIndex != selectedIndex){
//                    System.out.println("无需处理渲染逻辑");
                    return;
                }

//          PRPSCell selectPrpscell = GisExplorer.this.eventList.getSelectedValue();
//        if(!prpscell.isSelectedBackground()){
//            return;
//        }
                if (prpscell == null) {
                    for (int i = 0; i < 8; i++) {
                        int i4 = 0;//最大值
                        for (int j = 0; j < 5120; j++) {
                            int k3 = GisExplorer.this.prpsgrp[i].pdData[j];
                            if (i4 < k3)
                                i4 = k3;
                            GisExplorer.this.prpsgrp[i].amxdBm = (double) (i4 * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double) GisSignal.calMin_dBm;
                            GisExplorer.this.prpsgrp[i].pdData[j] = 0;
                            k3 = 0;
                        }
                        int l3 = 5055;
                        for (int l1 = 0; l1 < 64; ) {
                            if (l3 > 5120)
                                l3 = 5055;
                            GisExplorer.this.prpsgrp[i].columdata[l1++] = 0;//柱形图数据
                        }
                        GisExplorer.this.prpsgrp[i].resultString[0] = "";
                        GisExplorer.this.prpsgrp[i].resultString[1] = "";
                        GisExplorer.this.prpsgrp[i].resultString[2] = "";
                        GisExplorer.this.prpsgrp[i].resultString[3] = "";
                        GisExplorer.this.prpsgrp[i].resultString[4] = "";
                        GisExplorer.this.prpsgrp[i].resultString[5] = "";
                        GisExplorer.this.prpsgrp[i].resultString[6] = "";
                        GisExplorer.this.prpsgrp[i].resultString[7] = "";
                    }
                } else {
                    GisExplorer.this.psclSel(prpscell.time);
                    GisExplorer.this.pdclSel(prpscell.time);
                    GisExplorer.this.memoPanel.selectTime(prpscell.time);
                }
                for (int i = 0; i < 8; i++) {
                    GisExplorer.this.prpsgrp[i].prpsrefresh = true;
                    GisExplorer.this.prpsgrp[i].repaint();

                }
                // 确保选中项在可见范围内
                GisExplorer.this.eventList.ensureIndexIsVisible(selectedIndex);
            }
        });
    }

    //AEPRPD历史数据绘制
    public void DrawAEPRPD() {
        //绘制历史图
        this.aeprpdList.addListSelectionListener(new ListSelectionListener() {
            @SuppressWarnings("deprecation")
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                Object[] arrayOfObject = GisExplorer.this.aeprpdList.getSelectedValues();

                int m = arrayOfObject.length;
                if (m > 0) {
                    GisExplorer.this.trend.setSelected(((PRPDCell) arrayOfObject[0]).min, ((PRPDCell) arrayOfObject[(m - 1)]).min);

                    GisExplorer.this.aepsclSel(((PRPDCell) arrayOfObject[0]).min);//���ӵ����
                } else {
                    GisExplorer.this.trend.setSelected(0, 0);
                }

                getPRPSList();

                for (int i = 0; i < 8; i++) {
                    if (arrayOfObject.length > 0) {
                        GisExplorer.this.trendSelTimeStart = ((PRPDCell) arrayOfObject[0]).min;
                        GisExplorer.this.trendSelTimeEnd = ((PRPDCell) arrayOfObject[(m - 1)]).min;
                    } else {
                        GisExplorer.this.trendSelTimeStart = 0;
                        GisExplorer.this.trendSelTimeEnd = 0;
                    }
                    GisExplorer.this.aeprpdgrp[i].prpdrefresh = true;
                    GisExplorer.this.aeprpdgrp[i].repaint();
                    GisExplorer.this.aeprpdList.repaint();
                }
            }
        });
    }

    //AEPRPS历史数据绘制
    public void DrawAEPRPS() {
        //绘制历史图
        this.aeeventList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                getPRPSList();
            }
        });
    }

    //获取PRPS数据列表
    public void getPRPSList() {
        PRPSCell aeprpscell = GisExplorer.this.aeeventList.getSelectedValue();
        if (aeprpscell == null) {
            for (int i = 0; i < 8; i++) {
        	/*
      	int i4 = 0;//最大值
          for (int j = 0; j < 5120; j++) {           	 
          	 int k3 =  GisExplorer.this.aeprpsgrp[i].pdData[j];
          	 if(i4 < k3)
          		 i4 = k3;
          	 GisExplorer.this.aeprpsgrp[i].amxdBm = GisExplorer.this.aeprpdgrp[i].amxdBm = (double)(i4 * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double)GisSignal.calMin_dBm;
          	 GisExplorer.this.aeprpsgrp[i].pdData[j] = 0; 
          	 k3 = 0;
          }
          int l3 = 5055;
          for (int l1 = 0; l1 < 64;)
			{				
				if(l3 > 5120)
					l3 = 5055;
				GisExplorer.this.aeprpsgrp[i].columdata[l1++] = 0;//柱形图数据
			}
			*/
                GisExplorer.this.aeprpsgrp[i].resultString[0] = "";
                GisExplorer.this.aeprpsgrp[i].resultString[1] = "";
                GisExplorer.this.aeprpsgrp[i].resultString[2] = "";
                GisExplorer.this.aeprpsgrp[i].resultString[3] = "";
                GisExplorer.this.aeprpsgrp[i].resultString[4] = "";
                GisExplorer.this.aeprpsgrp[i].resultString[5] = "";
                GisExplorer.this.aeprpsgrp[i].resultString[6] = "";
                GisExplorer.this.aeprpsgrp[i].resultString[7] = "";

                GisExplorer.this.aeprpdgrp[i].resultStringprpd[0] = "";
                GisExplorer.this.aeprpdgrp[i].resultStringprpd[1] = "";
                GisExplorer.this.aeprpdgrp[i].resultStringprpd[2] = "";
                GisExplorer.this.aeprpdgrp[i].resultStringprpd[3] = "";
                GisExplorer.this.aeprpdgrp[i].resultStringprpd[4] = "";
                GisExplorer.this.aeprpdgrp[i].resultStringprpd[5] = "";
                GisExplorer.this.aeprpdgrp[i].resultStringprpd[6] = "";
                GisExplorer.this.aeprpdgrp[i].resultStringprpd[7] = "";
            }
        } else {
            GisExplorer.this.aepsclSel(aeprpscell.time);
            GisExplorer.this.pdclSel(aeprpscell.time);
            GisExplorer.this.memoPanel.selectTime(aeprpscell.time);
        }
        for (int i = 0; i < 8; i++) {
            GisExplorer.this.aeprpsgrp[i].prpsrefresh = true;
            GisExplorer.this.aeprpsgrp[i].repaint();

        }
    }

    //绘制趋势图
    public void DrawTrend() {
        //**************����*****************//
        //240	信号峰值						//
        //241	信号均值						//
        //242	信号计数						//
        //243	绝缘件异常放电 			    	//
        //244	自由导体放电					//
        //245	浮游电极放电					//
        //246	尖端放电						//
        //247	未知							//
        //15	噪声							//
        //**********************************//
        //String[] arrayOfString = { ChooseLang.M[240], ChooseLang.M[241], ChooseLang.M[242], "", ChooseLang.M[243], ChooseLang.M[244], ChooseLang.M[245], ChooseLang.M[246], "", "", ChooseLang.M[247], ChooseLang.M[15] };
        String[] arrayOfString = {ChooseLang.M[240], ChooseLang.M[241], ChooseLang.M[242], "", ChooseLang.M[243], ChooseLang.M[244], ChooseLang.M[245], ChooseLang.M[246]};

        //int[] arrayOfInt = { 0, 1, 4, -1, 5, 6, 7, 8, 9, 10, 11, 12 };
        int[] arrayOfInt = {0, 1, 4, -1, 5, 6, 7, 8};

        JPanel localJPanel7 = new JPanel(new GridLayout(2, 4));

        int i = 0;
        for (int j = 0; j < 8; j++)//8个数据
        {
            if (arrayOfString[j].length() > 0) {
                JCheckBox localJCheckBox = new JCheckBox(arrayOfString[j]);
                localJCheckBox.setFont(GisXYZ.fontG);
                localJCheckBox.setForeground(trend.grpColor[arrayOfInt[j]]);
                localJPanel7.add(localJCheckBox);
                class GraphSelListener implements ActionListener {
                    int       grpNum;
                    JCheckBox chk;

                    public void actionPerformed(ActionEvent e) {
                        GisExplorer.this.trend.valueVisible[grpNum] = chk.isSelected();
                        GisExplorer.this.trend.refresh = true;
                        GisExplorer.this.trend.repaint();
                    }
                }

                GraphSelListener local1GraphSelListener = new GraphSelListener();
                local1GraphSelListener.grpNum = arrayOfInt[j];
                local1GraphSelListener.chk = localJCheckBox;

                localJCheckBox.addActionListener(local1GraphSelListener);//��ѡ������
                if (((j > 3) && (j < 8)) || j == 0) {
                    localJCheckBox.doClick();
                }
            } else {
                localJPanel7.add(new JLabel());
            }
            //j += 4;
            //if (j >= 12)
            //{
            //j -= 11;
            //}

            if (j >= 8) {
                j -= 7;
            }
        }
        localJPanel7.setBorder(new TitledBorder(new EtchedBorder(1), ChooseLang.M[249])); //Graph Drawingsͼ��ͼֽģ��
        this.bottomPanel.add(localJPanel7);
    }
}
