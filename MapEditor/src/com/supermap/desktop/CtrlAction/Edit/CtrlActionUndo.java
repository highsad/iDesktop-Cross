package com.supermap.desktop.CtrlAction.Edit;

import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.Interface.IFormMap;
import com.supermap.desktop.implement.CtrlAction;

public class CtrlActionUndo extends CtrlAction {
	// 这个类对应的是菜单栏上的对象操作中的，工具条中的是DataEditor中的CtrlActionUndo
	// xiajt 2016年2月17日
	public CtrlActionUndo(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run() {
		try {
			IFormMap form = (IFormMap) Application.getActiveApplication().getActiveForm();
			form.getMapControl().undo();
			form.setSelectedGeometryProperty();
			form.getMapControl().getMap().refresh();
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	@Override
	public boolean enable() {
		boolean enable = false;
		try {
			IForm form = Application.getActiveApplication().getActiveForm();
			if (form != null && form instanceof IFormMap) {
				enable = ((IFormMap) form).getMapControl().canUndo();
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
		return enable;
	}
}
