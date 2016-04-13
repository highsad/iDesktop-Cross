package com.supermap.desktop.CtrlAction;

import com.supermap.data.Dataset;
import com.supermap.data.DatasetType;
import com.supermap.desktop.Application;
import com.supermap.desktop.CommonToolkit;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.Interface.IFormScene;
import com.supermap.desktop.enums.WindowType;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.ui.UICommonToolkit;
import com.supermap.desktop.utilties.SceneUtilties;
import com.supermap.realspace.Scene;

public class CtrlActionDatasetAddToNewScene extends CtrlAction {

	public CtrlActionDatasetAddToNewScene(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run() {
		try {
			Dataset[] datasets = Application.getActiveApplication().getActiveDatasets();
			String name = SceneUtilties.getAvailableSceneName(String.format("%s@%s", datasets[0].getName(), datasets[0].getDatasource().getAlias()),
					true);
			IFormScene formScene = (IFormScene) CommonToolkit.FormWrap.fireNewWindowEvent(WindowType.SCENE, name);
			if (formScene != null) {
				Scene scene = formScene.getSceneControl().getScene();
				// add by huchenpu 20150703
				// 这里必须要设置工作空间，否则不能显示出来。
				// 而且不能在new SceneControl的时候就设置工作空间，必须等球显示出来的时候才能设置。
				scene.setWorkspace(Application.getActiveApplication().getWorkspace());
				for (Dataset dataset : datasets) {
					SceneUtilties.addDatasetToScene(scene, dataset, true);
				}
				scene.refresh();
				UICommonToolkit.getLayersManager().setScene(scene);
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	@Override
	public boolean enable() {
		boolean enable = false;
		try {
			Dataset[] datasets = Application.getActiveApplication().getActiveDatasets();
			if (datasets != null && datasets.length > 0 && datasets[0].getType() != DatasetType.TABULAR && datasets[0].getType() != DatasetType.TOPOLOGY) {
				enable = true;
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
		return enable;
	}
}
