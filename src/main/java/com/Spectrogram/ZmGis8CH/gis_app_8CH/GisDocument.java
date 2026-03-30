package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;

import javax.imageio.ImageIO;
import javax.swing.JComponent;


public class GisDocument extends JComponent implements Printable {

    int           maxResult[][];//最大判定结果
    GisWordReport rc;
    GisXYZ        gm;
    int           graphData[][][][];
    int           totalPRPD[][][];
    int           maxdBm[];
    int           pageNum;
    long          t[];
    int           gnum[][];
    long          prps2disp[][][];
    int           prpsvalue[][][];
    long          prpd2disp[][][];
    int           prpdvalue[][][];
    int           startIndex;
    public boolean negativePrint;
    GisGraph    prps[][];
    PRPDGraph   prpd[][];
    PRPDGraph   tprpd[];
    int         prpsNum[];
    int         prpdNum[];
    GisDenoiser denoiser;
    int         pageContent[];
    long        prpstime[][];
    long        prpdtime[][];
    int         prpsResult[][][];
    Color       chColor[];
    String      graphName[];

    File outFile;
    int  maxnum[];//最大条目数
    //选中的条目的数量
    int listSize = 0;

    private int toDbmFromPeak(int peakValue) {
        return (int) ((double) (peakValue * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double) GisSignal.calMin_dBm);
    }

    private double toDbmFromPeakDouble(int peakValue) {
        return (double) (peakValue * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double) GisSignal.calMin_dBm;
    }

    private int calcPrpsPeak(int[] prpsData) {
        int peak = 0;
        for (int i = 0; i < prpsData.length; i++) {
            if (prpsData[i] > peak) {
                peak = prpsData[i];
            }
        }
        return peak;
    }


    // 可重用的 BufferedImage 和 Graphics 对象
    private BufferedImage reusableImage;
    private Graphics2D reusableGraphics;
    private File nfile ;
    public GisDocument(GisWordReport giswordreport, File file) {
        totalPRPD = new int[8][64][64];
        maxResult = new int[8][4];
        maxdBm = new int[8];
        pageNum = 0;
        gnum = new int[8][4];
        prps2disp = new long[8][4][8];
        prpsvalue = new int[8][4][8];
        prpd2disp = new long[8][4][8];
        prpdvalue = new int[8][4][8];
        startIndex = 0;
        negativePrint = false;
        prps = new GisGraph[8][8];
        prpd = new PRPDGraph[8][8];
        tprpd = new PRPDGraph[8];
        prpsNum = new int[8];
        prpdNum = new int[8];
        denoiser = new GisDenoiser(8);
        pageContent = new int[26];
        prpstime = new long[8][8];
        prpdtime = new long[8][8];
        prpsResult = new int[8][8][4];
        maxnum = new int[8];//最大条目
        outFile = file;
        chColor = (new Color[]{
                Color.red, Color.black, Color.blue, Color.gray, Color.red, Color.black, Color.blue, Color.gray
        });
        graphName = (new String[]{
                ChooseLang.M[160], ChooseLang.M[161], ChooseLang.M[162], ChooseLang.M[163], ChooseLang.M[164]
        });//局放判断（总）、绝缘件异常放电、自由导体放电、浮游电极放电、尖端放电
        rc = giswordreport;
        //部分报告
        final int totalSize = giswordreport.site.evList.size();
        if (giswordreport.reportType == 1) {
            long l10 = giswordreport.startTime;
            long l14 = giswordreport.endTime;
            int i = 0;
            int l6 = 0;
            for (int i5 = totalSize; i < i5; i++) {
                final PRPSCell prpsCell = (PRPSCell) giswordreport.site.evList.get(i);
                if (prpsCell.isSelected()) {
                    l6++;//数据总条目
                }
//				long l16 = ((PRPSCell)(PRPSCell)giswordreport.site.evList.get(i)).time;
//				if (l16 >= l10 && l16 <= l14)
//					l6++;//数据总条目
            }
            listSize = l6;
//            prps2disp = new long[16][4][l6];
            prps2disp = new long[8][4][listSize];
            prpsvalue = new int[8][4][listSize];
            prpd2disp = new long[8][4][listSize];
            prpdvalue = new int[8][4][listSize];
            prps = new GisGraph[8][listSize];
            prpd = new PRPDGraph[8][listSize];
            prpstime = new long[8][listSize];
            prpdtime = new long[8][listSize];
            prpsResult = new int[8][listSize][4];
            graphData = new int[listSize][8][5][3];
            // 8 -> 8个通道 存放8个通道的数据
            int ai[][] = new int[8][5120];
            int ai1[] = new int[280];
            int denoised[][] = new int[8][5120];
            boolean denoiseFlags[] = {true, true, true, true, true, true, true, true};
            int ai2[] = new int[8];
            int ai3[] = new int[8];
            t = new long[l6];
            int ai5[][] = new int[8][5];
            i = 0;
            l6 = 0;
            PRPSCell prpsCell;
            for (int j5 = totalSize; i < j5; i++) {
                prpsCell = (PRPSCell) giswordreport.site.evList.get(i);
                long l17 = prpsCell.time;//PRPS列表所有时间数据
//				if (l17 < l10 || l17 > l14)
//					continue;
                if (!prpsCell.isSelected()) {
                    continue;
                }
                giswordreport.site.getEvent(l17, ai[0], ai[1], ai[2], ai[3], ai[4], ai[5], ai[6], ai[7], ai1);//获取时间列表中单条8通道数据
                t[l6] = l17;
                for (int ch = 0; ch < 8; ch++) {
                    denoiser.preDefinedOffset[ch] = ai1[ch + 8];
                }
                denoiser.findOffset = false;
                denoiser.gdProc(ai, denoised, null, denoiseFlags, denoiseFlags, 3, ai1, 79, true);
                for (int ch = 0; ch < 8; ch++) {
                    int peak = calcPrpsPeak(denoised[ch]);
                    int peakDbm = toDbmFromPeak(peak);
                    if (l6 == 0 || peakDbm > maxdBm[ch]) {
                        maxdBm[ch] = peakDbm;
                    }
                }
                //PRPD图像值
                for (int k7 = 0; k7 < 8; k7++) {
                    int k6 = 0;
                    ai2[k7] = ai1[k7];//相位
                    ai3[k7] = ai1[k7 + 8];//阀值（8通道下标与界面一致）
                    for (int i2 = 0; i2 < 4; i2++) {
                        int j9 = ai1[8 + k7 * 32 + i2];
                        int k9 = (j9 + ai5[k7][i2 + 1]) / 2;
                        graphData[l6][k7][i2 + 1][0] = k9;
                        graphData[l6][k7][i2 + 1][1] = k9;
                        graphData[l6][k7][i2 + 1][2] = k9;
                        ai5[k7][i2 + 1] = j9;
                        k6 += j9;
                    }

                    int l9 = (k6 + ai5[k7][0]) / 2;
                    graphData[l6][k7][0][0] = l9;
                    graphData[l6][k7][0][1] = l9;
                    graphData[l6][k7][0][2] = l9;
                    ai5[k7][0] = k6;
                }

                for (int l7 = 0; l7 < 8; l7++) {
                    int i4 = 0;
                    for (int j2 = 0; j2 < 5120; j2++) {

                        int l4 = ai[l7][j2];
                        if (i4 < l4)
                            i4 = l4;

                        int l3 = (ai[l7][j2] - ai3[l7]) / 64;
                        if (l3 > 63)
                            l3 = 63;
                        if (l3 >= 0)
                            totalPRPD[l7][j2 + ai2[l7] & 0x3f][l3]++;
                    }

                }

                l6++;
            }

            for (int i8 = 0; i8 < 8; i8++)
                // 打印通道选择：每个选中时间戳按顺序逐条写入，保持与界面一致
                if (giswordreport.check[i8]) {
                    for (int i11 = 0; i11 < 4; i11++) {
                        for (int j = 0; j < listSize; j++) {
                            prpsvalue[i8][i11][j] = graphData[j][i8][i11 + 1][0];
                            prps2disp[i8][i11][j] = t[j];
                        }
                    }
                }

        } else {
            int i10 = (int) ((giswordreport.endTime - giswordreport.startTime) / 60000L);
            t = new long[i10 + 1];
            GregorianCalendar gregoriancalendar = new GregorianCalendar();
            gregoriancalendar.setTimeInMillis(giswordreport.startTime);
            startIndex = gregoriancalendar.get(11) * 60 + gregoriancalendar.get(12);
            graphData = new int[i10][8][5][3];
            t[0] = giswordreport.startTime;
            for (int k = 0; k < i10; k++) {
                for (int l2 = 0; l2 < 8; l2++) {
                    int i7 = 0;
                    for (int j4 = 0; j4 < 4; j4++) {
                        for (int k5 = 0; k5 < 3; k5++) {
                            graphData[k][l2][j4 + 1][k5] = SiteData.loadedHistory[startIndex + k][l2][j4 + 5][k5];
                        }

                        i7 += graphData[k][l2][j4 + 1][0];
                    }

                    graphData[k][l2][0][0] = i7;
                    graphData[k][l2][0][1] = i7;
                    graphData[k][l2][0][2] = i7;
                }

                t[k + 1] = t[k] + 60000L;
            }

            for (int l = 0; l < i10; l++) {
                if (giswordreport.site.loadedPrpdPos[l + startIndex] <= 0)
                    continue;
                for (int j8 = 0; j8 < 8; j8++)
                    if (giswordreport.check[j8])
                        giswordreport.site.getPRPD(l + startIndex, j8, totalPRPD[j8]);

            }

            label0:
            for (int k8 = 0; k8 < 8; k8++) {
                if (!giswordreport.check[k8])
                    continue;
                int j10 = 0;
                do {
                    if (j10 >= 4)
                        continue label0;
                    for (int i1 = 1; i1 < i10; i1++) {
//						System.out.println("i1:"+i1+" k8:"+k8+" j10:"+j10);

                        if (graphData[i1][k8][j10 + 1][0] <= prpdvalue[k8][j10][0])
                            continue;
                        prpdvalue[k8][j10][0] = graphData[i1][k8][j10 + 1][0];
                        prpd2disp[k8][j10][0] = i1;
                        for (int i3 = 0; i3 < 7; i3++)
                            if (prpdvalue[k8][j10][i3] >= prpdvalue[k8][j10][i3 + 1]) {
                                int k4 = prpdvalue[k8][j10][i3];
                                prpdvalue[k8][j10][i3] = prpdvalue[k8][j10][i3 + 1];
                                prpdvalue[k8][j10][i3 + 1] = k4;
                                long l12 = prpd2disp[k8][j10][i3];
                                prpd2disp[k8][j10][i3] = prpd2disp[k8][j10][i3 + 1];
                                prpd2disp[k8][j10][i3 + 1] = l12;
                            }

                    }

                    j10++;
                } while (true);
            }

            //int ai4[][] = new int[4][5120];
            int ai6[] = new int[280];
            for (int j1 = 0; j1 < 8; j1++) {
                label1:
                for (int l8 = 0; l8 < 8; l8++) {
                    if (!giswordreport.check[l8])
                        continue;
                    int k10 = 0;
                    do {
                        if (k10 >= 4)
                            continue label1;
                        long l15 = t[(int) prpd2disp[l8][k10][j1]];
                        long l13 = l15 - 60000L;
                        int j3 = 0;
                        for (int l5 = totalSize; j3 < l5; j3++) {
                            long l18 = ((PRPSCell) (PRPSCell) giswordreport.site.evList.get(j3)).time;
//							if (l18 >= l13 && l18 <= l15)
                            if (l18 >= l13 && l18 <= l15) {
                                giswordreport.site.getEventValue(l18, ai6);
                                int j7 = ai6[8 + l8 * 32 + k10];
                                if (j7 >= prpsvalue[l8][k10][j1]) {
                                    prps2disp[l8][k10][j1] = l18;
                                    prpsvalue[l8][k10][j1] = j7;
                                    // memo 20230822 待测试
                                    double i4 = (double) (j7 * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095.0D + (double) GisSignal.calMin_dBm;
                                    maxdBm[l8] = (int) i4;
                                }
                            }
                            if (l18 > l15)
                                j3 = l5;
                        }

                        k10++;
                    } while (true);
                }

            }

        }
        for (int k1 = 0; k1 < 8; k1++) {
            for (int i6 = 0; i6 < 4; i6++) {
                int k3 = 0;
                for (int l4 = graphData.length; k3 < l4; k3++) {
                    if (maxResult[k1][i6] >= graphData[k3][k1][i6 + 1][0])
                        continue;
                    maxResult[k1][i6] = graphData[k3][k1][i6 + 1][0];
                    if (maxResult[k1][i6] > 99)
                        maxResult[k1][i6] = 99;
                }

            }

        }

        for (int i9 = 0; i9 < 8; i9++) {
            int j6 = maxResult[i9][0] + maxResult[i9][1] + maxResult[i9][2] + maxResult[i9][3];
            if (j6 == 0)
                j6 = 1;
            System.out.print((new StringBuilder()).append("ch").append(i9 + 1).append(" : ").toString());
            for (int l1 = 0; l1 < 4; l1++) {
                gnum[i9][l1] = (int) ((8D * (double) maxResult[i9][l1]) / (double) j6 + 0.5D);
                System.out.print((new StringBuilder()).append("gnum : ").append(gnum[i9][l1]).append(" ").toString());
            }

            System.out.println();
        }

        if (giswordreport.reportType != 1) ;
        System.out.println("Search colplite. loading graph");
        initGraph();
        System.out.println("done.");
        giswordreport.gppage = 0;
        giswordreport.gtpage = pageNum;
    }

    void initGraph() {
        int ai[][] = new int[8][5120];
        int ai2[] = new int[280];
        for (int j2 = 0; j2 < 8; j2++)
            tprpd[j2] = new PRPDGraph(64, 64, totalPRPD[j2], null);

        if (rc.reportType == 0) {
            for (int k2 = 0; k2 < 8; k2++) {
                if (!rc.check[k2])
                    continue;
                int l1 = 0;
                System.out.print((new StringBuilder()).append("Ch").append(k2 + 1).append(" : ").toString());
                for (int i3 = 0; i3 < 4; i3++) {
                    for (int i = 0; i < gnum[k2][i3]; i++)
                        if (prpd2disp[k2][i3][7 - i] > 0L) {
                            if (l1 < 8) {
                                int ai3[][] = new int[64][64];
                                rc.site.getPRPD((int) prpd2disp[k2][i3][7 - i] + startIndex, k2, ai3);
                                prpdtime[k2][l1] = t[(int) prpd2disp[k2][i3][7 - i]];
                                prpd[k2][l1++] = new PRPDGraph(64, 64, ai3, null);
                            }
                        } else {
                            System.out.print((new StringBuilder()).append("<").append(prpd2disp[k2][i3][7 - i]).append(">").toString());
                        }

                    System.out.print((new StringBuilder()).append(l1).append(" ").toString());
                }

                prpdNum[k2] = l1;
                System.out.println();
            }

        }
        //int ai4[][] = new int[4][];
        boolean aflag[] = {
                true, true, true, true, true, true, true, false
        };
        for (int l2 = 0; l2 < 8; l2++) {
            if (!rc.check[l2])
                continue;
            int i2 = 0;
            // 选中时间戳报告按“每个时间戳一条PRPS”组织，避免同一时间重复4次
            for (int j3 = 0; j3 < 1; j3++) {
                for (int j = 0; j < listSize; j++)
//                    if (prps2disp[l2][j3][(listSize-1) - j] > 0L) {
                    if (prps2disp[l2][j3][listSize -1 - j] > 0L) {
                        if (i2 >= listSize)
                            continue;
                        rc.site.getEvent(prps2disp[l2][j3][listSize -1 - j], ai[0], ai[1], ai[2], ai[3], ai[4], ai[5], ai[6], ai[7], ai2);
                        int ai1[][] = new int[8][5120];
                        for (int k1 = 0; k1 < 8; k1++)
                            denoiser.preDefinedOffset[k1] = ai2[k1 + 8];
                        denoiser.findOffset = false;
                        denoiser.gdProc(ai, ai1, null, aflag, aflag, 3, ai2, 79, true);
                        prps[l2][i2] = new GisGraph(64, 80, ai1[l2], null, 5120);
                        prps[l2][i2].phaseDelay = ai2[l2];
                        prps[l2][i2].mindBm = (int) (denoiser.offset_dBm[l2] - 0.5D);
                        prps[l2][i2].maxdBm = GisSignal.calMax_dBm;
                        prps[l2][i2].amxdBm = toDbmFromPeakDouble(calcPrpsPeak(ai1[l2]));
                        prps[l2][i2].stringToDraw = "";
                        prpstime[l2][i2] = prps2disp[l2][j3][listSize -1 - j];
                        for (int k3 = 0; k3 < 4; k3++)
                            prpsResult[l2][i2][k3] = ai2[8 + l2 * 32 + k3];

                        if (rc.printBlackWhite)
                            prps[l2][i2].printBlackWhite = Boolean.valueOf(true);
                        else
                            prps[l2][i2].printBlackWhite = Boolean.valueOf(false);
                        i2++;
                    } else {
                        System.out.print((new StringBuilder()).append("<").append(prps2disp[l2][j3][listSize -1 - j]).append(">").toString());
                    }

            }

            prpsNum[l2] = i2;
            System.out.println((new StringBuilder()).append("prpsnum ch").append(l2 + 1).append(" : ").append(i2).toString());
        }

        pageNum = 0;
        if (rc.drawSeperate) {
            int k = 0;
            pageContent[k++] = 10;
            for (int i1 = 0; i1 < 8; i1++) {
                if (!rc.check[i1])
                    continue;
                pageContent[k++] = i1;
                if (prpsNum[i1] > 0)
                    pageContent[k++] = 100 + i1 * 10;
//				if (prpsNum[i1] > 4)
//					pageContent[k++] = 100 + i1 * 10 + 1;
            }

            pageNum = k;
        } else {
            int l = 0;
            pageContent[l++] = 10;
            pageContent[l++] = 11;
            for (int j1 = 0; j1 < 8; j1++) {
                if (!rc.check[0])
                    continue;
                if (prpsNum[j1] > 0)
                    pageContent[l++] = 100 + j1 * 10;
//				if (prpsNum[j1] > 4)
//					pageContent[l++] = 100 + j1 * 10 + 1;
            }

            pageNum = 6;
        }
    }


    @Override
    public int print(Graphics g, PageFormat pageformat, int i) throws PrinterException {
        int j = (int) pageformat.getImageableWidth();//返回页面可成像区域的宽度
        int k = (int) pageformat.getImageableHeight();//高度
        int l = (int) pageformat.getImageableX();//返回与此pageformat相关的paper对象的可成像区域左上方点的x坐标
        int i1 = (int) pageformat.getImageableY();//y坐标
        return print(g, j, k, l, i1, i);
    }

    public int print(Graphics g, int i, int j, int k, int l, int i1) {
        rc.gppage = i1;
        if (i1 >= pageNum) {
            rc.gtpage = -2;
            return 1;
        }
//		System.out.println((new StringBuilder()).append("Report page ").append(i1).toString());
//		System.out.println((new StringBuilder()).append(k).append(" ").append(l).append(" ").append(i).append(" ").append(j).toString());
        switch (pageContent[i1]) {
            case 0: // '\0'
                drawPage1sep((Graphics2D) g, k, l, i, j, 0);
                break;

            case 1: // '\001'
                drawPage1sep((Graphics2D) g, k, l, i, j, 1);
                break;

            case 2: // '\002'
                drawPage1sep((Graphics2D) g, k, l, i, j, 2);
                break;

            case 3: // '\003'
                drawPage1sep((Graphics2D) g, k, l, i, j, 3);
                break;

            case 4: // '\0'
                drawPage1sep((Graphics2D) g, k, l, i, j, 4);
                break;

            case 5: // '\001'
                drawPage1sep((Graphics2D) g, k, l, i, j, 5);
                break;

            case 6: // '\002'
                drawPage1sep((Graphics2D) g, k, l, i, j, 6);
                break;

            case 7: // '\003'
                drawPage1sep((Graphics2D) g, k, l, i, j, 7);
                break;

            case 10: // '\n'
                drawPage0((Graphics2D) g, k, l, i, j);
                break;

            case 11: // '\013'
                drawPage1((Graphics2D) g, k, l, i, j);
                break;

            case 100: // 'd'
            case 101: // 'e'
            case 110: // 'n'
            case 111: // 'o'
            case 120: // 'x'
            case 121: // 'y'
            case 130:
            case 131:
            case 140: // 'd'
            case 141: // 'e'
            case 150: // 'n'
            case 151: // 'o'
            case 160: // 'x'
            case 161: // 'y'
            case 170:
            case 171:
                drawPage2((Graphics2D) g, k, l, i, j, (pageContent[i1] - 100) / 10, (pageContent[i1] - 100) % 10);
                break;
        }
        if (negativePrint) {
            g.setColor(Color.black);
            g.setXORMode(Color.white);
            g.fillRect(k, l, i, j);
        }
        g.dispose();
        return 0;
    }

    void drawPage1(Graphics2D graphics2d, int i, int j, int k, int l) {
        l -= k / 10;
        int l1 = l / 6;
        int i2 = l / 5;
        float f;
        graphics2d.setFont(graphics2d.getFont().deriveFont(f = (float) k / 50F));
        graphics2d.setPaint(Color.gray);
        graphics2d.drawLine(i, j + (int) (f * 1.4F), i + k, j + (int) (f * 1.4F));
        int i1 = 0;
        int k1 = 0;
        for (; i1 < 8; i1++)
            if (rc.check[i1]) {
                graphics2d.setPaint(chColor[i1]);
                graphics2d.drawString((new StringBuilder()).append("Ch").append(i1 + 1).append(" : ").append(rc.subLocation[i1]).toString(), (float) i + f * 1.5F + (float) ((k / 2) * (k1 & 1)), (float) j + f * 1.5F * (float) (k1 / 2 + 2));
                k1++;
            }

        graphics2d.setPaint(Color.black);
        graphics2d.drawString((new StringBuilder()).append(rc.location).append(" / ").append(String.format("%tF %tT ~ %tF %tT", new Object[]{
                Long.valueOf(rc.startTime), Long.valueOf(rc.startTime), Long.valueOf(rc.endTime), Long.valueOf(rc.endTime)
        })).toString(), (float) i + f, (float) j + f);
        for (int j1 = 0; j1 < 5; j1++) {
            graphics2d.drawString(graphName[j1], (float) i + f, ((float) ((j + i2 * j1 + i2) - l1) - f / 2.0F) + f * 5F);
            Graphics2D graphics2d1 = (Graphics2D) (Graphics2D) graphics2d.create(i, (int) ((float) ((j + i2 * j1 + i2) - l1) + f * 5F), k, l1);
            drawTrendGraph(graphics2d1, k, l1, j1);

        }

    }

    void drawPage1sep(Graphics2D graphics2d, int i, int j, int k, int l, int i1) {
        l -= k / 10;
        int l1 = l / 6;
        int i2 = l / 5;
        float f;
        graphics2d.setFont(graphics2d.getFont().deriveFont(f = (float) k / 50F));
        graphics2d.setPaint(Color.gray);
        graphics2d.drawLine(i, j + (int) (f * 1.4F), i + k, j + (int) (f * 1.4F));
        graphics2d.setPaint(chColor[i1]);
        int k1 = 0;
        //通道
        graphics2d.drawString((new StringBuilder()).append("Ch").append(i1 + 1).append(" : ").toString(), (float) i + f * 1.5F + (float) ((k / 2) * (k1 & 1)), (float) j + f * 1.5F * (float) (k1 / 2 + 2));
        graphics2d.setPaint(Color.black);
        graphics2d.drawString((new StringBuilder()).append(rc.location).append(" / ").append(String.format("%tF %tT ~ %tF %tT", new Object[]{
                Long.valueOf(rc.startTime), Long.valueOf(rc.startTime), Long.valueOf(rc.endTime), Long.valueOf(rc.endTime)
        })).toString(), (float) i + f, (float) j + f);//设置页眉
        for (int j1 = 0; j1 < 5; j1++) {
            //趋势图
            graphics2d.drawString(graphName[j1], (float) i + f, ((float) ((j + i2 * j1 + i2) - l1) - f / 2.0F) + f * 5F);
            Graphics2D graphics2d1 = (Graphics2D) (Graphics2D) graphics2d.create(i, (int) ((float) ((j + i2 * j1 + i2) - l1) + f * 5F), k, l1);
            drawTrendGraphBox(graphics2d1, k, l1);
            GisTrendGraph.drawTrendGraph(graphics2d1, i1, j1, 5, 0, chColor[i1], k, l1, graphData);
            //png文件
            savetrendPng(outFile, i1, j1);
        }

    }

    public void drawTrendGraph(Graphics2D graphics2d, int i, int j, int k) {
        drawTrendGraphBox(graphics2d, i, j);
        for (int l = 0; l < 8; l++)
            if (rc.check[l])
                GisTrendGraph.drawTrendGraph(graphics2d, l, k, 5, 0, chColor[l], i, j, graphData);

    }

    public void drawTrendGraphBox(Graphics2D graphics2d, int i, int j) {
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2d.setStroke(new BasicStroke(1.0F, 1, 1));
        graphics2d.setPaint(Color.white);
        graphics2d.fillRect(0, 0, i, j);
        graphics2d.setPaint(Color.lightGray);
        graphics2d.drawRect(0, 0, i - 1, j - 1);
        Color color = new Color(224, 224, 224);
        graphics2d.setFont(graphics2d.getFont().deriveFont((float) i / 75F));
        GregorianCalendar gregoriancalendar = new GregorianCalendar();
        int l1 = 0;
        //boolean flag = false;
        try {
            gregoriancalendar.setTimeInMillis(t[0]);
        } catch (Exception exception) {
        }
        l1 = gregoriancalendar.get(11);
        float f = (float) i / (float) t.length;
        int k = 0;
        for (int k1 = t.length; k < k1; k++) {
            gregoriancalendar.setTimeInMillis(t[k]);
            int i2 = gregoriancalendar.get(11);
            if (l1 != i2) {
                int i1 = (int) (f * (float) k);
                graphics2d.setPaint(color);
                graphics2d.drawLine(i1, 0, i1, j);
                graphics2d.setPaint(Color.lightGray);
                graphics2d.drawString(String.format("%02d", new Object[]{
                        Integer.valueOf(i2)
                }), (float) i1 + (float) i / 400F, (float) i / 75F);
                l1 = i2;
            }
        }

        f = (float) j / 100F;
        for (int l = 25; l < 100; l += 25) {
            int j1 = (int) (f * (float) l);
            graphics2d.setPaint(color);
            graphics2d.drawLine(0, j1, i, j1);
            graphics2d.setPaint(Color.lightGray);
            graphics2d.drawString((new StringBuilder()).append(String.format("%3d", new Object[]{
                    Integer.valueOf(100 - l)
            })).append("%").toString(), (float) i / 400F, (float) j1 + (float) i / 75F);
        }

    }

    public void drawTrendGraphpng(Graphics2D graphics2d, int i, int j, int l, int k) {
        drawTrendGraphBox(graphics2d, i, j);
        //for (int l = 0; l < 8; l++)
        if (rc.check[l])
            GisTrendGraph.drawTrendGraph(graphics2d, l, k, 5, 0, chColor[l], i, j, graphData);

    }

    public void drawTrendbuffer(Graphics g, int i, int j, int k, int l, int l1, int l2) {
        g.setColor(Color.black);
        g.fillRect(k, l, i, j);

        BufferedImage bufferedimage = new BufferedImage(i, j, 1);
        Graphics g1 = bufferedimage.getGraphics();
        drawTrendGraphpng((Graphics2D) g1, i, j, l1, l2);
        g.drawImage(bufferedimage, k, l, null);
    }

    //趋势图png文件
    public void savetrendPng(File file, int il, int j1) {
        String pathfile = file.getParent() + File.separator + "Trend" + il + "_" + j1 + ".png";
        File nfile = new File(pathfile);
        BufferedImage bufferedimage = new BufferedImage(630, 150, 1);
        drawTrendbuffer(bufferedimage.getGraphics(), 630, 150, 0, 0, il, j1);
        try {
            ImageIO.write(bufferedimage, "PNG", nfile);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void drawPRPSGraphpng(Graphics2D graphics2d, int i, int j, int k1, int l, int k) {
        //drawTrendGraphBox(graphics2d, i, j);
        //for (int l = 0; l < 8; l++)
        int i3 = k1 + 1;
        if (rc.check[l])
        //GisTrendGraph.drawTrendGraph(graphics2d, l, k, 5, 0, chColor[l], i, j, graphData);
        {
            if (rc.print3DPRPS) {
                prps[l][k].setAngle(i3 - 2, i3, 1, 1);
                prps[l][k].redraw3D(graphics2d, i3, i3, 1, 1);
                graphics2d.drawRect(0, 0, i3 - 2, i3);
            } else {
                System.arraycopy(prps[l][k].pdData, 5056, prps[l][k].columdata, 0, 64);//复制最新一组数据到柱形显示图
                prps[l][k].redraw2D_Colum(graphics2d, i3, i3, 1, 1);
                graphics2d.drawRect(0, 0, i3, i3);
            }
        }

    }

    public void drawPRPSbuffer(Graphics g, int i, int j, int k, int l, int l1, int l2) {
        g.setColor(Color.black);
        g.fillRect(k, l, i - 1, j);

//        BufferedImage bufferedimage = new BufferedImage(i, j, 1);
//        Graphics g1 = bufferedimage.getGraphics();
        // 重用 BufferedImage 和 Graphics 对象
        if (reusableImage == null || reusableImage.getWidth() != i || reusableImage.getHeight() != j) {
            if (reusableGraphics != null) {
                reusableGraphics.dispose(); // 释放旧的 Graphics 对象
            }
            reusableImage = new BufferedImage(i, j, BufferedImage.TYPE_INT_RGB);
            reusableGraphics = reusableImage.createGraphics();
        }

        drawPRPSGraphpng(reusableGraphics, i, j, j, l1, l2);
        g.drawImage(reusableImage, k, l, null);
    }

    public void savePRPSPng(File file, int i3, int il, int j1) {
        String pathfile = file.getParent() + File.separator + "PRPS" + il + "_" + j1 + ".png";
        nfile = new File(pathfile);
//        bufferedimage = new BufferedImage(i3, i3, 1);
        // 如果 reusableImage 为 null 或者大小不匹配，则创建新的 BufferedImage
        // 初始化或重用 BufferedImage
        if (reusableImage == null || reusableImage.getWidth() != i3 || reusableImage.getHeight() != i3) {
            if (reusableGraphics != null) {
                reusableGraphics.dispose(); // 释放旧的 Graphics 对象
            }
            reusableImage = new BufferedImage(i3, i3, BufferedImage.TYPE_INT_RGB);
            reusableGraphics = reusableImage.createGraphics();
        }
        // 绘制内容
        drawPRPSbuffer(reusableGraphics, i3, i3, 0, 0, il, j1);
        try {
            ImageIO.write(reusableImage, "PNG", nfile);
        } catch (Exception exception) {
            exception.printStackTrace();
        }finally {
            // 显式释放资源
            reusableImage.flush();
        }
    }


    // 释放资源的方法
    public void cleanup() {
        if (reusableGraphics != null) {
            reusableGraphics.dispose();
        }
        if (reusableImage != null) {
            reusableImage.flush();
        }
        reusableImage = null;
        reusableGraphics = null;
    }

    void drawPage0(Graphics2D graphics2d, int i, int j, int k, int l)//设置第0页
    {
        graphics2d.setPaint(Color.black);
        graphics2d.setFont(graphics2d.getFont().deriveFont((float) k / 15F));
        graphics2d.drawString(ChooseLang.M[165], i + k / 5, j + k / 14);//GIS诊断报告
        float f;
        graphics2d.setFont(graphics2d.getFont().deriveFont(f = (float) k / 35F));
        int i1 = 0;
        graphics2d.drawString(ChooseLang.M[166], i, j + l / 6 + (k / 15) * i1++);//检测日期
        graphics2d.drawString(ChooseLang.M[143], i, j + l / 6 + (k / 15) * i1++);//检测地点
        graphics2d.drawString(ChooseLang.M[167], i, j + l / 6 + (k / 15) * i1);//检测点位置
        for (int k1 = 0; k1 < 8; k1++)
            if (rc.check[k1])
                i1++;

        graphics2d.drawString(ChooseLang.M[168], i, j + l / 6 + (k / 15) * i1++);//检测人
        graphics2d.drawString(ChooseLang.M[169], i, j + l / 6 + (k / 15) * i1++);//打印日期
        //graphics2d.drawString(ChooseLang.M[170], i, j + l / 6 + (k / 15) * i1++);//图像文件
        i1 = 0;
        graphics2d.drawString(":", i + k / 6, j + l / 6 + (k / 15) * i1++);
        graphics2d.drawString(":", i + k / 6, j + l / 6 + (k / 15) * i1++);
        graphics2d.drawString(":", i + k / 6, j + l / 6 + (k / 15) * i1);
        for (int l1 = 0; l1 < 8; l1++)
            if (rc.check[l1])
                i1++;

        graphics2d.drawString(":", i + k / 6, j + l / 6 + (k / 15) * i1++);
        graphics2d.drawString(":", i + k / 6, j + l / 6 + (k / 15) * i1++);
        graphics2d.drawString(":", i + k / 6, j + l / 6 + (k / 15) * i1++);
        i1 = 0;
        graphics2d.drawString(String.format("%tF %tT ~ %tF %tT", new Object[]{
                Long.valueOf(rc.startTime), Long.valueOf(rc.startTime), Long.valueOf(rc.endTime), Long.valueOf(rc.endTime)
        }), i + k / 4, j + l / 6 + (k / 15) * i1++);//检测日期
        graphics2d.drawString(rc.location, i + k / 4, j + l / 6 + (k / 15) * i1++);//检测地点
        for (int i2 = 0; i2 < HbDesktopPane.tongdao; i2++)
            if (rc.check[i2])
                graphics2d.drawString((new StringBuilder()).append("Ch").append(i2 + 1).append(" : ").append(rc.subLocation[i2]).toString(), i + k / 4, j + l / 6 + (k / 15) * i1++);//通道

        graphics2d.drawString(rc.userName, i + k / 4, j + l / 6 + (k / 15) * i1++);//检测人
        graphics2d.drawString(String.format("%tF %tT", new Object[]{
                Long.valueOf(System.currentTimeMillis()), Long.valueOf(System.currentTimeMillis())
        }), i + k / 4, j + l / 6 + (k / 15) * i1++);//打印日期
        //graphics2d.drawString(rc.memo, i + k / 4, j + l / 6 + (k / 15) * i1++);//图像文件
        graphics2d.setFont(graphics2d.getFont().deriveFont(f = (float) k / 40F));
        int k2 = 0;
        int j3 = k / 4;
        label0:
        for (int k3 = 0; k3 < 8; k3++) {
            //int k4=0;
            int j1 = 3;
            if (!rc.check[k3])
                continue;
            int l2 = ((k2 & 1) * k) / 2 + i;
            int i3 = ((k2 >> 1) * k) / 3 + (int) ((double) l * 0.65000000000000002D);
            Graphics2D graphics2d1 = (Graphics2D) (Graphics2D) graphics2d.create(l2, i3, k / 2, k / 2);
            drawPRPD(graphics2d1, 1, (int) f * 2 + 1, j3, j3, totalPRPD[k3], k3, 0, outFile, true);

            graphics2d1.setPaint(Color.black);
            graphics2d1.drawRect(0, (int) f * 2, j3 + 1, j3 + 1);
            graphics2d1.drawString((new StringBuilder()).append("Ch").append(k3 + 1).toString(), 0, (int) ((double) f * 1.5D));

            k2++;
            graphics2d1.drawString(ChooseLang.M[171], (float) j3 + f * 0.5F, f * ((float) j1 - 0.5F) * 1.5F);//最大检测值

            graphics2d1.drawString((new StringBuilder()).append(": ").append(String.format("%02d", new Object[]{
                    Integer.valueOf(maxdBm[k3])
            })).append("dBm").toString(), (float) j3 + f + 70, f * ((float) j1 - 0.5F) * 1.5F);
            j1++;
            int j2 = 0;
            do {
                if (j2 >= 4)
                    continue label0;
                if (k3 == 8 && GisExplorer.boolNoise)//如果第四通道时噪音k3==3
                {
                    if (j2 == 0)
                        graphics2d1.drawString((new StringBuilder()).append("    ").append(ChooseLang.M[259]).toString(), j3, f * (float) j1 * 1.5F);//外界噪声
                    else
                        graphics2d1.drawString(" ", j3, f * (float) j1 * 1.5F);
                } else {
                    graphics2d1.drawString((new StringBuilder()).append(" ").append(graphName[j2 + 1]).toString(), j3, f * (float) j1 * 1.5F);
                    graphics2d1.drawString((new StringBuilder()).append(String.format("%02d", new Object[]{
                            Integer.valueOf(maxResult[k3][j2])
                    })).append("%").toString(), (float) j3 + f * 6F + 20, f * (float) j1 * 1.5F);
                }
                j1++;
                j2++;
            } while (true);
        }

    }

    void drawPRPD(Graphics2D graphics2d, int i, int j, int k, int l, int ai[][], int ch, int sum, File file, boolean flag) {
        int ai1[] = new int[4096];
        int k2 = 0;
        for (int i1 = 0; i1 < 64; i1++) {
            for (int l1 = 0; l1 < 64; l1++)
                if (ai[i1][l1] > k2)
                    k2 = ai[i1][l1];

        }

        int l3 = k2;
        if (l3 == 0)
            l3 = 1;
        if (rc.printBlackWhite) {
            int l2 = 4096;
            for (int j1 = 0; j1 < 64; j1++) {
                for (int i2 = 63; i2 >= 0; i2--) {
                    int i4 = ai[i2][j1];
                    if (i4 < 0)
                        i4 = 0;
                    int j3 = (i4 * 256) / l3;
                    if (j3 > 255)
                        j3 = 255;
                    j3 = 255 - j3;
                    ai1[--l2] = j3 << 16 | j3 << 8 | j3;
                }

            }

        } else {
            int i3 = 4096;
            for (int k1 = 0; k1 < 64; k1++) {
                for (int j2 = 63; j2 >= 0; j2--) {
                    int j4 = ai[j2][k1];
                    if (j4 < 0)
                        j4 = 0;
                    int k3 = (j4 * 4095) / l3;
                    if (k3 > 4095)
                        k3 = 4095;
                    ai1[--i3] = tprpd[0].d3p.zColor(k3);
                }

            }

        }
        BufferedImage bufferedimage = new BufferedImage(64, 64, 1);
        bufferedimage.setRGB(0, 0, 64, 64, ai1, 0, 64);
        graphics2d.setPaint(Color.white);
        graphics2d.fillRect(0, 0, k, l);
        graphics2d.drawImage(bufferedimage, i, j, k, l, Color.black, null);
        int sumch = ch + 1;
        String name = "";
        if (flag) {
            name = file.getParent() + File.separator + "SUMCH" + sumch + "PRPD.png";
        } else {
            name = file.getParent() + File.separator + "CH" + sumch + "PRPD";
        }
        try {
            //memo 20230901 ImageIO.write
            if (flag) {
                ImageIO.write(bufferedimage, "PNG", new File(name));
            } else {
                ImageIO.write(bufferedimage, "PNG", new File(name + String.format("%d.png", new Object[]{Integer.valueOf(sum)})));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //byte[] data = ((DataBufferByte)bufferedimage.getData().getDataBuffer()).getData();//获取BufferedImage的byte[]
        bufferedimage = null;
        ai1 = null;
    }

    void drawPage2(Graphics2D graphics2d, int i, int j, int k, int l, int i1, int j1) {
        j1 *= 4;
        l -= k / 10;
        //int k2 = l / 6;
        //int l2 = l / 5;
        float f;
        graphics2d.setFont(graphics2d.getFont().deriveFont(f = (float) k / 50F));
        graphics2d.setPaint(Color.gray);
        graphics2d.drawLine(i, j + (int) (f * 1.4F), i + k, j + (int) (f * 1.4F));
        graphics2d.setPaint(chColor[i1]);
        int l1 = 0;
        graphics2d.drawString((new StringBuilder()).append("Ch").append(i1 + 1).append(" : ").append(rc.subLocation[i1]).toString(), (float) i + f * 1.5F + (float) ((k / 2) * (l1 & 1)), (float) j + f * 1.5F * (float) (l1 / 2 + 2));
        graphics2d.setPaint(Color.black);
        graphics2d.drawString((new StringBuilder()).append(rc.location).append(" / ").append(String.format("%tF %tT ~ %tF %tT", new Object[]{
                Long.valueOf(rc.startTime), Long.valueOf(rc.startTime), Long.valueOf(rc.endTime), Long.valueOf(rc.endTime)
        })).toString(), (float) i + f, (float) j + f);
        int i3 = (int) ((float) k / 3.3F);
        int j3 = 0;
        System.out.println((new StringBuilder()).append("prpdnum : ").append(prpdNum[i1]).toString());
        graphics2d.setPaint(Color.gray);
        for (int k3 = 0; k3 < 3; k3++) {
            int i2 = (int) ((float) (j + ((k3 + 1) * k) / 3) + 3F * f);
            graphics2d.drawLine(i, i2, i + k, i2);
        }

        int l3;
        if (prpdNum[i1] < prpsNum[i1])
            l3 = prpsNum[i1];
        else
            l3 = prpdNum[i1];
        j1--;
//		for (int k1 = 0; k1 < 4; k1++)           //memo 20230901 测试
        BufferedImage bufferedimage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);;
        for (int k1 = 0; k1 < listSize; k1++)           //memo 20230901 测试
        {

            if (++j1 >= l3)
                continue;

            Graphics2D graphics2d1 = (Graphics2D) (Graphics2D) graphics2d.create(i, (int) ((float) (j + (j3 * k) / 3) + 3.7F * f), k, k / 3);
            graphics2d1.setPaint(Color.gray);
            maxnum[i1] = j1;//总条目
            if (prpdNum[i1] > j1) {
                drawPRPD(graphics2d1, (int) ((float) k / 2.7F) + 1, 1, i3, i3, prpd[i1][j1].prpd, i1, j1, outFile, false);
                graphics2d1.setPaint(Color.gray);
                graphics2d1.drawRect((int) ((float) k / 2.7F), 0, i3 + 1, i3 + 1);
            }
            if (prpsNum[i1] > j1) {    //PRPS.png
                savePRPSPng(outFile, i3, i1, j1);
                //清理下内存
                this.cleanup();
                if (rc.print3DPRPS) {
                    prps[i1][j1].setAngle((int) ((float) i3 * 1.1F), i3, 1, 1);
                    prps[i1][j1].redraw3D(graphics2d1, (int) ((float) i3 * 1.1F), i3, 1, 1);
                    graphics2d1.drawRect(0, 0, (int) ((float) i3 * 1.1F) + 1, i3 + 1);


                } else {
                    System.arraycopy(prps[i1][j1].pdData, 5056, prps[i1][j1].columdata, 0, 64);//复制最新一组数据到柱形显示图
                    //prps[i1][j1].redraw2D(graphics2d1, i3, i3, 1, 1);
                    prps[i1][j1].redraw2D_Colum(graphics2d1, i3, i3, 1, 1);
                    graphics2d1.drawRect(0, 0, i3 + 1, i3 + 1);
                }
            }
            graphics2d1.setPaint(Color.black);
            int i4 = 1;
            for (int j2 = 0; j2 < 4; j2++) {
                if (i1 == 8 && GisExplorer.boolNoise)//如果第4通道时噪音i1==3
                {
                    if (j2 == 0)
                        graphics2d1.drawString((new StringBuilder()).append("    ").append(ChooseLang.M[259]).toString(), (float) i3 * 2.4F, f * 1.5F * (1.2F + (float) (i4 + 1)));
                    else
                        graphics2d1.drawString(" ", (float) i3 * 2.4F, f * 1.5F * (1.2F + (float) (i4 + 1)));
                } else {
                    graphics2d1.drawString(graphName[j2 + 1], (float) i3 * 2.4F, f * 1.5F * (1.2F + (float) (i4 + 1)));
                    graphics2d1.drawString((new StringBuilder()).append(String.format("%02d", new Object[]{
                            Integer.valueOf(prpsResult[i1][j1][j2])
                    })).append("%").toString(), (float) i3 * 2.7F + f * 2.0F + 20, f * 1.5F * (1.2F + (float) (i4 + 1)));
                }
                i4++;
            }

            if (prpsNum[i1] > j1)
                graphics2d1.drawString(String.format("%tF %tT", new Object[]{
                        Long.valueOf(prpstime[i1][j1]), Long.valueOf(prpstime[i1][j1])
                }), (float) i3 * 2.4F, f * 1.5F * 1.2F);
            else
                graphics2d1.drawString(String.format("%tF %tT", new Object[]{
                        Long.valueOf(prpdtime[i1][j1]), Long.valueOf(prpdtime[i1][j1])
                }), (float) i3 * 2.4F, f * 1.5F * 1.2F);
            j3++;
        }

    }

}
