/*
 * Copyright vopen.xyz
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acmedcare.framework.newim.spi.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 反射工具类.
 *
 * <p>提供调用getter/setter方法, 访问私有变量, 调用私有方法, 获取泛型类型Class, 被AOP过的真实类等工具函数.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version 1.2
 */
public class ReflectionUtils {
  private static final String SETTER_PREFIX = "set";

  private static final String GETTER_PREFIX = "get";

  private static final String CGLIB_CLASS_SEPARATOR = "$$";

  /** 调用Getter方法. */
  public static Object invokeGetter(Object obj, String propertyName) {
    String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(propertyName);
    return invokeMethod(obj, getterMethodName, new Class[] {}, new Object[] {});
  }

  /** 调用Setter方法, 仅匹配方法名。 */
  public static void invokeSetter(Object obj, String propertyName, Object value) {
    String setterMethodName = SETTER_PREFIX + StringUtils.capitalize(propertyName);
    invokeMethodByName(obj, setterMethodName, new Object[] {value});
  }

  /** 直接读取对象属性值, 无视private/protected修饰符, 不经过getter函数. */
  public static Object getFieldValue(final Object obj, final String fieldName) {
    Field field = getAccessibleField(obj, fieldName);

    if (field == null) {
      throw new IllegalArgumentException(
          "Could not find field [" + fieldName + "] on target [" + obj + "]");
    }

    Object result = null;
    try {
      result = field.get(obj);
    } catch (IllegalAccessException e) {
    }
    return result;
  }

  /** 直接设置对象属性值, 无视private/protected修饰符, 不经过setter函数. */
  public static void setFieldValue(final Object obj, final String fieldName, final Object value) {
    Field field = getAccessibleField(obj, fieldName);

    if (field == null) {
      throw new IllegalArgumentException(
          "Could not find field [" + fieldName + "] on target [" + obj + "]");
    }

    try {
      field.set(obj, value);
    } catch (IllegalAccessException e) {
    }
  }

  /**
   * 直接调用对象方法, 无视private/protected修饰符. 用于一次性调用的情况，否则应使用getAccessibleMethod()函数获得Method后反复调用.
   * 同时匹配方法名+参数类型，
   */
  public static Object invokeMethod(
      final Object obj,
      final String methodName,
      final Class<?>[] parameterTypes,
      final Object[] args) {
    Method method = getAccessibleMethod(obj, methodName, parameterTypes);
    if (method == null) {
      throw new IllegalArgumentException(
          "Could not find method [" + methodName + "] on target [" + obj + "]");
    }

    try {
      return method.invoke(obj, args);
    } catch (Exception e) {
      throw convertReflectionExceptionToUnchecked(e);
    }
  }

  /**
   * 直接调用对象方法, 无视private/protected修饰符， 用于一次性调用的情况，否则应使用getAccessibleMethodByName()函数获得Method后反复调用.
   * 只匹配函数名，如果有多个同名函数调用第一个。
   */
  public static Object invokeMethodByName(
      final Object obj, final String methodName, final Object[] args) {
    Method method = getAccessibleMethodByName(obj, methodName);
    if (method == null) {
      throw new IllegalArgumentException(
          "Could not find method [" + methodName + "] on target [" + obj + "]");
    }

    try {
      return method.invoke(obj, args);
    } catch (Exception e) {
      throw convertReflectionExceptionToUnchecked(e);
    }
  }

  /**
   * 循环向上转型, 获取对象的DeclaredField, 并强制设置为可访问.
   *
   * <p>如向上转型到Object仍无法找到, 返回null.
   */
  public static Field getAccessibleField(final Object obj, final String fieldName) {
    Assert.notNull(obj, "object can't be null");
    Assert.isTrue(StringUtils.hasText(fieldName), "fieldName can't be blank");
    for (Class<?> superClass = obj.getClass();
        superClass != Object.class;
        superClass = superClass.getSuperclass()) {
      try {
        Field field = superClass.getDeclaredField(fieldName);
        makeAccessible(field);
        return field;
      } catch (NoSuchFieldException e) { // NOSONAR
        // Field不在当前类定义,继续向上转型
      }
    }
    return null;
  }

  /**
   * 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问. 如向上转型到Object仍无法找到, 返回null. 匹配函数名+参数类型。
   *
   * <p>用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object... args)
   */
  public static Method getAccessibleMethod(
      final Object obj, final String methodName, final Class<?>... parameterTypes) {
    Assert.notNull(obj, "object can't be null");
    Assert.isTrue(StringUtils.hasText(methodName), "methodName can't be blank");

    for (Class<?> searchType = obj.getClass();
        searchType != Object.class;
        searchType = searchType.getSuperclass()) {
      try {
        Method method = searchType.getDeclaredMethod(methodName, parameterTypes);
        makeAccessible(method);
        return method;
      } catch (NoSuchMethodException e) {
        // Method不在当前类定义,继续向上转型
      }
    }
    return null;
  }

  /**
   * 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问. 如向上转型到Object仍无法找到, 返回null. 只匹配函数名。
   *
   * <p>用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object... args)
   */
  public static Method getAccessibleMethodByName(final Object obj, final String methodName) {
    Assert.notNull(obj, "object can't be null");
    Assert.isTrue(StringUtils.hasText(methodName), "methodName can't be blank");

    for (Class<?> searchType = obj.getClass();
        searchType != Object.class;
        searchType = searchType.getSuperclass()) {
      Method[] methods = searchType.getDeclaredMethods();
      for (Method method : methods) {
        if (method.getName().equals(methodName)) {
          makeAccessible(method);
          return method;
        }
      }
    }
    return null;
  }

  /** 改变private/protected的方法为public，尽量不调用实际改动的语句，避免JDK的SecurityManager抱怨。 */
  public static void makeAccessible(Method method) {
    if ((!Modifier.isPublic(method.getModifiers())
            || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
        && !method.isAccessible()) {
      method.setAccessible(true);
    }
  }

  /** 改变private/protected的成员变量为public，尽量不调用实际改动的语句，避免JDK的SecurityManager抱怨。 */
  public static void makeAccessible(Field field) {
    if ((!Modifier.isPublic(field.getModifiers())
            || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
            || Modifier.isFinal(field.getModifiers()))
        && !field.isAccessible()) {
      field.setAccessible(true);
    }
  }

  /**
   * 通过反射, 获得Class定义中声明的泛型参数的类型, 注意泛型必须定义在父类处 如无法找到, 返回Object.class. eg. public UserDao extends
   * HibernateDao<User>
   *
   * @param clazz The class to introspect
   * @return the first generic declaration, or Object.class if cannot be determined
   */
  public static <T> Class<T> getClassGenricType(final Class clazz) {
    return getClassGenricType(clazz, 0);
  }

  /**
   * 通过反射, 获得Class定义中声明的父类的泛型参数的类型. 如无法找到, 返回Object.class.
   *
   * <p>如public UserDao extends HibernateDao<User,Long>
   *
   * @param clazz clazz The class to introspect
   * @param index the Index of the generic ddeclaration,start from 0.
   * @return the index generic declaration, or Object.class if cannot be determined
   */
  public static Class getClassGenricType(final Class clazz, final int index) {

    Type genType = clazz.getGenericSuperclass();

    if (!(genType instanceof ParameterizedType)) {
      return Object.class;
    }

    Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

    if ((index >= params.length) || (index < 0)) {
      return Object.class;
    }
    if (!(params[index] instanceof Class)) {
      return Object.class;
    }

    return (Class) params[index];
  }

  public static Class<?> getUserClass(Object instance) {
    Assert.notNull(instance, "Instance must not be null");
    Class clazz = instance.getClass();
    if ((clazz != null) && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
      Class<?> superClass = clazz.getSuperclass();
      if ((superClass != null) && !Object.class.equals(superClass)) {
        return superClass;
      }
    }
    return clazz;
  }

  /** 将反射时的checked exception转换为unchecked exception. */
  public static RuntimeException convertReflectionExceptionToUnchecked(Exception e) {
    if ((e instanceof IllegalAccessException)
        || (e instanceof IllegalArgumentException)
        || (e instanceof NoSuchMethodException)) {
      return new IllegalArgumentException(e);
    } else if (e instanceof InvocationTargetException) {
      return new RuntimeException(((InvocationTargetException) e).getTargetException());
    } else if (e instanceof RuntimeException) {
      return (RuntimeException) e;
    }
    return new RuntimeException("Unexpected Checked Exception.", e);
  }

  /**
   * 得到调用者的{@code ClassLoader}.
   *
   * @param caller 指定{@code Class}
   * @return {@code ClassLoader}
   */
  public static ClassLoader getCallerClassLoader(Class<?> caller) {
    return caller.getClassLoader();
  }

  /**
   * 执行指定类的静态方法
   *
   * @param clazz 要执行的{@code Class}
   * @param methodName 方法名称
   * @param params 参数
   * @return 方法执行后的返回值
   */
  public static Object invokeStaticMethod(Class<?> clazz, String methodName, Object... params) {
    Object result = null;
    Class<?>[] paramsClass = new Class[params.length];
    for (int i = 0, j = params.length; i < j; i++) {
      paramsClass[i] = params[i].getClass();
    }

    Method method = null;
    try {
      method = clazz.getMethod(methodName, paramsClass);
      result = method.invoke(null, params);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return result;
  }
}
