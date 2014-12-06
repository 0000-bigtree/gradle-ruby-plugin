package bigtree

import org.gradle.api.Project

class RubyProjectExtension {
  
  String nameWithPath
    
  boolean isRailsProject = false
  
  boolean isCreateGemfile = true
  
  String railsVer = ''
  
  String railsNewArgs = ''
    
  String gemfileSource
  
  String defaultGems = ''
  
  //
  Project project  
  
  def getDefaultGems() {
    isRailsProject ? "${defaultGems} rails:${railsVer}" : defaultGems
  }
  
  def getNameWithPath() {
    if (null == nameWithPath || 0 >= nameWithPath.length()) {
      if (isRailsProject) {
        return 'src/main/ruby/railsProject'
      } else {
        return 'src/main/ruby/rubyProject'
      }
    }
    nameWithPath
  }  
  
  def isCreateGemfile() {
    if (isRailsProject) {
      false
    } else {
      isCreateGemfile
    }
  }
  
}
