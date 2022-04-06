package org.getlantern.firetweet.adapter;

import android.content.Context;

import org.getlantern.firetweet.model.ParcelableStatus;
import org.getlantern.firetweet.view.holder.StatusViewHolder;

import java.util.List;

/**
 * Created by mariotaku on 14/11/19.
 */
public class ParcelableStatusesAdapter extends AbsStatusesAdapter<List<ParcelableStatus>> {

    private List<ParcelableStatus> mData;

    public ParcelableStatusesAdapter(Context context, boolean compact) {
        super(context, compact);
        setHasStableIds(true);
    }

    @Override
    public boolean isGapItem(int position) {
        return getStatus(position).is_gap && position != getStatusesCount() - 1;
    }

    @Override
    protected void bindStatus(StatusViewHolder holder, int position) {
        holder.displayStatus(getStatus(position), isShowInReplyTo());
    }

    @Override
    public ParcelableStatus getStatus(int position) {
        if (position == getStatusesCount()) return null;
        return mData.get(position);
    }

    @Override
    public int getStatusesCount() {
        if (mData == null) return 0;
        return mData.size();
    }


    @Override
    public long getItemId(int position) {
        if (position == getStatusesCount()) return position;
        return mData.get(position).hashCode();
    }

    @Override
    public long getStatusId(int position) {
        if (position == getStatusesCount()) return -1;
        return mData.get(position).id;
    }

    public void setData(List<ParcelableStatus> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public List<ParcelableStatus> getData() {
        return mData;
    }

}
