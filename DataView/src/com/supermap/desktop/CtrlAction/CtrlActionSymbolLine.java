package com.supermap.desktop.CtrlAction;

import com.supermap.data.GeoStyle;
import com.supermap.data.SymbolType;
import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.controls.utilties.SymbolDialogFactory;
import com.supermap.desktop.dialog.symbolDialogs.SymbolDialog;
import com.supermap.desktop.implement.CtrlAction;

public class CtrlActionSymbolLine extends CtrlAction {

	public CtrlActionSymbolLine(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		try {
			SymbolDialog symbolDialog = SymbolDialogFactory.getSymbolDialog(SymbolType.LINE);
			symbolDialog.showDialog(new GeoStyle());
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

}
