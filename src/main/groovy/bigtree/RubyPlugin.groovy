package bigtree

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.Plugin
// 1. 遗留，当项目未创建时，执行命令，其当前目录应选择为HOME，而不是项目目录

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
      description 'Install Ruby'
      extractDistr(project)
      setExecutable(project)
      changeToDefaultGemSource(project)
      installDefaultGems(project)
    }
    
    project.task('reinstallRuby') << {
      description 'Reinstall Ruby, will delete installed Ruby previously'
      deleteRubyHome(project)    
      extractDistr(project)
      setExecutable(project)
      changeToDefaultGemSource(project)
      installDefaultGems(project)
    }    
        
    project.task('uninstallRuby') << {
      description 'Uninstall Ruby, will delete installed Ruby'        
      deleteRubyHome(project)    
    }
    
    project.task('addOfficialGemSource') << {
      addOrRemoveGemSource(project, true, project.rubyEnv.officialGemSource)    
    }
    
    project.task('addGemSource') << {  
      if (project.hasProperty('gemSource')) {
        final fromArg = project.gemSource
        if (null != fromArg && 0 < fromArg.length()) {
          addOrRemoveGemSource(project, true, fromArg)
        }
      }      
    }
    
    project.task('removeGemSource') << {  
      if (project.hasProperty('gemSource')) {
        final fromArg = project.gemSource
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
          
          // 执行 bundle，安装 gem
          exec(project, "-S bundle")   
        } else {
          // 非 rails 项目 
          new File(project.rubyProject.nameWithPath).mkdirs()
          if (project.rubyProject.isCreateGemfile) {
            createNewGemfile(project, "${project.rubyProject.nameWithPath}/Gemfile")
          }
        }
        if('jruby' == project.rubyEnv.engine) {
          // 执行 warble config，生成 warble 配置文件 
          exec(project, "-S warble config")
        }
      }
      
      project.task('war', type: org.gradle.api.tasks.bundling.War) << {
        // 准备好 assets
        def cmd = "-S rake assets:clobber assets:precompile"
        exec(project, cmd)
        // 打包为 war
        cmd = "-S warble compiled war"
        exec(project, cmd)
        
        // 移动到生成的 war 包到 project.buildDir
        def f = new File(project.rubyProject.nameWithPath)
        def name = f.name
        name = (name[0].toLowerCase() + name.substring(1) + '.war')
        def warFileWithPath = new File(f, name)
        ant.move(file: warFileWithPath, tofile: "${project.buildDir}/libs/${project.name}-${project.version}.war")
      }
      
      project.task('rails') << {
        def args = project.hasProperty('args') ? project.args : ''
        if (null == args || 0 >= args.length()) {
          args = ''
        }
        def cmd = "-S rails ${args}"
        exec(project, cmd)
      }      
      
      project.task('rackup') << {
        def args = project.hasProperty('args') ? project.args : ''
        if (null == args || 0 >= args.length()) {
          args = ''
        }
        def cmd = "-S rackup ${args}"
        exec(project, cmd)
      }  
      
      project.task('rake') << {
        def args = project.hasProperty('args') ? project.args : ''
        if (null == args || 0 >= args.length()) {
          args = ''
        }
        def cmd = "-S rake ${args}"
        exec(project, cmd)
      }      
      
      project.task('bundle') << {
        def args = project.hasProperty('args') ? project.args : ''
        if (null == args || 0 >= args.length()) {
          args = ''
        }
        def cmd = "-S bundle ${args}"
        exec(project, cmd)
      }      
      
      project.task('gem') << {
        def args = project.hasProperty('args') ? project.args : ''
        if (null == args || 0 >= args.length()) {
          args = 'list --local'
        }
        def cmd = "-S gem ${args}"
        exec(project, cmd)
      }      

      project.task('exec') << {  
        if (project.hasProperty('cmds')) {
          def cmds = project.cmds
          cmds.split(';').each {
            def cmd = "-S ${it}"
            exec(project, cmd)
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
    if (null == gems || 0 >= gems.length()) {
      gems = ''
    }
    gems += ' bundler '
    if('jruby' == project.rubyEnv.engine) {
      gems += ' warbler '      
    }
    installGems(project, gems)
  }
  
  def installGems(project, gems) {
    def cmd = "-S gem install ${gems} -N -V"
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
    def gemfile = new File(gemfileWithPath)
    if(!gemfile.exists()) {
      gemfile.createNewFile()
    }
    gemfile.setText('')
    def source = project.rubyProject.gemfileSource 
    if (null == source || 0 >= source.length()) {
      source = project.rubyEnv.defaultGemSource
    } 
    if (null == source || 0 >= source.length()) {
      source = project.rubyEnv.officialGemSource
    }    
    gemfile << "source '${source}'"
    gemfile << "${System.getProperty("line.separator")}ruby '${project.rubyEnv.rubyVer}', engine: '${project.rubyEnv.engine}', engine_version: '${project.rubyEnv.engineVer}'"
    gemfile << System.getProperty("line.separator") * 2
    gemfile << "# gem 'log4r', '~> 1.1.10'${System.getProperty("line.separator")}"
    gemfile << "# gem 'puma', '~> 2.10.2'${System.getProperty("line.separator")}"    
  }
  
  def exec(project, cmd) {
    def executable = getRubyExecutableWithPath(project)
    project.ant.exec(dir: project.rubyProject.nameWithPath, executable: executable) {
      env(key: 'HOME', value: project.rubyEnv.rubyHome)
      env(key: 'JRUBY_HOME', value: project.rubyEnv.rubyHome)
      arg(line: cmd)
    }    
  }
  
  static isWindows() {
    Os.isFamily(Os.FAMILY_WINDOWS)
  }    
}
