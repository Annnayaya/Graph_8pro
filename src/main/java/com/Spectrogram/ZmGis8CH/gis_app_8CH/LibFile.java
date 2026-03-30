package com.Spectrogram.ZmGis8CH.gis_app_8CH;
class LibFile implements Comparable<LibFile>
{

	String fileName;
	String name;
	String comment[];
	int index;

	LibFile(String s)
	{
		fileName = "";
		name = "";
		comment = new String[9];
		fileName = s;
		int l = s.indexOf(";");
		if (l > s.length() - 4 || l < 0)
			l = s.length() - 4;
		name = s.substring(0, l);
		for (int i = 0; i < 8; i++)
			comment[i] = "";

		if (s.length() > l + 4)
		{
			String s1 = s.substring(l + 1, s.length() - 4);
			int j = 0;
			int k = 1;
			do
			{
				if (j >= 8)
					break;
				int i1 = s1.indexOf(";");
				if (i1 < 0)
				{
					comment[k++] = s1.substring(0, s1.length());
					break;
				}
				comment[k++] = s1.substring(0, i1);
				s1 = s1.substring(i1 + 1, s1.length());
				//int ll = this.fileName.indexOf(".");
				//String lll = this.fileName.substring(0, ll);
				//this.index = Integer.parseInt(lll);
				j++;
			} while (true);
		}
	}

	public String toString()
	{
		return name;
	}

	@Override
	public int compareTo(LibFile o) {
		return 0;
	}
}
