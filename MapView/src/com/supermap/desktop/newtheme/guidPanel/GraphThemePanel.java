package com.supermap.desktop.newtheme.guidPanel;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.supermap.desktop.mapview.MapViewProperties;
import com.supermap.desktop.newtheme.commonUtils.ThemeGuideFactory;
import com.supermap.desktop.newtheme.commonUtils.ThemeUtil;
import com.supermap.desktop.ui.controls.InternalImageIconFactory;

public class GraphThemePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private ThemeLabelDecorator labelGraphTheme;
	private transient ThemeGuidDialog themeGuidDialog;
	private transient LocalMouseListener mouseListener = new LocalMouseListener();

	public GraphThemePanel(ThemeGuidDialog themeGuidDialog) {
		this.themeGuidDialog = themeGuidDialog;
		initComponents();
		registListener();
	}

	/**
	 * 界面布局入口
	 */
	private void initComponents() {
		// @formatter:off
		this.labelGraphTheme = new ThemeLabelDecorator(InternalImageIconFactory.THEMEGUIDE_GRAPH, MapViewProperties.getString("String_ThemeDefaultItem"));
		this.labelGraphTheme.selected(true);
		this.setBackground(Color.WHITE);
		this.setBorder(new LineBorder(Color.LIGHT_GRAY));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
								.addGap(20)
								.addComponent(labelGraphTheme)
								.addContainerGap(368, Short.MAX_VALUE))
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
								.addGap(24)
								.addComponent(labelGraphTheme)
								.addContainerGap(223, Short.MAX_VALUE))
				);
		setLayout(groupLayout);
		// @formatter:on
	}

	/**
	 * 注册事件
	 */
	private void registListener() {
		this.labelGraphTheme.addMouseListener(this.mouseListener);
	}

	/**
	 * 注销事件
	 */
	public void unregistListener() {
		this.labelGraphTheme.removeMouseListener(this.mouseListener);
	}

	class LocalMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				// 分段专题图
				ThemeGuideFactory.buildGraphTheme(ThemeUtil.getActiveLayer());
				themeGuidDialog.dispose();
				unregistListener();
			}
		}

	}
}
