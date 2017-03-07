package net.kwami.tcp;

public abstract class WaitingThread<T> extends Thread {
	private Holder<T> holder = new Holder<>(null);

	@Override
	public void run() {
		try {
			while (holder != null) {
				try {
					if (holder.getObj() != null)
						doTask(holder.getObj());
					synchronized (holder) {
						System.out.println("thread is waiting for work");
						holder.wait(0);
						System.out.println("thread is active");
					}
				} catch (InterruptedException e) {
					System.out.println("thread has been interrupted");
				}
			}
			System.out.println("thread is ending");
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	public T getObj() {
		return holder.getObj();
	}
	
	protected abstract void doTask(T t) throws Exception;
	
	public void kill(boolean join) {
		holder = null;
		this.interrupt();
		if (join)
			try {
				this.join();
			} catch (InterruptedException e) {
			}
	}
	
	public void process(T t) {
		synchronized (holder) {
			holder.setObj(t);
			holder.notify();
		}
	}
}
