package org.maddev.web.dax;


import com.allatori.annotations.DoNotRename;

@DoNotRename
public enum DaxPathStatus {
    UNMAPPED_REGION,
    SUCCESS,
    BLOCKED,
    EXCEEDED_SEARCH_LIMIT,
    UNREACHABLE,
    NO_WEB_PATH,

    INVALID_CREDENTIALS,
    RATE_LIMIT_EXCEEDED,


    //OFFLINE
    NO_RESPONSE_FROM_SERVER,

    //WHAT IS GOING ON
    UNKNOWN

}