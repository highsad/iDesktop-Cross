package com.supermap.desktop.CtrlAction;

import java.text.MessageFormat;

import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.mapview.MapViewProperties;
import com.supermap.desktop.mapview.utilties.SaveAsMapTemplateUtilties;

public class CtrlActionWorkspaceMapSaveAsTemplate extends CtrlAction{
	public CtrlActionWorkspaceMapSaveAsTemplate(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run(){
		String mapXml = SaveAsMapTemplateUtilties.getActiveMapXml();
		String message = MapViewProperties.getString("String_SavaAsTemplateFailed");
		if(mapXml != null && mapXml.length() > 0){
			String mapTemplatePath = SaveAsMapTemplateUtilties.saveAsMapTemplate(mapXml);
			if(mapTemplatePath != null && mapTemplatePath.length() > 0){
				message = MessageFormat.format(MapViewProperties.getString("String_SavaAsTemplateSuccessful"), mapTemplatePath);
			}
		}
		Application.getActiveApplication().getOutput().output(message);
	}
	
	@Override
	public boolean enable(){
		return true;
	}
}
