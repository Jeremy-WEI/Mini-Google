package cis555.loadbalancer;

public class Incrementor {

	private int count = 0;
	
	/**
	 * Gets the current count number and increment
	 * @return
	 */
	public synchronized int getCountAndIncrement(){
		int currentCount = this.count;
		count++;
		return currentCount;
	}
}
