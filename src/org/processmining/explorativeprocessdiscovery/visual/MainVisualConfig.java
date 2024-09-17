package org.processmining.explorativeprocessdiscovery.visual;

import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.jgraph.JGraph;
import org.processmining.explorativeprocessdiscovery.plugins.ResultUnitList;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;

public final class MainVisualConfig {

	public static JGraph MiniZoomGraphYS;
	public static List<String> MiniProjectEdgeSublogPairYS;
	public static List<String> MiniProjectTypeNameYS = Arrays.asList("Fitness","Precision","F1 Score");
	public static int MiniProjectTypeIndexYS;
	public static int MiniProjectTotalEventCountYS;
	public static int MiniProjectTotalActivityCountYS;
	public static JPanel MiniProjectFatherModel;
	public static JPanel MiniProjectSonModel;
	public static JPanel MiniProjectTempModel;
	public static JPanel MiniProjectOptimalModel;
	public static JGraph MiniProjectOptimalGraph;
	public static XLog MiniProjectInputLogYS;
	public static IMMiningDialog MiniProjectDialog;
	public static int MiniProjectVersionFlag;
	public static int MiniProjectFirstGoBackFlag;
	public static List<ResultUnitList> MiniProjectBestRouteResults;
	
	public JGraph getZoom() {
		return MiniZoomGraphYS;
	}
	
	public List<String> getSublogPair() {
		return MiniProjectEdgeSublogPairYS;
	}
	
	public String getTypeName(int Type) {
		return MiniProjectTypeNameYS.get(Type);
	}
	
	public int getType() {
		return MiniProjectTypeIndexYS;
	}
	
	public int getTotalEventCount() {
		return MiniProjectTotalEventCountYS;
	}
	
	public int getTotalActivityCount() {
		return MiniProjectTotalActivityCountYS;
	}
	
	public JPanel getFatherModel() {
		return MiniProjectFatherModel;
	}
	
	public JPanel getSonModel() {
		return MiniProjectSonModel;
	}
	
	public JPanel getTempModel() {
		return MiniProjectTempModel;
	}
	
	public JPanel getOptimalModel() {
		return MiniProjectOptimalModel;
	}
	
	public JGraph getOptimalGraph() {
		return MiniProjectOptimalGraph;
	}
	
	public XLog getInputLog() {
		return MiniProjectInputLogYS;
	}
	
	public IMMiningDialog getDialog() {
		return MiniProjectDialog;
	}
	
	public int getVersionFlag() {
		return MiniProjectVersionFlag;
	}
	
	public int getFirstGoBackFlag() {
		return MiniProjectFirstGoBackFlag;
	}
	
	public List<ResultUnitList> getBestRouteResult() {
		return MiniProjectBestRouteResults;
	}
	
	public void setZoom(JGraph MiniZoomGraphYS) {
		this.MiniZoomGraphYS = MiniZoomGraphYS;
	}
	
	public void setSublogPair(List<String> MiniProjectEdgeSublogPairYS) {
		this.MiniProjectEdgeSublogPairYS = MiniProjectEdgeSublogPairYS;
	}
	
	public void setType(int MiniProjectTypeIndexYS) {
		this.MiniProjectTypeIndexYS = MiniProjectTypeIndexYS;
	}
	
	public void setTotalEventCount(int MiniProjectTotalEventCountYS) {
		this.MiniProjectTotalEventCountYS = MiniProjectTotalEventCountYS;
	}
	
	public void setTotalActivityCount(int MiniProjectTotalActivityCountYS) {
		this.MiniProjectTotalActivityCountYS = MiniProjectTotalActivityCountYS;
	}
	
	public void setFatherModel(JPanel MiniProjectFatherModel) {
		this.MiniProjectFatherModel = MiniProjectFatherModel;
	}
	
	public void setSonModel(JPanel MiniProjectSonModel) {
		this.MiniProjectSonModel = MiniProjectSonModel;
	}
	
	public void setTempModel(JPanel MiniProjectTempModel) {
		this.MiniProjectTempModel = MiniProjectTempModel;
	}
	
	public void setOptimalModel(JPanel MiniProjectOptimalModel) {
		this.MiniProjectOptimalModel = MiniProjectOptimalModel;
	}
	
	public void setOptimalGraph(JGraph MiniProjectOptimalGraph) {
		this.MiniProjectOptimalGraph = MiniProjectOptimalGraph;
	}
	
	public void setInputLog(XLog MiniProjectInputLogYS) {
		this.MiniProjectInputLogYS = MiniProjectInputLogYS;
	}
	
	public void setDialog(IMMiningDialog MiniProjectDialog) {
		this.MiniProjectDialog = MiniProjectDialog;
	}
	
	public void setVersionFlag(int MiniProjectVersionFlag) {
		this.MiniProjectVersionFlag = MiniProjectVersionFlag;
	}
	
	public void setFirstGoBackFlag(int MiniProjectFirstGoBackFlag) {
		this.MiniProjectFirstGoBackFlag = MiniProjectFirstGoBackFlag;
	}
	
	public void setBestRouteResult(List<ResultUnitList> MiniProjectBestRouteResults) {
		this.MiniProjectBestRouteResults = MiniProjectBestRouteResults;
	}
}