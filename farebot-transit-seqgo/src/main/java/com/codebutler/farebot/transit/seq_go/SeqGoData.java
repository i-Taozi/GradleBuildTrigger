/*
 * SeqGoData.java
 *
 * This file is part of FareBot.
 * Learn more at: https://codebutler.github.io/farebot/
 *
 * Copyright (C) 2016 Eric Butler <eric@codebutler.com>
 * Copyright (C) 2016 Michael Farrell <micolous+git@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.farebot.transit.seq_go;

import com.codebutler.farebot.transit.Trip;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Constants used in Go card
 */
public final class SeqGoData {

    public static final Map<Integer, Trip.Mode> VEHICLES;

    private static final int VEHICLE_FARE_MACHINE = 1;
    private static final int VEHICLE_BUS = 4;
    private static final int VEHICLE_RAIL = 5;
    private static final int VEHICLE_FERRY = 18;

    static {
        // TODO: Gold Coast Light Rail
        VEHICLES = ImmutableMap.<Integer, Trip.Mode>builder()
                .put(VEHICLE_FARE_MACHINE, Trip.Mode.TICKET_MACHINE)
                .put(VEHICLE_RAIL, Trip.Mode.TRAIN)
                .put(VEHICLE_FERRY, Trip.Mode.FERRY)
                .put(VEHICLE_BUS, Trip.Mode.BUS)
                .build();
    }

    private SeqGoData() { }
}
