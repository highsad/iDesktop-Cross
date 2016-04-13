package com.supermap.desktop.CtrlAction.Dataset;

import com.supermap.data.Charset;
import com.supermap.data.DatasetType;
import com.supermap.data.EncodeType;
import com.supermap.desktop.Application;
import com.supermap.desktop.CommonToolkit;
import com.supermap.desktop.Interface.IFormMap;
import com.supermap.desktop.dataeditor.DataEditorProperties;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.ui.controls.DatasetComboBox;
import com.supermap.desktop.ui.controls.DatasourceComboBox;
import com.supermap.desktop.ui.controls.DialogResult;
import com.supermap.desktop.ui.controls.GridBagConstraintsHelper;
import com.supermap.desktop.ui.controls.SmDialog;
import com.supermap.desktop.ui.controls.button.SmButton;
import com.supermap.desktop.utilties.CharsetUtilties;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * 新建数据集的统一设置面板
 *
 * @author XiaJT
 */
public class JDialogSetAll extends SmDialog {

	private JCheckBox checkBoxTargetDatasource;
	private JCheckBox checkBoxDatasetType;
	private JCheckBox checkBoxEncodingType;
	private JCheckBox checkBoxCharest;
	private JCheckBox checkBoxAddToNewMap;

	private JComboBox comboboxTargetDatasource;
	private DatasetComboBox comboboxDatasetType;
	private JComboBox comboboxEncodingType;
	private JComboBox comboboxCharest;
	private JComboBox comboboxAddToNewMap;

	private SmButton buttonOk;
	private SmButton buttonCancel;

	public JDialogSetAll() {
		this.setSize(300, 250);
		this.setLocationRelativeTo(null);
		this.initComponent();
		this.initializeResource();
		this.addListeners();
		this.componentList.add(buttonOk);
		this.componentList.add(buttonCancel);
		this.setFocusTraversalPolicy(policy);
	}

	/**
	 * 初始化控件
	 */
	private void initComponent() {
		this.checkBoxTargetDatasource = new JCheckBox("TargetDatasource:");
		this.checkBoxDatasetType = new JCheckBox("DatasetType:");
		this.checkBoxEncodingType = new JCheckBox("EncodingType:");
		this.checkBoxCharest = new JCheckBox("Charset:");
		this.checkBoxAddToNewMap = new JCheckBox("AddToNewMap:");

		this.comboboxTargetDatasource = new JComboBox();
		this.comboboxDatasetType = new DatasetComboBox();
		this.comboboxEncodingType = new JComboBox();
		this.comboboxCharest = new JComboBox();
		this.comboboxAddToNewMap = new JComboBox();

		// 初始化目标数据源
		this.comboboxTargetDatasource = new DatasourceComboBox(Application.getActiveApplication().getWorkspace().getDatasources());

		// 初始化数据类型
		ArrayList<DatasetType> datasetTypes = new ArrayList<DatasetType>();
		datasetTypes.add(DatasetType.POINT);
		datasetTypes.add(DatasetType.LINE);
		datasetTypes.add(DatasetType.REGION);
		datasetTypes.add(DatasetType.TEXT);
		datasetTypes.add(DatasetType.CAD);
		datasetTypes.add(DatasetType.TABULAR);
		datasetTypes.add(DatasetType.POINT3D);
		datasetTypes.add(DatasetType.LINE3D);
		datasetTypes.add(DatasetType.REGION3D);
		this.comboboxDatasetType = new DatasetComboBox(datasetTypes.toArray(new DatasetType[datasetTypes.size()]));

		// 初始化字符集
		this.comboboxEncodingType.removeAllItems();
		ArrayList<String> temptempEncodeType = new ArrayList<String>();
		temptempEncodeType.add(CommonToolkit.EncodeTypeWrap.findName(EncodeType.NONE));
		temptempEncodeType.add(CommonToolkit.EncodeTypeWrap.findName(EncodeType.BYTE));
		temptempEncodeType.add(CommonToolkit.EncodeTypeWrap.findName(EncodeType.INT16));
		temptempEncodeType.add(CommonToolkit.EncodeTypeWrap.findName(EncodeType.INT24));
		temptempEncodeType.add(CommonToolkit.EncodeTypeWrap.findName(EncodeType.INT32));
		this.comboboxEncodingType.setModel(new DefaultComboBoxModel<String>(temptempEncodeType.toArray(new String[temptempEncodeType.size()])));

		// 初始化编码格式
		this.comboboxCharest.removeAllItems();
		ArrayList<String> charsetes = new ArrayList<String>();
		charsetes.add(CharsetUtilties.getCharsetName(Charset.OEM));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.EASTEUROPE));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.THAI));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.RUSSIAN));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.BALTIC));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.ARABIC));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.HEBREW));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.VIETNAMESE));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.TURKISH));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.GREEK));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.CHINESEBIG5));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.JOHAB));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.HANGEUL));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.SHIFTJIS));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.MAC));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.SYMBOL));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.DEFAULT));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.ANSI));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.UTF8));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.UTF7));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.WINDOWS1252));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.KOREAN));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.UNICODE));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.CYRILLIC));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.XIA5));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.XIA5GERMAN));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.XIA5SWEDISH));
		charsetes.add(CharsetUtilties.getCharsetName(Charset.XIA5NORWEGIAN));
		this.comboboxCharest.setModel(new DefaultComboBoxModel<String>(charsetes.toArray(new String[charsetes.size()])));

		// 初始化是否加入地图
		this.comboboxAddToNewMap.removeAllItems();
		ArrayList<String> addTos = new ArrayList<String>();
		addTos.add(AddToWindowMode.toString(AddToWindowMode.NONEWINDOW));
		addTos.add(AddToWindowMode.toString(AddToWindowMode.NEWWINDOW));
		if (Application.getActiveApplication().getActiveForm() != null && Application.getActiveApplication().getActiveForm() instanceof IFormMap) {
			addTos.add(AddToWindowMode.toString(AddToWindowMode.CURRENTWINDOW));
		}
		this.comboboxAddToNewMap.setModel(new DefaultComboBoxModel<Object>(addTos.toArray(new String[addTos.size()])));

		// 按钮
		buttonOk = new SmButton(CommonProperties.getString(CommonProperties.OK));
		buttonCancel = new SmButton(CommonProperties.getString(CommonProperties.Cancel));
		this.getRootPane().setDefaultButton(this.buttonOk);

		// 添加控件到面板中
		this.addComponentToPanel();
	}

	private void addComponentToPanel() {
		// 中央面板
		// @formatter:off
		Panel centerPanel = new Panel();
		centerPanel.setLayout(new GridBagLayout());

		centerPanel.add(checkBoxTargetDatasource, new GridBagConstraintsHelper(0, 0).setInsets(2).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1).setAnchor(GridBagConstraints.WEST).setIpad(0, 10));
		centerPanel.add(comboboxTargetDatasource, new GridBagConstraintsHelper(1, 0).setInsets(2).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1).setFill(GridBagConstraints.HORIZONTAL).setIpad(0, 10));

		centerPanel.add(checkBoxDatasetType, new GridBagConstraintsHelper(0, 1).setInsets(2).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1).setAnchor(GridBagConstraints.WEST).setIpad(0, 10));
		centerPanel.add(comboboxDatasetType, new GridBagConstraintsHelper(1, 1).setInsets(2).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1).setFill(GridBagConstraints.HORIZONTAL).setIpad(0, 10));

		centerPanel.add(checkBoxEncodingType, new GridBagConstraintsHelper(0, 2).setInsets(2).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1).setAnchor(GridBagConstraints.WEST).setIpad(0, 10));
		centerPanel.add(comboboxEncodingType, new GridBagConstraintsHelper(1, 2).setInsets(2).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1).setFill(GridBagConstraints.HORIZONTAL).setIpad(0, 10));

		centerPanel.add(checkBoxCharest, new GridBagConstraintsHelper(0, 3).setInsets(2).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1).setAnchor(GridBagConstraints.WEST).setIpad(0, 10));
		centerPanel.add(comboboxCharest, new GridBagConstraintsHelper(1, 3).setInsets(2).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1).setFill(GridBagConstraints.HORIZONTAL).setIpad(0, 10));

		centerPanel.add(checkBoxAddToNewMap, new GridBagConstraintsHelper(0, 4).setInsets(2).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1).setAnchor(GridBagConstraints.WEST).setIpad(0, 10));
		centerPanel.add(comboboxAddToNewMap, new GridBagConstraintsHelper(1, 4).setInsets(2).setAnchor(GridBagConstraints.CENTER).setWeight(1, 1).setFill(GridBagConstraints.HORIZONTAL).setIpad(0, 10));

		// 按钮面板
		Panel buttonPanel = new Panel();
		buttonPanel.setLayout(new GridBagLayout());

		buttonPanel.add(buttonOk, new GridBagConstraintsHelper(0, 0).setAnchor(GridBagConstraints.EAST).setInsets(5).setWeight(1, 1));
		buttonPanel.add(buttonCancel, new GridBagConstraintsHelper(1, 0).setAnchor(GridBagConstraints.CENTER).setInsets(5, 0, 5, 5).setWeight(0, 1));
	
		this.setLayout(new GridBagLayout());
		this.add(centerPanel, new GridBagConstraintsHelper(0, 0).setWeight(1, 1).setFill(GridBagConstraints.BOTH).setInsets(5));
		this.add(buttonPanel, new GridBagConstraintsHelper(0, 1).setWeight(1, 0).setFill(GridBagConstraints.BOTH).setInsets(5));
		// @formatter:on
	}

	/**
	 * 控件资源化
	 */
	private void initializeResource() {
		this.checkBoxTargetDatasource.setText(CommonProperties.getString("String_ColumnHeader_TargetDatasource"));
		this.checkBoxDatasetType.setText(DataEditorProperties.getString("String_CreateType"));
		this.checkBoxEncodingType.setText(CommonProperties.getString("String_ColumnHeader_EncodeType"));
		this.checkBoxCharest.setText(DataEditorProperties.getString("String_Charset"));
		this.checkBoxAddToNewMap.setText(DataEditorProperties.getString("String_DataGridViewComboBoxColumn_Name"));
	}

	/**
	 * 添加监听器
	 */
	private void addListeners() {
		this.comboboxTargetDatasource.addActionListener(commonActionListener);
		this.comboboxDatasetType.addActionListener(commonActionListener);
		this.comboboxEncodingType.addActionListener(commonActionListener);
		this.comboboxCharest.addActionListener(commonActionListener);
		this.comboboxAddToNewMap.addActionListener(commonActionListener);
		this.buttonOk.addActionListener(commonActionListener);
		this.buttonCancel.addActionListener(commonActionListener);
	}

	private ActionListener commonActionListener = new SetActionListener();

	private void buttonOk_Clicked() {
		setDialogResult(DialogResult.OK);
		this.dispose();
	}

	private void buttonCancel_Clicked() {
		setDialogResult(DialogResult.CANCEL);
		this.dispose();
	}

	/**
	 * 获得目标数据源
	 * 
	 * @return
	 */
	public Object getTargetDatasource() {
		if (checkBoxTargetDatasource.isSelected()) {
			return comboboxTargetDatasource.getSelectedItem();
		}
		return null;
	}

	/**
	 * 获取数据集类型
	 * 
	 * @return
	 */
	public Object getDatasetType() {
		if (checkBoxDatasetType.isSelected()) {
			return comboboxDatasetType.getSelectedItem();
		}
		return null;
	}

	/**
	 * 获取编码格式
	 * 
	 * @return
	 */
	public Object getEncodingType() {
		if (checkBoxEncodingType.isSelected()) {
			return comboboxEncodingType.getSelectedItem();
		}
		return null;
	}

	/**
	 * 获取字符集
	 * 
	 * @return
	 */
	public Object getCharset() {
		if (checkBoxCharest.isSelected()) {
			return comboboxCharest.getSelectedItem();
		}
		return null;
	}

	/**
	 * 获取是否加入地图
	 * 
	 * @return
	 */
	public Object getAddtoMap() {
		if (checkBoxAddToNewMap.isSelected()) {
			return comboboxAddToNewMap.getSelectedItem();
		}
		return null;
	}

	class SetActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == comboboxTargetDatasource) {
				checkBoxTargetDatasource.setSelected(true);
				return;
			}
			if (e.getSource() == comboboxDatasetType) {
				checkBoxDatasetType.setSelected(true);
				return;
			}
			if (e.getSource() == comboboxEncodingType) {
				checkBoxEncodingType.setSelected(true);
				return;
			}
			if (e.getSource() == comboboxCharest) {
				checkBoxCharest.setSelected(true);
				return;
			}
			if (e.getSource() == comboboxAddToNewMap) {
				checkBoxAddToNewMap.setSelected(true);
				return;
			}
			if (e.getSource() == buttonOk) {
				JDialogSetAll.this.buttonOk_Clicked();
				return;
			}
			if (e.getSource() == buttonCancel) {
				JDialogSetAll.this.buttonCancel_Clicked();
				return;
			}
		}
	}

}