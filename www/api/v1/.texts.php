<?php

const TEXT_DATA_MISSING = 'No text data sent';
const TEXT_EVALUATE_FIELDS = array(
    'type'     => 'evaluate_text_type',
    'title'    => array('evaluate_text',0,255),
    'text'     => 'evaluate_text',
    'language' => 'evaluate_language',
    'flags'    => 'evaluate_int',
);

function test_text_write_permission($uid,$key) {
    if(!test_write_permission($uid,$key)) {
        $u = intval(db_column("SELECT `userId` FROM `SphinxText` WHERE `textKey`={$key}"));
        if(!$u || $u!=$uid) error(403); /* Not permitted to write data */
    }
}

function evaluate_text_type(&$q,$i,$p,&$v,&$a,&$f) {
    if(!isset($q[$i])) return false;
    $v = &$q[$i];
    $v = trim($v);
    if(!isset($q[$v])) return false;
    if($v=='profile') {
        $pk = &$q[$v];
        $pk = intval(base_convert(trim($pk),36,10));
        if(db_column(SELECT_KEY_VALUE,$pk)!=$v) return false;
        $s = null;
    } elseif($v=='symbol') {
        $pk = null;
        $s = &$q[$v];
        $s = intval(base_convert(trim($s),36,10));
    } else return false;
    if(is_array($a)) {
        $a[] = &$v;
        $a[] = &$pk;
        $a[] = &$s;
        if(is_array($f)) $f[] = "`type`=?,`profileKey`=?,`symbol`=?";
    }
    return true;
}

function evaluate_flags(&$q,$key = false) {
    $flags = &$q['flags'];
    $flags &= (FLAG_PUBLIC|FLAG_PRIVATE|FLAG_STATIC);
    if($key!==false)
        $flags |= intval(db_column("SELECT `flags` FROM `SphinxText` WHERE `textKey`={$key}"));
}

function get_text_json($row,$ind = '',$prefix = '') {
    $key = get_key(intval($row['textKey']));
    $user = get_key(intval($row['userKey']));
    $type = $row['type'];
    if($type=='profile')
        $type_key = get_key(intval($row['profileKey']));
    elseif($type=='symbol')
        $type_key = strtoupper(base_convert(strval($row['symbol']),10,36));
    $title = json_encode($row['title'],JSON_ENCODE_FLAGS);
    $text = json_encode($row['text'],JSON_ENCODE_FLAGS);
    $writer = $row['writer'];
    if(!$writer)
        $writer = $row['user'];
    $writer = json_encode($writer,JSON_ENCODE_FLAGS);
    $user_vote = $row['userVote'];
    if(!$user_vote)
        $user_vote = 0;
    $json = <<<JSON
{$ind}{{$prefix}
{$ind}  "key": "{$key}",
{$ind}  "user": "{$user}",
{$ind}  "type": "{$type}",
{$ind}  "{$type}": "{$type_key}",
{$ind}  "title": {$title},
{$ind}  "text": {$text},
{$ind}  "writer": {$writer},
{$ind}  "userVote": {$user_vote},
{$ind}  "votes": {$row['votes']},
{$ind}  "rates": {$row['rates']},
{$ind}  "language": "{$row['language']}",
{$ind}  "flags": {$row['flags']},
{$ind}  "updated": {$row['updated']},
{$ind}  "created": {$row['created']}
{$ind}}
JSON;
    return $json;
}

function output_text($result,$params = '') {
    if(db_is_empty_result($result)) output();
    $row = $result->fetch_assoc();
    $out = get_text_json($row,'',"\n  \"status\": \"OK\",{$params}");
    if($out) $out = "{$out}\n";
    output($out);
}

function output_texts($result,$params = '') {
    if(db_is_empty_result($result)) output(false,true,'X-Total-Count: 0');
    $total = db_column('SELECT FOUND_ROWS()');
    $texts = '';
    for($i = 0; ($row = $result->fetch_assoc()); ++$i) {
        if($i>0) $texts .= ",\n";
        $texts .= get_text_json($row,'    ');
    }
    if($texts) $texts = "\n{$texts}\n  ";
    $out = <<<JSON
{
  "status": "OK",
  "total": {$total},{$params}
  "texts": [{$texts}]
}

JSON;
    header("X-Total-Count: {$total}");
    output($out);
}


function delete_text($uid,$key) {
    test_text_write_permission($uid,$key);
    db_exec("DELETE FROM `SphinxText` WHERE `textKey`={$key}");
    status(204);
}

function get_text($uid,$key) {
    if(!$key) error(404); /* Missing textId field */
    $result = db_query('SELECT t.`textKey`,u.`userKey`,t.`type`,t.`profileKey`,t.`symbol`,t.`title`,t.`text`,t.`writer`,u.`user`,'.
                       'v.`rate` AS userVote,t.`votes`,t.`rates`,t.`language`,t.`flags`,'.
                       'UNIX_TIMESTAMP(t.`updated`) AS `updated`,UNIX_TIMESTAMP(t.`created`) AS `created` '.
                       'FROM `SphinxText` AS t LEFT JOIN `SphinxUser` AS u ON t.`userId`=u.`id` '.
                       "LEFT JOIN `SphinxVote` AS v ON v.`userId`={$uid} AND v.`textKey`=t.`textKey` WHERE t.`textKey`={$key}");
    output_text($result);
}

function get_texts($uid,$keys = false) {
    query_string_fields($offset,$limit,TEXTS_LIMIT_MAX,$from,$to);
    $a = false;
    if(isset($_GET['profile'])) {
        $profile = test_key($_GET['profile'],KEY_PROFILE,404);
        $w = "t.`type`='profile' AND t.`profileKey`={$profile} AND t.`symbol` IS NULL";
    } elseif(isset($_GET['symbol'])) {
        $symbol = intval(base_convert($_GET['symbol'],36,10));
        $w = "t.`type`='symbol' AND t.`profileKey` IS NULL AND t.`symbol`={$symbol}";
    } elseif($uid) {
        $w = "t.`userId`={$uid}";
    } else {
        $w = "t.`userId` IN (0,1,2,3,4)";
    }
    if($keys!==false) {
        if(!is_array($keys)) $keys = array($keys);
        foreach($keys as &$key)
            $key = test_key($key,KEY_TEXT,400);
        $keys = implode(',',$keys);
        $w .= " AND t.`textKey` IN ({$keys})";
    }
    if($from!==false) $w .= " AND t.`updated`>=FROM_UNIXTIME({$from})";
    if($to!==false) $w .= " AND t.`updated`<=FROM_UNIXTIME({$to})";
    $result = db_query('SELECT SQL_CALC_FOUND_ROWS t.`textKey`,u.`userKey`,t.`type`,t.`profileKey`,t.`symbol`,t.`title`,t.`text`,t.`writer`,u.`user`,'.
                       'v.`rate` AS userVote,t.`votes`,t.`rates`,t.`language`,t.`flags`,'.
                       'UNIX_TIMESTAMP(t.`updated`) AS `updated`,UNIX_TIMESTAMP(t.`created`) AS `created` '.
                       'FROM `SphinxText` AS t LEFT JOIN `SphinxUser` AS u ON t.`userId`=u.`id` '.
                       "LEFT JOIN `SphinxVote` AS v ON v.`userId`={$uid} AND v.`textKey`=t.`textKey` ".
                       "WHERE {$w} LIMIT {$offset},{$limit}",$a);
    $p = '';
    if(isset($_GET['offset'])) $p .= "\n  \"offset\": {$offset},";
    if(isset($_GET['limit'])) $p .= "\n  \"limit\": {$limit},";
    if($from!==false) $p .= "\n  \"from\": {$from},";
    if($to!==false) $p .= "\n  \"to\": {$to},";
    output_texts($result,$p);
}

function patch_text($uid,$key,$query) {
    test_text_write_permission($uid,$key);
    if(!$query || !is_array($query)) error(422,false,TEXT_DATA_MISSING);
    $a = array();
    $f = array();
    evaluate_fields($query,TEXT_EVALUATE_FIELDS,$a,$f,false);
    evaluate_flags($query,$key);
    if(count($f)>0) {
        $f = implode(',',$f);
        db_exec("UPDATE `SphinxText` SET {$f} WHERE `textKey`={$key}",$a);
    }
    status(204);
}

function post_text($uid,$key,$query) {
    test_write_permission($uid,$key);
    if(!$query || !is_array($query)) error(422,false,TEXT_DATA_MISSING);
    $a = array();
    $f = false;
    evaluate_fields($query,TEXT_EVALUATE_FIELDS,$a,$f,true);
    evaluate_flags($query,$key);
    db_exec('INSERT INTO `SphinxText` (`id`,`userId`,`textKey`,`type`,`profileKey`,`symbol`,`title`,`html`,`text`,`writer`,'.
            '`votes`,`rates`,`language`,`flags`,`updated`,`created`) '.
            "VALUES (NULL,{$uid},{$key},?,?,?,?,NULL,?,NULL,0,0,?,?,NOW(),NOW())",$a);
    status(201);
}

function put_text($uid,$key,$query) {
    test_text_write_permission($uid,$key);
    if(!$query || !is_array($query)) error(422,false,TEXT_DATA_MISSING);
    $a = array();
    $f = array();
    evaluate_fields($query,TEXT_EVALUATE_FIELDS,$a,$f,true);
    evaluate_flags($query,$key);
    if(count($f)>0) {
        $f = implode(',',$f);
        db_exec("UPDATE `SphinxText` SET {$f} WHERE `textKey`={$key}",$a);
    }
    status(204);
}


