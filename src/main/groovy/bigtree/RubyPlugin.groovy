package bigtree

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.Plugin

class RubyPlugin implements Plugin<Project> {
  
  void apply(Project project) {
    def rubyEnv = project.extensions.create('rubyEnv', RubyEnvExtension)
    rubyEnv.project = project
    
    def rubyProject = project.extensions.create('rubyProject', RubyProjectExtension)
    rubyProject.project = project    
    
    project.configurations {
      rubyDistrDependency
    }
        
    project.task('installRuby') << {
      extractDistr(project)
      setExecutable(project)
      changeToDefaultGemSource(project)
      installDefaultGems(project)
    }
    
    project.task('reinstallRuby') << {
      deleteRubyHome(project)    
      extractDistr(project)
      setExecutable(project)
      changeToDefaultGemSource(project)
      installDefaultGems(project)
    }    
        
    project.task('uninstallRuby') << {
      deleteRubyHome(project)    
    }
    
    project.task('addOfficialGemSource') << {
      addOrRemoveGemSource(project, true, project.rubyEnv.officialGemSource)    
    }
    
    project.task('addGemSource') << {  
      if (project.hasProperty('gemSource')) {
        final fromArg = project.getProperty('gemSource')
        if (null != fromArg && 0 < fromArg.length()) {
          addOrRemoveGemSource(project, true, fromArg)
        }
      }      
    }
    
    project.task('removeGemSource') << {  
      if (project.hasProperty('gemSource')) {
        final fromArg = project.getProperty('gemSource')
        if (null != fromArg && 0 < fromArg.length()) {
          addOrRemoveGemSource(project, false, fromArg)
        }
      }      
    }
    
    project.task('newProject') << {  
        def gems = project.rubyProject.defaultGems
        if (null != gems && 0 < gems.length()) {
          installGems(project, gems)
        }
        if (project.rubyProject.isRailsProject) {
          println project.rubyProject.isRailsProject
          println project.rubyProject.nameWithPath
          
          // 如果是 rails 项目，利用 rails new 命令来创建项目
          def cmd = "-S rails new ${project.rubyProject.nameWithPath} --skip-bundle ${project.rubyProject.railsNewArgs}"
          def executable = getRubyExecutableWithPath(project)
          ant.exec(executable: executable) {
            env(key: 'HOME', value: project.rubyEnv.rubyHome)
            env(key: 'JRUBY_HOME', value: project.rubyEnv.rubyHome)
            arg(line: cmd)
          } 
          // 将 rails 生成的 Gemfile中的 Gem Source 替换为指定的 Source
          def source = project.rubyProject.gemfileSource 
          if (null == source || 0 >= source.length()) {
            source = project.rubyEnv.defaultGemSource
          } 
          if (null == source || 0 >= source.length()) {
            source = project.rubyEnv.officialGemSource
          }
          replaceGemfileSource(project, "${project.rubyProject.nameWithPath}/Gemfile", source)
        } else {
          // 非 rails 项目 
        }
      }

      project.task('exec') << {  
        if (project.hasProperty('cmds')) {
          def cmds = project.cmds
          cmds.split(';').each {
            def cmd = "-S ${it}"
            ant.exec(dir: project.rubyProject.nameWithPath, 
                     executable: getRubyExecutableWithPath(project)) {
              env(key: 'HOME', value: project.rubyEnv.rubyHome)
              env(key: 'JRUBY_HOME', value: project.rubyEnv.rubyHome)
              arg(line: cmd)
            }
          }          
        }
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
  
  // *nix下，要设置这些脚本的可执行权限
  def setExecutable(project) {
    def cmd = "500 ast gem irb jgem jirb jirb_swing jruby jruby.bash jruby.sh jrubyc rake rdoc ri testrb"
    project.ant.exec(dir: "${project.rubyEnv.rubyHome}/bin", 
                     executable: 'chmod', 
                     osfamily: 'unix') {
                       arg(line: cmd)      
                     }    
  }  
  
  // 添加 Gem 源 URL
  def addOrRemoveGemSource(project, isAdd, source) {
    def flag = isAdd ? '-a' : '-r'
    def cmd = "-S gem sources -c ${flag} ${source}"
    def executable = getRubyExecutableWithPath(project)
    project.ant.exec(executable: executable) {
        env(key: 'HOME', value: project.rubyEnv.rubyHome)
        env(key: 'JRUBY_HOME', value: project.rubyEnv.rubyHome)
        arg(line: cmd)      
    } 
  }    
  
  def changeToDefaultGemSource(project) {
    addOrRemoveGemSource(project, false, project.rubyEnv.officialGemSource)
    addOrRemoveGemSource(project, true, project.rubyEnv.defaultGemSource)   
  }

  def getRubyExecutableWithPath(project) {
    if ('jruby' == project.rubyEnv.engine) {
      def executable = isWindows() ? "jruby.exe" : "jruby"
      "${project.rubyEnv.rubyHome}/bin/${executable}"
    } else {
      def executable = isWindows() ? "ruby.exe" : "ruby"
      "${project.rubyEnv.rubyHome}/bin/${executable}"      
    }
  }
  
  def installDefaultGems(project) {
    def gems = project.rubyEnv.defaultGems
    if (null != gems && 0 < gems.length()) {
      installGems(project, gems)
    }
  }
  
  def installGems(project, gems) {
    def cmd = "-S gem install ${gems} -N"
    def executable = getRubyExecutableWithPath(project)
    project.ant.exec(executable: executable) {
      env(key: 'HOME', value: project.rubyEnv.rubyHome)
      env(key: 'JRUBY_HOME', value: project.rubyEnv.rubyHome)
      arg(line: cmd)      
    } 
  }
  
  def replaceGemfileSource(project, gemfileWithPath, source) {
    def contents = new File(gemfileWithPath).getText()
    contents = contents.replace("rubygems.org'", "rubygems.org'${System.getProperty("line.separator")}ruby '${project.rubyEnv.rubyVer}', engine: '${project.rubyEnv.engine}', engine_version: '${project.rubyEnv.engineVer}'")
    def newFile = new File(gemfileWithPath)
    newFile.setText("source '${source}'")
    newFile << System.getProperty("line.separator") 
    newFile << "# " << contents
  }  
  
  def createNewGemfile(project, gemfileWithPath) {
    gemfile = file(gemfileWithPath)
    if(!gemfile.isExists()) {
      gemfile.createNew()
    }
    gemfile.setText('')
    gemfile << ''
  }
  
  static isWindows() {
    Os.isFamily(Os.FAMILY_WINDOWS)
  }    
}
