package bigtree

import org.gradle.api.*
import org.gradle.testfixtures.ProjectBuilder
import org.junit.*
  
class RubyPluginTest {
  @Test
  void infoTaskIsAddedToProject() {
    final Project project = ProjectBuilder.builder().build()
    project.apply plugin: bigtree.RubyPlugin
    assert project.tasks.findByName('info')
  }
  
  @Test
  void configurePrefix() {
    final Project project = ProjectBuilder.builder().build()
    project.apply plugin: bigtree.RubyPlugin
    project.info.prefix = 'Sample'
    assert project.info.prefix == 'Sample'
  }
}
