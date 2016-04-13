package com.supermap.desktop.Action;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;

import com.supermap.data.Dataset;
import com.supermap.data.DatasetGrid;
import com.supermap.desktop.spatialanalyst.SpatialAnalystProperties;

public class TransparentBackground extends JPopupMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel jLabelDatasource = new JLabel("datasource:");
	private JLabel jLabelDataset = new JLabel("dataset:");
	private JLabel jLabelPointX = new JLabel("pointX:");
	private JLabel jLabelPointY = new JLabel("pointY:");
	private JLabel jLabelRowOfGrid = new JLabel("columnOfGrid:");
	private JLabel jLabelColumnOfGrid = new JLabel("lineOfGrid:");
	private JLabel jLabelGridValue = new JLabel("gridValue:");
	private final int gapWith = 10;

	public TransparentBackground() {
		setBorder(new LineBorder(Color.LIGHT_GRAY));
		initResources();
		initComponents();
	}

	/**
	 * 资源化
	 */
	private void initResources() {
		jLabelDatasource.setText(SpatialAnalystProperties.getString("String_QueryGridValue_Datasource").replace("{0}", "-"));
		jLabelDataset.setText(SpatialAnalystProperties.getString("String_QueryGridValue_Dataset").replace("{0}", "-"));
		jLabelPointX.setText(SpatialAnalystProperties.getString("String_QueryGridValue_CooX").replace("{0}", "-"));
		jLabelPointY.setText(SpatialAnalystProperties.getString("String_QueryGridValue_CooY").replace("{0}", "-"));
		jLabelRowOfGrid.setText(SpatialAnalystProperties.getString("String_QueryGridValue_Row").replace("{0}", "-"));
		jLabelColumnOfGrid.setText(SpatialAnalystProperties.getString("String_QueryGridValue_Collunm").replace("{0}", "-"));
		jLabelGridValue.setText(SpatialAnalystProperties.getString("String_QueryGridValue_GridValue").replace("{0}", "-"));
	}

	/**
	 * 初始化界面信息
	 */
	private void initComponents() {
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup()
						.addGroup(groupLayout.createSequentialGroup()
								.addGap(gapWith)
								.addComponent(jLabelDatasource))
						.addGroup(groupLayout.createSequentialGroup()
								.addGap(gapWith)
								.addComponent(jLabelDataset))
						.addGroup(groupLayout.createSequentialGroup()
								.addGap(gapWith)
								.addComponent(jLabelPointX))
						.addGroup(groupLayout.createSequentialGroup()
								.addGap(gapWith)
								.addComponent(jLabelPointY))
						.addGroup(groupLayout.createSequentialGroup()
								.addGap(gapWith)
								.addComponent(jLabelRowOfGrid))
						.addGroup(groupLayout.createSequentialGroup()
								.addGap(gapWith)
								.addComponent(jLabelColumnOfGrid))
						.addGroup(groupLayout.createSequentialGroup()
								.addGap(gapWith)
								.addComponent(jLabelGridValue))
				);
		groupLayout.setVerticalGroup(
				groupLayout.createSequentialGroup()
						.addComponent(jLabelDatasource)
						.addComponent(jLabelDataset)
						.addComponent(jLabelPointX)
						.addComponent(jLabelPointY)
						.addComponent(jLabelRowOfGrid)
						.addComponent(jLabelColumnOfGrid)
						.addComponent(jLabelGridValue)
				);
		setLayout(groupLayout);
	}

	public JLabel getjLabelDatasource() {
		return jLabelDatasource;
	}

	public void setjLabelDatasource(JLabel jLabelDatasource) {
		this.jLabelDatasource = jLabelDatasource;
	}

	public JLabel getjLabelDataset() {
		return jLabelDataset;
	}

	public void setjLabelDataset(JLabel jLabelDataset) {
		this.jLabelDataset = jLabelDataset;
	}

	public JLabel getjLabelPointX() {
		return jLabelPointX;
	}

	public void setjLabelPointX(JLabel jLabelPointX) {
		this.jLabelPointX = jLabelPointX;
	}

	public JLabel getjLabelPointY() {
		return jLabelPointY;
	}

	public void setjLabelPointY(JLabel jLabelPointY) {
		this.jLabelPointY = jLabelPointY;
	}

	public JLabel getjLabelRowOfGrid() {
		return jLabelRowOfGrid;
	}

	public void setjLabelRowOfGrid(JLabel jLabelRowOfGrid) {
		this.jLabelRowOfGrid = jLabelRowOfGrid;
	}

	public JLabel getjLabelColumnOfGrid() {
		return jLabelColumnOfGrid;
	}

	public void setjLabelColumnOfGrid(JLabel jLabelColumnOfGrid) {
		this.jLabelColumnOfGrid = jLabelColumnOfGrid;
	}

	public JLabel getjLabelGridValue() {
		return jLabelGridValue;
	}

	public void setjLabelGridValue(JLabel jLabelGridValue) {
		this.jLabelGridValue = jLabelGridValue;
	}

}
