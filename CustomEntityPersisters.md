# Custom EntityPersisters #

As already mentioned, custom entity persisters can be registered for handling certain object types. This section demonstrates the necessary steps for a notional `User` entity.

The complete source code of the following listings can be found in the [code.google.com/p/dormancy/source/browse/#svn/trunk/sample sample module].

```
@Entity
public class User {
	private String username;
	private String mail;
	private transient String password;

	@Id
	public String getUsername() {
		return username;
	}

	// more getters and setters
}
```

Below, the implementation of a custom `EntityPersister` is outlined.
It ensures that the client may only retrieve insensitive data of the `User` entity and inhibits undesired overrides to certain properties.

```
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
```

The `clone_(T, Map<Object, Object>)` method simply creates a new instance of the object to clone and copies all	property values except the user´s `password` to it.

```
	@Override
	public <T extends User> User merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || tree.containsKey(trObj)) {
			return (User) tree.get(trObj);
		}
		User dbObj = (User) sessionFactory.getCurrentSession().get(trObj.getClass(), trObj.getUsername());
		return merge_(trObj, dbObj, tree);
	}
```

The `merge(T, Map<Object, Object>)` method retrieves the `User` entity with the same identifier than the given object if possible and passes it to another method, which is responsible for mering the properties.

```
	@Override
	public <T extends User> User merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || dbObj == null || tree.containsKey(trObj)) {
			return (User) tree.get(trObj);
		}
		BeanUtils.copyProperties(trObj, dbObj, new String[] {"username", "password"});
		return dbObj;
	}
```

The `merge_(T, T, Map<Object, Object>)` copies all properties except the identifier (`username`) and the `password` from the transient object to the persistent object previously loaded.
Thus the persistent object is attached to the current persistence context and all property changes are applied to it.

```
	@Override
	public Class<?>[] getSupportedTypes() {
		return new Class[] {User.class};
	}
}
```

When the `UserPersister` is registered, `Dormancy` invokes the method `getSupportedTypes()` and	registers the instance for all types returned.