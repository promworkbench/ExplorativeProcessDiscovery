package org.processmining.explorativeprocessdiscovery.plugins;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.explorativeprocessdiscovery.visual.MainProjectionVisPanelBestRoute;
import org.processmining.explorativeprocessdiscovery.visual.MainProjectionVisPanelFull;
import org.processmining.explorativeprocessdiscovery.visual.MainProjectionVisPanelLite;
import org.processmining.explorativeprocessdiscovery.visual.MainVisualConfig;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;

public class F1ScoreVisualizer {
	MainVisualConfig miniprojectVisualconfig = new MainVisualConfig();

	@Plugin(name = "@0 F1 Score Visualizer", level = PluginLevel.PeerReviewed, returnLabels = {
			"Visualized Nodes" }, returnTypes = {
					JComponent.class }, parameterLabels = { "Nodes" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, ResultUnitList ret) {
		if (miniprojectVisualconfig.getVersionFlag() == 0) {
			List<Result> results = new ArrayList<Result>();
			for (ResultUnit i : ret) {
				Result r = new Result(i.name, i.fitness, i.precision, i.f1, i.eventcount, i.activitycount, i.subset);
				results.add(r);
			}
			Comparator<Result> comparatorAge = new Comparator<Result>() {
				public int compare(Result p1, Result p2) {
					int i1 = p1.sublog.split(",").length, i2 = p2.sublog.split(",").length;
					return i2 - i1;
				}
			};
			results.sort(comparatorAge);
			miniprojectVisualconfig.setType(2);
			return new MainProjectionVisPanelFull(context, results, 2);
		} else if (miniprojectVisualconfig.getVersionFlag() == 1) {
			List<Result> results = new ArrayList<Result>();
			for (ResultUnit i : ret) {
				Result r = new Result(i.name, i.fitness, i.precision, i.f1, i.eventcount, i.activitycount, i.subset);
				results.add(r);
			}
			return new MainProjectionVisPanelLite(context, results, 2);
		} else if (miniprojectVisualconfig.getVersionFlag() == 2) {
			List<Result> results = new ArrayList<Result>();
			for (ResultUnit i : miniprojectVisualconfig.getBestRouteResult().get(2)) {
				Result r = new Result(i.name, i.fitness, i.precision, i.f1, i.eventcount, i.activitycount, i.subset);
				results.add(r);
			}
			Comparator<Result> comparatorAge = new Comparator<Result>() {
				public int compare(Result p1, Result p2) {
					int i1 = p1.sublog.split(",").length, i2 = p2.sublog.split(",").length;
					return i2 - i1;
				}
			};
			results.sort(comparatorAge);
			miniprojectVisualconfig.setType(2);
			return new MainProjectionVisPanelBestRoute(context, results, 2);
		} else {
			return null;
		}
	}
}
