package com.supermap.desktop.util;

import com.supermap.data.*;
import com.supermap.data.conversion.*;
import com.supermap.desktop.Application;
import com.supermap.desktop.ImportFileInfo;
import com.supermap.desktop.dataconversion.DataConversionProperties;
import com.supermap.desktop.progress.Interface.UpdateProgressCallable;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.ui.UICommonToolkit;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CancellationException;

public class DataImportCallable extends UpdateProgressCallable {
	private ArrayList<ImportFileInfo> fileInfos;
	private JTable table;

	public DataImportCallable(List<ImportFileInfo> fileInfos, JTable table) {
		this.fileInfos = (ArrayList<ImportFileInfo>) fileInfos;
		this.table = table;
	}

	@Override
	public Boolean call() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		Datasources datasources = Application.getActiveApplication().getWorkspace().getDatasources();
		for (int i = 0; i < datasources.getCount(); i++) {
			map.put(datasources.get(i).getAlias(), 0);
		}
		// 不用了先置空回收对象
		datasources = null;
		try {
			for (int i = 0; i < fileInfos.size(); i++) {
				DataImport dataImport = new DataImport();
				ImportSettings importSettings = dataImport.getImportSettings();
				ImportFileInfo fileInfo = fileInfos.get(i);
				ImportSetting importSetting = fileInfo.getImportSetting();
				importSetting.setSourceFilePath(fileInfo.getFilePath());
				if (importSetting instanceof ImportSettingWOR) {
					Workspace workspace = Application.getActiveApplication().getWorkspace();
					((ImportSettingWOR) importSetting).setTargetWorkspace(workspace);
					((ImportSettingWOR) importSetting).setTargetDatasource(importSetting.getTargetDatasource());
				}
				importSettings.add(importSetting);
				PercentProgress percentProgress = new PercentProgress(i);
				dataImport.addImportSteppedListener(percentProgress);
				long startTime = System.currentTimeMillis(); // 获取开始时间

				ImportResult result = dataImport.run();
				if (null != result.getSucceedSettings() && result.getSucceedSettings().length > 0) {
					map.put(result.getSucceedSettings()[0].getTargetDatasource().getAlias(),
							map.get(result.getSucceedSettings()[0].getTargetDatasource().getAlias()) + 1);
				}
				long endTime = System.currentTimeMillis(); // 获取结束时间
				long time = endTime - startTime;
				printMessage(result, i, time);
				// 更新行
				((FileInfoModel) table.getModel()).updateRows(fileInfos);
				if (null != percentProgress && percentProgress.isCancel()) {
					break;
				}
				dataImport.removeImportSteppedListener(percentProgress);
				dataImport.dispose();
			}
		} catch (Exception e2) {
			Application.getActiveApplication().getOutput().output(e2);
		} finally {
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				if (entry.getValue() > 0) {
					UICommonToolkit.refreshSelectedDatasourceNode(entry.getKey());
				}
			}
		}
		return true;
	}

	/**
	 * 进度事件得到运行时间
	 *
	 * @author Administrator
	 */
	class PercentProgress implements ImportSteppedListener {
		private int i;
		private boolean isCancel = false;

		public PercentProgress(int i) {
			this.i = i;
		}

		public boolean isCancel() {
			return this.isCancel;
		}

		@Override
		public void stepped(ImportSteppedEvent arg0) {
			try {
				double count = fileInfos.size();
				int totalPercent = (int) ((100 * this.i + arg0.getSubPercent()) / count);
				updateProgressTotal(arg0.getSubPercent(),
						MessageFormat.format(CommonProperties.getString("String_TotalTaskNumber"), String.valueOf(fileInfos.size())), totalPercent,
						MessageFormat.format(DataConversionProperties.getString("String_FileInport"), arg0.getCurrentTask().getSourceFilePath()));
			} catch (CancellationException e) {
				arg0.setCancel(true);
				this.isCancel = true;
			}
		}
	}

	/**
	 * 打印导入信息
	 *
	 * @param result
	 * @param i
	 */
	private void printMessage(ImportResult result, int i, long time) {
		ImportSetting[] successImportSettings = result.getSucceedSettings();
		ImportSetting[] failImportSettings = result.getFailedSettings();
		String successImportInfo = DataConversionProperties.getString("String_FormImport_OutPutInfoOne");
		String failImportInfo = DataConversionProperties.getString("String_FormImport_OutPutInfoTwo");
		if (null != successImportSettings && 0 < successImportSettings.length) {
			String[] names = result.getSucceedDatasetNames(successImportSettings[0]);
			// 创建空间索引，字段索引
			fileInfos.get(i).setState(DataConversionProperties.getString("String_FormImport_Succeed"));
			// 导入成功提示信息
			ImportSetting sucessSetting = successImportSettings[0];
			if (null != names && names.length > 0) {
				for (int j = 0; j < names.length; j++) {
					String targetDatasetName = names[j];
					Dataset dataset = sucessSetting.getTargetDatasource().getDatasets().get(targetDatasetName);
					boolean isBuildSpatialIndex = fileInfos.get(i).isBuildSpatialIndex();
					boolean isBuildFiledIndex = fileInfos.get(i).isBuildFiledIndex();
					if (dataset instanceof DatasetVector) {
						if (isBuildFiledIndex) {
							int count = ((DatasetVector) dataset).getFieldInfos().getCount();
							for (int k = 0; k < count; k++) {
								String fieldName = ((DatasetVector) dataset).getFieldInfos().get(k).getName();
								String indexName = MessageFormat.format("{0}_{1}", fieldName, UUID.randomUUID());
								if (indexName.length() > 30) {
									indexName = indexName.substring(0, 30);
								}
								indexName = indexName.replace(DataConversionProperties.getString("String_Horizatal"), "");
								((DatasetVector) dataset).buildFieldIndex(new String[] { fieldName }, indexName);
							}

						} else if (isBuildSpatialIndex && sucessSetting.getSourceFileType() != FileType.DBF) {
							SpatialIndexInfo spatialIndexInfo = new SpatialIndexInfo();

							// 设置空间索引信息对象的信息
							spatialIndexInfo.setGridCenter(dataset.getBounds().getCenter());
							spatialIndexInfo.setType(SpatialIndexType.RTREE);
							((DatasetVector) dataset).buildSpatialIndex(spatialIndexInfo);
						}
					}
					Application
							.getActiveApplication()
							.getOutput()
							.output(MessageFormat.format(successImportInfo, sucessSetting.getSourceFilePath(), "->", targetDatasetName, sucessSetting
									.getTargetDatasource().getAlias(), String.valueOf((time / names.length)/ 1000.0)));
				}
			}

		} else if (null != failImportSettings && 0 < failImportSettings.length) {
			fileInfos.get(i).setState(DataConversionProperties.getString("String_FormImport_NotSucceed"));
			Application.getActiveApplication().getOutput().output(MessageFormat.format(failImportInfo, failImportSettings[0].getSourceFilePath(), "->", ""));
		}
	}

}
