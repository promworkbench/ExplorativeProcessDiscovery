package org.processmining.explorativeprocessdiscovery.plugins;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.explorativeprocessdiscovery.visual.MainVisualConfig;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.models.connections.petrinets.PNRepResultAllRequiredParamConnection;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.plugins.IM;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParamProvider;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.ui.PNAlgorithmStep;
import org.processmining.plugins.petrinet.replayer.ui.PNReplayerUI;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGen;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGenRes;

import nl.tue.astar.AStarException;

@Plugin(name = "Explorative Process Discovery using Activity Projections", parameterLabels = {
		"please input xlog" }, returnLabels = {
				"Result of different projection" }, returnTypes = { ResultUnitList.class })
public class MainProjectionPlugin {
	MainVisualConfig miniprojectVisualconfig = new MainVisualConfig();

	// steps
	int currentStep;
	int nofSteps = 2; // algorithm and parameter

	// gui for each steps
	JComponent[] replaySteps;
	Marking tgrmark;

	// reference
	private IPNReplayParamProvider paramProvider;

	@SuppressWarnings("unchecked")
	@UITopiaVariant(affiliation = "PADS", author = "Yisong Zhang", email = "yisongzhang95@163.com")
	@PluginVariant(variantLabel = "Explorative Process Discovery", requiredParameterLabels = { 0 })
	public ResultUnitList UnionPlugin(UIPluginContext context, XLog input) throws ConnectionCannotBeObtained {
		miniprojectVisualconfig.setInputLog(input);
		miniprojectVisualconfig.setFirstGoBackFlag(0);
		context.log("Plugin is running, please wait and be patient :)");

		// get distinct activity
		HashSet<String> dstActivities = new HashSet<String>();
		int TotalEventCount = 0;
		for (XTrace trace : input) {
			for (XEvent event : trace) {
				dstActivities.add(GetName(event));
				TotalEventCount = TotalEventCount + 1;
			}
		}

		int TotalActivityCount = dstActivities.size();
		miniprojectVisualconfig.setTotalEventCount(TotalEventCount);
		miniprojectVisualconfig.setTotalActivityCount(TotalActivityCount);

		// inductive
		IMMiningDialog dialog = new IMMiningDialog(input);

		InteractionResult result = InteractionResult.CONTINUE;
		int i = 0;
		Funcloop: while (result != InteractionResult.FINISHED) {
			switch (i) {
				case 0 :
					result = ChoiceStep(context, miniprojectVisualconfig);
					if (result == InteractionResult.NEXT || result == InteractionResult.CONTINUE) {
						MissingSelection(context, miniprojectVisualconfig);
						if (miniprojectVisualconfig.getFirstGoBackFlag() == 0) {
							// Show same dialog again
							continue Funcloop;
						}
					}
					break;
				case 1 :
					result = context.showWizard("Mine using Inductive Miner", false, true, dialog);
					if (result == InteractionResult.FINISHED && !IM.confirmLargeLogs(context, input, dialog)) {
						context.getFutureResult(0).cancel(false);
						context.getFutureResult(1).cancel(false);
						context.getFutureResult(2).cancel(false);
						return null;
					}
					break;
			}
			if (result == InteractionResult.NEXT || result == InteractionResult.CONTINUE) {
				i++;
			} else if (result == InteractionResult.PREV) {
				i--;
			} else if (result == InteractionResult.CANCEL) {
				return cancel(context);
			}
		}

		miniprojectVisualconfig.setDialog(dialog);

		if (miniprojectVisualconfig.getVersionFlag() == 0) {
			ResultUnitList results4Full = new ResultUnitList();
			// get subset
			List<HashSet<String>> subsets = GetSubset(dstActivities);
			dstActivities = null;

			// for each projection
			for (HashSet<String> subset : subsets) {
				ResultUnit r = StartCalculation(context, input, dialog, subset);
				results4Full.add(r);
				r = null;
			}

			subsets = null;
			System.gc();
			return results4Full;
		} else if (miniprojectVisualconfig.getVersionFlag() == 1) {
			ResultUnitList results4Lite = new ResultUnitList();
			// get subset
			List<HashSet<String>> subsets = GetChildset(dstActivities);
			dstActivities = null;

			// for each projection
			for (HashSet<String> subset : subsets) {
				ResultUnit r = StartCalculation(context, input, dialog, subset);
				results4Lite.add(r);
				r = null;
			}

			subsets = null;
			System.gc();
			return results4Lite;
		} else if (miniprojectVisualconfig.getVersionFlag() == 2) {
			ResultUnitList results4Best = new ResultUnitList();
			ResultUnitList resultsf1  = new ResultUnitList();
			ResultUnitList resultsfit = new ResultUnitList();
			ResultUnitList resultspre = new ResultUnitList();
			// get subset
			List<HashSet<String>> subsetsf1 = GetChildsetWithoutOri(dstActivities);
			List<HashSet<String>> subsetsfit = GetChildsetWithoutOri(dstActivities);
			List<HashSet<String>> subsetspre = GetChildsetWithoutOri(dstActivities);
			ResultUnit r = StartCalculation(context, input, dialog, dstActivities);
			resultsf1.add(r); resultsfit.add(r); resultspre.add(r);
			dstActivities = null;

			// for each projection
			List<ResultUnit> tempresultf1ALL  = new ArrayList<ResultUnit>();
			while (!subsetsf1.get(0).isEmpty()) {
				List<ResultUnit> tempresultf1  = new ArrayList<ResultUnit>();
				for (HashSet<String> subset : subsetsf1) {
					r = StartCalculation(context, input, dialog, subset);
					if (tempresultf1.isEmpty()){
						tempresultf1.add(r);
					} else if (r.f1 > tempresultf1.get(0).f1) {
						tempresultf1 = new ArrayList<ResultUnit>();
						tempresultf1.add(r);
					} else if (r.f1 == tempresultf1.get(0).f1) {
						tempresultf1.add(r);
					}
				}
				tempresultf1ALL.addAll(tempresultf1);
				subsetsf1 = new ArrayList<HashSet<String>>();
				for (ResultUnit f1 : tempresultf1) {
					subsetsf1.addAll(GetChildsetWithoutOri(f1.subset));
				}
			    @SuppressWarnings("rawtypes")
				HashSet antidup_set = new HashSet(subsetsf1);
			    subsetsf1.clear();
			    subsetsf1.addAll(antidup_set);
			};
			List<ResultUnit> tempresultfitALL = new ArrayList<ResultUnit>();
			while (!subsetsfit.get(0).isEmpty()) {
				List<ResultUnit> tempresultfit = new ArrayList<ResultUnit>();
				for (HashSet<String> subset : subsetsfit) {
					r = StartCalculation(context, input, dialog, subset);
					if (tempresultfit.isEmpty()){
						tempresultfit.add(r);
					} else if (r.fitness > tempresultfit.get(0).fitness) {
						tempresultfit = new ArrayList<ResultUnit>();
						tempresultfit.add(r);
					} else if (r.fitness == tempresultfit.get(0).fitness) {
						tempresultfit.add(r);
					}
				}
				tempresultfitALL.addAll(tempresultfit);
				subsetsfit = new ArrayList<HashSet<String>>();
				for (ResultUnit fit : tempresultfit) {
					subsetsfit.addAll(GetChildsetWithoutOri(fit.subset));
				}
				@SuppressWarnings("rawtypes")
				HashSet antidup_set = new HashSet(subsetsfit);
			    subsetsfit.clear();
			    subsetsfit.addAll(antidup_set);
			};
			List<ResultUnit> tempresultpreALL = new ArrayList<ResultUnit>();
			while (!subsetspre.get(0).isEmpty()) {
				List<ResultUnit> tempresultpre = new ArrayList<ResultUnit>();
				for (HashSet<String> subset : subsetspre) {
					r = StartCalculation(context, input, dialog, subset);
					if (tempresultpre.isEmpty()){
						tempresultpre.add(r);
					} else if (r.precision > tempresultpre.get(0).precision) {
						tempresultpre = new ArrayList<ResultUnit>();
						tempresultpre.add(r);
					} else if (r.precision == tempresultpre.get(0).precision) {
						tempresultpre.add(r);
					}
				}
				tempresultpreALL.addAll(tempresultpre);
				subsetspre = new ArrayList<HashSet<String>>();
				for (ResultUnit pre : tempresultpre) {
					subsetspre.addAll(GetChildsetWithoutOri(pre.subset));
				}
				@SuppressWarnings("rawtypes")
				HashSet antidup_set = new HashSet(subsetspre);
			    subsetspre.clear();
			    subsetspre.addAll(antidup_set);
			};
			for (ResultUnit f1 : tempresultf1ALL) {
				resultsf1.add(f1);
			}; tempresultf1ALL = null;
			for (ResultUnit fit : tempresultfitALL) {
				resultsfit.add(fit);
			}; tempresultfitALL = null;
			for (ResultUnit pre : tempresultpreALL) {
				resultspre.add(pre);
			}; tempresultpreALL = null;
			List<ResultUnitList> tempresults = new ArrayList<ResultUnitList>();
			tempresults.add(resultsfit); tempresults.add(resultspre); tempresults.add(resultsf1);
			miniprojectVisualconfig.setBestRouteResult(tempresults);
			tempresults = null;
			subsetsf1 = null; subsetsfit = null; subsetspre = null;
			System.gc();
			return results4Best;
		} else {
			return null;
		}
	}

	private Object[] showConfiguration(UIPluginContext context, XLog log, PetrinetGraph net,
			TransEvClassMapping mapping) {

		go(1, context, net, log, mapping);

		return new Object[] { mapping, ((PNAlgorithmStep) replaySteps[0]).getAlgorithm(),
				paramProvider.constructReplayParameter(replaySteps[1]) };

	}

	private int go(int direction, UIPluginContext context, PetrinetGraph net, XLog log, TransEvClassMapping mapping) {
		currentStep += direction;

		// check which algorithm is selected and adjust parameter as necessary
		if (currentStep == 1) {
			this.paramProvider = ((PNAlgorithmStep) replaySteps[0]).getAlgorithm().constructParamProvider(context, net,
					log, mapping);
			replaySteps[1] = paramProvider.constructUI();
		}

		if ((currentStep >= 0) && (currentStep < nofSteps)) {
			return currentStep;
		}
		return 0;
	}

	private PNRepResult replayLogPrivate(PluginContext context, PetrinetGraph net, XLog log,
			TransEvClassMapping mapping, IPNReplayAlgorithm selectedAlg, IPNReplayParameter parameters) {
		if (selectedAlg.isAllReqSatisfied(context, net, log, mapping, parameters)) {
			// for each trace, replay according to the algorithm. Only returns two objects
			PNRepResult replayRes = null;

			if (parameters.isGUIMode()) {

				try {
					replayRes = selectedAlg.replayLog(context, net, log, mapping, parameters);
				} catch (AStarException e) {
					e.printStackTrace();
				}

				NumberFormat nf = NumberFormat.getInstance();
				nf.setMinimumFractionDigits(2);
				nf.setMaximumFractionDigits(2);

			} else {
				try {
					replayRes = selectedAlg.replayLog(context, net, log, mapping, parameters);
				} catch (AStarException e) {
					e.printStackTrace();
				}
			}

			// add connection
			if (replayRes != null) {
				if (parameters.isCreatingConn()) {
					createConnections(context, net, log, mapping, selectedAlg, parameters, replayRes);
				}
			}

			return replayRes;
		} else {
			if (context != null) {
				context.getFutureResult(0).cancel(true);
			}
			return null;
		}
	}

	protected void createConnections(PluginContext context, PetrinetGraph net, XLog log, TransEvClassMapping mapping,
			IPNReplayAlgorithm selectedAlg, IPNReplayParameter parameters, PNRepResult replayRes) {
		context.addConnection(new PNRepResultAllRequiredParamConnection("Connection between replay result, "
				+ XConceptExtension.instance().extractName(log) + ", and " + net.getLabel(), net, log, mapping,
				selectedAlg, parameters, replayRes));
	}

	static String ToString(XTrace t) {
		StringBuilder sb = new StringBuilder();
		for (XEvent e : t) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			sb.append(GetName(e));
		}
		return sb.toString();
	}

	static String GetName(XEvent e) {
		return e.getAttributes().get("concept:name").toString();
	}

	static List<HashSet<String>> GetSubset(HashSet<String> es) {
		List<List<String>> subsets = new ArrayList<List<String>>();
		subsets.add(new ArrayList<String>());
		for (String n : es) {
			int size = subsets.size();
			for (int i = 0; i < size; i++) {
				List<String> newSub = new ArrayList<String>(subsets.get(i));
				newSub.add(n);
				subsets.add(newSub);
			}
		}
		subsets.remove(0);

		List<HashSet<String>> ans = new ArrayList<HashSet<String>>();
		for (List<String> sub : subsets) {
			HashSet<String> set = new HashSet<String>();
			for (String n : sub) {
				set.add(n);
			}
			ans.add(set);
		}

		return ans;
	}

	public static Pair<XLog, Integer> GetProjection(XLog input, HashSet<String> subset) {
		int tempEventCount = 0;
		XLog projection = new XLogImpl(new XAttributeMapImpl());
		System.out.println();
		for (XTrace t : input) {
			// 实例化 XTrance
			XTrace nt = new XTraceImpl(new XAttributeMapImpl());
			for (XEvent e : t) {
				if (subset.contains(GetName(e))) {
					XEvent ne = (XEvent) e.clone();
					nt.add(ne);
					tempEventCount = tempEventCount + 1;
				}
			}
			if (!nt.isEmpty()) {
				projection.add(nt);
			}
			nt = null;
		}
		return new Pair<>(projection, tempEventCount);
	}

	public static InteractionResult ChoiceStep(UIPluginContext context, MainVisualConfig miniprojectVisualconfig) {
		ChoiceStep selectF = new ChoiceStep();
		return context.showWizard("Select Function", true, false, selectF);
	}

	private ResultUnitList cancel(final UIPluginContext context) {
		context.getFutureResult(0).cancel(false);
		return null;
	}

	private static boolean MissingSelection(final UIPluginContext context, MainVisualConfig miniprojectVisualconfig) {
		int SFlag = miniprojectVisualconfig.getFirstGoBackFlag();
		String message;
		String title;
		Object[] options = { "Check Selection" };
		if (SFlag == 0) {
			message = "<HTML>You haven't select any function.<BR/> " + "Please go back and select a function.</HTML>";
			title = "Haven't select";
		} else {
			return false;
		}
		int warningResult = JOptionPane.showOptionDialog(context.getGlobalContext().getUI(), message, title,
				JOptionPane.PLAIN_MESSAGE, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		return warningResult == 1; // reconfigure
	}

	static List<HashSet<String>> GetChildset(HashSet<String> modelstr) {
		List<HashSet<String>> ans = new ArrayList<HashSet<String>>();
		ans.add(modelstr);
		for (String sub : modelstr) {
			HashSet<String> set = new HashSet<String>();
			for (String sub2 : modelstr) {
				if (sub.equals(sub2)) {
					;
				} else {
					set.add(sub2);
				}
			}
			ans.add(set);
		}
		return ans;
	}

	static List<HashSet<String>> GetChildsetWithoutOri(HashSet<String> modelstr) {
		List<HashSet<String>> ans = new ArrayList<HashSet<String>>();
		for (String sub : modelstr) {
			HashSet<String> set = new HashSet<String>();
			for (String sub2 : modelstr) {
				if (sub.equals(sub2)) {
					;
				} else {
					set.add(sub2);
				}
			}
			ans.add(set);
		}
		return ans;
	}

	private ResultUnit StartCalculation(UIPluginContext context, XLog input, IMMiningDialog dialog, HashSet<String> subset) throws ConnectionCannotBeObtained {
		StringBuffer sb = new StringBuffer();
		for (String e : subset) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			sb.append(e);
		}

		int tempActivityCount = subset.size();

		XLog tgr = GetProjection(input, subset).getFirst(); // step1
		int tempEventCount = GetProjection(input, subset).getSecond();

		Object[] tempIMresult = IMPetriNet.minePetriNet(context, tgr, dialog.getMiningParameters());

		InitialMarkingConnection Conbigstuff1 = new InitialMarkingConnection((Petrinet) tempIMresult[0],
				(Marking) tempIMresult[1]);
		context.addConnection(Conbigstuff1);
		Conbigstuff1 = null;
		FinalMarkingConnection Conbigstuff2 = new FinalMarkingConnection((Petrinet) tempIMresult[0],
				(Marking) tempIMresult[2]);
		context.addConnection(Conbigstuff2);
		Conbigstuff2 = null;

		PetrinetGraph tmpNet = (Petrinet) tempIMresult[0];

		// replayer
		// init local parameter
		EvClassLogPetrinetConnection conn1 = null;

		int retflag = 1;
		PNRepResult ret = null;

		// check existence of initial marking
		try {
			InitialMarkingConnection initCon = context.getConnectionManager()
					.getFirstConnection(InitialMarkingConnection.class, context, tmpNet);

			tgrmark = (Marking) initCon.getObjectWithRole(InitialMarkingConnection.MARKING);
		} catch (ConnectionCannotBeObtained exc) {
			;
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// check existence of final marking
		try {
			context.getConnectionManager().getFirstConnection(FinalMarkingConnection.class, context, tmpNet);
		} catch (ConnectionCannotBeObtained exc) {
			;
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// check connection in order to determine whether mapping step is needed
		// of not
		try {
			// connection is found, no need for mapping step
			// connection is not found, another plugin to create such connection
			// is automatically
			// executed

			conn1 = context.getConnectionManager().getFirstConnection(EvClassLogPetrinetConnection.class,
					context, tmpNet, tgr);
		} catch (Exception e1) {
			return null;
		}

		TransEvClassMapping mapping1 = null;

		if (retflag == 1) {
			// init gui for each step
			mapping1 = (TransEvClassMapping) conn1
					.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);

			replaySteps = new JComponent[nofSteps];
			replaySteps[0] = new PNAlgorithmStep(context, tmpNet, tgr, mapping1);

			// set current step
			currentStep = 0;

			Object[] resultConfiguration = showConfiguration(context, tgr, tmpNet, mapping1);

			// This connection MUST exists, as it is constructed by the configuration if necessary
			context.getConnectionManager().getFirstConnection(EvClassLogPetrinetConnection.class, context,
					tmpNet, tgr);

			// get all parameters
			IPNReplayAlgorithm selectedAlg = (IPNReplayAlgorithm) resultConfiguration[PNReplayerUI.ALGORITHM];
			IPNReplayParameter algParameters = (IPNReplayParameter) resultConfiguration[PNReplayerUI.PARAMETERS];

			ret = replayLogPrivate(context, tmpNet, tgr,
					(TransEvClassMapping) resultConfiguration[PNReplayerUI.MAPPING], selectedAlg,
					algParameters);
			selectedAlg = null;
			algParameters = null;

			context.getFutureResult(0).setLabel("Result of different projection");
		} else {
			;
		}

		if (ret != null) {
			double fitness = (double) ret.getInfo().get(PNRepResult.TRACEFITNESS);

			// alignment
			ConnectionManager connManager = context.getConnectionManager();
			EvClassLogPetrinetConnection conn = connManager
					.getFirstConnection(EvClassLogPetrinetConnection.class, context, tmpNet, tgr);
			TransEvClassMapping mapping = (TransEvClassMapping) conn
					.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);

			// get marking
			InitialMarkingConnection initMarkingConn = connManager
					.getFirstConnection(InitialMarkingConnection.class, context, tmpNet);
			Marking initMarking = initMarkingConn.getObjectWithRole(InitialMarkingConnection.MARKING);

			AlignmentPrecGen precGen = new AlignmentPrecGen();
			AlignmentPrecGenRes ret2 = precGen.measureConformanceAssumingCorrectAlignment(context, mapping, ret,
					(Petrinet) tmpNet, initMarking, true);
			precGen = null;

			double precision = ret2.getPrecision();
			ret2 = null;
			double f1 = 2.0 / ((1 / precision) + (1 / fitness));

			ResultUnit r = new ResultUnit();
			r.subset = subset;
			r.name = sb.toString();
			r.fitness = fitness;
			r.precision = precision;
			r.f1 = f1;
			r.eventcount = tempEventCount;
			r.activitycount = tempActivityCount;
			return r;
		} else {
			tempIMresult = null;

			double fitness = 0.0 / 0.0;
			double precision = 0.0 / 0.0;
			double f1 = 0.0 / 0.0;

			ResultUnit r = new ResultUnit();
			r.subset = subset;
			r.name = sb.toString();
			r.fitness = fitness;
			r.precision = precision;
			r.f1 = f1;
			r.eventcount = tempEventCount;
			r.activitycount = tempActivityCount;
			return r;
		}
	}
}