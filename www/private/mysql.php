<?php

$mysql_db = null;

$mysqli_bind_param_method = new ReflectionMethod('mysqli_stmt','bind_param');

function db_open() {
    global $mysql_db;
    $mysql_db = new mysqli(MYSQL_HOST,MYSQL_USER,MYSQL_PASSWORD,MYSQL_NAME);
    $mysql_db->set_charset("utf8");
}

function db_close() {
    global $mysql_db;
    if(!$mysql_db) return;
    $mysql_db->close();
    unset($mysql_db);
    $mysql_db = null;
}

function db_prepare_statement($sql,$p) {
    global $mysql_db,$mysqli_bind_param_method;
    $stmt = $mysql_db->prepare($sql);
    $t = '';
    if(!is_array($p)) $p = array($p);
    foreach($p as &$v) {
        if(is_int($v)) $t .= 'i';
        else {
            $t .= 's';
            if(is_array($v) || is_object($v))
                $v = json_encode($v,JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES);
        }
    }
    array_unshift($p,$t);
    $mysqli_bind_param_method->invokeArgs($stmt,$p);
    return $stmt;
}

function db_exec($sql,$p = false) {
    global $mysql_db;
    if($mysql_db===null) db_open();
    if($p===false) $mysql_db->query($sql);
    else {
        $stmt = db_prepare_statement($sql,$p);
        $stmt->execute();
    }
}

function db_query($sql,$p = false) {
    global $mysql_db;
    if($mysql_db===null) db_open();
    if($p===false) return $mysql_db->query($sql);
    $stmt = db_prepare_statement($sql,$p);
    $stmt->execute();
    return $stmt->get_result();
}

function db_row($sql,$p = false) {
    $result = db_query($sql,$p);
    if(is_object($result) && $result->num_rows>0)
        return $result->fetch_array(MYSQLI_ASSOC);
    return null;
}

function db_column($sql,$p = false) {
    $result = db_query($sql,$p);
    if(is_object($result) && $result->num_rows>0) {
        $row = $result->fetch_array(MYSQLI_NUM);
        return $row[0];
    }
    return null;
}

function db_insert_id() {
    global $mysql_db;
    if($mysql_db===null) return -1;
    return $mysql_db->insert_id;
}

function db_is_empty_result($result) {
    return (!is_object($result) || $result->num_rows===0);
}

function db_error() {
    global $mysql_db;
    if($mysql_db===null) return null;
    return $mysql_db->error;
}

function db_sql($s) {
    return str_replace('\'','\'\'',$s);
}


