package com.supermap.desktop.implement;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JLabel;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.ICtrlAction;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.ui.XMLCommand;

public class SmLabel extends JLabel implements IBaseItem {
	private static final long serialVersionUID = 1L;
	private transient IForm formClass = null;

	public IForm getFormClass() {
		return formClass;
	}

	public void setFormClass(IForm formClass) {
		this.formClass = formClass;
	}

	private transient XMLCommand xmlCommand = null;

	public SmLabel(IForm formClass, XMLCommand xmlCommand, JComponent parent) {
		super(xmlCommand.getLabel());
		super.setToolTipText(xmlCommand.getTooltip());

		this.formClass = formClass;
		this.xmlCommand = xmlCommand;
	}

	@Override
	protected void paintComponent(Graphics g) {
		try {
			super.paintComponent(g);
		} catch (Exception ex) {
			// do nothing
		}
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	@Override
	public boolean isChecked() {
		return false;
	}

	@Override
	public void setChecked(boolean checked) {
		// do nothing
	}

	@Override
	public boolean isVisible() {
		return super.isVisible();
	}

	@Override
	public void setVisible(boolean enabled) {
		super.setVisible(enabled);
	}

	@Override
	public int getIndex() {
		return this.xmlCommand.getIndex();
	}

	@Override
	public void setIndex(int index) {
		this.xmlCommand.setIndex(index);
	}

	@Override
	public String getID() {
		return this.xmlCommand.getID();
	}

	@Override
	public String getText() {
		return super.getText();
	}

	@Override
	public void setText(String text) {
		super.setText(text);
	}

	@Override
	public ICtrlAction getCtrlAction() {
		return this.xmlCommand.getCtrlAction();
	}

	@Override
	public void setCtrlAction(ICtrlAction ctrlAction) {
		this.xmlCommand.setCtrlAction(ctrlAction);
	}
}
