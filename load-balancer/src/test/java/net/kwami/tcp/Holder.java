package net.kwami.tcp;

public class Holder<E> {
	private E obj = null;

	public Holder(E obj) {
		super();
		this.obj = obj;
	}

	public E getObj() {
		return obj;
	}

	public void setObj(E obj) {
		this.obj = obj;
	}
}
