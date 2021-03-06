/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.dao.support;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * AOP Alliance MethodInterceptor that provides persistence exception translation
 * based on a given PersistenceExceptionTranslator.
 *
 * <p>Delegates to the given PersistenceExceptionTranslator to translate any
 * RuntimeException thrown into Spring's DataAccessException hierarchy
 * (if appropriate). If the RuntimeException in question is declared on the
 * target method, it is always propagated as-is (with no translation applied).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see PersistenceExceptionTranslator
 */
public class PersistenceExceptionTranslationInterceptor implements MethodInterceptor, InitializingBean {

	private PersistenceExceptionTranslator persistenceExceptionTranslator;


	/**
	 * Create a new PersistenceExceptionTranslationInterceptor.
	 * Needs to be configured with a PersistenceExceptionTranslator afterwards.
	 * @see #setPersistenceExceptionTranslator
	 */
	public PersistenceExceptionTranslationInterceptor() {
	}

	/**
	 * Create a new PersistenceExceptionTranslationInterceptor
	 * for the given PersistenceExceptionTranslator.
	 * @param persistenceExceptionTranslator the PersistenceExceptionTranslator to use
	 */
	public PersistenceExceptionTranslationInterceptor(PersistenceExceptionTranslator persistenceExceptionTranslator) {
		setPersistenceExceptionTranslator(persistenceExceptionTranslator);
	}

	/**
	 * Specify the PersistenceExceptionTranslator to use.
	 */
	public final void setPersistenceExceptionTranslator(PersistenceExceptionTranslator persistenceExceptionTranslator) {
		Assert.notNull(persistenceExceptionTranslator, "PersistenceExceptionTranslator must not be null");
		this.persistenceExceptionTranslator = persistenceExceptionTranslator;
	}

	public void afterPropertiesSet() {
		if (this.persistenceExceptionTranslator == null) {
			throw new IllegalArgumentException("persistenceExceptionTranslator is required");
		}
	}


	public Object invoke(MethodInvocation mi) throws Throwable {
		try {
			return mi.proceed();
		}
		catch (RuntimeException ex) {
			// Let it throw raw if the type of the exception is on the throws clause of the method.
			Class[] declaredExceptions = mi.getMethod().getExceptionTypes();
			for (int i = 0; i < declaredExceptions.length; i++) {
				if (declaredExceptions[i].isInstance(ex)) {
					throw ex;
				}
			}
			throw DataAccessUtils.translateIfNecessary(ex, this.persistenceExceptionTranslator);
		}
	}

}
