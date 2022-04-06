package org.getlantern.firetweet.model;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;

import java.io.File;
import java.io.FileNotFoundException;

public class UploaderMediaItem implements Parcelable {

    public static final Parcelable.Creator<UploaderMediaItem> CREATOR = new Parcelable.Creator<UploaderMediaItem>() {

        @Override
        public UploaderMediaItem createFromParcel(final Parcel source) {
            return new UploaderMediaItem(source);
        }

        @Override
        public UploaderMediaItem[] newArray(final int size) {
            return new UploaderMediaItem[size];
        }
    };

    public final String path;
    public final ParcelFileDescriptor fd;
    public final long size;

    public UploaderMediaItem(final Context context, final ParcelableMediaUpdate media) throws FileNotFoundException {
        path = Uri.parse(media.uri).getPath();
        final File file = new File(path);
        fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        size = file.length();
    }

    public UploaderMediaItem(final Parcel src) {
        path = src.readString();
        fd = src.readParcelable(ParcelFileDescriptor.class.getClassLoader());
        size = src.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "MediaUpload{path=" + path + ", fd=" + fd + ", size=" + size + "}";
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(path);
        dest.writeParcelable(fd, flags);
        dest.writeLong(size);
    }

    public static UploaderMediaItem[] getFromStatusUpdate(final Context context, final ParcelableStatusUpdate status)
            throws FileNotFoundException {
        if (status.media == null) return null;
        final UploaderMediaItem[] uploaderItems = new UploaderMediaItem[status.media.length];
        for (int i = 0, j = uploaderItems.length; i < j; i++) {
            uploaderItems[i] = new UploaderMediaItem(context, status.media[i]);
        }
        return uploaderItems;
    }

}
