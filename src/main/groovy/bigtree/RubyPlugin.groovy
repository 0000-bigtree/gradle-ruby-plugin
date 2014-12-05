package bigtree

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.Plugin

class RubyPlugin implements Plugin<Project> {
  
  void apply(Project project) {
    def rubyEnv = project.extensions.create('rubyEnv', RubyEnvExtension)
    rubyEnv.project = project
    
    project.configurations {
      rubyDistrDependency
    }
        
    project.task('installRuby') << {
      extractDistr(project)
    }
    
    project.task('reinstallRuby') << {
      deleteRubyHome(project)    
      extractDistr(project)
    }    
        
    project.task('uninstallRuby') << {
      deleteRubyHome(project)    
    }
  }
  
  def deleteRubyHome(project) {
    project.ant.delete(dir: project.rubyEnv.rubyHome)
  }
  
  def extractDistr(project) {
    // 添加依赖，根据 rubyEnv.rubyDistrDependency 的值
    project.dependencies {
      rubyDistrDependency project.rubyEnv.rubyDistrDependency
    } 
    
    // 取到最后加入的依赖，担心可能添加了多次
    final distrFile = project.configurations.rubyDistrDependency.last() 
    if(isWindows()) {
      project.ant.unzip(src: distrFile.absolutePath, 
      dest: project.rubyEnv.extractPath) 
    } else {
      project.ant.untar(src: distrFile.absolutePath, 
      compression: 'gzip',
      dest: project.rubyEnv.extractPath)           
    }
  }    

  
  static isWindows() {
    Os.isFamily(Os.FAMILY_WINDOWS)      
  }    
}
