package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Calendar;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

class MemoPanel extends JPanel
{
	private static final long serialVersionUID = 20080212001L;
	GisXYZ gm;
	GisExplorer ge;
	JLabel memoLabel;
	JTextField memoText1;
	JTextField memoText2;
	JComboBox<String> memoCombo;
	JButton memoSave;
	Calendar date;
	long dateList[];
	int maxDateList;

	public MemoPanel(GisXYZ gismmi)
	{
		super(new BorderLayout());
		date = Calendar.getInstance();
		date.setTimeInMillis(0L);
		gm = gismmi;
		//memoLabel = new JLabel((new StringBuilder()).append(" ").append(ChooseLang.M[140]).append(" 0000/00/00 00:00:00 ").toString());
		memoLabel = new JLabel((new StringBuilder()).append(" ").append(ChooseLang.M[145]).append(" 0000/00/00 00:00:00 ").toString());
		memoLabel.setFont(GisXYZ.fontG);
		memoText1 = new JTextField("");
		memoText1.setFont(GisXYZ.fontG);
		memoText1.setPreferredSize(new Dimension(100, 20));
		memoText2 = new JTextField("");
		memoText2.setFont(GisXYZ.fontG);
		memoSave = new JButton(ChooseLang.M[141]);
		memoSave.setFont(GisXYZ.fontG);
		memoSave.setEnabled(false);
		memoText1.setVisible(false);
		memoText2.setVisible(false);
		memoSave.setVisible(false);
		JPanel jpanel = new JPanel(new BorderLayout());
		JPanel jpanel1 = new JPanel(new BorderLayout());
		JPanel jpanel2 = new JPanel(new BorderLayout());	
		JLabel jcr = new JLabel((new StringBuilder()).append(" ").append(ChooseLang.M[142]).append(" ").toString());
		JLabel jcd = new JLabel((new StringBuilder()).append("   ").append(ChooseLang.M[143]).append(" ").toString());
		jcr.setFont(GisXYZ.fontG);
		jcd.setFont(GisXYZ.fontG);
		jcr.setVisible(false);
		jcd.setVisible(false);
		//jpanel1.add(new JLabel((new StringBuilder()).append(" ").append(ChooseLang.M[142]).append(" ").toString()), "West");
		jpanel1.add(jcr, "West");
		jpanel1.add(memoText1, "Center");
		//jpanel2.add(new JLabel((new StringBuilder()).append("   ").append(ChooseLang.M[143]).append(" ").toString()), "West");		
		jpanel2.add(jcd, "West");
		jpanel2.add(memoText2, "Center");
		jpanel.add(jpanel1, "West");
		jpanel.add(jpanel2, "Center");
		add(memoLabel, "West");
		add(jpanel, "Center");
		add(memoSave, "East");
		memoText1.addCaretListener(new CaretListener() {

			public void caretUpdate(CaretEvent caretevent)
			{
				if (GisXYZ.hwtest == 0)
					memoSave.setEnabled(true);
			}
		});
		memoText2.addCaretListener(new CaretListener() {

			public void caretUpdate(CaretEvent caretevent)
			{
				if (GisXYZ.hwtest == 0)
					memoSave.setEnabled(true);
			}
		});
		memoSave.addActionListener(new ActionListener() {
			
		public void actionPerformed(ActionEvent actionevent)
			{
				try
				{
					File file = new File((new StringBuilder()).append(SiteData.pathName).append(gm.currentOpenSite.siteName).append(File.separator).append(String.format("%tY"+File.separator+"%tF.memo", new Object[] {
						date, date
					})).toString());
					DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file, true));
					dataoutputstream.writeLong(date.getTimeInMillis());
					byte abyte0[] = memoText1.getText().getBytes();
					dataoutputstream.writeShort(abyte0.length);
					for (int i = 0; i < abyte0.length; i++)
						dataoutputstream.writeByte(abyte0[i]);

					abyte0 = memoText2.getText().getBytes();
					dataoutputstream.writeShort(abyte0.length);
					for (int j = 0; j < abyte0.length; j++)
						dataoutputstream.writeByte(abyte0[j]);

					dataoutputstream.close();
					gm.ge.memoPanel.updateData(date, memoText1.getText(), memoText2.getText());
					memoSave.setEnabled(false);
				}
				catch (Exception exception)
				{
					exception.printStackTrace();
				}
			}

		});
	}

	public MemoPanel(GisExplorer gisexplorer)
	{
		super(new BorderLayout());
		date = Calendar.getInstance();
		date.setTimeInMillis(0L);
		ge = gisexplorer;
		memoLabel = new JLabel((new StringBuilder()).append(" ").append(ChooseLang.M[144]).append(" ").toString());
		memoLabel.setVisible(false);
		memoCombo = new JComboBox<String>();
		memoCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent)
			{
				int i = memoCombo.getSelectedIndex();
				if (0 < i)
					if (i > maxDateList);
			}
		});
		memoCombo.setVisible(false);
		add(memoLabel, "West");
		add(memoCombo, "Center");
		memoCombo.addItem("");
		maxDateList = 0;
		dateList = new long[1000];
	}

	public void updateData(Calendar calendar, String s, String s1)
	{
		if (date.get(1) != calendar.get(1) || date.get(2) != calendar.get(2) || date.get(5) != calendar.get(5))
			return;
		long l = calendar.getTimeInMillis();
		if (maxDateList < 1000)
			dateList[maxDateList] = l;
		maxDateList++;
		memoCombo.addItem((new StringBuilder()).append(String.format(" %04d/%02d/%02d %02d:%02d:%02d ", new Object[] {
			Integer.valueOf(calendar.get(1)), Integer.valueOf(calendar.get(2) + 1), Integer.valueOf(calendar.get(5)), Integer.valueOf(calendar.get(11)), Integer.valueOf(calendar.get(12)), Integer.valueOf(calendar.get(13))
		})).append(s).append(" / ").append(s1).toString());
	}

	public void selectTime(long l)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(l);
		if (date.get(1) != calendar.get(1) || date.get(2) != calendar.get(2) || date.get(5) != calendar.get(5))
		{
			memoCombo.removeAllItems();
			memoCombo.addItem("");
			maxDateList = 0;
			try
			{
				date.setTimeInMillis(l);
				File file = new File((new StringBuilder()).append(SiteData.pathName).append(ge.currentSite.siteName).append(File.separator).append(String.format("%tY"+File.separator+"%tF.memo", new Object[] {
					date, date
				})).toString());
				DataInputStream datainputstream;
				String s;
				String s1;
				for (datainputstream = new DataInputStream(new FileInputStream(file)); 0 < datainputstream.available(); updateData(date, s, s1))
				{
					long l1 = datainputstream.readLong();
					Short short1 = Short.valueOf(datainputstream.readShort());
					byte abyte0[] = new byte[short1.shortValue()];
					for (int i = 0; i < short1.shortValue(); i++)
						abyte0[i] = datainputstream.readByte();

					s = new String(abyte0);
					short1 = Short.valueOf(datainputstream.readShort());
					abyte0 = new byte[short1.shortValue()];
					for (int j = 0; j < short1.shortValue(); j++)
						abyte0[j] = datainputstream.readByte();

					s1 = new String(abyte0);
					date.setTimeInMillis(l1);
				}

				datainputstream.close();
			}
			catch (Exception exception) { }
		}
		int k;
		for (k = 0; k < maxDateList && l > dateList[k]; k++);
		memoCombo.setSelectedIndex(k);
	}

	public void timer()
	{
		date = Calendar.getInstance();
		/*
		memoLabel.setText(String.format((new StringBuilder()).append(" ").append(ChooseLang.M[140]).append(" %04d/%02d/%02d %02d:%02d:%02d ").toString(), new Object[] {
			Integer.valueOf(date.get(1)), Integer.valueOf(date.get(2) + 1), Integer.valueOf(date.get(5)), Integer.valueOf(date.get(11)), Integer.valueOf(date.get(12)), Integer.valueOf(date.get(13))
		}));
		*/
		memoLabel.setText(String.format((new StringBuilder()).append(" ").append(ChooseLang.M[145]).append(" %04d/%02d/%02d %02d:%02d:%02d ").toString(), new Object[] {
				Integer.valueOf(date.get(1)), Integer.valueOf(date.get(2) + 1), Integer.valueOf(date.get(5)), Integer.valueOf(date.get(11)), Integer.valueOf(date.get(12)), Integer.valueOf(date.get(13))
			}));
	}
}
