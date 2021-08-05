<?php

require_once __DIR__.'/.init.php';

input($args,$query);
url_user_noun_fields($args,
                     $uid,$noun,$key,
                     array(
                         'profiles' => KEY_PROFILE,
                         'texts'    => KEY_TEXT,
                         'votes'    => KEY_TEXT,
                     )
);

if($noun===false) {
    if(METHOD==OPTIONS) {
        header('Allow: DELETE, GET, HEAD, PATCH, POST');
        status(204);
    }
    authorize();
    require_once __DIR__.'/.users.php';
    if(METHOD==DELETE) {
        delete_user($uid);
    } elseif(METHOD==GET || METHOD==HEAD) {
        if($uid!==false) get_user($uid);
        else get_users();
    } elseif(METHOD==PATCH) {
        patch_user($uid,$query);
    } elseif(METHOD==POST) {
        if(!is_array($query) || !isset($query['users']))
            error(422,false,'Missing field \\\"users\\\"');
        get_users($query['users']);
    }

} elseif($noun=='profiles') {
    if(METHOD==OPTIONS) {
        header('Allow: DELETE, GET, HEAD, PATCH, POST, PUT');
        status(204);
    }
    authorize();
    require_once __DIR__.'/.profiles.php';
    if(METHOD==DELETE) {
        delete_profile($uid,$key);
    } elseif(METHOD==GET || METHOD==HEAD) {
        if($key!==false) get_profile($uid,$key);
        else get_profiles($uid);
    } elseif(METHOD==PATCH) {
        patch_profile($uid,$key,$query);
    } elseif(METHOD==POST) {
        if(is_array($query) && isset($query['profiles'])) {
            get_profiles($uid,$query['profiles']);
        } else {
            post_profile($uid,$key,$query);
        }
    } elseif(METHOD==PUT) {
        put_profile($uid,$key,$query);
    }

} elseif($noun=='texts') {
    if(METHOD==OPTIONS) {
        header('Allow: DELETE, GET, HEAD, PATCH, POST, PUT');
        status(204);
    }
    authorize();
    require_once __DIR__.'/.texts.php';
    if(METHOD==DELETE) {
        delete_text($uid,$key);
    } elseif(METHOD==GET || METHOD==HEAD) {
        if($key!==false) get_text($uid,$key);
        else get_texts($uid);
    } elseif(METHOD==PATCH) {
        patch_text($uid,$key,$query);
    } elseif(METHOD==POST) {
        if(is_array($query) && isset($query['texts'])) {
            get_texts($uid,$query['texts']);
        } else {
            post_text($uid,$key,$query);
        }
    } elseif(METHOD==PUT) {
        put_text($uid,$key,$query);
    }

} elseif($noun=='votes') {
    if(METHOD==OPTIONS) {
        header('Allow: DELETE, GET, HEAD, PATCH, POST, PUT');
        status(204);
    }
    authorize();
    require_once __DIR__.'/.votes.php';
    if(METHOD==DELETE) {
        delete_vote($uid,$key);
    } elseif(METHOD==GET || METHOD==HEAD) {
        get_votes($uid,$key);
    } elseif(METHOD==PATCH) {
        patch_vote($uid,$key,$query);
    } elseif(METHOD==POST) {
        post_votes($uid,$key,$query);
    } elseif(METHOD==PUT) {
        put_vote($uid,$key,$query);
    }
}

error(405);

