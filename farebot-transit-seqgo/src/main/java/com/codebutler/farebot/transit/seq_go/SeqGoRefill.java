/*
 * SeqGoRefill.java
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

import android.content.res.Resources;
import androidx.annotation.NonNull;

import com.codebutler.farebot.transit.Refill;
import com.codebutler.farebot.transit.seq_go.record.SeqGoTopupRecord;
import com.google.auto.value.AutoValue;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Represents a top-up event on the Go card.
 */
@AutoValue
abstract class SeqGoRefill extends Refill {

    @NonNull
    static SeqGoRefill create(SeqGoTopupRecord topup) {
        return new AutoValue_SeqGoRefill(topup);
    }

    @Override
    public long getTimestamp() {
        return getTopup().getTimestamp().getTimeInMillis() / 1000;
    }

    @Override
    public String getAgencyName(@NonNull Resources resources) {
        return null;
    }

    @Override
    public String getShortAgencyName(@NonNull Resources resources) {
        return resources.getString(getTopup().getAutomatic()
                ? R.string.seqgo_refill_automatic
                : R.string.seqgo_refill_manual);
    }

    @Override
    public long getAmount() {
        return getTopup().getCredit();
    }

    @Override
    public String getAmountString(@NonNull Resources resources) {
        return NumberFormat.getCurrencyInstance(Locale.US).format((double) getAmount() / 100);
    }

    abstract SeqGoTopupRecord getTopup();
}
