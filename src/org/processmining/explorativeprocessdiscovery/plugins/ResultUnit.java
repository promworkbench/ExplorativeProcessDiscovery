package org.processmining.explorativeprocessdiscovery.plugins;

import java.util.HashSet;

public class ResultUnit {
	public String name;
	public double fitness;
	public double precision;
	public double f1;
	public int eventcount;
	public int activitycount;
	public HashSet<String> subset = new HashSet<String>();

	public String toString() {
		return String.format("name[%s] fit[%.2f] pre[%.2f] f1[%.2f]", name, fitness, precision, f1, eventcount, activitycount, subset);
	}
}
