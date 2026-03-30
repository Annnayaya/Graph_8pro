package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.util.Enumeration;
import java.util.GregorianCalendar;

import javax.swing.tree.TreeNode;

class PeriodBlock
	implements TreeNode
{

	SiteData parent;
	long stime;
	long etime;
	String dispString;
	GregorianCalendar cal;

	public PeriodBlock(SiteData sitedata, long l, long l1)
	{
		cal = new GregorianCalendar();
		parent = sitedata;
		stime = l;
		etime = l1;
		cal.setTimeInMillis(l);
		dispString = String.format("%tF ~ ", new Object[] {cal});
		cal.setTimeInMillis(l1);
		dispString += String.format("%tF", new Object[] { cal });
	}

	@SuppressWarnings("rawtypes")
	public Enumeration children()
	{
		return new Enumeration() 
		{

			public boolean hasMoreElements()
			{
				return false;
			}

			public TreeNode nextElement()
			{
				return null;
			}
		};
	}

	public boolean getAllowsChildren()
	{
		return false;
	}

	public TreeNode getChildAt(int i)
	{
		return null;
	}

	public int getChildCount()
	{
		return 0;
	}

	public int getIndex(TreeNode treenode)
	{
		return -1;
	}

	public TreeNode getParent()
	{
		return parent;
	}

	public boolean isLeaf()
	{
		return true;
	}

	public String toString()
	{
		return dispString;
	}
}
