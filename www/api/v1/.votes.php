<?php

const VOTE_DATA_MISSING = 'No vote data sent';
const VOTE_EVALUATE_FIELDS = array(
    'rate'  => 'evaluate_int',
    'title' => array('evaluate_text',0,255),
    'text'  => 'evaluate_text',
);

function test_vote_write_permission($uid,$key) {
    if(!test_write_permission($uid,$key)) {
        $u = intval(db_column("SELECT `userId` FROM `SphinxVote` WHERE `userId`={$uid} AND `textKey`={$key}"));
        if(!$u || $u!=$uid) error(403); /* Not permitted to write data */
    }
}

function get_vote_json($row,$ind = '',$prefix = '') {
    $user = get_key(intval($row['userKey']));
    $rate = intval($row['rate']);
    $title = json_encode($row['title'],JSON_ENCODE_FLAGS);
    $text = json_encode($row['text'],JSON_ENCODE_FLAGS);
    $json = <<<JSON
{$ind}{{$prefix}
{$ind}  "user": "{$user}",
{$ind}  "rate": {$rate},
{$ind}  "title": {$title},
{$ind}  "text": {$text},
{$ind}  "updated": {$row['updated']},
{$ind}  "created": {$row['created']}
{$ind}}
JSON;
    return $json;
}

function output_votes($result,$key) {
    if(db_is_empty_result($result)) output(false,true,'X-Total-Count: 0');
    $total = db_column('SELECT FOUND_ROWS()');
    $votes = '';
    for($i = 0; ($row = $result->fetch_assoc()); ++$i) {
        if($i>0) $votes .= ",\n";
        $votes .= get_vote_json($row,'    ');
    }
    if($votes) $votes = "\n{$votes}\n  ";
    $out = <<<JSON
{
  "status": "OK",
  "total": {$total},
  "text": {$key},
  "votes": [{$votes}]
}

JSON;
    header("X-Total-Count: {$total}");
    output($out);
}


function delete_vote($uid,$key) {
    test_vote_write_permission($uid,$key);
    db_exec("DELETE FROM `SphinxVote` WHERE `userId`={$uid} AND `textKey`={$key}");
    status(204);
}

function get_votes($uid,$key) {
    if(!$key) error(404); /* Missing textId field */
    query_string_fields($offset,$limit,VOTES_LIMIT_MAX,$from,$to);
    $w = "v.`textKey`={$key}";
    if($from!==false) $w .= " AND v.`updated`>=FROM_UNIXTIME({$from})";
    if($to!==false) $w .= " AND v.`updated`<=FROM_UNIXTIME({$to})";
    $result = db_query('SELECT u.`userKey`,v.`rate`,v.`title`,v.`text`,'.
                       'UNIX_TIMESTAMP(v.`updated`) AS `updated`,UNIX_TIMESTAMP(v.`created`) AS `created` '.
                       'FROM `SphinxVote` AS v LEFT JOIN `SphinxUser` AS u ON v.`userId`=u.`id` '.
                       "WHERE {$w} LIMIT {$offset},{$limit}");
    output_votes($result);
}

function patch_vote($uid,$key,$query) {
    test_vote_write_permission($uid,$key);
    if(!$query || !is_array($query)) error(422,false,VOTE_DATA_MISSING);
    $a = array();
    $f = array();
    evaluate_fields($query,VOTE_EVALUATE_FIELDS,$a,$f,false);
    if(count($f)>0) {
        $f = implode(',',$f);
        db_exec("UPDATE `SphinxVote` SET {$f} WHERE `userId`={$uid} AND `textKey`={$key}",$a);
    }
    status(204);
}

function post_vote($uid,$key,$query) {
    $id = intval(db_column("SELECT `id` FROM `SphinxVote` WHERE `userId`={$uid} AND `textKey`={$key}"));
    if($id) put_vote($uid,$key,$query);
    test_write_permission($uid,$key);
    if(!$query || !is_array($query)) error(422,false,VOTE_DATA_MISSING);
    $a = array();
    $f = false;
    evaluate_fields($query,TEXT_EVALUATE_FIELDS,$a,$f,true);
    db_exec('INSERT INTO `SphinxVote` (`id`,`userId`,`textKey`,`rate`,`title`,`text`,'.
            "`flags`,`updated`,`created`) VALUES (NULL,{$uid}},{$key},?,?,?,14,NOW(),NOW())",$a);
    status(201);
}

function put_vote($uid,$key,$query) {
    test_vote_write_permission($uid,$key);
    if(!$query || !is_array($query)) error(422,false,VOTE_DATA_MISSING);
    $a = array();
    $f = array();
    evaluate_fields($query,VOTE_EVALUATE_FIELDS,$a,$f,true);
    if(count($f)>0) {
        $f = implode(',',$f);
        db_exec("UPDATE `SphinxVote` SET {$f} WHERE `userId`={$uid} AND `textKey`={$key}",$a);
    }
    status(204);
}


