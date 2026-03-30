package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.io.IOException;
import java.util.Locale;
import javax.swing.JOptionPane;

import Language.Lang_en_US;
import Language.Lang_ja_JP;
import Language.Lang_ko_KR;
import Language.Lang_pt_BR;
import Language.Lang_zh_CN;


public class ChooseLang {
	public static int initlang = 0;
	public static int initchnum = 0;
	// 【修改点1】将数组大小从 400 改为 411 (以容纳 0-404 的索引)
	public static Object[] O = new Object[411];
	public static String[] M = new String[411];

	public static String[] A = {
			"English"
			,"日本語"
			,"한국어"
			,"Português"
			,"简体中文"
	};
	public static String[] CH = {
			"通道数选择"
			,"ch_num1"
			,"ch_num2"
			,"ch_num3"
			,"ch_num4"
			,"ch_num5"
			,"ch_num6"
			,"ch_num7"
			,"ch_num8"
	};

	public static void chooselang(){
		try {
			initlang = SiteChooser.readFileByChars(SiteChooser.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		switch(initlang){
			//英语
			case 0 :
			{
				JOptionPane.setDefaultLocale(Locale.ENGLISH);
				Lang_en_US en = new Lang_en_US();
				Object[][] data = en.getData(); // 获取数据源
				// 【修改点2】循环限制改为 411，并添加长度检查防止越界
				for(int i = 0; i < 411; i++){
					if (i < data.length) { // 安全检查：防止英文文件行数不足导致崩溃
						O[i] = data[i][1];
						M[i] = O[i].toString();
					} else {
						M[i] = ""; // 如果语言文件缺少该行，给个默认空值防止 GUI 报错
					}
				}
			}break;
			//日语
			case 1 :
			{
				JOptionPane.setDefaultLocale(Locale.JAPAN);
				Lang_ja_JP ja = new Lang_ja_JP();
				Object[][] data = ja.getData();
				for(int i = 0; i < 411; i++){
					if (i < data.length) {
						O[i] = data[i][1];
						M[i] = O[i].toString();
					} else {
						M[i] = "";
					}
				}
			}break;
			//韩语
			case 2 :
			{
				JOptionPane.setDefaultLocale(Locale.KOREA);
				Lang_ko_KR ko = new Lang_ko_KR();
				Object[][] data = ko.getData();
				for(int i = 0; i < 411; i++){
					if (i < data.length) {
						O[i] = data[i][1];
						M[i] = O[i].toString();
					} else {
						M[i] = "";
					}
				}
			}break;
			//葡萄牙语
			case 3 :
			{
				JOptionPane.setDefaultLocale(Locale.FRENCH);
				Lang_pt_BR pt = new Lang_pt_BR();
				Object[][] data = pt.getData();
				for(int i = 0; i < 411; i++){
					if (i < data.length) {
						O[i] = data[i][1];
						M[i] = O[i].toString();
					} else {
						M[i] = "";
					}
				}
			}break;
			//简体中文
			case 4 :
			{
				JOptionPane.setDefaultLocale(Locale.CHINA);
				Lang_zh_CN zh = new Lang_zh_CN();
				Object[][] data = zh.getData();
				for(int i = 0; i < 411; i++){
					if (i < data.length) {
						O[i] = data[i][1];
						M[i] = O[i].toString();
					} else {
						M[i] = "";
					}
				}
			}break;
		}
	}

	public static void choosechnum(){
		try {
			initchnum = SiteChooser.readFileByChars(SiteChooser.filenum);
		} catch (IOException e) {
			e.printStackTrace();
		}
		switch(initchnum){
			//ch_num1
			case 0 :
			{
				SiteChooser.chnum=0;
			}break;
			//ch_num2
			case 1 :
			{
				SiteChooser.chnum=1;

			}break;
			//ch_num3
			case 2 :
			{
				SiteChooser.chnum=2;

			}break;
			//ch_num4
			case 3 :
			{
				SiteChooser.chnum=3;

			}break;
			//ch_numall
			case 4 :
			{
				SiteChooser.chnum=4;

			}break;
			case 5 :
			{
				SiteChooser.chnum=5;

			}break;
			case 6 :
			{
				SiteChooser.chnum=6;

			}break;
			case 7 :
			{
				SiteChooser.chnum=7;

			}break;
			case 8 :
			{
				SiteChooser.chnum=8;

			}break;

		}
	}
}