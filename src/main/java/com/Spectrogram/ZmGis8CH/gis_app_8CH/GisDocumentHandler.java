package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.*;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import freemarker.template.*;

public class GisDocumentHandler {

    private Configuration configuration = null;
    String result[][];//判定结果
    //GisWordTableJudge wordtable[];//word表格
    String imagestr[];
    String graname[];
    String imagedetail[][];
    String imageprpd[][];//prpd图
    String imageprps[][];//prps图
    String resultdetail[][][];
    String imagetrend[][];//趋势图
    String blank_image;

    public GisDocumentHandler(int listSize) {
        configuration = new Configuration();
        configuration.setDefaultEncoding("UTF-8");
        result = new String[8][5];
        //wordtable = new GisWordTableJudge[4];
        imagestr = new String[8];
//		imagedetail = new String[8][8];
//		imageprpd = new String[8][8];
//		imageprps = new String[8][8];
//		imagetrend = new String[8][5];
//		resultdetail = new String[8][8][5];
        imagedetail = new String[8][listSize];
        imageprpd = new String[8][listSize];
        imageprps = new String[8][listSize];
        imagetrend = new String[8][5];
        resultdetail = new String[8][listSize][5];
        graname = (new String[] {
                ChooseLang.M[171], ChooseLang.M[161], ChooseLang.M[162], ChooseLang.M[163], ChooseLang.M[164], ChooseLang.M[160]
        });//最大放电量、绝缘件异常放电、自由导体放电、浮游电极放电、尖端放电、 局放判断（总）

        blank_image = new String("iVBORw0KGgoAAAANSUhEUgAAAI0AAACCCAYAAAB7CEtwAAAAAXNSR0IArs4c6QAAAARnQU1BAACx"+
                "jwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAE8SURBVHhe7dIxAQAADMOg+TfdacgPGrhBJA2Z"+
                "NGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTS"+
                "kElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElD"+
                "Jg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2Z"+
                "NGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTS"+
                "kElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElDJg2ZNGTSkElD"+
                "Jg3R9j9kWky5SHsJAAAAAElFTkSuQmCC");

    }

    public void createDoc(boolean[] chcheck, String[] front, int[] maxnum,int[] prpsNum,int[] prpdNum, int[] maxdBm, int[][] maxResult, long[][] prpsdate,long[][] prpddata, int[][][] chresult,int reporttype, File file) {

        Map<String,Object> dataMap=new HashMap<String,Object>();
        getData(dataMap,chcheck,front,maxnum,prpsNum,prpdNum,maxdBm,maxResult,prpsdate,prpddata,chresult,reporttype,file);
        configuration.setClassForTemplateLoading(this.getClass(), "/template/");
        Template t=null;

        // ==========================================
        // 【合并特性】根据当前语言动态选择模板文件
        // ==========================================
        String templateName = "report_en.ftl"; // 默认使用英文模板（兼容日语、韩语等其他情况）

        // 如果是简体中文 (代码 4)，则使用中文模板
        if (ChooseLang.initlang == 4) {
            templateName = "report_cn.ftl";
        }

        try {
            // 加载动态选定的模板文件名
            t = configuration.getTemplate(templateName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //String savefile = file.getParent() + File.separator + "GisDocument.doc";
        String savefile = file.getParent() + File.separator + file.getName() + ".doc";
        //File outFile = new File("D:/temp/outFile.doc");
        Writer out = null;
        try {
            try {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(savefile),"UTF-8"));
            } catch (UnsupportedEncodingException e2) {
                e2.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        try {

            t.process(dataMap, out);
        } catch (TemplateException e3) {
            e3.printStackTrace();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e4) {
            e4.printStackTrace();
        }

    }

    public void deletePRPDPNG(boolean[] chcheck,int[] maxnum, int[] prpsNum,int[] prpdNum,File file)
    {
        for(int i = 1; i < 9; i++)
        {
            if(!chcheck[i-1])
                continue;

            int prpdnum;// = prpdNum[k];
            if(prpdNum[i-1] > maxnum[i-1] )
                prpdnum = prpdNum[i-1];
            else
                prpdnum = maxnum[i-1];

            String imgFile = file.getParent() + File.separator + "SUMCH"+ i +"PRPD.png";
            File nfile = new File(imgFile);
            if(nfile.exists())
            {
                nfile.delete();
            }

            for(int j = 0; j < 8; j++)
            {
                String imgdetailFile = file.getParent() + File.separator + "CH" + i + "PRPD" + j + ".png";
                File detailnfile = new File(imgdetailFile);
                if(detailnfile.exists())
                {
                    detailnfile.delete();
                }
            }
            //删除趋势图
            for(int j1 = 0; j1 < 5; j1++)
            {
                int trendch = i -1;
                String trendFile = file.getParent() + File.separator + "Trend"+ trendch +"_"+ j1 +".png";
                File trendnfile = new File(trendFile);
                if(trendnfile.exists())
                {
                    trendnfile.delete();
                }
            }
            //删除prps图
            for(int j2 = 0; j2 < prpsNum[i-1]; j2++)
            {
                int prpsch = i - 1;
                String prpsFile = file.getParent() + File.separator + "PRPS"+ prpsch +"_"+ j2 +".png";
                File prpsnfile = new File(prpsFile);
                if(prpsnfile.exists())
                {
                    prpsnfile.delete();
                }
            }
        }
    }

    private void getData(Map<String,Object> dataMap,boolean[] chcheck,String[] front,int[] maxnum,int[] prpsNum,int[] prpdNum,int[] maxdBm, int[][] maxResult, long[][] prpsdate, long[][] prpddate, int[][][] chresult,int reporttype, File file)
    {
        dataMap.put("date", front[0]);
        dataMap.put("place", front[1]);
        dataMap.put("point", front[2]);

        dataMap.put("ch1", front[3]);
        dataMap.put("ch2", front[4]);
        dataMap.put("ch3", front[5]);
        dataMap.put("ch4", front[6]);
        dataMap.put("ch5", front[7]);
        dataMap.put("ch6", front[8]);
        dataMap.put("ch7", front[9]);
        dataMap.put("ch8", front[10]);

        dataMap.put("person", front[HbDesktopPane.tongdao+3]);
        dataMap.put("printdate", front[HbDesktopPane.tongdao+4]);

        //dataMap.put("image", getImageStr());
        List<GisWordTableJudge> _table1=new ArrayList<GisWordTableJudge>();//总判定结果
        List<GisWordTableJudge> _table2=new ArrayList<GisWordTableJudge>();//趋势图
        List<GisWordTableJudge> _table3=new ArrayList<GisWordTableJudge>();//详细结果
        //判断结果
        for(int i = 0; i < 8; i++)
        {

            //imagestr[i] = getImageStr(file,i);
            if(!chcheck[i])
                continue;
            imagestr[i] = getImageStr(file,i+1,0,0,true);
            for(int j = 0; j < 5; j++)
            {
                if(j == 0)
                {
                    result[i][j] = graname[j] + (new StringBuilder()).append(": ").append(String.format("%02d", new Object[] {
                            Integer.valueOf(maxdBm[i])
                    })).append("dBm").toString();
                }
                else
                {
                    result[i][j] = graname[j] + (new StringBuilder()).append(String.format("%02d", new Object[] {
                            Integer.valueOf(maxResult[i][j-1])
                    })).append("%").toString();
                }
                System.out.println(result[i][j]);
            }

            GisWordTableJudge wordtable = new GisWordTableJudge();
            int ch = i + 1;
            String resmaxString = result[i][0];
            wordtable.setChannel("CH" + ch);
            wordtable.setImage(imagestr[i]);
            wordtable.setRes_max(resmaxString);//最大
            wordtable.setRes_inn(result[i][1]);//内部
            wordtable.setRes_gra(result[i][2]);//颗粒
            wordtable.setRes_sus(result[i][3]);//悬浮
            wordtable.setRes_sop(result[i][4]);//尖端

            // ============================================
            // 【合并特性】使用正则表达式提取数字，彻底修复非中文环境崩溃问题
            // ============================================
            String resmaxStringR3 = "0"; // 默认安全值
            try {
                // 正则表达式：匹配 -78 或 20 这样的整数
                java.util.regex.Pattern p = java.util.regex.Pattern.compile("-?\\d+");
                java.util.regex.Matcher m = p.matcher(resmaxString);

                if (m.find()) {
                    resmaxStringR3 = m.group(); // 提取纯数字部分
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Parsed Value: " + resmaxStringR3);
            // ============================================

            String ran2 = "正常";
            String dbm1 = "正常";

            if (Integer.valueOf(resmaxStringR3) >= -60 && Integer.valueOf(resmaxStringR3) < -50){
                Random r1 = new Random();
                Integer ran1 = 1 + r1.nextInt(11);
                wordtable.setNum4puase("放电次数：" + ran1.toString());
                wordtable.setAlarmdbm("放电幅值：0");
                if (ran1 >= 40)
                    ran2 = "异常";
                else
                    ran2 = "正常";
                dbm1="正常";
            }
            else if (Integer.valueOf(resmaxStringR3) >= -50 && Integer.valueOf(resmaxStringR3) < -40) {
                Random r1 = new Random();
                Integer ran1 = 11 + r1.nextInt(11);
                wordtable.setNum4puase("放电次数：" + ran1.toString());
                wordtable.setAlarmdbm("放电幅值：0");
                if (ran1 >= 40)
                    ran2 = "异常";
                else
                    ran2 = "正常";
                dbm1="正常";
            }
            else if (Integer.valueOf(resmaxStringR3) >= -40 && Integer.valueOf(resmaxStringR3) < -30) {
                Random r1 = new Random();
                Integer ran1 = 22 + r1.nextInt(11);
                wordtable.setNum4puase("放电次数：" + ran1.toString());
                wordtable.setAlarmdbm("放电幅值：0");
                if (ran1 >= 40)
                    ran2 = "异常";
                else
                    ran2 = "正常";
                dbm1="异常";
            }
            else if (Integer.valueOf(resmaxStringR3) >= -30 && Integer.valueOf(resmaxStringR3) < -20) {
                Random r1 = new Random();
                Integer ran1 = 33 + r1.nextInt(11);
                wordtable.setNum4puase("放电次数：" + ran1.toString());
                wordtable.setAlarmdbm("放电幅值：0");
                if (ran1 >= 40)
                    ran2 = "异常";
                else
                    ran2 = "正常";
                dbm1="异常";
            }
            else if (Integer.valueOf(resmaxStringR3) == -20) {
                Random r1 = new Random();
                Integer ran1 = 44 + r1.nextInt(11);
                wordtable.setNum4puase("放电次数：" + ran1.toString());
                wordtable.setAlarmdbm("放电幅值：0");
                if (ran1 >= 40)
                    ran2 = "异常";
                else
                    ran2 = "正常";
                dbm1="异常";
            }
            else
            {
                wordtable.setNum4puase("放电次数：0");
                wordtable.setAlarmdbm("放电幅值：0");
                ran2 = "正常";
                dbm1="正常";
            }

            wordtable.setNum4puaseA("放电次数报警："+ran2);
            wordtable.setAlarmdbmA("放电幅值报警："+dbm1);



            _table1.add(wordtable);
//			  System.out.println(ch+"通道   "+"getRes_max：   " + wordtable.getRes_max());
//			  System.out.println(ch+"通道   "+"getRes_inn：   " + wordtable.getRes_inn());
//			  System.out.println(ch+"通道   "+"getImage：   " + wordtable.getImage());

        }

        //趋势图
        for(int i1 = 0; i1 <  8; i1++)
        {
            if(!chcheck[i1])
                continue;
            int ch = i1 + 1;
            GisWordTableJudge wordtable = new GisWordTableJudge();
            for(int l1 = 0; l1 < 5; l1++)
            {
                imagetrend[i1][l1] = getImageStr(file,i1,l1,1,false);//获取趋势图数据
            }
            wordtable.setChannel("CH"+ ch);
            wordtable.setName_sum(graname[5]);
            wordtable.setName_inn(graname[1]);
            wordtable.setName_gra(graname[2]);
            wordtable.setName_sus(graname[3]);
            wordtable.setName_sop(graname[4]);
            wordtable.setImage_sum(imagetrend[i1][0]);
            wordtable.setImage_inn(imagetrend[i1][1]);
            wordtable.setImage_gra(imagetrend[i1][2]);
            wordtable.setImage_sus(imagetrend[i1][3]);
            wordtable.setImage_sop(imagetrend[i1][4]);
            _table2.add(wordtable);

            // }
        }
        //详细判断结果
        for(int k = 0; k <  8; k++)
        {
            if(!chcheck[k])
                continue;
            int num;
            int prpdnum;//prpd图谱数
            int prpsnum;//prps图谱数
            prpdnum = prpdNum[k];
            prpsnum = prpsNum[k];

            // ============================================
            // 【保留文件1特性】选中时间戳报告(reporttype==1)按PRPS选中条目逐条输出，避免后续数据覆盖前面条目
            // ============================================
            if(reporttype == 1) {
                num = prpsnum;
            } else if(prpsnum > prpdnum) {
                num = prpsnum;
            } else {
                num = maxnum[k];
            }
            int ch = k + 1;
            //结果
            for(int l = 0; l < num; l++)
            {
                GisWordTableJudge wordtable = new GisWordTableJudge();
                for(int m = 0; m < 5; m++)
                {

                    if(m == 0)
                    {
                        long detailTime;
                        if(reporttype == 1 || l < prpsnum) {
                            detailTime = prpsdate[k][l];
                        } else {
                            detailTime = prpddate[k][l];
                        }
                        resultdetail[k][l][m] = String.format("%tF %tT", new Object[] {
                                Long.valueOf(detailTime), Long.valueOf(detailTime)
                        });
                    }
                    else
                    {
                        resultdetail[k][l][m] = graname[m] + (new StringBuilder()).append(String.format("%02d", new Object[] {
                                Integer.valueOf(chresult[k][l][m-1])
                        })).append("%").toString();
                    }
                }

                wordtable.setChannel("CH" + ch);
                //imagedetail[k][l] = getImageStr(file,k+1,l,0,false);
                imageprpd[k][l] = getImageStr(file,k+1,l,0,false);//获取prpd图像data
                imageprps[k][l] = getImageStr(file,k,l,2,false);//获取prps图像data
                //wordtable.setImage(imagedetail[k][l]);
                wordtable.setImage_prpd(imageprpd[k][l]);
                wordtable.setImage_prps(imageprps[k][l]);
                wordtable.setDate(resultdetail[k][l][0]);//日期
                wordtable.setRes_inn(resultdetail[k][l][1]);//内部
                wordtable.setRes_gra(resultdetail[k][l][2]);//颗粒
                wordtable.setRes_sus(resultdetail[k][l][3]);//悬浮
                wordtable.setRes_sop(resultdetail[k][l][4]);//尖端
                _table3.add(wordtable);
            }
        }
        dataMap.put("table1", _table1);

        dataMap.put("table2", _table2);
        dataMap.put("table3", _table3);
    }

    private String getImageStr(File file, int i,int sum,int style,boolean totle)

    {

        String imgFile = "";
        byte[] data = null;
        switch(style)
        {
            case 0://prpd图
                if(totle)
                {
                    imgFile = file.getParent() + File.separator + "SUMCH"+ i +"PRPD.png";
                }
                else
                {
                    imgFile = file.getParent() + File.separator + "CH"+ i +"PRPD"+ sum +".png";
                }
                break;
            case 1://趋势图
                imgFile = file.getParent() + File.separator + "Trend"+ i +"_"+ sum +".png";
                break;
            case 2://prps图
                imgFile = file.getParent() + File.separator + "PRPS"+ i +"_"+ sum +".png";
                break;

        }

        InputStream in = null;
        File f = new File(imgFile);
        //System.out.println(i+":     "+f.exists());
        if(f.exists())
        {
            try {
                in = new FileInputStream(imgFile);
                data = new byte[in.available()];
                //data = imgFile.getBytes("UTF-8");

                in.read(data);
//				for (byte b : data) {
//					System.out.println(i+"        :"+b);
//
//				}

                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Decoder decoder = Base64.getDecoder();
            try {
                data = decoder.decode(blank_image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(data);

    }
}