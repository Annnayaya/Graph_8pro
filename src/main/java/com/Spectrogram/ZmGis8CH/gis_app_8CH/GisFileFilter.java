package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.io.File;

import javax.swing.filechooser.FileFilter;


public class GisFileFilter extends FileFilter {

	@Override
	public boolean accept(File file) {
		String name = file.getName();
		return name.toLowerCase().endsWith(".gisdata");
	}

	@Override
	public String getDescription() {

		return "*.gisdata";
	}

}
