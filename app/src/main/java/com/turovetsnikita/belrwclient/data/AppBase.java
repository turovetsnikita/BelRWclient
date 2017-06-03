package com.turovetsnikita.belrwclient.data;

/**
 * Created by Nikita on 1.4.17.
 */

import android.provider.BaseColumns;

public final class AppBase {

    public AppBase() {

    }

    public static final class RecentDirections implements BaseColumns {
        public final static String TABLE_NAME = "recent";
        public final static String _ID = BaseColumns._ID;
        public final static String depart = "depart";
        public final static String arrival = "arrival";
    }

    public static final class TrainCache implements BaseColumns {
        public final static String TABLE_NAME = "trains";
        public final static String _ID = BaseColumns._ID;

        public final static String num = "num";
        public final static String lines = "lines";

        public final static String tr_route = "tr_route";

        public final static String t_depart = "t_depart";
        public final static String t_travel = "t_travel";
        public final static String t_arrival = "t_arrival";

        public final static String train_seats = "train_seats";
        public final static String train_price = "train_price";

        public final static String reg = "reg";

        public final static String alldays = "alldays";

        public final static String routehref = "routehref";
        public final static String seatshref = "seatshref";


        public final static String depart_num = "depart_num";
        public final static String arrival_num = "arrival_num";
    }
/*
    public static final class RouteCache implements BaseColumns {
        public final static String TABLE_NAME = "route";
        public final static String _ID = BaseColumns._ID;

        public final static String station = "station";
        public final static String stop_time = "stop_time";
        public final static String dep_time = "dep_time";
        public final static String stop_int = "stop_int";
        public final static Integer visit = 0;
    }

    public static final class CarCache implements BaseColumns {
        public final static String TABLE_NAME = "cars";
        public final static String _ID = BaseColumns._ID;

        public final static Integer num = 0;
        public final static String type = "type";
        public final static Float price = 0f;

        public final static Integer tplaces = 0;
        public final static Integer lplaces = 0;
        public final static Integer uplaces = 0;
        public final static Integer lsplaces = 0;
        public final static Integer usplaces = 0;
    }
    */
}
