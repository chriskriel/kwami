package zacobcx.threadpool;

import java.util.concurrent.TimeUnit;

public class TimeInterval {

	private long value;
	private final TimeUnit timeUnit;

	public TimeInterval(long value, TimeUnit timeUnit) {
		super();
		this.value = value;
		this.timeUnit = timeUnit;
	}

	public long getValue(TimeUnit targetUnit) {
		return targetUnit.convert(value, timeUnit);
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}
}
