<?php

require_once __DIR__.'/.config.php';

global $_USER,$_TEST;

header('Content-Type: application/json; charset=utf-8');
header('Accept: application/json');
header('Accept-Charset: utf-8');

ini_set('display_errors',1);
error_reporting(E_ALL);

$auth = false;
$auth_type = false;
if(isset($_SERVER['HTTP_AUTHORIZATION'])) {
    $auth = $_SERVER['HTTP_AUTHORIZATION'];
    if(($p = strpos($auth,' '))>=0) {
        $auth_type = substr($auth,0,$p);
        $auth = substr($auth,$p+1);
    }
}

define('HTTP_HOST',$_SERVER['HTTP_HOST']);
define('HTTP_AUTHORIZATION',$auth);
define('HTTP_AUTHORIZATION_TYPE',$auth_type);

const PROJECT_NAME = 'sphinx';
const PUBLIC_DIR = __DIR__;
const PRIVATE_DIR = PRIVATE_DIRS[HTTP_HOST];
const PRIVATE_DATA_DIR = PRIVATE_DIR.'/sphinx';

const DB_PREFIX = 'Sphinx';

const SESSION_LIFETIME = 3600; // 1 hour
const SESSION_TABLE = DB_PREFIX.'Session';

const SERVER_ID = '5fa5';

const DELETE = 1;
const GET = 2;
const HEAD = 3;
const OPTIONS = 4;
const PATCH = 5;
const POST = 6;
const PUT = 7;

$_methods = array(
    'DELETE'  => DELETE,
    'GET'     => GET,
    'HEAD'    => HEAD,
    'OPTIONS' => OPTIONS,
    'PATCH'   => PATCH,
    'POST'    => POST,
    'PUT'     => PUT,
);
$m = $_SERVER['REQUEST_METHOD'];
if(!isset($_methods[$m])) $m = 0;
else $m = $_methods[$m];

define('METHOD',$m);

const FLAG_PUBLIC = 0x0000;
const FLAG_PRIVATE = 0x0001;
const FLAG_STATIC = 0x0002;
const FLAG_CONFIRMED = 0x0004;
const FLAG_PUBLISHED = 0x0008;

const USER_ADMIN = 0x1000;
const USER_BLOCKED = 0x4000;
const USER_DELETED = 0x8000;

const USERS_LIMIT_MAX = 100;
const PROFILES_LIMIT_MAX = 100;
const TEXTS_LIMIT_MAX = 10;
const VOTES_LIMIT_MAX = 100;

const SELECT_KEY_VALUE = 'SELECT `value` FROM `SphinxKey` WHERE `key`=?';
const SELECT_USER_ID = 'SELECT `id` FROM `SphinxKey` WHERE `key`=? AND `value`=\'user\'';

const KEY_USER = 0;
const KEY_PROFILE = 2;
const KEY_TEXT = 5;

const JSON_ENCODE_FLAGS = JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES;

require_once PRIVATE_DIR.'/mysql.php';
require_once PRIVATE_DIR.'/session.php';

if(isset($_SESSION['user'])) $_USER = &$_SESSION['user'];
else $_USER = array('id'    => 0,'key' => 0,'googleId' => null,'facebookId' => null,'expires' => 0,
                    'email' => null,'user' => null,'name' => null,'picture' => null,'language' => 'en');
$_TEST = false;


/** Handle input from URL, query string, and php://input
 * @param $a [reference] Returns array containing all fields in URL, e.g. /page/[:field1]/[:field2] etc.
 * @param $q [reference] Returns php://input as text or JSON, depending on Content-Type
 * @param $parse [boolean] If true (default), parse query string into $_GET
 */
function input(&$a,&$q,$parse = true) {
    global $_TEST;
    $a = false;
    $q = false;
    if(isset($_SERVER['QUERY_STRING'])) {
        $a = explode('/',trim($_SERVER['QUERY_STRING'],'/ '));
    }
    if(isset($_SERVER['REQUEST_URI']) && ($i = strpos($u = $_SERVER['REQUEST_URI'],'?'))!==false) {
        $qs = substr($u,$i+1);
        $_SERVER['QUERY_STRING'] = $qs;
        parse_str($qs,$_GET);
        if(isset($_GET['test'])) $_TEST = true;
    }
    if(METHOD!=GET && METHOD!=HEAD && METHOD!=OPTIONS) {
        if(isset($_SERVER['CONTENT_LENGTH']) && $_SERVER['CONTENT_LENGTH']>0) {
            $q = file_get_contents('php://input');
            if(isset($_SERVER['CONTENT_TYPE'])) {
                $t = $_SERVER['CONTENT_TYPE'];
                if(strpos($t,'json')!==false)
                    $q = json_decode($q,true);
            }
        }
    }
}

/** Output data and exit */
function output($text = false,$exit = true,$headers = false) {
    if($headers!==false) {
        if(!is_array($headers)) $headers = array($headers);
        foreach($headers as $h) header($h);
    }
    if(METHOD!==HEAD) {
        if($text===false) $text = <<<JSON
{
  "status": "OK"
}

JSON;
        echo $text;
    }
    if($exit===true) exit;
}

/** Send status header and JSON output, and exit */
function status($code = 204,$status = false,$message = false) {
    $messages = array(
        200 => 'OK',
        201 => 'Created',
        202 => 'Accepted',
        203 => 'Non-authoritative Information',
        204 => 'No Content',
        205 => 'Reset Content',
        206 => 'Partial Content',
        207 => 'Multi-Status',
        208 => 'Already Reported',
        226 => 'IM Used',
    );
    $code = intval($code);
    if(!isset($messages[$code])) $code = 204;
    http_response_code($code);
    if($code==204) exit;
    if($status===false) $status = 'OK';
    if($message===false) $message = $messages[$code];
    $out = <<<JSON
{
  "status":  "{$status}",
  "message": "{$message}"
}

JSON;
    echo $out;
    exit;
}

/** Send error header and JSON output, and exit */
function error($code = 500,$status = false,$message = false) {
    $messages = array(
        400 => 'Bad Request',
        401 => 'Unauthorized',
        402 => 'Required',
        403 => 'Forbidden',
        404 => 'Not Found',
        405 => 'Method Not Allowed',
        406 => 'Not Acceptable',
        407 => 'Proxy Authentication Required',
        408 => 'Request Timeout',
        409 => 'Conflict',
        410 => 'Gone',
        411 => 'Length Required',
        412 => 'Precondition Failed',
        413 => 'Payload Too Large',
        414 => 'URI Too Long',
        415 => 'Unsupported Media Type',
        416 => 'Range Not Satisfiable',
        417 => 'Expectation Failed',
        418 => 'I\'m a teapot',
        421 => 'Misdirected Request',
        422 => 'Unprocessable Entity',
        423 => 'Locked',
        424 => 'Failed Dependency',
        426 => 'Upgrade Required',
        428 => 'Precondition Required',
        429 => 'Too Many Requests',
        431 => 'Request Header Fields Too Large',
        444 => 'Connection Closed Without Response',
        451 => 'Unavailable For Legal Reasons',
        499 => 'Client Closed Request',
        500 => 'Internal Server Error',
        501 => 'Not Implemented',
        502 => 'Bad Gateway',
        503 => 'Service Unavailable',
        504 => 'Gateway Timeout',
        505 => 'HTTP Version Not Supported',
    );
    $code = intval($code);
    if(!isset($messages[$code])) $code = 500;
    if($status===false) $status = 'error';
    if($message===false) $message = $messages[$code];
    else header("Warning: {$message}");
    http_response_code($code);
    $out = <<<JSON
{
  "status":  "{$status}",
  "message": "{$message}"
}

JSON;
    echo $out;
    exit;
}

/** Authorize user by given type, in Authorization header */
function authorize() {
    global $_TEST;
    static $_authorized = false;
    if($_authorized!==false) return $_authorized;
    if(HTTP_AUTHORIZATION_TYPE=='Google') {
        require_once PRIVATE_DIR.'/google.php';
        return $_authorized = google_authorize();
    }
    if($_TEST) return $_authorized = 0;
    error(401);
}

/** Handle fields in URL: /page/:user/[:noun/[:key/]] */
function url_user_noun_fields($args,&$uid,&$noun,&$key,$keys) {
    global $_TEST;
    $userKey = false;
    $user = false;
    $uid = false;
    $noun = false;
    $key = false;
    if(is_array($args)) {
        $n = count($args);
        if($n>3) error(400,false,'Malformed URL');
        if($n>=1 && ($i = strtoupper(trim($args[0])))) $userKey = $i;
        if($n>=2 && ($i = strtolower(trim($args[1])))) $noun = $i;
        if($n==3 && ($i = strtoupper(trim($args[2])))) $key = $i;
        if($userKey!==false) $user = intval(base_convert($userKey,36,10));
        if($key!==false) $key = intval(base_convert($key,36,10));
    }
    $type = -1;
    if($noun!==false) {
        if(!isset($keys[$noun])) error(400,false,'Malformed URL');
        $type = $keys[$noun];
    }
    if($_TEST) {
        if($user!==false) {
            $user = false;
            $uid = 0;
        }
    }
    if($user!==false && ($uid = intval(db_column(SELECT_USER_ID,$user)))===0)
        error(404,false,"Invalid user key \\\"{$userKey}\\\"");
    if($key!==false) test_key($key,$type,404,METHOD!=POST);

}

/** Handle common query string fields, e.g. offset, limit etc.
 * @param $o [reference] Offset
 * @param $l [reference] Limit
 * @param $lm [integer] Limit maximum
 */
function query_string_fields(&$o,&$l,$lm,&$f,&$t) {
    $o = 0;
    $l = 0;
    if(isset($_GET['offset'])) $o = intval($_GET['offset']);
    if(isset($_GET['limit'])) $l = intval($_GET['limit']);
    if($o<0) $o = 0;
    if($l<1) $l = $lm;
    elseif($l>$lm) $l = $lm;
    $f = false;
    $t = false;
    if(isset($_GET['from'])) $f = intval($_GET['from']);
    if(isset($_GET['to'])) $t = intval($_GET['to']);
}

/** Test if user has write permission for user and nounKey (if set)
 * @return true if ok, false if testing nounKey is required
 */
function test_write_permission($uid,$key = null) {
    global $_USER,$_TEST;
    if($_TEST) error(403);
    if($uid===false || $key===false) error(404);
    if(isset($_USER)) {
        if(isset($_USER['flags']) && ($_USER['flags']&USER_ADMIN)) return true;
        if(isset($_USER['id']) && $_USER['id']==$uid) {
            if($key===null) return true;
            return false;
        }
    }
    error(403); /* Not permitted to write data */
}

function is_valid_int($p,&$v) {
    $n = is_array($p)? count($p) : 0;
    $min = $n>=1? $p[0] : 0;
    $max = $n>=2? $p[1] : 0;
    $v = intval($v);
    return ($v>=$min && ($max<=$min || $v<=$max));
}

function is_valid_text($p,&$v) {
    $n = is_array($p)? count($p) : 0;
    $min = $n>=1? $p[0] : 0;
    $max = $n>=2? $p[1] : 0;
    $l = strlen($v);
    return ($l>=$min && ($max<=$min || $l<=$max));
}

function is_valid_key($p,&$v) {
    $type = is_array($p)? $p[0] : -1;
    $v = intval(base_convert($v,36,10));
    return is_key($v,$type);
}

function is_valid_language($p,&$v) {
    static $languages = array('en' => 'en','en_GB' => 'en','en_US' => 'en','sv' => 'sv');
    if(!isset($languages[$v])) return false;
    $v = $languages[trim($v)];
    return true;
}

function evaluate_field(&$q,$i,$p,&$a,&$f,$e) {
    if(!isset($q[$i])) return false;
    $v = &$q[$i];
    $v = trim($v);
    if(!$e($p,$v)) return false;
    if(is_array($a)) {
        $a[] = &$v;
        if(is_array($f)) $f[] = "`{$i}`=?";
    }
    return true;
}

function evaluate_int(&$q,$i,$p,&$a,&$f) {
    return evaluate_field($q,$i,$p,$a,$f,'is_valid_int');
}

function evaluate_text(&$q,$i,$p,&$a,&$f) {
    return evaluate_field($q,$i,$p,$a,$f,'is_valid_text');
}

function evaluate_key(&$q,$i,$p,&$a,&$f) {
    return evaluate_field($q,$i,$p,$a,$f,'is_valid_key');
}

function evaluate_language(&$q,$i,$p,&$a,&$f) {
    return evaluate_field($q,$i,$p,$a,$f,'is_valid_language');
}

function evaluate_fields(&$q,$e,&$a,&$f,$force = false) {
    foreach($e as $field => $params) {
        if(is_array($params)) $func = array_shift($params);
        else {
            $func = $params;
            $params = null;
        }
        if(!$func($q,$field,$params,$a,$f) && $force===true)
            error(400,false,"Missing or malformed field: \\\"{$field}\\\"");
    }
}

