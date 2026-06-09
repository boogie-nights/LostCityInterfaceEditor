package org.lostcityinterfaceeditor.models;

import javafx.scene.layout.Pane;
import org.lostcityinterfaceeditor.helpers.LayoutHelper;

import java.util.List;

public class ApplicationState {

	private LayoutHelper activeLayout = LayoutHelper.Standard;

	private Pane interfaceRenderArea;

	private List<InterfaceComponent> interfaceComponents;

	public LayoutHelper getActiveLayout() {
		return activeLayout;
	}

	public void setActiveLayout(LayoutHelper activeLayout) {
		this.activeLayout = activeLayout;
	}

	public Pane getInterfaceRenderArea() {
		return interfaceRenderArea;
	}

	public void setInterfaceRenderArea(Pane interfaceRenderArea) {
		this.interfaceRenderArea = interfaceRenderArea;
	}

	public List<InterfaceComponent> getInterfaceComponents() {
		return interfaceComponents;
	}

	public void setInterfaceComponents(List<InterfaceComponent> interfaceComponents) {
		this.interfaceComponents = interfaceComponents;
	}
}
