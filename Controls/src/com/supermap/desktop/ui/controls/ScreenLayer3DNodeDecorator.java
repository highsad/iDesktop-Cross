package com.supermap.desktop.ui.controls;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.supermap.desktop.controls.ControlsProperties;

/**
 * 三维屏幕图层节点装饰器
 * 
 * @author xuzw
 *
 */
class ScreenLayer3DNodeDecorator implements TreeNodeDecorator {

	public void decorate(JLabel label, TreeNodeData data) {
		if (data.getType().equals(NodeDataType.SCREEN_LAYER3D)) {
			label.setText(ControlsProperties.getString(ControlsProperties.ScreenLayerNodeName));
			ImageIcon icon = (ImageIcon) label.getIcon();
			BufferedImage bufferedImage = new BufferedImage(IMAGEICON_WIDTH, IMAGEICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
			Graphics graphics = bufferedImage.getGraphics();
			graphics.drawImage(InternalImageIconFactory.SCREEN_LAYER3D.getImage(), 0, 0, label);
			icon.setImage(bufferedImage);
		}
	}

}
