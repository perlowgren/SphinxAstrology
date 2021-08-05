<?php

function sess_open($path,$name) {
    return true;
}

function sess_close() {
    session_write_close();
    return true;
}

function sess_read($sid) {
    $data = db_column('SELECT `data` FROM `'.SESSION_TABLE.'` WHERE `sid`=?',array($sid));
    if($data===null || $data===false) {
        db_exec('INSERT INTO `'.SESSION_TABLE.'` (`id`,`sid`,`data`,`counter`,`created`) VALUES (NULL,?,\'\',1,NOW())',array($sid));
        return '';
    } else {
//		db_exec('UPDATE `'.SESSION_TABLE.'` SET `updated`=NOW() WHERE `sid`=?',array($sid));
        return $data;
    }
}

function sess_write($sid,$data) {
    db_exec('UPDATE `'.SESSION_TABLE.'` SET `data`=?,`counter`=`counter`+1,`updated`=NOW() WHERE `sid`=?',array($data,$sid));
    return true;
}

function sess_destroy($sid) {
    db_exec('DELETE FROM `'.SESSION_TABLE.'` WHERE `sid`=?',array($sid));
    return true;
}

function sess_gc($maxlifetime) {
    $tm = time();
    db_exec('DELETE FROM `'.SESSION_TABLE.'` WHERE `updated`<DATE_SUB(NOW(),INTERVAL '.SESSION_LIFETIME.' SECOND)');
    return true;
}

session_set_save_handler('sess_open','sess_close','sess_read','sess_write','sess_destroy','sess_gc');
session_start();


/*function UUID() {
	$t = explode(' ',microtime());
	$p = explode('.',$_SERVER["REMOTE_ADDR"]);
	$ip = sprintf('%02x%02x%02x%02x',$p[0],$p[1],$p[2],$p[3]);
	return sprintf('%08s-'.SERVER_ID.'-%04x-%04x-%08x%04x',
		$ip,
		intval($t[0]*0x10000)&0xffff,
		mt_rand(0,0xffff),
		intval($t[1])&0xffffffff,
		mt_rand(0,0xffff)
	);
}

function UUID_decode($uuid) {
	$arr = Array();
	$u = explode("-",$uuid);
	if(is_array($u) && count($u)==5) {
		$x = $u[0];
		$ip = hexdec(substr($x,0,2)).'.'.hexdec(substr($x,2,2)).'.';
		      hexdec(substr($x,4,2)).'.'.hexdec(substr($x,6,2));
		$arr = Array(
			'ip'        => $ip($u[0]),
			'id'        => hexdec($u[1]),
			'micro'     => hexdec($u[2])/0x10000,
			'unixtime'  => hexdec(substr($u[4],0,8)),
		);
	}
	return $arr;
}

function UUID_evaluate($uuid) {
	return preg_match('/^[0-9A-F]{8}-'.SERVER_ID.'-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}$/i',$uuid);
}*/


function make_key($type = -1,$index = -1) {
    $t = explode(' ',microtime());
    $n = intval($t[0]*0x100)&0xff;
    if($n==0) $n = 1;
    if($type!=-1 && $type!=KEY_USER) {
        if($index==-1) $index = mt_rand(0,0xf);
        $n = ($n<<8)|((intval($type)&0xf)<<4)|(intval($index)&0xf);
    }
    return ($n<<32)|(intval($t[1])&0xffffffff);
}

function get_key($type = -1,$index = -1) {
    if($type!=-1 && is_integer($type) && $type>0xffffffff) $key = $type;
    else $key = make_key($type,$index);
    return strtoupper(base_convert(strval($key),10,36));
}

function is_key($key,$type = -1) {
    if(is_integer($key) && $key>0xffffffff) $n = $key;
    else $n = base_convert($key,36,10);
    if($n<=0xffffffff) return false;
    if($type!=-1) {
        $n >>= 32;
        $t = ($n>>4)&0xf;
        $n >>= 8;
        return $t==intval($type);
    }
    return true;
}

function test_key($key,$type = -1,$error,$select = false) {
    static $itypes = array(KEY_USER => KEY_USER,'user' => KEY_USER,KEY_PROFILE => KEY_PROFILE,'profile' => KEY_PROFILE,KEY_TEXT => KEY_TEXT,'text' => KEY_TEXT);
    static $stypes = array(KEY_USER => 'user','user' => 'user',KEY_PROFILE => 'profile','profile' => 'profile',KEY_TEXT => 'text','text' => 'text');
    if(is_integer($key)) $n = $key;
    else $n = intval(base_convert($key,36,10));
    $s = isset($stypes[$type])? $stypes[$type] : '';
    $err = false;
    if($select===true) {
        $v = db_column(SELECT_KEY_VALUE,$n);
        if($s && $s!=$v) $err = true;
    } else {
        $i = isset($itypes[$type])? $itypes[$type] : -1;
        $err = !is_key($n,$i);
    }
    if($err===true) {
        $type = $s? " {$s} " : ' ';
        error($error,false,"Invalid{$type}key \\\"{$key}\\\"");
    }
    return $n===$key? true : $n;
}


