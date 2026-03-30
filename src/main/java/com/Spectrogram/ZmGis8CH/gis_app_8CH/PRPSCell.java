package com.Spectrogram.ZmGis8CH.gis_app_8CH;
public class PRPSCell
{

	boolean isSelected;
	private boolean selectedBackground; // 字符串背景选中状态
	int h;
	int m;
	int s;
	String str;
	String str2;
	long time;
	boolean showDate;

	public PRPSCell(long l, int i, int j, int k)
	{
		showDate = false;
		time = l;
		h = i;
		m = j;
		s = k;
		str = String.format("%02d : %02d : %02d", new Object[] {
			Integer.valueOf(i), Integer.valueOf(j), Integer.valueOf(k)
		});
		str2 = String.format("%tF %02d:%02d:%02d", new Object[] {
			Long.valueOf(l), Integer.valueOf(i), Integer.valueOf(j), Integer.valueOf(k)
		});
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}

	/**
	 * 获取 str2.
	 *
	 * @return String  {@link String}
	 */
	public String getStr2() {
		return str2;
	}

	/**
	 * 获取 str.
	 *
	 * @return String  {@link String}
	 */
	public String getStr() {
		return str;
	}


	/**
	 * 获取 selectedBackground.
	 *
	 * @return $field.typeName  {@link boolean}
	 */
	public boolean isSelectedBackground() {
		return selectedBackground;
	}

	/**
	 * 设置 selectedBackground.
	 *
	 * <p>You can use getSelectedBackground() to get the value of selectedBackground</p>
	 *
	 * @param selectedBackground selectedBackground
	 */
	public void setSelectedBackground(boolean selectedBackground) {
		this.selectedBackground = selectedBackground;
	}

	@Override
	public String toString()
	{
		if (showDate)
			return str2;
		else
			return str;
	}
}
