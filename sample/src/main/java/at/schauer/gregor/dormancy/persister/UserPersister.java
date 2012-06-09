package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.model.User;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeanUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Map;

/**
 * @author Gregor Schauer
 */
public class UserPersister extends AbstractEntityPersister<User> {
	@Inject
	SessionFactory sessionFactory;

	@Override
	public <T extends User> User clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (dbObj == null || tree.containsKey(dbObj)) {
			return (User) tree.get(dbObj);
		}
		User trObj = new User();
		BeanUtils.copyProperties(dbObj, trObj, new String[] {"password"});
		return trObj;
	}

	@Override
	public <T extends User> User merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || tree.containsKey(trObj)) {
			return (User) tree.get(trObj);
		}
		User dbObj = (User) sessionFactory.getCurrentSession().get(trObj.getClass(), trObj.getUsername());
		return merge_(trObj, dbObj, tree);
	}

	@Override
	public <T extends User> User merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || dbObj == null || tree.containsKey(trObj)) {
			return (User) tree.get(trObj);
		}
		BeanUtils.copyProperties(trObj, dbObj, new String[] {"username", "password"});
		return dbObj;
	}

	@Override
	public Class<?>[] getSupportedTypes() {
		return new Class[] {User.class};
	}
}
