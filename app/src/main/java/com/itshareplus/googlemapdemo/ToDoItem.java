package com.itshareplus.googlemapdemo;

/**
 * Created by eeshan on 22/3/18.
 */

public class ToDoItem {
    /**
     * Item placeId
     */
//    @com.google.gson.annotations.SerializedName("placeId")
//    private String mPlaceId;

    /**
     * Item Id
     */
    @com.google.gson.annotations.SerializedName("id")
    private String mId;


    @com.google.gson.annotations.SerializedName("rating")
    private String mRating;


    @com.google.gson.annotations.SerializedName("number")
    private String mNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ToDoItem toDoItem = (ToDoItem) o;

        return getmId() != null ? getmId().equals(toDoItem.getmId()) : toDoItem.getmId() == null;
    }

    @Override
    public int hashCode() {
        return getmId() != null ? getmId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ToDoItem{" +
                "mId='" + mId + '\'' +
                ", mRating='" + mRating + '\'' +
                ", mNumber='" + mNumber + '\'' +
                '}';
    }

    public String getmNumber() {
        return mNumber;
    }

    public void setmNumber(String mNumber) {
        this.mNumber = mNumber;
    }

    public ToDoItem() {

    }


    public ToDoItem(String mId, String mRating) {
//        this.setmPlaceId(mPlaceId);
        this.setmId(mId);
        this.setmRating(mRating);
        this.setmNumber("1");
    }

    public ToDoItem(String mId, String mRating, String mNumber) {
//        this.setmPlaceId(mPlaceId);
//        this.setmId(id);
        this.setmRating(mRating);
        this.setmNumber(mNumber);
        this.setmId(mId);
    }

//    public String getmPlaceId() {
//        return mPlaceId;
//    }

//    public void setmPlaceId(String mPlaceId) {
//        this.mPlaceId = mPlaceId;
//    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmRating() {
        return mRating;
    }

    public void setmRating(String mRating) {
        this.mRating = mRating;
    }
}
