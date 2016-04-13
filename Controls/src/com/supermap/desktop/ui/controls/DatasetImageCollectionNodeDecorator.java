package com.supermap.desktop.ui.controls;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import com.supermap.data.DatasetImageCollection;

/**
 * 影像数据集集合节点装饰器
 * 
 * @author gouyu
 *
 */
class DatasetImageCollectionNodeDecorator implements TreeNodeDecorator {
	@Override
	public void decorate(JLabel label, TreeNodeData data) {
		if (data.getType().equals(NodeDataType.DATASET_IMAGE_COLLECTION)) {
			DatasetImageCollection datasetImageCollection = (DatasetImageCollection) data.getData();
			label.setText(datasetImageCollection.getName());
			ImageIcon icon = (ImageIcon) label.getIcon();
			BufferedImage bufferedImage = new BufferedImage(IMAGEICON_WIDTH, IMAGEICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
			Graphics graphics = bufferedImage.getGraphics();
			graphics.drawImage(InternalImageIconFactory.DT_IMAGECOLLECTION.getImage(), 0, 0, label);
			icon.setImage(bufferedImage);
		}
	}

}
