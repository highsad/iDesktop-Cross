package com.supermap.desktop.CtrlAction.CreateGeometry;

import com.supermap.data.CursorType;
import com.supermap.data.DatasetType;
import com.supermap.data.DatasetVector;
import com.supermap.data.GeoText;
import com.supermap.data.Point2D;
import com.supermap.data.PrjCoordSysType;
import com.supermap.data.Recordset;
import com.supermap.data.TextPart;
import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.Interface.IFormMap;
import com.supermap.desktop.utilties.MapUtilties;
import com.supermap.desktop.utilties.StringUtilties;
import com.supermap.mapping.Layer;
import com.supermap.mapping.MapClosedEvent;
import com.supermap.mapping.MapClosedListener;
import com.supermap.ui.Action;
import com.supermap.ui.ActionChangedEvent;
import com.supermap.ui.ActionChangedListener;
import com.supermap.ui.MapControl;
import com.supermap.ui.TrackedEvent;
import com.supermap.ui.TrackedListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// @formatter:off
/**
 * 封装 MapControl 上文本绘制的操作
 * 1：JTextArea 没有办法根据文本内容的改变自适应大小，因此目前不支持输入文本的时候换行，按回车等于直接应用。
 * 2：目前的 Java 组件，MapControl 在 Action.CreateText 的时候，无法选中已有的文本对象进行绘制。
 * 3：组件提供的文本绘制思路是，在 TrackedListener 的时候，取出  GeoText，设置完内容，执行完 TrackedListener 的回调方法之后，
 * 		会将这个 GeoText 添加到地图。但是这样的思路需要阻塞方法，等待文本输入，实在太不易用。
 * 4：尝试过但放弃的方案。
 * 		这里自行实现思路，使用 CreateText，在 TrackedListener 中设置一个任意非空的 GeoText，然后触发 GeometryAdded，
 * 		在 GeometryAdded 中取出 id 以及对应 Layer 的 Recordset，然后修改。
 * 		放弃原因：因为只有在 Tracked 里添加一个非空文本才会触发 GeometryAdded，才能在 GeometryAdded 里记录新增文本对象的 id，
 * 		然后在 TextField 中完成编辑。这样的过程中，文本对象有一个属性文本内容，在 TextField 编辑时，无法完全隐藏或者遮盖这个文本内容。
 * 5：最终方案。
 * @author highsad
 *
 */
// @formatter:on
public class CreateTextAction {

	private static final double DEFAULT_FONT_HEIGHT = 10;
	private static final int DEFAULT_INPUT_HEIGHT = 45;
	private static final float DEFAULT_INPUT_FONT_SIZE = 25.0f;

	private JTextField textFieldInput = new JTextField();
	private MapControl mapControl;
	private LayoutManager preLayout;
	private GeoText editingGeoText; // 用来记录每一次点击获取到的 GeoText，当鼠标在另一个位置点击再次出发 Tracked 的时候，将编辑的数据保存

	private TrackedListener trackedListener = new TrackedListener() {

		@Override
		public void tracked(TrackedEvent arg0) {
			mapControlTracked(arg0);

		}
	};
	private ActionChangedListener actionChangedListener = new ActionChangedListener() {

		@Override
		public void actionChanged(ActionChangedEvent arg0) {

			abstractAvtionListener(arg0);
		}
	};
	private MapClosedListener mapClosedListener = new MapClosedListener() {
		@Override
		public void mapClosed(MapClosedEvent mapClosedEvent) {
			commitEditing();
			endAction();
			mapControl.getMap().removeMapClosedListener(this);
		}
	};

	private void abstractAvtionListener(ActionChangedEvent arg0) {
		if (arg0.getOldAction() == Action.CREATETEXT) {

			// 结束文本对象绘制，绘制过程中，按住中键会切换为漫游，此时不希望结束绘制，而是提交当前编辑，漫游结束之后继续绘制
			if (arg0.getNewAction() != Action.PAN) {
				endAction();
			} else {
				commitEditing();
			}
		} else if (arg0.getOldAction() == Action.PAN && arg0.getNewAction() != Action.CREATETEXT) {

			// 在漫游状态，改变为其他 Action，触发这个事件，表明在绘制中进行的漫游，如果切换为CreateText 之外的 Action，那么就结束绘制
			endAction();
		} else if (arg0.getNewAction() == Action.CREATETEXT) {

			// 开始文本对象绘制
			startAction();
		}
	}

	private ActionListener actionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			textFieldInputAction();
		}
	};

	public CreateTextAction() {
		this.textFieldInput.setBorder(null);
		this.textFieldInput.setSize(new Dimension(25, DEFAULT_INPUT_HEIGHT));
		this.textFieldInput.setFont(this.textFieldInput.getFont().deriveFont(Font.BOLD, (float) DEFAULT_INPUT_FONT_SIZE));
		this.textFieldInput.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				resizeInput();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				resizeInput();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// nothing
			}
		});
	}

	/**
	 * 开始文本绘制
	 */
	public void Start(MapControl mapControl) {
		if (this.mapControl != null && this.mapControl != mapControl) {
			endAction();
		}

		this.mapControl = mapControl;
		// 先移除监听，避免在 CreateAction 状态下，多次点击导致多次注册监听
		this.mapControl.removeActionChangedListener(this.actionChangedListener);
		// 添加监听，再设置 Action，才能在 ActionChanged 里处理
		this.mapControl.addActionChangedListener(this.actionChangedListener);

		if (this.mapControl.getAction() != Action.CREATETEXT) {
			this.mapControl.setAction(Action.CREATETEXT);
			// 激活当前窗口以响应按键。
			this.mapControl.requestFocusInWindow();
		} else {
			this.mapControl.setAction(Action.SELECT2);
		}
	}

	private void startAction() {
		this.preLayout = this.mapControl.getLayout();
		this.mapControl.setLayout(null);
		this.mapControl.add(this.textFieldInput);
		this.mapControl.addTrackedListener(this.trackedListener);
		this.mapControl.getMap().addMapClosedListener(this.mapClosedListener);
		this.textFieldInput.addActionListener(this.actionListener);
	}

	private void endAction() {
		if (!this.textFieldInput.getText().isEmpty()) {
			commitEditing();
		}
		this.mapControl.remove(this.textFieldInput);
		this.mapControl.setLayout(this.preLayout);
		this.mapControl.removeTrackedListener(this.trackedListener);
		this.mapControl.removeActionChangedListener(this.actionChangedListener);
		this.textFieldInput.removeActionListener(this.actionListener);
	}

	private void mapControlTracked(TrackedEvent e) {
		commitEditing();

		// 当地图在经纬坐标下时，鼠标点击位置在经纬范围之外，不做任何事情并输出提示
		if (this.mapControl.getMap().getPrjCoordSys() != null
				&& this.mapControl.getMap().getPrjCoordSys().getType() == PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE
				&& e.getGeometry() != null
				&& (e.getGeometry().getInnerPoint().getX() > 180 || e.getGeometry().getInnerPoint().getX() < -180
						|| e.getGeometry().getInnerPoint().getY() > 90 || e.getGeometry().getInnerPoint().getY() < -90)) {
			// 什么都不做，输出提示信息的工作已经由 FormMap 全权负责
		} else {
			this.editingGeoText = (GeoText) e.getGeometry();
			this.editingGeoText.getTextStyle().setSizeFixed(false);
			// DEFAULT_INPUT_HEIGHT / 2 是一个经验值，使得不固定大小的时候，最后绘制到地图上的文本大小与输入的时候基本一致
			this.editingGeoText.getTextStyle().setFontHeight(DEFAULT_INPUT_HEIGHT / 2 * MapUtilties.pixelLength(this.mapControl));

			// 获取 GeoText 的位置，将文本编辑控件显示到那个位置
			Point2D centerPointMap = this.editingGeoText.getInnerPoint();
			Point inputLocation = this.mapControl.getMap().mapToPixel(centerPointMap);

			this.textFieldInput.setLocation(inputLocation);
			this.textFieldInput.setVisible(true);
			this.textFieldInput.requestFocus();
		}
	}

	private void textFieldInputAction() {
		commitEditing();
	}

	private void commitEditing() {
		Recordset recordset = null;

		try {
			IForm activeForm = Application.getActiveApplication().getActiveForm();

			if (activeForm instanceof IFormMap) {
				Layer activeEditableLayer = ((IFormMap) activeForm).getMapControl().getActiveEditableLayer();

				if (activeEditableLayer.getDataset() instanceof DatasetVector
						&& (activeEditableLayer.getDataset().getType() == DatasetType.TEXT || activeEditableLayer.getDataset().getType() == DatasetType.CAD)) {
					recordset = ((DatasetVector) activeEditableLayer.getDataset()).getRecordset(false, CursorType.DYNAMIC);

					// 表明有待提交的编辑
					if (this.editingGeoText != null) {
						String text = this.textFieldInput.getText();
						if (!StringUtilties.isNullOrEmpty(text)) {
							TextPart textPart = new TextPart(text, this.editingGeoText.getPart(0).getAnchorPoint());
							this.editingGeoText.setPart(0, textPart);
							recordset.addNew(this.editingGeoText);
							recordset.update();

							// 选中新添加的文本对象
							recordset.moveLast();
							activeEditableLayer.getSelection().clear();
							activeEditableLayer.getSelection().add(recordset.getID());
							this.mapControl.getMap().refresh();
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			// Action结束之前，编辑提交之后，下一次编辑开始之前，隐藏编辑控件
			this.textFieldInput.setText("");
			this.textFieldInput.setVisible(false);
			if (this.editingGeoText != null) {
				this.editingGeoText.dispose();
			}
			if (recordset != null) {
				recordset.dispose();
			}
		}
	}

	/**
	 * 计算输入的文本长度，动态调整输入框的大小
	 */
	private void resizeInput() {
		FontMetrics fontMetrics = this.textFieldInput.getFontMetrics(this.textFieldInput.getFont());
		int textWidth = fontMetrics.stringWidth(this.textFieldInput.getText());
		this.textFieldInput.setSize(new Dimension(textWidth + 5, fontMetrics.getHeight()));
	}
}
