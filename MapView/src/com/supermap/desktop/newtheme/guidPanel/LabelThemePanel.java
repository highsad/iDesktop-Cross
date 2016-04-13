package com.supermap.desktop.newtheme.guidPanel;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.border.LineBorder;
import javax.swing.JLabel;

import com.supermap.desktop.mapview.MapViewProperties;
import com.supermap.desktop.ui.controls.InternalImageIconFactory;

public class LabelThemePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private ThemeLabelDecorator labelUniformTheme;
	private ThemeLabelDecorator labelRangeTheme;
	private MouseAdapter mouseAdapter;

	public LabelThemePanel() {
		initComponents();
		registListener();
	}

	/**
	 * 界面布局入口
	 */
	private void initComponents() {
		// @formatter:off
		this.setBackground(Color.WHITE);	
		this.setBorder(new LineBorder(Color.LIGHT_GRAY));
		this.labelUniformTheme = new ThemeLabelDecorator(InternalImageIconFactory.THEMEGUIDE_UNIFORM, MapViewProperties.getString("String_ThemeLabelUniformItem"));
		this.labelUniformTheme.selected(true);
		this.labelRangeTheme = new ThemeLabelDecorator(InternalImageIconFactory.THEMEGUIDE_RANGES, MapViewProperties.getString("String_ThemeLabelRangeItem"));
		this.labelRangeTheme.selected(false);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
				groupLayout.createSequentialGroup()
						.addGap(20)
						.addComponent(labelUniformTheme)
						.addComponent(labelRangeTheme)
						.addContainerGap(115, Short.MAX_VALUE)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
				groupLayout.createSequentialGroup().addGroup(
						groupLayout.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createSequentialGroup().addGap(20).addComponent(labelUniformTheme))
										.addGroup(groupLayout.createSequentialGroup().addGap(21).addComponent(labelRangeTheme)))
						.addContainerGap(180, Short.MAX_VALUE)));
		setLayout(groupLayout);
		// @formatter:on
	}

	/**
	 * 注册事件
	 */
	private void registListener() {
		this.mouseAdapter = new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				labelUniformTheme.selected(false);
			}
		};
		this.addMouseListener(this.mouseAdapter);
	}

	/**
	 * 注销事件
	 */
	public void unregistListener() {
		this.removeMouseListener(this.mouseAdapter);
	}

	public ThemeLabelDecorator getLabelUniformTheme() {
		return labelUniformTheme;
	}

	public void setLabelUniformTheme(ThemeLabelDecorator labelUniformTheme) {
		this.labelUniformTheme = labelUniformTheme;
	}

	public ThemeLabelDecorator getLabelRangeTheme() {
		return labelRangeTheme;
	}

	public void setLabelRangeTheme(ThemeLabelDecorator labelRangeTheme) {
		this.labelRangeTheme = labelRangeTheme;
	}

	
}
