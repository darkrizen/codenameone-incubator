<?php
/**
 * A script to add files and build phases to an Xcode project.  This is specifically
 * geared to be run on the Xcode project after xmlvm has created it.  This will
 * add a build phase to copy resources from the ../resources directory
 * into the application bundle preserving directory structure.
 */
define('PLUTIL', '/usr/bin/plutil');
define('PL_PATH', '/usr/bin/pl' );
class XCodeProject {
    
    /**
     * A temporary file path for a copy of the project file.
     * @var String 
     */
    private $tempPath = null;
    
    /**
     * The path to the project file. 
     * @var String 
     */
    private $sourcePath = null;
    
    /**
     * The XML DOM for the project file.
     * @var DOMDocument
     */
    private $dom = null;
    
    
    public function __construct(){
        
    }
    
    /**
     * Loads the Xcode project from the specified path.
     * @param type $path The path to the xcode project file.
     * @return type DOMDocument
     */
    public function load($path){
        $this->sourcePath = $path;
        $contents = file_get_contents($path);
        $this->tempPath = tempnam(dirname($path), 'temp_');
        file_put_contents($this->tempPath, $contents);
        exec(escapeshellcmd(PLUTIL)." -convert xml1 ".escapeshellarg($this->tempPath));
        $xml = file_get_contents($this->tempPath);
        $this->dom = new DOMDocument();
        $this->dom->loadXML($xml);

        return $dom;

    }
    
    
    /**
     * Adds a <key>, <dict> tag-pair with the given values to the parent tag.
     * @param DOMElement $parent The parent tag.
     * @param type $values The values to set as the dict's values.
     * @param type $key Optional key.  One will be generated if it isn't provided.
     * @return type
     */
    protected function addDict(DOMElement $parent, $values = array(), $key = null){
        if ( !isset($key) ){
            $xpath = new DOMXPath($this->dom);
            $keyEls = $xpath->query("//key");
            $keys = array();
            foreach ( $keyEls as $el){
                $keys[$el->nodeValue] = $el->nodeValue;
            }

            $key = substr(md5(__FILE__.time().''), 0, 24);
            while ( isset($keys[$key] ) ){
                $key = substr(md5(__FILE__.time().''), 0, 24);

            }
        }
        
        $keyEl = $parent->ownerDocument->createElement('key');
        $keyEl->nodeValue = $key;
        $parent->appendChild($keyEl);
        
        
        $dictEl = $parent->ownerDocument->createElement('dict');
        foreach ( $values as $k=>$v){
            $kel = $parent->ownerDocument->createElement('key');
            $kel->nodeValue = $k;
            $dictEl->appendChild($kel);
            
            if ( is_array($v) ){
                $arrEl = $parent->ownerDocument->createElement('array');
                foreach ( $v as $strval ){
                    $strEl = $parent->ownerDocument->createElement('string');
                    $strEl->nodeValue = $strval;
                    
                    $arrEl->appendChild($strEl);
                }
                $dictEl->appendChild($arrEl);
            } else {
                $strEl = $parent->ownerDocument->createElement('string');
                $strEl->nodeValue = $v;
                $dictEl->appendChild($strEl);
            }
        }
        
        $parent->appendChild($dictEl);
        
        
        return array(
            'key' => $keyEl,
            'dict' => $dictEl
        );
        
    }
    
    /**
     * Finds the "objects" dictionary for the project.  This is basically the root
     * element to which all children are added.
     * @return type
     */
    private function getObjectsDict(){
        $xpath = new DOMXPath($this->dom);
        $keys = $xpath->query('//key');
        $objectsDict = null;
        foreach ( $keys as $key){
            if ( $key->nodeValue == 'objects' ){
                
                $objectsDict = $key->nextSibling;
                while ( $objectsDict !== null and !($objectsDict instanceof DOMElement)){
                    $objectsDict = $objectsDict->nextSibling;
                }
                break;
            }
        }
        return $objectsDict;
    }
    
    /**
     * Adds a shell script build phase with the specified keys.
     * @param type $values
     * @return type
     * @throws Exception
     */
    public function addShellScriptBuildPhase($values = array()){
        $objectsDict = $this->getObjectsDict();
        
        if ( !isset($objectsDict) ){
            throw new Exception("Could not find objects dict");
        }
        
        $defaults = array(
            'buildActionMask' => '2147483647',
            'files' => array(),
            'inputPaths' => array(),
            'isa' => 'PBXShellScriptBuildPhase',
            'outputPaths' => array(),
            'runOnlyForDeploymentPostprocessing' => 0,
            'shellPath' => '/bin/sh',
            'shellScript' => ''
        );
        
        $values = array_merge($defaults, $values);
        $buildPhase =  $this->addDict($objectsDict, $values);
        
        // Now we need to add it to our target
        $xpath = new DOMXPath($this->dom);
        $keys = $xpath->query('//key');
        $arr = null;
        foreach ( $keys as $key ){
            if ( $key->nodeValue == 'buildPhases'){
                $arr = $key->nextSibling;
                while ( $arr != null and (!$arr instanceof DOMElement) ){
                    $arr = $arr->nextSibling;
                }
                break;
            }
        }
        
        if ( $arr === null ){
            throw new Exception("No targets with build phases found");
        }
        
        $strEl = $this->dom->createElement('string');
        $strEl->nodeValue = $buildPhase['key']->nodeValue;
        $arr->appendChild($strEl);
        
        return $buildPhase;
        
           
    }
    
    /**
     * Adds a file reference to the project with the specified keys.
     * @param type $values
     * @return type
     * @throws Exception
     */
    public function addFileReference($values = array()){
        $objectsDict = $this->getObjectsDict();
        if ( !isset($objectsDict) ){
            throw new Exception("Could not find objects dict");
        }
        $type = '?';
        if ( isset($values['path']) ){
            $type = $this->getType($values['path']);
        } else if ( isset($values['name']) ){
            $type = $this->getType($values['name']);
        }
        
        $defaults = array(
            'isa' => 'PBXFileReference',
            'lastKnownFileType' => $type,
            'name' => '',
            'path' => '',
            'sourceTree' => '<absolute>'
        );
        
        $values = array_merge($defaults, $values);
        return $this->addDict($objectsDict, $values);
        
    }
    
    /**
     * Guesses a file type and returns the string value that should appear 
     * in the lastKnownType tag for a file reference.
     * 
     * May need to add more types here.
     * @param type $file
     * @return string
     */
    public function getType($file){
        $ext = pathinfo($file, PATHINFO_EXTENSION);
        switch ($ext){
            case 'dylib':
                return 'compiled.mach-o.dylib';
            case 'strings':
                return 'Localizable.strings';
            case 'framework':
                return 'wrapper.framework';
            default:
                return '?';
        }
    }
    
    /**
     * Saves the project.
     */
    function save(){
        echo "\nSaving ".$this->tempPath;
        $this->dom->save($this->tempPath);
        exec(escapeshellcmd(PL_PATH).' -input '.escapeshellarg($this->tempPath));
        $contents = file_get_contents($this->tempPath);
        file_put_contents($this->sourcePath, $contents);
    }
    
}

/**
 * A build script that prepares the XMLVM xcode project.  Adds the
 * build phase to import resources into the app bundle, and also
 * adds file references for the specified files.
 * @param type $path
 * @param type $filesToAdd
 */
function prepareXcodeProject($path, $filesToAdd = array()){
    $proj = new XCodeProject();
    $proj->load($path);
    $proj->addShellScriptBuildPhase(array(
        'shellPath' => '/bin/sh',
        'shellScript' => 'cp -r "${PROJECT_DIR}/../resources/" "${BUILT_PRODUCTS_DIR}"'
    ));
    
    foreach ( $filesToAdd as $file ){
        $proj->addFileReference(array(
            'name' => basename($file),
            'path' => $file
        ));
    }
    
    
    $proj->save();
}

$projectPath = $argv[1];
$files = array();
if ( count($argv) > 2 ){
    $files = array_slice($argv, 2);
}
echo "Project path is ".$projectPath;
prepareXcodeProject($projectPath, $files);
