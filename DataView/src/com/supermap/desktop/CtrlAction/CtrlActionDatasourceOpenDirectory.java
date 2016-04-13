package com.supermap.desktop.CtrlAction;

import java.io.File;
import java.text.MessageFormat;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.supermap.data.Datasource;
import com.supermap.data.EngineType;
import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.dataview.DataViewProperties;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.ui.UICommonToolkit;
import com.supermap.desktop.ui.controls.TreeNodeData;
import com.supermap.desktop.ui.controls.WorkspaceTree;
import com.supermap.desktop.utilties.SystemPropertyUtilties;

public class CtrlActionDatasourceOpenDirectory extends CtrlAction {

	public CtrlActionDatasourceOpenDirectory(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run() {
		try {
			WorkspaceTree workspaceTree = UICommonToolkit.getWorkspaceManager().getWorkspaceTree();
			Object object = workspaceTree.getSelectionPath().getLastPathComponent();
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) object;
			TreeNodeData data = (TreeNodeData) node.getUserObject();
			Datasource datasource = (Datasource) data.getData();

			String path = datasource.getConnectionInfo().getServer();
			File file = new File(path);
			if (file.exists()) {
				if (SystemPropertyUtilties.isWindows()) {
					Runtime.getRuntime().exec("explorer.exe /select, " + file.getPath());
				} else {
					Application.getActiveApplication().getOutput()
							.output(MessageFormat.format(DataViewProperties.getString("String_LinuxOpenInDirectory"), file.getPath()));
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	@Override
	public boolean enable() {
		boolean enable = false;
		try {
			WorkspaceTree workspaceTree = UICommonToolkit.getWorkspaceManager().getWorkspaceTree();
			if (workspaceTree.getSelectionCount() == 1) {
				TreePath path = workspaceTree.getSelectionPath();
				Object object = path.getLastPathComponent();
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) object;
				TreeNodeData data = (TreeNodeData) node.getUserObject();
				if (data.getData() instanceof Datasource) {
					Datasource datasource = (Datasource) data.getData();
					enable = this.isFileEngine(datasource);
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
		return enable;
	}

	boolean isFileEngine(Datasource datasource) {
		boolean result = false;
		try {
			if ((datasource.getEngineType() == EngineType.IMAGEPLUGINS ||datasource.getEngineType() == EngineType.VECTORFILE || datasource.getEngineType() == EngineType.UDB) && !":memory:".equals(datasource.getConnectionInfo().getServer())) {
				result = true;
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}

		return result;
	}

}
