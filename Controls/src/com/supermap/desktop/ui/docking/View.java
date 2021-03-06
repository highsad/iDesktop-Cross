/*
 * Copyright (C) 2004 NNL Technology AB
 * Visit www.infonode.net for information about InfoNode(R) 
 * products and how to contact NNL Technology AB.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA 02111-1307, USA.
 */

// $Id: View.java,v 1.66 2005/12/03 14:34:34 jesper Exp $
package com.supermap.desktop.ui.docking;

import net.infonode.gui.ComponentUtil;
import net.infonode.gui.panel.SimplePanel;
import net.infonode.properties.base.Property;
import net.infonode.properties.propertymap.PropertyMap;
import net.infonode.properties.propertymap.PropertyMapWeakListenerManager;
import net.infonode.properties.util.PropertyChangeListener;
import net.infonode.tabbedpanel.TabbedPanel;
import net.infonode.util.ChangeNotifyList;
import net.infonode.util.Direction;
import net.infonode.util.StreamUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.supermap.desktop.ui.docking.drag.DockingWindowDragSource;
import com.supermap.desktop.ui.docking.drag.DockingWindowDragger;
import com.supermap.desktop.ui.docking.drag.DockingWindowDraggerProvider;
import com.supermap.desktop.ui.docking.drop.InteriorDropInfo;
import com.supermap.desktop.ui.docking.internal.ReadContext;
import com.supermap.desktop.ui.docking.internal.ViewTitleBar;
import com.supermap.desktop.ui.docking.internal.WriteContext;
import com.supermap.desktop.ui.docking.internalutil.DropAction;
import com.supermap.desktop.ui.docking.model.ViewItem;
import com.supermap.desktop.ui.docking.model.ViewWriter;
import com.supermap.desktop.ui.docking.properties.ViewProperties;
import com.supermap.desktop.ui.docking.properties.ViewTitleBarProperties;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * <p>
 * A view is a docking window containing a component.
 * </p>
 *
 * <p>
 * A view can also contain a title bar that can be shown on either side of the view component. The title bar is made visible by setting the visible property in
 * the ViewTitleBarProperties in the ViewProperties for this view. The title bar automatically inherits the view's title and icon but it's possible to specify a
 * specific title and icon for the title bar in the ViewTitleBarProperties in the ViewProperties for this view.
 * </p>
 *
 * @author $Author: jesper $
 * @version $Revision: 1.66 $
 * @see com.supermap.desktop.ui.docking.properties.ViewProperties
 * @see com.supermap.desktop.ui.docking.properties.ViewTitleBarProperties
 */
public class View extends DockingWindow {

	private static final long serialVersionUID = 1L;
	private Component lastFocusedComponent;
	private transient DockingWindowDragSource dockingWindowDragSource;
	private transient HierarchyListener focusComponentListener = new HierarchyListener() {
		@Override
		public void hierarchyChanged(HierarchyEvent e) {
			checkLastFocusedComponent();
		}
	};

	private SimplePanel contentPanel = new SimplePanel();
	private transient ViewProperties rootProperties = new ViewProperties();
	private transient WeakReference lastRootWindow;
	private transient PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChanged(Property property, Object valueContainer, Object oldValue, Object newValue) {
			fireTitleChanged();
		}
	};

	private transient PropertyChangeListener titleBarPropertiesListener = new PropertyChangeListener() {
		@Override
		public void propertyChanged(Property property, Object valueContainer, Object oldValue, Object newValue) {
			updateTitleBar(property, valueContainer);
		}
	};

	private ViewTitleBar titleBar;
	private boolean isfocused = false;
	private transient List customTitleBarComponents;

	private WindowTab ghostTab;

	private transient DropAction titleBarDropAction = new DropAction() {
		@Override
		public boolean showTitle() {
			return false;
		}

		@Override
		public void execute(DockingWindow window, MouseEvent mouseEvent) {
			removeGhostTab();

			((AbstractTabWindow) getWindowParent()).addTab(window);
		}

		@Override
		public void clear(DockingWindow window, DropAction newDropAction) {
			if (newDropAction != this)
				removeGhostTab();
		}

		private void removeGhostTab() {
			if (ghostTab != null) {
				TabbedPanel tp = ((AbstractTabWindow) getWindowParent()).getTabbedPanel();
				tp.removeTab(ghostTab);
				ghostTab = null;

				if (tp.getProperties().getEnsureSelectedTabVisible() && tp.getSelectedTab() != null) {
					tp.scrollTabToVisibleArea(tp.getSelectedTab());
				}
			}
		}

	};

	public View() {
		this("");
	}

	public View(String title) {
		this(title, null, null);
	}

	/**
	 * Constructor.
	 *
	 * @param title the title of the view
	 * @param icon the icon for the view
	 * @param component the component to place inside the view
	 */
	public View(String title, Icon icon, Component component) {
		super(new ViewItem());
		rootProperties.setTitle(title);
		rootProperties.setIcon(icon);
		getViewProperties().addSuperObject(rootProperties);
		super.setComponent(contentPanel);
		contentPanel.setComponent(component);

		PropertyMapWeakListenerManager.addWeakPropertyChangeListener(getViewProperties().getMap(), ViewProperties.TITLE, listener);
		PropertyMapWeakListenerManager.addWeakPropertyChangeListener(getViewProperties().getMap(), ViewProperties.ICON, listener);

		PropertyMapWeakListenerManager.addWeakPropertyChangeListener(getViewProperties().getViewTitleBarProperties().getMap(), ViewTitleBarProperties.VISIBLE,
				titleBarPropertiesListener);
		PropertyMapWeakListenerManager.addWeakPropertyChangeListener(getViewProperties().getViewTitleBarProperties().getMap(),
				ViewTitleBarProperties.CONTENT_TITLE_BAR_GAP, titleBarPropertiesListener);
		PropertyMapWeakListenerManager.addWeakPropertyChangeListener(getViewProperties().getViewTitleBarProperties().getMap(),
				ViewTitleBarProperties.ORIENTATION, titleBarPropertiesListener);

		updateTitleBar(null, null);

		init();
	}

	/**
	 * <p>
	 * Returns a list containing the custom window tab components. Changes to the list will be propagated to the tab.
	 * </p>
	 * <p>
	 * The custom tab components will be shown after the window title when the window tab is highlighted. The components are shown in the same order as they
	 * appear in the list. The custom tab components container layout is rotated with the tab direction.
	 * </p>
	 *
	 * @return a list containing the custom tab components, list elements are of type {@link JComponent}
	 * @since IDW 1.3.0
	 */
	public java.util.List getCustomTabComponents() {
		return getTab().getCustomTabComponentsList();
	}

	/**
	 * <p>
	 * Returns a list containing the custom view title bar components. Changes to the list will be propagated to the title bar.
	 * </p>
	 * <p>
	 * The custom title bar components will be shown after the view title in the title bar but before the close, minimize and restore buttons. The components
	 * are shown in the same order as they appear in the list. The custom title bar components container layout is rotated with the title bar direction.
	 * </p>
	 * <p>
	 * <strong>Note:</strong> The components are only shon if the title bar is visible, see {@link ViewTitleBarProperties}.
	 * </p>
	 *
	 * @return a list containing the custom title bar components, list elements are of type {@link JComponent}
	 * @since IDW 1.4.0
	 */
	public List getCustomTitleBarComponents() {
		if (customTitleBarComponents == null)
			customTitleBarComponents = new ChangeNotifyList() {
				@Override
				protected void changed() {
					if (titleBar != null)
						titleBar.updateCustomBarComponents(this);
				}
			};

		return customTitleBarComponents;
	}

	/**
	 * Gets the component inside the view.
	 *
	 * @return the component inside the view
	 * @since IDW 1.1.0
	 */
	public Component getComponent() {
		return contentPanel.getComponent(0);
	}

	/**
	 * Sets the component inside the view.
	 *
	 * @param component the component to place inside the view
	 * @since IDW 1.1.0
	 */
	@Override
	public void setComponent(Component component) {
		contentPanel.setComponent(component);
	}

	public void setSouthComponent(Component component) {
		contentPanel.setSouthComponent(component);
	}

	/**
	 * Returns the property values for this view.
	 *
	 * @return the property values for this view
	 */
	public ViewProperties getViewProperties() {
		return ((ViewItem) getWindowItem()).getViewProperties();
	}

	@Override
	protected void update() {
		// TODO:
	}

	@Override
	public DockingWindow getChildWindow(int index) {
		return null;
	}

	@Override
	public int getChildWindowCount() {
		return 0;
	}

	void setLastFocusedComponent(Component component) {
		if (component != lastFocusedComponent) {
			if (lastFocusedComponent != null)
				lastFocusedComponent.removeHierarchyListener(focusComponentListener);
			lastFocusedComponent = component;

			if (lastFocusedComponent != null)
				lastFocusedComponent.addHierarchyListener(focusComponentListener);
		}
	}

	Component getFocusComponent() {
		checkLastFocusedComponent();
		return lastFocusedComponent;
	}

	@Override
	public boolean isFocusCycleRoot() {
		return true;
	}

	/**
	 * Restores focus to the last focused child component or, if no child component has had focus, the first focusable component inside the view.
	 *
	 * @since IDW 1.1.0
	 */
	@Override
	public void restoreFocus() {
		makeVisible();
		checkLastFocusedComponent();

		if (lastFocusedComponent == null) {
			ComponentUtil.smartRequestFocus(contentPanel);
		} else {
			lastFocusedComponent.requestFocusInWindow();
		}
	}

	@Override
	public String getTitle() {
		return rootProperties.getTitle();
	}

	public void setTitle(String title) {
		rootProperties.setTitle(title);
	}

	@Override
	public Icon getIcon() {
		return getViewProperties().getIcon();
	}

	@Override
	protected void doReplace(DockingWindow oldWindow, DockingWindow newWindow) {
		throw new RuntimeException(View.class + ".replaceChildWindow called!");
	}

	@Override
	protected void doRemoveWindow(DockingWindow window) {
		throw new RuntimeException(View.class + ".removeChildWindow called!");
	}

	protected void write(ObjectOutputStream out, WriteContext context) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		try {
			context.getViewSerializer().writeView(this, oos);
			getWindowItem().writeSettings(oos, context);
		} finally {
			oos.close();
		}

		out.writeInt(baos.size());
		baos.writeTo(out);
	}

	static View read(ObjectInputStream in, ReadContext context) throws IOException {
		int size = in.readInt();
		byte[] viewData = new byte[size];
		StreamUtil.readAll(in, viewData);
		ObjectInputStream viewIn = new ObjectInputStream(new ByteArrayInputStream(viewData));
		View view = context.getViewSerializer().readView(viewIn);

		if (view != null)
			view.getWindowItem().readSettings(viewIn, context);

		return view;
	}

	@Override
	protected DropAction doAcceptDrop(Point p, DockingWindow window) {
		if (getWindowParent() instanceof TabWindow && titleBar != null && titleBar.contains(SwingUtilities.convertPoint(this, p, titleBar))) {
			return acceptInteriorDrop(p, window);
		}

		return getWindowParent() instanceof TabWindow && getWindowParent().getChildWindowCount() == 1 ? null : super.doAcceptDrop(p, window);
	}

	@Override
	protected DropAction acceptInteriorDrop(Point p, DockingWindow window) {
		if (getWindowParent() instanceof TabWindow && titleBar != null && window.getWindowParent() != getWindowParent()) {
			Point p2 = SwingUtilities.convertPoint(this, p, titleBar);
			if (titleBar.contains(p2)) {
				if (!getInteriorDropFilter().acceptDrop(new InteriorDropInfo(window, this, p)))
					return null;

				addGhostTab(window);
				Component c = getWindowParent() instanceof TabWindow ? getWindowParent() : this;
				getRootWindow().setDragRectangle(SwingUtilities.convertRectangle(c, new Rectangle(0, 0, c.getWidth(), c.getHeight()), getRootWindow()));

				return titleBarDropAction;
			}
		}

		return null;
	}

	private void addGhostTab(DockingWindow window) {
		if (ghostTab == null) {
			ghostTab = ((AbstractTabWindow) getWindowParent()).createGhostTab(window);
			((AbstractTabWindow) getWindowParent()).getTabbedPanel().addTab(ghostTab);
			((AbstractTabWindow) getWindowParent()).getTabbedPanel().scrollTabToVisibleArea(ghostTab);
		}
	}

	@Override
	public String toString() {
		return getTitle();
	}

	void setRootWindow(RootWindow newRoot) {
		if (newRoot == null)
			return;

		RootWindow last = lastRootWindow == null ? null : (RootWindow) lastRootWindow.get();

		if (last == newRoot)
			return;

		if (last != null)
			last.removeView(this);

		lastRootWindow = new WeakReference(newRoot);
		newRoot.addView(this);
	}

	@Override
	protected void setFocused(boolean focused) {
		super.setFocused(focused);
		if (isfocused != focused) {
			isfocused = focused;

			if (focused)
				getViewProperties().getViewTitleBarProperties().getNormalProperties()
						.addSuperObject(getViewProperties().getViewTitleBarProperties().getFocusedProperties());
			else
				getViewProperties().getViewTitleBarProperties().getNormalProperties()
						.removeSuperObject(getViewProperties().getViewTitleBarProperties().getFocusedProperties());
		}
	}

	@Override
	protected void rootChanged(final RootWindow oldRoot, final RootWindow newRoot) {
		super.rootChanged(oldRoot, newRoot);
		setRootWindow(newRoot);

		// TODO: eliminate root window = null because triggers property updates.
		if (oldRoot != getRootWindow()) {
			if (oldRoot != null)
				rootProperties.removeSuperObject(oldRoot.getRootWindowProperties().getViewProperties());

			if (getRootWindow() != null) {
				rootProperties.addSuperObject(getRootWindow().getRootWindowProperties().getViewProperties());
			}
		}
	}

	@Override
	protected PropertyMap getPropertyObject() {
		return getViewProperties().getMap();
	}

	@Override
	protected PropertyMap createPropertyObject() {
		return new ViewProperties().getMap();
	}

	@Override
	protected boolean needsTitleWindow() {
		return getViewProperties().getAlwaysShowTitle();
	}

	private void checkLastFocusedComponent() {
		if (lastFocusedComponent != null && !SwingUtilities.isDescendingFrom(lastFocusedComponent, this)) {
			lastFocusedComponent.removeHierarchyListener(focusComponentListener);
			lastFocusedComponent = null;
		}
	}

	@Override
	void removeWindowComponent(DockingWindow window) {
		// do nothing
	}

	@Override
	void restoreWindowComponent(DockingWindow window) {
		// do nothing
	}

	private void updateTitleBar(Property property, Object valueContainer) {
		boolean changed = valueContainer == null;

		ViewTitleBarProperties titleBarProperties = getViewProperties().getViewTitleBarProperties();

		if (changed || property == ViewTitleBarProperties.VISIBLE) {
			if (titleBarProperties.getVisible()) {
				if (titleBar == null) {
					titleBar = new ViewTitleBar(this);
					this.setDockingWindowDragSource(new DockingWindowDragSource(titleBar, new DockingWindowDraggerProvider() {
						@Override
						public DockingWindowDragger getDragger(MouseEvent mouseEvent) {
							return getWindowProperties().getDragEnabled() ? startDrag(getRootWindow()) : null;
						}
					}));
					titleBar.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {
							fireTabWindowMouseButtonEvent(e);
							checkPopupMenu(e);
						}

						@Override
						public void mouseClicked(MouseEvent e) {
							fireTabWindowMouseButtonEvent(e);
						}

						@Override
						public void mouseReleased(MouseEvent e) {
							fireTabWindowMouseButtonEvent(e);
							checkPopupMenu(e);
						}

						private void checkPopupMenu(MouseEvent e) {
							if (e.isPopupTrigger()) {
								showPopupMenu(e);
							}
						}
					});

					if (customTitleBarComponents != null)
						titleBar.updateCustomBarComponents(customTitleBarComponents);
					changed = true;
				}
			} else {
				if (titleBar != null) {
					remove(titleBar);
					titleBar.dispose();
					titleBar = null;

					changed = true;
				}
			}
		}

		if ((changed || property == ViewTitleBarProperties.ORIENTATION) && titleBar != null) {
			remove(titleBar);
			add(titleBar, ComponentUtil.getBorderLayoutOrientation(titleBarProperties.getOrientation()));
			changed = true;
		}

		if (changed || property == ViewTitleBarProperties.CONTENT_TITLE_BAR_GAP) {
			if (titleBar != null) {
				Direction orientation = titleBarProperties.getOrientation();
				int contentBarGap = titleBarProperties.getContentTitleBarGap();
				contentPanel.setBorder(new EmptyBorder(orientation == Direction.UP ? contentBarGap : 0, orientation == Direction.LEFT ? contentBarGap : 0,
						orientation == Direction.DOWN ? contentBarGap : 0, orientation == Direction.RIGHT ? contentBarGap : 0));
			} else {
				contentPanel.setBorder(null);
			}
		}
	}

	@Override
	protected void updateButtonVisibility() {
		super.updateButtonVisibility();

		if (titleBar != null)
			titleBar.updateViewButtons(null);
	}

	@Override
	protected void write(ObjectOutputStream out, WriteContext context, ViewWriter viewWriter) throws IOException {
		out.writeInt(WindowIds.VIEW);
		viewWriter.writeView(this, out, context);
	}

	public DockingWindowDragSource getDockingWindowDragSource() {
		return dockingWindowDragSource;
	}

	public void setDockingWindowDragSource(DockingWindowDragSource dockingWindowDragSource) {
		this.dockingWindowDragSource = dockingWindowDragSource;
	}
}
