package bigtree

import org.gradle.api.Project
import org.gradle.api.Plugin

class RubyPlugin implements Plugin<Project> {
  
  void apply(Project project) {
    def rubyEnv = project.extensions.create('rubyEnv', RubyEnvExtension)
    rubyEnv.project = project
        
    project.task('installRuby') << {
    }
    
        
    project.task('uninstallRuby') << {
    }
  }
}
