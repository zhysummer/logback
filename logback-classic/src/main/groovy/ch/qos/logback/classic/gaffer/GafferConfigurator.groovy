/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.gaffer

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.sift.SiftingAppender
import ch.qos.logback.core.util.ContextUtil

import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

class GafferConfigurator {

  LoggerContext context
                         
  GafferConfigurator(LoggerContext context) {
    this.context = context
  }

  protected void informContextOfURLUsedForConfiguration(URL url) {
    ConfigurationWatchListUtil.setMainWatchURL(context, url);
  }

  void run(URL url) {
    informContextOfURLUsedForConfiguration(url);
    run(url.text);
  }

  void run(File file) {
    informContextOfURLUsedForConfiguration(file.toURI().toURL());
    run(file.text);
  }

  void run(String dslText) {
    Binding binding = new Binding();
    binding.setProperty("hostname", ContextUtil.localHostName);

    def configuration = new CompilerConfiguration()
    configuration.addCompilationCustomizers(importCustomizer())

    // caller data should take into account groovy frames
    new ContextUtil(context).addGroovyPackages(context.getFrameworkPackages());

    Script dslScript = new GroovyShell(binding, configuration).parse(dslText)

    dslScript.metaClass.mixin(ConfigurationDelegate)
    dslScript.setContext(context)
    dslScript.metaClass.getDeclaredOrigin = { dslScript }

    dslScript.run()
  }

  protected ImportCustomizer importCustomizer() {
    def customizer = new ImportCustomizer()


    def core = 'ch.qos.logback.core'
    customizer.addStarImports(core, "${core}.encoder", "${core}.read", "${core}.rolling", "${core}.status",
            "ch.qos.logback.classic.net")

    customizer.addImports(PatternLayoutEncoder.class.name)

    customizer.addStaticStars(Level.class.name)

    customizer.addStaticImport('off', Level.class.name, 'OFF')
    customizer.addStaticImport('error', Level.class.name, 'ERROR')
    customizer.addStaticImport('warn', Level.class.name, 'WARN')
    customizer.addStaticImport('info', Level.class.name, 'INFO')
    customizer.addStaticImport('debug', Level.class.name, 'DEBUG')
    customizer.addStaticImport('trace', Level.class.name, 'TRACE')
    customizer.addStaticImport('all', Level.class.name, 'ALL')

    customizer
  }

}