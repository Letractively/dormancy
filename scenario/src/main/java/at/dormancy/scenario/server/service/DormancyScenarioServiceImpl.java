/*
 * Copyright 2013 Gregor Schauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.dormancy.scenario.server.service;

import at.dormancy.scenario.client.service.DormancyScenarioService;
import at.dormancy.scenario.server.DormancyScenarioModule;
import at.dormancy.scenario.server.dao.DormancyScenarioDao;
import at.dormancy.scenario.shared.model.Employee;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.List;

/**
 * @author Gregor Schauer
 * @since 1.0.2
 */
public class DormancyScenarioServiceImpl extends RemoteServiceServlet implements DormancyScenarioService {
	@Inject
	DormancyScenarioDao dormancyScenarioDao;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletContext servletContext = config.getServletContext();
		try {
			// Attempt to inject the dependencies via Spring
			WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
			ctx.getAutowireCapableBeanFactory().autowireBean(this);
		} catch (IllegalStateException e) {
			// If Spring is not configured, use the equivalent Guice configuration instead
			Injector injector = Guice.createInjector(new DormancyScenarioModule());
			injector.injectMembers(this);
		}
	}

	@Override
	public List<Employee> listEmployees() {
		return dormancyScenarioDao.listEmployees();
	}

	@Override
	public void save(Employee employee) {
		dormancyScenarioDao.save(employee);
	}
}
