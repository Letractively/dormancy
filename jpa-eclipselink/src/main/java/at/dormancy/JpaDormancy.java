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
package at.dormancy;

import at.dormancy.persistence.JpaPersistenceUnitProvider;
import at.dormancy.persistence.PersistenceUnitProvider;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;

/**
 * Clones JPA entities and merges them into a {@link EntityManager}.
 * <p/>
 * This class is a convenient subclass for using {@link Dormancy} along with Hibernate.<br/>
 * Its main purpose is to avoid the specification of the generic types explicitly. Thus the appropriate types for
 * <i>persistence unit</i>, <i>persistence context</i> and <i>persistence metadata</i> are used.
 * <p/>
 * Using this class is identical to the following definition:
 * <pre>
 * Dormancy&lt;EntityManagerFactory, EntityManager, EntityType&lt;&#63;&gt;&gt; dormancy;
 * </pre>
 *
 * @author Gregor Schauer
 * @see Dormancy
 */
public class JpaDormancy extends Dormancy<EntityManagerFactory, EntityManager, EntityType<?>> {
	@Inject
	public JpaDormancy(@Nonnull EntityManagerFactory emf) {
		this(new JpaPersistenceUnitProvider(emf));
	}

	public JpaDormancy(@Nonnull PersistenceUnitProvider<EntityManagerFactory, EntityManager, EntityType<?>> persistenceUnitProvider) {
		super(persistenceUnitProvider);
	}
}
