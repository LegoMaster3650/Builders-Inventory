package _3650.builders_inventory.util;

public class RangeSnapFloat {
	private float value;
	private final float[] range;
	private int index = 0;
	public final int size;
	
	public RangeSnapFloat(float value, float[] range) {
		this.value = value;
		if (range.length < 2) throw new IllegalArgumentException("RangeSnapFloat cannot have a range of less than two values");
		float[] newRange = new float[range.length];
		for (int i = 0; i < range.length - 1; i++) {
			if (range[i] >= range[i + 1]) throw new IllegalArgumentException("RangeSnapFloat range must be in ascending order");
			newRange[i] = range[i];
		}
		newRange[range.length - 1] = range[range.length - 1];
		this.range = newRange;
		this.set(value + 0.01f);
		this.size = range.length;
	}
	
	public float get() {
		return value;
	}
	
	public float[] getRange() {
		return range;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void set(float value) {
		float closestComp = Float.POSITIVE_INFINITY;
		int ind = 0;
		for (int i = 0; i < range.length; i++) {
			float comp = Math.abs(range[i] - value);
			if (comp < closestComp) {
				closestComp = comp;
				ind = i;
			}
		}
		this.value = this.range[ind];
		this.index = ind;
	}
	
	public static float judge(float value, float[] range) {
		float closestComp = Float.POSITIVE_INFINITY;
		int closestVal = 0;
		for (int i = 0; i < range.length; i++) {
			float comp = Math.abs(range[i] - value);
			if (comp < closestComp) {
				closestComp = comp;
				closestVal = i;
			}
		}
		return range[closestVal];
	}
	
	public static float[] scale(float scale) {
		float[] range = new float[Math.round(scale)];
		for (int i = 1; i <= range.length; i++) {
			range[i-1] = i / scale;
		}
		return range;
	}
	
}
