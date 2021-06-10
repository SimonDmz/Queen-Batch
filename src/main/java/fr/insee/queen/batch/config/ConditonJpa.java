package fr.insee.queen.batch.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Class to determine the JPA Condition.
 * If we need to instantiante a JPA connection or not
 * @author samco
 *
 */
public class ConditonJpa implements Condition{

	/**
	 * This method override the Condition.class and checking if
	 * the application needs to be launch in JPA persistence mode
	 */
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		Environment env = context.getEnvironment();
		return null != env 
				&& "JPA".equals(env.getProperty("fr.insee.queen.application.persistenceType"));
	}
}
