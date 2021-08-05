<?php

require_once __DIR__.'/.init.php';

input($args,$query);
$code = intval($_SERVER['QUERY_STRING']);

error($code);

