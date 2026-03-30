package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

class TimeListRenderer extends DefaultListCellRenderer
{
	private static final long serialVersionUID = 197307030016L;
	GisExplorer ge;
	public TimeListRenderer(GisExplorer gisexplorer)
	{
		ge = gisexplorer;
	}

	@Override
	public Component getListCellRendererComponent(JList jlist, Object obj, int i, boolean flag, boolean flag1)
	{
		super.getListCellRendererComponent(jlist, obj, i, flag, flag1);
		setHorizontalAlignment(0);
		int j = ((PRPDCell)obj).min;
		if (j <= ge.trendSelTimeEnd && j >= ge.trendSelTimeStart)
			setForeground(Color.BLUE);
		return this;
	}
}
