package bigtree

import org.gradle.api.Project

class RubyEnvExtension {
  // ruby interpreter, options is jruby and ruby(MRI), current only 'jruby'
  final String ruby = 'jruby' 
  
  String rubyVer = '1.7.16'
  
  String rubyDistrDependency
  
  String extractPath = 'rubybin'
  
  final String officialGemSource = 'https://rubygems.org/'
  
  String defaultGemSource = 'http://ruby.taobao.org/'
  
  String defaultGems = 'rubygems-update rake bundler'
  
  //
  Project project

  def getRubyDistrDependency() {
    if((null == rubyDistrDependency || 0 ==  rubyDistrDependency.length())
    && ('jruby' ==  ruby)) {
      final ext = RubyPlugin.isWindows() ? 'zip' : 'tar.gz'
      return "org.jruby:jruby-dist:${rubyVer}:bin@${ext}"
    }
    rubyDistrDependency
  } 
  
  def getExtractPath() {
    if (null == extractPath || 0 == extractPath.length()) {
      return project.projectDir.getAbsolutePath()        
    }
    new File(project.projectDir, extractPath).getAbsolutePath()
  }  

  def getRubyHome() {
    getExtractPath() + '/' + ruby + '-' + rubyVer
  }  
}
