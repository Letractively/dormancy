package at.schauer.gregor.dormancy.interceptor;

import at.schauer.gregor.dormancy.Dormancy;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
public class ServiceInterceptor implements MethodInterceptor {
	protected static final Logger logger = Logger.getLogger(ServiceInterceptor.class);
	@Inject
	protected Dormancy dormancy;

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object result = invocation.proceed();

		if (result != null) {
			byte[] bytes = SerializationUtils.serialize((Serializable) result);
			result = SerializationUtils.deserialize(bytes);
		}

		return result;
	}
}
