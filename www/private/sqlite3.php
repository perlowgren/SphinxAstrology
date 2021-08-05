<?php

use SQLite3;

const DB_FILE = PRIVATE_DATA_DIR.'/'.PROJECT_NAME.'.db';

$sqlite3_db_is_new = false;
$sqlite3_db = null;

function db_open() {
    global $sqlite3_db;
    $sqlite3_db_is_new = (!file_exists(DB_FILE) || filesize(DB_FILE)===0)? 1 : 0;
    $sqlite3_db = new SQLite3(DB_FILE);
    $sqlite3_db->busyTimeout(60000);
}

function db_close() {
    global $sqlite3_db;
    if(!$sqlite3_db) return;
    $sqlite3_db->close();
    unset($sqlite3_db);
    $sqlite3_db = null;
}

function db_prepare_statement($sql,$p = false) {
    global $sqlite3_db;
    $stmt = $sqlite3_db->prepare($sql);
    for($i = 0,$n = count($p); $i<$n; ++$i) {
        $v = $p[$i];
        if(is_int($v)) {
            $t = SQLITE3_INTEGER;
        } else {
            $t = SQLITE3_TEXT;
            if(is_array($v) || is_object($v))
                $v = json_encode($v,JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES);
        }
        $stmt->bindValue($i+1,$v,$t);
    }
    return $stmt;
}

function db_exec($sql,$p = false) {
    global $sqlite3_db;
    if($sqlite3_db===null) db_open();
    if($p===false) $sqlite3_db->exec($sql);
    else {
        $stmt = db_prepare_statement($sql,$p);
        $stmt->execute();
    }
}

function db_query($sql,$p = false) {
    global $sqlite3_db;
    if($sqlite3_db===null) db_open();
    if($p===false) return $sqlite3_db->query($sql);
    $stmt = db_prepare_statement($sql,$p);
    return $stmt->execute();
}

function db_row($sql,$p = false) {
    global $sqlite3_db;
    if($p===false) {
        if($sqlite3_db===null) db_open();
        return $sqlite3_db->querySingle($sql,true);
    }
    $result = db_query($sql,$p);
    if($result) return $result->fetchArray(SQLITE3_ASSOC);
    return null;
}

function db_column($sql,$p = false) {
    global $sqlite3_db;
    if($p===false) {
        if($sqlite3_db===null) db_open();
        return $sqlite3_db->querySingle($sql);
    }
    $result = db_query($sql,$p);
    if($result) {
        $row = $result->fetchArray(SQLITE3_NUM);
        return $row[0];
    }
    return null;
}

function db_insert_id() {
    global $sqlite3_db;
    if($sqlite3_db===null) db_open();
    return $sqlite3_db->lastInsertRowID();
}

function db_error() {
    global $sqlite3_db;
    if($sqlite3_db===null) db_open();
    return $sqlite3_db->lastErrorMsg();
}

function db_sql($s) {
    return str_replace('\'','\'\'',$s);
}


