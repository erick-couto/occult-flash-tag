package br.eti.erickcouto.occultflashtag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class DBAdapter {


    public static final String TABLE_EVENT = "event";
    public static final String COLUMN_EVENT_ID = "_id";
    public static final String COLUMN_EVENT_BODY1 = "body1";
    public static final String COLUMN_EVENT_BODY2 = "body2";
    public static final String COLUMN_EVENT_TYPE = "type";
    public static final String COLUMN_EVENT_ALTITUDE = "altitude";
    public static final String COLUMN_EVENT_LATITUDE = "latitude";
    public static final String COLUMN_EVENT_LONGITUDE = "longitude";
    public static final String COLUMN_EVENT_DATE = "date";
    public static final String COLUMN_EVENT_STATUS = "status";
    public static final String COLUMN_EVENT_SYNCED = "synced";
    public static final String COLUMN_EVENT_NOTE = "note";


    public static final String TABLE_MARK = "mark";
    public static final String COLUMN_MARK_ID = "id";
    public static final String COLUMN_MARK_EVENT = "event";
    public static final String COLUMN_MARK_ELAPSEDTIME = "elapsed";
    public static final String COLUMN_MARK_AUDITEDTIME = "audited";
    public static final String COLUMN_MARK_BOOTCOUNTER = "booted";



    private static final String DATABASE_NAME = "occultflashtag";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_DROP_EVENT = "drop table IF EXISTS " + TABLE_EVENT;
    private static final String DATABASE_DROP_MARK = "drop table IF EXISTS " + TABLE_MARK;

    private static final String DATABASE_CREATE_EVENT = "create table IF NOT EXISTS " +
            TABLE_EVENT + " (" + COLUMN_EVENT_ID + " integer primary key autoincrement, " +
            COLUMN_EVENT_BODY1 + " text , " +
            COLUMN_EVENT_BODY2 + " text , " +
            COLUMN_EVENT_TYPE + " text , " +
            COLUMN_EVENT_ALTITUDE + " real , " +
            COLUMN_EVENT_LATITUDE + " real , " +
            COLUMN_EVENT_LONGITUDE + " real , " +
            COLUMN_EVENT_DATE + " integer not null , " +
            COLUMN_EVENT_STATUS + " text , " +
            COLUMN_EVENT_SYNCED + " boolean default 0 check(" + COLUMN_EVENT_SYNCED + " in (0,1))  , " +
            COLUMN_EVENT_NOTE + " text  " +
            ");";

    private static final String DATABASE_CREATE_MARK = "create table IF NOT EXISTS " +
            TABLE_MARK + " (" + COLUMN_MARK_ID + " integer primary key autoincrement, " +
            COLUMN_MARK_EVENT + " integer not null, " +
            COLUMN_MARK_ELAPSEDTIME + " text not null, " +
            COLUMN_MARK_AUDITEDTIME + " text, " +
            COLUMN_MARK_BOOTCOUNTER + " integer not null " +
            ");";

    private final Context context;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_EVENT);
            db.execSQL(DATABASE_CREATE_MARK);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DATABASE_DROP_EVENT);
            db.execSQL(DATABASE_DROP_MARK);

            db.execSQL(DATABASE_CREATE_EVENT);
            db.execSQL(DATABASE_CREATE_MARK);
        }
    }

    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        DBHelper.close();
    }


    public List<Mark> getAllUnauditedsMarks(int activeBootCount){
        ArrayList<Mark> marks = new ArrayList<Mark>();

        try {
            open();
            Cursor cu = getAllUnauditedMarksCursor(activeBootCount);
            if (cu.moveToFirst()){
               do {
                    Mark mark = new Mark(cu.getLong(0));
                    mark.setEventId(cu.getLong(1));
                    mark.setElapsedTime(cu.getLong(2));
                    mark.setBootCounter(cu.getInt(4));
                    marks.add(mark);
                } while (cu.moveToNext());
            }
            cu.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return marks;
    }

    public List<Mark> getAllMarksByEvent(Long idEvent){
        ArrayList<Mark> marks = new ArrayList<Mark>();

        try {
            open();
            Cursor cu = getAllMarksByEventCursor(idEvent);
            if (cu.moveToFirst()){
                do {
                    Mark mark = new Mark(cu.getLong(0));
                    mark.setEventId(cu.getLong(1));
                    mark.setElapsedTime(cu.getLong(2));
                    mark.setAuditedTime(cu.getLong(3));
                    mark.setBootCounter(cu.getInt(4));
                    marks.add(mark);
                } while (cu.moveToNext());
            }
            cu.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return marks;
    }

    public List<Mark> getAllErrorMarks(int activeBootCount){
        ArrayList<Mark> marks = new ArrayList<Mark>();

        try {
            open();
            Cursor cu = getAllErrorMarksCursor(activeBootCount);
            if (cu.moveToFirst()){
                do {
                    Mark mark = new Mark(cu.getLong(0));
                    mark.setEventId(cu.getLong(1));
                    mark.setElapsedTime(cu.getLong(2));
                    mark.setBootCounter(cu.getInt(4));
                    marks.add(mark);
                } while (cu.moveToNext());
            }
            cu.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return marks;
    }



    private Cursor getAllUnauditedMarksCursor(int activeBootCounter) {

        String query = "SELECT "
                + TABLE_MARK + "." + COLUMN_MARK_ID + ", "
                + TABLE_MARK + "." + COLUMN_MARK_EVENT + ", "
                + TABLE_MARK + "." + COLUMN_MARK_ELAPSEDTIME + ", "
                + TABLE_MARK + "." + COLUMN_MARK_AUDITEDTIME + ", "
                + TABLE_MARK + "." + COLUMN_MARK_BOOTCOUNTER
                + " FROM " + TABLE_MARK
                + " WHERE " + TABLE_MARK + "." + COLUMN_MARK_AUDITEDTIME + " IS NULL "
                + " AND " + TABLE_MARK + "." + COLUMN_MARK_BOOTCOUNTER + " = ? ";

        return db.rawQuery(query, new String[]{String.valueOf(activeBootCounter)});
    }

    private Cursor getAllErrorMarksCursor(int activeBootCounter) {

        String query = "SELECT "
                + TABLE_MARK + "." + COLUMN_MARK_ID + ", "
                + TABLE_MARK + "." + COLUMN_MARK_EVENT + ", "
                + TABLE_MARK + "." + COLUMN_MARK_ELAPSEDTIME + ", "
                + TABLE_MARK + "." + COLUMN_MARK_AUDITEDTIME + ", "
                + TABLE_MARK + "." + COLUMN_MARK_BOOTCOUNTER
                + " FROM " + TABLE_MARK
                + " WHERE " + TABLE_MARK + "." + COLUMN_MARK_AUDITEDTIME + " IS NULL "
                + " AND " + TABLE_MARK + "." + COLUMN_MARK_BOOTCOUNTER + " <> ? ";

        return db.rawQuery(query, new String[]{String.valueOf(activeBootCounter)});
    }


    private Cursor getAllMarksByEventCursor(Long idEvent) {

        String query = "SELECT "
                + TABLE_MARK + "." + COLUMN_MARK_ID + ", "
                + TABLE_MARK + "." + COLUMN_MARK_EVENT + ", "
                + TABLE_MARK + "." + COLUMN_MARK_ELAPSEDTIME + ", "
                + TABLE_MARK + "." + COLUMN_MARK_AUDITEDTIME + ", "
                + TABLE_MARK + "." + COLUMN_MARK_BOOTCOUNTER
                + " FROM " + TABLE_MARK
                + " WHERE " + TABLE_MARK + "." + COLUMN_MARK_EVENT + " = ? ";

        return db.rawQuery(query, new String[]{String.valueOf(idEvent)});
    }



    public void markEventAsSynced(Long idEvent){

        try {
            open();

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_EVENT_SYNCED, 1);
            db.update(TABLE_EVENT, cv, COLUMN_EVENT_ID + "=" + idEvent, null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }

    }

    public void markEventAsError(Long idEvent){

        try {
            open();

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_EVENT_STATUS, "ER");
            db.update(TABLE_EVENT, cv, COLUMN_EVENT_ID + "=" + idEvent, null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }

    }

    public void deleteEvent(Long idEvent){

        try {
            open();
            db.delete(TABLE_MARK,COLUMN_MARK_EVENT + "=" + idEvent, null);
            db.delete(TABLE_EVENT,COLUMN_EVENT_ID + "=" + idEvent, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }

    }


    public void markEventAsProduction(Long idEvent){

        try {
            open();

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_EVENT_STATUS, "PR");
            db.update(TABLE_EVENT, cv, COLUMN_EVENT_ID + "=" + idEvent, null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }

    }

    public void markEventAsDefault(Long idEvent){

        try {
            open();

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_EVENT_STATUS, (byte[]) null);
            db.update(TABLE_EVENT, cv, COLUMN_EVENT_ID + "=" + idEvent, null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }

    }


    public void storeAuditedTime(Long idMark, Long auditedTime){

        try {
            open();

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_MARK_AUDITEDTIME, auditedTime);
            db.update(TABLE_MARK, cv, COLUMN_MARK_ID + "=" + idMark, null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }

    }

    private Cursor getAllEventsCursor() {

        String query = "SELECT "
                + TABLE_EVENT + "." + COLUMN_EVENT_ID + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_DATE + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_BODY1 + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_BODY2 + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_TYPE + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_ALTITUDE + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_LATITUDE + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_LONGITUDE + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_STATUS + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_SYNCED + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_NOTE
                + " FROM " + TABLE_EVENT
                + " ORDER BY " + TABLE_EVENT + "." + COLUMN_EVENT_DATE + " desc ";

        return db.rawQuery(query, null);
    }

    private Cursor getEventCursor(String id) {

        String query = "SELECT "
                + TABLE_EVENT + "." + COLUMN_EVENT_ID + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_DATE + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_BODY1 + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_BODY2 + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_TYPE + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_ALTITUDE + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_LATITUDE + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_LONGITUDE + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_STATUS + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_SYNCED + ", "
                + TABLE_EVENT + "." + COLUMN_EVENT_NOTE
                + " FROM " + TABLE_EVENT
                + " WHERE " + COLUMN_EVENT_ID + " = ? ";

            return db.rawQuery(query, new String[]{id});

    }


    private Cursor getMarksCursorByEvent(Long eventId) {

        String query = "SELECT "
                + TABLE_MARK + "." + COLUMN_MARK_ID + ", "
                + TABLE_MARK + "." + COLUMN_MARK_EVENT + ", "
                + TABLE_MARK + "." + COLUMN_MARK_ELAPSEDTIME + ", "
                + TABLE_MARK + "." + COLUMN_MARK_AUDITEDTIME + ", "
                + TABLE_MARK + "." + COLUMN_MARK_BOOTCOUNTER
                + " FROM " + TABLE_MARK
                + " WHERE " + COLUMN_MARK_EVENT + " = ? "
                + " ORDER BY " + TABLE_MARK + "." + COLUMN_MARK_ID;

        return db.rawQuery(query, new String[]{eventId.toString()});
    }


    public List<Event> getAllEvents(){
        ArrayList<Event> events = new ArrayList<Event>();

        Set ids = new HashSet<Integer>();

        try {
            open();
            Cursor cu = getAllEventsCursor();
            if (cu.moveToFirst()){

                do {
                    Event event = new Event();
                    event.setEventId(cu.getLong(0));
                    event.setStartDate(new Date(cu.getLong(1)));
                    event.setBody1(cu.getString(2));
                    event.setBody2(cu.getString(3));
                    event.setType(cu.getString(4));
                    event.setAltitude(cu.getDouble(5));
                    event.setLatitude(cu.getDouble(6));
                    event.setLongitude(cu.getDouble(7));
                    event.setStatus(cu.getString(8));
                    event.setSynced(cu.getInt(9) != 0 ? Boolean.TRUE : Boolean.FALSE);
                    event.setNotes(cu.getString(10));
                    events.add(event);
                } while (cu.moveToNext());

            }
            cu.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return events;
    }

    public Event getEvent(String id){

        Event event = null;

        try {
            open();
            Cursor cu = getEventCursor(id);
            if (cu.moveToFirst()){

                do {
                    event = new Event();
                    event.setEventId(cu.getLong(0));
                    event.setStartDate(new Date(cu.getLong(1)));
                    event.setBody1(cu.getString(2));
                    event.setBody2(cu.getString(3));
                    event.setType(cu.getString(4));
                    event.setAltitude(cu.getDouble(5));
                    event.setLatitude(cu.getDouble(6));
                    event.setLongitude(cu.getDouble(7));
                    event.setStatus(cu.getString(8));
                    event.setSynced(cu.getInt(9) != 0 ? Boolean.TRUE : Boolean.FALSE);
                    event.setNotes(cu.getString(10));
                } while (cu.moveToNext());

            }
            cu.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return event;
    }



    public boolean addEvent(Event event, int activeBootCounter){
        try {
            open();

            ContentValues initialValues = new ContentValues();

            initialValues.put(COLUMN_EVENT_DATE, event.getStartDate().getTime());
            initialValues.put(COLUMN_EVENT_BODY1, event.getBody1());
            initialValues.put(COLUMN_EVENT_BODY2, event.getBody2());
            initialValues.put(COLUMN_EVENT_TYPE, event.getType());
            initialValues.put(COLUMN_EVENT_ALTITUDE, event.getAltitude());
            initialValues.put(COLUMN_EVENT_LATITUDE, event.getLatitude());
            initialValues.put(COLUMN_EVENT_LONGITUDE, event.getLongitude());
            initialValues.put(COLUMN_EVENT_NOTE, event.getNotes());

            Long idEvent = db.insert(TABLE_EVENT, null, initialValues);

            SortedSet<Long> marks = event.getAllRegisteredTimes();

            Iterator it = marks.iterator();

            while(it.hasNext()){
                Long actual = (Long) it.next();

                ContentValues markValues = new ContentValues();

                markValues.put(COLUMN_MARK_EVENT, idEvent);
                markValues.put(COLUMN_MARK_ELAPSEDTIME, actual);
                markValues.put(COLUMN_MARK_BOOTCOUNTER, activeBootCounter);

                db.insert(TABLE_MARK, null, markValues);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return false;
    }


}