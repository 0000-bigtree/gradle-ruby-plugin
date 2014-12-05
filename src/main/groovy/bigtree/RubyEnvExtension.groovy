package bigtree

import org.gradle.api.Project

class RubyEnvExtension {
  // ruby interpreter, options is jruby and ruby(MRI), current only 'jruby'
  final String ruby = 'jruby' 
  
  String rubyVer = '1.7.16'
  
  String extractPath
  
  String rubyDistrDependency
  //
  Project project

  def getRubyHome() {
    getExtractPath() + '/' + ruby + '-' + rubyVer
  }
  
  def getExtractPath() {
    if (null == extractPath || 0 == extractPath.length()) {
      return project.projectDir.getAbsolutePath()        
    }
    extractPath
  }
  
  def getRubyDistrDependency() {
    if((null == rubyDistrDependency || 0 ==  rubyDistrDependency.length())
    && ('jruby' ==  ruby)) {
      final ext = RubyPlugin.isWindows() ? 'zip' : 'tar.gz'
      return "org.jruby:jruby-dist:${rubyVer}:bin@${ext}"
    }
    rubyDistrDependency
  }
}
