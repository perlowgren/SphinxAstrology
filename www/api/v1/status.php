<?php

require_once __DIR__.'/.init.php';

global $_USER,$_TEST;

if(METHOD==OPTIONS) {
    header('Allow: GET, HEAD');
    status(204);
} elseif(METHOD!=GET && METHOD!=HEAD) {
    error(405);
}

input($args,$query);

$n = authorize();

$uid = $_USER['id'];
$key = get_key(intval($_USER['key']));
$stats = '';

if(isset($_GET['timestamp'])) {
    $time = intval($_GET['timestamp']);
    $profiles = intval(db_column("SELECT count(*) FROM `SphinxProfile` WHERE `userId`='{$uid}' AND `updated`>FROM_UNIXTIME({$time})"));
    $texts = intval(db_column("SELECT count(*) FROM `SphinxText` WHERE `userId`='{$uid}' AND `updated`>FROM_UNIXTIME({$time})"));
    $stats = <<<JSON
,
  "profiles": {$profiles},
  "texts": {$texts}
JSON;
}

if($_TEST) $message = 'Test';
else $message = "Verified [{$n}]";

$email = json_encode($_USER['email'],JSON_ENCODE_FLAGS);
$user = json_encode($_USER['user'],JSON_ENCODE_FLAGS);
$name = json_encode($_USER['name'],JSON_ENCODE_FLAGS);

$out = <<<JSON
{
  "status": "OK",
  "message": "{$message}",
  "key": "{$key}",
  "expires": {$_USER['expires']},
  "email": {$email},
  "user": {$user},
  "name": {$name},
  "language": "{$_USER['language']}"{$stats}
}

JSON;
output($out);

