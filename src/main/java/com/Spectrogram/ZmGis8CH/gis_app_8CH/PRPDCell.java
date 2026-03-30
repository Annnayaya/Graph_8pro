package com.Spectrogram.ZmGis8CH.gis_app_8CH;
class PRPDCell
{

	int min;
	int pos;
	String str;
	String fileName;

	public PRPDCell(int i, int j, String s)
	{
		min = i;
		pos = j;
		str = String.format("%02d : %02d : %01d0", new Object[] {
			Integer.valueOf(i / 60), Integer.valueOf(i % 60), Integer.valueOf(0)
		});
		fileName = s;
	}

	public String toString()
	{
		return str;
	}
}
