package org.processmining.explorativeprocessdiscovery.plugins;

import java.util.HashSet;

public class Result {
	public String sublog;
	public double Fitness;
	public double Percisions;
	public double Score;
	public int eventcount;
	public int activitycount;
	public HashSet<String> subset = new HashSet<String>();

	public Result(String sublog, double Fitness, double Percisions, double Score, int eventcount, int activitycount, HashSet<String> subset) {
		this.sublog = sublog;
		this.Fitness = Fitness;
		this.Percisions = Percisions;
		this.Score = Score;
		this.eventcount = eventcount;
		this.activitycount = activitycount;
		this.subset = subset;
	}

//	public String toString() {
//		return String.format("name[%s] fitness[%.2f] precision[%.2f] f1[%.2f]", sublog, Fitness, Percisions, Score,
//				eventcount, activitycount);
//	}
//
//	static List<Result> GenRandomResult(int n) {
//		return null;
//	}
//
//	public static List<Result> GenResults() {
//		List<Result> result = new ArrayList<Result>();
//
//		result.add(new Result("A,B,C", 1.00, 1.00, 1.00, 1, 1));
//		result.add(new Result("A,B", Double.NaN, 1.00, Double.NaN, 1, 1));
//		result.add(new Result("A,C", Double.NaN, 1.00, Double.NaN, 1, 1));
//		result.add(new Result("B,C", 0.67, 1.00, 0.80, 1, 1));
//		result.add(new Result("A", 1.00, 1.00, 1.00, 1, 1));
//		result.add(new Result("B", 0.78, 1.0, 0.88, 1, 1));
//		result.add(new Result("C", 0.0, Double.NaN, 0.0, 1, 1));
//
//		return result;
//	}
}