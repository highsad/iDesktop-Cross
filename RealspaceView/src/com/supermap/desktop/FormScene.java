package com.supermap.desktop;

import com.supermap.data.Dataset;
import com.supermap.data.GeoStyle;
import com.supermap.data.GeoStyle3D;
import com.supermap.data.Resources;
import com.supermap.data.SymbolType;
import com.supermap.data.Workspace;
import com.supermap.data.WorkspaceClosingEvent;
import com.supermap.data.WorkspaceClosingListener;
import com.supermap.desktop.Interface.IContextMenuManager;
import com.supermap.desktop.Interface.IFormScene;
import com.supermap.desktop.controls.utilties.SymbolDialogFactory;
import com.supermap.desktop.dialog.DialogSaveAsScene;
import com.supermap.desktop.dialog.symbolDialogs.SymbolDialog;
import com.supermap.desktop.enums.WindowType;
import com.supermap.desktop.event.ActiveLayer3DsChangedEvent;
import com.supermap.desktop.event.ActiveLayer3DsChangedListener;
import com.supermap.desktop.realspaceview.RealspaceViewProperties;
import com.supermap.desktop.ui.FormBaseChild;
import com.supermap.desktop.ui.LayersComponentManager;
import com.supermap.desktop.ui.UICommonToolkit;
import com.supermap.desktop.ui.controls.DialogResult;
import com.supermap.desktop.ui.controls.Layer3DsTree;
import com.supermap.desktop.ui.controls.NodeDataType;
import com.supermap.desktop.ui.controls.TreeNodeData;
import com.supermap.desktop.utilties.SceneUtilties;
import com.supermap.realspace.Layer3D;
import com.supermap.realspace.Layer3DDataset;
import com.supermap.realspace.Layer3DSettingVector;
import com.supermap.realspace.Scene;
import com.supermap.realspace.Selection3D;
import com.supermap.ui.Action3D;
import com.supermap.ui.SceneControl;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class FormScene extends FormBaseChild implements IFormScene, WorkspaceClosingListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private DropTarget dropTargetImpl;

	private SceneControl sceneControl = null;
	private String title = "";
	private Layer3DsTree layer3DsTree;
	private ArrayList<Layer3D> activeLayer3DsList = new ArrayList<Layer3D>();

	// 场景窗口右键菜单
	private JPopupMenu formSceneContextMenu;

	public JPopupMenu getFormSceneContextMenu() {
		return this.formSceneContextMenu;
	}

	// 几何对象右键菜单
	private JPopupMenu geometryContextMenu;

	public JPopupMenu getGeometryContextMenu() {
		return this.geometryContextMenu;
	}

	private transient EventListenerList listenerLists = new EventListenerList();

	private transient TreeSelectionListener layer3DsSelectionListener = new TreeSelectionListener() {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			layer3DsTreeSelectionChanged();
		}
	};

	private transient MouseListener sceneControl_MouseListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			int buttonType = e.getButton();
			int clickCount = e.getClickCount();

			if (buttonType == MouseEvent.BUTTON3 && clickCount == 1 && getSceneControl().getAction() == Action3D.SELECT
					|| getSceneControl().getAction() == Action3D.PAN || getSceneControl().getAction() == Action3D.PAN2) {
				showPopupMenu(e);
			}
		}
	};

	private transient MouseListener layer3DsMouseListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			layer3DsTree_MouseClicked(e);
		}
	};

	private transient KeyListener sceneControl_KeyListener = new KeyAdapter() {
		/**
		 * Invoked when a key has been pressed.
		 */
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				for (int i = 0; i < FormScene.this.sceneControl.getScene().getLayers().getCount(); i++) {
					Layer3D layer3D = FormScene.this.sceneControl.getScene().getLayers().get(i);
					if (layer3D != null) {
						layer3D.getSelection().clear();
					}
				}
			}
		}
	};

	public FormScene() {
		this("");
	}

	public FormScene(String name) {
		this(name, null, null);
	}

	public FormScene(String title, Icon icon, Component component) {
		super(title, icon, component);

		try {
			this.title = title;
			this.sceneControl = new SceneControl();
			this.setComponent(this.sceneControl);

			this.layer3DsTree = UICommonToolkit.getLayersManager().getLayer3DsTree();
			this.layer3DsTree.addTreeSelectionListener(this.layer3DsSelectionListener);
			this.sceneControl.addMouseListener(sceneControl_MouseListener);
			this.sceneControl.addKeyListener(this.sceneControl_KeyListener);

			if (Application.getActiveApplication().getMainFrame() != null) {
				IContextMenuManager manager = Application.getActiveApplication().getMainFrame().getContextMenuManager();

				this.formSceneContextMenu = (JPopupMenu) manager.get("SuperMap.Desktop._FormScene.FormSceneContextMenu");
				this.geometryContextMenu = (JPopupMenu) manager.get("SuperMap.Desktop._FormScene.GeometryContextMenu");
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
		initDrag();
	}

	@Override
	public SceneControl getSceneControl() {
		return this.sceneControl;
	}

	// add by huchenpu 20150706
	// 这里必须要设置工作空间，否则不能显示出来。
	// 而且不能在new SceneControl的时候就设置工作空间，必须等球显示出来的时候才能设置。
	public void setWorkspace(Workspace workspace) {
		if (workspace != null && this.sceneControl != null && this.sceneControl.getScene() != null) {

			// 首先移除上一次的绑定
			if (this.sceneControl.getScene().getWorkspace() != null) {
				this.sceneControl.getScene().getWorkspace().removeClosingListener(this);
			}

			// 再添加本次绑定
			this.sceneControl.getScene().setWorkspace(workspace);
			workspace.addClosingListener(this);
		}
	}

	@Override
	public Layer3D[] getActiveLayer3Ds() {
		return this.activeLayer3DsList.toArray(new Layer3D[this.activeLayer3DsList.size()]);
	}

	@Override
	public void setActiveLayer3Ds(Layer3D[] activeLayer3Ds) {
		Layer3D[] oldActiveLayer3Ds = getActiveLayer3Ds();

		this.layer3DsTree.removeTreeSelectionListener(this.layer3DsSelectionListener);
		if (activeLayer3Ds != null && activeLayer3Ds.length > 0) {
			this.activeLayer3DsList.clear();
			ArrayList<TreePath> paths = new ArrayList<TreePath>();

			for (Layer3D layer3D : activeLayer3Ds) {
				if (this.sceneControl.getScene().getLayers().contains(layer3D.getName())) {
					this.activeLayer3DsList.add(layer3D);

					DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.layer3DsTree.getModel().getRoot();
					for (int i = 0; i < root.getChildCount(); i++) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
						TreeNodeData nodeData = (TreeNodeData) node.getUserObject();

						if (isNodeLayer3D(nodeData.getType()) && nodeData.getData() == layer3D) {
							paths.add(new TreePath(node));
							break;
						}
					}
				}
			}

			this.layer3DsTree.setSelectionPaths(paths.toArray(new TreePath[paths.size()]));
		} else {
			this.layer3DsTree.clearSelection();
			this.activeLayer3DsList.clear();
		}

		this.layer3DsTree.addTreeSelectionListener(this.layer3DsSelectionListener);
		if (oldActiveLayer3Ds != null && oldActiveLayer3Ds.length > 0 && !this.activeLayer3DsList.isEmpty()) {
			fireActiveLayer3DsChanged(new ActiveLayer3DsChangedEvent(this, oldActiveLayer3Ds, getActiveLayer3Ds()));
		}
	}

	@Override
	public String getText() {
		return this.title;
	}

	@Override
	public void setText(String text) {
		this.title = text;
	}

	@Override
	public WindowType getWindowType() {
		return WindowType.SCENE;
	}

	@Override
	public boolean save() {
		boolean result = false;
		try {
			if (this.isNeedSave()) {
				Workspace workspace = this.sceneControl.getScene().getWorkspace();
				if (workspace != null) {
					if (workspace.getScenes().indexOf(this.getText()) >= 0) {
						result = workspace.getScenes().setSceneXML(this.getText(), this.sceneControl.getScene().toXML());
					} else {
						result = save(true, true);
					}
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}

		return result;
	}

	@Override
	public boolean save(boolean notify, boolean isNewWindow) {
		boolean result = false;
		try {
			if (this.isNeedSave()) {
				Workspace workspace = this.sceneControl.getScene().getWorkspace();
				if (workspace != null) {
					if (notify) {
						result = this.saveAs(isNewWindow);
					} else {
						result = workspace.getScenes().add(this.getText(), this.sceneControl.getScene().toXML()) >= 0;
					}
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}

		return result;
	}

	@Override
	public boolean saveAs(boolean isNewWindow) {
		boolean result = false;
		try {
			Workspace workspace = this.sceneControl.getScene().getWorkspace();
			this.saveLayer3DKML();
			DialogSaveAsScene dialogSaveAs = new DialogSaveAsScene();
			dialogSaveAs.setScenes(workspace.getScenes());
			dialogSaveAs.setSceneName(this.getText());
			dialogSaveAs.setIsNewWindow(isNewWindow);
			dialogSaveAs.setFormTitle(this.getText());

			if (dialogSaveAs.showDialog() == DialogResult.YES) {
				result = workspace.getScenes().add(dialogSaveAs.getSceneName(), this.sceneControl.getScene().toXML()) >= 0;
				if (result) {
					this.setText(dialogSaveAs.getSceneName());
					this.sceneControl.getScene().setName(dialogSaveAs.getSceneName());
				}
			} else {
				result = false;
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}

		return result;
	}

	@Override
	public boolean isNeedSave() {
		return true;
	}

	@Override
	public void setNeedSave(boolean needSave) {
		// 未实现
	}

	@Override
	public boolean saveFormInfos() {
		return saveLayer3DKML();
	}

	public final void addActiveLayer3DsChangedListener(ActiveLayer3DsChangedListener listener) {
		this.listenerLists.add(ActiveLayer3DsChangedListener.class, listener);
	}

	public final void removeActiveLayer3DsChangedListener(ActiveLayer3DsChangedListener listener) {
		this.listenerLists.remove(ActiveLayer3DsChangedListener.class, listener);
	}

	protected void fireActiveLayer3DsChanged(ActiveLayer3DsChangedEvent e) {
		Object[] listeners = listenerLists.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ActiveLayer3DsChangedListener.class) {
				((ActiveLayer3DsChangedListener) listeners[i + 1]).activeLayer3DsChanged(e);
			}
		}
	}

	private boolean saveLayer3DKML() {
		boolean result = false;
		try {
			result = true;
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}

		return result;
	}

	private boolean isNodeLayer3D(NodeDataType type) {
		return type == NodeDataType.LAYER3D_DATASET || type == NodeDataType.LAYER3D_IMAGE_FILE || type == NodeDataType.LAYER3D_KML
				|| type == NodeDataType.LAYER3D_MAP || type == NodeDataType.LAYER3D_MODEL || type == NodeDataType.LAYER3D_VECTOR_FILE;
	}

	private void layer3DsTreeSelectionChanged() {
		TreePath[] selectedPaths = this.layer3DsTree.getSelectionPaths();
		Layer3D[] oldActiveLayer3Ds = getActiveLayer3Ds();

		this.activeLayer3DsList.clear();

		if (selectedPaths != null) {
			for (TreePath path : selectedPaths) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

				if (node != null) {
					TreeNodeData nodeData = (TreeNodeData) node.getUserObject();

					if (isNodeLayer3D(nodeData.getType())) {
						this.activeLayer3DsList.add((Layer3D) nodeData.getData());
					}
				}
			}
		}

		if (oldActiveLayer3Ds != null && oldActiveLayer3Ds.length > 0 && !this.activeLayer3DsList.isEmpty()) {
			fireActiveLayer3DsChanged(new ActiveLayer3DsChangedEvent(this, oldActiveLayer3Ds, getActiveLayer3Ds()));
		}
	}

	public void layer3DsTree_MouseClicked(MouseEvent e) {
		try {
			if (e.getButton() == 1 && e.getClickCount() == 2) {
				TreePath path = this.layer3DsTree.getPathForLocation(e.getX(), e.getY());
				if (path != null) {
					Object object = path.getLastPathComponent();
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) object;
					TreeNodeData data = (TreeNodeData) node.getUserObject();

					// 为数据集图层时弹出图层管理器
					if (data.getData() instanceof Layer3DDataset) {
						Layer3DDataset layer3DDataset = (Layer3DDataset) data.getData();
						if (layer3DDataset != null) {
							if (layer3DDataset.getTheme() == null) {
								// 设置图层属性
								this.showStyleSetDialog();
							} else {
								// 修改专题图风格
							}
						} else {
							// 暂时不支持设置矢量缓存图层的风格
						}
					}
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	@Override
	public void actived() {
		try {
			LayersComponentManager layersComponentManager = UICommonToolkit.getLayersManager();
			if (layersComponentManager != null) {
				boolean exist = false;
				for (int i = 0; i < Application.getActiveApplication().getMainFrame().getFormManager().getCount(); i++) {
					if (Application.getActiveApplication().getMainFrame().getFormManager().get(i) instanceof FormScene) {
						FormScene formMap = (FormScene) Application.getActiveApplication().getMainFrame().getFormManager().get(i);
						if (formMap != null && formMap.getText() == this.getText()) {
							exist = true;
							break;
						}
					}
				}

				if (exist) {
					layersComponentManager.setScene(this.getSceneControl().getScene());
					this.layer3DsTree.addTreeSelectionListener(this.layer3DsSelectionListener);
					this.layer3DsTree.addMouseListener(this.layer3DsMouseListener);
					setActiveLayer3Ds(getActiveLayer3Ds());
				} else {
					layersComponentManager.setScene(null);
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	@Override
	public void deactived() {
		try {
			this.layer3DsTree.removeTreeSelectionListener(this.layer3DsSelectionListener);
			this.layer3DsTree.removeMouseListener(this.layer3DsMouseListener);
			if (this.layer3DsTree != null) {
				this.layer3DsTree.setScene(null);
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	/**
	 * 窗体被激活时候触发
	 */
	@Override
	public void windowShown() {
		// 未实现
	}

	/**
	 * 窗体隐藏时触发
	 */
	@Override
	public void windowHidden() {
		this.sceneControl.getScene().close();
	}

	/**
	 * 
	 * 弹出风格设置窗口，返回选中的新风格
	 */
	public void showStyleSetDialog() {
		try {
			SymbolType symbolType = SymbolType.FILL;
			Layer3DSettingVector layerSetting = null;
			TreePath[] selections = this.layer3DsTree.getSelectionPaths();
			for (int index = 0; index < selections.length; index++) {
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selections[index].getLastPathComponent();
				TreeNodeData treeNodeData = (TreeNodeData) treeNode.getUserObject();
				Layer3DDataset tempLayer = (Layer3DDataset) treeNodeData.getData();
				if (tempLayer != null && tempLayer.getTheme() == null && tempLayer.getDataset() != null) {
					if (CommonToolkit.DatasetTypeWrap.isPoint(tempLayer.getDataset().getType())) {
						symbolType = SymbolType.MARKER;
						layerSetting = (Layer3DSettingVector) tempLayer.getAdditionalSetting();
						break;
					} else if (CommonToolkit.DatasetTypeWrap.isLine(tempLayer.getDataset().getType())) {
						symbolType = SymbolType.LINE;
						layerSetting = (Layer3DSettingVector) tempLayer.getAdditionalSetting();
						break;
					} else if (CommonToolkit.DatasetTypeWrap.isRegion(tempLayer.getDataset().getType())) {
						symbolType = SymbolType.FILL;
						layerSetting = (Layer3DSettingVector) tempLayer.getAdditionalSetting();
						break;
					}
				}
			}

			// notify by huchenpu 2015-06-30
			// 多选需要让用户指定设置哪些风格，现在暂时先只处理第一个图层
			if (layerSetting != null && selections.length == 1) {
				GeoStyle3D layerStyle3D = layerSetting.getStyle();
				GeoStyle geostyle = changeGeoStyle(this.getGeoStyle(layerStyle3D), symbolType);
				if (geostyle != null) {
					layerSetting.setStyle(this.getGeoStyle3D(geostyle));
					this.getSceneControl().getScene().refresh();
				}
			}

		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	public DropTarget getDropTargetImpl() {
		return this.dropTargetImpl;
	}

	public void removeLayers(Layer3D[] layer3Ds) {
		try {
			if (layer3Ds != null && layer3Ds.length > 0) {
				String message = "";
				if (layer3Ds.length == 1) {
					message = String.format(RealspaceViewProperties.getString("String_validateRemoveLayer3DMessage"), layer3Ds[0].getCaption());
				} else {
					message = String.format(RealspaceViewProperties.getString("String_validateRemoveRangeMessage"), layer3Ds.length);
				}

				int result = UICommonToolkit.showConfirmDialog(message);
				if (result == JOptionPane.OK_OPTION) {
					for (Layer3D layer3D : layer3Ds) {
						this.getSceneControl().getScene().getLayers().remove(layer3D.getName());
					}
					this.getSceneControl().getScene().refresh();
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	private GeoStyle3D getGeoStyle3D(GeoStyle style) {
		GeoStyle3D style3D = new GeoStyle3D();
		style3D.setFillBackColor(style.getFillBackColor());
		style3D.setFillForeColor(style.getFillForeColor());
		style3D.setFillGradientAngle(style.getFillGradientAngle());
		style3D.setFillGradientMode(style.getFillGradientMode());
		style3D.setFillSymbolID(style.getFillSymbolID());
		style3D.setLineColor(style.getLineColor());
		style3D.setLineSymbolID(style.getLineSymbolID());
		style3D.setLineWidth(style.getLineWidth());
		style3D.setMarker3DRotateX(style.getMarkerAngle());
		style3D.setMarker3DRotateY(style.getMarkerAngle());
		style3D.setMarker3DRotateZ(style.getMarkerAngle());
		style3D.setMarkerSize(style.getMarkerSize().getHeight());
		style3D.setMarkerSymbolID(style.getMarkerSymbolID());
		return style3D;
	}

	private GeoStyle getGeoStyle(GeoStyle3D style3D) {
		GeoStyle style = new GeoStyle();
		style.setFillBackColor(style3D.getFillBackColor());
		style.setFillForeColor(style3D.getFillForeColor());
		style.setFillGradientAngle(style3D.getFillGradientAngle());
		style.setFillGradientMode(style3D.getFillGradientMode());
		style.setFillGradientOffsetRatioX(style3D.getFillGradientOffsetRatioX());
		style.setFillGradientOffsetRatioY(style3D.getFillGradientOffsetRatioY());
		style.setFillSymbolID(style3D.getFillSymbolID());
		style.setLineColor(style3D.getLineColor());
		style.setLineSymbolID(style3D.getLineSymbolID());
		style.setLineWidth(style3D.getLineWidth());
		style.setMarkerSymbolID(style3D.getMarkerSymbolID());
		return style;
	}

	private GeoStyle changeGeoStyle(GeoStyle beforeStyle, SymbolType symbolType) {
		GeoStyle result = null;
		SymbolDialog symbolDialog = null;
		try {
			Resources resources = Application.getActiveApplication().getWorkspace().getResources();

			((JFrame) Application.getActiveApplication().getMainFrame()).setCursor(Cursor.WAIT_CURSOR);
			symbolDialog = SymbolDialogFactory.getSymbolDialog(symbolType);
			DialogResult dialogResult = symbolDialog.showDialog(beforeStyle);
			if (dialogResult == DialogResult.OK) {
				result = symbolDialog.getCurrentGeoStyle();
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		} finally {
			((JFrame) Application.getActiveApplication().getMainFrame()).setCursor(Cursor.DEFAULT_CURSOR);
		}
		return result;
	}

	private void showPopupMenu(MouseEvent e) {
		try {
			Selection3D[] selection3d = this.getSceneControl().getScene().findSelection(true);
			if (selection3d.length > 0) {
				this.getGeometryContextMenu().show((Component) this.getSceneControl(), (int) e.getPoint().getX(), (int) e.getPoint().getY());
			} else {
				this.getFormSceneContextMenu().show((Component) this.getSceneControl(), (int) e.getPoint().getX(), (int) e.getPoint().getY());
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	/**
	 * 拖动实现将数据集添加到当前地图图层
	 */
	private void initDrag() {
		this.dropTargetImpl = new DropTarget(this, new WorkspaceTreeDropTargetAdapter());
	}

	/**
	 * 场景窗口的资源释放只能在主线程，否则会崩溃，因此在关闭工作空间之前，先关闭已打开的场景
	 */
	@Override
	public void workspaceClosing(WorkspaceClosingEvent arg0) {
		if (this.sceneControl != null && this.sceneControl.getScene() != null) {
			this.sceneControl.getScene().close();
		}
	}

	/**
	 * 用于提供所涉及的 DropTarget 的 DnD 操作的通知
	 * 
	 * @author xie
	 */
	private class WorkspaceTreeDropTargetAdapter extends DropTargetAdapter {
		@Override
		public void drop(DropTargetDropEvent dtde) {
			try {
				// 将数据集添加到当前场景
				Dataset[] datasets = Application.getActiveApplication().getActiveDatasets();
				IFormScene formScene = (IFormScene) Application.getActiveApplication().getActiveForm();
				Scene scene = formScene.getSceneControl().getScene();

				for (Dataset dataset : datasets) {
					SceneUtilties.addDatasetToScene(scene, dataset, true);
				}

				scene.refresh();
				UICommonToolkit.getLayersManager().setScene(scene);
			} catch (Exception e) {
				Application.getActiveApplication().getOutput().output(e);
			}

		}
	}
}
