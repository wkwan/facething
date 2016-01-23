package com.dualcnhq.opencv;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class Labels {

	String mPath;

	class label {
		public label(String s, int n) {
			thelabel = s;
			num = n;
		}

		public label(int num, String thelabel, String sampleText) {
			this.num = num;
			this.thelabel = thelabel;
			this.sampleText = sampleText;
		}

		int num;
		String thelabel;
		String sampleText;
	}

	ArrayList<label> Labels = new ArrayList<>();
	public Labels(String Path) {
		mPath = Path;
	}

	public boolean isEmpty() {
		return !(Labels.size() > 0);
	}

	public void add(String label, int num, String sampleText) {
		Labels.add(new label(num, label, sampleText));
	}

	public String get(int i) {
		Iterator<label> Ilabel = Labels.iterator();
		while (Ilabel.hasNext()) {
			label l = Ilabel.next();
			if (l.num == i)
				return l.thelabel;
		}
		return "";
	}

	public int get(String s) {
		Iterator<label> Ilabel = Labels.iterator();
		while (Ilabel.hasNext()) {
			label l = Ilabel.next();
			if (l.thelabel.equalsIgnoreCase(s))
				return l.num;
		}
		return -1;
	}

	public void Save() {
		try {
			File f = new File(mPath + "faces.txt");
			f.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			Iterator<label> Ilabel = Labels.iterator();
			while (Ilabel.hasNext()) {
				label l = Ilabel.next();
				bw.write(l.thelabel + "," + l.num + "," + l.sampleText);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("error", e.getMessage() + " " + e.getCause());
			e.printStackTrace();
		}
	}

	public void Read() {
		try {

			FileInputStream fstream = new FileInputStream(
					mPath + "faces.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));

			String strLine;
			Labels = new ArrayList<label>();
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				StringTokenizer tokens = new StringTokenizer(strLine, ",");
				String label = tokens.nextToken();
				String num = tokens.nextToken();
				String sampleText = tokens.nextToken();

				Labels.add(new label(Integer.parseInt(num), label, sampleText));
			}
			br.close();
			fstream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int max() {
		int m = 0;
		Iterator<label> Ilabel = Labels.iterator();
		while (Ilabel.hasNext()) {
			label l = Ilabel.next();
			if (l.num > m) m = l.num;
		}
		return m;
	}

}
