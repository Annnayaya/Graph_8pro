package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

class Database implements TreeNode
{

	ArrayList<SiteData> sites = new ArrayList<SiteData>();

	public Database(String s)
	{
		(new File(s)).mkdir();
		(new File("report")).mkdir();
		if (!s.endsWith(File.separator))
			s = s.concat(File.separator);
		SiteData.pathName = s;
		SiteData.mainDB = this;
		getAvailableSites();
	}

	public SiteData openSite(String s)
	{
		int j = sites.size();
		System.out.println((new StringBuilder()).append("opening site : ").append(s).toString());
		for (int i = 0; i < j; i++)
			if (((SiteData)sites.get(i)).siteName.equals(s))
				return (SiteData)sites.get(i);

		SiteData sitedata = SiteData.load(s);
		sites.add(sitedata);
		return sitedata;
	}

	public void closeSite(SiteData sitedata)
	{
		closeSite(sitedata.siteName);
	}

	public void closeSite(String s)
	{
	}

	@SuppressWarnings("unused")
	private int findSite(String s)
	{
		int j = sites.size();
		for (int i = 0; i < j; i++)
			if (((SiteData)sites.get(i)).siteName.equals(s))
				return i;

		return -1;
	}

	public boolean  deleteSite(File file)
	{
		if(file.isDirectory()){
			String[] children = file.list();
			//递归删除目录中的子目录下            
			for (int i=0; i<children.length; i++) {                
				boolean success = deleteSite(new File(file, children[i]));
				if (!success) 
				{                    
					return false;                
				}  
			}			
		}
		return file.delete();
	}
	public SiteData[] getAvailableSiteArray()
	{
		int j = sites.size();
		SiteData asitedata[] = new SiteData[j];
		for (int i = 0; i < j; i++)
			asitedata[i] = (SiteData)sites.get(i);

		return asitedata;
	}

	public void getAvailableSites()
	{
		String as[] = (new File(SiteData.pathName)).list();
		int j;
		if (as != null)
			j = as.length;
		else
			j = 0;
		SiteData asitedata[] = new SiteData[j];
		ArrayList<SiteData> arraylist = new ArrayList<SiteData>();
		for (int i = 0; i < j; i++)
		{
			asitedata[i] = SiteData.load(as[i]);
			arraylist.add(asitedata[i]);
			System.out.println((new StringBuilder()).append(asitedata[i].siteName).append(" : ").append(asitedata[i].savedDay.length).toString());
		}

		sites = arraylist;
	}
	
	@Override
	public Enumeration<TreeNode> children()
	{
		return new Enumeration<TreeNode>() {

			int p;

			@Override
			public boolean hasMoreElements()
			{
				return sites.size() > p;
			}

			@Override
			public TreeNode nextElement()
			{
				return (TreeNode)sites.get(p);
			}
		};
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public TreeNode getChildAt(int i)
	{
		return (TreeNode)sites.get(i);
	}

	@Override
	public int getChildCount()
	{
		return sites.size();
	}

	@Override
	public int getIndex(TreeNode treenode)
	{
		return sites.indexOf(treenode);
	}

	@Override
	public TreeNode getParent()
	{
		return null;
	}

	@Override
	public boolean isLeaf()
	{
		return false;
	}

	public String toString()
	{
		//return M.M[267];//数据库
		return ChooseLang.M[267];//数据库
	}
}
