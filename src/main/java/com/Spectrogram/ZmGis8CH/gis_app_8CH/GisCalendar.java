package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.Dimension;
import java.util.Date;
import javax.swing.JComponent;

class GisCalendar extends JComponent
{
	private static final long serialVersionUID = 197307030006L;
	DateChooser dc;
	GisExplorer ge;
	long oldTimeS = 0L;

	public GisCalendar(GisExplorer gisexplorer)
	{
		setPreferredSize(new Dimension(180, 200));
		this.dc = new DateChooser(this, this);
		this.ge = gisexplorer;
	}

	public void dateSelected(long l)
	{
		if (ge == null)
			return;
		if (ge.currentSite == null)
		{
			return;
		} else
		{
			System.out.println(String.format("%tF -> %tF", new Object[] {Long.valueOf(ge.currentSite.loadedDate), Long.valueOf(l)}));
			ge.setDay(l);
			return;
		}
	}

	public boolean isAvailable(long l)
	{
		if (this.ge == null)
			return false;
		if (this.ge.currentSite == null)
			return false;
		int j = this.ge.currentSite.savedDay.length;
		for (int i = 0; i < j; i++)
			if (this.ge.currentSite.savedDay[i] == l)
				return true;

		return false;
	}

	public void selectDay(long l)
	{
		if (l == 0L)return;
		if (this.oldTimeS == l)return;
		this.oldTimeS = l;
		System.out.println("giscalendar sel : " + new Date(l));
		this.dc.select(new Date(l));
		this.dc.itemStateChanged(null);
	}
}
