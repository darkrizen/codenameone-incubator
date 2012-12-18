from mod_pbxproj import XcodeProject
import sys

projectPath = sys.argv[1]
print "Loading project from path : ", projectPath, "\n"

project = XcodeProject.Load(projectPath)
for file in sys.argv[2:]:
	print "Adding file ", file, "\n"
	project.add_file(file, None, None)
	
if project.modified:
	print "Saving project\n"
	project.backup()
	project.save()