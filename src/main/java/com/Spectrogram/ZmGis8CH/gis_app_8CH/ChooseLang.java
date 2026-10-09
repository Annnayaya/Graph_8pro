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
	// 将数组大小扩充为 430 (以容纳 0-419 及后续扩展索引)
	public static Object[] O = new Object[430];
	public static String[] M = new String[430];

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
		Object[][] data = null;
		switch(initlang){
			//英语
			case 0 :
			{
				JOptionPane.setDefaultLocale(Locale.ENGLISH);
				Lang_en_US en = new Lang_en_US();
				data = en.getData();
			}break;
			//日语
			case 1 :
			{
				JOptionPane.setDefaultLocale(Locale.JAPAN);
				Lang_ja_JP ja = new Lang_ja_JP();
				data = ja.getData();
			}break;
			//韩语
			case 2 :
			{
				JOptionPane.setDefaultLocale(Locale.KOREA);
				Lang_ko_KR ko = new Lang_ko_KR();
				data = ko.getData();
			}break;
			//葡萄牙语
			case 3 :
			{
				JOptionPane.setDefaultLocale(Locale.FRENCH);
				Lang_pt_BR pt = new Lang_pt_BR();
				data = pt.getData();
			}break;
			//简体中文
			case 4 :
			default:
			{
				JOptionPane.setDefaultLocale(Locale.CHINA);
				Lang_zh_CN zh = new Lang_zh_CN();
				data = zh.getData();
			}break;
		}

		for(int i = 0; i < 430; i++){
			if (data != null && i < data.length && data[i] != null && data[i].length > 1 && data[i][1] != null) {
				O[i] = data[i][1];
				M[i] = O[i].toString();
			} else {
				O[i] = "";
				M[i] = "";
			}
		}

		// 为 411-419 提供关键文案兜底，防止外部旧配置文件缺少新键导致按钮空白
		applyFallback(411, "持久测试");
		applyFallback(412, "等待采集");
		applyFallback(413, "预热");
		applyFallback(414, "初始化");
		applyFallback(415, "持久测试中");
		applyFallback(416, "结果已保持");
		applyFallback(417, "持久峰值");
		applyFallback(418, "CSV写入异常");
		applyFallback(419, "打开趋势图");
	}

	private static void applyFallback(int index, String defaultText) {
		if (index >= 0 && index < M.length) {
			if (M[index] == null || M[index].trim().isEmpty()) {
				M[index] = defaultText;
				O[index] = defaultText;
			}
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