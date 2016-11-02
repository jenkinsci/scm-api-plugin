/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jenkins.scm.api;

import java.lang.reflect.Method;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.Validate;

/**
 * The bits of commons-lang3 MethodUtils that we need to check if protected methods have been overridden.
 */
class MethodUtils {
    /**
     * Checks if the  method defined on the base type with the given arguments
     * are overridden in the given derived type.
     */
    public static boolean isOverridden(@Nonnull Class base, @Nonnull Class derived, @Nonnull String methodName,
                                       @Nonnull Class... types) {
        Method baseMethod = getMatchingMethod(base, methodName, types);
        Method derivedMethod = getMatchingMethod(derived, methodName, types);
        return baseMethod == null ? derivedMethod != null : !baseMethod.equals(derivedMethod);
    }

    // Remaining methods

    /**
     * <p>Retrieves a method whether or not it's accessible. If no such method
     * can be found, return {@code null}.</p>
     *
     * @param cls            The class that will be subjected to the method search
     * @param methodName     The method that we wish to call
     * @param parameterTypes Argument class types
     * @return The method
     */
    public static Method getMatchingMethod(final Class<?> cls, final String methodName,
                                           final Class<?>... parameterTypes) {
        Validate.notNull(cls, "Null class not allowed.");
        Validate.notEmpty(methodName, "Null or blank methodName not allowed.");

        // Address methods in superclasses
        Method[] methodArray = cls.getDeclaredMethods();
        final List<Class<?>> superclassList = ClassUtils.getAllSuperclasses(cls);
        for (final Class<?> klass : superclassList) {
            methodArray = (Method[])ArrayUtils.addAll(methodArray, klass.getDeclaredMethods());
        }

        Method inexactMatch = null;
        for (final Method method : methodArray) {
            if (methodName.equals(method.getName()) &&
                    ArrayUtils.isEquals(parameterTypes, method.getParameterTypes())) {
                return method;
            } else if (methodName.equals(method.getName()) &&
                    ClassUtils.isAssignable(parameterTypes, method.getParameterTypes(), true)) {
                if (inexactMatch == null) {
                    inexactMatch = method;
                } else if (distance(parameterTypes, method.getParameterTypes())
                        < distance(parameterTypes, inexactMatch.getParameterTypes())) {
                    inexactMatch = method;
                }
            }

        }
        return inexactMatch;
    }

    /**
     * <p>Returns the aggregate number of inheritance hops between assignable argument class types.  Returns -1
     * if the arguments aren't assignable.  Fills a specific purpose for getMatchingMethod and is not generalized.</p>
     *
     * @param classArray
     * @param toClassArray
     * @return the aggregate number of inheritance hops between assignable argument class types.
     */
    private static int distance(final Class<?>[] classArray, final Class<?>[] toClassArray) {
        int answer = 0;

        if (!ClassUtils.isAssignable(classArray, toClassArray, true)) {
            return -1;
        }
        for (int offset = 0; offset < classArray.length; offset++) {
            // Note InheritanceUtils.distance() uses different scoring system.
            if (classArray[offset].equals(toClassArray[offset])) {
                continue;
            } else if (ClassUtils.isAssignable(classArray[offset], toClassArray[offset], true)
                    && !ClassUtils.isAssignable(classArray[offset], toClassArray[offset], false)) {
                answer++;
            } else {
                answer = answer + 2;
            }
        }

        return answer;
    }
}
