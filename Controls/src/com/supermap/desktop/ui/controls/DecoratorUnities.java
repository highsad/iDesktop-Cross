package com.supermap.desktop.ui.controls;

import com.supermap.mapping.Layer;
import com.supermap.mapping.LayerGroup;
import com.supermap.mapping.ThemeGridRangeItem;
import com.supermap.mapping.ThemeGridUniqueItem;
import com.supermap.mapping.ThemeLabelItem;
import com.supermap.mapping.ThemeRangeItem;
import com.supermap.mapping.ThemeUniqueItem;
import com.supermap.realspace.Feature3D;
import com.supermap.realspace.Feature3Ds;
import com.supermap.realspace.Layer3D;
import com.supermap.realspace.ScreenLayer3D;
import com.supermap.realspace.TerrainLayer;
import com.supermap.realspace.Theme3DRangeItem;
import com.supermap.realspace.Theme3DUniqueItem;

public class DecoratorUnities {
	private DecoratorUnities(){
		// 公共类
	}
	
	public static boolean isDecoratorShow(Object internalData){
		boolean isDatasetNull = true;
		try {
			if (internalData instanceof ThemeGridRangeItem) {
				isDatasetNull = false;
			} else if (internalData instanceof ThemeGridUniqueItem) {
				isDatasetNull = false;
			} else if (internalData instanceof ThemeLabelItem) {
				isDatasetNull = false;
			} else if (internalData instanceof ThemeRangeItem) {
				isDatasetNull = false;
			} else if (internalData instanceof ThemeUniqueItem) {
				isDatasetNull = false;
			} else if (internalData instanceof Layer && !(internalData instanceof LayerGroup)) {
				Layer layer = (Layer) internalData;
				isDatasetNull = layer.getDataset() == null;
			} else if (internalData instanceof Layer3D) {
				Layer3D layer3D = (Layer3D) internalData;
				isDatasetNull = layer3D.getDataName() == null;
			} else if (internalData instanceof ScreenLayer3D) {
				isDatasetNull = false;
			} else if (internalData instanceof Theme3DRangeItem) {
				isDatasetNull = false;
			} else if (internalData instanceof Theme3DUniqueItem) {
				isDatasetNull = false;
			} else if (internalData instanceof TerrainLayer) {
				TerrainLayer terrainLayer = (TerrainLayer) internalData;
				isDatasetNull = terrainLayer.getDataset() == null;
			} else if (internalData instanceof Feature3D) {
				isDatasetNull = false;
			} else if (internalData instanceof Feature3Ds) {
				isDatasetNull = false;
			}
		} catch (Exception e) {
			isDatasetNull = true;
		}
		return isDatasetNull;
	}
	
}
