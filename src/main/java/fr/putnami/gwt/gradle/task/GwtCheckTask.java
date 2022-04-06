/**
 * This file is part of pwt.
 *
 * pwt is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * pwt is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with pwt. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.gwt.gradle.task;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import fr.putnami.gwt.gradle.action.JavaAction;
import fr.putnami.gwt.gradle.extension.CompilerOption;
import fr.putnami.gwt.gradle.extension.PutnamiExtension;
import fr.putnami.gwt.gradle.helper.CompileCommandBuilder;

public class GwtCheckTask extends AbstractTask {

	public static final String NAME = "gwtCheck";

	private List<String> modules;
	private FileCollection src;
	private File war;

	public GwtCheckTask() {
		setDescription("Check the GWT modules");

		dependsOn(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaPlugin.PROCESS_RESOURCES_TASK_NAME);
	}

	@TaskAction
	public void exec() {

		PutnamiExtension putnami = getProject().getExtensions().getByType(PutnamiExtension.class);
		CompilerOption compilerOptions = putnami.getCompile();
		CompileCommandBuilder commandBuilder = new CompileCommandBuilder();
		commandBuilder.addArg("-validateOnly");
		commandBuilder.configure(getProject(), compilerOptions, getSrc(), null, getModules());
		JavaAction compileAction = commandBuilder.buildJavaAction();
		compileAction.execute(this);
		compileAction.join();
		if (compileAction.exitValue() != 0) {
			throw new RuntimeException("Fail to compile GWT modules");
		}
	}

	public void configure(final Project project, final PutnamiExtension extention) {
		final CompilerOption options = extention.getCompile();

		options.init(getProject());

		JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
		SourceSet mainSourceSet = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		final FileCollection sources = getProject()
			.files(project.files(mainSourceSet.getOutput().getResourcesDir()))
			.plus(project.files(mainSourceSet.getOutput().getClassesDirs()))
			.plus(getProject().files(mainSourceSet.getAllSource().getSrcDirs()));

		ConventionMapping mapping = ((IConventionAware) this).getConventionMapping();

		mapping.map("modules", new Callable<List<String>>() {
			@Override
			public List<String> call() {
				return extention.getModule();
			}
		});
		mapping.map("war", new Callable<File>() {
			@Override
			public File call()  {
				return new File(getProject().getBuildDir(), "out");
			}
		});
		mapping.map("src", new Callable<FileCollection>() {
			@Override
			public FileCollection call()  {
				return sources;
			}
		});
	}

	@OutputDirectory
	public File getWar() {
		return war;
	}

	@Input
	public List<String> getModules() {
		return modules;
	}

	@InputFiles
	public FileCollection getSrc() {
		return src;
	}
}
