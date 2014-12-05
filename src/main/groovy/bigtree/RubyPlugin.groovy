package bigtree

import org.gradle.api.*

class RubyPlugin implements Plugin<Project> {
  void apply(Project project) {
    project.extensions.create('info', RubyPluginExtension)
    project.task('info') << {
      println "$project.info.prefix: $project.gradle.gradleVersion"
    }
  }
}
