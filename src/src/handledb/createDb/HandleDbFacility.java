package src.handledb.createDb;

import java.awt.geom.Point2D;
import java.security.interfaces.RSAKey;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.postgis.PGgeometry;

import src.Main;
import src.handledb.GeometryParsePostgres;
import src.handledb.GeometryTemplete;
import src.handledb.HandleDbTemplateSuper;

/**
 * 施設データの処理に関する
 * @author murase
 *
 */
public class HandleDbFacility extends HandleDbTemplateSuper{
	private static final String DBNAME = "osm_facility_db";	// Database Name
	private static final String TBNAME = "facility_table";
	private static final String USER = "postgres";			// user name for DB.
	private static final String PASS = "murase";		// password for DB.
	private static final String URL = "localhost";
	private static final int PORT = 5432;
	private static final String DBURL = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME;
	
	
	
	public HandleDbFacility() {
		super(DBNAME, USER, PASS, DBURL, HandleDbTemplateSuper.POSTGRESJDBCDRIVER_STRING);
	}
	
	/** 施設ID */
	public ArrayList<Integer> _id;
	/** カテゴリ */
	public ArrayList<String> _category;
	/**
	 * 指定範囲内の施設データを取り出す
	 */
	public void getFacilityFromPolygon(String polygonWkt){
		_id = new ArrayList<>();
		_category = new ArrayList<>();
		try{
			String stmt = "" +
					"select " +
						"id, category " +
					"from " +
						""+TBNAME+" " +
					"where " +
						" st_contains(" +
							"st_geomFromText('"+polygonWkt+"' ,4326)"+
							","+
							"location) ";
			System.out.println(stmt);
			ResultSet rs = execute(stmt);
			while(rs.next()){
				_id.add(rs.getInt("id"));
				_category.add(rs.getString("category"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 施設データの種類をすべて表示
	 */
	public ArrayList<String> getAllFacilityCategory(){
		ArrayList<String> allCategory = new ArrayList<>();
		try{
			String stmt = "select distinct category from facility_table";
			ResultSet rs = execute(stmt);
			while(rs.next()){
				allCategory.add(rs.getString("category"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return allCategory;
	}
}
