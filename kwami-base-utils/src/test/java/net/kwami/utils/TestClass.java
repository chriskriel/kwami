package net.kwami.utils;

import java.util.Date;

class TestClass extends MappedObject {
	protected Date date;
	protected String str;
	protected String[] strs;
	protected boolean bool;
	protected boolean[] bools;
	protected byte b;
	protected byte[] bb;
	protected char c;
	protected char[] cc;
	protected short h;
	protected short[] hh;
	protected int i;
	protected int[] ii;
	protected long l;
	protected long[] ll;
	protected float f;
	protected float[] ff;
	protected double d;
	protected double[] dd;

	public TestClass() {
		super();
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	public String[] getStrs() {
		return strs;
	}

	public void setStrs(String[] strs) {
		this.strs = strs;
	}

	public boolean isBool() {
		return bool;
	}

	public void setBool(boolean bool) {
		this.bool = bool;
	}

	public boolean[] getBools() {
		return bools;
	}

	public void setBools(boolean[] bools) {
		this.bools = bools;
	}

	public byte getB() {
		return b;
	}

	public void setB(byte b) {
		this.b = b;
	}

	public byte[] getBb() {
		return bb;
	}

	public void setBb(byte[] bb) {
		this.bb = bb;
	}

	public char getC() {
		return c;
	}

	public void setC(char c) {
		this.c = c;
	}

	public char[] getCc() {
		return cc;
	}

	public void setCc(char[] cc) {
		this.cc = cc;
	}

	public short getH() {
		return h;
	}

	public void setH(short h) {
		this.h = h;
	}

	public short[] getHh() {
		return hh;
	}

	public void setHh(short[] hh) {
		this.hh = hh;
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public int[] getIi() {
		return ii;
	}

	public void setIi(int[] ii) {
		this.ii = ii;
	}

	public long getL() {
		return l;
	}

	public void setL(long l) {
		this.l = l;
	}

	public long[] getLl() {
		return ll;
	}

	public void setLl(long[] ll) {
		this.ll = ll;
	}

	public float getF() {
		return f;
	}

	public void setF(float f) {
		this.f = f;
	}

	public float[] getFf() {
		return ff;
	}

	public void setFf(float[] ff) {
		this.ff = ff;
	}

	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}

	public double[] getDd() {
		return dd;
	}

	public void setDd(double[] dd) {
		this.dd = dd;
	}
	public void copyFields(Object from) {
		super.copyFields(from);
	}
}
