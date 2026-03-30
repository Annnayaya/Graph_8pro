package com.Spectrogram.ZmGis8CH.gis_app_8CH;
class GisDataProc
{

	GisChannelData gd;
	GisXYZ gm;

	public GisDataProc(int i, GisXYZ gismmi)
	{
		gd = new GisChannelData(i, gismmi);
		gm = gismmi;
	}
}
