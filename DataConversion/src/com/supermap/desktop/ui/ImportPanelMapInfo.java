package com.supermap.desktop.ui;

import com.supermap.data.Datasource;
import com.supermap.data.conversion.ImportSetting;
import com.supermap.data.conversion.ImportSettingMIF;
import com.supermap.data.conversion.ImportSettingTAB;
import com.supermap.desktop.ImportFileInfo;
import com.supermap.desktop.dataconversion.DataConversionProperties;
import com.supermap.desktop.ui.controls.CharsetComboBox;
import com.supermap.desktop.ui.controls.DatasetComboBox;
import com.supermap.desktop.ui.controls.DatasourceComboBox;
import com.supermap.desktop.ui.controls.GridBagConstraintsHelper;
import com.supermap.desktop.ui.controls.button.SmButton;
import com.supermap.desktop.util.CommonComboBoxModel;
import com.supermap.desktop.util.CommonFunction;
import com.supermap.desktop.util.ImportInfoUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator 实现右侧导入tab,mif数据类型的界面
 */
public class ImportPanelMapInfo extends AbstractImportPanel {

	private static final long serialVersionUID = 1L;
	private SmButton buttonProperty;
	private JCheckBox checkboxFieldIndex;
	private JCheckBox checkboxImportPropertyInfo;
	private JCheckBox checkboxSpatialIndex;
	private JComboBox<Object> comboBoxImportModel;
	private transient CharsetComboBox comboBoxCharset;
	private JComboBox<Object> comboBoxCodingType;
	private transient DatasetComboBox comboBoxDataType;
	private transient DatasourceComboBox comboBoxDatasource;
	private JLabel labelCharset;
	private JLabel labelCodingType;
	private JLabel labelFilePath;
	private JLabel labelDataset;
	private JLabel labelDatasetType;
	private JLabel labelDatasource;
	private JLabel labelImportModel;
	private JPanel panelResultSet;
	private JPanel panelDatapath;
	private JPanel panelTransform;
	private JTextField textFieldFilePath;
	private JTextField textFieldResultSet;
	private transient ImportFileInfo fileInfo;
	private transient ImportSetting importsetting = null;
	private ArrayList<ImportFileInfo> fileInfos;
	private ArrayList<JPanel> panels;
	private transient DataImportFrame dataImportFrame;

	private ActionListener comboBoxDataTypeAction;
	private ActionListener checkboxFieldIndexAction;
	private ActionListener checkboxSpatialIndexAction;
	private ActionListener checkboxImportPropertyInfoAction;
	private ActionListener buttonPropertyAction;

	public ImportPanelMapInfo(DataImportFrame dataImportFrame, ImportFileInfo fileInfo) {
		this.dataImportFrame = dataImportFrame;
		this.fileInfo = fileInfo;
		initComponents();
		initResource();
		registActionListener();
	}

	public ImportPanelMapInfo(List<ImportFileInfo> fileInfos, List<JPanel> panels) {
		this.fileInfos = (ArrayList<ImportFileInfo>) fileInfos;
		this.panels = (ArrayList<JPanel>) panels;
		initComponents();
		initResource();
		registActionListener();
	}

	@Override
	void initResource() {
		this.labelFilePath.setText(DataConversionProperties.getString("string_label_lblDataPath"));
		this.labelCharset.setText(DataConversionProperties.getString("string_label_lblCharset"));
		this.labelImportModel.setText(DataConversionProperties.getString("string_label_lblImportType"));
		this.labelDatasource.setText(DataConversionProperties.getString("string_label_lblDatasource"));
		this.labelDataset.setText(DataConversionProperties.getString("string_label_targetDataset"));
		this.labelCodingType.setText(DataConversionProperties.getString("string_label_lblCodingtype"));
		this.labelDatasetType.setText(DataConversionProperties.getString("string_label_lblDatasetType"));
		this.checkboxImportPropertyInfo.setText(DataConversionProperties.getString("string_checkbox_chckbxImportProperty"));
		this.checkboxFieldIndex.setText(DataConversionProperties.getString("string_checkbox_chckbxFieldIndex"));
		this.checkboxSpatialIndex.setText(DataConversionProperties.getString("string_checkbox_chckbxSpatialIndex"));
		this.buttonProperty.setText(DataConversionProperties.getString("string_button_property"));
		this.panelResultSet.setBorder(new TitledBorder(null, DataConversionProperties.getString("string_border_panel"), TitledBorder.LEADING, TitledBorder.TOP,
				null, null));
		this.panelTransform.setBorder(new TitledBorder(null,
				DataConversionProperties.getString("string_border_panelTransform"),
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.panelDatapath.setBorder(new TitledBorder(null, DataConversionProperties.getString("string_border_panelDatapath"),
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.comboBoxCharset.setModel(new CommonComboBoxModel());
		this.comboBoxCharset.setAutoscrolls(true);
		this.comboBoxImportModel.setModel(new DefaultComboBoxModel<Object>(
				new String[]{
						DataConversionProperties.getString("string_comboboxitem_null"),
						DataConversionProperties.getString("string_comboboxitem_add"),
						DataConversionProperties.getString("string_comboboxitem_cover")}));
		this.comboBoxCodingType.setModel(new DefaultComboBoxModel<Object>(
				new String[]{
						DataConversionProperties.getString("string_comboboxitem_nullcoding"),
						DataConversionProperties.getString("string_comboboxitem_byte"),
						DataConversionProperties.getString("string_comboboxitem_int16"),
						DataConversionProperties.getString("string_comboboxitem_int24"),
						DataConversionProperties.getString("string_comboboxitem_int32")}));
	}

	private void setDatasetType(String dataType, ImportSetting importsetting) {
		if (dataType.equals(DataConversionProperties
				.getString("string_comboboxitem_composite"))) {
			if (importsetting instanceof ImportSettingTAB) {
				((ImportSettingTAB) importsetting).setImportingAsCAD(true);
			}
			if (importsetting instanceof ImportSettingMIF) {
				((ImportSettingMIF) importsetting).setImportingAsCAD(true);
			}
		} else {
			if (importsetting instanceof ImportSettingTAB) {
				((ImportSettingTAB) importsetting).setImportingAsCAD(false);
			}
			if (importsetting instanceof ImportSettingMIF) {
				((ImportSettingMIF) importsetting).setImportingAsCAD(false);
			}
		}
	}

	private void setImport(boolean isImport, ImportSetting importsetting) {
		if (importsetting instanceof ImportSettingTAB) {
			((ImportSettingTAB) importsetting).setAttributeIgnored(isImport);
		}
		if (importsetting instanceof ImportSettingMIF) {
			((ImportSettingMIF) importsetting).setAttributeIgnored(isImport);
		}
	}

	@Override
	void initComponents() {

		this.panelResultSet = new JPanel();
		this.labelDatasource = new JLabel();
		this.comboBoxDatasource = new DatasourceComboBox();
		this.labelDataset = new JLabel();
		this.textFieldResultSet = new JTextField();
		this.textFieldResultSet.setColumns(10);
		this.labelCodingType = new JLabel();
		this.comboBoxCodingType = new JComboBox<Object>();
		this.labelDatasetType = new JLabel();
		this.checkboxFieldIndex = new JCheckBox();
		this.checkboxSpatialIndex = new JCheckBox();
		this.panelTransform = new JPanel();
		this.labelImportModel = new JLabel();
		this.comboBoxImportModel = new JComboBox<Object>();
		this.checkboxImportPropertyInfo = new JCheckBox();
		this.checkboxImportPropertyInfo.setSelected(true);
		this.panelDatapath = new JPanel();
		this.labelFilePath = new JLabel();
		this.textFieldFilePath = new JTextField();
		this.textFieldFilePath.setEditable(false);
		this.buttonProperty = new SmButton();
		this.labelCharset = new JLabel();
		this.comboBoxCharset = new CharsetComboBox();

		Datasource datasource = CommonFunction.getDatasource();
		this.comboBoxDatasource.setSelectedDatasource(datasource);

		// 设置目标数据源
		ImportInfoUtil.setDataSource(panels, fileInfos, fileInfo, comboBoxDatasource);

		// 设置fileInfo
		this.importsetting = ImportInfoUtil.setFileInfo(datasource, fileInfos, fileInfo,
				textFieldFilePath, importsetting, textFieldResultSet);
		// 设置目标数据集名称
		ImportInfoUtil.setDatasetName(textFieldResultSet, importsetting);
		// 设置编码格式
		ImportInfoUtil.setCodingType(panels, importsetting, comboBoxCodingType);
		// 设置导入模式
		ImportInfoUtil.setImportMode(panels, importsetting, comboBoxImportModel);
		// 设置源文件字符集
		ImportInfoUtil.setCharset(panels, importsetting, comboBoxCharset);

		initPanelResultSet();

		initpanelTransform();

		initPanelDatapath();

		initPanelMapInfo();
	}

	private void initPanelMapInfo() {
		//@formatter:off
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup().addComponent(this.panelResultSet,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,Short.MAX_VALUE)
				   .addComponent(this.panelTransform,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,Short.MAX_VALUE)
				   .addComponent(panelDatapath,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,Short.MAX_VALUE));
		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup().addComponent(this.panelResultSet,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
				   .addContainerGap().addComponent(this.panelTransform, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
				   .addContainerGap().addComponent(panelDatapath, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE));
		this.setLayout(groupLayout);
		//@formatter:on		
	}

	private void initPanelDatapath() {
		//@formatter:off
		this.panelDatapath.setLayout(new GridBagLayout());
		this.panelDatapath.add(this.labelFilePath,     new GridBagConstraintsHelper(0, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(10, 1).setInsets(10, 10, 5, 5));
		this.panelDatapath.add(this.textFieldFilePath, new GridBagConstraintsHelper(1, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(70, 1).setInsets(10, 0, 5, 5).setFill(GridBagConstraints.HORIZONTAL));
		this.panelDatapath.add(this.buttonProperty,    new GridBagConstraintsHelper(2, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(20, 1).setInsets(10, 0, 5, 10).setFill(GridBagConstraints.HORIZONTAL));
		this.panelDatapath.add(this.labelCharset,      new GridBagConstraintsHelper(0, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(10, 1).setInsets(0, 10, 10, 5));
		this.panelDatapath.add(this.comboBoxCharset,   new GridBagConstraintsHelper(1, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(40, 1).setInsets(0, 0, 10, 5).setIpad(20, 0));
		//@formatter:on
	}

	private void initpanelTransform() {
		//@formatter:off
		this.panelTransform.setLayout(new GridBagLayout());
		this.panelTransform.add(this.labelImportModel,          new GridBagConstraintsHelper(0, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(10, 1).setInsets(10, 10, 10, 5));
		this.panelTransform.add(this.comboBoxImportModel,       new GridBagConstraintsHelper(1, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(35, 1).setInsets(10, 0, 10, 20).setFill(GridBagConstraints.HORIZONTAL));
		this.panelTransform.add(this.checkboxImportPropertyInfo,new GridBagConstraintsHelper(2, 0, 2, 1).setAnchor(GridBagConstraints.WEST).setWeight(50, 1).setInsets(10, 0, 10, 10));
		//@formatter:on
	}

	private void initPanelResultSet() {
		//@formatter:off
		this.comboBoxDataType = new DatasetComboBox(new String[] { DataConversionProperties.getString("string_comboboxitem_composite"),
				DataConversionProperties.getString("string_comboboxitem_sample") });
		this.comboBoxDataType.setSelectedIndex(1);
		this.panelResultSet.setLayout(new GridBagLayout());
		this.panelResultSet.add(this.labelDatasource,      new GridBagConstraintsHelper(0, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(10, 1).setInsets(10, 10, 5, 5));
		this.panelResultSet.add(this.comboBoxDatasource,   new GridBagConstraintsHelper(1, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(40, 1).setInsets(10, 0, 5, 20).setFill(GridBagConstraints.HORIZONTAL));
		this.panelResultSet.add(this.labelDataset,         new GridBagConstraintsHelper(2, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(10, 1).setInsets(10, 0, 5, 5));
		this.panelResultSet.add(this.textFieldResultSet,   new GridBagConstraintsHelper(3, 0, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(40, 1).setInsets(10, 0, 5, 10).setFill(GridBagConstraints.HORIZONTAL));
		this.panelResultSet.add(this.labelCodingType,      new GridBagConstraintsHelper(0, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(10, 1).setInsets(0, 10, 10, 5));
		this.panelResultSet.add(this.comboBoxCodingType,   new GridBagConstraintsHelper(1, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(40, 1).setInsets(0, 0, 10, 20).setFill(GridBagConstraints.HORIZONTAL));
		this.panelResultSet.add(this.labelDatasetType,     new GridBagConstraintsHelper(2, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(10, 1).setInsets(0, 0, 10, 5));
		this.panelResultSet.add(this.comboBoxDataType,     new GridBagConstraintsHelper(3, 1, 1, 1).setAnchor(GridBagConstraints.WEST).setWeight(40, 1).setInsets(0, 0, 10, 10).setFill(GridBagConstraints.HORIZONTAL));
		this.panelResultSet.add(this.checkboxFieldIndex,   new GridBagConstraintsHelper(0, 2, 2, 1).setAnchor(GridBagConstraints.WEST).setWeight(10, 1).setInsets(0, 10, 10, 20));
		this.panelResultSet.add(this.checkboxSpatialIndex, new GridBagConstraintsHelper(2, 2, 2, 1).setAnchor(GridBagConstraints.WEST).setWeight(10, 1).setInsets(0, 0, 10, 10));
		// @formatter:on
	}

	public JComboBox<Object> getComboBox() {
		return comboBoxImportModel;
	}

	public CharsetComboBox getComboBoxCharset() {
		return comboBoxCharset;
	}

	public JComboBox<Object> getComboBoxCodingType() {
		return comboBoxCodingType;
	}

	public JCheckBox getChckbxFieldIndex() {
		return checkboxFieldIndex;
	}

	public JCheckBox getChckbxImport() {
		return checkboxImportPropertyInfo;
	}

	public JCheckBox getChckbxSpatialIndex() {
		return checkboxSpatialIndex;
	}

	public DatasetComboBox getComboBoxDataType() {
		return comboBoxDataType;
	}

	public DatasourceComboBox getComboBoxDatasource() {
		return comboBoxDatasource;
	}

	@Override
	void registActionListener() {
		// 设置数据集类型
		this.comboBoxDataTypeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String dataType = comboBoxDataType.getSelectItem();
				int select = comboBoxDataType.getSelectedIndex();
				if (null != importsetting) {
					setDatasetType(dataType, importsetting);
				} else {
					for (int i = 0; i < panels.size(); i++) {
						ImportPanelMapInfo tempPanel = (ImportPanelMapInfo) panels
								.get(i);
						tempPanel.getComboBoxDataType().setSelectedIndex(select);
					}
				}
			}
		};
		// 是否创建字段索引
		this.checkboxFieldIndexAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean isField = checkboxFieldIndex.isSelected();
				if (null != fileInfos) {
					for (int i = 0; i < panels.size(); i++) {
						ImportPanelMapInfo tempJPanel = (ImportPanelMapInfo) panels.get(i);
						tempJPanel.getChckbxFieldIndex().setSelected(isField);
						fileInfos.get(i).setBuildFiledIndex(isField);
					}
				} else if (null != fileInfo) {
					fileInfo.setBuildFiledIndex(isField);
				}
			}
		};
		// 是否创建空间索引
		this.checkboxSpatialIndexAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean isSpatial = checkboxSpatialIndex.isSelected();
				if (null != importsetting) {
					fileInfo.setBuildSpatialIndex(isSpatial);
				} else {
					for (int i = 0; i < panels.size(); i++) {
						ImportPanelMapInfo tempPanel = (ImportPanelMapInfo) panels.get(i);
						tempPanel.getChckbxSpatialIndex().setSelected(isSpatial);
						fileInfos.get(i).setBuildSpatialIndex(isSpatial);
					}
				}
			}
		};
		// 是否导入属性信息
		this.checkboxImportPropertyInfoAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean isImport = checkboxImportPropertyInfo.isSelected();
				if (null != importsetting) {
					setImport(isImport, importsetting);
				} else {
					for (int i = 0; i < panels.size(); i++) {
						ImportPanelMapInfo tempPanel = (ImportPanelMapInfo) panels
								.get(i);
						tempPanel.getChckbxImport().setSelected(isImport);
					}
				}
			}
		};
		this.buttonPropertyAction = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new FileProperty(dataImportFrame, fileInfo).setVisible(true);
			}
		};
		unregistActionListener();
		this.comboBoxDataType.addActionListener(this.comboBoxDataTypeAction);
		this.checkboxFieldIndex.addActionListener(this.checkboxFieldIndexAction);
		this.checkboxSpatialIndex.addActionListener(this.checkboxSpatialIndexAction);
		this.checkboxImportPropertyInfo.addActionListener(this.checkboxImportPropertyInfoAction);
		this.buttonProperty.addActionListener(this.buttonPropertyAction);
	}

	@Override
	void unregistActionListener() {
		this.comboBoxDataType.removeActionListener(this.comboBoxDataTypeAction);
		this.checkboxFieldIndex.removeActionListener(this.checkboxFieldIndexAction);
		this.checkboxSpatialIndex.removeActionListener(this.checkboxSpatialIndexAction);
		this.checkboxImportPropertyInfo.removeActionListener(this.checkboxImportPropertyInfoAction);
	}

}
