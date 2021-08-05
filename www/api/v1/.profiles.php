<?php

const PROFILE_DATA_MISSING = 'No profile data sent';
const PROFILE_EVALUATE_FIELDS = array(
    'categories' => 'evaluate_profile_categories',
    'name'       => array('evaluate_text',1,63),
    'time'       => 'evaluate_profile_time',
    'longitude'  => 'evaluate_int',
    'latitude'   => 'evaluate_int',
    'timeZone'   => 'evaluate_int',
    'dst'        => 'evaluate_int',
    'sun'        => 'evaluate_int',
    'moon'       => 'evaluate_int',
    'ascendant'  => 'evaluate_int',
    'picture'    => array('evaluate_text',0,255),
    'flags'      => 'evaluate_int',
);

function test_profile_write_permission($uid,$key) {
    if(!test_write_permission($uid,$key)) {
        $u = intval(db_column("SELECT `userId` FROM `SphinxProfile` WHERE `profileKey`={$key}"));
        if(!$u || $u!=$uid) error(403); /* Not permitted to write data */
    }
}

function evaluate_profile_categories(&$q,$i,$p,&$v,&$a,&$f) {
    if(!isset($q[$i])) return false;
    $v = &$q[$i];
    $v = trim($v);
    $c = explode(':',$v);
    if(is_array($a)) {
        $a[] = hexdec($c[0]);
        $a[] = count($c)>1 && $c[1]? hexdec($c[1]) : 0;
        if(is_array($f)) $f[] = '`cat1`=?,`cat2`=?';
    }
    return true;
}

function evaluate_profile_time(&$q,$i,$p,&$v,&$a,&$f) {
    if(!isset($q[$i])) return false;
    $v = &$q[$i];
    $v = trim($v);
    if(!preg_match('/^(BCE)? *(\d{1,4})-(\d?\d)-(\d?\d) *(\d?\d:\d?\d(?::\d?\d)?)? *([GJ])?$/i',$v,$m)) return false;
    if(is_array($a)) {
        $bce = $m[1]? true : false;
        $y = intval($m[2]);
        if($bce) $y = -$y;
        $a[] = $y;
        $a[] = intval($m[3]);
        $a[] = intval($m[4]);
        $a[] = $m[5];
        if(is_array($f)) $f[] = '`year`=?,`month`=?,`day`=?,`time`=?';
    }
    return true;
}

function evaluate_flags(&$q,$key = false) {
    $flags = &$q['flags'];
    $flags &= (FLAG_PUBLIC|FLAG_PRIVATE|FLAG_STATIC);
    if($key!==false)
        $flags |= intval(db_column("SELECT `flags` FROM `SphinxProfile` WHERE `profileKey`={$key}"));
}


function get_profile_json($row,$ind = '',$prefix = '') {
    $key = get_key(intval($row['profileKey']));
    $user = get_key(intval($row['userKey']));
    $cat1 = intval($row['cat1']);
    $cat2 = intval($row['cat2']);
    $cats = dechex($cat1).':'.dechex($cat2);
    $name = json_encode($row['name'],JSON_ENCODE_FLAGS);
    $year = $row['year'];
    $time = '';
    if($year<0) {
        $time .= 'BCE ';
        $year = -$year;
    }
    $time .= sprintf('%04d-%02d-%02d %s',$year,$row['month'],$row['day'],$row['time']);
    $tz = $row['timeZone'];
    $dst = $row['dst'];
    $json = <<<JSON
{$ind}{{$prefix}
{$ind}  "key": "{$key}",
{$ind}  "user": "{$user}",
{$ind}  "categories": "{$cats}",
{$ind}  "name": {$name},
{$ind}  "time": "{$time}",
{$ind}  "longitude": {$row['longitude']},
{$ind}  "latitude": {$row['latitude']},
{$ind}  "timeZone": {$tz},
{$ind}  "dst": {$dst},
{$ind}  "sun": {$row['sun']},
{$ind}  "moon": {$row['moon']},
{$ind}  "ascendant": {$row['ascendant']},
{$ind}  "picture": "{$row['picture']}",
{$ind}  "flags": {$row['flags']},
{$ind}  "updated": {$row['updated']},
{$ind}  "created": {$row['created']}
{$ind}}
JSON;
    return $json;
}

function output_profile($result,$ind = '') {
    if(db_is_empty_result($result)) output();
    $row = $result->fetch_assoc();
    $out = get_profile_json($row,'',"\n  \"status\": \"OK\",");
    if($out) $out = "{$out}\n";
    output($out);
}

function output_profiles($result) {
    if(db_is_empty_result($result)) output(false,true,'X-Total-Count: 0');
    $total = db_column('SELECT FOUND_ROWS()');
    $profiles = '';
    for($i = 0; ($row = $result->fetch_assoc()); ++$i) {
        if($i>0) $profiles .= ",\n";
        $profiles .= get_profile_json($row,'    ');
    }
    if($profiles) $profiles = "\n{$profiles}\n  ";
    $out = <<<JSON
{
  "status": "OK",
  "total": {$total},
  "profiles": [{$profiles}]
}

JSON;
    header("X-Total-Count: {$total}");
    output($out);
}


function delete_profile($uid,$key) {
    test_profile_write_permission($uid,$key);
    db_exec("DELETE FROM `SphinxProfile` WHERE `profileKey`={$key}");
    status(204);
}

function get_profile($uid,$key) {
    if(!$key) error(404); /* Missing profileId field */
    $result = db_query('SELECT p.`profileKey`,u.`userKey`,p.`cat1`,p.`cat2`,p.`name`,p.`year`,p.`month`,p.`day`,p.`time`,'.
                       'p.`longitude`,p.`latitude`,p.`timeZone`,p.`dst`,p.`sun`,p.`moon`,p.`ascendant`,p.`picture`,p.`flags`,'.
                       'UNIX_TIMESTAMP(p.`updated`) AS `updated`,UNIX_TIMESTAMP(p.`created`) AS `created` '.
                       "FROM `SphinxProfile` AS p LEFT JOIN `SphinxUser` AS u ON p.`userId`=u.`id` WHERE p.`profileKey`={$key}");
    output_profile($result);
}

function get_profiles($uid,$keys = false) {
    query_string_fields($offset,$limit,PROFILES_LIMIT_MAX,$from,$to);
    $w = "p.`userId`={$uid}";
    $a = false;
    if($keys!==false) {
        if(!is_array($keys)) $keys = array($keys);
        foreach($keys as &$key)
            $key = test_key($key,KEY_TEXT,400);
        $keys = implode(',',$keys);
        $w .= " AND p.`profileKey` IN ({$keys})";
    }
    if($from!==false) $w .= " AND p.`updated`>=FROM_UNIXTIME({$from})";
    if($to!==false) $w .= " AND p.`updated`<=FROM_UNIXTIME({$to})";
    $result = db_query('SELECT SQL_CALC_FOUND_ROWS p.`profileKey`,u.`userKey`,p.`cat1`,p.`cat2`,p.`name`,p.`year`,p.`month`,p.`day`,p.`time`,'.
                       'p.`longitude`,p.`latitude`,p.`timeZone`,p.`dst`,p.`sun`,p.`moon`,p.`ascendant`,p.`picture`,p.`flags`,'.
                       'UNIX_TIMESTAMP(p.`updated`) AS `updated`,UNIX_TIMESTAMP(p.`created`) AS `created` '.
                       'FROM `SphinxProfile` AS p LEFT JOIN `SphinxUser` AS u ON p.`userId`=u.`id` '.
                       "WHERE {$w} LIMIT {$offset},{$limit}",$a);
    output_profiles($result);
}

function patch_profile($uid,$key,$query) {
    test_profile_write_permission($uid,$key);
    if(!$query || !is_array($query)) error(422,false,PROFILE_DATA_MISSING);
    $a = array();
    $f = array();
    evaluate_fields($query,PROFILE_EVALUATE_FIELDS,$a,$f,false);
    evaluate_flags($query,$key);
    if(count($f)>0) {
        $f = implode(',',$f);
        db_exec("UPDATE `SphinxProfile` SET {$f} WHERE `profileKey`={$key}",$a);
    }
    status(204);
}

function post_profile($uid,$key,$query) {
    test_write_permission($uid,$key);
    if(!$query || !is_array($query)) error(422,false,PROFILE_DATA_MISSING);
    $a = array();
    $f = false;
    evaluate_fields($query,PROFILE_EVALUATE_FIELDS,$a,$f,true);
    evaluate_flags($query,$key);
    db_exec('INSERT INTO `SphinxProfile` (`id`,`userId`,`profileKey`,`cat1`,`cat2`,`name`,`year`,`month`,`day`,`time`,'.
            '`longitude`,`latitude`,`timeZone`,`dst`,`sun`,`moon`,`ascendant`,`picture`,`flags`,`updated`,`created`) '.
            "VALUES (NULL,{$uid},{$key},?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW())",$a);
    status(201);
}

function put_profile($uid,$key,$query) {
    test_profile_write_permission($uid,$key);
    if(!$query || !is_array($query)) error(422,false,PROFILE_DATA_MISSING);
    $a = array();
    $f = array();
    evaluate_fields($query,PROFILE_EVALUATE_FIELDS,$a,$f,true);
    evaluate_flags($query,$key);
    if(count($f)>0) {
        $f = implode(',',$f);
        db_exec("UPDATE `SphinxProfile` SET {$f} WHERE `profileKey`={$key}",$a);
    }
    status(204);
}


