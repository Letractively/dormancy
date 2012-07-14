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
package at.schauer.gregor.dormancy.scenario.server.dao;

import at.schauer.gregor.dormancy.Dormancy;
import at.schauer.gregor.dormancy.scenario.shared.model.Employee;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * @author Gregor Schauer
 * @since 1.0.2
 */
@Transactional
public class DormancyScenarioDao {
	@Inject
	private SessionFactory sessionFactory;
	@Inject
	private Dormancy dormancy;

	public ArrayList<Employee> listEmployees() {
		return (ArrayList<Employee>) sessionFactory.getCurrentSession().createQuery("FROM Employee").list();
	}

	public void save(Employee employee) {
		Employee merge = dormancy.merge(employee);
		sessionFactory.getCurrentSession().save(merge);
	}
}
