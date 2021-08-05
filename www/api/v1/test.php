<?php

require_once __DIR__.'/.init.php';

input($args,$query);

$args = implode(', ',$args);

/*foreach($_SERVER as $k=>$v)
	echo "\$_SERVER['{$k}'] : '{$v}'\n";
foreach($_GET as $k=>$v)
	echo "\$_GET['{$k}'] : '{$v}'\n";*/

$get = '';
foreach($_GET as $k => $v) {
    if($get) $get .= ",\n";
    $get .= "\t\t\"{$k}\": \"{$v}\"";
}
if($get) $get = "\n{$get}\n\t";

$hdr = '';
$arr = getallheaders();
foreach($arr as $k => $v) {
    if($hdr) $hdr .= ",\n";
    $hdr .= "\t\t\"{$k}\": \"{$v}\"";
}
if($hdr) $hdr = "\n{$hdr}\n\t";

$out = <<<JSON
{
	"args": "{$args}",
	"query": "{$query}",
	"get": {{$get}},
	"headers": {{$hdr}}
}

JSON;
output($out);

