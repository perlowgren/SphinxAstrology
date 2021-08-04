
#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <sys/time.h>
#include "astro/horoscope.h"

#define JAVA_PACKAGE "net/spirangle/sphinx/"

#if defined(__arm__)
#if defined(__ARM_ARCH_7A__)
    #if defined(__ARM_NEON__)
      #define ABI "armeabi-v7a/NEON"
    #else
      #define ABI "armeabi-v7a"
    #endif
  #else
   #define ABI "armeabi"
  #endif
#elif defined(__i386__)
#define ABI "x86"
#elif defined(__mips__)
#define ABI "mips"
#else
   #define ABI "unknown"
#endif

#include <android/log.h>
#define APPNAME "sphinx"
#define debug_output(...) ((void)__android_log_print(ANDROID_LOG_VERBOSE,APPNAME,__VA_ARGS__))


static int default_planet_ids[] = {
        ASTRO_SUN,
        ASTRO_MOON,
        ASTRO_MERCURY,
        ASTRO_VENUS,
        ASTRO_MARS,
        ASTRO_JUPITER,
        ASTRO_SATURN,
        ASTRO_URANUS,
        ASTRO_NEPTUNE,
        ASTRO_PLUTO,
        ASTRO_ASCENDANT,
        ASTRO_MC,
        -1};

static double default_aspect_orbs[13*10] = {
//   0  | 1  | 2  | 3  | 4  | 5  | 6  | 7  | 8  | 9
//  sun |asc | mc |rul-|rul-|mer |jup |ura |moon|etc
//  moon|    |    |asc |sun |ven |sat |nep |node|
//      |    |    |    |moon|mar |    |plu |    |
    10.0, 0.0, 0.0,10.0,10.0,10.0,10.0,10.0, 0.0, 0.0,   // ASTRO_CONJUNCTION
     3.0, 0.0, 0.0, 3.0, 3.0, 3.0, 3.0, 3.0, 0.0, 0.0,   // ASTRO_SEMISEXTILE
     1.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0,   // ASTRO_DECILE
     1.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0,   // ASTRO_NOVILE
     3.0, 0.0, 0.0, 3.0, 3.0, 3.0, 3.0, 3.0, 0.0, 0.0,   // ASTRO_SEMISQUARE
     1.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0,   // ASTRO_SEPTILE
     6.0, 0.0, 0.0, 6.0, 6.0, 6.0, 6.0, 6.0, 0.0, 0.0,   // ASTRO_SEXTILE
     1.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0,   // ASTRO_QUINTILE
    10.0, 0.0, 0.0,10.0,10.0,10.0,10.0,10.0, 0.0, 0.0,   // ASTRO_SQUARE
    10.0, 0.0, 0.0,10.0,10.0,10.0,10.0,10.0, 0.0, 0.0,   // ASTRO_TRINE
     3.0, 0.0, 0.0, 3.0, 3.0, 3.0, 3.0, 3.0, 0.0, 0.0,   // ASTRO_SESQUIQUADRATE
     3.0, 0.0, 0.0, 3.0, 3.0, 3.0, 3.0, 3.0, 0.0, 0.0,   // ASTRO_QUINCUNX
    10.0, 0.0, 0.0,10.0,10.0,10.0,10.0,10.0, 0.0, 0.0,   // ASTRO_OPPOSITION
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm,void *reserved) {
    debug_output("JNI_OnLoad");
//    jvm = vm;
    return JNI_VERSION_1_6; /* the required JNI version */
}

JNIEXPORT void JNICALL Java_net_spirangle_sphinx_Horoscope_calculateJNI(JNIEnv *env,jobject jthis,jintArray ji,jdoubleArray jd) {
//    const char *data;
//    char lon_arg[33],lat_arg[33],dst_arg[33],deg[33];
    int i,j,x,y,n,r;
    int year,month,day,hour,minute,ctype;
    double second,lon,lat,tz,dst;
    int *planet_ids = default_planet_ids;
    double *aspect_orbs = default_aspect_orbs;
    long usec,t1,t2,t3;
    struct timeval tv;
    astronomy *a;
    horoscope *h;
    astronomy_planet *p1;
    astro_planet *p2;
    astro_house *h2;
    astro_aspect *a2;
    astro_pattern *ap;

    jclass jcl/*,jcl1*/;
//    jobject jtime,jgmt;
    jmethodID jmid;
//    jfieldID jfid,jfy,jfm,jfd,jfh,jfn,jfs,jft,jfjd;
//    jstring jstr;

    gettimeofday(&tv,NULL);
    usec    = tv.tv_usec;

    jcl     = (*env)->GetObjectClass(env,jthis);

/*    jfid    = (*env)->GetFieldID(env,jcl,"time","L" JAVA_PACKAGE "Horoscope$Calendar;");
    jtime   = (*env)->GetObjectField(env,jthis,jfid);
    jcl1    = (*env)->GetObjectClass(env,jtime);
    jfy     = (*env)->GetFieldID(env,jcl1,"year","I");
    jfm     = (*env)->GetFieldID(env,jcl1,"month","I");
    jfd     = (*env)->GetFieldID(env,jcl1,"day","I");
    jfh     = (*env)->GetFieldID(env,jcl1,"hour","I");
    jfn     = (*env)->GetFieldID(env,jcl1,"minute","I");
    jfs     = (*env)->GetFieldID(env,jcl1,"second","D");
    jft     = (*env)->GetFieldID(env,jcl1,"type","I");
    jfjd    = (*env)->GetFieldID(env,jcl1,"julianDay","D");

    year    = (*env)->GetIntField(env,jtime,jfy);
    month   = (*env)->GetIntField(env,jtime,jfm);
    day     = (*env)->GetIntField(env,jtime,jfd);
    hour    = (*env)->GetIntField(env,jtime,jfh);
    minute  = (*env)->GetIntField(env,jtime,jfn);
    second  = (*env)->GetDoubleField(env,jtime,jfs);
    ctype   = (*env)->GetIntField(env,jtime,jft);

    jfid    = (*env)->GetFieldID(env,jcl,"dst","D");
    dst     = (*env)->GetDoubleField(env,jthis,jfid);

    jfid    = (*env)->GetFieldID(env,jcl,"longitude","D");
    lon     = (*env)->GetDoubleField(env,jthis,jfid);
    lon     = -lon;
    jfid    = (*env)->GetFieldID(env,jcl,"latitude","D");
    lat     = (*env)->GetDoubleField(env,jthis,jfid);

    jfid    = (*env)->GetFieldID(env,jcl,"timeZone","D");
    tz      = (*env)->GetDoubleField(env,jthis,jfid);

    if(jplanet_ids!=NULL) {
        jsize len   = (*env)->GetArrayLength(env,jplanet_ids);
        planet_ids  = (int *)malloc((len+1)*sizeof(int));
        jint *arr   = (*env)->GetIntArrayElements(env,jplanet_ids,0);
        for(i=0,n=0; i<len; ++i)
            planet_ids[n++] = (int)arr[i];
        planet_ids[n] = -1;
        (*env)->ReleaseIntArrayElements(env,jplanet_ids,arr,0);
    }
*/
    {
        jsize len;
        jint *iarr;
        jdouble *darr;

        len     = (*env)->GetArrayLength(env,ji);
        iarr    = (*env)->GetIntArrayElements(env,ji,0);

        year    = (int)iarr[0];
        month   = (int)iarr[1];
        day     = (int)iarr[2];
        hour    = (int)iarr[3];
        minute  = (int)iarr[4];
        ctype   = (int)iarr[5];

        planet_ids  = (int *)malloc((len-6+1)*sizeof(int));
        for(i=6,n=0; i<len; ++i)
            planet_ids[n++] = (int)iarr[i];
        planet_ids[n] = -1;

        (*env)->ReleaseIntArrayElements(env,ji,iarr,0);

        len     = (*env)->GetArrayLength(env,jd);
        darr    = (*env)->GetDoubleArrayElements(env,jd,0);

        second  = (double)darr[0];
        lon     = (double)darr[1];
        lon     = -lon;
        lat     = (double)darr[2];
        tz      = (double)darr[3];
        dst     = (double)darr[4];

        (*env)->ReleaseDoubleArrayElements(env,jd,darr,0);
    }

    gettimeofday(&tv,NULL);
    t3 = tv.tv_usec-usec;

/*    if(*data=='\0') {
        time_t t = time(0);
        struct tm *now = localtime(&t);
        lon = 0.0;
        lat = 0.0;
        year = 1900+now->tm_year;
        month = now->tm_mon+1;
        day = now->tm_mday;
        hour = now->tm_hour;
        minute = now->tm_min;
        second = now->tm_sec;
        tz = 0.0;
        dst = 0.0;
    } else {
        sscanf(data,"%s %s %d-%d-%d %d:%d:%lf %lf %s",lon_arg,lat_arg,&year,&month,&day,&hour,&minute,&second,&tz,dst_arg);
        lon = astro_read_coord(lon_arg);
        lat = astro_read_coord(lat_arg);
        dst = !strcmp(dst_arg,"1") || !strcmp(dst_arg,"yes")? 1 : 0;
    }*/

    astro_set_aspect_orbs(aspect_orbs);

    h = horoscope_new("",lon,lat,tz);
    horoscope_set_time(h,year,month,day,hour,minute,second,dst,ctype);

    a = astronomy_new(&h->gmt,h->lon,h->lat);

    gettimeofday(&tv,NULL);
    usec = tv.tv_usec;
    astronomy_get_planets(a,planet_ids,0/*ASTRONOMY_GLON|ASTRONOMY_MOON_PHASE*/);
    gettimeofday(&tv,NULL);
    t1 = tv.tv_usec-usec;

    gettimeofday(&tv,NULL);
    usec = tv.tv_usec;
    horoscope_cast(h,a,HOROSCOPE_NATAL/*|HOROSCOPE_ASPECT_IN_SIGN*/,ASTRO_PLACIDUS);
    gettimeofday(&tv,NULL);
    t2 = tv.tv_usec-usec;

    gettimeofday(&tv,NULL);
    usec = tv.tv_usec;

/*
    (*env)->SetDoubleField(env,jtime,jfjd,(jdouble)h->gmt.jd);

    jfid = (*env)->GetFieldID(env,jcl,"gmt","L" JAVA_PACKAGE "Horoscope$Calendar;");
    jgmt = (*env)->GetObjectField(env,jthis,jfid);
    (*env)->SetIntField(env,jgmt,jfy,(jint)h->gmt.year);
    (*env)->SetIntField(env,jgmt,jfm,(jint)h->gmt.month);
    (*env)->SetIntField(env,jgmt,jfd,(jint)h->gmt.day);
    (*env)->SetIntField(env,jgmt,jfh,(jint)h->gmt.hour);
    (*env)->SetIntField(env,jgmt,jfn,(jint)h->gmt.minute);
    (*env)->SetDoubleField(env,jgmt,jfs,(jdouble)h->gmt.second);
    (*env)->SetDoubleField(env,jgmt,jfjd,(jdouble)h->gmt.jd);
    (*env)->SetIntField(env,jgmt,jft,(jboolean)h->gmt.type);

    jfid = (*env)->GetFieldID(env,jcl,"siderealTime","D");
    (*env)->SetDoubleField(env,jthis,jfid,(jdouble)a->sdrlt);
    jfid = (*env)->GetFieldID(env,jcl,"deltaT","D");
    (*env)->SetDoubleField(env,jthis,jfid,(jdouble)a->deltat);
    jfid = (*env)->GetFieldID(env,jcl,"style","I");
    (*env)->SetIntField(env,jthis,jfid,(jdouble)h->style);
    jfid = (*env)->GetFieldID(env,jcl,"hsystem","I");
    (*env)->SetIntField(env,jthis,jfid,(jdouble)h->hsystem);

    jfid = (*env)->GetFieldID(env,jcl,"mphase","D");
    (*env)->SetDoubleField(env,jthis,jfid,(jdouble)a->mphase);
    jfid = (*env)->GetFieldID(env,jcl,"mphased","D");
    (*env)->SetDoubleField(env,jthis,jfid,(jdouble)a->mphased);

    jmid = (*env)->GetMethodID(env,jcl,"makePlanets","(I[I[I)V");
    if(jmid!=0) {
        int i2[] = { h->isun,h->imoon,h->imer,h->iven,h->imar,h->ijup,h->isat,h->iura,h->inep,h->iplu,h->iasc,h->imc,h->irulpl,h->irulh };
        jintArray ji1 = (*env)->NewIntArray(env,ASTRO_NUM_PLANET_POINTS);
        jintArray ji2 = (*env)->NewIntArray(env,14);
        (*env)->SetIntArrayRegion(env,ji1,0,ASTRO_NUM_PLANET_POINTS,h->iplanets);
        (*env)->SetIntArrayRegion(env,ji2,0,14,i2);
        (*env)->CallVoidMethod(env,jthis,jmid,(jint)h->nplanets,ji1,ji2);
    }

    jmid = (*env)->GetMethodID(env,jcl,"setPlanet","(IIIDIIZ)V");
    if(jmid!=0)
        for(i=0; i<h->nplanets; ++i) {
            p1 = &a->planets[i];
            p2 = &h->planets[i];
            r = p2->id!=ASTRO_ASCENDANT && p2->id!=ASTRO_MC? (p1->speed<0.0) : 0;
            (*env)->CallVoidMethod(env,jthis,jmid,
                                   (jint)i,(jint)p2->id,(jint)p2->sign,
                                   (jdouble)p2->lon,(jint)p2->house,
                                   (jint)p2->factors,(jboolean)r);
        }

    jmid = (*env)->GetMethodID(env,jcl,"setHouse","(IIDD)V");
    if(jmid!=0)
        for(i=0; i<12; ++i) {
            h2 = &h->houses[i];
            (*env)->CallVoidMethod(env,jthis,jmid,(jint)i,(jint)h2->sign,(jdouble)h2->cusp,(jdouble)h2->decl);
        }

    jmid = (*env)->GetMethodID(env,jcl,"setAspect","(IIID)V");
    if(jmid!=0)
        for(x=0; x<h->nplanets; ++x)
            for(y=x+1; y<h->nplanets; ++y) {
                a2 = &h->aspects[x+y*h->nplanets];
                if(a2->type!=-1) {
                    (*env)->CallVoidMethod(env,jthis,jmid,(jint)x,(jint)y,(jint)a2->type,(jdouble)a2->orb);
                }
            }

    jmid = (*env)->GetMethodID(env,jcl,"makeAspectPatterns","(I)V");
    if(jmid!=0) (*env)->CallVoidMethod(env,jthis,jmid,(jint)h->nasppat);

    jmid = (*env)->GetMethodID(env,jcl,"setAspectPattern","(II[I)V");
    if(jmid!=0) {
        int p3[h->nplanets];
        jintArray jp3;
        for(i=0; i<h->nasppat; ++i) {
            ap = &h->asppat[i];
            for(x=0,n=0; x<ap->nplanets; ++x)
                for(j=0; x*32+j<h->nplanets && j<32; ++j)
                    if(ap->planets[x]&(1<<j)) {
                        p3[n++] = h->planets[x*32+j].id;
                    }
            jp3 = (*env)->NewIntArray(env,n);
            (*env)->SetIntArrayRegion(env,jp3,0,n,p3);
            (*env)->CallVoidMethod(env,jthis,jmid,(jint)i,(jint)ap->type,jp3);
        }
    }

    gettimeofday(&tv,NULL);
    t3 += tv.tv_usec-usec;

    jfid = (*env)->GetFieldID(env,jcl,"calculatePlanetsTime","I");
    (*env)->SetIntField(env,jthis,jfid,t1);
    jfid = (*env)->GetFieldID(env,jcl,"castHoroscopeTime","I");
    (*env)->SetIntField(env,jthis,jfid,t2);
    jfid = (*env)->GetFieldID(env,jcl,"transferNativeDataTime","I");
    (*env)->SetIntField(env,jthis,jfid,t3);
*/

    {
        int *ui,x1,y1,r1;
        double *ud;

        gettimeofday(&tv,NULL);
        usec = tv.tv_usec;

        for(x1=0,n=0; x1<h->nplanets; ++x1)
            for(y1=x1+1; y1<h->nplanets; ++y1) ++n;

        for(x1=0,r=0; x1<h->nasppat; ++x1,r+=2+r1) {
            ap = &h->asppat[x1];
            for(y1=0,r1=0; y1<ap->nplanets; ++y1)
                for(j=0; y1*32+j<h->nplanets && j<32; ++j)
                    if(ap->planets[y1]&(1<<j))
                        ++r1;
//debug_output("JNI[%d]: r1: %d\n",x1,r1);
        }

        x = 11+ASTRO_NUM_PLANET_POINTS+14+h->nplanets*5+12+n+r+3;
        ui = (int *)malloc(x*sizeof(int));

        y = 6+h->nplanets+12*2+n;
        ud = (double *)malloc(y*sizeof(double));

//debug_output("JNI: x: %d, y: %d, n: %d, r: %d\n",x,y,n,r);

        x = 0,y = 0;
        ud[y+0]  = h->gmt.jd;
        ui[x+0]  = h->gmt.year;
        ui[x+1]  = h->gmt.month;
        ui[x+2]  = h->gmt.day;
        ui[x+3]  = h->gmt.hour;
        ui[x+4]  = h->gmt.minute;
        ud[y+1]  = h->gmt.second;
        ui[x+5]  = h->gmt.type;
        x += 6;
        y += 2;

        ud[y+0]  = a->sdrlt;
        ud[y+1]  = a->deltat;
        ui[x+0]  = h->style;
        ui[x+1]  = h->hsystem;
        x += 2;
        y += 2;

        ud[y+0]  = a->mphase;
        ud[y+1]  = a->mphased;
        y += 2;

        ui[x+0]  = h->nplanets;
        ui[x+1]  = h->nasppat;
        ui[x+2]  = ASTRO_NUM_PLANET_POINTS;
        for(x1=0; x1<ASTRO_NUM_PLANET_POINTS; ++x1)
            ui[x+3+x1] = h->iplanets[x1];
        x += 3+x1;

        ui[x+0]   = h->isun;
        ui[x+1]   = h->imoon;
        ui[x+2]   = h->imer;
        ui[x+3]   = h->iven;
        ui[x+4]   = h->imar;
        ui[x+5]   = h->ijup;
        ui[x+6]   = h->isat;
        ui[x+7]   = h->iura;
        ui[x+8]   = h->inep;
        ui[x+9]   = h->iplu;
        ui[x+10]  = h->iasc;
        ui[x+11]  = h->imc;
        ui[x+12]  = h->irulpl;
        ui[x+13]  = h->irulh;
        x += 14;

        for(x1=0; x1<h->nplanets; ++x1,x+=5,y+=1) {
            p1 = &a->planets[x1];
            p2 = &h->planets[x1];
            r = p2->id!=ASTRO_ASCENDANT && p2->id!=ASTRO_MC? (p1->speed<0.0) : 0;
            ui[x+0]  = p2->id;
            ui[x+1]  = p2->sign;
            ud[y+0]  = p2->lon;
            ui[x+2]  = p2->house;
            ui[x+3]  = p2->factors;
            ui[x+4]  = r;
        }

        for(x1=0; x1<12; ++x1,x+=1,y+=2) {
            h2 = &h->houses[x1];
            ui[x+0]  = h2->sign;
            ud[y+0]  = h2->cusp;
            ud[y+1]  = h2->decl;
        }

        for(x1=0; x1<h->nplanets; ++x1)
            for(y1=x1+1; y1<h->nplanets; ++y1,++x,++y) {
                a2 = &h->aspects[x1+y1*h->nplanets];
                ui[x+0]  = a2->type;
                ud[y+0]  = a2->orb;
            }

//debug_output("JNI: x: %d, y: %d\n",x,y);

        for(x1=0,r1=0; x1<h->nasppat; ++x1,x+=2+r1) {
            ap = &h->asppat[x1];
            for(y1=0,r1=0; y1<ap->nplanets; ++y1)
                for(j=0; y1*32+j<h->nplanets && j<32; ++j)
                    if(ap->planets[y1]&(1<<j)) {
                        ui[x+2+r1] = h->planets[y1*32+j].id;
                        ++r1;
                    }
            ui[x+0]  = ap->type;
            ui[x+1]  = r1;
//debug_output("JNI[%d]: x: %d, y: %d, r1: %d\n",x1,x,y,r1);
        }
        gettimeofday(&tv,NULL);
        t3 += tv.tv_usec-usec;

        ui[x+0]  = t1;
        ui[x+1]  = t2;
        ui[x+2]  = t3;
        x += 3;
//debug_output("JNI: x: %d, y: %d\n",x,y);

        jmid = (*env)->GetMethodID(env,jcl,"update","([I[D)V");
//debug_output("JNI: jmid: %p\n",jmid);
        if(jmid!=0) {
            jintArray jui     = (*env)->NewIntArray(env,x);
            jdoubleArray jud  = (*env)->NewDoubleArray(env,y);
            (*env)->SetIntArrayRegion(env,jui,0,x,ui);
            (*env)->SetDoubleArrayRegion(env,jud,0,y,ud);
            (*env)->CallVoidMethod(env,jthis,jmid,jui,jud);
        }
        free(ui);
        free(ud);
    }

    horoscope_delete(h);
    astronomy_delete(a);

    if(planet_ids!=default_planet_ids) free(planet_ids);
    if(aspect_orbs!=default_aspect_orbs) free(aspect_orbs);
}

