package net.kwami.tcp;

public class ObjectReceiver<T> extends Thread {
	private Holder<T> holder = new Holder<>(null);
	private ObjectReceiverListener<T> listener = null;

	public ObjectReceiver(ObjectReceiverListener<T> listener) {
		this.listener = listener;
	}

	@Override
	public void run() {
		try {
			while (holder != null) {
				try {
					if (holder.getObj() != null && listener != null)
						listener.processObject(holder.getObj());
					synchronized (holder) {
						System.out.println("waiting for object");
						holder.wait(0);
					}
				} catch (InterruptedException e) {
					break;
				}
			}
			System.out.println("thread is ending");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void kill() {
		holder = null;
	}

	public void go(T obj) {
		synchronized (holder) {
			holder.setObj(obj);;
			holder.notify();
		}
	}
}
