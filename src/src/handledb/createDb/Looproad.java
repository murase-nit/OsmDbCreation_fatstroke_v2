package src.handledb.createDb;

import java.awt.geom.Point2D;
import java.sql.ResultSet;
import java.util.ArrayList;


import org.postgis.PGgeometry;

import src.handledb.GeometryParsePostgres;
import src.handledb.HandleDbTemplateSuper;

/**
 * 周回道路に関する
 * @author murase
 *
 */
public class Looproad extends HandleDbTemplateSuper{

	protected static final String DBNAME = "osm_road_db";	// Database Name
	protected static final String SCHEMA = "looproad";
	protected static final String TBNAME = "looproad_geom";
	protected static final String USER = "postgres";			// user name for DB.
	protected static final String PASS = "usadasql";		// password for DB.
	protected static final String URL = "rain2.elcom.nitech.ac.jp";
	protected static final int PORT = 5432;
	protected static final String DBURL = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME;
	
	
	
	public Looproad() {
		super(DBNAME, USER, PASS, DBURL, HandleDbTemplateSuper.POSTGRESJDBCDRIVER_STRING);
	}

	/** id */
	public ArrayList<Integer> _areaId = new ArrayList<>();
	/** geometry */
	public ArrayList<ArrayList<Point2D>> _looproadGeom = new ArrayList<>();
	/** wkt */
	public ArrayList<String> _looproadStrings = new ArrayList<>();
	/**
	 * 指定範囲内の周回道路を取り出す
	 */
	public void getLooproadFromMBR(Point2D aUpperLeftLngLat, Point2D aLowerRightLngLat){
		_areaId = new ArrayList<>();
		_looproadGeom = new ArrayList<>();
		_looproadStrings = new ArrayList<>();
		
		try{
			String stmt = " select id, geom, st_asText(geom) as looproadString" +
									" from " +
										""+SCHEMA+"."+TBNAME+"" +
									" where " +
										"st_intersects(" +
											"st_polygonFromText(" +
												"'Polygon(("+aUpperLeftLngLat.getX() +" "+aLowerRightLngLat.getY()  +","+
															 aLowerRightLngLat.getX()+" "+ aLowerRightLngLat.getY() +","+
															 aLowerRightLngLat.getX()+" "+aUpperLeftLngLat.getY()   +","+
															 aUpperLeftLngLat.getX() +" "+aUpperLeftLngLat.getY()   +","+
															 aUpperLeftLngLat.getX() +" "+aLowerRightLngLat.getY()  +"))'," +
												""+WGS84_EPSG_CODE+")," +
											"geom)" +
										" and " +
										" st_isValid(geom);";
			ResultSet rs = execute(stmt);
			while(rs.next()){
				_areaId.add(rs.getInt("id"));
				_looproadStrings.add(rs.getString("looproadString"));
				_looproadGeom.add(GeometryParsePostgres.pgGeometryPolygon((PGgeometry)rs.getObject("geom")));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	
	
	/** 指定したストロークと接する周回道路WKT形式 */
	public ArrayList<String> touchedLooproadStrings = new ArrayList<>();
	/** 指定したストロークと線で接する周回道路ID. */
	public ArrayList<Integer> areaIdArrayList = new ArrayList<>();
	/** 接する周回道路のジオメトリ */
	public ArrayList<ArrayList<Point2D>> touchedLooproadGeom = new ArrayList<>();
	/**
	 * 一時テーブルからストロークと接する周回道路を取得する
	 * @param aStrokeString WKT形式のストローク
	 */
	public void calcNeighberLooproadFromTmpTableUsingStroke(String aStrokeString){
		touchedLooproadStrings = new ArrayList<>();
		areaIdArrayList = new ArrayList<>();
		touchedLooproadGeom = new ArrayList<>();
		try{
			String statement="";
			statement = " select " +
							" id ," +
							" geom, "+
							" st_asText(geom), "+
							" st_dimension(st_intersection(geom, st_geomFromText('"+aStrokeString+"',"+WGS84_EPSG_CODE+"))) as dimension "+
						" from " +
							" " +SCHEMA+"."+TBNAME+""+
						" where " +
						" st_isvalid(geom) "+
						" and " +
						" st_intersects(geom, st_geomFromText('"+aStrokeString+"',"+WGS84_EPSG_CODE+")) ";
			System.out.println(statement);
			ResultSet rs = execute(statement);
			while(rs.next()){
				if(rs.getInt("dimension") == 1){
					areaIdArrayList.add(rs.getInt("id"));
					touchedLooproadStrings.add(rs.getString(3));
					touchedLooproadGeom.add(GeometryParsePostgres.pgGeometryPolygon((PGgeometry)rs.getObject("geom")));
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 指定したWKTからIDを取得する
	 */
	public int getLooproadId(String geomString){
		int lrId = -1;
		try{
			String stmt = "select id from looproad.looproad_geom where st_contains(geom, st_geomFromText('"+geomString+"', 4326))";
			ResultSet rs = execute(stmt);
			if(rs.next()){
				lrId = rs.getInt("id");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return lrId;
	}
	
	
}
