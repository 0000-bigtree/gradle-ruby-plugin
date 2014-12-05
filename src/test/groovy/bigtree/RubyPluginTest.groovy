package bigtree

import org.gradle.api.*
import org.gradle.testfixtures.ProjectBuilder
import org.junit.*
  
class RubyPluginTest {
  
  @Test
  void installRubyTaskIsAddedToProject() {
    final Project project = ProjectBuilder.builder().build()
    project.apply plugin: bigtree.RubyPlugin
    assert project.tasks.findByName('installRuby')
  }
  
  @Test
  void uninstallRubyTaskIsAddedToProject() {
    final Project project = ProjectBuilder.builder().build()
    project.apply plugin: bigtree.RubyPlugin
    assert project.tasks.findByName('uninstallRuby')
  }
  
  @Test
  void configureRuby() {
    final Project project = ProjectBuilder.builder().build()
    project.apply plugin: bigtree.RubyPlugin
    assert project.rubyEnv.ruby == 'jruby'
  }
  
  @Test
  void configureRubyVer() {
    final Project project = ProjectBuilder.builder().build()
    project.apply plugin: bigtree.RubyPlugin
    project.rubyEnv.rubyVer = '2.2.2'
    assert project.rubyEnv.rubyVer == '2.2.2'
  }
  
  @Test
  void configureRubyHome() {
    final Project project = ProjectBuilder.builder().build()
    project.apply plugin: bigtree.RubyPlugin
    assert project.rubyEnv.rubyHome == "${project.projectDir}/${project.rubyEnv.ruby}-${project.rubyEnv.rubyVer}"
  }
  
  @Test
  void configureRubyDistrDependency() {
    final Project project = ProjectBuilder.builder().build()
    project.apply plugin: bigtree.RubyPlugin
    assert project.rubyEnv.rubyDistrDependency == "org.jruby:jruby-dist:1.7.16.1@zip"    
    project.rubyEnv.rubyVer = '2.2.2'
    assert project.rubyEnv.rubyDistrDependency == "org.jruby:jruby-dist:2.2.2@zip"
    project.rubyEnv.rubyDistrDependency = 'xxxx.zip'
    assert project.rubyEnv.rubyDistrDependency == 'xxxx.zip'
  }
}
