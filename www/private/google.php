<?php

const GOOGLE_TOKEN_VERIFIED = 1;
const GOOGLE_TOKEN_VERIFIED_SESSION = 2;
const GOOGLE_TOKEN_FAIL_INVALID = 3;
const GOOGLE_TOKEN_FAIL_EXPIRED = 4;

function google_verify($token,&$data) {
    global $_USER;
    $data = '';

    $init_session = array('id_token' => false,'id_token_verified' => false);
    foreach($init_session as $k => $v)
        if(!isset($_SESSION[$k]))
            $_SESSION[$k] = $v;
    $id_token = &$_SESSION['id_token'];
    $id_token_verified = &$_SESSION['id_token_verified'];

    if(!$token) {
        $data = 'Authorization header format error';
        return GOOGLE_TOKEN_FAIL_INVALID;
    }

    if($id_token!==false && $id_token_verified===true)
        if($token==$id_token) {
            if(isset($_USER) && $_SERVER['REMOTE_ADDR']==$_USER['ip'])
                return GOOGLE_TOKEN_VERIFIED_SESSION;
        }

    require_once PRIVATE_DIR.'/Google/autoload.php';

    $client = new Google_Client();
//	$client->setClientId(GOOGLE_CLIENT_ID);
//	$client->setClientSecret(GOOGLE_CLIENT_SECRET);
//	$client->setRedirectUri(GOOGLE_REDIRECT_URI);
//	$client->setScopes('email');
    $client->setAuthConfigFile(PRIVATE_DATA_DIR.'/'.GOOGLE_CLIENT_SECRET_API_JSON);

    try {

        $x = $client->verifyIdToken($token,GOOGLE_CLIENT_ID)
                    ->getAttributes();

    } catch(Google_Auth_Exception $e) {
        $m = $e->getMessage();
        $data = substr($m,0,strpos($m,':'));
        $reason = $data;
        if(($p = strpos($reason,','))>=0)
            $reason = substr($reason,0,$p);
        switch($reason) {
            case 'No issue time in token':
            case 'No expiration time in token':
            case 'Token used too early':
            case 'Token used too late':
                return GOOGLE_TOKEN_FAIL_EXPIRED;
            default:
                return GOOGLE_TOKEN_FAIL_INVALID;
        }
    }

    $id_token = $token;
    $id_token_verified = true;

    $payload = $x['payload'];
    $_USER = array(
        'id'         => 0,
        'googleId'   => $payload['sub'],
        'facebookId' => null,
        'expires'    => $payload['exp'],
        'email'      => $payload['email'],
        'user'       => $payload['name'],
        'name'       => $payload['name'],
        'picture'    => $payload['picture'],
        'language'   => $payload['locale']
    );
    return GOOGLE_TOKEN_VERIFIED;
}

function google_authorize() {
    global $_USER;
    $code = 401;
    $status = false;
    $message = false;
    switch(google_verify(HTTP_AUTHORIZATION,$data)) {
        case GOOGLE_TOKEN_VERIFIED:
            require_once PUBLIC_DIR.'/.users.php';
            select_user($_USER);
            $_SESSION['user'] = &$_USER;
            return 0;

        case GOOGLE_TOKEN_VERIFIED_SESSION:
            return 1;

        case GOOGLE_TOKEN_FAIL_INVALID:
            $status = 'invalid';
            $message = $data;
            break;

        case GOOGLE_TOKEN_FAIL_EXPIRED:
            $status = 'expired';
            $message = $data;
            break;

        default:
            $message = 'Unknown';
            break;
    }
    error($code,$status,$message);
}

