
cmake_minimum_required(VERSION 3.10.2)

project("astro")

add_library(
        astro SHARED

        main.c
        astro/astrology.c
        astro/astronomy.c
        astro/astronomy_data.c
        astro/astronomy_fixedstars.c
        astro/astronomy_orbits.c
        astro/calendar.c
        astro/horoscope.c
        astro/planets/earth404.c
        astro/planets/jupiter404.c
        astro/planets/mars404.c
        astro/planets/mercury404.c
        astro/planets/moon404.c
        astro/planets/neptune404.c
        astro/planets/pluto404.c
        astro/planets/saturn404.c
        astro/planets/uranus404.c
        astro/planets/venus404.c
        )

find_library(log-lib log)

target_link_libraries(astro m ${log-lib})
