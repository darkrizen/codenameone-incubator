<?php
ini_set("display_errors", "on");
echo "Configuring main stub....\n";
$projectPath = $argv[1];
$mainStubPath = $argv[2];
$nativePath = $projectPath.DIRECTORY_SEPARATOR.'native';
$iosPath = $nativePath.DIRECTORY_SEPARATOR.'ios';

$files = glob($iosPath.DIRECTORY_SEPARATOR.'*.h');
print_r($files);
$insert = array();
foreach ($files as $file){
	$file = basename($file);
	$interface = str_replace('_', '.', $file);
	$stub = preg_replace('/Impl\.h$/', '', $interface).'Stub.class';
	$interface = preg_replace('/Impl\.h$/', '', $interface).'.class';
	
	$insert[] = "com.codename1.system.NativeLookup.register($interface, $stub);";
	
}
$mainContents = file_get_contents($mainStubPath);
$mainContents = str_replace(
	'#CODENAMEONE_REGISTER_NATIVE_STUBS#', 
	implode("\n", $insert), 
	$mainContents
);
file_put_contents($mainStubPath, $mainContents);
