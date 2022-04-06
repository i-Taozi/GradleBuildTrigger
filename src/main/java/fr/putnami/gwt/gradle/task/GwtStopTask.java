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

import org.gradle.api.tasks.TaskAction;

import java.io.OutputStream;
import java.net.Socket;

import fr.putnami.gwt.gradle.extension.JettyOption;
import fr.putnami.gwt.gradle.extension.PutnamiExtension;

public class GwtStopTask extends AbstractTask {

	public static final String NAME = "gwtStop";

	public GwtStopTask() {
		setDescription("Stop jetty");
	}

	@TaskAction
	public void exec() throws Exception {
		PutnamiExtension putnami = getProject().getExtensions().getByType(PutnamiExtension.class);
		JettyOption jettyOption = putnami.getJetty();

		getLogger().info("Stopping jetty");
		Socket soket = new Socket("localhost", jettyOption.getStopPort());
		soket.setSoLinger(false, 0);
		OutputStream out = soket.getOutputStream();
		out.write((jettyOption.getStopKey() + "\r\nstop\r\n").getBytes());
		out.flush();
		soket.close();
	}

}
