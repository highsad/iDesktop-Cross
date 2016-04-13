package com.supermap.desktop.datatopology.CtrlAction;

import com.supermap.data.*;
import com.supermap.data.topology.TopologyPreprocessOptions;
import com.supermap.data.topology.TopologyValidator;
import com.supermap.desktop.Application;
import com.supermap.desktop.datatopology.DataTopologyProperties;
import com.supermap.desktop.progress.Interface.UpdateProgressCallable;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.ui.controls.mutiTable.component.MutiTable;
import com.supermap.desktop.ui.controls.mutiTable.component.MutiTableModel;

import java.text.MessageFormat;
import java.util.concurrent.CancellationException;

public class TopoPregressCallable extends UpdateProgressCallable {
	private MutiTable table;
	private double tolerance;
	private TopologyPreprocessOptions options;
	private PercentListener percentListener;

	public TopoPregressCallable(MutiTable table, double tolerance, TopologyPreprocessOptions options) {
		this.table = table;
		this.tolerance = tolerance;
		this.options = options;
	}

	@Override
	public Boolean call() throws Exception {
		try {
			int count = table.getRowCount();
			MutiTableModel model = (MutiTableModel) table.getModel();
			DatasetVector[] datasets = new DatasetVector[count];
			int[] precisionOrders = new int[count];
			if (0 < count) {
				for (int i = 0; i < count; i++) {
					String datasetName = model.getTagValue(i).get(1).toString();
					String datasourceName = model.getTagValue(i).get(2).toString();
					Datasource datasource = Application.getActiveApplication().getWorkspace().getDatasources().get(datasourceName);
					datasets[i] = (DatasetVector) datasource.getDatasets().get(datasetName);
					TopologyDatasetRelationItem item = new TopologyDatasetRelationItem(datasets[i]);
					precisionOrders[i] = item.getPrecisionOrder();
				}
				percentListener = new PercentListener();
				TopologyValidator.addSteppedListener(percentListener);
				long startTime = System.currentTimeMillis();
				boolean topologyPreprocessResult = TopologyValidator.preprocess(datasets, precisionOrders, options, tolerance);
				String time = String.valueOf((System.currentTimeMillis() - startTime)/1000.0);
				String topologyPreprocessInfo = "";
				if (topologyPreprocessResult) {
					topologyPreprocessInfo = MessageFormat.format(DataTopologyProperties.getString("String_Message_PreprogressSuccess"), time);
				} else {
					topologyPreprocessInfo = DataTopologyProperties.getString("String_Message_PreprogressFailed");
				}
				Application.getActiveApplication().getOutput().output(topologyPreprocessInfo);
			}
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		} finally {
			TopologyValidator.removeSteppedListener(percentListener);
		}
		return true;
	}

	class PercentListener implements SteppedListener {
		PercentListener() {
			//do nothing
		}

		@Override
		public void stepped(SteppedEvent arg0) {
			try {
				updateProgress(arg0.getPercent(), String.valueOf(arg0.getRemainTime()), arg0.getMessage());
			} catch (CancellationException e) {
				arg0.setCancel(true);
			}

		}

	}
}
