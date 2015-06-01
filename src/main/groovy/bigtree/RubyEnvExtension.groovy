package bigtree

import org.gradle.api.Project

class RubyEnvExtension {
  // ruby interpreter, options is jruby and ruby(MRI), current only 'jruby'
  final String engine = 'jruby' 
  
  String engineVer = '1.9.20'
  
  String rubyVer = '1.9.3'
  
  String rubyDistrDependency
  
  String extractPath = 'rubybin'
  
  final String officialGemSource = 'https://rubygems.org/'
  
  String defaultGemSource = 'http://ruby.taobao.org/'
  
  String defaultGems //= 'rubygems-update rake bundler'
  
  //
  Project project

  def getRubyDistrDependency() {
    if((null == rubyDistrDependency || 0 ==  rubyDistrDependency.length())
    && ('jruby' ==  engine)) {
      final ext = RubyPlugin.isWindows() ? 'zip' : 'tar.gz'
      return "org.jruby:jruby-dist:${engineVer}:bin@${ext}"
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
    getExtractPath() + '/' + engine + '-' + engineVer
  }  
}
