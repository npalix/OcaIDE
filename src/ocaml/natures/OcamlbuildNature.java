package ocaml.natures;

import ocaml.build.ocamlbuild.OcamlbuildBuilder;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

/**
 * This class represents the ocamlbuild projects nature.
 */
public class OcamlbuildNature implements IProjectNature {
	private IProject project;

	/**
	 * Nature unique identifier
	 */
	public static final String ID = "ocaml.ocamlbuildnature";

	/**
	 * This method is called when a nature is associated with a project.<br>
	 * We use it to register our Builder with the project.
	 */
	public void configure() throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] cmds = desc.getBuildSpec();
		ICommand[] newCmds = new ICommand[cmds.length + 1];

		/*
		 * copy cmds into newCmds, while shifting by one to put our Builder first
		 */
		System.arraycopy(cmds, 0, newCmds, 1, cmds.length);
		ICommand cmdBuilder = desc.newCommand();
		cmdBuilder.setBuilderName(OcamlbuildBuilder.ID);
		newCmds[0] = cmdBuilder;
		desc.setBuildSpec(newCmds);
		project.setDescription(desc, null);
		project.build(IncrementalProjectBuilder.AUTO_BUILD, null);
	}

	public void deconfigure() throws CoreException {
	}

	public IProject getProject() {
		return this.project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}
}