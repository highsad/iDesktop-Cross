package com.supermap.desktop.geometryoperation.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.supermap.data.CursorType;
import com.supermap.data.DatasetType;
import com.supermap.data.DatasetVector;
import com.supermap.data.EditType;
import com.supermap.data.GeoCompound;
import com.supermap.data.GeoLine;
import com.supermap.data.GeoLine3D;
import com.supermap.data.GeoLineM;
import com.supermap.data.GeoRegion;
import com.supermap.data.GeoRegion3D;
import com.supermap.data.GeoText;
import com.supermap.data.Geometry;
import com.supermap.data.Recordset;
import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IFormMap;
import com.supermap.desktop.core.recordset.RecordsetDelete;
import com.supermap.desktop.geometry.Abstract.IGeometry;
import com.supermap.desktop.geometry.Implements.DGeometryFactory;
import com.supermap.desktop.geometryoperation.EditEnvironment;
import com.supermap.desktop.geometryoperation.JDialogFieldOperationSetting;
import com.supermap.desktop.mapeditor.PluginEnvironment;
import com.supermap.desktop.mapeditor.MapEditorProperties;
import com.supermap.desktop.ui.controls.DialogResult;
import com.supermap.desktop.utilties.GeometryUtilties;
import com.supermap.desktop.utilties.MapUtilties;
import com.supermap.desktop.utilties.TabularUtilties;
import com.supermap.mapping.Layer;
import com.supermap.mapping.Selection;

//@formatter:off
/**
* 1.组合之后更改 Selection，无法触发 GeometrySelected 
* 2.暂时没有实现刷新打开的属性窗口
* 3.221行，.net 没有释放，因为大数据会崩溃，测试可以留意一下是否如此
* 4.需要想办法解决编辑功能 enable() 频繁读写 recordset 的问题
* 
* @author highsad
*
*/
//@formatter:on
public class CombinationEditor extends AbstractEditor {

	@Override
	public void activate(EditEnvironment environment) {
		try {
			EditEnvironment geometryEdit = PluginEnvironment.getGeometryEditManager().instance();
			DatasetType datasetType = DatasetType.CAD;
			if (environment.getEditProperties().getSelectedDatasetTypes().size() == 1) {
				datasetType = environment.getEditProperties().getSelectedDatasetTypes().get(0);
			}

			JDialogFieldOperationSetting formCombination = new JDialogFieldOperationSetting(
					MapEditorProperties.getString("String_GeometryOperation_Combination"), geometryEdit.getMap(), datasetType);
			if (formCombination.showDialog() == DialogResult.OK) {
				combination(environment, formCombination.getEditLayer(), formCombination.getPropertyData());
				TabularUtilties.refreshTabularForm((DatasetVector) formCombination.getEditLayer().getDataset());
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	@Override
	public boolean enble(EditEnvironment environment) {
		boolean result = false;
		try {
			if (environment.getEditProperties().getSelectedGeometryCount() > 1) {
				if (environment.getEditProperties().getSelectedDatasetTypes().size() > 1)// 多种数据集时，目标要为CAD
				{
					if (environment.getEditProperties().getEditableDatasetTypes().contains(DatasetType.CAD)) {
						result = true;
					}
				} else if (environment.getEditProperties().getSelectedDatasetTypes().size() == 1) // 只有一种时，目标相同或为CAD
				{
					if (!(environment.getEditProperties().getSelectedDatasetTypes().get(0) == DatasetType.POINT
							|| environment.getEditProperties().getSelectedDatasetTypes().get(0) == DatasetType.POINT3D)) {
						if (environment.getEditProperties().getEditableDatasetTypes().contains(DatasetType.CAD) || environment.getEditProperties()
								.getEditableDatasetTypes().contains(environment.getEditProperties().getSelectedDatasetTypes().get(0))) {
							result = true;
						}
					}
				} else {
					// 不做任何处理
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
		return result;
	}

	/**
	 * @param environment
	 * @param editLayer
	 * @param propertyData
	 */
	private void combination(EditEnvironment environment, Layer editLayer, Map<String, Object> propertyData) {
		Recordset editRecordset = null;
		IGeometry geometry = null;
		environment.getFormMap().getMapControl().getEditHistory().batchBegin();

		try {
			editRecordset = ((DatasetVector) editLayer.getDataset()).getRecordset(false, CursorType.DYNAMIC);

			// 创建结果对象
			geometry = DGeometryFactory.createNew(editLayer.getDataset().getType());

			// 获取有选中对象的图层
			List<Layer> selectedLayers = environment.getEditProperties().getSelectedLayers();

			for (Layer layer : selectedLayers) {
				geometry = GeometryUtilties.combination(geometry, layer);
			}

			// 删除目标图层选中的对象
			RecordsetDelete delete = new RecordsetDelete(editRecordset.getDataset(), environment.getMapControl().getEditHistory());
			delete.begin();
			for (int i = 0; i < editLayer.getSelection().getCount(); i++) {
				delete.delete(editLayer.getSelection().get(i));
			}
			delete.update();

			// 添加结果对象到目标图层
			editRecordset.addNew(geometry.getGeometry(), propertyData);
			editRecordset.update();

			// 添加历史记录
			environment.getMapControl().getEditHistory().add(EditType.ADDNEW, editRecordset, true);

			// 清空目标图层选择集并选中组合之后的几何对象
			int resultID = editRecordset.getID();
			editLayer.getSelection().clear();
			editLayer.getSelection().add(resultID);

			Application.getActiveApplication().getOutput().output(MapEditorProperties.getString("String_Completed"));
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		} finally {
			environment.getFormMap().getMapControl().getEditHistory().batchEnd();
			environment.getMapControl().getMap().refresh();

			if (editRecordset != null) {
				editRecordset.close();
				editRecordset.dispose();
			}

			if (geometry != null) {
				geometry.dispose();
			}
		}
	}
}
