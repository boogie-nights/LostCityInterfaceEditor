package org.lostcityinterfaceeditor.models;

import javafx.scene.layout.Pane;
import org.lostcityinterfaceeditor.helpers.LayoutHelper;

import java.util.List;

public class ApplicationState {

	// I hate this design, really gonna have to do better. Smdh.
	private static ApplicationState applicationState;

	private ApplicationState() {}

	public static ApplicationState getApplicationState() {
		if (applicationState == null) {
			applicationState = new ApplicationState();
		}
		return applicationState;
	}

	// these three feel related...
	private LayoutHelper activeLayout = LayoutHelper.Standard;

	private Pane interfaceRenderArea;

	private List<InterfaceComponent> interfaceComponents;

	// This guy is definitely different
	private String serverDirectoryPath;





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

    public String getServerDirectoryPath() {
        return serverDirectoryPath;
    }

    public void setServerDirectoryPath(String serverDirectoryPath) {
        this.serverDirectoryPath = serverDirectoryPath;
    }
}
