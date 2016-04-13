package com.supermap.desktop.geometryoperation.editor;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.supermap.data.CursorType;
import com.supermap.data.DatasetVector;
import com.supermap.data.FieldInfo;
import com.supermap.data.FieldInfos;
import com.supermap.data.GeoLine;
import com.supermap.data.GeoLine3D;
import com.supermap.data.GeoLineM;
import com.supermap.data.GeoRegion;
import com.supermap.data.GeoRegion3D;
import com.supermap.data.GeoText;
import com.supermap.data.Geometry;
import com.supermap.data.GeometryType;
import com.supermap.data.Recordset;
import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IFormMap;
import com.supermap.desktop.core.recordset.RecordsetAddNew;
import com.supermap.desktop.core.recordset.RecordsetDelete;
import com.supermap.desktop.geometry.Abstract.IGeometry;
import com.supermap.desktop.geometry.Abstract.IMultiPartFeature;
import com.supermap.desktop.geometry.Implements.DGeometryFactory;
import com.supermap.desktop.geometryoperation.EditEnvironment;
import com.supermap.desktop.mapeditor.MapEditorProperties;
import com.supermap.desktop.utilties.ArrayUtilties;
import com.supermap.desktop.utilties.RecordsetUtilties;
import com.supermap.desktop.utilties.TabularUtilties;
import com.supermap.mapping.Layer;
import com.supermap.mapping.Selection;

public class DecomposeEditor extends AbstractEditor {

	@Override
	public void activate(EditEnvironment environment) {
		decompose(environment);
	}

	private void decompose(EditEnvironment environment) {
		Recordset recordset = null;
		environment.getMapControl().getEditHistory().batchBegin();

		try {
			Layer editLayer = environment.getActiveEditableLayer();

			if (editLayer != null && editLayer.getDataset() instanceof DatasetVector) {
				recordset = ((DatasetVector) editLayer.getDataset()).getRecordset(false, CursorType.DYNAMIC);
				Selection selection = editLayer.getSelection();
				RecordsetDelete delete = new RecordsetDelete(recordset.getDataset(), environment.getMapControl().getEditHistory());
				RecordsetAddNew addNew = new RecordsetAddNew(recordset, environment.getMapControl().getEditHistory());
				delete.begin();
				addNew.begin();

				if (selection != null) {

					// 分解
					for (int i = 0; i < selection.getCount(); i++) {
						int id = selection.get(i);
						recordset.seekID(id);
						IGeometry geometry = DGeometryFactory.create(recordset.getGeometry());
						Geometry[] geometries = null;

						try {
							if (geometry instanceof IMultiPartFeature<?>) {
								IMultiPartFeature<?> multiPartFeature = (IMultiPartFeature<?>) geometry;

								// 该分解的才分解，仅当子对象个数大于1才分解，然后删除原对象
								if (multiPartFeature.getPartCount() > 1) {

									// 获取字段值
									Map<String, Object> fieldValues = RecordsetUtilties.getFieldValues(recordset);

									// 删除字段
									delete.delete(id);

									// 分解并添加对象
									geometries = divide((IMultiPartFeature<?>) geometry);

									for (int j = 0; j < geometries.length; j++) {
										addNew.addNew(geometries[j], fieldValues);
									}
								}
							}
						} finally {
							if (geometry != null) {
								geometry.dispose();
							}

							if (geometries != null && geometries.length > 0) {
								for (int j = 0; j < geometries.length; j++) {
									geometries[j].dispose();
								}
							}
						}
					}

					// 提交删除操作
					delete.update();

					// 提交添加操作
					addNew.update();

					// 清空选择集，选中新增对象
					editLayer.getSelection().clear();
					if (addNew.getAddHistoryIDs().size() > 0) {
						int[] ids = ArrayUtilties.convertToInt(addNew.getAddHistoryIDs().toArray(new Integer[addNew.getAddHistoryIDs().size()]));
						editLayer.getSelection().addRange(ids);
						TabularUtilties.refreshTabularForm((DatasetVector) editLayer.getDataset());
						Application
								.getActiveApplication()
								.getOutput()
								.output(MessageFormat.format(MapEditorProperties.getString("String_GeometryEdit_DecomposeSuccess"), addNew.getAddHistoryIDs()
										.size()));
					}
				}
			}
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		} finally {
			environment.getMapControl().getEditHistory().batchEnd();
			environment.getMapControl().getMap().refresh();

			if (recordset != null) {
				recordset.close();
				recordset.dispose();
			}
		}
	}

	// 保护性分解重写这个
	protected Geometry[] divide(IMultiPartFeature<?> geometry) {
		return geometry.divide();
	}

	@Override
	public boolean enble(EditEnvironment environment) {
		return environment.getEditProperties().getEditableSelectedMaxPartCount() > 1;
	}
}
