package src.handledb.createDb;

import java.sql.ResultSet;
import java.util.ArrayList;

import src.handledb.HandleDbTemplateSuper;

/**
 * ストロークと施設データの関係に関する
 * @author murase
 *
 */
public class StrokeFacilityTable extends HandleDbTemplateSuper{
	private static final String DBNAME = "osm_relation_db_v2";	// Database Name
	private static final String TBNAME1 = "looproad_and_facility_table";
	private static final String TBNAME2 = "stroke_and_looproad_table";
	private static final String TBNAME3 = "stroke_and_facility_table";
	private static final String USER = "postgres";			// user name for DB.
	private static final String PASS = "murase";		// password for DB.
	private static final String URL = "localhost";
	private static final int PORT = 5432;
	private static final String DBURL = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME;

	public StrokeFacilityTable() {
		super(DBNAME, USER, PASS, DBURL, HandleDbTemplateSuper.POSTGRESJDBCDRIVER_STRING);
	}
	
	//public int _stroke_id;
	
	public ArrayList<Integer>  _strokeId;
	public ArrayList<Integer> _facilityId;
	public ArrayList<String> _category;
	/**
	 * ストロークと施設データの関係を求める
	 */
	public void strokeFacility(){
		_strokeId = new ArrayList<>();
		_facilityId = new ArrayList<>();
		_category = new ArrayList<>();
		try{
			String stmtString = "" +
					"select " +
						" stroke_id, tb1.facility_id as shop, tb1.category as category " +
					"from " +
						""+TBNAME2+" as tb2 "+" inner join "+TBNAME1+" as tb1 "+" on tb2.looproad_id = tb1.looproad_id ";
			System.out.println(stmtString);
			ResultSet rs = execute(stmtString);
			while(rs.next()){
				_strokeId.add(rs.getInt("stroke_id"));
				_facilityId.add(rs.getInt("shop"));
				_category.add(rs.getString("category"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
//	public ArrayList<Integer> _strokeId;
	public ArrayList<Integer> _facilityCount;
//	public ArrayList<String> _category;
	/**
	 * 各ストロークについてそれぞれのストロークに紐づいている施設をカテゴリ別に集計する
	 */
	public void addingUpStrokeFacility(){
		_strokeId = new ArrayList<>();
		_facilityCount = new ArrayList<>();
		_category = new ArrayList<>();
		try{
			String stmt = "" +
					" select "+
						" stroke_id, count(facility_id) as count, category"+
					" from "+
						" "+TBNAME3+""+
					" group by stroke_id, category"+
					" order by stroke_id";
			ResultSet rs = execute(stmt);
			while(rs.next()){
				_strokeId.add(rs.getInt("stroke_id"));
				_facilityCount.add(rs.getInt("count"));
				_category.add(rs.getString("category"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 指定したストロークIDとカテゴリのデータがいくつあるか求める
	 */
	public int coutnStrokeFacilityNum(int aStrokeId, String aCategory){
		try{
			String stmt = "" +
					" select "+
						" count(*) as count"+
					" from "+
						" "+TBNAME3+""+
					" where "+
						" stroke_id = "+aStrokeId+" " +
						" and " +
						" category = '"+aCategory+"' ";
			ResultSet rs = execute(stmt);
			while(rs.next()){
				return rs.getInt("count");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return -1;
	}

	
}
