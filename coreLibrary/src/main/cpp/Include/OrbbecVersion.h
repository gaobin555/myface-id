#include <stdio.h>
#include <time.h>

#define  DEADLINE

#define MONTH 6
#define DAY 30
#define YEAR 2018

#define STRINGIFY(n) STRINGIFY_HELPER(n)
#define STRINGIFY_HELPER(n) #n

#define CONNECTGIFY(n) CONNECTGIFY_HELPER(n)
#define CONNECTGIFY_HELPER(n) n

#define BRIEF_VERSION_STRING \
    STRINGIFY(MONTH) "." \
    STRINGIFY(DAY) "." \
    STRINGIFY(YEAR) 


#define BRIEF_CODE(x, y, z) BRIEF_CODE_HELPER(x, y, z)
#define BRIEF_CODE_HELPER(x, y, z)  x##y##z


#define VCODE(x, y, z) \
        BRIEF_CODE(x, y, z)

#ifdef DEADLINE
const  static char Version[] __attribute__((used, section(".DLT"))) = BRIEF_VERSION_STRING;

//const static unsigned int versionCode = VCODE(YEAR, MONTH, DAY);
//const  static unsigned int Version __attribute__((used, section(".DLT"))) = versionCode;
#endif

bool isDlt(){

#ifdef DEADLINE
  time_t pTime;
    struct tm* currentTime;
    time(&pTime);
    currentTime = localtime(&pTime);
    int year = 1900 + currentTime->tm_year;
    int mon = 1 + currentTime->tm_mon;
    int day = currentTime->tm_mday;


    bool deadline =
            (year > YEAR) ?   true :
                    (year < YEAR ?false :
                            (mon > MONTH ? true :
                                (mon < MONTH) ?false : (day > DAY ? true : false)));

    if (deadline) {
        return 1;
    }
#endif

  return 0;
}


