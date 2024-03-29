#  Copyright 2012 Calvin Rien
#
#     Licensed under the Apache License, Version 2.0 (the "License");
#     you may not use this file except in compliance with the License.
#     You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#     Unless required by applicable law or agreed to in writing, software
#     distributed under the License is distributed on an "AS IS" BASIS,
#     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#     See the License for the specific language governing permissions and
#     limitations under the License.

#  A pbxproj file is an OpenStep format plist
#  {} represents dictionary of key=value pairs delimited by ;
#  () represents list of values delimited by ,
#  file starts with a comment specifying the character type
#  // !$*UTF8*$!

#  when adding a file to a project, create the PBXFileReference
#  add the PBXFileReference's guid to a group
#  create a PBXBuildFile with the PBXFileReference's guid
#  add the PBXBuildFile to the appropriate build phase

#  when adding a header search path add
#  HEADER_SEARCH_PATHS = "path/**";
#  to each XCBuildConfiguration object

#  Xcode4 will read either a OpenStep or XML plist.
#  this script uses `plutil` to validate, read and write
#  the pbxproj file.  Plutil is available in OS X 10.2 and higher
#  Plutil can't write OpenStep plists, so I save as XML

import re, uuid, sys, os, shutil, subprocess, datetime, json

from UserDict import IterableUserDict
from UserList import UserList

class PBXEncoder(json.JSONEncoder):

    def default(self, obj):
        """Tests the input object, obj, to encode as JSON."""

        if isinstance(obj, (PBXList, PBXDict)):
            return obj.data

        return json.JSONEncoder.default(self, obj)


class PBXDict(IterableUserDict):
    def __init__(self, d=None):
        if d:
            d = dict([(PBXType.Convert(k),PBXType.Convert(v)) for k,v in d.items()])

        IterableUserDict.__init__(self, d)

    def __setitem__(self, key, value):
        IterableUserDict.__setitem__(self, PBXType.Convert(key), PBXType.Convert(value))

    def remove(self, key):
        self.data.pop(PBXType.Convert(key), None)



class PBXList(UserList):
    def __init__(self, l=None):
        if isinstance(l, basestring):
            UserList.__init__(self)
            self.add(l)
            return
        elif l:
            l = [PBXType.Convert(v) for v in l]

        UserList.__init__(self, l)

    def add(self, value):
        value = PBXType.Convert(value)

        if value in self.data:
            return False

        self.data.append(value)
        return True

    def remove(self, value):
        value = PBXType.Convert(value)

        if value in self.data:
            self.data.remove(value)

    def __setitem__(self, key, value):
        UserList.__setitem__(self, PBXType.Convert(key), PBXType.Convert(value))


class PBXType(PBXDict):
    def __init__(self, d=None):
        PBXDict.__init__(self, d)

        if not self.has_key('isa'):
            self['isa'] = self.__class__.__name__
        self.id = None

    @staticmethod
    def Convert(o):
        if isinstance(o, list):
            return PBXList(o)
        elif isinstance(o, dict):
            isa = o.get('isa')

            if not isa:
                return PBXDict(o)

            cls = globals().get(isa)

            if cls and issubclass(cls, PBXType):
                return cls(o)

            print 'warning: unknown PBX type: %s' % isa
            return PBXDict(o)
        else:
            return o

    @staticmethod
    def IsGuid(o):
        return re.match('^[A-F0-9]{24}$', str(o))

    @classmethod
    def GenerateId(cls):
        return ''.join(str(uuid.uuid4()).upper().split('-')[1:])

    @classmethod
    def Create(cls, *args, **kwargs):
        return cls(*args, **kwargs)


class PBXFileReference(PBXType):
    def __init__(self, d=None):
        PBXType.__init__(self, d)
        self.build_phase = None

    types = {
        '.a':('archive.ar', 'PBXFrameworksBuildPhase'),
        '.app': ('wrapper.application', None),
        '.s': ('sourcecode.asm', 'PBXSourcesBuildPhase'),
        '.c': ('sourcecode.c.c', 'PBXSourcesBuildPhase'),
        '.cpp': ('sourcecode.cpp.cpp', 'PBXSourcesBuildPhase'),
        '.framework': ('wrapper.framework','PBXFrameworksBuildPhase'),
        '.h': ('sourcecode.c.h', None),
        '.icns': ('image.icns','PBXResourcesBuildPhase'),
        '.m': ('sourcecode.c.objc', 'PBXSourcesBuildPhase'),
        '.mm': ('sourcecode.cpp.objcpp', 'PBXSourcesBuildPhase'),
        '.nib': ('wrapper.nib', 'PBXResourcesBuildPhase'),
        '.plist': ('text.plist.xml', 'PBXResourcesBuildPhase'),
        '.png': ('image.png', 'PBXResourcesBuildPhase'),
        '.gif': ('image.gif', 'PBXResourcesBuildPhase'),
        '.jpg': ('image.jpeg', 'PBXResourcesBuildPhase'),
        '.jpeg': ('image.png', 'PBXResourcesBuildPhase'),
        '.html': ('text.html', 'PBXResourcesBuildPhase'),
        '.res' : ('file.res', 'PBXResourcesBuildPhase'),
        '.css' : ('text.css', 'PBXResourcesBuildPhase'),
        '.js' : ('text.javascript', 'PBXResourcesBuildPhase'),
        '.rtf': ('text.rtf', 'PBXResourcesBuildPhase'),
        '.tiff': ('image.tiff', 'PBXResourcesBuildPhase'),
        '.txt': ('text', 'PBXResourcesBuildPhase'),
        '.xcodeproj': ('wrapper.pb-project', None),
        '.xib': ('file.xib', 'PBXResourcesBuildPhase'),
        '.strings': ('text.plist.strings', 'PBXResourcesBuildPhase'),
        '.bundle': ('wrapper.plug-in', 'PBXResourcesBuildPhase'),
        '.dylib': ('compiled.mach-o.dylib', 'PBXFrameworksBuildPhase')
    }

    trees = [
        '<absolute>',
        '<group>',
        'BUILT_PRODUCTS_DIR',
        'DEVELOPER_DIR',
        'SDKROOT',
        'SOURCE_ROOT',
    ]

    def guess_file_type(self):
        self.remove('explicitFileType')
        self.remove('lastKnownFileType')
        ext = os.path.splitext(self.get('name', ''))[1]

        f_type, build_phase = PBXFileReference.types.get(ext, ('?', None))

        self['lastKnownFileType'] = f_type
        self.build_phase = build_phase

        if f_type == '?':
            print 'unknown file extension: %s' % ext
            print 'please add extension and Xcode type to PBXFileReference.types'

        return f_type

    def set_file_type(self, ft):
        self.remove('explicitFileType')
        self.remove('lastKnownFileType')

        self['explicitFileType'] = ft

    @classmethod
    def Create(cls, os_path, tree='SOURCE_ROOT'):
        if tree not in cls.trees:
            print 'Not a valid sourceTree type: %s' % tree
            return None

        fr = cls()
        fr.id = cls.GenerateId()
        fr['path'] = os_path
        fr['name'] = os.path.split(os_path)[1]
        fr['sourceTree'] = '<absolute>' if os.path.isabs(os_path) else tree
        fr.guess_file_type()

        return fr

class PBXBuildFile(PBXType):
    def set_weak_link(self, weak=False):
        k_settings = 'settings'
        k_attributes = 'ATTRIBUTES'

        s = self.get(k_settings)

        if not s:
            if weak:
                self[k_settings] = PBXDict({k_attributes:PBXList(['Weak'])})

            return True

        atr = s.get(k_attributes)

        if not atr:
            if weak:
                atr = PBXList()
            else:
                return False

        if weak:
            atr.add('Weak')
        else:
            atr.remove('Weak')

        self[k_settings][k_attributes] = atr

        return True

    def add_compiler_flag(self, flag):
        k_settings = 'settings'
        k_attributes = 'COMPILER_FLAGS'

        if not self.has_key(k_settings):
            self[k_settings] = PBXDict()

        if not self[k_settings].has_key(k_attributes):
            self[k_settings][k_attributes] = flag
            return True

        flags = self[k_settings][k_attributes].split(' ')

        if flag in flags:
            return False

        flags.append(flag)

        self[k_settings][k_attributes] = ' '.join(flags)

    @classmethod
    def Create(cls, file_ref, weak=False):
        if isinstance(file_ref, PBXFileReference):
            file_ref = file_ref.id

        bf = cls()
        bf.id = cls.GenerateId()
        bf['fileRef'] = file_ref

        if weak:
            bf.set_weak_link(True)

        return bf

class PBXGroup(PBXType):
    def add_child(self, ref):
        if not isinstance(ref, PBXDict):
            return None

        isa = ref.get('isa')

        if isa != 'PBXFileReference' and isa != 'PBXGroup':
            return None

        if not self.has_key('children'):
            self['children'] = PBXList()

        self['children'].add(ref.id)

        return ref.id

    def remove_child(self, id):
        if not self.has_key('children'):
            self['children'] = PBXList()
            return

        if not PBXType.IsGuid(id):
            id = id.id

        self['children'].remove(id)

    def has_child(self, id):
        if not self.has_key('children'):
            self['children'] = PBXList()
            return False

        if not PBXType.IsGuid(id):
            id = id.id

        return id in self['children']

    def get_name(self):
        path_name = os.path.split(self.get('path',''))[1]
        return self.get('name', path_name)

    @classmethod
    def Create(cls, name, path=None, tree='SOURCE_ROOT'):
        grp = cls()
        grp.id = cls.GenerateId()
        grp['name'] = name
        grp['children'] = PBXList()

        if path:
            grp['path'] = path
            grp['sourceTree'] = tree
        else:
            grp['sourceTree'] = '<group>'

        return grp


class PBXNativeTarget(PBXType):
    pass


class PBXProject(PBXType):
    pass


class PBXContainerItemProxy(PBXType):
    pass


class PBXReferenceProxy(PBXType):
    pass


class PBXVariantGroup(PBXType):
    pass


class PBXBuildPhase(PBXType):
    def add_build_file(self, bf):
        if bf.get('isa') != 'PBXBuildFile':
            return False

        if not self.has_key('files'):
            self['files'] = PBXList()

        self['files'].add(bf.id)

        return True

    def remove_build_file(self, id):
        if not self.has_key('files'):
            self['files'] = PBXList()
            return

        self['files'].remove(id)

    def has_build_file(self, id):
        if not self.has_key('files'):
            self['files'] = PBXList()
            return False

        if not PBXType.IsGuid(id):
            id = id.id

        return id in self['files']


class PBXFrameworksBuildPhase(PBXBuildPhase):
    pass


class PBXResourcesBuildPhase(PBXBuildPhase):
    pass


class PBXShellScriptBuildPhase(PBXBuildPhase):
    pass


class PBXSourcesBuildPhase(PBXBuildPhase):
    pass


class PBXCopyFilesBuildPhase(PBXBuildPhase):
    pass


class XCBuildConfiguration(PBXType):
    def add_search_paths(self, paths, base, key, recursive=True):
        modified = False

        if not isinstance(paths, list):
            paths = [paths]

        if not self.has_key(base):
            self[base] = PBXDict()

        for path in paths:
            if recursive and not path.endswith('/**'):
                path = os.path.join(path, '**')

            if not self[base].has_key(key):
                self[base][key] = PBXList()
            elif isinstance(self[base][key], basestring):
                self[base][key] = PBXList(self[base][key])

            if self[base][key].add('\\"%s\\"' % path):
                modified = True

        return modified

    def add_header_search_paths(self, paths, recursive=True):
        return self.add_search_paths(paths, 'buildSettings', 'HEADER_SEARCH_PATHS', recursive=recursive)

    def add_library_search_paths(self, paths, recursive=True):
        return self.add_search_paths(paths, 'buildSettings', 'LIBRARY_SEARCH_PATHS', recursive=recursive)

    def add_other_cflags(self, flags):
        modified = False

        base = 'buildSettings'
        key = 'OTHER_CFLAGS'

        if isinstance(flags, basestring):
            flags = PBXList(flags)

        if not self.has_key(base):
            self[base] = PBXDict()

        for flag in flags:

            if not self[base].has_key(key):
                self[base][key] = PBXList()
            elif isinstance(self[base][key], basestring):
                self[base][key] = PBXList(self[base][key])

            if self[base][key].add(flag):
                self[base][key] = [e for e in self[base][key] if e]
                modified = True

        return modified


class XCConfigurationList(PBXType):
    pass


class XcodeProject(PBXDict):
    plutil_path = 'plutil'
    special_folders = ['.bundle', '.framework', '.xcodeproj']

    def __init__(self, d=None, path=None):
        if not path:
            path = os.path.join(os.getcwd(), 'project.pbxproj')

        self.pbxproj_path =os.path.abspath(path)
        self.source_root = os.path.abspath(os.path.join(os.path.split(path)[0], '..'))

        IterableUserDict.__init__(self, d)

        self.data = PBXDict(self.data)
        self.objects = self.get('objects')
        self.modified = False

        root_id = self.get('rootObject')
        if root_id:
            self.root_object = self.objects[root_id]
            root_group_id = self.root_object.get('mainGroup')
            self.root_group = self.objects[root_group_id]
        else:
            print "error: project has no root object"
            self.root_object = None
            self.root_group = None

        for k,v in self.objects.iteritems():
            v.id = k

    def add_other_cflags(self, flags):
        build_configs = [b for b in self.objects.values() if b.get('isa') == 'XCBuildConfiguration']

        for b in build_configs:
            if b.add_other_cflags(flags):
                self.modified = True

    def add_header_search_paths(self, paths, recursive=True):
        build_configs = [b for b in self.objects.values() if b.get('isa') == 'XCBuildConfiguration']

        for b in build_configs:
            if b.add_header_search_paths(paths, recursive):
                self.modified = True

    def add_library_search_paths(self, paths, recursive=True):
        build_configs = [b for b in self.objects.values() if b.get('isa') == 'XCBuildConfiguration']

        for b in build_configs:
            if b.add_library_search_paths(paths, recursive):
                self.modified = True

        # TODO: need to return value if project has been modified

    def get_obj(self, id):
        return self.objects.get(id)

    def get_files_by_os_path(self, os_path, tree='SOURCE_ROOT'):
        files = [f for f in self.objects.values() if f.get('isa') == 'PBXFileReference'
                                            and f.get('path') == os_path
                                            and f.get('sourceTree') == tree]

        return files

    def get_files_by_name(self, name, parent=None):
        if parent:
            files = [f for f in self.objects.values() if f.get('isa') == 'PBXFileReference'
                                            and f.get(name) == name
                                            and parent.has_child(f)]
        else:
            files = [f for f in self.objects.values() if f.get('isa') == 'PBXFileReference'
                                            and f.get(name) == name]

        return files

    def get_build_files(self, id):
        files = [f for f in self.objects.values() if f.get('isa') == 'PBXBuildFile'
                                            and f.get('fileRef') == id]

        return files

    def get_groups_by_name(self, name, parent=None):
        if parent:
            groups = [g for g in self.objects.values() if g.get('isa') == 'PBXGroup'
                    and g.get_name() == name
                    and parent.has_child(g)]
        else:
            groups = [g for g in self.objects.values() if g.get('isa') == 'PBXGroup'
                    and g.get_name() == name]

        return groups

    def get_or_create_group(self, name, path=None, parent=None):
        if not name:
            return None

        if not parent:
            parent = self.root_group
        elif not isinstance(parent, PBXGroup):
            # assume it's an id
            parent = self.objects.get(parent, self.root_group)

        groups = self.get_groups_by_name(name)

        for grp in groups:
            if parent.has_child(grp.id):
                return grp

        grp = PBXGroup.Create(name, path)
        parent.add_child(grp)

        self.objects[grp.id] = grp

        self.modified = True

        return grp

    def get_groups_by_os_path(self, path):
        path = os.path.abspath(path)

        groups = [g for g in self.objects.values() if g.get('isa') == 'PBXGroup'
                    and os.path.abspath(g.get('path','/dev/null')) == path]

        return groups

    def get_build_phases(self, phase_name):
        phases = [p for p in self.objects.values() if p.get('isa') == phase_name]

        return phases

    def get_relative_path(self, os_path):
        return os.path.relpath(os_path, self.source_root)

    def verify_files(self, file_list, parent=None):
        # returns list of files not in the current project.
        if not file_list:
            return []

        if parent:
            exists_list = [f.get('name') for f in self.objects.values() if f.get('isa') == 'PBXFileReference' and f.get('name') in file_list and parent.has_child(f)]
        else:
            exists_list = [f.get('name') for f in self.objects.values() if f.get('isa') == 'PBXFileReference' and f.get('name') in file_list]

        return set(file_list).difference(exists_list)

    def add_folder(self, os_path, parent=None, excludes=None, recursive=True, create_build_files=True):
        if not os.path.isdir(os_path):
            return []

        if not excludes:
            excludes = []

        results = []

        if not parent:
            parent = self.root_group
        elif not isinstance(parent, PBXGroup):
            # assume it's an id
            parent = self.objects.get(parent, self.root_group)

        path_dict = {os.path.split(os_path)[0]:parent}
        special_list = []

        for (grp_path, subdirs, files) in os.walk(os_path):
            parent_folder, folder_name = os.path.split(grp_path)
            parent = path_dict.get(parent_folder, parent)

            if [sp for sp in special_list if parent_folder.startswith(sp)]:
                continue

            if folder_name.startswith('.'):
                special_list.append(grp_path)
                continue

            if os.path.splitext(grp_path)[1] in XcodeProject.special_folders:
                # if this file has a special extension (bundle or framework mainly) treat it as a file
                special_list.append(grp_path)

                new_files = self.verify_files([folder_name], parent=parent)

                if new_files:
                    results.extend(self.add_file(grp_path, parent, create_build_files=create_build_files))

                continue

            # create group
            grp = self.get_or_create_group(folder_name, path=self.get_relative_path(grp_path) , parent=parent)
            path_dict[grp_path] = grp

            results.append(grp)

            file_dict = {}

            for f in files:
                if f[0] == '.' or [m for m in excludes if re.match(m,f)]:
                    continue

                kwds = {
                    'create_build_files': create_build_files,
                    'parent': grp,
                    'name': f
                }

                f_path = os.path.join(grp_path, f)

                file_dict[f_path] = kwds

            new_files = self.verify_files([n.get('name') for n in file_dict.values()], parent=grp)

            add_files = [(k,v) for k,v in file_dict.items() if v.get('name') in new_files]

            for path, kwds in add_files:
                kwds.pop('name', None)

                self.add_file(path, **kwds)

            if not recursive:
                break

        for r in results:
            self.objects[r.id] = r

        return results

    def add_file(self, f_path, parent=None, tree='SOURCE_ROOT', create_build_files=True, weak=False):
        results = []

        abs_path = ''

        if os.path.isabs(f_path):
            abs_path = f_path

            if not os.path.exists(f_path):
                return results
            elif tree == 'SOURCE_ROOT':
                f_path = os.path.relpath(f_path, self.source_root)
            else:
                tree = '<absolute>'

        if not parent:
            parent = self.root_group
        elif not isinstance(parent, PBXGroup):
            # assume it's an id
            parent = self.objects.get(parent, self.root_group)

        file_ref = PBXFileReference.Create(f_path, tree)
        parent.add_child(file_ref)
        results.append(file_ref)
        # create a build file for the file ref
        if file_ref.build_phase and create_build_files:
            phases = self.get_build_phases(file_ref.build_phase)

            for phase in phases:
                build_file = PBXBuildFile.Create(file_ref, weak=weak)

                phase.add_build_file(build_file)
                results.append(build_file)

            if abs_path and tree == 'SOURCE_ROOT' and os.path.isfile(abs_path)\
                and file_ref.build_phase == 'PBXFrameworksBuildPhase':

                library_path = os.path.join('$(SRCROOT)', os.path.split(f_path)[0])

                self.add_library_search_paths([library_path], recursive=False)

        for r in results:
            self.objects[r.id] = r

        if results:
            self.modified = True

        return results

    def remove_group(self, grp):
        pass

    def remove_file(self, id):
        pass

    def move_file(self, id, dest_grp=None):
        pass

    def apply_patch(self, patch_path, xcode_path):
        if not os.path.isfile(patch_path) or not os.path.isdir(xcode_path):
            print 'ERROR: couldn\'t apply "%s" to "%s"' % (patch_path, xcode_path)
            return

        print 'applying "%s" to "%s"' % (patch_path, xcode_path)

        return subprocess.call(['patch', '-p1', '--forward', '--directory=%s'%xcode_path, '--input=%s'%patch_path])

    def apply_mods(self, mod_dict, default_path=None):
        if not default_path:
            default_path = os.getcwd()

        keys = mod_dict.keys()

        for k in keys:
            v = mod_dict.pop(k)

            mod_dict[k.lower()] = v

        parent = mod_dict.pop('group', None)

        if parent:
            parent = self.get_or_create_group(parent)

        excludes = mod_dict.pop('excludes', [])

        if excludes:
            excludes = [re.compile(e) for e in excludes]

        compiler_flags = mod_dict.pop('compiler_flags', {})

        for k,v in mod_dict.items():
            if k == 'patches':
                for p in v:
                    if not os.path.isabs(p):
                        p = os.path.join(default_path, p)

                    self.apply_patch(p, self.source_root)
            elif k == 'folders':
                # get and compile excludes list
                # do each folder individually
                for folder in v:
                    kwds = {}

                    # if path contains ':' remove it and set recursive to False
                    if ':' in folder:
                        args = folder.split(':')
                        kwds['recursive'] = False
                        folder = args.pop(0)

                    if os.path.isabs(folder) and os.path.isdir(folder):
                        pass
                    else:
                        folder = os.path.join(default_path, folder)
                        if not os.path.isdir(folder):
                            continue

                    if parent:
                        kwds['parent'] = parent

                    if excludes:
                        kwds['excludes'] = excludes

                    self.add_folder(folder, **kwds)
            elif k == 'headerpaths' or k == 'librarypaths':
                paths = []

                for p in v:
                    if p.endswith('/**'):
                        p = os.path.split(p)[0]

                    if not os.path.isabs(p):
                        p = os.path.join(default_path, p)

                    if not os.path.exists(p):
                        continue

                    p = self.get_relative_path(p)

                    paths.append(os.path.join('$(SRCROOT)', p, "**"))

                if k == 'headerpaths':
                    self.add_header_search_paths(paths)
                else:
                    self.add_library_search_paths(paths)
            elif k == 'other_cflags':
                self.add_other_cflags(v)
            elif k == 'libs' or k == 'frameworks' or k == 'files':
                paths = {}

                for p in v:
                    kwds = {}

                    if ':' in p:
                        args = p.split(':')
                        p = args.pop(0)

                        if 'weak' in args:
                            kwds['weak'] = True

                    file_path = os.path.join(default_path, p)
                    search_path, file_name = os.path.split(file_path)

                    if [m for m in excludes if re.match(m,file_name)]:
                        continue

                    try:
                        expr = re.compile(file_name)
                    except re.error:
                        expr = None

                    if expr and os.path.isdir(search_path):
                        file_list = os.listdir(search_path)

                        for f in file_list:
                            if [m for m in excludes if re.match(m,f)]:
                                continue

                            if re.search(expr,f):
                                kwds['name'] = f
                                paths[os.path.join(search_path, f)] = kwds
                                p = None

                    if k == 'libs':
                        kwds['parent'] = self.get_or_create_group('Libraries', parent=parent)
                    elif k == 'frameworks':
                        kwds['parent'] = self.get_or_create_group('Frameworks', parent=parent)

                    if p:
                        kwds['name'] = file_name

                        if k == 'libs':
                            p = os.path.join('usr','lib',p)
                            kwds['tree'] = 'SDKROOT'
                        elif k == 'frameworks':
                            p = os.path.join('System','Library','Frameworks',p)
                            kwds['tree'] = 'SDKROOT'
                        elif k == 'files' and not os.path.exists(file_path):
                            # don't add non-existent files to the project.
                            continue

                        paths[p] = kwds

                new_files = self.verify_files([n.get('name') for n in paths.values()])

                add_files = [(k,v) for k,v in paths.items() if v.get('name') in new_files]

                for path, kwds in add_files:
                    kwds.pop('name', None)

                    if not kwds.has_key('parent') and parent:
                        kwds['parent'] = parent

                    self.add_file(path, **kwds)

        if compiler_flags:
            for k,v in compiler_flags.items():
                filerefs = []

                for f in v:
                    filerefs.extend([fr.id for fr in self.objects.values() if fr.get('isa') == 'PBXFileReference'
                                            and fr.get('name') == f])


                buildfiles = [bf for bf in self.objects.values() if bf.get('isa') == 'PBXBuildFile'
                                        and bf.get('fileRef') in filerefs]

                for bf in buildfiles:
                    if bf.add_compiler_flag(k):
                        self.modified = True


    def backup(self, file_name=None):
        if not file_name:
            file_name = self.pbxproj_path

        backup_name = "%s.%s.backup" % (file_name, datetime.datetime.now().strftime('%d%m%y-%H%M%S'))

        shutil.copy2(file_name, backup_name)

    def save(self, file_name=None):
        if not file_name:
            file_name = self.pbxproj_path

        # JSON serialize the project and convert that json to an xml plist
        p = subprocess.Popen([XcodeProject.plutil_path, '-convert', 'xml1', '-o', file_name, '-'], stdin=subprocess.PIPE)
        p.communicate(PBXEncoder().encode(self.data))

    @classmethod
    def Load(cls, path):
        cls.plutil_path = os.path.join(os.path.split(__file__)[0], 'plutil')

        if not os.path.isfile(XcodeProject.plutil_path):
            cls.plutil_path = 'plutil'

        if subprocess.call([XcodeProject.plutil_path,'-lint','-s',path]):
            print 'ERROR: not a valid .pbxproj file'
            return None

        # load project by converting to JSON and parse
        p = subprocess.Popen([XcodeProject.plutil_path, '-convert', 'json', '-o', '-', path], stdout=subprocess.PIPE)
        tree = json.loads(p.communicate()[0])

        return XcodeProject(tree, path)


def test(argv=None):
    if not argv:
        argv = sys.argv

    proj = XcodeProject.Load('../../Build/Unity-iPhone.xcodeproj/project.pbxproj')

    proj.add_folder('../Assets/Editor/Airship/UI/Default/StoreFront')

    proj.add_file('../Assets/Editor/Airship/libUAirship-1.1.4.a')
    proj.add_file('../Assets/Plugins/Airship/AirshipConfig.plist')

    proj.backup()
    proj.save()

    print str(proj)

if __name__ == '__main__':
    sys.exit(test())



