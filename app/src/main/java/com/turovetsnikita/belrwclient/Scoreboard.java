package com.turovetsnikita.belrwclient;

/**
 * Created by Nikita on 8.4.17.
 */

public class Scoreboard {
    String num;
    byte lines;
    String descr, tr_route, t_depart, t_arrival, numbering, way, platform, tardiness;

    Scoreboard(String num, byte lines, String descr, String tr_route, String t_depart, String t_arrival,
               String numbering, String way, String platform, String tardiness) {
        this.num = num;
        this.lines = lines;
        this.descr = descr;

        this.tr_route = tr_route;
        this.t_depart = t_depart;
        this.t_arrival = t_arrival;

        this.numbering = numbering;
        this.way = way;
        this.platform = platform;
        this.tardiness = tardiness;
    }
}
