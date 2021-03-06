package com.supermap.desktop.progress.callable;

import com.supermap.data.Dataset;
import com.supermap.data.DatasetGrid;
import com.supermap.data.DatasetGridCollection;
import com.supermap.data.DatasetImage;
import com.supermap.data.DatasetImageCollection;
import com.supermap.data.SteppedEvent;
import com.supermap.data.SteppedListener;
import com.supermap.desktop.Application;
import com.supermap.desktop.progress.Interface.UpdateProgressCallable;
import com.supermap.desktop.properties.CoreProperties;

import java.text.MessageFormat;
import java.util.concurrent.CancellationException;

public class DeleteImagePyramidCallable extends UpdateProgressCallable {

	private boolean isCancel = false;
	private int totalDatasetCount = 0;
	private int completedCount = 0;
	private Dataset[] datasets = null;

	private SteppedListener steppedListener = new SteppedListener() {

		@Override
		public void stepped(SteppedEvent arg0) {
			int totalPercent = (arg0.getPercent() + 100 * completedCount) / totalDatasetCount;
			try {
				updateProgressTotal(arg0.getPercent(), totalPercent, String.valueOf(arg0.getRemainTime()), arg0.getMessage());
			} catch (CancellationException e) {
				isCancel = true;
				arg0.setCancel(true);
			}
		}
	};

	public DeleteImagePyramidCallable(Dataset[] datasets) {
		this.datasets = datasets;
		this.totalDatasetCount = this.datasets.length;
		this.completedCount = 0;
	}

	@Override
	public Boolean call() throws Exception {
		if (this.datasets == null || this.datasets.length == 0) {
			return false;
		}

		boolean isSucceeded = false;

		for (Dataset dataset : this.datasets) {
			boolean isDatasetGridOrImage = !(dataset instanceof DatasetGrid) && !(dataset instanceof DatasetImage) && !(dataset instanceof DatasetImageCollection) && !(dataset instanceof DatasetGridCollection);
			if (isCancel || isDatasetGridOrImage) {
				this.completedCount++;
				continue;
			}


			try {
				dataset.addSteppedListener(this.steppedListener);

				if (dataset instanceof DatasetImage) {
					isSucceeded = ((DatasetImage) dataset).removePyramid();
					dataset.close();
				} else if (dataset instanceof DatasetGrid) {
					isSucceeded = ((DatasetGrid) dataset).removePyramid();
					dataset.close();
				} else if (dataset instanceof DatasetGridCollection) {
					isSucceeded = ((DatasetGridCollection) dataset).removePyramid();
					dataset.close();
				} else {
					isSucceeded = ((DatasetImageCollection) dataset).removePyramid();
				}

				if (isSucceeded) {
					String message = MessageFormat.format(CoreProperties.getString("String_DeleteImagePyramid_Success"), dataset.getName());
					Application.getActiveApplication().getOutput().output(message);
				} else {
					String message = MessageFormat.format(CoreProperties.getString("String_DeleteImagePyramid_Failed"), dataset.getName());
					Application.getActiveApplication().getOutput().output(message);
				}
			} finally {
				dataset.removeSteppedListener(this.steppedListener);
			}

			this.completedCount++;
		}
		return true;
	}

}
