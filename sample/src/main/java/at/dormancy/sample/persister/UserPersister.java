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
package at.dormancy.sample.persister;

import at.dormancy.persister.AbstractEntityPersister;
import at.dormancy.sample.model.User;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeanUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;

/**
 * @author Gregor Schauer
 */
public class UserPersister extends AbstractEntityPersister<User> {
	@Inject
	SessionFactory sessionFactory;

	public UserPersister() {
		setSupportedTypes(Collections.<Class<? extends User>>singleton(User.class));
	}

	@Override
	public <T extends User> User clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (dbObj == null || tree.containsKey(dbObj)) {
			return (User) tree.get(dbObj);
		}
		// Create a new user object
		User trObj = new User();
		// Copy all properties except the transient one
		BeanUtils.copyProperties(dbObj, trObj, new String[]{"password"});
		return trObj;
	}

	@Override
	public <T extends User> User merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || tree.containsKey(trObj)) {
			return (User) tree.get(trObj);
		}
		// Retrieve the persistent entity with the same name and merge them
		User dbObj = (User) sessionFactory.getCurrentSession().get(trObj.getClass(), trObj.getUsername());
		return merge_(trObj, dbObj, tree);
	}

	@Override
	public <T extends User> User merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || dbObj == null || tree.containsKey(trObj)) {
			return (User) tree.get(trObj);
		}
		// Copy all properties from the transient entity to the persistent entity except username and password
		// username is the primary key and might not be changed anyway
		// The client must not modify the password this way i.e., prohibiting identity theft
		BeanUtils.copyProperties(trObj, dbObj, new String[]{"username", "password"});
		return dbObj;
	}
}
