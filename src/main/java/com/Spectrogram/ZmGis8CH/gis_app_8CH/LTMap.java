package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import javax.swing.JFrame;

class LTMap
{

	float ltdataf[][];
	private float total;
	float values[];
	PRPDGraph pg;
	float hsum[];
	float vsum[];

	public LTMap()
	{
		ltdataf = new float[64][64];
		total = 0.0F;
		values = new float[8];
		hsum = new float[64];
		vsum = new float[64];
		JFrame jframe = new JFrame("lt graph");
	}

	void ltMapCore(int ai[])
	{
		int i = GisXYZ.bufferLen / 64;
		//boolean flag = false;
		int i6 = 200;
		for (int j = 0; j < i * 64; j++)
			if (i6 < ai[j])
				i6 = ai[j];

		for (int k = 0; k < 64; k++)
		{
			for (int j4 = 0; j4 < 64; j4++)
				ltdataf[k][j4] *= 0.5F;

		}

		int i5 = i * 64 - 64;
		for (int l = 1; l < i5; l++)
		{
			for (int k4 = 0; k4 < 64; k4++)
			{
				int k5 = ai[l + k4 + 1] - ai[l + k4];
				int l5 = ai[l] - ai[l - 1];
				k5 = (k5 * 4096) / i6;
				l5 = (l5 * 4096) / i6;
				if (k5 <= 0 || l5 <= 0 || k5 < l5)
					continue;
				int j5 = (((k5 + l5) - i6 / 2) * 3) / 5;
				if (0 >= j5)
					continue;
				if (j5 >= 4096)
					j5 = 4095;
				if (k5 >= 4096)
					k5 = 4095;
				if (l5 >= 4096)
					l5 = 4095;
				ltdataf[k4][j5 >> 6] += l5 >> 6;
			}

		}

		for (int i1 = 0; i1 < 8; i1++)
			values[i1] = 0.0F;

		for (int j1 = 0; j1 < 64; j1++)
		{
			vsum[j1] = 0.0F;
			hsum[j1] = 0.0F;
		}

		total = 0.0F;
		for (int k1 = 0; k1 < 64; k1++)
		{
			for (int l4 = 0; l4 < 64; l4++)
			{
				vsum[k1] += ltdataf[k1][l4];
				hsum[l4] += ltdataf[k1][l4];
				total += ltdataf[k1][l4];
			}

		}

		for (int l1 = 0; l1 < 8; l1++)
			values[0] += vsum[l1];

		for (int i2 = 24; i2 < 40; i2++)
			values[0] += vsum[i2];

		for (int j2 = 56; j2 < 64; j2++)
			values[0] += vsum[j2];

		for (int k2 = 0; k2 < 16; k2++)
			values[1] += hsum[k2];

		for (int l2 = 16; l2 < 32; l2++)
			values[2] += hsum[l2];

		for (int i3 = 32; i3 < 48; i3++)
			values[3] += hsum[i3];

		float f = 0.0F;
		float f1 = 0.0F;
		for (int j3 = 0; j3 < 64; j3++)
		{
			f += vsum[j3];
			f1 += hsum[j3];
		}

		f *= 0.015625F;
		f1 *= 0.015625F;
		float f2 = 0.0F;
		float f3 = 0.0F;
		for (int k3 = 0; k3 < 64; k3++)
		{
			float f4 = vsum[k3] - f;
			f2 += f4 * f4;
			f4 = hsum[k3] - f1;
			f3 += f4 * f4;
		}

		values[6] = (float)Math.sqrt(f2);
		values[7] = (float)Math.sqrt(f3);
		float f5 = 1.0F / total;
		for (int l3 = 0; l3 < 8; l3++)
			values[l3] *= f5;

		values[4] = values[6] / values[7];
		if (GisXYZ.debug)
		{
			for (int i4 = 0; i4 < 5; i4++)
				System.out.print(String.format("%5.3f\t", new Object[] {
					Float.valueOf(values[i4])
				}));

			System.out.println();
		}
	}
}
