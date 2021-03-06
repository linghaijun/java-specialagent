/* Copyright 2019 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentracing.contrib.specialagent.rule.mule4.module.artifact;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import org.mule.runtime.module.artifact.api.classloader.ClassLoaderFilter;

public class FilteringArtifactAgentIntercept {
  private static final Logger logger = Logger.getLogger(FilteringArtifactAgentIntercept.class.getName());
  private static final String CLS_SFX = ".class";
  private static final int CLS_SFX_LENGTH = CLS_SFX.length();

  public static Object exit(final Object thiz, final Object returned, final Object resObj, final Object filter, final Object artifactClassLoader) {
    final String resName = (String)resObj;

    // if it's a class that wasn't found then we continue
    if (returned != null || !resName.endsWith(CLS_SFX))
      return returned;

    // @see https://github.com/mulesoft/mule/blob/mule-4.2.2/modules/artifact/src/main/java/org/mule/runtime/module/artifact/api/classloader/FilteringArtifactClassLoader.java#L91
    final ClassLoaderFilter classLoaderFilter = (ClassLoaderFilter)filter;
    final String className = resName.substring(0, resName.length() - CLS_SFX_LENGTH).replace('/', '.');
    if (!classLoaderFilter.exportsClass(className))
      return null;

    final ArtifactClassLoader classLoader = (ArtifactClassLoader)artifactClassLoader;
    final Object result = classLoader.getClassLoader().getResource(resName);
    if (result == null && logger.isLoggable(Level.FINE))
      logger.fine("Could not locate resource " + resName + " in " + thiz);

    return result;
  }
}