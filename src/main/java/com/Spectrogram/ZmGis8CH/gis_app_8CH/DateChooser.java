package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

public class DateChooser extends JPanel implements ItemListener, MouseListener, FocusListener, KeyListener, ActionListener
{	
	private static final long serialVersionUID = 197307030012L;
	private static final String[] MONTHS = {ChooseLang.M[180], ChooseLang.M[181], ChooseLang.M[182], ChooseLang.M[183], ChooseLang.M[184], ChooseLang.M[185], ChooseLang.M[186], ChooseLang.M[187], ChooseLang.M[188], ChooseLang.M[189], ChooseLang.M[190], ChooseLang.M[191]};
	private static final String[] DAYS = {ChooseLang.M[192], ChooseLang.M[193], ChooseLang.M[194], ChooseLang.M[195], ChooseLang.M[196], ChooseLang.M[197], ChooseLang.M[198]};
	private static final Color WEEK_DAYS_FOREGROUND = Color.black;
	private static final Color DAYS_FOREGROUND = Color.blue;
	private static final Color SELECTED_DAY_FOREGROUND = Color.white;
	private static final Color SELECTED_DAY_BACKGROUND = Color.blue;
	private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(1, 1, 1, 1);
	private static final Border FOCUSED_BORDER=BorderFactory.createLineBorder(Color.yellow, 1);
	private static final int FIRST_YEAR = 2005;
	private static final int LAST_YEAR = 2100;
	private GregorianCalendar calendar;
	private JLabel days[][];
	private JPanel daysGrid;
	private JComboBox<String> month;
	private JComboBox<String> year;
	private JButton ok;
	private JButton cancel;
	private int offset;
	private int lastDay;
	private JLabel day;
	private boolean okClicked;
	GisCalendar gc;
	
	private static class FocusablePanel extends JPanel
	{
		private static final long serialVersionUID = 197307030011L;
		public boolean isFocusable()
		{
			return true;
		}

		public FocusablePanel(LayoutManager layoutmanager)
		{
			super();
		}
	}

	private void construct()
	{
		setSize(new Dimension(180, 200));
		this.calendar = new GregorianCalendar();
		this.calendar.set(11, 0);
		this.calendar.set(12, 0);
		this.calendar.set(13, 0);
		this.calendar.set(14, 0);
		this.month = new JComboBox<String>(MONTHS);
		this.month.addItemListener(this);
		this.year = new JComboBox<String>();
		for (int i = 2005; i <= 2100; i++)
			this.year.addItem(Integer.toString(i));

		this.year.addItemListener(this);
		this.days = new JLabel[7][7];
		for (int j = 0; j < 7; j++)
		{
			this.days[0][j] = new JLabel(DAYS[j], 4);
			this.days[0][j].setForeground(WEEK_DAYS_FOREGROUND);
		}

		for (int k = 1; k < 7; k++)
		{
			for (int l = 0; l < 7; l++)
			{
				this.days[k][l] = new JLabel(" ", 4);
				this.days[k][l].setForeground(DAYS_FOREGROUND);
				this.days[k][l].setBackground(SELECTED_DAY_BACKGROUND);
				this.days[k][l].setBorder(EMPTY_BORDER);
				this.days[k][l].addMouseListener(this);
			}

		}

		this.ok = new JButton("Ok");
		this.ok.addActionListener(this);
		this.cancel = new JButton("Cancel");
		this.cancel.addActionListener(this);
		
		JPanel jpanel = new JPanel();
		jpanel.add(year);
		jpanel.add(month);
		jpanel.setSize(new Dimension(170, 35));
		jpanel.setPreferredSize(new Dimension(170, 35));
		
		this.daysGrid = new JPanel(new GridLayout(7, 7, 5, 0));
		this.daysGrid.addFocusListener(this);
		this.daysGrid.addKeyListener(this);
		for (int i1 = 0; i1 < 7; i1++)
		{
			for (int j1 = 0; j1 < 7; j1++)
				this.daysGrid.add(this.days[i1][j1]);

		}

		this.daysGrid.setBackground(Color.white);
		this.daysGrid.setBorder(new EtchedBorder(1));
		this.daysGrid.setPreferredSize(new Dimension(170, 145));
		JPanel jpanel1 = new JPanel();
		jpanel1.add(daysGrid);
		jpanel1.setPreferredSize(new Dimension(170, 145));
		JPanel jpanel2 = new JPanel();
		jpanel2.add(this.ok);
		jpanel2.add(this.cancel);
		add("North", jpanel);
		add("Center", jpanel1);
		select();
		update();
		setVisible(true);
	}

	private int getSelectedDay()
	{
		if (this.day == null)
			return -1;
		try
		{
			return Integer.parseInt(this.day.getText());
		}
		catch(NumberFormatException localNumberFormatException) {}		
		return 1;
	}

	private void setSelected(JLabel jlabel)
	{
		if (this.day != null)
		{
			this.calendar.set(5, Integer.parseInt(this.day.getText()));
			this.day.setForeground(this.gc.isAvailable(this.calendar.getTimeInMillis()) ? DAYS_FOREGROUND : Color.gray);
			this.day.setOpaque(false);
			this.day.setBorder(EMPTY_BORDER);
		}
		this.day = jlabel;
		this.day.setForeground(SELECTED_DAY_FOREGROUND);
		this.day.setOpaque(true);
		if (this.daysGrid.hasFocus())
			this.day.setBorder(FOCUSED_BORDER);
	}

	private void setSelected(int i)
	{
		setSelected(this.days[((i + this.offset) - 1) / 7 + 1][((i + this.offset) - 1) % 7]);
	}

	public void update()
	{
		int i = getSelectedDay();
		for (int j = 0; j < 7; j++)
		{
			this.days[1][j].setText("0");
			this.days[5][j].setText("0");
			this.days[6][j].setText("0");
			this.days[1][j].setForeground(Color.white);
			this.days[5][j].setForeground(Color.white);
			this.days[6][j].setForeground(Color.white);
		}

		this.calendar.set(5, 1);
		this.calendar.set(2, this.month.getSelectedIndex() + 0);
		this.calendar.set(1, this.year.getSelectedIndex() + 2005);
		this.offset = this.calendar.get(7) - 1;
		this.lastDay = this.calendar.getActualMaximum(5);
		for (int k = 0; k < lastDay; k++)
		{
			this.days[(k + this.offset) / 7 + 1][(k + this.offset) % 7].setText(String.valueOf(k + 1));
			this.calendar.set(5, k + 1);
			this.days[(k + this.offset) / 7 + 1][(k + this.offset) % 7].setForeground(this.gc.isAvailable(this.calendar.getTimeInMillis()) ? DAYS_FOREGROUND : Color.gray);
			this.days[(k + this.offset) / 7 + 1][(k + this.offset) % 7].setBackground(this.gc.isAvailable(this.calendar.getTimeInMillis()) ? SELECTED_DAY_BACKGROUND : Color.gray);
		}

		this.calendar.set(5, 1);
		if (i != -1)
		{
			if (i > this.lastDay)
				i = this.lastDay;
			setSelected(i);
		}
	}

	public void actionPerformed(ActionEvent actionevent)
	{
		if (actionevent.getSource() == this.ok)
			this.okClicked = true;
	}

	public void focusGained(FocusEvent focusevent)
	{
		setSelected(this.day);
	}

	public void focusLost(FocusEvent focusevent)
	{
		setSelected(this.day);
	}

	public void itemStateChanged(ItemEvent itemevent)
	{
		update();
	}

	public void keyPressed(KeyEvent keyevent)
	{
		int i = getSelectedDay();
		switch (keyevent.getKeyCode())
		{
		default:
			break;

		case 37: // '%'
			if (i > 1)
				setSelected(i - 1);
			break;

		case 39: // '\''
			if (i < this.lastDay)
				setSelected(i + 1);
			break;

		case 38: // '&'
			if (i > 7)
				setSelected(i - 7);
			break;

		case 40: // '('
			if (i <= this.lastDay - 7)
				setSelected(i + 7);
			break;
		}
	}

	public void mouseClicked (MouseEvent mouseevent)
	{
		JLabel jlabel = (JLabel)mouseevent.getSource();
	
		if (!jlabel.getText().equals(" "))
		{
			setSelected(jlabel);
			regetDate();
		}
		this.daysGrid.requestFocus();
	
		
	}

	public void regetDate()
	{
		this.calendar.set(5, Integer.parseInt(this.day.getText()));
		System.out.println(String.format("regetDate!! : %tF", new Object[] {this.calendar}));
		gc.dateSelected(this.calendar.getTimeInMillis());
	}

	public void keyReleased(KeyEvent keyevent)
	{
	}

	public void keyTyped(KeyEvent keyevent)
	{
	}

	public void mouseEntered(MouseEvent mouseevent)
	{
	}

	public void mouseExited(MouseEvent mouseevent)
	{
	}

	public void mousePressed(MouseEvent mouseevent)
	{
	}

	public void mouseReleased(MouseEvent mouseevent)
	{
	}

	public DateChooser(Container container, GisCalendar giscalendar)
	{
		this.gc = giscalendar;
		construct();
		container.add(this);
	}

	public Date select(Date date)
	{
		this.calendar.setTime(date);
		this.calendar.set(11, 0);
		this.calendar.set(12, 0);
		this.calendar.set(13, 0);
		this.calendar.set(14, 0);
		int i = this.calendar.get(5);
		int j = this.calendar.get(2);
		int k = this.calendar.get(1);
		year.setSelectedIndex(k - 2005);
		month.setSelectedIndex(j - 0);
		setSelected(i);
		this.okClicked = false;
		if (!this.okClicked)
		{
			return null;
		} else
		{
			this.calendar.set(5, getSelectedDay());
			this.calendar.set(2, this.month.getSelectedIndex() + 0);
			this.calendar.set(1, this.year.getSelectedIndex() + 2005);
			return this.calendar.getTime();
		}
	}

	public Date select()
	{
		return select(new Date());
	}

	public int getYear()
	{
		return this.calendar.get(1);
	}

	public int getMonth()
	{
		return this.calendar.get(2) + 0 + 1;
	}

	public int getDay()
	{
		return getSelectedDay();
	}
}
