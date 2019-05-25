<<<<<<< Updated upstream
package com.turovetsnikita.belrwclient.fragments;

/**
 * Created by Nikita on 15.3.17.
 */

public class Route {
    String station,stop_time,stop_date,dep_time,dep_date,stop_int;
    Boolean visit;

    Route (String station,String stop_time, String stop_date,String dep_time, String dep_date,String stop_int, Boolean visit) {
        this.station = station;
        this.stop_time = stop_time;
        this.stop_date = stop_date;
        this.dep_time = dep_time;
        this.dep_date = dep_date;
        this.stop_int = stop_int;
        this.visit = visit;
    }
}
=======
package com.turovetsnikita.belrwclient.fragments;

/**
 * Created by Nikita on 15.3.17.
 */

public class Route {
    String station,stop_time,stop_date,dep_time,dep_date,stop_int,trailer_car;
    Boolean visited;

    Route (String station,String stop_time, String stop_date,String dep_time, String dep_date,String stop_int, Boolean visited, String trailer_car) {
        this.station = station;
        this.stop_time = stop_time;
        this.stop_date = stop_date;
        this.dep_time = dep_time;
        this.dep_date = dep_date;
        this.stop_int = stop_int;
        this.visited = visited;
        this.trailer_car = trailer_car;
    }
}
>>>>>>> Stashed changes
