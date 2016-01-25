package src.handledb.createDb;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import src.handledb.GeometryParsePostgres;
import src.handledb.GeometryTemplete;
import src.handledb.HandleDbTemplateSuper;



/**
 * データベースにデータを格納する
 * @author murase
 *
 */
public class CreateDb extends HandleDbTemplateSuper{
	public static final String DBNAME = "osm_relation_db_v2";
	//public static final String TBNAME = "osm_all_traffic_signal";
	public static final String USER = "postgres";
	public static final String PASS = "murase";
	public static final String URL = "localhost";//"rain2.elcom.nitech.ac.jp";
	public static final int PORT = 5432;
	public static final String DBURL = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME;
	
	
	public CreateDb(){
		super(DBNAME, USER, PASS, DBURL, HandleDbTemplateSuper.POSTGRESJDBCDRIVER_STRING);
	}
	
	/**
	 * テーブルの存在有無
	 * @param aTableName
	 * @param aHandleDbTemplate
	 * @return
	 */
	private boolean isTableExist(String aTableName){
		
		
		boolean bool = false;
		
		try{
			String statement = "SELECT relname FROM pg_class WHERE relkind = \'r\' AND relname = \'"+aTableName+"\'";
			ResultSet rs = execute(statement);
			while(rs.next()){
				return true;
			}
			return false;
		}catch(Exception e){
			e.printStackTrace();
		}
		return bool;
	}
	
	/**
	 * テーブル作成
	 * @param aMeshCode
	 */
	public void createTable(){
		createStrokeAndLooproadTable("stroke_and_looproad_table");
		createLooproadAndFacilityTable("looproad_and_facility_table");
		createStrokeAndFacilityTable("stroke_and_facility_table");
		createFatStrokeTable("fatstroke_table");
	}
	
	// fatstroke作成に必要な各テーブルの作成.
	/**
	 * 周回道路と施設データの関係テーブル
	 * @param aTableName
	 */
	private void createLooproadAndFacilityTable(String aTableName){
		try{
			String command = "create table if not exists "+aTableName+""+
					" (id serial not null primary key ," +
					" looproad_id int, "+
					" facility_id int, "+
					" category text"+
					");" +
					"" +
					" create index "+aTableName+"_looproad_id on "+aTableName+" using btree(looproad_id); "+
					" create index "+aTableName+"_facility_id on "+aTableName+" using btree(facility_id); "+
					" create index "+aTableName+"_category on "+aTableName+" using btree(category); ";
			System.out.println(command);
			insertInto(command);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * データの格納
	 * @param looproad_id
	 * @param facility_id
	 * @param category
	 */
	public void insertLooproadAndFacilityTable(int looproad_id, int facility_id, String category){
		try {
			String command = "insert into " +
					"looproad_and_facility_table"+
				" (looproad_id, facility_id, category) "+
				" values(" + 
					""+looproad_id+"," +
					""+facility_id+"," +
					"'"+category+"'" +
				"); ";
			System.out.println(command);
			insertInto(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * ストロークと周回道路の関係テーブル
	 * @param aTableName
	 */
	private void createStrokeAndLooproadTable(String aTableName){
		try{
			String command = "create table if not exists "+aTableName+""+
					" (id serial not null primary key ," +
					" stroke_id int, "+
					" looproad_id int "+
					");" +
					"" +
					" create index "+aTableName+"_stroke_id on "+aTableName+" using btree(stroke_id); "+
					" create index "+aTableName+"_looproad_id on "+aTableName+" using btree(looproad_id); ";
			System.out.println(command);
			insertInto(command);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * ストロークと周回道路の関係テーブルにデータを格納
	 */
	public void insertStrokeAndLooproadTable(int stroke_id, int looproad_id){
		try {
			String command = "insert into " +
					"stroke_and_looproad_table"+
				" (stroke_id, looproad_id) "+
				" values(" + 
					""+stroke_id+", "+
					""+looproad_id+"" +
				"); ";
			System.out.println(command);
			insertInto(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * ストロークと施設データの関係テーブル
	 */
	private void createStrokeAndFacilityTable(String aTableName){
		try{
			String command = "create table if not exists "+aTableName+""+
					" (id serial not null primary key ," +
					" stroke_id int, "+
					" facility_id int, "+
					" category text"+
					");" +
					"" +
					" create index "+aTableName+"_stroke_id on "+aTableName+" using btree(stroke_id); "+
					" create index "+aTableName+"_facility_id on "+aTableName+" using btree(facility_id); "+
					" create index "+aTableName+"_category on "+aTableName+" using btree(category); ";
			System.out.println(command);
			insertInto(command);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * ストロークと施設データの関係テーブルにデータを格納
	 */
	public void insertStrokeAndFacilityTable(int stroke_id, int facility_id, String category){
		try {
			String command = "insert into " +
					"stroke_and_facility_table"+
				" (stroke_id, facility_id, category) "+
				" values(" + 
					""+stroke_id+", "+
					""+facility_id+"," +
					"'"+category+"'"+
				"); ";
			System.out.println(command);
			insertInto(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * fatstrokeテーブル
	 */
	private void createFatStrokeTable(String aTableName){
		try{
			String command = "create table if not exists "+aTableName+""+
					" (id serial not null primary key ," +
					" stroke_id int, "+
					" stroke_length double precision, "+
					" facility_num int,"+
					" category text,"+
					" stroke geometry(linestring, "+GeometryTemplete.SRID_wd+") "+
					");" +
					"" +
					" create index "+aTableName+"_stroke_id on "+aTableName+" using btree(stroke_id); "+
					" create index "+aTableName+"_category on "+aTableName+" using btree(category); ";
			System.out.println(command);
			insertInto(command);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * fatstrokeのインサート
	 * @param stroke_id
	 * @param stroke_length
	 * @param facility_num
	 * @param category
	 * @param strokeWkt
	 */
	public void insertFatStrokeData(int stroke_id, double stroke_length, int facility_num, String category, String strokeWkt){
		try{
			String command = "insert into " +
					"fatstroke_table"+
				" (stroke_id, stroke_length, facility_num, category, stroke) "+
				" values(" + 
					""+stroke_id+", "+
					""+stroke_length+"," +
					""+facility_num+","+
					"'"+category+"',"+
					"st_geomFromText('"+strokeWkt+"', 4326)"+
				"); ";
			System.out.println(command);
			insertInto(command);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
}
