package bigtree

import org.gradle.api.Project

class RubyEnvExtension {
  final String ruby = 'jruby' // ruby interpreter, options is jruby and ruby(MRI), current only 'jruby'
  String rubyVer = '1.7.16.1'  
  String rubyHome
  String rubyDistrDependency
  //
  Project project

  def getRubyHome() {
    project?.projectDir.getAbsolutePath() + '/' + ruby + '-' + rubyVer
  }
  
  def getRubyDistrDependency() {
    if((null == rubyDistrDependency || 0 ==  rubyDistrDependency.length())
    && ('jruby' ==  ruby)) {
      return "org.jruby:jruby-dist:${rubyVer}@zip"
    }
    rubyDistrDependency
  }  
}
