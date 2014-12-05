package bigtree

import org.gradle.api.*

class RubyPlugin implements Plugin<Project> {
  
  void apply(Project project) {
    def rubyEnv = project.extensions.create('rubyEnv', RubyEnvExtension)
    rubyEnv.project = project
        
    project.task('installRuby') << {
      println "$project.info.prefix: $project.gradle.gradleVersion"
    }
    project.task('uninstallRuby') << {
      println "$project.info.prefix: $project.gradle.gradleVersion"
    }
  }
}
