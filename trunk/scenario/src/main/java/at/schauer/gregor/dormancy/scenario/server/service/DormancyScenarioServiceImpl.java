/*
 * Copyright 2012 Gregor Schauer
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
package at.schauer.gregor.dormancy.scenario.server.service;

import at.schauer.gregor.dormancy.scenario.client.service.DormancyScenarioService;
import at.schauer.gregor.dormancy.scenario.server.dao.DormancyScenarioDao;
import at.schauer.gregor.dormancy.scenario.shared.model.Employee;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.ArrayList;

/**
 * @author Gregor Schauer
 */
public class DormancyScenarioServiceImpl extends RemoteServiceServlet implements DormancyScenarioService {
	private WebApplicationContext ctx;
	@Inject
	private DormancyScenarioDao dormancyScenarioDao;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletContext servletContext = config.getServletContext();
		ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		ctx.getAutowireCapableBeanFactory().autowireBean(this);
	}

	@Override
	public ArrayList<Employee> listEmployees() {
		return dormancyScenarioDao.listEmployees();
	}

	@Override
	public void save(Employee employee) {
		dormancyScenarioDao.save(employee);
	}
}
