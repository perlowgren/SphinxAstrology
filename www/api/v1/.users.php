<?php

const USER_DATA_MISSING = 'No user data sent';
const USER_EVALUATE_FIELDS = array(
    'user'     => array('evaluate_text',2,31),
    'name'     => array('evaluate_text',1,63),
    'language' => 'evaluate_language',
    'picture'  => array('evaluate_text',0,255),
);

function insert_user(&$user) {
    global $_TEST;
    if($_TEST) error(403);
    for($i = 0; $i<0x10; ++$i) {
        $key = make_key(KEY_USER,$i);
        $result = db_query(SELECT_KEY_VALUE,$key);
        if(db_is_empty_result($result)) break;
    }
    if($i==0x10) error(500);
    $user['key'] = $key;
    $fl = FLAG_PRIVATE;
    db_exec('INSERT INTO `SphinxUser` (`id`,`userKey`,`googleId`,`facebookId`,`email`,`user`,`name`,'.
            '`language`,`picture`,`ip`,`counter`,`flags`,`updated`,`created`) '.
            "VALUES (NULL,{$key},?,?,?,?,?,?,?,?,1,{$fl},NOW(),NOW())",
            array($user['googleId'],$user['facebookId'],$user['email'],$user['user'],$user['name'],
                  $user['language'],$user['picture'],$user['ip']));
    http_response_code(201);
}

function select_user(&$user) {
    global $_TEST;
    $user['ip'] = $_SERVER['REMOTE_ADDR'];
    $user['port'] = $_SERVER['REMOTE_PORT'];
    $field = false;
    if(isset($user['googleId']) && $user['googleId']) $field = 'googleId';
    elseif(isset($user['facebookId']) && $user['facebookId']) $field = 'facebookId';
    elseif(isset($user['email']) && $user['email']) $field = 'email';
    if($_TEST || $field===false) error(403);
    $result = db_query('SELECT `id`,`userKey`,`user`,`name`,`language`,`flags` FROM `SphinxUser` '.
                       "WHERE `{$field}`=?",$user[$field]);
    if(db_is_empty_result($result)) insert_user($user);
    else {
        $row = $result->fetch_assoc();
        if(intval($row['flags'])&USER_BLOCKED) error(403);
        $user['id'] = $row['id'];
        $user['key'] = $row['userKey'];
        $user['user'] = $row['user'];
        $user['name'] = $row['name'];
        $user['language'] = $row['language'];
        $user['flags'] = $row['flags'];
        db_exec("UPDATE `SphinxUser` SET `ip`='{$user['ip']}',`counter`=`counter`+1 WHERE `userKey`='{$user['key']}'");
    }
}

function get_user_json($row,$ind = '',$prefix = '') {
    $key = get_key(intval($row['userKey']));
    $json = <<<JSON
{$ind}{{$prefix}
{$ind}  "key":  "{$key}",
{$ind}  "user": "{$row['user']}",
{$ind}}
JSON;
    return $json;
}

function output_user($result) {
    if(db_is_empty_result($result)) error(404);
    $row = $result->fetch_assoc();
    $json = get_user_json($row,'',"\n  \"status\": \"OK\",");
    if($json) $json = "{$json}\n";
    output($json);
}

function output_users($result) {
    if(db_is_empty_result($result)) output(false,true,'X-Total-Count: 0');
    $total = db_column('SELECT FOUND_ROWS()');
    $users = '';
    for($i = 0; ($row = $result->fetch_assoc()); ++$i) {
        if($i>0) $users .= ",\n";
        $users .= get_user_json($row,'    ');
    }
    if($users) $users = "\n{$users}\n  ";
    $json = <<<JSON
{
  "status": "OK",
  "total": {$total},
  "users": [{$users}]
}

JSON;
    header("X-Total-Count: {$total}");
    output($json);
}


function delete_user($uid) {
    test_write_permission($uid);
    db_exec('UPDATE `SphinxUser` SET `flags`=(`flags`|'.USER_DELETED.") WHERE `id`={$uid}");
    status(204);
}

function activate_user($uid) {
    test_write_permission($uid);
    db_exec('UPDATE `SphinxUser` SET `flags`=(`flags`&'.(0xffffffff^USER_DELETED).") WHERE `id`={$uid}");
    status(204);
}

function get_user($uid) {
    global $_TEST;
    if($uid===false) error(404); /* Missing userId field */
    $nfl = USER_BLOCKED|USER_DELETED;
    if($_TEST) $nfl |= FLAG_PRIVATE;
    $result = db_query("SELECT `userKey`,`user` FROM `SphinxUser` WHERE `id`={$uid} AND NOT (`flags`&{$nfl})");
    output_user($result);
}

function get_users($keys = false) {
    global $_TEST;
    query_string_fields($offset,$limit,USERS_LIMIT_MAX,$from,$to);
    $nfl = USER_BLOCKED|USER_DELETED;
    if($_TEST) $nfl |= FLAG_PRIVATE;
    $w = '';
    if($keys!==false) {
        if(!is_array($keys)) $keys = array($keys);
        foreach($keys as &$key)
            $key = test_key($key,KEY_USER,400);
        $keys = implode(',',$keys);
        $w .= "`userKey` IN ({$keys}) AND ";
    }
    if($from!==false) $w .= " `created`>=FROM_UNIXTIME({$from}) AND ";
    if($to!==false) $w .= " `created`<=FROM_UNIXTIME({$to}) AND ";
    $w .= "NOT (`flags`&{$nfl})";
    $result = db_query("SELECT SQL_CALC_FOUND_ROWS `userKey`,`user` FROM `SphinxUser` WHERE {$w} LIMIT {$offset},{$limit}");
    output_users($result);
}

function patch_user($uid,$query) {
    test_write_permission($uid);
    if(!$query || !is_array($query)) error(422,false,USER_DATA_MISSING);
    $a = array();
    $f = array();
    evaluate_fields($query,USER_EVALUATE_FIELDS,$a,$f,false);
    if(count($f)>0) {
        $f = implode(',',$f);
        db_exec("UPDATE `SphinxUser` SET {$f} WHERE `id`={$uid}",$a);
    }
    status(204);
}

