package sugar.free.sightremote.adapters.history;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.descriptors.PumpStatus;
import sugar.free.sightremote.R;
import sugar.free.sightremote.database.BatteryInserted;
import sugar.free.sightremote.database.CannulaFilled;
import sugar.free.sightremote.database.CartridgeInserted;
import sugar.free.sightremote.database.PumpStatusChanged;
import sugar.free.sightremote.database.TubeFilled;
import sugar.free.sightremote.utils.HTMLUtil;
import sugar.free.sightremote.utils.UnitFormatter;

public class SystemAdapter extends HistoryAdapter<SystemAdapter.ViewHolder, Object> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = 0;
        switch (viewType) {
            case 0:
                layout = R.layout.adapter_pump_started;
                break;
            case 1:
                layout = R.layout.adapter_pump_paused;
                break;
            case 2:
                layout = R.layout.adapter_pump_stopped;
                break;
            case 3:
                layout = R.layout.adapter_cartridge_inserted;
                break;
            case 4:
                layout = R.layout.adapter_tube_filled;
                break;
            case 5:
                layout = R.layout.adapter_cannula_filled;
                break;
            case 6:
                layout = R.layout.adapter_battery_inserted;
                break;
        }
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new SystemAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Object entry, int position) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(holder.dateTime.getResources().getString(R.string.history_date_time_formatter));
        if (entry instanceof PumpStatusChanged) {
            PumpStatusChanged pumpStatusChanged = (PumpStatusChanged) entry;
            holder.dateTime.setText(simpleDateFormat.format(pumpStatusChanged.getDateTime()));
        } else if (entry instanceof CartridgeInserted) {
            CartridgeInserted cartridgeInserted = (CartridgeInserted) entry;
            holder.dateTime.setText(simpleDateFormat.format(cartridgeInserted.getDateTime()));
            holder.amount.setText(HTMLUtil.getHTML(R.string.cartridge_inserted, UnitFormatter.formatUnits(cartridgeInserted.getAmount())));
        } else if (entry instanceof TubeFilled) {
            TubeFilled tubeFilled = (TubeFilled) entry;
            holder.dateTime.setText(simpleDateFormat.format(tubeFilled.getDateTime()));
            holder.amount.setText(HTMLUtil.getHTML(R.string.tube_filled, UnitFormatter.formatUnits(tubeFilled.getAmount())));
        } else if (entry instanceof CannulaFilled) {
            CannulaFilled cannulaFilled = (CannulaFilled) entry;
            holder.dateTime.setText(simpleDateFormat.format(cannulaFilled.getDateTime()));
            holder.amount.setText(HTMLUtil.getHTML(R.string.cannula_filled, UnitFormatter.formatUnits(cannulaFilled.getAmount())));
        } else if (entry instanceof BatteryInserted) {
            BatteryInserted batteryInserted = (BatteryInserted) entry;
            holder.dateTime.setText(simpleDateFormat.format(batteryInserted.getDateTime()));
        }
    }

    @Override
    public int getItemViewType(Object entry, int position) {
        if (entry instanceof PumpStatusChanged) {
            PumpStatus pumpStatus = ((PumpStatusChanged) entry).getNewValue();
            switch (pumpStatus) {
                case STARTED: return 0;
                case PAUSED: return 1;
                case STOPPED: return 2;
            }
        } else if (entry instanceof CartridgeInserted) return 3;
        else if (entry instanceof TubeFilled) return 4;
        else if (entry instanceof CannulaFilled) return 5;
        else if (entry instanceof BatteryInserted) return 6;
        return super.getItemViewType(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView dateTime;
        private TextView amount;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.date_time);
            amount = itemView.findViewById(R.id.amount);
        }
    }

}
