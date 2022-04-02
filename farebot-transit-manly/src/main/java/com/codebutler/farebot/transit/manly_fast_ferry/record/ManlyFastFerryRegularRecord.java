/*
 * ManlyFastFerryRegularRecord.java
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

package com.codebutler.farebot.transit.manly_fast_ferry.record;

/**
 * Represents a "regular" type record.
 */
public class ManlyFastFerryRegularRecord extends ManlyFastFerryRecord {

    public static ManlyFastFerryRegularRecord recordFromBytes(byte[] input) {
        ManlyFastFerryRegularRecord record = null;
        if (input[0] != 0x02) {
            throw new AssertionError("Regular record must start with 0x02");
        }

        switch (input[1]) {
            case 0x02:
                record = ManlyFastFerryPurseRecord.recordFromBytes(input);
                break;
            case 0x03:
                record = ManlyFastFerryMetadataRecord.recordFromBytes(input);
                break;
            default:
                // Unknown record type
                break;
        }

        return record;
    }
}
